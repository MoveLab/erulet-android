// Copied from Space Mapper. Will need to be adapted.


package net.movelab.sudeau;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import net.movelab.sudeau.TrackingContentContract.Fixes;
import net.movelab.sudeau.model.HighLight;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;


import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Various static fields and methods used in the application, some taken from
 * Human Mobility Project.
 * 
 * @author Chang Y. Chung
 * @author Necati E. Ozgencil
 * @author John R.B. Palmer
 */
public class Util {

    public final static boolean DEBUG_MODE = true;

	public final static int DEFAULT_HIGHLIGHT_RADIUS = 10;
    public final static int DEFAULT_PRECISION = 0;
    public final static int DEFAULT_ORDER = -1;
    public final static int DEFAULT_ALTITUDE = -1;

	public final static String MESSAGE_STOP_FIXGET = ".STOP_FIXGET";
	public final static String MESSAGE_LONGSTOP_FIXGET = ".LONGSTOP_FIXGET";
	public final static String MESSAGE_SCHEDULE = ".SCHEDULE_SERVICE";
	public final static String MESSAGE_UNSCHEDULE = ".UNSCHEDULE_SERVICE";
	public final static String MESSAGE_FIX_RECORDED = ".NEW_FIX_RECORDED";
	public final static String MESSAGE_FIX_UPLOADED = ".NEW_FIX_UPLOADED";

	public final static int TRACKING_NOTIFICATION = 0;

	public final static long SECONDS = 1000;
	public final static long MINUTES = SECONDS * 60;
	public final static long HOURS = MINUTES * 60;
	public final static long DAYS = HOURS * 24;
	public final static long WEEKS = DAYS * 7;

	public static long UPLOAD_INTERVAL = 1 * HOURS;

	public static boolean PASSED_INTRO = false;

	public static int MAX_FILE_STORAGE_NUMBER = 10000;
	
	public static double MINIMUM_POP_DISTANCE_RADIUS = 50;	
	
	//Estanh Redon Coordinates
	public static final LatLng ESTANH_REDON = new LatLng(42.64, 0.78);
	//

	// Min average comfortable walking speed (cm/s) from Bohannon 1997,
	// http://ageing.oxfordjournals.org/content/26/1/15.full.pdf+html
	public static int WALKING_SPEED = 127;

	// Use the distance one would cover at walking speed capped at 80 (which is
	// standard city block size)
	public static int getMinDist() {
		int fixIntervalSeconds = (int) ((int) PropertyHolder.getAlarmInterval() / (int) SECONDS);

		int expectedWalkingDistanceMeters = (int) (WALKING_SPEED * fixIntervalSeconds) / 100;
		return Math.min(MIN_DIST, expectedWalkingDistanceMeters);
	}

	public static int MIN_DIST = 80;

	public static boolean needDatabaseUpdate = false;

	public static int EXTRARUNS = 4;

	public static boolean flushGPSFlag = false;

	public static boolean redrawMap = false;

	public static long xTime = 1 * 60 * 60 * 1000;
	/**
	 * Default value for the interval between location fixes. In milliseconds.
	 */
	public static final long ALARM_INTERVAL = 15000; // 15 seconds

	/**
	 * Server URL for uploads.
	 */
	public static final String SERVER = "xxxx";

	/**
	 * Extension to append to all files saved for uploading.
	 */
	public static final String EXTENSION = ".uploadque";

	/**
	 * Maximum length of time to run location listeners during each fix attempt.
	 * In milliseconds.
	 */
	public static final long LISTENER_WINDOW = 5 * 1000;

	/**
	 * Value at which a GPS location will be preferred to a network location,
	 * even if the network location is listed with a higher accuracy.
	 */
	public static final float MIN_GPS_ACCURACY = 50;

	/**
	 * Value at which a location will be used, and both listeners stopped even
	 * if not yet at the end of the listener window.
	 */
	public static final float OPT_ACCURACY = 15;

	/**
	 * Value at which a location will be used, and both listeners stopped even
	 * if not yet at the end of the listener window - for long runs.
	 */
	public static final float OPT_ACCURACY_LONGRUNS = 50;

	/**
	 * Minimum accuracy necessary for location to be used.
	 */
	public static final float MIN_ACCURACY = 500;

