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
		
		//Shape outline
		//drawShapes(g);

		//Dots
		g.setColor(Color.WHITE);
		for (Dot d : c.dots) {
			g.fillOval(d.getPos().x-(d.getSize()/2), (Render.sH-d.getPos().y)-(d.getSize()/2), d.getSize(), d.getSize());
		}
		
		//Painting indicator
		drawIndicator(g);
	}

	private void drawIndicator(Graphics2D g) {
		if (pI.getBlue()+pIDir>255||pI.getBlue()+pIDir<0) pIDir = -pIDir;
		pI = new Color(pI.getRed()-pIDir, 0, pI.getBlue()+pIDir);
		g.setColor(pI);
		g.fillOval(c.sW-30, 10, 15, 15);
	}

	private void drawShapes(Graphics2D g) {
		g.setColor(new Color(0, 255, 100, 100));
		for (Shape s : c.shapes) {
			for (int y=s.bounds.y; y<s.bounds.y+s.bounds.height; y++) {
				for (int x=s.bounds.x; x<s.bounds.x+s.bounds.width; x++) {
					for (Expression e : s.borders) {
						if (e.inBounds(x, y)) {
							g.fillRect(x, (int) (Render.sH-e.evaluateForY(x, y)), 1, 1);
						}
					}
				}
			}
		}
	}
}
