package net.movelab.sudeau;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Syncs app with server.
 *
 * @author John R.B. Palmer
 *
 */
public class DownloadRoutesMedia extends Service {

    private static String TAG = "DownloadRoutesMedia";

    private boolean downloading = false;

    DataBaseHelper dataBaseHelper;

    Context context;

    ContentResolver cr;
    Cursor c;

    @Override
    public void onStart(Intent intent, int startId) {

        Util.logInfo(context, TAG, "on start");


            if (!downloading) {
                downloading = true;

                Thread mediaDownloadThread = new Thread(null, doMediaDownloading,
                        "downloadrRoutesMedia");
                mediaDownloadThread.start();


        }
    };

    private Runnable doMediaDownloading = new Runnable() {
        public void run() {
            tryDownload();
        }
    };

    @Override
    public void onCreate() {

        // Log.e(TAG, "FileUploader onCreate.");

        context = getApplicationContext();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void tryDownload() {

        if(dataBaseHelper == null){
            dataBaseHelper = OpenHelperManager.getHelper(this, DataBaseHelper.class);
        }



        downloading = false;

    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