	/**
	 * Default time for storing user data when user selects to do so. In days.
	 */
	public static final int STORAGE_DAYS = 7;

	/**
	 * Dummy variable indicating whether application is currently taking fix.
	 */
	public static boolean locatingNow = false;

	/**
	 * Default value for figuring out when alarm manager started counting. For
	 * use with the display timer in the CountdownDisplay activity.
	 */
	public static long countingFrom = 0;

	public static long lastFixStartedAt = 0;

	/**
	 * counter for how many fixes have been missed in a row.
	 */
	public static int missedFixes = 0;

	/**
	 * temp holder for info on latest fix.
	 */
	public static String lastFixTimeStamp = null;

	/**
	 * temp holder for info on latest fix.
	 */
	public static long lastFixTime = 0;

	/**
	 * temp holder for info on latest fix.
	 */
	public static double lastFixLat = 0;

	/**
	 * temp holder for info on latest fix.
	 */
	public static double lastFixLon = 0;

	/**
	 * holder for current value of the listener window
	 */
	public static long listenerTimer = LISTENER_WINDOW;
	
	
	public static String baseFolder = "Holet";
	public static String routeMapsFolder = "route_maps";
    public static String generalReferencesFolder = "general_references";
    public static String routeMediaFolder = "route_media";

    public static String makeHighlightMediaPath(String highlight_id, String route_id, String media_name){
        File f = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/" + "route_" + route_id + "/" + "highlight_" + highlight_id + "/media/" + media_name);
        return f.getAbsolutePath();
    }

    public static String makeInteractiveImageImagePath(String interactive_image_id, String highlight_id, String route_id, String image_name){
        File f = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/" + "route_" + route_id + "/" + "highlight_" + highlight_id + "/interactive_image/" + interactive_image_id + "/" + image_name);
        return f.getAbsolutePath();
    }

    public static String makeReferencePath(String route_id, String lang_code){
        File f = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/" + "route_" + route_id + "/" + "route_reference/reference_" + lang_code + ".html");
        return f.getAbsolutePath();
    }

    public static String makeReferencePath(String route_id, String highlight_id, String reference_id, String lang_code){
        File f = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/" + "route_" + route_id + "/" + "highlight_" + highlight_id + "/reference_" + reference_id + "/reference_" + lang_code + ".html");
        return f.getAbsolutePath();
    }



    /**
	 * Surrounds the given string in quotation marks. Taken from Human Mobility
	 * Project code written by Chang Y. Chung and Necati E. Ozgencil.
	 * 
	 * @param str
	 *            The string to be encased in quotation marks.
	 * @return The given string trimmed and encased in quotation marks.
	 */
	public static String enquote(String str) {
		final String dq = "\"";
		final String ddq = dq + dq;
		StringBuilder sb = new StringBuilder("");
		sb.append(dq);
		sb.append((str.trim()).replace(dq, ddq));
		sb.append(dq);
		return sb.toString();
	}

	/**
	 * Formats the given coordinate and converts to String form. Taken from
	 * Human Mobility Project code written by Chang Y. Chung and Necati E.
	 * Ozgencil.
	 * 
	 * @param coord
	 *            The coordinate value to be formatted.
	 * @return The properly formatted coordinate in String form
	 */
	public static String fmtCoord(double coord) {
		return String.format("%1$11.6f", coord);
	}

	/**
	 * Formats the given time and converts to String form. Taken from Human
	 * Mobility Project code written by Chang Y. Chung and Necati E. Ozgencil.
	 * 
	 * @param time
	 *            The time value to be formatted.
	 * @return The properly formatted time value in String form
	 */
	public static String iso8601(long time) {
		return String.format("%1$tFT%1$tT", time);
	}

	/**
	 * Formats the given time and converts to String form. Taken from Human
	 * Mobility Project code written by Chang Y. Chung and Necati E. Ozgencil.
	 * 
	 * @param datetime
	 *            The Date object, whose long time value must be formatted.
	 * @return The properly formatted time value of the Date Object in String
	 *         form
	 */
	public static String iso8601(Date datetime) {
		return iso8601(datetime.getTime());
	}

