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
    private Integer id;
    @DatabaseField
    private Integer server_id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String longText;	
	@DatabaseField
	private String mediaPath;
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

	public void setServerId(int server_id) {
		this.server_id = server_id;
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

	public String getMediaFileName() {
		return mediaPath;
	}

	public void setMediaFileName(String mediaFileName) {
		this.mediaPath = mediaFileName;
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
        if(this.getFileManifest()!=null && this.getFileManifest().getPath() != null)
            Log.d("MEDIA FILE MADNESS:", this.getFileManifest().getPath());

        return this.getFileManifest()!=null && this.getFileManifest().getPath()!=null && !this.getFileManifest().getPath().isEmpty();
    }

}
