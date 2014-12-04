package net.movelab.sudeau.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import net.movelab.sudeau.Util;
import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;

@DatabaseTable(tableName = "route")
public class Route {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id = -1;
    @DatabaseField
    private boolean official;
	@DatabaseField
	private Integer idRouteBasedOn;
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
	private float globalRating;
    @DatabaseField
    private int totalRatings;
	@DatabaseField
	private int userRating = -1;
    @DatabaseField
    private boolean userRatingUploaded = false;
    @DatabaseField
    private long userRatingTime;

    /**
	 * Here we store the steps shared by other users
	 */
	@ForeignCollectionField
	private Collection<Step> sharedSteps;

	public Route() {
		setSharedSteps(new ArrayList<Step>());
        this.server_id = -1;
        routeContentLastUpdated = 0;
	}

    public void setId(int id){
        this.id = id;
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

    public void setLocalCartoLastUpdatedNow() {
        this.localCartoLastUpdated = System.currentTimeMillis()/1000;
    }


    public long getRouteContentLastUpdated() {
        return routeContentLastUpdated;
    }

    public void setRouteContentLastUpdated(long routeContentLastUpdated) {
        this.routeContentLastUpdated = routeContentLastUpdated;
    }

    public void setRouteContentLastUpdatedNow() {
        this.routeContentLastUpdated = System.currentTimeMillis()/1000;

    }

    public long getRouteJsonLastUpdated() {
        return routeJsonLastUpdated;
    }

    public void setRouteJsonLastUpdated(long routeJsonLastUpdated) {
        this.routeJsonLastUpdated = routeJsonLastUpdated;
    }

    public void setRouteJsonLastUpdatedNow() {
        this.routeJsonLastUpdated = System.currentTimeMillis()/1000;
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

	public int getId() {
		return id;
	}

    public int getServerId() {
        return server_id;

    }

    public void setServerId(int server_id) {
		this.server_id = server_id;
    }


    public String getFirstfilledName(){
        String result = name_oc;
        if(result != null && !result.isEmpty())
            return result;
        result = name_es;
        if(result != null && !result.isEmpty())
            return result;
        result = name_ca;
        if(result != null && !result.isEmpty())
            return result;
        result = name_fr;
        if(result != null && !result.isEmpty())
            return result;
        result = name_en;
        if(result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getName(String lang) {
        String result = "";
        if(lang.equals("oc")){
            result = name_oc;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("es")){
            result = name_es;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("ca")){
            result = name_ca;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("fr")){
            result = name_fr;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("en"))
            result = name_en;{
            if(result != null && !result.isEmpty())
                return result;}
           return getFirstfilledName();
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


    public String getFirstfilledDescription(){
        String result = description_oc;
        if(result != null && !result.isEmpty())
            return result;
        result = description_es;
        if(result != null && !result.isEmpty())
            return result;
        result = description_ca;
        if(result != null && !result.isEmpty())
            return result;
        result = description_fr;
        if(result != null && !result.isEmpty())
            return result;
        result = description_en;
        if(result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getDescription(String lang) {
        String result = "";
        if(lang.equals("oc")){
            result = description_oc;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("es")){
            result = description_es;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("ca")){
            result = description_ca;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("fr")){
            result = description_fr;
            if(result != null && !result.isEmpty())
                return result;}
        if(lang.equals("en"))
            result = description_en;{
            if(result != null && !result.isEmpty())
                return result;}
        return getFirstfilledDescription();
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

    public int getIdRouteBasedOn() {
		return idRouteBasedOn;
	}

	public void setIdRouteBasedOn(int idRouteBasedOn) {
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

	public float getGlobalRating() {
        Log.i("get global rating", "rating: " + globalRating);
		return globalRating;
	}

	public void setGlobalRating(float globalRating) {
        this.globalRating = globalRating;
        Log.i("set global rating", "rating: " + this.globalRating);
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }


    public int getUserRating() {
		return userRating;
	}

	public void setUserRating(int userRating) {
		this.userRating = userRating;
	}

    public String getUniqueName(){
        if(official){
            return "holet_route_" + server_id;
        } else{
            return "route_" + id;
        }
    }


    public long getUserRatingTime() {
        return userRatingTime;
    }

    public void setUserRatingTime(long userRatingTime) {
        this.userRatingTime = userRatingTime;
    }

    public boolean getUserRatingUploaded() {
        return userRatingUploaded;
    }

    public void setUserRatingUploaded(boolean userRatingUploaded) {
        this.userRatingUploaded = userRatingUploaded;
    }

    public Collection<HighLight> getHighlights(DataBaseHelper db){
        ArrayList<HighLight> result = new ArrayList<HighLight>();
        DataContainer.refreshRouteForTrack(this, db);
        Track this_track = this.getTrack();
        db.getTrackDataDao().refresh(this_track);
        Collection<Step> these_steps = this.getTrack().getSteps();
        for(Step s : these_steps){
            DataContainer.refreshStep(s, db);
            Collection<HighLight> these_highlights = s.getHighlights();
            for(HighLight h: these_highlights){
                result.add(h);
            }
        }
        return result;
    }

}
