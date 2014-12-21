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
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
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
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Syncs app with server.
 *
 * @author John R.B. Palmer
 */
public class DownloadCoreData extends IntentService {
    private EruletApp app;
    private boolean general_references_success = false;
    private boolean map_success = false;
    private boolean jsonSuccess = false;
    private int response_code = 0;

    Context context;

    public static String OUTGOING_MESSAGE_KEY_RESPONSE_CODE = "response_code";

    public static int RESPONSE_CODE_FAIL = 0;
    public static int RESPONSE_CODE_SUCCESS = 1;

    public DownloadCoreData() {
        super("DownloadCoreData");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        PropertyHolder.setCoreDataStatus(PropertyHolder.STATUS_CODE_DOWNLOADING);

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        context = getApplicationContext();

        if (Util.isOnline(context)) {

            if (app != null) {

                DataBaseHelper dataBaseHelper = app.getDataBaseHelper();

                Boolean refresh_json = PropertyHolder.isCoreDataRefreshNeeded();

                // JSON
                String jsonArrayStringFlatRoutes = Util.getJSON(UtilLocal.API_ROUTES_FLAT, context);
                if (jsonArrayStringFlatRoutes != null) {
                    try {
                        JSONArray jsonArrayFlatRoutes = new JSONArray(jsonArrayStringFlatRoutes);
                        if (jsonArrayFlatRoutes.length() > 0) {
                            for (int i = 0; i < jsonArrayFlatRoutes.length(); i++) {
                                JSONObject this_j = jsonArrayFlatRoutes.getJSONObject(i);
                                if (this_j != null) {
                                    int this_server_id = this_j.optInt("server_id", -1);
                                    if (this_server_id >= 0) {
                                        Route local_route = DataContainer.findRouteByServerId(this_server_id, dataBaseHelper);
                                        if (local_route != null) {
                                            // route is on phone so first make sure user is not currently following it; if currently following, don't update it now, wait for next sync
                                            if(PropertyHolder.getTripInProgressFollowing() != local_route.getId() && PropertyHolder.getTripInProgressTracking() != local_route.getId()){
                                            long local_lmod = local_route.getRouteJsonLastUpdated();
                                            String this_last_modified_date = this_j.optString("last_modified", null);
                                            if (this_last_modified_date != null) {
                                                long lmod = Util.ecma262ToLong(this_last_modified_date);
                                                if (lmod > local_lmod || refresh_json) {
                                                    // last modified on server after most recent update on phone
                                                    jsonSuccess = getRoute(this_server_id, dataBaseHelper);
                                                }
                                            } else {
                                                // server has no last modified date so better update
                                                jsonSuccess = getRoute(this_server_id, dataBaseHelper);
                                            }
                                            }
                                        } else {
                                            // route is not  on phone at all so
                                            jsonSuccess = getRoute(this_server_id, dataBaseHelper);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        jsonSuccess = false;
                    }
                }

                if(jsonSuccess){
                    PropertyHolder.setCoreDataRefreshNeeded(false);
                }

                // GENERAL REFERENCES
                boolean grSuccess = true;
                try {
                    HttpResponse response = Util.getResponse(Util.getUrlGeneralReferences(context), 180000);
                    int statusCode = response.getStatusLine().getStatusCode();
                    Log.i("GR Download", "Status code:" + statusCode);
                    if (statusCode == 200) {
                        if (response.containsHeader("Content-Length")) {
                            int fileSize = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
                            HttpEntity entity = response.getEntity();
                            File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route_map.zip");
                            String destinationPath = destinationFile.getPath();
                            destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/general_references.zip");
                            destinationPath = destinationFile.getPath();
                            entity = response.getEntity();
                            InputStream input = new BufferedInputStream(entity.getContent());
                            OutputStream output = new FileOutputStream(destinationPath);
                            byte data[] = new byte[1024];
                            int count;
                            while ((count = input.read(data)) != -1) {
                                output.write(data, 0, count);
                            }
                            output.flush();
                            output.close();
                            input.close();
                            // NOW UNZIP IT
                            ZipFile thisZipfile = new ZipFile(destinationPath);
                            int nEntries = thisZipfile.size();
                            int zipCounter = 0;
                            String zipFilePath = destinationPath;
                            File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.generalReferencesFolder);
                            String destDirectory = target_directory.getPath();
                            try {
                                final int BUFFER_SIZE = 4096;
                                File destDir = new File(destDirectory);
                                if (!destDir.exists()) {
                                    destDir.mkdirs();
                                }
                                ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
                                ZipEntry entry = zipIn.getNextEntry();
                                // iterates over entries in the zip file
                                while (entry != null) {
                                    String filePath = destDirectory + File.separator + entry.getName();
                                    if (!entry.isDirectory()) {
                                        // if the entry is a file, extracts it
                                        File f = new File(filePath);
                                        File dir = new File(f.getParent());
                                        dir.mkdirs();
                                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                                        byte[] bytesIn = new byte[BUFFER_SIZE];
                                        int read = 0;
                                        while ((read = zipIn.read(bytesIn)) != -1) {
                                            bos.write(bytesIn, 0, read);
                                        }
                                        bos.close();
                                        FileManifest this_file_manifest = new FileManifest();
                                        this_file_manifest.setPath(filePath);
                                        Reference this_reference = new Reference();
                                        this_reference.setGeneralReference(true);
                                        this_file_manifest.setReference(this_reference);
                                        try {
                                            dataBaseHelper.getFileManifestDataDao().create(this_file_manifest);
                                        } catch (RuntimeException ex) {
                                            grSuccess = false;
                                            Log.e("Creating file manifest", "Create error " + ex.toString());
                                        }
                                    } else {
                                        // if the entry is a directory, make the directory
                                        File dir = new File(filePath);
                                        dir.mkdirs();
                                    }
                                    zipIn.closeEntry();
                                    entry = zipIn.getNextEntry();
                                }
                                zipIn.close();
                            } catch (Exception ex) {
                                grSuccess = false;
                                ex.printStackTrace();
                            }
                        } else {
                        }
                    } else {
                        grSuccess = false;
                    }
                } catch (ClientProtocolException e) {
                    grSuccess = false;
                } catch (IOException e) {
                    grSuccess = false;
                }
                if (grSuccess) {
                    PropertyHolder.setLastUpdateGeneralReferencesNow();
                }


                // GENERAL MAP
                boolean mapSuccess = true;
                try {
                    HttpResponse response = Util.getResponse(Util.getUrlGeneralMap(context), 180000);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        if (response.containsHeader("Content-Length")) {
                            int fileSize = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
                            HttpEntity entity = response.getEntity();
                            File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route_map.zip");
                            String destinationPath = destinationFile.getPath();
                            InputStream input = new BufferedInputStream(entity.getContent());
                            OutputStream output = new FileOutputStream(destinationPath);
                            byte data[] = new byte[1024];
                            int count;
                            while ((count = input.read(data)) != -1) {
                                output.write(data, 0, count);
                            }
                            output.flush();
                            output.close();
                            input.close();
                            // NOW UNZIP IT
                            ZipFile thisZipfile = new ZipFile(destinationPath);
                            int nEntries = thisZipfile.size();
                            int zipCounter = 0;
                            String zipFilePath = destinationPath;
                            File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMapsFolder);
                            String destDirectory = target_directory.getPath();
                            try {
                                final int BUFFER_SIZE = 4096;
                                File destDir = new File(destDirectory);
                                if (!destDir.exists()) {
                                    destDir.mkdirs();
                                }
                                ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
                                ZipEntry entry = zipIn.getNextEntry();
                                // iterates over entries in the zip file. THE server should put only one in it, and I will save only the last entry as the map destination in shared preferences. But I am keeping the iteration just in case that would change in future.
                                while (entry != null) {

                                    String filePath = destDirectory + File.separator + entry.getName();
                                    PropertyHolder.setGeneralMapPath(filePath);
                                    if (!entry.isDirectory()) {
                                        // if the entry is a file, extracts it
//                            extractFile(zipIn, filePath);

                                        File f = new File(filePath);
                                        File dir = new File(f.getParent());
                                        dir.mkdirs();
                                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                                        byte[] bytesIn = new byte[BUFFER_SIZE];
                                        int read = 0;
                                        while ((read = zipIn.read(bytesIn)) != -1) {
                                            bos.write(bytesIn, 0, read);
                                        }
                                        bos.close();
                                    } else {
                                        // if the entry is a directory, make the directory
                                        File dir = new File(filePath);
                                        dir.mkdirs();
                                    }

                                    zipIn.closeEntry();
                                    entry = zipIn.getNextEntry();
                                }
                                zipIn.close();
                            } catch (Exception ex) {
                                mapSuccess = false;
                            }

                        } else {
                            Log.e("Maps", "entity: " + response.getEntity().getContent());
                        }
                    } else {
                        mapSuccess = false;
                    }
                } catch (ClientProtocolException e) {
                    mapSuccess = false;

                } catch (IOException e) {
                    mapSuccess = false;
                }
                if (mapSuccess) {
                    PropertyHolder.setLastUpdateGeneralMapNow();
                }

                // now sync any route content already downloaded that needs updating
                List<Route> routes = DataContainer.getAllOfficialRoutes(dataBaseHelper);
                for(Route route : routes){
                    // if last updates of either content or maps are above zero, then user has already downloaded it, so we should keep it in sync
                    // but only if not currentlly following
                    if(PropertyHolder.getTripInProgressTracking() != route.getId() && PropertyHolder.getTripInProgressFollowing() != route.getId()){
                    if(route.getRouteContentLastUpdated() > 0L || route.getLocalCartoLastUpdated() > 0L){
                        Intent updateRouteContentIntent = new Intent(context, DownloadRouteContent.class);
                        updateRouteContentIntent.putExtra(DownloadRouteContent.INCOMING_MESSAGE_KEY_ROUTE_ID, route.getId());
                        context.startService(updateRouteContentIntent);
                    }
                }
                }
                // now sync ratings
                context.startService(new Intent(context, UploadRatings.class));

            }

        }


        // send response back and set status in property holder
        if (jsonSuccess && general_references_success && map_success) {
            response_code = RESPONSE_CODE_SUCCESS;
            PropertyHolder.setCoreDataStatus(PropertyHolder.STATUS_CODE_READY);
        } else {
            response_code = RESPONSE_CODE_FAIL;
            PropertyHolder.setCoreDataStatus(PropertyHolder.STATUS_CODE_MISSING);
        }

        Intent localIntent =
                new Intent(Util.INTENT_CODE_CORE_DATE_RESPONSE)
                        .putExtra(OUTGOING_MESSAGE_KEY_RESPONSE_CODE, response_code);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);



    }

    private boolean getRoute(int server_id, DataBaseHelper dataBaseHelper) {
        boolean jsonSuccess = true;
        String jsonObjectString = Util.getJSON(UtilLocal.API_ROUTES + server_id + "/", context);
        if (jsonObjectString != null) {
            JSONObject j;
            try {
                j = new JSONObject(jsonObjectString);
                Route newRoute = JSONConverter.jsonObjectToRoute(j, dataBaseHelper);
                Route existingRoute = DataContainer.findRouteByServerId(newRoute.getServerId(), dataBaseHelper);
                if (existingRoute == null) {
                    DataContainer.insertRoute(newRoute, dataBaseHelper);
                } else {
                    // check again that route not being followed:
                    if(PropertyHolder.getTripInProgressFollowing() != existingRoute.getId() && PropertyHolder.getTripInProgressTracking() != existingRoute.getId()){
                    DataContainer.updateOfficialRouteFromServer(newRoute, existingRoute, app);
                }}
            } catch (JSONException e) {
                Log.e("JSON download 1", "" + e);
                jsonSuccess = false;
            }
        }
        return jsonSuccess;
    }

}