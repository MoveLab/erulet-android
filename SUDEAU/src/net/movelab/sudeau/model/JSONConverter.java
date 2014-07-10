package net.movelab.sudeau.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONConverter {
	
	public static Route jsonObjectToRoute(JSONObject j) throws JSONException{
		Route r = new Route();
		if(j.has("id")){
			r.setId(j.getString("id"));
		}
		if(j.has("name")){
			r.setName(j.getString("name"));
		}
		if(j.has("description")){
			r.setDescription(j.getString("description"));
		}
		if(j.has("idroutebasedon")){
			r.setIdRouteBasedOn(j.getString("idroutebasedon"));
		}
		if(j.has("reference")){
			if(j.getJSONObject("reference")!=null){
				Reference ref = jsonObjectToReference(j.getJSONObject("reference"));
				r.setReference(ref);
			}else{
				r.setReference(null);
			}
		}
		if(j.has("track")){
			if(j.getJSONObject("track")!=null){
				Track t = jsonObjectToTrack(j.getJSONObject("track"));
				r.setTrack(t);
			}else{
				r.setTrack(null);
			}
		}			
		if(j.has("userid")){
			r.setUserId(j.getString("userid"));
		}
		if(j.has("isecosystem")){
			r.setEcosystem(j.getBoolean("isecosystem"));
		}
		if(j.has("isuploaded")){
			r.setUpLoaded(j.getBoolean("isuploaded"));
		}
		if(j.has("eco")){
			if(j.getJSONObject("eco") != null){
				Route rt = jsonObjectToRoute(j.getJSONObject("eco"));
				r.setEco(rt);
			}else{
				r.setEco(null);
			}
		}
		return r;
	}
	
	public static Route jsonToRoute(String json) throws JSONException{
		JSONObject j = new JSONObject(json);
		Route r = jsonObjectToRoute(j);	
		return r;
	}
		
	
	public static Track jsonObjectToTrack(JSONObject j) throws JSONException{		
		Track t = new Track();
		if(j.has("id")){
			t.setId(j.getString("id"));
		}
		if(j.has("name")){
			t.setName(j.getString("name"));
		}
		if(j.has("reference")){
			if(j.getJSONObject("reference")!=null){
				Reference ref = jsonObjectToReference(j.getJSONObject("reference"));
				t.setReference(ref);
			}else{
				t.setReference(null);
			}
		}
		if(j.has("steps")){
			if(j.getJSONArray("steps")!=null){
				List<Step> steps = jsonArrayToStepList(j.getJSONArray("steps"));
				t.setSteps(steps);
			}
		}
		return t;
	}
	
	public static List<Step> jsonArrayToStepList(JSONArray a) throws JSONException{
		ArrayList<Step> retVal = new ArrayList<Step>();
		for(int i = 0; i < a.length(); i++){
			JSONObject jsonStep = a.getJSONObject(i);
			retVal.add(jsonObjectToStep(jsonStep));
		}
		return retVal;
	}
	
	public static Step jsonObjectToStep(JSONObject j) throws JSONException{
		Step s = new Step();
		SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
		if(j.has("id")){
			s.setId(j.getString("id"));
		}
		if(j.has("altitude")){
			s.setAltitude(j.getDouble("altitude"));
		}		
		if(j.has("latitude")){
			s.setLatitude(j.getDouble("latitude"));
		}
		if(j.has("longitude")){
			s.setLongitude(j.getDouble("longitude"));
		}
		if(j.has("name")){
			s.setName(j.getString("name"));
		}
		if(j.has("highlight")){
			JSONObject hl = j.getJSONObject("highlight");
			s.setHighlight(jsonObjectToHighLight(hl));
		}
		if(j.has("order")){
			s.setOrder(j.getInt("order"));
		}
		if(j.has("precision")){
			s.setPrecision(j.getDouble("precision"));
		}
		if(j.has("reference")){
			JSONObject ref = j.getJSONObject("reference");
			s.setReference(jsonObjectToReference(ref));
		}
		if(j.has("absoluteTime")){
			String dateString = j.getString("absoluteTime");			
			try {
				s.setAbsoluteTime(spdf.parse(dateString));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		if(j.has("relativeTime")){			
			s.setAbsoluteTimeMillis(j.getLong("relativeTime"));
		}		
		return s;
	}
	
	public static HighLight jsonObjectToHighLight(JSONObject j) throws JSONException{
		HighLight h = new HighLight();
		if(j.has("id")){
			h.setId(j.getString("id"));
		}
		if(j.has("longText")){
			h.setLongText(j.getString("longText"));
		}
		if(j.has("name")){
			h.setName(j.getString("name"));
		}
		if(j.has("imagePath")){
			h.setMediaPath(j.getString("imagePath"));
		}
		if(j.has("radius")){
			h.setRadius(j.getDouble("radius"));
		}		
		return h;
	}
	
	public static Reference jsonObjectToReference(JSONObject j) throws JSONException{		
		Reference r = new Reference();
		if(j.has("id")){
			r.setId(j.getString("id"));
		}
		if(j.has("name")){
			r.setName(j.getString("name"));
		}
		if(j.has("textContent")){
			r.setTextContent(j.getString("textContent"));
		}
		return r;
	}
	
	public static JSONObject highLightToJSONObject(HighLight h) throws JSONException{
		if(h == null)
			return null;
		JSONObject j = new JSONObject();
		j.put("id", h.getId());
		j.put("longText", h.getLongText());
		j.put("imagePath",h.getMediaPath());
		j.put("name", h.getName());
		j.put("radius", h.getRadius());
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
		j.put("isecosystem", r.isEcosystem());
		j.put("isuploaded", r.isUpLoaded());
		j.put("eco", routeToJSONObject(r.getEco()));
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
		j.put("highlight", highLightToJSONObject(s.getHighlight()));
		j.put("order", s.getOrder());
		j.put("precision", s.getPrecision());
		j.put("reference", referenceToJSONObject(s.getReference()));
		if(s.getAbsoluteTime()!=null){
			j.put("absoluteTime", spdf.format(s.getAbsoluteTime()));
		}
		j.put("relativeTime", s.getAbsoluteTimeMillis());
		return j;
	}
		
	public static JSONArray stepListToJSONArray(List<Step> steps) throws JSONException{
		JSONArray arr = new JSONArray();
		for(Step step : steps){
			arr.put(stepToJSONObject(step));
		}
		return arr;
	}
		

}
