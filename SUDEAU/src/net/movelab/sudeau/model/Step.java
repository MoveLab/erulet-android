package net.movelab.sudeau.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

@DatabaseTable(tableName = "step")
public class Step implements Comparable<Step> {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@DatabaseField
	private double latitude;
	@DatabaseField
	private double longitude;
	@DatabaseField
	private double altitude;
	@DatabaseField
	private double precision;
	@DatabaseField(foreign=true, columnName="hlId")
	private HighLight highlight;
	@DatabaseField(foreign=true, columnName="referenceId")
	private Reference reference;
	@DatabaseField
	private int order;
	@DatabaseField
	private Date absoluteTime;
	@DatabaseField
	private long absoluteTimeMillis;
	@DatabaseField(foreign=true, columnName="trackId")
    private Track track;
	/**
	 * This is only for shared steps there is a reference to the route it is
	 * based on
	 */
	@DatabaseField(foreign=true, columnName="routeId")
    private Route route;

	public Step() {		
	}
	
	public Step(String id) {
		this.setId(id); 
	}
	
	public Step(String id, String name, double latitude, double longitude, double altitude,
			double precision, int order, Track track, HighLight highlight){
		this.id=id;
		this.name=name;
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude=altitude;
		this.precision=precision;
		this.order=order;
		this.track=track;
		this.highlight=highlight;
	}


	public Step(String id, String name, double latitude, double longitude, double altitude,
			double precision, int order, Track track, HighLight highlight, Reference reference){
		this.id=id;
		this.name=name;
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude=altitude;
		this.precision=precision;
		this.order=order;
		this.track=track;
		this.highlight=highlight;
		this.reference = reference;
	}
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Date getAbsoluteTime() {
		return absoluteTime;
	}

	public void setAbsoluteTime(Date timestamp) {
		this.absoluteTime = timestamp;
	}

	public long getAbsoluteTimeMillis() {
		return absoluteTimeMillis;
	}

	public void setAbsoluteTimeMillis(long relativeTime) {
		this.absoluteTimeMillis = relativeTime;
	}

	public HighLight getHighlight() {
		return highlight;
	}

	public void setHighlight(HighLight highlight) {
		this.highlight = highlight;
	}

	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}
	
	public Track getTrack(){
		return track;
	}
	
	public void setTrack(Track track){
		this.track=track;
	}
	
	@Override
	public String toString(){
		return "STEP" + " " + id + " " + name + " " + latitude + " " + longitude; 
	}
	
	public String getInfoWindowString(){
		StringBuilder sl = new StringBuilder();
		SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
		sl.append("Data: " + spdf.format(getAbsoluteTime())  + "\n");
		spdf = new SimpleDateFormat("HH:mm:ss");
		Date hour = new Date(getAbsoluteTimeMillis());
		sl.append("Hora: " + spdf.format(hour) + "\n");
		NumberFormat df = new DecimalFormat("0.00");
		sl.append("Latitud: " + df.format(getLatitude()) + "\n");
		sl.append("Longitud: " + df.format(getLongitude()) + "\n");
		sl.append("Alçada: " + df.format(getAltitude()) + "\n");
		return sl.toString();
	}

	@Override
	public int compareTo(Step another) {
		return Integer.valueOf(order).compareTo(Integer.valueOf(another.getOrder()));
	}
	
	@Override
	public boolean equals(Object o) {
		if(!( o instanceof Step )){
			return false;
		}
		Step that = (Step)o;
		if( getId()!=null && that.getId()!=null){
			return getId().equalsIgnoreCase(that.getId());
		}else{
			return this.getLatitude() == that.getLatitude() &&
					this.getLongitude() == that.getLongitude() &&
					this.getAltitude() == that.getAltitude();
		}
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}	
	
}
