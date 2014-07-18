package net.movelab.sudeau.model;

import com.j256.ormlite.field.DatabaseField;

public class Reference {
	
	@DatabaseField(id=true)
	private String id;
	@DatabaseField
	private String name;
	@DatabaseField
	private String textContent;

	public Reference(){
		
	}
	
	public Reference(String id) {
		this.setId(id); 
	}

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

}
