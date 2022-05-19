package shapes;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import core.Vector;

public class Expression {

	public String expression;

	enum Operator {
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

		root = build(expression);
		printTree("", root);
		System.out.println("END ROOT\n");
	}

	public double evaluateForY(double x, double y) {
		//Check we are not wasting our time
		switch (boundsAxis) {
		case X:
			if (x<=(parent.bounds.x+(bounds.x*parent.bounds.width)) || x>=parent.bounds.x+(bounds.y*parent.bounds.width)) return Double.MAX_VALUE; break;
		case Y:
			if (y<=parent.bounds.y+bounds.x*parent.bounds.width || y>=parent.bounds.y+bounds.y*parent.bounds.width) return Double.MAX_VALUE; break;
		}

		return dfsForY(new Vector(x, 0), root);
	}

	private double dfsForY(Vector in, Node n) {
		if (n.isConstant()) return n.constant; //Is just a constant
		
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
		case OX: return parent.bounds.x;
		case OY: return parent.bounds.y;
		case W: return parent.bounds.width;
		case H: return parent.bounds.height;
		case ROOT: return dfsForY(in, n.children.get(0));
		default: break;
		}

		return 0;
	}

	public double evaluateForDiffY(double x) {
		return dfsForY(new Vector(x, 0), diffRoot);
	}
	
	public void differentiateExpression() {
		diffRoot = dfsForDifferental(root, new Node());
		
		printTree("", diffRoot);
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
	private Node dfsForDifferental(Node reg, Node diff) {
		Node next = null;
		
		//If current regular tree node is a constant
		if (reg.isConstant()) {
			next = new Node(reg.constant);
			for (Node regChild : reg.children) {
				next.addChild(dfsForDifferental(regChild, next));
			}
			return next;
		}
		
		//If current regular tree node is an operator
		switch (reg.o) {
		case EXP: 
			//Get nodes from regular tree
			Node root1 = reg.children.get(0);
			Node root2 = reg.children.get(1);
			//Make new nodes
			Node prodNode = new Node(Operator.PROD);
			Node expNode = new Node(Operator.EXP);
			Node sumNode = new Node(Operator.SUM);
			Node constNode = new Node(-1);
			//Insert all nodes into new tree in order
			prodNode.addChild(root2);
			prodNode.addChild(expNode);
			expNode.addChild(root1);
			expNode.addChild(sumNode);
			sumNode.addChild(root2);
			sumNode.addChild(constNode);
			
			next = prodNode;
			break;
			
		default: 
			next = new Node(reg.o);
			for (Node regChild : reg.children) {
				next.addChild(dfsForDifferental(regChild, next));
			}
			break;
		}
		
		return next;
	}

	private Node build(String expression) {
		char[] arr = expression.toCharArray();
		String temp = "";
		Node root = new Node();
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

		return root;
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
			if (x>=(parent.bounds.x+(bounds.x*parent.bounds.width)) && x<=parent.bounds.x+(bounds.y*parent.bounds.width)) return true;
			else return false;
		case Y:
			if (y>=parent.bounds.y+bounds.x*parent.bounds.width && y<=parent.bounds.y+bounds.y*parent.bounds.width) return true;
			else return false;
		}
		return false;
	}

	public void printTree(String indent, Node n) {
		if (n.isConstant()) System.out.println(indent+n.constant);
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


		public boolean isConstant() {
			if (o==null) return true;
			return false;
		}

		@Override
		public String toString() {
			if (o==null) return constant+"";
			return o.toString();
		}
	}
}
