package shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import core.Vector;
import shapes.Expression.Operator;

public class Expression {

	public String expression;

	public enum Operator {
		SUM, SUBSUM, SQRT, EXP, PROD, DIV,
		ROOT, //Signal for root of tree
		X, //Provided x value
		Y, //Provided y value

		//Operators below here make expression proportionate to shape it belongs to.
		W, //Width of shape
		H, //Height of shape
		OX, //Origin x of shape
		OY //Origin y of shape
	}

	Shape parent;
	public Vector bounds;
	public enum BoundsAxis {X, Y}
	BoundsAxis boundsAxis; //The axis the bounds apply to

	public Node root; //Root of the expression
	public Node diffRoot; //Root of the differntial of the expression

	public Expression(Shape parent, String expression, Vector bounds, BoundsAxis boundsAxis) {
		this.expression = expression;
		this.parent = parent;
		this.bounds = bounds;
		this.boundsAxis = boundsAxis;

		build(expression);
		differentiate(Operator.X);
		
		printTree("", root);
		System.out.println("END ROOT\nDERIVATIVE");
		printTree("", diffRoot);
		System.out.println("END ROOT\nEND DERIVATIVE\n");
	}

	public double evaluateAtPoint(double x, double y) {
		//Check we are not wasting our time
		switch (boundsAxis) {
		case X:
			if (x<(parent.origin.x+(bounds.x*parent.size)) || x>parent.origin.x+(bounds.y*parent.size)) return Double.MAX_VALUE; break;
		case Y:
			if (y<parent.origin.y+bounds.x*parent.size || y>parent.origin.y+bounds.y*parent.size) return Double.MAX_VALUE; break;
		}

		return dfsForY(new Vector(x, 0), root);
	}

	private double dfsForY(Vector in, Node n) {
		if (n.o==null) return n.constant; //Is just a constant

		//Calculate expression
		switch (n.o) {
		case DIV: return dfsForY(in, n.children.get(0)) / dfsForY(in, n.children.get(1));
		case EXP: return Math.pow(dfsForY(in, n.children.get(0)), dfsForY(in, n.children.get(1)));
		case PROD:
			double result = dfsForY(in, n.children.get(0));
			for (int i=1; i<n.children.size(); i++) {
				result *= dfsForY(in, n.children.get(i));
			}
			return result;
		case SQRT: return Math.sqrt(dfsForY(in, n.children.get(0)));
		case SUBSUM:
			result = dfsForY(in, n.children.get(0));
			for (int i=1; i<n.children.size(); i++) {
				result -= dfsForY(in, n.children.get(i));
			}return result;
		case SUM:
			result = 0;
			for (Node child : n.children) result += dfsForY(in, child);
			return result;
		case X: return in.x;
		case Y: return in.y;
		case OX: return parent.origin.x;
		case OY: return parent.origin.y;
		case W: return parent.size;
		case H: return parent.size;
		case ROOT: return dfsForY(in, n.children.get(0));
		default: break;
		}

		return 0;
	}
	
	public double nSlope;
	
	public Vector getNormalUnitVectorAtPoint(Vector point) {
		double tSlope = derivativeAtPoint(point.x); //Find tangent slope
		double nSlope = -1/tSlope; //Find normal slope
		//Now normal equation is y = nSlope*(x-pos.x)+pos.y
		this.nSlope = nSlope;
		
		//Find two points on normal line
		Vector p1 = new Vector(0, 0);
		Vector p2 = new Vector(10, 0);
		p1.y = nSlope*(p1.x-point.x)+point.y;
		p2.y = nSlope*(p2.x-point.x)+point.y;
		
		//Find normal vector
		Vector normal = new Vector(p2.x-p1.x, p2.y-p1.y);
		normal.normalise(); //Normalise normal vector to get normal unit vector
		
		return normal;
	}

	public double derivativeAtPoint(double x) {
		return dfsForY(new Vector(x, 0), diffRoot);
	}

	public void differentiate(Operator respectTo) {
		diffRoot = dfsForDifferental(root, new Node(), respectTo);
	}

