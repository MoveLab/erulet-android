package net.movelab.sudeau.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.movelab.sudeau.Util;

@DatabaseTable(tableName = "route")
public class Route {
		
	@DatabaseField(id=true)
	private String id;
    @DatabaseField
    private boolean official;
	@DatabaseField
	private String idRouteBasedOn;
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
    private String description_oc;
    @DatabaseField
    private String description_es;
    @DatabaseField
    private String description_ca;
    @DatabaseField
    private String description_fr;
	@DatabaseField
	private String description_en;
	@DatabaseField
	private String userId;
	@DatabaseField(foreign=true, columnName="trackId")
	private Track track;
	@DatabaseField(foreign=true, columnName="referenceId")
	private Reference reference;
	@DatabaseField
	private boolean upLoaded;
	@DatabaseField
	private String localCarto;
    @DatabaseField
    private long localCartoLastUpdated;
    @DatabaseField
    private long routeContentLastUpdated;
    @DatabaseField
    private long routeJsonLastUpdated;
	@DatabaseField
	private int globalRating;
	@DatabaseField
	private int userRating;
	/**
	 * Here we store the steps shared by other users
	 */
	@ForeignCollectionField
	private Collection<Step> sharedSteps;

	public Route() {
		setSharedSteps(new ArrayList<Step>());
	}
	
	public Route(String id) {
		this.id=id; 
	}

    public boolean getOfficial() {
        return official;
    }

    public void setOfficial(Boolean official) {
        this.official = official;
    }
    public long getLocalCartoLastUpdated() {
        return localCartoLastUpdated;
    }

    public void setLocalCartoLastUpdated(long localCartoLastUpdated) {
        this.localCartoLastUpdated = localCartoLastUpdated;
    }

    public long getRouteContentLastUpdated() {
        return routeContentLastUpdated;
    }

    public void setRouteContentLastUpdated(long routeContentLastUpdated) {
        this.routeContentLastUpdated = routeContentLastUpdated;
    }

    public void setRouteContentLastUpdatedNow() {
        this.routeContentLastUpdated = System.currentTimeMillis()/1000;
        Log.e("SET ROUTE JSON LU NOW", "reads" + this.getRouteContentLastUpdated());

    }

    public long getRouteJsonLastUpdated() {
        return routeJsonLastUpdated;
    }

    public void setRouteJsonLastUpdated(long routeJsonLastUpdated) {
        this.routeJsonLastUpdated = routeJsonLastUpdated;
    }

    public void setRouteJsonLastUpdatedNow() {
        this.routeJsonLastUpdated = System.currentTimeMillis()/1000;
        Log.e("SET ROUTE JSON LU NOW", "");
    }


	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public static String[] langArray = new String[]{"oc", "es", "ca", "fr", "en"};
    public static ArrayList<String> langArrayList = new ArrayList<String>(Arrays.asList(langArray));
    String[] nameArray = new String[]{name_oc, name_es, name_ca, name_fr, name_en};
    Object[] nameAttrArray = new Object[]{this.name_oc, this.name_es, this.name_ca, this.name_fr, this.name_en};
    String[] descriptionArray = new String[]{description_oc, description_es, description_ca, description_fr, description_en};
    Object[] descriptionAttrArray = new Object[]{this.description_oc, this.description_es, this.description_ca, this.description_fr, this.description_en};

    public String getName(String lang) {
        Log.i("ROUTE GET NAME: ", "lang " + lang);
        String result = "";
        // First see if desired lang is available
        if(langArrayList.contains(lang)){
            result = nameArray[langArrayList.indexOf(lang)];
            if(result != null && !result.equals("")){
                Log.i("ROUTE GET NAME: ", "result 1: " + result);
                return result;
            }
        }
        // If not, then go through and return first available
        for(String this_lang:langArrayList){
            result = nameArray[langArrayList.indexOf(this_lang)];
            if(result != null && !result.equals("")){
                Log.i("ROUTE GET NAME: ", "result 2: " + result);
                    return result;
            }
            }
        // if nothing yet, then return empty string
        Log.i("ROUTE GET NAME: ", "result 3: " + result);
        return result;
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


    public String getDescription(String lang) {
        String result = "";
        // First see if desired lang is available
        if(langArrayList.contains(lang)){
            result = descriptionArray[langArrayList.indexOf(lang)];
            if(result != null && !result.equals("")){
                return result;
            }
        }
        // If not, then go through and return first available
        for(String this_lang:langArrayList){
            result = descriptionArray[langArrayList.indexOf(this_lang)];
            if(result != null && !result.equals("")){
                return result;
            }
        }
        // if nothing yet, then return empty string
        return result;
    }

    public void setDescription(String lang, String description) {
        if(lang.equals("oc"))
            this.description_oc = description;
        if(lang.equals("es"))
            this.description_es = description;
        if(lang.equals("ca"))
            this.description_ca = description;
        if(lang.equals("fr"))
            this.description_fr = description;
        if(lang.equals("en"))
            this.description_en = description;
    }


    public Track getTrack(){
		return track;
	}
	
	public void setTrack(Track track){
		this.track=track;
	}
	

	@Override
	public String toString(){
		return "ROUTE " + id + " " + this.getName("oc");
	}

    public String toString(String lang){
        return "ROUTE " + id + " " + this.getName(lang);
    }

    public String getIdRouteBasedOn() {
		return idRouteBasedOn;
	}

	public void setIdRouteBasedOn(String idRouteBasedOn) {
		this.idRouteBasedOn = idRouteBasedOn;
	}
	
	public boolean isUpLoaded() {
		return upLoaded;
	}

	public void setUpLoaded(boolean upLoaded) {
		this.upLoaded = upLoaded;
	}

	public String getLocalCarto() {
		return localCarto;
	}

	public void setLocalCarto(String localCarto) {
		this.localCarto = localCarto;
	}

	public Collection<Step> getSharedSteps() {
		return sharedSteps;
	}

	public void setSharedSteps(Collection<Step> sharedSteps) {
		this.sharedSteps = sharedSteps;
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
}
