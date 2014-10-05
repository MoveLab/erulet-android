package net.movelab.sudeau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.URL;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class ChooseItineraryActivity extends Activity {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

	private GoogleMap mMap;
	private MapBoxOfflineTileProvider tileProvider;
//	private static final LatLng MY_POINT = new LatLng(41.66, 1.54);
//	private static final LatLng VALL_ARAN_1 = new LatLng(42.74, 0.79);
//	private static final LatLng VALL_ARAN_2 = new LatLng(42.73, 0.82);		
	//private DataBaseHelper dataBaseHelper;
	private Hashtable<Marker, Route> routeTable;
	private Marker selectedMarker;
	private List<Route> routes;
	
	//private static final String TITLE = "Tria la opci� de ruta que prefereixes:";
	private String TITLE;
 	private String OPTION_1;
	private String OPTION_2;
	private String OPTION_3;
	
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST+1;
	private ProgressBar progressBar;
	
	private EruletApp app;

    private SharedPreferences mPreferences;

// TODO fix this... move all this logic to DB
	public boolean r7downloaded;

	private static final String TAG = "ChooseItineraryActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TITLE = getString(R.string.trip_option);
        OPTION_1 = getString(R.string.trip_option_1);
		OPTION_2 = getString(R.string.trip_option_2);
		OPTION_3 = getString(R.string.trip_option_3);
		setContentView(R.layout.choose_itinerary_map);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        mPreferences = getSharedPreferences("EruletPreferences", MODE_PRIVATE);

        // TODO just for testing
        mPreferences.edit().putBoolean("r7d", false).apply();

        r7downloaded = mPreferences.getBoolean("r7d", false);

        progressBar = (ProgressBar) findViewById(R.id.pbChooseItinerary);
		//setUpDBIfNeeded();
		setUpMapIfNeeded();
		setUpCamera();
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		refreshMapView();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){		
		menu.add(group1,first_id,first_id,getString(R.string.choose_it_my_itineraries));		
		menu.add(group1,second_id,second_id,getString(R.string.choose_it_shared_itineraries));
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:
				Intent i1 = new Intent(ChooseItineraryActivity.this,
						MyItinerariesActivity.class);				
				startActivity(i1);
			    return true;	
			case 2:
				Intent i2 = new Intent(ChooseItineraryActivity.this,
						OtherItinerariesActivity.class);				
				startActivity(i2);
			    return true;
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(tileProvider!=null){
			tileProvider.close();
		}
	}
	
	public void showItineraryOptions(){
		CharSequence[] items = null;
		if(app.isPrivilegedUser()){
			items = new CharSequence[]{OPTION_1,OPTION_2,OPTION_3};
		}else{
			items = new CharSequence[]{OPTION_1,OPTION_2};
		}		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(r7downloaded){
            builder.setTitle(TITLE);
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
					Intent i = new Intent(ChooseItineraryActivity.this,
						DetailItineraryActivity.class);
					Route r = routeTable.get(selectedMarker);
					i.putExtra("idRoute",r.getId());
					i.putExtra("mode",which);
					dialog.dismiss();
					startActivity(i);					
				}
			}
		);
        } else{
            builder.setTitle("Get Route Content");
            builder.setMessage("You have not yet downloaded the content for this route to your phone. Would you like to download it now?");
            builder.setPositiveButton("Download content", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startDownload();
                }
            });
        }
		builder.show();		
	}
	
