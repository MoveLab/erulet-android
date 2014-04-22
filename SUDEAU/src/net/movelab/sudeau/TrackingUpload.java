// Copied from Space Mapper. Will need to be adapted.

package net.movelab.sudeau;

import android.provider.BaseColumns;

public class TrackingUpload {

	public TrackingUpload() {
	}

	public static final class Uploads implements BaseColumns {
		private Uploads() {
		}


		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.palmerasmlibrary.uploads";

		/** The row ID key name */
		public static final String KEY_ROWID = "_id";

		/** The timelong key name */
		public static final String KEY_PREFIX = "prefix";

		public static final String KEY_DATA = "data";

		/** The names of all the fields contained in the location fix table */
		public static final String[] KEYS_ALL = { KEY_ROWID, KEY_PREFIX,
				KEY_DATA };


	}

}
