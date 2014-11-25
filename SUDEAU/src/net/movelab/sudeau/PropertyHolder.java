package net.movelab.sudeau;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

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
    public static final String IS_FIRST_TIME = "IS_FIRST_TIME_4";
    public static final String USER_KEY = "USER_KEY";
    public static final String IS_REGISTERED = "IS_REGISTERED";
    public static final String USER_ID = "USER_ID";
    public static final String USER_NAME = "USER_NAME";
    public static final String LAST_UPDATE_GENERAL_MAP = "LAST_UPDATE_GENERAL_MAP";
    public static final String LAST_UPDATE_GENERAL_REFERENCES = "LAST_UPDATE_GENERAL_REFERENCES";
    public static final String LOCALE = "pref_locale";
    public static final String GENERAL_MAP_PATH = "GENERAL_MAP_PATH";
    public static final String GENERAL_REFERENCE_PATH = "GENERAL_REFERENCE_PATH";
    public static final String NEEDS_SYNC_FIX = "NEEDS_SYNC_FIX_2";
    public static final String AUTOCENTER = "AUTOCENTER";
    public static final String USERHIGHLIGHTS = "USERHIGHLIGHTS";
    public static final String USERITINERARIES = "USERITINERARIES";
    public static final String TRIP_IN_PROGRESS_FOLLOWING = "TRIP_IN_PROGRESS_FOLLOWING";
    public static final String TRIP_IN_PROGRESS_TRACKING = "TRIP_IN_PROGRESS_TRACKING";
    public static final String TRIP_IN_PROGRESS_MODE = "TRIP_IN_PROGRESS_MODE";


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

    public static boolean isAutoCenterOn() {
        return sharedPreferences.getBoolean(AUTOCENTER, false);
    }

    public static void setAutocenterOn(boolean _isOn) {
        editor.putBoolean(AUTOCENTER, _isOn);
        editor.apply();
    }
    public static boolean isUserHighlightsOn() {
        return sharedPreferences.getBoolean(USERHIGHLIGHTS, false);
    }

    public static void setUserHighlightsOn(boolean _isOn) {
        editor.putBoolean(USERHIGHLIGHTS, _isOn);
        editor.apply();
    }

    public static boolean isUserItinerariesOn() {
        return sharedPreferences.getBoolean(USERITINERARIES, false);
    }

    public static void setUseritinerariesOn(boolean _isOn) {
        editor.putBoolean(USERITINERARIES, _isOn);
        editor.apply();
    }

    public static int getTripInProgressFollowing() {
        return sharedPreferences.getInt(TRIP_IN_PROGRESS_FOLLOWING, -1);
    }

    public static void setTripInProgressFollowing(int following_id) {
        editor.putInt(TRIP_IN_PROGRESS_FOLLOWING, following_id);
        editor.apply();
    }

    public static int getTripInProgressTracking() {
        return sharedPreferences.getInt(TRIP_IN_PROGRESS_TRACKING, -1);
    }

    public static void setTripInProgressTracking(int tracking_id) {
        editor.putInt(TRIP_IN_PROGRESS_TRACKING, tracking_id);
        editor.apply();
    }

    public static int getTripInProgressMode() {
        return sharedPreferences.getInt(TRIP_IN_PROGRESS_MODE, -1);
    }

    public static void setTripInProgressMode(int mode) {
        editor.putInt(TRIP_IN_PROGRESS_MODE, mode);
        editor.apply();
    }


    public static boolean isFirstTime() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME, true);
    }

    public static void setFirstTime(boolean _isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME, _isFirstTime);
        editor.apply();
    }

    public static boolean needsSyncFix() {
        return sharedPreferences.getBoolean(NEEDS_SYNC_FIX, true);
    }

    public static void setNeedsSyncFix(boolean _needs_sync_fix) {
        editor.putBoolean(NEEDS_SYNC_FIX, _needs_sync_fix);
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
		String id = sharedPreferences.getString(USER_ID, null);
        if(id == null){
            id = UUID.randomUUID().toString();
            editor.putString(USER_ID, id);
            editor.apply();
        }
        return id;
	}

    public static String getUserName() {
        return sharedPreferences.getString(USER_NAME, UtilLocal.SERVULET_ANONYMOUS_USERNAME);
    }

    public static void setUserName(String _userName) {
        editor.putString(USER_NAME, _userName);
        editor.apply();
    }

    public static String getUserKey() {
        return sharedPreferences.getString(USER_KEY, UtilLocal.SERVULET_ANONYMOUS_API_KEY);
    }

    public static void setUserKey(String _userKey) {
        editor.putString(USER_KEY, _userKey);
        editor.apply();
    }


    public static String getGeneralMapPath() {
        return sharedPreferences.getString(GENERAL_MAP_PATH, null);
    }

    public static void setGeneralMapPath(String _path) {
        editor.putString(GENERAL_MAP_PATH, _path);
        editor.apply();
    }

    public static String getGeneralReferencePath() {
        return sharedPreferences.getString(GENERAL_REFERENCE_PATH, null);
    }

    public static void setGeneralReferencePath(String _path) {
        editor.putString(GENERAL_REFERENCE_PATH, _path);
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
        return sharedPreferences.getString(LOCALE, "oc");
    }

    public static void setLocale(String _locale) {
        editor.putString(LOCALE, _locale);
        editor.apply();
    }


}