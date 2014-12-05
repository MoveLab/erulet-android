package net.movelab.sudeau.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class Reference {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id;
	@DatabaseField
	private String name;
    @DatabaseField(foreign=true, columnName="highlightId")
    private HighLight highlight;
    @DatabaseField
    private String html_path_oc;
    @DatabaseField
    private String html_path_es;
    @DatabaseField
    private String html_path_ca;
    @DatabaseField
    private String html_path_fr;
    @DatabaseField
    private String html_path_en;
    @ForeignCollectionField
    private ForeignCollection<FileManifest> file_manifests;
    @DatabaseField
    private boolean general_reference;



    public Reference(){
        this.server_id = -1;
    }

    public ForeignCollection<FileManifest> getFileManifests() { return file_manifests; }
    public void setFileManifests(ForeignCollection<FileManifest> file_manifests){ this.file_manifests = file_manifests;}

    public boolean isGeneralReference() { return general_reference; }
    public void setGeneralReference(boolean is_general_reference){ this.general_reference = is_general_reference;}

	public int getId() {
		return id;
	}

    public int getServerId() {
        return server_id;
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

    public String getFirstfilledHtml_path(){
        String result = html_path_oc;
        if(result != null && !result.isEmpty())
            return result;
        result = html_path_es;
        if(result != null && !result.isEmpty())
            return result;
        result = html_path_ca;
        if(result != null && !result.isEmpty())
            return result;
        result = html_path_fr;
        if(result != null && !result.isEmpty())
            return result;
        result = html_path_en;
        if(result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getHtmlPath(String lang) {
        String result = "";
        if(lang.equals("oc")){
            result = html_path_oc;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("es")){
            result = html_path_es;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("ca")){
            result = html_path_ca;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("fr")){
            result = html_path_fr;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("en"))
            result = html_path_en;{
            if(result != null && !result.isEmpty())
                return result;}
        return getFirstfilledHtml_path();
    }

    public void setHtmlPath(String lang, String html_path) {
        if(lang.equals("oc"))
            this.html_path_oc = html_path;
        if(lang.equals("es"))
            this.html_path_es = html_path;
        if(lang.equals("ca"))
            this.html_path_ca = html_path;
        if(lang.equals("fr"))
            this.html_path_fr = html_path;
        if(lang.equals("en"))
            this.html_path_en = html_path;
    }



    public HighLight getHighlight(){
        return highlight;
    }
    public void setHighlight(HighLight highlight){
        this.highlight = highlight;
    }

}
