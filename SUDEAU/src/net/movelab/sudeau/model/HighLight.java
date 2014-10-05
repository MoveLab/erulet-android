package net.movelab.sudeau.model;

import android.util.Log;

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
	@DatabaseField
	private int globalRating;
	@DatabaseField
	private int userRating;
	@DatabaseField(foreign=true, columnName="stepId")
    private Step step;
	@DatabaseField(foreign=true, columnName="referenceId")
	private Reference reference;
	@DatabaseField(foreign=true, columnName="interactiveImageId")
	private InteractiveImage interactiveImage;
	
	public static final int WAYPOINT = 1;
	public static final int POINT_OF_INTEREST = 2;
	public static final int POINT_OF_INTEREST_OFFICIAL = 3;
	public static final int ALERT = 4;
	public static final int POINT_OF_INTEREST_SHARED = 5;
	public static final int CONTAINER_N = 6;
	public static final int INTERACTIVE_IMAGE = 7;
	public static final int REFERENCE = 8;
	
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
	
	public HighLight(String id, String name, String longText, double radius, int type, Step step) {
		this.id=id;
		this.name=name;
		this.radius=radius;		
		this.longText=longText;
		this.type=type;
		this.step=step;
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
        Log.d("HighlightMediaPath SET: ",imagePath);
    }

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getGlobalRating() {
		return globalRating;
	}

	public void setGlobalRating(int globalRating) {
		this.globalRating = globalRating;
	}

	public int getUserRating() {
		return userRating;
	}

	public void setUserRating(int userRating) {
		this.userRating = userRating;
	}

	public Step getStep() {
		return step;
	}

	public void setStep(Step step) {
		this.step = step;
	}

	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
        Log.d("HIGHLIGHT REFERENCE SET: REFID: ", reference.getId());

    }

	public InteractiveImage getInteractiveImage() {
		return interactiveImage;
	}

	public void setInteractiveImage(InteractiveImage interactiveImage) {
		this.interactiveImage = interactiveImage;
        Log.d("HIGHLIGHT II SET: II ID: ", interactiveImage.getId());

    }

}
