package core;

import java.awt.Color;
import java.awt.Graphics2D;

import shapes.Expression;
import shapes.Shape;

public class Screen {

	Render c;

	//Stuff for a painting icon to see if a regular paint is occuring
	Color pI = new Color(255, 0, 0);
	int pIDir = 15;

	public Screen(Render c) {
		this.c = c;
	}

	public void draw(Graphics2D g) {
		//Background
		g.clearRect(0, 0, c.sW, c.sH);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, c.sW, c.sH);

		//Dots
		g.setColor(Color.WHITE);
		for (Dot d : c.dots) {
			g.fillOval((int) (d.getPos().x-(d.getSize()/2)), (int) ((Render.sH-d.getPos().y)-(d.getSize()/2)), d.getSize(), d.getSize());
		}
		
		//Diagnostic stuff
		drawShapes(g);
		//drawNormals(g);
		drawIndicator(g);
	}

	private void drawIndicator(Graphics2D g) {
		if (pI.getBlue()+pIDir>255||pI.getBlue()+pIDir<0) pIDir = -pIDir;
		pI = new Color(pI.getRed()-pIDir, 0, pI.getBlue()+pIDir);
		g.setColor(pI);
		g.fillOval(c.sW-30, 10, 15, 15);
	}

	private void drawShapes(Graphics2D g) {
		g.setColor(new Color(0, 255, 100));
		for (Shape s : c.shapes) {
			for (int y=(int) s.origin.y; y<s.origin.y+s.size; y++) {
				for (int x=(int) s.origin.x; x<s.origin.x+s.size; x++) {
					for (Expression e : s.borders) {
						if (e.inBounds(x, y)) {
							g.fillRect(x, (int) (Render.sH-e.evaluateAtPoint(x, y)), 1, 1);
						}
					}
				}
			}
		}
	}
	
	private void drawNormals(Graphics2D g) {
		for (Shape s : c.shapes) {
			for (Expression e : s.borders) {
				//Pick point in expression
				Vector point = new Vector(s.origin.x+(e.bounds.x*s.size)+((e.bounds.y-e.bounds.x)*s.size)/2, 0);
				point.y = e.evaluateAtPoint(point.x, 0);
				g.fillRect((int) point.x, (int) (Render.sH-point.y), 2, 2);
				Vector n = e.getNormalUnitVectorAtPoint(point);
				
				g.setColor(new Color(255, 0, 0, 3));
				double nY = 0;
				for (int y=(int) s.origin.y; y<s.origin.y+s.size; y++) {
					for (int x=(int) (s.origin.x+(e.bounds.x*s.size)); x<(int) (s.origin.x+(e.bounds.y*s.size)); x++) {
						nY = e.nSlope*(x-point.x)+point.y;
						g.fillRect(x, (int) (Render.sH-nY), 1, 1);
					}
				}
				
				g.setColor(Color.BLUE);
				g.drawLine((int) point.x, (int) (Render.sH-point.y), (int) (point.x+(50*n.x)), (int) (Render.sH-(point.y+(50*n.y))));
			}
		}
		
	}
}
