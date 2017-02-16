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
package osm.map.worldwind.gl;

import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.Movable2;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Highlightable;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.PreRenderable;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Color;
import java.awt.Point;
import com.jogamp.opengl.GL2;

public abstract class GLRenderable implements Renderable, PreRenderable, Highlightable, Movable, Movable2 {

	protected Layer pickLayer;

	protected Position position;
	protected double azimuth = 0.0;
	protected double roll = 0.0;
	protected double elevation = 0.0;
	protected double renderDistance = 50000; //do not draw if object is this far from eye
	protected boolean keepConstantSize = true;
	protected double size = 1;
	protected boolean clamp = false;
	protected boolean useLighting = true;
	protected boolean visible = true;
	protected Vec4 lightSource = new Vec4(1.0, 0.5, 1.0);
	protected double eyeDistance;
	protected double eyeDistanceOffset = 0;
	boolean drawnOnce = false;

	protected PickSupport pickSupport = new PickSupport();
	private boolean highlighted;

	public GLRenderable(Position position) {
		this.position = position;
	}

	public void clamp() {
		this.clamp = true;
	}

	@Override
	public void render(DrawContext dc) {
		if (!this.visible) {
			return;
		}

		dc.addOrderedRenderable(new OrderedGLRenderable());
	}

	protected void updateEyeDistance(DrawContext dc) {
		eyeDistance = dc.getGlobe().computePointFromPosition(position).distanceTo3(dc.getView().getEyePoint()) + eyeDistanceOffset;
	}

	public void myRender(DrawContext dc) {

		updateEyeDistance(dc);

		// If far away, don't render it
		if (eyeDistance > renderDistance) {
			return;
		}

		try {
			if (dc.isPickingMode()) {
				if (dc.getCurrentLayer() != null) {
					this.pickLayer = dc.getCurrentLayer();
				}
			}
			beginDraw(dc);
			if (dc.isPickingMode()) {
				GL2 gl = dc.getGL().getGL2();
				Color pickColor = dc.getUniquePickColor();
				pickSupport.addPickableObject(pickColor.getRGB(), this, this.position);
				gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
			}
			draw(dc);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			endDraw(dc);
		}
	}

	protected void draw(DrawContext dc) {
		GL2 gl = dc.getGL().getGL2();

		Vec4 loc;
		if (clamp) {
			loc = dc.computeTerrainPoint(position.latitude, position.longitude, 0);
		} else {
			loc = dc.getGlobe().computePointFromPosition(position, position.elevation * dc.getVerticalExaggeration());
		}
		double localSize = this.computeSize(dc, loc);

		if (dc.getView().getFrustumInModelCoordinates().contains(loc)) {
			dc.getView().pushReferenceCenter(dc, loc);
			gl.glRotated(position.getLongitude().degrees, 0, 1, 0);
			gl.glRotated(-position.getLatitude().degrees, 1, 0, 0);
			gl.glRotated(-azimuth, 0, 0, 1);
			gl.glRotated(elevation, 1, 0, 0);
			gl.glRotated(roll, 0, 1, 0);
			gl.glScaled(localSize, localSize, localSize);
			drawGL(dc);
			dc.getView().popReferenceCenter(dc);
		}
	}

	protected abstract void drawGL(DrawContext dc);