	/**
	 * Formats a date object for displaying it to the user.
	 * 
	 * @param date
	 *            The Date object to be formatted.
	 * @return The properly formatted time and date as a String.
	 * 
	 */
	public static String userDate(Date date) {
		SimpleDateFormat s = new SimpleDateFormat("HH:mm dd/MM/yyyy");
		String format = s.format(date);
		return format;
	}

	public static String userDateNoTime(Date date) {
		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yyyy");
		String format = s.format(date);
		return format;
	}

	public static String userDateY2D(Date date) {
		SimpleDateFormat s = new SimpleDateFormat("dd/MM/yy");
		String format = s.format(date);
		return format;
	}

	/**
	 * Formats the location time, given as a long in milliseconds, for use in
	 * filenames.
	 * 
	 * @param locationTime
	 *            The long value to be formatted.
	 * @return The properly formatted time and date as a String.
	 */
	public static String fileNameDate(long locationTime) {
		Date date = new Date(locationTime);
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String format = s.format(date);
		return format;
	}

	/**
	 * Gets the current system time in milliseconds. Taken from Human Mobility
	 * Project code written by Chang Y. Chung and Necati E. Ozgencil.
	 * 
	 * @return The current system time in milliseconds.
	 */
	public static String now() {
		return iso8601(System.currentTimeMillis());
	}

	/**
	 * Displays a brief message on the phone screen. Taken from Human Mobility
	 * Project code written by Chang Y. Chung and Necati E. Ozgencil.
	 * 
	 * @param context
	 *            Interface to application environment
	 * @param msg
	 *            The message to be displayed to the user
	 */
	public static void toast(Context context, String msg) {
		Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
		// t.setDuration(5*Toast.LENGTH_LONG);
		t.show();
	}

	/**
	 * Encrypt a byte array using RSA. Relies on the public key file stored in
	 * the raw folder (which is not included in the public source code).
	 * 
	 * @param context
	 *            The application context.
	 * @param in
	 *            The byte array to be encrypted.
	 * @return An encrypted byte array.
	 */
	public static byte[] encryptRSA(Context context, byte[] in) {
		byte[] result = new byte[1];

		// TODO create this method. This was previously the RSA encryption
		// method in Space Mapper, but symmetric encryption probably makes more
		// sense for this app.

		return result;
	}

