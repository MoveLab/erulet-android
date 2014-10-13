// Copied from Space Mapper. Will need to be adapted.

package net.movelab.sudeau;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import net.movelab.sudeau.TrackingContentContract.Fixes;

public abstract class TrackingContentProvider extends ContentProvider {

	// private static final String TAG = "SpaceMapperContentProvider";

	/** The SQLite database name */
	public static final String DATABASE_NAME = "userdata";

	/** The database version */
	private static final int DATABASE_VERSION = 13;

	/** The location fix table name; currently "HMPfixes" */
	public static final String DATABASE_TABLE = "HMPfixes";

	/** The SQL command to create the HMPfixes table */
	private static final String DATABASE_CREATE = "create table HMPfixes ("
			+ "_id integer primary key autoincrement," + "accuracy real,"
			+ "altitude real," + "latitude real," + "longitude real,"
			+ "provider text," + "timelong long," + "sdtimelong long);";

	private DatabaseHelper mDbHelper;

	private UriMatcher sUriMatcher;

	protected abstract String getAuthority();
	
	Uri contentUri = Uri.parse("content://"
			+ getAuthority() + "/" + DATABASE_TABLE);

	public TrackingContentProvider() {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(getAuthority(), DATABASE_TABLE, FIXES);
		sUriMatcher.addURI(getAuthority(), DATABASE_TABLE + "/#", FIX_ID);

		fixesProjectionMap = new HashMap<String, String>();
		fixesProjectionMap.put(Fixes.KEY_ROWID, Fixes.KEY_ROWID);
		fixesProjectionMap.put(Fixes.KEY_TIMELONG, Fixes.KEY_TIMELONG);
		fixesProjectionMap.put(Fixes.KEY_ACCURACY, Fixes.KEY_ACCURACY);
		fixesProjectionMap.put(Fixes.KEY_ALTITUDE, Fixes.KEY_ALTITUDE);
		fixesProjectionMap.put(Fixes.KEY_LATITUDE, Fixes.KEY_LATITUDE);
		fixesProjectionMap.put(Fixes.KEY_LONGITUDE, Fixes.KEY_LONGITUDE);
		fixesProjectionMap.put(Fixes.KEY_PROVIDER, Fixes.KEY_PROVIDER);
		fixesProjectionMap.put(Fixes.KEY_STATION_DEPARTURE_TIMELONG,
				Fixes.KEY_STATION_DEPARTURE_TIMELONG);

	}

	private static final int FIXES = 1;
	private static final int FIX_ID = 2;

	private static HashMap<String, String> fixesProjectionMap;

	/**
	 * DatabaseHelper class. Manages the TripInProgress SQLite database creation
	 * and upgrades.
	 * 
	 * @author Chang Y. Chung
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			// Log.i(TAG, "Upgrading database from version " + oldVersion +
			// " to "
			// + newVersion);

			if (oldVersion < 11) {
				// create temp. table to hold data
				db.execSQL("CREATE TEMPORARY TABLE HMPfixes_backup (_id integer primary key autoincrement, accuracy real, altitude real, latitude real, longitude real, provider text, timelong long);");
				// insert data from old table into temp table

				db.execSQL("INSERT INTO HMPfixes_backup SELECT _id, accuracy, altitude, latitude, longitude, provider, timelong FROM HMPfixes ");
				// drop the old table now that your data is safe in the
				// temporary
				// one
				db.execSQL("DROP TABLE HMPfixes");

				// recreate the table with new columns and minus deleted column
				db.execSQL("CREATE TABLE HMPfixes (_id integer primary key autoincrement, accuracy real, altitude real, latitude real, longitude real, provider text, timelong long, sdtimelong long);");

				// fill it up using null for new columns
				db.execSQL("INSERT INTO HMPfixes SELECT _id, accuracy, altitude, latitude, longitude, provider, timelong, null FROM HMPfixes_backup");
				// then drop the temporary table
				db.execSQL("DROP TABLE HMPfixes_backup");
			} else {
				// create temp. table to hold data
				db.execSQL("CREATE TEMPORARY TABLE HMPfixes_backup (_id integer primary key autoincrement, accuracy real, altitude real, latitude real, longitude real, provider text, timelong long, sdtimelong long);");
				// insert data from old table into temp table

				db.execSQL("INSERT INTO HMPfixes_backup SELECT _id, accuracy, altitude, latitude, longitude, provider, timelong, sdtimelong FROM HMPfixes ");
				// drop the old table now that your data is safe in the
				// temporary
				// one
				db.execSQL("DROP TABLE HMPfixes");

				// recreate the table with new columns and minus deleted column
				db.execSQL("CREATE TABLE HMPfixes (_id integer primary key autoincrement, accuracy real, altitude real, latitude real, longitude real, provider text, timelong long, sdtimelong long);");

				// fill it up using null for new columns
				db.execSQL("INSERT INTO HMPfixes SELECT _id, accuracy, altitude, latitude, longitude, provider, timelong, sdtimelong FROM HMPfixes_backup");
				// then drop the temporary table
				db.execSQL("DROP TABLE HMPfixes_backup");
			}

			Util.needDatabaseUpdate = true;

		}
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		switch (sUriMatcher.match(uri)) {
		case FIXES:
			break;
		case FIX_ID:
			where = where + "_id = " + uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		int count = db.delete(DATABASE_TABLE, where, whereArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case FIXES:
			return Fixes.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (sUriMatcher.match(uri) != FIXES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId = db.insert(DATABASE_TABLE, null, values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(contentUri, rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(DATABASE_TABLE);
		qb.setProjectionMap(fixesProjectionMap);

		switch (sUriMatcher.match(uri)) {
		case FIXES:
			break;
		case FIX_ID:
			selection = selection + "_id = " + uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);

		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case FIXES:
			count = db.update(DATABASE_TABLE, values, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

}
