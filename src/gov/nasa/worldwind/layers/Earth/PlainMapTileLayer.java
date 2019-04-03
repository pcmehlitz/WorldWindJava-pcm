/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer;
import gov.nasa.worldwind.layers.mercator.MercatorSector;
import gov.nasa.worldwind.layers.mercator.MercatorTextureTile;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * base type for plain map tile layer
 * added to avoid too much code duplication (PCM)
 * TODO - this should be configured from a data file (in analogy to WMS)
 */
public class PlainMapTileLayer extends BasicMercatorTiledImageLayer {

    protected String name;
    protected Dimension tileSize;

    protected Color fallbackColor = Color.gray;
    protected TextureData fallbackTextureData = null; // needs to be created on demand since we need a GLProfile
    protected int emptySize = 0;

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
        this.tileSize = tileSize;
    }

    public PlainMapTileLayer(String name, LevelSet levelSet) {
        super(levelSet);
        this.name = name;

        int w = (Integer) levelSet.getValue(AVKey.TILE_WIDTH);
        int h = (Integer) levelSet.getValue(AVKey.TILE_HEIGHT);
        this.tileSize = new Dimension(w,h);
    }

    @Override
    public String toString() { return name; }

    protected void setEmptyTileSize( int n) { emptySize = n; }
    protected void setFallbackColor (Color clr) { fallbackColor = clr; }

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

    // this overrides BasicMercatorTiledImageLayer to add handling of empty tiles, which
    // might just be a unicolor tile. Replace with a configurable background color tile (e.g.
    // for water, which is not covered by OpenStreetMap)
    protected boolean loadTexture(MercatorTextureTile tile, java.net.URL textureURL){
        TextureData textureData;

        synchronized (this.fileLock) {
            try {
                InputStream stream = new BufferedInputStream(textureURL.openStream());
                GLProfile glp = Configuration.getMaxCompatibleGLProfile();

                try {
                    if (stream.available() == emptySize) {
                        if (fallbackTextureData == null) {
                            fallbackTextureData = createFallbackTextureData(glp,isUseMipMaps());
                        }
                        textureData = fallbackTextureData;
                    } else {
                        textureData = OGLUtil.newTextureData(glp, stream, isUseMipMaps());
                    }
                } finally {
                    stream.close();
                }
            }
            catch (Exception e) {
                String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", textureURL.toString());
                Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);
                return false;
            }
        }

        if (textureData == null)
            return false;

        tile.setTextureData(textureData);
        if (tile.getLevelNumber() != 0 || !this.isRetainLevelZeroTiles())
            this.addTileToCache(tile);

        return true;
    }

    // this should be only called once, but we need a valid GLProfile
    protected TextureData createFallbackTextureData (GLProfile glp, boolean useMipMaps) {
        BufferedImage img = new BufferedImage(tileSize.width,tileSize.height,TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(fallbackColor);
        g.fillRect(0,0,tileSize.width,tileSize.height);
        return AWTTextureIO.newTextureData(glp, img, useMipMaps);
    }
}
