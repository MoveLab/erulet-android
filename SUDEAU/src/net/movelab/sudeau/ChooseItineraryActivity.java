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
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.RBB;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
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
import com.google.android.gms.maps.SupportMapFragment;
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

public class ChooseItineraryActivity extends FragmentActivity {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

	private GoogleMap mMap;
	private MapBoxOfflineTileProvider tileProvider;
//	private static final LatLng MY_POINT = new LatLng(41.66, 1.54);
//	private static final LatLng VALL_ARAN_1 = new LatLng(42.74, 0.79);
//	private static final LatLng VALL_ARAN_2 = new LatLng(42.73, 0.82);		
	//private DataBaseHelper dataBaseHelper;
	private Hashtable<Marker, RBB> routeTable;
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
    private int third_id = Menu.FIRST+2;
	private ProgressBar progressBar;
	
	private EruletApp app;

	private static final String TAG = "ChooseItineraryActivity";

    private String currentLocale;

    private boolean isUserItinerariesOn = false;

    Context context;

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

        context = getApplicationContext();

        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);

        isUserItinerariesOn = PropertyHolder.isUserItinerariesOn();

        currentLocale = PropertyHolder.getLocale();
        progressBar = (ProgressBar) findViewById(R.id.pbChooseItinerary);
		//setUpDBIfNeeded();
		setUpMapIfNeeded();
		setUpCamera();
	}
	
	@Override
	protected void onResume() {	
		super.onResume();
		refreshMapView();
        currentLocale = PropertyHolder.getLocale();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){		
		menu.add(group1,first_id,first_id,getString(R.string.choose_it_my_itineraries));
        menu.add(group1, second_id, second_id, getString(R.string.preferences));

        menu.add(group1, third_id, third_id, userItineraryMenuItemText(isUserItinerariesOn));

        // TODO decide if we want this in - I am removing it for now
//		menu.add(group1,second_id,second_id,getString(R.string.choose_it_shared_itineraries));
		return super.onCreateOptionsMenu(menu);
	}

    public static String userItineraryMenuItemText(boolean ison){
        if(ison)
            return "Hide my routes";
        else
            return "Show my routes";
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
                Intent i = new Intent(ChooseItineraryActivity.this, EruletPreferencesActivity.class);
                startActivity(i);
			    return true;
            case 3:
                PropertyHolder.setUseritinerariesOn(!isUserItinerariesOn);
                isUserItinerariesOn = !isUserItinerariesOn;
                item.setTitle(userItineraryMenuItemText(isUserItinerariesOn));
                mMap.clear();
                mMap = null;
                setUpMapIfNeeded();
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
        final RBB rbb = routeTable.get(selectedMarker);
            // rbb[2] is localized name
            builder.setTitle(rbb.name);
            builder.setIcon(R.drawable.ic_pin_info);
            // rbb[3] is localized description
            Log.i("rbb4", Float.toString(rbb.globalRating));
            if(rbb.globalRating>=0)
                builder.setMessage(Html.fromHtml(rbb.description + "<br><br><b>Average Rating: " + Float.toString(rbb.globalRating)+ "</b>"));
            else
                builder.setMessage(rbb.description);
            builder.setNegativeButton(getString(R.string.trip_option_1), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(ChooseItineraryActivity.this,
                            DetailItineraryActivity.class);
                    // rbb[0] is route id as string
                    intent.putExtra("idRoute", rbb.id);
                    intent.putExtra("mode",0);
                    dialogInterface.dismiss();
                    startActivity(intent);				                }
            });
            builder.setNeutralButton(getString(R.string.trip_option_2), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(ChooseItineraryActivity.this,
                            DetailItineraryActivity.class);
                    // rbb[0] is route ID as string
                    intent.putExtra("idRoute",rbb.id);
                    intent.putExtra("mode",1);
                    dialogInterface.dismiss();
                    finish();
                    startActivity(intent);				                }
            });
            // this will be only for super users - need to set up check
            if(false){
            builder.setPositiveButton(getString(R.string.trip_option_3), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(ChooseItineraryActivity.this,
                            DetailItineraryActivity.class);
                    // rbb[0] is route ID as string
                    intent.putExtra("idRoute",rbb.id);
                    intent.putExtra("mode",2);
                    dialogInterface.dismiss();
                    startActivity(intent);
                    finish();
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
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// addMarkers
				addRouteMarkersFromDB();
			}
			if (mMap != null) {
				tileProvider = initTileProvider(context);
				if(tileProvider!=null){
					TileOverlay tileOverlay = mMap
							.addTileOverlay(new TileOverlayOptions()
									.tileProvider(tileProvider));
					tileOverlay.setVisible(true);
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
                    showItineraryOptions();
                    return true;
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
			

		}}
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
		tileProvider = initTileProvider(context);
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
			RouteDrawerWorkerTask task = new RouteDrawerWorkerTask(mMap,progressBar);
			task.execute(app);
	}

	private void addRouteMarkersFromDB() {
        List<RBB> routesBareBones;
        if(isUserItinerariesOn){
		routesBareBones = DataContainer.getAllRoutesBareBones(app.getDataBaseHelper(), currentLocale);
        } else{
            routesBareBones = DataContainer.getOfficialRoutesBareBones(app.getDataBaseHelper(), currentLocale);
        }
		if(routeTable == null){
			routeTable = new Hashtable<Marker, RBB>();
		}
		for(RBB rbb : routesBareBones){
            Log.i("rbb orig", "rbb4: " + Float.toString(rbb.globalRating));
			LatLng middle = DataContainer.getRouteMiddleFast(Integer.toString(rbb.trackId), app.getDataBaseHelper());
			if(middle!=null){
				Marker my_marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(middle.latitude, middle.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(rbb.official?R.drawable.itinerary_marker:R.drawable.itinerary_marker_user)));
				routeTable.put(my_marker, rbb);
			}
		}
	}

	private MapBoxOfflineTileProvider initTileProvider(Context context) {
        AssetManager am = context.getAssets();
        File sdcard = new File(Environment.getExternalStorageDirectory(),
                Util.baseFolder + "/" + Util.routeMapsFolder);


        File f = new File(PropertyHolder.getGeneralMapPath());

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
		}
		return null;
	}



}
