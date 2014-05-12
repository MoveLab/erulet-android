package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;

public class Preference {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String key;
	@DatabaseField
	private String value;
	@DatabaseField(foreign=true, columnName="userId")
    private User user;
	
	public Preference(){		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setUser(User user){
		this.user=user;
	}
	public User getUser(){
		return user;
	}
	
	@Override
	public String toString() {
		return "PREFERENCE " + id + " " + key + " " + value;
	}

}
