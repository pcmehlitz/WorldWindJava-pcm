/*
 * Copyright (C) 2019 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import java.awt.*;

/**
 * OSM map tile layer
 * TODO -  this should not be a hardcoded class but configuration
 */
public class OSMHumanitarianLayer extends PlainMapTileLayer {
    public OSMHumanitarianLayer() {
        super("OSMHumanitarian",
                "http://tile.openstreetmap.fr/hot/",
                new Dimension(256,256),
                "Earth/OSM-Mercator/Humanitarian",
                ".png");
    }
}
