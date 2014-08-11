package net.movelab.sudeau;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

public class EruletApp extends Application{
	
	private SharedPreferences mPrefs;
	private static Context context;
	private Locale locale = null;
	private static final SimpleDateFormat HOURS_MINUTES_SECONDS_FORMAT = new SimpleDateFormat("HH:mm:ss.SSSZ");
	private static final SimpleDateFormat MEDIA_TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private static final SimpleDateFormat DAY_MONTH_YEAR = new SimpleDateFormat("dd/MM/yyyy");
	private DataBaseHelper dataBaseHelper;
	private boolean trackingServiceOn = false;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		EruletApp.context = getApplicationContext();
		SharedPreferences settings = getPrefs();
		
		Configuration config = getBaseContext().getResources().getConfiguration();

		//Store language setting in preferences
        String lang = settings.getString("pref_locale", "");
        if (! "".equals(lang) && ! config.locale.getLanguage().equals(lang))
        {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        
        if(dataBaseHelper == null){
    		dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);
    		//DataContainer.loadSampleData(dataBaseHelper, this.getBaseContext());			
    		DataContainer.loadRedonCompact(dataBaseHelper, this.getBaseContext());
    	}

	}
		
	
	public static Context getAppContext() {
        return EruletApp.context;
    }
	
	public boolean isPrivilegedUser() {
		//@TODO Check if the user is really a privileged user (can create new routes)
		return true;
	}
	
	public SharedPreferences getPrefs() {
        if (mPrefs == null) {
            mPrefs = getSharedPreferences("EruletPreferences", MODE_PRIVATE);   
        }
        return mPrefs;
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
