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
import net.movelab.sudeau.model.Reference;
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
	
	public static Route refreshRoute(Route r, DataBaseHelper db) {	
		db.getRouteDataDao().refresh(r);
		return r;
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
	
	public static void editRoute(Route editedRoute, DataBaseHelper db) {
		db.getRouteDataDao().update(editedRoute);
	}
	
	public static Route findRouteById(String idRoute, DataBaseHelper db){
		Route r = db.getRouteDataDao().queryForId(idRoute);
		return r;
	}
	
	public static Step findStepById(String idStep, DataBaseHelper db){
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
	
	public static List<Route> getAllRoutes(DataBaseHelper db) {		
		QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao().queryBuilder();
		Where<Route, String> where = queryBuilder.where();
		List<Route> routes = new ArrayList<Route>();
		try {			
			where.eq("ecosystem", false);
			PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
			routes = db.getRouteDataDao().query(preparedQuery);											
			return routes;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return routes;
		
	}
	
	public static Route getRouteEcosystem(Route route, DataBaseHelper db) {
		Route r = route.getEco();
		db.getRouteDataDao().refresh(r);
		return r;
	}
	
	public static Reference getReferenceStep(Step s, DataBaseHelper db){
		Reference r = s.getReference();		
		db.getReferenceDataDao().refresh(r);
		return r;		
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
	
	public static void loadRedon(DataBaseHelper db, Context context){
		
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		RuntimeExceptionDao<EruMedia, String> mediaDataDao = db.getMediaDataDao();
		
		Route ecosystem = loadSampleEcosystem(db, context);
		
		Track t = new Track("TRACK_REDON","Waypoints recorregut Estanh Redon");			
		try{
			trackDataDao.create(t);			
		}catch(RuntimeException ex){
			Log.e("Inserting track","Insert error " + ex.toString());
		}
		
		/**
		 *  Imatges 
		 *  
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
		
		**/
		
		/**
		 * Punts d'inter�s
		 */ 		 		
		HighLight h1 = new HighLight(
				"hl_redon_wp01",
				"WP01 - Espitau de Vielha (sortida)",
				"Des del p�rquing de l�Espitau de Vielha, passar pel carrer que formen " + 
				"el complex d�edificacions de l�Espitau per trobar al fons un corriol que puja " + 
				"fins a a la pista de Conangles (0.2 km, desnivell 60 m)",
				10,
				null);		
		HighLight h2 = new HighLight(
				"hl_redon_wp02",
				"WP02 - Pista de Conangles",
				"Seguir la pista cap a la dreta, fins arribar a una bifurcaci� abans del pont " + 
				"que travessa el riu de Conangles (0.7 km, desnivell 40 m)",
				10,
				null);
		HighLight h3 = new HighLight(
				"hl_redon_wp03",
				"WP03 - Bifurcaci�",
				"Pendre el ramal de l�esquerra fins arribar al final on hi ha un pas de pedres " + 
				"per travessar el barranc del Redon (0.2 km, desnivell -10 m)",
				10,
				null);		
		HighLight h4 = new HighLight(
				"hl_redon_wp04",
				"WP04 - Pas del barranc del Redon",
				"Travessar el riu i seguir el corriol balisat amb les marques blanques i " +
				"vermelles del GR-11, que travessa una fageda que m�s adalt del cam� es converteix " + 
				"en un bosc de pins m�s obert, fins arribar a un ressalt empinat on el cam� fa " + 
				"diverses marrades (0.75 km, desnivell 90 m)",
				10,
				null);
		HighLight h5 = new HighLight(
				"hl_redon_wp05",
				"WP05 - Ressalt",
				"Pujar el resalt, travessar el pletiu que s�obre al cap d�amunt i pujar suaument "
				+ "en direcci� al barranc que queda al davant per anar a trobar el pas per creuar-ho " +
				"(0.2 km, desnivell 40 m)",
				10,
				null);
		
		HighLight h6 = new HighLight(
				"hl_redon_wp06",
				"WP06 - Pas del barranc",
				"Travessar el rierol i pujar cap a l�esquerra i pendent pronunciada, en direcci� "
				+ "a un vell pi solitari on comencen els Marr�cs (marrades) de l�Escaleta " +
				"( 0.1 km, desnivell 50 m)",
				10,
				null);
		HighLight h7 = new HighLight(
				"hl_redon_wp07",
				"WP07 - Marr�cs de l�Escaleta",
				"Seguir el corriol que puja amb pendent forta fent diverses marrades, per arribar " +
				"al punt on torna a travessar el barranc m�s amunt, ja convertit en una canal molt " + 
				"dreta (0.3 km, desnivell 100 m)",
				10,
				null);
		HighLight h8 = new HighLight(
				"hl_redon_wp08",
				"WP08 - Pas de la canal",
				"Travessar la canal, sortir per un pas amb una petita grimpada sense dificultat i " +
				"continuar pel corriol que uns pocs metres m�s amunt flanqueja unes amples pales " +
				"herboses, fins arribar on el GR-11 es desvia cap a la dreta per anar al " + 
				"P�rt de Rius (0.3 km, desnivell 20 m) ",
				10,
				null);
		
		HighLight h9 = new HighLight(
				"hl_redon_wp09",
				"WP09 - desviament GR11",
				"Deixar el desviament a la dreta i continuar en la mateixa direcci� que portem, " +
				"tot continuant el llarg flanqueig, fins arribar a un ressalt de roca que queda per " +
				"sobre del cam� (0.2 km, desnivell 40 m)",
				10,
				null);		
		HighLight h10 = new HighLight(
				"hl_redon_wp10",
				"WP10 - ressalt de roca",
				"Passar per sota del resalt, per� de seguida pujar pel corriol que s�enfila cap " + 
				"a la dreta, per continuar pel cam� que va es va apropant al barranc del Redon en un " + 
				"llarg flanqueig, fins a trobar-ho i resseguir-ho fins arribar a l�estany " + 
				"(0.7 km, desnivell 100 m)",
				10,
				null);
		HighLight h11 = new HighLight(
				"hl_redon_wp11",
				"WP11 - Arribada",
				null,
				10,
				null);
		try{
			
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
			
		}catch(RuntimeException ex){
			Log.e("Inserting preference","Insert error " + ex.toString());
		}		
		
		//Sortida
		Step s1 = new Step("step_redon_1", "step1", 42.6271286833071,0.763500210996426, 0, 10, 1, t, h1);
		Step s2 = new Step("step_redon_2", "step1", 42.6275014082057,0.765600470040226, 0, 10, 2, t, h2);
		Step s3 = new Step("step_redon_3", "step1", 42.6274169991666,0.76702904625563, 0, 10, 3, t, null);
		Step s4 = new Step("step_redon_4", "step1", 42.6277910435601,0.768183665859397, 0, 10, 4, t, null);
		Step s5 = new Step("step_redon_5", "step1", 42.6272820099257,0.769414374849475, 0, 10, 5, t, null);
		Step s6 = new Step("step_redon_6", "step1", 42.6274246175308,0.770694949860626, 0, 10, 6, t, null);
		Step s7 = new Step("step_redon_7", "step1", 42.6277790705353,0.771784410153487, 0, 10, 7, t, null);
		Step s8 = new Step("step_redon_8", "step1", 42.6281256982475,0.772518923367898, 0, 10, 8, t, null);
		Step s9 = new Step("step_redon_9", "step1", 42.6283586202044,0.773143964179326, 0, 10, 9, t, h3);
		Step s10 = new Step("step_redon_10", "step1", 42.6285187747999,0.773703827508143, 0, 10, 10, t, h4);
		Step s11 = new Step("step_redon_11", "step1", 42.6289143764006,0.774921917764004, 0, 10, 11, t, null);
		Step s12 = new Step("step_redon_12", "step1", 42.6293034630362,0.775732826296007, 0, 10, 12, t, null);
		Step s13 = new Step("step_redon_13", "step1", 42.629195381669 ,0.776484281595913, 0, 10, 13, t, null);
		Step s14 = new Step("step_redon_14", "step1", 42.6287473460252,0.777377578342566, 0, 10, 14, t, null);
		Step s15 = new Step("step_redon_15", "step1", 42.6286806404823,0.778269812290091, 0, 10, 15, t, null);
		Step s16 = new Step("step_redon_16", "step1", 42.6288146821753,0.779466030362528, 0, 10, 16, t, null);
		Step s17 = new Step("step_redon_17", "step1", 42.6288213903172,0.779793056208291, 0, 10, 17, t, null);
		Step s18 = new Step("step_redon_18", "step1", 42.6287648668222,0.780519604486472, 0, 10, 18, t, null);
		Step s19 = new Step("step_redon_19", "step1", 42.6281997327169,0.781384989168146, 0, 10, 19, t, null);
		Step s20 = new Step("step_redon_20", "step1", 42.6280008714093,0.78193631637987, 0, 10, 20, t, null);
		Step s21 = new Step("step_redon_21", "step1", 42.6279912551399,0.782715891327825, 0, 10, 21, t, null);
		Step s22 = new Step("step_redon_22", "step1", 42.6283277355105,0.783183607418323, 0, 10, 22, t, null);
		Step s23 = new Step("step_redon_23", "step1", 42.6286961450387,0.78332195924304, 0, 10, 23, t, h5);
		Step s24 = new Step("step_redon_24", "step1", 42.6289801160601,0.783028013756848, 0, 10, 24, t, null);
		Step s25 = new Step("step_redon_25", "step1", 42.6290598024207,0.783288400853053, 0, 10, 25, t, null);
		Step s26 = new Step("step_redon_26", "step1", 42.6288466394908,0.783497322408948, 0, 10, 26, t, null);
		Step s27 = new Step("step_redon_27", "step1", 42.6290849906945,0.784266906193807, 0, 10, 27, t, null);
		Step s28 = new Step("step_redon_28", "step1", 42.6295709295664,0.784949717635755, 0, 10, 28, t, null);
		Step s29 = new Step("step_redon_29", "step1", 42.6294109930313,0.785815174676827, 0, 10, 29, t, h6);
		Step s30 = new Step("step_redon_30", "step1", 42.6298632752878,0.786870443844236, 0, 10, 30, t, null);
		Step s31 = new Step("step_redon_31", "step1", 42.6300761310995,0.786732152919285, 0, 10, 31, t, h7);
		Step s32 = new Step("step_redon_32", "step1", 42.6304034411442,0.787075782791362, 0, 10, 32, t, null);
		Step s33 = new Step("step_redon_33", "step1", 42.6305053631222,0.786702730474644, 0, 10, 33, t, null);
		Step s34 = new Step("step_redon_34", "step1", 42.6307556893153,0.786726723672473, 0, 10, 34, t, null);
		Step s35 = new Step("step_redon_35", "step1", 42.6308261470695,0.786639578072936, 0, 10, 35, t, null);
		Step s36 = new Step("step_redon_36", "step1", 42.6311142239138,0.786989605039707, 0, 10, 36, t, null);
		Step s37 = new Step("step_redon_37", "step1", 42.6312904023507,0.78676936464035, 0, 10, 37, t, h8);
		Step s38 = new Step("step_redon_38", "step1", 42.6312485157541,0.785862178818206, 0, 10, 38, t, null);
		Step s39 = new Step("step_redon_39", "step1", 42.6317794196075,0.784029416754493, 0, 10, 39, t, h9);
		Step s40 = new Step("step_redon_40", "step1", 42.6322480747331,0.782761523751105, 0, 10, 40, t, null);
		Step s41 = new Step("step_redon_41", "step1", 42.6324657095392,0.782338840908778, 0, 10, 41, t, null);
		Step s42 = new Step("step_redon_42", "step1", 42.6327575651158,0.781457874727494, 0, 10, 42, t, h10);
		Step s43 = new Step("step_redon_43", "step1", 42.6339509523314,0.780352731844687, 0, 10, 43, t, null);
		Step s44 = new Step("step_redon_44", "step1", 42.634951327891 ,0.779336649138713, 0, 10, 44, t, null);
		Step s45 = new Step("step_redon_45", "step1", 42.635587315599 ,0.778691564119072, 0, 10, 45, t, null);
		Step s46 = new Step("step_redon_46", "step1", 42.6360381107224,0.778628872722038, 0, 10, 46, t, null);
		Step s47 = new Step("step_redon_47", "step1", 42.6363437705731,0.778909421569551, 0, 10, 47, t, null);
		Step s48 = new Step("step_redon_48", "step1", 42.6371684800233,0.778816999300034, 0, 10, 48, t, null);
		Step s49 = new Step("step_redon_49", "step1", 42.6376781829003,0.778901685334858, 0, 10, 49, t, null);
		Step s50 = new Step("step_redon_50", "step1", 42.6384718382178,0.779002474446315, 0, 10, 50, t, null);
		Step s51 = new Step("step_redon_51", "step1", 42.6384369616426,0.779003202624153, 0, 10, 51, t, h11);
		
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
		}catch(RuntimeException ex){
			Log.e("Inserting step","Insert error " + ex.toString());
		}
		
		Route r = new Route();
		r.setId("ROUTE_REDON");
		r.setName("Estanh Redon");		
		r.setDescription("Itinerari de 3.6 km amb 600 m de desnivell (1.5 - 2 h a peu), per visitar l�Estanh Redon.\nSortida des de l�Espitau de Vielha, a la boca sud del t�nel. El cam� �s al comen�ament una pista forestal, i despr�s un corriol ben fresat per on es pot caminar amb poca dificultat. Segueix inicialment la ruta GR-11, per� cap a l��tim ter� el cam� pren una variant. L�itinerari �s adequat per a persones a partir de 10 anys, acostumades a caminar per la muntanya i amb una condici� f�sica mitja. El Redon �s un exemple d�estany de gran mida i aig�es molt transparents, encaixonat en un circ de parets escarpades. Ha estat objecte de recerca cient�fica des de fa d�cades, i a la vora hi ha un petit laboratori de camp i una estaci� meteorol�gica.");
		r.setUserId("1");
		r.setEco(ecosystem);		
		r.setTrack(t);
		
		try{
			routeDataDao.create(r);			
		}catch(RuntimeException ex){
			Log.e("Inserting route","Insert error " + ex.toString());
		}
		
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
		//HighLight h2 = new HighLight("hl2","hl2",200d,m1);
		try{
			hlDataDao.create(h1);
			//hlDataDao.create(h2);
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
		Step s12 = new Step("step12", "step1", 42.6360d, 0.7716, 0d, 10d, 1, t, null);
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
	
	public static Route loadSampleEcosystem(DataBaseHelper db, Context context){
		
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		RuntimeExceptionDao<EruMedia, String> mediaDataDao = db.getMediaDataDao();
		RuntimeExceptionDao<Reference, String> referenceDataDao = db.getReferenceDataDao();
		
		Track t = new Track("TRACK1_ECO","Lac Redon POI Ecosystem track");			
		try{
			trackDataDao.create(t);			
		}catch(RuntimeException ex){
			Log.e("Inserting track","Insert error " + ex.toString());
		}
								
//		byte[] image1 = null;
//		try {
//			image1 = Util.readFile("no_picture.png", context);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		EruMedia m1 = new EruMedia("MEDIA1", "A picture", image1);
//		
//		try{
//			mediaDataDao.create(m1);
//		}catch(RuntimeException ex){
//			Log.e("Inserting media","Insert error " + ex.toString());
//		}
		
		HighLight h1 = new HighLight(
				"hl_redon_poi01",
				"POI1 - Estaci� limnol�gica",
				"Fes clic per detalls...",
				10,
				null);
		
		HighLight h2 = new HighLight(
				"hl_redon_poi02",
				"POI2 - Volantins",
				"Fes clic per detalls...",
				10,
				null);
		
		HighLight h3 = new HighLight(
				"hl_redon_poi03",
				"POI3 - Peixos",
				"Fes clic per detalls...",
				10,
				null);
		
		HighLight h4 = new HighLight(
				"hl_redon_poi04",
				"POI4 - Capbussada",
				"Fes clic per detalls...",
				10,
				null);
				
		try{
			
			hlDataDao.create(h1);
			hlDataDao.create(h2);
			hlDataDao.create(h3);
			hlDataDao.create(h4);
			
		}catch(RuntimeException ex){
			Log.e("Inserting highlight","Insert error " + ex.toString());
		}
		
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
				
		try{
			
			referenceDataDao.create(r1);
			referenceDataDao.create(r2);
			referenceDataDao.create(r3);
			referenceDataDao.create(r4);
			referenceDataDao.create(r5);
			referenceDataDao.create(r6);
			
		}catch(RuntimeException ex){
			Log.e("Inserting reference","Insert error " + ex.toString());
		}
		
		
		Step s1 = new Step("step_redon_eco1", "step1", 42.63831295204,0.779336675659579, 0, 10, 1, t, h1, r1);
		Step s2 = new Step("step_redon_eco2", "step2", 42.6388210872392,0.781418193455465, 0, 10, 1, t, h2, r2);
		Step s3 = new Step("step_redon_eco3", "step3", 42.6400928970224,0.782741571056249, 0, 10, 1, t, h3, r4);
		Step s4 = new Step("step_redon_eco4", "step4", 42.6418097433816,0.780948969604651, 0, 10, 1, t, h4, r5);				
		
		try{
			
			stepDataDao.create(s1);
			stepDataDao.create(s2);
			stepDataDao.create(s3);
			stepDataDao.create(s4);	
			
		}catch(RuntimeException ex){
			Log.e("Inserting step","Insert error " + ex.toString());
		}
		
		Route r = new Route();
		r.setId("ROUTE2_ECO");
		r.setName("Estanh Redon");
		r.setDescription("Ecosistemes aq��tics de l'Estanh Redon");
		r.setUserId("1");
		r.setEcosystem(true);
		r.setTrack(t);
		r.setReference(r6);
		
		try{
			routeDataDao.create(r);			
		}catch(RuntimeException ex){
			Log.e("Inserting route","Insert error " + ex.toString());
		}
		
		return r;
	}
	

}
