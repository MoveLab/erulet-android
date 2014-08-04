package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "route")
public class Route {
		
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String idRouteBasedOn;
	@DatabaseField
	private String name;
	@DatabaseField
	private String description;
	@DatabaseField
	private String userId;	
	@DatabaseField(foreign=true, columnName="trackId")
	private Track track;
	@DatabaseField(foreign=true, columnName="referenceId")
	private Reference reference;	
	@DatabaseField
	private boolean upLoaded;
	@DatabaseField
	private String localCarto;

	public Route() {		
	}
	
	public Route(String id) {
		this.id=id; 
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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
	
	public Track getTrack(){
		return track;
	}
	
	public void setTrack(Track track){
		this.track=track;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description=description;
	}
	
	@Override
	public String toString(){
		return "ROUTE " + id + " " + name;
	}

	public String getIdRouteBasedOn() {
		return idRouteBasedOn;
	}

	public void setIdRouteBasedOn(String idRouteBasedOn) {
		this.idRouteBasedOn = idRouteBasedOn;
	}
	
	public boolean isUpLoaded() {
		return upLoaded;
	}

	public void setUpLoaded(boolean upLoaded) {
		this.upLoaded = upLoaded;
	}

	public String getLocalCarto() {
		return localCarto;
	}

	public void setLocalCarto(String localCarto) {
		this.localCarto = localCarto;
	}
	
}
