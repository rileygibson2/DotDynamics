package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

import shapes.Expression;
import shapes.Expression.BoundsAxis;
import shapes.Expression.Operator;
import shapes.Shape;

public class Render extends JPanel implements KeyListener {

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
			{ //TILTED SQUARE
				//xShift, yShift, size (w & h)
				{"200", "400", "200"},
				//expression, minBound, maxBound, boundAxis
				{"SUM(X, DIV(W, 2), PROD(-1, OX), OY)", "0", "0.5", "X"},
				{"SUM(PROD(-1, X), PROD(1.5, W), OX, OY)", "0.5", "1", "X"},
				{"SUBSUM(X, DIV(W, 2), OX, PROD(-1, OY))", "0.5", "1", "X"},
				{"SUM(PROD(-1, X), DIV(W, 2), OX, OY)", "0", "0.5", "X"}
			}
			/*{ //CIRCLE
				{"200", "150", "100", "100"},
				{"SUM(PROD(1000, SUBSUM(X, OX)), OY)", "0", "0.001", "X"},
				{"SUM(W, OY)", "0", "0.35", "X"},
				{"SUM(PROD(0.5, W), OY)", "0", "0.35", "X"},
				{"SUM(SQRT(SUBSUM(EXP(PROD(0.25, W), 2), EXP(SUBSUM(X, SUBSUM(PROD(0.6, W), PROD(0.25, W), PROD(-1, OX))), 2))), PROD(0.75, W), OY)", "0.35", "1", "X"},
				{"SUM(PROD(-1, SQRT(SUBSUM(EXP(PROD(0.25, W), 2), EXP(SUBSUM(X, SUBSUM(PROD(0.6, W), PROD(0.25, W), PROD(-1, OX))), 2)))), PROD(0.75, W), OY)", "0.35", "1", "X"},
				{"SUM(PROD(-1.25, SUBSUM(X, OX)), W, OY)", "0.4", "0.8", "X"}
			}*/
	};

	public void initialise() {
		dots = new HashSet<Dot>();
		shapes = new HashSet<Shape>();

		makeShapes();
	}


	static boolean trapInstantly = false;
	boolean stopWhenTrapped = false;

	public void cycleDots() {
		for (Dot d : dots) {
			if (stopWhenTrapped&&d.isTrapped()) continue;
			
			d.move(); //Move dot
			for (Shape s : shapes) { //Check whether to trap dot
				int touchingCount = s.isTouchingBorder(d.getPos());
				if (touchingCount>0) {
					d.hitShape(s, touchingCount);
				}
			}
		}
	}

	public void makeShapes() {
		for (int s=0; s<shapeData.length; s++) {
			Shape shape = new Shape(new Vector(Integer.parseInt(shapeData[s][0][0]), Integer.parseInt(shapeData[s][0][1])), Double.parseDouble(shapeData[s][0][2]));

			for (int b=1; b<shapeData[s].length; b++) {
				Vector bounds = new Vector(Double.parseDouble(shapeData[s][b][1]), Double.parseDouble(shapeData[s][b][2]));
				BoundsAxis bA;
				if (shapeData[s][b][3].equals("X")) bA = BoundsAxis.X;
				else bA = BoundsAxis.Y;
				shape.addBorder(shapeData[s][b][0], bounds, bA);
			}

			//shape.size = 400;
			shapes.add(shape);
		}
	}

	public void makeDots() {
		//System.out.println("\n\n\n\n");
		int am = 100;
		//for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(690, 690, 10, 10), sizeRange));
		//for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(0, 0, 10, 10), sizeRange));
		//for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(690, 0, 10, 10), sizeRange));
		//for (int i=0; i<am; i++) dots.add(new Dot(new Rectangle(0, 690, 10, 10), sizeRange));

		for (int i=0; i<1; i++) dots.add(new Dot(new Rectangle(295, 495, 10, 10), sizeRange));
		for (Dot d : dots) d.setSpeed(5);
		
		//Dot d = new Dot(new Vector(206d, 500), 8, new Vector(1, 1));
		//d.setSpeed(5);
		//dots.add(d);
	}

	public static double dotProduct(Vector a, Vector b) {
		return (a.x*b.x)+(a.y*b.y);
	}

	public static double random(double min, double max){
		return (Math.random()*((max-min)))+min;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_D :
			dots.clear();
			makeDots();
			break;
		case KeyEvent.VK_C : 
			dots.clear();
			break;
		case KeyEvent.VK_R :
			dotsRunning = !dotsRunning;
			break;
		default : break;
		}

	}

	@Override
	public void paintComponent(Graphics g) {
		s.draw((Graphics2D) g);
	}

	public Render() {
		Render.s = new Screen(this);
		Render.painter = new Painter(this);
		addKeyListener(this);
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

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}
