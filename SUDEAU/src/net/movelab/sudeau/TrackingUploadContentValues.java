// Copied from Space Mapper. Will need to be adapted.


package net.movelab.sudeau;

import android.content.ContentValues;
import net.movelab.sudeau.TrackingUpload.Uploads;

public class TrackingUploadContentValues {
	
	
	public static ContentValues createUpload(String prefix, String data) {

		ContentValues initialValues = new ContentValues();
		initialValues.put(Uploads.KEY_PREFIX, prefix);
		initialValues.put(Uploads.KEY_DATA, data);

		return initialValues;
	}

	


}
