package org.esa.snap.engine_utilities.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.io.IOException;

/**
 * Created by jcoravu on 4/4/2019.
 */
public class ZipFileSystemBuilder {

    private static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = getZipFileSystemProvider();

    private ZipFileSystemBuilder() {
    }

    private static FileSystemProvider getZipFileSystemProvider() {
		for (FileSystemProvider fsr : FileSystemProvider.installedProviders()) {
			if (fsr.getClass().getSimpleName().equals("ZipFileSystemProvider"))
				return (FileSystemProvider) fsr;
		}
		throw new FileSystemNotFoundException("The zip file system provider is not installed!");
    }

    public static FileSystem newZipFileSystem(Path zipPath) throws IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
		if (zipPath.getFileSystem().getClass().getSimpleName().equals("ZipFileSystem")) {
			throw new IllegalArgumentException("Can't create a ZIP file system nested in a ZIP file system. (" + zipPath
					+ " is nested in " + zipPath.getFileSystem() + ")");
		}
		return ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(URI.create("jar:" + zipPath.toUri()),
				new HashMap<String, String>());
	}

    public static Path buildZipEntryPath(Path zipArchiveRoot, String zipEntryPath) {
        String fileSystemSeparator = zipArchiveRoot.getFileSystem().getSeparator();
        String childRelativePath = FileSystemUtils.replaceFileSeparator(zipEntryPath, fileSystemSeparator);

        String rootAsString = zipArchiveRoot.toString();
        if (childRelativePath.startsWith(rootAsString)) {
            return zipArchiveRoot.getFileSystem().getPath(childRelativePath);
        }
        if (childRelativePath.startsWith(fileSystemSeparator)) {
            childRelativePath = childRelativePath.substring(fileSystemSeparator.length());
        }
        return zipArchiveRoot.resolve(childRelativePath);
    }
}
