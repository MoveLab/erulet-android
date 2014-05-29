package net.movelab.sudeau.geometry;

import java.util.ArrayList;
import java.util.List;

import net.movelab.sudeau.model.Step;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;

public class SnapCalculatorV2 {
	
	private SphericalMercatorProjection sp;

	public SnapCalculatorV2() {
		sp = new SphericalMercatorProjection(6371);
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
	
	private double distanceBetweenPoints(LatLng latLng1, LatLng latLng2) {
		return SphericalUtil.computeDistanceBetween(latLng1, latLng2);
	}
	
	private Step getLastStep(List<Step> stepsInProgress) {
		return stepsInProgress.get(stepsInProgress.size() - 1);
	}
	
	private List<LatLngLineSegment> stepsToLatLngLineSegments(List<Step> steps) {
		ArrayList<LatLngLineSegment> retVal = new ArrayList<LatLngLineSegment>();
		boolean finished = false;
		int i = 0;
		do{
			Step s0 = steps.get(i);
			Step s1 = steps.get(i + 1);
			LatLngLineSegment ls = new LatLngLineSegment(
						new LatLng(s0.getLatitude(),s0.getLongitude()),
						new LatLng(s1.getLatitude(),s1.getLongitude())
					);
			retVal.add(ls);
			if (i == steps.size() - 2) {
				finished = true;
			}
			i++;
		}while(!finished);				
		return retVal;
	}
	
	private double distanceToLatLngLineSegment(LatLng tap, LatLngLineSegment s) {
		Point a = sp.toPoint(s.getL1());
		Point b = sp.toPoint(s.getL2());
		Point p = sp.toPoint(tap);
		double normalLength = Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y)
				* (b.y - a.y));
		return Math.abs((p.x - a.x) * (b.y - a.y) - (p.y - a.y) * (b.x - a.x))
				/ normalLength;
	}
	
	private LatLngLineSegment nearestLatLngLineSegment(LatLng tap, List<LatLngLineSegment> segments) {
		double mindist = Double.MAX_VALUE;
		LatLngLineSegment retVal = null;
		for (int i = 0; i < segments.size(); i++) {
			LatLngLineSegment s = segments.get(i);
			double dist_n = distanceToLatLngLineSegment(tap, s);
			if (dist_n < mindist) {
				mindist = dist_n;
				retVal = s;
			}
		}
		return retVal;
	}
	
	private boolean segmentIsCompletelyVertical(LatLngLineSegment segment){
		LatLng p1 = segment.getL1();
		LatLng p2 = segment.getL2();
		if(p1.longitude==p2.longitude){
			return true;
		}
		return false;
	}
	
	private boolean segmentIsCompletelyHorizontal(LatLngLineSegment segment){
		LatLng p1 = segment.getL1();
		LatLng p2 = segment.getL2();
		if(p1.latitude==p2.latitude){
			return true;
		}
		return false;
	}
	
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
			List<LatLngLineSegment> segments;
			segments = stepsToLatLngLineSegments(stepsInProgress);
			LatLngLineSegment nearestSegment = nearestLatLngLineSegment(screenTap, segments);			
			if( segmentIsCompletelyHorizontal(nearestSegment) ){
				//Intersection is such that
				//y is value of horizontal segment, x is x of tap
				return new LatLng(nearestSegment.getL1().latitude, screenTap.longitude);
			}else if( segmentIsCompletelyVertical(nearestSegment) ){
				//Intersection is such that
				//y is value of tap, x is x of horizontal segment
				return new LatLng(screenTap.latitude, nearestSegment.getL1().longitude);
			}else{
				double fraction = 0.5;
				LatLng from = nearestSegment.getL1();
				LatLng to = nearestSegment.getL2();
				double distanceFrom = SphericalUtil.computeDistanceBetween(from, screenTap);
				double distanceTo = SphericalUtil.computeDistanceBetween(to, screenTap);
				//double horiz_dist = Math.abs(to.longitude - from.longitude);
				double norm_dist_from = distanceFrom/distanceFrom+distanceTo;
				double norm_dist_to = distanceTo/distanceFrom+distanceTo;
				if( distanceFrom < distanceTo ){
					//Tap is closer to from
					fraction = norm_dist_from;					
				}else if( distanceFrom > distanceTo ){
					//Tap is closer to 'to'
					fraction = norm_dist_to;					
				}else{
					fraction = 0.5;					
				}
				return SphericalUtil.interpolate(
						from, 
						to, 
						fraction);
			}					
		}		
	}

}
