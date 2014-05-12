package net.movelab.sudeau.model;

import java.util.Collection;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "track")
public class Track {
		
	@DatabaseField(id=true)
	private String id;	
	@DatabaseField
	private String name;
	@ForeignCollectionField
	private Collection<Step> steps;
	private Reference reference;

	public Track(){		
	}
	
	public Track(String id) {
		this.id=id; 
	}
	
	public Track(String id, String name) {
		this.id=id; 
		this.name=name;
	}

	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public Collection<Step> getSteps() {
		return steps;
	}

	public void setSteps(Collection<Step> steps) {
		this.steps = steps;
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
	
	@Override
	public String toString(){
		return "TRACK " + id + " " + name;
	}

}
