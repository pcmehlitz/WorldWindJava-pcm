/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * base type for plain map tile layer
 * added to avoid too much code duplication (PCM)
 * TODO - this should be configured from a data file (in analogy to WMS)
 */
public class PlainMapTileLayer extends BasicMercatorTiledImageLayer {

    String name;

    static class PlainMapTileURLBuilder implements TileUrlBuilder {
        String fileExtension;

        PlainMapTileURLBuilder (String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public URL getURL(Tile tile, String imageFormat) throws MalformedURLException {
            return new URL(tile.getLevel().getService()
                    + (tile.getLevelNumber() + 3) + "/" + tile.getColumn() + "/"
                    + ((1 << (tile.getLevelNumber()) + 3) - 1 - tile.getRow()) + fileExtension);
        }
    }

    public PlainMapTileLayer (String name, String serviceURL, Dimension tileSize, String cacheDir, String fileExtension) {
        super( createLevelSet(name, serviceURL, tileSize, cacheDir, fileExtension));
        this.name = name;
    }

    public PlainMapTileLayer(String name, LevelSet levelSet) {
        super(levelSet);
        this.name = name;
    }

    @Override
    public String toString() { return name; }

    static LevelSet createLevelSet (String name, String serviceURL, Dimension tileSize,
                                    String cacheDir, String fileExtension) {
        AVList params = new AVListImpl();

        params.setValue(AVKey.SERVICE, serviceURL);
        params.setValue(AVKey.TILE_WIDTH, tileSize.width);
        params.setValue(AVKey.TILE_HEIGHT, tileSize.height);
        params.setValue(AVKey.DATA_CACHE_NAME, cacheDir);

        params.setValue(AVKey.TILE_URL_BUILDER, new PlainMapTileURLBuilder(fileExtension));

        // not sure about these
        params.setValue(AVKey.DATASET_NAME, "h");
        params.setValue(AVKey.FORMAT_SUFFIX, fileExtension);
        params.setValue(AVKey.NUM_LEVELS, 20);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle
                .fromDegrees(22.5d), Angle.fromDegrees(45d)));
        params.setValue(AVKey.SECTOR, new MercatorSector(-1.0, 1.0, Angle.NEG180, Angle.POS180));

        return new LevelSet(params);
    }
}
