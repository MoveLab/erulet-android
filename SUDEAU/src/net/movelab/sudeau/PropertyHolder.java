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
    public static final String SYNC_ALARM_ON = "SYNC_ALARM_ON";
    public static final String AUTOCENTER = "AUTOCENTER";
    public static final String USERHIGHLIGHTS = "USERHIGHLIGHTS";
    public static final String USERITINERARIES = "USERITINERARIES";
    public static final String TRIP_IN_PROGRESS_FOLLOWING = "TRIP_IN_PROGRESS_FOLLOWING";
    public static final String TRIP_IN_PROGRESS_TRACKING = "TRIP_IN_PROGRESS_TRACKING";
    public static final String TRIP_IN_PROGRESS_MODE = "TRIP_IN_PROGRESS_MODE";
    public static final String GOOGLE_MAPS_OFFLINE_READY = "GOOGLE_MAPS_OFFLINE_READY";

    public static final String CORE_DATA_STATUS = "CORE_DATA_STATUS";
    public static final String ROUTE_CONTENT_STATUS_PREFIX = "ROUTE_CONTENT_STATUS_ROUTE";

    public static final String ARANESE = "oc";
    public static final String SPANISH = "es";
    public static final String CATALAN = "ca";
    public static final String FRENCH = "fr";
    public static final String ENGLISH = "en";

    public static final int STATUS_CODE_MISSING = 0;
    public static final int STATUS_CODE_QUEUED = 1;
    public static final int STATUS_CODE_DOWNLOADING = 2;
    public static final int STATUS_CODE_READY = 3;
    public static final int STATUS_CODE_CANCELLED = 4;


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

    public static boolean isSyncAlarmOn() {
        return sharedPreferences.getBoolean(SYNC_ALARM_ON, false);
    }

    public static void setSyncAlarmOn(boolean _alarm_on) {
        editor.putBoolean(SYNC_ALARM_ON, _alarm_on);
        editor.apply();
    }


    public static boolean isGoogleMapsOfflineReady() {
        return sharedPreferences.getBoolean(GOOGLE_MAPS_OFFLINE_READY, false);
    }

    public static void setGoogleMapsOfflineReady(boolean _isReady) {
        editor.putBoolean(GOOGLE_MAPS_OFFLINE_READY, _isReady);
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

    public static int getCoreDataStatus(){
        return sharedPreferences.getInt(CORE_DATA_STATUS, 0);
    }

    public static void setCoreDataStatus(int status_code){
        editor.putInt(CORE_DATA_STATUS, status_code);
        editor.apply();
    }

    public static int getRouteContentStatus(int route_id){
        return sharedPreferences.getInt(ROUTE_CONTENT_STATUS_PREFIX + route_id, 0);
    }

    public static void setRouteContentStatus(int route_id, int status_code){
        editor.putInt(ROUTE_CONTENT_STATUS_PREFIX + route_id, status_code);
        editor.apply();
    }




}