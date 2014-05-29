package net.movelab.sudeau.geometry;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import net.movelab.sudeau.model.Step;

public class SnapCalculatorV1 {

	private SphericalMercatorProjection sp;
	private LineSegment perpendicularToNearest;

	public SnapCalculatorV1() {
		sp = new SphericalMercatorProjection(6371);
	}

	/**
	 * Provided a screen tap from the user and a stepsInProgress, this function
	 * transforms the tap to a geographical position and attempts to snap to the
	 * track in progress. In some cases the snap will simply return the latest
	 * vertex in the track; if the tap happens between two points it will
	 * attempt to snap to the nearest segment (arc between two vertexes)
	 * 
	 * @param stepsInProgress
	 *            the track which is being built
	 * @param screenTap
	 *            the geographical coordinates of the tap performed by the user
	 * @return LatLng snapped to trackInProgress
	 * @throws TransformException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 */
	public LatLng snapToCurrentTrack(List<Step> stepsInProgress,
			LatLng screenTap) {
		Step nearestToTap = getNearestStepToPoint(stepsInProgress, screenTap);
		Step lastStep = getLastStep(stepsInProgress);
 		if (nearestToTap.equals(lastStep)) {
			// Case 1 (snap to end of track) happens when the last step is the
			// nearest point to
			// screenTap
			return new LatLng(lastStep.getLatitude(), lastStep.getLongitude());
		} else {
			// Case 2 happens when the last step is not the nearest to the
			// screen tap
			List<LineSegment> segments;
			Point intersection = null;
			segments = stepsToLineStrings(stepsInProgress);
			LineSegment nearestSegment = nearestLineString(screenTap, segments);
			Point tapPoint = sp.toPoint(screenTap);
			if( segmentIsCompletelyHorizontal(nearestSegment) ){
				//Intersection is such that
				//y is value of horizontal segment, x is x of tap
				intersection = new Point(tapPoint.x, nearestSegment.getP1().y);
			}else if( segmentIsCompletelyVertical(nearestSegment) ){
				//Intersection is such that
				//y is value of tap, x is x of horizontal segment
				intersection = new Point(nearestSegment.getP1().x,tapPoint.y);
			}else{
				double slope = getLineSegmentSlope(nearestSegment);				
				double perpendicularSlope = negativeReciprocalSlope(slope);
				LineEquation perpendicular = new LineEquation(tapPoint, perpendicularSlope);
				LineEquation segment = new LineEquation(nearestSegment);
				Log.d("Intersection","Line segment params - m=" + segment.getM() + " b=" + segment.getB());
				Log.d("Intersection","Perpendicular segment params - m=" + perpendicular.getM() + " b=" + perpendicular.getB());
				Log.d("Intersection","Tap on screen - x=" + tapPoint.x + " y=" + tapPoint.y);
				intersection = lineIntersection(perpendicular, segment);
				Log.d("Intersection","Intersection at x=" + intersection.x + " y=" + intersection.y);				
				perpendicularToNearest = new LineSegment(tapPoint,intersection);				
			}
			return sp.toLatLng(intersection);		
		}		
	}

	private LineSegment perpendicularPassesThroughPoint(LineSegment segment,
			LatLng screenTap) {
		//Screen tap
		Point c0 = sp.toPoint(screenTap);
		Point c1;		
		double lineSlope = getLineSegmentSlope(segment);
		double perpSlope = negativeReciprocalSlope(lineSlope);			
		double c1_x = c0.x;
		double c1_y = getYValue(perpSlope, c1_x, c0);
		c1 = new Point(c1_x, c1_y);
		return new LineSegment(c0, c1);
	}
	
	private boolean segmentIsCompletelyVertical(LineSegment segment){
		Point p1 = segment.getP1();
		Point p2 = segment.getP2();
		if(p1.x==p2.x){
			return true;
		}
		return false;
	}
	
	private boolean segmentIsCompletelyHorizontal(LineSegment segment){
		Point p1 = segment.getP1();
		Point p2 = segment.getP2();
		if(p1.y==p2.y){
			return true;
		}
		return false;
	}

	/**
	 * y = mx + b
	 * 
	 * @param ls
	 */
	private double[] getLineSegmentEquation(LineSegment ls) {
		double[] retVal = new double[2];
		Point p1 = ls.getP1();
		// y-y'=m(x-x')
		double y1 = p1.y;
		double x1 = p1.x;
		double m = getLineSegmentSlope(ls);
		double b = (-m * x1) + y1;
		retVal[0] = m;
		retVal[1] = b;
		return retVal;
	}
	
	
	private Point lineIntersection(LineEquation le1, LineEquation le2) {
		double m1 = le1.getM();
		double m2 = le2.getM();
		double b1 = le1.getB();
		double b2 = le2.getB();
		double x0 = -(b1 - b2) / (m1 - m2);
		double y0 = le2.getY(x0);
		return new Point(x0, y0);
	}

