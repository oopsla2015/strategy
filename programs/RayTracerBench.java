/*
 * This file is part of the Panini project at Iowa State University.
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 * 
 * For more details and the latest version of this code please see
 * http://paninij.org
 * 
 * Contributor(s): Ganesha Upadhyaya
 */

/**
 * desc: http://www2.epcc.ed.ac.uk/computing/research_activities/java_grande/threads/s3contents.html#raytracer
 */

import java.util.Vector;

class Interval implements java.io.Serializable
{
/*
public int number;
  public int width;
  public int height;
  public int yfrom;
  public int yto;
  public int total;
*/  
    public final int number;
    public final int width;
    public final int height;
    public final int yfrom;
    public final int yto;
    public final int total;
    public final int threadid;

    public Interval(int number, int width, int height, int yfrom, int yto, int total, int threadid)
    {
        this.number = number;
        this.width = width;
        this.height = height;
        this.yfrom = yfrom;
        this.yto = yto;
        this.total = total;
        this.threadid = threadid;
    }
}

class Isect {
	public double		t;
	public int		enter;
	public Primitive	prim;
	public Surface		surf;
}

class Light implements java.io.Serializable {
    public Vec	pos;
    public double	brightness;
	
    public Light() {
    }
    
    public Light(double x, double y, double z, double brightness) {
	this.pos = new Vec(x, y, z);
	this.brightness = brightness;
    }
}

abstract class Primitive implements java.io.Serializable {
	public Surface	surf = new Surface();

	public void setColor(double r, double g, double b) {
		surf.color = new Vec(r, g, b);
	}

	public abstract Vec normal(Vec pnt);
	public abstract Isect intersect(Ray ry);
	public abstract String toString();
	public abstract Vec getCenter();
	public abstract void setCenter(Vec c);
}

class Ray {
	public Vec P, D;

	public Ray(Vec pnt, Vec dir) {
		P = new Vec(pnt.x, pnt.y, pnt.z);
		D = new Vec(dir.x, dir.y, dir.z);
		D.normalize();
	}

	public Ray() {
		P = new Vec();
		D = new Vec();
	}

	public Vec point(double t) {
		return new Vec(P.x + D.x * t, P.y + D.y * t, P.z + D.z * t);
	}

	public String toString() {
		return "{" + P.toString() + " -> " + D.toString() + "}";
	}
}

class Scene implements java.io.Serializable
{
    public final Vector lights;
    public final Vector objects;
    private View view;  
  
    public Scene ()
    {
        this.lights = new Vector ();
        this.objects = new Vector ();        
    }
  
    public void addLight(Light l)
    {
        this.lights.addElement(l);
    }
  
    public void addObject(Primitive object)
    {
        this.objects.addElement(object);
    }
  
    public void setView(View view)
    {
        this.view = view;
    }
  
    public View getView()
    {
        return this.view;
    }
  
    public Light getLight(int number)
    {
        return (Light) this.lights.elementAt(number);
    }
  
    public Primitive getObject(int number)
    {
        return (Primitive) objects.elementAt(number);
    }
  
    public int getLights()
    {
        return this.lights.size();
    }
  
    public int getObjects()
    {
        return this.objects.size();
    }
  
    public void setObject(Primitive object, int pos)
    {
        this.objects.setElementAt(object, pos);
    }
}

class Sphere extends Primitive implements java.io.Serializable {
	  Vec      c;
	  double   r, r2;
	  Vec      v,b; // temporary vecs used to minimize the memory load

	  public Sphere(Vec center, double radius) {
	    c = center;
	    r = radius;
	    r2 = r*r;
	    v=new Vec();
	    b=new Vec();
	  }
	  
	  public Isect intersect(Ray ry) {
	    double b, disc, t;
	    Isect ip;
	    v.sub2(c, ry.P);
	    b = Vec.dot(v, ry.D);
	    disc = b*b - Vec.dot(v, v) + r2;
	    if (disc < 0.0) {
	      return null;
	    }
	    disc = Math.sqrt(disc);
	    t = (b - disc < 1e-6) ? b + disc : b - disc;
	    if (t < 1e-6) {
	      return null;
	    }
	    ip = new Isect();
	    ip.t = t;
	    ip.enter = Vec.dot(v, v) > r2 + 1e-6 ? 1 : 0;
	    ip.prim = this;
	    ip.surf = surf;
	    return ip;
	  }

	  public Vec normal(Vec p) {
	    Vec r;
	    r = Vec.sub(p, c);
	    r.normalize();
	    return r;
	  }

