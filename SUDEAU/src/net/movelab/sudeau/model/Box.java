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
    @DatabaseField
    private String message_oc;
    @DatabaseField
    private String message_es;
    @DatabaseField
    private String message_ca;
    @DatabaseField
    private String message_fr;
    @DatabaseField
    private String message_en;


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
		

    public String getFirstFilledMessage(){
        String result = message_oc;
        if(result != null && !result.isEmpty())
            return result;
        result = message_es;
        if(result != null && !result.isEmpty())
            return result;
        result = message_ca;
        if(result != null && !result.isEmpty())
            return result;
        result = message_fr;
        if(result != null && !result.isEmpty())
            return result;
        result = message_en;
        if(result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getMessage(String lang) {
        String result = "";
        if(lang.equals("oc")){
            result = message_oc;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("es")){
            result = message_es;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("ca")){
            result = message_ca;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("fr")){
            result = message_fr;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("en"))
            result = message_en;{
            if(result != null && !result.isEmpty())
                return result;}
        return getFirstFilledMessage();
    }

    public void setMessage(String lang, String message) {
        if(lang.equals("oc"))
            this.message_oc = message;
        if(lang.equals("es"))
            this.message_es = message;
        if(lang.equals("ca"))
            this.message_ca = message;
        if(lang.equals("fr"))
            this.message_fr = message;
        if(lang.equals("en"))
            this.message_en = message;
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
