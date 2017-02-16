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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MtlLoader {

	String basePath;
	public List<Material> materials = new ArrayList<>();

	public MtlLoader(String basePath, String mtlPath) {
		this.basePath = basePath;

		BufferedReader brm = null;
		try {
			InputStream is = this.getInputStream(basePath, mtlPath);
			brm = new BufferedReader(new InputStreamReader(is));
			loadobject(brm);
		} catch (IOException e) {
			System.out.println("Could not open file: " + basePath + "/" + mtlPath);
			materials = null;
		} finally {
			if (brm != null) {
				try {
					brm.close();
				} catch (IOException ex) {
					Logger.getLogger(MtlLoader.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
	}

	final InputStream getInputStream(String basePath, String mtlPath) throws IOException {
		InputStream is = this.getClass().getResourceAsStream(basePath + "/" + mtlPath);
		if (is == null) {
			File mtlFile = new File(mtlPath);
			if (!mtlFile.isAbsolute()) {
				mtlFile = new File(new File(basePath), mtlPath);
			}
			if (mtlFile.exists() && mtlFile.canRead()) {
				is = new FileInputStream(mtlFile);
			}
		}
		return is;
	}

	boolean exists(String basePath, String mtlPath) throws IOException {
		URL url = this.getClass().getResource(basePath+"/"+mtlPath);
		if (url == null) {
			File mtlFile = new File(mtlPath);
			if (!mtlFile.isAbsolute()) {
				mtlFile = new File(new File(basePath), mtlPath);
			}
			url = mtlFile.toURI().toURL();
		}
		return url != null;
	}

	public Material getMtl(String namepass) {
		for (int i = 0; i < materials.size(); i++) {
			Material mtl = (Material) materials.get(i);
			if (mtl.name.matches(namepass)) {
				return mtl;
			}
		}
		return null;
	}

	private float[] getValues(String str) {
		String[] data = str.split("\\s+");
		return new float[]{Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])};
	}

	private float getValue(String str) {
		return Float.parseFloat(str.split("\\s+")[1]);
	}

	private void loadobject(BufferedReader br) {
		int linecounter = 0;
		try {

			String newline;
			boolean firstpass = true;
			Material matset = new Material();
			int mtlcounter = 0;

			while (((newline = br.readLine()) != null)) {
				linecounter++;
				newline = newline.trim();
				if (newline.length() > 0) {
					if (newline.startsWith("newmtl")) {
						if (firstpass) {
							firstpass = false;
						} else {
							materials.add(matset);
							matset = new Material();
						}
						String[] coordstext = newline.split("\\s+");
						matset.name = coordstext[1];
						matset.mtlnum = mtlcounter;
						mtlcounter++;
					}
					if (newline.startsWith("Ka")) {
						matset.Ka = getValues(newline);
					}
					if (newline.startsWith("Kd")) {
						matset.Kd = getValues(newline);
					}
					if (newline.startsWith("Ks")) {
						matset.Ks = getValues(newline);
					}
					if (newline.startsWith("d")) {
						matset.d = getValue(newline);
					}
					if (newline.startsWith("Ns")) {
						matset.Ns = getValue(newline);
					}
					if (newline.startsWith("illum")) {
						matset.illum = getValue(newline);
					}
					if (newline.startsWith("map_Kd")) { //texture image
						String map_Kd = newline.trim().substring(newline.indexOf(" ")).trim();
						if (this.exists(basePath, map_Kd)) {
							matset.map_Kd = map_Kd;
						} else {
							System.err.println("Error: unable to read texture " + map_Kd);
						}
					}
				}
			}
			materials.add(matset);

		} catch (IOException e) {
			System.out.println("Failed to read file: " + br.toString());
			e.printStackTrace();
		} catch (NumberFormatException | StringIndexOutOfBoundsException e) {
			System.out.println("Malformed MTL (on line " + linecounter + "): " + br.toString() + "\r \r" + e.getMessage());
		}
	}

	public class Material {
		public String name;
		public int mtlnum;
		public float Ns, Ni, Tr, illum;
		public float[] Tf = new float[3];
		public float d = 1f;
		public float[] Ka = new float[3];
		public float[] Kd = new float[3];
		public float[] Ks = new float[3];
		public float[] Ke = new float[3];
		public String map_Kd = null;
	}
}
