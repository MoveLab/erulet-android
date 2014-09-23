package net.movelab.sudeau.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.movelab.sudeau.EruletApp;
import net.movelab.sudeau.Util;
import net.movelab.sudeau.model.Box;
//import net.movelab.sudeau.model.EruMedia;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings.Secure;
import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class DataContainer {

	/**
	 * Incremental unique route id
	 * 
	 * @param db
	 * @param userId
	 * @return
	 */
	public static String getRouteId(DataBaseHelper db, String userId) {
		QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao()
				.queryBuilder();
		Where<Route, String> where = queryBuilder.where();
		String retVal = null;
		try {
			where.eq("userId", userId);
			PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
			List<Route> userRoutes = db.getRouteDataDao().query(preparedQuery);
			if (userRoutes != null) {
				// int c = userRoutes.size() + 1;
				List<String> routeIds = getRouteIds(userRoutes);
				int c = getMaxCounter(routeIds);
				retVal = "R_" + userId + "_" + c;
			} else {
				retVal = "R_" + userId + "_1";
			}			
			Log.d("getRouteId", "Returning route id" + retVal);			
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	/**
	 * Incremental unique route id
	 * 
	 * @param db
	 * @param userId
	 * @return
	 */
	public static String getHighLightId(DataBaseHelper db, String userId) {
		QueryBuilder<HighLight, String> queryBuilder = db.getHlDataDao()
				.queryBuilder();
		Where<HighLight, String> where = queryBuilder.where();
		String retVal = null;
		try {
			where.like("id", "%" + userId + "%");
			PreparedQuery<HighLight> preparedQuery = queryBuilder.prepare();
			List<HighLight> userHighLights = db.getHlDataDao().query(
					preparedQuery);
			if (userHighLights != null) {
				// int c = userRoutes.size() + 1;
				List<String> highLightIds = getHighLightIds(userHighLights);
				int c = getMaxCounter(highLightIds);
				retVal = "H_" + userId + "_" + c;
			} else {
				retVal = "H_" + userId + "_1";
			}			
			Log.d("getHighLightId", "Returning higlight id" + retVal);			
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	private static List<String> getHighLightIds(List<HighLight> userHighLights) {
		ArrayList<String> retVal = new ArrayList<String>();
		for (int i = 0; i < userHighLights.size(); i++) {
			retVal.add(userHighLights.get(i).getId());
		}
		return retVal;
	}

	private static List<String> getRouteIds(List<Route> userRoutes) {
		ArrayList<String> retVal = new ArrayList<String>();
		for (int i = 0; i < userRoutes.size(); i++) {
			retVal.add(userRoutes.get(i).getId());
		}
		return retVal;
	}

	/**
	 * Creates an empty route/track entry to the database, and returns the route
	 * object
	 */
	public static Route createEmptyRoute(DataBaseHelper db, String userId,
			String routeBasedOnId) {
		String idTrack = getTrackId(db, userId);
		Log.d("createEmptyRoute", "Getting track id " + idTrack);
		Track t = new Track();
		t.setId(idTrack);
		db.getTrackDataDao().create(t);
		String idRoute = getRouteId(db, userId);
		Log.d("createEmptyRoute", "Getting route id " + idRoute);
		Route r = new Route();
		r.setId(idRoute);
		r.setIdRouteBasedOn(routeBasedOnId);
		r.setName("La meva ruta");
		r.setDescription("La meva descripcio");
		r.setUserId(userId);
		r.setTrack(t);
		db.getRouteDataDao().create(r);
		Log.d("createEmptyRoute", "Route " + idRoute + " saved");		
		return r;
	}

	public static void addStepToTrack(Step s, Track t, String userId,
			DataBaseHelper db) {
		// Track is already created
		s.setTrack(t);
		String stepId = getStepId(db, userId);		
		Log.d("addStepToTrack", "Getting step id " + stepId);		
		s.setId(stepId);
		db.getStepDataDao().create(s);		
		Log.d("addStepToTrack", "step " + stepId + " saved");		
		t.getSteps().add(s);
	}

	public static void addHighLightToStep(Step s, HighLight h, String userId,
			DataBaseHelper db) {
		// Step already exists
		h.setStep(s);
		String hlId = getHighLightId(db, userId);
		h.setId(hlId);		
		db.getHlDataDao().create(h);		
		Log.d("addHighLightToStep", "highlight " + hlId + " saved");
//		s.getHighlights().add(h);
//		s.setHighlight(h);
		db.getStepDataDao().update(s);		
		Log.d("addHighLightToStep", "step " + s.getId()
				+ " updated (added highlight)");		
	}

	public static Route refreshRoute(Route r, DataBaseHelper db) {
		db.getRouteDataDao().refresh(r);
		return r;
	}
	
	public static Reference refreshReference(Reference r, DataBaseHelper db) {
		db.getReferenceDataDao().refresh(r);
		return r;
	}

	public static void deleteRouteCascade(Route r, EruletApp app) {
		if (r.getTrack() != null) {
			deleteTrackCascade(r.getTrack(), app);
		}
		if(r.getReference() != null){
			deleteReference(r.getReference(), app);
		}		
		app.getDataBaseHelper().getRouteDataDao().delete(r);
		SharedPreferences.Editor ed = app.getPrefsEditor();
		ed.remove(r.getId());
		ed.commit();
	}
	
	public static void deleteInteractiveImageCascade(InteractiveImage img, EruletApp app) {
		app.getDataBaseHelper().getInteractiveImageDataDao().refresh(img);
		if(img.getBoxes() != null){
			List<Box> boxes = getInteractiveImageBoxes(img, app.getDataBaseHelper());
			for (Box b : boxes) {
				app.getDataBaseHelper().getBoxDataDao().delete(b);
			}
		}
		app.getDataBaseHelper().getInteractiveImageDataDao().delete(img);
	}

	public static void deleteTrackCascade(Track t, EruletApp app) {
		if (t.getSteps() != null) {
			List<Step> steps = getTrackSteps(t, app.getDataBaseHelper());
			for (Step s : steps) {
				deleteStepCascade(s, app);
			}
		}
		app.getDataBaseHelper().getTrackDataDao().delete(t);
	}

	public static void deleteStepCascade(Step s, EruletApp app) {
//		if (s.getHighlight() != null) {
//			deleteHighLight(s.getHighlight(), app);
//		}
		if(s.getHighlights()!= null){
			for(HighLight h : s.getHighlights()){
				deleteHighLight(h, app);
			}
		}
		if(s.getReference() != null){
			deleteReference(s.getReference(), app);
		}		
		app.getDataBaseHelper().getStepDataDao().delete(s);
	}
	
	public static void deleteReference(Reference r, EruletApp app) {
		app.getDataBaseHelper().getReferenceDataDao().delete(r);
	}

	public static void deleteHighLight(HighLight h, EruletApp app) {
		if(h.getReference()!=null){
			app.getDataBaseHelper().getReferenceDataDao().delete(h.getReference());
		}
		if(h.getInteractiveImage() != null){
			deleteInteractiveImageCascade(h.getInteractiveImage(), app);
		}
		app.getDataBaseHelper().getHlDataDao().delete(h);
		SharedPreferences.Editor ed = app.getPrefsEditor();
		ed.remove(h.getId());
		ed.commit();
	}

	/**
	 * Incremental unique track id
	 * 
	 * @param db
	 * @param userId
	 * @return
	 */
	public static String getTrackId(DataBaseHelper db, String userId) {
		QueryBuilder<Track, String> queryBuilder = db.getTrackDataDao()
				.queryBuilder();
		Where<Track, String> where = queryBuilder.where();
		String retVal = null;
		try {
			where.like("id", "%" + userId + "%");
			PreparedQuery<Track> preparedQuery = queryBuilder.prepare();
			List<Track> trackRoutes = db.getTrackDataDao().query(preparedQuery);
			if (trackRoutes != null) {
				List<String> ids = getTrackIds(trackRoutes);
				int c = getMaxCounter(ids);
				// int c = trackRoutes.size() + 1;
				retVal = "T_" + userId + "_" + c;
			} else {
				retVal = "T_" + userId + "_1";
			}			
			Log.d("getTrackId", "Returning track id" + retVal);			
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	private static List<String> getTrackIds(List<Track> tracks) {
		ArrayList<String> ids = new ArrayList<String>();
		for (int i = 0; i < tracks.size(); i++) {
			ids.add(tracks.get(i).getId());
		}
		return ids;
	}

	private static int getMaxCounter(List<String> ids) {
		// Each string has the structure [CHARACTER]_[Android_id]_[Counter]
		// We want to extract the counter
		if (ids.size() == 0) {
			return 1;
		} else {
			int max = 0;
			for (int i = 0; i < ids.size(); i++) {
				String counter_s = ids.get(i).split("_")[2];
				int counter = Integer.parseInt(counter_s);
				if (counter > max)
					max = counter;
			}
			return max + 1;
		}
	}

	public static String getStepId(DataBaseHelper db, String userId) {
		QueryBuilder<Step, String> queryBuilder = db.getStepDataDao()
				.queryBuilder();
		Where<Step, String> where = queryBuilder.where();
		String retVal = null;
		try {
			where.like("id", "%" + userId + "%");
			PreparedQuery<Step> preparedQuery = queryBuilder.prepare();
			List<Step> userSteps = db.getStepDataDao().query(preparedQuery);
			if (userSteps != null) {
				List<String> stepIds = getStepIds(userSteps);
				// int c = userSteps.size() + 1;
				int c = getMaxCounter(stepIds);
				retVal = "S_" + userId + "_" + c;
			} else {
				retVal = "S_" + userId + "_1";
			}			
			Log.d("getTrackId", "Returning step id" + retVal);			
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	private static List<String> getStepIds(List<Step> steps) {
		ArrayList<String> ids = new ArrayList<String>();
		for (int i = 0; i < steps.size(); i++) {
			ids.add(steps.get(i).getId());
		}
		return ids;
	}

	public static String getAndroidId(ContentResolver cr) {
		String android_id = Secure.getString(cr, Secure.ANDROID_ID);
		return android_id;
	}

	public static void editRoute(Route editedRoute, DataBaseHelper db) {
		db.getRouteDataDao().update(editedRoute);
	}

	public static InteractiveImage findInteractiveImageById(String idImage,
			DataBaseHelper db) {
		InteractiveImage i = db.getInteractiveImageDataDao()
				.queryForId(idImage);
		return i;
	}
	
	public static HighLight findHighLightById(String idHighLight, DataBaseHelper db) {
		HighLight h = db.getHlDataDao().queryForId(idHighLight);
		return h;
	}

	public static Route findRouteById(String idRoute, DataBaseHelper db) {
		Route r = db.getRouteDataDao().queryForId(idRoute);
		return r;
	}

	public static Step findStepById(String idStep, DataBaseHelper db) {
		Step s = db.getStepDataDao().queryForId(idStep);
		return s;
	}

	public static Reference findReferenceById(String idReference,
			DataBaseHelper db) {
		Reference r = db.getReferenceDataDao().queryForId(idReference);
		return r;
	}

	public static List<Track> getAllTracks(DataBaseHelper db) {
		List<Track> tracks = db.getTrackDataDao().queryForAll();
		return tracks;
	}

	public static List<Route> getUserRoutes(DataBaseHelper db, String userId) {
		List<Route> userRoutes = new ArrayList<Route>();
		QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao()
				.queryBuilder();
		Where<Route, String> where = queryBuilder.where();
		String retVal = null;
		try {
			where.eq("userId", userId);
			PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
			userRoutes = db.getRouteDataDao().query(preparedQuery);
			return userRoutes;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userRoutes;
	}

	public static List<Route> getAllRoutes(DataBaseHelper db) {
		List<Route> routes = db.getRouteDataDao().queryForAll();
		return routes;
	}

//	public static HighLight getHighLightStep(Step s, DataBaseHelper db) {
//		HighLight h = s.getHighlight();
//		if (h == null)
//			return null;
//		else
//			db.getHlDataDao().refresh(h);
//		return h;
//	}

	public static Reference getReferenceStep(Step s, DataBaseHelper db) {
		Reference r = s.getReference();
		db.getReferenceDataDao().refresh(r);
		return r;
	}
	
//	QueryBuilder<Track, String> queryBuilder = db.getTrackDataDao()
//			.queryBuilder();
//	Where<Track, String> where = queryBuilder.where();
//	String retVal = null;
//	try {
//		where.like("id", "%" + userId + "%");
//		PreparedQuery<Track> preparedQuery = queryBuilder.prepare();
//		List<Track> trackRoutes = db.getTrackDataDao().query(preparedQuery);
//		if (trackRoutes != null) {
//			List<String> ids = getTrackIds(trackRoutes);
//			int c = getMaxCounter(ids);
//			// int c = trackRoutes.size() + 1;
//			retVal = "T_" + userId + "_" + c;
//		} else {
//			retVal = "T_" + userId + "_1";
//		}			
//		Log.d("getTrackId", "Returning track id" + retVal);			
//		return retVal;
//	} catch (SQLException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	return retVal;
	
	public static Step getRouteStarterFast(Route route, DataBaseHelper db) {
		if(route.getTrack()!=null){
			try{
				List<Step> steps = db.getStepDataDao().query(
					db.getStepDataDao().queryBuilder().where()
					.eq("trackId", route.getTrack().getId()).and().eq("order", 1).prepare());
				if(steps != null && steps.size()>0){
					return steps.get(0);
				}
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return null;
	}

	public static Step getRouteStarter(Route route, DataBaseHelper db) {
		Track t = route.getTrack();
		db.getTrackDataDao().refresh(t);
		List<Step> steps = getTrackSteps(t, db);
		if (steps != null && steps.size() > 0) {
			return steps.get(0);
		}
		return null;
	}

	public static List<Step> getRouteSteps(Route route, DataBaseHelper db) {
		Track t = route.getTrack();
		db.getTrackDataDao().refresh(t);
		return getTrackSteps(t, db);
	}

	public static List<Box> getInteractiveImageBoxes(InteractiveImage img,
			DataBaseHelper db) {
		db.getInteractiveImageDataDao().refresh(img);
		ArrayList<Box> retVal = new ArrayList<Box>();
		Iterator<Box> boxIt = img.getBoxes().iterator();
		while (boxIt.hasNext()) {
			Box b = boxIt.next();
			retVal.add(b);
		}
		return retVal;
	}
		
	
	public static Step refreshStep(Step s, DataBaseHelper db){
		db.getStepDataDao().refresh(s);
		return s;
	}
	
	public static List<HighLight> getStepHighLights(Step step, DataBaseHelper db) {
		db.getStepDataDao().refresh(step);		
		ArrayList<HighLight> retVal = new ArrayList<HighLight>();
		Iterator<HighLight> hlIt = step.getHighlights().iterator();
		while (hlIt.hasNext()) {
			HighLight h = hlIt.next();			
			retVal.add(h);
		}		
		return retVal;
	}

	public static List<Step> getTrackSteps(Track track, DataBaseHelper db) {
		db.getTrackDataDao().refresh(track);
		ArrayList<Step> retVal = new ArrayList<Step>();
		Iterator<Step> stepIt = track.getSteps().iterator();
		while (stepIt.hasNext()) {
			Step s = stepIt.next();
//			if (s.getHighlight() != null) {
//				db.getHlDataDao().refresh(s.getHighlight());
//			}
			if(s.getHighlights()!= null){
				for(HighLight h : s.getHighlights()){
					db.getHlDataDao().refresh(h);
				}
			}
			retVal.add(s);
		}
		Collections.sort(retVal);
		return retVal;
	}

	// public static byte[] getStepMedia(Step s, DataBaseHelper db){
	// if(s!=null && s.getHighlight()!=null){
	// HighLight hl = s.getHighlight();
	// db.getHlDataDao().refresh(hl);
	// if(hl.getMedia()!=null){
	// EruMedia em = hl.getMedia();
	// db.getMediaDataDao().refresh(em);
	// return em.getImage();
	// }
	// }
	// return null;
	// }
	
	public static void loadBassaOles(DataBaseHelper db, Context context) {
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		
		Track t = new Track("TRACK_BASSAOLES", "Waypoints recorregut Bassa d'Oles");
		try {
			trackDataDao.create(t);
		} catch (RuntimeException ex) {
			Log.e("Inserting track", "Insert error " + ex.toString());
		}
		
		
		Step s1 = new Step("step_bassaoles_1", "step", 42.7149171657416, 0.774089345161048, 0, 10, 1, t);
		Step s2 = new Step("step_bassaoles_2", "step", 42.7149504407864, 0.774179742608889, 0, 10, 2, t, null);
		Step s3 = new Step("step_bassaoles_3", "step", 42.7150084580268, 0.774153244934471, 0, 10, 3, t, null);
		Step s4 = new Step("step_bassaoles_4", "step", 42.7150488336915, 0.77414569546976, 0, 10, 4, t, null);
		Step s5 = new Step("step_bassaoles_5", "step", 42.7150896838814, 0.774162552344225, 0, 10, 5, t, null);
		Step s6 = new Step("step_bassaoles_6", "step", 42.715130534069, 0.774179409239797, 0, 10, 6, t);
		Step s7 = new Step("step_bassaoles_7", "step", 42.7151762395073, 0.774214410071807, 0, 10, 7, t);
		Step s8 = new Step("step_bassaoles_8", "step", 42.7152417217644, 0.774340291518999, 0, 10, 8, t, null);
		Step s9 = new Step("step_bassaoles_9", "step", 42.7152601937158, 0.774364054485635, 0, 10, 9, t, null);
		Step s10 = new Step("step_bassaoles_10", "step", 42.7152882574889, 0.774418003786129, 0, 10, 10, t, null);
		Step s11 = new Step("step_bassaoles_11", "step", 42.7153233075173, 0.774483915077973, 0, 10, 11, t, null);
		Step s12 = new Step("step_bassaoles_12", "step", 42.7154632583167, 0.774387323796554, 0, 10, 12, t, null);
		Step s13 = new Step("step_bassaoles_13", "step", 42.7155297998447, 0.774336097822315, 0, 10, 13, t, null);
		Step s14 = new Step("step_bassaoles_14", "step", 42.7155644899055, 0.77426769291704, 0, 10, 14, t, null);
		Step s15 = new Step("step_bassaoles_15", "step", 42.7156635945871, 0.774270255403993, 0, 10, 15, t, null);
		Step s16 = new Step("step_bassaoles_16", "step", 42.715696039152, 0.774317942487713, 0, 10, 16, t, null);
		Step s17 = new Step("step_bassaoles_17", "step", 42.7157596233959, 0.774346198608682, 0, 10, 17, t, null);
		Step s18 = new Step("step_bassaoles_18", "step", 42.7158715144317, 0.774311668627549, 0, 10, 18, t, null);
		Step s19 = new Step("step_bassaoles_19", "step", 42.7158788523856, 0.774225923573729, 0, 10, 19, t, null);
		Step s20 = new Step("step_bassaoles_20", "step", 42.7159320143604, 0.77418128137689, 0, 10, 20, t, null);
		Step s21 = new Step("step_bassaoles_21", "step", 42.7160298140649, 0.774116725239958, 0, 10, 21, t, null);
		Step s22 = new Step("step_bassaoles_22", "step", 42.7160797816235, 0.77413936225245, 0, 10, 22, t, null);
		Step s23 = new Step("step_bassaoles_23", "step", 42.7162133389408, 0.77406131538973, 0, 10, 23, t, null);
		Step s24 = new Step("step_bassaoles_24", "step", 42.7161982985145, 0.77398247601243, 0, 10, 24, t, null);
		Step s25 = new Step("step_bassaoles_25", "step", 42.7162269465401, 0.773834908995614, 0, 10, 25, t, null);
		Step s26 = new Step("step_bassaoles_26", "step", 42.7162516882982, 0.773718011264, 0, 10, 26, t, null);
		Step s27 = new Step("step_bassaoles_27", "step", 42.716249908404, 0.773626485774426, 0, 10, 27, t);
		Step s28 = new Step("step_bassaoles_28", "step", 42.7162925287309, 0.773502842505807, 0, 10, 28, t);
		Step s29 = new Step("step_bassaoles_29", "step", 42.7163133641518, 0.773416613991842, 0, 10, 29, t, null);
		Step s30 = new Step("step_bassaoles_30", "step", 42.7163238953036, 0.77326358855263, 0, 10, 30, t, null);
		Step s31 = new Step("step_bassaoles_31", "step", 42.7163480430252, 0.77311618190158, 0, 10, 31, t, null);
		Step s32 = new Step("step_bassaoles_32", "step", 42.7163771647028, 0.772993021010115, 0, 10, 32, t, null);
		Step s33 = new Step("step_bassaoles_33", "step", 42.7164112604146, 0.772894105879156, 0, 10, 33, t, null);
		Step s34 = new Step("step_bassaoles_34", "step", 42.7164407379703, 0.77278924991004, 0, 10, 34, t, null);
		Step s35 = new Step("step_bassaoles_35", "step", 42.7164657160669, 0.772684554834878, 0, 10, 35, t, null);
		Step s36 = new Step("step_bassaoles_36", "step", 42.7164909315018, 0.7725920631159, 0, 10, 36, t, null);
		Step s37 = new Step("step_bassaoles_37", "step", 42.7165253830248, 0.772511452760378, 0, 10, 37, t, null);
		Step s38 = new Step("step_bassaoles_38", "step", 42.7165591221245, 0.772394231971268, 0, 10, 38, t, null);
		Step s39 = new Step("step_bassaoles_39", "step", 42.7165840998511, 0.772289536551583, 0, 10, 39, t);
		Step s40 = new Step("step_bassaoles_40", "step", 42.7166084837776, 0.772154332410751, 0, 10, 40, t, null);
		Step s41 = new Step("step_bassaoles_41", "step", 42.7166148107337, 0.772016721461265, 0, 10, 41, t, null);
		Step s42 = new Step("step_bassaoles_42", "step", 42.7166281867697, 0.771952129926618, 0, 10, 42, t, null);
		Step s43 = new Step("step_bassaoles_43", "step", 42.7166012466787, 0.771898140388993, 0, 10, 43, t);
		Step s44 = new Step("step_bassaoles_44", "step", 42.7165879200237, 0.771791762822445, 0, 10, 44, t);
		Step s45 = new Step("step_bassaoles_45", "step", 42.7166364034811, 0.771738126516824, 0, 10, 45, t, null);
		Step s46 = new Step("step_bassaoles_46", "step", 42.7166839961534, 0.771638727126253, 0, 10, 46, t, null);
		Step s47 = new Step("step_bassaoles_47", "step", 42.7167231838891, 0.771570158417617, 0, 10, 47, t, null);
		Step s48 = new Step("step_bassaoles_48", "step", 42.7167669897144, 0.771507530287216, 0, 10, 48, t, null);
		Step s49 = new Step("step_bassaoles_49", "step", 42.7168631248334, 0.771357544591017, 0, 10, 49, t, null);
		Step s50 = new Step("step_bassaoles_50", "step", 42.7169204286028, 0.7712944328926, 0, 10, 50, t, null);
		Step s51 = new Step("step_bassaoles_51", "step", 42.7169782074966, 0.771255728119061, 0, 10, 51, t, null);
		Step s52 = new Step("step_bassaoles_52", "step", 42.7170312494346, 0.771204980839846, 0, 10, 52, t, null);
		Step s53 = new Step("step_bassaoles_53", "step", 42.7170711496665, 0.771173022092318, 0, 10, 53, t);
		Step s54 = new Step("step_bassaoles_54", "step", 42.717072085038, 0.770989807914199, 0, 10, 54, t, null);
		Step s55 = new Step("step_bassaoles_55", "step", 42.7170957545157, 0.770817991638304, 0, 10, 55, t, null);
		Step s56 = new Step("step_bassaoles_56", "step", 42.7171287789071, 0.770664158278927, 0, 10, 56, t, null);
		Step s57 = new Step("step_bassaoles_57", "step", 42.7171513783969, 0.770437425773829, 0, 10, 57, t, null);
		Step s58 = new Step("step_bassaoles_58", "step", 42.7171617866628, 0.770278296156939, 0, 10, 58, t, null);
		Step s59 = new Step("step_bassaoles_59", "step", 42.7171549103967, 0.770156421851355, 0, 10, 59, t, null);
		Step s60 = new Step("step_bassaoles_60", "step", 42.7171576269784, 0.770064734073243, 0, 10, 60, t, null);
		Step s61 = new Step("step_bassaoles_61", "step", 42.717165080555, 0.769985088647642, 0, 10, 61, t, null);
		Step s62 = new Step("step_bassaoles_62", "step", 42.7172284105905, 0.769769108528357, 0, 10, 62, t, null);
		Step s63 = new Step("step_bassaoles_63", "step", 42.7172651007477, 0.76957240065225, 0, 10, 63, t, null);
		Step s64 = new Step("step_bassaoles_64", "step", 42.7172899569507, 0.76946160122528, 0, 10, 64, t, null);
		Step s65 = new Step("step_bassaoles_65", "step", 42.7173217779254, 0.769384745987653, 0, 10, 65, t, null);
		Step s66 = new Step("step_bassaoles_66", "step", 42.7173355447926, 0.769340289108466, 0, 10, 66, t);
		Step s67 = new Step("step_bassaoles_67", "step", 42.7173300814664, 0.769279424413057, 0, 10, 67, t, null);
		Step s68 = new Step("step_bassaoles_68", "step", 42.7173053750884, 0.769189940332657, 0, 10, 68, t, null);
		Step s69 = new Step("step_bassaoles_69", "step", 42.7172469473054, 0.769102886080448, 0, 10, 69, t, null);
		Step s70 = new Step("step_bassaoles_70", "step", 42.7171279428011, 0.769003348620459, 0, 10, 70, t, null);
		Step s71 = new Step("step_bassaoles_71", "step", 42.71701072191, 0.768995337846102, 0, 10, 71, t, null);
		Step s72 = new Step("step_bassaoles_72", "step", 42.7169523492841, 0.769003535963987, 0, 10, 72, t, null);
		Step s73 = new Step("step_bassaoles_73", "step", 42.7169122118709, 0.769023292523154, 0, 10, 73, t, null);
		Step s74 = new Step("step_bassaoles_74", "step", 42.7168404601017, 0.769038076103431, 0, 10, 74, t);
		Step s75 = new Step("step_bassaoles_75", "step", 42.7168223437971, 0.769032619388548, 0, 10, 75, t);
		Step s76 = new Step("step_bassaoles_76", "step", 42.7167934451336, 0.768935959192282, 0, 10, 76, t, null);
		Step s77 = new Step("step_bassaoles_77", "step", 42.7167518807636, 0.768882494995035, 0, 10, 77, t, null);
		Step s78 = new Step("step_bassaoles_78", "step", 42.7166842716161, 0.76887881226059, 0, 10, 78, t, null);
		Step s79 = new Step("step_bassaoles_79", "step", 42.7166008055062, 0.768985605442682, 0, 10, 79, t, null);
		Step s80 = new Step("step_bassaoles_80", "step", 42.7165844728174, 0.769071674450463, 0, 10, 80, t, null);
		Step s81 = new Step("step_bassaoles_81", "step", 42.716509529615, 0.769153737893996, 0, 10, 81, t);
		Step s82 = new Step("step_bassaoles_82", "step", 42.7164557752181, 0.769167876234365, 0, 10, 82, t, null);
		Step s83 = new Step("step_bassaoles_83", "step", 42.7163672149925, 0.769244321408601, 0, 10, 83, t, null);
		Step s84 = new Step("step_bassaoles_84", "step", 42.7163004381066, 0.769283350086419, 0, 10, 84, t, null);
		Step s85 = new Step("step_bassaoles_85", "step", 42.7162742742505, 0.7693270292774, 0, 10, 85, t, null);
		Step s86 = new Step("step_bassaoles_86", "step", 42.7162574657429, 0.769388691022172, 0, 10, 86, t, null);
		Step s87 = new Step("step_bassaoles_87", "step", 42.7161735235886, 0.769471076086403, 0, 10, 87, t, null);
		Step s88 = new Step("step_bassaoles_88", "step", 42.7161023661446, 0.769516367278524, 0, 10, 88, t, null);
		Step s89 = new Step("step_bassaoles_89", "step", 42.7160361835407, 0.769585903796351, 0, 10, 89, t);
		Step s90 = new Step("step_bassaoles_90", "step", 42.7159562650888, 0.769643720474979, 0, 10, 90, t, null);
		Step s91 = new Step("step_bassaoles_91", "step", 42.715903580455, 0.769712773086963, 0, 10, 91, t, null);
		Step s92 = new Step("step_bassaoles_92", "step", 42.7158408272655, 0.769727233187595, 0, 10, 92, t, null);
		Step s93 = new Step("step_bassaoles_93", "step", 42.7157326050774, 0.769718898711305, 0, 10, 93, t, null);
		Step s94 = new Step("step_bassaoles_94", "step", 42.7156284067599, 0.76968599656874, 0, 10, 94, t);
		Step s95 = new Step("step_bassaoles_95", "step", 42.7155697963118, 0.769681990572029, 0, 10, 95, t, null);
		Step s96 = new Step("step_bassaoles_96", "step", 42.7155117802128, 0.769708492632318, 0, 10, 96, t, null);
		Step s97 = new Step("step_bassaoles_97", "step", 42.7153937272217, 0.769657769415839, 0, 10, 97, t, null);
		Step s98 = new Step("step_bassaoles_98", "step", 42.7152940282396, 0.769624706327384, 0, 10, 98, t, null);
		Step s99 = new Step("step_bassaoles_99", "step", 42.7152387283918, 0.769559523359864, 0, 10, 99, t, null);
		Step s100 = new Step("step_bassaoles_100", "step", 42.7151974020815, 0.76951826318346, 0, 10, 100, t, null);
		Step s101 = new Step("step_bassaoles_101", "step", 42.7151747864432, 0.769512967663435, 0, 10, 101, t);
		Step s102 = new Step("step_bassaoles_102", "step", 42.7150385161249, 0.769682707421322, 0, 10, 102, t, null);
		Step s103 = new Step("step_bassaoles_103", "step", 42.7149503120725, 0.769777454718513, 0, 10, 103, t);
		Step s104 = new Step("step_bassaoles_104", "step", 42.7148580840141, 0.769896769117571, 0, 10, 104, t, null);
		Step s105 = new Step("step_bassaoles_105", "step", 42.7147433590529, 0.770016889027111, 0, 10, 105, t, null);
		Step s106 = new Step("step_bassaoles_106", "step", 42.7146506553444, 0.770111796560806, 0, 10, 106, t, null);
		Step s107 = new Step("step_bassaoles_107", "step", 42.7145980892166, 0.770186948816087, 0, 10, 107, t, null);
		Step s108 = new Step("step_bassaoles_108", "step", 42.7145428062068, 0.770353784867504, 0, 10, 108, t, null);
		Step s109 = new Step("step_bassaoles_109", "step", 42.7145142814317, 0.770507450787815, 0, 10, 109, t, null);
		Step s110 = new Step("step_bassaoles_110", "step", 42.7144462127599, 0.77071137877815, 0, 10, 110, t, null);
		Step s111 = new Step("step_bassaoles_111", "step", 42.7144297598801, 0.770791342875013, 0, 10, 111, t, null);
		Step s112 = new Step("step_bassaoles_112", "step", 42.7143750699611, 0.77098868549066, 0, 10, 112, t, null);
		Step s113 = new Step("step_bassaoles_113", "step", 42.7144136783071, 0.771121629868399, 0, 10, 113, t, null);
		Step s114 = new Step("step_bassaoles_114", "step", 42.7144751396432, 0.77127207355318, 0, 10, 114, t, null);
		Step s115 = new Step("step_bassaoles_115", "step", 42.7145215586029, 0.771343680984947, 0, 10, 115, t);
		Step s116 = new Step("step_bassaoles_116", "step", 42.7146262316231, 0.771400991538531, 0, 10, 116, t, null);
		Step s117 = new Step("step_bassaoles_117", "step", 42.7146989340108, 0.77143502360602, 0, 10, 117, t, null);
		Step s118 = new Step("step_bassaoles_118", "step", 42.7147491395981, 0.771469861089874, 0, 10, 118, t, null);
		Step s119 = new Step("step_bassaoles_119", "step", 42.7148491944317, 0.771521231563069, 0, 10, 119, t, null);
		Step s120 = new Step("step_bassaoles_120", "step", 42.71491456081, 0.771641007844769, 0, 10, 120, t, null);
		Step s121 = new Step("step_bassaoles_121", "step", 42.7149718784082, 0.771809919005867, 0, 10, 121, t, null);
		Step s122 = new Step("step_bassaoles_122", "step", 42.7149765096444, 0.772047880119055, 0, 10, 122, t, null);
		Step s123 = new Step("step_bassaoles_123", "step", 42.714933297156, 0.772141013845551, 0, 10, 123, t, null);
		Step s124 = new Step("step_bassaoles_124", "step", 42.7148809671343, 0.772228367912406, 0, 10, 124, t, null);
		Step s125 = new Step("step_bassaoles_125", "step", 42.7148472281252, 0.77234558558503, 0, 10, 125, t, null);
		Step s126 = new Step("step_bassaoles_126", "step", 42.7148268683555, 0.77245621859027, 0, 10, 126, t, null);
		Step s127 = new Step("step_bassaoles_127", "step", 42.7147985781782, 0.772622087477844, 0, 10, 127, t, null);
		Step s128 = new Step("step_bassaoles_128", "step", 42.7147578579872, 0.772843353087412, 0, 10, 128, t, null);
		Step s129 = new Step("step_bassaoles_129", "step", 42.714742471867, 0.772978231023634, 0, 10, 129, t, null);
		Step s130 = new Step("step_bassaoles_130", "step", 42.7147179681736, 0.773107329250955, 0, 10, 130, t, null);
		Step s131 = new Step("step_bassaoles_131", "step", 42.7146625621782, 0.773268061670985, 0, 10, 131, t, null);
		Step s132 = new Step("step_bassaoles_132", "step", 42.7146066812717, 0.77340438766344, 0, 10, 132, t, null);
		Step s133 = new Step("step_bassaoles_133", "step", 42.7145734156832, 0.773546010363791, 0, 10, 133, t, null);
		Step s134 = new Step("step_bassaoles_134", "step", 42.7145672647619, 0.773692769014009, 0, 10, 134, t, null);
		Step s135 = new Step("step_bassaoles_135", "step", 42.7145652570751, 0.773821062142863, 0, 10, 135, t, null);
		Step s136 = new Step("step_bassaoles_136", "step", 42.7145829076528, 0.774034133238308, 0, 10, 136, t, null);
		Step s137 = new Step("step_bassaoles_137", "step", 42.7146171317759, 0.774173342506307, 0, 10, 137, t, null);
		Step s138 = new Step("step_bassaoles_138", "step", 42.7146981203747, 0.774170446806709, 0, 10, 138, t, null);
		Step s139 = new Step("step_bassaoles_139", "step", 42.7147922511801, 0.774148763800047, 0, 10, 139, t, null);
		Step s140 = new Step("step_bassaoles_140", "step", 42.7148415069474, 0.774134791050513, 0, 10, 140, t, null);
		Step s141 = new Step("step_bassaoles_141", "step", 42.714880814908, 0.774072327496432, 0, 10, 141, t, null);
		Step s142 = new Step("step_bassaoles_142", "step", 42.7149337395687, 0.774015482779203, 0, 10, 142, t, null);
		Step s143 = new Step("step_bassaoles_143", "step", 42.7149858336996, 0.773915926928948, 0, 10, 143, t);
		Step s144 = new Step("step_bassaoles_144", "step", 42.7150845824518, 0.773900184112129, 0, 10, 144, t, null);
		Step s145 = new Step("step_bassaoles_145", "step", 42.7151241275698, 0.773849923344069, 0, 10, 145, t, null);
		Step s146 = new Step("step_bassaoles_146", "step", 42.7151761028917, 0.773744265455674, 0, 10, 146, t, null);
		Step s147 = new Step("step_bassaoles_147", "step", 42.7152332894253, 0.773675056076249, 0, 10, 147, t, null);
		Step s148 = new Step("step_bassaoles_148", "step", 42.7152863325307, 0.773624312299503, 0, 10, 148, t);
		Step s149 = new Step("step_bassaoles_149", "step", 42.7153346389206, 0.773561526136282, 0, 10, 149, t, null);
		Step s150 = new Step("step_bassaoles_150", "step", 42.7153726411901, 0.773431943987928, 0, 10, 150, t, null);
		Step s151 = new Step("step_bassaoles_151", "step", 42.7154170414704, 0.773399826606396, 0, 10, 151, t, null);
		Step s152 = new Step("step_bassaoles_152", "step", 42.7154336148333, 0.773325963460736, 0, 10, 152, t, null);
		Step s153 = new Step("step_bassaoles_153", "step", 42.7154769469653, 0.7732389313748, 0, 10, 153, t, null);
		Step s154 = new Step("step_bassaoles_154", "step", 42.7155742713813, 0.77314996775767, 0, 10, 154, t, null);
		Step s155 = new Step("step_bassaoles_155", "step", 42.7155957000076, 0.773094248305261, 0, 10, 155, t, null);
		Step s156 = new Step("step_bassaoles_156", "step", 42.7156256525599, 0.773013800354681, 0, 10, 156, t, null);
		Step s157 = new Step("step_bassaoles_157", "step", 42.7156602231134, 0.772939292999515, 0, 10, 157, t, null);
		Step s158 = new Step("step_bassaoles_158", "step", 42.7156946749147, 0.772858683921336, 0, 10, 158, t, null);
		Step s159 = new Step("step_bassaoles_159", "step", 42.7157245085892, 0.772772134090361, 0, 10, 159, t, null);
		Step s160 = new Step("step_bassaoles_160", "step", 42.7157375318453, 0.772747244571204, 0, 10, 160, t, null);
		Step s161 = new Step("step_bassaoles_161", "step", 42.7157815757851, 0.772696821561942, 0, 10, 161, t, null);
		Step s162 = new Step("step_bassaoles_162", "step", 42.7157754144448, 0.772611559466752, 0, 10, 162, t, null);
		Step s163 = new Step("step_bassaoles_163", "step", 42.7158541472901, 0.772492730156882, 0, 10, 163, t, null);
		Step s164 = new Step("step_bassaoles_164", "step", 42.7159460218984, 0.772355112527268, 0, 10, 164, t, null);
		Step s165 = new Step("step_bassaoles_165", "step", 42.7159895907236, 0.772280282400553, 0, 10, 165, t, null);
		Step s166 = new Step("step_bassaoles_166", "step", 42.7160198988924, 0.772218138576882, 0, 10, 166, t, null);
		Step s167 = new Step("step_bassaoles_167", "step", 42.716074122314, 0.77211239732219, 0, 10, 167, t, null);
		Step s168 = new Step("step_bassaoles_168", "step", 42.7161335574747, 0.77204310493412, 0, 10, 168, t, null);
		Step s169 = new Step("step_bassaoles_169", "step", 42.7161926957139, 0.771958558196698, 0, 10, 169, t, null);
		Step s170 = new Step("step_bassaoles_170", "step", 42.7162301089975, 0.771914477518357, 0, 10, 170, t, null);
		Step s171 = new Step("step_bassaoles_171", "step", 42.7162925062441, 0.771881714416701, 0, 10, 171, t, null);
		Step s172 = new Step("step_bassaoles_172", "step", 42.7163323473098, 0.771846705644592, 0, 10, 172, t, null);
		Step s173 = new Step("step_bassaoles_173", "step", 42.7163582746311, 0.771790824026602, 0, 10, 173, t, null);
		Step s174 = new Step("step_bassaoles_174", "step", 42.7164340511479, 0.771751475850595, 0, 10, 174, t, null);
		Step s175 = new Step("step_bassaoles_175", "step", 42.7165157521577, 0.771785187121623, 0, 10, 175, t, null);
		Step s176 = new Step("step_bassaoles_176", "step", 42.7165478414627, 0.771814568338217, 0, 10, 176, t, null);
		Step s177 = new Step("step_bassaoles_177", "step", 42.7165432233454, 0.771808627671093, 0, 10, 177, t, null);
		Step s178 = new Step("step_bassaoles_178", "step", 42.7165833612856, 0.771788873015393, 0, 10, 178, t, null);
		Step s179 = new Step("step_bassaoles_179", "step", 42.7165477227033, 0.771808466617539, 0, 10, 179, t, null);

		try {
			stepDataDao.create(s1);
			stepDataDao.create(s2);
			stepDataDao.create(s3);
			stepDataDao.create(s4);
			stepDataDao.create(s5);
			stepDataDao.create(s6);
			stepDataDao.create(s7);
			stepDataDao.create(s8);
			stepDataDao.create(s9);
			stepDataDao.create(s10);
			stepDataDao.create(s11);
			stepDataDao.create(s12);
			stepDataDao.create(s13);
			stepDataDao.create(s14);
			stepDataDao.create(s15);
			stepDataDao.create(s16);
			stepDataDao.create(s17);
			stepDataDao.create(s18);
			stepDataDao.create(s19);
			stepDataDao.create(s20);
			stepDataDao.create(s21);
			stepDataDao.create(s22);
			stepDataDao.create(s23);
			stepDataDao.create(s24);
			stepDataDao.create(s25);
			stepDataDao.create(s26);
			stepDataDao.create(s27);
			stepDataDao.create(s28);
			stepDataDao.create(s29);
			stepDataDao.create(s30);
			stepDataDao.create(s31);
			stepDataDao.create(s32);
			stepDataDao.create(s33);
			stepDataDao.create(s34);
			stepDataDao.create(s35);
			stepDataDao.create(s36);
			stepDataDao.create(s37);
			stepDataDao.create(s38);
			stepDataDao.create(s39);
			stepDataDao.create(s40);
			stepDataDao.create(s41);
			stepDataDao.create(s42);
			stepDataDao.create(s43);
			stepDataDao.create(s44);
			stepDataDao.create(s45);
			stepDataDao.create(s46);
			stepDataDao.create(s47);
			stepDataDao.create(s48);
			stepDataDao.create(s49);
			stepDataDao.create(s50);
			stepDataDao.create(s51);
			stepDataDao.create(s52);
			stepDataDao.create(s53);
			stepDataDao.create(s54);
			stepDataDao.create(s55);
			stepDataDao.create(s56);
			stepDataDao.create(s57);
			stepDataDao.create(s58);
			stepDataDao.create(s59);
			stepDataDao.create(s60);
			stepDataDao.create(s61);
			stepDataDao.create(s62);
			stepDataDao.create(s63);
			stepDataDao.create(s64);
			stepDataDao.create(s65);
			stepDataDao.create(s66);
			stepDataDao.create(s67);
			stepDataDao.create(s68);
			stepDataDao.create(s69);
			stepDataDao.create(s70);
			stepDataDao.create(s71);
			stepDataDao.create(s72);
			stepDataDao.create(s73);
			stepDataDao.create(s74);
			stepDataDao.create(s75);
			stepDataDao.create(s76);
			stepDataDao.create(s77);
			stepDataDao.create(s78);
			stepDataDao.create(s79);
			stepDataDao.create(s80);
			stepDataDao.create(s81);
			stepDataDao.create(s82);
			stepDataDao.create(s83);
			stepDataDao.create(s84);
			stepDataDao.create(s85);
			stepDataDao.create(s86);
			stepDataDao.create(s87);
			stepDataDao.create(s88);
			stepDataDao.create(s89);
			stepDataDao.create(s90);
			stepDataDao.create(s91);
			stepDataDao.create(s92);
			stepDataDao.create(s93);
			stepDataDao.create(s94);
			stepDataDao.create(s95);
			stepDataDao.create(s96);
			stepDataDao.create(s97);
			stepDataDao.create(s98);
			stepDataDao.create(s99);
			stepDataDao.create(s100);
			stepDataDao.create(s101);
			stepDataDao.create(s102);
			stepDataDao.create(s103);
			stepDataDao.create(s104);
			stepDataDao.create(s105);
			stepDataDao.create(s106);
			stepDataDao.create(s107);
			stepDataDao.create(s108);
			stepDataDao.create(s109);
			stepDataDao.create(s110);
			stepDataDao.create(s111);
			stepDataDao.create(s112);
			stepDataDao.create(s113);
			stepDataDao.create(s114);
			stepDataDao.create(s115);
			stepDataDao.create(s116);
			stepDataDao.create(s117);
			stepDataDao.create(s118);
			stepDataDao.create(s119);
			stepDataDao.create(s120);
			stepDataDao.create(s121);
			stepDataDao.create(s122);
			stepDataDao.create(s123);
			stepDataDao.create(s124);
			stepDataDao.create(s125);
			stepDataDao.create(s126);
			stepDataDao.create(s127);
			stepDataDao.create(s128);
			stepDataDao.create(s129);
			stepDataDao.create(s130);
			stepDataDao.create(s131);
			stepDataDao.create(s132);
			stepDataDao.create(s133);
			stepDataDao.create(s134);
			stepDataDao.create(s135);
			stepDataDao.create(s136);
			stepDataDao.create(s137);
			stepDataDao.create(s138);
			stepDataDao.create(s139);
			stepDataDao.create(s140);
			stepDataDao.create(s141);
			stepDataDao.create(s142);
			stepDataDao.create(s143);
			stepDataDao.create(s144);
			stepDataDao.create(s145);
			stepDataDao.create(s146);
			stepDataDao.create(s147);
			stepDataDao.create(s148);
			stepDataDao.create(s149);
			stepDataDao.create(s150);
			stepDataDao.create(s151);
			stepDataDao.create(s152);
			stepDataDao.create(s153);
			stepDataDao.create(s154);
			stepDataDao.create(s155);
			stepDataDao.create(s156);
			stepDataDao.create(s157);
			stepDataDao.create(s158);
			stepDataDao.create(s159);
			stepDataDao.create(s160);
			stepDataDao.create(s161);
			stepDataDao.create(s162);
			stepDataDao.create(s163);
			stepDataDao.create(s164);
			stepDataDao.create(s165);
			stepDataDao.create(s166);
			stepDataDao.create(s167);
			stepDataDao.create(s168);
			stepDataDao.create(s169);
			stepDataDao.create(s170);
			stepDataDao.create(s171);
			stepDataDao.create(s172);
			stepDataDao.create(s173);
			stepDataDao.create(s174);
			stepDataDao.create(s175);
			stepDataDao.create(s176);
			stepDataDao.create(s177);
			stepDataDao.create(s178);
			stepDataDao.create(s179);
		} catch (RuntimeException ex) {
			Log.e("Inserting step", "Insert error " + ex.toString());
		}
		
		//WP
		HighLight h1 = new HighLight("hl_bassaoles_wpo01","wpO01-inici",null,10,HighLight.WAYPOINT,s1);
		HighLight h2 = new HighLight("hl_bassaoles_wpo02","wpO02-represa",null,10,HighLight.WAYPOINT,s7);
		HighLight h3 = new HighLight("hl_bassaoles_wpo03","wpO03-vista canal",null,10,HighLight.WAYPOINT,s27);
		HighLight h4 = new HighLight("hl_bassaoles_wpo04","wpO04-arbre tombat",null,10,HighLight.WAYPOINT,s39);
		HighLight h5 = new HighLight("hl_bassaoles_wpo05","wpO05-extrem N bassa",null,10,HighLight.WAYPOINT,s44);
		HighLight h6 = new HighLight("hl_bassaoles_wpo06","wpO06-cruïlla pista",null,10,HighLight.WAYPOINT,s66);
		HighLight h7 = new HighLight("hl_bassaoles_wpo07","wpO07-desviament pista",null,10,HighLight.WAYPOINT);
		HighLight h8 = new HighLight("hl_bassaoles_wpo08","wpO08-basseta",null,10,HighLight.WAYPOINT,s75);
		HighLight h9 = new HighLight("hl_bassaoles_wpo09","wpO09-sortida sender",null,10,HighLight.WAYPOINT,s81);
		HighLight h10 = new HighLight("hl_bassaoles_wpo10","wpO10-inici sender",null,10,HighLight.WAYPOINT,s89);
		HighLight h11 = new HighLight("hl_bassaoles_wpo11","wpO11-prat",null,10,HighLight.WAYPOINT,s94);
		HighLight h12 = new HighLight("hl_bassaoles_wpo12","wpO12-pista",null,10,HighLight.WAYPOINT,s101);
		HighLight h13 = new HighLight("hl_bassaoles_wpo13","wpO13-desviament borda",null,10,HighLight.WAYPOINT,s115);
		HighLight h14 = new HighLight("hl_bassaoles_wpo14","wpO14",null,10,HighLight.WAYPOINT,s143);

		//POI
		HighLight h15 = new HighLight("hl_bassaoles_poio01","poiO01-represa",null,10,HighLight.POINT_OF_INTEREST_OFFICIAL,s6);
		HighLight h16 = new HighLight("hl_bassaoles_poio02","poiO02-vista canal",null,10,HighLight.POINT_OF_INTEREST_OFFICIAL,s28);
		HighLight h17 = new HighLight("hl_bassaoles_poio03","poiO03-vista bassa N",null,10,HighLight.POINT_OF_INTEREST_OFFICIAL,s43);
		HighLight h18 = new HighLight("hl_bassaoles_poio04","poiO04-basseta",null,10,HighLight.POINT_OF_INTEREST_OFFICIAL,s74);
		HighLight h19 = new HighLight("hl_bassaoles_poio05","poiO05-aiguamoll",null,10,HighLight.POINT_OF_INTEREST_OFFICIAL,s103);
		HighLight h20 = new HighLight("hl_bassaoles_poio06","poiO06-illots càrex",null,10,HighLight.POINT_OF_INTEREST_OFFICIAL,s148);
				
		
		try {
			hlDataDao.create(h1);
			hlDataDao.create(h2);
			hlDataDao.create(h3);
			hlDataDao.create(h4);
			hlDataDao.create(h5);
			hlDataDao.create(h6);
			hlDataDao.create(h7);
			hlDataDao.create(h8);
			hlDataDao.create(h9);
			hlDataDao.create(h10);
			hlDataDao.create(h11);
			hlDataDao.create(h12);
			hlDataDao.create(h13);
			hlDataDao.create(h14);
			hlDataDao.create(h15);
			hlDataDao.create(h16);
			hlDataDao.create(h17);
			hlDataDao.create(h18);
			hlDataDao.create(h19);
			hlDataDao.create(h20);			
		} catch (RuntimeException ex) {
			Log.e("Inserting highlight", "Insert error " + ex.toString());
		}

		
		Route r = new Route();
		r.setId("ROUTE_BASSAOLES");
		r.setName("Bassa d'Oles");
		r.setDescription("Itinerari circular de 1.7 km amb 50 m de desnivell (30 minuts a peu) al voltant del complex de la  Bassa d’Oles, una altra petita bassa i un aiguamoll propers. El camí segueix una pista i senders força planers, i travessa algun prat. Sense cap dificultat, l’itinerari és apte per a tots els públics. La Bassa d’Oles és una bassa d’origen natural que posteriorment es va represar. Es practica la pesca intensiva, i la bassa és repoblada regularment amb truites. És per tant un exemple d’ecosistema aquàtic fortament humanitzat. Està envoltada d’una pineda de pi roig de gran valor paisatgístic. L’interés principal rau en l’important entapissat de vegetació aqüàtica submergida i les illes de joncs que s’hi troben. Degut a l’accessibilitat a les vores i la poca fondària, és fàcil observar la vegetació submergida.");
		r.setUserId("1");
		// Ph_ch parameters
		// r.setReference(r6);
		// Interactive image
		// r.setInteractiveImage(img);
		r.setTrack(t);
		r.setLocalCarto("bassa_oles.mbtiles");

		try {
			routeDataDao.create(r);
		} catch (RuntimeException ex) {
			Log.e("Inserting route", "Insert error " + ex.toString());
		}
		
	}

	public static void loadVarrados(DataBaseHelper db, Context context) {
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();

		Track t = new Track("TRACK_VARRADOS", "Waypoints recorregut Varradòs");
		try {
			trackDataDao.create(t);
		} catch (RuntimeException ex) {
			Log.e("Inserting track", "Insert error " + ex.toString());
		}


		Step s1 = new Step("step_varrados_1", "step", 42.7378633696433,
				0.756368635182401, 0, 10, 1, t);
		Step s2 = new Step("step_varrados_2", "step", 42.7379694385642,
				0.756267080117364, 0, 10, 2, t, null);
		Step s3 = new Step("step_varrados_3", "step", 42.7381332329505,
				0.755894686489318, 0, 10, 3, t, null);
		Step s4 = new Step("step_varrados_4", "step", 42.738266536489,
				0.755804363736847, 0, 10, 4, t);
		Step s5 = new Step("step_varrados_5", "step", 42.7384793911674,
				0.75563787281689, 0, 10, 5, t, null);
		Step s6 = new Step("step_varrados_6", "step", 42.7387275218083,
				0.75543345889914, 0, 10, 6, t, null);
		Step s7 = new Step("step_varrados_7", "step", 42.7389301803697,
				0.75520625172494, 0, 10, 7, t, null);
		Step s8 = new Step("step_varrados_8", "step", 42.7390577960882,
				0.754986638180492, 0, 10, 8, t, null);
		Step s9 = new Step("step_varrados_9", "step", 42.739123404994,
				0.75477353774358, 0, 10, 9, t, null);
		Step s10 = new Step("step_varrados_10", "step", 42.7391872427623,
				0.754584933557306, 0, 10, 10, t, null);
		Step s11 = new Step("step_varrados_11", "step", 42.7393061238921,
				0.754333259697102, 0, 10, 11, t, null);
		Step s12 = new Step("step_varrados_12", "step", 42.739429888622,
				0.754215789236744, 0, 10, 12, t, null);
		Step s13 = new Step("step_varrados_13", "step", 42.7395440216613,
				0.75418112712893, 0, 10, 13, t, null);
		Step s14 = new Step("step_varrados_14", "step", 42.7396849364974,
				0.754249337910437, 0, 10, 14, t, null);
		Step s15 = new Step("step_varrados_15", "step", 42.7397723388377,
				0.754343914076339, 0, 10, 15, t, null);
		Step s16 = new Step("step_varrados_16", "step", 42.7398897647497,
				0.754477110227804, 0, 10, 16, t, null);
		Step s17 = new Step("step_varrados_17", "step", 42.740048641912,
				0.754657676638004, 0, 10, 17, t, null);
		Step s18 = new Step("step_varrados_18", "step", 42.740191292433,
				0.754814396543274, 0, 10, 18, t, null);
		Step s19 = new Step("step_varrados_19", "step", 42.7403250638875,
				0.754977545954112, 0, 10, 19, t, null);
		Step s20 = new Step("step_varrados_20", "step", 42.7404709212418,
				0.755183018056262, 0, 10, 20, t, null);
		Step s21 = new Step("step_varrados_21", "step", 42.7405656702552,
				0.755307873171522, 0, 10, 21, t, null);
		Step s22 = new Step("step_varrados_22", "step", 42.7406837289709,
				0.755358587391483, 0, 10, 22, t, null);
		Step s23 = new Step("step_varrados_23", "step", 42.7408236258214,
				0.75537491773891, 0, 10, 23, t, null);
		Step s24 = new Step("step_varrados_24", "step", 42.7410625423447,
				0.755274669604904, 0, 10, 24, t, null);
		Step s25 = new Step("step_varrados_25", "step", 42.7411279615596,
				0.755281471079679, 0, 10, 25, t, null);
		Step s26 = new Step("step_varrados_26", "step", 42.7412210592842,
				0.755436929171511, 0, 10, 26, t, null);
		Step s27 = new Step("step_varrados_27", "step", 42.7412863831568,
				0.755553685416148, 0, 10, 27, t, null);
		Step s28 = new Step("step_varrados_28", "step", 42.7413736049802,
				0.755639109671303, 0, 10, 28, t, null);
		Step s29 = new Step("step_varrados_29", "step", 42.7414603480529,
				0.755700117780109, 0, 10, 29, t, null);
		Step s30 = new Step("step_varrados_30", "step", 42.741561821396,
				0.755708672913028, 0, 10, 30, t, null);
		Step s31 = new Step("step_varrados_31", "step", 42.7416787430638,
				0.755701399518536, 0, 10, 31, t, null);
		Step s32 = new Step("step_varrados_32", "step", 42.7419855680362,
				0.75562008009342, 0, 10, 32, t, null);
		Step s33 = new Step("step_varrados_33", "step", 42.7420815846475,
				0.755579964247571, 0, 10, 33, t, null);
		Step s34 = new Step("step_varrados_34", "step", 42.7422038788581,
				0.75550224901752, 0, 10, 34, t, null);
		Step s35 = new Step("step_varrados_35", "step", 42.7422642601805,
				0.755481744401051, 0, 10, 35, t, null);
		Step s36 = new Step("step_varrados_36", "step", 42.742362945284,
				0.755462911595888, 0, 10, 36, t, null);
		Step s37 = new Step("step_varrados_37", "step", 42.7424890450155,
				0.755464469111663, 0, 10, 37, t, null);
		Step s38 = new Step("step_varrados_38", "step", 42.7426111840525,
				0.755493657939936, 0, 10, 38, t, null);
		Step s39 = new Step("step_varrados_39", "step", 42.7427383609523,
				0.755550153486482, 0, 10, 39, t, null);
		Step s40 = new Step("step_varrados_40", "step", 42.7429564211789,
				0.755649183596262, 0, 10, 40, t, null);
		Step s41 = new Step("step_varrados_41", "step", 42.7431292491026,
				0.755737629499467, 0, 10, 41, t, null);
		Step s42 = new Step("step_varrados_42", "step", 42.7434116155677,
				0.755901534968657, 0, 10, 42, t, null);
		Step s43 = new Step("step_varrados_43", "step", 42.7435390316137,
				0.755970240617789, 0, 10, 43, t, null);
		Step s44 = new Step("step_varrados_44", "step", 42.7436458418437,
				0.756021364163949, 0, 10, 44, t, null);
		Step s45 = new Step("step_varrados_45", "step", 42.7437615309703,
				0.75606605886636, 0, 10, 45, t, null);
		Step s46 = new Step("step_varrados_46", "step", 42.7438820185107,
				0.75612585222186, 0, 10, 46, t, null);
		Step s47 = new Step("step_varrados_47", "step", 42.7439435964877,
				0.756166390995714, 0, 10, 47, t, null);
		Step s48 = new Step("step_varrados_48", "step", 42.7439720878703,
				0.756241721264142, 0, 10, 48, t, null);
		Step s49 = new Step("step_varrados_49", "step", 42.7439557295304,
				0.75644084291647, 0, 10, 49, t, null);
		Step s50 = new Step("step_varrados_50", "step", 42.743939453899,
				0.75675908032097, 0, 10, 50, t, null);
		Step s51 = new Step("step_varrados_51", "step", 42.7439753149419,
				0.756980752920395, 0, 10, 51, t, null);
		Step s52 = new Step("step_varrados_52", "step", 42.7440554735923,
				0.757280240916633, 0, 10, 52, t, null);
		Step s53 = new Step("step_varrados_53", "step", 42.7441347346219,
				0.757533946969081, 0, 10, 53, t, null);
		Step s54 = new Step("step_varrados_54", "step", 42.7442814468155,
				0.757898232502361, 0, 10, 54, t, null);
		Step s55 = new Step("step_varrados_55", "step", 42.7444711021119,
				0.758386200317835, 0, 10, 55, t, null);
		Step s56 = new Step("step_varrados_56", "step", 42.7446727804482,
				0.758913444563633, 0, 10, 56, t, null);
		Step s57 = new Step("step_varrados_57", "step", 42.7447430782772,
				0.759054466531015, 0, 10, 57, t, null);
		Step s58 = new Step("step_varrados_58", "step", 42.7449231514533,
				0.759283164566515, 0, 10, 58, t, null);
		Step s59 = new Step("step_varrados_59", "step", 42.7450726644815,
				0.759445768470534, 0, 10, 59, t, null);
		Step s60 = new Step("step_varrados_60", "step", 42.7452566985989,
				0.759646836570152, 0, 10, 60, t, null);
		Step s61 = new Step("step_varrados_61", "step", 42.7453681261012,
				0.75970391019452, 0, 10, 61, t, null);
		Step s62 = new Step("step_varrados_62", "step", 42.7455816038119,
				0.759684001783452, 0, 10, 62, t, null);
		Step s63 = new Step("step_varrados_63", "step", 42.7459327879692,
				0.759683567446753, 0, 10, 63, t, null);
		Step s64 = new Step("step_varrados_64", "step", 42.7460227739183,
				0.75968032552045, 0, 10, 64, t, null);
		Step s65 = new Step("step_varrados_65", "step", 42.7461186725003,
				0.759634108447249, 0, 10, 65, t, null);
		Step s66 = new Step("step_varrados_66", "step", 42.7463572940286,
				0.759518605644059, 0, 10, 66, t, null);
		Step s67 = new Step("step_varrados_67", "step", 42.74657797797,
				0.759406802706668, 0, 10, 67, t, null);
		Step s68 = new Step("step_varrados_68", "step", 42.7467401916037,
				0.759413175635029, 0, 10, 68, t, null);
		Step s69 = new Step("step_varrados_69", "step", 42.7468969706313,
				0.759486942852562, 0, 10, 69, t);
		Step s70 = new Step("step_varrados_70", "step", 42.747036469306,
				0.759597986826308, 0, 10, 70, t, null);
		Step s71 = new Step("step_varrados_71", "step", 42.7471890081756,
				0.759800196157452, 0, 10, 71, t);
		Step s72 = new Step("step_varrados_72", "step", 42.747327212606,
				0.759960159897268, 0, 10, 72, t, null);
		Step s73 = new Step("step_varrados_73", "step", 42.7476661929537,
				0.760256454091534, 0, 10, 73, t);
		Step s74 = new Step("step_varrados_74", "step", 42.7479424212676,
				0.760567229599749, 0, 10, 74, t, null);
		Step s75 = new Step("step_varrados_75", "step", 42.7481755472055,
				0.760861232737148, 0, 10, 75, t, null);
		Step s76 = new Step("step_varrados_76", "step", 42.7483711855274,
				0.761080224144762, 0, 10, 76, t, null);
		Step s77 = new Step("step_varrados_77", "step", 42.7485230055718,
				0.761245812555204, 0, 10, 77, t, null);
		Step s78 = new Step("step_varrados_78", "step", 42.7487707433078,
				0.76148125808793, 0, 10, 78, t, null);
		Step s79 = new Step("step_varrados_79", "step", 42.749038429023,
				0.761700714465977, 0, 10, 79, t, null);
		Step s80 = new Step("step_varrados_80", "step", 42.7492717727853,
				0.761890862911023, 0, 10, 80, t, null);
		Step s81 = new Step("step_varrados_81", "step", 42.74953768552,
				0.762134823395086, 0, 10, 81, t, null);
		Step s82 = new Step("step_varrados_82", "step", 42.749681382754,
				0.762230453630847, 0, 10, 82, t, null);
		Step s83 = new Step("step_varrados_83", "step", 42.7497627282271,
				0.762245853644363, 0, 10, 83, t, null);
		Step s84 = new Step("step_varrados_84", "step", 42.7502153078581,
				0.762134871069054, 0, 10, 84, t, null);
		Step s85 = new Step("step_varrados_85", "step", 42.7505426826927,
				0.762068104013806, 0, 10, 85, t, null);
		Step s86 = new Step("step_varrados_86", "step", 42.7507069487103,
				0.761949168806979, 0, 10, 86, t, null);
		Step s87 = new Step("step_varrados_87", "step", 42.7509331120053,
				0.761656942989657, 0, 10, 87, t, null);
		Step s88 = new Step("step_varrados_88", "step", 42.7511008620675,
				0.76148595087081, 0, 10, 88, t, null);
		Step s89 = new Step("step_varrados_89", "step", 42.7511500558878,
				0.761468906248276, 0, 10, 89, t, null);
		Step s90 = new Step("step_varrados_90", "step", 42.7512591135396,
				0.761519964057411, 0, 10, 90, t, null);
		Step s91 = new Step("step_varrados_91", "step", 42.7513829822585,
				0.761637692102152, 0, 10, 91, t, null);
		Step s92 = new Step("step_varrados_92", "step", 42.7514506096208,
				0.761757445328808, 0, 10, 92, t, null);
		Step s93 = new Step("step_varrados_93", "step", 42.7514653792315,
				0.761937140924976, 0, 10, 93, t, null);
		Step s94 = new Step("step_varrados_94", "step", 42.7514643413329,
				0.762114350895788, 0, 10, 94, t, null);
		Step s95 = new Step("step_varrados_95", "step", 42.751479510159,
				0.762199336522364, 0, 10, 95, t, null);
		Step s96 = new Step("step_varrados_96", "step", 42.751500532778,
				0.762238290970709, 0, 10, 96, t, null);
		Step s97 = new Step("step_varrados_97", "step", 42.7517885237434,
				0.762460083703489, 0, 10, 97, t, null);
		Step s98 = new Step("step_varrados_98", "step", 42.7521843606319,
				0.762555808010364, 0, 10, 98, t, null);
		Step s99 = new Step("step_varrados_99", "step", 42.7526276787626,
				0.762662043724061, 0, 10, 99, t, null);
		Step s100 = new Step("step_varrados_100", "step", 42.7528913230436,
				0.762790020442354, 0, 10, 100, t, null);
		Step s101 = new Step("step_varrados_101", "step", 42.7529558047571,
				0.762864069358013, 0, 10, 101, t, null);
		Step s102 = new Step("step_varrados_102", "step", 42.7530386588023,
				0.763071867544895, 0, 10, 102, t, null);
		Step s103 = new Step("step_varrados_103", "step", 42.753135427923,
				0.763300549123155, 0, 10, 103, t, null);
		Step s104 = new Step("step_varrados_104", "step", 42.7533702502195,
				0.763912223893644, 0, 10, 104, t, null);
		Step s105 = new Step("step_varrados_105", "step", 42.7535246463691,
				0.764325178362552, 0, 10, 105, t, null);
		Step s106 = new Step("step_varrados_106", "step", 42.7535756888368,
				0.764402767694741, 0, 10, 106, t, null);
		Step s107 = new Step("step_varrados_107", "step", 42.753691033083,
				0.764545250715059, 0, 10, 107, t, null);
		Step s108 = new Step("step_varrados_108", "step", 42.7537557521663,
				0.764631513096752, 0, 10, 108, t, null);
		Step s109 = new Step("step_varrados_109", "step", 42.7538785757878,
				0.764926468990585, 0, 10, 109, t, null);
		Step s110 = new Step("step_varrados_110", "step", 42.7539184443863,
				0.765123598999281, 0, 10, 110, t, null);
		Step s111 = new Step("step_varrados_111", "step", 42.7539755345091,
				0.76574167595968, 0, 10, 111, t, null);
		Step s112 = new Step("step_varrados_112", "step", 42.7540442897697,
				0.765919439754413, 0, 10, 112, t, null);
		Step s113 = new Step("step_varrados_113", "step", 42.7542209371614,
				0.766319384829133, 0, 10, 113, t, null);
		Step s114 = new Step("step_varrados_114", "step", 42.7542981082615,
				0.766582383125749, 0, 10, 114, t, null);
		Step s115 = new Step("step_varrados_115", "step", 42.7544202852444,
				0.766959849520706, 0, 10, 115, t, null);
		Step s116 = new Step("step_varrados_116", "step", 42.754481111863,
				0.767192887996512, 0, 10, 116, t, null);
		Step s117 = new Step("step_varrados_117", "step", 42.7545744325407,
				0.767360608028847, 0, 10, 117, t, null);
		Step s118 = new Step("step_varrados_118", "step", 42.7546382097549,
				0.76751411522955, 0, 10, 118, t, null);
		Step s119 = new Step("step_varrados_119", "step", 42.7546581716781,
				0.767614208679179, 0, 10, 119, t, null);
		Step s120 = new Step("step_varrados_120", "step", 42.7546895379153,
				0.768068256413764, 0, 10, 120, t, null);
		Step s121 = new Step("step_varrados_121", "step", 42.7547481601841,
				0.768188346202553, 0, 10, 121, t, null);
		Step s122 = new Step("step_varrados_122", "step", 42.754847097637,
				0.768297824325534, 0, 10, 122, t, null);
		Step s123 = new Step("step_varrados_123", "step", 42.7549181463831,
				0.768362480869417, 0, 10, 123, t, null);
		Step s124 = new Step("step_varrados_124", "step", 42.7550295699245,
				0.768419578444533, 0, 10, 124, t, null);
		Step s125 = new Step("step_varrados_125", "step", 42.7551932711892,
				0.768502293561864, 0, 10, 125, t, null);
		Step s126 = new Step("step_varrados_126", "step", 42.7553150503093,
				0.768513196537709, 0, 10, 126, t, null);
		Step s127 = new Step("step_varrados_127", "step", 42.7554209627955,
				0.768518559312707, 0, 10, 127, t, null);
		Step s128 = new Step("step_varrados_128", "step", 42.7555430989728,
				0.768547778935622, 0, 10, 128, t, null);
		Step s129 = new Step("step_varrados_129", "step", 42.7556046728885,
				0.768588337320896, 0, 10, 129, t, null);
		Step s130 = new Step("step_varrados_130", "step", 42.7559637829239,
				0.76911005812229, 0, 10, 130, t, null);
		Step s131 = new Step("step_varrados_131", "step", 42.7560535424418,
				0.769210704360424, 0, 10, 131, t, null);
		Step s132 = new Step("step_varrados_132", "step", 42.7564021534368,
				0.769540347636323, 0, 10, 132, t, null);
		Step s133 = new Step("step_varrados_133", "step", 42.7566178551696,
				0.769865597988014, 0, 10, 133, t, null);
		Step s134 = new Step("step_varrados_134", "step", 42.7568472323883,
				0.770199524911563, 0, 10, 134, t, null);
		Step s135 = new Step("step_varrados_135", "step", 42.7569401326287,
				0.770345886014687, 0, 10, 135, t, null);
		Step s136 = new Step("step_varrados_136", "step", 42.7570445976575,
				0.770739285821348, 0, 10, 136, t, null);
		Step s137 = new Step("step_varrados_137", "step", 42.7571397060557,
				0.771114692565971, 0, 10, 137, t, null);
		Step s138 = new Step("step_varrados_138", "step", 42.7571915168515,
				0.771231979134727, 0, 10, 138, t, null);
		Step s139 = new Step("step_varrados_139", "step", 42.7574324357169,
				0.771580775333147, 0, 10, 139, t, null);
		Step s140 = new Step("step_varrados_140", "step", 42.7577189410492,
				0.771958490269943, 0, 10, 140, t, null);
		Step s141 = new Step("step_varrados_141", "step", 42.7579290592865,
				0.772112874764003, 0, 10, 141, t, null);
		Step s142 = new Step("step_varrados_142", "step", 42.7582271677281,
				0.772508509486808, 0, 10, 142, t, null);
		Step s143 = new Step("step_varrados_143", "step", 42.7583792692447,
				0.772689416328076, 0, 10, 143, t, null);
		Step s144 = new Step("step_varrados_144", "step", 42.7587124570898,
				0.772689697257636, 0, 10, 144, t, null);
		Step s145 = new Step("step_varrados_145", "step", 42.7595605802226,
				0.772659305239216, 0, 10, 145, t, null);
		Step s146 = new Step("step_varrados_146", "step", 42.7596410928572,
				0.772631979211767, 0, 10, 146, t, null);
		Step s147 = new Step("step_varrados_147", "step", 42.7597778392118,
				0.772486543683098, 0, 10, 147, t, null);
		Step s148 = new Step("step_varrados_148", "step", 42.7598213471987,
				0.772408606604344, 0, 10, 148, t, null);
		Step s149 = new Step("step_varrados_149", "step", 42.7598019834022,
				0.772339033016901, 0, 10, 149, t, null);
		Step s150 = new Step("step_varrados_150", "step", 42.7597439593895,
				0.772249459358015, 0, 10, 150, t, null);
		Step s151 = new Step("step_varrados_151", "step", 42.7596656289292,
				0.772157558611538, 0, 10, 151, t, null);
		Step s152 = new Step("step_varrados_152", "step", 42.7596456113578,
				0.772054402562493, 0, 10, 152, t, null);
		Step s153 = new Step("step_varrados_153", "step", 42.7596797638585,
				0.771958470076934, 0, 10, 153, t, null);
		Step s154 = new Step("step_varrados_154", "step", 42.7597625853888,
				0.771934115425259, 0, 10, 154, t, null);
		Step s155 = new Step("step_varrados_155", "step", 42.7600838878753,
				0.772133401236853, 0, 10, 155, t, null);
		Step s156 = new Step("step_varrados_156", "step", 42.7602687249133,
				0.772261201782552, 0, 10, 156, t, null);
		Step s157 = new Step("step_varrados_157", "step", 42.7603561727838,
				0.772358887021055, 0, 10, 157, t, null);
		Step s158 = new Step("step_varrados_158", "step", 42.7604719829671,
				0.772525824371773, 0, 10, 158, t, null);
		Step s159 = new Step("step_varrados_159", "step", 42.7605595571756,
				0.77274571174046, 0, 10, 159, t, null);
		Step s160 = new Step("step_varrados_160", "step", 42.7606217831478,
				0.772819860675776, 0, 10, 160, t, null);
		Step s161 = new Step("step_varrados_161", "step", 42.7606716915476,
				0.772839458341983, 0, 10, 161, t);
		Step s162 = new Step("step_varrados_162", "step", 42.7607446829809,
				0.772772684453635, 0, 10, 162, t, null);
		Step s163 = new Step("step_varrados_163", "step", 42.760756933473,
				0.772708087116996, 0, 10, 163, t, null);
		Step s164 = new Step("step_varrados_164", "step", 42.7607376292422,
				0.772641565422394, 0, 10, 164, t, null);
		Step s165 = new Step("step_varrados_165", "step", 42.7603905382039,
				0.771926880447086, 0, 10, 165, t, null);
		Step s166 = new Step("step_varrados_166", "step", 42.7603047453567,
				0.771798585068385, 0, 10, 166, t, null);
		Step s167 = new Step("step_varrados_167", "step", 42.7602498620383,
				0.771754725623485, 0, 10, 167, t, null);
		Step s168 = new Step("step_varrados_168", "step", 42.7601538310517,
				0.771678735027784, 0, 10, 168, t, null);
		Step s169 = new Step("step_varrados_169", "step", 42.7600739636824,
				0.77162355102629, 0, 10, 169, t, null);
		Step s170 = new Step("step_varrados_170", "step", 42.760052409182,
				0.771557111075856, 0, 10, 170, t, null);
		Step s171 = new Step("step_varrados_171", "step", 42.7600939045971,
				0.77149146566253, 0, 10, 171, t, null);
		Step s172 = new Step("step_varrados_172", "step", 42.7601588475893,
				0.771473861499772, 0, 10, 172, t, null);
		Step s173 = new Step("step_varrados_173", "step", 42.7602248604431,
				0.771511211326118, 0, 10, 173, t, null);
		Step s174 = new Step("step_varrados_174", "step", 42.7602872060498,
				0.771591464615939, 0, 10, 174, t, null);
		Step s175 = new Step("step_varrados_175", "step", 42.7605267573268,
				0.771870059728542, 0, 10, 175, t, null);
		Step s176 = new Step("step_varrados_176", "step", 42.7606368804157,
				0.771976097409376, 0, 10, 176, t, null);
		Step s177 = new Step("step_varrados_177", "step", 42.7610436782296,
				0.77228841806596, 0, 10, 177, t, null);
		Step s178 = new Step("step_varrados_178", "step", 42.7611236047198,
				0.772346656792, 0, 10, 178, t, null);
		Step s179 = new Step("step_varrados_179", "step", 42.7612161973587,
				0.772361669148077, 0, 10, 179, t, null);
		Step s180 = new Step("step_varrados_180", "step", 42.761397239788,
				0.772410173607852, 0, 10, 180, t, null);
		Step s181 = new Step("step_varrados_181", "step", 42.7615382635752,
				0.772484554214866, 0, 10, 181, t, null);
		Step s182 = new Step("step_varrados_182", "step", 42.761789231599,
				0.772655816715317, 0, 10, 182, t, null);
		Step s183 = new Step("step_varrados_183", "step", 42.761980995318,
				0.772792539547549, 0, 10, 183, t, null);
		Step s184 = new Step("step_varrados_184", "step", 42.7620410904267,
				0.772872876971648, 0, 10, 184, t, null);
		Step s185 = new Step("step_varrados_185", "step", 42.7621153962517,
				0.772989368038823, 0, 10, 185, t, null);
		Step s186 = new Step("step_varrados_186", "step", 42.7621502103627,
				0.773043114638706, 0, 10, 186, t, null);
		Step s187 = new Step("step_varrados_187", "step", 42.7622989330848,
				0.773166105414332, 0, 10, 187, t, null);
		Step s188 = new Step("step_varrados_188", "step", 42.7623953195163,
				0.773260419505892, 0, 10, 188, t, null);
		Step s189 = new Step("step_varrados_189", "step", 42.7624878004409,
				0.773385426253887, 0, 10, 189, t, null);
		Step s190 = new Step("step_varrados_190", "step", 42.7628477080571,
				0.773717776272258, 0, 10, 190, t, null);
		Step s191 = new Step("step_varrados_191", "step", 42.7629283464177,
				0.773812656130906, 0, 10, 191, t, null);
		Step s192 = new Step("step_varrados_192", "step", 42.7632667655045,
				0.774197720325713, 0, 10, 192, t, null);
		Step s193 = new Step("step_varrados_193", "step", 42.7634606052285,
				0.774441311881087, 0, 10, 193, t, null);
		Step s194 = new Step("step_varrados_194", "step", 42.7635478139394,
				0.774526792411825, 0, 10, 194, t, null);
		Step s195 = new Step("step_varrados_195", "step", 42.7636552695726,
				0.774611548157717, 0, 10, 195, t, null);
		Step s196 = new Step("step_varrados_196", "step", 42.7638595852204,
				0.774815048332683, 0, 10, 196, t, null);
		Step s197 = new Step("step_varrados_197", "step", 42.7639797133597,
				0.774972678880803, 0, 10, 197, t, null);
		Step s198 = new Step("step_varrados_198", "step", 42.7640208008094,
				0.775001761005139, 0, 10, 198, t, null);
		Step s199 = new Step("step_varrados_199", "step", 42.7640769237418,
				0.77499364098131, 0, 10, 199, t, null);
		Step s200 = new Step("step_varrados_200", "step", 42.7642493170693,
				0.774944694064014, 0, 10, 200, t, null);
		Step s201 = new Step("step_varrados_201", "step", 42.7646610057363,
				0.774929953679766, 0, 10, 201, t, null);
		Step s202 = new Step("step_varrados_202", "step", 42.7650362305595,
				0.7750081797042, 0, 10, 202, t, null);
		Step s203 = new Step("step_varrados_203", "step", 42.765272504699,
				0.77500277540988, 0, 10, 203, t, null);
		Step s204 = new Step("step_varrados_204", "step", 42.7655574461424,
				0.775068957997575, 0, 10, 204, t, null);
		Step s205 = new Step("step_varrados_205", "step", 42.7657437583774,
				0.775157004804499, 0, 10, 205, t, null);
		Step s206 = new Step("step_varrados_206", "step", 42.7658942539437,
				0.775255500646277, 0, 10, 206, t, null);
		Step s207 = new Step("step_varrados_207", "step", 42.7660227868425,
				0.775382281981833, 0, 10, 207, t, null);
		Step s208 = new Step("step_varrados_208", "step", 42.7662211236189,
				0.775625726727916, 0, 10, 208, t, null);
		Step s209 = new Step("step_varrados_209", "step", 42.7665279231475,
				0.776005841290113, 0, 10, 209, t, null);
		Step s210 = new Step("step_varrados_210", "step", 42.7665668793399,
				0.7760411123908, 0, 10, 210, t, null);
		Step s211 = new Step("step_varrados_211", "step", 42.7666280948921,
				0.776063365098574, 0, 10, 211, t, null);
		Step s212 = new Step("step_varrados_212", "step", 42.7667326473006,
				0.776114621401144, 0, 10, 212, t, null);
		Step s213 = new Step("step_varrados_213", "step", 42.7667787083775,
				0.776167971147485, 0, 10, 213, t, null);
		Step s214 = new Step("step_varrados_214", "step", 42.7668495201174,
				0.776336542683346, 0, 10, 214, t, null);
		Step s215 = new Step("step_varrados_215", "step", 42.7669604145338,
				0.77659839975816, 0, 10, 215, t, null);
		Step s216 = new Step("step_varrados_216", "step", 42.7671055239619,
				0.776767369484479, 0, 10, 216, t, null);
		Step s217 = new Step("step_varrados_217", "step", 42.7672397999147,
				0.776958115918646, 0, 10, 217, t, null);
		Step s218 = new Step("step_varrados_218", "step", 42.7673232191108,
				0.777080406340934, 0, 10, 218, t, null);
		Step s219 = new Step("step_varrados_219", "step", 42.767391720599,
				0.777246007532521, 0, 10, 219, t, null);
		Step s220 = new Step("step_varrados_220", "step", 42.7674849089098,
				0.777407670583029, 0, 10, 220, t, null);
		Step s221 = new Step("step_varrados_221", "step", 42.7675865621227,
				0.777541531892306, 0, 10, 221, t, null);
		Step s222 = new Step("step_varrados_222", "step", 42.7677120130381,
				0.777625654480976, 0, 10, 222, t, null);
		Step s223 = new Step("step_varrados_223", "step", 42.7677538705123,
				0.77769443402618, 0, 10, 223, t, null);
		Step s224 = new Step("step_varrados_224", "step", 42.7677655959675,
				0.777834567897921, 0, 10, 224, t, null);
		Step s225 = new Step("step_varrados_225", "step", 42.767758790453,
				0.777947864948048, 0, 10, 225, t, null);
		Step s226 = new Step("step_varrados_226", "step", 42.7677856138515,
				0.778169957667498, 0, 10, 226, t, null);
		Step s227 = new Step("step_varrados_227", "step", 42.7678371808608,
				0.778275056591699, 0, 10, 227, t, null);
		Step s228 = new Step("step_varrados_228", "step", 42.768017986335,
				0.778427478737412, 0, 10, 228, t, null);
		Step s229 = new Step("step_varrados_229", "step", 42.7680708546537,
				0.778483643417834, 0, 10, 229, t, null);
		Step s230 = new Step("step_varrados_230", "step", 42.7680949513088,
				0.778565280984924, 0, 10, 230, t, null);
		Step s231 = new Step("step_varrados_231", "step", 42.7680815145833,
				0.778684926662358, 0, 10, 231, t, null);
		Step s232 = new Step("step_varrados_232", "step", 42.7680753599909,
				0.778831811657673, 0, 10, 232, t, null);
		Step s233 = new Step("step_varrados_233", "step", 42.7681096992619,
				0.778977249205644, 0, 10, 233, t, null);
		Step s234 = new Step("step_varrados_234", "step", 42.7683920392395,
				0.779489653769941, 0, 10, 234, t, null);
		Step s235 = new Step("step_varrados_235", "step", 42.7685119845966,
				0.779754255045796, 0, 10, 235, t, null);
		Step s236 = new Step("step_varrados_236", "step", 42.7685468549303,
				0.779811064381669, 0, 10, 236, t, null);
		Step s237 = new Step("step_varrados_237", "step", 42.7686619442598,
				0.779941396248993, 0, 10, 237, t, null);
		Step s238 = new Step("step_varrados_238", "step", 42.7687569048455,
				0.780078558847235, 0, 10, 238, t, null);
		Step s239 = new Step("step_varrados_239", "step", 42.7688637649775,
				0.78024890789966, 0, 10, 239, t, null);
		Step s240 = new Step("step_varrados_240", "step", 42.7689422077848,
				0.780346939138516, 0, 10, 240, t, null);
		Step s241 = new Step("step_varrados_241", "step", 42.7690013505496,
				0.780378437595495, 0, 10, 241, t, null);
		Step s242 = new Step("step_varrados_242", "step", 42.769061262732,
				0.780333519396677, 0, 10, 242, t, null);
		Step s243 = new Step("step_varrados_243", "step", 42.7693990671012,
				0.779991448464035, 0, 10, 243, t, null);
		Step s244 = new Step("step_varrados_244", "step", 42.7694668533206,
				0.780004304438475, 0, 10, 244, t, null);
		Step s245 = new Step("step_varrados_245", "step", 42.7695695097605,
				0.780073970838153, 0, 10, 245, t, null);
		Step s246 = new Step("step_varrados_246", "step", 42.7698425499342,
				0.780339220171129, 0, 10, 246, t, null);
		Step s247 = new Step("step_varrados_247", "step", 42.7699163749288,
				0.780431306757887, 0, 10, 247, t, null);
		Step s248 = new Step("step_varrados_248", "step", 42.7701068868257,
				0.780852287950365, 0, 10, 248, t, null);
		Step s249 = new Step("step_varrados_249", "step", 42.7701571491725,
				0.780890215853949, 0, 10, 249, t, null);
		Step s250 = new Step("step_varrados_250", "step", 42.7703227367254,
				0.780954581231887, 0, 10, 250, t, null);
		Step s251 = new Step("step_varrados_251", "step", 42.7704013568431,
				0.780945662028552, 0, 10, 251, t, null);
		Step s252 = new Step("step_varrados_252", "step", 42.7704693207432,
				0.780851565512661, 0, 10, 252, t, null);
		Step s253 = new Step("step_varrados_253", "step", 42.7706099252481,
				0.780672371738157, 0, 10, 253, t, null);
		Step s254 = new Step("step_varrados_254", "step", 42.7707850442175,
				0.780531667677621, 0, 10, 254, t, null);
		Step s255 = new Step("step_varrados_255", "step", 42.7709011981865,
				0.780484739116719, 0, 10, 255, t, null);
		Step s256 = new Step("step_varrados_256", "step", 42.7709602817121,
				0.780513185074354, 0, 10, 256, t);
		Step s257 = new Step("step_varrados_257", "step", 42.7710402044645,
				0.780687558647637, 0, 10, 257, t, null);
		Step s258 = new Step("step_varrados_258", "step", 42.7711005311031,
				0.780896244959194, 0, 10, 258, t, null);
		Step s259 = new Step("step_varrados_259", "step", 42.7711330326064,
				0.781063146018213, 0, 10, 259, t, null);
		Step s260 = new Step("step_varrados_260", "step", 42.7711444581665,
				0.781188020505514, 0, 10, 260, t, null);
		Step s261 = new Step("step_varrados_261", "step", 42.7711932993862,
				0.781268779470774, 0, 10, 261, t, null);
		Step s262 = new Step("step_varrados_262", "step", 42.7715084889457,
				0.781501978779148, 0, 10, 262, t, null);
		Step s263 = new Step("step_varrados_263", "step", 42.7717193646634,
				0.781696125045084, 0, 10, 263, t, null);
		Step s264 = new Step("step_varrados_264", "step", 42.7719079208662,
				0.781900236583173, 0, 10, 264, t, null);
		Step s265 = new Step("step_varrados_265", "step", 42.7720532596065,
				0.782081446416839, 0, 10, 265, t, null);
		Step s266 = new Step("step_varrados_266", "step", 42.7721322926163,
				0.78221002098729, 0, 10, 266, t, null);
		Step s267 = new Step("step_varrados_267", "step", 42.7721173125004,
				0.782366397449931, 0, 10, 267, t, null);
		Step s268 = new Step("step_varrados_268", "step", 42.772056569669,
				0.782484682882959, 0, 10, 268, t, null);
		Step s269 = new Step("step_varrados_269", "step", 42.7720215213924,
				0.782534825291184, 0, 10, 269, t, null);
		Step s270 = new Step("step_varrados_270", "step", 42.772005472112,
				0.782868470995283, 0, 10, 270, t, null);
		Step s271 = new Step("step_varrados_271", "step", 42.7719688837229,
				0.782955336657463, 0, 10, 271, t, null);
		Step s272 = new Step("step_varrados_272", "step", 42.7719392217131,
				0.783051122192925, 0, 10, 272, t, null);
		Step s273 = new Step("step_varrados_273", "step", 42.7719323520595,
				0.783161372918805, 0, 10, 273, t, null);
		Step s274 = new Step("step_varrados_274", "step", 42.7719548463403,
				0.783276687344823, 0, 10, 274, t, null);
		Step s275 = new Step("step_varrados_275", "step", 42.7719606466529,
				0.783343706062897, 0, 10, 275, t, null);
		Step s276 = new Step("step_varrados_276", "step", 42.7719355341898,
				0.783790735871737, 0, 10, 276, t, null);
		Step s277 = new Step("step_varrados_277", "step", 42.7719468977012,
				0.783912558989395, 0, 10, 277, t, null);
		Step s278 = new Step("step_varrados_278", "step", 42.772053269037,
				0.784406846646409, 0, 10, 278, t, null);
		Step s279 = new Step("step_varrados_279", "step", 42.7721465067807,
				0.784571586318066, 0, 10, 279, t, null);
		Step s280 = new Step("step_varrados_280", "step", 42.7722994809059,
				0.78468224908333, 0, 10, 280, t, null);
		Step s281 = new Step("step_varrados_281", "step", 42.7723627633392,
				0.784811389076323, 0, 10, 281, t, null);
		Step s282 = new Step("step_varrados_282", "step", 42.7723706351623,
				0.784869167325226, 0, 10, 282, t, null);
		Step s283 = new Step("step_varrados_283", "step", 42.7722187667845,
				0.785280993585848, 0, 10, 283, t, null);
		Step s284 = new Step("step_varrados_284", "step", 42.7722602510573,
				0.785679815058983, 0, 10, 284, t, null);
		Step s285 = new Step("step_varrados_285", "step", 42.77226894431,
				0.785896461902821, 0, 10, 285, t, null);
		Step s286 = new Step("step_varrados_286", "step", 42.7722385012989,
				0.786184785117649, 0, 10, 286, t, null);
		Step s287 = new Step("step_varrados_287", "step", 42.7722444124397,
				0.786374029546798, 0, 10, 287, t, null);
		Step s288 = new Step("step_varrados_288", "step", 42.7722861467688,
				0.786436712848652, 0, 10, 288, t, null);
		Step s289 = new Step("step_varrados_289", "step", 42.7723881500038,
				0.786472803080377, 0, 10, 289, t, null);
		Step s290 = new Step("step_varrados_290", "step", 42.7725505993181,
				0.786491461116626, 0, 10, 290, t, null);
		Step s291 = new Step("step_varrados_291", "step", 42.7726745088285,
				0.786496213610886, 0, 10, 291, t, null);
		Step s292 = new Step("step_varrados_292", "step", 42.77276632653,
				0.786587670579065, 0, 10, 292, t, null);
		Step s293 = new Step("step_varrados_293", "step", 42.7727589220837,
				0.786670439689906, 0, 10, 293, t, null);
		Step s294 = new Step("step_varrados_294", "step", 42.7726405625359,
				0.786952729287422, 0, 10, 294, t, null);
		Step s295 = new Step("step_varrados_295", "step", 42.7726224407417,
				0.787063381650234, 0, 10, 295, t, null);
		Step s296 = new Step("step_varrados_296", "step", 42.7726454624363,
				0.78720618172146, 0, 10, 296, t, null);
		Step s297 = new Step("step_varrados_297", "step", 42.7727403556754,
				0.787340310683958, 0, 10, 297, t, null);
		Step s298 = new Step("step_varrados_298", "step", 42.772901138771,
				0.78750570684998, 0, 10, 298, t, null);
		Step s299 = new Step("step_varrados_299", "step", 42.7730201289373,
				0.787605365280818, 0, 10, 299, t, null);
		Step s300 = new Step("step_varrados_300", "step", 42.7730983886915,
				0.787694251412437, 0, 10, 300, t, null);
		Step s301 = new Step("step_varrados_301", "step", 42.7731337864082,
				0.787778552481707, 0, 10, 301, t, null);
		Step s302 = new Step("step_varrados_302", "step", 42.7731466210564,
				0.787976720603781, 0, 10, 302, t, null);
		Step s303 = new Step("step_varrados_303", "step", 42.7731926105193,
				0.788143150681533, 0, 10, 303, t, null);
		Step s304 = new Step("step_varrados_304", "step", 42.7732882697522,
				0.788316980081918, 0, 10, 304, t, null);
		Step s305 = new Step("step_varrados_305", "step", 42.7733150222966,
				0.788419924200438, 0, 10, 305, t, null);
		Step s306 = new Step("step_varrados_306", "step", 42.7733289846185,
				0.788559993067808, 0, 10, 306, t, null);
		Step s307 = new Step("step_varrados_307", "step", 42.7733189081683,
				0.788737586998799, 0, 10, 307, t, null);
		Step s308 = new Step("step_varrados_308", "step", 42.7734036130455,
				0.788927086462359, 0, 10, 308, t, null);
		Step s309 = new Step("step_varrados_309", "step", 42.7734861352461,
				0.789003599945369, 0, 10, 309, t, null);
		Step s310 = new Step("step_varrados_310", "step", 42.7735691798092,
				0.789223717285691, 0, 10, 310, t, null);
		Step s311 = new Step("step_varrados_311", "step", 42.7736667922672,
				0.789382200903272, 0, 10, 311, t, null);
		Step s312 = new Step("step_varrados_312", "step", 42.7737010682849,
				0.789408483432002, 0, 10, 312, t, null);
		Step s313 = new Step("step_varrados_313", "step", 42.7737637645438,
				0.789390973487553, 0, 10, 313, t, null);
		Step s314 = new Step("step_varrados_314", "step", 42.7738004757903,
				0.789310216269113, 0, 10, 314, t, null);
		Step s315 = new Step("step_varrados_315", "step", 42.7737921933188,
				0.789231060108237, 0, 10, 315, t, null);
		Step s316 = new Step("step_varrados_316", "step", 42.7737495250725,
				0.789003393399802, 0, 10, 316, t, null);
		Step s317 = new Step("step_varrados_317", "step", 42.7737050276235,
				0.788681062340284, 0, 10, 317, t, null);
		Step s318 = new Step("step_varrados_318", "step", 42.7736853937788,
				0.788363958198278, 0, 10, 318, t, null);
		Step s319 = new Step("step_varrados_319", "step", 42.7737102732768,
				0.788136943339402, 0, 10, 319, t, null);
		Step s320 = new Step("step_varrados_320", "step", 42.7736983240545,
				0.787984578840411, 0, 10, 320, t, null);
		Step s321 = new Step("step_varrados_321", "step", 42.7736309068664,
				0.787757794647661, 0, 10, 321, t, null);
		Step s322 = new Step("step_varrados_322", "step", 42.7736110276476,
				0.78754459693637, 0, 10, 322, t, null);
		Step s323 = new Step("step_varrados_323", "step", 42.7736647905904,
				0.787414338798607, 0, 10, 323, t, null);
		Step s324 = new Step("step_varrados_324", "step", 42.7736585794088,
				0.787325941909105, 0, 10, 324, t, null);
		Step s325 = new Step("step_varrados_325", "step", 42.7735971361554,
				0.787175340629432, 0, 10, 325, t, null);
		Step s326 = new Step("step_varrados_326", "step", 42.7735575991284,
				0.786993401400927, 0, 10, 326, t, null);
		Step s327 = new Step("step_varrados_327", "step", 42.7735796297565,
				0.786852050165476, 0, 10, 327, t, null);
		Step s328 = new Step("step_varrados_328", "step", 42.7737812918349,
				0.786453723354377, 0, 10, 328, t, null);
		Step s329 = new Step("step_varrados_329", "step", 42.7739331744788,
				0.78580964734589, 0, 10, 329, t, null);
		Step s330 = new Step("step_varrados_330", "step", 42.7739804226565,
				0.785691842487462, 0, 10, 330, t, null);
		Step s331 = new Step("step_varrados_331", "step", 42.774055789278,
				0.785631095658737, 0, 10, 331, t, null);
		Step s332 = new Step("step_varrados_332", "step", 42.774205158903,
				0.785555487504637, 0, 10, 332, t, null);
		Step s333 = new Step("step_varrados_333", "step", 42.774282538801,
				0.785482445203941, 0, 10, 333, t, null);
		Step s334 = new Step("step_varrados_334", "step", 42.7743652497013,
				0.785335872729122, 0, 10, 334, t, null);
		Step s335 = new Step("step_varrados_335", "step", 42.7744587907791,
				0.785283644648003, 0, 10, 335, t, null);
		Step s336 = new Step("step_varrados_336", "step", 42.7745181686376,
				0.785327365370258, 0, 10, 336, t, null);
		Step s337 = new Step("step_varrados_337", "step", 42.7745091062956,
				0.785440754454927, 0, 10, 337, t, null);
		Step s338 = new Step("step_varrados_338", "step", 42.7742088806118,
				0.785747871718166, 0, 10, 338, t, null);
		Step s339 = new Step("step_varrados_339", "step", 42.7741741853385,
				0.78581633643123, 0, 10, 339, t, null);
		Step s340 = new Step("step_varrados_340", "step", 42.7741578418535,
				0.785902481926288, 0, 10, 340, t, null);
		Step s341 = new Step("step_varrados_341", "step", 42.7741689669604,
				0.786012095000084, 0, 10, 341, t, null);
		Step s342 = new Step("step_varrados_342", "step", 42.774223184829,
				0.786254628694396, 0, 10, 342, t, null);
		Step s343 = new Step("step_varrados_343", "step", 42.7742624277895,
				0.78642130072574, 0, 10, 343, t, null);
		Step s344 = new Step("step_varrados_344", "step", 42.7742908886465,
				0.786728925199284, 0, 10, 344, t, null);
		Step s345 = new Step("step_varrados_345", "step", 42.7743243326311,
				0.786828576054762, 0, 10, 345, t, null);
		Step s346 = new Step("step_varrados_346", "step", 42.7744616160194,
				0.786942862856268, 0, 10, 346, t, null);
		Step s347 = new Step("step_varrados_347", "step", 42.7745782389225,
				0.787036495229018, 0, 10, 347, t, null);
		Step s348 = new Step("step_varrados_348", "step", 42.7747073434473,
				0.787309978382285, 0, 10, 348, t, null);
		Step s349 = new Step("step_varrados_349", "step", 42.7747712789664,
				0.787356594731871, 0, 10, 349, t, null);
		Step s350 = new Step("step_varrados_350", "step", 42.7748433867766,
				0.787360138133705, 0, 10, 350, t, null);
		Step s351 = new Step("step_varrados_351", "step", 42.7749077420277,
				0.787312008079561, 0, 10, 351, t, null);
		Step s352 = new Step("step_varrados_352", "step", 42.7750551646635,
				0.78713562863484, 0, 10, 352, t, null);
		Step s353 = new Step("step_varrados_353", "step", 42.7752270973085,
				0.786946151686652, 0, 10, 353, t, null);
		Step s354 = new Step("step_varrados_354", "step", 42.7753982560719,
				0.786833098297272, 0, 10, 354, t, null);
		Step s355 = new Step("step_varrados_355", "step", 42.7755748588154,
				0.786768744556797, 0, 10, 355, t, null);
		Step s356 = new Step("step_varrados_356", "step", 42.7757116758729,
				0.78674247873467, 0, 10, 356, t, null);
		Step s357 = new Step("step_varrados_357", "step", 42.775929304192,
				0.786704165811307, 0, 10, 357, t, null);
		Step s358 = new Step("step_varrados_358", "step", 42.7760093419832,
				0.786768544168538, 0, 10, 358, t, null);
		Step s359 = new Step("step_varrados_359", "step", 42.7760848743687,
				0.786949208013942, 0, 10, 359, t, null);
		Step s360 = new Step("step_varrados_360", "step", 42.776123585085,
				0.787088401140237, 0, 10, 360, t, null);
		Step s361 = new Step("step_varrados_361", "step", 42.7761455379176,
				0.78729236567939, 0, 10, 361, t, null);
		Step s362 = new Step("step_varrados_362", "step", 42.7761368802842,
				0.787543259398628, 0, 10, 362, t, null);
		Step s363 = new Step("step_varrados_363", "step", 42.7761109848788,
				0.78795061871718, 0, 10, 363, t, null);
		Step s364 = new Step("step_varrados_364", "step", 42.7761157721521,
				0.788081852706751, 0, 10, 364, t, null);
		Step s365 = new Step("step_varrados_365", "step", 42.7761569007831,
				0.788346253237913, 0, 10, 365, t, null);
		Step s366 = new Step("step_varrados_366", "step", 42.7762101109201,
				0.788536881891589, 0, 10, 366, t, null);
		Step s367 = new Step("step_varrados_367", "step", 42.7762664042055,
				0.788654058914006, 0, 10, 367, t, null);
		Step s368 = new Step("step_varrados_368", "step", 42.7763304331369,
				0.789055162130488, 0, 10, 368, t, null);
		Step s369 = new Step("step_varrados_369", "step", 42.7763601931836,
				0.789313856953321, 0, 10, 369, t, null);
		Step s370 = new Step("step_varrados_370", "step", 42.7763375022788,
				0.789537747285399, 0, 10, 370, t, null);
		Step s371 = new Step("step_varrados_371", "step", 42.7762995938565,
				0.789789682249797, 0, 10, 371, t, null);
		Step s372 = new Step("step_varrados_372", "step", 42.7763171106018,
				0.789880736837757, 0, 10, 372, t, null);
		Step s373 = new Step("step_varrados_373", "step", 42.7764144274862,
				0.79002395918295, 0, 10, 373, t, null);
		Step s374 = new Step("step_varrados_374", "step", 42.7764875965438,
				0.790082474797855, 0, 10, 374, t, null);
		Step s375 = new Step("step_varrados_375", "step", 42.7768229575291,
				0.790311964250921, 0, 10, 375, t, null);
		Step s376 = new Step("step_varrados_376", "step", 42.7769245964252,
				0.790445866800845, 0, 10, 376, t, null);
		Step s377 = new Step("step_varrados_377", "step", 42.77696596528,
				0.790606361171, 0, 10, 377, t, null);
		Step s378 = new Step("step_varrados_378", "step", 42.7769468871512,
				0.790784285368203, 0, 10, 378, t, null);
		Step s379 = new Step("step_varrados_379", "step", 42.7768710362069,
				0.790936724818772, 0, 10, 379, t, null);
		Step s380 = new Step("step_varrados_380", "step", 42.7767425583079,
				0.791045196065839, 0, 10, 380, t, null);
		Step s381 = new Step("step_varrados_381", "step", 42.7766790264956,
				0.791136077723212, 0, 10, 381, t, null);
		Step s382 = new Step("step_varrados_382", "step", 42.7766559300048,
				0.791222465467439, 0, 10, 382, t, null);
		Step s383 = new Step("step_varrados_383", "step", 42.7766588763478,
				0.791375157953404, 0, 10, 383, t, null);
		Step s384 = new Step("step_varrados_384", "step", 42.7766804573054,
				0.791676929304354, 0, 10, 384, t, null);
		Step s385 = new Step("step_varrados_385", "step", 42.7766754757159,
				0.791768784778536, 0, 10, 385, t, null);
		Step s386 = new Step("step_varrados_386", "step", 42.7766463366228,
				0.791892058417891, 0, 10, 386, t, null);
		Step s387 = new Step("step_varrados_387", "step", 42.7766338288034,
				0.792060579944958, 0, 10, 387, t, null);
		Step s388 = new Step("step_varrados_388", "step", 42.776650095438,
				0.792203631164066, 0, 10, 388, t, null);
		Step s389 = new Step("step_varrados_389", "step", 42.7767272212296,
				0.792350631448415, 0, 10, 389, t, null);
		Step s390 = new Step("step_varrados_390", "step", 42.7768135105292,
				0.79262260051576, 0, 10, 390, t, null);
		Step s391 = new Step("step_varrados_391", "step", 42.7768559952057,
				0.792841119567739, 0, 10, 391, t, null);
		Step s392 = new Step("step_varrados_392", "step", 42.7768134035025,
				0.793084053203851, 0, 10, 392, t, null);
		Step s393 = new Step("step_varrados_393", "step", 42.7767949815359,
				0.793179442208182, 0, 10, 393, t, null);
		Step s394 = new Step("step_varrados_394", "step", 42.7768106580516,
				0.793291955534023, 0, 10, 394, t, null);
		Step s395 = new Step("step_varrados_395", "step", 42.7768663452478,
				0.793494726297457, 0, 10, 395, t, null);
		Step s396 = new Step("step_varrados_396", "step", 42.7768381920615,
				0.793786041984956, 0, 10, 396, t, null);
		Step s397 = new Step("step_varrados_397", "step", 42.7768363281549,
				0.794039752381415, 0, 10, 397, t, null);
		Step s398 = new Step("step_varrados_398", "step", 42.7767995867353,
				0.794352764304011, 0, 10, 398, t, null);
		Step s399 = new Step("step_varrados_399", "step", 42.7767555796334,
				0.79452240414351, 0, 10, 399, t, null);
		Step s400 = new Step("step_varrados_400", "step", 42.7767210555943,
				0.794600028618358, 0, 10, 400, t, null);
		Step s401 = new Step("step_varrados_401", "step", 42.7766907366955,
				0.794662223979899, 0, 10, 401, t, null);
		Step s402 = new Step("step_varrados_402", "step", 42.7766752690982,
				0.794794178808121, 0, 10, 402, t, null);
		Step s403 = new Step("step_varrados_403", "step", 42.7766793872558,
				0.795007948875949, 0, 10, 403, t, null);
		Step s404 = new Step("step_varrados_404", "step", 42.776710442483,
				0.79521770694295, 0, 10, 404, t, null);
		Step s405 = new Step("step_varrados_405", "step", 42.7767296607019,
				0.795397325824665, 0, 10, 405, t, null);
		Step s406 = new Step("step_varrados_406", "step", 42.7767219559054,
				0.795464830144568, 0, 10, 406, t, null);
		Step s407 = new Step("step_varrados_407", "step", 42.7766779619362,
				0.795518342552067, 0, 10, 407, t, null);
		Step s408 = new Step("step_varrados_408", "step", 42.776555875674,
				0.795608241523846, 0, 10, 408, t, null);
		Step s409 = new Step("step_varrados_409", "step", 42.7764902080511,
				0.795705305914341, 0, 10, 409, t, null);
		Step s410 = new Step("step_varrados_410", "step", 42.7763961024794,
				0.795962287681267, 0, 10, 410, t, null);
		Step s411 = new Step("step_varrados_411", "step", 42.7763642821522,
				0.796180387815375, 0, 10, 411, t, null);
		Step s412 = new Step("step_varrados_412", "step", 42.7763590304933,
				0.796492279098973, 0, 10, 412, t, null);
		Step s413 = new Step("step_varrados_413", "step", 42.7762902147463,
				0.796659740245492, 0, 10, 413, t, null);
		Step s414 = new Step("step_varrados_414", "step", 42.7762513599574,
				0.797214240986082, 0, 10, 414, t, null);
		Step s415 = new Step("step_varrados_415", "step", 42.7761750134279,
				0.797458366121874, 0, 10, 415, t, null);
		Step s416 = new Step("step_varrados_416", "step", 42.776185741141,
				0.79778191356464, 0, 10, 416, t, null);
		Step s417 = new Step("step_varrados_417", "step", 42.7762314643933,
				0.79805226994135, 0, 10, 417, t, null);
		Step s418 = new Step("step_varrados_418", "step", 42.7762423430153,
				0.798149673945792, 0, 10, 418, t, null);
		Step s419 = new Step("step_varrados_419", "step", 42.776211593403,
				0.798306616008923, 0, 10, 419, t, null);
		Step s420 = new Step("step_varrados_420", "step", 42.7761988641251,
				0.798698225382795, 0, 10, 420, t, null);
		Step s421 = new Step("step_varrados_421", "step", 42.7761647705638,
				0.799032529028595, 0, 10, 421, t, null);
		Step s422 = new Step("step_varrados_422", "step", 42.7760614824423,
				0.799280661984654, 0, 10, 422, t, null);
		Step s423 = new Step("step_varrados_423", "step", 42.7760210279495,
				0.799401275846473, 0, 10, 423, t, null);
		Step s424 = new Step("step_varrados_424", "step", 42.7760058881392,
				0.799667676708948, 0, 10, 424, t, null);
		Step s425 = new Step("step_varrados_425", "step", 42.7760049502403,
				0.79997024543479, 0, 10, 425, t, null);
		Step s426 = new Step("step_varrados_426", "step", 42.7760588259213,
				0.800313655989049, 0, 10, 426, t, null);
		Step s427 = new Step("step_varrados_427", "step", 42.7760720110037,
				0.800414034380415, 0, 10, 427, t, null);
		Step s428 = new Step("step_varrados_428", "step", 42.7760750037385,
				0.800569780216528, 0, 10, 428, t, null);
		Step s429 = new Step("step_varrados_429", "step", 42.7760838652278,
				0.800679479519669, 0, 10, 429, t, null);
		Step s430 = new Step("step_varrados_430", "step", 42.7761158103362,
				0.80081892082877, 0, 10, 430, t, null);
		Step s431 = new Step("step_varrados_431", "step", 42.7761051869872,
				0.800969036965324, 0, 10, 431, t, null);
		Step s432 = new Step("step_varrados_432", "step", 42.7761453061674,
				0.801416837184857, 0, 10, 432, t, null);
		Step s433 = new Step("step_varrados_433", "step", 42.7760982553064,
				0.801662975964865, 0, 10, 433, t, null);
		Step s434 = new Step("step_varrados_434", "step", 42.7760185352094,
				0.801849152058006, 0, 10, 434, t, null);
		Step s435 = new Step("step_varrados_435", "step", 42.7758800293593,
				0.802022128110766, 0, 10, 435, t, null);
		Step s436 = new Step("step_varrados_436", "step", 42.7757647824979,
				0.8022340072202, 0, 10, 436, t, null);
		Step s437 = new Step("step_varrados_437", "step", 42.7756961708374,
				0.802529800974016, 0, 10, 437, t, null);
		Step s438 = new Step("step_varrados_438", "step", 42.7756589733588,
				0.802937551187753, 0, 10, 438, t, null);
		Step s439 = new Step("step_varrados_439", "step", 42.775658332895,
				0.803490691317206, 0, 10, 439, t, null);
		Step s440 = new Step("step_varrados_440", "step", 42.7756744217956,
				0.803859886157333, 0, 10, 440, t, null);
		Step s441 = new Step("step_varrados_441", "step", 42.7756422933383,
				0.804062711187519, 0, 10, 441, t, null);
		Step s442 = new Step("step_varrados_442", "step", 42.7755843047292,
				0.804208388075337, 0, 10, 442, t, null);
		Step s443 = new Step("step_varrados_443", "step", 42.7754874265838,
				0.804321824439771, 0, 10, 443, t, null);
		Step s444 = new Step("step_varrados_444", "step", 42.7754263093,
				0.804421772996898, 0, 10, 444, t, null);
		Step s445 = new Step("step_varrados_445", "step", 42.7753792034483,
				0.804900155803276, 0, 10, 445, t, null);
		Step s446 = new Step("step_varrados_446", "step", 42.7753474830112,
				0.80512435609702, 0, 10, 446, t, null);
		Step s447 = new Step("step_varrados_447", "step", 42.7752670297324,
				0.805390004234243, 0, 10, 447, t, null);
		Step s448 = new Step("step_varrados_448", "step", 42.7751615948705,
				0.80564431061667, 0, 10, 448, t, null);
		Step s449 = new Step("step_varrados_449", "step", 42.7750454962375,
				0.805929551445099, 0, 10, 449, t, null);
		Step s450 = new Step("step_varrados_450", "step", 42.7750035787098,
				0.806209115708045, 0, 10, 450, t, null);
		Step s451 = new Step("step_varrados_451", "step", 42.7750116222369,
				0.806511362424659, 0, 10, 451, t, null);
		Step s452 = new Step("step_varrados_452", "step", 42.7751073451084,
				0.80680745788484, 0, 10, 452, t, null);
		Step s453 = new Step("step_varrados_453", "step", 42.7752064550642,
				0.807045373048572, 0, 10, 453, t, null);
		Step s454 = new Step("step_varrados_454", "step", 42.7752945499883,
				0.807295901289179, 0, 10, 454, t, null);
		Step s455 = new Step("step_varrados_455", "step", 42.7753378526142,
				0.807441055201133, 0, 10, 455, t, null);
		Step s456 = new Step("step_varrados_456", "step", 42.7753339696393,
				0.807590930486194, 0, 10, 456, t, null);
		Step s457 = new Step("step_varrados_457", "step", 42.775312003035,
				0.807854511530735, 0, 10, 457, t, null);
		Step s458 = new Step("step_varrados_458", "step", 42.7753154534439,
				0.808034686783989, 0, 10, 458, t, null);
		Step s459 = new Step("step_varrados_459", "step", 42.7753623219862,
				0.80836612436825, 0, 10, 459, t, null);
		Step s460 = new Step("step_varrados_460", "step", 42.7753722868652,
				0.808533846453008, 0, 10, 460, t, null);
		Step s461 = new Step("step_varrados_461", "step", 42.7753597261427,
				0.80881848675547, 0, 10, 461, t, null);
		Step s462 = new Step("step_varrados_462", "step", 42.7753273562275,
				0.809244396161309, 0, 10, 462, t, null);
		Step s463 = new Step("step_varrados_463", "step", 42.7753179490309,
				0.809458639643713, 0, 10, 463, t, null);
		Step s464 = new Step("step_varrados_464", "step", 42.7753207249547,
				0.809721347616641, 0, 10, 464, t, null);
		Step s465 = new Step("step_varrados_465", "step", 42.7752951303944,
				0.810030893991682, 0, 10, 465, t, null);
		Step s466 = new Step("step_varrados_466", "step", 42.7752618213004,
				0.810407941380881, 0, 10, 466, t, null);
		Step s467 = new Step("step_varrados_467", "step", 42.7751296403066,
				0.810794584414833, 0, 10, 467, t, null);
		Step s468 = new Step("step_varrados_468", "step", 42.7751042206386,
				0.810877988745142, 0, 10, 468, t, null);
		Step s469 = new Step("step_varrados_469", "step", 42.7751178023864,
				0.811235047198648, 0, 10, 469, t, null);
		Step s470 = new Step("step_varrados_470", "step", 42.7750959985646,
				0.811743090053863, 0, 10, 470, t, null);
		Step s471 = new Step("step_varrados_471", "step", 42.7751696144567,
				0.812296666181634, 0, 10, 471, t, null);
		Step s472 = new Step("step_varrados_472", "step", 42.7752334442038,
				0.81269168250887, 0, 10, 472, t, null);
		Step s473 = new Step("step_varrados_473", "step", 42.7752976552614,
				0.812988898023178, 0, 10, 473, t, null);
		Step s474 = new Step("step_varrados_474", "step", 42.7754012775451,
				0.813345844591932, 0, 10, 474, t, null);
		Step s475 = new Step("step_varrados_475", "step", 42.775501507522,
				0.813760973681991, 0, 10, 475, t, null);
		Step s476 = new Step("step_varrados_476", "step", 42.775560633222,
				0.81402781159088, 0, 10, 476, t, null);
		Step s477 = new Step("step_varrados_477", "step", 42.7756599617077,
				0.814277955994017, 0, 10, 477, t, null);
		Step s478 = new Step("step_varrados_478", "step", 42.7757383424968,
				0.814492167313064, 0, 10, 478, t, null);
		Step s479 = new Step("step_varrados_479", "step", 42.7757933990937,
				0.814899721762458, 0, 10, 479, t, null);
		Step s480 = new Step("step_varrados_480", "step", 42.7758929823953,
				0.815281263712942, 0, 10, 480, t, null);
		Step s481 = new Step("step_varrados_481", "step", 42.775972258676,
				0.815660465028408, 0, 10, 481, t, null);
		Step s482 = new Step("step_varrados_482", "step", 42.7760100481753,
				0.815989175273752, 0, 10, 482, t, null);
		Step s483 = new Step("step_varrados_483", "step", 42.7760195447374,
				0.816486955956657, 0, 10, 483, t, null);
		Step s484 = new Step("step_varrados_484", "step", 42.7760726722223,
				0.816793736481651, 0, 10, 484, t, null);
		Step s485 = new Step("step_varrados_485", "step", 42.7760927302982,
				0.817019169716828, 0, 10, 485, t, null);
		Step s486 = new Step("step_varrados_486", "step", 42.7760844945573,
				0.817413672927407, 0, 10, 486, t, null);
		Step s487 = new Step("step_varrados_487", "step", 42.7760476514336,
				0.817842796085849, 0, 10, 487, t, null);
		Step s488 = new Step("step_varrados_488", "step", 42.7760216651029,
				0.818014840334125, 0, 10, 488, t, null);
		Step s489 = new Step("step_varrados_489", "step", 42.7760043281568,
				0.818168245150497, 0, 10, 489, t, null);
		Step s490 = new Step("step_varrados_490", "step", 42.7760497526734,
				0.818661708661911, 0, 10, 490, t, null);
		Step s491 = new Step("step_varrados_491", "step", 42.776017501126,
				0.818977600704243, 0, 10, 491, t, null);
		Step s492 = new Step("step_varrados_492", "step", 42.7760711455972,
				0.819311868528207, 0, 10, 492, t, null);
		Step s493 = new Step("step_varrados_493", "step", 42.7761117180697,
				0.819432681163947, 0, 10, 493, t, null);
		Step s494 = new Step("step_varrados_494", "step", 42.7762931885548,
				0.819741072043309, 0, 10, 494, t, null);
		Step s495 = new Step("step_varrados_495", "step", 42.7763512349266,
				0.819833768719272, 0, 10, 495, t, null);
		Step s496 = new Step("step_varrados_496", "step", 42.7763905458871,
				0.820006577428274, 0, 10, 496, t, null);
		Step s497 = new Step("step_varrados_497", "step", 42.7763570510796,
				0.820139158071436, 0, 10, 497, t, null);
		Step s498 = new Step("step_varrados_498", "step", 42.7762628316443,
				0.820393050617975, 0, 10, 498, t, null);
		Step s499 = new Step("step_varrados_499", "step", 42.7762184494235,
				0.821018017182332, 0, 10, 499, t, null);
		Step s500 = new Step("step_varrados_500", "step", 42.7762371622563,
				0.821173213174239, 0, 10, 500, t, null);
		Step s501 = new Step("step_varrados_501", "step", 42.7765568073295,
				0.822473002064509, 0, 10, 501, t, null);
		Step s502 = new Step("step_varrados_502", "step", 42.7765589408704,
				0.822466815385324, 0, 10, 502, t, null);
		Step s503 = new Step("step_varrados_503", "step", 42.7765484052475,
				0.822623037875871, 0, 10, 503, t, null);
		Step s504 = new Step("step_varrados_504", "step", 42.7765341674711,
				0.822702991472857, 0, 10, 504, t, null);
		Step s505 = new Step("step_varrados_505", "step", 42.7764800308892,
				0.82293408456318, 0, 10, 505, t, null);
		Step s506 = new Step("step_varrados_506", "step", 42.7764636165972,
				0.823136351897984, 0, 10, 506, t, null);
		Step s507 = new Step("step_varrados_507", "step", 42.7764795112157,
				0.823380270206765, 0, 10, 507, t, null);
		Step s508 = new Step("step_varrados_508", "step", 42.7764806008795,
				0.823792784118838, 0, 10, 508, t, null);
		Step s509 = new Step("step_varrados_509", "step", 42.7764430810115,
				0.82406913310175, 0, 10, 509, t, null);
		Step s510 = new Step("step_varrados_510", "step", 42.776343322474,
				0.824387388721933, 0, 10, 510, t, null);
		Step s511 = new Step("step_varrados_511", "step", 42.7763429712083,
				0.824842735311837, 0, 10, 511, t, null);
		Step s512 = new Step("step_varrados_512", "step", 42.7763042168889,
				0.824935770299625, 0, 10, 512, t, null);
		Step s513 = new Step("step_varrados_513", "step", 42.7762284935527,
				0.82509732998596, 0, 10, 513, t, null);
		Step s514 = new Step("step_varrados_514", "step", 42.7761689952813,
				0.825401949798703, 0, 10, 514, t, null);
		Step s515 = new Step("step_varrados_515", "step", 42.7761490052893,
				0.825653235456425, 0, 10, 515, t, null);
		Step s516 = new Step("step_varrados_516", "step", 42.7761749955355,
				0.825954862487439, 0, 10, 516, t, null);
		Step s517 = new Step("step_varrados_517", "step", 42.7761988391526,
				0.826143495556443, 0, 10, 517, t, null);
		Step s518 = new Step("step_varrados_518", "step", 42.7761796474808,
				0.82631835499656, 0, 10, 518, t, null);
		Step s519 = new Step("step_varrados_519", "step", 42.776170034366,
				0.826523438455042, 0, 10, 519, t, null);
		Step s520 = new Step("step_varrados_520", "step", 42.7762287697869,
				0.827245638711149, 0, 10, 520, t, null);
		Step s521 = new Step("step_varrados_521", "step", 42.7764283441946,
				0.827678713523929, 0, 10, 521, t, null);
		Step s522 = new Step("step_varrados_522", "step", 42.7766734051224,
				0.828492194711843, 0, 10, 522, t, null);
		Step s523 = new Step("step_varrados_523", "step", 42.7769212224383,
				0.829213907078676, 0, 10, 523, t, null);
		Step s524 = new Step("step_varrados_524", "step", 42.7769706765796,
				0.829447488283819, 0, 10, 524, t, null);
		Step s525 = new Step("step_varrados_525", "step", 42.7769468814774,
				0.829854763125599, 0, 10, 525, t, null);
		Step s526 = new Step("step_varrados_526", "step", 42.7769234901335,
				0.830283415310618, 0, 10, 526, t, null);
		Step s527 = new Step("step_varrados_527", "step", 42.7769578305941,
				0.830551140858247, 0, 10, 527, t, null);
		Step s528 = new Step("step_varrados_528", "step", 42.7770048007902,
				0.830772586149133, 0, 10, 528, t, null);
		Step s529 = new Step("step_varrados_529", "step", 42.7770553441433,
				0.830945011477055, 0, 10, 529, t, null);
		Step s530 = new Step("step_varrados_530", "step", 42.7771137359068,
				0.831175226558588, 0, 10, 530, t, null);
		Step s531 = new Step("step_varrados_531", "step", 42.7771387792926,
				0.831308815086685, 0, 10, 531, t, null);
		Step s532 = new Step("step_varrados_532", "step", 42.7771187763774,
				0.831560103310123, 0, 10, 532, t, null);
		Step s533 = new Step("step_varrados_533", "step", 42.7770540093798,
				0.831825177706942, 0, 10, 533, t, null);
		Step s534 = new Step("step_varrados_534", "step", 42.776934097034,
				0.832150239429747, 0, 10, 534, t, null);
		Step s535 = new Step("step_varrados_535", "step", 42.7768034498415,
				0.832383995701556, 0, 10, 535, t, null);
		Step s536 = new Step("step_varrados_536", "step", 42.7766970812424,
				0.832473274070256, 0, 10, 536, t);
		Step s537 = new Step("step_varrados_537", "step", 42.7764305757402,
				0.832546747917371, 0, 10, 537, t, null);
		Step s538 = new Step("step_varrados_538", "step", 42.7763261097903,
				0.832617623450312, 0, 10, 538, t, null);
		Step s539 = new Step("step_varrados_539", "step", 42.7762241352266,
				0.832938998098033, 0, 10, 539, t, null);
		Step s540 = new Step("step_varrados_540", "step", 42.7761700355568,
				0.83317313585596, 0, 10, 540, t, null);
		Step s541 = new Step("step_varrados_541", "step", 42.7762295788758,
				0.833464428260158, 0, 10, 541, t, null);
		Step s542 = new Step("step_varrados_542", "step", 42.7762748095106,
				0.833594255890246, 0, 10, 542, t, null);
		Step s543 = new Step("step_varrados_543", "step", 42.7762873918613,
				0.833783284967075, 0, 10, 543, t, null);
		Step s544 = new Step("step_varrados_544", "step", 42.7763574849622,
				0.83391835788763, 0, 10, 544, t, null);
		Step s545 = new Step("step_varrados_545", "step", 42.7763171090475,
				0.834045059100275, 0, 10, 545, t, null);
		Step s546 = new Step("step_varrados_546", "step", 42.7763111185418,
				0.834323358036312, 0, 10, 546, t, null);
		Step s547 = new Step("step_varrados_547", "step", 42.7763196046106,
				0.834533921720014, 0, 10, 547, t, null);
		Step s548 = new Step("step_varrados_548", "step", 42.7763563034808,
				0.834926858306302, 0, 10, 548, t, null);
		Step s549 = new Step("step_varrados_549", "step", 42.7763887876225,
				0.835216040315766, 0, 10, 549, t);
		Step s550 = new Step("step_varrados_550", "step", 42.7769300506112,
				0.835029099821934, 0, 10, 550, t, null);
		Step s551 = new Step("step_varrados_551", "step", 42.7771585489586,
				0.835207550715466, 0, 10, 551, t, null);
		Step s552 = new Step("step_varrados_552", "step", 42.7774050424153,
				0.835266192252793, 0, 10, 552, t, null);
		Step s553 = new Step("step_varrados_553", "step", 42.7774917457864,
				0.835327346591656, 0, 10, 553, t, null);
		Step s554 = new Step("step_varrados_554", "step", 42.7775566393055,
				0.835187565350371, 0, 10, 554, t, null);
		Step s555 = new Step("step_varrados_555", "step", 42.7776458178069,
				0.835022489414666, 0, 10, 555, t, null);
		Step s556 = new Step("step_varrados_556", "step", 42.7777065033012,
				0.835017318334268, 0, 10, 556, t);
		Step s557 = new Step("step_varrados_557", "step", 42.7777066188254,
				0.835023426315142, 0, 10, 557, t, null);
		Step s558 = new Step("step_varrados_558", "step", 42.7777299999833,
				0.835029999549526, 0, 10, 558, t, null);
		Step s559 = new Step("step_varrados_559", "step", 42.7777499999833,
				0.835039999549541, 0, 10, 559, t, null);
		Step s560 = new Step("step_varrados_560", "step", 42.7777399999834,
				0.835259999549861, 0, 10, 560, t, null);
		Step s561 = new Step("step_varrados_561", "step", 42.7777699999833,
				0.835199999549774, 0, 10, 561, t, null);
		Step s562 = new Step("step_varrados_562", "step", 42.7777799999833,
				0.835249999549846, 0, 10, 562, t, null);
		Step s563 = new Step("step_varrados_563", "step", 42.7777699999833,
				0.835329999549962, 0, 10, 563, t, null);
		Step s564 = new Step("step_varrados_564", "step", 42.7777999999834,
				0.83536999955002, 0, 10, 564, t, null);
		Step s565 = new Step("step_varrados_565", "step", 42.7778599999834,
				0.835399999550062, 0, 10, 565, t, null);
		Step s566 = new Step("step_varrados_566", "step", 42.7779099999834,
				0.835419999550091, 0, 10, 566, t, null);
		Step s567 = new Step("step_varrados_567", "step", 42.7780399999833,
				0.835309999549929, 0, 10, 567, t, null);
		Step s568 = new Step("step_varrados_568", "step", 42.7780899999834,
				0.835569999550307, 0, 10, 568, t, null);
		Step s569 = new Step("step_varrados_569", "step", 42.7780899999834,
				0.835879999550756, 0, 10, 569, t, null);
		Step s570 = new Step("step_varrados_570", "step", 42.7780699999834,
				0.835969999550888, 0, 10, 570, t, null);
		Step s571 = new Step("step_varrados_571", "step", 42.7780699999834,
				0.836049999551003, 0, 10, 571, t, null);
		Step s572 = new Step("step_varrados_572", "step", 42.7780499999834,
				0.836149999551148, 0, 10, 572, t, null);
		Step s573 = new Step("step_varrados_573", "step", 42.7779999999834,
				0.836369999551467, 0, 10, 573, t, null);
		Step s574 = new Step("step_varrados_574", "step", 42.7779599999834,
				0.836479999551628, 0, 10, 574, t, null);
		Step s575 = new Step("step_varrados_575", "step", 42.7779499999834,
				0.836649999551873, 0, 10, 575, t, null);
		Step s576 = new Step("step_varrados_576", "step", 42.7779499999834,
				0.836729999551989, 0, 10, 576, t, null);
		Step s577 = new Step("step_varrados_577", "step", 42.7779499999834,
				0.836729999551989, 0, 10, 577, t, null);
		Step s578 = new Step("step_varrados_578", "step", 42.7779399999834,
				0.836819999552119, 0, 10, 578, t, null);
		Step s579 = new Step("step_varrados_579", "step", 42.7778999999834,
				0.83688999955222, 0, 10, 579, t, null);
		Step s580 = new Step("step_varrados_580", "step", 42.7778499999834,
				0.836929999552279, 0, 10, 580, t, null);
		Step s581 = new Step("step_varrados_581", "step", 42.7779399999834,
				0.837039999552436, 0, 10, 581, t, null);
		Step s582 = new Step("step_varrados_582", "step", 42.7779499999834,
				0.837139999552582, 0, 10, 582, t, null);
		Step s583 = new Step("step_varrados_583", "step", 42.7779199999834,
				0.837219999552698, 0, 10, 583, t, null);
		Step s584 = new Step("step_varrados_584", "step", 42.7779099999834,
				0.837379999552927, 0, 10, 584, t, null);
		Step s585 = new Step("step_varrados_585", "step", 42.7779199999834,
				0.837449999553028, 0, 10, 585, t, null);
		Step s586 = new Step("step_varrados_586", "step", 42.7779199999834,
				0.837539999553159, 0, 10, 586, t, null);
		Step s587 = new Step("step_varrados_587", "step", 42.7779599999835,
				0.837599999553244, 0, 10, 587, t, null);
		Step s588 = new Step("step_varrados_588", "step", 42.7779199999835,
				0.837739999553447, 0, 10, 588, t, null);
		Step s589 = new Step("step_varrados_589", "step", 42.7778999999835,
				0.837799999553534, 0, 10, 589, t, null);
		Step s590 = new Step("step_varrados_590", "step", 42.7779999999835,
				0.838169999554065, 0, 10, 590, t, null);
		Step s591 = new Step("step_varrados_591", "step", 42.7780399999835,
				0.838249999554181, 0, 10, 591, t, null);
		Step s592 = new Step("step_varrados_592", "step", 42.7780599999835,
				0.838329999554295, 0, 10, 592, t, null);
		Step s593 = new Step("step_varrados_593", "step", 42.7780499999835,
				0.838489999554526, 0, 10, 593, t, null);
		Step s594 = new Step("step_varrados_594", "step", 42.7780299999835,
				0.838559999554625, 0, 10, 594, t, null);
		Step s595 = new Step("step_varrados_595", "step", 42.7780199999835,
				0.838689999554815, 0, 10, 595, t, null);
		Step s596 = new Step("step_varrados_596", "step", 42.7780299999835,
				0.838759999554914, 0, 10, 596, t, null);
		Step s597 = new Step("step_varrados_597", "step", 42.7780599999835,
				0.838829999555015, 0, 10, 597, t, null);
		Step s598 = new Step("step_varrados_598", "step", 42.7780299999835,
				0.83913999955546, 0, 10, 598, t, null);
		Step s599 = new Step("step_varrados_599", "step", 42.7780099999835,
				0.839239999555605, 0, 10, 599, t, null);
		Step s600 = new Step("step_varrados_600", "step", 42.7779899999835,
				0.839319999555719, 0, 10, 600, t, null);
		Step s601 = new Step("step_varrados_601", "step", 42.7780299999835,
				0.839399999555834, 0, 10, 601, t, null);
		Step s602 = new Step("step_varrados_602", "step", 42.7780499999835,
				0.839569999556078, 0, 10, 602, t, null);
		Step s603 = new Step("step_varrados_603", "step", 42.7780699999835,
				0.839649999556192, 0, 10, 603, t, null);
		Step s604 = new Step("step_varrados_604", "step", 42.7780799999835,
				0.839719999556292, 0, 10, 604, t, null);
		Step s605 = new Step("step_varrados_605", "step", 42.7780799999835,
				0.839979999556665, 0, 10, 605, t, null);
		Step s606 = new Step("step_varrados_606", "step", 42.7780699999836,
				0.840069999556794, 0, 10, 606, t, null);
		Step s607 = new Step("step_varrados_607", "step", 42.7780699999836,
				0.840189999556966, 0, 10, 607, t, null);
		Step s608 = new Step("step_varrados_608", "step", 42.7780599999836,
				0.84026999955708, 0, 10, 608, t, null);
		Step s609 = new Step("step_varrados_609", "step", 42.7779899999836,
				0.840569999557511, 0, 10, 609, t, null);
		Step s610 = new Step("step_varrados_610", "step", 42.7780299999836,
				0.840689999557681, 0, 10, 610, t, null);
		Step s611 = new Step("step_varrados_611", "step", 42.7780299999836,
				0.840769999557796, 0, 10, 611, t, null);
		Step s612 = new Step("step_varrados_612", "step", 42.7780399999836,
				0.840849999557911, 0, 10, 612, t, null);
		Step s613 = new Step("step_varrados_613", "step", 42.7780899999836,
				0.840969999558081, 0, 10, 613, t, null);
		Step s614 = new Step("step_varrados_614", "step", 42.7780799999836,
				0.841119999558296, 0, 10, 614, t, null);
		Step s615 = new Step("step_varrados_615", "step", 42.7780899999836,
				0.841259999558495, 0, 10, 615, t, null);
		Step s616 = new Step("step_varrados_616", "step", 42.7780999999836,
				0.841399999558695, 0, 10, 616, t, null);
		Step s617 = new Step("step_varrados_617", "step", 42.7781299999836,
				0.841459999558781, 0, 10, 617, t, null);
		Step s618 = new Step("step_varrados_618", "step", 42.7781599999836,
				0.841579999558951, 0, 10, 618, t, null);
		Step s619 = new Step("step_varrados_619", "step", 42.7781699999836,
				0.84166999955908, 0, 10, 619, t, null);
		Step s620 = new Step("step_varrados_620", "step", 42.7781999999836,
				0.841839999559322, 0, 10, 620, t, null);
		Step s621 = new Step("step_varrados_621", "step", 42.7781799999836,
				0.841969999559508, 0, 10, 621, t, null);
		Step s622 = new Step("step_varrados_622", "step", 42.7781399999836,
				0.842029999559593, 0, 10, 622, t, null);
		Step s623 = new Step("step_varrados_623", "step", 42.7780999999836,
				0.842119999559723, 0, 10, 623, t, null);
		Step s624 = new Step("step_varrados_624", "step", 42.7780999999836,
				0.842219999559864, 0, 10, 624, t, null);
		Step s625 = new Step("step_varrados_625", "step", 42.7781199999836,
				0.842289999559963, 0, 10, 625, t, null);
		Step s626 = new Step("step_varrados_626", "step", 42.7781699999836,
				0.84239999956012, 0, 10, 626, t, null);
		Step s627 = new Step("step_varrados_627", "step", 42.7781799999836,
				0.84246999956022, 0, 10, 627, t, null);
		Step s628 = new Step("step_varrados_628", "step", 42.7782199999837,
				0.842679999560517, 0, 10, 628, t, null);
		Step s629 = new Step("step_varrados_629", "step", 42.7782299999837,
				0.842769999560644, 0, 10, 629, t, null);
		Step s630 = new Step("step_varrados_630", "step", 42.7782699999837,
				0.842849999560758, 0, 10, 630, t, null);
		Step s631 = new Step("step_varrados_631", "step", 42.7783099999837,
				0.842919999560858, 0, 10, 631, t, null);
		Step s632 = new Step("step_varrados_632", "step", 42.7783299999837,
				0.842979999560943, 0, 10, 632, t, null);
		Step s633 = new Step("step_varrados_633", "step", 42.7783299999837,
				0.843009999560985, 0, 10, 633, t);
		Step s634 = new Step("step_varrados_634", "step", 42.7783099999837,
				0.843059999561057, 0, 10, 634, t, null);
		Step s635 = new Step("step_varrados_635", "step", 42.7782399999837,
				0.843029999561015, 0, 10, 635, t, null);
		Step s636 = new Step("step_varrados_636", "step", 42.7781799999837,
				0.843009999560988, 0, 10, 636, t, null);
		Step s637 = new Step("step_varrados_637", "step", 42.7781399999837,
				0.843019999561002, 0, 10, 637, t, null);
		Step s638 = new Step("step_varrados_638", "step", 42.7781799999837,
				0.843239999561314, 0, 10, 638, t, null);
		Step s639 = new Step("step_varrados_639", "step", 42.7781599999837,
				0.84334999956147, 0, 10, 639, t, null);
		Step s640 = new Step("step_varrados_640", "step", 42.7781499999837,
				0.843449999561613, 0, 10, 640, t, null);
		Step s641 = new Step("step_varrados_641", "step", 42.7781799999837,
				0.843589999561811, 0, 10, 641, t, null);
		Step s642 = new Step("step_varrados_642", "step", 42.7781799999837,
				0.84365999956191, 0, 10, 642, t, null);
		Step s643 = new Step("step_varrados_643", "step", 42.7782199999837,
				0.84377999956208, 0, 10, 643, t, null);
		Step s644 = new Step("step_varrados_644", "step", 42.7782599999837,
				0.843839999562164, 0, 10, 644, t, null);
		Step s645 = new Step("step_varrados_645", "step", 42.7783099999837,
				0.844049999562461, 0, 10, 645, t, null);
		Step s646 = new Step("step_varrados_646", "step", 42.7783099999837,
				0.844169999562632, 0, 10, 646, t, null);
		Step s647 = new Step("step_varrados_647", "step", 42.7782799999837,
				0.844299999562816, 0, 10, 647, t, null);
		Step s648 = new Step("step_varrados_648", "step", 42.7783299999837,
				0.844469999563056, 0, 10, 648, t, null);
		Step s649 = new Step("step_varrados_649", "step", 42.7783399999837,
				0.844549999563169, 0, 10, 649, t, null);
		Step s650 = new Step("step_varrados_650", "step", 42.7783099999837,
				0.844629999563282, 0, 10, 650, t, null);
		Step s651 = new Step("step_varrados_651", "step", 42.7783199999838,
				0.844779999563493, 0, 10, 651, t, null);
		Step s652 = new Step("step_varrados_652", "step", 42.7783299999837,
				0.844869999563622, 0, 10, 652, t, null);
		Step s653 = new Step("step_varrados_653", "step", 42.7783299999838,
				0.844989999563791, 0, 10, 653, t, null);
		Step s654 = new Step("step_varrados_654", "step", 42.7783499999838,
				0.845099999563946, 0, 10, 654, t, null);
		Step s655 = new Step("step_varrados_655", "step", 42.7783699999838,
				0.845139999564003, 0, 10, 655, t, null);
		Step s656 = new Step("step_varrados_656", "step", 42.7782199999837,
				0.845119999563976, 0, 10, 656, t, null);
		Step s657 = new Step("step_varrados_657", "step", 42.778193924612,
				0.845219303130673, 0, 10, 657, t);
		Step s658 = new Step("step_varrados_658", "step", 42.7781906144428,
				0.845276512469736, 0, 10, 658, t, null);
		Step s659 = new Step("step_varrados_659", "step", 42.7781999999838,
				0.845309999564245, 0, 10, 659, t, null);
		Step s660 = new Step("step_varrados_660", "step", 42.7782299999838,
				0.845409999564385, 0, 10, 660, t, null);
		Step s661 = new Step("step_varrados_661", "step", 42.7782899999838,
				0.84546999956447, 0, 10, 661, t, null);
		Step s662 = new Step("step_varrados_662", "step", 42.7783299999838,
				0.845589999564637, 0, 10, 662, t, null);
		Step s663 = new Step("step_varrados_663", "step", 42.7783199999838,
				0.845659999564736, 0, 10, 663, t, null);
		Step s664 = new Step("step_varrados_664", "step", 42.7782799999838,
				0.845719999564823, 0, 10, 664, t, null);
		Step s665 = new Step("step_varrados_665", "step", 42.7782799999838,
				0.845809999564949, 0, 10, 665, t, null);
		Step s666 = new Step("step_varrados_666", "step", 42.7782299999838,
				0.845889999565062, 0, 10, 666, t, null);
		Step s667 = new Step("step_varrados_667", "step", 42.7781399999838,
				0.846029999565261, 0, 10, 667, t, null);
		Step s668 = new Step("step_varrados_668", "step", 42.7781299999838,
				0.846099999565359, 0, 10, 668, t, null);
		Step s669 = new Step("step_varrados_669", "step", 42.7781099999838,
				0.8461999995655, 0, 10, 669, t, null);
		Step s670 = new Step("step_varrados_670", "step", 42.7780699999838,
				0.846309999565656, 0, 10, 670, t, null);
		Step s671 = new Step("step_varrados_671", "step", 42.7780399999838,
				0.84636999956574, 0, 10, 671, t, null);
		Step s672 = new Step("step_varrados_672", "step", 42.7779999999838,
				0.846419999565811, 0, 10, 672, t, null);
		Step s673 = new Step("step_varrados_673", "step", 42.7779799999838,
				0.846489999565911, 0, 10, 673, t, null);
		Step s674 = new Step("step_varrados_674", "step", 42.7778999999838,
				0.846539999565981, 0, 10, 674, t, null);
		Step s675 = new Step("step_varrados_675", "step", 42.7778499999838,
				0.846589999566052, 0, 10, 675, t, null);
		Step s676 = new Step("step_varrados_676", "step", 42.7778499999838,
				0.846659999566151, 0, 10, 676, t, null);
		Step s677 = new Step("step_varrados_677", "step", 42.7778199999838,
				0.846779999566321, 0, 10, 677, t, null);
		Step s678 = new Step("step_varrados_678", "step", 42.7778099999838,
				0.846909999566503, 0, 10, 678, t, null);
		Step s679 = new Step("step_varrados_679", "step", 42.7777799999838,
				0.846989999566615, 0, 10, 679, t, null);
		Step s680 = new Step("step_varrados_680", "step", 42.7777899999839,
				0.847079999566741, 0, 10, 680, t);
		Step s681 = new Step("step_varrados_681", "step", 42.7777743908828,
				0.847330664240011, 0, 10, 681, t, null);
		Step s682 = new Step("step_varrados_682", "step", 42.7777662906937,
				0.847618209753621, 0, 10, 682, t, null);
		Step s683 = new Step("step_varrados_683", "step", 42.7778267163819,
				0.847958389282735, 0, 10, 683, t, null);
		Step s684 = new Step("step_varrados_684", "step", 42.77796005223,
				0.848344940200717, 0, 10, 684, t, null);
		Step s685 = new Step("step_varrados_685", "step", 42.7781883363357,
				0.848752652692298, 0, 10, 685, t, null);
		Step s686 = new Step("step_varrados_686", "step", 42.7784078495049,
				0.849172896023431, 0, 10, 686, t, null);
		Step s687 = new Step("step_varrados_687", "step", 42.7785863155844,
				0.849325630997836, 0, 10, 687, t, null);
		Step s688 = new Step("step_varrados_688", "step", 42.7788581001006,
				0.849652387636669, 0, 10, 688, t, null);
		Step s689 = new Step("step_varrados_689", "step", 42.7790680623843,
				0.849804034759551, 0, 10, 689, t, null);
		Step s690 = new Step("step_varrados_690", "step", 42.7792129656676,
				0.849847915968234, 0, 10, 690, t, null);
		Step s691 = new Step("step_varrados_691", "step", 42.779295448997,
				0.84992451890576, 0, 10, 691, t, null);
		Step s692 = new Step("step_varrados_692", "step", 42.779505181498,
				0.850063951535098, 0, 10, 692, t, null);
		Step s693 = new Step("step_varrados_693", "step", 42.7796262995609,
				0.850279798513893, 0, 10, 693, t, null);
		Step s694 = new Step("step_varrados_694", "step", 42.7796808416743,
				0.850546847921519, 0, 10, 694, t, null);
		Step s695 = new Step("step_varrados_695", "step", 42.7797702326602,
				0.850751569834354, 0, 10, 695, t, null);
		Step s696 = new Step("step_varrados_696", "step", 42.7799053068193,
				0.850991385114767, 0, 10, 696, t, null);
		Step s697 = new Step("step_varrados_697", "step", 42.780057576022,
				0.851187820863933, 0, 10, 697, t, null);
		Step s698 = new Step("step_varrados_698", "step", 42.7800969840978,
				0.85160820262647, 0, 10, 698, t, null);
		Step s699 = new Step("step_varrados_699", "step", 42.7800902778279,
				0.851730679534439, 0, 10, 699, t, null);
		Step s700 = new Step("step_varrados_700", "step", 42.7801930507754,
				0.851928829438331, 0, 10, 700, t, null);
		Step s701 = new Step("step_varrados_701", "step", 42.7802027082405,
				0.852203547081022, 0, 10, 701, t, null);
		Step s702 = new Step("step_varrados_702", "step", 42.7802428304992,
				0.852422201132553, 0, 10, 702, t, null);
		Step s703 = new Step("step_varrados_703", "step", 42.7803490704996,
				0.852565222342275, 0, 10, 703, t, null);
		Step s704 = new Step("step_varrados_704", "step", 42.7805039724491,
				0.852902154948675, 0, 10, 704, t, null);
		Step s705 = new Step("step_varrados_705", "step", 42.7806143361502,
				0.853265077805767, 0, 10, 705, t, null);
		Step s706 = new Step("step_varrados_706", "step", 42.7807388024506,
				0.853419696008476, 0, 10, 706, t, null);
		Step s707 = new Step("step_varrados_707", "step", 42.7809600499328,
				0.853693216922956, 0, 10, 707, t, null);
		Step s708 = new Step("step_varrados_708", "step", 42.7811599442205,
				0.8540286010906, 0, 10, 708, t, null);
		Step s709 = new Step("step_varrados_709", "step", 42.7813090487819,
				0.854536888283475, 0, 10, 709, t, null);
		Step s710 = new Step("step_varrados_710", "step", 42.7813740684705,
				0.854883047663739, 0, 10, 710, t, null);
		Step s711 = new Step("step_varrados_711", "step", 42.7815447873424,
				0.855103309921731, 0, 10, 711, t, null);
		Step s712 = new Step("step_varrados_712", "step", 42.7816968208779,
				0.855287544154884, 0, 10, 712, t, null);
		Step s713 = new Step("step_varrados_713", "step", 42.7818153753182,
				0.855607408470042, 0, 10, 713, t, null);
		Step s714 = new Step("step_varrados_714", "step", 42.7819559690084,
				0.855902063448126, 0, 10, 714, t, null);
		Step s715 = new Step("step_varrados_715", "step", 42.7820725768879,
				0.856118085904114, 0, 10, 715, t, null);
		Step s716 = new Step("step_varrados_716", "step", 42.7821616150266,
				0.85630449847388, 0, 10, 716, t, null);
		Step s717 = new Step("step_varrados_717", "step", 42.7821608517515,
				0.856744621422754, 0, 10, 717, t, null);
		Step s718 = new Step("step_varrados_718", "step", 42.7822677332258,
				0.857162693076775, 0, 10, 718, t, null);
		Step s719 = new Step("step_varrados_719", "step", 42.7820915877997,
				0.85833624765761, 0, 10, 719, t, null);
		Step s720 = new Step("step_varrados_720", "step", 42.781906405239,
				0.859027225215981, 0, 10, 720, t, null);
		Step s721 = new Step("step_varrados_721", "step", 42.7817128130731,
				0.860231930867915, 0, 10, 721, t);
		Step s722 = new Step("step_varrados_722", "step", 42.781705581511,
				0.861772507172661, 0, 10, 722, t, null);
		Step s723 = new Step("step_varrados_723", "step", 42.78218558005,
				0.862642288379833, 0, 10, 723, t, null);
		Step s724 = new Step("step_varrados_724", "step", 42.7823879129296,
				0.863833370920577, 0, 10, 724, t, null);
		Step s725 = new Step("step_varrados_725", "step", 42.7826355686355,
				0.864558353109902, 0, 10, 725, t, null);
		Step s726 = new Step("step_varrados_726", "step", 42.7828043906573,
				0.864919299584165, 0, 10, 726, t, null);
		Step s727 = new Step("step_varrados_727", "step", 42.7828968889682,
				0.865050595330602, 0, 10, 727, t, null);
		Step s728 = new Step("step_varrados_728", "step", 42.7831021046226,
				0.865190242059001, 0, 10, 728, t, null);
		Step s729 = new Step("step_varrados_729", "step", 42.7832552616738,
				0.865435592593108, 0, 10, 729, t, null);
		Step s730 = new Step("step_varrados_730", "step", 42.7833616562951,
				0.865829253137265, 0, 10, 730, t, null);
		Step s731 = new Step("step_varrados_731", "step", 42.7836339649767,
				0.866186651642873, 0, 10, 731, t, null);
		Step s732 = new Step("step_varrados_732", "step", 42.7838946546472,
				0.866403861972128, 0, 10, 732, t, null);
		Step s733 = new Step("step_varrados_733", "step", 42.7840763461002,
				0.866489309620539, 0, 10, 733, t, null);
		Step s734 = new Step("step_varrados_734", "step", 42.7841837617688,
				0.866937955973928, 0, 10, 734, t, null);
		Step s735 = new Step("step_varrados_735", "step", 42.7842236818478,
				0.867388921647377, 0, 10, 735, t, null);
		Step s736 = new Step("step_varrados_736", "step", 42.7844630614356,
				0.867912504872536, 0, 10, 736, t, null);
		Step s737 = new Step("step_varrados_737", "step", 42.7846410487183,
				0.868040873731418, 0, 10, 737, t, null);
		Step s738 = new Step("step_varrados_738", "step", 42.7848566878145,
				0.868015132759172, 0, 10, 738, t);
		Step s739 = new Step("step_varrados_739", "step", 42.7850416881734,
				0.867794835741044, 0, 10, 739, t, null);
		Step s740 = new Step("step_varrados_740", "step", 42.7851526461063,
				0.867467050307945, 0, 10, 740, t, null);
		Step s741 = new Step("step_varrados_741", "step", 42.7853719346968,
				0.867153881422759, 0, 10, 741, t, null);
		Step s742 = new Step("step_varrados_742", "step", 42.7855315892755,
				0.866781630835407, 0, 10, 742, t, null);
		Step s743 = new Step("step_varrados_743", "step", 42.7857749128277,
				0.866308698710837, 0, 10, 743, t, null);
		Step s744 = new Step("step_varrados_744", "step", 42.7860111713883,
				0.865939923846105, 0, 10, 744, t, null);
		Step s745 = new Step("step_varrados_745", "step", 42.7863172576237,
				0.865452601641779, 0, 10, 745, t, null);
		Step s746 = new Step("step_varrados_746", "step", 42.7867065547263,
				0.864840155601482, 0, 10, 746, t, null);
		Step s747 = new Step("step_varrados_747", "step", 42.7871232672078,
				0.864258321216513, 0, 10, 747, t, null);
		Step s748 = new Step("step_varrados_748", "step", 42.7872250751744,
				0.863673091419051, 0, 10, 748, t, null);
		Step s749 = new Step("step_varrados_749", "step", 42.7870660357844,
				0.862871654968839, 0, 10, 749, t, null);
		Step s750 = new Step("step_varrados_750", "step", 42.7872323293287,
				0.862132377312593, 0, 10, 750, t, null);
		Step s751 = new Step("step_varrados_751", "step", 42.7876293996571,
				0.861213983917439, 0, 10, 751, t, null);
		Step s752 = new Step("step_varrados_752", "step", 42.7879096092201,
				0.860788647165056, 0, 10, 752, t, null);
		Step s753 = new Step("step_varrados_753", "step", 42.7880638292242,
				0.860367647793654, 0, 10, 753, t, null);
		Step s754 = new Step("step_varrados_754", "step", 42.7882180476298,
				0.859946646377445, 0, 10, 754, t, null);
		Step s755 = new Step("step_varrados_755", "step", 42.7883633092681,
				0.859287542080679, 0, 10, 755, t, null);
		Step s756 = new Step("step_varrados_756", "step", 42.788572432533,
				0.858913547391222, 0, 10, 756, t, null);
		Step s757 = new Step("step_varrados_757", "step", 42.7886293386215,
				0.85810465720313, 0, 10, 757, t, null);
		Step s758 = new Step("step_varrados_758", "step", 42.7887173927825,
				0.857276351421657, 0, 10, 758, t, null);
		Step s759 = new Step("step_varrados_759", "step", 42.7889440499654,
				0.856877290152976, 0, 10, 759, t, null);
		Step s760 = new Step("step_varrados_760", "step", 42.7892898283524,
				0.856107324400259, 0, 10, 760, t, null);
		Step s761 = new Step("step_varrados_761", "step", 42.7895346201181,
				0.854753972925662, 0, 10, 761, t, null);
		Step s762 = new Step("step_varrados_762", "step", 42.7896621146797,
				0.853869268488976, 0, 10, 762, t, null);
		Step s763 = new Step("step_varrados_763", "step", 42.7903876672934,
				0.853183957719014, 0, 10, 763, t, null);
		Step s764 = new Step("step_varrados_764", "step", 42.7908493188203,
				0.851871974290023, 0, 10, 764, t, null);
		Step s765 = new Step("step_varrados_765", "step", 42.7912873468876,
				0.851221030950311, 0, 10, 765, t, null);
		Step s766 = new Step("step_varrados_766", "step", 42.7916358392038,
				0.850597631536814, 0, 10, 766, t);
		Step s767 = new Step("step_varrados_767", "step", 42.79136392681,
				0.851462914946149, 0, 10, 767, t, null);
		Step s768 = new Step("step_varrados_768", "step", 42.7912567422093,
				0.852469213065517, 0, 10, 768, t, null);
		Step s769 = new Step("step_varrados_769", "step", 42.791381691836,
				0.853369668572236, 0, 10, 769, t, null);
		Step s770 = new Step("step_varrados_770", "step", 42.7914535560984,
				0.854320869293166, 0, 10, 770, t, null);
		Step s771 = new Step("step_varrados_771", "step", 42.791472928553,
				0.854393560241215, 0, 10, 771, t, null);
		Step s772 = new Step("step_varrados_772", "step", 42.7918396232009,
				0.855701383738248, 0, 10, 772, t, null);
		Step s773 = new Step("step_varrados_773", "step", 42.791943346071,
				0.856431411452408, 0, 10, 773, t, null);
		Step s774 = new Step("step_varrados_774", "step", 42.7919630202251,
				0.857482240247933, 0, 10, 774, t, null);
		Step s775 = new Step("step_varrados_775", "step", 42.7919254895003,
				0.858363867209495, 0, 10, 775, t, null);
		Step s776 = new Step("step_varrados_776", "step", 42.7918312148422,
				0.859100727909447, 0, 10, 776, t, null);
		Step s777 = new Step("step_varrados_777", "step", 42.7910550126868,
				0.860209552007539, 0, 10, 777, t, null);
		Step s778 = new Step("step_varrados_778", "step", 42.790584132224,
				0.861032733463832, 0, 10, 778, t, null);
		Step s779 = new Step("step_varrados_779", "step", 42.79064021665,
				0.862106740412443, 0, 10, 779, t, null);
		Step s780 = new Step("step_varrados_780", "step", 42.791208660015,
				0.862649595159584, 0, 10, 780, t, null);
		Step s781 = new Step("step_varrados_781", "step", 42.7919769069564,
				0.863283397903733, 0, 10, 781, t, null);
		Step s782 = new Step("step_varrados_782", "step", 42.7924644741081,
				0.864318135009438, 0, 10, 782, t, null);
		Step s783 = new Step("step_varrados_783", "step", 42.7928173837804,
				0.864892891734061, 0, 10, 783, t, null);
		Step s784 = new Step("step_varrados_784", "step", 42.79314562801,
				0.865352345737581, 0, 10, 784, t, null);
		Step s785 = new Step("step_varrados_785", "step", 42.7935677920574,
				0.865778008049059, 0, 10, 785, t, null);
		Step s786 = new Step("step_varrados_786", "step", 42.7935440965955,
				0.866439089002562, 0, 10, 786, t, null);
		Step s787 = new Step("step_varrados_787", "step", 42.7934290394676,
				0.867029945423603, 0, 10, 787, t, null);
		Step s788 = new Step("step_varrados_788", "step", 42.793441333406,
				0.86768978837989, 0, 10, 788, t);
		Step s789 = new Step("step_varrados_789", "step", 42.793218734803,
				0.86830878825355, 0, 10, 789, t, null);
		Step s790 = new Step("step_varrados_790", "step", 42.7930063954668,
				0.868511711790476, 0, 10, 790, t, null);
		Step s791 = new Step("step_varrados_791", "step", 42.7928384055643,
				0.869403937978139, 0, 10, 791, t, null);
		Step s792 = new Step("step_varrados_792", "step", 42.7926315219247,
				0.869900116736355, 0, 10, 792, t, null);
		Step s793 = new Step("step_varrados_793", "step", 42.7924870802089,
				0.869880618482486, 0, 10, 793, t, null);
		Step s794 = new Step("step_varrados_794", "step", 42.7924521814484,
				0.870908879012247, 0, 10, 794, t, null);
		Step s795 = new Step("step_varrados_795", "step", 42.7921197568489,
				0.871433807479138, 0, 10, 795, t, null);
		Step s796 = new Step("step_varrados_796", "step", 42.7917059521334,
				0.871698643425879, 0, 10, 796, t, null);
		Step s797 = new Step("step_varrados_797", "step", 42.7912448091214,
				0.872081251914902, 0, 10, 797, t, null);
		Step s798 = new Step("step_varrados_798", "step", 42.7913426794132,
				0.872744254192972, 0, 10, 798, t, null);
		Step s799 = new Step("step_varrados_799", "step", 42.7916259779579,
				0.873694350187148, 0, 10, 799, t, null);
		Step s800 = new Step("step_varrados_800", "step", 42.7914903133744,
				0.874389807909378, 0, 10, 800, t, null);
		Step s801 = new Step("step_varrados_801", "step", 42.7913977494549,
				0.875224395008848, 0, 10, 801, t, null);
		Step s802 = new Step("step_varrados_802", "step", 42.7913298382217,
				0.875446800087101, 0, 10, 802, t, null);
		Step s803 = new Step("step_varrados_803", "step", 42.7907169842163,
				0.875418864040742, 0, 10, 803, t, null);
		Step s804 = new Step("step_varrados_804", "step", 42.7903601904137,
				0.875602244099836, 0, 10, 804, t, null);
		Step s805 = new Step("step_varrados_805", "step", 42.7900381812964,
				0.875961715764307, 0, 10, 805, t, null);
		Step s806 = new Step("step_varrados_806", "step", 42.789655827681,
				0.875980908479342, 0, 10, 806, t, null);
		Step s807 = new Step("step_varrados_807", "step", 42.7888787201491,
				0.875836323453298, 0, 10, 807, t, null);
		Step s808 = new Step("step_varrados_808", "step", 42.7885695720514,
				0.875675732448398, 0, 10, 808, t, null);
		Step s809 = new Step("step_varrados_809", "step", 42.7882505946022,
				0.875228165516202, 0, 10, 809, t, null);
		Step s810 = new Step("step_varrados_810", "step", 42.7876389516565,
				0.874295466603189, 0, 10, 810, t, null);
		Step s811 = new Step("step_varrados_811", "step", 42.7871015559418,
				0.872755061633696, 0, 10, 811, t, null);
		Step s812 = new Step("step_varrados_812", "step", 42.7868223213976,
				0.871297527062438, 0, 10, 812, t, null);
		Step s813 = new Step("step_varrados_813", "step", 42.7864730768667,
				0.87085325702052, 0, 10, 813, t, null);
		Step s814 = new Step("step_varrados_814", "step", 42.7862369824473,
				0.870290270242631, 0, 10, 814, t, null);
		Step s815 = new Step("step_varrados_815", "step", 42.7859857204915,
				0.86979761608405, 0, 10, 815, t, null);
		Step s816 = new Step("step_varrados_816", "step", 42.7855346813597,
				0.869335926723184, 0, 10, 816, t, null);
		Step s817 = new Step("step_varrados_817", "step", 42.7850673239389,
				0.868634820051698, 0, 10, 817, t, null);
		Step s818 = new Step("step_varrados_818", "step", 42.7848584865535,
				0.868045418969845, 0, 10, 818, t, null);

		try {
			stepDataDao.create(s1);
			stepDataDao.create(s2);
			stepDataDao.create(s3);
			stepDataDao.create(s4);
			stepDataDao.create(s5);
			stepDataDao.create(s6);
			stepDataDao.create(s7);
			stepDataDao.create(s8);
			stepDataDao.create(s9);
			stepDataDao.create(s10);
			stepDataDao.create(s11);
			stepDataDao.create(s12);
			stepDataDao.create(s13);
			stepDataDao.create(s14);
			stepDataDao.create(s15);
			stepDataDao.create(s16);
			stepDataDao.create(s17);
			stepDataDao.create(s18);
			stepDataDao.create(s19);
			stepDataDao.create(s20);
			stepDataDao.create(s21);
			stepDataDao.create(s22);
			stepDataDao.create(s23);
			stepDataDao.create(s24);
			stepDataDao.create(s25);
			stepDataDao.create(s26);
			stepDataDao.create(s27);
			stepDataDao.create(s28);
			stepDataDao.create(s29);
			stepDataDao.create(s30);
			stepDataDao.create(s31);
			stepDataDao.create(s32);
			stepDataDao.create(s33);
			stepDataDao.create(s34);
			stepDataDao.create(s35);
			stepDataDao.create(s36);
			stepDataDao.create(s37);
			stepDataDao.create(s38);
			stepDataDao.create(s39);
			stepDataDao.create(s40);
			stepDataDao.create(s41);
			stepDataDao.create(s42);
			stepDataDao.create(s43);
			stepDataDao.create(s44);
			stepDataDao.create(s45);
			stepDataDao.create(s46);
			stepDataDao.create(s47);
			stepDataDao.create(s48);
			stepDataDao.create(s49);
			stepDataDao.create(s50);
			stepDataDao.create(s51);
			stepDataDao.create(s52);
			stepDataDao.create(s53);
			stepDataDao.create(s54);
			stepDataDao.create(s55);
			stepDataDao.create(s56);
			stepDataDao.create(s57);
			stepDataDao.create(s58);
			stepDataDao.create(s59);
			stepDataDao.create(s60);
			stepDataDao.create(s61);
			stepDataDao.create(s62);
			stepDataDao.create(s63);
			stepDataDao.create(s64);
			stepDataDao.create(s65);
			stepDataDao.create(s66);
			stepDataDao.create(s67);
			stepDataDao.create(s68);
			stepDataDao.create(s69);
			stepDataDao.create(s70);
			stepDataDao.create(s71);
			stepDataDao.create(s72);
			stepDataDao.create(s73);
			stepDataDao.create(s74);
			stepDataDao.create(s75);
			stepDataDao.create(s76);
			stepDataDao.create(s77);
			stepDataDao.create(s78);
			stepDataDao.create(s79);
			stepDataDao.create(s80);
			stepDataDao.create(s81);
			stepDataDao.create(s82);
			stepDataDao.create(s83);
			stepDataDao.create(s84);
			stepDataDao.create(s85);
			stepDataDao.create(s86);
			stepDataDao.create(s87);
			stepDataDao.create(s88);
			stepDataDao.create(s89);
			stepDataDao.create(s90);
			stepDataDao.create(s91);
			stepDataDao.create(s92);
			stepDataDao.create(s93);
			stepDataDao.create(s94);
			stepDataDao.create(s95);
			stepDataDao.create(s96);
			stepDataDao.create(s97);
			stepDataDao.create(s98);
			stepDataDao.create(s99);
			stepDataDao.create(s100);
			stepDataDao.create(s101);
			stepDataDao.create(s102);
			stepDataDao.create(s103);
			stepDataDao.create(s104);
			stepDataDao.create(s105);
			stepDataDao.create(s106);
			stepDataDao.create(s107);
			stepDataDao.create(s108);
			stepDataDao.create(s109);
			stepDataDao.create(s110);
			stepDataDao.create(s111);
			stepDataDao.create(s112);
			stepDataDao.create(s113);
			stepDataDao.create(s114);
			stepDataDao.create(s115);
			stepDataDao.create(s116);
			stepDataDao.create(s117);
			stepDataDao.create(s118);
			stepDataDao.create(s119);
			stepDataDao.create(s120);
			stepDataDao.create(s121);
			stepDataDao.create(s122);
			stepDataDao.create(s123);
			stepDataDao.create(s124);
			stepDataDao.create(s125);
			stepDataDao.create(s126);
			stepDataDao.create(s127);
			stepDataDao.create(s128);
			stepDataDao.create(s129);
			stepDataDao.create(s130);
			stepDataDao.create(s131);
			stepDataDao.create(s132);
			stepDataDao.create(s133);
			stepDataDao.create(s134);
			stepDataDao.create(s135);
			stepDataDao.create(s136);
			stepDataDao.create(s137);
			stepDataDao.create(s138);
			stepDataDao.create(s139);
			stepDataDao.create(s140);
			stepDataDao.create(s141);
			stepDataDao.create(s142);
			stepDataDao.create(s143);
			stepDataDao.create(s144);
			stepDataDao.create(s145);
			stepDataDao.create(s146);
			stepDataDao.create(s147);
			stepDataDao.create(s148);
			stepDataDao.create(s149);
			stepDataDao.create(s150);
			stepDataDao.create(s151);
			stepDataDao.create(s152);
			stepDataDao.create(s153);
			stepDataDao.create(s154);
			stepDataDao.create(s155);
			stepDataDao.create(s156);
			stepDataDao.create(s157);
			stepDataDao.create(s158);
			stepDataDao.create(s159);
			stepDataDao.create(s160);
			stepDataDao.create(s161);
			stepDataDao.create(s162);
			stepDataDao.create(s163);
			stepDataDao.create(s164);
			stepDataDao.create(s165);
			stepDataDao.create(s166);
			stepDataDao.create(s167);
			stepDataDao.create(s168);
			stepDataDao.create(s169);
			stepDataDao.create(s170);
			stepDataDao.create(s171);
			stepDataDao.create(s172);
			stepDataDao.create(s173);
			stepDataDao.create(s174);
			stepDataDao.create(s175);
			stepDataDao.create(s176);
			stepDataDao.create(s177);
			stepDataDao.create(s178);
			stepDataDao.create(s179);
			stepDataDao.create(s180);
			stepDataDao.create(s181);
			stepDataDao.create(s182);
			stepDataDao.create(s183);
			stepDataDao.create(s184);
			stepDataDao.create(s185);
			stepDataDao.create(s186);
			stepDataDao.create(s187);
			stepDataDao.create(s188);
			stepDataDao.create(s189);
			stepDataDao.create(s190);
			stepDataDao.create(s191);
			stepDataDao.create(s192);
			stepDataDao.create(s193);
			stepDataDao.create(s194);
			stepDataDao.create(s195);
			stepDataDao.create(s196);
			stepDataDao.create(s197);
			stepDataDao.create(s198);
			stepDataDao.create(s199);
			stepDataDao.create(s200);
			stepDataDao.create(s201);
			stepDataDao.create(s202);
			stepDataDao.create(s203);
			stepDataDao.create(s204);
			stepDataDao.create(s205);
			stepDataDao.create(s206);
			stepDataDao.create(s207);
			stepDataDao.create(s208);
			stepDataDao.create(s209);
			stepDataDao.create(s210);
			stepDataDao.create(s211);
			stepDataDao.create(s212);
			stepDataDao.create(s213);
			stepDataDao.create(s214);
			stepDataDao.create(s215);
			stepDataDao.create(s216);
			stepDataDao.create(s217);
			stepDataDao.create(s218);
			stepDataDao.create(s219);
			stepDataDao.create(s220);
			stepDataDao.create(s221);
			stepDataDao.create(s222);
			stepDataDao.create(s223);
			stepDataDao.create(s224);
			stepDataDao.create(s225);
			stepDataDao.create(s226);
			stepDataDao.create(s227);
			stepDataDao.create(s228);
			stepDataDao.create(s229);
			stepDataDao.create(s230);
			stepDataDao.create(s231);
			stepDataDao.create(s232);
			stepDataDao.create(s233);
			stepDataDao.create(s234);
			stepDataDao.create(s235);
			stepDataDao.create(s236);
			stepDataDao.create(s237);
			stepDataDao.create(s238);
			stepDataDao.create(s239);
			stepDataDao.create(s240);
			stepDataDao.create(s241);
			stepDataDao.create(s242);
			stepDataDao.create(s243);
			stepDataDao.create(s244);
			stepDataDao.create(s245);
			stepDataDao.create(s246);
			stepDataDao.create(s247);
			stepDataDao.create(s248);
			stepDataDao.create(s249);
			stepDataDao.create(s250);
			stepDataDao.create(s251);
			stepDataDao.create(s252);
			stepDataDao.create(s253);
			stepDataDao.create(s254);
			stepDataDao.create(s255);
			stepDataDao.create(s256);
			stepDataDao.create(s257);
			stepDataDao.create(s258);
			stepDataDao.create(s259);
			stepDataDao.create(s260);
			stepDataDao.create(s261);
			stepDataDao.create(s262);
			stepDataDao.create(s263);
			stepDataDao.create(s264);
			stepDataDao.create(s265);
			stepDataDao.create(s266);
			stepDataDao.create(s267);
			stepDataDao.create(s268);
			stepDataDao.create(s269);
			stepDataDao.create(s270);
			stepDataDao.create(s271);
			stepDataDao.create(s272);
			stepDataDao.create(s273);
			stepDataDao.create(s274);
			stepDataDao.create(s275);
			stepDataDao.create(s276);
			stepDataDao.create(s277);
			stepDataDao.create(s278);
			stepDataDao.create(s279);
			stepDataDao.create(s280);
			stepDataDao.create(s281);
			stepDataDao.create(s282);
			stepDataDao.create(s283);
			stepDataDao.create(s284);
			stepDataDao.create(s285);
			stepDataDao.create(s286);
			stepDataDao.create(s287);
			stepDataDao.create(s288);
			stepDataDao.create(s289);
			stepDataDao.create(s290);
			stepDataDao.create(s291);
			stepDataDao.create(s292);
			stepDataDao.create(s293);
			stepDataDao.create(s294);
			stepDataDao.create(s295);
			stepDataDao.create(s296);
			stepDataDao.create(s297);
			stepDataDao.create(s298);
			stepDataDao.create(s299);
			stepDataDao.create(s300);
			stepDataDao.create(s301);
			stepDataDao.create(s302);
			stepDataDao.create(s303);
			stepDataDao.create(s304);
			stepDataDao.create(s305);
			stepDataDao.create(s306);
			stepDataDao.create(s307);
			stepDataDao.create(s308);
			stepDataDao.create(s309);
			stepDataDao.create(s310);
			stepDataDao.create(s311);
			stepDataDao.create(s312);
			stepDataDao.create(s313);
			stepDataDao.create(s314);
			stepDataDao.create(s315);
			stepDataDao.create(s316);
			stepDataDao.create(s317);
			stepDataDao.create(s318);
			stepDataDao.create(s319);
			stepDataDao.create(s320);
			stepDataDao.create(s321);
			stepDataDao.create(s322);
			stepDataDao.create(s323);
			stepDataDao.create(s324);
			stepDataDao.create(s325);
			stepDataDao.create(s326);
			stepDataDao.create(s327);
			stepDataDao.create(s328);
			stepDataDao.create(s329);
			stepDataDao.create(s330);
			stepDataDao.create(s331);
			stepDataDao.create(s332);
			stepDataDao.create(s333);
			stepDataDao.create(s334);
			stepDataDao.create(s335);
			stepDataDao.create(s336);
			stepDataDao.create(s337);
			stepDataDao.create(s338);
			stepDataDao.create(s339);
			stepDataDao.create(s340);
			stepDataDao.create(s341);
			stepDataDao.create(s342);
			stepDataDao.create(s343);
			stepDataDao.create(s344);
			stepDataDao.create(s345);
			stepDataDao.create(s346);
			stepDataDao.create(s347);
			stepDataDao.create(s348);
			stepDataDao.create(s349);
			stepDataDao.create(s350);
			stepDataDao.create(s351);
			stepDataDao.create(s352);
			stepDataDao.create(s353);
			stepDataDao.create(s354);
			stepDataDao.create(s355);
			stepDataDao.create(s356);
			stepDataDao.create(s357);
			stepDataDao.create(s358);
			stepDataDao.create(s359);
			stepDataDao.create(s360);
			stepDataDao.create(s361);
			stepDataDao.create(s362);
			stepDataDao.create(s363);
			stepDataDao.create(s364);
			stepDataDao.create(s365);
			stepDataDao.create(s366);
			stepDataDao.create(s367);
			stepDataDao.create(s368);
			stepDataDao.create(s369);
			stepDataDao.create(s370);
			stepDataDao.create(s371);
			stepDataDao.create(s372);
			stepDataDao.create(s373);
			stepDataDao.create(s374);
			stepDataDao.create(s375);
			stepDataDao.create(s376);
			stepDataDao.create(s377);
			stepDataDao.create(s378);
			stepDataDao.create(s379);
			stepDataDao.create(s380);
			stepDataDao.create(s381);
			stepDataDao.create(s382);
			stepDataDao.create(s383);
			stepDataDao.create(s384);
			stepDataDao.create(s385);
			stepDataDao.create(s386);
			stepDataDao.create(s387);
			stepDataDao.create(s388);
			stepDataDao.create(s389);
			stepDataDao.create(s390);
			stepDataDao.create(s391);
			stepDataDao.create(s392);
			stepDataDao.create(s393);
			stepDataDao.create(s394);
			stepDataDao.create(s395);
			stepDataDao.create(s396);
			stepDataDao.create(s397);
			stepDataDao.create(s398);
			stepDataDao.create(s399);
			stepDataDao.create(s400);
			stepDataDao.create(s401);
			stepDataDao.create(s402);
			stepDataDao.create(s403);
			stepDataDao.create(s404);
			stepDataDao.create(s405);
			stepDataDao.create(s406);
			stepDataDao.create(s407);
			stepDataDao.create(s408);
			stepDataDao.create(s409);
			stepDataDao.create(s410);
			stepDataDao.create(s411);
			stepDataDao.create(s412);
			stepDataDao.create(s413);
			stepDataDao.create(s414);
			stepDataDao.create(s415);
			stepDataDao.create(s416);
			stepDataDao.create(s417);
			stepDataDao.create(s418);
			stepDataDao.create(s419);
			stepDataDao.create(s420);
			stepDataDao.create(s421);
			stepDataDao.create(s422);
			stepDataDao.create(s423);
			stepDataDao.create(s424);
			stepDataDao.create(s425);
			stepDataDao.create(s426);
			stepDataDao.create(s427);
			stepDataDao.create(s428);
			stepDataDao.create(s429);
			stepDataDao.create(s430);
			stepDataDao.create(s431);
			stepDataDao.create(s432);
			stepDataDao.create(s433);
			stepDataDao.create(s434);
			stepDataDao.create(s435);
			stepDataDao.create(s436);
			stepDataDao.create(s437);
			stepDataDao.create(s438);
			stepDataDao.create(s439);
			stepDataDao.create(s440);
			stepDataDao.create(s441);
			stepDataDao.create(s442);
			stepDataDao.create(s443);
			stepDataDao.create(s444);
			stepDataDao.create(s445);
			stepDataDao.create(s446);
			stepDataDao.create(s447);
			stepDataDao.create(s448);
			stepDataDao.create(s449);
			stepDataDao.create(s450);
			stepDataDao.create(s451);
			stepDataDao.create(s452);
			stepDataDao.create(s453);
			stepDataDao.create(s454);
			stepDataDao.create(s455);
			stepDataDao.create(s456);
			stepDataDao.create(s457);
			stepDataDao.create(s458);
			stepDataDao.create(s459);
			stepDataDao.create(s460);
			stepDataDao.create(s461);
			stepDataDao.create(s462);
			stepDataDao.create(s463);
			stepDataDao.create(s464);
			stepDataDao.create(s465);
			stepDataDao.create(s466);
			stepDataDao.create(s467);
			stepDataDao.create(s468);
			stepDataDao.create(s469);
			stepDataDao.create(s470);
			stepDataDao.create(s471);
			stepDataDao.create(s472);
			stepDataDao.create(s473);
			stepDataDao.create(s474);
			stepDataDao.create(s475);
			stepDataDao.create(s476);
			stepDataDao.create(s477);
			stepDataDao.create(s478);
			stepDataDao.create(s479);
			stepDataDao.create(s480);
			stepDataDao.create(s481);
			stepDataDao.create(s482);
			stepDataDao.create(s483);
			stepDataDao.create(s484);
			stepDataDao.create(s485);
			stepDataDao.create(s486);
			stepDataDao.create(s487);
			stepDataDao.create(s488);
			stepDataDao.create(s489);
			stepDataDao.create(s490);
			stepDataDao.create(s491);
			stepDataDao.create(s492);
			stepDataDao.create(s493);
			stepDataDao.create(s494);
			stepDataDao.create(s495);
			stepDataDao.create(s496);
			stepDataDao.create(s497);
			stepDataDao.create(s498);
			stepDataDao.create(s499);
			stepDataDao.create(s500);
			stepDataDao.create(s501);
			stepDataDao.create(s502);
			stepDataDao.create(s503);
			stepDataDao.create(s504);
			stepDataDao.create(s505);
			stepDataDao.create(s506);
			stepDataDao.create(s507);
			stepDataDao.create(s508);
			stepDataDao.create(s509);
			stepDataDao.create(s510);
			stepDataDao.create(s511);
			stepDataDao.create(s512);
			stepDataDao.create(s513);
			stepDataDao.create(s514);
			stepDataDao.create(s515);
			stepDataDao.create(s516);
			stepDataDao.create(s517);
			stepDataDao.create(s518);
			stepDataDao.create(s519);
			stepDataDao.create(s520);
			stepDataDao.create(s521);
			stepDataDao.create(s522);
			stepDataDao.create(s523);
			stepDataDao.create(s524);
			stepDataDao.create(s525);
			stepDataDao.create(s526);
			stepDataDao.create(s527);
			stepDataDao.create(s528);
			stepDataDao.create(s529);
			stepDataDao.create(s530);
			stepDataDao.create(s531);
			stepDataDao.create(s532);
			stepDataDao.create(s533);
			stepDataDao.create(s534);
			stepDataDao.create(s535);
			stepDataDao.create(s536);
			stepDataDao.create(s537);
			stepDataDao.create(s538);
			stepDataDao.create(s539);
			stepDataDao.create(s540);
			stepDataDao.create(s541);
			stepDataDao.create(s542);
			stepDataDao.create(s543);
			stepDataDao.create(s544);
			stepDataDao.create(s545);
			stepDataDao.create(s546);
			stepDataDao.create(s547);
			stepDataDao.create(s548);
			stepDataDao.create(s549);
			stepDataDao.create(s550);
			stepDataDao.create(s551);
			stepDataDao.create(s552);
			stepDataDao.create(s553);
			stepDataDao.create(s554);
			stepDataDao.create(s555);
			stepDataDao.create(s556);
			stepDataDao.create(s557);
			stepDataDao.create(s558);
			stepDataDao.create(s559);
			stepDataDao.create(s560);
			stepDataDao.create(s561);
			stepDataDao.create(s562);
			stepDataDao.create(s563);
			stepDataDao.create(s564);
			stepDataDao.create(s565);
			stepDataDao.create(s566);
			stepDataDao.create(s567);
			stepDataDao.create(s568);
			stepDataDao.create(s569);
			stepDataDao.create(s570);
			stepDataDao.create(s571);
			stepDataDao.create(s572);
			stepDataDao.create(s573);
			stepDataDao.create(s574);
			stepDataDao.create(s575);
			stepDataDao.create(s576);
			stepDataDao.create(s577);
			stepDataDao.create(s578);
			stepDataDao.create(s579);
			stepDataDao.create(s580);
			stepDataDao.create(s581);
			stepDataDao.create(s582);
			stepDataDao.create(s583);
			stepDataDao.create(s584);
			stepDataDao.create(s585);
			stepDataDao.create(s586);
			stepDataDao.create(s587);
			stepDataDao.create(s588);
			stepDataDao.create(s589);
			stepDataDao.create(s590);
			stepDataDao.create(s591);
			stepDataDao.create(s592);
			stepDataDao.create(s593);
			stepDataDao.create(s594);
			stepDataDao.create(s595);
			stepDataDao.create(s596);
			stepDataDao.create(s597);
			stepDataDao.create(s598);
			stepDataDao.create(s599);
			stepDataDao.create(s600);
			stepDataDao.create(s601);
			stepDataDao.create(s602);
			stepDataDao.create(s603);
			stepDataDao.create(s604);
			stepDataDao.create(s605);
			stepDataDao.create(s606);
			stepDataDao.create(s607);
			stepDataDao.create(s608);
			stepDataDao.create(s609);
			stepDataDao.create(s610);
			stepDataDao.create(s611);
			stepDataDao.create(s612);
			stepDataDao.create(s613);
			stepDataDao.create(s614);
			stepDataDao.create(s615);
			stepDataDao.create(s616);
			stepDataDao.create(s617);
			stepDataDao.create(s618);
			stepDataDao.create(s619);
			stepDataDao.create(s620);
			stepDataDao.create(s621);
			stepDataDao.create(s622);
			stepDataDao.create(s623);
			stepDataDao.create(s624);
			stepDataDao.create(s625);
			stepDataDao.create(s626);
			stepDataDao.create(s627);
			stepDataDao.create(s628);
			stepDataDao.create(s629);
			stepDataDao.create(s630);
			stepDataDao.create(s631);
			stepDataDao.create(s632);
			stepDataDao.create(s633);
			stepDataDao.create(s634);
			stepDataDao.create(s635);
			stepDataDao.create(s636);
			stepDataDao.create(s637);
			stepDataDao.create(s638);
			stepDataDao.create(s639);
			stepDataDao.create(s640);
			stepDataDao.create(s641);
			stepDataDao.create(s642);
			stepDataDao.create(s643);
			stepDataDao.create(s644);
			stepDataDao.create(s645);
			stepDataDao.create(s646);
			stepDataDao.create(s647);
			stepDataDao.create(s648);
			stepDataDao.create(s649);
			stepDataDao.create(s650);
			stepDataDao.create(s651);
			stepDataDao.create(s652);
			stepDataDao.create(s653);
			stepDataDao.create(s654);
			stepDataDao.create(s655);
			stepDataDao.create(s656);
			stepDataDao.create(s657);
			stepDataDao.create(s658);
			stepDataDao.create(s659);
			stepDataDao.create(s660);
			stepDataDao.create(s661);
			stepDataDao.create(s662);
			stepDataDao.create(s663);
			stepDataDao.create(s664);
			stepDataDao.create(s665);
			stepDataDao.create(s666);
			stepDataDao.create(s667);
			stepDataDao.create(s668);
			stepDataDao.create(s669);
			stepDataDao.create(s670);
			stepDataDao.create(s671);
			stepDataDao.create(s672);
			stepDataDao.create(s673);
			stepDataDao.create(s674);
			stepDataDao.create(s675);
			stepDataDao.create(s676);
			stepDataDao.create(s677);
			stepDataDao.create(s678);
			stepDataDao.create(s679);
			stepDataDao.create(s680);
			stepDataDao.create(s681);
			stepDataDao.create(s682);
			stepDataDao.create(s683);
			stepDataDao.create(s684);
			stepDataDao.create(s685);
			stepDataDao.create(s686);
			stepDataDao.create(s687);
			stepDataDao.create(s688);
			stepDataDao.create(s689);
			stepDataDao.create(s690);
			stepDataDao.create(s691);
			stepDataDao.create(s692);
			stepDataDao.create(s693);
			stepDataDao.create(s694);
			stepDataDao.create(s695);
			stepDataDao.create(s696);
			stepDataDao.create(s697);
			stepDataDao.create(s698);
			stepDataDao.create(s699);
			stepDataDao.create(s700);
			stepDataDao.create(s701);
			stepDataDao.create(s702);
			stepDataDao.create(s703);
			stepDataDao.create(s704);
			stepDataDao.create(s705);
			stepDataDao.create(s706);
			stepDataDao.create(s707);
			stepDataDao.create(s708);
			stepDataDao.create(s709);
			stepDataDao.create(s710);
			stepDataDao.create(s711);
			stepDataDao.create(s712);
			stepDataDao.create(s713);
			stepDataDao.create(s714);
			stepDataDao.create(s715);
			stepDataDao.create(s716);
			stepDataDao.create(s717);
			stepDataDao.create(s718);
			stepDataDao.create(s719);
			stepDataDao.create(s720);
			stepDataDao.create(s721);
			stepDataDao.create(s722);
			stepDataDao.create(s723);
			stepDataDao.create(s724);
			stepDataDao.create(s725);
			stepDataDao.create(s726);
			stepDataDao.create(s727);
			stepDataDao.create(s728);
			stepDataDao.create(s729);
			stepDataDao.create(s730);
			stepDataDao.create(s731);
			stepDataDao.create(s732);
			stepDataDao.create(s733);
			stepDataDao.create(s734);
			stepDataDao.create(s735);
			stepDataDao.create(s736);
			stepDataDao.create(s737);
			stepDataDao.create(s738);
			stepDataDao.create(s739);
			stepDataDao.create(s740);
			stepDataDao.create(s741);
			stepDataDao.create(s742);
			stepDataDao.create(s743);
			stepDataDao.create(s744);
			stepDataDao.create(s745);
			stepDataDao.create(s746);
			stepDataDao.create(s747);
			stepDataDao.create(s748);
			stepDataDao.create(s749);
			stepDataDao.create(s750);
			stepDataDao.create(s751);
			stepDataDao.create(s752);
			stepDataDao.create(s753);
			stepDataDao.create(s754);
			stepDataDao.create(s755);
			stepDataDao.create(s756);
			stepDataDao.create(s757);
			stepDataDao.create(s758);
			stepDataDao.create(s759);
			stepDataDao.create(s760);
			stepDataDao.create(s761);
			stepDataDao.create(s762);
			stepDataDao.create(s763);
			stepDataDao.create(s764);
			stepDataDao.create(s765);
			stepDataDao.create(s766);
			stepDataDao.create(s767);
			stepDataDao.create(s768);
			stepDataDao.create(s769);
			stepDataDao.create(s770);
			stepDataDao.create(s771);
			stepDataDao.create(s772);
			stepDataDao.create(s773);
			stepDataDao.create(s774);
			stepDataDao.create(s775);
			stepDataDao.create(s776);
			stepDataDao.create(s777);
			stepDataDao.create(s778);
			stepDataDao.create(s779);
			stepDataDao.create(s780);
			stepDataDao.create(s781);
			stepDataDao.create(s782);
			stepDataDao.create(s783);
			stepDataDao.create(s784);
			stepDataDao.create(s785);
			stepDataDao.create(s786);
			stepDataDao.create(s787);
			stepDataDao.create(s788);
			stepDataDao.create(s789);
			stepDataDao.create(s790);
			stepDataDao.create(s791);
			stepDataDao.create(s792);
			stepDataDao.create(s793);
			stepDataDao.create(s794);
			stepDataDao.create(s795);
			stepDataDao.create(s796);
			stepDataDao.create(s797);
			stepDataDao.create(s798);
			stepDataDao.create(s799);
			stepDataDao.create(s800);
			stepDataDao.create(s801);
			stepDataDao.create(s802);
			stepDataDao.create(s803);
			stepDataDao.create(s804);
			stepDataDao.create(s805);
			stepDataDao.create(s806);
			stepDataDao.create(s807);
			stepDataDao.create(s808);
			stepDataDao.create(s809);
			stepDataDao.create(s810);
			stepDataDao.create(s811);
			stepDataDao.create(s812);
			stepDataDao.create(s813);
			stepDataDao.create(s814);
			stepDataDao.create(s815);
			stepDataDao.create(s816);
			stepDataDao.create(s817);
			stepDataDao.create(s818);
		} catch (RuntimeException ex) {
			Log.e("Inserting step", "Insert error " + ex.toString());
		}
		
		// HL
		HighLight h1 = new HighLight("hl_varrados_poiv01",
				"poiV01-Mola deth Batan", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s69);
		HighLight h2 = new HighLight("hl_varrados_poiv02",
				"poiV02-Hònt deth Sofre", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s71);
		HighLight h3 = new HighLight("hl_varrados_poiv03",
				"poiV03-desguàs conducció Varradòs", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s73);
		HighLight h4 = new HighLight("hl_varrados_poiv04",
				"poiV04-vista barranc Nere", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s161);
		HighLight h5 = new HighLight("hl_varrados_poiv05",
				"poiV05-Barranc Salies", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s256);
		HighLight h6 = new HighLight("hl_varrados_poiv06",
				"poiV06-Barratge de Varradòs", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s549);
		HighLight h7 = new HighLight("hl_varrados_poiv07",
				"poiV07-Saut deth Pish", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s556);
		HighLight h8 = new HighLight("hl_varrados_poiv08",
				"poiV08-Hònt dera Pila", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s680);
		HighLight h9 = new HighLight("hl_varrados_poiv09",
				"poiV09-Nere de Uèrri", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s766);
		HighLight h10 = new HighLight("hl_varrados_poiv10",
				"poiV10-Estanh Pica Palomèra", null, 10,
				HighLight.POINT_OF_INTEREST_OFFICIAL,s788);

		// WP
		HighLight h11 = new HighLight("hl_varrados_wpv01",
				"wpV01-sortida rotonda", null, 10, HighLight.WAYPOINT,s1);
		HighLight h12 = new HighLight("hl_varrados_wpv02",
				"wpV02-inici pista Varradòs", null, 10, HighLight.WAYPOINT,s4);
		HighLight h13 = new HighLight("hl_varrados_wpv03", "wpV03-aparcament",
				null, 10, HighLight.WAYPOINT,s536);
//		HighLight h14 = new HighLight("hl_varrados_wpv04",
//				"wpV04-Barratge Varradòs", null, 10, HighLight.WAYPOINT);
//		HighLight h15 = new HighLight("hl_varrados_wpv05",
//				"wpV05-Saut deth Pish", null, 10, HighLight.WAYPOINT);
		HighLight h16 = new HighLight("hl_varrados_wpv06",
				"wpV06-trencall amunt camí", null, 10, HighLight.WAYPOINT,s633);
		HighLight h17 = new HighLight("hl_varrados_wpv07",
				"wpV07-tronc caigut", null, 10, HighLight.WAYPOINT,s657);
		HighLight h18 = new HighLight("hl_varrados_wpv08",
				"wpV08-barranc de Siesso", null, 10, HighLight.WAYPOINT,s721);
		HighLight h19 = new HighLight("hl_varrados_wpv09",
				"wpV09-inici/final circuit estanys", null, 10,
				HighLight.WAYPOINT,s738);
//		HighLight h20 = new HighLight("hl_varrados_wpv10",
//				"wpV10-Nere de Uèrri", null, 10, HighLight.WAYPOINT);
//		HighLight h21 = new HighLight("hl_varrados_wpv11",
//				"wpV11-Estanh Pica Palomèra", null, 10, HighLight.WAYPOINT);

		try {
			hlDataDao.create(h1);
			hlDataDao.create(h2);
			hlDataDao.create(h3);
			hlDataDao.create(h4);
			hlDataDao.create(h5);
			hlDataDao.create(h6);
			hlDataDao.create(h7);
			hlDataDao.create(h8);
			hlDataDao.create(h9);
			hlDataDao.create(h10);
			hlDataDao.create(h11);
			hlDataDao.create(h12);
			hlDataDao.create(h13);
			//hlDataDao.create(h14);
			//hlDataDao.create(h15);
			hlDataDao.create(h16);
			hlDataDao.create(h17);
			hlDataDao.create(h18);
			hlDataDao.create(h19);
			//hlDataDao.create(h20);
			//hlDataDao.create(h21);
		} catch (RuntimeException ex) {
			Log.e("Inserting highlight", "Insert error " + ex.toString());
		}

		
		Route r = new Route();
		r.setId("ROUTE_VARRADOS");
		r.setName("Vall de Varradós");
		r.setDescription("Itinerari en dues parts: primer 10 km de pista asfaltada amb 695 m de desnivell (30 minuts, anada), per recòrrer el riu de Varradòs i visitar el Saut deth Pish i el Barratge de Varradòs; després, itinerari circular de 12.3 km amb 980 m de desnivell (6-7 hores a peu), per visitar els estanhs Nere de Uèrri i de Pica Palomèra. El camí a peu transcorre per senders poc marcats i zones on la progressió es fa dificultosa, en una de les zones més recòndites i salvatges de l’Aran. És imprescindible tenir un bon sentit de l’orientació, bona forma física i experiència en alta muntanya. El riu de Varradòs està encaixonant en una vall de relleu escarpat, que dona lloca lloc a una intensa activitat d’allaus i a cascades impressionants, com el Saut deth Pish. Els estanys a visitar són un exemple d’hàbitats extrems, degut a l’acidesa de les seves aigües i l’alt contingut de metalls que provenen de les roques que els envolten.");
		r.setUserId("1");
		// Ph_ch parameters
		// r.setReference(r6);
		// Interactive image
		// r.setInteractiveImage(img);
		r.setTrack(t);
		r.setLocalCarto("OSMPublicTransport_HiRes.mbtiles");

		try {
			routeDataDao.create(r);
		} catch (RuntimeException ex) {
			Log.e("Inserting route", "Insert error " + ex.toString());
		}


	}

//	public static void loadGaronaPeu(DataBaseHelper db, Context context) {
//		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
//		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
//		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
//		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
//
//		Track t = new Track("TRACK_GARONAP",
//				"Waypoints recorregut Garona a peu");
//		try {
//			trackDataDao.create(t);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting track", "Insert error " + ex.toString());
//		}
//
//		HighLight h1 = new HighLight("hl_garonap_poig01",
//				"poiG01-Pònt d'Arròs", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h2 = new HighLight("hl_garonap_poig02",
//				"poiG02-Macroinvertebrats", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h3 = new HighLight("hl_garonap_poig03",
//				"poiG03-Perfil sòl al·luvial", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h4 = new HighLight("hl_garonap_poig04", "poiG04-Mirador riu",
//				null, 10, HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h5 = new HighLight("hl_garonap_poig05",
//				"poiG05-Pònt de Beussa", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h6 = new HighLight("hl_garonap_poig06",
//				"poiG06-Respirador conducció", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h7 = new HighLight("hl_garonap_poig07",
//				"poiG07-Assut i captació central Benós", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h8 = new HighLight("hl_garonap_poig08",
//				"poiG08-Pònt i Mòla de Betren", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//		HighLight h9 = new HighLight("hl_garonap_poig09",
//				"poiG09-Vista Es Cants", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		HighLight h10 = new 
//				HighLight("hl_garonap_wpG01",
//				"wpG01 - Parada de l’autobus a la rotonda (N-230) de Pònt d’Arròs (sortida)", 
//				"Prendre la primera sortida de la rotonda, en direcció a Arres, i desviar-se cap a l’esquerra just desprès dels edificis, seguint la indicació per anar cap a l’Airau de Servicis del Conselh d’Aran, fins arribar al pont sobre la Garona (0.3 km, desnivell -13 m)", 10, HighLight.WAYPOINT);
//		// Ja hi ha poi
//		HighLight h11 = new
//		HighLight("hl_garonap_wpG02",
//				"wpG02-Pònt d'Arròs",
//				"Travessar el pont, girar a l’esquerra i continuar pel camí de ferradura fins als prats que queden entre el camí i el riu aprop d’Aubèrt (1.5 km, desnivell 32 m)",10,HighLight.WAYPOINT);
//		
//		HighLight h12 = new HighLight("hl_garonap_wpG03",
//				"wpG03-Prats de Plaus", 
//				"Continuar pel camí fins arribar a les primeres cases d’Aubèrt, abans de travessar el pont (0.6 km, desnivell 11 m)", 10, HighLight.WAYPOINT);
//		
//		HighLight h13 = new HighLight("hl_garonap_wpG04",
//				"wpG04 - trencall al Pònt d’Aubèrt", 
//				"Continuar pel carrer que va paral•lel al riu, fins que al final de les cases (0.2 km)", 10, HighLight.WAYPOINT);
//		
//		// Ja hi ha poi
//		HighLight h14 = new
//		HighLight("hl_garonap_wpG05",
//				"wpG05 - perfil sòl al·luvial",
//				"Continuar per camí de terra fins la tanca d’un prat, on el camí es desvia cap a la dreta en pujada (0.1 km)",10,HighLight.WAYPOINT);
//		
//		HighLight h15 = new HighLight("hl_garonap_wpG06",
//				"wpG06 - sender per pujar al Camin Reiau", 
//				"Reseguir el sender, primer una curta pujada y després cap a l’esquerra, seguint el curs del riu a una certa altura, fins arribar al punt més alt des d’on hi ha una vista panoràmica del riu i la vall (1.3 km, desnivell 50 m)", 10, HighLight.WAYPOINT);
//		
//		// Ja hi ha poi
//		HighLight h16 = new
//		HighLight("hl_garonap_wpG07",
//				"wpG07 - Mirador sobre el riu",
//				"Continuar pel sender, en baixada suau, fins al Pònt de Beussa (0.5 km, desnivell -20 m)",10,HighLight.WAYPOINT);
//		
//		// Ja hi ha poi
//		HighLight h17 = new
//		HighLight("hl_garonap_wpG08",
//				"wpG08 - Pònt de Beussa",
//				"A l’extrem del pont i pel cantó on hem arribat, prendre el camí senyalitzat cap a Gausac, on hi ha una barana de fusta, fins arribar a una reixa al terra que protegeix el respirador d’una conducció d’aigua (0.6 km, desnivell 15 m)",
//				10,HighLight.WAYPOINT);
//		
//		// Ja hi ha poi
//		HighLight h18 = new
//		HighLight("hl_garonap_wpG09",
//				"wpG09 - respirador conducció",
//				"Continuar pel camí, que ja s’ha eixamplat per convertir-se en un camí de ferradura, fins una clariana en els arbres al costat esquerre on s’obre un prat (0.4 km, desnivell 16 m)",
//				10,HighLight.WAYPOINT);
//		
//		HighLight h19 = new HighLight("hl_garonap_wpG10",
//				"wpG10 - trencall a l’assut", 
//				"Travessar el prat fins a la vora del riu, on hi ha l’assut i la captació d’aigua de la central de Benòs (50 m, desnivell -10 m)", 
//				10, HighLight.WAYPOINT);
//		
//		// Ja hi ha poi
//		HighLight h20 = new
//		HighLight("hl_garonap_wpG11",
//				"wpG11 - Assut i captació central de Benòs",
//				"Tornar enrere fins el camí de ferradura que haviem deixat, i continuar fins arribar a la carretera (0.5 km, desnivell 37 m)",
//				10,HighLight.WAYPOINT);
//		
//		HighLight h21 = new HighLight("hl_garonap_wpG12",
//				"wpG12 - Carretera de Gausac", 
//				"Continuar cap a l’esquerra per la carretera, fins arribar a la cruïlla amb el carrer Anglada (0.2 km)", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h22 = new HighLight("hl_garonap_wpG13",
//				"wpG13 - carrer Anglada", 
//				"Girar a l’esquerra i baixar pel carrer Anglada, fins arribar a la carretera nacional. Travessar la carretera pel pas de zebra que queda a l’esquerra, i seguir per la continuació del carrer Anglada fins que acaba al Passeig dera Libertat (0.4 km, desnivell -20 m)", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h23 = new HighLight("hl_garonap_wpG14",
//				"wpG14 - Passeig dera Libertat", 
//				"Girar a la dreta tot pujant el Passeig, caminar 35 m i prendre el carreró que queda a l’esquerra i va cap a la Plaça del Conselh Generau d’Aran", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h24 = new HighLight("hl_garonap_wpG15",
//				"wpG15 - Carreró Conselh Aran", 
//				"Passar el carreró i travessar la plaça fins arribar al riu Nere (60 m), i buscar el pont que queda a uns 20 m a l’esquerra.", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h25 = new HighLight("hl_garonap_wpG16", 
//				"wpG16 - Pont Nere",
//				"Travessar el pont i passar pel passadís que hi ha al front i continuar recte, primer pel davant de l’hospital i després pel carrer Dr. Manel Vidal, fins arribar al carrer Querimònia, davant de l’explanada d’aparcament (0.2 km)", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h26 = new HighLight("hl_garonap_wpG17",
//				"wpG17 - carrer Querimònia", 
//				"Girar a l’esquerra pel carrer Querimònia, i continuar fins arribar al pont sobre la Garona (45 m)", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h27 = new HighLight("hl_garonap_wpG18", 
//				"wpG18-Pont Garona",
//				"Abans de travessar el pont, girar a la dreta i continuar pel camí del parc que ressegueix la vora del riu, fins arribar a l’avinguda deth Solan, amb doble carril. Travessar pel pas zebra i buscar l’inici del passeig fluvial que queda uns 20 m a l’esquerra, darrere dels edificis i per la mateixa vora del riu que veniem (0.5 km, desnivell 8 m)", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h28 = new HighLight("hl_garonap_wpG19",
//				"wpG19 - Inici passeig fluvial", 
//				"Seguir el passeig fins el final, al Pònt i la Mòla de Betren (0.6 km, desnivell 8 m)", 
//				10, HighLight.WAYPOINT);
//
//		// Ja hi ha poi
//		HighLight h29 = new
//		HighLight("hl_garonap_wpG20",
//				"wpG20 - Pònt dera Mòla de Betren",
//				"Travessar el pont, fer una curta pujada fins al camí (GR-211) i girar a la dreta. El camí és ben marcat. Va primer per uns prats de pastura i després es converteix en un sender per zona de matollar i roca. Continuar fins arribar a una zona amb vistes sobre la plana fluvial, una mica més enllà d’Escunhau (1.4 km, desnivell 58 m)",
//				10,HighLight.WAYPOINT);
//		
//		// Ja hi ha poi
//		HighLight h30 = new
//		HighLight("hl_garonap_wpG21",
//				"wpG21 - vista Es Cants",
//				"Seguir sempre pel camí, fins arribar a un trencall a la dreta que baixa cap el riu, una mica passat Casarilh (0.9 km, desnivell 68 m)",
//				10,HighLight.WAYPOINT);
//		
//		HighLight h31 = new HighLight("hl_garonap_wpG22",
//				"wpG22 -  trencall baixada al Pònt de Casarilh", 
//				"Prendre el trencall de baixada, quasi en sentit contrari al que veniem fins el pont de Casarilh (0.4 km, desnivell -78 m)", 
//				10,HighLight.WAYPOINT);
//		
//		HighLight h32 = new HighLight("hl_garonap_wpG23",
//				"wpG23 - Pònt de Casarilh", 
//				"Travessar el pont i continuar pel camí que puja a Casarilh, i en arribar al poble seguir recte cap amunt, primer pel carrer de Sant Antòni i després pel de Sant Tomàs, fins arribar al final al Carrèr Major. Girar a la dreta fins arribar a la carretera C-28 on hi ha la parada d’autobus (0.25 km, desnivell 17 m)", 
//				10, HighLight.WAYPOINT);
//		
//		HighLight h33 = new HighLight("hl_garonap_wpG24",
//				"wpG24 - Parada bus Casarilh (final)", 
//				null, 
//				10, HighLight.WAYPOINT);
//
//		try {
//			hlDataDao.create(h1);
//			hlDataDao.create(h2);
//			hlDataDao.create(h3);
//			hlDataDao.create(h4);
//			hlDataDao.create(h5);
//			hlDataDao.create(h6);
//			hlDataDao.create(h7);
//			hlDataDao.create(h8);
//			hlDataDao.create(h9);
//			hlDataDao.create(h10);
//			hlDataDao.create(h11);
//			hlDataDao.create(h12);
//			hlDataDao.create(h13);
//			hlDataDao.create(h14);
//			hlDataDao.create(h15);
//			hlDataDao.create(h16);
//			hlDataDao.create(h17);
//			hlDataDao.create(h18);
//			hlDataDao.create(h19);
//			hlDataDao.create(h20);
//			hlDataDao.create(h21);
//			hlDataDao.create(h22);
//			hlDataDao.create(h23);
//			hlDataDao.create(h24);
//			hlDataDao.create(h25);
//			hlDataDao.create(h26);
//			hlDataDao.create(h27);
//			hlDataDao.create(h28);
//			hlDataDao.create(h29);
//			hlDataDao.create(h30);
//			hlDataDao.create(h31);
//			hlDataDao.create(h32);
//			hlDataDao.create(h33);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting highlight", "Insert error " + ex.toString());
//		}
//
//		Step s1 = new Step("step_garonap_1", "step", 42.7379506261369,
//				0.756428022649132, 0, 10, 1, t, h10);
//		Step s2 = new Step("step_garonap_2", "step", 42.7380049298091,
//				0.756212281338312, 0, 10, 2, t, null);
//		Step s3 = new Step("step_garonap_3", "step", 42.738082088476,
//				0.756014039373045, 0, 10, 3, t, null);
//		Step s4 = new Step("step_garonap_4", "step", 42.7381500088834,
//				0.755803914046115, 0, 10, 4, t, null);
//		Step s5 = new Step("step_garonap_5", "step", 42.7381910528193,
//				0.755600866134868, 0, 10, 5, t, null);
//		Step s6 = new Step("step_garonap_6", "step", 42.7381734859312,
//				0.75539382490252, 0, 10, 6, t, null);
//		Step s7 = new Step("step_garonap_7", "step", 42.7381547217206,
//				0.755125746109274, 0, 10, 7, t, null);
//		Step s8 = new Step("step_garonap_8", "step", 42.738061863123,
//				0.754982503491942, 0, 10, 8, t, null);
//		Step s9 = new Step("step_garonap_9", "step", 42.7379919796428,
//				0.754862864306545, 0, 10, 9, t, null);
//		Step s10 = new Step("step_garonap_10", "step", 42.7378470447076,
//				0.754819231167867, 0, 10, 10, t, null);
//		Step s11 = new Step("step_garonap_11", "step", 42.737683633742,
//				0.754751832949353, 0, 10, 11, t, null);
//		Step s12 = new Step("step_garonap_12", "step", 42.7374304764013,
//				0.75469989064771, 0, 10, 12, t, null);
//		Step s13 = new Step("step_garonap_13", "step", 42.7371813394429,
//				0.754623371629231, 0, 10, 13, t, null);
//		Step s14 = new Step("step_garonap_14", "step", 42.7370460016134,
//				0.754609933003155, 0, 10, 14, t, null);
//		Step s15 = new Step("step_garonap_15", "step", 42.7368425758331,
//				0.754568412416713, 0, 10, 15, t, h11);
////		Step s15 = new Step("step_garonap_15", "step", 42.7368425758331,
////				0.754568412416713, 0, 10, 15, t, null);
//		Step s16 = new Step("step_garonap_16", "step", 42.736720616137,
//				0.754548383082882, 0, 10, 16, t, h1);
//		Step s17 = new Step("step_garonap_17", "step", 42.7367274398906,
//				0.754896289831158, 0, 10, 17, t, null);
//		Step s18 = new Step("step_garonap_18", "step", 42.736728615119,
//				0.755415423069767, 0, 10, 18, t, null);
//		Step s19 = new Step("step_garonap_19", "step", 42.7366917595461,
//				0.755832093252925, 0, 10, 19, t, null);
//		Step s20 = new Step("step_garonap_20", "step", 42.7366495657082,
//				0.756206199913777, 0, 10, 20, t, null);
//		Step s21 = new Step("step_garonap_21", "step", 42.7366115110819,
//				0.756561832978397, 0, 10, 21, t, null);
//		Step s22 = new Step("step_garonap_22", "step", 42.736587192424,
//				0.75692918617058, 0, 10, 22, t, null);
//		Step s23 = new Step("step_garonap_23", "step", 42.7365449949673,
//				0.757303291606728, 0, 10, 23, t, null);
//		Step s24 = new Step("step_garonap_24", "step", 42.7364206133145,
//				0.7576192798585, 0, 10, 24, t, null);
//		Step s25 = new Step("step_garonap_25", "step", 42.7363749467735,
//				0.757816379329086, 0, 10, 25, t, null);
//		Step s26 = new Step("step_garonap_26", "step", 42.736284047809,
//				0.75800289294844, 0, 10, 26, t, null);
//		Step s27 = new Step("step_garonap_27", "step", 42.736183791337,
//				0.758171419561272, 0, 10, 27, t, null);
//		Step s28 = new Step("step_garonap_28", "step", 42.7361066287084,
//				0.758369652856376, 0, 10, 28, t, null);
//		Step s29 = new Step("step_garonap_29", "step", 42.7358936919534,
//				0.758762121810783, 0, 10, 29, t, null);
//		Step s30 = new Step("step_garonap_30", "step", 42.7356969588146,
//				0.759062386625876, 0, 10, 30, t, null);
//		Step s31 = new Step("step_garonap_31", "step", 42.7355397628493,
//				0.759312362763404, 0, 10, 31, t, null);
//		Step s32 = new Step("step_garonap_32", "step", 42.7353877823288,
//				0.759598796696846, 0, 10, 32, t, null);
//		Step s33 = new Step("step_garonap_33", "step", 42.7352479851532,
//				0.759817604639363, 0, 10, 33, t, null);
//		Step s34 = new Step("step_garonap_34", "step", 42.7351964211381,
//				0.760173713916043, 0, 10, 34, t, null);
//		Step s35 = new Step("step_garonap_35", "step", 42.7351510269053,
//				0.760615109635294, 0, 10, 35, t, null);
//		Step s36 = new Step("step_garonap_36", "step", 42.7351092946404,
//				0.761013618378703, 0, 10, 36, t, null);
//		Step s37 = new Step("step_garonap_37", "step", 42.7351571891351,
//				0.76139057794438, 0, 10, 37, t, null);
//		Step s38 = new Step("step_garonap_38", "step", 42.7351996985659,
//				0.762183062310446, 0, 10, 38, t, null);
//		Step s39 = new Step("step_garonap_39", "step", 42.735235164929,
//				0.762615440592345, 0, 10, 39, t, null);
//		Step s40 = new Step("step_garonap_40", "step", 42.7352645798415,
//				0.762968635484435, 0, 10, 40, t, null);
//		Step s41 = new Step("step_garonap_41", "step", 42.7352411943827,
//				0.763384807202687, 0, 10, 41, t, null);
//		Step s42 = new Step("step_garonap_42", "step", 42.7352285630324,
//				0.763660112538517, 0, 10, 42, t, null);
//		Step s43 = new Step("step_garonap_43", "step", 42.7351637274582,
//				0.76402891121301, 0, 10, 43, t, null);
//		Step s44 = new Step("step_garonap_44", "step", 42.7350617041407,
//				0.764337967670039, 0, 10, 44, t, null);
//		Step s45 = new Step("step_garonap_45", "step", 42.7349152826551,
//				0.764679157345344, 0, 10, 45, t, null);
//		Step s46 = new Step("step_garonap_46", "step", 42.7347648374338,
//				0.765044921001478, 0, 10, 46, t, null);
//		Step s47 = new Step("step_garonap_47", "step", 42.7344987190957,
//				0.765482022962659, 0, 10, 47, t, null);
//		Step s48 = new Step("step_garonap_48", "step", 42.7342817342351,
//				0.765899033532103, 0, 10, 48, t, null);
//		Step s49 = new Step("step_garonap_49", "step", 42.7340983605064,
//				0.766192680469989, 0, 10, 49, t, null);
//		Step s50 = new Step("step_garonap_50", "step", 42.7339079858623,
//				0.766358315822015, 0, 10, 50, t, null);
//		Step s51 = new Step("step_garonap_51", "step", 42.7337168964675,
//				0.766487329856614, 0, 10, 51, t, null);
//		Step s52 = new Step("step_garonap_52", "step", 42.7334969311493,
//				0.766751747681391, 0, 10, 52, t, null);
//		Step s53 = new Step("step_garonap_53", "step", 42.7332531834757,
//				0.767181922727206, 0, 10, 53, t, null);
//		Step s54 = new Step("step_garonap_54", "step", 42.733147485122,
//				0.767533849511616, 0, 10, 54, t, null);
//		Step s55 = new Step("step_garonap_55", "step", 42.7331039432697,
//				0.767840791824734, 0, 10, 55, t, null);
//		Step s56 = new Step("step_garonap_56", "step", 42.7330159052554,
//				0.76840584869563, 0, 10, 56, t, null);
//		Step s57 = new Step("step_garonap_57", "step", 42.7328992031045,
//				0.76888642582388, 0, 10, 57, t, null);
//		Step s58 = new Step("step_garonap_58", "step", 42.7328538936587,
//				0.769333903532403, 0, 10, 58, t, null);
//		Step s59 = new Step("step_garonap_59", "step", 42.732728069959,
//				0.769808697098018, 0, 10, 59, t, null);
//		Step s60 = new Step("step_garonap_60", "step", 42.7325611735795,
//				0.770486509729414, 0, 10, 60, t, null);
//		Step s61 = new Step("step_garonap_61", "step", 42.7324670612722,
//				0.770740291291089, 0, 10, 61, t, null);
//		Step s62 = new Step("step_garonap_62", "step", 42.7323777227022,
//				0.770973053568929, 0, 10, 62, t, h12);
//		Step s63 = new Step("step_garonap_63", "step", 42.7322916115451,
//				0.771159680182343, 0, 10, 63, t, h2);
//		Step s64 = new Step("step_garonap_64", "step", 42.7322274568405,
//				0.771384057969184, 0, 10, 64, t, null);
//		Step s65 = new Step("step_garonap_65", "step", 42.7321943135986,
//				0.771531825411059, 0, 10, 65, t, null);
//		Step s66 = new Step("step_garonap_66", "step", 42.7320535714245,
//				0.772165938605142, 0, 10, 66, t, null);
//		Step s67 = new Step("step_garonap_67", "step", 42.7319921383107,
//				0.772479620374202, 0, 10, 67, t, null);
//		Step s68 = new Step("step_garonap_68", "step", 42.7319462080769,
//				0.772664489370636, 0, 10, 68, t, null);
//		Step s69 = new Step("step_garonap_69", "step", 42.731863582849,
//				0.77304611092908, 0, 10, 69, t, null);
//		Step s70 = new Step("step_garonap_70", "step", 42.7318141020619,
//				0.773279965979321, 0, 10, 70, t, null);
//		Step s71 = new Step("step_garonap_71", "step", 42.7317531406297,
//				0.77361805843217, 0, 10, 71, t, null);
//		Step s72 = new Step("step_garonap_72", "step", 42.7317078019507,
//				0.773833442021426, 0, 10, 72, t, null);
//		Step s73 = new Step("step_garonap_73", "step", 42.7316514466523,
//				0.773945392693058, 0, 10, 73, t, null);
//		Step s74 = new Step("step_garonap_74", "step", 42.7315231115739,
//				0.774292001270779, 0, 10, 74, t, null);
//		Step s75 = new Step("step_garonap_75", "step", 42.7314517246494,
//				0.774557174814398, 0, 10, 75, t, null);
//		Step s76 = new Step("step_garonap_76", "step", 42.7313124908676,
//				0.774806452015675, 0, 10, 76, t, null);
//		Step s77 = new Step("step_garonap_77", "step", 42.7311132228102,
//				0.774978478722623, 0, 10, 77, t, null);
//		Step s78 = new Step("step_garonap_78", "step", 42.7309506609924,
//				0.775185836043857, 0, 10, 78, t, null);
//		Step s79 = new Step("step_garonap_79", "step", 42.7307524597074,
//				0.775412788658466, 0, 10, 79, t, null);
//		Step s80 = new Step("step_garonap_80", "step", 42.7306559643375,
//				0.775544493344813, 0, 10, 80, t, null);
//		Step s81 = new Step("step_garonap_81", "step", 42.7305658654765,
//				0.775773686540755, 0, 10, 81, t, null);
//		Step s82 = new Step("step_garonap_82", "step", 42.7303558247633,
//				0.776086561640664, 0, 10, 82, t, null);
//		Step s83 = new Step("step_garonap_83", "step", 42.7301682799454,
//				0.776398630641528, 0, 10, 83, t, null);
//		Step s84 = new Step("step_garonap_84", "step", 42.7300816118505,
//				0.77657273246095, 0, 10, 84, t, null);
//		Step s85 = new Step("step_garonap_85", "step", 42.7300404112055,
//				0.776769638329833, 0, 10, 85, t, null);
//		Step s86 = new Step("step_garonap_86", "step", 42.7300792506196,
//				0.776914825782487, 0, 10, 86, t, h13);
//		Step s87 = new Step("step_garonap_87", "step", 42.7299604936281,
//				0.777059536608392, 0, 10, 87, t, null);
//		Step s88 = new Step("step_garonap_88", "step", 42.7296852100353,
//				0.777490772862168, 0, 10, 88, t, null);
//		Step s89 = new Step("step_garonap_89", "step", 42.7294613108586,
//				0.777785810851847, 0, 10, 89, t, null);
//		Step s90 = new Step("step_garonap_90", "step", 42.7293000466402,
//				0.778060288319032, 0, 10, 90, t, null);
//		Step s91 = new Step("step_garonap_91", "step", 42.7291519245094,
//				0.778315973465675, 0, 10, 91, t, null);
//		Step s92 = new Step("step_garonap_92", "step", 42.7289758588514,
//				0.778755870854447, 0, 10, 92, t, null);
//		Step s93 = new Step("step_garonap_93", "step", 42.72885508652,
//		0.77902889830158, 0, 10, 93, t, h14);
//		//Step s93 = new Step("step_garonap_93", "step", 42.72885508652,
//				//0.77902889830158, 0, 10, 93, t, null);
//		Step s94 = new Step("step_garonap_94", "step", 42.7287684142379,
//				0.779202992871558, 0, 10, 94, t, null);
//		Step s95 = new Step("step_garonap_95", "step", 42.7286556925048,
//				0.779426873920701, 0, 10, 95, t, h3);
//		Step s96 = new Step("step_garonap_96", "step", 42.7285246177172,
//				0.77963308760451, 0, 10, 96, t, null);
//		Step s97 = new Step("step_garonap_97", "step", 42.7284636387301,
//				0.779739085034967, 0, 10, 97, t, h15);
//		Step s98 = new Step("step_garonap_98", "step", 42.728327118801,
//				0.779664563641114, 0, 10, 98, t, null);
//		Step s99 = new Step("step_garonap_99", "step", 42.7282857958706,
//				0.779623288283965, 0, 10, 99, t, null);
//		Step s100 = new Step("step_garonap_100", "step", 42.7281647846393,
//				0.779884105263349, 0, 10, 100, t, null);
//		Step s101 = new Step("step_garonap_101", "step", 42.7280642578152,
//				0.780040369640177, 0, 10, 101, t, null);
//		Step s102 = new Step("step_garonap_102", "step", 42.7276854766684,
//				0.780475270953061, 0, 10, 102, t, null);
//		Step s103 = new Step("step_garonap_103", "step", 42.7275140237715,
//				0.78068902630411, 0, 10, 103, t, null);
//		Step s104 = new Step("step_garonap_104", "step", 42.7273383076639,
//				0.780915146562518, 0, 10, 104, t, null);
//		Step s105 = new Step("step_garonap_105", "step", 42.7271051637184,
//				0.781198276389597, 0, 10, 105, t, null);
//		Step s106 = new Step("step_garonap_106", "step", 42.7269032781351,
//				0.781468075646611, 0, 10, 106, t, null);
//		Step s107 = new Step("step_garonap_107", "step", 42.7266084421417,
//				0.781820577121505, 0, 10, 107, t, null);
//		Step s108 = new Step("step_garonap_108", "step", 42.7265512509053,
//				0.781889792312905, 0, 10, 108, t, null);
//		Step s109 = new Step("step_garonap_109", "step", 42.7264728633469,
//				0.782026939345862, 0, 10, 109, t, null);
//		Step s110 = new Step("step_garonap_110", "step", 42.7261740059048,
//				0.782171943662779, 0, 10, 110, t, null);
//		Step s111 = new Step("step_garonap_111", "step", 42.7259373120366,
//				0.782271982887074, 0, 10, 111, t, null);
//		Step s112 = new Step("step_garonap_112", "step", 42.7256613057608,
//				0.782434490965814, 0, 10, 112, t, null);
//		Step s113 = new Step("step_garonap_113", "step", 42.725451962526,
//				0.782551874236454, 0, 10, 113, t, null);
//		Step s114 = new Step("step_garonap_114", "step", 42.7253251446403,
//				0.78274570510479, 0, 10, 114, t, null);
//		Step s115 = new Step("step_garonap_115", "step", 42.7252301766572,
//				0.78295672095248, 0, 10, 115, t, null);
//		Step s116 = new Step("step_garonap_116", "step", 42.725108920988,
//				0.7832053134919, 0, 10, 116, t, null);
//		Step s117 = new Step("step_garonap_117", "step", 42.7250458023066,
//				0.783433513893019, 0, 10, 117, t, null);
//		Step s118 = new Step("step_garonap_118", "step", 42.7250009170016,
//				0.783673278065403, 0, 10, 118, t, null);
//		Step s119 = new Step("step_garonap_119", "step", 42.7249831456485,
//				0.783918183151125, 0, 10, 119, t, null);
//		Step s120 = new Step("step_garonap_120", "step", 42.7249961607641,
//				0.784125351062623, 0, 10, 120, t, null);
//		Step s121 = new Step("step_garonap_121", "step", 42.7250188829463,
//				0.784368814300015, 0, 10, 121, t, null);
//		Step s122 = new Step("step_garonap_122", "step", 42.725069073339,
//				0.784635727114057, 0, 10, 122, t, null);
//		Step s123 = new Step("step_garonap_123", "step", 42.7249861829274,
//				0.78477302764315, 0, 10, 123, t, null);
//		Step s124 = new Step("step_garonap_124", "step", 42.7248278653937,
//				0.784967973695518, 0, 10, 124, t, null);
//		Step s125 = new Step("step_garonap_125", "step", 42.7246094024482,
//				0.785079564573151, 0, 10, 125, t, null);
//		Step s126 = new Step("step_garonap_126", "step", 42.7244880369316,
//				0.785089990891862, 0, 10, 126, t, null);
//		Step s127 = new Step("step_garonap_127", "step", 42.7243576726261,
//				0.785100737434111, 0, 10, 127, t, null);
//		Step s128 = new Step("step_garonap_128", "step", 42.7239954650556,
//				0.785229656018561, 0, 10, 128, t, null);
//		Step s129 = new Step("step_garonap_129", "step", 42.7238721973571,
//				0.785374496887399, 0, 10, 129, t, null);
//		Step s130 = new Step("step_garonap_130", "step", 42.7237878893904,
//				0.78543856391992, 0, 10, 130, t, null);
//		Step s131 = new Step("step_garonap_131", "step", 42.7237297514173,
//				0.785458952781642, 0, 10, 131, t, null);
//		Step s132 = new Step("step_garonap_132", "step", 42.7234370408948,
//				0.785689208094464, 0, 10, 132, t, null);
//		Step s133 = new Step("step_garonap_133", "step", 42.72329269188,
//				0.785908076397926, 0, 10, 133, t, null);
//		Step s134 = new Step("step_garonap_134", "step", 42.7231516616865,
//				0.786065759363535, 0, 10, 134, t, null);
//		Step s135 = new Step("step_garonap_135", "step", 42.7229513130501,
//				0.786182805380926, 0, 10, 135, t, null);
//		Step s136 = new Step("step_garonap_136", "step", 42.7226533935179,
//				0.78637660007165, 0, 10, 136, t, null);
//		Step s137 = new Step("step_garonap_137", "step", 42.722512008637,
//				0.786515973223368, 0, 10, 137, t, null);
//		Step s138 = new Step("step_garonap_138", "step", 42.7222640537507,
//				0.786732414157625, 0, 10, 138, t, null);
//		Step s139 = new Step("step_garonap_139", "step", 42.7221117654385,
//				0.787006516822682, 0, 10, 139, t, null);
//		Step s140 = new Step("step_garonap_140", "step", 42.7219723848972,
//				0.78724962667851, 0, 10, 140, t, null);
//		Step s141 = new Step("step_garonap_141", "step", 42.7218810819743,
//				0.787417748304443, 0, 10, 141, t, null);
//		Step s142 = new Step("step_garonap_142", "step", 42.7218488606664,
//				0.787614301705813, 0, 10, 142, t, null);
//		Step s143 = new Step("step_garonap_143", "step", 42.7217833558366,
//				0.787952487232368, 0, 10, 143, t, null);
//		Step s144 = new Step("step_garonap_144", "step", 42.7216991450113,
//				0.788254697890897, 0, 10, 144, t, null);
//		Step s145 = new Step("step_garonap_145", "step", 42.7216091372802,
//				0.788489942487949, 0, 10, 145, t, null);
//		Step s146 = new Step("step_garonap_146", "step", 42.7215673259249,
//				0.788656302859154, 0, 10, 146, t, null);
//		Step s147 = new Step("step_garonap_147", "step", 42.721415032811,
//				0.788930397866985, 0, 10, 147, t, null);
//		Step s148 = new Step("step_garonap_148", "step", 42.7212439164675,
//				0.789162415068196, 0, 10, 148, t, null);
//		Step s149 = new Step("step_garonap_149", "step", 42.7210466279925,
//		0.789438105482576, 0, 10, 149, t, h16);
////		Step s149 = new Step("step_garonap_149", "step", 42.7210466279925,
////				0.789438105482576, 0, 10, 149, t, null);
//		Step s150 = new Step("step_garonap_150", "step", 42.7209093850462,
//				0.789559001622671, 0, 10, 150, t, h4);
//		Step s151 = new Step("step_garonap_151", "step", 42.7206524251199,
//				0.789775744385537, 0, 10, 151, t, null);
//		Step s152 = new Step("step_garonap_152", "step", 42.7205908470986,
//				0.789851207574949, 0, 10, 152, t, null);
//		Step s153 = new Step("step_garonap_153", "step", 42.7204309888135,
//				0.78996679853366, 0, 10, 153, t, null);
//		Step s154 = new Step("step_garonap_154", "step", 42.7203503528295,
//				0.7899879807745, 0, 10, 154, t, null);
//		Step s155 = new Step("step_garonap_155", "step", 42.7201910833427,
//				0.790134081681648, 0, 10, 155, t, null);
//		Step s156 = new Step("step_garonap_156", "step", 42.7200513424494,
//				0.790358870702432, 0, 10, 156, t, null);
//		Step s157 = new Step("step_garonap_157", "step", 42.7199674782682,
//				0.790679376260055, 0, 10, 157, t, null);
//		Step s158 = new Step("step_garonap_158", "step", 42.7199525423126,
//				0.790838670541259, 0, 10, 158, t, null);
//		Step s159 = new Step("step_garonap_159", "step", 42.7198839410076,
//				0.79125022788579, 0, 10, 159, t, null);
//		Step s160 = new Step("step_garonap_160", "step", 42.7197967519887,
//				0.791631912326017, 0, 10, 160, t, null);
//		Step s161 = new Step("step_garonap_161", "step", 42.7196790072623,
//				0.7920635299125, 0, 10, 161, t, null);
//		Step s162 = new Step("step_garonap_162", "step", 42.7195514383749,
//				0.792452750372908, 0, 10, 162, t, null);
//		Step s163 = new Step("step_garonap_163", "step", 42.719390842771,
//				0.792763759012425, 0, 10, 163, t, null);
//		Step s164 = new Step("step_garonap_164", "step", 42.7192918265516,
//				0.792999307682903, 0, 10, 164, t, null);
//		Step s165 = new Step("step_garonap_165", "step", 42.7192280994469,
//				0.793196967022475, 0, 10, 165, t, null);
//		Step s166 = new Step("step_garonap_166", "step", 42.7190940278244,
//				0.793482606935137, 0, 10, 166, t, null);
//		Step s167 = new Step("step_garonap_167", "step", 42.7189271659346,
//				0.793702239508606, 0, 10, 167, t, null);
//		Step s168 = new Step("step_garonap_168", "step", 42.7188407052766,
//				0.793888490283838, 0, 10, 168, t, null);
//		Step s169 = new Step("step_garonap_169", "step", 42.7188941967563,
//				0.794094205526611, 0, 10, 169, t, null);
//		Step s170 = new Step("step_garonap_170", "step", 42.7188818631107,
//				0.794166032101082, 0, 10, 170, t, null);
//		Step s171 = new Step("step_garonap_171", "step", 42.7187878737291,
//				0.794247206067629, 0, 10, 171, t, null);
//		Step s172 = new Step("step_garonap_172", "step", 42.7186742810488,
//				0.794364565233033, 0, 10, 172, t, null);
//		Step s173 = new Step("step_garonap_173", "step", 42.7185994366653,
//				0.794452703847136, 0, 10, 173, t, h17);
////		Step s173 = new Step("step_garonap_173", "step", 42.7185994366653,
////				0.794452703847136, 0, 10, 173, t, null);
//		Step s174 = new Step("step_garonap_174", "step", 42.7185187996814,
//				0.794473879586904, 0, 10, 174, t, null);
//		Step s175 = new Step("step_garonap_175", "step", 42.718399196049,
//				0.794575816038592, 0, 10, 175, t, h5);
//		Step s176 = new Step("step_garonap_176", "step", 42.7182575653611,
//				0.794702957078126, 0, 10, 176, t, null);
//		Step s177 = new Step("step_garonap_177", "step", 42.7181669561606,
//				0.794907669806896, 0, 10, 177, t, null);
//		Step s178 = new Step("step_garonap_178", "step", 42.7180533793527,
//				0.795088770983759, 0, 10, 178, t, null);
//		Step s179 = new Step("step_garonap_179", "step", 42.717981002167,
//				0.795305048952062, 0, 10, 179, t, null);
//		Step s180 = new Step("step_garonap_180", "step", 42.7178583083391,
//				0.795480365628268, 0, 10, 180, t, null);
//		Step s181 = new Step("step_garonap_181", "step", 42.7177261453958,
//				0.795631592550618, 0, 10, 181, t, null);
//		Step s182 = new Step("step_garonap_182", "step", 42.7175981292099,
//				0.795764353732163, 0, 10, 182, t, null);
//		Step s183 = new Step("step_garonap_183", "step", 42.7174286781445,
//				0.795849732976278, 0, 10, 183, t, null);
//		Step s184 = new Step("step_garonap_184", "step", 42.7173254262046,
//				0.79586560115653, 0, 10, 184, t, null);
//		Step s185 = new Step("step_garonap_185", "step", 42.7172082060507,
//				0.795857539692494, 0, 10, 185, t, null);
//		Step s186 = new Step("step_garonap_186", "step", 42.71705948988,
//				0.795850593487173, 0, 10, 186, t, null);
//		Step s187 = new Step("step_garonap_187", "step", 42.7169326834557,
//				0.795812341415924, 0, 10, 187, t, null);
//		Step s188 = new Step("step_garonap_188", "step", 42.7167882317454,
//				0.79579303231421, 0, 10, 188, t, null);
//		Step s189 = new Step("step_garonap_189", "step", 42.7165797906542,
//				0.7957888257786, 0, 10, 189, t, null);
//		Step s190 = new Step("step_garonap_190", "step", 42.7163662250747,
//				0.795856822852375, 0, 10, 190, t, null);
//		Step s191 = new Step("step_garonap_191", "step", 42.7161335788487,
//				0.795859432467507, 0, 10, 191, t, null);
//		Step s192 = new Step("step_garonap_192", "step", 42.7158532898864,
//				0.795874984548163, 0, 10, 192, t, null);
//		Step s193 = new Step("step_garonap_193", "step", 42.7157003092061,
//				0.795880401102269, 0, 10, 193, t, null);
//		Step s194 = new Step("step_garonap_194", "step", 42.7154571049314,
//				0.795876800351318, 0, 10, 194, t, null);
//		Step s195 = new Step("step_garonap_195", "step", 42.7152414846105,
//				0.795902752203113, 0, 10, 195, t, null);
//		Step s196 = new Step("step_garonap_196", "step", 42.7147558983136,
//				0.795938261931262, 0, 10, 196, t, null);
//		Step s197 = new Step("step_garonap_197", "step", 42.7145491593403,
//				0.795957792957934, 0, 10, 197, t, null);
//		Step s198 = new Step("step_garonap_198", "step", 42.714289954198,
//				0.796058556158272, 0, 10, 198, t, null);
//		Step s199 = new Step("step_garonap_199", "step", 42.7141113863756,
//				0.796138146678393, 0, 10, 199, t, null);
//		Step s200 = new Step("step_garonap_200", "step", 42.7139499940366,
//				0.796174388651822, 0, 10, 200, t, null);
//		Step s201 = new Step("step_garonap_201", "step", 42.7137561659995,
//				0.796162932963384, 0, 10, 201, t, h6);
//		Step s202 = new Step("step_garonap_202", "step", 42.7136200084001,
//				0.796106695960067, 0, 10, 202, t, h18);
////		Step s202 = new Step("step_garonap_202", "step", 42.7136200084001,
////				0.796106695960067, 0, 10, 202, t, null);
//		Step s203 = new Step("step_garonap_203", "step", 42.7134924972581,
//				0.796031836056873, 0, 10, 203, t, null);
//		Step s204 = new Step("step_garonap_204", "step", 42.7132908449452,
//				0.796081714426866, 0, 10, 204, t, null);
//		Step s205 = new Step("step_garonap_205", "step", 42.7130342597222,
//				0.796084691955194, 0, 10, 205, t, null);
//		Step s206 = new Step("step_garonap_206", "step", 42.7127364748228,
//				0.796052494235294, 0, 10, 206, t, null);
//		Step s207 = new Step("step_garonap_207", "step", 42.7123674037056,
//				0.796059453731284, 0, 10, 207, t, null);
//		Step s208 = new Step("step_garonap_208", "step", 42.7121554604227,
//				0.796042534208833, 0, 10, 208, t, null);
//		Step s209 = new Step("step_garonap_209", "step", 42.7120061568454,
//				0.796005080743564, 0, 10, 209, t, null);
//		Step s210 = new Step("step_garonap_210", "step", 42.7118385031277,
//				0.795949960494411, 0, 10, 210, t, null);
//		Step s211 = new Step("step_garonap_211", "step", 42.7116843476909,
//				0.795894362665415, 0, 10, 211, t, null);
//		Step s212 = new Step("step_garonap_212", "step", 42.7116159164305,
//				0.795847941262015, 0, 10, 212, t, null);
//		Step s213 = new Step("step_garonap_213", "step", 42.7114564392111,
//				0.795749793909824, 0, 10, 213, t, null);
//		Step s214 = new Step("step_garonap_214", "step", 42.711232442594,
//				0.795574560389983, 0, 10, 214, t, null);
//		Step s215 = new Step("step_garonap_215", "step", 42.7110636138299,
//				0.795458429094648, 0, 10, 215, t, null);
//		Step s216 = new Step("step_garonap_216", "step", 42.7108538200759,
//				0.795319327270523, 0, 10, 216, t, null);
//		Step s217 = new Step("step_garonap_217", "step", 42.7107173096058,
//				0.795244790748861, 0, 10, 217, t, null);
//		Step s218 = new Step("step_garonap_218", "step", 42.7105263358772,
//				0.795147761622871, 0, 10, 218, t, null);
//		Step s219 = new Step("step_garonap_219", "step", 42.7103730026206,
//				0.795134875317701, 0, 10, 219, t, null);
//		Step s220 = new Step("step_garonap_220", "step", 42.710268928124,
//				0.795108033979835, 0, 10, 220, t, null);
//		Step s221 = new Step("step_garonap_221", "step", 42.7102160753158,
//				0.795402962791543, 0, 10, 221, t, h7);
//		Step s222 = new Step("step_garonap_222", "step", 42.7099943106244,
//				0.795343656183616, 0, 10, 222, t, h20);
////		Step s222 = new Step("step_garonap_222", "step", 42.7099943106244,
////				0.795343656183616, 0, 10, 222, t, null);
//		Step s223 = new Step("step_garonap_223", "step", 42.7099896447435,
//				0.794867605223311, 0, 10, 223, t, h19);
//		Step s224 = new Step("step_garonap_224", "step", 42.7098643993585,
//				0.794676670538079, 0, 10, 224, t, null);
//		Step s225 = new Step("step_garonap_225", "step", 42.7097842655293,
//				0.79449024400067, 0, 10, 225, t, null);
//		Step s226 = new Step("step_garonap_226", "step", 42.7096598423578,
//				0.794342018686057, 0, 10, 226, t, null);
//		Step s227 = new Step("step_garonap_227", "step", 42.7095627682485,
//				0.794211140964105, 0, 10, 227, t, null);
//		Step s228 = new Step("step_garonap_228", "step", 42.70947813426,
//				0.794024875354, 0, 10, 228, t, null);
//		Step s229 = new Step("step_garonap_229", "step", 42.7093739738169,
//				0.79375993353112, 0, 10, 229, t, null);
//		Step s230 = new Step("step_garonap_230", "step", 42.7092981028091,
//				0.793561148037036, 0, 10, 230, t, null);
//		Step s231 = new Step("step_garonap_231", "step", 42.7092142909507,
//				0.793417591772048, 0, 10, 231, t, null);
//		Step s232 = new Step("step_garonap_232", "step", 42.7089220939664,
//				0.793208158336612, 0, 10, 232, t, null);
//		Step s233 = new Step("step_garonap_233", "step", 42.7086730965105,
//				0.793137615369244, 0, 10, 233, t, null);
//		Step s234 = new Step("step_garonap_234", "step", 42.7082887633132,
//				0.793053554085464, 0, 10, 234, t, null);
//		Step s235 = new Step("step_garonap_235", "step", 42.7081306952428,
//				0.793028630687249, 0, 10, 235, t, null);
//		Step s236 = new Step("step_garonap_236", "step", 42.7078604942661,
//				0.793025997122811, 0, 10, 236, t, null);
//		Step s237 = new Step("step_garonap_237", "step", 42.7076987500243,
//				0.793043939984704, 0, 10, 237, t, null);
//		Step s238 = new Step("step_garonap_238", "step", 42.7075771478165,
//				0.793042144734097, 0, 10, 238, t, null);
//		Step s239 = new Step("step_garonap_239", "step", 42.707495922865,
//				0.793032813336416, 0, 10, 239, t, null);
//		Step s240 = new Step("step_garonap_240", "step", 42.7074273729282,
//				0.792980297144086, 0, 10, 240, t, null);
//		Step s241 = new Step("step_garonap_241", "step", 42.7073807320407,
//				0.792896479131994, 0, 10, 241, t, null);
//		Step s242 = new Step("step_garonap_242", "step", 42.7073169139723,
//				0.79268004414661, 0, 10, 242, t, null);
//		Step s243 = new Step("step_garonap_243", "step", 42.7072632178131,
//				0.792406133891176, 0, 10, 243, t, null);
//		Step s244 = new Step("step_garonap_244", "step", 42.707227957117,
//				0.792212021920038, 0, 10, 244, t, null);
//		Step s245 = new Step("step_garonap_245", "step", 42.7072028715577,
//				0.79207859998812, 0, 10, 245, t, null);
//		Step s246 = new Step("step_garonap_246", "step", 42.7071457856474,
//				0.791978405000477, 0, 10, 246, t, null);
//		Step s247 = new Step("step_garonap_247", "step", 42.7070490068401,
//				0.791905251094096, 0, 10, 247, t, null);
//		Step s248 = new Step("step_garonap_248", "step", 42.7069323457608,
//				0.791825675528476, 0, 10, 248, t, null);
//		Step s249 = new Step("step_garonap_249", "step", 42.7067964456492,
//				0.791722442863761, 0, 10, 249, t, h21);
//		Step s250 = new Step("step_garonap_250", "step", 42.706476138579,
//				0.791750278022758, 0, 10, 250, t, null);
//		Step s251 = new Step("step_garonap_251", "step", 42.7062786344992,
//				0.791781701671464, 0, 10, 251, t, null);
//		Step s252 = new Step("step_garonap_252", "step", 42.706108715313,
//				0.79184267169908, 0, 10, 252, t, null);
//		Step s253 = new Step("step_garonap_253", "step", 42.7058759217621,
//				0.791911975727565, 0, 10, 253, t, null);
//		Step s254 = new Step("step_garonap_254", "step", 42.7056209839898,
//				0.792000379081069, 0, 10, 254, t, null);
//		Step s255 = new Step("step_garonap_255", "step", 42.7054097461854,
//				0.792020078559059, 0, 10, 255, t, null);
//		Step s256 = new Step("step_garonap_256", "step", 42.7051722177306,
//				0.792077339185851, 0, 10, 256, t, h22);
//		Step s257 = new Step("step_garonap_257", "step", 42.7050913168597,
//				0.792318296791535, 0, 10, 257, t, null);
//		Step s258 = new Step("step_garonap_258", "step", 42.7049473058145,
//				0.792555385975424, 0, 10, 258, t, null);
//		Step s259 = new Step("step_garonap_259", "step", 42.7046860435506,
//				0.793016403053459, 0, 10, 259, t, null);
//		Step s260 = new Step("step_garonap_260", "step", 42.7044424243274,
//				0.793458476737786, 0, 10, 260, t, null);
//		Step s261 = new Step("step_garonap_261", "step", 42.7041764236892,
//				0.793907444537171, 0, 10, 261, t, null);
//		Step s262 = new Step("step_garonap_262", "step", 42.7040463776417,
//				0.794168450853407, 0, 10, 262, t, null);
//		Step s263 = new Step("step_garonap_263", "step", 42.7040878128819,
//				0.794215821276987, 0, 10, 263, t, null);
//		Step s264 = new Step("step_garonap_264", "step", 42.7039609727565,
//				0.794409560857457, 0, 10, 264, t, null);
//		Step s265 = new Step("step_garonap_265", "step", 42.7038918357889,
//				0.794326543071416, 0, 10, 265, t, null);
//		Step s266 = new Step("step_garonap_266", "step", 42.7036822090476,
//				0.794663622045782, 0, 10, 266, t, null);
//		Step s267 = new Step("step_garonap_267", "step", 42.7034034100196,
//				0.795149660343416, 0, 10, 267, t, null);
//		Step s268 = new Step("step_garonap_268", "step", 42.7032588054003,
//				0.79535623404935, 0, 10, 268, t, null);
//		Step s269 = new Step("step_garonap_269", "step", 42.7031587249293,
//				0.795536812139352, 0, 10, 269, t, null);
//		Step s270 = new Step("step_garonap_270", "step", 42.7031077856421,
//				0.795697336379686, 0, 10, 270, t, h23);
//		Step s271 = new Step("step_garonap_271", "step", 42.7028996367606,
//				0.795643657148288, 0, 10, 271, t, h24);
//		Step s272 = new Step("step_garonap_272", "step", 42.7027225219291,
//				0.796266492895049, 0, 10, 272, t, null);
//		Step s273 = new Step("step_garonap_273", "step", 42.7028064474688,
//				0.796416139015297, 0, 10, 273, t, h25);
//		Step s274 = new Step("step_garonap_274", "step", 42.7019993667751,
//				0.797293227268689, 0, 10, 274, t, null);
//		Step s275 = new Step("step_garonap_275", "step", 42.7019048387498,
//				0.797528543050591, 0, 10, 275, t, null);
//		Step s276 = new Step("step_garonap_276", "step", 42.7016219967413,
//				0.79803911536514, 0, 10, 276, t, h26);
//		Step s277 = new Step("step_garonap_277", "step", 42.701823726914,
//				0.798227328393374, 0, 10, 277, t, null);
//		Step s278 = new Step("step_garonap_278", "step", 42.7019459389781,
//				0.798316295864678, 0, 10, 278, t, h27);
//		Step s279 = new Step("step_garonap_279", "step", 42.7018894540479,
//				0.798559230814839, 0, 10, 279, t, null);
//		Step s280 = new Step("step_garonap_280", "step", 42.7018212469798,
//				0.798775796973528, 0, 10, 280, t, null);
//		Step s281 = new Step("step_garonap_281", "step", 42.7017394458233,
//				0.798929576691971, 0, 10, 281, t, null);
//		Step s282 = new Step("step_garonap_282", "step", 42.7015540598688,
//				0.799189164884239, 0, 10, 282, t, null);
//		Step s283 = new Step("step_garonap_283", "step", 42.7014234149497,
//				0.79941964768088, 0, 10, 283, t, null);
//		Step s284 = new Step("step_garonap_284", "step", 42.7013071246281,
//				0.799563073469289, 0, 10, 284, t, null);
//		Step s285 = new Step("step_garonap_285", "step", 42.701203483898,
//				0.799689910655384, 0, 10, 285, t, null);
//		Step s286 = new Step("step_garonap_286", "step", 42.7010971910029,
//				0.79993885262039, 0, 10, 286, t, null);
//		Step s287 = new Step("step_garonap_287", "step", 42.7009556898023,
//				0.800150393857236, 0, 10, 287, t, null);
//		Step s288 = new Step("step_garonap_288", "step", 42.7008955104843,
//				0.800299026041539, 0, 10, 288, t, null);
//		Step s289 = new Step("step_garonap_289", "step", 42.700845970641,
//				0.800532744365657, 0, 10, 289, t, null);
//		Step s290 = new Step("step_garonap_290", "step", 42.7007905246423,
//				0.800693417647816, 0, 10, 290, t, null);
//		Step s291 = new Step("step_garonap_291", "step", 42.7007035822002,
//				0.800855202721635, 0, 10, 291, t, null);
//		Step s292 = new Step("step_garonap_292", "step", 42.700650994798,
//				0.800930312528757, 0, 10, 292, t, null);
//		Step s293 = new Step("step_garonap_293", "step", 42.7005579122099,
//				0.801006851908628, 0, 10, 293, t, null);
//		Step s294 = new Step("step_garonap_294", "step", 42.700449057795,
//				0.801199931486613, 0, 10, 294, t, null);
//		Step s295 = new Step("step_garonap_295", "step", 42.7003931422178,
//				0.801336202132914, 0, 10, 295, t, null);
//		Step s296 = new Step("step_garonap_296", "step", 42.7003343669913,
//				0.801558034933307, 0, 10, 296, t, null);
//		Step s297 = new Step("step_garonap_297", "step", 42.7002926521435,
//				0.801730430120081, 0, 10, 297, t, null);
//		Step s298 = new Step("step_garonap_298", "step", 42.7002470231672,
//				0.801933485116484, 0, 10, 298, t, null);
//		Step s299 = new Step("step_garonap_299", "step", 42.7002287419243,
//				0.80215388794023, 0, 10, 299, t, null);
//		Step s300 = new Step("step_garonap_300", "step", 42.7002144913334,
//				0.802349730908876, 0, 10, 300, t, null);
//		Step s301 = new Step("step_garonap_301", "step", 42.7001809545809,
//				0.802479106081099, 0, 10, 301, t, null);
//		Step s302 = new Step("step_garonap_302", "step", 42.7000961672942,
//				0.802518723682369, 0, 10, 302, t, null);
//		Step s303 = new Step("step_garonap_303", "step", 42.7000573113065,
//				0.802605555627353, 0, 10, 303, t, null);
//		Step s304 = new Step("step_garonap_304", "step", 42.7000549192785,
//				0.802715518550822, 0, 10, 304, t, null);
//		Step s305 = new Step("step_garonap_305", "step", 42.7000875862862,
//				0.802775409807448, 0, 10, 305, t, null);
//		Step s306 = new Step("step_garonap_306", "step", 42.7001148172232,
//				0.802786657934727, 0, 10, 306, t, null);
//		Step s307 = new Step("step_garonap_307", "step", 42.7000855452984,
//				0.802903673494532, 0, 10, 307, t, null);
//		Step s308 = new Step("step_garonap_308", "step", 42.7002356665676,
//				0.802983839375256, 0, 10, 308, t, h28);
//		Step s309 = new Step("step_garonap_309", "step", 42.7002925999779,
//				0.803604478576593, 0, 10, 309, t, null);
//		Step s310 = new Step("step_garonap_310", "step", 42.7003013763198,
//				0.804061998164675, 0, 10, 310, t, null);
//		Step s311 = new Step("step_garonap_311", "step", 42.7003134172972,
//				0.804220287759198, 0, 10, 311, t, null);
//		Step s312 = new Step("step_garonap_312", "step", 42.7002673159158,
//				0.804398940140014, 0, 10, 312, t, null);
//		Step s313 = new Step("step_garonap_313", "step", 42.7001226938091,
//				0.804605482350391, 0, 10, 313, t, null);
//		Step s314 = new Step("step_garonap_314", "step", 42.6999853157341,
//				0.804720202929586, 0, 10, 314, t, null);
//		Step s315 = new Step("step_garonap_315", "step", 42.6998448415974,
//				0.804908284222837, 0, 10, 315, t, null);
//		Step s316 = new Step("step_garonap_316", "step", 42.6997485431975,
//				0.805052077624011, 0, 10, 316, t, null);
//		Step s317 = new Step("step_garonap_317", "step", 42.699632960313,
//				0.805129402554399, 0, 10, 317, t, null);
//		Step s318 = new Step("step_garonap_318", "step", 42.6994384917731,
//				0.80531938431058, 0, 10, 318, t, null);
//		Step s319 = new Step("step_garonap_319", "step", 42.6992177275928,
//				0.805546916979539, 0, 10, 319, t, null);
//		Step s320 = new Step("step_garonap_320", "step", 42.6990106951633,
//				0.805786172800393, 0, 10, 320, t, null);
//		Step s321 = new Step("step_garonap_321", "step", 42.6988824911777,
//				0.806144735438104, 0, 10, 321, t, null);
//		Step s322 = new Step("step_garonap_322", "step", 42.698861922867,
//				0.806481193466609, 0, 10, 322, t, null);
//		Step s323 = new Step("step_garonap_323", "step", 42.6987409627537,
//				0.806747935230372, 0, 10, 323, t, null);
//		Step s324 = new Step("step_garonap_324", "step", 42.6986280661819,
//				0.806965558266134, 0, 10, 324, t, null);
//		Step s325 = new Step("step_garonap_325", "step", 42.6985496461207,
//				0.807102611987361, 0, 10, 325, t, null);
//		Step s326 = new Step("step_garonap_326", "step", 42.6984548635894,
//				0.80732570026897, 0, 10, 326, t, null);
//		Step s327 = new Step("step_garonap_327", "step", 42.6983909928881,
//				0.807517178838267, 0, 10, 327, t, null);
//		Step s328 = new Step("step_garonap_328", "step", 42.6982668155926,
//				0.807851175853119, 0, 10, 328, t, null);
//		Step s329 = new Step("step_garonap_329", "step", 42.6982038782877,
//				0.808091453921972, 0, 10, 329, t, null);
//		Step s330 = new Step("step_garonap_330", "step", 42.6980976971985,
//				0.808424815617884, 0, 10, 330, t, h8);
//		Step s331 = new Step("step_garonap_331", "step", 42.6982390494523,
//				0.808517509368717, 0, 10, 331, t, h29);
////		Step s331 = new Step("step_garonap_331", "step", 42.6982390494523,
////				0.808517509368717, 0, 10, 331, t, null);
//		Step s332 = new Step("step_garonap_332", "step", 42.6982581522004,
//				0.808809838358955, 0, 10, 332, t, null);
//		Step s333 = new Step("step_garonap_333", "step", 42.698253939641,
//				0.809060258270997, 0, 10, 333, t, null);
//		Step s334 = new Step("step_garonap_334", "step", 42.6982614100193,
//				0.809450663628147, 0, 10, 334, t, null);
//		Step s335 = new Step("step_garonap_335", "step", 42.6982878755528,
//				0.80965727544426, 0, 10, 335, t, null);
//		Step s336 = new Step("step_garonap_336", "step", 42.6982073376839,
//				0.810154546359384, 0, 10, 336, t, null);
//		Step s337 = new Step("step_garonap_337", "step", 42.6981542111028,
//				0.810437206287912, 0, 10, 337, t, null);
//		Step s338 = new Step("step_garonap_338", "step", 42.6981028334707,
//				0.810811366880503, 0, 10, 338, t, null);
//		Step s339 = new Step("step_garonap_339", "step", 42.6980329901453,
//				0.811161759052069, 0, 10, 339, t, null);
//		Step s340 = new Step("step_garonap_340", "step", 42.6979958766337,
//				0.811340083939453, 0, 10, 340, t, null);
//		Step s341 = new Step("step_garonap_341", "step", 42.6980177226605,
//				0.81154075383399, 0, 10, 341, t, null);
//		Step s342 = new Step("step_garonap_342", "step", 42.6980625321716,
//				0.811765033850573, 0, 10, 342, t, null);
//		Step s343 = new Step("step_garonap_343", "step", 42.6980639783748,
//				0.812076295756864, 0, 10, 343, t, null);
//		Step s344 = new Step("step_garonap_344", "step", 42.6980439752571,
//				0.812443248491826, 0, 10, 344, t, null);
//		Step s345 = new Step("step_garonap_345", "step", 42.6980284704543,
//				0.812810043050314, 0, 10, 345, t, null);
//		Step s346 = new Step("step_garonap_346", "step", 42.6981162642845,
//				0.813399065475173, 0, 10, 346, t, null);
//		Step s347 = new Step("step_garonap_347", "step", 42.6982065311485,
//				0.81364616840453, 0, 10, 347, t, null);
//		Step s348 = new Step("step_garonap_348", "step", 42.6982221264275,
//				0.813755496383607, 0, 10, 348, t, null);
//		Step s349 = new Step("step_garonap_349", "step", 42.6982091369783,
//				0.814018432267375, 0, 10, 349, t, null);
//		Step s350 = new Step("step_garonap_350", "step", 42.6981786147425,
//				0.814306399987137, 0, 10, 350, t, null);
//		Step s351 = new Step("step_garonap_351", "step", 42.6982521625217,
//				0.814621237167462, 0, 10, 351, t, null);
//		Step s352 = new Step("step_garonap_352", "step", 42.6982666337971,
//				0.814907626466275, 0, 10, 352, t, null);
//		Step s353 = new Step("step_garonap_353", "step", 42.6982951849087,
//				0.815224043030397, 0, 10, 353, t, null);
//		Step s354 = new Step("step_garonap_354", "step", 42.6982909583089,
//				0.815474463236999, 0, 10, 354, t, null);
//		Step s355 = new Step("step_garonap_355", "step", 42.698291696184,
//				0.815749126105804, 0, 10, 355, t, null);
//		Step s356 = new Step("step_garonap_356", "step", 42.6983031390813,
//				0.815876912958879, 0, 10, 356, t, null);
//		Step s357 = new Step("step_garonap_357", "step", 42.6983435570402,
//				0.816107455339153, 0, 10, 357, t, null);
//		Step s358 = new Step("step_garonap_358", "step", 42.6983702433235,
//				0.816326270998531, 0, 10, 358, t, null);
//		Step s359 = new Step("step_garonap_359", "step", 42.6983979762103,
//				0.816599988007018, 0, 10, 359, t, null);
//		Step s360 = new Step("step_garonap_360", "step", 42.6984161278298,
//				0.81684352003045, 0, 10, 360, t, null);
//		Step s361 = new Step("step_garonap_361", "step", 42.6985501030758,
//				0.817021951017111, 0, 10, 361, t, null);
//		Step s362 = new Step("step_garonap_362", "step", 42.6986465700238,
//				0.817122342173221, 0, 10, 362, t, null);
//		Step s363 = new Step("step_garonap_363", "step", 42.6987371417396,
//				0.817149689356138, 0, 10, 363, t, null);
//		Step s364 = new Step("step_garonap_364", "step", 42.6987695687377,
//				0.817197386925544, 0, 10, 364, t, null);
//		Step s365 = new Step("step_garonap_365", "step", 42.6987827544493,
//				0.817416677609585, 0, 10, 365, t, null);
//		Step s366 = new Step("step_garonap_366", "step", 42.6989121130524,
//				0.81758916835104, 0, 10, 366, t, null);
//		Step s367 = new Step("step_garonap_367", "step", 42.6989581898297,
//				0.817880558462921, 0, 10, 367, t, null);
//		Step s368 = new Step("step_garonap_368", "step", 42.6990294371346,
//				0.818311465499376, 0, 10, 368, t, null);
//		Step s369 = new Step("step_garonap_369", "step", 42.6990771392656,
//				0.818688259596035, 0, 10, 369, t, null);
//		Step s370 = new Step("step_garonap_370", "step", 42.6990898572991,
//				0.818883150976211, 0, 10, 370, t, null);
//		Step s371 = new Step("step_garonap_371", "step", 42.6991223160772,
//				0.819168915405873, 0, 10, 371, t, null);
//		Step s372 = new Step("step_garonap_372", "step", 42.6991484472492,
//				0.819595300082534, 0, 10, 372, t, null);
//		Step s373 = new Step("step_garonap_373", "step", 42.6990439439758,
//				0.819782086290741, 0, 10, 373, t, null);
//		Step s374 = new Step("step_garonap_374", "step", 42.6990575893824,
//				0.820025779513071, 0, 10, 374, t, null);
//		Step s375 = new Step("step_garonap_375", "step", 42.6990656894823,
//				0.820214728451568, 0, 10, 375, t, null);
//		Step s376 = new Step("step_garonap_376", "step", 42.6989848440728,
//				0.820461728179917, 0, 10, 376, t, null);
//		Step s377 = new Step("step_garonap_377", "step", 42.6989390653329,
//				0.820658666318526, 0, 10, 377, t, null);
//		Step s378 = new Step("step_garonap_378", "step", 42.6989904179125,
//				0.820754537845457, 0, 10, 378, t, null);
//		Step s379 = new Step("step_garonap_379", "step", 42.6990242367668,
//				0.820875440012328, 0, 10, 379, t, null);
//		Step s380 = new Step("step_garonap_380", "step", 42.6990887676933,
//				0.821190604709674, 0, 10, 380, t, null);
//		Step s381 = new Step("step_garonap_381", "step", 42.6991617164347,
//				0.821474954248126, 0, 10, 381, t, null);
//		Step s382 = new Step("step_garonap_382", "step", 42.6992246208774,
//				0.821704717114926, 0, 10, 382, t, null);
//		Step s383 = new Step("step_garonap_383", "step", 42.6992452883503,
//				0.821844393017998, 0, 10, 383, t, null);
//		Step s384 = new Step("step_garonap_384", "step", 42.6992972202195,
//				0.821970767210281, 0, 10, 384, t, null);
//		Step s385 = new Step("step_garonap_385", "step", 42.6992831676281,
//				0.822178804574617, 0, 10, 385, t, null);
//		Step s386 = new Step("step_garonap_386", "step", 42.6992856043683,
//				0.822306909552628, 0, 10, 386, t, null);
//		Step s387 = new Step("step_garonap_387", "step", 42.699329580901,
//				0.822488501015807, 0, 10, 387, t, null);
//		Step s388 = new Step("step_garonap_388", "step", 42.6993145992689,
//				0.822647736476098, 0, 10, 388, t, null);
//		Step s389 = new Step("step_garonap_389", "step", 42.6994358113832,
//				0.823339390589776, 0, 10, 389, t, null);
//		Step s390 = new Step("step_garonap_390", "step", 42.6995065515336,
//				0.823507839412039, 0, 10, 390, t, null);
//		Step s391 = new Step("step_garonap_391", "step", 42.6996531326316,
//				0.824113150542566, 0, 10, 391, t, null);
//		Step s392 = new Step("step_garonap_392", "step", 42.6997365587615,
//				0.824238427331007, 0, 10, 392, t, h9);
//		Step s393 = new Step("step_garonap_393", "step", 42.6998143259799,
//				0.824302858646369, 0, 10, 393, t, h30);
////		Step s393 = new Step("step_garonap_393", "step", 42.6998143259799,
////				0.824302858646369, 0, 10, 393, t, null);
//		Step s394 = new Step("step_garonap_394", "step", 42.6999268356069,
//				0.824536998609412, 0, 10, 394, t, null);
//		Step s395 = new Step("step_garonap_395", "step", 42.7000430534772,
//				0.824966349726837, 0, 10, 395, t, null);
//		Step s396 = new Step("step_garonap_396", "step", 42.7001630543903,
//				0.825120873220563, 0, 10, 396, t, null);
//		Step s397 = new Step("step_garonap_397", "step", 42.7002915906641,
//				0.82525068171424, 0, 10, 397, t, null);
//		Step s398 = new Step("step_garonap_398", "step", 42.7003362578314,
//				0.825468880470861, 0, 10, 398, t, null);
//		Step s399 = new Step("step_garonap_399", "step", 42.7004394185255,
//				0.825685037613229, 0, 10, 399, t, null);
//		Step s400 = new Step("step_garonap_400", "step", 42.7005888484233,
//				0.82596672907845, 0, 10, 400, t, null);
//		Step s401 = new Step("step_garonap_401", "step", 42.7006695105368,
//				0.826183673134563, 0, 10, 401, t, null);
//		Step s402 = new Step("step_garonap_402", "step", 42.7008467297918,
//				0.826269054714255, 0, 10, 402, t, null);
//		Step s403 = new Step("step_garonap_403", "step", 42.700949194152,
//				0.826448612478708, 0, 10, 403, t, null);
//		Step s404 = new Step("step_garonap_404", "step", 42.7009762083657,
//				0.826685743754815, 0, 10, 404, t, null);
//		Step s405 = new Step("step_garonap_405", "step", 42.7010594164841,
//				0.82703689958388, 0, 10, 405, t, null);
//		Step s406 = new Step("step_garonap_406", "step", 42.7012429871273,
//				0.827219734312425, 0, 10, 406, t, null);
//		Step s407 = new Step("step_garonap_407", "step", 42.7013401401484,
//				0.827356748558449, 0, 10, 407, t, null);
//		Step s408 = new Step("step_garonap_408", "step", 42.7014113376129,
//				0.827549609117265, 0, 10, 408, t, null);
//		Step s409 = new Step("step_garonap_409", "step", 42.7015420704791,
//				0.827795334358408, 0, 10, 409, t, null);
//		Step s410 = new Step("step_garonap_410", "step", 42.7015644666919,
//				0.828026525006436, 0, 10, 410, t, null);
//		Step s411 = new Step("step_garonap_411", "step", 42.7016012868114,
//				0.828306049065754, 0, 10, 411, t, null);
//		Step s412 = new Step("step_garonap_412", "step", 42.7016269087665,
//				0.828469977875742, 0, 10, 412, t, null);
//		Step s413 = new Step("step_garonap_413", "step", 42.7016688058219,
//				0.828779848146594, 0, 10, 413, t, null);
//		Step s414 = new Step("step_garonap_414", "step", 42.70174104207,
//				0.829027615977415, 0, 10, 414, t, null);
//		Step s415 = new Step("step_garonap_415", "step", 42.7017508625148,
//				0.829308082428259, 0, 10, 415, t, null);
//		Step s416 = new Step("step_garonap_416", "step", 42.7018481287825,
//				0.829451201637618, 0, 10, 416, t, null);
//		Step s417 = new Step("step_garonap_417", "step", 42.7018952059939,
//				0.829559442945178, 0, 10, 417, t, null);
//		Step s418 = new Step("step_garonap_418", "step", 42.7019593561074,
//				0.829618253013644, 0, 10, 418, t, null);
//		Step s419 = new Step("step_garonap_419", "step", 42.7019835988199,
//				0.829947054526833, 0, 10, 419, t, null);
//		Step s420 = new Step("step_garonap_420", "step", 42.7019966447902,
//				0.83016025966969, 0, 10, 420, t, null);
//		Step s421 = new Step("step_garonap_421", "step", 42.7020048439932,
//				0.830355320052075, 0, 10, 421, t, null);
//		Step s422 = new Step("step_garonap_422", "step", 42.7020223887533,
//				0.830568368655385, 0, 10, 422, t, null);
//		Step s423 = new Step("step_garonap_423", "step", 42.7020139760988,
//				0.830837262525609, 0, 10, 423, t, null);
//		Step s424 = new Step("step_garonap_424", "step", 42.7020314043932,
//				0.831044210791007, 0, 10, 424, t, null);
//		Step s425 = new Step("step_garonap_425", "step", 42.7020406421403,
//				0.831294176350437, 0, 10, 425, t, null);
//		Step s426 = new Step("step_garonap_426", "step", 42.7021217565921,
//				0.831535534912694, 0, 10, 426, t, null);
//		Step s427 = new Step("step_garonap_427", "step", 42.7021281111581,
//				0.831871065350929, 0, 10, 427, t, null);
//		Step s428 = new Step("step_garonap_428", "step", 42.7021802632923,
//				0.832009655232063, 0, 10, 428, t, null);
//		Step s429 = new Step("step_garonap_429", "step", 42.7021915727575,
//				0.832131353207038, 0, 10, 429, t, null);
//		Step s430 = new Step("step_garonap_430", "step", 42.7022295328773,
//				0.832233809884104, 0, 10, 430, t, null);
//		Step s431 = new Step("step_garonap_431", "step", 42.7022096917504,
//				0.832374905904706, 0, 10, 431, t, null);
//		Step s432 = new Step("step_garonap_432", "step", 42.7021453168965,
//				0.832541969966642, 0, 10, 432, t, null);
//		Step s433 = new Step("step_garonap_433", "step", 42.7020951333478,
//				0.832745167294073, 0, 10, 433, t, null);
//		Step s434 = new Step("step_garonap_434", "step", 42.7020939826358,
//				0.832922239955123, 0, 10, 434, t, null);
//		Step s435 = new Step("step_garonap_435", "step", 42.7020967538842,
//				0.833068653293017, 0, 10, 435, t, null);
//		Step s436 = new Step("step_garonap_436", "step", 42.7020991785731,
//				0.833196764980653, 0, 10, 436, t, null);
//		Step s437 = new Step("step_garonap_437", "step", 42.7020821040073,
//				0.833246195580775, 0, 10, 437, t, null);
//		Step s438 = new Step("step_garonap_438", "step", 42.7020842975994,
//				0.833362106140984, 0, 10, 438, t, null);
//		Step s439 = new Step("step_garonap_439", "step", 42.7019899250753,
//				0.833609571623513, 0, 10, 439, t, null);
//		Step s440 = new Step("step_garonap_440", "step", 42.7019307404864,
//				0.833813080643969, 0, 10, 440, t, null);
//		Step s441 = new Step("step_garonap_441", "step", 42.7019547387651,
//				0.833891605262984, 0, 10, 441, t, null);
//		Step s442 = new Step("step_garonap_442", "step", 42.7019286647782,
//				0.833941348538168, 0, 10, 442, t, null);
//		Step s443 = new Step("step_garonap_443", "step", 42.7019227818294,
//				0.834106376164522, 0, 10, 443, t, null);
//		Step s444 = new Step("step_garonap_444", "step", 42.7019108988524,
//				0.834192253174528, 0, 10, 444, t, null);
//		Step s445 = new Step("step_garonap_445", "step", 42.7018547126228,
//				0.834316297979857, 0, 10, 445, t, null);
//		Step s446 = new Step("step_garonap_446", "step", 42.7018072945875,
//				0.834427828601895, 0, 10, 446, t, h31);
//		Step s447 = new Step("step_garonap_447", "step", 42.7017895271361,
//				0.834440655460724, 0, 10, 447, t, null);
//		Step s448 = new Step("step_garonap_448", "step", 42.7017781047362,
//				0.834312857218621, 0, 10, 448, t, null);
//		Step s449 = new Step("step_garonap_449", "step", 42.7017624134452,
//				0.834197416551358, 0, 10, 449, t, null);
//		Step s450 = new Step("step_garonap_450", "step", 42.7017181089228,
//				0.833997507464477, 0, 10, 450, t, null);
//		Step s451 = new Step("step_garonap_451", "step", 42.7016875335769,
//				0.833809330231786, 0, 10, 451, t, null);
//		Step s452 = new Step("step_garonap_452", "step", 42.7016473816372,
//				0.833590963636796, 0, 10, 452, t, null);
//		Step s453 = new Step("step_garonap_453", "step", 42.7016123060762,
//				0.833402943365369, 0, 10, 453, t, null);
//		Step s454 = new Step("step_garonap_454", "step", 42.7016147275521,
//				0.833292977659579, 0, 10, 454, t, null);
//		Step s455 = new Step("step_garonap_455", "step", 42.7015802288119,
//				0.833135460202849, 0, 10, 455, t, null);
//		Step s456 = new Step("step_garonap_456", "step", 42.7015175737484,
//				0.832679801249094, 0, 10, 456, t, null);
//		Step s457 = new Step("step_garonap_457", "step", 42.7014836516356,
//				0.832552786880139, 0, 10, 457, t, null);
//		Step s458 = new Step("step_garonap_458", "step", 42.7014582664971,
//				0.832401057507168, 0, 10, 458, t, null);
//		Step s459 = new Step("step_garonap_459", "step", 42.7014148829508,
//				0.832249954682394, 0, 10, 459, t, null);
//		Step s460 = new Step("step_garonap_460", "step", 42.7013678082535,
//				0.83214171205647, 0, 10, 460, t, null);
//		Step s461 = new Step("step_garonap_461", "step", 42.701319000647,
//				0.831941962460644, 0, 10, 461, t, null);
//		Step s462 = new Step("step_garonap_462", "step", 42.7012781519841,
//				0.831686995675128, 0, 10, 462, t, null);
//		Step s463 = new Step("step_garonap_463", "step", 42.7012344208796,
//				0.831517592421824, 0, 10, 463, t, null);
//		Step s464 = new Step("step_garonap_464", "step", 42.7011910361912,
//				0.831366490797223, 0, 10, 464, t, null);
//		Step s465 = new Step("step_garonap_465", "step", 42.7010763518768,
//				0.831254498915946, 0, 10, 465, t, null);
//		Step s466 = new Step("step_garonap_466", "step", 42.7010345847609,
//				0.831188804088818, 0, 10, 466, t, null);
//		Step s467 = new Step("step_garonap_467", "step", 42.7009998520379,
//				0.83101908848995, 0, 10, 467, t, null);
//		Step s468 = new Step("step_garonap_468", "step", 42.7009797735023,
//				0.83090990736074, 0, 10, 468, t, null);
//		Step s469 = new Step("step_garonap_469", "step", 42.7009855363059,
//				0.830738781686243, 0, 10, 469, t, null);
//		Step s470 = new Step("step_garonap_470", "step", 42.7009408790549,
//				0.830520576481157, 0, 10, 470, t, null);
//		Step s471 = new Step("step_garonap_471", "step", 42.700917909854,
//				0.830258884888131, 0, 10, 471, t, null);
//		Step s472 = new Step("step_garonap_472", "step", 42.7008334495184,
//				0.830078693666048, 0, 10, 472, t, null);
//		Step s473 = new Step("step_garonap_473", "step", 42.700772411674,
//				0.82994652239823, 0, 10, 473, t, null);
//		Step s474 = new Step("step_garonap_474", "step", 42.7007039934501,
//				0.829900070640326, 0, 10, 474, t, null);
//		Step s475 = new Step("step_garonap_475", "step", 42.7005537839118,
//				0.830051810215482, 0, 10, 475, t, h32);
//		Step s476 = new Step("step_garonap_476", "step", 42.700507179347,
//				0.83020604426604, 0, 10, 476, t, null);
//		Step s477 = new Step("step_garonap_477", "step", 42.7005010704958,
//				0.830358867473515, 0, 10, 477, t, null);
//		Step s478 = new Step("step_garonap_478", "step", 42.7004552746822,
//				0.830555803875438, 0, 10, 478, t, null);
//		Step s479 = new Step("step_garonap_479", "step", 42.7004297801009,
//				0.830636049179791, 0, 10, 479, t, null);
//		Step s480 = new Step("step_garonap_480", "step", 42.7003320615113,
//				0.830706601016693, 0, 10, 480, t, null);
//		Step s481 = new Step("step_garonap_481", "step", 42.7002602999057,
//				0.830721309099512, 0, 10, 481, t, null);
//		Step s482 = new Step("step_garonap_482", "step", 42.7001879603681,
//				0.830705515345847, 0, 10, 482, t, null);
//		Step s483 = new Step("step_garonap_483", "step", 42.7001560011789,
//				0.830682210880768, 0, 10, 483, t, null);
//		Step s484 = new Step("step_garonap_484", "step", 42.7001152740652,
//				0.830671420567829, 0, 10, 484, t, null);
//		Step s485 = new Step("step_garonap_485", "step", 42.699964369932,
//				0.830786554801424, 0, 10, 485, t, null);
//		Step s486 = new Step("step_garonap_486", "step", 42.6998764593605,
//				0.830899494765724, 0, 10, 486, t, null);
//		Step s487 = new Step("step_garonap_487", "step", 42.6997755122938,
//				0.831037305746708, 0, 10, 487, t, null);
//		Step s488 = new Step("step_garonap_488", "step", 42.6996782556693,
//				0.831132257426743, 0, 10, 488, t, null);
//		Step s489 = new Step("step_garonap_489", "step", 42.6994911234993,
//				0.83123644245166, 0, 10, 489, t, null);
//		Step s490 = new Step("step_garonap_490", "step", 42.6993381387687,
//				0.831241769100076, 0, 10, 490, t, null);
//		Step s491 = new Step("step_garonap_491", "step", 42.699289560869,
//				0.831054227266137, 0, 10, 491, t, null);
//		Step s492 = new Step("step_garonap_492", "step", 42.6992128295946,
//				0.830806623238661, 0, 10, 492, t, null);
//		Step s493 = new Step("step_garonap_493", "step", 42.6991403661616,
//				0.830546662612537, 0, 10, 493, t, h33);
//
//		try {
//			stepDataDao.create(s1);
//			stepDataDao.create(s2);
//			stepDataDao.create(s3);
//			stepDataDao.create(s4);
//			stepDataDao.create(s5);
//			stepDataDao.create(s6);
//			stepDataDao.create(s7);
//			stepDataDao.create(s8);
//			stepDataDao.create(s9);
//			stepDataDao.create(s10);
//			stepDataDao.create(s11);
//			stepDataDao.create(s12);
//			stepDataDao.create(s13);
//			stepDataDao.create(s14);
//			stepDataDao.create(s15);
//			stepDataDao.create(s16);
//			stepDataDao.create(s17);
//			stepDataDao.create(s18);
//			stepDataDao.create(s19);
//			stepDataDao.create(s20);
//			stepDataDao.create(s21);
//			stepDataDao.create(s22);
//			stepDataDao.create(s23);
//			stepDataDao.create(s24);
//			stepDataDao.create(s25);
//			stepDataDao.create(s26);
//			stepDataDao.create(s27);
//			stepDataDao.create(s28);
//			stepDataDao.create(s29);
//			stepDataDao.create(s30);
//			stepDataDao.create(s31);
//			stepDataDao.create(s32);
//			stepDataDao.create(s33);
//			stepDataDao.create(s34);
//			stepDataDao.create(s35);
//			stepDataDao.create(s36);
//			stepDataDao.create(s37);
//			stepDataDao.create(s38);
//			stepDataDao.create(s39);
//			stepDataDao.create(s40);
//			stepDataDao.create(s41);
//			stepDataDao.create(s42);
//			stepDataDao.create(s43);
//			stepDataDao.create(s44);
//			stepDataDao.create(s45);
//			stepDataDao.create(s46);
//			stepDataDao.create(s47);
//			stepDataDao.create(s48);
//			stepDataDao.create(s49);
//			stepDataDao.create(s50);
//			stepDataDao.create(s51);
//			stepDataDao.create(s52);
//			stepDataDao.create(s53);
//			stepDataDao.create(s54);
//			stepDataDao.create(s55);
//			stepDataDao.create(s56);
//			stepDataDao.create(s57);
//			stepDataDao.create(s58);
//			stepDataDao.create(s59);
//			stepDataDao.create(s60);
//			stepDataDao.create(s61);
//			stepDataDao.create(s62);
//			stepDataDao.create(s63);
//			stepDataDao.create(s64);
//			stepDataDao.create(s65);
//			stepDataDao.create(s66);
//			stepDataDao.create(s67);
//			stepDataDao.create(s68);
//			stepDataDao.create(s69);
//			stepDataDao.create(s70);
//			stepDataDao.create(s71);
//			stepDataDao.create(s72);
//			stepDataDao.create(s73);
//			stepDataDao.create(s74);
//			stepDataDao.create(s75);
//			stepDataDao.create(s76);
//			stepDataDao.create(s77);
//			stepDataDao.create(s78);
//			stepDataDao.create(s79);
//			stepDataDao.create(s80);
//			stepDataDao.create(s81);
//			stepDataDao.create(s82);
//			stepDataDao.create(s83);
//			stepDataDao.create(s84);
//			stepDataDao.create(s85);
//			stepDataDao.create(s86);
//			stepDataDao.create(s87);
//			stepDataDao.create(s88);
//			stepDataDao.create(s89);
//			stepDataDao.create(s90);
//			stepDataDao.create(s91);
//			stepDataDao.create(s92);
//			stepDataDao.create(s93);
//			stepDataDao.create(s94);
//			stepDataDao.create(s95);
//			stepDataDao.create(s96);
//			stepDataDao.create(s97);
//			stepDataDao.create(s98);
//			stepDataDao.create(s99);
//			stepDataDao.create(s100);
//			stepDataDao.create(s101);
//			stepDataDao.create(s102);
//			stepDataDao.create(s103);
//			stepDataDao.create(s104);
//			stepDataDao.create(s105);
//			stepDataDao.create(s106);
//			stepDataDao.create(s107);
//			stepDataDao.create(s108);
//			stepDataDao.create(s109);
//			stepDataDao.create(s110);
//			stepDataDao.create(s111);
//			stepDataDao.create(s112);
//			stepDataDao.create(s113);
//			stepDataDao.create(s114);
//			stepDataDao.create(s115);
//			stepDataDao.create(s116);
//			stepDataDao.create(s117);
//			stepDataDao.create(s118);
//			stepDataDao.create(s119);
//			stepDataDao.create(s120);
//			stepDataDao.create(s121);
//			stepDataDao.create(s122);
//			stepDataDao.create(s123);
//			stepDataDao.create(s124);
//			stepDataDao.create(s125);
//			stepDataDao.create(s126);
//			stepDataDao.create(s127);
//			stepDataDao.create(s128);
//			stepDataDao.create(s129);
//			stepDataDao.create(s130);
//			stepDataDao.create(s131);
//			stepDataDao.create(s132);
//			stepDataDao.create(s133);
//			stepDataDao.create(s134);
//			stepDataDao.create(s135);
//			stepDataDao.create(s136);
//			stepDataDao.create(s137);
//			stepDataDao.create(s138);
//			stepDataDao.create(s139);
//			stepDataDao.create(s140);
//			stepDataDao.create(s141);
//			stepDataDao.create(s142);
//			stepDataDao.create(s143);
//			stepDataDao.create(s144);
//			stepDataDao.create(s145);
//			stepDataDao.create(s146);
//			stepDataDao.create(s147);
//			stepDataDao.create(s148);
//			stepDataDao.create(s149);
//			stepDataDao.create(s150);
//			stepDataDao.create(s151);
//			stepDataDao.create(s152);
//			stepDataDao.create(s153);
//			stepDataDao.create(s154);
//			stepDataDao.create(s155);
//			stepDataDao.create(s156);
//			stepDataDao.create(s157);
//			stepDataDao.create(s158);
//			stepDataDao.create(s159);
//			stepDataDao.create(s160);
//			stepDataDao.create(s161);
//			stepDataDao.create(s162);
//			stepDataDao.create(s163);
//			stepDataDao.create(s164);
//			stepDataDao.create(s165);
//			stepDataDao.create(s166);
//			stepDataDao.create(s167);
//			stepDataDao.create(s168);
//			stepDataDao.create(s169);
//			stepDataDao.create(s170);
//			stepDataDao.create(s171);
//			stepDataDao.create(s172);
//			stepDataDao.create(s173);
//			stepDataDao.create(s174);
//			stepDataDao.create(s175);
//			stepDataDao.create(s176);
//			stepDataDao.create(s177);
//			stepDataDao.create(s178);
//			stepDataDao.create(s179);
//			stepDataDao.create(s180);
//			stepDataDao.create(s181);
//			stepDataDao.create(s182);
//			stepDataDao.create(s183);
//			stepDataDao.create(s184);
//			stepDataDao.create(s185);
//			stepDataDao.create(s186);
//			stepDataDao.create(s187);
//			stepDataDao.create(s188);
//			stepDataDao.create(s189);
//			stepDataDao.create(s190);
//			stepDataDao.create(s191);
//			stepDataDao.create(s192);
//			stepDataDao.create(s193);
//			stepDataDao.create(s194);
//			stepDataDao.create(s195);
//			stepDataDao.create(s196);
//			stepDataDao.create(s197);
//			stepDataDao.create(s198);
//			stepDataDao.create(s199);
//			stepDataDao.create(s200);
//			stepDataDao.create(s201);
//			stepDataDao.create(s202);
//			stepDataDao.create(s203);
//			stepDataDao.create(s204);
//			stepDataDao.create(s205);
//			stepDataDao.create(s206);
//			stepDataDao.create(s207);
//			stepDataDao.create(s208);
//			stepDataDao.create(s209);
//			stepDataDao.create(s210);
//			stepDataDao.create(s211);
//			stepDataDao.create(s212);
//			stepDataDao.create(s213);
//			stepDataDao.create(s214);
//			stepDataDao.create(s215);
//			stepDataDao.create(s216);
//			stepDataDao.create(s217);
//			stepDataDao.create(s218);
//			stepDataDao.create(s219);
//			stepDataDao.create(s220);
//			stepDataDao.create(s221);
//			stepDataDao.create(s222);
//			stepDataDao.create(s223);
//			stepDataDao.create(s224);
//			stepDataDao.create(s225);
//			stepDataDao.create(s226);
//			stepDataDao.create(s227);
//			stepDataDao.create(s228);
//			stepDataDao.create(s229);
//			stepDataDao.create(s230);
//			stepDataDao.create(s231);
//			stepDataDao.create(s232);
//			stepDataDao.create(s233);
//			stepDataDao.create(s234);
//			stepDataDao.create(s235);
//			stepDataDao.create(s236);
//			stepDataDao.create(s237);
//			stepDataDao.create(s238);
//			stepDataDao.create(s239);
//			stepDataDao.create(s240);
//			stepDataDao.create(s241);
//			stepDataDao.create(s242);
//			stepDataDao.create(s243);
//			stepDataDao.create(s244);
//			stepDataDao.create(s245);
//			stepDataDao.create(s246);
//			stepDataDao.create(s247);
//			stepDataDao.create(s248);
//			stepDataDao.create(s249);
//			stepDataDao.create(s250);
//			stepDataDao.create(s251);
//			stepDataDao.create(s252);
//			stepDataDao.create(s253);
//			stepDataDao.create(s254);
//			stepDataDao.create(s255);
//			stepDataDao.create(s256);
//			stepDataDao.create(s257);
//			stepDataDao.create(s258);
//			stepDataDao.create(s259);
//			stepDataDao.create(s260);
//			stepDataDao.create(s261);
//			stepDataDao.create(s262);
//			stepDataDao.create(s263);
//			stepDataDao.create(s264);
//			stepDataDao.create(s265);
//			stepDataDao.create(s266);
//			stepDataDao.create(s267);
//			stepDataDao.create(s268);
//			stepDataDao.create(s269);
//			stepDataDao.create(s270);
//			stepDataDao.create(s271);
//			stepDataDao.create(s272);
//			stepDataDao.create(s273);
//			stepDataDao.create(s274);
//			stepDataDao.create(s275);
//			stepDataDao.create(s276);
//			stepDataDao.create(s277);
//			stepDataDao.create(s278);
//			stepDataDao.create(s279);
//			stepDataDao.create(s280);
//			stepDataDao.create(s281);
//			stepDataDao.create(s282);
//			stepDataDao.create(s283);
//			stepDataDao.create(s284);
//			stepDataDao.create(s285);
//			stepDataDao.create(s286);
//			stepDataDao.create(s287);
//			stepDataDao.create(s288);
//			stepDataDao.create(s289);
//			stepDataDao.create(s290);
//			stepDataDao.create(s291);
//			stepDataDao.create(s292);
//			stepDataDao.create(s293);
//			stepDataDao.create(s294);
//			stepDataDao.create(s295);
//			stepDataDao.create(s296);
//			stepDataDao.create(s297);
//			stepDataDao.create(s298);
//			stepDataDao.create(s299);
//			stepDataDao.create(s300);
//			stepDataDao.create(s301);
//			stepDataDao.create(s302);
//			stepDataDao.create(s303);
//			stepDataDao.create(s304);
//			stepDataDao.create(s305);
//			stepDataDao.create(s306);
//			stepDataDao.create(s307);
//			stepDataDao.create(s308);
//			stepDataDao.create(s309);
//			stepDataDao.create(s310);
//			stepDataDao.create(s311);
//			stepDataDao.create(s312);
//			stepDataDao.create(s313);
//			stepDataDao.create(s314);
//			stepDataDao.create(s315);
//			stepDataDao.create(s316);
//			stepDataDao.create(s317);
//			stepDataDao.create(s318);
//			stepDataDao.create(s319);
//			stepDataDao.create(s320);
//			stepDataDao.create(s321);
//			stepDataDao.create(s322);
//			stepDataDao.create(s323);
//			stepDataDao.create(s324);
//			stepDataDao.create(s325);
//			stepDataDao.create(s326);
//			stepDataDao.create(s327);
//			stepDataDao.create(s328);
//			stepDataDao.create(s329);
//			stepDataDao.create(s330);
//			stepDataDao.create(s331);
//			stepDataDao.create(s332);
//			stepDataDao.create(s333);
//			stepDataDao.create(s334);
//			stepDataDao.create(s335);
//			stepDataDao.create(s336);
//			stepDataDao.create(s337);
//			stepDataDao.create(s338);
//			stepDataDao.create(s339);
//			stepDataDao.create(s340);
//			stepDataDao.create(s341);
//			stepDataDao.create(s342);
//			stepDataDao.create(s343);
//			stepDataDao.create(s344);
//			stepDataDao.create(s345);
//			stepDataDao.create(s346);
//			stepDataDao.create(s347);
//			stepDataDao.create(s348);
//			stepDataDao.create(s349);
//			stepDataDao.create(s350);
//			stepDataDao.create(s351);
//			stepDataDao.create(s352);
//			stepDataDao.create(s353);
//			stepDataDao.create(s354);
//			stepDataDao.create(s355);
//			stepDataDao.create(s356);
//			stepDataDao.create(s357);
//			stepDataDao.create(s358);
//			stepDataDao.create(s359);
//			stepDataDao.create(s360);
//			stepDataDao.create(s361);
//			stepDataDao.create(s362);
//			stepDataDao.create(s363);
//			stepDataDao.create(s364);
//			stepDataDao.create(s365);
//			stepDataDao.create(s366);
//			stepDataDao.create(s367);
//			stepDataDao.create(s368);
//			stepDataDao.create(s369);
//			stepDataDao.create(s370);
//			stepDataDao.create(s371);
//			stepDataDao.create(s372);
//			stepDataDao.create(s373);
//			stepDataDao.create(s374);
//			stepDataDao.create(s375);
//			stepDataDao.create(s376);
//			stepDataDao.create(s377);
//			stepDataDao.create(s378);
//			stepDataDao.create(s379);
//			stepDataDao.create(s380);
//			stepDataDao.create(s381);
//			stepDataDao.create(s382);
//			stepDataDao.create(s383);
//			stepDataDao.create(s384);
//			stepDataDao.create(s385);
//			stepDataDao.create(s386);
//			stepDataDao.create(s387);
//			stepDataDao.create(s388);
//			stepDataDao.create(s389);
//			stepDataDao.create(s390);
//			stepDataDao.create(s391);
//			stepDataDao.create(s392);
//			stepDataDao.create(s393);
//			stepDataDao.create(s394);
//			stepDataDao.create(s395);
//			stepDataDao.create(s396);
//			stepDataDao.create(s397);
//			stepDataDao.create(s398);
//			stepDataDao.create(s399);
//			stepDataDao.create(s400);
//			stepDataDao.create(s401);
//			stepDataDao.create(s402);
//			stepDataDao.create(s403);
//			stepDataDao.create(s404);
//			stepDataDao.create(s405);
//			stepDataDao.create(s406);
//			stepDataDao.create(s407);
//			stepDataDao.create(s408);
//			stepDataDao.create(s409);
//			stepDataDao.create(s410);
//			stepDataDao.create(s411);
//			stepDataDao.create(s412);
//			stepDataDao.create(s413);
//			stepDataDao.create(s414);
//			stepDataDao.create(s415);
//			stepDataDao.create(s416);
//			stepDataDao.create(s417);
//			stepDataDao.create(s418);
//			stepDataDao.create(s419);
//			stepDataDao.create(s420);
//			stepDataDao.create(s421);
//			stepDataDao.create(s422);
//			stepDataDao.create(s423);
//			stepDataDao.create(s424);
//			stepDataDao.create(s425);
//			stepDataDao.create(s426);
//			stepDataDao.create(s427);
//			stepDataDao.create(s428);
//			stepDataDao.create(s429);
//			stepDataDao.create(s430);
//			stepDataDao.create(s431);
//			stepDataDao.create(s432);
//			stepDataDao.create(s433);
//			stepDataDao.create(s434);
//			stepDataDao.create(s435);
//			stepDataDao.create(s436);
//			stepDataDao.create(s437);
//			stepDataDao.create(s438);
//			stepDataDao.create(s439);
//			stepDataDao.create(s440);
//			stepDataDao.create(s441);
//			stepDataDao.create(s442);
//			stepDataDao.create(s443);
//			stepDataDao.create(s444);
//			stepDataDao.create(s445);
//			stepDataDao.create(s446);
//			stepDataDao.create(s447);
//			stepDataDao.create(s448);
//			stepDataDao.create(s449);
//			stepDataDao.create(s450);
//			stepDataDao.create(s451);
//			stepDataDao.create(s452);
//			stepDataDao.create(s453);
//			stepDataDao.create(s454);
//			stepDataDao.create(s455);
//			stepDataDao.create(s456);
//			stepDataDao.create(s457);
//			stepDataDao.create(s458);
//			stepDataDao.create(s459);
//			stepDataDao.create(s460);
//			stepDataDao.create(s461);
//			stepDataDao.create(s462);
//			stepDataDao.create(s463);
//			stepDataDao.create(s464);
//			stepDataDao.create(s465);
//			stepDataDao.create(s466);
//			stepDataDao.create(s467);
//			stepDataDao.create(s468);
//			stepDataDao.create(s469);
//			stepDataDao.create(s470);
//			stepDataDao.create(s471);
//			stepDataDao.create(s472);
//			stepDataDao.create(s473);
//			stepDataDao.create(s474);
//			stepDataDao.create(s475);
//			stepDataDao.create(s476);
//			stepDataDao.create(s477);
//			stepDataDao.create(s478);
//			stepDataDao.create(s479);
//			stepDataDao.create(s480);
//			stepDataDao.create(s481);
//			stepDataDao.create(s482);
//			stepDataDao.create(s483);
//			stepDataDao.create(s484);
//			stepDataDao.create(s485);
//			stepDataDao.create(s486);
//			stepDataDao.create(s487);
//			stepDataDao.create(s488);
//			stepDataDao.create(s489);
//			stepDataDao.create(s490);
//			stepDataDao.create(s491);
//			stepDataDao.create(s492);
//			stepDataDao.create(s493);
//
//		} catch (RuntimeException ex) {
//			Log.e("Inserting step", "Insert error " + ex.toString());
//		}
//
//		Route r = new Route();
//		r.setId("ROUTE_GARONAP");
//		r.setName("La Garona");
//		r.setDescription("Aquest itinerari té dues variants: en cotxe seguint la N-230 i la C-28, i a peu o BTT seguint el Camin Reiau i el GR-211, entre el Pont d’Arròs i Casarilh. La distància total és de 11 km, amb un desnivell acumulat de 184 m (3-4 h a peu). Per fer el recorregut a peu, el servei d’autobus intern de la Val d’Aran permet anar o retornar del punt de sortida o arribada. Aixímateix, és possible prendre l’autobús en diversos punts intermitjos del recorregut, cosa que fa possible fer recorreguts parcials de l’itinerari. El camí no presenta cap dificultat, i és adequat per a qualsevol persona amb una forma física mitja. La variant en cotxe fa l’itinerari accesible a tot tipus de visitants. La Garona ofereix la possibilitat de conèixer un ecosistema fluvial, els usos i aprofitaments humans que s’han fet des d’antic, i com aquests han modificat i configurat el riu actual.");
//		r.setUserId("1");
//		// Ph_ch parameters
//		// r.setReference(r6);
//		// Interactive image
//		// r.setInteractiveImage(img);
//		r.setTrack(t);
//		r.setLocalCarto("OSMPublicTransport_HiRes.mbtiles");
//
//		try {
//			routeDataDao.create(r);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting route", "Insert error " + ex.toString());
//		}
//
//	}

	public static void loadEscunhau(DataBaseHelper db, Context context) {
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		RuntimeExceptionDao<InteractiveImage, String> interactiveImageDataDao = db
				.getInteractiveImageDataDao();
		RuntimeExceptionDao<Reference, String> referenceDataDao = db
				.getReferenceDataDao();
		RuntimeExceptionDao<Box, String> boxDataDao = db.getBoxDataDao();

		Track t = new Track("TRACK_ESCUNHAU", "Waypoints recorregut Escunhau");
		try {
			trackDataDao.create(t);
		} catch (RuntimeException ex) {
			Log.e("Inserting track", "Insert error " + ex.toString());
		}
		
		Step s1 = new Step("step_escunhau_1", "step", 42.697801176393,
				0.821759757526527, 0, 10, 1, t);
		Step s2 = new Step("step_escunhau_2", "step", 42.6977078469484,
				0.821824062502701, 0, 10, 2, t, null);
		Step s3 = new Step("step_escunhau_3", "step", 42.6976553613319,
				0.821905251526495, 0, 10, 3, t, null);
		Step s4 = new Step("step_escunhau_4", "step", 42.6976299888476,
				0.821991596541998, 0, 10, 4, t, null);
		Step s5 = new Step("step_escunhau_5", "step", 42.6976415408099,
				0.822125483552144, 0, 10, 5, t, null);
		Step s6 = new Step("step_escunhau_6", "step", 42.6978283925123,
				0.822717157542542, 0, 10, 6, t, null);
		Step s7 = new Step("step_escunhau_7", "step", 42.6978547181972,
				0.822917674453926, 0, 10, 7, t, null);
		Step s8 = new Step("step_escunhau_8", "step", 42.6977931406577,
				0.823231138128576, 0, 10, 8, t, null);
		Step s9 = new Step("step_escunhau_9", "step", 42.6977487486159,
				0.823501271722226, 0, 10, 9, t, null);
		Step s10 = new Step("step_escunhau_10", "step", 42.6976905094852,
				0.823753576281597, 0, 10, 10, t, null);
		Step s11 = new Step("step_escunhau_11", "step", 42.6976033016158,
				0.823903122108358, 0, 10, 11, t, null);
		Step s12 = new Step("step_escunhau_12", "step", 42.6974385580881,
				0.824000438908918, 0, 10, 12, t, null);
		Step s13 = new Step("step_escunhau_13", "step", 42.6971568266475,
				0.82410184158772, 0, 10, 13, t, null);
		Step s14 = new Step("step_escunhau_14", "step", 42.6970448020839,
				0.824130170904539, 0, 10, 14, t, null);
		Step s15 = new Step("step_escunhau_15", "step", 42.6969074032789,
				0.824244843264791, 0, 10, 15, t, null);
		Step s16 = new Step("step_escunhau_16", "step", 42.6968508800105,
				0.824350586428121, 0, 10, 16, t, null);
		Step s17 = new Step("step_escunhau_17", "step", 42.6968488146781,
				0.824478843731745, 0, 10, 17, t, null);
		Step s18 = new Step("step_escunhau_18", "step", 42.6969103221826,
				0.824635401268425, 0, 10, 18, t, null);
		Step s19 = new Step("step_escunhau_19", "step", 42.6969257910816,
				0.824738630152774, 0, 10, 19, t, null);
		Step s20 = new Step("step_escunhau_20", "step", 42.6968743463351,
				0.824874715863375, 0, 10, 20, t, null);
		Step s21 = new Step("step_escunhau_21", "step", 42.6967272523615,
				0.824953100500379, 0, 10, 21, t, null);
		Step s22 = new Step("step_escunhau_22", "step", 42.6965935410452,
				0.825024913473575, 0, 10, 22, t, null);
		Step s23 = new Step("step_escunhau_23", "step", 42.696548893341,
				0.825044784406785, 0, 10, 23, t);
		Step s24 = new Step("step_escunhau_24", "step", 42.6965499362486,
				0.825099684234192, 0, 10, 24, t, null);
		Step s25 = new Step("step_escunhau_25", "step", 42.6966334771012,
				0.825231056306626, 0, 10, 25, t, null);
		Step s26 = new Step("step_escunhau_26", "step", 42.6967953636925,
				0.825457358540868, 0, 10, 26, t, null);
		Step s27 = new Step("step_escunhau_27", "step", 42.6968588392883,
				0.825717617194875, 0, 10, 27, t, null);
		Step s28 = new Step("step_escunhau_28", "step", 42.6968640519814,
				0.825992117848877, 0, 10, 28, t, null);
		Step s29 = new Step("step_escunhau_29", "step", 42.6968025985735,
				0.826549731245128, 0, 10, 29, t, null);
		Step s30 = new Step("step_escunhau_30", "step", 42.6969032258814,
				0.826869735593364, 0, 10, 30, t, null);
		Step s31 = new Step("step_escunhau_31", "step", 42.6969548048764,
				0.826977809447135, 0, 10, 31, t, null);
		Step s32 = new Step("step_escunhau_32", "step", 42.6969777811949,
				0.827239482927907, 0, 10, 32, t, null);
		Step s33 = new Step("step_escunhau_33", "step", 42.6970297071241,
				0.827365857340302, 0, 10, 33, t);
		Step s34 = new Step("step_escunhau_34", "step", 42.6970008726697,
				0.82750725666083, 0, 10, 34, t, null);
		Step s35 = new Step("step_escunhau_35", "step", 42.6970478357911,
				0.827609388108385, 0, 10, 35, t, null);
		Step s36 = new Step("step_escunhau_36", "step", 42.6971918354035,
				0.827842425833535, 0, 10, 36, t, null);
		Step s37 = new Step("step_escunhau_37", "step", 42.6972985652746,
				0.828009618977683, 0, 10, 37, t, null);
		Step s38 = new Step("step_escunhau_38", "step", 42.6973704556994,
				0.828239068444273, 0, 10, 38, t, null);
		Step s39 = new Step("step_escunhau_39", "step", 42.6974708470103,
				0.828546878426241, 0, 10, 39, t, null);
		Step s40 = new Step("step_escunhau_40", "step", 42.697569270917,
				0.828750987798672, 0, 10, 40, t, null);
		Step s41 = new Step("step_escunhau_41", "step", 42.6975873201971,
				0.828848024748562, 0, 10, 41, t, null);
		Step s42 = new Step("step_escunhau_42", "step", 42.6975787838231,
				0.828872738793547, 0, 10, 42, t, null);
		Step s43 = new Step("step_escunhau_43", "step", 42.697529635858,
				0.828892764164054, 0, 10, 43, t, null);
		Step s44 = new Step("step_escunhau_44", "step", 42.6974658324749,
				0.828852259111819, 0, 10, 44, t, null);
		Step s45 = new Step("step_escunhau_45", "step", 42.6974509456645,
				0.828779528583586, 0, 10, 45, t, null);
		Step s46 = new Step("step_escunhau_46", "step", 42.6973294456189,
				0.828545703422797, 0, 10, 46, t, null);
		Step s47 = new Step("step_escunhau_47", "step", 42.6972269846443,
				0.828366152265302, 0, 10, 47, t, null);
		Step s48 = new Step("step_escunhau_48", "step", 42.6971755226364,
				0.828264176799734, 0, 10, 48, t, null);
		Step s49 = new Step("step_escunhau_49", "step", 42.6970615297888,
				0.828188797935342, 0, 10, 49, t, null);
		Step s50 = new Step("step_escunhau_50", "step", 42.6969978418903,
				0.828154393936546, 0, 10, 50, t, null);
		Step s51 = new Step("step_escunhau_51", "step", 42.6969475368956,
				0.828113419340383, 0, 10, 51, t, null);
		Step s52 = new Step("step_escunhau_52", "step", 42.6968186566483,
				0.827965311434745, 0, 10, 52, t, null);
		Step s53 = new Step("step_escunhau_53", "step", 42.6967003955332,
				0.827902290437757, 0, 10, 53, t, null);
		Step s54 = new Step("step_escunhau_54", "step", 42.6966149042139,
				0.827905271382495, 0, 10, 54, t, null);
		Step s55 = new Step("step_escunhau_55", "step", 42.6965621826804,
				0.827974254007595, 0, 10, 55, t, null);
		Step s56 = new Step("step_escunhau_56", "step", 42.6965379627024,
				0.828121595123009, 0, 10, 56, t, null);
		Step s57 = new Step("step_escunhau_57", "step", 42.6965183577845,
				0.82827487925725, 0, 10, 57, t, null);
		Step s58 = new Step("step_escunhau_58", "step", 42.696479250356,
				0.828349490878323, 0, 10, 58, t, null);
		Step s59 = new Step("step_escunhau_59", "step", 42.6964298710688,
				0.828357316303933, 0, 10, 59, t, null);
		Step s60 = new Step("step_escunhau_60", "step", 42.6963303025662,
				0.828330267304323, 0, 10, 60, t);		
		Step s61 = new Step("step_escunhau_61", "step", 42.6957126667467,
				0.827576597377646, 0, 10, 61, t, null);
		Step s62 = new Step("step_escunhau_62", "step", 42.6956252077515,
				0.827475880359284, 0, 10, 62, t, null);
		Step s63 = new Step("step_escunhau_63", "step", 42.6953870644011,
				0.82726444444047, 0, 10, 63, t, null);
		Step s64 = new Step("step_escunhau_64", "step", 42.6951776389002,
				0.826905514995521, 0, 10, 64, t, null);
		Step s65 = new Step("step_escunhau_65", "step", 42.6950985995092,
				0.826773986623963, 0, 10, 65, t, null);
		Step s66 = new Step("step_escunhau_66", "step", 42.6946185053025,
				0.826387881041675, 0, 10, 66, t, null);
		Step s67 = new Step("step_escunhau_67", "step", 42.6945859662696,
				0.82633408182595, 0, 10, 67, t, null);
		Step s68 = new Step("step_escunhau_68", "step", 42.694406430779,
				0.82612671239642, 0, 10, 68, t, null);
		Step s69 = new Step("step_escunhau_69", "step", 42.6942369364557,
				0.825973927934984, 0, 10, 69, t, null);
		Step s70 = new Step("step_escunhau_70", "step", 42.6941263992802,
				0.825843501914912, 0, 10, 70, t, null);
		Step s71 = new Step("step_escunhau_71", "step", 42.6939819325059,
				0.82558608123122, 0, 10, 71, t, null);
		Step s72 = new Step("step_escunhau_72", "step", 42.6937494249889,
				0.82519745235879, 0, 10, 72, t, null);
		Step s73 = new Step("step_escunhau_73", "step", 42.6935867269153,
				0.824928463730742, 0, 10, 73, t, null);
		Step s74 = new Step("step_escunhau_74", "step", 42.6934788339184,
				0.824700288965216, 0, 10, 74, t, null);
		Step s75 = new Step("step_escunhau_75", "step", 42.6933835118485,
				0.824422846288533, 0, 10, 75, t, null);
		Step s76 = new Step("step_escunhau_76", "step", 42.6933180629394,
				0.824058909219104, 0, 10, 76, t, null);
		Step s77 = new Step("step_escunhau_77", "step", 42.6932363538909,
				0.823787096433522, 0, 10, 77, t, null);
		Step s78 = new Step("step_escunhau_78", "step", 42.6931698820869,
				0.823606307472901, 0, 10, 78, t, null);
		Step s79 = new Step("step_escunhau_79", "step", 42.6931082574128,
				0.823443660623567, 0, 10, 79, t, null);
		Step s80 = new Step("step_escunhau_80", "step", 42.6930169680106,
				0.823141665899559, 0, 10, 80, t, null);
		Step s81 = new Step("step_escunhau_81", "step", 42.6929623700103,
				0.822875011916141, 0, 10, 81, t, null);
		Step s82 = new Step("step_escunhau_82", "step", 42.6928911652764,
				0.822682182792421, 0, 10, 82, t, null);
		Step s83 = new Step("step_escunhau_83", "step", 42.6927724126748,
				0.822356735521471, 0, 10, 83, t, null);
		Step s84 = new Step("step_escunhau_84", "step", 42.6927291068565,
				0.82197371916993, 0, 10, 84, t, null);
		Step s85 = new Step("step_escunhau_85", "step", 42.6927274292869,
				0.821412241961612, 0, 10, 85, t, null);
		Step s86 = new Step("step_escunhau_86", "step", 42.6927668530418,
				0.821117887988392, 0, 10, 86, t, null);
		Step s87 = new Step("step_escunhau_87", "step", 42.6927611646842,
				0.820819007896042, 0, 10, 87, t, null);
		Step s88 = new Step("step_escunhau_88", "step", 42.6926749485597,
				0.820547359479081, 0, 10, 88, t, null);
		Step s89 = new Step("step_escunhau_89", "step", 42.6926444679364,
				0.820365316480998, 0, 10, 89, t, null);
		Step s90 = new Step("step_escunhau_90", "step", 42.69264548369,
				0.820182171682795, 0, 10, 90, t, null);
		Step s91 = new Step("step_escunhau_91", "step", 42.6926253951961,
				0.820073009008106, 0, 10, 91, t, null);
		Step s92 = new Step("step_escunhau_92", "step", 42.6926012716428,
				0.819988402145573, 0, 10, 92, t, null);
		Step s93 = new Step("step_escunhau_93", "step", 42.6926423178893,
				0.819779442095769, 0, 10, 93, t, null);
		Step s94 = new Step("step_escunhau_94", "step", 42.6927564015472,
				0.819622858521449, 0, 10, 94, t, null);
		Step s95 = new Step("step_escunhau_95", "step", 42.6928207579073,
				0.819455807466627, 0, 10, 95, t, null);
		Step s96 = new Step("step_escunhau_96", "step", 42.6928247613546,
				0.819193209996198, 0, 10, 96, t, null);
		Step s97 = new Step("step_escunhau_97", "step", 42.6927481214408,
				0.818951746596972, 0, 10, 97, t, null);
		Step s98 = new Step("step_escunhau_98", "step", 42.6927072771759,
				0.81893486550499, 0, 10, 98, t, null);
		Step s99 = new Step("step_escunhau_99", "step", 42.6924551878861,
				0.818937586878173, 0, 10, 99, t, null);
		Step s100 = new Step("step_escunhau_100", "step", 42.6923017394309,
				0.818918544240579, 0, 10, 100, t, null);
		Step s101 = new Step("step_escunhau_101", "step", 42.692147128977,
				0.818838506432469, 0, 10, 101, t, null);
		Step s102 = new Step("step_escunhau_102", "step", 42.6920279335981,
				0.818726711232534, 0, 10, 102, t, null);
		Step s103 = new Step("step_escunhau_103", "step", 42.6920103676088,
				0.818513700946638, 0, 10, 103, t, null);
		Step s104 = new Step("step_escunhau_104", "step", 42.6920362853955,
				0.818219821550296, 0, 10, 104, t, null);
		Step s105 = new Step("step_escunhau_105", "step", 42.6920915244489,
				0.818046986731989, 0, 10, 105, t, null);
		Step s106 = new Step("step_escunhau_106", "step", 42.6920663531891,
				0.817907485813944, 0, 10, 106, t, null);
		Step s107 = new Step("step_escunhau_107", "step", 42.6919790023259,
				0.817812887913594, 0, 10, 107, t, null);
		Step s108 = new Step("step_escunhau_108", "step", 42.6918090324244,
				0.81763573426953, 0, 10, 108, t, null);
		Step s109 = new Step("step_escunhau_109", "step", 42.6917170303242,
				0.817297158052207, 0, 10, 109, t, null);
		Step s110 = new Step("step_escunhau_110", "step", 42.691645700116,
				0.81709824002093, 0, 10, 110, t, null);
		Step s111 = new Step("step_escunhau_111", "step", 42.6916093551191,
				0.817081202779328, 0, 10, 111, t, null);
		Step s112 = new Step("step_escunhau_112", "step", 42.6915734752943,
				0.817088563402404, 0, 10, 112, t);
		Step s113 = new Step("step_escunhau_113", "step", 42.691538409508,
				0.81713862022293, 0, 10, 113, t, null);
		Step s114 = new Step("step_escunhau_114", "step", 42.6915354217379,
				0.817218070818133, 0, 10, 114, t, null);
		Step s115 = new Step("step_escunhau_115", "step", 42.6915827425475,
				0.817338483726203, 0, 10, 115, t, null);
		Step s116 = new Step("step_escunhau_116", "step", 42.691620482832,
				0.817428714719622, 0, 10, 116, t, null);
		Step s117 = new Step("step_escunhau_117", "step", 42.6916179599428,
				0.817532563284265, 0, 10, 117, t, null);
		Step s118 = new Step("step_escunhau_118", "step", 42.6915931719041,
				0.817649398801721, 0, 10, 118, t, null);
		Step s119 = new Step("step_escunhau_119", "step", 42.6915772665205,
				0.817759819574692, 0, 10, 119, t, null);
		Step s120 = new Step("step_escunhau_120", "step", 42.6915825798299,
				0.817802358234394, 0, 10, 120, t, null);
		Step s121 = new Step("step_escunhau_121", "step", 42.6916295512891,
				0.817904473290443, 0, 10, 121, t, null);
		Step s122 = new Step("step_escunhau_122", "step", 42.6917712450146,
				0.818015478306577, 0, 10, 122, t, null);
		Step s123 = new Step("step_escunhau_123", "step", 42.6918136005363,
				0.818111651917166, 0, 10, 123, t, null);
		Step s124 = new Step("step_escunhau_124", "step", 42.6917890443751,
				0.818240686573139, 0, 10, 124, t, null);
		Step s125 = new Step("step_escunhau_125", "step", 42.6917151095874,
				0.818377553696456, 0, 10, 125, t, null);
		Step s126 = new Step("step_escunhau_126", "step", 42.6916363264561,
				0.818496279619857, 0, 10, 126, t, null);
		Step s127 = new Step("step_escunhau_127", "step", 42.691571506596,
				0.818638930510673, 0, 10, 127, t, null);
		Step s128 = new Step("step_escunhau_128", "step", 42.6915546706051,
				0.818700555301119, 0, 10, 128, t, null);
		Step s129 = new Step("step_escunhau_129", "step", 42.6915922095774,
				0.819016625053135, 0, 10, 129, t, null);
		Step s130 = new Step("step_escunhau_130", "step", 42.6916264946982,
				0.819161909904845, 0, 10, 130, t, null);
		Step s131 = new Step("step_escunhau_131", "step", 42.6916145064816,
				0.819241675552347, 0, 10, 131, t, null);
		Step s132 = new Step("step_escunhau_132", "step", 42.6915437920813,
				0.81931128951988, 0, 10, 132, t, null);
		Step s133 = new Step("step_escunhau_133", "step", 42.6913327792602,
				0.819343089016582, 0, 10, 133, t, null);
		Step s134 = new Step("step_escunhau_134", "step", 42.6911539600883,
				0.819410382506593, 0, 10, 134, t, null);
		Step s135 = new Step("step_escunhau_135", "step", 42.6908467456089,
				0.81959203137642, 0, 10, 135, t, null);
		Step s136 = new Step("step_escunhau_136", "step", 42.6908038411692,
				0.819703395109934, 0, 10, 136, t, null);
		Step s137 = new Step("step_escunhau_137", "step", 42.6907960336565,
				0.820002737885712, 0, 10, 137, t, null);
		Step s138 = new Step("step_escunhau_138", "step", 42.6907997498447,
				0.820197918615659, 0, 10, 138, t, null);
		Step s139 = new Step("step_escunhau_139", "step", 42.6908242215595,
				0.820300821350813, 0, 10, 139, t, null);
		Step s140 = new Step("step_escunhau_140", "step", 42.6909257656843,
				0.820431545400246, 0, 10, 140, t, null);
		Step s141 = new Step("step_escunhau_141", "step", 42.6910093115652,
				0.820562899438434, 0, 10, 141, t, null);
		Step s142 = new Step("step_escunhau_142", "step", 42.6910377019773,
				0.820635148172183, 0, 10, 142, t, null);
		Step s143 = new Step("step_escunhau_143", "step", 42.6910389790898,
				0.82070224183228, 0, 10, 143, t, null);
		Step s144 = new Step("step_escunhau_144", "step", 42.6909401336879,
				0.820949838160066, 0, 10, 144, t, null);
		Step s145 = new Step("step_escunhau_145", "step", 42.6908585894002,
				0.821160207892362, 0, 10, 145, t, null);
		Step s146 = new Step("step_escunhau_146", "step", 42.6908369042759,
				0.821203690467688, 0, 10, 146, t, null);
		Step s147 = new Step("step_escunhau_147", "step", 42.6908657854,
				0.821538371260987, 0, 10, 147, t, null);
		Step s148 = new Step("step_escunhau_148", "step", 42.6909328405168,
				0.821749648381779, 0, 10, 148, t, null);
		Step s149 = new Step("step_escunhau_149", "step", 42.6910478816318,
				0.821879903194499, 0, 10, 149, t, null);
		Step s150 = new Step("step_escunhau_150", "step", 42.6911477995669,
				0.821925238190976, 0, 10, 150, t, null);
		Step s151 = new Step("step_escunhau_151", "step", 42.6911577268917,
				0.821973719128422, 0, 10, 151, t, null);
		Step s152 = new Step("step_escunhau_152", "step", 42.6911430935594,
				0.822151232029202, 0, 10, 152, t, null);
		Step s153 = new Step("step_escunhau_153", "step", 42.6911361828493,
				0.822261336491172, 0, 10, 153, t, null);
		Step s154 = new Step("step_escunhau_154", "step", 42.6911834984248,
				0.822381752689849, 0, 10, 154, t, null);
		Step s155 = new Step("step_escunhau_155", "step", 42.6911983891178,
				0.822454474264511, 0, 10, 155, t, null);
		Step s156 = new Step("step_escunhau_156", "step", 42.691114778335,
				0.822793088493195, 0, 10, 156, t, null);
		Step s157 = new Step("step_escunhau_157", "step", 42.6910893125494,
				0.82311135986002, 0, 10, 157, t, null);
		Step s158 = new Step("step_escunhau_158", "step", 42.6911511695152,
				0.823286200061812, 0, 10, 158, t, null);
		Step s159 = new Step("step_escunhau_159", "step", 42.691308675953,
				0.823518734390421, 0, 10, 159, t, null);
		Step s160 = new Step("step_escunhau_160", "step", 42.6913641776886,
				0.823596141312828, 0, 10, 160, t, null);
		Step s161 = new Step("step_escunhau_161", "step", 42.6914871723931,
				0.823671190842495, 0, 10, 161, t, null);
		Step s162 = new Step("step_escunhau_162", "step", 42.6916577129719,
				0.82387885794752, 0, 10, 162, t, null);
		Step s163 = new Step("step_escunhau_163", "step", 42.6917092945759,
				0.823986920284788, 0, 10, 163, t, null);
		Step s164 = new Step("step_escunhau_164", "step", 42.6917410444816,
				0.824236057151079, 0, 10, 164, t, null);
		Step s165 = new Step("step_escunhau_165", "step", 42.6917540047819,
				0.824681164151323, 0, 10, 165, t, null);
		Step s166 = new Step("step_escunhau_166", "step", 42.691760494059,
				0.825022736764739, 0, 10, 166, t, null);
		Step s167 = new Step("step_escunhau_167", "step", 42.6918424317973,
				0.825306743973024, 0, 10, 167, t, null);
		Step s168 = new Step("step_escunhau_168", "step", 42.6919641696533,
				0.825552741451239, 0, 10, 168, t, null);
		Step s169 = new Step("step_escunhau_169", "step", 42.6920427469577,
				0.825659863590625, 0, 10, 169, t, null);
		Step s170 = new Step("step_escunhau_170", "step", 42.6922137294772,
				0.825653896481875, 0, 10, 170, t, null);
		Step s171 = new Step("step_escunhau_171", "step", 42.6923526360144,
				0.825618530676778, 0, 10, 171, t, null);
		Step s172 = new Step("step_escunhau_172", "step", 42.6924890122716,
				0.82568701469393, 0, 10, 172, t, null);
		Step s173 = new Step("step_escunhau_173", "step", 42.6925817830715,
				0.825830264391077, 0, 10, 173, t, null);
		Step s174 = new Step("step_escunhau_174", "step", 42.6928403760193,
				0.826407191948311, 0, 10, 174, t, null);
		Step s175 = new Step("step_escunhau_175", "step", 42.6928650738609,
				0.826522299943081, 0, 10, 175, t, null);
		Step s176 = new Step("step_escunhau_176", "step", 42.6928680843798,
				0.826680890234468, 0, 10, 176, t, null);
		Step s177 = new Step("step_escunhau_177", "step", 42.6928620955934,
				0.826839794445911, 0, 10, 177, t, null);
		Step s178 = new Step("step_escunhau_178", "step", 42.6928476863347,
				0.827029510640309, 0, 10, 178, t, null);
		Step s179 = new Step("step_escunhau_179", "step", 42.6928594637219,
				0.827175587839032, 0, 10, 179, t, null);
		Step s180 = new Step("step_escunhau_180", "step", 42.6929455485911,
				0.827441147233797, 0, 10, 180, t, null);
		Step s181 = new Step("step_escunhau_181", "step", 42.6929812120054,
				0.827659635990828, 0, 10, 181, t, null);
		Step s182 = new Step("step_escunhau_182", "step", 42.693063721973,
				0.827974150693846, 0, 10, 182, t, null);
		Step s183 = new Step("step_escunhau_183", "step", 42.6931127673204,
				0.828186069997959, 0, 10, 183, t, null);
		Step s184 = new Step("step_escunhau_184", "step", 42.6931865081516,
				0.82851309910555, 0, 10, 184, t, null);
		Step s185 = new Step("step_escunhau_185", "step", 42.6932728328032,
				0.829028905354917, 0, 10, 185, t, null);
		Step s186 = new Step("step_escunhau_186", "step", 42.6934007971527,
				0.829366254543255, 0, 10, 186, t, null);
		Step s187 = new Step("step_escunhau_187", "step", 42.6935010694727,
				0.829667947408443, 0, 10, 187, t, null);
		Step s188 = new Step("step_escunhau_188", "step", 42.6935869174609,
				0.829921313763451, 0, 10, 188, t, null);
		Step s189 = new Step("step_escunhau_189", "step", 42.6936817640072,
				0.830174367411956, 0, 10, 189, t, null);
		Step s190 = new Step("step_escunhau_190", "step", 42.6937154572299,
				0.830289165190389, 0, 10, 190, t, null);
		Step s191 = new Step("step_escunhau_191", "step", 42.6937462769736,
				0.830727562093559, 0, 10, 191, t, null);
		Step s192 = new Step("step_escunhau_192", "step", 42.6937710933399,
				0.831086819604102, 0, 10, 192, t, null);
		Step s193 = new Step("step_escunhau_193", "step", 42.6938283297329,
				0.831493779025723, 0, 10, 193, t, null);
		Step s194 = new Step("step_escunhau_194", "step", 42.6938328351857,
				0.831731668932176, 0, 10, 194, t, null);
		Step s195 = new Step("step_escunhau_195", "step", 42.6938289185528,
				0.832000370771451, 0, 10, 195, t, null);
		Step s196 = new Step("step_escunhau_196", "step", 42.6937888893319,
				0.832264225351173, 0, 10, 196, t, null);
		Step s197 = new Step("step_escunhau_197", "step", 42.6937044412752,
				0.832560143697217, 0, 10, 197, t, null);
		Step s198 = new Step("step_escunhau_198", "step", 42.6936859848562,
				0.832774416982821, 0, 10, 198, t, null);
		Step s199 = new Step("step_escunhau_199", "step", 42.6936932554173,
				0.832920654041329, 0, 10, 199, t, null);
		Step s200 = new Step("step_escunhau_200", "step", 42.6937675600887,
				0.833278191036785, 0, 10, 200, t, null);
		Step s201 = new Step("step_escunhau_201", "step", 42.6937725235197,
				0.833540479965135, 0, 10, 201, t, null);
		Step s202 = new Step("step_escunhau_202", "step", 42.693776909308,
				0.83377227023615, 0, 10, 202, t, null);
		Step s203 = new Step("step_escunhau_203", "step", 42.6938310206204,
				0.834014539266169, 0, 10, 203, t, null);
		Step s204 = new Step("step_escunhau_204", "step", 42.6939607009378,
				0.834205351508764, 0, 10, 204, t, null);
		Step s205 = new Step("step_escunhau_205", "step", 42.6940631528108,
				0.834384903451313, 0, 10, 205, t, null);
		Step s206 = new Step("step_escunhau_206", "step", 42.6941713730069,
				0.834631397243679, 0, 10, 206, t, null);
		Step s207 = new Step("step_escunhau_207", "step", 42.694332895362,
				0.834839416579379, 0, 10, 207, t, null);
		Step s208 = new Step("step_escunhau_208", "step", 42.6945397590477,
				0.83506417282144, 0, 10, 208, t, null);
		Step s209 = new Step("step_escunhau_209", "step", 42.6946489004646,
				0.835359468396412, 0, 10, 209, t, null);
		Step s210 = new Step("step_escunhau_210", "step", 42.6948870270295,
				0.835808984390924, 0, 10, 210, t, null);
		Step s211 = new Step("step_escunhau_211", "step", 42.695024435378,
				0.83593239240873, 0, 10, 211, t, null);
		Step s212 = new Step("step_escunhau_212", "step", 42.6952268006508,
				0.835919258830192, 0, 10, 212, t, null);
		Step s213 = new Step("step_escunhau_213", "step", 42.6953398663416,
				0.835945850717434, 0, 10, 213, t, null);
		Step s214 = new Step("step_escunhau_214", "step", 42.6953410192948,
				0.836006849783049, 0, 10, 214, t, null);
		Step s215 = new Step("step_escunhau_215", "step", 42.6952837931151,
				0.836075980588532, 0, 10, 215, t, null);
		Step s216 = new Step("step_escunhau_216", "step", 42.6949950136904,
				0.836043284239348, 0, 10, 216, t, null);
		Step s217 = new Step("step_escunhau_217", "step", 42.694790919001,
				0.835964919540738, 0, 10, 217, t, null);
		Step s218 = new Step("step_escunhau_218", "step", 42.6945841723118,
				0.835746259193842, 0, 10, 218, t, null);
		Step s219 = new Step("step_escunhau_219", "step", 42.6944123839026,
				0.835471451330175, 0, 10, 219, t, null);
		Step s220 = new Step("step_escunhau_220", "step", 42.6942649380075,
				0.835293460001423, 0, 10, 220, t, null);
		Step s221 = new Step("step_escunhau_221", "step", 42.694200560096,
				0.835222451325113, 0, 10, 221, t, null);
		Step s222 = new Step("step_escunhau_222", "step", 42.6940243849855,
				0.835191950300166, 0, 10, 222, t, null);
		Step s223 = new Step("step_escunhau_223", "step", 42.6936470003279,
				0.834759490164181, 0, 10, 223, t, null);
		Step s224 = new Step("step_escunhau_224", "step", 42.6934292910486,
				0.834437455432124, 0, 10, 224, t, null);
		Step s225 = new Step("step_escunhau_225", "step", 42.6932709975578,
				0.833924142424682, 0, 10, 225, t, null);
		Step s226 = new Step("step_escunhau_226", "step", 42.6932829945632,
				0.833606332447321, 0, 10, 226, t, null);
		Step s227 = new Step("step_escunhau_227", "step", 42.6932718024551,
				0.833490751176381, 0, 10, 227, t, null);
		Step s228 = new Step("step_escunhau_228", "step", 42.6931726944698,
				0.833250049671795, 0, 10, 228, t, null);
		Step s229 = new Step("step_escunhau_229", "step", 42.6931306977225,
				0.833172162221375, 0, 10, 229, t, null);
		Step s230 = new Step("step_escunhau_230", "step", 42.6931085434328,
				0.832953199832503, 0, 10, 230, t, null);
		Step s231 = new Step("step_escunhau_231", "step", 42.693038163636,
				0.832803055723824, 0, 10, 231, t, null);
		Step s232 = new Step("step_escunhau_232", "step", 42.6929474671906,
				0.832055458501564, 0, 10, 232, t, null);
		Step s233 = new Step("step_escunhau_233", "step", 42.6929400800578,
				0.831903123681609, 0, 10, 233, t, null);
		Step s234 = new Step("step_escunhau_234", "step", 42.6927452012959,
				0.831360576341877, 0, 10, 234, t, null);
		Step s235 = new Step("step_escunhau_235", "step", 42.6927136978294,
				0.831123630472102, 0, 10, 235, t, null);
		Step s236 = new Step("step_escunhau_236", "step", 42.6925169740834,
				0.830721535340053, 0, 10, 236, t, null);
		Step s237 = new Step("step_escunhau_237", "step", 42.6924037838513,
				0.830450813411191, 0, 10, 237, t, null);
		Step s238 = new Step("step_escunhau_238", "step", 42.6923020195796,
				0.830307870382078, 0, 10, 238, t, null);
		Step s239 = new Step("step_escunhau_239", "step", 42.6921356429903,
				0.830081727703753, 0, 10, 239, t, null);
		Step s240 = new Step("step_escunhau_240", "step", 42.6919378847217,
				0.829862782858245, 0, 10, 240, t, null);
		Step s241 = new Step("step_escunhau_241", "step", 42.691735048379,
				0.829613498492999, 0, 10, 241, t, null);
		Step s242 = new Step("step_escunhau_242", "step", 42.6915354390236,
				0.829296964406399, 0, 10, 242, t);
		Step s243 = new Step("step_escunhau_243", "step", 42.6911829484009,
				0.828753826334206, 0, 10, 243, t, null);
		Step s244 = new Step("step_escunhau_244", "step", 42.6909232229831,
				0.828353944305907, 0, 10, 244, t, null);
		Step s245 = new Step("step_escunhau_245", "step", 42.6907581024515,
				0.827956871081335, 0, 10, 245, t, null);
		Step s246 = new Step("step_escunhau_246", "step", 42.6906099371597,
				0.827504277780364, 0, 10, 246, t, null);
		Step s247 = new Step("step_escunhau_247", "step", 42.6905531501322,
				0.827121741122666, 0, 10, 247, t, null);
		Step s248 = new Step("step_escunhau_248", "step", 42.6904947409353,
				0.826653813683295, 0, 10, 248, t, null);
		Step s249 = new Step("step_escunhau_249", "step", 42.6904003501411,
				0.826425176001993, 0, 10, 249, t, null);
		Step s250 = new Step("step_escunhau_250", "step", 42.6902565797355,
				0.826204365176348, 0, 10, 250, t, null);
		Step s251 = new Step("step_escunhau_251", "step", 42.6901870020568,
				0.826096931334906, 0, 10, 251, t, null);
		Step s252 = new Step("step_escunhau_252", "step", 42.6898550252458,
				0.825449349750989, 0, 10, 252, t, null);
		Step s253 = new Step("step_escunhau_253", "step", 42.6897403356296,
				0.825337388529275, 0, 10, 253, t, null);
		Step s254 = new Step("step_escunhau_254", "step", 42.6896201036584,
				0.825170691032564, 0, 10, 254, t, null);
		Step s255 = new Step("step_escunhau_255", "step", 42.689480251128,
				0.824919232385421, 0, 10, 255, t, null);
		Step s256 = new Step("step_escunhau_256", "step", 42.6893175527162,
				0.824650262488643, 0, 10, 256, t, null);
		Step s257 = new Step("step_escunhau_257", "step", 42.6891779308854,
				0.824411004763881, 0, 10, 257, t, null);
		Step s258 = new Step("step_escunhau_258", "step", 42.6890468440073,
				0.824147036908603, 0, 10, 258, t, null);
		Step s259 = new Step("step_escunhau_259", "step", 42.6889199083022,
				0.823864615350247, 0, 10, 259, t, null);
		Step s260 = new Step("step_escunhau_260", "step", 42.6887597581219,
				0.823729833349182, 0, 10, 260, t, null);
		Step s261 = new Step("step_escunhau_261", "step", 42.6886103459316,
				0.82368622568427, 0, 10, 261, t, null);
		Step s262 = new Step("step_escunhau_262", "step", 42.6883474018594,
				0.823591654512428, 0, 10, 262, t, null);
		Step s263 = new Step("step_escunhau_263", "step", 42.688021580145,
				0.823505383513009, 0, 10, 263, t, null);
		Step s264 = new Step("step_escunhau_264", "step", 42.6877461808435,
				0.823466177418518, 0, 10, 264, t, null);
		Step s265 = new Step("step_escunhau_265", "step", 42.6874525514826,
				0.823415402111058, 0, 10, 265, t, null);
		Step s266 = new Step("step_escunhau_266", "step", 42.6872265311003,
				0.823368368773792, 0, 10, 266, t, null);
		Step s267 = new Step("step_escunhau_267", "step", 42.6871395325352,
				0.823292067258649, 0, 10, 267, t, null);
		Step s268 = new Step("step_escunhau_268", "step", 42.6868757764518,
				0.82315480655511, 0, 10, 268, t, null);
		Step s269 = new Step("step_escunhau_269", "step", 42.6867350152436,
				0.823092590007172, 0, 10, 269, t, null);
		Step s270 = new Step("step_escunhau_270", "step", 42.6867025907765,
				0.823044898224885, 0, 10, 270, t, null);
		Step s271 = new Step("step_escunhau_271", "step", 42.6866507765107,
				0.822924647211745, 0, 10, 271, t, null);
		Step s272 = new Step("step_escunhau_272", "step", 42.6866835254672,
				0.822752617456962, 0, 10, 272, t, null);
		Step s273 = new Step("step_escunhau_273", "step", 42.6867547061613,
				0.82270740901945, 0, 10, 273, t, null);
		Step s274 = new Step("step_escunhau_274", "step", 42.686865222775,
				0.822599795295003, 0, 10, 274, t, null);
		Step s275 = new Step("step_escunhau_275", "step", 42.686932459491,
				0.822347219546666, 0, 10, 275, t, null);
		Step s276 = new Step("step_escunhau_276", "step", 42.6870152573523,
				0.821965934491029, 0, 10, 276, t, null);
		Step s277 = new Step("step_escunhau_277", "step", 42.6869682895822,
				0.821863823643951, 0, 10, 277, t, null);
		Step s278 = new Step("step_escunhau_278", "step", 42.6868515337861,
				0.821880110809224, 0, 10, 278, t, null);
		Step s279 = new Step("step_escunhau_279", "step", 42.6866857472232,
				0.821922523894265, 0, 10, 279, t, null);
		Step s280 = new Step("step_escunhau_280", "step", 42.6864930794567,
				0.821971979308661, 0, 10, 280, t, null);
		Step s281 = new Step("step_escunhau_281", "step", 42.6864209708999,
				0.821968396553794, 0, 10, 281, t, null);
		Step s282 = new Step("step_escunhau_282", "step", 42.6863523176653,
				0.821909765806409, 0, 10, 282, t, null);
		Step s283 = new Step("step_escunhau_283", "step", 42.6863411140685,
				0.821794199931151, 0, 10, 283, t, null);
		Step s284 = new Step("step_escunhau_284", "step", 42.6864190357123,
				0.821156760941089, 0, 10, 284, t, null);
		Step s285 = new Step("step_escunhau_285", "step", 42.6865445309885,
				0.820889942186682, 0, 10, 285, t, null);
		Step s286 = new Step("step_escunhau_286", "step", 42.6866899972864,
				0.820726175890648, 0, 10, 286, t, null);
		Step s287 = new Step("step_escunhau_287", "step", 42.6867819333691,
				0.820588693263691, 0, 10, 287, t, null);
		Step s288 = new Step("step_escunhau_288", "step", 42.6867844592166,
				0.820484852778723, 0, 10, 288, t, null);
		Step s289 = new Step("step_escunhau_289", "step", 42.6867885817164,
				0.820228379745392, 0, 10, 289, t, null);
		Step s290 = new Step("step_escunhau_290", "step", 42.68683079013,
				0.820080429586226, 0, 10, 290, t, null);
		Step s291 = new Step("step_escunhau_291", "step", 42.6869990713862,
				0.819696151270253, 0, 10, 291, t, null);
		Step s292 = new Step("step_escunhau_292", "step", 42.6871762341859,
				0.819305456952515, 0, 10, 292, t, null);
		Step s293 = new Step("step_escunhau_293", "step", 42.6872574268064,
				0.819076800425066, 0, 10, 293, t, null);
		Step s294 = new Step("step_escunhau_294", "step", 42.6873051788671,
				0.818983582394727, 0, 10, 294, t, null);
		Step s295 = new Step("step_escunhau_295", "step", 42.6872653484743,
				0.8187835737025, 0, 10, 295, t, null);
		Step s296 = new Step("step_escunhau_296", "step", 42.6871665591252,
				0.818561216796812, 0, 10, 296, t, null);
		Step s297 = new Step("step_escunhau_297", "step", 42.6870275060916,
				0.818352476415802, 0, 10, 297, t, null);
		Step s298 = new Step("step_escunhau_298", "step", 42.6869632343913,
				0.81828759263887, 0, 10, 298, t, null);
		Step s299 = new Step("step_escunhau_299", "step", 42.6868270531545,
				0.817993309990593, 0, 10, 299, t, null);
		Step s300 = new Step("step_escunhau_300", "step", 42.686736912628,
				0.817752343826732, 0, 10, 300, t, null);
		Step s301 = new Step("step_escunhau_301", "step", 42.6867223333561,
				0.817459907484375, 0, 10, 301, t, null);
		Step s302 = new Step("step_escunhau_302", "step", 42.6867708628819,
				0.817171363728422, 0, 10, 302, t, null);
		Step s303 = new Step("step_escunhau_303", "step", 42.686791034848,
				0.817048595791908, 0, 10, 303, t, null);
		Step s304 = new Step("step_escunhau_304", "step", 42.686797940708,
				0.816938498658422, 0, 10, 304, t, null);
		Step s305 = new Step("step_escunhau_305", "step", 42.6867520151939,
				0.816891283035241, 0, 10, 305, t, null);
		Step s306 = new Step("step_escunhau_306", "step", 42.6867026367884,
				0.816899115972319, 0, 10, 306, t, null);
		Step s307 = new Step("step_escunhau_307", "step", 42.686656281861,
				0.817065522370545, 0, 10, 307, t, null);
		Step s308 = new Step("step_escunhau_308", "step", 42.6865453406525,
				0.817386766886517, 0, 10, 308, t, null);
		Step s309 = new Step("step_escunhau_309", "step", 42.6864548035863,
				0.817597441093828, 0, 10, 309, t, null);
		Step s310 = new Step("step_escunhau_310", "step", 42.6863815667354,
				0.817770890704826, 0, 10, 310, t, null);
		Step s311 = new Step("step_escunhau_311", "step", 42.6863654285336,
				0.817869104248526, 0, 10, 311, t, null);
		Step s312 = new Step("step_escunhau_312", "step", 42.6863655783532,
				0.818113220023351, 0, 10, 312, t, null);
		Step s313 = new Step("step_escunhau_313", "step", 42.6863787613612,
				0.818332467414172, 0, 10, 313, t, null);
		Step s314 = new Step("step_escunhau_314", "step", 42.6863677032437,
				0.818461018096931, 0, 10, 314, t, null);
		Step s315 = new Step("step_escunhau_315", "step", 42.6862956588979,
				0.818933472637762, 0, 10, 315, t, null);
		Step s316 = new Step("step_escunhau_316", "step", 42.6862265164223,
				0.819558399860801, 0, 10, 316, t, null);
		Step s317 = new Step("step_escunhau_317", "step", 42.6861486905761,
				0.819963921275531, 0, 10, 317, t, null);
		Step s318 = new Step("step_escunhau_318", "step", 42.6860641576634,
				0.820253719332304, 0, 10, 318, t, null);
		Step s319 = new Step("step_escunhau_319", "step", 42.6859707410358,
				0.820549930284311, 0, 10, 319, t, null);
		Step s320 = new Step("step_escunhau_320", "step", 42.6858354348584,
				0.820774369467548, 0, 10, 320, t, null);
		Step s321 = new Step("step_escunhau_321", "step", 42.685746372911,
				0.820826307651531, 0, 10, 321, t, null);
		Step s322 = new Step("step_escunhau_322", "step", 42.6856302249548,
				0.821111105086517, 0, 10, 322, t, null);
		Step s323 = new Step("step_escunhau_323", "step", 42.6854912304493,
				0.82137839132961, 0, 10, 323, t, null);
		Step s324 = new Step("step_escunhau_324", "step", 42.6852081341848,
				0.821644610523965, 0, 10, 324, t, null);
		Step s325 = new Step("step_escunhau_325", "step", 42.6850125913874,
				0.821779606445269, 0, 10, 325, t, null);
		Step s326 = new Step("step_escunhau_326", "step", 42.684830314966,
				0.821901932080256, 0, 10, 326, t, null);
		Step s327 = new Step("step_escunhau_327", "step", 42.6846658045015,
				0.822011430370134, 0, 10, 327, t, null);
		Step s328 = new Step("step_escunhau_328", "step", 42.684614130328,
				0.822135293397946, 0, 10, 328, t, null);
		Step s329 = new Step("step_escunhau_329", "step", 42.6846607497793,
				0.822219104256028, 0, 10, 329, t, null);
		Step s330 = new Step("step_escunhau_330", "step", 42.6847982878823,
				0.822348561182472, 0, 10, 330, t, null);
		Step s331 = new Step("step_escunhau_331", "step", 42.6849229071935,
				0.822508984494856, 0, 10, 331, t, null);
		Step s332 = new Step("step_escunhau_332", "step", 42.6849649108623,
				0.822586854423426, 0, 10, 332, t, null);
		Step s333 = new Step("step_escunhau_333", "step", 42.6849753017418,
				0.822659726060688, 0, 10, 333, t, null);
		Step s334 = new Step("step_escunhau_334", "step", 42.6849782011888,
				0.82281219703602, 0, 10, 334, t, null);
		Step s335 = new Step("step_escunhau_335", "step", 42.6849554947713,
				0.823038797193987, 0, 10, 335, t, null);
		Step s336 = new Step("step_escunhau_336", "step", 42.6849570022461,
				0.823118082093138, 0, 10, 336, t, null);
		Step s337 = new Step("step_escunhau_337", "step", 42.6850016723536,
				0.823336225890896, 0, 10, 337, t, null);
		Step s338 = new Step("step_escunhau_338", "step", 42.6850270685971,
				0.823487911266111, 0, 10, 338, t, null);
		Step s339 = new Step("step_escunhau_339", "step", 42.6850277859365,
				0.823762516543814, 0, 10, 339, t, null);
		Step s340 = new Step("step_escunhau_340", "step", 42.6850188080779,
				0.824000842978889, 0, 10, 340, t, null);
		Step s341 = new Step("step_escunhau_341", "step", 42.6850191764965,
				0.824257151728562, 0, 10, 341, t, null);
		Step s342 = new Step("step_escunhau_342", "step", 42.6850030326398,
				0.824355361658695, 0, 10, 342, t, null);
		Step s343 = new Step("step_escunhau_343", "step", 42.6850411149743,
				0.824463884321404, 0, 10, 343, t, null);
		Step s344 = new Step("step_escunhau_344", "step", 42.6852041414509,
				0.824513119363456, 0, 10, 344, t, null);
		Step s345 = new Step("step_escunhau_345", "step", 42.6854255460719,
				0.824554213829477, 0, 10, 345, t, null);
		Step s346 = new Step("step_escunhau_346", "step", 42.6856604493025,
				0.82459483734504, 0, 10, 346, t, null);
		Step s347 = new Step("step_escunhau_347", "step", 42.6859645837927,
				0.824724589248389, 0, 10, 347, t, null);
		Step s348 = new Step("step_escunhau_348", "step", 42.6861195380443,
				0.824822931400497, 0, 10, 348, t, null);
		Step s349 = new Step("step_escunhau_349", "step", 42.686343607427,
				0.825004304016657, 0, 10, 349, t, null);
		Step s350 = new Step("step_escunhau_350", "step", 42.6864891175599,
				0.825316583435956, 0, 10, 350, t, null);
		Step s351 = new Step("step_escunhau_351", "step", 42.6865958505672,
				0.825483744127189, 0, 10, 351, t, null);
		Step s352 = new Step("step_escunhau_352", "step", 42.6867418051273,
				0.82558240313638, 0, 10, 352, t, null);
		Step s353 = new Step("step_escunhau_353", "step", 42.6870143259694,
				0.82570716187199, 0, 10, 353, t, null);
		Step s354 = new Step("step_escunhau_354", "step", 42.6872562764107,
				0.825881813181463, 0, 10, 354, t, null);
		Step s355 = new Step("step_escunhau_355", "step", 42.6874788555385,
				0.826221926324303, 0, 10, 355, t, null);
		Step s356 = new Step("step_escunhau_356", "step", 42.6875416339293,
				0.826445552662351, 0, 10, 356, t, null);
		Step s357 = new Step("step_escunhau_357", "step", 42.6875985385829,
				0.826834169342234, 0, 10, 357, t, null);
		Step s358 = new Step("step_escunhau_358", "step", 42.6876287918758,
				0.827242025483086, 0, 10, 358, t, null);
		Step s359 = new Step("step_escunhau_359", "step", 42.687640929804,
				0.827644410402548, 0, 10, 359, t, null);
		Step s360 = new Step("step_escunhau_360", "step", 42.6876407117577,
				0.827870234694045, 0, 10, 360, t, null);
		Step s361 = new Step("step_escunhau_361", "step", 42.6876284986627,
				0.828175818050366, 0, 10, 361, t, null);
		Step s362 = new Step("step_escunhau_362", "step", 42.6875579062505,
				0.828489538839207, 0, 10, 362, t, null);
		Step s363 = new Step("step_escunhau_363", "step", 42.6875039232261,
				0.828729442513703, 0, 10, 363, t, null);
		Step s364 = new Step("step_escunhau_364", "step", 42.6874500553404,
				0.828975444897335, 0, 10, 364, t, null);
		Step s365 = new Step("step_escunhau_365", "step", 42.6873818787806,
				0.829179223409423, 0, 10, 365, t, null);
		Step s366 = new Step("step_escunhau_366", "step", 42.6873190107977,
				0.829425538419149, 0, 10, 366, t, null);
		Step s367 = new Step("step_escunhau_367", "step", 42.6872657194483,
				0.829702034980665, 0, 10, 367, t, null);
		Step s368 = new Step("step_escunhau_368", "step", 42.6872095373908,
				0.82982605393007, 0, 10, 368, t, null);
		Step s369 = new Step("step_escunhau_369", "step", 42.6871527857974,
				0.830157598115768, 0, 10, 369, t, null);
		Step s370 = new Step("step_escunhau_370", "step", 42.6871181843108,
				0.830470061230662, 0, 10, 370, t, null);
		Step s371 = new Step("step_escunhau_371", "step", 42.6871345792922,
				0.830860089159033, 0, 10, 371, t, null);
		Step s372 = new Step("step_escunhau_372", "step", 42.6871613583987,
				0.831322993197616, 0, 10, 372, t, null);
		Step s373 = new Step("step_escunhau_373", "step", 42.687261168133,
				0.831838284642799, 0, 10, 373, t, null);
		Step s374 = new Step("step_escunhau_374", "step", 42.6873967411492,
				0.832340127439747, 0, 10, 374, t, null);
		Step s375 = new Step("step_escunhau_375", "step", 42.6875398103236,
				0.832762370831243, 0, 10, 375, t, null);
		Step s376 = new Step("step_escunhau_376", "step", 42.6876421486615,
				0.832935803091335, 0, 10, 376, t, null);
		Step s377 = new Step("step_escunhau_377", "step", 42.6877306417545,
				0.833091407875649, 0, 10, 377, t, null);
		Step s378 = new Step("step_escunhau_378", "step", 42.6877092982341,
				0.833153181700324, 0, 10, 378, t, null);
		Step s379 = new Step("step_escunhau_379", "step", 42.6876332673273,
				0.833180238145405, 0, 10, 379, t, null);
		Step s380 = new Step("step_escunhau_380", "step", 42.6875736186801,
				0.833121280789669, 0, 10, 380, t, null);
		Step s381 = new Step("step_escunhau_381", "step", 42.6875445434843,
				0.833012435203893, 0, 10, 381, t, null);
		Step s382 = new Step("step_escunhau_382", "step", 42.6874878935821,
				0.832874032978955, 0, 10, 382, t, null);
		Step s383 = new Step("step_escunhau_383", "step", 42.6874094383918,
				0.832773008177433, 0, 10, 383, t, null);
		Step s384 = new Step("step_escunhau_384", "step", 42.6873202521424,
				0.832580809997573, 0, 10, 384, t, null);
		Step s385 = new Step("step_escunhau_385", "step", 42.6872626779974,
				0.832393615861696, 0, 10, 385, t, null);
		Step s386 = new Step("step_escunhau_386", "step", 42.6871054202832,
				0.832173271208299, 0, 10, 386, t, null);
		Step s387 = new Step("step_escunhau_387", "step", 42.6868902405229,
				0.831747438070818, 0, 10, 387, t, null);
		Step s388 = new Step("step_escunhau_388", "step", 42.6867081734768,
				0.831405898380832, 0, 10, 388, t, null);
		Step s389 = new Step("step_escunhau_389", "step", 42.6866238311632,
				0.831231844880144, 0, 10, 389, t, null);
		Step s390 = new Step("step_escunhau_390", "step", 42.6865636039009,
				0.831142395205067, 0, 10, 390, t, null);
		Step s391 = new Step("step_escunhau_391", "step", 42.6864995702356,
				0.831089696442335, 0, 10, 391, t, null);
		Step s392 = new Step("step_escunhau_392", "step", 42.6864409604108,
				0.831085633303708, 0, 10, 392, t, null);
		Step s393 = new Step("step_escunhau_393", "step", 42.6864235398461,
				0.831116754799904, 0, 10, 393, t, null);
		Step s394 = new Step("step_escunhau_394", "step", 42.6864296568625,
				0.831201984408142, 0, 10, 394, t, null);
		Step s395 = new Step("step_escunhau_395", "step", 42.6864810004795,
				0.831297846159592, 0, 10, 395, t, null);
		Step s396 = new Step("step_escunhau_396", "step", 42.68667714404,
				0.831669409806974, 0, 10, 396, t, null);
		Step s397 = new Step("step_escunhau_397", "step", 42.6868187142463,
				0.832012359006493, 0, 10, 397, t, null);
		Step s398 = new Step("step_escunhau_398", "step", 42.6869132093062,
				0.832247091755097, 0, 10, 398, t, null);
		Step s399 = new Step("step_escunhau_399", "step", 42.6869587860025,
				0.832514042182832, 0, 10, 399, t, null);
		Step s400 = new Step("step_escunhau_400", "step", 42.6869750585682,
				0.832897970862619, 0, 10, 400, t, null);
		Step s401 = new Step("step_escunhau_401", "step", 42.6869777157292,
				0.833276270069984, 0, 10, 401, t, null);
		Step s402 = new Step("step_escunhau_402", "step", 42.6870026386775,
				0.833641589079146, 0, 10, 402, t, null);
		Step s403 = new Step("step_escunhau_403", "step", 42.6870844394754,
				0.833919487974448, 0, 10, 403, t, null);
		Step s404 = new Step("step_escunhau_404", "step", 42.6872144657585,
				0.834128577233525, 0, 10, 404, t, null);
		Step s405 = new Step("step_escunhau_405", "step", 42.6872481544503,
				0.834481387132852, 0, 10, 405, t, null);
		Step s406 = new Step("step_escunhau_406", "step", 42.6872431926203,
				0.834695168659741, 0, 10, 406, t, null);
		Step s407 = new Step("step_escunhau_407", "step", 42.6871784672632,
				0.834843892376476, 0, 10, 407, t, null);
		Step s408 = new Step("step_escunhau_408", "step", 42.6871442007239,
				0.834936629588546, 0, 10, 408, t, null);
		Step s409 = new Step("step_escunhau_409", "step", 42.6871200865146,
				0.835090045034968, 0, 10, 409, t, null);
		Step s410 = new Step("step_escunhau_410", "step", 42.687085358489,
				0.835158385649249, 0, 10, 410, t, null);
		Step s411 = new Step("step_escunhau_411", "step", 42.6868143440244,
				0.835350892920359, 0, 10, 411, t, null);
		Step s412 = new Step("step_escunhau_412", "step", 42.6866913539192,
				0.835513844823078, 0, 10, 412, t, null);
		Step s413 = new Step("step_escunhau_413", "step", 42.6866160141599,
				0.835577492223991, 0, 10, 413, t, null);
		Step s414 = new Step("step_escunhau_414", "step", 42.6862273182292,
				0.835737465027271, 0, 10, 414, t, null);
		Step s415 = new Step("step_escunhau_415", "step", 42.6861115974332,
				0.835808617045571, 0, 10, 415, t, null);
		Step s416 = new Step("step_escunhau_416", "step", 42.6860495256451,
				0.835859596705978, 0, 10, 416, t, null);
		Step s417 = new Step("step_escunhau_417", "step", 42.6859242292183,
				0.83590056586629, 0, 10, 417, t, null);
		Step s418 = new Step("step_escunhau_418", "step", 42.685811163435,
				0.835873977381947, 0, 10, 418, t, null);
		Step s419 = new Step("step_escunhau_419", "step", 42.6857200191115,
				0.835816112869865, 0, 10, 419, t, null);
		Step s420 = new Step("step_escunhau_420", "step", 42.6857057133965,
				0.835773888910089, 0, 10, 420, t, null);
		Step s421 = new Step("step_escunhau_421", "step", 42.6855394596799,
				0.835791868539386, 0, 10, 421, t, null);
		Step s422 = new Step("step_escunhau_422", "step", 42.6852300265,
				0.835857540753394, 0, 10, 422, t, null);
		Step s423 = new Step("step_escunhau_423", "step", 42.6849480518103,
				0.835946670318868, 0, 10, 423, t, null);
		Step s424 = new Step("step_escunhau_424", "step", 42.6847746432059,
				0.836062543641954, 0, 10, 424, t, null);
		Step s425 = new Step("step_escunhau_425", "step", 42.6846620385172,
				0.836060350715447, 0, 10, 425, t, null);
		Step s426 = new Step("step_escunhau_426", "step", 42.6845448189818,
				0.836052215174874, 0, 10, 426, t, null);
		Step s427 = new Step("step_escunhau_427", "step", 42.6844290979028,
				0.836123364567168, 0, 10, 427, t, null);
		Step s428 = new Step("step_escunhau_428", "step", 42.6843763708566,
				0.836192326523053, 0, 10, 428, t, null);
		Step s429 = new Step("step_escunhau_429", "step", 42.6841892327068,
				0.836296469061471, 0, 10, 429, t, null);
		Step s430 = new Step("step_escunhau_430", "step", 42.6840203232522,
				0.836412183719248, 0, 10, 430, t, null);
		Step s431 = new Step("step_escunhau_431", "step", 42.6838654886005,
				0.836557923068219, 0, 10, 431, t, null);
		Step s432 = new Step("step_escunhau_432", "step", 42.6837230001504,
				0.836642205399481, 0, 10, 432, t, null);
		Step s433 = new Step("step_escunhau_433", "step", 42.6835794745434,
				0.83667159850501, 0, 10, 433, t, null);
		Step s434 = new Step("step_escunhau_434", "step", 42.6834086057499,
				0.836683632342023, 0, 10, 434, t, null);
		Step s435 = new Step("step_escunhau_435", "step", 42.6832551591095,
				0.836664547723448, 0, 10, 435, t, null);
		Step s436 = new Step("step_escunhau_436", "step", 42.683160898397,
				0.836680025059722, 0, 10, 436, t, null);
		Step s437 = new Step("step_escunhau_437", "step", 42.6830316780997,
				0.836751640152422, 0, 10, 437, t, null);
		Step s438 = new Step("step_escunhau_438", "step", 42.6827604299096,
				0.836931929996725, 0, 10, 438, t, null);
		Step s439 = new Step("step_escunhau_439", "step", 42.6825244867931,
				0.837074377093035, 0, 10, 439, t, null);
		Step s440 = new Step("step_escunhau_440", "step", 42.6823502703821,
				0.837147552057632, 0, 10, 440, t, null);
		Step s441 = new Step("step_escunhau_441", "step", 42.6819870714509,
				0.837227284447762, 0, 10, 441, t, null);
		Step s442 = new Step("step_escunhau_442", "step", 42.6818403131804,
				0.837323915865846, 0, 10, 442, t, null);
		Step s443 = new Step("step_escunhau_443", "step", 42.6817295514805,
				0.837419297832375, 0, 10, 443, t, null);
		Step s444 = new Step("step_escunhau_444", "step", 42.6815411449191,
				0.837456347792828, 0, 10, 444, t, null);
		Step s445 = new Step("step_escunhau_445", "step", 42.6814037311273,
				0.837570961369291, 0, 10, 445, t, null);
		Step s446 = new Step("step_escunhau_446", "step", 42.6808413903819,
				0.837834570815905, 0, 10, 446, t, null);
		Step s447 = new Step("step_escunhau_447", "step", 42.6807162081398,
				0.83788163066418, 0, 10, 447, t, null);
		Step s448 = new Step("step_escunhau_448", "step", 42.6805769513134,
				0.837898666204623, 0, 10, 448, t, null);
		Step s449 = new Step("step_escunhau_449", "step", 42.6803190845469,
				0.838072376940451, 0, 10, 449, t, null);
		Step s450 = new Step("step_escunhau_450", "step", 42.680123060258,
				0.838182917169287, 0, 10, 450, t, null);
		Step s451 = new Step("step_escunhau_451", "step", 42.6799539182252,
				0.838286421968002, 0, 10, 451, t, null);
		Step s452 = new Step("step_escunhau_452", "step", 42.6798473097718,
				0.838363347986584, 0, 10, 452, t, null);
		Step s453 = new Step("step_escunhau_453", "step", 42.6797537394978,
				0.838415412053614, 0, 10, 453, t, null);
		Step s454 = new Step("step_escunhau_454", "step", 42.6793762346319,
				0.838452912763093, 0, 10, 454, t, null);
		Step s455 = new Step("step_escunhau_455", "step", 42.679224054523,
				0.838500906343089, 0, 10, 455, t, null);
		Step s456 = new Step("step_escunhau_456", "step", 42.678924424047,
				0.838608933356019, 0, 10, 456, t, null);
		Step s457 = new Step("step_escunhau_457", "step", 42.6785575298135,
				0.838731496014362, 0, 10, 457, t, null);
		Step s458 = new Step("step_escunhau_458", "step", 42.6781942141338,
				0.83880511522018, 0, 10, 458, t, null);
		Step s459 = new Step("step_escunhau_459", "step", 42.6777763277605,
				0.838850114441506, 0, 10, 459, t, null);
		Step s460 = new Step("step_escunhau_460", "step", 42.67757869108,
				0.838875272883988, 0, 10, 460, t, null);
		Step s461 = new Step("step_escunhau_461", "step", 42.6775251563968,
				0.83890153753097, 0, 10, 461, t, null);
		Step s462 = new Step("step_escunhau_462", "step", 42.6774225865992,
				0.838953910695564, 0, 10, 462, t, null);
		Step s463 = new Step("step_escunhau_463", "step", 42.6772556413306,
				0.838935289670495, 0, 10, 463, t, null);
		Step s464 = new Step("step_escunhau_464", "step", 42.6770298560702,
				0.838900402212179, 0, 10, 464, t, null);
		Step s465 = new Step("step_escunhau_465", "step", 42.6768582960957,
				0.838875839286142, 0, 10, 465, t, null);
		Step s466 = new Step("step_escunhau_466", "step", 42.6765882055049,
				0.838879100637751, 0, 10, 466, t, null);
		Step s467 = new Step("step_escunhau_467", "step", 42.6764801002051,
				0.838876746313115, 0, 10, 467, t, null);
		Step s468 = new Step("step_escunhau_468", "step", 42.6763806487852,
				0.838855785820125, 0, 10, 468, t, null);
		Step s469 = new Step("step_escunhau_469", "step", 42.6762543149127,
				0.838841859386957, 0, 10, 469, t, null);
		Step s470 = new Step("step_escunhau_470", "step", 42.6761017891914,
				0.838871555199, 0, 10, 470, t, null);
		Step s471 = new Step("step_escunhau_471", "step", 42.6759588381245,
				0.83893142899311, 0, 10, 471, t, null);
		Step s472 = new Step("step_escunhau_472", "step", 42.6757710061833,
				0.83899896027955, 0, 10, 472, t, null);
		Step s473 = new Step("step_escunhau_473", "step", 42.6755917130182,
				0.83904178724684, 0, 10, 473, t, null);
		Step s474 = new Step("step_escunhau_474", "step", 42.6754659545932,
				0.839058350420421, 0, 10, 474, t, null);
		Step s475 = new Step("step_escunhau_475", "step", 42.6753443504919,
				0.839056463658833, 0, 10, 475, t, null);
		Step s476 = new Step("step_escunhau_476", "step", 42.6752346339037,
				0.838968737655508, 0, 10, 476, t, null);
		Step s477 = new Step("step_escunhau_477", "step", 42.67517914256,
				0.838891336063467, 0, 10, 477, t, null);
		Step s478 = new Step("step_escunhau_478", "step", 42.6750243148268,
				0.838799072515439, 0, 10, 478, t, null);
		Step s479 = new Step("step_escunhau_479", "step", 42.6748019931384,
				0.838709149304307, 0, 10, 479, t, null);
		Step s480 = new Step("step_escunhau_480", "step", 42.674584055868,
				0.838612972820577, 0, 10, 480, t, null);
		Step s481 = new Step("step_escunhau_481", "step", 42.6744433022768,
				0.838550732145163, 0, 10, 481, t, null);
		Step s482 = new Step("step_escunhau_482", "step", 42.6742480930813,
				0.83846597239086, 0, 10, 482, t, null);
		Step s483 = new Step("step_escunhau_483", "step", 42.6741225748056,
				0.83825676013493, 0, 10, 483, t, null);
		Step s484 = new Step("step_escunhau_484", "step", 42.6740028222638,
				0.838114468797004, 0, 10, 484, t, null);
		Step s485 = new Step("step_escunhau_485", "step", 42.6739197571345,
				0.838007516593589, 0, 10, 485, t, null);
		Step s486 = new Step("step_escunhau_486", "step", 42.6738055402118,
				0.837919950416371, 0, 10, 486, t, null);
		Step s487 = new Step("step_escunhau_487", "step", 42.6736088336225,
				0.837755922194914, 0, 10, 487, t, null);
		Step s488 = new Step("step_escunhau_488", "step", 42.6735214989058,
				0.837661322757822, 0, 10, 488, t, null);
		Step s489 = new Step("step_escunhau_489", "step", 42.6732892559148,
				0.837522935675366, 0, 10, 489, t, null);
		Step s490 = new Step("step_escunhau_490", "step", 42.6731255431587,
				0.837437088068124, 0, 10, 490, t, null);
		Step s491 = new Step("step_escunhau_491", "step", 42.672965984411,
				0.837332791645855, 0, 10, 491, t, null);
		Step s492 = new Step("step_escunhau_492", "step", 42.672787966495,
				0.837204729275498, 0, 10, 492, t, null);
		Step s493 = new Step("step_escunhau_493", "step", 42.6726188324379,
				0.837070257736578, 0, 10, 493, t, null);
		Step s494 = new Step("step_escunhau_494", "step", 42.672450850166,
				0.83699676363118, 0, 10, 494, t, null);
		Step s495 = new Step("step_escunhau_495", "step", 42.6720318192665,
				0.836742829226121, 0, 10, 495, t, null);
		Step s496 = new Step("step_escunhau_496", "step", 42.6719246420362,
				0.836551296146504, 0, 10, 496, t, null);
		Step s497 = new Step("step_escunhau_497", "step", 42.6718647647971,
				0.836480154513686, 0, 10, 497, t, null);
		Step s498 = new Step("step_escunhau_498", "step", 42.6717092439398,
				0.836351316023451, 0, 10, 498, t, null);
		Step s499 = new Step("step_escunhau_499", "step", 42.6715553361474,
				0.836307844303245, 0, 10, 499, t, null);
		Step s500 = new Step("step_escunhau_500", "step", 42.6714187353154,
				0.836227162774051, 0, 10, 500, t, null);
		Step s501 = new Step("step_escunhau_501", "step", 42.6712905574312,
				0.836115681497727, 0, 10, 501, t, null);
		Step s502 = new Step("step_escunhau_502", "step", 42.671207721043,
				0.836020931633197, 0, 10, 502, t, null);
		Step s503 = new Step("step_escunhau_503", "step", 42.6710881964046,
				0.835890846197976, 0, 10, 503, t, null);
		Step s504 = new Step("step_escunhau_504", "step", 42.6709430571312,
				0.835834868597098, 0, 10, 504, t, null);
		Step s505 = new Step("step_escunhau_505", "step", 42.6707705745516,
				0.835761535827731, 0, 10, 505, t, null);
		Step s506 = new Step("step_escunhau_506", "step", 42.6705302523538,
				0.835672253818069, 0, 10, 506, t, null);
		Step s507 = new Step("step_escunhau_507", "step", 42.6703177344406,
				0.835624717504287, 0, 10, 507, t, null);
		Step s508 = new Step("step_escunhau_508", "step", 42.6700244544652,
				0.835592187807647, 0, 10, 508, t, null);
		Step s509 = new Step("step_escunhau_509", "step", 42.670024339204,
				0.83558609037872, 0, 10, 509, t, null);
		Step s510 = new Step("step_escunhau_510", "step", 42.6700245697261,
				0.835598285236611, 0, 10, 510, t, null);
		Step s511 = new Step("step_escunhau_511", "step", 42.6699999999834,
				0.835619999551635, 0, 10, 511, t, null);
		Step s512 = new Step("step_escunhau_512", "step", 42.6699399999834,
				0.835599999551608, 0, 10, 512, t, null);
		Step s513 = new Step("step_escunhau_513", "step", 42.6696199999834,
				0.83554999955154, 0, 10, 513, t, null);
		Step s514 = new Step("step_escunhau_514", "step", 42.6694999999834,
				0.835369999551279, 0, 10, 514, t, null);
		Step s515 = new Step("step_escunhau_515", "step", 42.6694399999834,
				0.835349999551252, 0, 10, 515, t, null);
		Step s516 = new Step("step_escunhau_516", "step", 42.6693799999834,
				0.835339999551238, 0, 10, 516, t, null);
		Step s517 = new Step("step_escunhau_517", "step", 42.6693199999834,
				0.835289999551166, 0, 10, 517, t, null);
		Step s518 = new Step("step_escunhau_518", "step", 42.6692999999834,
				0.835209999551051, 0, 10, 518, t, null);
		Step s519 = new Step("step_escunhau_519", "step", 42.6692399999834,
				0.835179999551007, 0, 10, 519, t, null);
		Step s520 = new Step("step_escunhau_520", "step", 42.6691099999834,
				0.835009999550764, 0, 10, 520, t, null);
		Step s521 = new Step("step_escunhau_521", "step", 42.6690599999834,
				0.834949999550677, 0, 10, 521, t, null);
		Step s522 = new Step("step_escunhau_522", "step", 42.6689999999834,
				0.834929999550649, 0, 10, 522, t, null);
		Step s523 = new Step("step_escunhau_523", "step", 42.6685499999834,
				0.83480999955048, 0, 10, 523, t, null);
		Step s524 = new Step("step_escunhau_524", "step", 42.6685399999834,
				0.834799999550466, 0, 10, 524, t, null);
		Step s525 = new Step("step_escunhau_525", "step", 42.6683199999834,
				0.83475999955041, 0, 10, 525, t, null);
		Step s526 = new Step("step_escunhau_526", "step", 42.6682199999834,
				0.834749999550397, 0, 10, 526, t, null);
		Step s527 = new Step("step_escunhau_527", "step", 42.6679599999834,
				0.834649999550255, 0, 10, 527, t, null);
		Step s528 = new Step("step_escunhau_528", "step", 42.6678499999834,
				0.834629999550228, 0, 10, 528, t, null);
		Step s529 = new Step("step_escunhau_529", "step", 42.6677799999834,
				0.834559999550126, 0, 10, 529, t, null);
		Step s530 = new Step("step_escunhau_530", "step", 42.6674499999834,
				0.834339999549811, 0, 10, 530, t, null);
		Step s531 = new Step("step_escunhau_531", "step", 42.6673899999834,
				0.834239999549667, 0, 10, 531, t);
		Step s532 = new Step("step_escunhau_532", "step", 42.6671999999834,
				0.834169999549567, 0, 10, 532, t, null);
		Step s533 = new Step("step_escunhau_533", "step", 42.6670299999834,
				0.834279999549729, 0, 10, 533, t, null);
		Step s534 = new Step("step_escunhau_534", "step", 42.6669299999834,
				0.834319999549787, 0, 10, 534, t, null);
		Step s535 = new Step("step_escunhau_535", "step", 42.6667099999834,
				0.834279999549734, 0, 10, 535, t, null);
		Step s536 = new Step("step_escunhau_536", "step", 42.6665699999834,
				0.834299999549763, 0, 10, 536, t, null);
		Step s537 = new Step("step_escunhau_537", "step", 42.6664499999834,
				0.834299999549765, 0, 10, 537, t, null);
		Step s538 = new Step("step_escunhau_538", "step", 42.6662899999834,
				0.834139999549535, 0, 10, 538, t, null);
		Step s539 = new Step("step_escunhau_539", "step", 42.6662499999834,
				0.834069999549434, 0, 10, 539, t, null);
		Step s540 = new Step("step_escunhau_540", "step", 42.6661999999834,
				0.833949999549259, 0, 10, 540, t, null);
		Step s541 = new Step("step_escunhau_541", "step", 42.6661799999833,
				0.83385999954913, 0, 10, 541, t, null);
		Step s542 = new Step("step_escunhau_542", "step", 42.6661099999833,
				0.833719999548927, 0, 10, 542, t, null);
		Step s543 = new Step("step_escunhau_543", "step", 42.6659599999833,
				0.833479999548579, 0, 10, 543, t, null);
		Step s544 = new Step("step_escunhau_544", "step", 42.6657899999833,
				0.833319999548349, 0, 10, 544, t, null);
		Step s545 = new Step("step_escunhau_545", "step", 42.6655899999833,
				0.833229999548219, 0, 10, 545, t, null);
		Step s546 = new Step("step_escunhau_546", "step", 42.6655199999833,
				0.833209999548192, 0, 10, 546, t, null);
		Step s547 = new Step("step_escunhau_547", "step", 42.6654699999833,
				0.83315999954812, 0, 10, 547, t, null);
		Step s548 = new Step("step_escunhau_548", "step", 42.6654199999833,
				0.833139999548091, 0, 10, 548, t, null);
		Step s549 = new Step("step_escunhau_549", "step", 42.6649699999833,
				0.832999999547893, 0, 10, 549, t, null);
		Step s550 = new Step("step_escunhau_550", "step", 42.6649699999833,
				0.832989999547878, 0, 10, 550, t, null);
		Step s551 = new Step("step_escunhau_551", "step", 42.6649399999833,
				0.83296999954785, 0, 10, 551, t, null);
		Step s552 = new Step("step_escunhau_552", "step", 42.6645099999833,
				0.832769999547562, 0, 10, 552, t, null);
		Step s553 = new Step("step_escunhau_553", "step", 42.6644899999833,
				0.832769999547563, 0, 10, 553, t, null);
		Step s554 = new Step("step_escunhau_554", "step", 42.6642799999833,
				0.832679999547434, 0, 10, 554, t, null);
		Step s555 = new Step("step_escunhau_555", "step", 42.6640199999833,
				0.832549999547248, 0, 10, 555, t, null);
		Step s556 = new Step("step_escunhau_556", "step", 42.6639399999833,
				0.832479999547146, 0, 10, 556, t, null);
		Step s557 = new Step("step_escunhau_557", "step", 42.6638899999833,
				0.832429999547075, 0, 10, 557, t, null);
		Step s558 = new Step("step_escunhau_558", "step", 42.6636399999833,
				0.832299999546887, 0, 10, 558, t, null);
		Step s559 = new Step("step_escunhau_559", "step", 42.6634999999833,
				0.832179999546714, 0, 10, 559, t, null);
		Step s560 = new Step("step_escunhau_560", "step", 42.6633899999833,
				0.832109999546612, 0, 10, 560, t, null);
		Step s561 = new Step("step_escunhau_561", "step", 42.6633299999833,
				0.832019999546482, 0, 10, 561, t, null);
		Step s562 = new Step("step_escunhau_562", "step", 42.6630599999833,
				0.831909999546324, 0, 10, 562, t, null);
		Step s563 = new Step("step_escunhau_563", "step", 42.6628999999833,
				0.831799999546167, 0, 10, 563, t, null);
		Step s564 = new Step("step_escunhau_564", "step", 42.6627799999833,
				0.831769999546122, 0, 10, 564, t, null);
		Step s565 = new Step("step_escunhau_565", "step", 42.6626399999833,
				0.831729999546066, 0, 10, 565, t, null);
		Step s566 = new Step("step_escunhau_566", "step", 42.6625899999833,
				0.831679999545994, 0, 10, 566, t, null);
		Step s567 = new Step("step_escunhau_567", "step", 42.6624199999833,
				0.831559999545821, 0, 10, 567, t, null);
		Step s568 = new Step("step_escunhau_568", "step", 42.6623299999833,
				0.83148999954572, 0, 10, 568, t, null);
		Step s569 = new Step("step_escunhau_569", "step", 42.6621299999833,
				0.831389999545576, 0, 10, 569, t, null);
		Step s570 = new Step("step_escunhau_570", "step", 42.6620499999833,
				0.831329999545488, 0, 10, 570, t, null);
		Step s571 = new Step("step_escunhau_571", "step", 42.6619999999832,
				0.831289999545431, 0, 10, 571, t);
		Step s572 = new Step("step_escunhau_572", "step", 42.6619299999832,
				0.831269999545402, 0, 10, 572, t, null);
		Step s573 = new Step("step_escunhau_573", "step", 42.6618299999832,
				0.831259999545389, 0, 10, 573, t, null);
		Step s574 = new Step("step_escunhau_574", "step", 42.6617399999832,
				0.831249999545374, 0, 10, 574, t, null);
		Step s575 = new Step("step_escunhau_575", "step", 42.6616199999833,
				0.831239999545362, 0, 10, 575, t, null);
		Step s576 = new Step("step_escunhau_576", "step", 42.6615799999832,
				0.831209999545318, 0, 10, 576, t, null);
		Step s577 = new Step("step_escunhau_577", "step", 42.6615299999833,
				0.831179999545275, 0, 10, 577, t, null);
		Step s578 = new Step("step_escunhau_578", "step", 42.6614399999832,
				0.831159999545247, 0, 10, 578, t, null);
		Step s579 = new Step("step_escunhau_579", "step", 42.6613799999832,
				0.831199999545307, 0, 10, 579, t, null);
		Step s580 = new Step("step_escunhau_580", "step", 42.6613299999832,
				0.831199999545308, 0, 10, 580, t, null);
		Step s581 = new Step("step_escunhau_581", "step", 42.6612399999833,
				0.831159999545249, 0, 10, 581, t, null);
		Step s582 = new Step("step_escunhau_582", "step", 42.6611499999832,
				0.831149999545237, 0, 10, 582, t, null);
		Step s583 = new Step("step_escunhau_583", "step", 42.6610999999832,
				0.831129999545207, 0, 10, 583, t, null);
		Step s584 = new Step("step_escunhau_584", "step", 42.6610099999832,
				0.83108999954515, 0, 10, 584, t, null);
		Step s585 = new Step("step_escunhau_585", "step", 42.6609499999833,
				0.831079999545135, 0, 10, 585, t, null);
		Step s586 = new Step("step_escunhau_586", "step", 42.6608499999833,
				0.83110999954518, 0, 10, 586, t, null);
		Step s587 = new Step("step_escunhau_587", "step", 42.6607999999832,
				0.831069999545122, 0, 10, 587, t, null);
		Step s588 = new Step("step_escunhau_588", "step", 42.6607499999833,
				0.831069999545122, 0, 10, 588, t, null);
		Step s589 = new Step("step_escunhau_589", "step", 42.6606099999832,
				0.831059999545111, 0, 10, 589, t, null);
		Step s590 = new Step("step_escunhau_590", "step", 42.6605199999832,
				0.831019999545054, 0, 10, 590, t, null);
		Step s591 = new Step("step_escunhau_591", "step", 42.6604599999832,
				0.831029999545069, 0, 10, 591, t, null);
		Step s592 = new Step("step_escunhau_592", "step", 42.6603399999832,
				0.830969999544982, 0, 10, 592, t, null);
		Step s593 = new Step("step_escunhau_593", "step", 42.6602899999832,
				0.830929999544924, 0, 10, 593, t, null);
		Step s594 = new Step("step_escunhau_594", "step", 42.6602399999832,
				0.830889999544866, 0, 10, 594, t, null);
		Step s595 = new Step("step_escunhau_595", "step", 42.6601699999832,
				0.830829999544778, 0, 10, 595, t, null);
		Step s596 = new Step("step_escunhau_596", "step", 42.6601299999832,
				0.830779999544706, 0, 10, 596, t, null);
		Step s597 = new Step("step_escunhau_597", "step", 42.6600599999832,
				0.830739999544647, 0, 10, 597, t, null);
		Step s598 = new Step("step_escunhau_598", "step", 42.6599799999832,
				0.830689999544575, 0, 10, 598, t, null);
		Step s599 = new Step("step_escunhau_599", "step", 42.6599299999832,
				0.830649999544517, 0, 10, 599, t, null);
		Step s600 = new Step("step_escunhau_600", "step", 42.6598099999832,
				0.83060999954446, 0, 10, 600, t, null);
		Step s601 = new Step("step_escunhau_601", "step", 42.6597199999832,
				0.830609999544461, 0, 10, 601, t, null);
		Step s602 = new Step("step_escunhau_602", "step", 42.6595999999832,
				0.830649999544522, 0, 10, 602, t, null);
		Step s603 = new Step("step_escunhau_603", "step", 42.6594999999832,
				0.830659999544538, 0, 10, 603, t, null);
		Step s604 = new Step("step_escunhau_604", "step", 42.6593699999832,
				0.830729999544641, 0, 10, 604, t, null);
		Step s605 = new Step("step_escunhau_605", "step", 42.6592799999832,
				0.830729999544642, 0, 10, 605, t, null);
		Step s606 = new Step("step_escunhau_606", "step", 42.6592599999832,
				0.830649999544526, 0, 10, 606, t, null);
		Step s607 = new Step("step_escunhau_607", "step", 42.6592199999832,
				0.830589999544438, 0, 10, 607, t, null);
		Step s608 = new Step("step_escunhau_608", "step", 42.6590499999832,
				0.830359999544103, 0, 10, 608, t, null);
		Step s609 = new Step("step_escunhau_609", "step", 42.6589699999832,
				0.830279999543986, 0, 10, 609, t, null);
		Step s610 = new Step("step_escunhau_610", "step", 42.6589199999832,
				0.8302199995439, 0, 10, 610, t, null);
		Step s611 = new Step("step_escunhau_611", "step", 42.6587599999832,
				0.830119999543755, 0, 10, 611, t, null);
		Step s612 = new Step("step_escunhau_612", "step", 42.6587099999832,
				0.830069999543682, 0, 10, 612, t, null);
		Step s613 = new Step("step_escunhau_613", "step", 42.6586499999832,
				0.830029999543623, 0, 10, 613, t, null);
		Step s614 = new Step("step_escunhau_614", "step", 42.6585899999832,
				0.829989999543565, 0, 10, 614, t, null);
		Step s615 = new Step("step_escunhau_615", "step", 42.6584799999832,
				0.829929999543479, 0, 10, 615, t, null);
		Step s616 = new Step("step_escunhau_616", "step", 42.6584099999832,
				0.829919999543465, 0, 10, 616, t, null);
		Step s617 = new Step("step_escunhau_617", "step", 42.6583499999832,
				0.829869999543392, 0, 10, 617, t, null);
		Step s618 = new Step("step_escunhau_618", "step", 42.6581299999832,
				0.829579999542967, 0, 10, 618, t, null);
		Step s619 = new Step("step_escunhau_619", "step", 42.6580999999832,
				0.829509999542866, 0, 10, 619, t, null);
		Step s620 = new Step("step_escunhau_620", "step", 42.6580599999832,
				0.829419999542734, 0, 10, 620, t, null);
		Step s621 = new Step("step_escunhau_621", "step", 42.6579699999832,
				0.829289999542543, 0, 10, 621, t, null);
		Step s622 = new Step("step_escunhau_622", "step", 42.6579099999832,
				0.829239999542472, 0, 10, 622, t, null);
		Step s623 = new Step("step_escunhau_623", "step", 42.6578399999832,
				0.829219999542443, 0, 10, 623, t, null);
		Step s624 = new Step("step_escunhau_624", "step", 42.6574499999832,
				0.829129999542314, 0, 10, 624, t, null);
		Step s625 = new Step("step_escunhau_625", "step", 42.6574399999831,
				0.829019999542152, 0, 10, 625, t, null);
		Step s626 = new Step("step_escunhau_626", "step", 42.6573799999831,
				0.828789999541815, 0, 10, 626, t, null);
		Step s627 = new Step("step_escunhau_627", "step", 42.6572999999831,
				0.828559999541476, 0, 10, 627, t, null);
		Step s628 = new Step("step_escunhau_628", "step", 42.6572799999831,
				0.828279999541064, 0, 10, 628, t, null);
		Step s629 = new Step("step_escunhau_629", "step", 42.6572499999831,
				0.827689999540193, 0, 10, 629, t, null);
		Step s630 = new Step("step_escunhau_630", "step", 42.6572099999831,
				0.827519999539942, 0, 10, 630, t, null);
		Step s631 = new Step("step_escunhau_631", "step", 42.6571699999831,
				0.82744999953984, 0, 10, 631, t, null);
		Step s632 = new Step("step_escunhau_632", "step", 42.6571199999831,
				0.827369999539722, 0, 10, 632, t, null);
		Step s633 = new Step("step_escunhau_633", "step", 42.6569999999831,
				0.827209999539486, 0, 10, 633, t, null);
		Step s634 = new Step("step_escunhau_634", "step", 42.6569099999831,
				0.827149999539399, 0, 10, 634, t, null);
		Step s635 = new Step("step_escunhau_635", "step", 42.6568499999831,
				0.82712999953937, 0, 10, 635, t, null);
		Step s636 = new Step("step_escunhau_636", "step", 42.6567899999831,
				0.82710999953934, 0, 10, 636, t, null);
		Step s637 = new Step("step_escunhau_637", "step", 42.6567399999831,
				0.827059999539269, 0, 10, 637, t, null);
		Step s638 = new Step("step_escunhau_638", "step", 42.6566499999831,
				0.827029999539224, 0, 10, 638, t, null);
		Step s639 = new Step("step_escunhau_639", "step", 42.6565299999831,
				0.826969999539137, 0, 10, 639, t, null);
		Step s640 = new Step("step_escunhau_640", "step", 42.6564399999831,
				0.82690999953905, 0, 10, 640, t, null);
		Step s641 = new Step("step_escunhau_641", "step", 42.6563599999831,
				0.826789999538873, 0, 10, 641, t, null);
		Step s642 = new Step("step_escunhau_642", "step", 42.6563199999831,
				0.826729999538785, 0, 10, 642, t, null);
		Step s643 = new Step("step_escunhau_643", "step", 42.656239999983,
				0.826639999538651, 0, 10, 643, t, null);
		Step s644 = new Step("step_escunhau_644", "step", 42.6561899999831,
				0.826609999538608, 0, 10, 644, t);
		Step s645 = new Step("step_escunhau_645", "step", 42.6561654531682,
				0.826597854857393, 0, 10, 645, t);
		Step s646 = new Step("step_escunhau_646", "step", 42.6561108797628,
				0.826569255857387, 0, 10, 646, t, null);
		Step s647 = new Step("step_escunhau_647", "step", 42.6560627085683,
				0.82652213366983, 0, 10, 647, t, null);
		Step s648 = new Step("step_escunhau_648", "step", 42.6560279782103,
				0.826471493225761, 0, 10, 648, t, null);
		Step s649 = new Step("step_escunhau_649", "step", 42.6560090541879,
				0.826423352133826, 0, 10, 649, t, null);
		Step s650 = new Step("step_escunhau_650", "step", 42.6559925535126,
				0.8263842767132, 0, 10, 650, t, null);
		Step s651 = new Step("step_escunhau_651", "step", 42.6559785340545,
				0.826357314961448, 0, 10, 651, t, null);
		Step s652 = new Step("step_escunhau_652", "step", 42.6559668800989,
				0.826336370848854, 0, 10, 652, t, null);
		Step s653 = new Step("step_escunhau_653", "step", 42.6559341679952,
				0.826273460165384, 0, 10, 653, t, null);
		Step s654 = new Step("step_escunhau_654", "step", 42.6559064183448,
				0.826234776804671, 0, 10, 654, t, null);
		Step s655 = new Step("step_escunhau_655", "step", 42.655880744871,
				0.826186871069151, 0, 10, 655, t, null);
		Step s656 = new Step("step_escunhau_656", "step", 42.6558664939029,
				0.826147717396001, 0, 10, 656, t, null);
		Step s657 = new Step("step_escunhau_657", "step", 42.6558587029378,
				0.826093088528456, 0, 10, 657, t, null);
		Step s658 = new Step("step_escunhau_658", "step", 42.6558402995809,
				0.826072379706766, 0, 10, 658, t, null);
		Step s659 = new Step("step_escunhau_659", "step", 42.6558191834809,
				0.826027365288287, 0, 10, 659, t, null);
		Step s660 = new Step("step_escunhau_660", "step", 42.6557957018372,
				0.82597633330404, 0, 10, 660, t, null);
		Step s661 = new Step("step_escunhau_661", "step", 42.6557765461236,
				0.825916000551784, 0, 10, 661, t, null);
		Step s662 = new Step("step_escunhau_662", "step", 42.6557512197625,
				0.82588638303581, 0, 10, 662, t);
		Step s663 = new Step("step_escunhau_663", "step", 42.6557192597883,
				0.825863096758205, 0, 10, 663, t, null);
		Step s664 = new Step("step_escunhau_664", "step", 42.6556341152121,
				0.82588436417894, 0, 10, 664, t, null);
		Step s665 = new Step("step_escunhau_665", "step", 42.6555733710245,
				0.82588648114498, 0, 10, 665, t, null);
		Step s666 = new Step("step_escunhau_666", "step", 42.6555189132037,
				0.825863978997329, 0, 10, 666, t, null);
		Step s667 = new Step("step_escunhau_667", "step", 42.6554825694048,
				0.825846945596825, 0, 10, 667, t, null);
		Step s668 = new Step("step_escunhau_668", "step", 42.6554484175138,
				0.825826785825016, 0, 10, 668, t, null);
		Step s669 = new Step("step_escunhau_669", "step", 42.6554045719741,
				0.825770363942931, 0, 10, 669, t, null);
		Step s670 = new Step("step_escunhau_670", "step", 42.6553832821312,
				0.825716206002214, 0, 10, 670, t, null);
		Step s671 = new Step("step_escunhau_671", "step", 42.6553481462819,
				0.825644230715752, 0, 10, 671, t, null);
		Step s672 = new Step("step_escunhau_672", "step", 42.655306434679,
				0.825581634686981, 0, 10, 672, t, null);
		Step s673 = new Step("step_escunhau_673", "step", 42.6552718775727,
				0.825540139300894, 0, 10, 673, t, null);
		Step s674 = new Step("step_escunhau_674", "step", 42.6552201325326,
				0.82554194290529, 0, 10, 674, t, null);
		Step s675 = new Step("step_escunhau_675", "step", 42.6551613487288,
				0.825528741916384, 0, 10, 675, t, null);
		Step s676 = new Step("step_escunhau_676", "step", 42.6551250048845,
				0.825511708802377, 0, 10, 676, t, null);
		Step s677 = new Step("step_escunhau_677", "step", 42.6550726231315,
				0.825479984820828, 0, 10, 677, t, null);
		Step s678 = new Step("step_escunhau_678", "step", 42.6550314902918,
				0.825447868789471, 0, 10, 678, t, null);
		Step s679 = new Step("step_escunhau_679", "step", 42.654999414466,
				0.825418487073975, 0, 10, 679, t, null);
		Step s680 = new Step("step_escunhau_680", "step", 42.6549586868088,
				0.825407706824676, 0, 10, 680, t, null);
		Step s681 = new Step("step_escunhau_681", "step", 42.6549179591508,
				0.825396926588404, 0, 10, 681, t, null);
		Step s682 = new Step("step_escunhau_682", "step", 42.6548840387301,
				0.825388959052868, 0, 10, 682, t, null);
		Step s683 = new Step("step_escunhau_683", "step", 42.6548330384249,
				0.82531143759244, 0, 10, 683, t);
		Step s684 = new Step("step_escunhau_684", "step", 42.6548074224761,
				0.825266581009465, 0, 10, 684, t, null);
		Step s685 = new Step("step_escunhau_685", "step", 42.6547707312595,
				0.825231260447665, 0, 10, 685, t, null);
		Step s686 = new Step("step_escunhau_686", "step", 42.6547341558139,
				0.825202035820004, 0, 10, 686, t, null);
		Step s687 = new Step("step_escunhau_687", "step", 42.6546747351728,
				0.825155307857814, 0, 10, 687, t, null);
		Step s688 = new Step("step_escunhau_688", "step", 42.6546269686816,
				0.825129523413544, 0, 10, 688, t, null);
		Step s689 = new Step("step_escunhau_689", "step", 42.6545428582833,
				0.825086706288795, 0, 10, 689, t, null);
		Step s690 = new Step("step_escunhau_690", "step", 42.6544930735692,
				0.825073192118761, 0, 10, 690, t, null);
		Step s691 = new Step("step_escunhau_691", "step", 42.6544523458832,
				0.825062412170201, 0, 10, 691, t);
		Step s692 = new Step("step_escunhau_692", "step", 42.6543689880889,
				0.82505921831559, 0, 10, 692, t, null);
		Step s693 = new Step("step_escunhau_693", "step", 42.6542814201936,
				0.825071420950397, 0, 10, 693, t, null);
		Step s694 = new Step("step_escunhau_694", "step", 42.6542474997526,
				0.825063453659897, 0, 10, 694, t, null);
		Step s695 = new Step("step_escunhau_695", "step", 42.6542041749167,
				0.825034464663954, 0, 10, 695, t, null);
		Step s696 = new Step("step_escunhau_696", "step", 42.6541806928622,
				0.824983434368218, 0, 10, 696, t, null);
		Step s697 = new Step("step_escunhau_697", "step", 42.6541531164949,
				0.824953896407813, 0, 10, 697, t, null);
		Step s698 = new Step("step_escunhau_698", "step", 42.6541066154849,
				0.824876219322369, 0, 10, 698, t, null);
		Step s699 = new Step("step_escunhau_699", "step", 42.6540605776254,
				0.82482292565608, 0, 10, 699, t, null);
		Step s700 = new Step("step_escunhau_700", "step", 42.6540014463464,
				0.82479143808913, 0, 10, 700, t, null);
		Step s701 = new Step("step_escunhau_701", "step", 42.6539246062953,
				0.824775817647445, 0, 10, 701, t, null);
		Step s702 = new Step("step_escunhau_702", "step", 42.6538552104023,
				0.8247965367474, 0, 10, 702, t, null);
		Step s703 = new Step("step_escunhau_703", "step", 42.6538106200257,
				0.824819440861984, 0, 10, 703, t, null);
		Step s704 = new Step("step_escunhau_704", "step", 42.6537553597092,
				0.824873216116239, 0, 10, 704, t, null);
		Step s705 = new Step("step_escunhau_705", "step", 42.653688792548,
				0.824924335556584, 0, 10, 705, t, null);
		Step s706 = new Step("step_escunhau_706", "step", 42.6536289747132,
				0.824975219585233, 0, 10, 706, t, null);
		Step s707 = new Step("step_escunhau_707", "step", 42.6535979987887,
				0.825003748669323, 0, 10, 707, t, null);
		Step s708 = new Step("step_escunhau_708", "step", 42.6535533504694,
				0.825023604646941, 0, 10, 708, t, null);
		Step s709 = new Step("step_escunhau_709", "step", 42.6535326971521,
				0.825002975318556, 0, 10, 709, t, null);
		Step s710 = new Step("step_escunhau_710", "step", 42.653496237447,
				0.824979847148619, 0, 10, 710, t);
		Step s711 = new Step("step_escunhau_711", "step", 42.6534398191566,
				0.824972664322525, 0, 10, 711, t, null);
		Step s712 = new Step("step_escunhau_712", "step", 42.653403822609,
				0.824973919251237, 0, 10, 712, t, null);
		Step s713 = new Step("step_escunhau_713", "step", 42.6533816142407,
				0.824989942980614, 0, 10, 713, t, null);
		Step s714 = new Step("step_escunhau_714", "step", 42.6533283141464,
				0.825028399883611, 0, 10, 714, t, null);
		Step s715 = new Step("step_escunhau_715", "step", 42.6533065689113,
				0.825068806585416, 0, 10, 715, t, null);
		Step s716 = new Step("step_escunhau_716", "step", 42.653292499266,
				0.825157743991316, 0, 10, 716, t, null);
		Step s717 = new Step("step_escunhau_717", "step", 42.6532776770041,
				0.82520705897875, 0, 10, 717, t, null);
		Step s718 = new Step("step_escunhau_718", "step", 42.6532269160522,
				0.825260676573842, 0, 10, 718, t, null);
		Step s719 = new Step("step_escunhau_719", "step", 42.6531786942854,
				0.825329455014903, 0, 10, 719, t, null);
		Step s720 = new Step("step_escunhau_720", "step", 42.6531481234517,
				0.825379318797314, 0, 10, 720, t, null);
		Step s721 = new Step("step_escunhau_721", "step", 42.6531198602627,
				0.825432151983491, 0, 10, 721, t, null);
		Step s722 = new Step("step_escunhau_722", "step", 42.653112134519,
				0.825499518738101, 0, 10, 722, t, null);
		Step s723 = new Step("step_escunhau_723", "step", 42.6530998512857,
				0.825563994431704, 0, 10, 723, t, null);
		Step s724 = new Step("step_escunhau_724", "step", 42.6531081054692,
				0.825643003764258, 0, 10, 724, t, null);
		Step s725 = new Step("step_escunhau_725", "step", 42.6530931672406,
				0.825686222762621, 0, 10, 725, t, null);
		Step s726 = new Step("step_escunhau_726", "step", 42.6530646724075,
				0.825726864303671, 0, 10, 726, t, null);
		Step s727 = new Step("step_escunhau_727", "step", 42.6530468723572,
				0.825761643313924, 0, 10, 727, t, null);
		Step s728 = new Step("step_escunhau_728", "step", 42.6530467942415,
				0.825804954362334, 0, 10, 728, t);
		Step s729 = new Step("step_escunhau_729", "step", 42.6530480081095,
				0.825845170505533, 0, 10, 729, t, null);
		Step s730 = new Step("step_escunhau_730", "step", 42.6530369407343,
				0.825890694430097, 0, 10, 730, t, null);
		Step s731 = new Step("step_escunhau_731", "step", 42.6530279462695,
				0.82592089669865, 0, 10, 731, t, null);
		Step s732 = new Step("step_escunhau_732", "step", 42.653037042673,
				0.825955348369173, 0, 10, 732, t, null);
		Step s733 = new Step("step_escunhau_733", "step", 42.6530614054302,
				0.825981643375512, 0, 10, 733, t, null);
		Step s734 = new Step("step_escunhau_734", "step", 42.6530774049504,
				0.825982458305967, 0, 10, 734, t, null);
		Step s735 = new Step("step_escunhau_735", "step", 42.6530973976276,
				0.826003873307141, 0, 10, 735, t, null);
		Step s736 = new Step("step_escunhau_736", "step", 42.6531277755237,
				0.826003119760667, 0, 10, 736, t, null);
		Step s737 = new Step("step_escunhau_737", "step", 42.6531386099198,
				0.826016466719561, 0, 10, 737, t, null);
		Step s738 = new Step("step_escunhau_738", "step", 42.6531692533015,
				0.826017838848641, 0, 10, 738, t, null);
		Step s739 = new Step("step_escunhau_739", "step", 42.6531906089072,
				0.826016179736723, 0, 10, 739, t);
		Step s740 = new Step("step_escunhau_740", "step", 42.6532072131338,
				0.826048844955729, 0, 10, 740, t, null);
		Step s741 = new Step("step_escunhau_741", "step", 42.653272021332,
				0.826094775025653, 0, 10, 741, t, null);
		Step s742 = new Step("step_escunhau_742", "step", 42.6533133854476,
				0.826139082148928, 0, 10, 742, t, null);
		Step s743 = new Step("step_escunhau_743", "step", 42.653361730356,
				0.826195345688749, 0, 10, 743, t, null);
		Step s744 = new Step("step_escunhau_744", "step", 42.6534033258799,
				0.826251844476161, 0, 10, 744, t, null);
		Step s745 = new Step("step_escunhau_745", "step", 42.6534363851066,
				0.826333039962014, 0, 10, 745, t, null);
		Step s746 = new Step("step_escunhau_746", "step", 42.6534669052061,
				0.826399074481415, 0, 10, 746, t, null);
		Step s747 = new Step("step_escunhau_747", "step", 42.6534950597711,
				0.826459091668745, 0, 10, 747, t, null);
		Step s748 = new Step("step_escunhau_748", "step", 42.6535158864016,
				0.826488865147032, 0, 10, 748, t, null);
		Step s749 = new Step("step_escunhau_749", "step", 42.6535235035753,
				0.826534348361402, 0, 10, 749, t, null);
		Step s750 = new Step("step_escunhau_750", "step", 42.6535089121263,
				0.826595854788954, 0, 10, 750, t, null);
		Step s751 = new Step("step_escunhau_751", "step", 42.6535053960256,
				0.826647825657751, 0, 10, 751, t, null);
		Step s752 = new Step("step_escunhau_752", "step", 42.6534907466657,
				0.826706284154383, 0, 10, 752, t, null);
		Step s753 = new Step("step_escunhau_753", "step", 42.653482326005,
				0.826737076521902, 0, 10, 753, t);
		Step s754 = new Step("step_escunhau_754", "step", 42.6534632927177,
				0.826801787487938, 0, 10, 754, t, null);
		Step s755 = new Step("step_escunhau_755", "step", 42.6534665837285,
				0.826856571102666, 0, 10, 755, t, null);
		Step s756 = new Step("step_escunhau_756", "step", 42.65345210779,
				0.826924173180054, 0, 10, 756, t, null);
		Step s757 = new Step("step_escunhau_757", "step", 42.6534439762805,
				0.826970204936637, 0, 10, 757, t, null);
		Step s758 = new Step("step_escunhau_758", "step", 42.6534318079011,
				0.827040776493674, 0, 10, 758, t, null);
		Step s759 = new Step("step_escunhau_759", "step", 42.6533943712692,
				0.827084778975547, 0, 10, 759, t, null);
		Step s760 = new Step("step_escunhau_760", "step", 42.6533517988247,
				0.827095411402839, 0, 10, 760, t, null);
		Step s761 = new Step("step_escunhau_761", "step", 42.6533140151437,
				0.827121126507873, 0, 10, 761, t, null);
		Step s762 = new Step("step_escunhau_762", "step", 42.6532807888682,
				0.827149732751908, 0, 10, 762, t, null);
		Step s763 = new Step("step_escunhau_763", "step", 42.6532701761328,
				0.827183651191749, 0, 10, 763, t);
		Step s764 = new Step("step_escunhau_764", "step", 42.6532709858306,
				0.827226321499189, 0, 10, 764, t, null);
		Step s765 = new Step("step_escunhau_765", "step", 42.6532921592696,
				0.827274382419607, 0, 10, 765, t, null);
		Step s766 = new Step("step_escunhau_766", "step", 42.653324755125,
				0.82733119527867, 0, 10, 766, t, null);
		Step s767 = new Step("step_escunhau_767", "step", 42.6533364667195,
				0.827355186600303, 0, 10, 767, t, null);
		Step s768 = new Step("step_escunhau_768", "step", 42.6533382016321,
				0.827446623083061, 0, 10, 768, t, null);
		Step s769 = new Step("step_escunhau_769", "step", 42.6533689524088,
				0.827524849609962, 0, 10, 769, t, null);
		Step s770 = new Step("step_escunhau_770", "step", 42.6533815313527,
				0.8275945592649, 0, 10, 770, t, null);
		Step s771 = new Step("step_escunhau_771", "step", 42.6534138379341,
				0.827636132942355, 0, 10, 771, t, null);
		Step s772 = new Step("step_escunhau_772", "step", 42.6534575090886,
				0.827683410762904, 0, 10, 772, t, null);
		Step s773 = new Step("step_escunhau_773", "step", 42.653489584351,
				0.82771279297337, 0, 10, 773, t, null);
		Step s774 = new Step("step_escunhau_774", "step", 42.653542196808,
				0.827756709678961, 0, 10, 774, t, null);
		Step s775 = new Step("step_escunhau_775", "step", 42.6535648681546,
				0.827765070031788, 0, 10, 775, t, null);
		Step s776 = new Step("step_escunhau_776", "step", 42.653596712116,
				0.827782260748565, 0, 10, 776, t, null);
		Step s777 = new Step("step_escunhau_777", "step", 42.6535974059568,
				0.827818835503552, 0, 10, 777, t, null);
		Step s778 = new Step("step_escunhau_778", "step", 42.6536162137307,
				0.827860879416976, 0, 10, 778, t, null);
		Step s779 = new Step("step_escunhau_779", "step", 42.6536522103459,
				0.827859626143857, 0, 10, 779, t, null);
		Step s780 = new Step("step_escunhau_780", "step", 42.6536811685014,
				0.827843368355429, 0, 10, 780, t, null);
		Step s781 = new Step("step_escunhau_781", "step", 42.6537485465146,
				0.827834922634723, 0, 10, 781, t, null);
		Step s782 = new Step("step_escunhau_782", "step", 42.6538363460796,
				0.827834915634842, 0, 10, 782, t, null);
		Step s783 = new Step("step_escunhau_783", "step", 42.6538588439629,
				0.827834132324025, 0, 10, 783, t, null);
		Step s784 = new Step("step_escunhau_784", "step", 42.6539126654232,
				0.827823108639241, 0, 10, 784, t, null);
		Step s785 = new Step("step_escunhau_785", "step", 42.6539533350715,
				0.827830842408781, 0, 10, 785, t, null);
		Step s786 = new Step("step_escunhau_786", "step", 42.6540050802017,
				0.827829040781812, 0, 10, 786, t, null);
		Step s787 = new Step("step_escunhau_787", "step", 42.654052210115,
				0.827821299976857, 0, 10, 787, t, null);
		Step s788 = new Step("step_escunhau_788", "step", 42.6541101264032,
				0.827788784125495, 0, 10, 788, t, null);
		Step s789 = new Step("step_escunhau_789", "step", 42.654145776086,
				0.827769243251112, 0, 10, 789, t, null);
		Step s790 = new Step("step_escunhau_790", "step", 42.6542369765943,
				0.827711168846106, 0, 10, 790, t);
		Step s791 = new Step("step_escunhau_791", "step", 42.6543033715767,
				0.827650908064314, 0, 10, 791, t, null);
		Step s792 = new Step("step_escunhau_792", "step", 42.6543827448083,
				0.82756274569707, 0, 10, 792, t, null);
		Step s793 = new Step("step_escunhau_793", "step", 42.6545877012555,
				0.827448860120986, 0, 10, 793, t, null);
		Step s794 = new Step("step_escunhau_794", "step", 42.6546838059728,
				0.827411963756861, 0, 10, 794, t, null);
		Step s795 = new Step("step_escunhau_795", "step", 42.6547397614971,
				0.827394765282715, 0, 10, 795, t, null);
		Step s796 = new Step("step_escunhau_796", "step", 42.6548330959015,
				0.827330515486049, 0, 10, 796, t, null);
		Step s797 = new Step("step_escunhau_797", "step", 42.6548887044016,
				0.827295029142691, 0, 10, 797, t, null);
		Step s798 = new Step("step_escunhau_798", "step", 42.6549441393807,
				0.827250398851674, 0, 10, 798, t, null);
		Step s799 = new Step("step_escunhau_799", "step", 42.65500488363,
				0.827248283259614, 0, 10, 799, t, null);
		Step s800 = new Step("step_escunhau_800", "step", 42.6550733546053,
				0.82717879896044, 0, 10, 800, t);
		Step s801 = new Step("step_escunhau_801", "step", 42.6551188071027,
				0.82708266639223, 0, 10, 801, t, null);
		Step s802 = new Step("step_escunhau_802", "step", 42.6551576836862,
				0.826995912694027, 0, 10, 802, t, null);
		Step s803 = new Step("step_escunhau_803", "step", 42.6552025575618,
				0.826869300138222, 0, 10, 803, t, null);
		Step s804 = new Step("step_escunhau_804", "step", 42.6552435102165,
				0.82677332389844, 0, 10, 804, t, null);
		Step s805 = new Step("step_escunhau_805", "step", 42.6552842313798,
				0.826665155629377, 0, 10, 805, t, null);
		Step s806 = new Step("step_escunhau_806", "step", 42.6553112801571,
				0.82654831357676, 0, 10, 806, t, null);
		Step s807 = new Step("step_escunhau_807", "step", 42.6553466915079,
				0.826397630116284, 0, 10, 807, t, null);
		Step s808 = new Step("step_escunhau_808", "step", 42.6553622017665,
				0.826265939900143, 0, 10, 808, t, null);
		Step s809 = new Step("step_escunhau_809", "step", 42.6553814082905,
				0.826210370729374, 0, 10, 809, t, null);
		Step s810 = new Step("step_escunhau_810", "step", 42.6553919628391,
				0.826173402992188, 0, 10, 810, t, null);
		Step s811 = new Step("step_escunhau_811", "step", 42.6554265127429,
				0.826095949111756, 0, 10, 811, t, null);
		Step s812 = new Step("step_escunhau_812", "step", 42.6554664301958,
				0.826064058104267, 0, 10, 812, t, null);
		Step s813 = new Step("step_escunhau_813", "step", 42.6555156361235,
				0.82604709338467, 0, 10, 813, t, null);
		Step s814 = new Step("step_escunhau_814", "step", 42.6555658258143,
				0.826081944447034, 0, 10, 814, t, null);
		Step s815 = new Step("step_escunhau_815", "step", 42.6556095554744,
				0.826132270713396, 0, 10, 815, t, null);
		Step s816 = new Step("step_escunhau_816", "step", 42.65565293792,
				0.826164309089819, 0, 10, 816, t, null);
		Step s817 = new Step("step_escunhau_817", "step", 42.6556854764846,
				0.82621807545081, 0, 10, 817, t, null);
		Step s818 = new Step("step_escunhau_818", "step", 42.6557380895027,
				0.826261992341967, 0, 10, 818, t, null);
		Step s819 = new Step("step_escunhau_819", "step", 42.6557837795624,
				0.826297000480548, 0, 10, 819, t, null);
		Step s820 = new Step("step_escunhau_820", "step", 42.6558288909925,
				0.826301528646995, 0, 10, 820, t, null);
		Step s821 = new Step("step_escunhau_821", "step", 42.6558643089279,
				0.826269794338414, 0, 10, 821, t, null);
		Step s822 = new Step("step_escunhau_822", "step", 42.6558816707083,
				0.826235639142377, 0, 10, 822, t, null);
		Step s823 = new Step("step_escunhau_823", "step", 42.6558902647975,
				0.826213989529764, 0, 10, 823, t, null);
		Step s824 = new Step("step_escunhau_824", "step", 42.655894648638,
				0.826207736729093, 0, 10, 824, t, null);

		try {
			stepDataDao.create(s1);
			stepDataDao.create(s2);
			stepDataDao.create(s3);
			stepDataDao.create(s4);
			stepDataDao.create(s5);
			stepDataDao.create(s6);
			stepDataDao.create(s7);
			stepDataDao.create(s8);
			stepDataDao.create(s9);
			stepDataDao.create(s10);
			stepDataDao.create(s11);
			stepDataDao.create(s12);
			stepDataDao.create(s13);
			stepDataDao.create(s14);
			stepDataDao.create(s15);
			stepDataDao.create(s16);
			stepDataDao.create(s17);
			stepDataDao.create(s18);
			stepDataDao.create(s19);
			stepDataDao.create(s20);
			stepDataDao.create(s21);
			stepDataDao.create(s22);
			stepDataDao.create(s23);
			stepDataDao.create(s24);
			stepDataDao.create(s25);
			stepDataDao.create(s26);
			stepDataDao.create(s27);
			stepDataDao.create(s28);
			stepDataDao.create(s29);
			stepDataDao.create(s30);
			stepDataDao.create(s31);
			stepDataDao.create(s32);
			stepDataDao.create(s33);
			stepDataDao.create(s34);
			stepDataDao.create(s35);
			stepDataDao.create(s36);
			stepDataDao.create(s37);
			stepDataDao.create(s38);
			stepDataDao.create(s39);
			stepDataDao.create(s40);
			stepDataDao.create(s41);
			stepDataDao.create(s42);
			stepDataDao.create(s43);
			stepDataDao.create(s44);
			stepDataDao.create(s45);
			stepDataDao.create(s46);
			stepDataDao.create(s47);
			stepDataDao.create(s48);
			stepDataDao.create(s49);
			stepDataDao.create(s50);
			stepDataDao.create(s51);
			stepDataDao.create(s52);
			stepDataDao.create(s53);
			stepDataDao.create(s54);
			stepDataDao.create(s55);
			stepDataDao.create(s56);
			stepDataDao.create(s57);
			stepDataDao.create(s58);
			stepDataDao.create(s59);
			stepDataDao.create(s60);
			stepDataDao.create(s61);
			stepDataDao.create(s62);
			stepDataDao.create(s63);
			stepDataDao.create(s64);
			stepDataDao.create(s65);
			stepDataDao.create(s66);
			stepDataDao.create(s67);
			stepDataDao.create(s68);
			stepDataDao.create(s69);
			stepDataDao.create(s70);
			stepDataDao.create(s71);
			stepDataDao.create(s72);
			stepDataDao.create(s73);
			stepDataDao.create(s74);
			stepDataDao.create(s75);
			stepDataDao.create(s76);
			stepDataDao.create(s77);
			stepDataDao.create(s78);
			stepDataDao.create(s79);
			stepDataDao.create(s80);
			stepDataDao.create(s81);
			stepDataDao.create(s82);
			stepDataDao.create(s83);
			stepDataDao.create(s84);
			stepDataDao.create(s85);
			stepDataDao.create(s86);
			stepDataDao.create(s87);
			stepDataDao.create(s88);
			stepDataDao.create(s89);
			stepDataDao.create(s90);
			stepDataDao.create(s91);
			stepDataDao.create(s92);
			stepDataDao.create(s93);
			stepDataDao.create(s94);
			stepDataDao.create(s95);
			stepDataDao.create(s96);
			stepDataDao.create(s97);
			stepDataDao.create(s98);
			stepDataDao.create(s99);
			stepDataDao.create(s100);
			stepDataDao.create(s101);
			stepDataDao.create(s102);
			stepDataDao.create(s103);
			stepDataDao.create(s104);
			stepDataDao.create(s105);
			stepDataDao.create(s106);
			stepDataDao.create(s107);
			stepDataDao.create(s108);
			stepDataDao.create(s109);
			stepDataDao.create(s110);
			stepDataDao.create(s111);
			stepDataDao.create(s112);
			stepDataDao.create(s113);
			stepDataDao.create(s114);
			stepDataDao.create(s115);
			stepDataDao.create(s116);
			stepDataDao.create(s117);
			stepDataDao.create(s118);
			stepDataDao.create(s119);
			stepDataDao.create(s120);
			stepDataDao.create(s121);
			stepDataDao.create(s122);
			stepDataDao.create(s123);
			stepDataDao.create(s124);
			stepDataDao.create(s125);
			stepDataDao.create(s126);
			stepDataDao.create(s127);
			stepDataDao.create(s128);
			stepDataDao.create(s129);
			stepDataDao.create(s130);
			stepDataDao.create(s131);
			stepDataDao.create(s132);
			stepDataDao.create(s133);
			stepDataDao.create(s134);
			stepDataDao.create(s135);
			stepDataDao.create(s136);
			stepDataDao.create(s137);
			stepDataDao.create(s138);
			stepDataDao.create(s139);
			stepDataDao.create(s140);
			stepDataDao.create(s141);
			stepDataDao.create(s142);
			stepDataDao.create(s143);
			stepDataDao.create(s144);
			stepDataDao.create(s145);
			stepDataDao.create(s146);
			stepDataDao.create(s147);
			stepDataDao.create(s148);
			stepDataDao.create(s149);
			stepDataDao.create(s150);
			stepDataDao.create(s151);
			stepDataDao.create(s152);
			stepDataDao.create(s153);
			stepDataDao.create(s154);
			stepDataDao.create(s155);
			stepDataDao.create(s156);
			stepDataDao.create(s157);
			stepDataDao.create(s158);
			stepDataDao.create(s159);
			stepDataDao.create(s160);
			stepDataDao.create(s161);
			stepDataDao.create(s162);
			stepDataDao.create(s163);
			stepDataDao.create(s164);
			stepDataDao.create(s165);
			stepDataDao.create(s166);
			stepDataDao.create(s167);
			stepDataDao.create(s168);
			stepDataDao.create(s169);
			stepDataDao.create(s170);
			stepDataDao.create(s171);
			stepDataDao.create(s172);
			stepDataDao.create(s173);
			stepDataDao.create(s174);
			stepDataDao.create(s175);
			stepDataDao.create(s176);
			stepDataDao.create(s177);
			stepDataDao.create(s178);
			stepDataDao.create(s179);
			stepDataDao.create(s180);
			stepDataDao.create(s181);
			stepDataDao.create(s182);
			stepDataDao.create(s183);
			stepDataDao.create(s184);
			stepDataDao.create(s185);
			stepDataDao.create(s186);
			stepDataDao.create(s187);
			stepDataDao.create(s188);
			stepDataDao.create(s189);
			stepDataDao.create(s190);
			stepDataDao.create(s191);
			stepDataDao.create(s192);
			stepDataDao.create(s193);
			stepDataDao.create(s194);
			stepDataDao.create(s195);
			stepDataDao.create(s196);
			stepDataDao.create(s197);
			stepDataDao.create(s198);
			stepDataDao.create(s199);
			stepDataDao.create(s200);
			stepDataDao.create(s201);
			stepDataDao.create(s202);
			stepDataDao.create(s203);
			stepDataDao.create(s204);
			stepDataDao.create(s205);
			stepDataDao.create(s206);
			stepDataDao.create(s207);
			stepDataDao.create(s208);
			stepDataDao.create(s209);
			stepDataDao.create(s210);
			stepDataDao.create(s211);
			stepDataDao.create(s212);
			stepDataDao.create(s213);
			stepDataDao.create(s214);
			stepDataDao.create(s215);
			stepDataDao.create(s216);
			stepDataDao.create(s217);
			stepDataDao.create(s218);
			stepDataDao.create(s219);
			stepDataDao.create(s220);
			stepDataDao.create(s221);
			stepDataDao.create(s222);
			stepDataDao.create(s223);
			stepDataDao.create(s224);
			stepDataDao.create(s225);
			stepDataDao.create(s226);
			stepDataDao.create(s227);
			stepDataDao.create(s228);
			stepDataDao.create(s229);
			stepDataDao.create(s230);
			stepDataDao.create(s231);
			stepDataDao.create(s232);
			stepDataDao.create(s233);
			stepDataDao.create(s234);
			stepDataDao.create(s235);
			stepDataDao.create(s236);
			stepDataDao.create(s237);
			stepDataDao.create(s238);
			stepDataDao.create(s239);
			stepDataDao.create(s240);
			stepDataDao.create(s241);
			stepDataDao.create(s242);
			stepDataDao.create(s243);
			stepDataDao.create(s244);
			stepDataDao.create(s245);
			stepDataDao.create(s246);
			stepDataDao.create(s247);
			stepDataDao.create(s248);
			stepDataDao.create(s249);
			stepDataDao.create(s250);
			stepDataDao.create(s251);
			stepDataDao.create(s252);
			stepDataDao.create(s253);
			stepDataDao.create(s254);
			stepDataDao.create(s255);
			stepDataDao.create(s256);
			stepDataDao.create(s257);
			stepDataDao.create(s258);
			stepDataDao.create(s259);
			stepDataDao.create(s260);
			stepDataDao.create(s261);
			stepDataDao.create(s262);
			stepDataDao.create(s263);
			stepDataDao.create(s264);
			stepDataDao.create(s265);
			stepDataDao.create(s266);
			stepDataDao.create(s267);
			stepDataDao.create(s268);
			stepDataDao.create(s269);
			stepDataDao.create(s270);
			stepDataDao.create(s271);
			stepDataDao.create(s272);
			stepDataDao.create(s273);
			stepDataDao.create(s274);
			stepDataDao.create(s275);
			stepDataDao.create(s276);
			stepDataDao.create(s277);
			stepDataDao.create(s278);
			stepDataDao.create(s279);
			stepDataDao.create(s280);
			stepDataDao.create(s281);
			stepDataDao.create(s282);
			stepDataDao.create(s283);
			stepDataDao.create(s284);
			stepDataDao.create(s285);
			stepDataDao.create(s286);
			stepDataDao.create(s287);
			stepDataDao.create(s288);
			stepDataDao.create(s289);
			stepDataDao.create(s290);
			stepDataDao.create(s291);
			stepDataDao.create(s292);
			stepDataDao.create(s293);
			stepDataDao.create(s294);
			stepDataDao.create(s295);
			stepDataDao.create(s296);
			stepDataDao.create(s297);
			stepDataDao.create(s298);
			stepDataDao.create(s299);
			stepDataDao.create(s300);
			stepDataDao.create(s301);
			stepDataDao.create(s302);
			stepDataDao.create(s303);
			stepDataDao.create(s304);
			stepDataDao.create(s305);
			stepDataDao.create(s306);
			stepDataDao.create(s307);
			stepDataDao.create(s308);
			stepDataDao.create(s309);
			stepDataDao.create(s310);
			stepDataDao.create(s311);
			stepDataDao.create(s312);
			stepDataDao.create(s313);
			stepDataDao.create(s314);
			stepDataDao.create(s315);
			stepDataDao.create(s316);
			stepDataDao.create(s317);
			stepDataDao.create(s318);
			stepDataDao.create(s319);
			stepDataDao.create(s320);
			stepDataDao.create(s321);
			stepDataDao.create(s322);
			stepDataDao.create(s323);
			stepDataDao.create(s324);
			stepDataDao.create(s325);
			stepDataDao.create(s326);
			stepDataDao.create(s327);
			stepDataDao.create(s328);
			stepDataDao.create(s329);
			stepDataDao.create(s330);
			stepDataDao.create(s331);
			stepDataDao.create(s332);
			stepDataDao.create(s333);
			stepDataDao.create(s334);
			stepDataDao.create(s335);
			stepDataDao.create(s336);
			stepDataDao.create(s337);
			stepDataDao.create(s338);
			stepDataDao.create(s339);
			stepDataDao.create(s340);
			stepDataDao.create(s341);
			stepDataDao.create(s342);
			stepDataDao.create(s343);
			stepDataDao.create(s344);
			stepDataDao.create(s345);
			stepDataDao.create(s346);
			stepDataDao.create(s347);
			stepDataDao.create(s348);
			stepDataDao.create(s349);
			stepDataDao.create(s350);
			stepDataDao.create(s351);
			stepDataDao.create(s352);
			stepDataDao.create(s353);
			stepDataDao.create(s354);
			stepDataDao.create(s355);
			stepDataDao.create(s356);
			stepDataDao.create(s357);
			stepDataDao.create(s358);
			stepDataDao.create(s359);
			stepDataDao.create(s360);
			stepDataDao.create(s361);
			stepDataDao.create(s362);
			stepDataDao.create(s363);
			stepDataDao.create(s364);
			stepDataDao.create(s365);
			stepDataDao.create(s366);
			stepDataDao.create(s367);
			stepDataDao.create(s368);
			stepDataDao.create(s369);
			stepDataDao.create(s370);
			stepDataDao.create(s371);
			stepDataDao.create(s372);
			stepDataDao.create(s373);
			stepDataDao.create(s374);
			stepDataDao.create(s375);
			stepDataDao.create(s376);
			stepDataDao.create(s377);
			stepDataDao.create(s378);
			stepDataDao.create(s379);
			stepDataDao.create(s380);
			stepDataDao.create(s381);
			stepDataDao.create(s382);
			stepDataDao.create(s383);
			stepDataDao.create(s384);
			stepDataDao.create(s385);
			stepDataDao.create(s386);
			stepDataDao.create(s387);
			stepDataDao.create(s388);
			stepDataDao.create(s389);
			stepDataDao.create(s390);
			stepDataDao.create(s391);
			stepDataDao.create(s392);
			stepDataDao.create(s393);
			stepDataDao.create(s394);
			stepDataDao.create(s395);
			stepDataDao.create(s396);
			stepDataDao.create(s397);
			stepDataDao.create(s398);
			stepDataDao.create(s399);
			stepDataDao.create(s400);
			stepDataDao.create(s401);
			stepDataDao.create(s402);
			stepDataDao.create(s403);
			stepDataDao.create(s404);
			stepDataDao.create(s405);
			stepDataDao.create(s406);
			stepDataDao.create(s407);
			stepDataDao.create(s408);
			stepDataDao.create(s409);
			stepDataDao.create(s410);
			stepDataDao.create(s411);
			stepDataDao.create(s412);
			stepDataDao.create(s413);
			stepDataDao.create(s414);
			stepDataDao.create(s415);
			stepDataDao.create(s416);
			stepDataDao.create(s417);
			stepDataDao.create(s418);
			stepDataDao.create(s419);
			stepDataDao.create(s420);
			stepDataDao.create(s421);
			stepDataDao.create(s422);
			stepDataDao.create(s423);
			stepDataDao.create(s424);
			stepDataDao.create(s425);
			stepDataDao.create(s426);
			stepDataDao.create(s427);
			stepDataDao.create(s428);
			stepDataDao.create(s429);
			stepDataDao.create(s430);
			stepDataDao.create(s431);
			stepDataDao.create(s432);
			stepDataDao.create(s433);
			stepDataDao.create(s434);
			stepDataDao.create(s435);
			stepDataDao.create(s436);
			stepDataDao.create(s437);
			stepDataDao.create(s438);
			stepDataDao.create(s439);
			stepDataDao.create(s440);
			stepDataDao.create(s441);
			stepDataDao.create(s442);
			stepDataDao.create(s443);
			stepDataDao.create(s444);
			stepDataDao.create(s445);
			stepDataDao.create(s446);
			stepDataDao.create(s447);
			stepDataDao.create(s448);
			stepDataDao.create(s449);
			stepDataDao.create(s450);
			stepDataDao.create(s451);
			stepDataDao.create(s452);
			stepDataDao.create(s453);
			stepDataDao.create(s454);
			stepDataDao.create(s455);
			stepDataDao.create(s456);
			stepDataDao.create(s457);
			stepDataDao.create(s458);
			stepDataDao.create(s459);
			stepDataDao.create(s460);
			stepDataDao.create(s461);
			stepDataDao.create(s462);
			stepDataDao.create(s463);
			stepDataDao.create(s464);
			stepDataDao.create(s465);
			stepDataDao.create(s466);
			stepDataDao.create(s467);
			stepDataDao.create(s468);
			stepDataDao.create(s469);
			stepDataDao.create(s470);
			stepDataDao.create(s471);
			stepDataDao.create(s472);
			stepDataDao.create(s473);
			stepDataDao.create(s474);
			stepDataDao.create(s475);
			stepDataDao.create(s476);
			stepDataDao.create(s477);
			stepDataDao.create(s478);
			stepDataDao.create(s479);
			stepDataDao.create(s480);
			stepDataDao.create(s481);
			stepDataDao.create(s482);
			stepDataDao.create(s483);
			stepDataDao.create(s484);
			stepDataDao.create(s485);
			stepDataDao.create(s486);
			stepDataDao.create(s487);
			stepDataDao.create(s488);
			stepDataDao.create(s489);
			stepDataDao.create(s490);
			stepDataDao.create(s491);
			stepDataDao.create(s492);
			stepDataDao.create(s493);
			stepDataDao.create(s494);
			stepDataDao.create(s495);
			stepDataDao.create(s496);
			stepDataDao.create(s497);
			stepDataDao.create(s498);
			stepDataDao.create(s499);
			stepDataDao.create(s500);
			stepDataDao.create(s501);
			stepDataDao.create(s502);
			stepDataDao.create(s503);
			stepDataDao.create(s504);
			stepDataDao.create(s505);
			stepDataDao.create(s506);
			stepDataDao.create(s507);
			stepDataDao.create(s508);
			stepDataDao.create(s509);
			stepDataDao.create(s510);
			stepDataDao.create(s511);
			stepDataDao.create(s512);
			stepDataDao.create(s513);
			stepDataDao.create(s514);
			stepDataDao.create(s515);
			stepDataDao.create(s516);
			stepDataDao.create(s517);
			stepDataDao.create(s518);
			stepDataDao.create(s519);
			stepDataDao.create(s520);
			stepDataDao.create(s521);
			stepDataDao.create(s522);
			stepDataDao.create(s523);
			stepDataDao.create(s524);
			stepDataDao.create(s525);
			stepDataDao.create(s526);
			stepDataDao.create(s527);
			stepDataDao.create(s528);
			stepDataDao.create(s529);
			stepDataDao.create(s530);
			stepDataDao.create(s531);
			stepDataDao.create(s532);
			stepDataDao.create(s533);
			stepDataDao.create(s534);
			stepDataDao.create(s535);
			stepDataDao.create(s536);
			stepDataDao.create(s537);
			stepDataDao.create(s538);
			stepDataDao.create(s539);
			stepDataDao.create(s540);
			stepDataDao.create(s541);
			stepDataDao.create(s542);
			stepDataDao.create(s543);
			stepDataDao.create(s544);
			stepDataDao.create(s545);
			stepDataDao.create(s546);
			stepDataDao.create(s547);
			stepDataDao.create(s548);
			stepDataDao.create(s549);
			stepDataDao.create(s550);
			stepDataDao.create(s551);
			stepDataDao.create(s552);
			stepDataDao.create(s553);
			stepDataDao.create(s554);
			stepDataDao.create(s555);
			stepDataDao.create(s556);
			stepDataDao.create(s557);
			stepDataDao.create(s558);
			stepDataDao.create(s559);
			stepDataDao.create(s560);
			stepDataDao.create(s561);
			stepDataDao.create(s562);
			stepDataDao.create(s563);
			stepDataDao.create(s564);
			stepDataDao.create(s565);
			stepDataDao.create(s566);
			stepDataDao.create(s567);
			stepDataDao.create(s568);
			stepDataDao.create(s569);
			stepDataDao.create(s570);
			stepDataDao.create(s571);
			stepDataDao.create(s572);
			stepDataDao.create(s573);
			stepDataDao.create(s574);
			stepDataDao.create(s575);
			stepDataDao.create(s576);
			stepDataDao.create(s577);
			stepDataDao.create(s578);
			stepDataDao.create(s579);
			stepDataDao.create(s580);
			stepDataDao.create(s581);
			stepDataDao.create(s582);
			stepDataDao.create(s583);
			stepDataDao.create(s584);
			stepDataDao.create(s585);
			stepDataDao.create(s586);
			stepDataDao.create(s587);
			stepDataDao.create(s588);
			stepDataDao.create(s589);
			stepDataDao.create(s590);
			stepDataDao.create(s591);
			stepDataDao.create(s592);
			stepDataDao.create(s593);
			stepDataDao.create(s594);
			stepDataDao.create(s595);
			stepDataDao.create(s596);
			stepDataDao.create(s597);
			stepDataDao.create(s598);
			stepDataDao.create(s599);
			stepDataDao.create(s600);
			stepDataDao.create(s601);
			stepDataDao.create(s602);
			stepDataDao.create(s603);
			stepDataDao.create(s604);
			stepDataDao.create(s605);
			stepDataDao.create(s606);
			stepDataDao.create(s607);
			stepDataDao.create(s608);
			stepDataDao.create(s609);
			stepDataDao.create(s610);
			stepDataDao.create(s611);
			stepDataDao.create(s612);
			stepDataDao.create(s613);
			stepDataDao.create(s614);
			stepDataDao.create(s615);
			stepDataDao.create(s616);
			stepDataDao.create(s617);
			stepDataDao.create(s618);
			stepDataDao.create(s619);
			stepDataDao.create(s620);
			stepDataDao.create(s621);
			stepDataDao.create(s622);
			stepDataDao.create(s623);
			stepDataDao.create(s624);
			stepDataDao.create(s625);
			stepDataDao.create(s626);
			stepDataDao.create(s627);
			stepDataDao.create(s628);
			stepDataDao.create(s629);
			stepDataDao.create(s630);
			stepDataDao.create(s631);
			stepDataDao.create(s632);
			stepDataDao.create(s633);
			stepDataDao.create(s634);
			stepDataDao.create(s635);
			stepDataDao.create(s636);
			stepDataDao.create(s637);
			stepDataDao.create(s638);
			stepDataDao.create(s639);
			stepDataDao.create(s640);
			stepDataDao.create(s641);
			stepDataDao.create(s642);
			stepDataDao.create(s643);
			stepDataDao.create(s644);
			stepDataDao.create(s645);
			stepDataDao.create(s646);
			stepDataDao.create(s647);
			stepDataDao.create(s648);
			stepDataDao.create(s649);
			stepDataDao.create(s650);
			stepDataDao.create(s651);
			stepDataDao.create(s652);
			stepDataDao.create(s653);
			stepDataDao.create(s654);
			stepDataDao.create(s655);
			stepDataDao.create(s656);
			stepDataDao.create(s657);
			stepDataDao.create(s658);
			stepDataDao.create(s659);
			stepDataDao.create(s660);
			stepDataDao.create(s661);
			stepDataDao.create(s662);
			stepDataDao.create(s663);
			stepDataDao.create(s664);
			stepDataDao.create(s665);
			stepDataDao.create(s666);
			stepDataDao.create(s667);
			stepDataDao.create(s668);
			stepDataDao.create(s669);
			stepDataDao.create(s670);
			stepDataDao.create(s671);
			stepDataDao.create(s672);
			stepDataDao.create(s673);
			stepDataDao.create(s674);
			stepDataDao.create(s675);
			stepDataDao.create(s676);
			stepDataDao.create(s677);
			stepDataDao.create(s678);
			stepDataDao.create(s679);
			stepDataDao.create(s680);
			stepDataDao.create(s681);
			stepDataDao.create(s682);
			stepDataDao.create(s683);
			stepDataDao.create(s684);
			stepDataDao.create(s685);
			stepDataDao.create(s686);
			stepDataDao.create(s687);
			stepDataDao.create(s688);
			stepDataDao.create(s689);
			stepDataDao.create(s690);
			stepDataDao.create(s691);
			stepDataDao.create(s692);
			stepDataDao.create(s693);
			stepDataDao.create(s694);
			stepDataDao.create(s695);
			stepDataDao.create(s696);
			stepDataDao.create(s697);
			stepDataDao.create(s698);
			stepDataDao.create(s699);
			stepDataDao.create(s700);
			stepDataDao.create(s701);
			stepDataDao.create(s702);
			stepDataDao.create(s703);
			stepDataDao.create(s704);
			stepDataDao.create(s705);
			stepDataDao.create(s706);
			stepDataDao.create(s707);
			stepDataDao.create(s708);
			stepDataDao.create(s709);
			stepDataDao.create(s710);
			stepDataDao.create(s711);
			stepDataDao.create(s712);
			stepDataDao.create(s713);
			stepDataDao.create(s714);
			stepDataDao.create(s715);
			stepDataDao.create(s716);
			stepDataDao.create(s717);
			stepDataDao.create(s718);
			stepDataDao.create(s719);
			stepDataDao.create(s720);
			stepDataDao.create(s721);
			stepDataDao.create(s722);
			stepDataDao.create(s723);
			stepDataDao.create(s724);
			stepDataDao.create(s725);
			stepDataDao.create(s726);
			stepDataDao.create(s727);
			stepDataDao.create(s728);
			stepDataDao.create(s729);
			stepDataDao.create(s730);
			stepDataDao.create(s731);
			stepDataDao.create(s732);
			stepDataDao.create(s733);
			stepDataDao.create(s734);
			stepDataDao.create(s735);
			stepDataDao.create(s736);
			stepDataDao.create(s737);
			stepDataDao.create(s738);
			stepDataDao.create(s739);
			stepDataDao.create(s740);
			stepDataDao.create(s741);
			stepDataDao.create(s742);
			stepDataDao.create(s743);
			stepDataDao.create(s744);
			stepDataDao.create(s745);
			stepDataDao.create(s746);
			stepDataDao.create(s747);
			stepDataDao.create(s748);
			stepDataDao.create(s749);
			stepDataDao.create(s750);
			stepDataDao.create(s751);
			stepDataDao.create(s752);
			stepDataDao.create(s753);
			stepDataDao.create(s754);
			stepDataDao.create(s755);
			stepDataDao.create(s756);
			stepDataDao.create(s757);
			stepDataDao.create(s758);
			stepDataDao.create(s759);
			stepDataDao.create(s760);
			stepDataDao.create(s761);
			stepDataDao.create(s762);
			stepDataDao.create(s763);
			stepDataDao.create(s764);
			stepDataDao.create(s765);
			stepDataDao.create(s766);
			stepDataDao.create(s767);
			stepDataDao.create(s768);
			stepDataDao.create(s769);
			stepDataDao.create(s770);
			stepDataDao.create(s771);
			stepDataDao.create(s772);
			stepDataDao.create(s773);
			stepDataDao.create(s774);
			stepDataDao.create(s775);
			stepDataDao.create(s776);
			stepDataDao.create(s777);
			stepDataDao.create(s778);
			stepDataDao.create(s779);
			stepDataDao.create(s780);
			stepDataDao.create(s781);
			stepDataDao.create(s782);
			stepDataDao.create(s783);
			stepDataDao.create(s784);
			stepDataDao.create(s785);
			stepDataDao.create(s786);
			stepDataDao.create(s787);
			stepDataDao.create(s788);
			stepDataDao.create(s789);
			stepDataDao.create(s790);
			stepDataDao.create(s791);
			stepDataDao.create(s792);
			stepDataDao.create(s793);
			stepDataDao.create(s794);
			stepDataDao.create(s795);
			stepDataDao.create(s796);
			stepDataDao.create(s797);
			stepDataDao.create(s798);
			stepDataDao.create(s799);
			stepDataDao.create(s800);
			stepDataDao.create(s801);
			stepDataDao.create(s802);
			stepDataDao.create(s803);
			stepDataDao.create(s804);
			stepDataDao.create(s805);
			stepDataDao.create(s806);
			stepDataDao.create(s807);
			stepDataDao.create(s808);
			stepDataDao.create(s809);
			stepDataDao.create(s810);
			stepDataDao.create(s811);
			stepDataDao.create(s812);
			stepDataDao.create(s813);
			stepDataDao.create(s814);
			stepDataDao.create(s815);
			stepDataDao.create(s816);
			stepDataDao.create(s817);
			stepDataDao.create(s818);
			stepDataDao.create(s819);
			stepDataDao.create(s820);
			stepDataDao.create(s821);
			stepDataDao.create(s822);
			stepDataDao.create(s823);
			stepDataDao.create(s824);
		} catch (RuntimeException ex) {
			Log.e("Inserting step", "Insert error " + ex.toString());
		}
		
		/**
		 * References
		 */
		
//		Reference r1 = new Reference("ref_escunhau_01");
//		r1.setName("panoramica_escunhau.jpg");
//		r1.setTextContent("intimg_escunhau_poi03");
		
		Reference r2 = new Reference("ref_escunhau_02");
		r2.setName("fisics_escunhau.html");
		r2.setTextContent("fisics_escunhau.html");
		
		Reference r3 = new Reference("ref_escunhau_03");
		r3.setName("poi1_escunhau.html");
		r3.setTextContent("poi1_escunhau.html");
		
		Reference r4 = new Reference("ref_escunhau_04");
		r4.setName("poi2_escunhau.html");
		r4.setTextContent("poi2_escunhau.html");
		//-->
		Reference r5 = new Reference("ref_escunhau_05");
		r5.setName("poi4_escunhau.html");
		r5.setTextContent("poi4_escunhau.html");
		
		Reference r6 = new Reference("ref_escunhau_06");
		r6.setName("poi5_escunhau.html");
		r6.setTextContent("poi5_escunhau.html");
		
		Reference r7 = new Reference("ref_escunhau_07");
		r7.setName("poi6_escunhau.html");
		r7.setTextContent("poi6_escunhau.html");
		
		Reference r8 = new Reference("ref_escunhau_08");
		r8.setName("poi7_escunhau.html");
		r8.setTextContent("poi7_escunhau.html");
		
		Reference r9 = new Reference("ref_escunhau_09");
		r9.setName("poi8_escunhau.html");
		r9.setTextContent("poi8_escunhau.html");
		
		Reference r10 = new Reference("ref_escunhau_10");
		r10.setName("poi9_escunhau.html");
		r10.setTextContent("poi9_escunhau.html");
		
		Reference r11 = new Reference("ref_escunhau_11");
		r11.setName("poi10_escunhau.html");
		r11.setTextContent("poi10_escunhau.html");
		
		Reference r12 = new Reference("ref_escunhau_12");
		r12.setName("poi11_escunhau.html");
		r12.setTextContent("poi11_escunhau.html");
		
		Reference r13 = new Reference("ref_escunhau_13");
		r13.setName("poi12_escunhau.html");
		r13.setTextContent("poi12_escunhau.html");
		
		Reference r14 = new Reference("ref_escunhau_14");
		r14.setName("poi13_escunhau.html");
		r14.setTextContent("poi13_escunhau.html");

		try {
			
			//referenceDataDao.create(r1);
			referenceDataDao.create(r2);
			referenceDataDao.create(r3);
			referenceDataDao.create(r4);
			referenceDataDao.create(r5);
			referenceDataDao.create(r6);
			referenceDataDao.create(r7);
			referenceDataDao.create(r8);
			referenceDataDao.create(r9);
			referenceDataDao.create(r10);
			referenceDataDao.create(r11);
			referenceDataDao.create(r12);
			referenceDataDao.create(r13);
			referenceDataDao.create(r14);
			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
		
		
		/**
		 * Interactive images
		 */
		InteractiveImage geo = new InteractiveImage("intimg_escunhau_geo_poi03");
		geo.setMediaPath("panoramica_escunhau_geo.jpg");
		geo.setOriginalWidth(1554);
		geo.setOriginalHeight(480);
		try {
			interactiveImageDataDao.create(geo);
		} catch (Exception ex) {
			Log.e("Inserting interact img geo", "Insert error " + ex.toString());
		}
		
		String msg_arenoses = "Pedres arenoses (gresos i lutites) formades fa 350 milions d’anys (Carbonífer)"; 
		
		Box b13 = new Box("b_escunh_13",0,74,331,480,geo);
		b13.setMessage(msg_arenoses);
		Box b14 = new Box("b_escunh_14",331,276,614,480,geo);
		b14.setMessage(msg_arenoses);
		Box b15 = new Box("b_escunh_15",614,308,1553,480,geo);
		b15.setMessage(msg_arenoses);
		Box b16 = new Box("b_escunh_16",614,276,1247,308,geo);
		b16.setMessage(msg_arenoses);
		Box b17 = new Box("b_escunh_17",1247,11,1553,308,geo);
		b17.setMessage(msg_arenoses);
		Box b18 = new Box("b_escunh_18",331,142,1247,276,geo);
		b18.setMessage("Marbres, formats fa 400 milions d’anys (Devonià-Carbonífer) i metamorfitzats fa 300 milions d’anys (Carbonífer-Permià)");
		Box b19 = new Box("b_escunh_19",331,80,1092,142,geo);
		b19.setMessage("Roques granítiques, formades fa 300 milions d’anys (Carbonífer-Permià)");

		try {
			boxDataDao.create(b13);
			boxDataDao.create(b14);
			boxDataDao.create(b15);
			boxDataDao.create(b16);
			boxDataDao.create(b17);
			boxDataDao.create(b18);
			boxDataDao.create(b19);			
		} catch (Exception ex) {
			Log.e("Inserting box", "Insert error " + ex.toString());
		}
		
		
		InteractiveImage img = new InteractiveImage("intimg_escunhau_poi03");
		img.setMediaPath("panoramica_escunhau.jpg");
		img.setOriginalWidth(1558);
		img.setOriginalHeight(480);
		try {
			interactiveImageDataDao.create(img);
		} catch (Exception ex) {
			Log.e("Inserting interact img", "Insert error " + ex.toString());
		}

		Box b1 = new Box("b_escunh_1", 56,16,104,82, img);
		b1.setMessage("Pujoalbo, 2500 m");
		
		Box b2 = new Box("b_escunh_2", 466,36,508,96, img);
		b2.setMessage("Tuca dera Aubeta, 2487 m");
		
		Box b3 = new Box("b_escunh_3", 619,15,664,79, img);
		b3.setMessage("Cap dera Aubeta, 2532 m");
		
		Box b4 = new Box("b_escunh_4", 915,25,964,90, img);
		b4.setMessage("Sèrra de Rius");
		
		Box b5 = new Box("b_escunh_5", 1209,25,1256,89, img);
		b5.setMessage("Tuca de Betren o des Molassi, 2518 m");
		
		Box b6 = new Box("b_escunh_6", 1357,0,1410,58, img);
		b6.setMessage("Tuc de Roca Blanca, 2460 m");
		
		Box b7 = new Box("b_escunh_7", 702,199,752,265, img);
		b7.setMessage("Vessant d’esbadregalls: És una acumulació de blocs i graves, causada per la fragmentació de la roca de les zones de més altitud, que van baixant pel vessant. En els llocs molt pendents es pot formar un conus d’acumulació com el que veiem, que en aquest cas arriba a penetrar en l’estany. Són les formes del relleu més recent, produides després de que es fonessin els gels de l’última glaciació (fa 15.000 anys) i encara actives.");
		
		Box b8 = new Box("b_escunh_8", 805,230,851,300, img);
		b8.setMessage("Avenc de desguàs de l’estany: Aquest estany no te una sortida d’aigua superficial, sino que s’escola cap el subsòl i surt per filtració en zones més baixes cap el riu. El punt marcat en el mapa és el desguàs principal de l’estany. Es tracta d’un avenc causat per l’erosió de tipus kàrstic, típica de les roques calcàries que formen la vora on es troba.");
		
		Box b9 = new Box("b_escunh_9", 866,148,918,210, img);
		b9.setMessage("Penyasegats: Són de roca calcària, tipus marbre. Aquest tipus de roques es van formar per sedimentació fa uns 400 milions d’anys, en el fons d’un antic mar. Fa uns 300 milions d’anys es van transformar (metamorfitzar) en marbres per efecte d’altes temperatures. Quan es van formar, aquestes roques es disposaven en estrats o capes horitzontals. Però fa uns 30 milions d’anys, quan l’escorça terrestre es va plegar per formar els Pirineus, aquests estrats es van posar en posició vertical. Alguns fragments han quedat exposats i han resistit a l’erosió, i formen parets com la que veiem aquí.");
		
		Box b10 = new Box("b_escunh_10", 1004,264,1053,330, img);
		b10.setMessage("Bassal: Aquesta àrea queda separada de la cubeta principal de l’estany. La poca fondària i l’entrada d’aigua per filtració des de la vora dreta li donen característiques molt diferenciades de la resta de l’estany: aigües molt fredes i tèrboles, i una vegetació espesa i diferent de la de la resta de l’estany que cobreix quasi tot el bassal.");
		
		Box b11 = new Box("b_escunh_11", 1165,165,1215,231, img);
		b11.setMessage("Tot aquest vessant està format per fragments de roca, graves i sorra, sobre els que s’ha format una fina capa de sòl que permet que s’estableixin uns prats en pendent. El subsòl és molt porós, i conté una quantitat d’aigua subterrània important que es recarrega durant el desglaç i va fluint lentament cap a l’estany. Fins i tot durant l’estiu arriba a l’estany aigua freda que prové d’aquest dipòsit ocult sota terra.");

		Box b12 = new Box("b_escunh_12", 863,400,909,459, img);
		b12.setMessage("Morrena: Les morrenes son acumulacions de pedres, que poden tenir sorres i llims, que arrosegaven les antigues glaceres i quedaven dipositades en la part frontal. Quan es van fondre les glaceres, aquestes morrenes formaven moltes vegades represaments naturals que han donat lloc a estanys. Aquest és el cas de l’Estanho d’Escunhau, i ara mateix esteu a sobre de la morrena que va tancar i formar l’estany.");
		
		try {
			boxDataDao.create(b1);
			boxDataDao.create(b2);
			boxDataDao.create(b3);
			boxDataDao.create(b4);
			boxDataDao.create(b5);
			boxDataDao.create(b6);
			boxDataDao.create(b7);
			boxDataDao.create(b8);
			boxDataDao.create(b9);
			boxDataDao.create(b10);
			boxDataDao.create(b11);
			boxDataDao.create(b12);
		} catch (Exception ex) {
			Log.e("Inserting box", "Insert error " + ex.toString());
		}


		/**
		 * HighLights
		 */		
		HighLight h1 = new HighLight("hl_escunhau_poi01",
				"poiE01-Hònt des Audèths", "Fes clic per més informació", 10,
				HighLight.REFERENCE,
				s503);
		h1.setReference(r3);
		
		HighLight h2 = new HighLight("hl_escunhau_poi02",
				"poiE02-vista barranc Malh Nere", "Fes clic per més informació", 10,
				HighLight.REFERENCE,
				s571);
		h2.setReference(r4);
		
		HighLight h3 = new HighLight("hl_escunhau_poi03",
				"poiE03-vista panoràmica", 
				"Fes clic per la foto interactiva...", 10,
				HighLight.INTERACTIVE_IMAGE,s645);
		h3.setInteractiveImage(img);
		
		HighLight h3b = new HighLight("hl_escunhau_poi03b",
				"poiE03-vista panoràmica geologia", 
				"Fes clic per la foto interactiva...", 10,
				HighLight.INTERACTIVE_IMAGE,s645);
		h3b.setInteractiveImage(geo);
		
		HighLight h4 = new HighLight("hl_escunhau_poi04",
				"poiE04-bassal sortida potamogeton petit", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s662);
		h4.setReference(r5);
		
		HighLight h5 = new HighLight("hl_escunhau_poi05",
				"poiE05 potamogeton gros", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s684);
		h5.setReference(r6);
		
		HighLight h6 = new HighLight("hl_escunhau_poi06", "poiE06-esparganis",
				"Fes clic per més informació", 10, HighLight.REFERENCE,s691);
		h6.setReference(r7);
		
		HighLight h7 = new HighLight("hl_escunhau_poi07",
				"poiE07-badia caròfits", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s710);
		h7.setReference(r8);
		
		HighLight h8 = new HighLight("hl_escunhau_poi08",
				"poiE08-engolidor sortida", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s728);
		h8.setReference(r9);
		
		HighLight h9 = new HighLight("hl_escunhau_poi09",
				"poiE09-roca volantins+potamogetons", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s739);
		h9.setReference(r10);
		
		HighLight h10 = new HighLight("hl_escunhau_poi10", "poiE10-tartera",
				"Fes clic per més informació", 10, HighLight.REFERENCE,s753);
		h10.setReference(r11);
		
		HighLight h11 = new HighLight("hl_escunhau_poi11",
				"poiE11-potamogeton mig", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s763);
		h11.setReference(r12);
		
		HighLight h12 = new HighLight("hl_escunhau_poi12",
				"poiE12-zonació macròfits", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s790);
		h12.setReference(r13);
		
		HighLight h13 = new HighLight("hl_escunhau_poi13",
				"poiE13-potamogeton fi en superfície", "Fes clic per més informació", 10,
				HighLight.REFERENCE,s800);
		h13.setReference(r14);
		//, h13, r14
		
		//WP Escunhau
		
		HighLight h14 = new HighLight("hl_escunhau_wpe01","wpE01 - inici", "Entrada a Escunhau per la sortida de la dreta de la rotonda a la C-28 (km 26, de Vielha en direcció a la Bonaigua), i pujada al cap del poble pels carrers principals de Santa Anna i Sant Pere, fins a l’esglèsia de Sant Pèir (0.37 km, desnivell 26 m)", 10,HighLight.WAYPOINT, 
				s1);
		HighLight h15 = new HighLight("hl_escunhau_wpe02","wpE02 - esglèsia de Sant Pèir", "Inici de la pista darrere de l’esglèsia, cap a l’esquerra, fins a la bifurcació de Casarilh (0.2 km, desnivell 20 m)", 10,HighLight.WAYPOINT, 
				s23);
		HighLight h16 = new HighLight("hl_escunhau_wpe03","wpE03 - trencall Casarilh", "Agafar el ramal de la dreta i seguir per la pista. Després d’una corba molt tancada i una ziga-zaga (primer esquerra i després dreta), hi ha una nova bifurcació (0.36 km, desnivell 32 m)", 10,HighLight.WAYPOINT, 
				s33);
		HighLight h17 = new HighLight("hl_escunhau_wpe04","wpE04 - entrada prat", "Seguir recte i deixar la pista que surt cap a l’esquerra, fins la propera corba molt tancada (1.2 km, desnivell 93 m)", 10,HighLight.WAYPOINT, 
				s60);
		HighLight h18 = new HighLight("hl_escunhau_wpe05","wpE05 - trencall Tuca", "Completar la corba a l’esquerra i deixar la pista que surt cap a la dreta. Passar una nova corba tancada i continuar fins el proper desviament (2.7 km, desnivell 139 m)", 10,HighLight.WAYPOINT, 
				s112);
		HighLight h19 = new HighLight("hl_escunhau_wpe06","wpE06 - entrada prat", "Continuar recte, deixant el ramal de la dreta. Seguir la pista, sense trobar cap més bifurcació fin a la Plèta dera Lana (5.7 km, desnivell 485 m)", 10,HighLight.WAYPOINT, 
				s242);
		HighLight h20 = new HighLight("hl_escunhau_wpe07","wpE07 - Plèta dera Lana", "Aparcar el cotxe i continuar a peu en la mateixa direcció que veiem, pel sender que surt des de la tanca en el mur de pedra seca que queda al davant, fins arribar a on el sender creua el riu (0.3 km, desnivell 20 m)", 10,HighLight.WAYPOINT,
				s511);
		HighLight h21 = new HighLight("hl_escunhau_wpe08","wpE08 - gual riu", "Seguir el sender marcat, fins arribar al pletiu on es troba la Cabana d’Estanho i un poc més amunt la colladeta que tanca l’estany (1.5 km, desnivell 203 m). A partir d’aquí baixar a l’estany i recòrrer la vora començant per la banda dreta.", 10,HighLight.WAYPOINT,
				s531);
		HighLight h22 = new HighLight("hl_escunhau_wpe09","wpE09 - final",null, 10,HighLight.WAYPOINT,
				s644);
		
		

		try {
			hlDataDao.create(h1);
			hlDataDao.create(h2);
			hlDataDao.create(h3);
			hlDataDao.create(h3b);
			hlDataDao.create(h4);
			hlDataDao.create(h5);
			hlDataDao.create(h6);
			hlDataDao.create(h7);
			hlDataDao.create(h8);
			hlDataDao.create(h9);
			hlDataDao.create(h10);
			hlDataDao.create(h11);
			hlDataDao.create(h12);
			hlDataDao.create(h13);
			hlDataDao.create(h14);
			hlDataDao.create(h15);
			hlDataDao.create(h16);
			hlDataDao.create(h17);
			hlDataDao.create(h18);
			hlDataDao.create(h19);
			hlDataDao.create(h20);
			hlDataDao.create(h21);
			hlDataDao.create(h22);
		} catch (RuntimeException ex) {
			Log.e("Inserting highlight", "Insert error " + ex.toString());
		}

		
		

		Route r = new Route();
		r.setId("ROUTE_ESCUNHAU");
		r.setName("Estanho d’Escunhau");
		r.setDescription("Itinerari de 10 km i 795 m de desnivell per pista (30-45 min en cotxe, anada), seguit de 1.8 km i 200 m de desnivell a peu (30-45 min, anada), per acabar amb un circuit (1 km) al voltant de l’Estanho d’Escunhau. La pista és de terra i està en bon estat, tret d’algun pas concret. Amb molta precaució és possible fer-la amb un cotxe que no sigui 4x4. El camí a peu és un sender força ben marcat, sense dificultat i amb un pendent moderat. L’itinerari és adequat per a qualsevol persona amb un mínim de forma física. L’Estanho d’Escunhau está situat sota unes escarpades parets de roca calcària, amb formes erosives peculiars, com ara l’avenc per on l’estany desaigua.  La seva característica principal és l’abundància de diverses plantes aquàtiques, que es poden distingir des de les vores, i que arriben al màxim desenvolupament a partir de mitjans d’agost.");
		r.setUserId("1");
		// Ph_ch parameters
		r.setReference(r2);	
		r.setTrack(t);
		r.setLocalCarto("OSMPublicTransport_HiRes.mbtiles");

		try {
			routeDataDao.create(r);
		} catch (RuntimeException ex) {
			Log.e("Inserting route", "Insert error " + ex.toString());
		}
	}

//	public static void loadArtigaDeLin(DataBaseHelper db, Context context) {
//		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
//		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
//		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
//		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
//		RuntimeExceptionDao<Reference, String> referenceDataDao = db
//				.getReferenceDataDao();
//		RuntimeExceptionDao<InteractiveImage, String> interactiveImageDataDao = db
//				.getInteractiveImageDataDao();
//		RuntimeExceptionDao<Box, String> boxDataDao = db.getBoxDataDao();
//
//		Track t = new Track("TRACK_ARTIGA",
//				"Waypoints recorregut Artiga de Lin");
//		try {
//			trackDataDao.create(t);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting track", "Insert error " + ex.toString());
//		}
//
//		/**
//		 * Punts d'interès
//		 */
//		HighLight h1 = new HighLight("hl_artiga_poi01",
//				"POIA01 - Panoràmica Artiga", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		HighLight h2 = new HighLight("hl_artiga_poi02",
//				"POIA02 - Estanyó petit", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		HighLight h3 = new HighLight("hl_artiga_poi03",
//				"POIA03 - Panoràmica Estanho Pois", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		HighLight h4 = new HighLight("hl_artiga_poi04", "POIA04 - Platja Pois",
//				null, 10, HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		HighLight h5 = new HighLight("hl_artiga_poi05",
//				"POIA05 - Panoràmica Aneto", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		HighLight h6 = new HighLight("hl_artiga_poi06",
//				"POIA06 - Estanh Còth deth Hòro", null, 10,
//				HighLight.POINT_OF_INTEREST_OFFICIAL);
//
//		try {
//			hlDataDao.create(h1);
//			hlDataDao.create(h2);
//			hlDataDao.create(h3);
//			hlDataDao.create(h4);
//			hlDataDao.create(h5);
//			hlDataDao.create(h6);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting highlight", "Insert error " + ex.toString());
//		}
//
//		Step s1 = new Step("step_artiga_1", "step", 42.6780874940772,
//				0.70639391424744, 0, 10, 1, t, null);
//		Step s2 = new Step("step_artiga_2", "step", 42.6781726325401,
//				0.706598255667527, 0, 10, 2, t, null);
//		Step s3 = new Step("step_artiga_3", "step", 42.6782750345795,
//				0.706765349338538, 0, 10, 3, t, null);
//		Step s4 = new Step("step_artiga_4", "step", 42.6783771921292,
//				0.706920248138909, 0, 10, 4, t, null);
//		Step s5 = new Step("step_artiga_5", "step", 42.6784630623908,
//				0.707161177576831, 0, 10, 5, t, null);
//		Step s6 = new Step("step_artiga_6", "step", 42.6784939650688,
//				0.707355311730512, 0, 10, 6, t, null);
//		Step s7 = new Step("step_artiga_7", "step", 42.6783546308282,
//				0.707592320410737, 0, 10, 7, t, null);
//		Step s8 = new Step("step_artiga_8", "step", 42.6781972993111,
//				0.707829989853185, 0, 10, 8, t, null);
//		Step s9 = new Step("step_artiga_9", "step", 42.6780640327065,
//				0.707920321030832, 0, 10, 9, t, null);
//		Step s10 = new Step("step_artiga_10", "step", 42.6778847975497,
//				0.707963524856564, 0, 10, 10, t, null);
//		Step s11 = new Step("step_artiga_11", "step", 42.6777143166067,
//				0.707994202233485, 0, 10, 11, t, null);
//		Step s12 = new Step("step_artiga_12", "step", 42.6775995348579,
//				0.708108261155843, 0, 10, 12, t, null);
//		Step s13 = new Step("step_artiga_13", "step", 42.6775049467313,
//				0.708331415343726, 0, 10, 13, t, null);
//		Step s14 = new Step("step_artiga_14", "step", 42.6773838511537,
//				0.708579951731964, 0, 10, 14, t, null);
//		Step s15 = new Step("step_artiga_15", "step", 42.6772520482475,
//				0.708743451385567, 0, 10, 15, t, null);
//		Step s16 = new Step("step_artiga_16", "step", 42.6769648320468,
//				0.70879062125427, 0, 10, 16, t, null);
//		Step s17 = new Step("step_artiga_17", "step", 42.6764774548236,
//				0.708735311354794, 0, 10, 17, t, null);
//		Step s18 = new Step("step_artiga_18", "step", 42.6762792462457,
//				0.708730392933751, 0, 10, 18, t, null);
//		Step s19 = new Step("step_artiga_19", "step", 42.6761362482492,
//				0.708784464995635, 0, 10, 19, t, null);
//		Step s20 = new Step("step_artiga_20", "step", 42.6759426763469,
//				0.709011250702988, 0, 10, 20, t, null);
//		Step s21 = new Step("step_artiga_21", "step", 42.6756771168231,
//				0.709240680394708, 0, 10, 21, t, null);
//		Step s22 = new Step("step_artiga_22", "step", 42.6753976786786,
//				0.709226541507, 0, 10, 22, t, null);
//		Step s23 = new Step("step_artiga_23", "step", 42.6748468247421,
//				0.709149154270196, 0, 10, 23, t, null);
//		Step s24 = new Step("step_artiga_24", "step", 42.6745056178163,
//				0.709198303979824, 0, 10, 24, t, null);
//		Step s25 = new Step("step_artiga_25", "step", 42.6742624171962,
//				0.709195037294174, 0, 10, 25, t, null);
//		Step s26 = new Step("step_artiga_26", "step", 42.6738759742392,
//				0.709233644857135, 0, 10, 26, t, null);
//		Step s27 = new Step("step_artiga_27", "step", 42.6734382246113,
//				0.709408374957781, 0, 10, 27, t, null);
//		Step s28 = new Step("step_artiga_28", "step", 42.6731787348967,
//				0.709491129567422, 0, 10, 28, t, null);
//		Step s29 = new Step("step_artiga_29", "step", 42.6729571902496,
//				0.70967011615414, 0, 10, 29, t, null);
//		Step s30 = new Step("step_artiga_30", "step", 42.6727704192674,
//				0.709786807476522, 0, 10, 30, t, null);
//		Step s31 = new Step("step_artiga_31", "step", 42.6725165389364,
//				0.71015002848364, 0, 10, 31, t, null);
//		Step s32 = new Step("step_artiga_32", "step", 42.672277971784,
//				0.710378448764149, 0, 10, 32, t, null);
//		Step s33 = new Step("step_artiga_33", "step", 42.6720279672937,
//				0.710485255583021, 0, 10, 33, t, null);
//		Step s34 = new Step("step_artiga_34", "step", 42.6717186312228,
//				0.710777286836933, 0, 10, 34, t, null);
//		Step s35 = new Step("step_artiga_35", "step", 42.6714669196175,
//				0.710798732277186, 0, 10, 35, t, null);
//		Step s36 = new Step("step_artiga_36", "step", 42.6712332048111,
//				0.710819516710177, 0, 10, 36, t, null);
//		Step s37 = new Step("step_artiga_37", "step", 42.670875706456,
//				0.710954672299687, 0, 10, 37, t, null);
//		Step s38 = new Step("step_artiga_38", "step", 42.670660963554,
//				0.711023570920359, 0, 10, 38, t, null);
//		Step s39 = new Step("step_artiga_39", "step", 42.6705262311737,
//				0.711040720214603, 0, 10, 39, t, null);
//		Step s40 = new Step("step_artiga_40", "step", 42.670291784914,
//				0.71102492164316, 0, 10, 40, t, null);
//		Step s41 = new Step("step_artiga_41", "step", 42.670121058876,
//				0.711043392200034, 0, 10, 41, t, null);
//		Step s42 = new Step("step_artiga_42", "step", 42.669781069399,
//				0.711153495618405, 0, 10, 42, t, null);
//		Step s43 = new Step("step_artiga_43", "step", 42.66953228267,
//				0.711321263302189, 0, 10, 43, t, null);
//		Step s44 = new Step("step_artiga_44", "step", 42.6691978788256,
//				0.711260322422523, 0, 10, 44, t, null);
//		Step s45 = new Step("step_artiga_45", "step", 42.6690371261049,
//				0.711327236116013, 0, 10, 45, t, null);
//		Step s46 = new Step("step_artiga_46", "step", 42.6684838551714,
//				0.711579391233537, 0, 10, 46, t, null);
//		Step s47 = new Step("step_artiga_47", "step", 42.6681054341831,
//				0.711568874853909, 0, 10, 47, t, null);
//		Step s48 = new Step("step_artiga_48", "step", 42.6679072053218,
//				0.711112460329464, 0, 10, 48, t, null);
//		Step s49 = new Step("step_artiga_49", "step", 42.6678585566515,
//				0.710931210870238, 0, 10, 49, t, null);
//		Step s50 = new Step("step_artiga_50", "step", 42.6676900026221,
//				0.710607936053487, 0, 10, 50, t, null);
//		Step s51 = new Step("step_artiga_51", "step", 42.6674983539097,
//				0.710480747499566, 0, 10, 51, t, null);
//		Step s52 = new Step("step_artiga_52", "step", 42.6673334564616,
//				0.710340375163057, 0, 10, 52, t, null);
//		Step s53 = new Step("step_artiga_53", "step", 42.6671753624981,
//				0.710089933772287, 0, 10, 53, t, null);
//		Step s54 = new Step("step_artiga_54", "step", 42.6669672518181,
//				0.709894329120829, 0, 10, 54, t, null);
//		Step s55 = new Step("step_artiga_55", "step", 42.6668236672116,
//				0.709614763282966, 0, 10, 55, t, null);
//		Step s56 = new Step("step_artiga_56", "step", 42.666709137724,
//				0.709391987500729, 0, 10, 56, t, null);
//		Step s57 = new Step("step_artiga_57", "step", 42.6666545591372,
//				0.709200999857469, 0, 10, 57, t, null);
//		Step s58 = new Step("step_artiga_58", "step", 42.6666346588952,
//				0.708944488590185, 0, 10, 58, t, null);
//		Step s59 = new Step("step_artiga_59", "step", 42.6666053457371,
//				0.708829645609096, 0, 10, 59, t, null);
//		Step s60 = new Step("step_artiga_60", "step", 42.6665131656526,
//				0.70872321381137, 0, 10, 60, t, null);
//		Step s61 = new Step("step_artiga_61", "step", 42.6663302693805,
//				0.708583509645673, 0, 10, 61, t, null);
//		Step s62 = new Step("step_artiga_62", "step", 42.6661177088031,
//				0.70853641145743, 0, 10, 62, t, null);
//		Step s63 = new Step("step_artiga_63", "step", 42.6659490420527,
//				0.708432791678731, 0, 10, 63, t, null);
//		Step s64 = new Step("step_artiga_64", "step", 42.665751199225,
//				0.708446163051702, 0, 10, 64, t, null);
//		Step s65 = new Step("step_artiga_65", "step", 42.6655478810358,
//				0.708410927935236, 0, 10, 65, t, null);
//		Step s66 = new Step("step_artiga_66", "step", 42.6653328955161,
//				0.708467635682434, 0, 10, 66, t, null);
//		Step s67 = new Step("step_artiga_67", "step", 42.6652096021454,
//				0.708606386483448, 0, 10, 67, t, null);
//		Step s68 = new Step("step_artiga_68", "step", 42.6650737869824,
//				0.708794404019768, 0, 10, 68, t, null);
//		Step s69 = new Step("step_artiga_69", "step", 42.6648972346797,
//				0.708971715574049, 0, 10, 69, t, null);
//		Step s70 = new Step("step_artiga_70", "step", 42.6646761779439,
//				0.709175064444704, 0, 10, 70, t, null);
//		Step s71 = new Step("step_artiga_71", "step", 42.6645182316423,
//				0.709382194545018, 0, 10, 71, t, null);
//		Step s72 = new Step("step_artiga_72", "step", 42.6642916992463,
//				0.709536935174847, 0, 10, 72, t, null);
//		Step s73 = new Step("step_artiga_73", "step", 42.6641636613479,
//				0.709663654081805, 0, 10, 73, t, null);
//		Step s74 = new Step("step_artiga_74", "step", 42.6639863759052,
//				0.709804382628057, 0, 10, 74, t, null);
//		Step s75 = new Step("step_artiga_75", "step", 42.6638044691235,
//				0.709939179277252, 0, 10, 75, t, null);
//		Step s76 = new Step("step_artiga_76", "step", 42.663563584745,
//				0.710051737708748, 0, 10, 76, t, null);
//		Step s77 = new Step("step_artiga_77", "step", 42.6631876020482,
//				0.710163154952702, 0, 10, 77, t, null);
//		Step s78 = new Step("step_artiga_78", "step", 42.662667754584,
//				0.710060228730287, 0, 10, 78, t, null);
//		Step s79 = new Step("step_artiga_79", "step", 42.6622387680773,
//				0.710448118269159, 0, 10, 79, t, null);
//		Step s80 = new Step("step_artiga_80", "step", 42.6619015925419,
//				0.710924141322333, 0, 10, 80, t, null);
//		Step s81 = new Step("step_artiga_81", "step", 42.6617258795158,
//				0.710918389402623, 0, 10, 81, t, null);
//		Step s82 = new Step("step_artiga_82", "step", 42.661497384337,
//				0.710749859853263, 0, 10, 82, t, null);
//		Step s83 = new Step("step_artiga_83", "step", 42.6611039917652,
//				0.7104409722183, 0, 10, 83, t, null);
//		Step s84 = new Step("step_artiga_84", "step", 42.6608571446486,
//				0.71048053638603, 0, 10, 84, t, null);
//		Step s85 = new Step("step_artiga_85", "step", 42.6605949713819,
//				0.710429155838838, 0, 10, 85, t, null);
//		Step s86 = new Step("step_artiga_86", "step", 42.6602454844885,
//				0.710289474521873, 0, 10, 86, t, null);
//		Step s87 = new Step("step_artiga_87", "step", 42.6595568378111,
//				0.70985112777171, 0, 10, 87, t, null);
//		Step s88 = new Step("step_artiga_88", "step", 42.6593471916119,
//				0.709724617921489, 0, 10, 88, t, null);
//		Step s89 = new Step("step_artiga_89", "step", 42.659154566577,
//				0.709548681279485, 0, 10, 89, t, null);
//		Step s90 = new Step("step_artiga_90", "step", 42.6588594350902,
//				0.709425312532134, 0, 10, 90, t, null);
//		Step s91 = new Step("step_artiga_91", "step", 42.6586431071699,
//				0.709414955988403, 0, 10, 91, t, null);
//		Step s92 = new Step("step_artiga_92", "step", 42.6581316342018,
//				0.709055524998564, 0, 10, 92, t, h2);
//		Step s93 = new Step("step_artiga_93", "step", 42.6579995715218,
//				0.708981072728697, 0, 10, 93, t, null);
//		Step s94 = new Step("step_artiga_94", "step", 42.657954809412,
//				0.708769209660486, 0, 10, 94, t, null);
//		Step s95 = new Step("step_artiga_95", "step", 42.657939725973,
//				0.708690461079678, 0, 10, 95, t, null);
//		Step s96 = new Step("step_artiga_96", "step", 42.6578430467766,
//				0.70858420901184, 0, 10, 96, t, null);
//		Step s97 = new Step("step_artiga_97", "step", 42.6577281266597,
//				0.708466427024692, 0, 10, 97, t, null);
//		Step s98 = new Step("step_artiga_98", "step", 42.6575338067345,
//				0.708430864660225, 0, 10, 98, t, null);
//		Step s99 = new Step("step_artiga_99", "step", 42.6572820951017,
//				0.708452312194157, 0, 10, 99, t, null);
//		Step s100 = new Step("step_artiga_100", "step", 42.6572017193487,
//				0.708485765610824, 0, 10, 100, t, null);
//		Step s101 = new Step("step_artiga_101", "step", 42.6570993207397,
//				0.708318723985005, 0, 10, 101, t, null);
//		Step s102 = new Step("step_artiga_102", "step", 42.6570157728196,
//				0.708193691085099, 0, 10, 102, t, null);
//		Step s103 = new Step("step_artiga_103", "step", 42.6568953769773,
//				0.708027311707062, 0, 10, 103, t, null);
//		Step s104 = new Step("step_artiga_104", "step", 42.6567499380925,
//				0.707959454570857, 0, 10, 104, t, null);
//		Step s105 = new Step("step_artiga_105", "step", 42.6566063136999,
//				0.707757329477427, 0, 10, 105, t, null);
//		Step s106 = new Step("step_artiga_106", "step", 42.6565099998224,
//				0.707669367809613, 0, 10, 106, t, null);
//		Step s107 = new Step("step_artiga_107", "step", 42.6564871376746,
//				0.707651907780906, 0, 10, 107, t, null);
//		Step s108 = new Step("step_artiga_108", "step", 42.6564823943886,
//				0.707639881971629, 0, 10, 108, t, null);
//		Step s109 = new Step("step_artiga_109", "step", 42.6564454636227,
//				0.707624247548333, 0, 10, 109, t, null);
//		Step s110 = new Step("step_artiga_110", "step", 42.6563904140361,
//				0.707601246266936, 0, 10, 110, t, h3);
//		Step s111 = new Step("step_artiga_111", "step", 42.6563522199472,
//				0.707570410143596, 0, 10, 111, t, null);
//		Step s112 = new Step("step_artiga_112", "step", 42.6562499999542,
//				0.707469998660506, 0, 10, 112, t, null);
//		Step s113 = new Step("step_artiga_113", "step", 42.6562099999542,
//				0.707409998660261, 0, 10, 113, t, null);
//		Step s114 = new Step("step_artiga_114", "step", 42.6561699999542,
//				0.70735999866006, 0, 10, 114, t, null);
//		Step s115 = new Step("step_artiga_115", "step", 42.6560359699567,
//				0.707229501206452, 0, 10, 115, t, null);
//		Step s116 = new Step("step_artiga_116", "step", 42.6558823239695,
//				0.706939942799315, 0, 10, 116, t, null);
//		Step s117 = new Step("step_artiga_117", "step", 42.6557499999541,
//				0.706819998657868, 0, 10, 117, t, null);
//		Step s118 = new Step("step_artiga_118", "step", 42.6556299999541,
//				0.706689998657342, 0, 10, 118, t, null);
//		Step s119 = new Step("step_artiga_119", "step", 42.655589999954,
//				0.706639998657137, 0, 10, 119, t, null);
//		Step s120 = new Step("step_artiga_120", "step", 42.6555466537245,
//				0.706542286744587, 0, 10, 120, t, null);
//		Step s121 = new Step("step_artiga_121", "step", 42.655508177441,
//				0.706506825390633, 0, 10, 121, t, null);
//		Step s122 = new Step("step_artiga_122", "step", 42.6554697168817,
//				0.706409075458465, 0, 10, 122, t, null);
//		Step s123 = new Step("step_artiga_123", "step", 42.6553165289883,
//				0.706258204508854, 0, 10, 123, t, null);
//		Step s124 = new Step("step_artiga_124", "step", 42.6551493258728,
//				0.706147752077774, 0, 10, 124, t, null);
//		Step s125 = new Step("step_artiga_125", "step", 42.6549953717295,
//				0.706080484013809, 0, 10, 125, t, null);
//		Step s126 = new Step("step_artiga_126", "step", 42.6548914746316,
//				0.705992866768404, 0, 10, 126, t, null);
//		Step s127 = new Step("step_artiga_127", "step", 42.6547463035121,
//				0.70581877468682, 0, 10, 127, t, null);
//		Step s128 = new Step("step_artiga_128", "step", 42.6545884644396,
//				0.705654849616083, 0, 10, 128, t, null);
//		Step s129 = new Step("step_artiga_129", "step", 42.6544694281219,
//				0.705629794985222, 0, 10, 129, t, null);
//		Step s130 = new Step("step_artiga_130", "step", 42.6543091661203,
//				0.705569657598968, 0, 10, 130, t, null);
//		Step s131 = new Step("step_artiga_131", "step", 42.654114234239,
//				0.705427914646299, 0, 10, 131, t, null);
//		Step s132 = new Step("step_artiga_132", "step", 42.6539464705562,
//				0.705219333569117, 0, 10, 132, t, null);
//		Step s133 = new Step("step_artiga_133", "step", 42.6537098601125,
//				0.705132861304782, 0, 10, 133, t, null);
//		Step s134 = new Step("step_artiga_134", "step", 42.6535827086216,
//				0.705112009683707, 0, 10, 134, t, null);
//		Step s135 = new Step("step_artiga_135", "step", 42.6534664278152,
//				0.705114975314016, 0, 10, 135, t, null);
//		Step s136 = new Step("step_artiga_136", "step", 42.6533836025282,
//				0.70510901277488, 0, 10, 136, t, null);
//		Step s137 = new Step("step_artiga_137", "step", 42.6532860914891,
//				0.705098382583596, 0, 10, 137, t, h4);
//		Step s138 = new Step("step_artiga_138", "step", 42.653201573024,
//				0.705058478557863, 0, 10, 138, t, null);
//		Step s139 = new Step("step_artiga_139", "step", 42.653099999954,
//				0.704999998650504, 0, 10, 139, t, null);
//		Step s140 = new Step("step_artiga_140", "step", 42.653049999954,
//				0.704939998650259, 0, 10, 140, t, null);
//		Step s141 = new Step("step_artiga_141", "step", 42.6529999999538,
//				0.704899998650097, 0, 10, 141, t, null);
//		Step s142 = new Step("step_artiga_142", "step", 42.6528599999539,
//				0.704809998649732, 0, 10, 142, t, null);
//		Step s143 = new Step("step_artiga_143", "step", 42.6528099999539,
//				0.70476999864957, 0, 10, 143, t, null);
//		Step s144 = new Step("step_artiga_144", "step", 42.6527999999538,
//				0.704689998649241, 0, 10, 144, t, null);
//		Step s145 = new Step("step_artiga_145", "step", 42.6527399999538,
//				0.704659998649121, 0, 10, 145, t, null);
//		Step s146 = new Step("step_artiga_146", "step", 42.6526499999538,
//				0.704589998648834, 0, 10, 146, t, null);
//		Step s147 = new Step("step_artiga_147", "step", 42.6525699999538,
//				0.704479998648386, 0, 10, 147, t, null);
//		Step s148 = new Step("step_artiga_148", "step", 42.6525299999538,
//				0.704409998648099, 0, 10, 148, t, null);
//		Step s149 = new Step("step_artiga_149", "step", 42.6524899999538,
//				0.704359998647897, 0, 10, 149, t, null);
//		Step s150 = new Step("step_artiga_150", "step", 42.6524199999538,
//				0.70427999864757, 0, 10, 150, t, null);
//		Step s151 = new Step("step_artiga_151", "step", 42.6523699999538,
//				0.704229998647363, 0, 10, 151, t, null);
//		Step s152 = new Step("step_artiga_152", "step", 42.6523299999538,
//				0.7041899986472, 0, 10, 152, t, null);
//		Step s153 = new Step("step_artiga_153", "step", 42.6522399999538,
//				0.704179998647164, 0, 10, 153, t, null);
//		Step s154 = new Step("step_artiga_154", "step", 42.6521799999538,
//				0.704189998647206, 0, 10, 154, t, null);
//		Step s155 = new Step("step_artiga_155", "step", 42.6520699999538,
//				0.704149998647046, 0, 10, 155, t, null);
//		Step s156 = new Step("step_artiga_156", "step", 42.6519499999538,
//				0.704099998646844, 0, 10, 156, t, null);
//		Step s157 = new Step("step_artiga_157", "step", 42.6518699999538,
//				0.704039998646599, 0, 10, 157, t, null);
//		Step s158 = new Step("step_artiga_158", "step", 42.6518199999538,
//				0.703999998646438, 0, 10, 158, t, null);
//		Step s159 = new Step("step_artiga_159", "step", 42.6517899999538,
//				0.70393999864619, 0, 10, 159, t, null);
//		Step s160 = new Step("step_artiga_160", "step", 42.6517299999538,
//				0.703909998646069, 0, 10, 160, t, null);
//		Step s161 = new Step("step_artiga_161", "step", 42.6516299999538,
//				0.703859998645866, 0, 10, 161, t, null);
//		Step s162 = new Step("step_artiga_162", "step", 42.6516099999538,
//				0.70378999864558, 0, 10, 162, t, null);
//		Step s163 = new Step("step_artiga_163", "step", 42.6515599999538,
//				0.7037699986455, 0, 10, 163, t, null);
//		Step s164 = new Step("step_artiga_164", "step", 42.6514899999538,
//				0.70375999864546, 0, 10, 164, t, null);
//		Step s165 = new Step("step_artiga_165", "step", 42.6514599999538,
//				0.703689998645172, 0, 10, 165, t, null);
//		Step s166 = new Step("step_artiga_166", "step", 42.6514099999538,
//				0.703639998644969, 0, 10, 166, t, null);
//		Step s167 = new Step("step_artiga_167", "step", 42.6513599999538,
//				0.70362999864493, 0, 10, 167, t, null);
//		Step s168 = new Step("step_artiga_168", "step", 42.6513099999538,
//				0.703579998644724, 0, 10, 168, t, null);
//		Step s169 = new Step("step_artiga_169", "step", 42.6512599999538,
//				0.70352999864452, 0, 10, 169, t, null);
//		Step s170 = new Step("step_artiga_170", "step", 42.6512199999538,
//				0.703469998644275, 0, 10, 170, t, null);
//		Step s171 = new Step("step_artiga_171", "step", 42.6511299999538,
//				0.703349998643782, 0, 10, 171, t, null);
//		Step s172 = new Step("step_artiga_172", "step", 42.6510599999538,
//				0.70329999864358, 0, 10, 172, t, null);
//		Step s173 = new Step("step_artiga_173", "step", 42.6510099999538,
//				0.703339998643745, 0, 10, 173, t, null);
//		Step s174 = new Step("step_artiga_174", "step", 42.6509399999538,
//				0.703339998643748, 0, 10, 174, t, null);
//		Step s175 = new Step("step_artiga_175", "step", 42.6508899999538,
//				0.703319998643668, 0, 10, 175, t, null);
//		Step s176 = new Step("step_artiga_176", "step", 42.6508199999538,
//				0.703309998643629, 0, 10, 176, t, null);
//		Step s177 = new Step("step_artiga_177", "step", 42.6507799999538,
//				0.703239998643342, 0, 10, 177, t, null);
//		Step s178 = new Step("step_artiga_178", "step", 42.6507299999538,
//				0.703179998643096, 0, 10, 178, t, null);
//		Step s179 = new Step("step_artiga_179", "step", 42.6506699999536,
//				0.703169998643056, 0, 10, 179, t, null);
//		Step s180 = new Step("step_artiga_180", "step", 42.6506199999538,
//				0.703189998643141, 0, 10, 180, t, null);
//		Step s181 = new Step("step_artiga_181", "step", 42.6505599999538,
//				0.703219998643266, 0, 10, 181, t, null);
//		Step s182 = new Step("step_artiga_182", "step", 42.6505099999538,
//				0.703229998643309, 0, 10, 182, t, null);
//		Step s183 = new Step("step_artiga_183", "step", 42.6504899999536,
//				0.703129998642898, 0, 10, 183, t, null);
//		Step s184 = new Step("step_artiga_184", "step", 42.6504599999537,
//				0.703089998642734, 0, 10, 184, t, null);
//		Step s185 = new Step("step_artiga_185", "step", 42.6503899999538,
//				0.703159998643025, 0, 10, 185, t, null);
//		Step s186 = new Step("step_artiga_186", "step", 42.6503399999538,
//				0.703139998642942, 0, 10, 186, t, null);
//		Step s187 = new Step("step_artiga_187", "step", 42.6502599999536,
//				0.703099998642782, 0, 10, 187, t, null);
//		Step s188 = new Step("step_artiga_188", "step", 42.6501999999536,
//				0.703079998642703, 0, 10, 188, t, null);
//		Step s189 = new Step("step_artiga_189", "step", 42.6501199999538,
//				0.703079998642704, 0, 10, 189, t, null);
//		Step s190 = new Step("step_artiga_190", "step", 42.6500599999536,
//				0.703099998642788, 0, 10, 190, t, null);
//		Step s191 = new Step("step_artiga_191", "step", 42.6499699999536,
//				0.703099998642793, 0, 10, 191, t, null);
//		Step s192 = new Step("step_artiga_192", "step", 42.6498899999537,
//				0.703069998642671, 0, 10, 192, t, null);
//		Step s193 = new Step("step_artiga_193", "step", 42.6497999999538,
//				0.703059998642634, 0, 10, 193, t, null);
//		Step s194 = new Step("step_artiga_194", "step", 42.6497499999536,
//				0.703079998642717, 0, 10, 194, t, null);
//		Step s195 = new Step("step_artiga_195", "step", 42.6496899999538,
//				0.70308999864276, 0, 10, 195, t, null);
//		Step s196 = new Step("step_artiga_196", "step", 42.6496399999537,
//				0.703089998642763, 0, 10, 196, t, null);
//		Step s197 = new Step("step_artiga_197", "step", 42.6495799999538,
//				0.703079998642724, 0, 10, 197, t, null);
//		Step s198 = new Step("step_artiga_198", "step", 42.6494999999536,
//				0.703079998642728, 0, 10, 198, t, null);
//		Step s199 = new Step("step_artiga_199", "step", 42.6494399999537,
//				0.703049998642604, 0, 10, 199, t, null);
//		Step s200 = new Step("step_artiga_200", "step", 42.6494199999536,
//				0.703039998642564, 0, 10, 200, t, null);
//		Step s201 = new Step("step_artiga_201", "step", 42.6493799999538,
//				0.703069998642688, 0, 10, 201, t, null);
//		Step s202 = new Step("step_artiga_202", "step", 42.6493499999536,
//				0.703009998642442, 0, 10, 202, t, null);
//		Step s203 = new Step("step_artiga_203", "step", 42.6492999999537,
//				0.703009998642444, 0, 10, 203, t, null);
//		Step s204 = new Step("step_artiga_204", "step", 42.6492599999536,
//				0.702959998642241, 0, 10, 204, t, null);
//		Step s205 = new Step("step_artiga_205", "step", 42.6492099999536,
//				0.702969998642284, 0, 10, 205, t, null);
//		Step s206 = new Step("step_artiga_206", "step", 42.6491599999537,
//				0.703009998642449, 0, 10, 206, t, null);
//		Step s207 = new Step("step_artiga_207", "step", 42.6491599999537,
//				0.702939998642158, 0, 10, 207, t, null);
//		Step s208 = new Step("step_artiga_208", "step", 42.6490799999536,
//				0.70290999864204, 0, 10, 208, t, null);
//		Step s209 = new Step("step_artiga_209", "step", 42.6489999999536,
//				0.702909998642042, 0, 10, 209, t, null);
//		Step s210 = new Step("step_artiga_210", "step", 42.6489399999537,
//				0.702859998641839, 0, 10, 210, t, null);
//		Step s211 = new Step("step_artiga_211", "step", 42.6488799999536,
//				0.702869998641882, 0, 10, 211, t, null);
//		Step s212 = new Step("step_artiga_212", "step", 42.6488299999536,
//				0.702839998641759, 0, 10, 212, t, null);
//		Step s213 = new Step("step_artiga_213", "step", 42.6487299999536,
//				0.702829998641722, 0, 10, 213, t, null);
//		Step s214 = new Step("step_artiga_214", "step", 42.6486799999536,
//				0.702829998641722, 0, 10, 214, t, null);
//		Step s215 = new Step("step_artiga_215", "step", 42.6485999999536,
//				0.702739998641353, 0, 10, 215, t, null);
//		Step s216 = new Step("step_artiga_216", "step", 42.6485199999536,
//				0.702719998641276, 0, 10, 216, t, null);
//		Step s217 = new Step("step_artiga_217", "step", 42.6484799999536,
//				0.70267999864111, 0, 10, 217, t, null);
//		Step s218 = new Step("step_artiga_218", "step", 42.6484699999536,
//				0.7027499986414, 0, 10, 218, t, null);
//		Step s219 = new Step("step_artiga_219", "step", 42.6484599999536,
//				0.702799998641608, 0, 10, 219, t, null);
//		Step s220 = new Step("step_artiga_220", "step", 42.6483199999536,
//				0.702799998641612, 0, 10, 220, t, null);
//		Step s221 = new Step("step_artiga_221", "step", 42.6481999999536,
//				0.702709998641245, 0, 10, 221, t, null);
//		Step s222 = new Step("step_artiga_222", "step", 42.6482499999536,
//				0.702719998641283, 0, 10, 222, t, null);
//		Step s223 = new Step("step_artiga_223", "step", 42.6482499999536,
//				0.702649998640994, 0, 10, 223, t, null);
//		Step s224 = new Step("step_artiga_224", "step", 42.6481799999536,
//				0.70259999864079, 0, 10, 224, t, null);
//		Step s225 = new Step("step_artiga_225", "step", 42.6480899999536,
//				0.70255999864063, 0, 10, 225, t, null);
//		Step s226 = new Step("step_artiga_226", "step", 42.6479599999536,
//				0.70252999864051, 0, 10, 226, t, null);
//		Step s227 = new Step("step_artiga_227", "step", 42.6478999999536,
//				0.702529998640511, 0, 10, 227, t, null);
//		Step s228 = new Step("step_artiga_228", "step", 42.6479599999536,
//				0.702469998640261, 0, 10, 228, t, null);
//		Step s229 = new Step("step_artiga_229", "step", 42.6478999999536,
//				0.702459998640222, 0, 10, 229, t, null);
//		Step s230 = new Step("step_artiga_230", "step", 42.6478199999536,
//				0.702459998640224, 0, 10, 230, t, null);
//		Step s231 = new Step("step_artiga_231", "step", 42.6477699999536,
//				0.702399998639981, 0, 10, 231, t, null);
//		Step s232 = new Step("step_artiga_232", "step", 42.6476499999536,
//				0.702259998639404, 0, 10, 232, t, null);
//		Step s233 = new Step("step_artiga_233", "step", 42.6477099999536,
//				0.70223999863932, 0, 10, 233, t, null);
//		Step s234 = new Step("step_artiga_234", "step", 42.6476799999536,
//				0.702189998639113, 0, 10, 234, t, null);
//		Step s235 = new Step("step_artiga_235", "step", 42.6476199999536,
//				0.702209998639199, 0, 10, 235, t, null);
//		Step s236 = new Step("step_artiga_236", "step", 42.6475499999536,
//				0.702179998639077, 0, 10, 236, t, null);
//		Step s237 = new Step("step_artiga_237", "step", 42.6474799999536,
//				0.702069998638623, 0, 10, 237, t, null);
//		Step s238 = new Step("step_artiga_238", "step", 42.6474599999536,
//				0.701999998638334, 0, 10, 238, t, null);
//		Step s239 = new Step("step_artiga_239", "step", 42.6474499999536,
//				0.701899998637922, 0, 10, 239, t, null);
//		Step s240 = new Step("step_artiga_240", "step", 42.6474599999536,
//				0.701839998637672, 0, 10, 240, t, null);
//		Step s241 = new Step("step_artiga_241", "step", 42.6474499999536,
//				0.701759998637342, 0, 10, 241, t, null);
//		Step s242 = new Step("step_artiga_242", "step", 42.6473899999536,
//				0.701679998637013, 0, 10, 242, t, null);
//		Step s243 = new Step("step_artiga_243", "step", 42.6473699999536,
//				0.701609998636724, 0, 10, 243, t, null);
//		Step s244 = new Step("step_artiga_244", "step", 42.6473799999536,
//				0.701489998636226, 0, 10, 244, t, null);
//		Step s245 = new Step("step_artiga_245", "step", 42.6474299999536,
//				0.701449998636058, 0, 10, 245, t, null);
//		Step s246 = new Step("step_artiga_246", "step", 42.6474499999536,
//				0.701379998635768, 0, 10, 246, t, null);
//		Step s247 = new Step("step_artiga_247", "step", 42.6474299999536,
//				0.701309998635477, 0, 10, 247, t, null);
//		Step s248 = new Step("step_artiga_248", "step", 42.6474399999534,
//				0.701219998635105, 0, 10, 248, t, null);
//		Step s249 = new Step("step_artiga_249", "step", 42.6474499999534,
//				0.701149998634815, 0, 10, 249, t, null);
//		Step s250 = new Step("step_artiga_250", "step", 42.6474899999534,
//				0.70099999863419, 0, 10, 250, t, null);
//		Step s251 = new Step("step_artiga_251", "step", 42.6474899999534,
//				0.700909998633817, 0, 10, 251, t, null);
//		Step s252 = new Step("step_artiga_252", "step", 42.6474399999534,
//				0.700769998633238, 0, 10, 252, t, null);
//		Step s253 = new Step("step_artiga_253", "step", 42.6474099999534,
//				0.700649998632742, 0, 10, 253, t, null);
//		Step s254 = new Step("step_artiga_254", "step", 42.6474599999534,
//				0.700499998632115, 0, 10, 254, t, null);
//		Step s255 = new Step("step_artiga_255", "step", 42.6474599999534,
//				0.700419998631784, 0, 10, 255, t, null);
//		Step s256 = new Step("step_artiga_256", "step", 42.6475099999534,
//				0.700379998631617, 0, 10, 256, t, null);
//		Step s257 = new Step("step_artiga_257", "step", 42.6475599999534,
//				0.70035999863153, 0, 10, 257, t, null);
//		Step s258 = new Step("step_artiga_258", "step", 42.6476199999534,
//				0.700299998631277, 0, 10, 258, t, null);
//		Step s259 = new Step("step_artiga_259", "step", 42.6476899999534,
//				0.700299998631275, 0, 10, 259, t, null);
//		Step s260 = new Step("step_artiga_260", "step", 42.6477899999534,
//				0.700259998631108, 0, 10, 260, t, null);
//		Step s261 = new Step("step_artiga_261", "step", 42.6478199999534,
//				0.700199998630855, 0, 10, 261, t, null);
//		Step s262 = new Step("step_artiga_262", "step", 42.6477499999534,
//				0.700139998630608, 0, 10, 262, t, null);
//		Step s263 = new Step("step_artiga_263", "step", 42.6477599999534,
//				0.700149998630651, 0, 10, 263, t, null);
//		Step s264 = new Step("step_artiga_264", "step", 42.6477199999534,
//				0.700149998630652, 0, 10, 264, t, null);
//		Step s265 = new Step("step_artiga_265", "step", 42.6477499999534,
//				0.700039998630195, 0, 10, 265, t, null);
//		Step s266 = new Step("step_artiga_266", "step", 42.6477899999534,
//				0.699969998629901, 0, 10, 266, t, null);
//		Step s267 = new Step("step_artiga_267", "step", 42.6477899999534,
//				0.69988999862957, 0, 10, 267, t, null);
//		Step s268 = new Step("step_artiga_268", "step", 42.6477999999533,
//				0.699809998629235, 0, 10, 268, t, null);
//		Step s269 = new Step("step_artiga_269", "step", 42.6478199999533,
//				0.699739998628941, 0, 10, 269, t, null);
//		Step s270 = new Step("step_artiga_270", "step", 42.6478199999533,
//				0.699659998628608, 0, 10, 270, t, null);
//		Step s271 = new Step("step_artiga_271", "step", 42.6478399999532,
//				0.699579998628275, 0, 10, 271, t, null);
//		Step s272 = new Step("step_artiga_272", "step", 42.6479099999532,
//				0.699499998627941, 0, 10, 272, t, null);
//		Step s273 = new Step("step_artiga_273", "step", 42.6479299999532,
//				0.699439998627689, 0, 10, 273, t, null);
//		Step s274 = new Step("step_artiga_274", "step", 42.6479799999532,
//				0.699409998627562, 0, 10, 274, t, null);
//		Step s275 = new Step("step_artiga_275", "step", 42.6480199999532,
//				0.699379998627436, 0, 10, 275, t, null);
//		Step s276 = new Step("step_artiga_276", "step", 42.6480699999532,
//				0.699379998627433, 0, 10, 276, t, null);
//		Step s277 = new Step("step_artiga_277", "step", 42.6480999999532,
//				0.699289998627058, 0, 10, 277, t, null);
//		Step s278 = new Step("step_artiga_278", "step", 42.6480899999532,
//				0.699209998626724, 0, 10, 278, t, null);
//		Step s279 = new Step("step_artiga_279", "step", 42.6481199999532,
//				0.699159998626515, 0, 10, 279, t, null);
//		Step s280 = new Step("step_artiga_280", "step", 42.6481199999532,
//				0.699099998626267, 0, 10, 280, t, null);
//		Step s281 = new Step("step_artiga_281", "step", 42.6481199999532,
//				0.699019998625932, 0, 10, 281, t, null);
//		Step s282 = new Step("step_artiga_282", "step", 42.6481499999532,
//				0.69895999862568, 0, 10, 282, t, null);
//		Step s283 = new Step("step_artiga_283", "step", 42.6481999999532,
//				0.69889999862543, 0, 10, 283, t, null);
//		Step s284 = new Step("step_artiga_284", "step", 42.6482199999532,
//				0.69879999862501, 0, 10, 284, t, null);
//		Step s285 = new Step("step_artiga_285", "step", 42.6482499999532,
//				0.698599998624174, 0, 10, 285, t, null);
//		Step s286 = new Step("step_artiga_286", "step", 42.6482599999532,
//				0.698529998623882, 0, 10, 286, t, null);
//		Step s287 = new Step("step_artiga_287", "step", 42.6482799999532,
//				0.698449998623546, 0, 10, 287, t, null);
//		Step s288 = new Step("step_artiga_288", "step", 42.6483099999532,
//				0.698329998623043, 0, 10, 288, t, null);
//		Step s289 = new Step("step_artiga_289", "step", 42.6483499999532,
//				0.698279998622833, 0, 10, 289, t, null);
//		Step s290 = new Step("step_artiga_290", "step", 42.6483999999532,
//				0.698259998622748, 0, 10, 290, t, null);
//		Step s291 = new Step("step_artiga_291", "step", 42.6484399999532,
//				0.698209998622539, 0, 10, 291, t, null);
//		Step s292 = new Step("step_artiga_292", "step", 42.6484599999532,
//				0.698139998622244, 0, 10, 292, t, null);
//		Step s293 = new Step("step_artiga_293", "step", 42.6485299999532,
//				0.698089998622034, 0, 10, 293, t, null);
//		Step s294 = new Step("step_artiga_294", "step", 42.6485399999531,
//				0.697989998621614, 0, 10, 294, t, null);
//		Step s295 = new Step("step_artiga_295", "step", 42.648569999953,
//				0.697829998620944, 0, 10, 295, t, null);
//		Step s296 = new Step("step_artiga_296", "step", 42.648589999953,
//				0.697769998620692, 0, 10, 296, t, null);
//		Step s297 = new Step("step_artiga_297", "step", 42.648619999953,
//				0.697659998620232, 0, 10, 297, t, null);
//		Step s298 = new Step("step_artiga_298", "step", 42.648679999953,
//				0.697639998620143, 0, 10, 298, t, null);
//		Step s299 = new Step("step_artiga_299", "step", 42.648849999953,
//				0.69752999861968, 0, 10, 299, t, null);
//		Step s300 = new Step("step_artiga_300", "step", 42.648839999953,
//				0.697449998619344, 0, 10, 300, t, null);
//		Step s301 = new Step("step_artiga_301", "step", 42.648859999953,
//				0.697379998619051, 0, 10, 301, t, null);
//		Step s302 = new Step("step_artiga_302", "step", 42.648829999953,
//				0.697269998618589, 0, 10, 302, t, null);
//		Step s303 = new Step("step_artiga_303", "step", 42.648859999953,
//				0.697209998618336, 0, 10, 303, t, null);
//		Step s304 = new Step("step_artiga_304", "step", 42.648899999953,
//				0.697159998618127, 0, 10, 304, t, null);
//		Step s305 = new Step("step_artiga_305", "step", 42.648939999953,
//				0.697049998617663, 0, 10, 305, t, null);
//		Step s306 = new Step("step_artiga_306", "step", 42.648979999953,
//				0.697009998617495, 0, 10, 306, t, null);
//		Step s307 = new Step("step_artiga_307", "step", 42.649009999953,
//				0.696949998617242, 0, 10, 307, t, null);
//		Step s308 = new Step("step_artiga_308", "step", 42.649079999953,
//				0.696829998616737, 0, 10, 308, t, null);
//		Step s309 = new Step("step_artiga_309", "step", 42.649129999953,
//				0.696729998616315, 0, 10, 309, t, null);
//		Step s310 = new Step("step_artiga_310", "step", 42.649169999953,
//				0.696689998616146, 0, 10, 310, t, null);
//		Step s311 = new Step("step_artiga_311", "step", 42.6492099999529,
//				0.696629998615895, 0, 10, 311, t, null);
//		Step s312 = new Step("step_artiga_312", "step", 42.649239999953,
//				0.69651999861543, 0, 10, 312, t, null);
//		Step s313 = new Step("step_artiga_313", "step", 42.6492699999529,
//				0.696449998615135, 0, 10, 313, t, null);
//		Step s314 = new Step("step_artiga_314", "step", 42.6493299999529,
//				0.696459998615175, 0, 10, 314, t, null);
//		Step s315 = new Step("step_artiga_315", "step", 42.6494099999529,
//				0.696389998614878, 0, 10, 315, t, null);
//		Step s316 = new Step("step_artiga_316", "step", 42.6494599999528,
//				0.696329998614624, 0, 10, 316, t, null);
//		Step s317 = new Step("step_artiga_317", "step", 42.6494920307125,
//				0.696277533552514, 0, 10, 317, t, null);
//		Step s318 = new Step("step_artiga_318", "step", 42.649555832867,
//				0.696246157014432, 0, 10, 318, t, null);
//		Step s319 = new Step("step_artiga_319", "step", 42.6495999999528,
//				0.69617999861399, 0, 10, 319, t, null);
//		Step s320 = new Step("step_artiga_320", "step", 42.6496199999528,
//				0.696109998613694, 0, 10, 320, t, null);
//		Step s321 = new Step("step_artiga_321", "step", 42.6496199999528,
//				0.696009998613273, 0, 10, 321, t, null);
//		Step s322 = new Step("step_artiga_322", "step", 42.6496299999528,
//				0.69593999861298, 0, 10, 322, t, null);
//		Step s323 = new Step("step_artiga_323", "step", 42.6496799999528,
//				0.695889998612768, 0, 10, 323, t, null);
//		Step s324 = new Step("step_artiga_324", "step", 42.6497999999528,
//				0.695859998612638, 0, 10, 324, t, null);
//		Step s325 = new Step("step_artiga_325", "step", 42.6499599999528,
//				0.695769998612254, 0, 10, 325, t, h5);
//		Step s326 = new Step("step_artiga_326", "step", 42.6499599999528,
//				0.695679998611874, 0, 10, 326, t, null);
//		Step s327 = new Step("step_artiga_327", "step", 42.6499299999528,
//				0.695649998611748, 0, 10, 327, t, null);
//		Step s328 = new Step("step_artiga_328", "step", 42.6498799999528,
//				0.695589998611498, 0, 10, 328, t, null);
//		Step s329 = new Step("step_artiga_329", "step", 42.6498499999528,
//				0.695479998611036, 0, 10, 329, t, null);
//		Step s330 = new Step("step_artiga_330", "step", 42.6497999999528,
//				0.695419998610787, 0, 10, 330, t, null);
//		Step s331 = new Step("step_artiga_331", "step", 42.6497499999528,
//				0.695319998610365, 0, 10, 331, t, null);
//		Step s332 = new Step("step_artiga_332", "step", 42.6497099999528,
//				0.695259998610115, 0, 10, 332, t, null);
//		Step s333 = new Step("step_artiga_333", "step", 42.6496699999528,
//				0.695219998609949, 0, 10, 333, t, null);
//		Step s334 = new Step("step_artiga_334", "step", 42.6496499999528,
//				0.695159998609697, 0, 10, 334, t, null);
//		Step s335 = new Step("step_artiga_335", "step", 42.6495999999528,
//				0.695109998609488, 0, 10, 335, t, null);
//		Step s336 = new Step("step_artiga_336", "step", 42.6495599999528,
//				0.69506999860932, 0, 10, 336, t, null);
//		Step s337 = new Step("step_artiga_337", "step", 42.6494799999528,
//				0.69500999860907, 0, 10, 337, t, null);
//		Step s338 = new Step("step_artiga_338", "step", 42.6493999999528,
//				0.69489999860861, 0, 10, 338, t, null);
//		Step s339 = new Step("step_artiga_339", "step", 42.6492699999528,
//				0.694809998608234, 0, 10, 339, t, null);
//		Step s340 = new Step("step_artiga_340", "step", 42.6492499999528,
//				0.694729998607897, 0, 10, 340, t, null);
//		Step s341 = new Step("step_artiga_341", "step", 42.6491999999528,
//				0.694699998607772, 0, 10, 341, t, null);
//		Step s342 = new Step("step_artiga_342", "step", 42.6491199999528,
//				0.694679998607691, 0, 10, 342, t, null);
//		Step s343 = new Step("step_artiga_343", "step", 42.6490799999527,
//				0.694629998607483, 0, 10, 343, t, null);
//		Step s344 = new Step("step_artiga_344", "step", 42.6490299999526,
//				0.694589998607316, 0, 10, 344, t, null);
//		Step s345 = new Step("step_artiga_345", "step", 42.6489799999527,
//				0.694579998607275, 0, 10, 345, t, null);
//		Step s346 = new Step("step_artiga_346", "step", 42.6489299999526,
//				0.694529998607066, 0, 10, 346, t, null);
//		Step s347 = new Step("step_artiga_347", "step", 42.6488599999526,
//				0.694459998606772, 0, 10, 347, t, null);
//		Step s348 = new Step("step_artiga_348", "step", 42.6487399999526,
//				0.694329998606229, 0, 10, 348, t, null);
//		Step s349 = new Step("step_artiga_349", "step", 42.6487099999526,
//				0.694259998605934, 0, 10, 349, t, null);
//		Step s350 = new Step("step_artiga_350", "step", 42.6486799999526,
//				0.694189998605639, 0, 10, 350, t, null);
//		Step s351 = new Step("step_artiga_351", "step", 42.6486499999526,
//				0.694069998605135, 0, 10, 351, t, null);
//		Step s352 = new Step("step_artiga_352", "step", 42.6486399999526,
//				0.693999998604838, 0, 10, 352, t, null);
//		Step s353 = new Step("step_artiga_353", "step", 42.6486199999526,
//				0.693919998604502, 0, 10, 353, t, null);
//		Step s354 = new Step("step_artiga_354", "step", 42.6486299999526,
//				0.693839998604162, 0, 10, 354, t, null);
//		Step s355 = new Step("step_artiga_355", "step", 42.6486899999526,
//				0.693769998603865, 0, 10, 355, t, null);
//		Step s356 = new Step("step_artiga_356", "step", 42.6487199999526,
//				0.693699998603568, 0, 10, 356, t, null);
//		Step s357 = new Step("step_artiga_357", "step", 42.6486399999526,
//				0.693449998602512, 0, 10, 357, t, null);
//		Step s358 = new Step("step_artiga_358", "step", 42.6485698220586,
//				0.693411333586057, 0, 10, 358, t, null);
//		Step s359 = new Step("step_artiga_359", "step", 42.6485796468986,
//				0.693320916882744, 0, 10, 359, t, null);
//		Step s360 = new Step("step_artiga_360", "step", 42.6486604430582,
//				0.693176333440883, 0, 10, 360, t, null);
//		Step s361 = new Step("step_artiga_361", "step", 42.6487484396991,
//				0.693150668022171, 0, 10, 361, t, null);
//		Step s362 = new Step("step_artiga_362", "step", 42.6488138177057,
//				0.693119739479062, 0, 10, 362, t, null);
//		Step s363 = new Step("step_artiga_363", "step", 42.6488914266399,
//				0.693109215223147, 0, 10, 363, t, null);
//		Step s364 = new Step("step_artiga_364", "step", 42.6489818997924,
//				0.693086143637086, 0, 10, 364, t, null);
//		Step s365 = new Step("step_artiga_365", "step", 42.6491199999525,
//				0.693029998600718, 0, 10, 365, t, null);
//		Step s366 = new Step("step_artiga_366", "step", 42.6491399999525,
//				0.692959998600421, 0, 10, 366, t, null);
//		Step s367 = new Step("step_artiga_367", "step", 42.6492199999524,
//				0.692879998600078, 0, 10, 367, t, null);
//		Step s368 = new Step("step_artiga_368", "step", 42.6493499999525,
//				0.692849998599946, 0, 10, 368, t, null);
//		Step s369 = new Step("step_artiga_369", "step", 42.6494099999525,
//				0.692859998599985, 0, 10, 369, t, null);
//		Step s370 = new Step("step_artiga_370", "step", 42.6495299999524,
//				0.692779998599643, 0, 10, 370, t, null);
//		Step s371 = new Step("step_artiga_371", "step", 42.6495899999525,
//				0.692759998599554, 0, 10, 371, t, null);
//		Step s372 = new Step("step_artiga_372", "step", 42.6496499999524,
//				0.692769998599597, 0, 10, 372, t, null);
//		Step s373 = new Step("step_artiga_373", "step", 42.6496899999524,
//				0.692729998599425, 0, 10, 373, t, null);
//		Step s374 = new Step("step_artiga_374", "step", 42.6497901418921,
//				0.692697667349449, 0, 10, 374, t, null);
//		Step s375 = new Step("step_artiga_375", "step", 42.649944414551,
//				0.692686884883166, 0, 10, 375, t, null);
//		Step s376 = new Step("step_artiga_376", "step", 42.6500542929118,
//				0.692654912912677, 0, 10, 376, t, null);
//		Step s377 = new Step("step_artiga_377", "step", 42.6502474315001,
//				0.692636915373026, 0, 10, 377, t, null);
//		Step s378 = new Step("step_artiga_378", "step", 42.6503794241362,
//				0.692712670865908, 0, 10, 378, t, null);
//		Step s379 = new Step("step_artiga_379", "step", 42.6505145564897,
//				0.692684552184795, 0, 10, 379, t, null);
//		Step s380 = new Step("step_artiga_380", "step", 42.6506299999524,
//				0.692739998599433, 0, 10, 380, t, null);
//		Step s381 = new Step("step_artiga_381", "step", 42.6507099999524,
//				0.692719998599346, 0, 10, 381, t, null);
//		Step s382 = new Step("step_artiga_382", "step", 42.6507699999524,
//				0.692739998599428, 0, 10, 382, t, null);
//		Step s383 = new Step("step_artiga_383", "step", 42.6508699999524,
//				0.692649998599041, 0, 10, 383, t, null);
//		Step s384 = new Step("step_artiga_384", "step", 42.6509699999524,
//				0.692579998598742, 0, 10, 384, t, null);
//		Step s385 = new Step("step_artiga_385", "step", 42.6510399999524,
//				0.692619998598908, 0, 10, 385, t, null);
//		Step s386 = new Step("step_artiga_386", "step", 42.6510899999524,
//				0.69262999859895, 0, 10, 386, t, null);
//		Step s387 = new Step("step_artiga_387", "step", 42.6512199999524,
//				0.692559998598647, 0, 10, 387, t, null);
//		Step s388 = new Step("step_artiga_388", "step", 42.6512999999524,
//				0.692429998598093, 0, 10, 388, t, null);
//		Step s389 = new Step("step_artiga_389", "step", 42.6512599999524,
//				0.692359998597799, 0, 10, 389, t, null);
//		Step s390 = new Step("step_artiga_390", "step", 42.6512699999524,
//				0.692289998597501, 0, 10, 390, t, null);
//		Step s391 = new Step("step_artiga_391", "step", 42.6512999999524,
//				0.692229998597245, 0, 10, 391, t, null);
//		Step s392 = new Step("step_artiga_392", "step", 42.6511901395951,
//				0.692179422093369, 0, 10, 392, t, null);
//		Step s393 = new Step("step_artiga_393", "step", 42.6510810163314,
//				0.692165415647261, 0, 10, 393, t, null);
//		Step s394 = new Step("step_artiga_394", "step", 42.6510305239734,
//				0.692071913781839, 0, 10, 394, t, null);
//		Step s395 = new Step("step_artiga_395", "step", 42.6511808965408,
//				0.691891687419782, 0, 10, 395, t, null);
//		Step s396 = new Step("step_artiga_396", "step", 42.651267869005,
//				0.691786970022742, 0, 10, 396, t, null);
//		Step s397 = new Step("step_artiga_397", "step", 42.6514062981239,
//				0.691777941423554, 0, 10, 397, t, null);
//		Step s398 = new Step("step_artiga_398", "step", 42.6514941906377,
//				0.691752753114766, 0, 10, 398, t, null);
//		Step s399 = new Step("step_artiga_399", "step", 42.6515878286451,
//				0.69170579908271, 0, 10, 399, t, null);
//		Step s400 = new Step("step_artiga_400", "step", 42.6516987278742,
//				0.691589852682355, 0, 10, 400, t, null);
//		Step s401 = new Step("step_artiga_401", "step", 42.6518348634628,
//				0.691510309553591, 0, 10, 401, t, null);
//		Step s402 = new Step("step_artiga_402", "step", 42.6519401192034,
//				0.691432406669908, 0, 10, 402, t, null);
//		Step s403 = new Step("step_artiga_403", "step", 42.6520749884911,
//				0.69134876741875, 0, 10, 403, t, null);
//		Step s404 = new Step("step_artiga_404", "step", 42.6521494640565,
//				0.691296419515734, 0, 10, 404, t, null);
//		Step s405 = new Step("step_artiga_405", "step", 42.6522396239088,
//				0.691215658522509, 0, 10, 405, t, null);
//		Step s406 = new Step("step_artiga_406", "step", 42.6523310801226,
//				0.691297226837849, 0, 10, 406, t, null);
//		Step s407 = new Step("step_artiga_407", "step", 42.6524299999524,
//				0.691349998593464, 0, 10, 407, t, null);
//		Step s408 = new Step("step_artiga_408", "step", 42.652481547868,
//				0.691255307140365, 0, 10, 408, t, null);
//		Step s409 = new Step("step_artiga_409", "step", 42.6525774174165,
//				0.691273758864647, 0, 10, 409, t, null);
//		Step s410 = new Step("step_artiga_410", "step", 42.6526386656654,
//				0.69120040312848, 0, 10, 410, t, null);
//		Step s411 = new Step("step_artiga_411", "step", 42.6527299999522,
//				0.691179998592729, 0, 10, 411, t, null);
//		Step s412 = new Step("step_artiga_412", "step", 42.6527699999522,
//				0.691119998592471, 0, 10, 412, t, null);
//		Step s413 = new Step("step_artiga_413", "step", 42.6528099999522,
//				0.691009998592004, 0, 10, 413, t, null);
//		Step s414 = new Step("step_artiga_414", "step", 42.6528099999522,
//				0.690929998591661, 0, 10, 414, t, null);
//		Step s415 = new Step("step_artiga_415", "step", 42.6528399999522,
//				0.690849998591319, 0, 10, 415, t, null);
//		Step s416 = new Step("step_artiga_416", "step", 42.6528499999522,
//				0.690769998590977, 0, 10, 416, t, null);
//		Step s417 = new Step("step_artiga_417", "step", 42.6528499999522,
//				0.690689998590637, 0, 10, 417, t, null);
//		Step s418 = new Step("step_artiga_418", "step", 42.6528999999522,
//				0.690639998590422, 0, 10, 418, t, null);
//		Step s419 = new Step("step_artiga_419", "step", 42.6529699999522,
//				0.690549998590036, 0, 10, 419, t, null);
//		Step s420 = new Step("step_artiga_420", "step", 42.6530199999522,
//				0.690469998589693, 0, 10, 420, t, null);
//		Step s421 = new Step("step_artiga_421", "step", 42.6530299999522,
//				0.690369998589267, 0, 10, 421, t, null);
//		Step s422 = new Step("step_artiga_422", "step", 42.6530299999522,
//				0.690279998588881, 0, 10, 422, t, null);
//		Step s423 = new Step("step_artiga_423", "step", 42.6530299999522,
//				0.690069998587987, 0, 10, 423, t, null);
//		Step s424 = new Step("step_artiga_424", "step", 42.6530499999522,
//				0.690029998587813, 0, 10, 424, t, null);
//		Step s425 = new Step("step_artiga_425", "step", 42.6530999999522,
//				0.690079998588024, 0, 10, 425, t, null);
//		Step s426 = new Step("step_artiga_426", "step", 42.6531099999522,
//				0.689999998587684, 0, 10, 426, t, null);
//		Step s427 = new Step("step_artiga_427", "step", 42.6531799999522,
//				0.690019998587766, 0, 10, 427, t, null);
//		Step s428 = new Step("step_artiga_428", "step", 42.6532299999522,
//				0.690109998588149, 0, 10, 428, t, null);
//		Step s429 = new Step("step_artiga_429", "step", 42.6532899999522,
//				0.690099998588104, 0, 10, 429, t, null);
//		Step s430 = new Step("step_artiga_430", "step", 42.6533615314402,
//				0.690089240395326, 0, 10, 430, t, null);
//		Step s431 = new Step("step_artiga_431", "step", 42.6533899999522,
//				0.690049998587888, 0, 10, 431, t, null);
//		Step s432 = new Step("step_artiga_432", "step", 42.6534499999522,
//				0.690019998587759, 0, 10, 432, t, null);
//		Step s433 = new Step("step_artiga_433", "step", 42.6534599999522,
//				0.689969998587543, 0, 10, 433, t, null);
//		Step s434 = new Step("step_artiga_434", "step", 42.6534799999522,
//				0.689929998587371, 0, 10, 434, t, null);
//		Step s435 = new Step("step_artiga_435", "step", 42.6535299999522,
//				0.689909998587284, 0, 10, 435, t, null);
//		Step s436 = new Step("step_artiga_436", "step", 42.6535299999522,
//				0.689829998586942, 0, 10, 436, t, null);
//		Step s437 = new Step("step_artiga_437", "step", 42.6535199999522,
//				0.689749998586601, 0, 10, 437, t, null);
//		Step s438 = new Step("step_artiga_438", "step", 42.6535699999522,
//				0.689749998586598, 0, 10, 438, t, null);
//		Step s439 = new Step("step_artiga_439", "step", 42.6535899999521,
//				0.689549998585743, 0, 10, 439, t, null);
//		Step s440 = new Step("step_artiga_440", "step", 42.653529999952,
//				0.689409998585147, 0, 10, 440, t, null);
//		Step s441 = new Step("step_artiga_441", "step", 42.653559999952,
//				0.689359998584931, 0, 10, 441, t, null);
//		Step s442 = new Step("step_artiga_442", "step", 42.653579999952,
//				0.689299998584675, 0, 10, 442, t, null);
//		Step s443 = new Step("step_artiga_443", "step", 42.653529999952,
//				0.689159998584077, 0, 10, 443, t, null);
//		Step s444 = new Step("step_artiga_444", "step", 42.653509999952,
//				0.689089998583778, 0, 10, 444, t, null);
//		Step s445 = new Step("step_artiga_445", "step", 42.653569999952,
//				0.688989998583348, 0, 10, 445, t, null);
//		Step s446 = new Step("step_artiga_446", "step", 42.653589999952,
//				0.688839998582705, 0, 10, 446, t, null);
//		Step s447 = new Step("step_artiga_447", "step", 42.653589999952,
//				0.688729998582235, 0, 10, 447, t, null);
//		Step s448 = new Step("step_artiga_448", "step", 42.653609999952,
//				0.688649998581889, 0, 10, 448, t, null);
//		Step s449 = new Step("step_artiga_449", "step", 42.653639999952,
//				0.688519998581332, 0, 10, 449, t, null);
//		Step s450 = new Step("step_artiga_450", "step", 42.653679999952,
//				0.688349998580602, 0, 10, 450, t, null);
//		Step s451 = new Step("step_artiga_451", "step", 42.653699999952,
//				0.688269998580258, 0, 10, 451, t, null);
//		Step s452 = new Step("step_artiga_452", "step", 42.6537699999519,
//				0.688119998579613, 0, 10, 452, t, null);
//		Step s453 = new Step("step_artiga_453", "step", 42.6539399999518,
//				0.687819998578316, 0, 10, 453, t, null);
//		Step s454 = new Step("step_artiga_454", "step", 42.6540199999518,
//				0.687629998577499, 0, 10, 454, t, null);
//		Step s455 = new Step("step_artiga_455", "step", 42.6540999999518,
//				0.687479998576851, 0, 10, 455, t, null);
//		Step s456 = new Step("step_artiga_456", "step", 42.6541599999518,
//				0.687489998576893, 0, 10, 456, t, null);
//		Step s457 = new Step("step_artiga_457", "step", 42.6542999999518,
//				0.687469998576801, 0, 10, 457, t, null);
//		Step s458 = new Step("step_artiga_458", "step", 42.6543399999518,
//				0.68740999857654, 0, 10, 458, t, null);
//		Step s459 = new Step("step_artiga_459", "step", 42.6543099999518,
//				0.687329998576199, 0, 10, 459, t, null);
//		Step s460 = new Step("step_artiga_460", "step", 42.6543599999518,
//				0.687289998576024, 0, 10, 460, t, null);
//		Step s461 = new Step("step_artiga_461", "step", 42.6544199999518,
//				0.687239998575807, 0, 10, 461, t, null);
//		Step s462 = new Step("step_artiga_462", "step", 42.6544299999518,
//				0.687139998575376, 0, 10, 462, t, null);
//		Step s463 = new Step("step_artiga_463", "step", 42.6544399999518,
//				0.687049998574988, 0, 10, 463, t, null);
//		Step s464 = new Step("step_artiga_464", "step", 42.6545899999518,
//				0.68670999857352, 0, 10, 464, t, null);
//		Step s465 = new Step("step_artiga_465", "step", 42.6546199999518,
//				0.686629998573174, 0, 10, 465, t, null);
//		Step s466 = new Step("step_artiga_466", "step", 42.6546799999518,
//				0.68655999857287, 0, 10, 466, t, null);
//		Step s467 = new Step("step_artiga_467", "step", 42.6547999999518,
//				0.686489998572563, 0, 10, 467, t, null);
//		Step s468 = new Step("step_artiga_468", "step", 42.6548799999518,
//				0.686409998572214, 0, 10, 468, t, null);
//		Step s469 = new Step("step_artiga_469", "step", 42.6549899999517,
//				0.686279998571653, 0, 10, 469, t, null);
//		Step s470 = new Step("step_artiga_470", "step", 42.6552199999516,
//				0.686029998570564, 0, 10, 470, t, null);
//		Step s471 = new Step("step_artiga_471", "step", 42.6552699999516,
//				0.685909998570046, 0, 10, 471, t, null);
//		Step s472 = new Step("step_artiga_472", "step", 42.6553199999516,
//				0.685859998569827, 0, 10, 472, t, null);
//		Step s473 = new Step("step_artiga_473", "step", 42.6553499999516,
//				0.68580999856961, 0, 10, 473, t, null);
//		Step s474 = new Step("step_artiga_474", "step", 42.6554099999516,
//				0.685799998569566, 0, 10, 474, t, null);
//		Step s475 = new Step("step_artiga_475", "step", 42.6554699999516,
//				0.685799998569564, 0, 10, 475, t, null);
//		Step s476 = new Step("step_artiga_476", "step", 42.6555299999516,
//				0.685769998569429, 0, 10, 476, t, null);
//		Step s477 = new Step("step_artiga_477", "step", 42.6555899999516,
//				0.685809998569603, 0, 10, 477, t, null);
//		Step s478 = new Step("step_artiga_478", "step", 42.6557099999516,
//				0.68584999856977, 0, 10, 478, t, null);
//		Step s479 = new Step("step_artiga_479", "step", 42.6557699999516,
//				0.685819998569639, 0, 10, 479, t, null);
//		Step s480 = new Step("step_artiga_480", "step", 42.6558599999516,
//				0.685839998569721, 0, 10, 480, t, null);
//		Step s481 = new Step("step_artiga_481", "step", 42.6559099999516,
//				0.685899998569978, 0, 10, 481, t, null);
//		Step s482 = new Step("step_artiga_482", "step", 42.6559699999516,
//				0.685939998570149, 0, 10, 482, t, null);
//		Step s483 = new Step("step_artiga_483", "step", 42.6560699999516,
//				0.685989998570361, 0, 10, 483, t, null);
//		Step s484 = new Step("step_artiga_484", "step", 42.6561099999516,
//				0.685969998570272, 0, 10, 484, t, null);
//		Step s485 = new Step("step_artiga_485", "step", 42.6562299999516,
//				0.685699998569104, 0, 10, 485, t, null);
//		Step s486 = new Step("step_artiga_486", "step", 42.6563599999516,
//				0.685619998568752, 0, 10, 486, t, null);
//		Step s487 = new Step("step_artiga_487", "step", 42.6563899999516,
//				0.685669998568966, 0, 10, 487, t, null);
//		Step s488 = new Step("step_artiga_488", "step", 42.6564299999516,
//				0.685669998568965, 0, 10, 488, t, null);
//		Step s489 = new Step("step_artiga_489", "step", 42.6565199999516,
//				0.685699998569092, 0, 10, 489, t, null);
//		Step s490 = new Step("step_artiga_490", "step", 42.6565599999516,
//				0.685699998569092, 0, 10, 490, t, null);
//		Step s491 = new Step("step_artiga_491", "step", 42.6565999999516,
//				0.685589998568614, 0, 10, 491, t, null);
//		Step s492 = new Step("step_artiga_492", "step", 42.6566399999516,
//				0.685539998568396, 0, 10, 492, t, null);
//		Step s493 = new Step("step_artiga_493", "step", 42.6566899999516,
//				0.685579998568567, 0, 10, 493, t, null);
//		Step s494 = new Step("step_artiga_494", "step", 42.6567299999516,
//				0.685539998568393, 0, 10, 494, t, null);
//		Step s495 = new Step("step_artiga_495", "step", 42.6567799999516,
//				0.685489998568175, 0, 10, 495, t, null);
//		Step s496 = new Step("step_artiga_496", "step", 42.6568499999516,
//				0.685459998568043, 0, 10, 496, t, null);
//		Step s497 = new Step("step_artiga_497", "step", 42.6569599999516,
//				0.685379998567693, 0, 10, 497, t, null);
//		Step s498 = new Step("step_artiga_498", "step", 42.6570899999516,
//				0.685269998567212, 0, 10, 498, t, null);
//		Step s499 = new Step("step_artiga_499", "step", 42.6571799999516,
//				0.685299998567339, 0, 10, 499, t, null);
//		Step s500 = new Step("step_artiga_500", "step", 42.6571999999516,
//				0.685359998567598, 0, 10, 500, t, null);
//		Step s501 = new Step("step_artiga_501", "step", 42.6572099999516,
//				0.685439998567944, 0, 10, 501, t, null);
//		Step s502 = new Step("step_artiga_502", "step", 42.6572099999516,
//				0.685529998568332, 0, 10, 502, t, null);
//		Step s503 = new Step("step_artiga_503", "step", 42.6572399999516,
//				0.685769998569368, 0, 10, 503, t, null);
//		Step s504 = new Step("step_artiga_504", "step", 42.6572499999516,
//				0.685859998569757, 0, 10, 504, t, null);
//		Step s505 = new Step("step_artiga_505", "step", 42.6572799999516,
//				0.68631999857174, 0, 10, 505, t, null);
//		Step s506 = new Step("step_artiga_506", "step", 42.6572599999516,
//				0.686469998572388, 0, 10, 506, t, null);
//		Step s507 = new Step("step_artiga_507", "step", 42.6572599999518,
//				0.686589998572903, 0, 10, 507, t, null);
//		Step s508 = new Step("step_artiga_508", "step", 42.6572399999518,
//				0.686669998573249, 0, 10, 508, t, null);
//		Step s509 = new Step("step_artiga_509", "step", 42.6571699999518,
//				0.686739998573554, 0, 10, 509, t, null);
//		Step s510 = new Step("step_artiga_510", "step", 42.6571399999518,
//				0.68678999857377, 0, 10, 510, t, null);
//		Step s511 = new Step("step_artiga_511", "step", 42.6571599999518,
//				0.686869998574113, 0, 10, 511, t, null);
//		Step s512 = new Step("step_artiga_512", "step", 42.6572199999518,
//				0.686949998574456, 0, 10, 512, t, null);
//		Step s513 = new Step("step_artiga_513", "step", 42.6572799999518,
//				0.68693999857441, 0, 10, 513, t, null);
//		Step s514 = new Step("step_artiga_514", "step", 42.6574599999518,
//				0.686889998574189, 0, 10, 514, t, null);
//		Step s515 = new Step("step_artiga_515", "step", 42.6575499999518,
//				0.686849998574013, 0, 10, 515, t, null);
//		Step s516 = new Step("step_artiga_516", "step", 42.6575999999518,
//				0.686829998573924, 0, 10, 516, t, null);
//		Step s517 = new Step("step_artiga_517", "step", 42.6577299999518,
//				0.686739998573532, 0, 10, 517, t, null);
//		Step s518 = new Step("step_artiga_518", "step", 42.6577499999518,
//				0.686659998573187, 0, 10, 518, t, null);
//		Step s519 = new Step("step_artiga_519", "step", 42.6578499999518,
//				0.686559998572751, 0, 10, 519, t, null);
//		Step s520 = new Step("step_artiga_520", "step", 42.6579399999518,
//				0.686569998572791, 0, 10, 520, t, null);
//		Step s521 = new Step("step_artiga_521", "step", 42.6580299999518,
//				0.68653999857266, 0, 10, 521, t, null);
//		Step s522 = new Step("step_artiga_522", "step", 42.6581499999517,
//				0.686549998572699, 0, 10, 522, t, null);
//		Step s523 = new Step("step_artiga_523", "step", 42.6582299999518,
//				0.686509998572522, 0, 10, 523, t, null);
//		Step s524 = new Step("step_artiga_524", "step", 42.6582899999517,
//				0.686389998572003, 0, 10, 524, t, null);
//		Step s525 = new Step("step_artiga_525", "step", 42.6583499999516,
//				0.686339998571785, 0, 10, 525, t, null);
//		Step s526 = new Step("step_artiga_526", "step", 42.6584099999517,
//				0.686289998571569, 0, 10, 526, t, null);
//		Step s527 = new Step("step_artiga_527", "step", 42.6585699999516,
//				0.686079998570656, 0, 10, 527, t, null);
//		Step s528 = new Step("step_artiga_528", "step", 42.6586099999516,
//				0.686029998570438, 0, 10, 528, t, null);
//		Step s529 = new Step("step_artiga_529", "step", 42.6586599999516,
//				0.686039998570479, 0, 10, 529, t, null);
//		Step s530 = new Step("step_artiga_530", "step", 42.6587099999516,
//				0.686079998570651, 0, 10, 530, t, null);
//		Step s531 = new Step("step_artiga_531", "step", 42.6587699999516,
//				0.686069998570606, 0, 10, 531, t, null);
//		Step s532 = new Step("step_artiga_532", "step", 42.6591299999516,
//				0.68580999856947, 0, 10, 532, t, null);
//		Step s533 = new Step("step_artiga_533", "step", 42.6591799999516,
//				0.685769998569295, 0, 10, 533, t, null);
//		Step s534 = new Step("step_artiga_534", "step", 42.6593399999516,
//				0.6856799985689, 0, 10, 534, t, null);
//		Step s535 = new Step("step_artiga_535", "step", 42.6594999999516,
//				0.685719998569068, 0, 10, 535, t, null);
//		Step s536 = new Step("step_artiga_536", "step", 42.6595799999516,
//				0.685779998569324, 0, 10, 536, t, null);
//		Step s537 = new Step("step_artiga_537", "step", 42.6596499999516,
//				0.685789998569365, 0, 10, 537, t, null);
//		Step s538 = new Step("step_artiga_538", "step", 42.6597099999516,
//				0.685769998569275, 0, 10, 538, t, null);
//		Step s539 = new Step("step_artiga_539", "step", 42.6597599999516,
//				0.685749998569187, 0, 10, 539, t, null);
//		Step s540 = new Step("step_artiga_540", "step", 42.6598099999516,
//				0.685709998569013, 0, 10, 540, t, null);
//		Step s541 = new Step("step_artiga_541", "step", 42.6599299999516,
//				0.685659998568792, 0, 10, 541, t, null);
//		Step s542 = new Step("step_artiga_542", "step", 42.6599899999516,
//				0.685669998568834, 0, 10, 542, t, null);
//		Step s543 = new Step("step_artiga_543", "step", 42.6600399999516,
//				0.685719998569049, 0, 10, 543, t, null);
//		Step s544 = new Step("step_artiga_544", "step", 42.6602499999516,
//				0.68593999856999, 0, 10, 544, t, null);
//		Step s545 = new Step("step_artiga_545", "step", 42.6603099999516,
//				0.68597999857016, 0, 10, 545, t, null);
//		Step s546 = new Step("step_artiga_546", "step", 42.6603699999516,
//				0.685989998570199, 0, 10, 546, t, null);
//		Step s547 = new Step("step_artiga_547", "step", 42.6604399999516,
//				0.686059998570499, 0, 10, 547, t, null);
//		Step s548 = new Step("step_artiga_548", "step", 42.6604899999516,
//				0.68609999857067, 0, 10, 548, t, null);
//		Step s549 = new Step("step_artiga_549", "step", 42.6605499999516,
//				0.686139998570842, 0, 10, 549, t, null);
//		Step s550 = new Step("step_artiga_550", "step", 42.6605899999516,
//				0.686189998571057, 0, 10, 550, t, null);
//		Step s551 = new Step("step_artiga_551", "step", 42.6606399999516,
//				0.68623999857127, 0, 10, 551, t, null);
//		Step s552 = new Step("step_artiga_552", "step", 42.6606899999517,
//				0.686309998571569, 0, 10, 552, t, null);
//		Step s553 = new Step("step_artiga_553", "step", 42.6607199999516,
//				0.686369998571827, 0, 10, 553, t, null);
//		Step s554 = new Step("step_artiga_554", "step", 42.6607399999516,
//				0.686429998572086, 0, 10, 554, t, null);
//		Step s555 = new Step("step_artiga_555", "step", 42.6607899999518,
//				0.686479998572298, 0, 10, 555, t, null);
//		Step s556 = new Step("step_artiga_556", "step", 42.6608199999518,
//				0.686549998572599, 0, 10, 556, t, null);
//		Step s557 = new Step("step_artiga_557", "step", 42.6608499999518,
//				0.686679998573158, 0, 10, 557, t, null);
//		Step s558 = new Step("step_artiga_558", "step", 42.6608699999518,
//				0.686749998573458, 0, 10, 558, t, null);
//		Step s559 = new Step("step_artiga_559", "step", 42.6609099999518,
//				0.686799998573673, 0, 10, 559, t, null);
//		Step s560 = new Step("step_artiga_560", "step", 42.6609899999518,
//				0.686919998574187, 0, 10, 560, t, null);
//		Step s561 = new Step("step_artiga_561", "step", 42.6610599999518,
//				0.686989998574485, 0, 10, 561, t, null);
//		Step s562 = new Step("step_artiga_562", "step", 42.6610899999518,
//				0.687049998574741, 0, 10, 562, t, null);
//		Step s563 = new Step("step_artiga_563", "step", 42.6611399999518,
//				0.687109998574998, 0, 10, 563, t, null);
//		Step s564 = new Step("step_artiga_564", "step", 42.6612499999518,
//				0.687229998575509, 0, 10, 564, t, null);
//		Step s565 = new Step("step_artiga_565", "step", 42.6612899999518,
//				0.687299998575809, 0, 10, 565, t, null);
//		Step s566 = new Step("step_artiga_566", "step", 42.6613199999518,
//				0.687419998576324, 0, 10, 566, t, null);
//		Step s567 = new Step("step_artiga_567", "step", 42.6613099999518,
//				0.687489998576628, 0, 10, 567, t, null);
//		Step s568 = new Step("step_artiga_568", "step", 42.6613099999518,
//				0.687689998577485, 0, 10, 568, t, h6);
//		Step s569 = new Step("step_artiga_569", "step", 42.6613799999518,
//				0.687779998577871, 0, 10, 569, t, null);
//		Step s570 = new Step("step_artiga_570", "step", 42.6614499999518,
//				0.687829998578082, 0, 10, 570, t, null);
//		Step s571 = new Step("step_artiga_571", "step", 42.6615299999518,
//				0.687879998578293, 0, 10, 571, t, null);
//		Step s572 = new Step("step_artiga_572", "step", 42.6616699999518,
//				0.687949998578589, 0, 10, 572, t, null);
//		Step s573 = new Step("step_artiga_573", "step", 42.6616899999518,
//				0.688029998578931, 0, 10, 573, t, null);
//		Step s574 = new Step("step_artiga_574", "step", 42.6616899999519,
//				0.688119998579318, 0, 10, 574, t, null);
//		Step s575 = new Step("step_artiga_575", "step", 42.6617199999519,
//				0.688219998579748, 0, 10, 575, t, null);
//		Step s576 = new Step("step_artiga_576", "step", 42.661759999952,
//				0.688299998580088, 0, 10, 576, t, null);
//		Step s577 = new Step("step_artiga_577", "step", 42.661809999952,
//				0.6883499985803, 0, 10, 577, t, null);
//		Step s578 = new Step("step_artiga_578", "step", 42.661869999952,
//				0.6883499985803, 0, 10, 578, t, null);
//		Step s579 = new Step("step_artiga_579", "step", 42.661929999952,
//				0.688359998580339, 0, 10, 579, t, null);
//		Step s580 = new Step("step_artiga_580", "step", 42.661979999952,
//				0.688329998580208, 0, 10, 580, t, null);
//		Step s581 = new Step("step_artiga_581", "step", 42.662019999952,
//				0.688379998580423, 0, 10, 581, t, null);
//		Step s582 = new Step("step_artiga_582", "step", 42.662039999952,
//				0.68843999858068, 0, 10, 582, t, null);
//		Step s583 = new Step("step_artiga_583", "step", 42.662079999952,
//				0.688569998581234, 0, 10, 583, t, null);
//		Step s584 = new Step("step_artiga_584", "step", 42.662119999952,
//				0.688599998581362, 0, 10, 584, t, null);
//		Step s585 = new Step("step_artiga_585", "step", 42.662209999952,
//				0.688609998581401, 0, 10, 585, t, null);
//		Step s586 = new Step("step_artiga_586", "step", 42.662269999952,
//				0.688709998581827, 0, 10, 586, t, null);
//		Step s587 = new Step("step_artiga_587", "step", 42.662319999952,
//				0.688759998582041, 0, 10, 587, t, null);
//		Step s588 = new Step("step_artiga_588", "step", 42.662439999952,
//				0.688939998582806, 0, 10, 588, t, null);
//		Step s589 = new Step("step_artiga_589", "step", 42.662519999952,
//				0.688989998583017, 0, 10, 589, t, null);
//		Step s590 = new Step("step_artiga_590", "step", 42.662579999952,
//				0.688989998583015, 0, 10, 590, t, null);
//		Step s591 = new Step("step_artiga_591", "step", 42.662639999952,
//				0.688999998583056, 0, 10, 591, t, null);
//		Step s592 = new Step("step_artiga_592", "step", 42.662689999952,
//				0.688999998583054, 0, 10, 592, t, null);
//		Step s593 = new Step("step_artiga_593", "step", 42.662689999952,
//				0.689079998583397, 0, 10, 593, t, null);
//		Step s594 = new Step("step_artiga_594", "step", 42.662689999952,
//				0.68915999858374, 0, 10, 594, t, null);
//		Step s595 = new Step("step_artiga_595", "step", 42.662759999952,
//				0.68923999858408, 0, 10, 595, t, null);
//		Step s596 = new Step("step_artiga_596", "step", 42.662809999952,
//				0.689269998584207, 0, 10, 596, t, null);
//		Step s597 = new Step("step_artiga_597", "step", 42.662859999952,
//				0.689289998584289, 0, 10, 597, t, null);
//		Step s598 = new Step("step_artiga_598", "step", 42.662879999952,
//				0.689379998584675, 0, 10, 598, t, null);
//		Step s599 = new Step("step_artiga_599", "step", 42.662929999952,
//				0.689489998585142, 0, 10, 599, t, null);
//		Step s600 = new Step("step_artiga_600", "step", 42.662959999952,
//				0.689549998585399, 0, 10, 600, t, null);
//		Step s601 = new Step("step_artiga_601", "step", 42.662959999952,
//				0.689639998585783, 0, 10, 601, t, null);
//		Step s602 = new Step("step_artiga_602", "step", 42.6629899999522,
//				0.689819998586551, 0, 10, 602, t, null);
//		Step s603 = new Step("step_artiga_603", "step", 42.6630399999522,
//				0.689879998586804, 0, 10, 603, t, null);
//		Step s604 = new Step("step_artiga_604", "step", 42.6631699999522,
//				0.68998999858727, 0, 10, 604, t, null);
//		Step s605 = new Step("step_artiga_605", "step", 42.6632399999522,
//				0.690019998587397, 0, 10, 605, t, null);
//		Step s606 = new Step("step_artiga_606", "step", 42.6632999999522,
//				0.690059998587565, 0, 10, 606, t, null);
//		Step s607 = new Step("step_artiga_607", "step", 42.6633699999522,
//				0.69015999858799, 0, 10, 607, t, null);
//		Step s608 = new Step("step_artiga_608", "step", 42.6634099999522,
//				0.690229998588285, 0, 10, 608, t, null);
//		Step s609 = new Step("step_artiga_609", "step", 42.6635199999522,
//				0.690339998588752, 0, 10, 609, t, null);
//		Step s610 = new Step("step_artiga_610", "step", 42.6636399999522,
//				0.690499998589431, 0, 10, 610, t, null);
//		Step s611 = new Step("step_artiga_611", "step", 42.6637199999522,
//				0.690589998589812, 0, 10, 611, t, null);
//		Step s612 = new Step("step_artiga_612", "step", 42.6638099999522,
//				0.690619998589935, 0, 10, 612, t, null);
//		Step s613 = new Step("step_artiga_613", "step", 42.6638499999522,
//				0.690659998590107, 0, 10, 613, t, null);
//		Step s614 = new Step("step_artiga_614", "step", 42.6639099999522,
//				0.690719998590359, 0, 10, 614, t, null);
//		Step s615 = new Step("step_artiga_615", "step", 42.6639599999522,
//				0.690749998590484, 0, 10, 615, t, null);
//		Step s616 = new Step("step_artiga_616", "step", 42.6640199999522,
//				0.690799998590696, 0, 10, 616, t, null);
//		Step s617 = new Step("step_artiga_617", "step", 42.6640999999522,
//				0.690869998590991, 0, 10, 617, t, null);
//		Step s618 = new Step("step_artiga_618", "step", 42.6642799999522,
//				0.690989998591496, 0, 10, 618, t, null);
//		Step s619 = new Step("step_artiga_619", "step", 42.6643199999522,
//				0.691049998591752, 0, 10, 619, t, null);
//		Step s620 = new Step("step_artiga_620", "step", 42.6643399999522,
//				0.691189998592346, 0, 10, 620, t, null);
//		Step s621 = new Step("step_artiga_621", "step", 42.6644099999524,
//				0.691309998592853, 0, 10, 621, t, null);
//		Step s622 = new Step("step_artiga_622", "step", 42.6646199999522,
//				0.691429998593358, 0, 10, 622, t, null);
//		Step s623 = new Step("step_artiga_623", "step", 42.6646699999523,
//				0.6913699985931, 0, 10, 623, t, null);
//		Step s624 = new Step("step_artiga_624", "step", 42.6647099999522,
//				0.69133999859297, 0, 10, 624, t, null);
//		Step s625 = new Step("step_artiga_625", "step", 42.6647699999522,
//				0.691359998593052, 0, 10, 625, t, null);
//		Step s626 = new Step("step_artiga_626", "step", 42.6648299999522,
//				0.691379998593136, 0, 10, 626, t, null);
//		Step s627 = new Step("step_artiga_627", "step", 42.6648899999523,
//				0.691369998593092, 0, 10, 627, t, null);
//		Step s628 = new Step("step_artiga_628", "step", 42.6649399999522,
//				0.691379998593133, 0, 10, 628, t, null);
//		Step s629 = new Step("step_artiga_629", "step", 42.6649799999522,
//				0.691319998592876, 0, 10, 629, t, null);
//		Step s630 = new Step("step_artiga_630", "step", 42.6650199999522,
//				0.691209998592405, 0, 10, 630, t, null);
//		Step s631 = new Step("step_artiga_631", "step", 42.6651199999522,
//				0.691259998592615, 0, 10, 631, t, null);
//		Step s632 = new Step("step_artiga_632", "step", 42.6652199999522,
//				0.691319998592864, 0, 10, 632, t, null);
//		Step s633 = new Step("step_artiga_633", "step", 42.6652599999522,
//				0.691289998592739, 0, 10, 633, t, null);
//		Step s634 = new Step("step_artiga_634", "step", 42.6652999999522,
//				0.691349998592992, 0, 10, 634, t, null);
//		Step s635 = new Step("step_artiga_635", "step", 42.6653599999523,
//				0.69142999859333, 0, 10, 635, t, null);
//		Step s636 = new Step("step_artiga_636", "step", 42.6653899999523,
//				0.691489998593584, 0, 10, 636, t, null);
//		Step s637 = new Step("step_artiga_637", "step", 42.6654399999523,
//				0.69151999859371, 0, 10, 637, t, null);
//		Step s638 = new Step("step_artiga_638", "step", 42.6654899999522,
//				0.691479998593537, 0, 10, 638, t, null);
//		Step s639 = new Step("step_artiga_639", "step", 42.6655299999522,
//				0.691439998593366, 0, 10, 639, t, null);
//		Step s640 = new Step("step_artiga_640", "step", 42.6655599999522,
//				0.691369998593067, 0, 10, 640, t, null);
//		Step s641 = new Step("step_artiga_641", "step", 42.6655999999522,
//				0.691339998592938, 0, 10, 641, t, null);
//		Step s642 = new Step("step_artiga_642", "step", 42.6656799999522,
//				0.691269998592637, 0, 10, 642, t, null);
//		Step s643 = new Step("step_artiga_643", "step", 42.6657099999522,
//				0.691209998592379, 0, 10, 643, t, null);
//		Step s644 = new Step("step_artiga_644", "step", 42.6657699999522,
//				0.691109998591953, 0, 10, 644, t, null);
//		Step s645 = new Step("step_artiga_645", "step", 42.6657899999522,
//				0.691039998591654, 0, 10, 645, t, null);
//		Step s646 = new Step("step_artiga_646", "step", 42.6658499999522,
//				0.691019998591565, 0, 10, 646, t, null);
//		Step s647 = new Step("step_artiga_647", "step", 42.6658899999522,
//				0.691059998591736, 0, 10, 647, t, null);
//		Step s648 = new Step("step_artiga_648", "step", 42.6659199999522,
//				0.691119998591989, 0, 10, 648, t, null);
//		Step s649 = new Step("step_artiga_649", "step", 42.6659699999522,
//				0.691169998592202, 0, 10, 649, t, null);
//		Step s650 = new Step("step_artiga_650", "step", 42.6659899999522,
//				0.691249998592539, 0, 10, 650, t, null);
//		Step s651 = new Step("step_artiga_651", "step", 42.6660199999523,
//				0.691349998592964, 0, 10, 651, t, null);
//		Step s652 = new Step("step_artiga_652", "step", 42.6660699999523,
//				0.691399998593176, 0, 10, 652, t, null);
//		Step s653 = new Step("step_artiga_653", "step", 42.6660899999523,
//				0.691469998593472, 0, 10, 653, t, null);
//		Step s654 = new Step("step_artiga_654", "step", 42.6661199999523,
//				0.691549998593812, 0, 10, 654, t, null);
//		Step s655 = new Step("step_artiga_655", "step", 42.6661299999524,
//				0.691619998594111, 0, 10, 655, t, null);
//		Step s656 = new Step("step_artiga_656", "step", 42.6661399999524,
//				0.691689998594407, 0, 10, 656, t, null);
//		Step s657 = new Step("step_artiga_657", "step", 42.6661699999524,
//				0.691909998595341, 0, 10, 657, t, null);
//		Step s658 = new Step("step_artiga_658", "step", 42.6661599999524,
//				0.691999998595725, 0, 10, 658, t, null);
//		Step s659 = new Step("step_artiga_659", "step", 42.6661599999524,
//				0.692079998596065, 0, 10, 659, t, null);
//		Step s660 = new Step("step_artiga_660", "step", 42.6661299999524,
//				0.692159998596405, 0, 10, 660, t, null);
//		Step s661 = new Step("step_artiga_661", "step", 42.6660999999524,
//				0.692229998596704, 0, 10, 661, t, null);
//		Step s662 = new Step("step_artiga_662", "step", 42.6660999999524,
//				0.692349998597214, 0, 10, 662, t, null);
//		Step s663 = new Step("step_artiga_663", "step", 42.6661099999524,
//				0.692409998597468, 0, 10, 663, t, null);
//		Step s664 = new Step("step_artiga_664", "step", 42.6661099999524,
//				0.692489998597808, 0, 10, 664, t, null);
//		Step s665 = new Step("step_artiga_665", "step", 42.6661099999524,
//				0.69261999859836, 0, 10, 665, t, null);
//		Step s666 = new Step("step_artiga_666", "step", 42.6661099999524,
//				0.6926999985987, 0, 10, 666, t, null);
//		Step s667 = new Step("step_artiga_667", "step", 42.6661599999524,
//				0.69274999859891, 0, 10, 667, t, null);
//		Step s668 = new Step("step_artiga_668", "step", 42.6661699999524,
//				0.692819998599205, 0, 10, 668, t, null);
//		Step s669 = new Step("step_artiga_669", "step", 42.6661899999524,
//				0.692899998599544, 0, 10, 669, t, null);
//		Step s670 = new Step("step_artiga_670", "step", 42.6661999999524,
//				0.693009998600009, 0, 10, 670, t, null);
//		Step s671 = new Step("step_artiga_671", "step", 42.6662499999524,
//				0.692999998599966, 0, 10, 671, t, null);
//		Step s672 = new Step("step_artiga_672", "step", 42.6662999999524,
//				0.693039998600133, 0, 10, 672, t, null);
//		Step s673 = new Step("step_artiga_673", "step", 42.6663099999524,
//				0.693119998600471, 0, 10, 673, t, null);
//		Step s674 = new Step("step_artiga_674", "step", 42.6662599999525,
//				0.693219998600898, 0, 10, 674, t, null);
//		Step s675 = new Step("step_artiga_675", "step", 42.6661999999525,
//				0.693319998601324, 0, 10, 675, t, null);
//		Step s676 = new Step("step_artiga_676", "step", 42.6661499999526,
//				0.693409998601706, 0, 10, 676, t, null);
//		Step s677 = new Step("step_artiga_677", "step", 42.6662099999526,
//				0.693439998601829, 0, 10, 677, t, null);
//		Step s678 = new Step("step_artiga_678", "step", 42.6663199999526,
//				0.693499998602082, 0, 10, 678, t, null);
//		Step s679 = new Step("step_artiga_679", "step", 42.6663599999526,
//				0.693539998602249, 0, 10, 679, t, null);
//		Step s680 = new Step("step_artiga_680", "step", 42.6663999999526,
//				0.693539998602248, 0, 10, 680, t, null);
//		Step s681 = new Step("step_artiga_681", "step", 42.6663899999526,
//				0.693609998602544, 0, 10, 681, t, null);
//		Step s682 = new Step("step_artiga_682", "step", 42.6664099999526,
//				0.693619998602586, 0, 10, 682, t, null);
//		Step s683 = new Step("step_artiga_683", "step", 42.6664799999526,
//				0.693679998602836, 0, 10, 683, t, null);
//		Step s684 = new Step("step_artiga_684", "step", 42.6665399999526,
//				0.693709998602961, 0, 10, 684, t, null);
//		Step s685 = new Step("step_artiga_685", "step", 42.6665999999526,
//				0.693719998603001, 0, 10, 685, t, null);
//		Step s686 = new Step("step_artiga_686", "step", 42.6666499999526,
//				0.693829998603465, 0, 10, 686, t, null);
//		Step s687 = new Step("step_artiga_687", "step", 42.6666999999526,
//				0.693919998603844, 0, 10, 687, t, null);
//		Step s688 = new Step("step_artiga_688", "step", 42.6666799999526,
//				0.693979998604098, 0, 10, 688, t, null);
//		Step s689 = new Step("step_artiga_689", "step", 42.6667299999526,
//				0.694029998604306, 0, 10, 689, t, null);
//		Step s690 = new Step("step_artiga_690", "step", 42.6667699999526,
//				0.694079998604518, 0, 10, 690, t, null);
//		Step s691 = new Step("step_artiga_691", "step", 42.6667599999526,
//				0.694139998604772, 0, 10, 691, t, null);
//		Step s692 = new Step("step_artiga_692", "step", 42.6667299999526,
//				0.694199998605025, 0, 10, 692, t, null);
//		Step s693 = new Step("step_artiga_693", "step", 42.6667299999526,
//				0.694319998605533, 0, 10, 693, t, null);
//		Step s694 = new Step("step_artiga_694", "step", 42.6666899999526,
//				0.694349998605661, 0, 10, 694, t, null);
//		Step s695 = new Step("step_artiga_695", "step", 42.6666099999526,
//				0.694349998605663, 0, 10, 695, t, null);
//		Step s696 = new Step("step_artiga_696", "step", 42.6665899999526,
//				0.694409998605918, 0, 10, 696, t, null);
//		Step s697 = new Step("step_artiga_697", "step", 42.6665699999526,
//				0.694479998606214, 0, 10, 697, t, null);
//		Step s698 = new Step("step_artiga_698", "step", 42.6665699999526,
//				0.694569998606595, 0, 10, 698, t, null);
//		Step s699 = new Step("step_artiga_699", "step", 42.6665899999526,
//				0.694679998607058, 0, 10, 699, t, null);
//		Step s700 = new Step("step_artiga_700", "step", 42.6666099999526,
//				0.69472999860727, 0, 10, 700, t, null);
//		Step s701 = new Step("step_artiga_701", "step", 42.6666199999526,
//				0.694809998607606, 0, 10, 701, t, null);
//		Step s702 = new Step("step_artiga_702", "step", 42.6666299999528,
//				0.694889998607943, 0, 10, 702, t, null);
//		Step s703 = new Step("step_artiga_703", "step", 42.6666699999527,
//				0.69493999860815, 0, 10, 703, t, null);
//		Step s704 = new Step("step_artiga_704", "step", 42.6667199999527,
//				0.694939998608152, 0, 10, 704, t, null);
//		Step s705 = new Step("step_artiga_705", "step", 42.6667799999528,
//				0.695019998608486, 0, 10, 705, t, null);
//		Step s706 = new Step("step_artiga_706", "step", 42.6668199999528,
//				0.695009998608442, 0, 10, 706, t, null);
//		Step s707 = new Step("step_artiga_707", "step", 42.6668599999528,
//				0.695089998608778, 0, 10, 707, t, null);
//		Step s708 = new Step("step_artiga_708", "step", 42.6668799999528,
//				0.695129998608945, 0, 10, 708, t, null);
//		Step s709 = new Step("step_artiga_709", "step", 42.6669299999528,
//				0.69515999860907, 0, 10, 709, t, null);
//		Step s710 = new Step("step_artiga_710", "step", 42.6669399999528,
//				0.695279998609576, 0, 10, 710, t, null);
//		Step s711 = new Step("step_artiga_711", "step", 42.6669799999528,
//				0.695229998609364, 0, 10, 711, t, null);
//		Step s712 = new Step("step_artiga_712", "step", 42.6670199999528,
//				0.695249998609447, 0, 10, 712, t, null);
//		Step s713 = new Step("step_artiga_713", "step", 42.6670399999528,
//				0.695279998609572, 0, 10, 713, t, null);
//		Step s714 = new Step("step_artiga_714", "step", 42.6670499999528,
//				0.695399998610078, 0, 10, 714, t, null);
//		Step s715 = new Step("step_artiga_715", "step", 42.6670999999528,
//				0.695419998610159, 0, 10, 715, t, null);
//		Step s716 = new Step("step_artiga_716", "step", 42.6671499999528,
//				0.695459998610327, 0, 10, 716, t, null);
//		Step s717 = new Step("step_artiga_717", "step", 42.6671899999528,
//				0.695509998610536, 0, 10, 717, t, null);
//		Step s718 = new Step("step_artiga_718", "step", 42.6672299999528,
//				0.695569998610787, 0, 10, 718, t, null);
//		Step s719 = new Step("step_artiga_719", "step", 42.6672899999528,
//				0.69559999861091, 0, 10, 719, t, null);
//		Step s720 = new Step("step_artiga_720", "step", 42.6673999999528,
//				0.695569998610781, 0, 10, 720, t, null);
//		Step s721 = new Step("step_artiga_721", "step", 42.6674299999528,
//				0.695629998611032, 0, 10, 721, t, null);
//		Step s722 = new Step("step_artiga_722", "step", 42.6674799999528,
//				0.695689998611283, 0, 10, 722, t, null);
//		Step s723 = new Step("step_artiga_723", "step", 42.6674799999528,
//				0.69576999861162, 0, 10, 723, t, null);
//		Step s724 = new Step("step_artiga_724", "step", 42.6674799999528,
//				0.695839998611915, 0, 10, 724, t, null);
//		Step s725 = new Step("step_artiga_725", "step", 42.6674699999528,
//				0.695909998612207, 0, 10, 725, t, null);
//		Step s726 = new Step("step_artiga_726", "step", 42.6674799999528,
//				0.695989998612546, 0, 10, 726, t, null);
//		Step s727 = new Step("step_artiga_727", "step", 42.6675599999528,
//				0.696139998613175, 0, 10, 727, t, null);
//		Step s728 = new Step("step_artiga_728", "step", 42.6675899999528,
//				0.696239998613593, 0, 10, 728, t, null);
//		Step s729 = new Step("step_artiga_729", "step", 42.6676399999528,
//				0.696299998613843, 0, 10, 729, t, null);
//		Step s730 = new Step("step_artiga_730", "step", 42.6676599999528,
//				0.696299998613842, 0, 10, 730, t, null);
//		Step s731 = new Step("step_artiga_731", "step", 42.6676999999528,
//				0.696369998614134, 0, 10, 731, t, null);
//		Step s732 = new Step("step_artiga_732", "step", 42.6677399999528,
//				0.696429998614385, 0, 10, 732, t, null);
//		Step s733 = new Step("step_artiga_733", "step", 42.6677499999528,
//				0.696399998614257, 0, 10, 733, t, null);
//		Step s734 = new Step("step_artiga_734", "step", 42.6677399999528,
//				0.696329998613965, 0, 10, 734, t, null);
//		Step s735 = new Step("step_artiga_735", "step", 42.6677399999528,
//				0.696299998613839, 0, 10, 735, t, null);
//		Step s736 = new Step("step_artiga_736", "step", 42.6677699999528,
//				0.69635999861409, 0, 10, 736, t, null);
//		Step s737 = new Step("step_artiga_737", "step", 42.6678099999529,
//				0.696399998614255, 0, 10, 737, t, null);
//		Step s738 = new Step("step_artiga_738", "step", 42.6678399999529,
//				0.696469998614548, 0, 10, 738, t, null);
//		Step s739 = new Step("step_artiga_739", "step", 42.6678899999528,
//				0.696419998614338, 0, 10, 739, t, null);
//		Step s740 = new Step("step_artiga_740", "step", 42.6679199999529,
//				0.696379998614168, 0, 10, 740, t, null);
//		Step s741 = new Step("step_artiga_741", "step", 42.6679799999528,
//				0.696349998614039, 0, 10, 741, t, null);
//		Step s742 = new Step("step_artiga_742", "step", 42.6680699999528,
//				0.696349998614037, 0, 10, 742, t, null);
//		Step s743 = new Step("step_artiga_743", "step", 42.6681299999528,
//				0.696349998614034, 0, 10, 743, t, null);
//		Step s744 = new Step("step_artiga_744", "step", 42.6681699999528,
//				0.696379998614159, 0, 10, 744, t, null);
//		Step s745 = new Step("step_artiga_745", "step", 42.6682199999528,
//				0.696419998614325, 0, 10, 745, t, null);
//		Step s746 = new Step("step_artiga_746", "step", 42.6682799999528,
//				0.696419998614324, 0, 10, 746, t, null);
//		Step s747 = new Step("step_artiga_747", "step", 42.6683299999528,
//				0.696369998614112, 0, 10, 747, t, null);
//		Step s748 = new Step("step_artiga_748", "step", 42.6683999999528,
//				0.696379998614151, 0, 10, 748, t, null);
//		Step s749 = new Step("step_artiga_749", "step", 42.6685599999529,
//				0.696379998614144, 0, 10, 749, t, null);
//		Step s750 = new Step("step_artiga_750", "step", 42.6685999999528,
//				0.696399998614228, 0, 10, 750, t, null);
//		Step s751 = new Step("step_artiga_751", "step", 42.6686499999528,
//				0.696429998614354, 0, 10, 751, t, null);
//		Step s752 = new Step("step_artiga_752", "step", 42.6687299999528,
//				0.696479998614561, 0, 10, 752, t, null);
//		Step s753 = new Step("step_artiga_753", "step", 42.6687899999529,
//				0.696509998614684, 0, 10, 753, t, null);
//		Step s754 = new Step("step_artiga_754", "step", 42.6688699999528,
//				0.696479998614555, 0, 10, 754, t, null);
//		Step s755 = new Step("step_artiga_755", "step", 42.6689899999528,
//				0.696479998614551, 0, 10, 755, t, null);
//		Step s756 = new Step("step_artiga_756", "step", 42.6690499999528,
//				0.696539998614802, 0, 10, 756, t, null);
//		Step s757 = new Step("step_artiga_757", "step", 42.6690899999528,
//				0.696549998614841, 0, 10, 757, t, null);
//		Step s758 = new Step("step_artiga_758", "step", 42.6691399999528,
//				0.69649999861463, 0, 10, 758, t, null);
//		Step s759 = new Step("step_artiga_759", "step", 42.669219999953,
//				0.696529998614751, 0, 10, 759, t, null);
//		Step s760 = new Step("step_artiga_760", "step", 42.669279999953,
//				0.696549998614834, 0, 10, 760, t, null);
//		Step s761 = new Step("step_artiga_761", "step", 42.6693299999528,
//				0.696499998614621, 0, 10, 761, t, null);
//		Step s762 = new Step("step_artiga_762", "step", 42.6693799999528,
//				0.696499998614619, 0, 10, 762, t, null);
//		Step s763 = new Step("step_artiga_763", "step", 42.6694999999528,
//				0.696509998614658, 0, 10, 763, t, null);
//		Step s764 = new Step("step_artiga_764", "step", 42.6695399999528,
//				0.69647999861453, 0, 10, 764, t, null);
//		Step s765 = new Step("step_artiga_765", "step", 42.6695899999529,
//				0.69648999861457, 0, 10, 765, t, null);
//		Step s766 = new Step("step_artiga_766", "step", 42.669659999953,
//				0.696569998614903, 0, 10, 766, t, null);
//		Step s767 = new Step("step_artiga_767", "step", 42.6697199999529,
//				0.696669998615323, 0, 10, 767, t, null);
//		Step s768 = new Step("step_artiga_768", "step", 42.669769999953,
//				0.696739998615614, 0, 10, 768, t, null);
//		Step s769 = new Step("step_artiga_769", "step", 42.669819999953,
//				0.69677999861578, 0, 10, 769, t, null);
//		Step s770 = new Step("step_artiga_770", "step", 42.669869999953,
//				0.696789998615819, 0, 10, 770, t, null);
//		Step s771 = new Step("step_artiga_771", "step", 42.669929999953,
//				0.696789998615818, 0, 10, 771, t, null);
//		Step s772 = new Step("step_artiga_772", "step", 42.669969999953,
//				0.696739998615607, 0, 10, 772, t, null);
//		Step s773 = new Step("step_artiga_773", "step", 42.670019999953,
//				0.696759998615689, 0, 10, 773, t, null);
//		Step s774 = new Step("step_artiga_774", "step", 42.670109999953,
//				0.696779998615769, 0, 10, 774, t, null);
//		Step s775 = new Step("step_artiga_775", "step", 42.670179999953,
//				0.696789998615808, 0, 10, 775, t, null);
//		Step s776 = new Step("step_artiga_776", "step", 42.670229999953,
//				0.696809998615892, 0, 10, 776, t, null);
//		Step s777 = new Step("step_artiga_777", "step", 42.670279999953,
//				0.696859998616099, 0, 10, 777, t, null);
//		Step s778 = new Step("step_artiga_778", "step", 42.670319999953,
//				0.696939998616433, 0, 10, 778, t, null);
//		Step s779 = new Step("step_artiga_779", "step", 42.670329999953,
//				0.69702999861681, 0, 10, 779, t, null);
//		Step s780 = new Step("step_artiga_780", "step", 42.670369999953,
//				0.697109998617145, 0, 10, 780, t, null);
//		Step s781 = new Step("step_artiga_781", "step", 42.670429999953,
//				0.697159998617351, 0, 10, 781, t, null);
//		Step s782 = new Step("step_artiga_782", "step", 42.670469999953,
//				0.697219998617602, 0, 10, 782, t, null);
//		Step s783 = new Step("step_artiga_783", "step", 42.670489999953,
//				0.697359998618188, 0, 10, 783, t, null);
//		Step s784 = new Step("step_artiga_784", "step", 42.670529999953,
//				0.697509998618816, 0, 10, 784, t, null);
//		Step s785 = new Step("step_artiga_785", "step", 42.670569999953,
//				0.697559998619023, 0, 10, 785, t, null);
//		Step s786 = new Step("step_artiga_786", "step", 42.670619999953,
//				0.697619998619274, 0, 10, 786, t, null);
//		Step s787 = new Step("step_artiga_787", "step", 42.670669999953,
//				0.697639998619355, 0, 10, 787, t, null);
//		Step s788 = new Step("step_artiga_788", "step", 42.670719999953,
//				0.697699998619603, 0, 10, 788, t, null);
//		Step s789 = new Step("step_artiga_789", "step", 42.670769999953,
//				0.697799998620022, 0, 10, 789, t, null);
//		Step s790 = new Step("step_artiga_790", "step", 42.670859999953,
//				0.697929998620564, 0, 10, 790, t, null);
//		Step s791 = new Step("step_artiga_791", "step", 42.670929999953,
//				0.698009998620894, 0, 10, 791, t, null);
//		Step s792 = new Step("step_artiga_792", "step", 42.6709799999531,
//				0.698059998621101, 0, 10, 792, t, null);
//		Step s793 = new Step("step_artiga_793", "step", 42.671019999953,
//				0.69810999862131, 0, 10, 793, t, null);
//		Step s794 = new Step("step_artiga_794", "step", 42.671049999953,
//				0.698179998621601, 0, 10, 794, t, null);
//		Step s795 = new Step("step_artiga_795", "step", 42.671099999953,
//				0.69822999862181, 0, 10, 795, t, null);
//		Step s796 = new Step("step_artiga_796", "step", 42.671159999953,
//				0.698239998621848, 0, 10, 796, t, null);
//		Step s797 = new Step("step_artiga_797", "step", 42.6712099999531,
//				0.698219998621762, 0, 10, 797, t, null);
//		Step s798 = new Step("step_artiga_798", "step", 42.6712799999532,
//				0.698279998622012, 0, 10, 798, t, null);
//		Step s799 = new Step("step_artiga_799", "step", 42.6713499999532,
//				0.698459998622762, 0, 10, 799, t, null);
//		Step s800 = new Step("step_artiga_800", "step", 42.6713599999532,
//				0.698539998623096, 0, 10, 800, t, null);
//		Step s801 = new Step("step_artiga_801", "step", 42.6714099999532,
//				0.698639998623513, 0, 10, 801, t, null);
//		Step s802 = new Step("step_artiga_802", "step", 42.6714699999532,
//				0.698659998623593, 0, 10, 802, t, null);
//		Step s803 = new Step("step_artiga_803", "step", 42.6715599999532,
//				0.698739998623924, 0, 10, 803, t, null);
//		Step s804 = new Step("step_artiga_804", "step", 42.6715999999532,
//				0.698809998624214, 0, 10, 804, t, null);
//		Step s805 = new Step("step_artiga_805", "step", 42.6716299999532,
//				0.698919998624671, 0, 10, 805, t, null);
//		Step s806 = new Step("step_artiga_806", "step", 42.6717299999532,
//				0.698989998624961, 0, 10, 806, t, null);
//		Step s807 = new Step("step_artiga_807", "step", 42.6717799999532,
//				0.699029998625127, 0, 10, 807, t, null);
//		Step s808 = new Step("step_artiga_808", "step", 42.6718399999532,
//				0.699069998625293, 0, 10, 808, t, null);
//		Step s809 = new Step("step_artiga_809", "step", 42.6718899999532,
//				0.699119998625498, 0, 10, 809, t, null);
//		Step s810 = new Step("step_artiga_810", "step", 42.6719599999532,
//				0.699159998625665, 0, 10, 810, t, null);
//		Step s811 = new Step("step_artiga_811", "step", 42.6720199999532,
//				0.699259998626076, 0, 10, 811, t, null);
//		Step s812 = new Step("step_artiga_812", "step", 42.6720799999532,
//				0.699269998626118, 0, 10, 812, t, null);
//		Step s813 = new Step("step_artiga_813", "step", 42.6721499999532,
//				0.699259998626073, 0, 10, 813, t, null);
//		Step s814 = new Step("step_artiga_814", "step", 42.6721999999532,
//				0.699269998626112, 0, 10, 814, t, null);
//		Step s815 = new Step("step_artiga_815", "step", 42.6722799999532,
//				0.699349998626444, 0, 10, 815, t, null);
//		Step s816 = new Step("step_artiga_816", "step", 42.6723099999532,
//				0.699449998626859, 0, 10, 816, t, null);
//		Step s817 = new Step("step_artiga_817", "step", 42.6723599999532,
//				0.699489998627024, 0, 10, 817, t, null);
//		Step s818 = new Step("step_artiga_818", "step", 42.6724499999532,
//				0.699459998626897, 0, 10, 818, t, null);
//		Step s819 = new Step("step_artiga_819", "step", 42.6725199999532,
//				0.699449998626853, 0, 10, 819, t, null);
//		Step s820 = new Step("step_artiga_820", "step", 42.6725799999532,
//				0.699429998626766, 0, 10, 820, t, null);
//		Step s821 = new Step("step_artiga_821", "step", 42.6726499999532,
//				0.699419998626722, 0, 10, 821, t, null);
//		Step s822 = new Step("step_artiga_822", "step", 42.6726999999532,
//				0.699419998626719, 0, 10, 822, t, null);
//		Step s823 = new Step("step_artiga_823", "step", 42.6729299999532,
//				0.699629998627588, 0, 10, 823, t, null);
//		Step s824 = new Step("step_artiga_824", "step", 42.6729799999532,
//				0.699689998627834, 0, 10, 824, t, null);
//		Step s825 = new Step("step_artiga_825", "step", 42.6730399999532,
//				0.699799998628292, 0, 10, 825, t, null);
//		Step s826 = new Step("step_artiga_826", "step", 42.6730799999534,
//				0.699859998628538, 0, 10, 826, t, null);
//		Step s827 = new Step("step_artiga_827", "step", 42.6731299999532,
//				0.699919998628789, 0, 10, 827, t, null);
//		Step s828 = new Step("step_artiga_828", "step", 42.6732199999533,
//				0.699989998629077, 0, 10, 828, t, null);
//		Step s829 = new Step("step_artiga_829", "step", 42.6733099999534,
//				0.700069998629405, 0, 10, 829, t, null);
//		Step s830 = new Step("step_artiga_830", "step", 42.6733699999534,
//				0.70009999862953, 0, 10, 830, t, null);
//		Step s831 = new Step("step_artiga_831", "step", 42.6734699999534,
//				0.7001899986299, 0, 10, 831, t, null);
//		Step s832 = new Step("step_artiga_832", "step", 42.6736099999534,
//				0.700269998630227, 0, 10, 832, t, null);
//		Step s833 = new Step("step_artiga_833", "step", 42.6736499999534,
//				0.700329998630478, 0, 10, 833, t, null);
//		Step s834 = new Step("step_artiga_834", "step", 42.6737899999534,
//				0.700469998631052, 0, 10, 834, t, null);
//		Step s835 = new Step("step_artiga_835", "step", 42.6738399999534,
//				0.700549998631384, 0, 10, 835, t, null);
//		Step s836 = new Step("step_artiga_836", "step", 42.6738999999534,
//				0.700639998631754, 0, 10, 836, t, null);
//		Step s837 = new Step("step_artiga_837", "step", 42.6738999999534,
//				0.700649998631797, 0, 10, 837, t, null);
//		Step s838 = new Step("step_artiga_838", "step", 42.6739444210212,
//				0.70074356848571, 0, 10, 838, t, null);
//		Step s839 = new Step("step_artiga_839", "step", 42.6741035148114,
//				0.701042789882016, 0, 10, 839, t, null);
//		Step s840 = new Step("step_artiga_840", "step", 42.6742576190117,
//				0.701317789581594, 0, 10, 840, t, null);
//		Step s841 = new Step("step_artiga_841", "step", 42.6744342184431,
//				0.701591961245179, 0, 10, 841, t, null);
//		Step s842 = new Step("step_artiga_842", "step", 42.6746145820819,
//				0.70182938467433, 0, 10, 842, t, null);
//		Step s843 = new Step("step_artiga_843", "step", 42.6747894670397,
//				0.702018196606366, 0, 10, 843, t, null);
//		Step s844 = new Step("step_artiga_844", "step", 42.6751094548956,
//				0.702482347639689, 0, 10, 844, t, null);
//		Step s845 = new Step("step_artiga_845", "step", 42.6752702295714,
//				0.702641173368482, 0, 10, 845, t, null);
//		Step s846 = new Step("step_artiga_846", "step", 42.6754133743615,
//				0.702818955322094, 0, 10, 846, t, null);
//		Step s847 = new Step("step_artiga_847", "step", 42.6755307471284,
//				0.703058706976384, 0, 10, 847, t, null);
//		Step s848 = new Step("step_artiga_848", "step", 42.6757093954414,
//				0.70321077473123, 0, 10, 848, t, null);
//		Step s849 = new Step("step_artiga_849", "step", 42.6758918088642,
//				0.703326092862345, 0, 10, 849, t, null);
//		Step s850 = new Step("step_artiga_850", "step", 42.6760715575981,
//				0.703533039478473, 0, 10, 850, t, null);
//		Step s851 = new Step("step_artiga_851", "step", 42.6762201786122,
//				0.703759439903775, 0, 10, 851, t, null);
//		Step s852 = new Step("step_artiga_852", "step", 42.676351413883,
//				0.704016991798673, 0, 10, 852, t, null);
//		Step s853 = new Step("step_artiga_853", "step", 42.6765700892171,
//				0.704368956861044, 0, 10, 853, t, null);
//		Step s854 = new Step("step_artiga_854", "step", 42.6767468039646,
//				0.704649244921104, 0, 10, 854, t, null);
//		Step s855 = new Step("step_artiga_855", "step", 42.6769558679402,
//				0.704971057938917, 0, 10, 855, t, null);
//		Step s856 = new Step("step_artiga_856", "step", 42.6771143401249,
//				0.705239816829727, 0, 10, 856, t, null);
//		Step s857 = new Step("step_artiga_857", "step", 42.6772777996928,
//				0.705532801873913, 0, 10, 857, t, null);
//		Step s858 = new Step("step_artiga_858", "step", 42.6773454316346,
//				0.705762192264402, 0, 10, 858, t, null);
//		Step s859 = new Step("step_artiga_859", "step", 42.6774204731799,
//				0.705911983025229, 0, 10, 859, t, null);
//		Step s860 = new Step("step_artiga_860", "step", 42.677575888987,
//				0.706028305611309, 0, 10, 860, t, null);
//		Step s861 = new Step("step_artiga_861", "step", 42.6777169519444,
//				0.706102441977757, 0, 10, 861, t, null);
//		Step s862 = new Step("step_artiga_862", "step", 42.6778353967688,
//				0.70617130886651, 0, 10, 862, t, null);
//		Step s863 = new Step("step_artiga_863", "step", 42.6779544523718,
//				0.70627066434574, 0, 10, 863, t, null);
//		Step s864 = new Step("step_artiga_864", "step", 42.678072774945,
//				0.706333434122288, 0, 10, 864, t, h1);
//
//		try {
//			stepDataDao.create(s1);
//			stepDataDao.create(s2);
//			stepDataDao.create(s3);
//			stepDataDao.create(s4);
//			stepDataDao.create(s5);
//			stepDataDao.create(s6);
//			stepDataDao.create(s7);
//			stepDataDao.create(s8);
//			stepDataDao.create(s9);
//			stepDataDao.create(s10);
//			stepDataDao.create(s11);
//			stepDataDao.create(s12);
//			stepDataDao.create(s13);
//			stepDataDao.create(s14);
//			stepDataDao.create(s15);
//			stepDataDao.create(s16);
//			stepDataDao.create(s17);
//			stepDataDao.create(s18);
//			stepDataDao.create(s19);
//			stepDataDao.create(s20);
//			stepDataDao.create(s21);
//			stepDataDao.create(s22);
//			stepDataDao.create(s23);
//			stepDataDao.create(s24);
//			stepDataDao.create(s25);
//			stepDataDao.create(s26);
//			stepDataDao.create(s27);
//			stepDataDao.create(s28);
//			stepDataDao.create(s29);
//			stepDataDao.create(s30);
//			stepDataDao.create(s31);
//			stepDataDao.create(s32);
//			stepDataDao.create(s33);
//			stepDataDao.create(s34);
//			stepDataDao.create(s35);
//			stepDataDao.create(s36);
//			stepDataDao.create(s37);
//			stepDataDao.create(s38);
//			stepDataDao.create(s39);
//			stepDataDao.create(s40);
//			stepDataDao.create(s41);
//			stepDataDao.create(s42);
//			stepDataDao.create(s43);
//			stepDataDao.create(s44);
//			stepDataDao.create(s45);
//			stepDataDao.create(s46);
//			stepDataDao.create(s47);
//			stepDataDao.create(s48);
//			stepDataDao.create(s49);
//			stepDataDao.create(s50);
//			stepDataDao.create(s51);
//			stepDataDao.create(s52);
//			stepDataDao.create(s53);
//			stepDataDao.create(s54);
//			stepDataDao.create(s55);
//			stepDataDao.create(s56);
//			stepDataDao.create(s57);
//			stepDataDao.create(s58);
//			stepDataDao.create(s59);
//			stepDataDao.create(s60);
//			stepDataDao.create(s61);
//			stepDataDao.create(s62);
//			stepDataDao.create(s63);
//			stepDataDao.create(s64);
//			stepDataDao.create(s65);
//			stepDataDao.create(s66);
//			stepDataDao.create(s67);
//			stepDataDao.create(s68);
//			stepDataDao.create(s69);
//			stepDataDao.create(s70);
//			stepDataDao.create(s71);
//			stepDataDao.create(s72);
//			stepDataDao.create(s73);
//			stepDataDao.create(s74);
//			stepDataDao.create(s75);
//			stepDataDao.create(s76);
//			stepDataDao.create(s77);
//			stepDataDao.create(s78);
//			stepDataDao.create(s79);
//			stepDataDao.create(s80);
//			stepDataDao.create(s81);
//			stepDataDao.create(s82);
//			stepDataDao.create(s83);
//			stepDataDao.create(s84);
//			stepDataDao.create(s85);
//			stepDataDao.create(s86);
//			stepDataDao.create(s87);
//			stepDataDao.create(s88);
//			stepDataDao.create(s89);
//			stepDataDao.create(s90);
//			stepDataDao.create(s91);
//			stepDataDao.create(s92);
//			stepDataDao.create(s93);
//			stepDataDao.create(s94);
//			stepDataDao.create(s95);
//			stepDataDao.create(s96);
//			stepDataDao.create(s97);
//			stepDataDao.create(s98);
//			stepDataDao.create(s99);
//			stepDataDao.create(s100);
//			stepDataDao.create(s101);
//			stepDataDao.create(s102);
//			stepDataDao.create(s103);
//			stepDataDao.create(s104);
//			stepDataDao.create(s105);
//			stepDataDao.create(s106);
//			stepDataDao.create(s107);
//			stepDataDao.create(s108);
//			stepDataDao.create(s109);
//			stepDataDao.create(s110);
//			stepDataDao.create(s111);
//			stepDataDao.create(s112);
//			stepDataDao.create(s113);
//			stepDataDao.create(s114);
//			stepDataDao.create(s115);
//			stepDataDao.create(s116);
//			stepDataDao.create(s117);
//			stepDataDao.create(s118);
//			stepDataDao.create(s119);
//			stepDataDao.create(s120);
//			stepDataDao.create(s121);
//			stepDataDao.create(s122);
//			stepDataDao.create(s123);
//			stepDataDao.create(s124);
//			stepDataDao.create(s125);
//			stepDataDao.create(s126);
//			stepDataDao.create(s127);
//			stepDataDao.create(s128);
//			stepDataDao.create(s129);
//			stepDataDao.create(s130);
//			stepDataDao.create(s131);
//			stepDataDao.create(s132);
//			stepDataDao.create(s133);
//			stepDataDao.create(s134);
//			stepDataDao.create(s135);
//			stepDataDao.create(s136);
//			stepDataDao.create(s137);
//			stepDataDao.create(s138);
//			stepDataDao.create(s139);
//			stepDataDao.create(s140);
//			stepDataDao.create(s141);
//			stepDataDao.create(s142);
//			stepDataDao.create(s143);
//			stepDataDao.create(s144);
//			stepDataDao.create(s145);
//			stepDataDao.create(s146);
//			stepDataDao.create(s147);
//			stepDataDao.create(s148);
//			stepDataDao.create(s149);
//			stepDataDao.create(s150);
//			stepDataDao.create(s151);
//			stepDataDao.create(s152);
//			stepDataDao.create(s153);
//			stepDataDao.create(s154);
//			stepDataDao.create(s155);
//			stepDataDao.create(s156);
//			stepDataDao.create(s157);
//			stepDataDao.create(s158);
//			stepDataDao.create(s159);
//			stepDataDao.create(s160);
//			stepDataDao.create(s161);
//			stepDataDao.create(s162);
//			stepDataDao.create(s163);
//			stepDataDao.create(s164);
//			stepDataDao.create(s165);
//			stepDataDao.create(s166);
//			stepDataDao.create(s167);
//			stepDataDao.create(s168);
//			stepDataDao.create(s169);
//			stepDataDao.create(s170);
//			stepDataDao.create(s171);
//			stepDataDao.create(s172);
//			stepDataDao.create(s173);
//			stepDataDao.create(s174);
//			stepDataDao.create(s175);
//			stepDataDao.create(s176);
//			stepDataDao.create(s177);
//			stepDataDao.create(s178);
//			stepDataDao.create(s179);
//			stepDataDao.create(s180);
//			stepDataDao.create(s181);
//			stepDataDao.create(s182);
//			stepDataDao.create(s183);
//			stepDataDao.create(s184);
//			stepDataDao.create(s185);
//			stepDataDao.create(s186);
//			stepDataDao.create(s187);
//			stepDataDao.create(s188);
//			stepDataDao.create(s189);
//			stepDataDao.create(s190);
//			stepDataDao.create(s191);
//			stepDataDao.create(s192);
//			stepDataDao.create(s193);
//			stepDataDao.create(s194);
//			stepDataDao.create(s195);
//			stepDataDao.create(s196);
//			stepDataDao.create(s197);
//			stepDataDao.create(s198);
//			stepDataDao.create(s199);
//			stepDataDao.create(s200);
//			stepDataDao.create(s201);
//			stepDataDao.create(s202);
//			stepDataDao.create(s203);
//			stepDataDao.create(s204);
//			stepDataDao.create(s205);
//			stepDataDao.create(s206);
//			stepDataDao.create(s207);
//			stepDataDao.create(s208);
//			stepDataDao.create(s209);
//			stepDataDao.create(s210);
//			stepDataDao.create(s211);
//			stepDataDao.create(s212);
//			stepDataDao.create(s213);
//			stepDataDao.create(s214);
//			stepDataDao.create(s215);
//			stepDataDao.create(s216);
//			stepDataDao.create(s217);
//			stepDataDao.create(s218);
//			stepDataDao.create(s219);
//			stepDataDao.create(s220);
//			stepDataDao.create(s221);
//			stepDataDao.create(s222);
//			stepDataDao.create(s223);
//			stepDataDao.create(s224);
//			stepDataDao.create(s225);
//			stepDataDao.create(s226);
//			stepDataDao.create(s227);
//			stepDataDao.create(s228);
//			stepDataDao.create(s229);
//			stepDataDao.create(s230);
//			stepDataDao.create(s231);
//			stepDataDao.create(s232);
//			stepDataDao.create(s233);
//			stepDataDao.create(s234);
//			stepDataDao.create(s235);
//			stepDataDao.create(s236);
//			stepDataDao.create(s237);
//			stepDataDao.create(s238);
//			stepDataDao.create(s239);
//			stepDataDao.create(s240);
//			stepDataDao.create(s241);
//			stepDataDao.create(s242);
//			stepDataDao.create(s243);
//			stepDataDao.create(s244);
//			stepDataDao.create(s245);
//			stepDataDao.create(s246);
//			stepDataDao.create(s247);
//			stepDataDao.create(s248);
//			stepDataDao.create(s249);
//			stepDataDao.create(s250);
//			stepDataDao.create(s251);
//			stepDataDao.create(s252);
//			stepDataDao.create(s253);
//			stepDataDao.create(s254);
//			stepDataDao.create(s255);
//			stepDataDao.create(s256);
//			stepDataDao.create(s257);
//			stepDataDao.create(s258);
//			stepDataDao.create(s259);
//			stepDataDao.create(s260);
//			stepDataDao.create(s261);
//			stepDataDao.create(s262);
//			stepDataDao.create(s263);
//			stepDataDao.create(s264);
//			stepDataDao.create(s265);
//			stepDataDao.create(s266);
//			stepDataDao.create(s267);
//			stepDataDao.create(s268);
//			stepDataDao.create(s269);
//			stepDataDao.create(s270);
//			stepDataDao.create(s271);
//			stepDataDao.create(s272);
//			stepDataDao.create(s273);
//			stepDataDao.create(s274);
//			stepDataDao.create(s275);
//			stepDataDao.create(s276);
//			stepDataDao.create(s277);
//			stepDataDao.create(s278);
//			stepDataDao.create(s279);
//			stepDataDao.create(s280);
//			stepDataDao.create(s281);
//			stepDataDao.create(s282);
//			stepDataDao.create(s283);
//			stepDataDao.create(s284);
//			stepDataDao.create(s285);
//			stepDataDao.create(s286);
//			stepDataDao.create(s287);
//			stepDataDao.create(s288);
//			stepDataDao.create(s289);
//			stepDataDao.create(s290);
//			stepDataDao.create(s291);
//			stepDataDao.create(s292);
//			stepDataDao.create(s293);
//			stepDataDao.create(s294);
//			stepDataDao.create(s295);
//			stepDataDao.create(s296);
//			stepDataDao.create(s297);
//			stepDataDao.create(s298);
//			stepDataDao.create(s299);
//			stepDataDao.create(s300);
//			stepDataDao.create(s301);
//			stepDataDao.create(s302);
//			stepDataDao.create(s303);
//			stepDataDao.create(s304);
//			stepDataDao.create(s305);
//			stepDataDao.create(s306);
//			stepDataDao.create(s307);
//			stepDataDao.create(s308);
//			stepDataDao.create(s309);
//			stepDataDao.create(s310);
//			stepDataDao.create(s311);
//			stepDataDao.create(s312);
//			stepDataDao.create(s313);
//			stepDataDao.create(s314);
//			stepDataDao.create(s315);
//			stepDataDao.create(s316);
//			stepDataDao.create(s317);
//			stepDataDao.create(s318);
//			stepDataDao.create(s319);
//			stepDataDao.create(s320);
//			stepDataDao.create(s321);
//			stepDataDao.create(s322);
//			stepDataDao.create(s323);
//			stepDataDao.create(s324);
//			stepDataDao.create(s325);
//			stepDataDao.create(s326);
//			stepDataDao.create(s327);
//			stepDataDao.create(s328);
//			stepDataDao.create(s329);
//			stepDataDao.create(s330);
//			stepDataDao.create(s331);
//			stepDataDao.create(s332);
//			stepDataDao.create(s333);
//			stepDataDao.create(s334);
//			stepDataDao.create(s335);
//			stepDataDao.create(s336);
//			stepDataDao.create(s337);
//			stepDataDao.create(s338);
//			stepDataDao.create(s339);
//			stepDataDao.create(s340);
//			stepDataDao.create(s341);
//			stepDataDao.create(s342);
//			stepDataDao.create(s343);
//			stepDataDao.create(s344);
//			stepDataDao.create(s345);
//			stepDataDao.create(s346);
//			stepDataDao.create(s347);
//			stepDataDao.create(s348);
//			stepDataDao.create(s349);
//			stepDataDao.create(s350);
//			stepDataDao.create(s351);
//			stepDataDao.create(s352);
//			stepDataDao.create(s353);
//			stepDataDao.create(s354);
//			stepDataDao.create(s355);
//			stepDataDao.create(s356);
//			stepDataDao.create(s357);
//			stepDataDao.create(s358);
//			stepDataDao.create(s359);
//			stepDataDao.create(s360);
//			stepDataDao.create(s361);
//			stepDataDao.create(s362);
//			stepDataDao.create(s363);
//			stepDataDao.create(s364);
//			stepDataDao.create(s365);
//			stepDataDao.create(s366);
//			stepDataDao.create(s367);
//			stepDataDao.create(s368);
//			stepDataDao.create(s369);
//			stepDataDao.create(s370);
//			stepDataDao.create(s371);
//			stepDataDao.create(s372);
//			stepDataDao.create(s373);
//			stepDataDao.create(s374);
//			stepDataDao.create(s375);
//			stepDataDao.create(s376);
//			stepDataDao.create(s377);
//			stepDataDao.create(s378);
//			stepDataDao.create(s379);
//			stepDataDao.create(s380);
//			stepDataDao.create(s381);
//			stepDataDao.create(s382);
//			stepDataDao.create(s383);
//			stepDataDao.create(s384);
//			stepDataDao.create(s385);
//			stepDataDao.create(s386);
//			stepDataDao.create(s387);
//			stepDataDao.create(s388);
//			stepDataDao.create(s389);
//			stepDataDao.create(s390);
//			stepDataDao.create(s391);
//			stepDataDao.create(s392);
//			stepDataDao.create(s393);
//			stepDataDao.create(s394);
//			stepDataDao.create(s395);
//			stepDataDao.create(s396);
//			stepDataDao.create(s397);
//			stepDataDao.create(s398);
//			stepDataDao.create(s399);
//			stepDataDao.create(s400);
//			stepDataDao.create(s401);
//			stepDataDao.create(s402);
//			stepDataDao.create(s403);
//			stepDataDao.create(s404);
//			stepDataDao.create(s405);
//			stepDataDao.create(s406);
//			stepDataDao.create(s407);
//			stepDataDao.create(s408);
//			stepDataDao.create(s409);
//			stepDataDao.create(s410);
//			stepDataDao.create(s411);
//			stepDataDao.create(s412);
//			stepDataDao.create(s413);
//			stepDataDao.create(s414);
//			stepDataDao.create(s415);
//			stepDataDao.create(s416);
//			stepDataDao.create(s417);
//			stepDataDao.create(s418);
//			stepDataDao.create(s419);
//			stepDataDao.create(s420);
//			stepDataDao.create(s421);
//			stepDataDao.create(s422);
//			stepDataDao.create(s423);
//			stepDataDao.create(s424);
//			stepDataDao.create(s425);
//			stepDataDao.create(s426);
//			stepDataDao.create(s427);
//			stepDataDao.create(s428);
//			stepDataDao.create(s429);
//			stepDataDao.create(s430);
//			stepDataDao.create(s431);
//			stepDataDao.create(s432);
//			stepDataDao.create(s433);
//			stepDataDao.create(s434);
//			stepDataDao.create(s435);
//			stepDataDao.create(s436);
//			stepDataDao.create(s437);
//			stepDataDao.create(s438);
//			stepDataDao.create(s439);
//			stepDataDao.create(s440);
//			stepDataDao.create(s441);
//			stepDataDao.create(s442);
//			stepDataDao.create(s443);
//			stepDataDao.create(s444);
//			stepDataDao.create(s445);
//			stepDataDao.create(s446);
//			stepDataDao.create(s447);
//			stepDataDao.create(s448);
//			stepDataDao.create(s449);
//			stepDataDao.create(s450);
//			stepDataDao.create(s451);
//			stepDataDao.create(s452);
//			stepDataDao.create(s453);
//			stepDataDao.create(s454);
//			stepDataDao.create(s455);
//			stepDataDao.create(s456);
//			stepDataDao.create(s457);
//			stepDataDao.create(s458);
//			stepDataDao.create(s459);
//			stepDataDao.create(s460);
//			stepDataDao.create(s461);
//			stepDataDao.create(s462);
//			stepDataDao.create(s463);
//			stepDataDao.create(s464);
//			stepDataDao.create(s465);
//			stepDataDao.create(s466);
//			stepDataDao.create(s467);
//			stepDataDao.create(s468);
//			stepDataDao.create(s469);
//			stepDataDao.create(s470);
//			stepDataDao.create(s471);
//			stepDataDao.create(s472);
//			stepDataDao.create(s473);
//			stepDataDao.create(s474);
//			stepDataDao.create(s475);
//			stepDataDao.create(s476);
//			stepDataDao.create(s477);
//			stepDataDao.create(s478);
//			stepDataDao.create(s479);
//			stepDataDao.create(s480);
//			stepDataDao.create(s481);
//			stepDataDao.create(s482);
//			stepDataDao.create(s483);
//			stepDataDao.create(s484);
//			stepDataDao.create(s485);
//			stepDataDao.create(s486);
//			stepDataDao.create(s487);
//			stepDataDao.create(s488);
//			stepDataDao.create(s489);
//			stepDataDao.create(s490);
//			stepDataDao.create(s491);
//			stepDataDao.create(s492);
//			stepDataDao.create(s493);
//			stepDataDao.create(s494);
//			stepDataDao.create(s495);
//			stepDataDao.create(s496);
//			stepDataDao.create(s497);
//			stepDataDao.create(s498);
//			stepDataDao.create(s499);
//			stepDataDao.create(s500);
//			stepDataDao.create(s501);
//			stepDataDao.create(s502);
//			stepDataDao.create(s503);
//			stepDataDao.create(s504);
//			stepDataDao.create(s505);
//			stepDataDao.create(s506);
//			stepDataDao.create(s507);
//			stepDataDao.create(s508);
//			stepDataDao.create(s509);
//			stepDataDao.create(s510);
//			stepDataDao.create(s511);
//			stepDataDao.create(s512);
//			stepDataDao.create(s513);
//			stepDataDao.create(s514);
//			stepDataDao.create(s515);
//			stepDataDao.create(s516);
//			stepDataDao.create(s517);
//			stepDataDao.create(s518);
//			stepDataDao.create(s519);
//			stepDataDao.create(s520);
//			stepDataDao.create(s521);
//			stepDataDao.create(s522);
//			stepDataDao.create(s523);
//			stepDataDao.create(s524);
//			stepDataDao.create(s525);
//			stepDataDao.create(s526);
//			stepDataDao.create(s527);
//			stepDataDao.create(s528);
//			stepDataDao.create(s529);
//			stepDataDao.create(s530);
//			stepDataDao.create(s531);
//			stepDataDao.create(s532);
//			stepDataDao.create(s533);
//			stepDataDao.create(s534);
//			stepDataDao.create(s535);
//			stepDataDao.create(s536);
//			stepDataDao.create(s537);
//			stepDataDao.create(s538);
//			stepDataDao.create(s539);
//			stepDataDao.create(s540);
//			stepDataDao.create(s541);
//			stepDataDao.create(s542);
//			stepDataDao.create(s543);
//			stepDataDao.create(s544);
//			stepDataDao.create(s545);
//			stepDataDao.create(s546);
//			stepDataDao.create(s547);
//			stepDataDao.create(s548);
//			stepDataDao.create(s549);
//			stepDataDao.create(s550);
//			stepDataDao.create(s551);
//			stepDataDao.create(s552);
//			stepDataDao.create(s553);
//			stepDataDao.create(s554);
//			stepDataDao.create(s555);
//			stepDataDao.create(s556);
//			stepDataDao.create(s557);
//			stepDataDao.create(s558);
//			stepDataDao.create(s559);
//			stepDataDao.create(s560);
//			stepDataDao.create(s561);
//			stepDataDao.create(s562);
//			stepDataDao.create(s563);
//			stepDataDao.create(s564);
//			stepDataDao.create(s565);
//			stepDataDao.create(s566);
//			stepDataDao.create(s567);
//			stepDataDao.create(s568);
//			stepDataDao.create(s569);
//			stepDataDao.create(s570);
//			stepDataDao.create(s571);
//			stepDataDao.create(s572);
//			stepDataDao.create(s573);
//			stepDataDao.create(s574);
//			stepDataDao.create(s575);
//			stepDataDao.create(s576);
//			stepDataDao.create(s577);
//			stepDataDao.create(s578);
//			stepDataDao.create(s579);
//			stepDataDao.create(s580);
//			stepDataDao.create(s581);
//			stepDataDao.create(s582);
//			stepDataDao.create(s583);
//			stepDataDao.create(s584);
//			stepDataDao.create(s585);
//			stepDataDao.create(s586);
//			stepDataDao.create(s587);
//			stepDataDao.create(s588);
//			stepDataDao.create(s589);
//			stepDataDao.create(s590);
//			stepDataDao.create(s591);
//			stepDataDao.create(s592);
//			stepDataDao.create(s593);
//			stepDataDao.create(s594);
//			stepDataDao.create(s595);
//			stepDataDao.create(s596);
//			stepDataDao.create(s597);
//			stepDataDao.create(s598);
//			stepDataDao.create(s599);
//			stepDataDao.create(s600);
//			stepDataDao.create(s601);
//			stepDataDao.create(s602);
//			stepDataDao.create(s603);
//			stepDataDao.create(s604);
//			stepDataDao.create(s605);
//			stepDataDao.create(s606);
//			stepDataDao.create(s607);
//			stepDataDao.create(s608);
//			stepDataDao.create(s609);
//			stepDataDao.create(s610);
//			stepDataDao.create(s611);
//			stepDataDao.create(s612);
//			stepDataDao.create(s613);
//			stepDataDao.create(s614);
//			stepDataDao.create(s615);
//			stepDataDao.create(s616);
//			stepDataDao.create(s617);
//			stepDataDao.create(s618);
//			stepDataDao.create(s619);
//			stepDataDao.create(s620);
//			stepDataDao.create(s621);
//			stepDataDao.create(s622);
//			stepDataDao.create(s623);
//			stepDataDao.create(s624);
//			stepDataDao.create(s625);
//			stepDataDao.create(s626);
//			stepDataDao.create(s627);
//			stepDataDao.create(s628);
//			stepDataDao.create(s629);
//			stepDataDao.create(s630);
//			stepDataDao.create(s631);
//			stepDataDao.create(s632);
//			stepDataDao.create(s633);
//			stepDataDao.create(s634);
//			stepDataDao.create(s635);
//			stepDataDao.create(s636);
//			stepDataDao.create(s637);
//			stepDataDao.create(s638);
//			stepDataDao.create(s639);
//			stepDataDao.create(s640);
//			stepDataDao.create(s641);
//			stepDataDao.create(s642);
//			stepDataDao.create(s643);
//			stepDataDao.create(s644);
//			stepDataDao.create(s645);
//			stepDataDao.create(s646);
//			stepDataDao.create(s647);
//			stepDataDao.create(s648);
//			stepDataDao.create(s649);
//			stepDataDao.create(s650);
//			stepDataDao.create(s651);
//			stepDataDao.create(s652);
//			stepDataDao.create(s653);
//			stepDataDao.create(s654);
//			stepDataDao.create(s655);
//			stepDataDao.create(s656);
//			stepDataDao.create(s657);
//			stepDataDao.create(s658);
//			stepDataDao.create(s659);
//			stepDataDao.create(s660);
//			stepDataDao.create(s661);
//			stepDataDao.create(s662);
//			stepDataDao.create(s663);
//			stepDataDao.create(s664);
//			stepDataDao.create(s665);
//			stepDataDao.create(s666);
//			stepDataDao.create(s667);
//			stepDataDao.create(s668);
//			stepDataDao.create(s669);
//			stepDataDao.create(s670);
//			stepDataDao.create(s671);
//			stepDataDao.create(s672);
//			stepDataDao.create(s673);
//			stepDataDao.create(s674);
//			stepDataDao.create(s675);
//			stepDataDao.create(s676);
//			stepDataDao.create(s677);
//			stepDataDao.create(s678);
//			stepDataDao.create(s679);
//			stepDataDao.create(s680);
//			stepDataDao.create(s681);
//			stepDataDao.create(s682);
//			stepDataDao.create(s683);
//			stepDataDao.create(s684);
//			stepDataDao.create(s685);
//			stepDataDao.create(s686);
//			stepDataDao.create(s687);
//			stepDataDao.create(s688);
//			stepDataDao.create(s689);
//			stepDataDao.create(s690);
//			stepDataDao.create(s691);
//			stepDataDao.create(s692);
//			stepDataDao.create(s693);
//			stepDataDao.create(s694);
//			stepDataDao.create(s695);
//			stepDataDao.create(s696);
//			stepDataDao.create(s697);
//			stepDataDao.create(s698);
//			stepDataDao.create(s699);
//			stepDataDao.create(s700);
//			stepDataDao.create(s701);
//			stepDataDao.create(s702);
//			stepDataDao.create(s703);
//			stepDataDao.create(s704);
//			stepDataDao.create(s705);
//			stepDataDao.create(s706);
//			stepDataDao.create(s707);
//			stepDataDao.create(s708);
//			stepDataDao.create(s709);
//			stepDataDao.create(s710);
//			stepDataDao.create(s711);
//			stepDataDao.create(s712);
//			stepDataDao.create(s713);
//			stepDataDao.create(s714);
//			stepDataDao.create(s715);
//			stepDataDao.create(s716);
//			stepDataDao.create(s717);
//			stepDataDao.create(s718);
//			stepDataDao.create(s719);
//			stepDataDao.create(s720);
//			stepDataDao.create(s721);
//			stepDataDao.create(s722);
//			stepDataDao.create(s723);
//			stepDataDao.create(s724);
//			stepDataDao.create(s725);
//			stepDataDao.create(s726);
//			stepDataDao.create(s727);
//			stepDataDao.create(s728);
//			stepDataDao.create(s729);
//			stepDataDao.create(s730);
//			stepDataDao.create(s731);
//			stepDataDao.create(s732);
//			stepDataDao.create(s733);
//			stepDataDao.create(s734);
//			stepDataDao.create(s735);
//			stepDataDao.create(s736);
//			stepDataDao.create(s737);
//			stepDataDao.create(s738);
//			stepDataDao.create(s739);
//			stepDataDao.create(s740);
//			stepDataDao.create(s741);
//			stepDataDao.create(s742);
//			stepDataDao.create(s743);
//			stepDataDao.create(s744);
//			stepDataDao.create(s745);
//			stepDataDao.create(s746);
//			stepDataDao.create(s747);
//			stepDataDao.create(s748);
//			stepDataDao.create(s749);
//			stepDataDao.create(s750);
//			stepDataDao.create(s751);
//			stepDataDao.create(s752);
//			stepDataDao.create(s753);
//			stepDataDao.create(s754);
//			stepDataDao.create(s755);
//			stepDataDao.create(s756);
//			stepDataDao.create(s757);
//			stepDataDao.create(s758);
//			stepDataDao.create(s759);
//			stepDataDao.create(s760);
//			stepDataDao.create(s761);
//			stepDataDao.create(s762);
//			stepDataDao.create(s763);
//			stepDataDao.create(s764);
//			stepDataDao.create(s765);
//			stepDataDao.create(s766);
//			stepDataDao.create(s767);
//			stepDataDao.create(s768);
//			stepDataDao.create(s769);
//			stepDataDao.create(s770);
//			stepDataDao.create(s771);
//			stepDataDao.create(s772);
//			stepDataDao.create(s773);
//			stepDataDao.create(s774);
//			stepDataDao.create(s775);
//			stepDataDao.create(s776);
//			stepDataDao.create(s777);
//			stepDataDao.create(s778);
//			stepDataDao.create(s779);
//			stepDataDao.create(s780);
//			stepDataDao.create(s781);
//			stepDataDao.create(s782);
//			stepDataDao.create(s783);
//			stepDataDao.create(s784);
//			stepDataDao.create(s785);
//			stepDataDao.create(s786);
//			stepDataDao.create(s787);
//			stepDataDao.create(s788);
//			stepDataDao.create(s789);
//			stepDataDao.create(s790);
//			stepDataDao.create(s791);
//			stepDataDao.create(s792);
//			stepDataDao.create(s793);
//			stepDataDao.create(s794);
//			stepDataDao.create(s795);
//			stepDataDao.create(s796);
//			stepDataDao.create(s797);
//			stepDataDao.create(s798);
//			stepDataDao.create(s799);
//			stepDataDao.create(s800);
//			stepDataDao.create(s801);
//			stepDataDao.create(s802);
//			stepDataDao.create(s803);
//			stepDataDao.create(s804);
//			stepDataDao.create(s805);
//			stepDataDao.create(s806);
//			stepDataDao.create(s807);
//			stepDataDao.create(s808);
//			stepDataDao.create(s809);
//			stepDataDao.create(s810);
//			stepDataDao.create(s811);
//			stepDataDao.create(s812);
//			stepDataDao.create(s813);
//			stepDataDao.create(s814);
//			stepDataDao.create(s815);
//			stepDataDao.create(s816);
//			stepDataDao.create(s817);
//			stepDataDao.create(s818);
//			stepDataDao.create(s819);
//			stepDataDao.create(s820);
//			stepDataDao.create(s821);
//			stepDataDao.create(s822);
//			stepDataDao.create(s823);
//			stepDataDao.create(s824);
//			stepDataDao.create(s825);
//			stepDataDao.create(s826);
//			stepDataDao.create(s827);
//			stepDataDao.create(s828);
//			stepDataDao.create(s829);
//			stepDataDao.create(s830);
//			stepDataDao.create(s831);
//			stepDataDao.create(s832);
//			stepDataDao.create(s833);
//			stepDataDao.create(s834);
//			stepDataDao.create(s835);
//			stepDataDao.create(s836);
//			stepDataDao.create(s837);
//			stepDataDao.create(s838);
//			stepDataDao.create(s839);
//			stepDataDao.create(s840);
//			stepDataDao.create(s841);
//			stepDataDao.create(s842);
//			stepDataDao.create(s843);
//			stepDataDao.create(s844);
//			stepDataDao.create(s845);
//			stepDataDao.create(s846);
//			stepDataDao.create(s847);
//			stepDataDao.create(s848);
//			stepDataDao.create(s849);
//			stepDataDao.create(s850);
//			stepDataDao.create(s851);
//			stepDataDao.create(s852);
//			stepDataDao.create(s853);
//			stepDataDao.create(s854);
//			stepDataDao.create(s855);
//			stepDataDao.create(s856);
//			stepDataDao.create(s857);
//			stepDataDao.create(s858);
//			stepDataDao.create(s859);
//			stepDataDao.create(s860);
//			stepDataDao.create(s861);
//			stepDataDao.create(s862);
//			stepDataDao.create(s863);
//			stepDataDao.create(s864);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting step", "Insert error " + ex.toString());
//		}
//		
//	
//		Route r = new Route();
//		r.setId("ROUTE_ARTIGA");
//		r.setName("Estanys de l’Artiga de Lin: Estanhons de Pois i Estanh deth Còth deth Hòro");
//		r.setDescription("Itinerari circular de 10 km amb 1040 m de desnivell (5-6 h a peu), per visitar " + 
//		"els Estanhons de Pois i l’Estanh de Còth deth Hòro. Sortida des de l’aparcament del Plan dera Artiga " + 
//		"de Lin. El camí segueix corriols de muntanya, de vegades poc marcats i de pendents molt acusades, " + 
//		"amb alguns trams de tartera i grimpades amb poca dificultat. L’itinerari és adequat per persones amb " + 
//		"un bon entrenament i experiència en alta muntanya. Alternativament, es pot visitar només un dels estanys, " + 
//		"sense completar el circuit: Pois, 3 km (anada) i 595 m de desnivell; Coth deth Hòro, 3.25 km (anada, " + 
//		"seguint la part final de l’itinerari a l’inversa) i 770 m de desnivell. Són estanys típicament alpins i " +
//		"amb aigües de tipus carbonatat, en un paisatge de roques calcàries i sorrenques de formes " + 
//		"espectaculars i vistes sobre el massís i glacera de l’Aneto.");
//		r.setUserId("1");
//		// Ph_ch parameters
//		// r.setReference(r6);
//		// Interactive image
//		//r.setInteractiveImage(img);
//		r.setTrack(t);
//		r.setLocalCarto("OSMPublicTransport_HiRes.mbtiles");
//
//		try {
//			routeDataDao.create(r);
//		} catch (RuntimeException ex) {
//			Log.e("Inserting route", "Insert error " + ex.toString());
//		}
//
//	}		

	public static void loadRedonCompact(DataBaseHelper db, Context context) {
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		RuntimeExceptionDao<Reference, String> referenceDataDao = db
				.getReferenceDataDao();
		RuntimeExceptionDao<InteractiveImage, String> interactiveImageDataDao = db
				.getInteractiveImageDataDao();
		RuntimeExceptionDao<Box, String> boxDataDao = db.getBoxDataDao();

		Track t = new Track("TRACK_REDON", "Waypoints recorregut Estanh Redon");
		try {
			trackDataDao.create(t);
		} catch (RuntimeException ex) {
			Log.e("Inserting track", "Insert error " + ex.toString());
		}
				
		// Sortida
		Step s1 = new Step("step_redon_1", "step1", 42.6271286833071,
				0.763500210996426, 0, 10, 1, t);		
		Step s2 = new Step("step_redon_2", "step1", 42.6275014082057,
				0.765600470040226, 0, 10, 2, t);		
		Step s3 = new Step("step_redon_3", "step1", 42.6274169991666,
				0.76702904625563, 0, 10, 3, t, null);
		Step s4 = new Step("step_redon_4", "step1", 42.6277910435601,
				0.768183665859397, 0, 10, 4, t, null);
		Step s5 = new Step("step_redon_5", "step1", 42.6272820099257,
				0.769414374849475, 0, 10, 5, t, null);
		Step s6 = new Step("step_redon_6", "step1", 42.6274246175308,
				0.770694949860626, 0, 10, 6, t, null);
		Step s7 = new Step("step_redon_7", "step1", 42.6277790705353,
				0.771784410153487, 0, 10, 7, t, null);
		Step s8 = new Step("step_redon_8", "step1", 42.6281256982475,
				0.772518923367898, 0, 10, 8, t, null);
		Step s9 = new Step("step_redon_9", "step1", 42.6283586202044,
				0.773143964179326, 0, 10, 9, t);		
		Step s10 = new Step("step_redon_10", "step1", 42.6285187747999,
				0.773703827508143, 0, 10, 10, t);		
		Step s11 = new Step("step_redon_11", "step1", 42.6289143764006,
				0.774921917764004, 0, 10, 11, t, null);
		Step s12 = new Step("step_redon_12", "step1", 42.6293034630362,
				0.775732826296007, 0, 10, 12, t, null);
		Step s13 = new Step("step_redon_13", "step1", 42.629195381669,
				0.776484281595913, 0, 10, 13, t, null);
		Step s14 = new Step("step_redon_14", "step1", 42.6287473460252,
				0.777377578342566, 0, 10, 14, t, null);
		Step s15 = new Step("step_redon_15", "step1", 42.6286806404823,
				0.778269812290091, 0, 10, 15, t, null);
		Step s16 = new Step("step_redon_16", "step1", 42.6288146821753,
				0.779466030362528, 0, 10, 16, t, null);
		Step s17 = new Step("step_redon_17", "step1", 42.6288213903172,
				0.779793056208291, 0, 10, 17, t, null);
		Step s18 = new Step("step_redon_18", "step1", 42.6287648668222,
				0.780519604486472, 0, 10, 18, t, null);
		Step s19 = new Step("step_redon_19", "step1", 42.6281997327169,
				0.781384989168146, 0, 10, 19, t, null);
		Step s20 = new Step("step_redon_20", "step1", 42.6280008714093,
				0.78193631637987, 0, 10, 20, t, null);
		Step s21 = new Step("step_redon_21", "step1", 42.6279912551399,
				0.782715891327825, 0, 10, 21, t, null);
		Step s22 = new Step("step_redon_22", "step1", 42.6283277355105,
				0.783183607418323, 0, 10, 22, t, null);
		Step s23 = new Step("step_redon_23", "step1", 42.6286961450387,
				0.78332195924304, 0, 10, 23, t);		
		Step s24 = new Step("step_redon_24", "step1", 42.6289801160601,
				0.783028013756848, 0, 10, 24, t, null);
		Step s25 = new Step("step_redon_25", "step1", 42.6290598024207,
				0.783288400853053, 0, 10, 25, t, null);
		Step s26 = new Step("step_redon_26", "step1", 42.6288466394908,
				0.783497322408948, 0, 10, 26, t, null);
		Step s27 = new Step("step_redon_27", "step1", 42.6290849906945,
				0.784266906193807, 0, 10, 27, t, null);
		Step s28 = new Step("step_redon_28", "step1", 42.6295709295664,
				0.784949717635755, 0, 10, 28, t, null);
		Step s29 = new Step("step_redon_29", "step1", 42.6294109930313,
				0.785815174676827, 0, 10, 29, t);		
		Step s30 = new Step("step_redon_30", "step1", 42.6298632752878,
				0.786870443844236, 0, 10, 30, t, null);
		Step s31 = new Step("step_redon_31", "step1", 42.6300761310995,
				0.786732152919285, 0, 10, 31, t);		
		Step s32 = new Step("step_redon_32", "step1", 42.6304034411442,
				0.787075782791362, 0, 10, 32, t, null);
		Step s33 = new Step("step_redon_33", "step1", 42.6305053631222,
				0.786702730474644, 0, 10, 33, t, null);
		Step s34 = new Step("step_redon_34", "step1", 42.6307556893153,
				0.786726723672473, 0, 10, 34, t, null);
		Step s35 = new Step("step_redon_35", "step1", 42.6308261470695,
				0.786639578072936, 0, 10, 35, t, null);
		Step s36 = new Step("step_redon_36", "step1", 42.6311142239138,
				0.786989605039707, 0, 10, 36, t, null);
		Step s37 = new Step("step_redon_37", "step1", 42.6312904023507,
				0.78676936464035, 0, 10, 37, t);		
		Step s38 = new Step("step_redon_38", "step1", 42.6312485157541,
				0.785862178818206, 0, 10, 38, t, null);
		Step s39 = new Step("step_redon_39", "step1", 42.6317794196075,
				0.784029416754493, 0, 10, 39, t);		
		Step s40 = new Step("step_redon_40", "step1", 42.6322480747331,
				0.782761523751105, 0, 10, 40, t, null);
		Step s41 = new Step("step_redon_41", "step1", 42.6324657095392,
				0.782338840908778, 0, 10, 41, t, null);
		Step s42 = new Step("step_redon_42", "step1", 42.6327575651158,
				0.781457874727494, 0, 10, 42, t);		
		Step s43 = new Step("step_redon_43", "step1", 42.6339509523314,
				0.780352731844687, 0, 10, 43, t, null);
		Step s44 = new Step("step_redon_44", "step1", 42.634951327891,
				0.779336649138713, 0, 10, 44, t, null);
		Step s45 = new Step("step_redon_45", "step1", 42.635587315599,
				0.778691564119072, 0, 10, 45, t, null);
		Step s46 = new Step("step_redon_46", "step1", 42.6360381107224,
				0.778628872722038, 0, 10, 46, t, null);
		Step s47 = new Step("step_redon_47", "step1", 42.6363437705731,
				0.778909421569551, 0, 10, 47, t, null);
		Step s48 = new Step("step_redon_48", "step1", 42.6371684800233,
				0.778816999300034, 0, 10, 48, t, null);
		Step s49 = new Step("step_redon_49", "step1", 42.6376781829003,
				0.778901685334858, 0, 10, 49, t, null);
		Step s50 = new Step("step_redon_50", "step1", 42.6384718382178,
				0.779002474446315, 0, 10, 50, t, null);
		Step s51 = new Step("step_redon_51", "step1", 42.6384369616426,
				0.779003202624153, 0, 10, 51, t);		
		// Ecosystem steps
		Step s52 = new Step("step_redon_eco1", "step1", 42.63831295204,
				0.779336675659579, 0, 10, 52, t, null);		
		Step s53 = new Step("step_redon_eco2", "step2", 42.6388210872392,
				0.781418193455465, 0, 10, 53, t, null);		
		Step s54 = new Step("step_redon_eco3", "step3", 42.6400928970224,
				0.782741571056249, 0, 10, 54, t, null);		
		Step s55 = new Step("step_redon_eco4", "step4", 42.6418097433816,
				0.780948969604651, 0, 10, 55, t, null);		

		try {
			stepDataDao.create(s1);
			stepDataDao.create(s2);
			stepDataDao.create(s3);
			stepDataDao.create(s4);
			stepDataDao.create(s5);
			stepDataDao.create(s6);
			stepDataDao.create(s7);
			stepDataDao.create(s8);
			stepDataDao.create(s9);
			stepDataDao.create(s10);
			stepDataDao.create(s11);
			stepDataDao.create(s12);
			stepDataDao.create(s13);
			stepDataDao.create(s14);
			stepDataDao.create(s15);
			stepDataDao.create(s16);
			stepDataDao.create(s17);
			stepDataDao.create(s18);
			stepDataDao.create(s19);
			stepDataDao.create(s20);
			stepDataDao.create(s21);
			stepDataDao.create(s22);
			stepDataDao.create(s23);
			stepDataDao.create(s24);
			stepDataDao.create(s25);
			stepDataDao.create(s26);
			stepDataDao.create(s27);
			stepDataDao.create(s28);
			stepDataDao.create(s29);
			stepDataDao.create(s30);
			stepDataDao.create(s31);
			stepDataDao.create(s32);
			stepDataDao.create(s33);
			stepDataDao.create(s34);
			stepDataDao.create(s35);
			stepDataDao.create(s36);
			stepDataDao.create(s37);
			stepDataDao.create(s38);
			stepDataDao.create(s39);
			stepDataDao.create(s40);
			stepDataDao.create(s41);
			stepDataDao.create(s42);
			stepDataDao.create(s43);
			stepDataDao.create(s44);
			stepDataDao.create(s45);
			stepDataDao.create(s46);
			stepDataDao.create(s47);
			stepDataDao.create(s48);
			stepDataDao.create(s49);
			stepDataDao.create(s50);
			stepDataDao.create(s51);
			stepDataDao.create(s52);
			stepDataDao.create(s53);
			stepDataDao.create(s54);
			stepDataDao.create(s55);
		} catch (RuntimeException ex) {
			Log.e("Inserting step", "Insert error " + ex.toString());
		}
		
		/**
		 * Material html
		 */
		
		Reference r1 = new Reference("ref_er_01");
		r1.setName("poi1_er.html");
		r1.setTextContent("poi1_er.html");

		Reference r2 = new Reference("ref_er_02");
		r2.setName("poi2_er.html");
		r2.setTextContent("poi2_er.html");

		Reference r3 = new Reference("ref_er_03");
		r3.setName("transv1_er.html");
		r3.setTextContent("transv1_er.html");

		Reference r4 = new Reference("ref_er_04");
		r4.setName("poi3_er.html");
		r4.setTextContent("poi3_er.html");

		Reference r5 = new Reference("ref_er_05");
		r5.setName("poi4_er.html");
		r5.setTextContent("poi4_er.html");

		Reference r6 = new Reference("ref_er_06");
		r6.setName("fisics_er.html");
		r6.setTextContent("fisics_er.html");

		try {
			referenceDataDao.create(r1);			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
		try {
			referenceDataDao.create(r2);			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
		try {
			referenceDataDao.create(r3);			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
		try {
			referenceDataDao.create(r4);			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
		try {
			referenceDataDao.create(r5);			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
		try {
			referenceDataDao.create(r6);			
		} catch (RuntimeException ex) {
			Log.e("Inserting reference", "Insert error " + ex.toString());
		}
				
		// Interactive image
		// Create image
		InteractiveImage img = new InteractiveImage("intimg_redon");
		img.setMediaPath("redon_panorama.jpg");
		img.setOriginalWidth(1500);
		img.setOriginalHeight(479);
		try {
			interactiveImageDataDao.create(img);
		} catch (Exception ex) {
			Log.e("Inserting interact img", "Insert error " + ex.toString());
		}

		// Create boxes
		Box tuc = new Box("b_redon_tuc", 132, 42, 178, 78, img);
		// ORANGE
		// Box tuc = new Box("b_redon_tuc",Color.argb(255, 255, 127, 0) ,img);
		tuc.setMessage("Tuc deth Pòrt de Vielha, 2605m");
		Box tartera = new Box("b_redon_tartera", 113, 106, 155, 137, img);
		// RED
		// Box tartera = new Box("b_redon_tartera",Color.argb(255, 255, 0, 0)
		// ,img);
		tartera.setMessage("TARTERA: és una extensió de roca fragmentada. La fragmentació augmenta la "
				+ "superfície de roca exposada a l'intemperie. Per tant, facilita la dissolució de les sals minerals de les "
				+ "roques, que van a parar a l'aigua de l'estany. Les tarteres ocupen un 36% de la conca del Redon.");
		Box serra = new Box("b_redon_serra", 598, 43, 629, 64, img);
		// YELLOW
		// Box serra = new Box("b_redon_serra",Color.argb(255, 255, 255,
		// 0),img);
		serra.setMessage("Serra de Fontfreda");
		Box prats = new Box("b_redon_prats", 937, 162, 971, 190, img);
		// GREEN
		// Box prats = new Box("b_redon_prats",Color.argb(255, 0, 255, 0),img);
		prats.setMessage("PRATS ALPINS: Formats per herbes de port baix. Depenent de l'orientació dominen gramínies (Festuco) "
				+ "o ciperàcies (Corex). Poden ocupar zones amb fort pendent, on les seves arrels ajuden a fixar un sòl molt "
				+ "orgànic i poc profund, sovint de menys de 30 cm. Les plantes i microbis que viuen al sòl tenen una forta "
				+ "influència sobre l'aigua que s'escorre. Els prats ocupen el 45% de la conca del Redon.");
		Box roca = new Box("b_redon_roca", 1189, 155, 1227, 187, img);
		// BLUE
		// Box roca = new Box("b_redon_roca",Color.argb(255, 0, 0, 255),img);
		roca.setMessage("ROCA EXPOSADA: La roca mare de la conca queda en superfície en afloraments i escarpaments. "
				+ "L'aigua circula ràpidament i el temps de contacte en aquestes zones és curt. Les zones de roca nua ocupen el "
				+ "19% de la conca del Redon.");
		Box sarra = new Box("b_redon_sarra", 1309, 24, 1345, 53, img);
		// MAGENTA
		// Box sarra = new Box("b_redon_sarra",Color.argb(255, 75, 0, 130),img);
		sarra.setMessage("Tuc de Sarrahèra, 2630m");

		try {
			boxDataDao.create(tuc);
			boxDataDao.create(tartera);
			boxDataDao.create(serra);
			boxDataDao.create(prats);
			boxDataDao.create(roca);
			boxDataDao.create(sarra);
		} catch (Exception ex) {
			Log.e("Inserting box", "Insert error " + ex.toString());
		}

		
		
		/**
		 * Punts d'interès
		 */
		HighLight h1 = new HighLight(
				"hl_redon_wp01",
				"WP01 - Espitau de Vielha (sortida)",
				"Des del pàrquing de l’Espitau de Vielha, passar pel carrer que formen "
						+ "el complex d’edificacions de l’Espitau per trobar al fons un corriol que puja "
						+ "fins a a la pista de Conangles (0.2 km, desnivell 60 m)",
				10, HighLight.WAYPOINT,
				s1);

		HighLight h2 = new HighLight(
				"hl_redon_wp02",
				"WP02 - Pista de Conangles",
				"Seguir la pista cap a la dreta, fins arribar a una bifurcació abans del pont "
						+ "que travessa el riu de Conangles (0.7 km, desnivell 40 m)",
				10, HighLight.WAYPOINT,
				s2);

		HighLight h3 = new HighLight(
				"hl_redon_wp03",
				"WP03 - Bifurcació",
				"Pendre el ramal de l’esquerra fins arribar al final on hi ha un pas de pedres "
						+ "per travessar el barranc del Redon (0.2 km, desnivell -10 m)",
				10, HighLight.WAYPOINT,
				s9);

		HighLight h4 = new HighLight(
				"hl_redon_wp04",
				"WP04 - Pas del barranc del Redon",
				"Travessar el riu i seguir el corriol balisat amb les marques blanques i "
						+ "vermelles del GR-11, que travessa una fageda que més adalt del camí es converteix "
						+ "en un bosc de pins més obert, fins arribar a un ressalt empinat on el camí fa "
						+ "diverses marrades (0.75 km, desnivell 90 m)", 10,
				HighLight.WAYPOINT,
				s10);

		HighLight h5 = new HighLight(
				"hl_redon_wp05",
				"WP05 - Ressalt",
				"Pujar el resalt, travessar el pletiu que s’obre al cap d’amunt i pujar suaument "
						+ "en direcció al barranc que queda al davant per anar a trobar el pas per creuar-ho "
						+ "(0.2 km, desnivell 40 m)", 10, 
						HighLight.WAYPOINT,
						s23);

		HighLight h6 = new HighLight(
				"hl_redon_wp06",
				"WP06 - Pas del barranc",
				"Travessar el rierol i pujar cap a l’esquerra i pendent pronunciada, en direcció "
						+ "a un vell pi solitari on comencen els Marrècs (marrades) de l’Escaleta "
						+ "( 0.1 km, desnivell 50 m)", 10, HighLight.WAYPOINT, s29);

		HighLight h7 = new HighLight(
				"hl_redon_wp07",
				"WP07 - Marrècs de l’Escaleta",
				"Seguir el corriol que puja amb pendent forta fent diverses marrades, per arribar "
						+ "al punt on torna a travessar el barranc més amunt, ja convertit en una canal molt "
						+ "dreta (0.3 km, desnivell 100 m)", 10,
				HighLight.WAYPOINT, s31);

		HighLight h8 = new HighLight(
				"hl_redon_wp08",
				"WP08 - Pas de la canal",
				"Travessar la canal, sortir per un pas amb una petita grimpada sense dificultat i "
						+ "continuar pel corriol que uns pocs metres més amunt flanqueja unes amples pales "
						+ "herboses, fins arribar on el GR-11 es desvia cap a la dreta per anar al "
						+ "Pòrt de Rius (0.3 km, desnivell 20 m) ", 10,
				HighLight.WAYPOINT,s37);

		HighLight h9 = new HighLight(
				"hl_redon_wp09",
				"WP09 - desviament GR11",
				"Deixar el desviament a la dreta i continuar en la mateixa direcció que portem, "
						+ "tot continuant el llarg flanqueig, fins arribar a un ressalt de roca que queda per "
						+ "sobre del camí (0.2 km, desnivell 40 m)", 10,
				HighLight.WAYPOINT,s39);

		HighLight h10 = new HighLight(
				"hl_redon_wp10",
				"WP10 - ressalt de roca",
				"Passar per sota del resalt, però de seguida pujar pel corriol que s’enfila cap "
						+ "a la dreta, per continuar pel camí que va es va apropant al barranc del Redon en un "
						+ "llarg flanqueig, fins a trobar-ho i resseguir-ho fins arribar a l’estany "
						+ "(0.7 km, desnivell 100 m)", 10, HighLight.WAYPOINT,s42);

		HighLight h11 = new HighLight("hl_redon_wp11", "WP11 - Arribada", null,
				10, HighLight.INTERACTIVE_IMAGE,s51);
		h11.setInteractiveImage(img);

		HighLight h12 = new HighLight("hl_redon_poi01",
				"POI1 - Estació limnològica", "Fes clic per detalls...", 10,
				HighLight.REFERENCE,s52);
		h12.setReference(r1);

		HighLight h13 = new HighLight("hl_redon_poi02", "POI2 - Volantins",
				"Fes clic per detalls...", 10,
				HighLight.REFERENCE,s53);
		h13.setReference(r2);

		HighLight h14 = new HighLight("hl_redon_poi03", "POI3 - Peixos",
				"Fes clic per detalls...", 10,
				HighLight.REFERENCE,s54);
		h14.setReference(r4);

		HighLight h15 = new HighLight("hl_redon_poi04", "POI4 - Capbussada",
				"Fes clic per detalls...", 10,
				HighLight.REFERENCE,s55);
		h15.setReference(r5);

		try {

			hlDataDao.create(h1);
			hlDataDao.create(h2);
			hlDataDao.create(h3);
			hlDataDao.create(h4);
			hlDataDao.create(h5);
			hlDataDao.create(h6);
			hlDataDao.create(h7);
			hlDataDao.create(h8);
			hlDataDao.create(h9);
			hlDataDao.create(h10);
			hlDataDao.create(h11);
			hlDataDao.create(h12);
			hlDataDao.create(h13);
			hlDataDao.create(h14);
			hlDataDao.create(h15);

		} catch (RuntimeException ex) {
			Log.e("Inserting highlight", "Insert error " + ex.toString());
		}


		Route r = new Route();
		r.setId("ROUTE_REDON");
		r.setName("Estanh Redon");
		r.setDescription("Itinerari de 3.6 km amb 600 m de desnivell (1.5 - 2 h a peu), per visitar l’Estanh Redon.\nSortida des de l’Espitau de Vielha, a la boca sud del túnel. El camí és al començament una pista forestal, i després un corriol ben fresat per on es pot caminar amb poca dificultat. Segueix inicialment la ruta GR-11, però cap a l’útim terç el camí pren una variant. L’itinerari és adequat per a persones a partir de 10 anys, acostumades a caminar per la muntanya i amb una condició física mitja. El Redon és un exemple d’estany de gran mida i aigües molt transparents, encaixonat en un circ de parets escarpades. Ha estat objecte de recerca científica des de fa dècades, i a la vora hi ha un petit laboratori de camp i una estació meteorològica.");
		r.setUserId("1");
		// Ph_ch parameters
		r.setReference(r6);
		r.setTrack(t);
		r.setLocalCarto("OSMPublicTransport_HiRes.mbtiles");

		try {
			routeDataDao.create(r);
		} catch (RuntimeException ex) {
			Log.e("Inserting route", "Insert error " + ex.toString());
		}
	}	
	
	public static void updateRoute(Route r,DataBaseHelper dataBaseHelper){
		dataBaseHelper.getRouteDataDao().update(r);
	}
	
	public static void updateHighLight(HighLight h,DataBaseHelper dataBaseHelper){
		dataBaseHelper.getHlDataDao().update(h);
	}

	public static void insertRoute(Route editedRoute,
			DataBaseHelper dataBaseHelper, String android_id) {
		// Save track
		// Track t = new Track();
		if (editedRoute.getTrack() != null) {
			Track t = editedRoute.getTrack();
			editedRoute.getTrack().setId(
					DataContainer.getTrackId(dataBaseHelper, android_id));			
			Log.d("insertRoute", "Adding track - id " + t.getId());			
			try {
				dataBaseHelper.getTrackDataDao().create(t);
			} catch (RuntimeException ex) {
				Log.e("Inserting track", "Insert error " + ex.toString());
			}

			if (t.getSteps() != null) {
				List<Step> currentSteps = (List<Step>) t.getSteps();
				for (int i = 0; i < currentSteps.size(); i++) {
					Step s = currentSteps.get(i);
					s.setId(DataContainer.getStepId(dataBaseHelper, android_id));
					s.setTrack(t);
//					if (s.getHighlight() != null) {
//						HighLight h = s.getHighlight();
//						h.setId(DataContainer.getHighLightId(dataBaseHelper,
//								android_id));						
//							Log.d("insertRoute",
//									"Adding highlight - id " + h.getId());						
//						try {
//							dataBaseHelper.getHlDataDao().create(h);
//						} catch (RuntimeException ex) {
//							Log.e("Inserting step",
//									"Insert error " + ex.toString());
//						}
//					}
					if(s.getHighlights()!=null){
						for( HighLight h : s.getHighlights() ){
							h.setId(DataContainer.getHighLightId(dataBaseHelper, android_id));
							s.getHighlights().add(h);
							h.setStep(s);
							Log.d("insertRoute", "Adding highlight - id " + h.getId());
							try{
								dataBaseHelper.getHlDataDao().create(h);
							}catch (RuntimeException ex){
								Log.e("Inserting step",
								"Insert error " + ex.toString());
							}
						}
					}
					Log.d("insertRoute", "Adding step - id " + s.getId()
							+ " order " + s.getOrder());					
					try {
						dataBaseHelper.getStepDataDao().create(s);
					} catch (RuntimeException ex) {
						Log.e("Inserting step", "Insert error " + ex.toString());
					}
				}
			}
		}

		editedRoute.setId(DataContainer.getRouteId(dataBaseHelper, android_id));
		try {
			dataBaseHelper.getRouteDataDao().create(editedRoute);
		} catch (RuntimeException ex) {
			Log.e("Inserting route", "Insert error " + ex.toString());
		}
	}

}
