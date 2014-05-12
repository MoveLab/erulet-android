package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "route")
public class Route {
		
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String description;
	@DatabaseField(foreign=true, columnName="userId")
	private User user;	
	@DatabaseField(foreign=true, columnName="trackId")
	private Track track;
	private Reference reference;

	public Route() {		
	}
	
	public Route(String id) {
		this.id=id; 
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

}
