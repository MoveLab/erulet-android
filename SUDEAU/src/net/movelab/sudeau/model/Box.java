package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "box")
public class Box {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id;
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
        this.server_id = -1;
	}
	

	public Box(int server_id, int minX, int minY, int maxX,int maxY){
		this.server_id=server_id;
		this.minX=minX;
		this.minY=minY;
		this.maxX=maxX;
		this.maxY=maxY;
	}
	
	public Box(int server_id, int minX, int minY, int maxX,int maxY, InteractiveImage img){
		this.server_id=server_id;
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

	public int getId() {
		return id;
	}

    public int getServerId() {
        return server_id;
    }

    public void setServerId(int server_id) {
		this.server_id = server_id;
	}

    public void setInteractiveImage(InteractiveImage ii){
        this.interactiveImage = ii;
    }

}
