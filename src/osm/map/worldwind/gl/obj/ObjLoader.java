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

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import com.jogamp.opengl.GL2;
import osm.map.worldwind.gl.obj.MtlLoader.Material;

public class ObjLoader {

	List<float[]> vertexSets = new ArrayList<>();
	List<float[]> vertexSetsNorms = new ArrayList<>();
	List<float[]> vertexSetsTexs = new ArrayList<>();
	List<Face> faces = new ArrayList<>();
	int objectlist;
	float toppoint, bottompoint, leftpoint, rightpoint, farpoint, nearpoint;
	Map<String, Texture> textureCache = new HashMap<>();
	BoundingBox bbox;
	private final static Logger logger = Logger.getLogger(ObjLoader.class.getName());

	String basePath;
	boolean flipTextureVertically;

	public ObjLoader(String objPath, GL2 gl, boolean centered, boolean flipTextureVertically) {
		this.flipTextureVertically = flipTextureVertically;
		String path="";
		objPath = objPath.replaceAll("\\\\", "/");
		int index = objPath.lastIndexOf("/");
		if(index < 0) {
			index = 0;
		}
		String name = objPath.substring(index+1);
		path=objPath.substring(0,index);
		this.init(path, name, gl, centered);
	}

	public ObjLoader(String basePath, String objPath, GL2 gl, boolean centered, boolean flipTextureVertically) {
		this.flipTextureVertically = flipTextureVertically;
		this.init(basePath, objPath, gl, centered);
	}

	private void init(String basePath, String objPath, GL2 gl, boolean centered) {
		try {
			if (basePath == null) {
				this.basePath = "";
			} else {
				this.basePath = basePath;
			}
			BufferedReader bufferedReader = null;
			InputStream is;
			try {
				is = getInputStream(basePath, objPath);
				bufferedReader = new BufferedReader(new InputStreamReader(is));
				loadObject(bufferedReader);
				if (centered) {
					centerit();
				}
				opengldrawtolist(gl);
				cleanup();
			} finally {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			}
			this.bbox = new BoundingBox(this.getXWidth(), this.getYHeight(), this.getZDepth(), this.bottompoint, centered);

		} catch (Exception e) {
			throw new RuntimeException("error loading model: " + basePath + "/" + objPath, e);
		}
	}

	InputStream getInputStream(String basePath, String objPath) throws IOException {
		String path = basePath + "/" + objPath;
		InputStream is = this.getClass().getResourceAsStream(path);
		if (is == null) {
			File f = new File(path);
			if (f.exists()) {
				is = new FileInputStream(f);
			}
		}
		if (path.endsWith(".gz")){
			is = new GZIPInputStream(is,8192);
		}
		return is;
	}

	public BoundingBox getBoundingBox() {
		return bbox;
	}

	private void cleanup() {
		vertexSets.clear();
		vertexSetsNorms.clear();
		vertexSetsTexs.clear();
		faces.clear();
	}

