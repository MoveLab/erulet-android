package net.movelab.sudeau.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import net.movelab.sudeau.database.DataContainer;

@DatabaseTable(tableName = "step")
public class Step implements Comparable<Step> {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id;
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
//	@DatabaseField(foreign=true, columnName="hlId")
//	private HighLight highlight;
	@ForeignCollectionField
	private Collection<HighLight> highlights;
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
		setHighlights(new ArrayList<HighLight>());
        this.server_id = -1;
    }

	public Step(int server_id, String name, double latitude, double longitude, double altitude,
			double precision, int order, Track track){
		this.server_id=server_id;
		this.name=name;
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude=altitude;
		this.precision=precision;
		this.order=order;
		this.track=track;
		//this.highlight=highlight;
		setHighlights(new ArrayList<HighLight>());
	}


	public Step(int server_id, String name, double latitude, double longitude, double altitude,
			double precision, int order, Track track, Reference reference){
		this.server_id=server_id;
		this.name=name;
		this.latitude=latitude;
		this.longitude=longitude;
		this.altitude=altitude;
		this.precision=precision;
		this.order=order;
		this.track=track;
		//this.highlight=highlight;
		this.reference = reference;
		setHighlights(new ArrayList<HighLight>());
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

//	public HighLight getHighlight() {
//		return highlight;
//	}
//
//	public void setHighlight(HighLight highlight) {
//		this.highlight = highlight;
//	}

	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public int getId() {
		return id;
	}

    public int getServerId() {
        return server_id;
    }

    public void setServerId(int server_id) {
		this.server_id = server_id;
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
			return getId() == that.getId();

	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	public Collection<HighLight> getHighlights() {
		return highlights;
	}

	public void setHighlights(Collection<HighLight> highlights) {
		this.highlights = highlights;
	}

    // Make sure to refresh step first!
	public boolean hasHighLights(){
		return (highlights!= null && highlights.size() > 0);
	}
	
	public boolean hasSingleHighLight(){
		return (highlights!= null && highlights.size() == 1);
	}
	
	public boolean hasMultipleHighLights(){
		return (highlights!= null && highlights.size() > 1);			 
	}
	
}