	  public String toString() {
	    return "Sphere {" + c.toString() + "," + r + "}";
	  }
		
	  public Vec getCenter() {
	    return c;
	  }
	  public void setCenter(Vec c) {
	    this.c = c;
	  }
}
class Surface implements java.io.Serializable{
	public Vec	color;
	public double	kd;
	public double	ks;
	public double	shine;
	public double	kt;
	public double	ior;

	public Surface() {
		color = new Vec(1, 0, 0);
		kd = 1.0;
		ks = 0.0;
		shine = 0.0;
		kt = 0.0;
		ior = 1.0;
	}

	public String toString() {
		return "Surface { color=" + color + "}";
	}
}

class Vec implements java.io.Serializable {

	  /**
	   * The x coordinate
	   */
	  public double x; 

	  /**
	   * The y coordinate
	   */
	  public double y;

	  /**
	   * The z coordinate
	   */
	  public double z;

	  /**
	   * Constructor
	   * @param a the x coordinate
	   * @param b the y coordinate
	   * @param c the z coordinate
	   */
	  public Vec(double a, double b, double c) {
	    x = a;
	    y = b;
	    z = c;
	  }

	  /**
	   * Copy constructor
	   */
	  public Vec(Vec a) {
	    x = a.x;
	    y = a.y;
	    z = a.z;
	  }
	  /**
	   * Default (0,0,0) constructor
	   */
	  public Vec() {
	    x = 0.0;
	    y = 0.0; 
	    z = 0.0;
	  }

	  /**
	   * Add a vector to the current vector
	   * @param: a The vector to be added
	   */
	  public final void add(Vec a) {
	    x+=a.x;
	    y+=a.y;
	    z+=a.z;
	  }  

	  /**
	   * adds: Returns a new vector such as
	   * new = sA + B
	   */
	  public static Vec adds(double s, Vec a, Vec b) {
	    return new Vec(s * a.x + b.x, s * a.y + b.y, s * a.z + b.z);
	  }
	    
	  /**
	   * Adds vector such as:
	   * this+=sB
	   * @param: s The multiplier
	   * @param: b The vector to be added
	   */
	  public final void adds(double s,Vec b){
	      x+=s*b.x;
	      y+=s*b.y;
	      z+=s*b.z;
	  }

	  /**
	   * Substracs two vectors
	   */
	  public static Vec sub(Vec a, Vec b) {
	    return new Vec(a.x - b.x, a.y - b.y, a.z - b.z);
	  }

	  /**
	   * Substracts two vects and places the results in the current vector
	   * Used for speedup with local variables -there were too much Vec to be gc'ed
	   * Consumes about 10 units, whether sub consumes nearly 999 units!! 
	   * cf thinking in java p. 831,832
	   */
	  public final void sub2(Vec a,Vec b) {
	    this.x=a.x-b.x;
	    this.y=a.y-b.y;
	    this.z=a.z-b.z;
	  }

	  public static Vec mult(Vec a, Vec b) {
	    return new Vec(a.x * b.x, a.y * b.y, a.z * b.z);
	  }

	  public static Vec cross(Vec a, Vec b) {
	    return
	      new Vec(a.y*b.z - a.z*b.y,
		      a.z*b.x - a.x*b.z,
		      a.x*b.y - a.y*b.x);
	  }

	  public static double dot(Vec a, Vec b) {
	    return a.x*b.x + a.y*b.y + a.z*b.z;
	  }

	  public static Vec comb(double a, Vec A, double b, Vec B) {
	    return
	      new Vec(a * A.x + b * B.x,
		      a * A.y + b * B.y,
		      a * A.z + b * B.z);
	  }

	  public final void comb2(double a,Vec A,double b,Vec B) {
	    x=a * A.x + b * B.x;
	    y=a * A.y + b * B.y;
	    z=a * A.z + b * B.z;      
	  }

	  public final void scale(double t) {
	    x *= t;
	    y *= t;
	    z *= t;
	  }

	  public final void negate() {
	    x = -x;
	    y = -y;
	    z = -z;
	  }

	  public final double normalize() {
	    double len;
	    len = Math.sqrt(x*x + y*y + z*z);
	    if (len > 0.0) {
	      x /= len;
	      y /= len;
	      z /= len;
	    }
	    return len;
	  }

	  public final String toString() {
	    return "<" + x + "," + y + "," + z + ">";
	  }
	}


