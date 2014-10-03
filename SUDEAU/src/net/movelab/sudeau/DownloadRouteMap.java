package net.movelab.sudeau;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Syncs app with server.
 *
 * @author John R.B. Palmer
 *
 */
public class DownloadRouteMap extends Service {

    private static String TAG = "DownloadRouteMap";

    private boolean downloading = false;

    Context context;

    ContentResolver cr;
    Cursor c;

    @Override
    public void onStart(Intent intent, int startId) {

        Util.logInfo(context, TAG, "on start");


            if (!downloading) {
                downloading = true;

                Thread mapDownloadThread = new Thread(null, doMapDownloading,
                        "downloadrRouteMap");
                mapDownloadThread.start();


        }
    };

    private Runnable doMapDownloading = new Runnable() {
        public void run() {
            tryDownload();
        }
    };

    @Override
    public void onCreate() {

        context = getApplicationContext();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    private void tryDownload() {
        int count;


        try {

            URL url = new URL("http://107.170.174.182//media/holet/route_maps/map_route7.zip");

            Log.d("ANDRO_ASYNC", "uRL: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.connect();

            int lenghtOfFile = conn.getContentLength();
            Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

            Log.d("ANDRO_ASYNC", "SD path: " + Environment.getExternalStorageDirectory().getPath());

            File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route_map.zip");
            String destinationPath = destinationFile.getPath();
            Log.d("ANDRO_ASYNC", "Save path: " + destinationPath);

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(destinationPath);

            byte data[] = new byte[1024];


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
            Log.d("ANDRO_ASYNC", "read path: " + destinationPath);
            File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder);
            String destDirectory = target_directory.getPath();
            Log.d("ANDRO_ASYNC", "Save path: " + destDirectory);

            UnzipUtility unzipper = new UnzipUtility();
            try {
//                    unzipper.unzip(zipFilePath, destDirectory);
//                    publishProgress(""+100);

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
                // some errors occurred
                ex.printStackTrace();
            }

        } catch (Exception e) {}




        downloading = false;

    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
