package net.movelab.sudeau.database;

import net.movelab.sudeau.model.HighLight;
//import net.movelab.sudeau.model.EruMedia;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {
	
	//Database Version
	public static final int DATABASE_VERSION = 93;
	// Database Name
	public static final String DATABASE_NAME = "appdata";
		
	private Dao<Route, String> routeDao;
	private RuntimeExceptionDao<Route, String> routeRuntimeDao;
	
	private Dao<Track, String> trackDao;
	private RuntimeExceptionDao<Track, String> trackRuntimeDao;
	
	private Dao<Step, String> stepDao;
	private RuntimeExceptionDao<Step, String> stepRuntimeDao;
	
	private Dao<HighLight, String> hlDao;
	private RuntimeExceptionDao<HighLight, String> hlRuntimeDao;
	
//	private Dao<EruMedia, String> mediaDao;
//	private RuntimeExceptionDao<EruMedia, String> mediaRuntimeDao;
	
	private Dao<Reference, String> referenceDao;
	private RuntimeExceptionDao<Reference, String> referenceRuntimeDao;
           
	public DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			Log.i(DataBaseHelper.class.getName(), "onCreate");			
			TableUtils.createTable(connectionSource, Track.class);
			TableUtils.createTable(connectionSource, Route.class);
			TableUtils.createTable(connectionSource, Step.class);
			TableUtils.createTable(connectionSource, HighLight.class);
			//TableUtils.createTable(connectionSource, EruMedia.class);
			TableUtils.createTable(connectionSource, Reference.class);
		} catch (java.sql.SQLException e) {
			Log.e(DataBaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, 
			int newVersion) {		
		try {
			Log.i(DataBaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, Route.class, true);
			TableUtils.dropTable(connectionSource, Track.class, true);
			TableUtils.dropTable(connectionSource, Step.class, true);
			TableUtils.dropTable(connectionSource, HighLight.class, true);
			//TableUtils.dropTable(connectionSource, EruMedia.class, true);
			TableUtils.dropTable(connectionSource, Reference.class, true);
			onCreate(db,connectionSource);
		} catch (java.sql.SQLException e) {
			Log.e(DataBaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}		
	}
	
	public Dao<Route, String> getRouteDao() throws java.sql.SQLException {
		if (routeDao == null) {
			routeDao = getDao(Route.class);
		}
		return routeDao;
	}
	
	public RuntimeExceptionDao<Route, String> getRouteDataDao() {
		if (routeRuntimeDao == null) {			
			routeRuntimeDao = getRuntimeExceptionDao(Route.class);
		}
		return routeRuntimeDao;
	}
	
	public Dao<Track, String> getTrackDao() throws java.sql.SQLException {
		if (trackDao == null) {
			trackDao = getDao(Track.class);
		}
		return trackDao;
	}
	
	public RuntimeExceptionDao<Track, String> getTrackDataDao() {
		if (trackRuntimeDao == null) {			
			trackRuntimeDao = getRuntimeExceptionDao(Track.class);
		}
		return trackRuntimeDao;
	}
	
	public Dao<Step, String> getStepDao() throws java.sql.SQLException {
		if (stepDao == null) {
			stepDao = getDao(Track.class);
		}
		return stepDao;
	}
	
	public RuntimeExceptionDao<Step, String> getStepDataDao() {
		if (stepRuntimeDao == null) {			
			stepRuntimeDao = getRuntimeExceptionDao(Step.class);
		}
		return stepRuntimeDao;
	}
	
	public Dao<HighLight, String> getHlDao() throws java.sql.SQLException {
		if (hlDao == null) {
			hlDao = getDao(HighLight.class);
		}
		return hlDao;
	}
	
	public RuntimeExceptionDao<HighLight, String> getHlDataDao() {
		if (hlRuntimeDao == null) {			
			hlRuntimeDao = getRuntimeExceptionDao(HighLight.class);
		}
		return hlRuntimeDao;
	}		
	
//	public Dao<EruMedia, String> getMediaDao() throws java.sql.SQLException {
//		if (mediaDao == null) {
//			mediaDao = getDao(EruMedia.class);
//		}
//		return mediaDao;
//	}
//	
//	public RuntimeExceptionDao<EruMedia, String> getMediaDataDao() {
//		if (mediaRuntimeDao == null) {			
//			mediaRuntimeDao = getRuntimeExceptionDao(EruMedia.class);
//		}
//		return mediaRuntimeDao;
//	}
	
	public Dao<Reference, String> getReferenceDao() throws java.sql.SQLException {
		if (referenceDao == null) {
			referenceDao = getDao(Reference.class);
		}
		return referenceDao;
	}
	
	public RuntimeExceptionDao<Reference, String> getReferenceDataDao() {
		if (referenceRuntimeDao == null) {			
			referenceRuntimeDao = getRuntimeExceptionDao(Reference.class);
		}
		return referenceRuntimeDao;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();		
		routeDao = null;
		routeRuntimeDao = null;
		trackDao = null;
		trackRuntimeDao = null;
		stepDao = null;
		stepRuntimeDao = null;
		hlDao = null;
		hlRuntimeDao = null;
//		mediaDao = null;
//		mediaRuntimeDao = null;
		referenceDao = null;
		referenceRuntimeDao = null;
	}
	
	
		

}
