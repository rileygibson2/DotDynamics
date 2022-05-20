package core;

public class Vector {
	public double x;
	public double y;
	
	public Vector(double x, double y) {this.x = x; this.y = y;}
	
	public void normalise() {
		double mag = Math.sqrt(Math.pow(x, 2)+Math.pow(y, 2));
		this.x = this.x/mag;
		this.y = this.y/mag;
	}
	
	@Override
	public String toString() {return "{"+x+", "+y+"}";}
}