//	private void setUpDBIfNeeded() {
//		if(dataBaseHelper == null){
//			dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);
//			//DataContainer.loadSampleData(dataBaseHelper, this.getBaseContext());			
//			DataContainer.loadRedonCompact(dataBaseHelper, this.getBaseContext());
//		}
//	}	

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// addMarkers
				addRouteMarkersFromDB();
			}
			if (mMap != null) {
				tileProvider = initTileProvider();
				if(tileProvider!=null){
					TileOverlay tileOverlay = mMap
							.addTileOverlay(new TileOverlayOptions()
									.tileProvider(tileProvider));
					tileOverlay.setVisible(true);
				}
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);			
			mMap.setOnMapClickListener(new OnMapClickListener() {				
				@Override
				public void onMapClick(LatLng point) {
					selectedMarker=null;					
				}
			});
			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {				
				@Override
				public boolean onMarkerClick(Marker marker) {
					selectedMarker=marker;
					Route r = routeTable.get(marker);					
					return false;
				}
			});
			
			mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker arg0) {
					if(selectedMarker==null){
						Toast.makeText(getApplicationContext(), getString(R.string.no_route_selected), Toast.LENGTH_LONG).show();
					}else{
						showItineraryOptions();										
					}
				}
			});
			
			mMap.setInfoWindowAdapter(new InfoWindowAdapter() {
				
				@Override
				public View getInfoWindow(Marker marker) {
					return null;
				}
				
				@Override
				public View getInfoContents(Marker marker) {
					View myContentView = getLayoutInflater().inflate(R.layout.custominfowindow, null); 
					TextView snippet = (TextView) myContentView.findViewById(R.id.info_snippet);
					TextView title = (TextView) myContentView.findViewById(R.id.info_title);
					Route r = routeTable.get(marker);
					r = DataContainer.refreshRoute(r,app.getDataBaseHelper());
		            snippet.setText(r.getDescription());
		            title.setText(r.getName());
		            ImageView picture = (ImageView)myContentView.findViewById(R.id.info_pic);
		            picture.setImageResource(R.drawable.ic_pin_info);		            
		            return myContentView;
				}
			});						
						
		}
	}
	
	private void setUpCamera(){				
		LatLngBounds bounds = new LatLngBounds.Builder().include(Util.ESTANH_REDON).build();                
		if(routeTable.size()>0){
			if(routeTable.size()==1){
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Util.ESTANH_REDON, 11));
			}else{
				Enumeration<Marker> markers = routeTable.keys();
				while(markers.hasMoreElements()){
					Marker m = markers.nextElement();
					bounds = bounds.including(m.getPosition());
					Util.fitMapViewToBounds(mMap, getBaseContext(), bounds, 20);
//					int[] screen_sizes = Util.getScreenSize(getBaseContext());
//					int wsize = Util.getSmallerDimension(screen_sizes);
//					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,wsize,wsize,20));
					//mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,200,200,20));
					//mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));
					//mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 10));
				}
			}									
		}		
	}
	
	private void refreshMapView(){
		mMap.clear();
		if(tileProvider!=null){
			tileProvider.close();
		}
		tileProvider = initTileProvider();
		if(tileProvider!=null){
			TileOverlay tileOverlay = mMap
					.addTileOverlay(new TileOverlayOptions()
							.tileProvider(tileProvider));
			tileOverlay.setVisible(true);
		}
		addRouteMarkersFromDB();
		asyncLoadRouteTracks();
		setUpCamera();
	}
	
	private void asyncLoadRouteTracks(){
		if(progressBar!=null){
        	progressBar.setVisibility(View.VISIBLE);
        }
		if(routes!=null){
			JSONArray route_j_list = new JSONArray();
			for(int i=0; i<routes.size();i++){
				Route r = routes.get(i);
				JSONObject route_j;
				try {
					route_j = JSONConverter.routeToJSONObject(r);
					route_j_list.put(route_j);														
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			String params = route_j_list.toString();
			RouteDrawerWorkerTask task = new RouteDrawerWorkerTask(mMap,app,progressBar);
			task.execute(params);
		}else{
			if(progressBar!=null){
	        	progressBar.setVisibility(View.GONE);
	        }
		}
	}

	private void addRouteMarkersFromDB() {
		// custom icon
		routes = DataContainer.getAllRoutes(app.getDataBaseHelper());
		if(routeTable == null){
			routeTable = new Hashtable<Marker, Route>();
		}
		for(int i=0; i < routes.size(); i++){			
			Route r = routes.get(i);
			Step start = DataContainer.getRouteStarterFast(r, app.getDataBaseHelper());
			if(start!=null){
//				Marker my_marker = mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(start.getLatitude(), start.getLongitude()))
//				.title(r.getName())
//				.snippet(null)
//				.icon(BitmapDescriptorFactory
//						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				IconGenerator ic = new IconGenerator(getBaseContext());
				ic.setTextAppearance(R.style.BubbleFont);
				//Marker my_marker = MapObjectsFactory.addStartRouteMarker(mMap, new LatLng(start.getLatitude(), start.getLongitude()), r.getName());
				Marker my_marker = mMap.addMarker( new MarkerOptions()
				.position(new LatLng(start.getLatitude(), start.getLongitude()))
				.title(r.getName())
				.snippet(null)
				.icon( BitmapDescriptorFactory.fromBitmap(ic.makeIcon(r.getName())))); 
				routeTable.put(my_marker, r);
			}
		}
	}

	private MapBoxOfflineTileProvider initTileProvider() {
		File f = new File(Environment.getExternalStorageDirectory(), Util.baseFolder + "/" + Util.routeMapsFolder + "/Vista_general_vielha.mbtiles");
		//File f = new File(getCacheDir() + "/Vista_general_vielha.mbtiles");
//		if (!f.exists())
//			try {
//				InputStream is = getAssets().open(
//						"Vista_general_vielha.mbtiles");
//				int size = is.available();
//				byte[] buffer = new byte[size];
//				is.read(buffer);
//				is.close();
//				FileOutputStream fos = new FileOutputStream(f);
//				fos.write(buffer);
//				fos.close();
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
		if (f.exists()){
			return new MapBoxOfflineTileProvider(f.getPath());
		}else{
			Log.d(TAG,"Fitxer cartografia no trobat " + f.getAbsolutePath());
		}
		return null;
	}


    private void startDownload() {
        // TODO unhardcode the route number and pass it instead
        String url = "http://107.170.174.182/media/holet/zipped_routes/content_route7.zip";
        new DownloadFileAsync().execute(url);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Downloading route content...please wait");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }


    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(DIALOG_DOWNLOAD_PROGRESS);
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {

                URL url = new URL(aurl[0]);
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
                    total += count;
                    publishProgress(""+(int)((total*90)/lenghtOfFile));
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
// TODO take out hard coded route 7
                File target_directory = new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/route_7");
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

                    mPreferences.edit().putBoolean("r7d", true).apply();
                    r7downloaded = true;

                } catch (Exception ex) {
                    // some errors occurred
                    ex.printStackTrace();
                }

            } catch (Exception e) {}


            return null;

        }
        protected void onProgressUpdate(String... progress) {
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            showItineraryOptions();
        }
    }

}
