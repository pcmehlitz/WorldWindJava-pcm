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

package osm.map.worldwind.gl.obj;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import gov.nasa.worldwind.render.DrawContext;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;
import com.jogamp.opengl.GL2;
import osm.map.worldwind.gl.GLRenderable;

public class ObjRenderable extends GLRenderable {

	static Map<String, ObjLoader> modelCache = new HashMap<>();
	protected String modelSource;
	protected boolean centerit = false, flipTextureVertically = false;

	private String id;

	public ObjRenderable(Position pos, String modelSource) {
		super(pos);
		this.modelSource = modelSource;
	}

	public ObjRenderable(Position pos, String modelSource, boolean centerit, boolean flipTextureVertically) {
		super(pos);
		this.modelSource = modelSource;
		this.centerit = centerit;
		this.flipTextureVertically = flipTextureVertically;
	}

	protected ObjLoader getModel(final DrawContext dc) {
		String key = modelSource + "#" + dc.hashCode();
		if (modelCache.get(key) == null) {
			modelCache.put(key, new ObjLoader(modelSource, dc.getGL().getGL2(), centerit, flipTextureVertically));
		}
		ObjLoader model = modelCache.get(key);
		eyeDistanceOffset = Math.max(Math.max(model.getXWidth(), model.getYHeight()), model.getZDepth());
		return modelCache.get(key);
	}

	public static void reload() {
		modelCache.clear();
	}

	@Override
	protected void drawGL(DrawContext dc) {
		GL2 gl = dc.getGL().getGL2();
		gl.glRotated(90, 1, 0, 0);
		ObjLoader l = getModel(dc);
		if (dc.isPickingMode()) {
			l.getBoundingBox().drawUnitCube(dc);
		} else {
			getModel(dc).opengldraw(gl);
			if (this.isHighlighted()) {
				l.getBoundingBox().drawUnitCubeOutline(dc);
			}
		}
	}

	private double getPixelsPerMeter() {
		int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		return dpi / .0254;
	}

	@Override
	protected double computeSize(DrawContext dc, Vec4 loc) {
		if (this.keepConstantSize) {
			return size;
		}
		if (loc == null) {
			System.err.println("Null location when computing size");
			return 1;
		}
		double d = loc.distanceTo3(dc.getView().getEyePoint());
		double metersPerPixel = dc.getView().computePixelSizeAtDistance(d);
		double dpm = this.getPixelsPerMeter();

		double modelSizeMeters = this.eyeDistanceOffset;
		double modelPixels = modelSizeMeters / metersPerPixel;

		double scale = size / modelPixels;

		if (scale < .5) {
			scale = .5;
		}
		return scale;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
