package net.movelab.sudeau.model;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.location.Location;

@DatabaseTable(tableName = "step")
public class Step implements Comparable<Step> {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	//private Location location;
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
	private Reference reference;
	@DatabaseField
	private int order;
	@DatabaseField
	private Date absoluteTime;
	@DatabaseField
	private long relativeTime;
	@DatabaseField(foreign=true, columnName="trackId")
    private Track track;

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

//	public Location getLocation() {
//		return location;		
//	}
//
//	public void setLocation(Location location) {
//		this.location = location;
//	}

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

	public long getRelativeTime() {
		return relativeTime;
	}

	public void setRelativeTime(long relativeTime) {
		this.relativeTime = relativeTime;
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

	@Override
	public int compareTo(Step another) {
		return Integer.valueOf(order).compareTo(Integer.valueOf(another.getOrder()));
	}

}