class View implements java.io.Serializable
{
/*    public  Vec     from;
	public  Vec	    at;
	public  Vec	    up;
	public  double	dist;
	public  double	angle;
	public  double	aspect;*/
    public final Vec       from;
	public final Vec	    at;
	public final Vec	    up;
	public final double	dist;
	public final double	angle;
	public final double	aspect;
		
	public View (Vec from, Vec at, Vec up, double dist, double angle, double aspect)
	{
        this.from = from;
        this.at = at;
        this.up = up;
        this.dist = dist;
        this.angle = angle;
        this.aspect = aspect;	    	    
	}
}

class Long {
    private long x;
    public Long (long y) { x = y; }
    public long value() { return x; }
}

class int_ {
    private int x;
    public int_ (int y) { x = y; }
    public int val() { return x; }
}


capsule RayTracer (int id, int nthreads) {
	
	Scene scene;
	/**
	 * Lights for the rendering scene
	 */
	Light lights[];

	/**
	 * Objects (spheres) for the rendering scene
	 */
	Primitive prim[];

	/**
	 * The view for the rendering scene
	 */
	View view;

	/**
	 * Temporary ray
	 */
	Ray tRay = new Ray();

	/**
	 * Alpha channel
	 */
	int alpha = 255 << 24;

	/**
	 * Null vector (for speedup, instead of <code>new Vec(0,0,0)</code>
	 */
	Vec voidVec = new Vec();

	/**
	 * Temporary vect
	 */
	Vec L = new Vec();

	/**
	 * Current intersection instance (only one is needed!)
	 */
	Isect inter = new Isect();

	long checksum = 0;

	int numobjects;
	
	
	Long start(int_ widthA, int_ heightA) {
		int width = widthA.val();
		int height = heightA.val();
		// create the objects to be rendered
		scene = createScene();
		// get lights, objects etc. from scene.
		setScene(scene);
		numobjects = scene.getObjects();
		
		// Set interval to be rendered to the whole picture
		// (overkill, but will be useful to retain this for parallel versions)
		Interval interval = new Interval(0, width, height, 0, height, 1, id);
		
		// synchronise threads and start timer
		//br.DoBarrier(new int_(id));
		
		render(interval);
		
		// synchronise threads and stop timer
		//br.DoBarrier(new int_(id));
		
		return new Long(checksum);
	}

	/**
	 * Create and initialize the scene for the rendering picture.
	 * 
	 * @return The scene just created
	 */

	private Scene createScene() {
		int x = 0;
		int y = 0;

		Scene scene = new Scene();

		/* create spheres */

		Primitive p;
		int nx = 4;
		int ny = 4;
		int nz = 4;
		for (int i = 0; i < nx; i++) {
			for (int j = 0; j < ny; j++) {
				for (int k = 0; k < nz; k++) {
					double xx = 20.0 / (nx - 1) * i - 10.0;
					double yy = 20.0 / (ny - 1) * j - 10.0;
					double zz = 20.0 / (nz - 1) * k - 10.0;

					p = new Sphere(new Vec(xx, yy, zz), 3);
					// p.setColor(i/(double) (nx-1), j/(double)(ny-1),
					// k/(double) (nz-1));
					p.setColor(0, 0, (i + j) / (double) (nx + ny - 2));
					p.surf.shine = 15.0;
					p.surf.ks = 1.5 - 1.0;
					p.surf.kt = 1.5 - 1.0;
					scene.addObject(p);
				}
			}
		}

		/* Creates five lights for the scene */
		scene.addLight(new Light(100, 100, -50, 1.0));
		scene.addLight(new Light(-100, 100, -50, 1.0));
		scene.addLight(new Light(100, -100, -50, 1.0));
		scene.addLight(new Light(-100, -100, -50, 1.0));
		scene.addLight(new Light(200, 200, 0, 1.0));

		/* Creates a View (viewing point) for the rendering scene */
		View v = new View(new Vec(x, 20, -30), new Vec(x, y, 0), new Vec(0, 1,
				0), 1.0, 35.0 * 3.14159265 / 180.0, 1.0);
		/*
		 * v.from = new Vec(x, y, -30); v.at = new Vec(x, y, -15); v.up = new
		 * Vec(0, 1, 0); v.angle = 35.0 * 3.14159265 / 180.0; v.aspect = 1.0;
		 * v.dist = 1.0;
		 */
		scene.setView(v);

		return scene;
	}

	private void setScene(Scene scene) {
		// Get the objects count
		int nLights = scene.getLights();
		int nObjects = scene.getObjects();

		lights = new Light[nLights];
		prim = new Primitive[nObjects];

		// Get the lights
		for (int l = 0; l < nLights; l++) {
			lights[l] = scene.getLight(l);
		}

		// Get the primitives
		for (int o = 0; o < nObjects; o++) {
			prim[o] = scene.getObject(o);
		}

		// Set the view
		view = scene.getView();
	}

	private void render(Interval interval) {

		// Screen variables
		int row[] = new int[interval.width * (interval.yto - interval.yfrom)];
		int pixCounter = 0; // iterator

		// Rendering variables
		int x, y, red, green, blue;
		double xlen, ylen;
		Vec viewVec;

		viewVec = Vec.sub(view.at, view.from);

		viewVec.normalize();

		Vec tmpVec = new Vec(viewVec);
		tmpVec.scale(Vec.dot(view.up, viewVec));

		Vec upVec = Vec.sub(view.up, tmpVec);
		upVec.normalize();

		Vec leftVec = Vec.cross(view.up, viewVec);
		leftVec.normalize();

		double frustrumwidth = view.dist * Math.tan(view.angle);

		upVec.scale(-frustrumwidth);
		leftVec.scale(view.aspect * frustrumwidth);

		Ray r = new Ray(view.from, voidVec);
		Vec col = new Vec();

		// Header for .ppm file
		// System.out.println("P3");
		// System.out.println(width + " " + height);
		// System.out.println("255");

		// All loops are reversed for 'speedup' (cf. thinking in java p331)

		// For each line

		for (y = interval.yfrom + interval.threadid; y < interval.yto; y += nthreads) {

			ylen = (double) (2.0 * y) / (double) interval.width - 1.0;
			// System.out.println("Doing line " + y);
			// For each pixel of the line
			for (x = 0; x < interval.width; x++) {
				xlen = (double) (2.0 * x) / (double) interval.width - 1.0;
				r.D = Vec.comb(xlen, leftVec, ylen, upVec);
				r.D.add(viewVec);
				r.D.normalize();
				col = trace(0, 1.0, r);

				// computes the color of the ray
				red = (int) (col.x * 255.0);
				if (red > 255)
					red = 255;
				green = (int) (col.y * 255.0);
				if (green > 255)
					green = 255;
				blue = (int) (col.z * 255.0);
				if (blue > 255)
					blue = 255;

				checksum += red;
				checksum += green;
				checksum += blue;

				// RGB values for .ppm file
				// System.out.println(red + " " + green + " " + blue);
				// Sets the pixels
				row[pixCounter++] = alpha | (red << 16) | (green << 8) | (blue);
			} // end for (x)
		} // end for (y)

	}

	private boolean intersect(Ray r, double maxt) {
		Isect tp;
		int i, nhits;

		nhits = 0;
		inter.t = 1e9;
		for (i = 0; i < prim.length; i++) {
			// uses global temporary Prim (tp) as temp.object for speedup
			tp = prim[i].intersect(r);
			if (tp != null && tp.t < inter.t) {
				inter.t = tp.t;
				inter.prim = tp.prim;
				inter.surf = tp.surf;
				inter.enter = tp.enter;
				nhits++;
			}
		}
		return nhits > 0 ? true : false;
	}

	/**
	 * Checks if there is a shadow
	 * 
	 * @param r
	 *            The ray
	 * @return Returns 1 if there is a shadow, 0 if there isn't
	 */
	private int Shadow(Ray r, double tmax) {
		if (intersect(r, tmax))
			return 0;
		return 1;
	}

	/**
	 * Return the Vector's reflection direction
	 * 
	 * @return The specular direction
	 */
	private Vec SpecularDirection(Vec I, Vec N) {
		Vec r;
		r = Vec.comb(1.0 / Math.abs(Vec.dot(I, N)), I, 2.0, N);
		r.normalize();
		return r;
	}

	/**
	 * Return the Vector's transmission direction
	 */
	private Vec TransDir(Surface m1, Surface m2, Vec I, Vec N) {
		double n1, n2, eta, c1, cs2;
		Vec r;
		n1 = m1 == null ? 1.0 : m1.ior;
		n2 = m2 == null ? 1.0 : m2.ior;
		eta = n1 / n2;
		c1 = -Vec.dot(I, N);
		cs2 = 1.0 - eta * eta * (1.0 - c1 * c1);
		if (cs2 < 0.0)
			return null;
		r = Vec.comb(eta, I, eta * c1 - Math.sqrt(cs2), N);
		r.normalize();
		return r;
	}

	/**
	 * Returns the shaded color
	 * 
	 * @return The color in Vec form (rgb)
	 */
	private Vec shade(int level, double weight, Vec P, Vec N, Vec I, Isect hit) {
		double n1, n2, eta, c1, cs2;
		Vec r;
		Vec tcol;
		Vec R;
		double t, diff, spec;
		Surface surf;
		Vec col;
		int l;

		col = new Vec();
		surf = hit.surf;
		R = new Vec();
		if (surf.shine > 1e-6) {
			R = SpecularDirection(I, N);
		}

		// Computes the effectof each light
		for (l = 0; l < lights.length; l++) {
			L.sub2(lights[l].pos, P);
			if (Vec.dot(N, L) >= 0.0) {
				t = L.normalize();

				tRay.P = P;
				tRay.D = L;

				// Checks if there is a shadow
				if (Shadow(tRay, t) > 0) {
					diff = Vec.dot(N, L) * surf.kd * lights[l].brightness;

					col.adds(diff, surf.color);
					if (surf.shine > 1e-6) {
						spec = Vec.dot(R, L);
						if (spec > 1e-6) {
							spec = Math.pow(spec, surf.shine);
							col.x += spec;
							col.y += spec;
							col.z += spec;
						}
					}
				}
			} // if
		} // for

		tRay.P = P;
		if (surf.ks * weight > 1e-3) {
			tRay.D = SpecularDirection(I, N);
			tcol = trace(level + 1, surf.ks * weight, tRay);
			col.adds(surf.ks, tcol);
		}
		if (surf.kt * weight > 1e-3) {
			if (hit.enter > 0)
				tRay.D = TransDir(null, surf, I, N);
			else
				tRay.D = TransDir(surf, null, I, N);
			tcol = trace(level + 1, surf.kt * weight, tRay);
			col.adds(surf.kt, tcol);
		}

		// garbaging...
		tcol = null;
		surf = null;

		return col;
	}

	/**
	 * Launches a ray
	 */
	private Vec trace(int level, double weight, Ray r) {
		Vec P, N;
		boolean hit;

		// Checks the recursion level
		if (level > 6) {
			return new Vec();
		}

		hit = intersect(r, 1e6);
		if (hit) {
			P = r.point(inter.t);
			N = inter.prim.normal(P);
			if (Vec.dot(r.D, N) >= 0.0) {
				N.negate();
			}
			return shade(level, weight, P, N, r.D, inter);
		}
		// no intersection --> col = 0,0,0
		return voidVec;
	}
	/*
	public static void main(String argv[]) {

		RayTracer rt = new RayTracer();

		// create the objects to be rendered
		rt.scene = rt.createScene();

		// get lights, objects etc. from scene.
		rt.setScene(rt.scene);

		// Set interval to be rendered to the whole picture
		// (overkill, but will be useful to retain this for parallel versions)
		Interval interval = new Interval(0, rt.width, rt.height, 0, rt.height,
				1, 0);

		// Do the business!
		rt.render(interval);

	}*/
}


