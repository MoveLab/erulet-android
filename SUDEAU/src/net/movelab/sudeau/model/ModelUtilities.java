package net.movelab.sudeau.model;

import java.util.Collections;
import java.util.List;

import android.location.Location;

public class ModelUtilities {
	
	/**
	 * Computes average distance between steps of a route taken in pairs
	 * @param r The route which contains the track
	 * @return Average distance in meters between gps fixes
	 */
	public static float getAverageDistanceBetweenSamples(Route r){
		float retVal = 0;
		List<Step> steps = getStepsIfAvailable(r);
		if(steps != null && steps.size() > 1){
			float[] distances = new float[steps.size()-1];			
			boolean finished = false;
			int i = 0;
			do{
				Step s0 = steps.get(i);
				Step s1 = steps.get(i + 1);
				
				Location location1 = new Location("");
				location1.setLatitude(s0.getLatitude());
				location1.setLongitude(s0.getLongitude());
				
				Location location2 = new Location("");
				location2.setLatitude(s1.getLatitude());
				location2.setLongitude(s1.getLongitude());
				
				distances[i] = location1.distanceTo(location2);
				
				if (i == steps.size() - 2) {
					finished = true;
				}
				i++;
				
			}while(!finished);
			retVal = getFloatAverage(distances);
		}
		return retVal;
	}
	
	/**
	 * Computes average of values in vector
	 * @param values Vector of float values
	 * @return sum of elements in vector divided by number of elements
	 */
	private static float getFloatAverage(float[] values){
		float retVal = 0;
		if(values.length > 0){
			for(int i = 0; i < values.length; i++){
				retVal += values[i];
			}
			retVal = retVal/values.length;
		}
		return retVal;
	}
	
	/**
	 * Computes distance between start and finish. The distance is equal to the sum
	 * of the distance between the waypoints taken by pairs.
	 * 
	 * @param r The route which contains the track
	 * @return The distance computed as described above
	 */
	public static float getTotalDistance(Route r){
		float retVal = 0;
		List<Step> steps = getStepsIfAvailable(r);
		if(steps != null){
			boolean finished = false;
			int i = 0;
			do{
				Step s0 = steps.get(i);
				Step s1 = steps.get(i + 1);
				
				Location location1 = new Location("");
				location1.setLatitude(s0.getLatitude());
				location1.setLongitude(s0.getLongitude());
				
				Location location2 = new Location("");
				location2.setLatitude(s1.getLatitude());
				location2.setLongitude(s1.getLongitude());
				
				retVal+= location1.distanceTo(location2);
				
				if (i == steps.size() - 2) {
					finished = true;
				}
				i++;
				
			}while(!finished);
		}
		return retVal;
	}
	
	/**
	 * Extracts safely (checking for nulls) steps from a Route r
	 * 
	 * @param r The route
	 * @return The steps if available ,null otherwise
	 */
	private static List<Step> getStepsIfAvailable(Route r){
		if(r.getTrack()!=null){
			Track t = r.getTrack();
			if(t!=null){
				List<Step> steps = (List<Step>) t.getSteps();
				if(steps!=null && steps.size() > 1){
					return steps;
				}
			}
		}
		return null;
	}
	
	/**
	 * Computes time difference between start and arrival
	 * 
	 * @param r The route which contains the track
	 * @return Difference between timestamp in milliseconds at start and end
	 */
	public static long getTotalTime(Route r){
		long retVal = 0;
		List<Step> steps = getStepsIfAvailable(r);		
		if(steps!=null && steps.size() > 1){
			Collections.sort(steps);
			Step first = steps.get(0);
			Step last = steps.get(steps.size()-1);
			retVal = last.getAbsoluteTimeMillis()-first.getAbsoluteTimeMillis();
		}		
		return retVal;
	}
	
	/**
	 * Counts number of steps in route's track
	 * 
	 * @param r The route which contains the track
	 * @return the number of steps
	 */
	public static int getNumberOfVertexes(Route r){
		int retVal = 0;
		List<Step> steps = getStepsIfAvailable(r);
		if(steps!=null){
			return steps.size();
		}
		return retVal;
	}
	
	/**
	 * Counts number of steps with info added by user
	 * 
	 * @param r The route which contains the steps
	 * @return The number of steps with info (pictures, text) in the track
	 */
	public static int getNumberOfHighLights(Route r){
		int retVal = 0;
		List<Step> steps = getStepsIfAvailable(r);
		if(steps!=null){
			for(int i = 0; i < steps.size(); i++){
				Step s = steps.get(i);
				if(s.getHighlight()!=null){
					retVal++;
				}
			}
		}
		return retVal;
	}
	
	/**
	 * Computes vertical distance between first and last steps of Route
	 * 
	 * @param r The route which contains the track
	 * @return Difference between first and last steps altitude. If negative,
	 * end is lower than start 
	 */
	public static double getRamp(Route r){
		double retVal = 0;
		List<Step> steps = getStepsIfAvailable(r);		
		if(steps!=null && steps.size() > 1){
			Collections.sort(steps);
			Step first = steps.get(0);
			Step last = steps.get(steps.size()-1);
			retVal = first.getAltitude()-last.getAltitude();
		}
		return retVal;
	}
	
	
		

}
