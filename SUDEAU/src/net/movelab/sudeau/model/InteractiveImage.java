package net.movelab.sudeau.model;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "interactiveimage")
public class InteractiveImage {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id;
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
        this.server_id = -1;
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

	public Collection<Box> getBoxes() {
		return boxes;
	}

	public void setBoxes(Collection<Box> boxes) {
		this.boxes = boxes;

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
    }

    // Note this should only be called after refreshing highlight for file manifest
    public boolean hasMediaFile(){
        return this.getFileManifest()!=null && this.getFileManifest().getPath()!=null && !this.getFileManifest().getPath().isEmpty();
    }

}