	private Point lineSegmentIntersection(LineSegment l1, LineSegment l2) {
		double[] equationL1 = getLineSegmentEquation(l1);
		double[] equationL2 = getLineSegmentEquation(l2);
		double m1 = equationL1[0];
		double m2 = equationL2[0];
		double b1 = equationL1[1];
		double b2 = equationL2[1];
		double x0 = -(b1 - b2) / (m1 - m2);
		double y0 = getYValue(m1, x0, l1.getP1());
		return new Point(x0, y0);
	}

	/**
	 * Computes the yValue in a line that passes through pointInLine with a
	 * given slope for a given xValue
	 * 
	 * @param slope
	 * @param xValue
	 * @param pointInLine
	 * @return
	 */
	private double getYValue(double slope, double xValue, Point pointInLine) {
		return slope * (xValue - pointInLine.x) + pointInLine.y;
	}

	/**
	 * LineString will always have 2 coordinates
	 * 
	 * @param segment
	 * @return
	 */
	private double getLineSegmentSlope(LineSegment segment) {
		Point p1 = segment.getP1();
		Point p2 = segment.getP2();
		double deltaY = Math.abs(p1.y - p2.y);
		double deltaX = Math.abs(p1.x - p2.x);
		return deltaY / deltaX;
	}

	/**
	 * Negative reciprocal of slope x is the slope of a perpendicular line
	 * 
	 * @param slope
	 * @return
	 */
	private double negativeReciprocalSlope(double slope) {
		return -(1 / slope);
	}

	/**
	 * Minimal distance between latlng and a segment represented by two
	 * coordinates
	 * 
	 * @param tap
	 * @param s
	 * @return
	 */
	private double distanceToLineSegment(LatLng tap, LineSegment s) {
		Point a = s.getP1();
		Point b = s.getP2();
		Point p = sp.toPoint(tap);
		double normalLength = Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y)
				* (b.y - a.y));
		return Math.abs((p.x - a.x) * (b.y - a.y) - (p.y - a.y) * (b.x - a.x))
				/ normalLength;
	}

	private Step getLastStep(List<Step> stepsInProgress) {
		return stepsInProgress.get(stepsInProgress.size() - 1);
	}

	private Step getNearestStepToPoint(List<Step> stepsInProgress,
			LatLng screenTap) {
		double min = Double.MAX_VALUE;
		Step retVal = null;
		for (int i = 0; i < stepsInProgress.size(); i++) {
			Step s = stepsInProgress.get(i);
			double dist = distanceBetweenPoints(screenTap,
					new LatLng(s.getLatitude(), s.getLongitude()));
			if (dist < min) {
				min = dist;
				retVal = s;
			}
		}
		return retVal;
	}

	/**
	 * Distance between 2 lat longs. Previously performs transform to cartesian
	 * coordinates
	 * 
	 * @param latLng1
	 * @param latLng2
	 * @return
	 */
	private double distanceBetweenPoints(LatLng latLng1, LatLng latLng2) {
		return SphericalUtil.computeDistanceBetween(latLng1, latLng2);
	}

	//
	/**
	 * The size of steps must be at least 2
	 * 
	 * @param steps
	 * @return
	 * @throws TransformException
	 * @throws FactoryException
	 * @throws MismatchedDimensionException
	 */
	private List<LineSegment> stepsToLineStrings(List<Step> steps) {
		ArrayList<LineSegment> retVal = new ArrayList<LineSegment>();
		boolean finished = false;
		int i = 0;
		do{
			Step s0 = steps.get(i);
			Step s1 = steps.get(i + 1);
			LineSegment ls = buildLineSegmentFromSteps(s0, s1);
			retVal.add(ls);
			if (i == steps.size() - 2) {
				finished = true;
			}
			i++;
		}while(!finished);				
		return retVal;
	}

	private LineSegment buildLineSegmentFromSteps(Step s0, Step s1) {
		Point p0 = sp.toPoint(new LatLng(s0.getLatitude(), s0.getLongitude()));
		Point p1 = sp.toPoint(new LatLng(s1.getLatitude(), s1.getLongitude()));
		return new LineSegment(p0, p1);
	}

	/**
	 * Closest segment to tap
	 * 
	 * @param tap
	 * @param segments
	 * @return
	 */
	private LineSegment nearestLineString(LatLng tap, List<LineSegment> segments) {
		double mindist = Double.MAX_VALUE;
		LineSegment retVal = null;
		for (int i = 0; i < segments.size(); i++) {
			LineSegment s = segments.get(i);
			double dist_n = distanceToLineSegment(tap, s);
			if (dist_n < mindist) {
				mindist = dist_n;
				retVal = s;
			}
		}
		return retVal;
	}

	public LineSegment getPerpendicularToNearest() {
		return perpendicularToNearest;
	}

	public void setPerpendicularToNearest(LineSegment perpendicularToNearest) {
		this.perpendicularToNearest = perpendicularToNearest;
	}

}