	/**
	 * Creates the new differentiated subtree, by calculatin g next node in
	 * diff tree based on curr node in reg tree. Searches regular tree and 
	 * builds diff tree at the same time.
	 * 
	 * POWER RULE
	 * EXP(subtree1, subtree2)
	 * becomes
	 * PROD(subtree2, EXP(subtree1, SUBSUM(subtree2, -1)))
	 * 
	 * CONSTANT RULE
	 * constants = 0;
	 * 
	 * CONSTANT MULTIPLICATION RULE
	 * if constant's parent == PROD, DIV, where another argument of the parent
	 * is a differntiable function, then the constant stays.
	 * 
	 * @param regN
	 * @param diffN
	 */
	private Node dfsForDifferental(Node reg, Node diff, Operator respectTo) {
		Node next = null;

		//Save time by checking to see if subtree from and including this point is differentiable
		if (subTreeIsConstant(reg, respectTo)) { //Subtree is not undifferentiable
			next = new Node(0);
			return next;
		}

		//At this point, at least one child is differentiable, so all children, even constants have to be respected
		if (reg.isConstantInDerivative(respectTo)) { //If regular node is a constant in this differentation
			next = new Node(reg.constant);
			return next;
		}
		if (reg.o.equals(respectTo)) {
			next = new Node(1);
			return next;
		}

		switch (reg.o) { //If regular node is an operator
		case EXP: //Employ power rule
			//Get nodes from regular tree
			Node root1 = reg.children.get(0);
			Node root2 = reg.children.get(1);
			//Make new nodes
			Node prodNode = new Node(Operator.PROD);
			Node expNode = new Node(Operator.EXP);
			Node sumNode = new Node(Operator.SUM);
			//Insert all nodes into new tree in order
			prodNode.addChild(root2);
			prodNode.addChild(expNode);
			expNode.addChild(root1);
			expNode.addChild(sumNode);
			sumNode.addChild(root2);
			sumNode.addChild(new Node(-1));

			next = prodNode;
			//To evaluate down, use expNoderoot1
			break;

		case SQRT: //Make it an exp with -0.5 power and 0.5 coefficient
			Node root = reg.children.get(0);
			prodNode = new Node(Operator.PROD);
			expNode = new Node(Operator.EXP);
			
			prodNode.addChild(new Node(0.5));
			prodNode.addChild(expNode);
			expNode.addChild(root);
			expNode.addChild(new Node(-0.5));
			
			next = prodNode;
			break;

		case PROD: //Employ product rule
			sumNode = new Node(Operator.SUM);

			for (int i=0; i<reg.children.size(); i++) {
				prodNode = new Node(Operator.PROD);
				for (int z=0; z<reg.children.size(); z++) {
					if (i==z) prodNode.addChild(dfsForDifferental(reg.getChild(z), next, respectTo));
					else prodNode.addChild(reg.getChild(z));
				}
				sumNode.addChild(prodNode);
			}

			next = sumNode;
			break;

		case DIV: //Employ U/V rule
			Node divNode = new Node(Operator.DIV);

			//Denominator
			Node subNode = new Node(Operator.SUBSUM);
			divNode.addChild(subNode);

			prodNode = new Node(Operator.PROD);
			prodNode.addChild(reg.getChild(1));
			prodNode.addChild(dfsForDifferental(reg.getChild(0), next, respectTo));
			subNode.addChild(prodNode);

			prodNode = new Node(Operator.PROD);
			prodNode.addChild(reg.getChild(0));
			prodNode.addChild(dfsForDifferental(reg.getChild(1), next, respectTo));
			subNode.addChild(prodNode);

			//Numerator
			if (reg.children.size()>1) {
				expNode = new Node(Operator.EXP);
				expNode.addChild(reg.getChild(1));
				expNode.addChild(new Node(2));
				divNode.addChild(expNode);
			}
			next = divNode;
			break;

		default: 
			next = new Node(reg.o);
			for (Node regChild : reg.children) {
				next.addChild(dfsForDifferental(regChild, next, respectTo));
			}
			break;
		}

		return next;
	}

	public boolean subTreeIsConstant(Node n, Operator respectTo) {
		if (n.o!=null) if (n.o.equals(respectTo)) return false;
		for (Node child : n.children) {
			if (!subTreeIsConstant(child, respectTo)) return false;
		}
		return true;
	}

	private void build(String expression) {
		char[] arr = expression.toCharArray();
		String temp = "";
		root = new Node();
		Stack<Node> stack = new Stack<>();
		stack.push(root);

		//Special case - expression is a single constant
		if (!expression.contains("(")) root.addChild(makeNode(expression));

		//Go through expression and build tree
		for (int i=0; i<arr.length; i++) {
			if (arr[i]==' ') continue;
			if (arr[i]=='(') { //Evaluate and move down one level
				Node n = makeNode(temp);
				stack.peek().addChild(n);
				stack.push(n);
				temp = "";
			}
			else if (arr[i]==')') { //Evaluate and move back up a level
				if (!temp.isBlank()) {
					Node n = makeNode(temp);
					stack.peek().addChild(n);
				}
				stack.pop();
				temp = "";
			}
			else if (arr[i]==',') { //Evaluate at current level
				if (!temp.isBlank()) {
					Node n = makeNode(temp);
					stack.peek().addChild(n);
					temp = "";
				}
			}
			else temp += arr[i];
		}
	}

	private Node makeNode(String op) {
		Operator operator = null;
		for (Operator o : Operator.values()) {
			if (op.equals(o.toString())) operator = o;
		}

		if (operator==null) return new Node(Double.parseDouble(op));
		else return new Node(operator);
	}

	public boolean inBounds(int x, int y) {
		switch (boundsAxis) {
		case X:
			if (x>=(parent.origin.x+(bounds.x*parent.size)) && x<=parent.origin.x+(bounds.y*parent.size)) return true;
			else return false;
		case Y:
			if (y>=parent.origin.y+bounds.x*parent.size && y<=parent.origin.y+bounds.y*parent.size) return true;
			else return false;
		}
		return false;
	}

	public void printTree(String indent, Node n) {
		if (n.o==null) System.out.println(indent+n.constant);
		else System.out.println(indent+n.o.toString());

		for (Node c : n.children) printTree(indent+"    ", c);
	}

	private class Node {
		Operator o;
		double constant;
		List<Node> children = new ArrayList<>();

		public Node() {this.o = Operator.ROOT;}
		public Node(Operator o) {this.o = o;}
		public Node(double constant) {this.constant = constant;}

		public void addChild(Node child) {children.add(child);}

		public Node getChild(int i) {return children.get(i);}

		public boolean isConstantInDerivative(Operator respectTo) {
			if (o==null) return true;
			switch (o) {
			case H:
			case OX:
			case OY:
			case W:
			case X:
			case Y: if (!o.equals(respectTo)) return true;
			default: return false;
			}
		}

		@Override
		public String toString() {
			if (o==null) return constant+"";
			return o.toString();
		}
	}
}
