package net.movelab.sudeau.model;

import android.util.Log;

import net.movelab.sudeau.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONConverter {

    public static ArrayList<Route> jsonArrayToRouteArray(JSONArray ja)
     throws JSONException{
        ArrayList<Route> ra = new ArrayList<Route>();
        if (ja != null && ja.length() > 0){
            for(int i = 0; i < ja.length(); i++){
                JSONObject this_j = ja.getJSONObject(i);
                Route this_r = jsonObjectToRoute(this_j);
                ra.add(this_r);
            }
        }
        return ra;
    }


	public static Route jsonObjectToRoute(JSONObject j) throws JSONException{
		Route r = new Route();
		if(j.has("id")){
			r.setId(j.getString("id"));
		}
        // for now, just using Catalan -- will  update models for multlingual
		if(j.has("name_ca")){
			r.setName(j.getString("name_ca"));
		}
		if(j.has("description_ca")){
			r.setDescription(j.getString("description_ca"));
		}
		if(j.has("id_route_based_on")){
			r.setIdRouteBasedOn(j.getString("id_route_based_on"));
		}

// TODO more languages
		if(j.has("reference")){
			if(j.optJSONObject("reference")!=null){
				Reference ref = jsonObjectToReference(j.getJSONObject("reference"), r.getId(), "ca");
				r.setReference(ref);
			}else{
				r.setReference(null);
			}
		}
		if(j.has("track")){
			if(j.getJSONObject("track")!=null){
				Track t = jsonObjectToTrack(j.getJSONObject("track"), r.getId());
				r.setTrack(t);
			}else{
				r.setTrack(null);
			}
		}			
		if(j.has("created_by")){
			r.setUserId(j.getString("created_by"));
		}
        // this is irrelevant - will never have this on the server
		if(j.has("isuploaded")){
			r.setUpLoaded(j.getBoolean("isuploaded"));
		}		
		if(j.has("local_carto_name")){
			r.setLocalCarto(j.getString("local_carto_name"));
		}
        // We will need to change this and instead grab just the calcualted global rating
		if(j.has("userrating")){
			r.setUserRating(j.getInt("userrating"));
		}

		if(j.has("globalrating")){
			r.setGlobalRating(j.getInt("globalrating"));
		}
		return r;
	}
	
	public static Route jsonToRoute(String json) throws JSONException{
		JSONObject j = new JSONObject(json);
		Route r = jsonObjectToRoute(j);	
		return r;
	}
		
	
	public static Track jsonObjectToTrack(JSONObject j, String route_id) throws JSONException{
		Track t = new Track();
		if(j.has("id")){
			t.setId(j.getString("id"));
		}
        // again just using ca for now
		if(j.has("name_ca")){
			t.setName(j.getString("name_ca"));
		}
        // TODO tracks will not have references
		if(j.has("reference")){

			}
		if(j.has("steps")){
			if(j.getJSONArray("steps")!=null){
				List<Step> steps = jsonArrayToStepList(j.getJSONArray("steps"), route_id);
				t.setSteps(steps);
			}
		}
		return t;
	}
	
	public static List<HighLight> jsonArrayToHighLightList(JSONArray a, String route_id) throws JSONException{
		ArrayList<HighLight> retVal = new ArrayList<HighLight>();
		for(int i = 0; i < a.length(); i++){
			JSONObject jsonHighLight = a.getJSONObject(i);
			retVal.add(jsonObjectToHighLight(jsonHighLight, route_id));
		}
		return retVal;
	}
	
	public static List<Step> jsonArrayToStepList(JSONArray a, String route_id) throws JSONException{
		ArrayList<Step> retVal = new ArrayList<Step>();
		for(int i = 0; i < a.length(); i++){
			JSONObject jsonStep = a.getJSONObject(i);
			retVal.add(jsonObjectToStep(jsonStep, route_id));
		}
		return retVal;
	}
	
	public static Step jsonObjectToStep(JSONObject j,String route_id) throws JSONException{
		Step s = new Step();
		SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
		if(j.has("id")){
			s.setId(j.getString("id"));
		}

		s.setAltitude(j.optDouble("altitude", Util.DEFAULT_ALTITUDE));
		if(j.has("latitude")){
			s.setLatitude(j.getDouble("latitude"));
		}
		if(j.has("longitude")){
			s.setLongitude(j.getDouble("longitude"));
		}
        // since steps are not named on server, setting name to id
		s.setName("steps" + s.getId());
//		if(j.has("highlight")){
//			JSONObject hl = j.getJSONObject("highlight");
//			s.setHighlight(jsonObjectToHighLight(hl));
//		}
		if(j.has("highlights")){
			JSONArray highLights = j.getJSONArray("highlights");
			s.setHighlights(jsonArrayToHighLightList(highLights, route_id));
		}

		s.setOrder(j.optInt("order", Util.DEFAULT_ORDER));

        // check this
    	s.setPrecision(j.optDouble("precision", Util.DEFAULT_PRECISION));

        // Will need to change model so that we can have multiple references and also interactive images
        // TODO Step should not have referecence
		if(j.has("reference")){
		}
		if(j.has("absolute_time")){
			String dateString = j.getString("absolute_time");
			try {
				s.setAbsoluteTime(spdf.parse(dateString));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		if(j.has("relative_time")){
			s.setAbsoluteTimeMillis(j.getLong("relative_time"));
		}		
		return s;
	}
	
	public static HighLight jsonObjectToHighLight(JSONObject j, String route_id) throws JSONException{
		HighLight h = new HighLight();
		if(j.has("id")){
			h.setId(j.getString("id"));
		}
        // languages...
			h.setLongText(j.optString("long_text_ca", "none"));
			h.setName(j.optString("name_ca", "none"));

        // TODO add this to server
        if(j.has("media_name")){
			h.setMediaPath(Util.makeHighlightMediaPath(h.getId(), route_id, j.optString("media_name", "")));
        }
        // check if this makes sense and come up with good default radius
		h.setRadius(j.optDouble("radius", Util.DEFAULT_HIGHLIGHT_RADIUS));

			h.setType(j.optInt("type", 0));

        // TODO fix this
			h.setUserRating(j.optInt("userrating", 5));
			h.setGlobalRating(j.optInt("globalrating", 5));

        //  TODO this will need to become multiple references

			JSONArray refs = j.optJSONArray("references");
        Log.d("REFERENCES JSON ARRAY: ", refs.toString());
            if(refs.length() > 0){
			h.setReference(jsonObjectToReference(refs.getJSONObject(0), route_id, h.getId(),"ca" ));
                Log.d("REFERENCES JSON OBJECT 0: ", h.getReference().toString());
            }
		return h;
	}
	
	public static Reference jsonObjectToReference(JSONObject j, String route_id, String lang_code) throws JSONException{
		Reference r = new Reference();
        // TODO language issue
			r.setId(j.optString("id", "none"));
			r.setName(j.optString("name_ca", "none"));
    // TODO deal with this
			r.setTextContent(Util.makeReferencePath(route_id, lang_code));
		return r;
	}

    public static Reference jsonObjectToReference(JSONObject j, String route_id, String highlight_id, String lang_code) throws JSONException{
        Reference r = new Reference();
        r.setId(j.optString("id", "none"));
        r.setName(j.optString("name_ca", "none"));
        // TODO deal with this
        r.setTextContent(Util.makeReferencePath(route_id, highlight_id, r.getId(), lang_code));
        return r;
    }

    public static JSONObject highLightToJSONObject(HighLight h) throws JSONException{
		if(h == null)
			return null;
		JSONObject j = new JSONObject();
		j.put("id", h.getId());
		j.put("long_text", h.getLongText());
		j.put("image_path",h.getMediaPath());
		j.put("name", h.getName());
		j.put("radius", h.getRadius());
		j.put("type", h.getType());
		j.put("user_rating", h.getUserRating());
		j.put("global_rating", h.getGlobalRating());
		j.put("reference", h.getReference());
		return j;
	}
	
	public static JSONObject trackToJSONObject(Track t) throws JSONException{
		if(t==null)
			return null;
		JSONObject j = new JSONObject();
		j.put("id", t.getId());
		j.put("name", t.getName());
		j.put("reference", t.getReference());
		ArrayList<Step> steps = new ArrayList<Step>(t.getSteps());
		j.put("steps", stepListToJSONArray(steps) );
		return j;
	}
	
	public static JSONObject routeToJSONObject(Route r) throws JSONException {
		if(r == null)
			return null;
		JSONObject j = new JSONObject();
		j.put("id", r.getId());
		j.put("name", r.getName());
		j.put("description", r.getDescription());		
		j.put("idroutebasedon", r.getIdRouteBasedOn());		
		j.put("reference", referenceToJSONObject(r.getReference()));
		j.put("track", trackToJSONObject(r.getTrack()));
		j.put("userid", r.getUserId());		
		j.put("isuploaded", r.isUpLoaded());
		j.put("localcarto", r.getLocalCarto());
		j.put("userrating", r.getUserRating());
		j.put("globalrating", r.getGlobalRating());
		return j;
	}
	
	public static JSONObject referenceToJSONObject(Reference r) throws JSONException{
		if(r == null)
			return null;
		JSONObject j = new JSONObject();
		j.put("id", r.getId());
		j.put("name", r.getName());
		//r.getMedia();		
		j.put("textContent",r.getTextContent());		
		return j;
	}
	
	public static JSONObject stepToJSONObject(Step s) throws JSONException{
		JSONObject j = new JSONObject();
		SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
		j.put("id",s.getId());
		j.put("altitude", s.getAltitude());
		j.put("latitude", s.getLatitude());
		j.put("longitude", s.getLongitude());
		j.put("name", s.getName());
		//j.put("highlight", highLightToJSONObject(s.getHighlight()));
		ArrayList<HighLight> highLights= new ArrayList<HighLight>(s.getHighlights());
		j.put("highlights", highLightListToJSONArray(highLights) );
		j.put("order", s.getOrder());
		j.put("precision", s.getPrecision());
		j.put("reference", referenceToJSONObject(s.getReference()));
		if(s.getAbsoluteTime()!=null){
			j.put("absoluteTime", spdf.format(s.getAbsoluteTime()));
		}
		j.put("relativeTime", s.getAbsoluteTimeMillis());
		return j;
	}
	
	public static JSONArray highLightListToJSONArray(List<HighLight> highLights) throws JSONException{
		JSONArray arr = new JSONArray();
		for(HighLight highLight: highLights){
			//arr.put(stepToJSONObject(step));
			arr.put(highLightToJSONObject(highLight));
		}
		return arr;
	}
		
	public static JSONArray stepListToJSONArray(List<Step> steps) throws JSONException{
		JSONArray arr = new JSONArray();
		for(Step step : steps){
			arr.put(stepToJSONObject(step));
		}
		return arr;
	}
		

}
