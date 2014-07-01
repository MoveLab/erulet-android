package net.movelab.sudeau.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import net.movelab.sudeau.Util;

public class RouteInfoFormatter {
	
	private Route route;
	NumberFormat df = new DecimalFormat("0.00"); 
	
	public RouteInfoFormatter(Route route){
		this.route=route;
		df = new DecimalFormat("0.00");
	}
		
	public String getFormattedTotalDistance(){
		float distance = ModelUtilities.getTotalDistance(route);
		return "Distància total: " + df.format(distance) + " metres";
	}
	
	public String getFormattedTotalTime(){
		float time = ModelUtilities.getTotalTime(route);
		int seconds = (int) (time / Util.SECONDS) % 60 ;
		int minutes = (int) ((time / (Util.MINUTES)) % 60);
		int hours   = (int) ((time / (Util.HOURS)) % 24); 
		return "Temps total: " + String.format("%d hores, %d minuts, %d segons",hours,minutes,seconds);
	}
	
	public String getFormattedNumberPointsInTrack(){
		int n_vertexes = ModelUtilities.getNumberOfVertexes(route);
		return "Nombre de punts: " + String.format("%d",n_vertexes);
	}
	
	public String getFormattedNumberHighlights(){
		int n_highs = ModelUtilities.getNumberOfHighLights(route);
		return "Nombre de punts d'interès: " + String.format("%d",n_highs);
	}
	
	public String getFormattedRamp(){
		double ramp = ModelUtilities.getRamp(route);
		return "Desnivell: " + df.format(ramp) + " metres";
	}
	
	
	

}
