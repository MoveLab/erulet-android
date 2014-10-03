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

    Context context;

    public boolean hasGRs;
    public boolean hasMaps;
    public boolean hasJson;

    DataBaseHelper dataBaseHelper;

    ContentResolver cr;
    Cursor c;

	static final int REGISTRATION_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        context = getApplicationContext();





        if (app == null) { app = (EruletApp) getApplicationContext(); }
		setContentView(R.layout.activity_switchboard);
		initButtons();
		//See if we need to display the user registration
		mPreferences = getSharedPreferences("EruletPreferences", MODE_PRIVATE);
        
        boolean firstTime = mPreferences.getBoolean("first_time", true);


        // TODO JUST FOR TESTING
//        mPreferences.edit().putBoolean("has_maps", false).apply();
  //      mPreferences.edit().putBoolean("has_grs", false).apply();
    //    mPreferences.edit().putBoolean("has_json", false).apply();


        hasMaps = mPreferences.getBoolean("has_maps", false);
        hasGRs = mPreferences.getBoolean("has_grs", false);
        hasJson = mPreferences.getBoolean("has_json", false);

        startInitialSync();

        // TODO Put this back in -- I have it out just for initial testing
//        tryToRegister();
                
        if (firstTime) {
        	//Show user manual, maybe?
        	//Toast.makeText(this, "First time!", Toast.LENGTH_SHORT).show();
        	mPreferences.edit().putBoolean("first_time", false).apply();

        }
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REGISTRATION_REQUEST) {
			if(resultCode == RESULT_OK){
				//The user registered ok
				//We write it to preferences, along with any other necessary data
				mPreferences.edit().putBoolean("registered_user", true).apply();				
				//And here we store all necessary tokens/etc for the user
			} else if (resultCode == RESULT_CANCELED) {
				//No luck
				/** ************************************************************
				 *  ************************************************************
				 *               JUST FOR TEST
				 *               REMOVE THIS ASAP
				 *  ************************************************************
				 *  ************************************************************
				 */
				mPreferences.edit().putBoolean("registered_user", true).apply();
			}
		}
	}	
	
	
	private void tryToRegister(){
		boolean userIsRegistered = mPreferences.getBoolean("registered_user", false);        
        if(!userIsRegistered){        	
        	if(Util.isOnline(getBaseContext())){
        		Intent intent = new Intent(this, RegistrationActivity.class);
				startActivityForResult(intent,REGISTRATION_REQUEST);
        	}else{
        		Toast.makeText(this, getString(R.string.no_data_access), Toast.LENGTH_SHORT).show();
        	}
        }else{
        	Toast.makeText(this, getString(R.string.already_registered), Toast.LENGTH_SHORT).show();
        }
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(group1, first_id, first_id, getString(R.string.preferences));
		menu.add(group1, second_id, second_id, getString(R.string.register_user));
		//getMenuInflater().inflate(R.menu.switchboard, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				Intent i = new Intent(Switchboard.this,EruletPreferencesActivity.class);				
				startActivity(i);
				break;
			case 2:
				tryToRegister();
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
						SecurityRules.class);
				startActivity(i);
			}
		});
		btn_routes = (Button) findViewById(R.id.btn_routes);
		btn_routes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
        if(mPreferences.getBoolean("has_maps", false) &&
                 mPreferences.getBoolean("has_grs", false) &&
                mPreferences.getBoolean("has_json", false)
   ){
				Intent i = new Intent(Switchboard.this,
						ChooseItineraryActivity.class);
				startActivity(i);}
                else{
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
        new InitialSyncAsync().execute();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Holet is performing an initial sync with the server...stay connected to the internet ");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
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
if(!hasJson){
    publishProgress(""+1);

    if(dataBaseHelper == null){
                dataBaseHelper = OpenHelperManager.getHelper(context, DataBaseHelper.class);
            }

            String jsonArrayString = Util.getJSON(UtilLocal.API_ROUTES, context);
            Util.logInfo(context, "API", jsonArrayString);
            try {
                JSONArray ja = null;
                try {
                    ja = new JSONArray(jsonArrayString);
                    publishProgress(""+5);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ArrayList<Route> routes = JSONConverter.jsonArrayToRouteArray(ja);
                int nDone = 0;
                int totalRoutes = routes.size();
                for(Route route: routes){
                    DataContainer.insertRoute(route, dataBaseHelper, "this_id");
                    publishProgress(""+(5 + (int)((nDone*95))/(totalRoutes)));

                }

                mPreferences.edit().putBoolean("has_json", true).apply();


            } catch (Exception e) {}

}
            // MAPS
if(!hasMaps){
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

                int total = 0;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);

                    total += count;
                    publishProgress(""+(int)((total*90)/lenghtOfFile));
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


                        publishProgress(""+(int)(90+ ((++zipCounter*10)/nEntries)));

                        zipIn.closeEntry();
                        entry = zipIn.getNextEntry();
                    }
                    zipIn.close();


                    mPreferences.edit().putBoolean("has_maps", true).apply();



                } catch (Exception ex) {
                    // some errors occurred
                    ex.printStackTrace();
                }



            } catch (Exception e) {}
}
// GENERAL REFS

            if(!hasGRs){

            try {

                URL url = new URL("http://107.170.174.182/media/holet/zipped_general_references/general_references.zip");

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
                    output.write(data, 0, count);
                    total += count;
                    publishProgress(""+(int)((total*90)/lenghtOfFile));

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
                File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder+ "/" + Util.generalReferencesFolder);
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

                        publishProgress(""+(int)(90+ ((++zipCounter*10)/nEntries)));


                        zipIn.closeEntry();
                        entry = zipIn.getNextEntry();
                    }
                    zipIn.close();
                    mPreferences.edit().putBoolean("has_grs", true).apply();




                } catch (Exception ex) {
                    // some errors occurred
                    ex.printStackTrace();
                }

            } catch (Exception e) {}


}


            return null;

        }
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
        }
    }


}
