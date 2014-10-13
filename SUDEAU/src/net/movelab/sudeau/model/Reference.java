package net.movelab.sudeau.model;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class Reference {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String textContent;
    @DatabaseField(foreign=true, columnName="highlightId")
    private HighLight highlight;
    @ForeignCollectionField
    private ForeignCollection<FileManifest> file_manifests;
    @DatabaseField
    private boolean general_reference;



    public Reference(){
		
	}
	
	public Reference(String id) {
		this.setId(id);
	}

    public ForeignCollection<FileManifest> getFileManifests() { return file_manifests; }
    public void setFileManifests(ForeignCollection<FileManifest> file_manifests){ this.file_manifests = file_manifests;}

    public boolean isGeneralReference() { return general_reference; }
    public void setGeneralReference(boolean is_general_reference){ this.general_reference = is_general_reference;}


    public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
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


    public HighLight getHighlight(){
        return highlight;
    }
    public void setHighlight(HighLight highlight){
        this.highlight = highlight;
    }

}
