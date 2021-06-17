package org.esa.snap.dataio.envi;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.FileUtils;
import org.esa.snap.core.util.io.SnapFileFilter;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class EnviProductReaderPlugIn implements ProductReaderPlugIn {

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        if (input instanceof ImageInputStream) {
            return checkDecodeQualificationOnStream((ImageInputStream) input);
        } else if(input != null){
            return checkDecodeQualificationOnFile(new File(input.toString()));
        }
        return DecodeQualification.UNABLE;
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{EnviConstants.FORMAT_NAME};
    }

    @Override
    public ProductReader createReaderInstance() {
        return new EnviProductReader(this);
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[]{EnviConstants.HDR_EXTENSION, EnviConstants.ZIP_EXTENSION};
    }

    @Override
    public Class[] getInputTypes() {
        return new Class[]{String.class, File.class};
    }

    @Override
    public String getDescription(Locale locale) {
        return EnviConstants.DESCRIPTION;
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null));
    }

    ///////////////////////////////////////////////////////////////////////////
    /////// END OF PUBLIC
    ///////////////////////////////////////////////////////////////////////////

    static File getInputFile(Object input) {
        return new File(input.toString());
    }


    static boolean isCompressedFile(File file) {
        return file.getPath().lastIndexOf(EnviConstants.ZIP_EXTENSION) > -1;
    }

    static InputStream getHeaderStreamFromZip(ZipFile productZip) throws IOException {
        final Enumeration entries = productZip.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry zipEntry = (ZipEntry) entries.nextElement();
            final String name = zipEntry.getName();
            if (name.indexOf(EnviConstants.HDR_EXTENSION) > 0) {
                return productZip.getInputStream(zipEntry);
            }
        }
        return null;
    }

    private static DecodeQualification checkDecodeQualificationOnStream(InputStream headerStream) {
        return checkDecodeQualificationOnStream(new MemoryCacheImageInputStream(headerStream));
    }

    private static DecodeQualification checkDecodeQualificationOnStream(ImageInputStream headerStream) {
        try {
            final String line = headerStream.readLine();
            if (line != null && line.startsWith(EnviConstants.FIRST_LINE)) {
                return DecodeQualification.SUITABLE;
            }

        } catch (IOException ignore) {
            // intentionally nothing in here tb 20080409
        }
        return DecodeQualification.UNABLE;
    }


    private static DecodeQualification checkDecodeQualificationOnFile(File inputFile) {
        try {
            final String fileName = inputFile.getName().toLowerCase();
            boolean validExt = false;
            for(String ext : EnviConstants.VALID_EXTENSIONS) {
                if(fileName.endsWith(ext)) {
                    validExt = true;
                    break;
                }
            }
            if(!validExt) return DecodeQualification.UNABLE;

            if (isCompressedFile(inputFile)) {
                final ZipFile productZip = new ZipFile(inputFile, ZipFile.OPEN_READ);

                if (productZip.size() != 2) {
                    productZip.close();
                    return DecodeQualification.UNABLE;
                }

                final InputStream headerStream = getHeaderStreamFromZip(productZip);
                if (headerStream != null) {
                    final DecodeQualification result = checkDecodeQualificationOnStream(headerStream);
                    headerStream.close();
                    productZip.close();
                    return result;
                }
                productZip.close();
            } else if (EnviConstants.HDR_EXTENSION.equalsIgnoreCase(FileUtils.getExtension(inputFile))) {
                DecodeQualification result;
                try (ImageInputStream headerStream = new FileImageInputStream(inputFile)) {
                    result = checkDecodeQualificationOnStream(headerStream);
                }
                return result;
            }
        }
        catch (IOException ignored) {
            // intentionally left empty - returns the same as the line below tb 20080409
        }
        return DecodeQualification.UNABLE;
    }
}
