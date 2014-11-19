package net.movelab.sudeau.model;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.movelab.sudeau.database.DataBaseHelper;

import java.util.Collection;

@DatabaseTable(tableName = "highlight")
public class HighLight {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id;
    @DatabaseField
    private String name_oc;
    @DatabaseField
    private String name_es;
    @DatabaseField
    private String name_ca;
    @DatabaseField
    private String name_fr;
    @DatabaseField
    private String name_en;
    @DatabaseField
    private String longText_oc;
    @DatabaseField
    private String longText_es;
    @DatabaseField
    private String longText_ca;
    @DatabaseField
    private String longText_fr;
    @DatabaseField
    private String longText_en;
    @DatabaseField(foreign=true, columnName="fileManifestId")
    private FileManifest file_manifest;
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
    @ForeignCollectionField
	private Collection<Reference> references;
    @ForeignCollectionField
	private Collection<InteractiveImage> interactiveImages;
	
	public static final int WAYPOINT = 1;
	public static final int POINT_OF_INTEREST = 2;
	public static final int POINT_OF_INTEREST_OFFICIAL = 3;
	public static final int ALERT = 4;
	public static final int POINT_OF_INTEREST_SHARED = 5;
	public static final int CONTAINER_N = 6;
	public static final int INTERACTIVE_IMAGE = 7;
	public static final int REFERENCE = 8;
	
	public HighLight() {
        this.server_id = -1;
    }
	

    public FileManifest getFileManifest() { return file_manifest; }
    public void setFileManifest(FileManifest file_manifest){ this.file_manifest = file_manifest;}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
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


	@Override
	public String toString(){
		return "HIGHLIGHT " + id + " " + getFirstfilledName() + " " + radius;
	}



    public String getFirstfilledName(){
        String result = name_oc;
        if(result != null && !result.isEmpty())
            return result;
        result = name_es;
        if(result != null && !result.isEmpty())
            return result;
        result = name_ca;
        if(result != null && !result.isEmpty())
            return result;
        result = name_fr;
        if(result != null && !result.isEmpty())
            return result;
        result = name_en;
        if(result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getName(String lang) {
        String result = "";
        if(lang.equals("oc")){
            result = name_oc;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("es")){
            result = name_es;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("ca")){
            result = name_ca;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("fr")){
            result = name_fr;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("en"))
            result = name_en;{
            if(result != null && !result.isEmpty())
                return result;}
        return getFirstfilledName();
    }

    public void setName(String lang, String name) {
        if(lang.equals("oc"))
            this.name_oc = name;
        if(lang.equals("es"))
            this.name_es = name;
        if(lang.equals("ca"))
            this.name_ca = name;
        if(lang.equals("fr"))
            this.name_fr = name;
        if(lang.equals("en"))
            this.name_en = name;
    }


    public String getFirstfilledLongText(){
        String result = longText_oc;
        if(result != null && !result.isEmpty())
            return result;
        result = longText_es;
        if(result != null && !result.isEmpty())
            return result;
        result = longText_ca;
        if(result != null && !result.isEmpty())
            return result;
        result = longText_fr;
        if(result != null && !result.isEmpty())
            return result;
        result = longText_en;
        if(result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getLongText(String lang) {
        String result = "";
        if(lang.equals("oc")){
            result = longText_oc;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("es")){
            result = longText_es;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("ca")){
            result = longText_ca;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("fr")){
            result = longText_fr;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("en"))
            result = longText_en;{
            if(result != null && !result.isEmpty())
                return result;}
        return getFirstfilledLongText();
    }

    public void setLongText(String lang, String longText) {
        if(lang.equals("oc"))
            this.longText_oc = longText;
        if(lang.equals("es"))
            this.longText_es = longText;
        if(lang.equals("ca"))
            this.longText_ca = longText;
        if(lang.equals("fr"))
            this.longText_fr = longText;
        if(lang.equals("en"))
            this.longText_en = longText;
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

	public Collection<Reference> getReferences() {
		return references;
	}

	public void setReferences(Collection<Reference> references) {
		this.references = references;
    }

	public Collection<InteractiveImage> getInteractiveImages() {
		return interactiveImages;
	}

	public void setInteractiveImages(Collection<InteractiveImage> interactiveImages) {
		this.interactiveImages = interactiveImages;

    }

    // Note this should only be called after refreshing highlight for file manifest
    public boolean hasMediaFile(){
        if(this.getFileManifest()!=null && this.getFileManifest().getPath() != null){
         // report error
        }
        return this.getFileManifest()!=null && this.getFileManifest().getPath()!=null && !this.getFileManifest().getPath().isEmpty();
    }

}
