package net.movelab.sudeau.database;

import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Box;
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
	public static final int DATABASE_VERSION = 238;
	// Database Name
	public static final String DATABASE_NAME = "appdata";
		
	private Dao<Route, Integer> routeDao;
	private RuntimeExceptionDao<Route, Integer> routeRuntimeDao;

    private Dao<FileManifest, Integer> fileManifestDao;
    private RuntimeExceptionDao<FileManifest, Integer> fileManifestRuntimeDao;

    private Dao<Track, Integer> trackDao;
	private RuntimeExceptionDao<Track, Integer> trackRuntimeDao;
	
	private Dao<Step, Integer> stepDao;
	private RuntimeExceptionDao<Step, Integer> stepRuntimeDao;
	
	private Dao<HighLight, Integer> hlDao;
	private RuntimeExceptionDao<HighLight, Integer> hlRuntimeDao;
		
	private Dao<Reference, Integer> referenceDao;
	private RuntimeExceptionDao<Reference, Integer> referenceRuntimeDao;
	
	private Dao<InteractiveImage, Integer> interactiveImageDao;
	private RuntimeExceptionDao<InteractiveImage, Integer> interactiveImageRuntimeDao;
	
	private Dao<Box, Integer> boxDao;
	private RuntimeExceptionDao<Box, Integer> boxRuntimeDao;
           
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
			TableUtils.createTable(connectionSource, Reference.class);
			TableUtils.createTable(connectionSource, InteractiveImage.class);
			TableUtils.createTable(connectionSource, Box.class);
            TableUtils.createTable(connectionSource, FileManifest.class);
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
			TableUtils.dropTable(connectionSource, Reference.class, true);
			TableUtils.dropTable(connectionSource, InteractiveImage.class, true);
			TableUtils.dropTable(connectionSource, Box.class, true);
            TableUtils.dropTable(connectionSource, FileManifest.class, true);
			onCreate(db,connectionSource);
		} catch (java.sql.SQLException e) {
			Log.e(DataBaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}

	}
	
	public Dao<Box, Integer> getBoxDao() throws java.sql.SQLException {
		if (boxDao == null) {
			boxDao = getDao(Box.class);
		}
		return boxDao;
	}
	
	public RuntimeExceptionDao<Box, Integer> getBoxDataDao() {
		if (boxRuntimeDao == null) {			
			boxRuntimeDao = getRuntimeExceptionDao(Box.class);
		}
		return boxRuntimeDao;
	}
	
	public Dao<InteractiveImage, Integer> getInteractiveImageDao() throws java.sql.SQLException {
		if (interactiveImageDao == null) {
			interactiveImageDao = getDao(InteractiveImage.class);
		}
		return interactiveImageDao;
	}
	
	public RuntimeExceptionDao<InteractiveImage, Integer> getInteractiveImageDataDao() {
		if (interactiveImageRuntimeDao == null) {			
			interactiveImageRuntimeDao = getRuntimeExceptionDao(InteractiveImage.class);
		}
		return interactiveImageRuntimeDao;
	}
	
	public Dao<Route, Integer> getRouteDao() throws java.sql.SQLException {
		if (routeDao == null) {
			routeDao = getDao(Route.class);
		}
		return routeDao;
	}
	
	public RuntimeExceptionDao<Route, Integer> getRouteDataDao() {
		if (routeRuntimeDao == null) {			
			routeRuntimeDao = getRuntimeExceptionDao(Route.class);
		}
		return routeRuntimeDao;
	}
	
    public Dao<FileManifest, Integer> getFileManifestDao() throws java.sql.SQLException {
        if (fileManifestDao == null) {
            fileManifestDao = getDao(FileManifest.class);
        }
        return fileManifestDao;        
    }

    public RuntimeExceptionDao<FileManifest, Integer> getFileManifestDataDao() {
        if (fileManifestRuntimeDao == null) {
            fileManifestRuntimeDao = getRuntimeExceptionDao(FileManifest.class);
        }
        return fileManifestRuntimeDao;
    }


    public Dao<Track, Integer> getTrackDao() throws java.sql.SQLException {
		if (trackDao == null) {
			trackDao = getDao(Track.class);
		}
		return trackDao;
	}
	
	public RuntimeExceptionDao<Track, Integer> getTrackDataDao() {
		if (trackRuntimeDao == null) {			
			trackRuntimeDao = getRuntimeExceptionDao(Track.class);
		}
		return trackRuntimeDao;
	}
	
	public Dao<Step, Integer> getStepDao() throws java.sql.SQLException {
		if (stepDao == null) {
			stepDao = getDao(Step.class);
		}
		return stepDao;
	}
	
	public RuntimeExceptionDao<Step, Integer> getStepDataDao() {
		if (stepRuntimeDao == null) {			
			stepRuntimeDao = getRuntimeExceptionDao(Step.class);
		}
		return stepRuntimeDao;
	}
	
	public Dao<HighLight, Integer> getHlDao() throws java.sql.SQLException {
		if (hlDao == null) {
			hlDao = getDao(HighLight.class);
		}
		return hlDao;
	}
	
	public RuntimeExceptionDao<HighLight, Integer> getHlDataDao() {
		if (hlRuntimeDao == null) {			
			hlRuntimeDao = getRuntimeExceptionDao(HighLight.class);
		}
		return hlRuntimeDao;
	}		
		
	public Dao<Reference, Integer> getReferenceDao() throws java.sql.SQLException {
		if (referenceDao == null) {
			referenceDao = getDao(Reference.class);
		}
		return referenceDao;
	}
	
	public RuntimeExceptionDao<Reference, Integer> getReferenceDataDao() {
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
		referenceDao = null;
		referenceRuntimeDao = null;
		interactiveImageDao = null;
		interactiveImageRuntimeDao = null;
		boxDao = null;
		boxRuntimeDao = null;
        fileManifestDao = null;
        fileManifestRuntimeDao = null;
	}
	
	
		

}
