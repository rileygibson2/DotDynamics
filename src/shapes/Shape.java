package shapes;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import core.Render;
import core.Vector;
import shapes.Expression.BoundsAxis;

public class Shape {

	public Rectangle bounds;
	public Set<Expression> borders;

	public Shape(Rectangle bounds) {
		this.bounds = bounds;
		this.borders = new HashSet<Expression>();
	}

	public void addBorder(String expression, Vector bounds, BoundsAxis boundsAxis) {
		borders.add(new Expression(this, expression, bounds, boundsAxis));
	}

	public boolean isTouchingBorder(double x, double y) {
		if (Render.console) System.out.println();
		int tolerance = 5;

		for (Expression e : borders) {
			double eY = e.evaluateForY(x, y);

			if (Render.console) {
				System.out.println(e.expression+"  "+e.bounds.toString());
				if (eY==Double.MAX_VALUE) System.out.println("out of bounds");
				else System.out.println("x: "+x+" y: "+y+" e: "+eY+" ");
			}

			if (eY==Double.MAX_VALUE) continue; //Over bounds for expression
			//Evaluate with a scope, so overshooting does not affect result
			if (y>=eY-tolerance&&y<=eY+tolerance) return true;
		}

		return false;
	}
}
