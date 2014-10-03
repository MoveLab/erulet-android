package net.movelab.sudeau;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import android.app.Service;
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.os.IBinder;

        import com.j256.ormlite.android.apptools.OpenHelperManager;

        import net.movelab.sudeau.database.DataBaseHelper;
        import net.movelab.sudeau.database.DataContainer;
        import net.movelab.sudeau.model.JSONConverter;
        import net.movelab.sudeau.model.Route;

        import java.util.ArrayList;

/**
 * Syncs app with server.
 *
 * @author John R.B. Palmer
 *
 */
public class DownloadRoutesJson extends Service {

    private static String TAG = "SyncData";

    private boolean uploading = false;

    DataBaseHelper dataBaseHelper;

    Context context;

    ContentResolver cr;
    Cursor c;

    @Override
    public void onStart(Intent intent, int startId) {

        Util.logInfo(context, TAG, "on start");


            if (!uploading) {
                uploading = true;

                Thread uploadThread = new Thread(null, doSyncing,
                        "uploadBackground");
                uploadThread.start();


        }
    };

    private Runnable doSyncing = new Runnable() {
        public void run() {
            tryUploads();
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

    private void tryUploads() {

        if(dataBaseHelper == null){
            dataBaseHelper = OpenHelperManager.getHelper(this, DataBaseHelper.class);
        }

        String jsonArrayString = Util.getJSON(UtilLocal.API_ROUTES, context);
        Util.logInfo(context, "API", jsonArrayString);
        try {
            JSONArray ja = null;
            try {
                ja = new JSONArray(jsonArrayString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayList<Route> routes = JSONConverter.jsonArrayToRouteArray(ja);
            for(Route route: routes){
                DataContainer.insertRoute(route, dataBaseHelper, "this_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        uploading = false;

    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
