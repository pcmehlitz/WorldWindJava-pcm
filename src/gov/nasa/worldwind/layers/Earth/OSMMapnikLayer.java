/*
 * Copyright (C) 2013 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.Earth;

import java.awt.*;

/**
 * @version $Id: OSMMapnikLayer.java 1171 2013-02-11 21:45:02Z dcollins $
 *
 * PCM - refactored to reduce code duplication
 */
public class OSMMapnikLayer extends PlainMapTileLayer {
    public OSMMapnikLayer() {
        super("OSMMapnik",
                "http://a.tile.openstreetmap.org/",
                new Dimension(256,256),
                "Earth/OSM-Mercator/Mapnik",
                ".png");
    }
}