	/**
	 * Checks if the phone has an internet connection.
	 * 
	 * @param context
	 *            The application context.
	 * @return True if phone has a connection; false if not.
	 */
	public static boolean isOnline(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static String csvFixes(Cursor c) {

		int accuracy = c.getColumnIndexOrThrow(Fixes.KEY_ACCURACY);
		int altitude = c.getColumnIndexOrThrow(Fixes.KEY_ALTITUDE);
		int latitude = c.getColumnIndexOrThrow(Fixes.KEY_LATITUDE);
		int longitude = c.getColumnIndexOrThrow(Fixes.KEY_LONGITUDE);
		int provider = c.getColumnIndexOrThrow(Fixes.KEY_PROVIDER);
		int timelong = c.getColumnIndexOrThrow(Fixes.KEY_TIMELONG);
		StringBuilder sb = new StringBuilder("");
		sb.append("accuracy").append(",");
		sb.append("altitude").append(",");
		sb.append("latitude").append(",");
		sb.append("longitude").append(",");
		sb.append("provider").append(",");
		sb.append("time");

		c.moveToFirst();
		while (!c.isAfterLast()) {
			sb.append("\n");
			sb.append(doubleFieldVal(c, accuracy)).append(",");
			sb.append(doubleFieldVal(c, altitude)).append(",");
			sb.append(doubleFieldVal(c, latitude)).append(",");
			sb.append(doubleFieldVal(c, longitude)).append(",");
			sb.append(Util.enquote(c.getString(provider))).append(",");
			sb.append(Util.enquote(Util.userDate(new Date(c.getLong(timelong)))
					.trim()));
			c.moveToNext();
		}
		return sb.toString();
	}

	/**
	 * Converts the supposedly double value contained in the row, at which the
	 * given Cursor is pointing, and the col(umn) specified to its String
	 * representation.
	 * 
	 * @return The String representation of the value contained in the cell
	 *         [c.getPosition(), col]
	 */
	public static String doubleFieldVal(Cursor c, int col) {
		Double val = (Double) c.getDouble(col);
		return (val == null) ? "" : val.toString();
	}

	/**
	 * Locates the index of the first element greater than or equal to a given
	 * value from an array of integers sorted in ascending order.
	 * 
	 * @author = John R.B. Palmer
	 * 
	 * @param A
	 *            The array to search in. Must be sorted in ascending order
	 * @param key
	 *            The value to be searched for.
	 * @param imin
	 *            The minimum index of the array to search in.
	 * @param imax
	 *            The maximum index of the array to search in.
	 * 
	 * @return -1 if imax is less than imin or if the key is above imax.
	 *         Otherwise the index of the first element that is greater than or
	 *         equal to the key.
	 */
	public static int minElementGreaterThanOrEqualToKey(int A[], int key,
			int imin, int imax) {

		// Return -1 if the maximum value is less than the minimum or if the key
		// is great than the maximum
		if (imax < imin || key > A[imax])
			return -1;

		// Return the first element of the array if that element is greater than
		// or equal to the key.
		if (key < A[imin])
			return imin;

		// When the minimum and maximum values become equal, we have located the
		// element.
		if (imax == imin)
			return imax;

		else {
			// calculate midpoint to cut set in half, avoiding integer overflow
			int imid = imin + ((imax - imin) / 2);

			// if key is in upper subset, then recursively search in that subset
			if (A[imid] < key)
				return minElementGreaterThanOrEqualToKey(A, key, imid + 1, imax);

			// if key is in lower subset, then recursively search in that subset
			else
				return minElementGreaterThanOrEqualToKey(A, key, imin, imid);
		}
	}

	/**
	 * Locates the index of the first element greater than or equal to a given
	 * value from an array of integers sorted in ascending order.
	 * 
	 * @author = John R.B. Palmer
	 * 
	 * @param A
	 *            The array to search in. Must be sorted in ascending order
	 * @param key
	 *            The value to be searched for.
	 * @param imin
	 *            The minimum index of the array to search in.
	 * @param imax
	 *            The maximum index of the array to search in.
	 * 
	 * @return -1 if imax is less than imin or if the key is above imax.
	 *         Otherwise the index of the first element that is greater than or
	 *         equal to the key.
	 */
	public static int minElementGreaterThanOrEqualToKey(long A[], long key,
			int imin, int imax) {

		// Return -1 if the maximum value is less than the minimum or if the key
		// is great than the maximum
		if (imax < imin || key > A[imax])
			return -1;

		// Return the first element of the array if that element is greater than
		// or equal to the key.
		if (key < A[imin])
			return imin;

		// When the minimum and maximum values become equal, we have located the
		// element.
		if (imax == imin)
			return imax;

		else {
			// calculate midpoint to cut set in half, avoiding integer overflow
			int imid = imin + ((imax - imin) / 2);

			// if key is in upper subset, then recursively search in that subset
			if (A[imid] < key)
				return minElementGreaterThanOrEqualToKey(A, key, imid + 1, imax);

			// if key is in lower subset, then recursively search in that subset
			else
				return minElementGreaterThanOrEqualToKey(A, key, imin, imid);
		}
	}

	/**
	 * Locates the index of the last element less than or equal to a given value
	 * from an array sorted in ascending order.
	 * 
	 * @author = John R.B. Palmer
	 * 
	 * @param A
	 *            The array to search in. Must be sorted in ascending order
	 * @param key
	 *            The value to be searched for.
	 * @param imin
	 *            The minimum index of the array to search in.
	 * @param imax
	 *            The maximum index of the array to search in.
	 * 
	 * @return -1 if imax is less than imin or if the key is below imin.
	 *         Otherwise the index of the last element that is less than or
	 *         equal to the key.
	 */
	public static int maxElementLessThanOrEqualToKey(int A[], int key,
			int imin, int imax) {

		if (imax < imin || key < A[imin])
			return -1;
		if (key > A[imax])
			return imax;
		if (imax == imin) {
			return imax;
		} else {
			// calculate midpoint to cut set in half
			int imid = imax - ((imax - imin) / 2);

			if (A[imid] > key)
				// key is in lower subset
				return maxElementLessThanOrEqualToKey(A, key, imin, imid - 1);
			else
				// key is in upper subset
				return maxElementLessThanOrEqualToKey(A, key, imid, imax);

		}
	}

	/**
	 * Locates the index of the last element less than or equal to a given value
	 * from an array sorted in ascending order.
	 * 
	 * @author = John R.B. Palmer
	 * 
	 * @param A
	 *            The array to search in. Must be sorted in ascending order
	 * @param key
	 *            The value to be searched for.
	 * @param imin
	 *            The minimum index of the array to search in.
	 * @param imax
	 *            The maximum index of the array to search in.
	 * 
	 * @return -1 if imax is less than imin or if the key is below imin.
	 *         Otherwise the index of the last element that is less than or
	 *         equal to the key.
	 */
	public static int maxElementLessThanOrEqualToKey(long A[], long key,
			int imin, int imax) {

		if (imax < imin || key < A[imin])
			return -1;
		if (key > A[imax])
			return imax;
		if (imax == imin) {
			return imax;
		} else {
			// calculate midpoint to cut set in half
			int imid = imax - ((imax - imin) / 2);

			if (A[imid] > key)
				// key is in lower subset
				return maxElementLessThanOrEqualToKey(A, key, imin, imid - 1);
			else
				// key is in upper subset
				return maxElementLessThanOrEqualToKey(A, key, imid, imax);

		}
	}

	public static File[] listFiles(Context context) {
		File directory = new File(context.getFilesDir().getAbsolutePath());
		File[] files = directory.listFiles();
		return files;
	}

	public static String getAvailableMB() {
		final long SIZE_KB = 1024L;
		final long SIZE_MB = SIZE_KB * SIZE_KB;
		final File path = Environment.getDataDirectory();
		long availableSpace = -1L;
		StatFs stat = new StatFs(path.getPath());
		availableSpace = (long) stat.getAvailableBlocks()
				* (long) stat.getBlockSize();
		return String.valueOf(availableSpace / SIZE_MB + " MB");

	}

	public static String getUserDbMB(Context context) {
		final long SIZE_KB = 1024L;
		final long SIZE_MB = SIZE_KB * SIZE_KB;
		if (context.getDatabasePath(TrackingContentProvider.DATABASE_NAME) != null) {
			final long totalSize = context.getDatabasePath(
					TrackingContentProvider.DATABASE_NAME).length();
			if (totalSize > SIZE_MB)
				return String.valueOf(totalSize / SIZE_MB) + " MB";
			if (totalSize < SIZE_MB && totalSize > SIZE_KB)
				return String.valueOf(totalSize / SIZE_KB) + " KB";
			else
				return String.valueOf(totalSize) + " bytes";
		} else
			return "0 bytes";

	}

	public static String getPendingUploadsMB(Context context) {
		final long SIZE_KB = 1024L;
		final long SIZE_MB = SIZE_KB * SIZE_KB;
		if (context.getDatabasePath(TrackingUploadQueueContentProvider.DATABASE_NAME) != null) {
			final long totalSize = context.getDatabasePath(
					TrackingUploadQueueContentProvider.DATABASE_NAME).length();
			if (totalSize > SIZE_MB)
				return String.valueOf(totalSize / SIZE_MB) + " MB";
			if (totalSize < SIZE_MB && totalSize > SIZE_KB)
				return String.valueOf(totalSize / SIZE_KB) + " KB";
			else
				return String.valueOf(totalSize) + " bytes";
		} else
			return "0 bytes";

	}

	public static String formatLongTime(Long time) {
		final long hours = time / HOURS;
		final long minutes = (time % HOURS) / MINUTES;
		final long seconds = (time % MINUTES) / SECONDS;
		return String.valueOf(hours) + ":" + String.valueOf(minutes) + ":"
				+ String.valueOf(seconds);
	}

	public static String createFileName(String prefix) {
		return prefix + PropertyHolder.getUserId() + "_"
				+ fileNameDate(System.currentTimeMillis()) + EXTENSION;
	}

	public static String TAG = "UPLOADER";

	public static boolean uploadEncryptedString(Context context,
			String filePrefix, String stringToUpload, String uploadurl) {

		if (!isOnline(context))
			return false;

		String fileNameOnServer = createFileName(filePrefix);

		byte[] bytes;
		try {
			bytes = encryptRSA(context, stringToUpload.getBytes("UTF-8"));

		} catch (UnsupportedEncodingException e) {

			return false;
		}

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		// DataInputStream inStream = null;

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 64 * 1024; // old value 1024*1024
		ByteArrayInputStream byteArrayInputStream = null;
		boolean isSuccess = true;
		byteArrayInputStream = new ByteArrayInputStream(bytes);

		try {
			// open a URL connection to the Servlet
			URL url = new URL(uploadurl);
			// Open a HTTP connection to the URL
			conn = (HttpURLConnection) url.openConnection();
			// Allow Inputs
			conn.setDoInput(true);
			// Allow Outputs
			conn.setDoOutput(true);
			// Don't use a cached copy.
			conn.setUseCaches(false);
			// set timeout
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			// Use a post method.
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data;boundary=" + boundary);

			dos = new DataOutputStream(conn.getOutputStream());
			dos.writeBytes(twoHyphens + boundary + lineEnd);
			dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\""
					+ fileNameOnServer + "\"" + lineEnd);
			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			bytesAvailable = byteArrayInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
			while (bytesRead > 0) {
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = byteArrayInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = byteArrayInputStream.read(buffer, 0, bufferSize);
			}

			// send multipart form data necesssary after file data...
			dos.writeBytes(lineEnd);
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// close streams
			// Log.e(TAG,"UploadService Runnable:File is written");
			// fileInputStream.close();
			// dos.flush();
			// dos.close();
		} catch (Exception e) {
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {

				}
			}
			if (byteArrayInputStream != null) {
				try {
					byteArrayInputStream.close();
				} catch (IOException e) {

				}
			}

		}

