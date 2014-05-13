package net.movelab.sudeau.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.movelab.sudeau.IGlobalValues;
import net.movelab.sudeau.Util;
import net.movelab.sudeau.model.EruMedia;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import android.content.Context;
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
	public static String getRouteId(DataBaseHelper db, String userId){
		QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao().queryBuilder();
		Where<Route, String> where = queryBuilder.where();
		String retVal=null;
		try {
			where.eq("userId", userId);
			PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
			List<Route> userRoutes = db.getRouteDataDao().query(preparedQuery);			
			if(userRoutes != null){
				int c = userRoutes.size() + 1;
				retVal = "R_" + userId + "_" + c; 				
			}else{
				retVal = "R_" + userId + "_1";
			}
			if(IGlobalValues.DEBUG){
				Log.d("getRouteId","Returning route id" + retVal);
			}
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return retVal;
	}
	
	/**
	 * Incremental unique track id
	 * 
	 * @param db
	 * @param userId
	 * @return
	 */
	public static String getTrackId(DataBaseHelper db, String userId){
		QueryBuilder<Track, String> queryBuilder = db.getTrackDataDao().queryBuilder();
		Where<Track, String> where = queryBuilder.where();
		String retVal=null;
		try {			
			where.like("id", "%" + userId + "%");
			PreparedQuery<Track> preparedQuery = queryBuilder.prepare();
			List<Track> trackRoutes = db.getTrackDataDao().query(preparedQuery);			
			if(trackRoutes != null){
				int c = trackRoutes.size() + 1;
				retVal = "T_" + userId + "_" + c;
			}else{
				retVal = "T_" + userId + "_1";
			}
			if(IGlobalValues.DEBUG){
				Log.d("getTrackId","Returning track id" + retVal);
			}			
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return retVal;
	}
	
	public static String getStepId(DataBaseHelper db, String userId){
		QueryBuilder<Step, String> queryBuilder = db.getStepDataDao().queryBuilder();
		Where<Step, String> where = queryBuilder.where();
		String retVal=null;
		try {			
			where.like("id", "%" + userId + "%");
			PreparedQuery<Step> preparedQuery = queryBuilder.prepare();
			List<Step> userSteps = db.getStepDataDao().query(preparedQuery);			
			if(userSteps != null){
				int c = userSteps.size() + 1;
				retVal = "S_" + userId + "_" + c;
			}else{
				retVal = "S_" + userId + "_1";
			}
			if(IGlobalValues.DEBUG){
				Log.d("getTrackId","Returning step id" + retVal);
			}			
			return retVal;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return retVal;
	}
	
	public static Route findRouteById(String idRoute, DataBaseHelper db){
		Route r = db.getRouteDataDao().queryForId(idRoute);
		return r;
	}
		
	public static List<Track> getAllTracks(DataBaseHelper db) {
		List<Track> tracks = db.getTrackDataDao().queryForAll();
		return tracks;
	}
	
	public static List<Route> getAllRoutes(DataBaseHelper db) {
		List<Route> routes = db.getRouteDataDao().queryForAll();
		return routes;
	}
	
	public static Step getRouteStarter(Route route, DataBaseHelper db) {
		Track t = route.getTrack();
		db.getTrackDataDao().refresh(t);
		List<Step> steps = getTrackSteps(t, db);
		if(steps!=null && steps.size() > 0){
			return steps.get(0);
		}
		return null;
	}
	
	public static List<Step> getRouteSteps(Route route, DataBaseHelper db){
		Track t = route.getTrack();
		db.getTrackDataDao().refresh(t);
		return getTrackSteps(t, db);
	}
	
	public static List<Step> getTrackSteps(Track track, DataBaseHelper db) {
		db.getTrackDataDao().refresh(track);
		ArrayList<Step> retVal = new ArrayList<Step>();
		Iterator<Step> stepIt = track.getSteps().iterator();
		while(stepIt.hasNext()){
			Step s = stepIt.next();
			if(s.getHighlight()!=null){
				db.getHlDataDao().refresh(s.getHighlight());
			}
			retVal.add(s);
		}
		Collections.sort(retVal);
		return retVal;
	}
	
	public static byte[] getStepMedia(Step s, DataBaseHelper db){		
		if(s!=null && s.getHighlight()!=null){
			HighLight hl = s.getHighlight();
			db.getHlDataDao().refresh(hl);
			if(hl.getMedia()!=null){
				EruMedia em = hl.getMedia();
				db.getMediaDataDao().refresh(em);
				return em.getImage();
			}
		}
		return null;
	}
	
	public static void loadSampleData(DataBaseHelper db, Context context){
		
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		RuntimeExceptionDao<EruMedia, String> mediaDataDao = db.getMediaDataDao();
				
		Track t = new Track("TRACK1","A track");			
		try{
			trackDataDao.create(t);			
		}catch(RuntimeException ex){
			Log.e("Inserting track","Insert error " + ex.toString());
		}
								
		byte[] image1 = null;
		try {
			image1 = Util.readFile("no_picture.png", context);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EruMedia m1 = new EruMedia("MEDIA1", "A picture", image1);
		
		try{
			mediaDataDao.create(m1);
		}catch(RuntimeException ex){
			Log.e("Inserting media","Insert error " + ex.toString());
		}
		
		HighLight h1 = new HighLight("hl1","hl1",100d);
		HighLight h2 = new HighLight("hl2","hl2",200d,m1);
		try{
			hlDataDao.create(h1);
			hlDataDao.create(h2);
		}catch(RuntimeException ex){
			Log.e("Inserting preference","Insert error " + ex.toString());
		}
		
		Step s1 = new Step("step1", "step1", 42.6274d, 0.7633, 0d, 10d, 1, t, null);
		Step s2 = new Step("step2", "step1", 42.6281d, 0.7617, 0d, 10d, 1, t, h1);
		Step s3 = new Step("step3", "step1", 42.6292d, 0.7608, 0d, 10d, 1, t, null);
		Step s4 = new Step("step4", "step1", 42.6300d, 0.7594, 0d, 10d, 1, t, null);
		Step s5 = new Step("step5", "step1", 42.6309d, 0.7597, 0d, 10d, 1, t, null);
		Step s6 = new Step("step6", "step1", 42.6321d, 0.7609, 0d, 10d, 1, t, null);
		Step s7 = new Step("step7", "step1", 42.6334d, 0.7629, 0d, 10d, 1, t, null);
		Step s8 = new Step("step8", "step1", 42.6347d, 0.7645, 0d, 10d, 1, t, null);
		Step s9 = new Step("step9", "step1", 42.6354d, 0.7663, 0d, 10d, 1, t, null);
		Step s10 = new Step("step10", "step1", 42.6357d, 0.7684, 0d, 10d, 1, t, null);
		Step s11 = new Step("step11", "step1", 42.6357d, 0.7702, 0d, 10d, 1, t, null);
		Step s12 = new Step("step12", "step1", 42.6360d, 0.7716, 0d, 10d, 1, t, h2);
		Step s13 = new Step("step13", "step1", 42.6370d, 0.7727, 0d, 10d, 1, t, null);
		Step s14 = new Step("step14", "step1", 42.6375d, 0.7740, 0d, 10d, 1, t, null);
		Step s15 = new Step("step15", "step1", 42.6382d, 0.7750, 0d, 10d, 1, t, null);		
		
		try{			
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
		}catch(RuntimeException ex){
			Log.e("Inserting step","Insert error " + ex.toString());
		}
		
		Route r = new Route();
		r.setId("ROUTE1");
		r.setName("Estanh Redon");
		r.setDescription("Ruta a l'Estanh Redon");
		r.setUserId("1");
		r.setTrack(t);
		
		try{
			routeDataDao.create(r);			
		}catch(RuntimeException ex){
			Log.e("Inserting route","Insert error " + ex.toString());
		}
	}

}
