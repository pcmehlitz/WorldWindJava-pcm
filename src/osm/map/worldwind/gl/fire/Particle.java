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

public class Particle implements Comparable<Particle> {
        private float pos[];
        private float veloc[];
        private float age;
        private float lifeTime;
        private float color[];
        private static float viewer[];
        
        public static void setViewerPosition(float v[]) {
            Particle.viewer = v;
        }
        
        public Particle(float pos[], float veloc[],float life) {
            this.pos = pos;
            this.veloc = veloc;
            this.age = 0;
            this.lifeTime = life;
            this.color = new float[]{0f, 0f, 0f};
        }
        
        public void setVeloc(float veloc[]) {
            this.veloc = veloc;
        }
        
        public void setColor(float color[]) {
            if (color.length == 3)
                this.color = new float[] {color[0], color[1], color[2]};
            else if (color.length == 4)
                this.color = new float[] {color[0], color[1], color[2], color[3]};
        }
        
        public void reset(float pos[], float veloc[], float life) {
            this.pos = pos;
            this.veloc = veloc;
            this.lifeTime = life;
            this.age = 0f;
        }
        
        public void setColor(int i, float c) {
            this.color[i] = c;
        }
        
        public void setPos(int i, float c) {
            this.pos[i] = c;
        }
        
        public float getAge() {
            return this.age;
        }
        
        public float getLifeTime() {
            return this.lifeTime;
        }
        
        public float[] getColor() {
            if (this.color.length == 4)
                return new float[] {this.color[0], this.color[1], this.color[2], this.color[3]};
            else
                return new float[] {this.color[0], this.color[1], this.color[2]};
        }
        
        public void move(float dt, float gravity[]) {
            this.veloc[0] += gravity[0]*dt;
            this.veloc[1] += gravity[1]*dt;
            this.veloc[2] += gravity[2]*dt;
            
            this.pos[0] += this.veloc[0]*dt;
            this.pos[1] += this.veloc[1]*dt;
            this.pos[2] += this.veloc[2]*dt;
            
            age += dt;
        }
        
        public boolean isAlive() {
            return this.age <= this.lifeTime;
        }
        
        public float[] getPos() {
            return new float[] {this.pos[0], this.pos[1], this.pos[2]};
        }
        
        public float[] getVeloc() {
            return new float[] {this.veloc[0], this.veloc[1], this.veloc[2]};
        }

    public int compareTo(Particle p) {
        float pos[] = p.getPos();
        float d1 = (Particle.viewer[0]-pos[0])*(Particle.viewer[0]-pos[0])+(Particle.viewer[1]-pos[1])*(Particle.viewer[1]-pos[1])+(Particle.viewer[2]-pos[2])*(Particle.viewer[2]-pos[2]);
        float d2 = (Particle.viewer[0]-this.pos[0])*(Particle.viewer[0]-this.pos[0])+(Particle.viewer[1]-this.pos[1])*(Particle.viewer[1]-this.pos[1])+(Particle.viewer[2]-this.pos[2])*(Particle.viewer[2]-this.pos[2]);
        
        if (d1 > d2)
            return 1;
        else if (d1 < d2)
            return -1;
        else
            return 0;
    }
    
}
