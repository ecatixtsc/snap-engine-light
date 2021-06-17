package org.esa.snap.core.gpf.common.resample;

import org.esa.snap.core.datamodel.RasterDataNode;
import org.esa.snap.core.image.ImageManager;

import java.awt.image.DataBuffer;

/**
 * Created by obarrile on 14/04/2019.
 */
public class FlagAndAggregator implements Downsampling {

    @Override
    public String getName() {
        return "FlagAnd";
    }

    @Override
    public boolean isCompatible(RasterDataNode rasterDataNode, int dataBufferType) {
        if (dataBufferType == DataBuffer.TYPE_FLOAT || dataBufferType == DataBuffer.TYPE_DOUBLE) {
            return false;
        }
        return true;
    }

    @Override
    public Aggregator createDownsampler(RasterDataNode rasterDataNode, int dataBufferType) {
        return AggregatorFactory.createAggregator(AggregationType.FlagAnd,dataBufferType);
    }

    public static class Spi extends DownsamplerSpi {
        public Spi() {
            super(FlagAndAggregator.class,"FlagAnd");
        }
    }
}