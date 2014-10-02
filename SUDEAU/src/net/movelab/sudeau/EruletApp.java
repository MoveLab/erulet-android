package net.movelab.sudeau;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Route;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Environment;
import android.util.Log;

public class EruletApp extends Application{
	
	private SharedPreferences mPrefs;	
	private static Context context;
	private Locale locale = null;
	private static final SimpleDateFormat HOURS_MINUTES_SECONDS_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSZ");
	private static final SimpleDateFormat MEDIA_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static final SimpleDateFormat DAY_MONTH_YEAR = new SimpleDateFormat("dd/MM/yyyy");
	private DataBaseHelper dataBaseHelper;
	private boolean trackingServiceOn = false;
	private Locale deviceLocale = null;
	private static final String TAG = "EruletApp";
	
	@Override
	public void onCreate() {
		super.onCreate();
		EruletApp.context = getApplicationContext();
		deviceLocale = getResources().getConfiguration().locale;
		applyLocaleSettings();
		createAppFolders();
        if(dataBaseHelper == null){
    		dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);
    		//DataContainer.loadSampleData(dataBaseHelper, this.getBaseContext());
    		boolean loaded = getPrefs().getBoolean("redon_loaded", false);
//    		if(!loaded){
//    			DataContainer.loadRedonCompact(dataBaseHelper, this.getBaseContext());
//    		reloadRedonCompact();
//    		reloadEscunHau();
//    		reloadBassaOles();
//    		reloadVarrados();
//    		reloadGaronaPeu();
    		//reloadArtigaDeLin();

    	}

	}
	
//	private void reloadArtigaDeLin(){
//		Route r = DataContainer.findRouteById("ROUTE_ARTIGA", dataBaseHelper);
//		if(r!=null){
//			DataContainer.deleteRouteCascade(r,this);
//		}
//		DataContainer.loadArtigaDeLin(dataBaseHelper, this.getBaseContext());
//	}
	
	private void reloadGaronaPeu(){
		Route r = DataContainer.findRouteById("ROUTE_GARONAP", dataBaseHelper);
		if(r!=null){
			//DataContainer.deleteRouteCascade(r,this);
			r.setLocalCarto("Garona_hires.mbtiles");
			DataContainer.updateRoute(r, dataBaseHelper);
		}
		DataContainer.loadGaronaPeu(dataBaseHelper, this.getBaseContext());
	}
	
	private void reloadVarrados(){
		Route r = DataContainer.findRouteById("ROUTE_VARRADOS", dataBaseHelper);
		if(r!=null){
			DataContainer.deleteRouteCascade(r,this);
		}
		DataContainer.loadVarrados(dataBaseHelper, this.getBaseContext());
	}

	private void reloadBassaOles(){
		Route r = DataContainer.findRouteById("ROUTE_BASSAOLES", dataBaseHelper);
		if(r!=null){
			DataContainer.deleteRouteCascade(r,this);
		}
		DataContainer.loadBassaOles(dataBaseHelper, this.getBaseContext());
	}
//	
	private void reloadEscunHau(){
		Route r = DataContainer.findRouteById("ROUTE_ESCUNHAU", dataBaseHelper);
		if(r!=null){
			DataContainer.deleteRouteCascade(r,this);
		}
		DataContainer.loadEscunhau(dataBaseHelper, this.getBaseContext());
	}
	
	private void reloadRedonCompact(){
		Route r = DataContainer.findRouteById("ROUTE_REDON", dataBaseHelper);
		if(r!=null){
			DataContainer.deleteRouteCascade(r,this);
		}
		DataContainer.loadRedonCompact(dataBaseHelper, this.getBaseContext());
	}
	
	public void createFolder(String path){
		File file = new File(Environment.getExternalStorageDirectory(), path);
	    if (!file.exists()) {
	        if (!file.mkdirs()) {
	        	Log.d(TAG,"Error creating folder " + path);
	        }
	    }
	}
	
	public void createAppFolders(){
		createFolder(Util.baseFolder);
		createFolder(Util.baseFolder + "/" + Util.picturesFolder);
		createFolder(Util.baseFolder + "/" + Util.videosFolder);
		createFolder(Util.baseFolder + "/" + Util.othersFolder);
	}
	
	public void applyLocaleSettings(){
    	SharedPreferences settings = getPrefs();

        Configuration config = getBaseContext().getResources().getConfiguration();
        
        String lang = settings.getString("pref_locale", "");
        if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        {
            locale = new Locale(lang);            
        }else{        	
        	locale = deviceLocale;
        }
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
	
	public void restart(){
    	Intent i = getBaseContext().getPackageManager()
	             .getLaunchIntentForPackage( getBaseContext().getPackageName() );
	    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    startActivity(i);
    }
		
	
	public static Context getAppContext() {
        return EruletApp.context;
    }
	
	public boolean isPrivilegedUser() {
		//@TODO Check if the user is really a privileged user (can create new routes)
		return false;
	}
	
	public SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = getSharedPreferences("EruletPreferences", MODE_PRIVATE);   
        }
        return mPrefs;
    }
	
	public SharedPreferences.Editor getPrefsEditor(){
		return getPrefs().edit();
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	Configuration config = new Configuration(newConfig); 
    	if (locale != null)
        {
    		config.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
    }
	
	public DataBaseHelper getDataBaseHelper(){
		return dataBaseHelper;
	}
	
	public String formatDateHoursMinutesSeconds(Date date) { return HOURS_MINUTES_SECONDS_FORMAT.format(date); }
	public String formatDateMediaTimestamp(Date date) { return MEDIA_TIMESTAMP.format(date); }
	public String formatDateDayMonthYear(Date date) { return DAY_MONTH_YEAR.format(date); }


	public boolean isTrackingServiceOn() {
		return trackingServiceOn;
	}	
	
	public void startTrackingService(){
		Intent intent = new Intent(getString(R.string.internal_message_id)
				+ Util.MESSAGE_SCHEDULE);
		sendBroadcast(intent);
		trackingServiceOn=true;
	}
	
	public void stopTrackingService(){
		Intent intent = new Intent(getString(R.string.internal_message_id)
				+ Util.MESSAGE_UNSCHEDULE);
		sendBroadcast(intent);
		trackingServiceOn=false;
	}
	
}
