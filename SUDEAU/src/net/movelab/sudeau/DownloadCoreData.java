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
public class DownloadCoreData extends IntentService {
    private EruletApp app;
    private boolean general_references_success = false;
    private boolean map_success = false;
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


        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        context = getApplicationContext();

        if (app != null) {

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
                                        app.getDataBaseHelper().getFileManifestDataDao().create(this_file_manifest);
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
        }

        // send response back
        if (general_references_success && map_success) {
            response_code = RESPONSE_CODE_SUCCESS;
        } else {
            response_code = RESPONSE_CODE_FAIL;
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("CORE_DATE_RESPONSE");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(OUTGOING_MESSAGE_KEY_RESPONSE_CODE, response_code);
        sendBroadcast(broadcastIntent);
    }

}