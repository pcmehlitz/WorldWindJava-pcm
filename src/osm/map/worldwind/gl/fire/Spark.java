/*
 * Copyright (C) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * this code was published by user 'robotfire' on the WorldWind forum thread
 *   https://forum.worldwindcentral.com/forum/world-wind-java-forums/development-help/17605-collada-models-with-lighting
 * By permission of its creator it is put under the same license and copyright as WorldWind.
 */

package osm.map.worldwind.gl.fire;

public class Spark extends Particle {
    
    public float roty, rotx, inc;
    public float size;
    private int type;
    
    public Spark (float pos[], float veloc[], float life) {
        super(pos, veloc, life);
        this.roty = (float) (Math.random()*360);
        this.size = (float) (Math.random()*15+10f);
        this.inc = (float) Math.random();
        this.type = 0;    
    }
    
    public int getType() {
        return this.type;
    }
    
}
