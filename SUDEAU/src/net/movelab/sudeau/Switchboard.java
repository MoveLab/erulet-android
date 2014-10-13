package net.movelab.sudeau;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class Switchboard extends Activity {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

    private Button btn_manual;
    private Button btn_security;
    private Button btn_routes;
    private Button btn_credits;
    private EruletApp app;
    private SharedPreferences mPreferences;

    private int group1 = 1;
    private int first_id = Menu.FIRST;
    private int second_id = Menu.FIRST + 1;
    private int third_id = Menu.FIRST + 2;

    Context context;

    DataBaseHelper dataBaseHelper;

    ContentResolver cr;
    Cursor c;

    static final int REGISTRATION_REQUEST = 1;

    InitialSyncAsync initialSyncAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        setContentView(R.layout.activity_switchboard);
        initButtons();
        //See if we need to display the user registration
        mPreferences = getSharedPreferences("EruletPreferences", MODE_PRIVATE);

        boolean firstTime = mPreferences.getBoolean("first_time", true);

        //TODO take this out; for testing only
        Util.forceNewDownloads(context, app);

        // TODO TEMP TESTING
        mPreferences.edit().putBoolean("registered_user", false).apply();

        startInitialSync();

        // TODO Put this back in -- I have it out just for initial testing
//        tryToRegister();

        if (firstTime) {
            //Show user manual, maybe?
            //Toast.makeText(this, "First time!", Toast.LENGTH_SHORT).show();
            mPreferences.edit().putBoolean("first_time", false).apply();

        }
    }


    private void tryToRegister() {
        boolean userIsRegistered = mPreferences.getBoolean("registered_user", false);
        if (!userIsRegistered) {
            if (Util.isOnline(getBaseContext())) {
                Intent intent = new Intent(this, RegistrationActivity.class);
                startActivityForResult(intent, REGISTRATION_REQUEST);
            } else {
                Toast.makeText(this, getString(R.string.no_data_access), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.already_registered), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(group1, first_id, first_id, getString(R.string.preferences));
        menu.add(group1, second_id, second_id, getString(R.string.register_user));
        menu.add(group1, third_id, third_id, "Sync");
        //getMenuInflater().inflate(R.menu.switchboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent i = new Intent(Switchboard.this, EruletPreferencesActivity.class);
                startActivity(i);
                break;
            case 2:
                tryToRegister();
                break;
            case 3:
                startInitialSync();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initButtons() {
        btn_manual = (Button) findViewById(R.id.btn_manual);
        btn_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Intent i = new Intent(Switchboard.this, ManualActivity.class);
                Intent i = new Intent(Switchboard.this, ManualActivity.class);
                startActivity(i);
            }
        });
        btn_security = (Button) findViewById(R.id.btn_security_info);
        btn_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Switchboard.this,
                        BeforeLeaving.class);
                startActivity(i);
            }
        });
        btn_routes = (Button) findViewById(R.id.btn_routes);
        btn_routes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (Util.hasMinimumContents(context, app)) {
                    Intent i = new Intent(Switchboard.this,
                            ChooseItineraryActivity.class);
                    startActivity(i);
                } else {
                    showStillSyncingDialog();
                }
            }
        });
        btn_credits = (Button) findViewById(R.id.btn_credits);
        btn_credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Switchboard.this,
                        CreditsActivity.class);
                startActivity(i);
            }
        });

    }


    private void showStillSyncingDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                Switchboard.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Holet is still syncing");
        builderSingle.setMessage("Holet needs to complete an initial sync with the server. Please make sure you have an internet connection and wait a few moments before trying this button again.");
        builderSingle.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.show();
    }


    private void startInitialSync() {

        if (Util.isOnline(context)) {
            new InitialSyncAsync().execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Eth Holet Needs the Internet");
            builder.setIcon(R.drawable.ic_erulet_new);
            builder.setMessage("Eth Holet needs an internet connection so he can get offline maps and route media from the server. Before starting a trip, please connect to the internet and choose 'Sync' from the options menu.");

            builder.setCancelable(true);
            builder.show();
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Holet is performing an initial sync with the server...stay connected to the internet ");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(true);
                ;
                mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        if(initialSyncAsync != null)
                           initialSyncAsync.cancel(true);
                    }
                });
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }


    class InitialSyncAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);

        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            // JSON

                publishProgress("" + 1);

                if (dataBaseHelper == null) {
                    dataBaseHelper = OpenHelperManager.getHelper(context, DataBaseHelper.class);
                }

                String jsonArrayString = Util.getJSON(UtilLocal.API_ROUTES, context);
                Util.logInfo(context, "API", jsonArrayString);
                try {
                    JSONArray ja = null;
                    try {
                        ja = new JSONArray(jsonArrayString);
                        publishProgress("" + 5);

                    } catch (JSONException e) {
                        Log.e("JSON ERROR: ", e.getMessage() + e.getCause(), e);
                    }
                    ArrayList<Route> routes = JSONConverter.jsonArrayToRouteArray(ja);
                    int nDone = 0;
                    int totalRoutes = routes.size();
                    for (Route route : routes) {
                        if (isCancelled()){
                            break;}
                        else {
                            DataContainer.insertRoute(route, dataBaseHelper, "this_id");
                            publishProgress("" + (5 + (int) ((nDone * 95)) / (totalRoutes)));
                        }
                    }


                } catch (Exception e) {

                    Log.e("JSON ERROR: ", e.getMessage() + e.getCause(), e);

                }

            //Maps

            boolean mapSuccess = true;
                try {

                    URL url = new URL(Util.getUrlGeneralMap(context));

                    Log.d("ANDRO_ASYNC MAPS", "uRL: " + url.toString());

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

                    int total = 0;
                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()){
                            mapSuccess = false;
                            break;
                        }
                        else {

                            output.write(data, 0, count);

                            total += count;
                            publishProgress("" + (int) ((total * 90) / lenghtOfFile));
                        }
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
                    File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMapsFolder);
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
                            if (isCancelled()){
                                mapSuccess = false;
                                break;
                            }
                            else {
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
                            }


                            publishProgress("" + (int) (90 + ((++zipCounter * 10) / nEntries)));

                            zipIn.closeEntry();
                            entry = zipIn.getNextEntry();
                        }
                        zipIn.close();


                    } catch (Exception ex) {
                        mapSuccess = false;
                        ex.printStackTrace();
                    }


                } catch (Exception e) {
                    mapSuccess = false;
                }
