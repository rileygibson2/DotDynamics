package shapes;

import java.util.HashSet;
import java.util.Set;

import core.Render;
import core.Vector;
import shapes.Expression.BoundsAxis;

public class Shape {

	public Vector origin;
	public double size;
	public Set<Expression> borders;
	
	//Border touching tolerance
	public static int tolerance = 5;

	public Shape(Vector origin, double size) {
		this.origin = origin;
		this.size = size;
		this.borders = new HashSet<Expression>();
	}

	public void addBorder(String expression, Vector bounds, BoundsAxis boundsAxis) {
		borders.add(new Expression(this, expression, bounds, boundsAxis));
	}

	public int isTouchingBorder(Vector pos) {
		int touchingCount = 0;
		
		for (Expression e : borders) {
			double eY = e.evaluateAtPoint(pos.x, pos.y);
			if (eY==Double.MAX_VALUE) continue; //Over bounds for expression
			
			//Evaluate with a tolerance, so overshooting does not affect result
			if (pos.y>=eY-tolerance&&pos.y<=eY+tolerance) touchingCount++;
		}

		return touchingCount;
	}
	
	public Expression touchingBorder(Vector pos) {
		for (Expression e : borders) {
			double eY = e.evaluateAtPoint(pos.x, pos.y);

			if (Render.console) {
				System.out.println(e.expression+"  "+e.bounds.toString());
				if (eY==Double.MAX_VALUE) System.out.println("out of bounds");
				else System.out.println("x: "+pos.x+" y: "+pos.y+" e: "+eY+" ");
			}

			if (eY==Double.MAX_VALUE) continue; //Over bounds for expression
			//Evaluate with a scope, so overshooting does not affect result
			if (pos.y>=eY-tolerance&&pos.y<=eY+tolerance) return e;
		}
		
		return null;
	}
}
