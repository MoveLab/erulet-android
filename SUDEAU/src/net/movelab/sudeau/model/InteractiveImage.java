package net.movelab.sudeau.model;

import android.util.Log;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "interactiveimage")
public class InteractiveImage {

    @DatabaseField(generatedId=true)
    private Integer id;
    @DatabaseField
    private Integer server_id;
	@DatabaseField
	private String mediaPath;	
	@DatabaseField
	private int originalWidth;
	@DatabaseField
	private int originalHeight;
    @DatabaseField(foreign=true, columnName = "highlightId")
    private HighLight highlight;
    @DatabaseField(foreign=true, columnName="fileManifestId")
    private FileManifest file_manifest;
    @ForeignCollectionField
	private Collection<Box> boxes;	
	
	public InteractiveImage(){
		
	}

	public int getId() {
		return id;
	}

	public void setServerId(int id) {
		this.server_id = server_id;
        Log.d("II SERVER ID SET: ", "" + server_id);

    }

	public String getMediaPath() {
		return mediaPath;
	}

	public void setMediaPath(String mediaPath) {
		this.mediaPath = mediaPath;
        Log.d("II MEDIA PATH SET: ", mediaPath);

    }

	public Collection<Box> getBoxes() {
		return boxes;
	}

	public void setBoxes(Collection<Box> boxes) {
		this.boxes = boxes;
        Log.d("II Boxes SET: ", "" + id);

    }

    public FileManifest getFileManifest() { return file_manifest; }
    public void setFileManifest(FileManifest file_manifest){ this.file_manifest = file_manifest;}

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

    public HighLight getHighlight(){
        return highlight;
    }
    public void setHighlight(HighLight highlight){
        this.highlight = highlight;
        Log.d("II HIGHLIGHT SET: ", "" + id);

    }
}
