package net.movelab.sudeau;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Syncs app with server.
 *
 * @author John R.B. Palmer
 */
public class DownloadRouteContent extends IntentService {
    private EruletApp app;
    private int route_id;
    private boolean media_content_success= false;
    private boolean map_success = false;
    private int response_code = 0;

    Context context;

    public static String INCOMING_MESSAGE_KEY_ROUTE_ID = "route_id";
    public static String OUTGOING_MESSAGE_KEY_RESPONSE_CODE = "response_code";

    public static int RESPONSE_CODE_FAIL = 0;
    public static int RESPONSE_CODE_SUCCESS = 1;

    public DownloadRouteContent() {
        super("DownloadRouteContent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        route_id = intent.getIntExtra(INCOMING_MESSAGE_KEY_ROUTE_ID, -1);

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        context = getApplicationContext();

        if (app != null) {
            Route route = DataContainer.findRouteById(route_id, app.getDataBaseHelper());
            if (route != null) {

                // Media Content

                media_content_success = true;
                try {
                    HttpResponse response = Util.getResponse(Util.getUrlRouteContent(context, route.getServerId(), route.getRouteContentLastUpdated()), 900000);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        if (response.containsHeader("Content-Length")) {
                            Log.d("Route Media status code: ", "" + statusCode);
                            HttpEntity entity = response.getEntity();
                            File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route.zip");
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
                            String zipFilePath = destinationPath;
                            File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder);
                            String destDirectory = target_directory.getPath();

                            try {

                                final int BUFFER_SIZE = 4096;

                                File destDir = new File(destDirectory);
                                if (!destDir.exists()) {
                                    destDir.mkdirs();
                                }
                                ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
                                ZipEntry entry = zipIn.getNextEntry();

                                // iterate over entries in the zip file
                                while (entry != null) {
                                        String filePath = destDirectory + File.separator + entry.getName();
                                        if (!entry.isDirectory()) {
                                            // if the entry is a file, extract it
                                            File f = new File(filePath);
                                            File dir = new File(f.getParent());
                                            dir.mkdirs();
                                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                                            byte[] bytesIn = new byte[BUFFER_SIZE];
                                            int read;
                                            while ((read = zipIn.read(bytesIn)) != -1) {
                                                bos.write(bytesIn, 0, read);

                                            }
                                            bos.close();

                                            // save the file manifest to database
                                            FileManifest this_file_manifest = DataContainer.createFileManifest(filePath, app.getDataBaseHelper());

                                            if (filePath.contains("highlight_")) {
                                                String this_highlight_server_id = filePath.split("highlight_")[1].split("/")[0];
                                                if (filePath.contains("/media/")) {
                                                    // This is a media file associated directly withy the highlight
                                                    Log.d("HighlightServerId: ", this_highlight_server_id);
                                                    HighLight this_highlight = DataContainer.findHighlightByServerId(Integer.parseInt(this_highlight_server_id), app.getDataBaseHelper());
                                                    this_highlight.setFileManifest(this_file_manifest);
                                                    DataContainer.updateHighLight(this_highlight, app.getDataBaseHelper());
                                                } else if (filePath.contains("reference")) {
                                                    // This is a reference
                                                    String this_reference_server_id = filePath.split("reference_")[1].split("/")[0];
                                                    Reference this_reference = DataContainer.findReferenceByServerId(Integer.parseInt(this_reference_server_id), app.getDataBaseHelper());
                                                    this_file_manifest.setReference(this_reference);
                                                    DataContainer.updateFileManifest(this_file_manifest, app.getDataBaseHelper());
                                                    if (filePath.contains(".html")) {
                                                        String lang = filePath.substring(filePath.length() - 7, filePath.length() - 5);
                                                        this_reference.setHtmlPath(lang, filePath);

                                                        DataContainer.updateReference(this_reference, app.getDataBaseHelper());

                                                    }

                                                } else if (filePath.contains("interactive_image")) {
                                                    String this_ii_server_id = filePath.split("interactive_image_")[1].split("/")[0];

                                                    InteractiveImage this_ii = DataContainer.findInteractiveImageByServerId(Integer.parseInt(this_ii_server_id), app.getDataBaseHelper());
                                                    this_ii.setFileManifest(this_file_manifest);
                                                    DataContainer.updateInteractiveImage(this_ii, app.getDataBaseHelper());

                                                }
                                            } else if (filePath.contains("route_reference")) {
                                                Reference this_reference = route.getReference();
                                                this_file_manifest.setReference(this_reference);
                                                DataContainer.updateFileManifest(this_file_manifest, app.getDataBaseHelper());
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
                                Log.e("Exception", "asdasd");

                                media_content_success = false;
                                ex.printStackTrace();
                            }
                        } else {
                            Log.e("Maps", "entity: " + response.getEntity().getContent());
                        }
                    } else {
                        Log.e("NEWTRY", "failed to get map");
                        media_content_success = false;
                    }
                } catch (ClientProtocolException e) {
                    Log.e("NEWTRY", "error: " + e);
                    media_content_success = false;
                } catch (IOException e) {
                    media_content_success = false;
                    Log.e("NEWTRY", "error: " + e);
                }
                if (media_content_success) {
                    route.setRouteContentLastUpdatedNow();
                    DataContainer.updateRoute(route, app.getDataBaseHelper());
                }


                // Route Map
                map_success = true;
                if (route.getLocalCarto() != null && !route.getLocalCarto().equals("") && !route.getLocalCarto().equals("none")) {
                    try {
                        HttpResponse response = Util.getResponse(Util.getUrlRouteMap(route), 180000);
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == 200) {
                            if (response.containsHeader("Content-Length")) {
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

                                                // add it to route
                                                route.setLocalCarto(filePath);
                                                route.setLocalCartoLastUpdatedNow();
                                                DataContainer.updateRoute(route, app.getDataBaseHelper());
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
                                    map_success = false;
                                }

                            } else {
                            }
                        } else {
                            map_success = false;
                        }
                    } catch (ClientProtocolException e) {
                        map_success = false;

                    } catch (IOException e) {
                        map_success = false;
                    }
                    if (map_success) {
                        route.setLocalCartoLastUpdatedNow();
                    }
                }
            }
        }

        // send response back
        if(media_content_success && map_success){
            response_code = RESPONSE_CODE_SUCCESS;
        } else{
            response_code = RESPONSE_CODE_FAIL;
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ROUTE_CONTENT_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(OUTGOING_MESSAGE_KEY_RESPONSE_CODE, response_code);
        sendBroadcast(broadcastIntent);
    }

}