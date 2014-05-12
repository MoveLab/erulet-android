package net.movelab.sudeau.model;

public class Reference {
	
	private String id;
	private String name;
	private String textContent;
	private EruMedia media;

	public Reference(String id) {
		this.setId(id); 
	}

	public String getTextContent() {
		return textContent;
	}

	public void getTextContent(String textContent) {
		this.textContent = textContent;
	}

	public EruMedia getMedia() {
		return media;
	}

	public void setMedia(EruMedia media) {
		this.media = media;
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
