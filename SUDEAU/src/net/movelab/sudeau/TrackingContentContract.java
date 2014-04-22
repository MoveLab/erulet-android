// Copied from Space Mapper. Will need to be adapted.

package net.movelab.sudeau;

import android.provider.BaseColumns;

public class TrackingContentContract {

	public TrackingContentContract() {
	}

	public static final class Fixes implements BaseColumns {
		private Fixes() {
		}


		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.palmerasmlibrary.fixes";

		/** The row ID key name */
		public static final String KEY_ROWID = "_id";

		/** The timelong key name */
		public static final String KEY_TIMELONG = "timelong";

		public static final String KEY_ACCURACY = "accuracy";

		public static final String KEY_ALTITUDE = "altitude";

		public static final String KEY_LATITUDE = "latitude";

		public static final String KEY_LONGITUDE = "longitude";

		public static final String KEY_PROVIDER = "provider";
		
		public static final String KEY_STATION_DEPARTURE_TIMELONG = "sdtimelong";

		/** The names of all the fields contained in the location fix table */
		public static final String[] KEYS_ALL = { KEY_ROWID, KEY_ACCURACY,
				KEY_ALTITUDE, KEY_LATITUDE, KEY_LONGITUDE, KEY_PROVIDER,
				KEY_TIMELONG, KEY_STATION_DEPARTURE_TIMELONG };

		public static final String[] KEYS_SAVECSV = { KEY_ACCURACY,
				KEY_ALTITUDE, KEY_LATITUDE, KEY_LONGITUDE, KEY_PROVIDER,
				KEY_TIMELONG, KEY_STATION_DEPARTURE_TIMELONG};

		public static final String[] KEYS_LATLON = { KEY_ROWID, KEY_LATITUDE,
				KEY_LONGITUDE };

		public static final String[] KEYS_LATLONACC = { KEY_ROWID,
				KEY_LATITUDE, KEY_LONGITUDE, KEY_ACCURACY };

		public static final String[] KEYS_LATLONTIME = { KEY_ROWID,
				KEY_LATITUDE, KEY_LONGITUDE, KEY_TIMELONG };

		public static final String[] KEYS_LATLONACCTIMES = { KEY_ROWID,
			KEY_LATITUDE, KEY_LONGITUDE, KEY_ACCURACY, KEY_TIMELONG, KEY_STATION_DEPARTURE_TIMELONG };

	}

}