		// ------------------ read the SERVER RESPONSE
		try {

			if (conn.getResponseCode() != 200) {

				isSuccess = false;
			}
		} catch (IOException e) {
		}

		return isSuccess;

	}

	public static Uri getUploadQueueUri(Context context) {

		return Uri.parse("content://"
				+ context.getResources().getString(
						R.string.content_provider_authority_uploadqueue) + "/"
				+ TrackingUploadQueueContentProvider.DATABASE_TABLE);

	}

	public static Uri getFixesUri(Context context) {

		return Uri.parse("content://"
				+ context.getResources().getString(
						R.string.content_provider_authority_fixes) + "/"
				+ TrackingContentProvider.DATABASE_TABLE);

	}
	
//	File f = new File(getCacheDir() + "/OSMPublicTransport_HiRes.mbtiles");
//	if (!f.exists())
//		try {
//			InputStream is = getAssets().open("OSMPublicTransport_HiRes.mbtiles");
//			int size = is.available();
//			byte[] buffer = new byte[size];
//			is.read(buffer);
//			is.close();
//			FileOutputStream fos = new FileOutputStream(f);
//			fos.write(buffer);
//			fos.close();
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}

	
	public static byte[] readFile(String file, Context context) throws IOException {
		File f = new File(context.getCacheDir() + "/" + file);		
		if(!f.exists()){
			try{
				InputStream is = context.getAssets().open(file);				
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.close();				
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return readFile(f);
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }
    
    /**
     * Image/picture stuff 
     */
    
    /**
     * Read bitmap dimensions and type
     * @param pathName
     * @return
     */
    public static BitmapFactory.Options getImageOptions(String pathName){
    	BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;    	
    	BitmapFactory.decodeFile(pathName, options);
//    	int imageHeight = options.outHeight;
//    	int imageWidth = options.outWidth;
//    	String imageType = options.outMimeType;
    	return options;
    }

    /**
     * Computes inSampleSize, which is a reduction factor:
     * i.e a 2048x1536 with inSampleSize 4 scales to 512x384
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    	return inSampleSize;
    }
    
    /**
     * Return scaled down bitmap to load into limited size ImageView
     * 
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return Bitmap scaled down to reqWidth x reqHeight dimensions 
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName,options);        

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(pathName,options);
    }
    
    /**
     * Returns screen size of current device, in pixels
     * 
     * @param context Application context
     * @return int[] that contains width at index 0 and height at index 1
     */
    public static int[] getScreenSize(Context context){
    	int[] retVal = new int[2];
    	WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	Point size = new Point();
    	display.getSize(size);
    	retVal[0] = size.x;
    	retVal[1] = size.y;
    	return retVal;
    }
    
    /**
     * Returns biggest integer in int[]
     * 
     * @param dimensions Usually screen sizes in pixels
     * @return The biggest integer in the int array
     */
    public static int getBiggestDimension(int[] dimensions){
    	int retVal = Integer.MIN_VALUE;
    	for(int i = 0; i < dimensions.length; i++){
    		if(dimensions[i] > retVal){
    			retVal = dimensions[i];
    		}
    	}
    	return retVal;
    }
    
    /**
     * Returns smallest integer in int[]
     * 
     * @param dimensions Usually screen sizes in pixels
     * @return The smallest integer in the int array
     */
    public static int getSmallestDimension(int[] dimensions){
    	int retVal = Integer.MAX_VALUE;
    	for(int i = 0; i < dimensions.length; i++){
    		if(dimensions[i] < retVal){
    			retVal = dimensions[i];
    		}
    	}
    	return retVal;
    }
    
    /**
     * Zooms map to a certain extent
     * 
     * @param map The map on which zoom will be performed
     * @param context The application context
     * @param bounds The bounds which will be centered on screen
     * @param padding Padding in pixels around the centered window
     */
    public static void fitMapViewToBounds(GoogleMap map, Context context, 
    		LatLngBounds bounds, int padding){
    	int[] screen_sizes = Util.getScreenSize(context);
		int wsize = Util.getSmallestDimension(screen_sizes);
		double adj_wsize = (double)wsize * 0.75;
		map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,wsize,(int)adj_wsize,padding));
    }
    
    /**
     * We want to fit an image to a maximum screen width. Given the full image
     * size, and knowing the screen width in pixels, we want to know the height
     * of the scaled image in pixels. 
     * 
     * @param originalW
     * @param originalH
     * @param fitToW The screen width in pixels
     * @return The height of a image with the same aspect ratio that the original
     */
    public static int getScaledImageHeight(int originalW, int originalH, float fitToW){
    	float retVal = 0;    	
    	float factor = (float)originalW/(float)originalH;    	
    	retVal = fitToW/factor;
    	return (int)retVal;
    }
    
    
    public static int getScaledImageWidth(int originalW, int originalH, float fitToH){
    	float retVal = 0;    	
    	float factor = (float)originalW/(float)originalH;    	
    	retVal = fitToH*factor;
    	return (int)retVal;
    }
    
    /**
     * Limit to 40 characters length
     * @param highLights
     * @return
     */
    public static String getMultipleHighLightsNameLabel(List<HighLight> highLights){
    	StringBuffer retVal = new StringBuffer();
    	String delim = "";
    	for (HighLight i : highLights) {
    		retVal.append(delim).append(i.getName() == null ? "" : i.getName());
    	    delim = " - ";
    	}
    	return retVal.toString();
    }

    public static String getJSON(String apiEndpoint, Context context) {

        if (!isOnline(context)) {
            return "";
        } else {

            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 8000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);
            int timeoutSocket = 8000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient(httpParameters);
            HttpGet httpGet = new HttpGet(UtilLocal.URL_SERVULET_API_ROOT + apiEndpoint);

            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("Authorization", UtilLocal.SERVULET_AUTHORIZATION);

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                Util.logInfo(context, TAG, "Status code:" + statusCode);

                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Util.logInfo(context, TAG, "failed to get JSON data");
                }
            } catch (ClientProtocolException e) {
                Util.logError(context, TAG, "error: " + e);
            } catch (IOException e) {
                Util.logError(context, TAG, "error: " + e);
            }

            return builder.toString();
        }
    }


    public static boolean debugMode(Context context) {
        boolean result = false;
        if (DEBUG_MODE)
            result = true;
        return result;
    }

    public static void logError(Context context, String tag, String message) {
        if (debugMode(context))
            Log.e(tag, message);
    }

    public static void logInfo(Context context, String tag, String message) {
        if (debugMode(context))
            Log.i(tag, message);
    }


}
