package core;

import java.awt.Point;
import java.awt.Rectangle;

import shapes.Expression;
import shapes.Shape;

public class Dot {
	
	private Vector pos;
	private int size;
	private Vector vec;
	private double speed = 1;
	
	private boolean trapped = false;
	private Shape trappingShape;

	public Dot(Vector pos, int size, Vector vec) {
		this.pos = pos;
		this.size = size;
		this.vec = vec;
	}
	
	/**
	 * Make dot at some random place inside set bounds and make it
	 * some random size, but provide the initial vector.
	 * 
	 * @param bounds
	 * @param sizeRange
	 * @param vec
	 */
	public Dot(Rectangle bounds, Point sizeRange) {
		int x = (int) Math.floor(Math.random()*((bounds.x+bounds.width)-bounds.x+1)+bounds.x);
		int y = (int) Math.floor(Math.random()*((bounds.y+bounds.height)-bounds.y+1)+bounds.y);
		this.pos = new Vector(x, y);
		this.size = (int) Math.floor(Math.random()*(sizeRange.y-sizeRange.x+1)+sizeRange.x);
		this.vec = new Vector(Render.random(-1, 1), Render.random(-1, 1));
		this.speed = Render.random(1, Shape.tolerance);
	}
	
	public void move() {
		Vector newPos = new Vector(pos.x, pos.y);
		
		newPos.x += vec.x*speed;
		newPos.y += vec.y*speed;
		
		if (newPos.x>=Render.sW) vec = reflectAbout(new Vector(-1, 0));
		else if (newPos.x<=0) vec = reflectAbout(new Vector(1, 0));
		else if (newPos.y>=Render.sH) vec = reflectAbout(new Vector(0, -1));
		else if (newPos.y<=0) vec = reflectAbout(new Vector(0, 1));
		else pos = newPos;
		
		//System.out.println(vec);
	}
	
	public void trap(Shape s) {
		trapped = true;
		trappingShape = s;
		System.out.println("trapping "+this+" at "+pos.toString());
	}
	
	public void hitShape(Shape s, int touchingCount) {
		if (Render.trapInstantly && !trapped) trap(s);
		else bounce(s.touchingBorder(pos), touchingCount);
	}
	
	public void bounce(Expression e, int touchingCount) {
		if (touchingCount>1) { //Touching an intersect of two lines
			vec.x = -vec.x;
			vec.y = -vec.y;
			return;
		}

		Vector unitNormal = e.getNormalUnitVectorAtPoint(pos);
		/*
		 * Check whether dot is just sliding up the wall
		 * ball is in tolerence to register as a hit, but
		 * if it is it right angles to the normal, then it
		 * is in range but not actually hitting, its
		 * actually sliding up wall.
		 */
		if (Render.dotProduct(vec, unitNormal)==0) {
			System.out.println("FALSE BOUNCE"+touchingCount);
			return;
		}
		if (Render.dotProduct(vec, unitNormal)>0) { //Check to see that vectors are facing one and other and flip if they arent
			unitNormal.x = -unitNormal.x;
			unitNormal.y = -unitNormal.y;
		}
		
		this.vec = reflectAbout(unitNormal); //Reflect about normal
		vec.normalise(); //Normalise the vector
		
		move();
		//System.out.println("BOUNCE "+touchingCount);
	}
	
	public Vector reflectAbout(Vector normal) {
		/*
		 * R = V-2ProjNV
		 * ProjNV = (V.N/N.N)N
		*/
		double dPdivdP = ((vec.x*normal.x)+(vec.y*normal.y))/((normal.x*normal.x)+(normal.y*normal.y));
		Vector projNV = new Vector(dPdivdP*normal.x, dPdivdP*normal.y);
		vec = new Vector((-2*projNV.x)+vec.x, (-2*projNV.y)+vec.y);
		return vec;
	}
	
	//Getters and setters
	
	public void setPos(Vector newPos) {this.pos = newPos;}
	
	public void setVec(Vector newVec) {this.vec = newVec;}
	
	public void setSpeed(double speed) {this.speed = speed;}
	
	public Vector getPos() {return this.pos;}
	
	public Vector getVec() {return this.vec;}
	
	public int getSize() {return this.size;}
	
	public double getSpeed() {return this.speed;}
	
	public boolean isTrapped() {return this.trapped;}
}
