package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

@DatabaseTable(tableName = "file_manifest")
public class FileManifest {

	@DatabaseField(generatedId=true)
	private int id;
	@DatabaseField
	private String path;
    @DatabaseField(foreign=true, columnName = "referenceId")
    private Reference reference;

	public FileManifest() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }


    public Integer getId() {
		return id;
	}

	@Override
	public String toString(){
		return "FILE_MANIFEST " + id + " " + path;
	}
}