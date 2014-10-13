// Copied from Space Mapper. Will need to be adapted.


package net.movelab.sudeau;

import java.util.Calendar;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import net.movelab.sudeau.TrackingContentContract.Fixes;
import net.movelab.sudeau.TrackingUpload.Uploads;

/**
 * Uploads files to the server. The tryUploads() method is mostly taken from the
 * Funf code.
 * 
 * @author John R.B. Palmer
 * 
 */
public class FileUploader extends Service {
	// private static final String TAG = "FileUploader";
	private static boolean isFix;
	private boolean uploading = false;

	Context context;

	@Override
	public void onStart(Intent intent, int startId) {

		

		
		if (!uploading) {
			uploading = true;

			isFix = false;

			Thread uploadThread = new Thread(null, doFileUploading,
					"uploadBackground");
			uploadThread.start();

		}
	};

	private Runnable doFileUploading = new Runnable() {
		public void run() {
			tryUploads();
		}
	};

	@Override
	public void onCreate() {

		// Log.e(TAG, "FileUploader onCreate.");

		context = getApplicationContext();
		if (PropertyHolder.isInit() == false)
			PropertyHolder.init(context);
	}

	@Override
	public void onDestroy() {

	}

	private void tryUploads() {

		if (Util.isOnline(context)) {
			// Log.e(TAG, "FileUploader online.");

			ContentResolver cr = getContentResolver();

			Cursor c = cr.query(Util.getUploadQueueUri(context),
					Uploads.KEYS_ALL, null, null, null);

			if (!c.moveToFirst()) {
				c.close();
				return;
			}

			int idIndex = c.getColumnIndexOrThrow(Uploads.KEY_ROWID);
			int prefixIndex = c.getColumnIndexOrThrow(Uploads.KEY_PREFIX);
			int dataIndex = c.getColumnIndexOrThrow(Uploads.KEY_DATA);

			while (!c.isAfterLast()) {

				String prefix = c.getString(prefixIndex);
				isFix = prefix.equals("FIX");


				int thisId = c.getInt(idIndex);


				if (Util.uploadEncryptedString(context, prefix,
						c.getString(dataIndex), Util.SERVER)) {


					cr.delete(Util.getUploadQueueUri(context),
							Uploads.KEY_ROWID + " = " + String.valueOf(thisId),
							null);

				}

				c.moveToNext();

			}

			c.close();
		}


		uploading = false;

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	private void announceUpload(int nUploads) {
		Intent intent = new Intent(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_UPLOADED);
		intent.putExtra("nUploads", nUploads);
		sendBroadcast(intent);
	}

}
