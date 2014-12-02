package net.movelab.sudeau;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

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
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class Switchboard extends FragmentActivity {

    private Button btn_manual;
    private Button btn_security;
    private Button btn_routes;
    private Button btn_credits;
    private EruletApp app;


    Context context;

    DataBaseHelper dataBaseHelper;

    ContentResolver cr;
    Cursor c;

    static final int REGISTRATION_REQUEST = 1;

    InitialSyncAsync initialSyncAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        setContentView(R.layout.activity_switchboard);
        initButtons();

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);


        // check route size and force db updates at top
        List<Route> routes = DataContainer.getAllOfficialRoutes(app.getDataBaseHelper());

        if (PropertyHolder.isFirstTime()) {

            // TODO tryToRegister();
            // check that auto id is working
            showWelcomeDialog();
            PropertyHolder.setFirstTime(false);

            // if first time is called after this update has been written no need for sync fix
            PropertyHolder.setNeedsSyncFix(false);

        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PropertyHolder.getTripInProgressTracking() >= 0) {
            Intent intent = new Intent(Switchboard.this,
                    DetailItineraryActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void tryToRegister() {

        boolean userIsRegistered = PropertyHolder.isRegistered();
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


    private void tryToLogin() {
        if (Util.isOnline(getBaseContext())) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.no_data_access), Toast.LENGTH_SHORT).show();
        }
    }


    private int group1 = 1;
    private int first_id = Menu.FIRST;
    private int second_id = Menu.FIRST + 1;
    private int third_id = Menu.FIRST + 2;
    private int fourth_id = Menu.FIRST + 3;
    private int fifth_id = Menu.FIRST + 4;
    private int sixth_id = Menu.FIRST + 5;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(group1, first_id, first_id, getString(R.string.preferences));
        menu.add(group1, second_id, second_id, getString(R.string.register_user));
        menu.add(group1, third_id, third_id, getResources().getString(R.string.home_login));
        menu.add(group1, fourth_id, fourth_id, getResources().getString(R.string.sync));
        menu.add(group1, fifth_id, fifth_id, getString(R.string.choose_it_my_itineraries));
        menu.add(group1, sixth_id, sixth_id, getString(R.string.take_survey));

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
                tryToLogin();
                break;
            case 4:
                startInitialSync();
                break;
            case 5:
                Intent i1 = new Intent(Switchboard.this,
                        MyItinerariesActivity.class);
                startActivity(i1);
                break;
            case 6:
                Intent survey_intent = new Intent(Switchboard.this, SurveyActivity.class);
                survey_intent.putExtra(SurveyActivity.SURVEY_TYPE_KEY, "general_survey");
                startActivity(survey_intent);
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
        builderSingle.setTitle(getResources().getString(R.string.still_syncing_title));
        builderSingle.setMessage(getResources().getString(R.string.still_syncing_message));
        builderSingle.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setNeutralButton(getResources().getString(R.string.sync_later),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Switchboard.this,
                                ChooseItineraryActivity.class);
                        startActivity(i);
                    }
                });
        builderSingle.setPositiveButton(getResources().getString(R.string.sync_now),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startInitialSync();
                    }
                });

        builderSingle.show();
    }

    private void showWelcomeDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(
                Switchboard.this);
        b.setIcon(R.drawable.ic_launcher);
        b.setTitle(getResources().getString(R.string.welcome_title));
        b.setMessage(getResources().getString(R.string.welcome_message));
        b.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startInitialSync();
                dialogInterface.dismiss();
            }
        });
        b.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        b.show();
    }


    private void startInitialSync() {

        if (Util.isOnline(context)) {
            new InitialSyncAsync().execute(context);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.internet_needed_title));
            builder.setIcon(R.drawable.ic_erulet_new);
            builder.setMessage(getResources().getString(R.string.internet_needed_message_content));

            builder.setCancelable(true);
            builder.show();
        }

    }


    class InitialSyncAsync extends AsyncTask<Context, Integer, Boolean> {

        ProgressDialog prog;

        int myProgress;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            prog = new ProgressDialog(context);
            prog.setTitle(getResources().getString(R.string.syncing));
            prog.setIndeterminate(false);
            prog.setMax(100);
            prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            prog.show();

            myProgress = 0;

        }

        @Override
        protected Boolean doInBackground(Context... context) {
            int count;

            if (dataBaseHelper == null) {
                dataBaseHelper = OpenHelperManager.getHelper(context[0], DataBaseHelper.class);
            }

            // JSON
// TODO testing - iimplement better test for needing update
            if (true) {

                myProgress = 10;
                publishProgress(myProgress);

                myProgress += 20;
                publishProgress(myProgress);
                String jsonArrayString = Util.getJSON(UtilLocal.API_ROUTES, context[0]);
                Util.logInfo(context[0], "API", jsonArrayString);
                myProgress += 20;
                publishProgress(myProgress);
                try {
                    JSONArray ja = null;
                    try {
                        ja = new JSONArray(jsonArrayString);
                        myProgress += 10;
                        publishProgress(myProgress);

                    } catch (JSONException e) {
                        Util.logError(context[0], "JSON download 1", "" + e);
                    }
                    ArrayList<Route> these_routes = JSONConverter.jsonArrayToRouteArray(ja, app.getDataBaseHelper());
                    myProgress += 20;
                    publishProgress(myProgress);
                    int nDone = 0;
                    int totalRoutes = these_routes.size();
                    for (Route route : these_routes) {
                        if (isCancelled()) {
                            break;
                        } else {
                            Route existingRoute = DataContainer.findRouteByServerId(route.getServerId(), dataBaseHelper);
                            if (existingRoute == null) {
                                DataContainer.insertRoute(route, dataBaseHelper);
                            } else {
                                DataContainer.updateRouteFromServer(route, existingRoute, app);
                            }
                        }
                    }
                    myProgress += 20;
                    publishProgress(myProgress);

                } catch (Exception e) {
                    Util.logError(context[0], "JSON download 2", "" + e);
                }
            }
            //General Maps
            boolean mapSuccess = true;
            try {
                HttpResponse response = Util.getResponse(Util.getUrlGeneralMap(context[0]), 180000);
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
                        int total = 0;
                        while ((count = input.read(data)) != -1) {
                            if (isCancelled()) {
                                mapSuccess = false;
                                break;
                            } else {
                                output.write(data, 0, count);
                                total += count;
                                myProgress = (int) ((total * 90) / fileSize);
                                publishProgress(myProgress);
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
                                if (isCancelled()) {
                                    mapSuccess = false;
                                    break;
                                } else {
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
                                }
                                myProgress += (int) ((++zipCounter * 10) / nEntries);
                                publishProgress(myProgress);
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


//Route Maps
            List<Route> routes = DataContainer.getAllOfficialRoutes(app.getDataBaseHelper());
            for (Route route : routes) {
                boolean routeMapSuccess = true;
                if (route.getLocalCarto() != null && !route.getLocalCarto().equals("") && !route.getLocalCarto().equals("none")) {
                    try {
                        HttpResponse response = Util.getResponse(Util.getUrlRouteMap(route), 180000);
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
                                int total = 0;
                                while ((count = input.read(data)) != -1) {
                                    if (isCancelled()) {
                                        routeMapSuccess = false;
                                        break;
                                    } else {
                                        output.write(data, 0, count);
                                        total += count;
                                        myProgress = (int) ((total * 90) / fileSize);
                                        publishProgress(myProgress);
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
                                        if (isCancelled()) {
                                            routeMapSuccess = false;
                                            break;
                                        } else {
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
                                        }
                                        myProgress += (int) ((++zipCounter * 10) / nEntries);
                                        publishProgress(myProgress);
                                        zipIn.closeEntry();
                                        entry = zipIn.getNextEntry();
                                    }
                                    zipIn.close();
                                } catch (Exception ex) {
                                    routeMapSuccess = false;
                                }

                            } else {
                            }
                        } else {
                            routeMapSuccess = false;
                        }
                    } catch (ClientProtocolException e) {
                        routeMapSuccess = false;

                    } catch (IOException e) {
                        routeMapSuccess = false;
                    }
                    if (routeMapSuccess) {
                        route.setLocalCartoLastUpdatedNow();
                    }
                }
            }
// GENERAL REFS


            boolean grSuccess = true;
            try {


                HttpResponse response = Util.getResponse(Util.getUrlGeneralReferences(context[0]), 180000);
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
                        int total = 0;
                        while ((count = input.read(data)) != -1) {
                            if (isCancelled()) {
                                grSuccess = false;
                                break;
                            } else {
                                output.write(data, 0, count);
                                total += count;
                                myProgress = (int) ((total * 90) / fileSize);
                                publishProgress(myProgress);
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
                                if (isCancelled()) {
                                    grSuccess = false;
                                    break;
                                } else {
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
                                }
                                myProgress += (int) ((++zipCounter * 10) / nEntries);
                                publishProgress(myProgress);
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

            // ROUTE MEDIA
            for (Route route : routes) {
                boolean routeSuccess = true;
                try {
                    HttpResponse response = Util.getResponse(Util.getUrlRouteContent(context[0], route.getServerId(), route.getRouteContentLastUpdated()), 900000);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        if (response.containsHeader("Content-Length")) {
                            Log.d("Route Media status code: ", "" + statusCode);
                            int fileSize = Integer.parseInt(response.getFirstHeader("Content-Length").getValue());
                            HttpEntity entity = response.getEntity();
                            File destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route_map.zip");
                            String destinationPath = destinationFile.getPath();
                            destinationFile = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/route.zip");
                            destinationPath = destinationFile.getPath();
                            entity = response.getEntity();
                            InputStream input = new BufferedInputStream(entity.getContent());
                            OutputStream output = new FileOutputStream(destinationPath);
                            byte data[] = new byte[1024];

                            long total = 0;
                            while ((count = input.read(data)) != -1) {
                                if (isCancelled()) {
                                    routeSuccess = false;
                                    break;
                                } else {
                                    total += count;
                                    myProgress = (int) ((total * 90) / fileSize);
                                    publishProgress(myProgress);
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
                            File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder);
                            String destDirectory = target_directory.getPath();

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
                                    if (isCancelled()) {
                                        routeSuccess = false;
                                        break;
                                    } else {

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
                                    }
                                    myProgress += (int) ((++zipCounter * 10) / nEntries);
                                    publishProgress(myProgress);

                                    zipIn.closeEntry();
                                    entry = zipIn.getNextEntry();
                                }
                                zipIn.close();
                            } catch (Exception ex) {
                                Log.e("Exception", "asdasd");

                                routeSuccess = false;
                                ex.printStackTrace();
                            }
                        } else {
                            Log.e("Maps", "entity: " + response.getEntity().getContent());
                        }
                    } else {
                        Log.e("NEWTRY", "failed to get map");
                        routeSuccess = false;
                    }
                } catch (ClientProtocolException e) {
                    Log.e("NEWTRY", "error: " + e);
                    routeSuccess = false;
                } catch (IOException e) {
                    routeSuccess = false;
                    Log.e("NEWTRY", "error: " + e);
                }
                if (routeSuccess) {
                    route.setRouteContentLastUpdatedNow();
                    DataContainer.updateRoute(route, app.getDataBaseHelper());
                }
            }
            return true;
        }

        protected void onProgressUpdate(Integer... progress) {
            prog.setProgress(progress[0]);
        }


        protected void onPostExecute(Boolean result) {

            try {
                prog.dismiss();
                prog = null;
            } catch (Exception e) {
                // I realize this is ugly, but it is a solution to the problem
                // discussed here:
                // https://stackoverflow.com/questions/2745061/java-lang-illegalargumentexception-view-not-attached-to-window-manager/5102572#5102572
            }

            String srTitle;
            String srMessage;

            if (Util.hasMinimumContents(context, app)) {
                srTitle = getResources().getString(R.string.sync_success_title);
                srMessage = getResources().getString(R.string.sync_success_message);
            } else {
                srTitle = getResources().getString(R.string.sync_error_title);
                srMessage = getResources().getString(R.string.sync_error_message);

            }

            AlertDialog.Builder syncReport = new AlertDialog.Builder(
                    Switchboard.this);
            syncReport.setIcon(R.drawable.ic_launcher);
            syncReport.setTitle(srTitle);
            syncReport.setMessage(srMessage);
            syncReport.setNegativeButton(getResources().getString(R.string.ok),
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
