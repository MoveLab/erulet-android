package net.movelab.sudeau.model;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "media")
public class EruMedia {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] image;
	private int bulk;

	public EruMedia() {		
	}
	
	public EruMedia(String id, String name, byte[] image) {
		this.id=id;
		this.name=name;
		this.image=image;
	}
	
	public byte[] getImage(){
		return image;
	}
	
	public void setImage(byte[] image){
		this.image=image;
	}
	
	public int getBulk() {
		return bulk;
	}

	public void setBulk(int bulk) {
		this.bulk = bulk;
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

}