capsule Runner (int size, RayTracer[] rts) {
	long checksum1 = 0;
	int datasizes[] = { 150, 500 };
	
	void application() {
		Long[] checksums = new Long[rts.length];
		// set image size
		int width = datasizes[size];
		int height = datasizes[size];
		
		// Init TournamentBarrier
		//br.init();
		
		for(int i = 0; i < rts.length; i++) {
			checksums[i] = rts[i].start(new int_(width), new int_(height));
		}
		
		for(int i = 0; i < rts.length; i++) {
			checksum1 += checksums[i].value();
		}
	}
	
	void validate() {
		System.out.println("validate");
		long refval[] = { 2676692, 29827635 };
		long dev = checksum1 - refval[size];
		if (dev != 0) {
			System.out.println("Validation failed");
			System.out.println("Pixel checksum = " + checksum1);
			System.out.println("Reference value = " + refval[size]);
		} else {
			System.out.println("Validation succeeded");
		}
	}
}

@Parallelism(4)
capsule RayTracerBench {
	design {
		/*sequential*/ Runner runner;
		/*sequential*/ RayTracer rts[10];
		// size = {0,1}
		runner(1, rts);
		//br(10);
		rts[0](0, 10);
		rts[1](1, 10);
		rts[2](2, 10);
		rts[3](3, 10);
		rts[4](4, 10);
		rts[5](5, 10);
		rts[6](6, 10);
		rts[7](7, 10);
		rts[8](8, 10);
		rts[9](9, 10);
	}
	void run() {
		runner.application();
		runner.validate();
	}
}
