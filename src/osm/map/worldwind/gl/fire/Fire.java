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

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import gov.nasa.worldwind.util.OGLUtil;
import java.awt.Color;
import java.util.ArrayList;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

public class Fire {
    
    float radius;
    float height;
    float lifespan;
    int genRate;
    Color color = Color.RED;
    Texture texture;
    
    private ArrayList<Spark> particles;
    
    public Fire(float radius,  float height, float lifespan, int genRate) {
        this.radius = radius;
        this.height = height;
        this.lifespan = lifespan;
        this.genRate = genRate;
        
        this.particles = new ArrayList<Spark>();
        
        try {
            this.texture = TextureIO.newTexture(getClass().getResource("/osm/map/worldwind/gl/fire/smoke3.png"), false, ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.spawnParticles(1);
    }
    
    public void draw(GL2 gl) {
        gl.glEnable(GL.GL_BLEND);
        OGLUtil.applyBlending(gl, false);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glPushAttrib(GL2.GL_ENABLE_BIT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
        gl.glEnable(GL.GL_TEXTURE_2D);        
        
        for (Spark p : this.particles) {
            gl.glPushMatrix();
            gl.glColor4fv(p.getColor(), 0);
            gl.glTranslatef(p.getPos()[0], p.getPos()[1], p.getPos()[2]);            
            gl.glRotatef(p.roty, 0f, 1f, 0f);
            this.texture.bind(gl);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(-p.size/2f, 0f, 0f);
            gl.glTexCoord2f(0, 1);
            gl.glVertex3f(0, p.size, 0f);
            gl.glTexCoord2f(1, 0);
            gl.glVertex3f(p.size/2, 0f, 0f);
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(0f, 0f, -p.size/2f);
            gl.glTexCoord2f(0, 1);
            gl.glVertex3f(0, p.size, 0f);
            gl.glTexCoord2f(1, 0);
            gl.glVertex3f(0, 0f, p.size/2);
            gl.glEnd();
            gl.glPopMatrix();
        }
        
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glPopAttrib();
        gl.glEnable(GL2.GL_LIGHTING);
    }
    
    public void update(float dt) {
        float g[] = new float[] {(float) Math.random()*10-5f, 0, (float) Math.random()*10-5f };
        ArrayList<Spark> dead = new ArrayList<Spark>();
        for (Spark p : this.particles) {
            if (p.isAlive()) {
                p.move(dt, g);
                float d = p.getAge()/p.getLifeTime();
                if (d <= .9f || p.getPos()[1] < this.height*20) {
                    p.setColor(1, d);
                    p.setColor(3, 1-d);
                }
                else
                    p.setColor(new float[] {.1f, .1f, .1f, .1f});
            }
            else {
                dead.add(p);
            }  
        }
        this.particles.removeAll(dead);
        dead.clear();
        this.spawnParticles(dt);
    }
    
    private void spawnParticles(float dt) {
        float n = dt*this.genRate;
        float decimal = (int) n - n;
        n = (int) n;
        if (Math.random() < decimal)
            n+=1;
        
        for (int i=0; i<n; i++) {
            float r = (float) Math.sqrt(Math.random())*radius;
            float angle = (float) (Math.random()*Math.PI*2f);
            Spark p = new Spark( new float[]{(float) Math.cos(angle) * r, 0, (float) Math.sin(angle) * r},
                        new float[]{(float) Math.random()*2-1f, (float) Math.random() * height*6, (float) Math.random()*2-1f},
                        (float) Math.random()*height*lifespan+1f);
            p.setColor(new float[] { color.getRed() / 256f, color.getGreen() / 256f, color.getBlue() / 256f, .3f});
            this.particles.add( p );
        }
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public void setLifeSpan(float lifespan) {
        this.lifespan = lifespan;
    }
    
    public void setGenRate(int genRate) {
        this.genRate = genRate;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
}
