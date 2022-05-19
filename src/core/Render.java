package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import shapes.Expression;
import shapes.Expression.BoundsAxis;
import shapes.Shape;

public class Render extends JPanel {

	public static JFrame frame;
	public static final int sW = 700;
	public static final int sH = 700;
	public static boolean console = false;

	static Screen s;
	static Painter painter;

	public boolean paint = false;

	Set<Dot> dots;
	Point sizeRange = new Point(2, 10);
	public boolean dotsRunning = true;

	Set<Shape> shapes;
	
	String[][][] shapeData = {
			/*{ //TILTED SQUARE
				//expression, minBound, maxBound, boundAxis
				{"200", "200", "100", "100"}, //First row is bounding box of overall shape
				{"SUM(X, DIV(W, 2), PROD(-1, OX), OY)", "0", "0.5", "X"},
				{"SUM(PROD(-1, X), PROD(1.5, W), OX, OY)", "0.5", "1", "X"},
				{"SUBSUM(X, DIV(W, 2), OX, PROD(-1, OY))", "0.5", "1", "X"},
				{"SUM(PROD(-1, X), DIV(W, 2), OX, OY)", "0", "0.5", "X"}
			},
			{ //CIRCLE
				{"200", "150", "100", "100"},
				{"SUM(PROD(1000, SUBSUM(X, OX)), OY)", "0", "0.001", "X"},
				{"SUM(W, OY)", "0", "0.35", "X"},
				{"SUM(PROD(0.5, W), OY)", "0", "0.35", "X"},
				{"SUM(SQRT(SUBSUM(EXP(PROD(0.25, W), 2), EXP(SUBSUM(X, SUBSUM(PROD(0.6, W), PROD(0.25, W), PROD(-1, OX))), 2))), PROD(0.75, W), OY)", "0.35", "1", "X"},
				{"SUM(PROD(-1, SQRT(SUBSUM(EXP(PROD(0.25, W), 2), EXP(SUBSUM(X, SUBSUM(PROD(0.6, W), PROD(0.25, W), PROD(-1, OX))), 2)))), PROD(0.75, W), OY)", "0.35", "1", "X"},
				{"SUM(PROD(-1.25, SUBSUM(X, OX)), W, OY)", "0.4", "0.8", "X"}
			},*/
			{ //TEST
				{"0", "0", "100", "100"},
				{"SUM(PROD(12, EXP(X, 3)), PROD(7, X)", "0", "1", "X"}
			}
	};

	public void initialise() {
		dots = new HashSet<Dot>();
		shapes = new HashSet<Shape>();
		
		makeShapes();
		//makeDots();
		
		for (Shape s : shapes) {
			for (Expression ex : s.borders) {
				System.out.println(ex.evaluateForY(2, 0));
				ex.differentiateExpression();
				System.out.println(ex.evaluateForDiffY(2));
			}
		}
	}

	public void cycleDots() {
		for (Dot d : dots) {
			if (!d.isTrapped()) {
				d.move(); //Move dot
				for (Shape s : shapes) { //Check whether to trap dot
					if (s.isTouchingBorder(d.getPos().x, d.getPos().y)) {
						d.trap(s);
						System.out.println("trapping "+d+" at "+d.getPos().toString());
					}
				}
			}
		}
	}

	public void makeShapes() {
		for (int s=0; s<shapeData.length; s++) {
			Shape shape = new Shape(new Rectangle(Integer.parseInt(shapeData[s][0][0]), Integer.parseInt(shapeData[s][0][1]), Integer.parseInt(shapeData[s][0][2]), Integer.parseInt(shapeData[s][0][3])));

			for (int b=1; b<shapeData[s].length; b++) {
				Vector bounds = new Vector(Double.parseDouble(shapeData[s][b][1]), Double.parseDouble(shapeData[s][b][2]));
				BoundsAxis bA;
				if (shapeData[s][b][3].equals("X")) bA = BoundsAxis.X;
				else bA = BoundsAxis.Y;
				shape.addBorder(shapeData[s][b][0], bounds, bA);
			}
			
			shape.bounds.width = 400;
			shapes.add(shape);
		}
	}

	public void makeDots() {
		int am = 100;
		for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(600, 600, 100, 100), sizeRange));
		for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(0, 0, 100, 100), sizeRange));
		for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(600, 0, 100, 100), sizeRange));
		for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(0, 600, 100, 100), sizeRange));
		
		//dots.add(new Dot(new Point(300, 300), 8, new Vector(-1, -1)));
	}
	
	public static double random(double min, double max){
		return ((Math.random()*((max-min)+1))+min);
	}

	@Override
	public void paintComponent(Graphics g) {
		s.draw((Graphics2D) g);
	}

	public Render() {
		Render.s = new Screen(this);
		Render.painter = new Painter(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		initialise();
		this.paint = true;
		painter.start();
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				frame = new JFrame();
				Render panel = new Render();
				panel.setPreferredSize(new Dimension(sW, sH));
				frame.getContentPane().add(panel);


				//Label and build
				frame.setTitle("Dot Dynamics");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				//Finish up
				frame.setVisible(true);
				frame.pack();
			}
		});
	}
}