	private void loadObject(BufferedReader br) {
		String mtlID = null;
		MtlLoader mtlLoader = null;
		try {
			boolean firstpass = true;
			String newline;
			while ((newline = br.readLine()) != null) {
				if (newline.length() > 0) {
					newline = newline.trim();

					//Loads vertex coordinates
					if (newline.startsWith("v ")) {
						float coords[] = new float[4];
						newline = newline.substring(2, newline.length());
						StringTokenizer st = new StringTokenizer(newline, " ");
						for (int i = 0; st.hasMoreTokens(); i++) {
							coords[i] = Float.parseFloat(st.nextToken());
						}
						if (firstpass) {
							rightpoint = coords[0];
							leftpoint = coords[0];
							toppoint = coords[1];
							bottompoint = coords[1];
							nearpoint = coords[2];
							farpoint = coords[2];
							firstpass = false;
						}
						rightpoint = Math.max(coords[0], rightpoint);
						leftpoint = Math.min(coords[0], leftpoint);
						toppoint = Math.max(coords[1], toppoint);
						bottompoint = Math.min(coords[1], bottompoint);
						nearpoint = Math.max(coords[2], nearpoint);
						farpoint = Math.min(coords[2], farpoint);
						vertexSets.add(coords);
					} else //Loads vertex texture coordinates
					{
						if (newline.startsWith("vt")) {
							float coords[] = new float[4];
							newline = newline.substring(3, newline.length());
							StringTokenizer st = new StringTokenizer(newline, " ");
							for (int i = 0; st.hasMoreTokens(); i++) {
								coords[i] = Float.parseFloat(st.nextToken());
							}
							vertexSetsTexs.add(coords);
						} else //Loads vertex normals coordinates
						{
							if (newline.startsWith("vn")) {
								float coords[] = new float[4];
								newline = newline.substring(3, newline.length());
								StringTokenizer st = new StringTokenizer(newline, " ");
								for (int i = 0; st.hasMoreTokens(); i++) {
									coords[i] = Float.parseFloat(st.nextToken());
								}
								vertexSetsNorms.add(coords);
							} else if (newline.startsWith("f ")) { //Loads face coordinates
								newline = newline.substring(2, newline.length());
								StringTokenizer st = new StringTokenizer(newline, " ");
								int count = st.countTokens();
								int v[] = new int[count];
								int vt[] = new int[count];
								int vn[] = new int[count];
								for (int i = 0; i < count; i++) {
									char chars[] = st.nextToken().toCharArray();
									StringBuilder sb = new StringBuilder();
									char lc = 'x';
									for (int k = 0; k < chars.length; k++) {
										if (chars[k] == '/' && lc == '/') {
											sb.append('0');
										}
										lc = chars[k];
										sb.append(lc);
									}
									StringTokenizer st2 = new StringTokenizer(sb.toString(), "/");
									int num = st2.countTokens();
									v[i] = Integer.parseInt(st2.nextToken());
									if (num > 1) {
										vt[i] = Integer.parseInt(st2.nextToken());
									} else {
										vt[i] = 0;
									}
									if (num > 2) {
										vn[i] = Integer.parseInt(st2.nextToken());
									} else {
										vn[i] = 0;
									}
								}
								faces.add(new Face(mtlLoader.getMtl(mtlID), v, vn, vt));
							} else if (newline.startsWith("mtllib")) { //Loads materials
								mtlLoader = new MtlLoader(basePath, newline.substring(newline.indexOf(" ")).trim());
							} else if (newline.startsWith("usemtl")) { //Uses materials
								mtlID = newline.split("\\s+")[1];
							}
						}
					}
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to read file: " + br.toString());
		} catch (NumberFormatException e) {
			System.out.println("Malformed OBJ file: " + br.toString() + "\r \r" + e.getMessage());
		}
		Collections.sort(faces);
	}

	private void centerit() {
		float xshift = (rightpoint - leftpoint) / 2.0F;
		float yshift = (toppoint - bottompoint) / 2.0F;
		float zshift = (nearpoint - farpoint) / 2.0F;
		for (int i = 0; i < vertexSets.size(); i++) {
			float coords[] = new float[4];
			coords[0] = (vertexSets.get(i))[0] - leftpoint - xshift;
			//coords[1] = (vertexSets.get(i))[1] - bottompoint - yshift;
			coords[1] = (vertexSets.get(i))[1] - bottompoint;
			coords[2] = (vertexSets.get(i))[2] - farpoint - zshift;
			vertexSets.set(i, coords);
		}

	}

	public double getMaxDimen() {
		double max = Math.max(getXWidth(), getYHeight());
		return Math.max(max, getZDepth());
	}

	public float getXWidth() {
		return rightpoint - leftpoint;
	}

	public float getYHeight() {
		return toppoint - bottompoint;
	}

	public float getZDepth() {
		return nearpoint - farpoint;
	}

	public int getPolygonCount() {
		return faces.size();
	}

	private void opengldrawtolist(GL2 gl) {
		String lastMapKd = "";
		Texture texture = null;
		this.objectlist = gl.glGenLists(1);

		gl.glNewList(objectlist, GL2.GL_COMPILE);
		Material mtl = null;
		for (Face face : faces) {
			if (mtl == null || !mtl.name.equals(face.mtl.name)) { //has mtl changed?  if so, set up the new mtl
				mtl = face.mtl;
				if (mtl.map_Kd == null) { //no texture?
					if (texture != null) { //disable previous texture if it's not null
						texture.disable(gl);
						texture = null;
						lastMapKd = "";
					}
				} else if (!lastMapKd.equals(mtl.map_Kd.toString())) { //yes texture, and it changed?
					if (texture != null) {
						texture.disable(gl);
					}
					texture = face.texture;
					texture.enable(gl);
					texture.bind(gl);
					gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
					gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
					gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
					gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
					lastMapKd = mtl.map_Kd.toString();
				}

				//determine color
				gl.glEnable(GL2.GL_COLOR_MATERIAL);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA); //enable alpha (transparency) channel
				gl.glEnable(GL2.GL_BLEND); //and blending
				float[] color = lighten(new float[]{Math.min(1, mtl.Kd[0] + mtl.Ka[0]),
					Math.min(1, mtl.Kd[1] + mtl.Ka[1]), Math.min(1, mtl.Kd[2] + mtl.Ka[2])}, 0.15f);
				gl.glColor4f(color[0], color[1], color[2], mtl.d);
			}

			//draw the polygons for this face
			gl.glBegin(face.polyType);
			for (int w = 0; w < face.v.length; w++) {
				if (face.vn[w] != 0) {
					float[] floats = vertexSetsNorms.get(face.vn[w] - 1);
					gl.glNormal3f(floats[0], floats[1], floats[2]);
				}
				if (face.vt[w] != 0) {
					float[] floats = vertexSetsTexs.get(face.vt[w] - 1);
					if (flipTextureVertically) {
						gl.glTexCoord2f(floats[0], 1f - floats[1]);
					} else {
						gl.glTexCoord2f(floats[0], floats[1]);
					}
				}
				float[] floats = vertexSets.get(face.v[w] - 1);
				gl.glVertex3f(floats[0], floats[1], floats[2]);
			}
			gl.glEnd();
		}
		gl.glDisable(GL2.GL_COLOR_MATERIAL);
		if (texture != null) {
			texture.disable(gl);
		}
		gl.glEndList();
	}

	private float[] lighten(float[] color, float amount) {
		float r = Math.min(1, color[0] + amount);
		float g = Math.min(1, color[1] + amount);
		float b = Math.min(1, color[2] + amount);
		return new float[]{r, g, b};
	}

	public void opengldraw(GL2 gl) {
		gl.glCallList(objectlist);
	}

	private Texture getTexture(String map_Kd) throws IOException {
		if (map_Kd == null) {
			return null;
		}
		if (textureCache.get(map_Kd) == null) {
			InputStream is = null;
			try {
				is = this.getInputStream(basePath, map_Kd);
				String suffix = null;
				String tokens[] = map_Kd.split("\\.");
				if (tokens != null) {
					if (tokens.length > 1) {
						suffix = tokens[tokens.length - 1];
					}
				}
				Texture t = TextureIO.newTexture(is, false, suffix);
				textureCache.put(map_Kd, t);
			} finally {
				if (is != null) {
					is.close();
				}
			}
		}
		return textureCache.get(map_Kd);
	}

	public class Face implements Comparable<Face> {

		MtlLoader.Material mtl;
		int[] v; //face
		int[] vn; //normal
		int[] vt; //texture
		int polyType;
		Texture texture;

		public Face(MtlLoader.Material mtl, int[] v, int[] vn, int[] vt) {
			this.mtl = mtl;
			this.v = v;
			this.vn = vn;
			this.vt = vt;
			switch (v.length) {
				case 3:
					polyType = GL2.GL_TRIANGLES;
					break;
				case 4:
					polyType = GL2.GL_QUADS;
					break;
				default:
					polyType = GL2.GL_POLYGON;
					break;
			}
			if (mtl.map_Kd != null) {
				try {
					texture = getTexture(mtl.map_Kd);
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Exception reading texture: " + mtl.map_Kd, e);
				}
			}
		}

		@Override
		public int compareTo(Face face) {
			if (this.mtl.d > face.mtl.d) { //draw opaque faces first
				return -1;
			} else if (this.mtl.d < face.mtl.d) {
				return 1;
			}

			if (this.texture == null && face.texture != null) { //draw non-textured faces first
				return -1;
			} else if (this.texture != null && face.texture == null) {
				return 1;
			} else if (this.texture != null && face.texture != null) { //order by texture name
				return this.mtl.map_Kd.compareTo(face.mtl.map_Kd);
			}
			return this.mtl.name.compareTo(face.mtl.name); //order by mtl name
		}

	}

}
