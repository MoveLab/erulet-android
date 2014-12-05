package net.movelab.sudeau.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;

import android.content.Context;

import net.movelab.sudeau.R;
import net.movelab.sudeau.Util;

public class RouteInfoFormatter {
	
	private Route route;
	private Hashtable<String,String> strings;
	private Context context;
	NumberFormat df = new DecimalFormat("0.00"); 
	
	public RouteInfoFormatter(Route route, Context context){
		this.route=route;
		this.context=context;
		df = new DecimalFormat("0.00");		
	}
		
	public String getFormattedTotalDistance(){
		float distance = ModelUtilities.getTotalDistance(route);
		return context.getString(R.string.total_distance) + ": " + df.format(distance) + " " + context.getString(R.string.meters);
	}
	
	public String getFormattedTotalTime(){
		float time = ModelUtilities.getTotalTime(route);
		int seconds = (int) (time / Util.SECONDS) % 60 ;
		int minutes = (int) ((time / (Util.MINUTES)) % 60);
		int hours   = (int) ((time / (Util.HOURS)) % 24); 
		return context.getString(R.string.total_time) + 
				": " 
		+ String.format(
				"%d " + 
				context.getString(R.string.hours) + 
				" %d " + 
				context.getString(R.string.minutes) + 
				" %d " + 
				context.getString(R.string.seconds),
				hours,
				minutes,
				seconds);
	}
	
	public String getFormattedNumberPointsInTrack(){
		int n_vertexes = ModelUtilities.getNumberOfVertexes(route);
		return context.getString(R.string.points_number) + 
				": " + 
				String.format("%d",n_vertexes);
	}
	
	public String getFormattedNumberHighlights(){
		int n_highs = ModelUtilities.getNumberOfHighLights(route);
		return context.getString(R.string.points_of_interest_number) + 
				": " + 
				String.format("%d",n_highs);
	}
	
	public String getFormattedRamp(){
		double ramp = ModelUtilities.getRamp(route);
		return context.getString(R.string.slope) + ": " + df.format(ramp) + " " + context.getString(R.string.meters);
	}
	
	public String getFormattedAverageSpeedKmH(){
		float time = ModelUtilities.getTotalTime(route);
		float distance = ModelUtilities.getTotalDistance(route);
		float time_hours = (time / (Util.HOURS));
		float distance_km = (distance / 1000);		
		return 
				context.getString(R.string.avg_speed) + 
				": " + 
				df.format(distance_km/time_hours) + 
				" " + 
				context.getString(R.string.kilometers)  + 
				"/" + 
				context.getString(R.string.hour);
	}
	
	public String getFormattedAverageSampleDistanceMeters(){
		float avgDistance = ModelUtilities.getAverageDistanceBetweenSamples(route);			
		return 
				context.getString(R.string.avg_distance_samples) + 
				": " + 
				df.format(avgDistance) + 
				" " + 
				context.getString(R.string.meters);
	}
	

}
