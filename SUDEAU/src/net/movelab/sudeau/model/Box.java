package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "box")
public class Box {
		
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private int minX;
	@DatabaseField
	private int minY;
	@DatabaseField
	private int maxX;
	@DatabaseField
	private int maxY;	
	@DatabaseField
	private String message;
	@DatabaseField(foreign=true, columnName="interactiveImageId")
    private InteractiveImage interactiveImage;
	
	public Box(){	
	}
	
	public Box(String id){
		this.id=id;
	}
	
	public Box(String id, int minX, int minY, int maxX,int maxY){
		this.id=id;
		this.minX=minX;
		this.minY=minY;
		this.maxX=maxX;
		this.maxY=maxY;
	}
	
	public Box(String id, int minX, int minY, int maxX,int maxY, InteractiveImage img){
		this.id=id;
		this.minX=minX;
		this.minY=minY;
		this.maxX=maxX;
		this.maxY=maxY;
		this.interactiveImage = img;
	}
		
	public String getMessage(){
		return message;
	}
	
	public void setMessage(String message){
		this.message=message;
	}
	
	public boolean isInside(int x, int y){
		if( x <= maxX && x >= minX && y <= maxY && y >= minY )
			return true;
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
