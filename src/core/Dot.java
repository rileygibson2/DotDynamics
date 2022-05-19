package core;

import java.awt.Point;
import java.awt.Rectangle;

import shapes.Shape;

public class Dot {
	
	private Point pos;
	private int size;
	private Vector vec;
	private double speed = 1;
	
	private boolean trapped = false;
	private Shape trappingShape;

	public Dot(Point pos, int size, Vector vec) {
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
		this.pos = new Point(x, y);
		this.size = (int) Math.floor(Math.random()*(sizeRange.y-sizeRange.x+1)+sizeRange.x);
		this.vec = new Vector(Render.random(1, 5), Render.random(1, 5));
	}
	
	public void move() {
		Point newPos = new Point(pos.x, pos.y);
		
		newPos.x += vec.x*speed;
		newPos.y += vec.y*speed;
		
		if (newPos.x>=Render.sW) vec = reflectAbout(new Vector(-1, 0));
		if (newPos.x<=0) vec = reflectAbout(new Vector(1, 0));
		if (newPos.y>=Render.sH) vec = reflectAbout(new Vector(0, -1));
		if (newPos.y<=0) vec = reflectAbout(new Vector(0, 1));
		
		else pos = newPos;
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
	
	public void trap(Shape s) {
		trapped = true;
		trappingShape = s;
	}
	
	public void changePos(int xChange, int yChange) {
		this.pos.x += xChange;
		this.pos.y += yChange;
	}
	
	//Getters and setters
	
	public void setPos(Point newPos) {this.pos = newPos;}
	
	public void setVec(Vector newVec) {this.vec = newVec;}
	
	public void setSpeed(double speed) {this.speed = speed;}
	
	public Point getPos() {return this.pos;}
	
	public Vector getVec() {return this.vec;}
	
	public int getSize() {return this.size;}
	
	public double getSpeed() {return this.speed;}
	
	public boolean isTrapped() {return this.trapped;}
}
