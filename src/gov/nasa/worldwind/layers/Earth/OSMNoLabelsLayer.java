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
public class OSMNoLabelsLayer extends PlainMapTileLayer {
    public OSMNoLabelsLayer() {
        super("OSMNoLabels",
                "http://tiles.wmflabs.org/osm-no-labels/",
                new Dimension(256,256),
                "Earth/OSM-Mercator/NoLabels",
                ".png");
    }
}
