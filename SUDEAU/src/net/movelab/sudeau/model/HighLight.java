package net.movelab.sudeau.model;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;

@DatabaseTable(tableName = "highlight")
public class HighLight {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private int server_id;
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
    private String longText_oc;
    @DatabaseField
    private String longText_es;
    @DatabaseField
    private String longText_ca;
    @DatabaseField
    private String longText_fr;
    @DatabaseField
    private String longText_en;
    @DatabaseField
    private String media_url;
    @DatabaseField(foreign = true, columnName = "fileManifestId")
    private FileManifest file_manifest;
    @DatabaseField
    private double radius;
    @DatabaseField
    private int type;
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
    @DatabaseField(foreign = true, columnName = "stepId")
    private Step step;
    @ForeignCollectionField
    private Collection<Reference> references;
    @ForeignCollectionField
    private Collection<InteractiveImage> interactiveImages;

    public static final int WAYPOINT = 1;
    public static final int POINT_OF_INTEREST = 2;
    public static final int POINT_OF_INTEREST_OFFICIAL = 3;
    public static final int ALERT = 4;
    public static final int POINT_OF_INTEREST_SHARED = 5;
    public static final int CONTAINER_N = 6;
    public static final int INTERACTIVE_IMAGE = 7;
    public static final int REFERENCE = 8;

    public HighLight() {
        this.server_id = -1;
    }


    public FileManifest getFileManifest() {
        return file_manifest;
    }

    public void setFileManifest(FileManifest file_manifest) {
        this.file_manifest = file_manifest;
    }

    public int getServerType() {
        int SERVER_POINT_OF_INTEREST = 0;
        int SERVER_WAYPOINT = 1;
        int SERVER_ALERT = 2;

        int result = 0;
        if (type == WAYPOINT) {
            result = SERVER_WAYPOINT;
        } else if (type == POINT_OF_INTEREST || type == POINT_OF_INTEREST_OFFICIAL || type == POINT_OF_INTEREST_SHARED) {
            result = SERVER_POINT_OF_INTEREST;
        } else if (type == ALERT) {
            result = SERVER_ALERT;
        }
        return result;
    }


    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
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

    public String getMediaUrl() {
        return media_url;
    }

    public void setMediaUrl(String media_url) {
        this.media_url = media_url;
    }



    @Override
    public String toString() {
        return "HIGHLIGHT " + id + " " + getFirstfilledName() + " " + radius;
    }


    public String getFirstfilledName() {
        String result = name_oc;
        if (result != null && !result.isEmpty())
            return result;
        result = name_es;
        if (result != null && !result.isEmpty())
            return result;
        result = name_ca;
        if (result != null && !result.isEmpty())
            return result;
        result = name_fr;
        if (result != null && !result.isEmpty())
            return result;
        result = name_en;
        if (result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getName(String lang) {
        String result = "";
        if (lang.equals("oc")) {
            result = name_oc;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("es")) {
            result = name_es;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("ca")) {
            result = name_ca;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("fr")) {
            result = name_fr;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("en"))
            result = name_en;
        {
            if (result != null && !result.isEmpty())
                return result;
        }
        return getFirstfilledName();
    }

    public void setName(String lang, String name) {
        if (lang.equals("oc"))
            this.name_oc = name;
        if (lang.equals("es"))
            this.name_es = name;
        if (lang.equals("ca"))
            this.name_ca = name;
        if (lang.equals("fr"))
            this.name_fr = name;
        if (lang.equals("en"))
            this.name_en = name;
    }


    public String getFirstfilledLongText() {
        String result = longText_oc;
        if (result != null && !result.isEmpty())
            return result;
        result = longText_es;
        if (result != null && !result.isEmpty())
            return result;
        result = longText_ca;
        if (result != null && !result.isEmpty())
            return result;
        result = longText_fr;
        if (result != null && !result.isEmpty())
            return result;
        result = longText_en;
        if (result != null && !result.isEmpty())
            return result;
        return "";
    }

    public String getLongText(String lang) {
        String result = "";
        if (lang.equals("oc")) {
            result = longText_oc;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("es")) {
            result = longText_es;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("ca")) {
            result = longText_ca;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("fr")) {
            result = longText_fr;
            if (result != null && !result.isEmpty())
                return result;
        }
        if (lang.equals("en"))
            result = longText_en;
        {
            if (result != null && !result.isEmpty())
                return result;
        }
        return getFirstfilledLongText();
    }

    public void setLongText(String lang, String longText) {
        if (lang.equals("oc"))
            this.longText_oc = longText;
        if (lang.equals("es"))
            this.longText_es = longText;
        if (lang.equals("ca"))
            this.longText_ca = longText;
        if (lang.equals("fr"))
            this.longText_fr = longText;
        if (lang.equals("en"))
            this.longText_en = longText;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getGlobalRating() {
        Log.i("hl global get", "rating: " + globalRating);
        return globalRating;
    }

    public void setGlobalRating(float globalRating) {
        this.globalRating = globalRating;
        Log.i("hl global", "rating: " + this.globalRating);
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


    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public Collection<Reference> getReferences() {
        return references;
    }

    public void setReferences(Collection<Reference> references) {
        this.references = references;
    }

    public Collection<InteractiveImage> getInteractiveImages() {
        return interactiveImages;
    }

    public void setInteractiveImages(Collection<InteractiveImage> interactiveImages) {
        this.interactiveImages = interactiveImages;

    }

    // Note this should only be called after refreshing highlight for file manifest
    public boolean hasMediaFile() {
        if (this.getFileManifest() != null && this.getFileManifest().getPath() != null) {
            // report error
        }
        return this.getFileManifest() != null && this.getFileManifest().getPath() != null && !this.getFileManifest().getPath().isEmpty();
    }

}