if(mapSuccess){
    PropertyHolder.setLastUpdateGeneralMapNow();
}

// GENERAL REFS


                boolean grSuccess = true;
                try {

                    URL url = new URL(Util.getUrlGeneralReferences(context));

                    Log.d("ANDRO_ASYNC", "uRL: " + url.toString());

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoOutput(true);
                    conn.connect();

                    int lenghtOfFile = conn.getContentLength();
                    Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                    Log.d("ANDRO_ASYNC", "SD path: " + Environment.getExternalStorageDirectory().getPath());

                    File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/general_references.zip");
                    String destinationPath = destinationFile.getPath();
                    Log.d("ANDRO_ASYNC", "Save path: " + destinationPath);

                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(destinationPath);

                    byte data[] = new byte[1024];

                    int total = 0;
                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()){
                            grSuccess = false;
                            break;}
                        else {
                            output.write(data, 0, count);
                            total += count;
                            publishProgress("" + (int) ((total * 90) / lenghtOfFile));

                        }
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
                    File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.generalReferencesFolder);
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
                            if (isCancelled()){
                                grSuccess= false;
                                break;}
                            else {
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
                            }

                            publishProgress("" + (int) (90 + ((++zipCounter * 10) / nEntries)));


                            zipIn.closeEntry();
                            entry = zipIn.getNextEntry();
                        }
                        zipIn.close();


                    } catch (Exception ex) {
                        grSuccess = false;
                        ex.printStackTrace();
                    }

                } catch (Exception e) {
                    grSuccess = false;

                }

            if(grSuccess){
                PropertyHolder.setLastUpdateGeneralReferencesNow();
            }


            // ROUTE MEDIA

            List<Route> routes = DataContainer.getAllOfficialRoutes(app.getDataBaseHelper());

            Log.e("ROUTES:", "size: "  + routes.size());

            for(Route route: routes){

                boolean routeSuccess=true;
                try {

                    URL url = new URL(Util.getUrlRouteContent(context, route.getId(), route.getRouteContentLastUpdated()));
                    Log.d("ANDRO_ASYNC", "uRL: " + url.toString());

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoOutput(true);
                    conn.connect();

                    int lenghtOfFile = conn.getContentLength();
                    Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                    Log.d("ANDRO_ASYNC", "SD path: " + Environment.getExternalStorageDirectory().getPath());

                    File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route.zip");
                    String destinationPath = destinationFile.getPath();
                    Log.d("ANDRO_ASYNC", "Save path: " + destinationPath);

                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(destinationPath);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        if (isCancelled()){
                         routeSuccess = false;
                            break;
                        }
                        else {

                            total += count;
                            publishProgress("" + (int) ((total * 90) / lenghtOfFile));
                            output.write(data, 0, count);
                        }
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
                    File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/route_" + route.getId());
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
                            if (isCancelled()){
                                routeSuccess = false;
                                break;}
                            else {

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
                            }
                            publishProgress("" + (int) (90 + ((++zipCounter * 10) / nEntries)));

                            zipIn.closeEntry();
                            entry = zipIn.getNextEntry();
                        }
                        zipIn.close();

                    } catch (Exception ex) {
Log.e("Exception", "asdasd");

                        routeSuccess = false;
                        ex.printStackTrace();
                    }

                } catch (Exception e) {
                    Log.e("Exception", "asdaasdas");

                    routeSuccess = false;
                }

if(routeSuccess){
    route.setRouteContentLastUpdatedNow();
    dataBaseHelper.getRouteDataDao().update(route);

}
            }

            return null;

        }

        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);

            String srTitle;
            String srMessage;



            if (Util.hasMinimumContents(context, app)) {
                srTitle = "Success!";
                srMessage = "Eth Holet has all the data he needs. You can go ahead and start hiking!";
            } else {
                srTitle = "We had some trouble...";
                srMessage = "Eth Holet was not able to get all of the data he needs from the server. Please check you internet connection and try again.";

            }

            AlertDialog.Builder syncReport = new AlertDialog.Builder(
                    Switchboard.this);
            syncReport.setIcon(R.drawable.ic_launcher);
            syncReport.setTitle(srTitle);
            syncReport.setMessage(srMessage);
            syncReport.setNegativeButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            syncReport.show();

        }
    }


}
