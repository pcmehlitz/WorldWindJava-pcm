/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import java.awt.*;

/**
 * Stamen.com terrain layer
 * TODO -  this should not be a hardcoded class but configuration
 */
public class StamenTerrainLayer extends PlainMapTileLayer {
    public StamenTerrainLayer() {
        super("StamenTerrain",
                "http://tile.stamen.com/terrain/",
                new Dimension(256,256),
                "Earth/OSM-Mercator/StamenTerrain",
                ".png");
        setEmptyTileSize(129); // might depend on tile size
        setFallbackColor(new Color(152,203,203)); // StamenTerrain water color
    }
}