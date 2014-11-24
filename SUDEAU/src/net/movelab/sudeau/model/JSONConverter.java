package net.movelab.sudeau.model;

import android.provider.ContactsContract;
import android.util.Log;

import net.movelab.sudeau.EruletApp;
import net.movelab.sudeau.Util;
import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;

import java.io.File;
import java.sql.Ref;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONConverter {

    public static ArrayList<Route> jsonArrayToRouteArray(JSONArray ja, DataBaseHelper db)
            throws JSONException {
        ArrayList<Route> ra = new ArrayList<Route>();
        if (ja != null && ja.length() > 0) {
            for (int i = 0; i < ja.length(); i++) {
                JSONObject this_j = ja.getJSONObject(i);
                Route this_r = jsonObjectToRoute(this_j, db);
                ra.add(this_r);
                Log.i("JSON Converter", "route added with server ID: " + this_r.getServerId());
            }
        }
        return ra;
    }



    public static Route jsonObjectToRoute(JSONObject j, DataBaseHelper db) throws JSONException {

        Route r;
        int server_id = j.optInt("server_id", -1);
        if(server_id != -1){
            r = DataContainer.findRouteByServerId(server_id, db);
            if(r == null){
                r = new Route();
                r.setServerId(j.optInt("server_id", -1));
            }
        } else{
            r = new Route();
            r.setServerId(j.optInt("server_id", -1));}


        r.setRouteJsonLastUpdatedNow();

        r.setOfficial(j.optBoolean("official", false));
        r.setGlobalRating((float) j.optDouble("average_rating", -1.0));
        r.setTotalRatings(j.optInt("total_ratings"));

        r.setName("oc", j.optString("name_oc", ""));
        r.setName("es", j.optString("name_es", ""));
        r.setName("ca", j.optString("name_ca", ""));
        r.setName("fr", j.optString("name_fr", ""));
        r.setName("en", j.optString("name_en", ""));

        r.setDescription("oc", j.optString("description_oc", ""));
        r.setDescription("es", j.optString("description_es", ""));
        r.setDescription("ca", j.optString("description_ca", ""));
        r.setDescription("fr", j.optString("description_fr", ""));
        r.setDescription("en", j.optString("description_en", ""));



            r.setIdRouteBasedOn(j.optInt("id_route_based_on", -1));

// TODO more languages
        if (j.has("reference")) {
            if (j.optJSONObject("reference") != null) {
                Reference ref = jsonObjectToReference(j.getJSONObject("reference"), db);
                r.setReference(ref);
            } else {
                r.setReference(null);
            }
        }
        if (j.has("track")) {
            if (j.getJSONObject("track") != null) {
                Track t = jsonObjectToTrack(j.getJSONObject("track"), r.getUniqueName(), db);
                r.setTrack(t);
                t.setRoute(r);
            } else {
                r.setTrack(null);
            }
        }
        if (j.has("created_by")) {
            r.setUserId(j.optString("created_by", ""));
        }
        // this is irrelevant - will never have this on the server
        if (j.has("isuploaded")) {
            r.setUpLoaded(j.getBoolean("isuploaded"));
        }
        r.setLocalCarto("none");
        if (j.has("map")) {
            if (j.getJSONObject("map") != null && !j.getJSONObject("map").equals("null")) {
                JSONObject mapj = j.getJSONObject("map");
                if(mapj.getString("map_file_name") != null){
                r.setLocalCarto(mapj.getString("map_file_name"));
                }
            }
        }


            r.setGlobalRating((float) j.optDouble("globalrating", -1.0));

        return r;
    }

    public static Route jsonToRoute(String json, DataBaseHelper db) throws JSONException {
        JSONObject j = new JSONObject(json);
        Route r = jsonObjectToRoute(j, db);
        return r;
    }


    public static Track jsonObjectToTrack(JSONObject j, String route_id, DataBaseHelper db) throws JSONException {

        Track t;
        int server_id = j.optInt("server_id", -1);
        if(server_id != -1){
           t = DataContainer.findTrackByServerId(server_id, db);
            if(t == null){
                t = new Track();
                t.setServerId(j.optInt("server_id", -1));
            }
        } else{
        t = new Track();
            t.setServerId(j.optInt("server_id", -1));}


        // again just using ca for now
        if (j.has("name_ca")) {
            t.setName(j.getString("name_ca"));
        }
        // TODO tracks will not have references
        if (j.has("reference")) {

        }
        if (j.has("steps")) {
            if (j.getJSONArray("steps") != null) {
                List<Step> steps = jsonArrayToStepList(j.getJSONArray("steps"), route_id, db);
                t.setSteps(steps);
                for(Step this_step : steps){
                    this_step.setTrack(t);
                }
            }
        }
        return t;
    }

    public static List<HighLight> jsonArrayToHighLightList(JSONArray a, Step s, DataBaseHelper db) throws JSONException {
        ArrayList<HighLight> retVal = new ArrayList<HighLight>();
        for (int i = 0; i < a.length(); i++) {
            JSONObject jsonHighLight = a.getJSONObject(i);
            HighLight hl = jsonObjectToHighLight(jsonHighLight, s, db);
                retVal.add(hl);

        }
        return retVal;
    }

    public static List<Step> jsonArrayToStepList(JSONArray a, String route_id, DataBaseHelper db) throws JSONException {
        ArrayList<Step> retVal = new ArrayList<Step>();
        for (int i = 0; i < a.length(); i++) {
            JSONObject jsonStep = a.getJSONObject(i);
            retVal.add(jsonObjectToStep(jsonStep, db));
        }
        return retVal;
    }

    public static Step jsonObjectToStep(JSONObject j, DataBaseHelper db) throws JSONException {

        Step s;
        int server_id = j.optInt("server_id");
        if(server_id != -1){
            s = DataContainer.findStepByServerId(server_id, db);
            if(s == null){
                s = new Step();
                s.setServerId(j.optInt("server_id", -1));
            }
        } else{
            s = new Step();
            s.setServerId(j.optInt("server_id", -1));}

        SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");

        s.setAltitude(j.optDouble("altitude", Util.DEFAULT_ALTITUDE));
        if (j.has("latitude")) {
            s.setLatitude(j.getDouble("latitude"));
        }
        if (j.has("longitude")) {
            s.setLongitude(j.getDouble("longitude"));
        }
        // since steps are not named on server, setting name to id
        s.setName("");
//		if(j.has("highlight")){
//			JSONObject hl = j.getJSONObject("highlight");
//			s.setHighlight(jsonObjectToHighLight(hl));
//		}
        if (j.has("highlights")) {
            JSONArray highLights = j.getJSONArray("highlights");
            s.setHighlights(jsonArrayToHighLightList(highLights, s, db));

        }

        s.setOrder(j.optInt("order", Util.DEFAULT_ORDER));

        s.setPrecision(j.optDouble("precision", Util.DEFAULT_PRECISION));

        if (j.has("absolute_time") && j.optString("absolute_time") != "null"){
            String dateString = j.getString("absolute_time");
            try {
                s.setAbsoluteTime(spdf.parse(dateString));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (j.has("relative_time") && j.optString("relative_time") != "null") {
            s.setAbsoluteTimeMillis(j.getLong("relative_time"));
        }
        return s;
    }

    public static HighLight jsonObjectToHighLight(JSONObject j, Step s, DataBaseHelper db) throws JSONException {

        HighLight h;
        int server_id = j.optInt("server_id");
        if(server_id != -1){
            h = DataContainer.findHighlightByServerId(server_id, db);
            if(h == null){
                h = new HighLight();
                h.setServerId(j.optInt("server_id", -1));
            }
        } else{
            h = new HighLight();
            h.setServerId(j.optInt("server_id", -1));}

        h.setStep(s);
        h.setGlobalRating((float) j.optDouble("average_rating", -1.0));
        h.setTotalRatings(j.optInt("total_ratings", -1));

        h.setName("oc", j.optString("name_oc", ""));
        h.setName("es", j.optString("name_es", ""));
        h.setName("ca", j.optString("name_ca", ""));
        h.setName("fr", j.optString("name_fr", ""));
        h.setName("en", j.optString("name_en", ""));

        h.setLongText("oc", j.optString("long_text_oc", ""));
        h.setLongText("es", j.optString("long_text_es", ""));
        h.setLongText("ca", j.optString("long_text_ca", ""));
        h.setLongText("fr", j.optString("long_text_fr", ""));
        h.setLongText("en", j.optString("long_text_en", ""));

        if (j.has("media_path") && !j.optString("media_path", "").isEmpty()) {
            FileManifest this_file_manifest = new FileManifest();
            this_file_manifest.setPath(j.optString("media_path", ""));
            h.setFileManifest(this_file_manifest);
        }


        h.setRadius(j.optDouble("radius", Util.DEFAULT_HIGHLIGHT_RADIUS));
        h.setType(j.optInt("type", 0) == 0 ? 3 : 1);

        // TODO deal with ratings

        JSONArray refs = j.optJSONArray("references");
        JSONArray iis = j.optJSONArray("interactive_images");


        if (refs != null) {
            ArrayList<Reference> reflist = new ArrayList<Reference>();
            for (int i = 0; i < refs.length(); i++) {
                Reference ref = jsonObjectToReference(refs.getJSONObject(i), db);
                ref.setHighlight(h);
                reflist.add(ref);
            }
            h.setReferences(reflist);
        }
        // Now the interactive images

        if (iis != null) {
            ArrayList<InteractiveImage> iilist = new ArrayList<InteractiveImage>();
            for (int i = 0; i < iis.length(); i++) {
                InteractiveImage ii = jsonObjectToInteractiveImage(iis.getJSONObject(i), db);
                ii.setHighlight(h);
                iilist.add(ii);
            }
            h.setInteractiveImages(iilist);

        }
        return h;
    }

    public static Reference jsonObjectToReference(JSONObject j, DataBaseHelper db) throws JSONException {

        Reference r;
        int server_id = j.optInt("server_id");
        if(server_id != -1){
            r = DataContainer.findReferenceByServerId(server_id, db);
            if(r == null){
                r = new Reference();
                r.setServerId(j.optInt("server_id", -1));
            }
        } else{
            r = new Reference();
            r.setServerId(j.optInt("server_id", -1));}


        r.setName(j.optString("name_ca", "none"));
        // TODO check that this is ok - I am not setting any reference file paths here -- only via file downloads
        return r;
    }

    public static InteractiveImage jsonObjectToInteractiveImage(JSONObject j, DataBaseHelper db) throws JSONException {

        InteractiveImage ii;
        int server_id = j.optInt("server_id");
        if(server_id != -1){
            ii = DataContainer.findInteractiveImageByServerId(server_id, db);
            if(ii == null){
                ii = new InteractiveImage();
                ii.setServerId(j.optInt("server_id", -1));
            }
        } else{
            ii = new InteractiveImage();
            ii.setServerId(j.optInt("server_id", -1));}

        ii.setOriginalHeight(j.optInt("original_height"));
        ii.setOriginalWidth(j.optInt("original_width"));
        JSONArray boxArray = j.optJSONArray("boxes");
        if (boxArray != null) {
            ii.setBoxes(jsonArrayToBoxes(boxArray, ii));
        }
        return ii;
    }

    public static ArrayList<Box> jsonArrayToBoxes(JSONArray ja, InteractiveImage ii) throws JSONException {
        ArrayList<Box> result = new ArrayList<Box>();
        for (int i = 0; i < ja.length(); i++) {
            JSONObject j = ja.optJSONObject(i);
            Box b = new Box(j.optInt("id"), j.optInt("min_x"), j.optInt("min_y"), j.optInt("max_x"), j.optInt("max_y"), ii);
            // TODO languages
            b.setMessage(j.optString("message_ca"));
            result.add(b);
        }
        return result;
    }


    public static JSONObject highLightToJSONObject(HighLight h, EruletApp app) throws JSONException {
        if (h == null)
            return null;
        JSONObject j = new JSONObject();
        j.put("id", h.getId());
        if(h.getFileManifest() != null){
        DataContainer.refreshHighlightForFileManifest(h, app.getDataBaseHelper());
            if(h.getFileManifest().getPath() != null && !h.getFileManifest().getPath().isEmpty()){
        j.put("media_path", h.getFileManifest().getPath());
            }
        }
        j.put("name_oc", h.getName("oc"));
        j.put("name_es", h.getName("es"));
        j.put("name_ca", h.getName("ca"));
        j.put("name_fr", h.getName("fr"));
        j.put("name_en", h.getName("en"));

        j.put("long_text_oc", h.getLongText("oc"));
        j.put("long_text_es", h.getLongText("es"));
        j.put("long_text_ca", h.getLongText("ca"));
        j.put("long_text_fr", h.getLongText("fr"));
        j.put("long_text_en", h.getLongText("en"));
        j.put("radius", h.getRadius());
        j.put("type", h.getType());
        j.put("user_rating", h.getUserRating());
        j.put("average_rating", h.getGlobalRating());
        j.put("total_ratings", h.getTotalRatings());
        List<Reference> reference_list = DataContainer.getHighlightReferences(h, app.getDataBaseHelper());
        j.put("references", referenceListToJSONArray(reference_list));

        List<InteractiveImage> ii_list = DataContainer.getHighlightIIs(h, app.getDataBaseHelper());
        j.put("interactive_images", iiListToJSONArray(ii_list));

        return j;
    }

    public static JSONObject userHighLightToServerJSONObject(HighLight h, EruletApp app) throws JSONException {
        if (h == null)
            return null;
        JSONObject j = new JSONObject();

        j.put("id_on_creator_device", h.getId());

        j.put("name_oc", h.getName("oc"));
        j.put("name_es", h.getName("es"));
        j.put("name_ca", h.getName("ca"));
        j.put("name_fr", h.getName("fr"));
        j.put("name_en", h.getName("en"));

        j.put("long_text_oc", h.getLongText("oc"));
        j.put("long_text_es", h.getLongText("es"));
        j.put("long_text_ca", h.getLongText("ca"));
        j.put("long_text_fr", h.getLongText("fr"));
        j.put("long_text_en", h.getLongText("en"));

        j.put("radius", h.getRadius());
        j.put("type", h.getServerType());

        return j;
    }


    public static JSONObject trackToJSONObject(Track t, EruletApp app) throws JSONException {
        if (t == null)
            return null;
        JSONObject j = new JSONObject();
        j.put("id", t.getId());
        j.put("name", t.getName());
        j.put("reference", t.getReference());
        List<Step> steps = DataContainer.getTrackSteps(t, app.getDataBaseHelper());
        j.put("steps", stepListToJSONArray(steps, app));
        return j;
    }

    public static JSONObject userTrackToServerJSONObject(Track t, EruletApp app) throws JSONException {
        if (t == null)
            return null;
        JSONObject j = new JSONObject();
        List<Step> steps = DataContainer.getTrackSteps(t, app.getDataBaseHelper());
        j.put("steps", userStepListToServerJSONArray(steps, app));
        return j;
    }


    public static JSONObject routeToJSONObject(Route r, EruletApp app) throws JSONException {
        if (r == null)
            return null;
        JSONObject j = new JSONObject();
        j.put("id", r.getId());
        j.put("server_id", r.getServerId());

        j.put("name_oc", r.getName("oc"));
        Util.logInfo(app, "r2joc", r.getName("oc"));
        j.put("name_es", r.getName("es"));
        Util.logInfo(app, "r2jes", r.getName("es"));
        j.put("name_ca", r.getName("ca"));
        j.put("name_fr", r.getName("fr"));
        j.put("name_en", r.getName("en"));
        j.put("id_route_based_on", r.getIdRouteBasedOn());
        j.put("reference", referenceToJSONObject(r.getReference()));
        j.put("track", trackToJSONObject(r.getTrack(), app));
        j.put("userid", r.getUserId());
        j.put("isuploaded", r.isUpLoaded());
        j.put("localcarto", r.getLocalCarto());
        j.put("user_rating", r.getUserRating());
        j.put("average_rating", r.getGlobalRating());
        j.put("total_ratings", r.getTotalRatings());
        return j;
    }

    public static JSONObject userRouteToServerJSONObject(Route r, EruletApp app) throws JSONException {
        if (r == null)
            return null;
        JSONObject j = new JSONObject();

        j.put("server_id", r.getServerId());

        j.put("name_oc", r.getName("oc"));
        j.put("name_es", r.getName("es"));
        j.put("name_ca", r.getName("ca"));
        j.put("name_fr", r.getName("fr"));
        j.put("name_en", r.getName("en"));

        j.put("description_oc", r.getDescription("oc"));
        j.put("description_es", r.getDescription("es"));
        j.put("description_ca", r.getDescription("ca"));
        j.put("description_fr", r.getDescription("fr"));
        j.put("description_en", r.getDescription("en"));

        j.put("id_route_based_on", r.getIdRouteBasedOn());

        j.put("track", userTrackToServerJSONObject(r.getTrack(), app));

        return j;
    }


    public static JSONObject referenceToJSONObject(Reference r) throws JSONException {
        if (r == null)
            return null;
        JSONObject j = new JSONObject();
        j.put("id", r.getId());
        j.put("name", r.getName());
// TODO check if we need manifests attached too
        return j;
    }


    public static JSONObject fmToJSONObject(FileManifest fm) throws JSONException {
        if (fm == null)
            return null;
        JSONObject j = new JSONObject();
        j.put("id", fm.getId());
        j.put("path", fm.getPath());
        return j;
    }


    public static JSONObject iiToJSONObject(InteractiveImage ii) throws JSONException {
        if (ii == null)
            return null;
        JSONObject j = new JSONObject();
        j.put("id", ii.getId());
        j.put("original_width", ii.getOriginalWidth());
        j.put("original_height", ii.getOriginalHeight());
        // TODO check if we need manifests too
        return j;
    }

    public static JSONArray iiListToJSONArray(Collection<InteractiveImage> iilist) throws JSONException {
        if (iilist == null)
            return null;
        JSONArray ja = new JSONArray();
        for(InteractiveImage ii:iilist){
            ja.put(iiToJSONObject(ii));
        }
        return ja;
    }

    public static JSONArray referenceListToJSONArray(Collection<Reference> rlist) throws JSONException {
        if (rlist == null)
            return null;
        JSONArray ja = new JSONArray();
        for(Reference ref:rlist){
            ja.put(referenceToJSONObject(ref));
        }
        return ja;
    }

    public static JSONObject stepToJSONObject(Step s, EruletApp app) throws JSONException {
        JSONObject j = new JSONObject();
        SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
        j.put("id", s.getId());
        j.put("altitude", s.getAltitude());
        j.put("latitude", s.getLatitude());
        j.put("longitude", s.getLongitude());
        j.put("name", s.getName());
        //j.put("highlight", highLightToJSONObject(s.getHighlight()));
        List<HighLight> highLights = DataContainer.getStepHighLights(s, app.getDataBaseHelper());
        j.put("highlights", highLightListToJSONArray(highLights, app));
        j.put("order", s.getOrder());
        j.put("precision", s.getPrecision());
        j.put("reference", referenceToJSONObject(s.getReference()));
        if (s.getAbsoluteTime() != null) {
            j.put("absoluteTime", spdf.format(s.getAbsoluteTime()));
        }
        j.put("relativeTime", s.getAbsoluteTimeMillis());
        return j;
    }

    public static JSONObject userStepToServerJSONObject(Step s, EruletApp app) throws JSONException {
        JSONObject j = new JSONObject();
        j.put("altitude", s.getAltitude());
        j.put("latitude", s.getLatitude());
        j.put("longitude", s.getLongitude());
        List<HighLight> highLights = DataContainer.getStepHighLights(s, app.getDataBaseHelper());
        j.put("highlights", userHighLightListToServerJSONArray(highLights, app));
        j.put("order", s.getOrder());
        j.put("precision", s.getPrecision());
        if (s.getAbsoluteTime() != null) {
            j.put("absoluteTime", Util.ecma262(s.getAbsoluteTimeMillis()));
        }
        return j;
    }


    public static JSONArray highLightListToJSONArray(List<HighLight> highLights, EruletApp app) throws JSONException {
        JSONArray arr = new JSONArray();
        for (HighLight highLight : highLights) {
            arr.put(highLightToJSONObject(highLight, app));
        }

        return arr;
    }

    public static JSONArray stepListToJSONArray(List<Step> steps, EruletApp app) throws JSONException {
        JSONArray arr = new JSONArray();
        for (Step step : steps) {
            arr.put(stepToJSONObject(step, app));
        }
        return arr;
    }

    public static JSONArray userHighLightListToServerJSONArray(List<HighLight> highLights, EruletApp app) throws JSONException {
        JSONArray arr = new JSONArray();
        for (HighLight highLight : highLights) {
            arr.put(userHighLightToServerJSONObject(highLight, app));
        }

        return arr;
    }

    public static JSONArray userStepListToServerJSONArray(List<Step> steps, EruletApp app) throws JSONException {
        JSONArray arr = new JSONArray();
        for (Step step : steps) {
            arr.put(userStepToServerJSONObject(step, app));
        }
        return arr;
    }

    public static JSONObject userHighlightRatingToServerJSONObject(HighLight hl) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("rating", hl.getUserRating());
        result.put("time", Util.ecma262(hl.getUserRatingTime()));
        result.put("highlight", hl.getServerId());
        return result;
    }

    public static JSONObject userRouteRatingToServerJSONObject(Route r) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("rating", r.getUserRating());
        result.put("time", Util.ecma262(r.getUserRatingTime()));
        result.put("route", r.getServerId());
        return result;
    }



}
