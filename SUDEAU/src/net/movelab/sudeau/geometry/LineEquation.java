package net.movelab.sudeau.geometry;

import com.google.maps.android.geometry.Point;

/**
 * Line equation of the form 
 * 
 * y = mx + b
 * 
 * @author a.escobar
 *
 */
public class LineEquation {

	private double b;
	private double m;
	
	public LineEquation(LineSegment l){
		Point p1 = l.getP1();
		Point p2 = l.getP2();
		// y-y'=m(x-x')
		double y1 = p1.y;
		double x1 = p1.x;
		double deltaY = Math.abs(p1.y - p2.y);
		double deltaX = Math.abs(p1.x - p2.x);
		this.m = deltaY / deltaX;		
		this.b = (-m * x1) + y1;		
	}
	
	public LineEquation(Point p, double slope){
		double y1 = p.y;
		double x1 = p.x;
		this.m = slope;
		this.b = (-slope * x1) + y1;
	}
	
	public double getY(double x){
		return m * x + b;
	}
	
	public double getB(){
		return b;
	}
	
	public double getM(){
		return m;
	}
	
}