	// puts opengl in the correct state for this layer
	protected void beginDraw(DrawContext dc) {
		GL2 gl = dc.getGL().getGL2();
		gl.glPushAttrib(
			GL2.GL_TEXTURE_BIT
			| GL2.GL_DEPTH_BUFFER_BIT
			| GL2.GL_COLOR_BUFFER_BIT
			| GL2.GL_HINT_BIT
			| GL2.GL_POLYGON_BIT
			| GL2.GL_ENABLE_BIT
			| GL2.GL_CURRENT_BIT
			| GL2.GL_LIGHTING_BIT
			| GL2.GL_TRANSFORM_BIT
			| GL2.GL_CLIENT_VERTEX_ARRAY_BIT);

		if (useLighting && !dc.isPickingMode()) {
			gl.glEnable(GL2.GL_CULL_FACE);
			gl.glEnable(GL2.GL_SMOOTH);
			gl.glEnable(GL2.GL_LIGHTING);
			gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
			gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_FALSE);

			gl.glEnable(GL2.GL_LIGHT0);

			Vec4 vec = lightSource.normalize3();
			float[] params = new float[]{(float) vec.x, (float) vec.y, (float) vec.z, 0f};
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, params, 0);
			gl.glPopMatrix();
		}

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPushMatrix();
	}

	// resets opengl state
	protected void endDraw(DrawContext dc) {
		GL2 gl = dc.getGL().getGL2();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glPopMatrix();
		gl.glPopAttrib();
	}

	protected double computeSize(DrawContext dc, Vec4 loc) {
		if (this.keepConstantSize) {
			return size;
		}
		if (loc == null) {
			System.err.println("Null location when computing size");
			return 1;
		}
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double newSize = 60 * dc.getView().computePixelSizeAtDistance(d);
		if (newSize < 2) {
			newSize = 2;
		}
		return newSize;
	}

	protected final Vec4 computeTerrainPoint(DrawContext dc, Angle lat, Angle lon) {
		Vec4 p = dc.getSurfaceGeometry().getSurfacePoint(lat, lon);
		if (p == null) {
			p = dc.getGlobe().computePointFromPosition(lat, lon,
				dc.getGlobe().getElevation(lat, lon) * dc.getVerticalExaggeration());
		}
		return p;
	}

	public boolean isConstantSize() {
		return keepConstantSize;
	}

	public void setKeepConstantSize(boolean val) {
		this.keepConstantSize = val;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(double val) {
		this.azimuth = val;
	}

	public double getRoll() {
		return roll;
	}

	public void setRoll(double val) {
		this.roll = val;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double val) {
		this.elevation = val;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public void setRenderDistance(double renderDistance) {
		this.renderDistance = renderDistance;
	}

	public class OrderedGLRenderable implements OrderedRenderable {

		@Override
		public double getDistanceFromEye() {
			return GLRenderable.this.eyeDistance;
		}

		@Override
		public void pick(DrawContext dc, Point pickPoint) {
			pickSupport.clearPickList();
			try {
				pickSupport.beginPicking(dc);
				GLRenderable.this.myRender(dc);
			} finally {
				pickSupport.endPicking(dc);
				pickSupport.resolvePick(dc, pickPoint, pickLayer);
			}
		}

		@Override
		public void render(DrawContext dc) {

			GLRenderable.this.myRender(dc);
		}

	}

	@Override
	public void preRender(DrawContext dc) {
		if (dc.getCurrentLayer() != null) {
			this.pickLayer = dc.getCurrentLayer();
		}

	}

	@Override
	public boolean isHighlighted() {
		return this.highlighted;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	@Override
	public Position getReferencePosition() {
		return this.position;
	}

	@Override
	public void move(Position position) {
        Angle heading = LatLon.greatCircleAzimuth(this.getReferencePosition(),position);
        Angle pathLength = LatLon.greatCircleDistance(this.getReferencePosition(), position);
        this.position = new Position(LatLon.greatCircleEndPosition(this.position, heading, pathLength),this.position.elevation);
    }

    protected void doMoveTo(Globe globe, Position oldReferencePosition, Position newReferencePosition) {
//        List<LatLon> locations = new ArrayList<LatLon>(1);
//        locations.add(this.getCenter());
//        List<LatLon> newLocations = LatLon.computeShiftedLocations(globe, oldReferencePosition, newReferencePosition,locations); 
//        this.setCenter(newLocations.get(0));
		this.position = new Position(newReferencePosition,this.position.getElevation());
	}

	@Override
	public void moveTo(Position position) {
		this.position = position;
	}

	@Override
	public void moveTo(Globe globe, Position position) {
		this.position = new Position(position,this.position.getElevation());
	}

}
