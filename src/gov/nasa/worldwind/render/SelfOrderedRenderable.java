/*
 * Copyright (C) 2018 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.render;

/**
 * a OrderedRenderable which does not need a wrapper to be added to the DrawContext
 *
 * 5/12/2018 pcm
 */
public interface SelfOrderedRenderable extends OrderedRenderable {

    /**
     * transient link references for in-situ queueing by DrawContext
     *
     * @return next object to render or null if none left
     *
     * 05/12/18 pcm
     */
    SelfOrderedRenderable getNext();

    void setNext(SelfOrderedRenderable next);

    /**
     * order in descending eye distance
     * @return -1 if own eye distance is greater than other, 1 if it is lower, 0 if equal
     */
    default int compareTo (OrderedRenderable other) {
        double dOwn = getDistanceFromEye();
        double dOther = other.getDistanceFromEye();
        if (dOwn > dOther) {
            return -1;
        } else if (dOwn < dOther) {
            return 1;
        } else {
            return 0;
        }
    }

    boolean isBehind();
    void setBehind(boolean cond);
}
