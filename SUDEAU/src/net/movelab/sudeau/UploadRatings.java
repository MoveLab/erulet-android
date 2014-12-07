package net.movelab.sudeau;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Syncs app with server.
 *
 * @author John R.B. Palmer
 */
public class UploadRatings extends IntentService {
    private EruletApp app;

    Context context;

    public UploadRatings() {
        super("DownloadRouteContent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        context = getApplicationContext();

        if (Util.isOnline(context)) {

            if (app != null) {

                DataBaseHelper dataBaseHelper = app.getDataBaseHelper();

                HttpResponse response;
                int statusCode;

                for (Route this_route : DataContainer.getRoutesWithRatingsNotUploaded(dataBaseHelper)) {
                    try {
                        JSONObject this_rating = JSONConverter.userRouteRatingToServerJSONObject(this_route);
                        response = Util.postJSON(this_rating, UtilLocal.URL_USER_RATINGS, context);
                        statusCode = Util.getResponseStatusCode(response);
                        if (statusCode >= 200 && statusCode < 300) {
                            this_route.setUserRatingUploaded(true);
                            DataContainer.updateRoute(this_route, dataBaseHelper);
                        } else {
                            //TODO
                        }
                    } catch (JSONException e) {
                        //TODO
                    }
                }

                for (HighLight this_hl : DataContainer.getHighlightsWithRatingsNotUploaded(dataBaseHelper)) {
                    try {
                        JSONObject this_rating = JSONConverter.userHighlightRatingToServerJSONObject(this_hl);
                        response = Util.postJSON(this_rating, UtilLocal.URL_USER_RATINGS, context);
                        statusCode = Util.getResponseStatusCode(response);
                        if (statusCode >= 200 && statusCode < 300) {
                            this_hl.setUserRatingUploaded(true);
                            DataContainer.updateHighLight(this_hl, dataBaseHelper);
                        } else {
                            //TODO
                        }
                    } catch (JSONException e) {
                        //TODO
                    }
                }

            }
        }
    }
}