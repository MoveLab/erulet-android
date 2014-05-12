package net.movelab.sudeau.model;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "user")
public class User {		
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@ForeignCollectionField
	private Collection<Preference> preferences;
	@ForeignCollectionField
	private Collection<Route> routes;
	
	public User(){
	}

	public User(String id, String name){
		this.id=id;
		this.name=name;
	}
	
	@Override
	public String toString(){
		return "USER " + id + " " + name + " ";
	}
	
	public Collection<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(Collection<Route> routes) {
		this.routes = routes;
	}

	public Collection<Preference> getPreferences() {
		return preferences;
	}

	public void setPreferences(Collection<Preference> preferences) {
		this.preferences = preferences;
	}
	
	public String getId(){
		return id;
	}
	
	public void setId(String id){
		this.id=id;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
}
