package net.movelab.sudeau.model;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "interactiveimage")
public class InteractiveImage {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String mediaPath;
	@DatabaseField
	private String heatPath;
	@DatabaseField
	private int originalWidth;
	@DatabaseField
	private int originalHeight;
	@ForeignCollectionField
	private Collection<Box> boxes;	
	
	public InteractiveImage(){
		
	}
	
	public InteractiveImage(String id){
		this.setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
	}

	public Collection<Box> getBoxes() {
		return boxes;
	}

	public void setBoxes(Collection<Box> boxes) {
		this.boxes = boxes;
	}

	public String getHeatPath() {
		return heatPath;
	}

	public void setHeatPath(String heatPath) {
		this.heatPath = heatPath;
	}

	public int getOriginalWidth() {
		return originalWidth;
	}

	public void setOriginalWidth(int originalWidth) {
		this.originalWidth = originalWidth;
	}

	public int getOriginalHeight() {
		return originalHeight;
	}

	public void setOriginalHeight(int originalHeight) {
		this.originalHeight = originalHeight;
	}
	

}
