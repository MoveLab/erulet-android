package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "highlight")
public class HighLight {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String longText;	
	@DatabaseField
	private String mediaPath;
	@DatabaseField
	private double radius;
	@DatabaseField
	private int type;
	
	public static final int WAYPOINT = 0;
	public static final int POINT_OF_INTEREST = 1;
	public static final int POINT_OF_INTEREST_OFFICIAL = 2;
	public static final int ALERT = 3;
	
	public HighLight() {		
	}
	
	public HighLight(String id) {
		this.id=id; 
	}
	
	public HighLight(String id, String name) {
		this.id=id;
		this.name=name;		
	}
	
	public HighLight(String id, String name, double radius) {
		this.id=id;
		this.name=name;
		this.radius=radius;
	}		
	
	public HighLight(String id, String name, String longText, double radius) {
		this.id=id;
		this.name=name;
		this.radius=radius;		
		this.longText=longText;
	}
	
	public HighLight(String id, String name, String longText, double radius, int type) {
		this.id=id;
		this.name=name;
		this.radius=radius;		
		this.longText=longText;
		this.type=type;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
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
		return "HIGHLIGHT " + id + " " + name + " " + radius;
	}

	public String getLongText() {
		return longText;
	}

	public void setLongText(String longText) {
		this.longText = longText;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String imagePath) {
		this.mediaPath = imagePath;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
