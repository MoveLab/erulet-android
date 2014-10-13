package net.movelab.sudeau;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemClock;

/**
 * Manipulates the application's shared preferences, values that must persist
 * throughout the application's installed lifetime.
 * <p>
 * Dependencies: Util.java
 */
public class PropertyHolder {
	private static SharedPreferences sharedPreferences;
	private static Editor editor;

    public static final String SHARED_PREFERENCES_NAME = "PROPERTIES";
    public static final String ALARM_INTERVAL = "ALARM_INTERVAL";
    public static final String SERVICE_ON = "SERVICE_ON";
    public static final String IS_REGISTERED = "IS_REGISTERED";
    public static final String USER_ID = "USER_ID";
    public static final String LAST_UPDATE_GENERAL_MAP = "LAST_UPDATE_GENERAL_MAP";
    public static final String LAST_UPDATE_GENERAL_REFERENCES = "LAST_UPDATE_GENERAL_REFERENCES";
    public static final String LOCALE = "pref_locale";
    public static final String ARANESE = "oc";
    public static final String SPANISH = "es";
    public static final String CATALAN = "ca";
    public static final String FRENCH = "fr";
    public static final String ENGLISH = "en";



    /**
	 * Initialize the shared preferences handle.
	 * 
	 * @param context
	 *            Interface to application environment
	 */
	public static void init(Context context) {
		sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		editor = sharedPreferences.edit();
	}

	public static boolean isInit() {
		return sharedPreferences != null;
	}

	public static void deleteAll() {
		editor.clear();
		editor.apply();
	}



	public static void setAlarmInterval(long alarmInterval) {
		editor.putLong(ALARM_INTERVAL, alarmInterval);
		editor.apply();
	}

	public static long getAlarmInterval() {
		long interval = sharedPreferences.getLong(ALARM_INTERVAL, -1);
		if (interval == -1) {
			interval = Util.ALARM_INTERVAL;
			PropertyHolder.setAlarmInterval(interval);
		}
		return interval;
	}

	public static boolean isServiceOn() {
		return sharedPreferences.getBoolean(SERVICE_ON, false);
	}

	public static void setServiceOn(boolean _isOn) {
		editor.putBoolean(SERVICE_ON, _isOn);
		editor.apply();
	}

	public static boolean isRegistered() {
		return sharedPreferences.getBoolean(IS_REGISTERED, false);
	}

	public static void setRegistered(boolean _isRegistered) {
		editor.putBoolean(IS_REGISTERED, _isRegistered);
		editor.apply();
	}

	public static String getUserId() {
		return sharedPreferences.getString(USER_ID, null);
	}

	public static void setUserId(String _userId) {
		editor.putString(USER_ID, _userId);
		editor.apply();
	}


    public static long getLastUpdateGeneralMap(){
        return sharedPreferences.getLong(LAST_UPDATE_GENERAL_MAP, 0L);
    }

    public static void setLastUpdateGeneralMap(long _update_unix_time){
        editor.putLong(LAST_UPDATE_GENERAL_MAP, _update_unix_time);
        editor.apply();
    }

    public static void setLastUpdateGeneralMapNow(){
        long _update_unix_time = (long) System.currentTimeMillis()/1000;
        editor.putLong(LAST_UPDATE_GENERAL_MAP, _update_unix_time);
        editor.apply();
    }


    public static long getLastUpdateGeneralReferences(){
        return sharedPreferences.getLong(LAST_UPDATE_GENERAL_REFERENCES, 0L);
    }

    public static void setLastUpdateGeneralReferences(long _update_unix_time){
        editor.putLong(LAST_UPDATE_GENERAL_REFERENCES, _update_unix_time);
        editor.apply();
    }

    public static void setLastUpdateGeneralReferencesNow(){
        long _update_unix_time = (long) System.currentTimeMillis()/1000;
        editor.putLong(LAST_UPDATE_GENERAL_REFERENCES, _update_unix_time);
        editor.apply();
    }


    public static String getLocale() {
        return sharedPreferences.getString(LOCALE, "");
    }

    public static void setLocale(String _locale) {
        editor.putString(LOCALE, _locale);
        editor.apply();
    }


}