/*
 * Copyright (C) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

/*
 * this code was published by user 'wobster' on the WorldWind forum thread
 *   https://forum.worldwindcentral.com/forum/world-wind-java-forums/development-help/17605-collada-models-with-lighting
 * By permission of its creator it is put under the same license and copyright as WorldWind.
 */

package osm.map.worldwind.gl.obj;

import gov.nasa.worldwind.render.DrawContext;
import com.jogamp.opengl.GL2;

public class BoundingBox {

	boolean centerit = false;
	// Vertices of a unit cube, centered on the origin.
	float[][] v = {
		{-0.5f, 0.5f, -0.5f},
		{-0.5f, 0.5f, 0.5f},
		{0.5f, 0.5f, 0.5f},
		{0.5f, 0.5f, -0.5f},
		{-0.5f, -0.5f, 0.5f},
		{0.5f, -0.5f, 0.5f},
		{0.5f, -0.5f, -0.5f},
		{-0.5f, -0.5f, -0.5f}};

	// Array to group vertices into faces
	int[][] faces = {{0, 1, 2, 3}, {2, 5, 6, 3}, {1, 4, 5, 2}, {0, 7, 4, 1}, {0, 7, 6, 3}, {4, 7, 6, 5}};

	// Normal vectors for each face
	float[][] n = {{0, 1, 0}, {1, 0, 0}, {0, 0, 1}, {-1, 0, 0}, {0, 0, -1}, {0, -1, 0}};

	public BoundingBox(float dx, float dy, float dz, float bottomPoint, boolean centerit) {
		this.centerit = centerit;
		if (centerit) {
			center(dx, dy, dz, bottomPoint);
		}
	}

	private void center(float dx, float dy, float dz, float bottomPoint) {
		if (!this.centerit) {
			bottomPoint = 0;
		}
		for (float row[] : v) {
			row[0] = row[0] * dx;
			row[1] = (row[1]) * dy - bottomPoint;
			row[2] = row[2] * dz;
		}
	}

	protected void drawUnitCubeOutline(DrawContext dc) {
		GL2 gl = dc.getGL().getGL2();
		gl.glLineWidth(4f);
		gl.glEnable(GL2.GL_LINE_STIPPLE);
		boolean lighting = gl.glIsEnabled(GL2.GL_LIGHTING);
		if (lighting) {
			gl.glDisable(GL2.GL_LIGHTING);
		}
		gl.glLineStipple(1, (short) 0x00FF);
//		gl.glColorMaterial(GL2.GL_FRONT_AND_BACK,GL2.GL_AMBIENT_AND_DIFFUSE);
//		gl.glEnable(GL2.GL_COLOR_MATERIAL);
		gl.glColor3ub((byte) 255, (byte) 255, (byte) 0);
		for (int[] face : faces) {
			try {
				gl.glBegin(GL2.GL_LINE_LOOP);
				for (int j = 0; j < faces[0].length; j++) {
					gl.glVertex3f(v[face[j]][0], v[face[j]][1], v[face[j]][2]);
				}
			} finally {
				gl.glEnd();
			}
		}
		gl.glDisable(GL2.GL_LINE_STIPPLE);
//		gl.glDisable(GL2.GL_COLOR_MATERIAL);
		if (lighting) {
			gl.glEnable(GL2.GL_LIGHTING);
		}
	}

	/**
	 * Draw a unit cube, using the active modelview matrix to orient the shape.
	 *
	 * @param dc Current draw context.
	 * @param dx X scale factor
	 * @param dy Y scale factor
	 * @param dz Z scale factor
	 */
	protected void drawUnitCube(DrawContext dc) {

		// Note: draw the cube in OpenGL immediate mode for simplicity. Real applications should use vertex arrays
		// or vertex buffer objects to achieve better performance.
		GL2 gl = dc.getGL().getGL2();
		gl.glBegin(GL2.GL_QUADS);
		try {
			for (int i = 0; i < faces.length; i++) {
				gl.glNormal3f(n[i][0], n[i][1], n[i][2]);
				for (int j = 0; j < faces[0].length; j++) {
					gl.glVertex3f(v[faces[i][j]][0], v[faces[i][j]][1], v[faces[i][j]][2]);
				}
			}
		} finally {
			gl.glEnd();
		}
	}

}
