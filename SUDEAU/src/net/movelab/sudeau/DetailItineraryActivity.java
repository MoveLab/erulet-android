package net.movelab.sudeau;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import net.movelab.sudeau.util.MarkerAnimationTest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Debug;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

public class DetailItineraryActivity extends Activity 
	implements 	GooglePlayServicesClient.ConnectionCallbacks,
				GooglePlayServicesClient.OnConnectionFailedListener {

	private GoogleMap mMap;
	private MapBoxOfflineTileProvider tileProvider;
	private Marker selectedMarker;
	private Marker lastPositionMarker;	
	private String stepBeingEditedId;
	private Step currentStep;
	// Route selected in choose itinerary activity
	private Route selectedRoute;	
	private Polyline selectedRoutePolyLine;
	private List<Circle> selectedRoutePoints;
	private List<Step> selectedRouteSteps;
	private List<Marker> directionMarkers;
	private Marker arrivalMarker;
	private Marker startMarker;
	
	private Route routeInProgress;
	private ArrayList<Step> highLightedSteps;
	private IntentFilter fixFilter;
	private FixReceiver fixReceiver;
	private Hashtable<Marker, Step> selectedRouteMarkers;
	private Hashtable<Marker, Step> routeInProgressMarkers;	

	private ImageButton btn_compass;
	private ImageButton btn_stop_tracking;
	private ImageButton btn_add_content;
	private ImageButton btn_whereami;

	private int routeMode;
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST + 1;

	private float TAP_TOLERANCE_DIST = 100;
	static final int HIGHLIGHT_EDIT_REQUEST = 1;
	static final int END_TRIP = 2;
	//MINI_KIND
	static final int IMAGE_THUMBNAIL_WIDTH = 384;
	static final int IMAGE_THUMBNAIL_HEIGTH = 512;
	static final String TAG = "DetailItineraryActivity";
	
	private LocationClient locationClient;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private EruletApp app;
	private MapObjectsFactory mObjFactory;
	private CountDownTimer countDown;
	private ProximityWarning proximityWarning;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		 getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//				    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.detail_itinerary_map);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		proximityWarning = new ProximityWarning(app);
		mObjFactory = new MapObjectsFactory();
		// Check availability of google play services
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());
		if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		} else { //Proceed normally			

			setWorkingMode();
			setupView();

			btn_compass = (ImageButton) findViewById(R.id.btn_compass);
			btn_compass.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
						Intent i = new Intent(DetailItineraryActivity.this,
								CompassActivity.class);
						if(selectedMarker!=null){
							Step s = selectedRouteMarkers.get(selectedMarker);
							if(s!=null){
								i.putExtra("idStep", s.getId());
							}
							if(selectedMarker.equals(startMarker)){
								if(selectedRouteSteps!=null){
									i.putExtra("idStep", selectedRouteSteps.get(0).getId());
								}
							}
							if(selectedMarker.equals(arrivalMarker)){
								if(selectedRouteSteps!=null){
									i.putExtra("idStep", selectedRouteSteps.get(selectedRouteSteps.size()-1).getId());
								}
							}
						}
						i.putExtra("wasTracking", app.isTrackingServiceOn());
						startActivity(i);
//					}
				}
			});					

			btn_stop_tracking = (ImageButton) findViewById(R.id.btn_stop_tracking);
			btn_stop_tracking.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {					
					new AlertDialog.Builder(DetailItineraryActivity.this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(getString(R.string.finish_trip))
					.setMessage(
							getString(R.string.finish_trip_long))
					.setPositiveButton(getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								stopTracking();									
							}
						}).setNegativeButton(getString(R.string.no), 
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {																
							}
						}).show();										
				}
			});

			btn_add_content = (ImageButton) findViewById(R.id.btn_add_content);
			btn_add_content.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (currentStep != null) {
						showAddMarkerOnTapDialog(currentStep);
					} else {
						Toast.makeText(
								getApplicationContext(),
								getString(R.string.no_points_in_trip),
								Toast.LENGTH_LONG).show();
					}
				}
			});

			btn_whereami = (ImageButton) findViewById(R.id.btn_whereami);
			btn_whereami.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					singlePositionFix();
				}
			});

			configureUI();

			if (routeMode > 0) {
				checkLocationServicesStatus();
			}
			startOrResumeTracking();			
			
			locationClient = new LocationClient(this, this, this);
		}
	}

	private void configureUI() {
		switch (routeMode) {
		case 0:
			btn_compass.setVisibility(View.VISIBLE);
			btn_stop_tracking.setVisibility(View.GONE);
			btn_add_content.setVisibility(View.GONE);
			btn_whereami.setVisibility(View.VISIBLE);
			break;
		case 1:
			btn_compass.setVisibility(View.VISIBLE);
			btn_stop_tracking.setVisibility(View.VISIBLE);
			btn_add_content.setVisibility(View.VISIBLE);
			btn_whereami.setVisibility(View.VISIBLE);
			break;
		case 2:
			btn_compass.setVisibility(View.VISIBLE);
			btn_stop_tracking.setVisibility(View.VISIBLE);
			btn_add_content.setVisibility(View.VISIBLE);
			btn_whereami.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		locationClient.connect();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		locationClient.disconnect();
		super.onStop();		
	}

	@Override
	public void onBackPressed() {
//		if (routeMode == 1 && tracking) {
//			stopTracking();
//		}
		if ((routeMode == 1 || routeMode == 2) && app.isTrackingServiceOn()) {
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(getString(R.string.finish_trip))
					.setMessage(
							getString(R.string.finish_trip_leave_long))
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									stopTracking();
									finish();
								}
							}).setNegativeButton(getString(R.string.no), null).show();
		} else {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == HIGHLIGHT_EDIT_REQUEST) {
			if (resultCode == RESULT_OK) {
				String hlName = data.getStringExtra("hlName");
				String hlLongText = data.getStringExtra("hlLongText");
				String imagePath = data.getStringExtra("imagePath");
				int hlType = data.getIntExtra("hlType", HighLight.WAYPOINT );
				HighLight hl = new HighLight();
				hl.setName(hlName);
				hl.setLongText(hlLongText);
				hl.setMediaPath(imagePath);
				hl.setType(hlType);
				Step s = fixReceiver.getStepById(stepBeingEditedId);
				hl.setRadius(s.getPrecision());
				if (s != null) {
					s.setHighlight(hl);
				}
				// Aggressively save highlight
				saveHighLight(hl);
				// This is a cheap way to refresh the info window content
				if (selectedMarker != null) {
					selectedMarker.setIcon( MapObjectsFactory.getUserBitmapDescriptor(hlType) );
					selectedMarker.hideInfoWindow();
					selectedMarker.showInfoWindow();
					//removeTemporalMarkers(selectedMarker);
				}				
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.user_cancel), Toast.LENGTH_LONG).show();
			}
		}
		if (requestCode == END_TRIP){			//
				//Clear map
				fixReceiver.clearTrackObjects();
				//Set working mode to map
				routeMode = 0;
				//Adjust interface
				configureUI();
				//Load saved route (not strictly necessary...)				
				selectedRoute = routeInProgress;
				//Free resources from fixReceiver, we don't need it anymore
				fixReceiver.destroy();
				fixReceiver = null;
				//Re-display selected route
				resetSelectedRouteMarkers();
				updateSelectedRoute();				
		}
	}
	
	//Clears unused temporal markers after succesfully adding highlight
	private void removeTemporalMarkers(Marker selectedMarker){
		Enumeration<Marker> e = routeInProgressMarkers.keys();
		ArrayList<Marker> removeThese = new ArrayList<Marker>();
		LatLng selectedMarkerPosition = selectedMarker.getPosition();		
		while(e.hasMoreElements()){
			Marker m = e.nextElement();
			LatLng currentMarkerPosition = m.getPosition();
			if(!m.equals(selectedMarker) && currentMarkerPosition.equals(selectedMarkerPosition) ){
				removeThese.add(m);
				m.remove();				
			}
		}
		for(int i = 0; i < removeThese.size(); i++){
			routeInProgressMarkers.remove(removeThese.get(i));
		}
	}
		
	private void resetSelectedRouteMarkers(){
		Enumeration<Marker> e = selectedRouteMarkers.keys();
		while(e.hasMoreElements()){
			e.nextElement().remove();
		}
		selectedRouteMarkers.clear();
	}

	private void saveHighLight(HighLight h) {
		Step s = DataContainer.findStepById(stepBeingEditedId, app.getDataBaseHelper());
		if (s == null) {
			// Something has gone very wrong
			if (Util.DEBUG) {
				Log.d("saveHighLight", "Step id not found " + stepBeingEditedId);
			}
		} else {			
			DataContainer.addHighLightToStep(s, h, DataContainer.getAndroidId(getContentResolver()), app.getDataBaseHelper());
		}
	}

	private void checkLocationServicesStatus() {
		LocationManager lm = null;
		boolean gps_enabled = false;
		boolean network_enabled = false;
		if (lm == null)
			lm = (LocationManager) getBaseContext().getSystemService(
					Context.LOCATION_SERVICE);		
		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}
		try {
			network_enabled = lm
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		if (!gps_enabled && !network_enabled) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage(getString(R.string.location_not_enabled));
			dialog.setPositiveButton(getString(R.string.enable_location),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface paramDialogInterface,
							int paramInt) {
						Intent myIntent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(myIntent);							
					}
				});
			dialog.setNegativeButton(getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(
							DialogInterface paramDialogInterface,
							int paramInt) {
						// TODO Auto-generated method stub

					}
				});
			dialog.show();
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (selectedRoute != null) {
			if(selectedRoute.getReference()==null){
				menu.getItem(0).setVisible(false);
			}
			if(selectedRoute.getInteractiveImage()==null){
				menu.getItem(1).setVisible(false);
			}
//			for (int i = 0; i < menu.size(); i++) {
//				menu.getItem(i).setVisible(false);
//			}
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(group1, first_id, first_id, getString(R.string.envir_data));
		menu.add(group1, second_id, second_id, getString(R.string.interactive_pic));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			Intent ihtml = new Intent(DetailItineraryActivity.this,
					HTMLViewerActivity.class);
			ihtml.putExtra("idReference", selectedRoute.getReference().getId());
			startActivity(ihtml);
			return true;
		case 2:
			Intent i = new Intent(DetailItineraryActivity.this,
					InteractiveImageActivityHeatMap.class);			
			i.putExtra("int_image_id", selectedRoute.getInteractiveImage().getId());
			startActivity(i);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);		
	}

	private void startOrResumeTracking() {
		if (routeMode > 0) {
			app.startTrackingService();
			fixFilter = new IntentFilter(getResources().getString(
					R.string.internal_message_id)
					+ Util.MESSAGE_FIX_RECORDED);
			fixReceiver = new FixReceiver(mMap,proximityWarning,selectedRouteMarkers);
			registerReceiver(fixReceiver, fixFilter);
		}
	}

	private void singlePositionFix() {
		checkLocationServicesStatus();
		Location current = locationClient.getLastLocation();		
		if(current!=null){
			addTemporalPositionMarker(current);
		}else{
			Toast.makeText(this, getString(R.string.still_lost), Toast.LENGTH_SHORT).show();
		}
	}
	
	private void addTemporalPositionMarker(Location current){
		if(countDown!=null){
			countDown.cancel();
		}else{			
			countDown = new CountDownTimer(2000,2000) {				
				@Override
				public void onTick(long millisUntilFinished) {
					// TODO Auto-generated method stub
					//Do nothing on tick					
				}				
				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					lastPositionMarker.remove();
					lastPositionMarker=null;
				}
			};
		}
		LatLng currentLatLng = new LatLng(current.getLatitude(),current.getLongitude());
		CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16);
		if(lastPositionMarker==null){
			lastPositionMarker = mObjFactory.addLastPositionMarker(
					mMap, 
					getString(R.string.you_are_here_exp),
					getString(R.string.more_or_less_par)
					);
		}
		lastPositionMarker.setPosition(currentLatLng);
		lastPositionMarker.showInfoWindow();
		mMap.moveCamera(cu);
		countDown.start();
	}

//	private Route createNewRoute() {
//		ArrayList<Step> currentSteps = fixReceiver.getStepsInProgress();
//		String android_id = Secure.getString(getBaseContext()
//				.getContentResolver(), Secure.ANDROID_ID);
//		Track t = new Track();
//		t.setSteps(currentSteps);
//		for (int i = 0; i < currentSteps.size(); i++) {
//			Step s = currentSteps.get(i);
//			s.setTrack(t);
//		}
//		// Save route
//		Route r = new Route();
//		if (selectedRoute != null)
//			r.setIdRouteBasedOn(selectedRoute.getId());
//		r.setUserId(android_id);
//		r.setTrack(t);
//		return r;
//	}

	private void saveRoute() {
		Intent i = new Intent(DetailItineraryActivity.this,
				EditRouteActivity.class);
		String routeJson;
		try {
			routeJson = JSONConverter.routeToJSONObject(routeInProgress)
					.toString();
			i.putExtra("routeJson", routeJson);
			startActivityForResult(i,END_TRIP);			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void stopTracking() {
//		Intent intent = new Intent(getString(R.string.internal_message_id)
//				+ Util.MESSAGE_UNSCHEDULE);
//		sendBroadcast(intent);		
		app.stopTrackingService();
		if (routeMode == 1 || routeMode == 2) {
			saveRoute();
		}
	}

	private void setupView() {
		selectedRoutePoints = new ArrayList<Circle>();
		setUpRoutes();
		setUpMapIfNeeded();
		updateSelectedRoute();
		if (fixReceiver != null) {
			fixReceiver.updateTrackInProgress();
		}
		refreshUserMarkers();
		setUpCamera();
	}

	private void refreshUserMarkers() {
		if (routeInProgressMarkers != null) {
			Enumeration<Marker> userMarkers = routeInProgressMarkers.keys();
			while (userMarkers.hasMoreElements()) {
				Marker m = userMarkers.nextElement();
				Step s = routeInProgressMarkers.get(m);
				int hlType = -1;
				if(s.getHighlight()!=null){
					hlType = s.getHighlight().getType();
					m = MapObjectsFactory.addHighLightMarker(
							mMap, 
							m.getPosition(), 
							m.getTitle(), 
							m.getSnippet(), 
							hlType);
				}				
				Log.d(TAG,"refresh markers added " + hlType);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupView();
		if (fixReceiver != null) {
			fixReceiver.moveCameraToLastPosition();
		}
	}

	private void setWorkingMode() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			routeMode = extras.getInt("mode");
		}
	}

	private void setUpRoutes() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String idRoute = extras.getString("idRoute");
			selectedRoute = DataContainer
					.findRouteById(idRoute, app.getDataBaseHelper());
		}
		// Create route entry in database
		// We create a route also in mode 1, though we only draw the points the user hits
		if (routeInProgress == null && (routeMode == 1 || routeMode == 2)) {
			//This is very important for the ratings system
			String idRouteBasedOn = selectedRoute!=null ? selectedRoute.getId() : null;
			routeInProgress = DataContainer.createEmptyRoute(
					app.getDataBaseHelper(),
					DataContainer.getAndroidId(getContentResolver()),
					idRouteBasedOn
					);
		}
	}

//	private void setUpDBIfNeeded() {
//		if (dataBaseHelper == null) {
//			dataBaseHelper = OpenHelperManager.getHelper(this,
//					DataBaseHelper.class);
//			userId = DataContainer.getAndroidId(getContentResolver());
//		}
//	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		if (dataBaseHelper != null) {
//			OpenHelperManager.releaseHelper();
//			dataBaseHelper = null;
//		}
		if (fixReceiver != null) {
			unregisterReceiver(fixReceiver);
			fixReceiver = null;
		}
		if (tileProvider != null) {
			tileProvider.close();
			tileProvider = null;
		}
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapDetail)).getMap();			
			// Check if we were successful in obtaining the map.
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

			mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					Step s = selectedRouteMarkers.get(marker);
					boolean isUserMarker = false;
					if (s == null) {
						if (routeInProgressMarkers != null) {
							s = routeInProgressMarkers.get(marker);
							isUserMarker = true;
						}
					}
					if (s != null && s.getReference() != null) {
						Intent i = new Intent(DetailItineraryActivity.this,
								HTMLViewerActivity.class);
						i.putExtra("idReference", s.getReference().getId());
						startActivity(i);
					} else {
						//Might be start/end markers
						if(marker.equals(startMarker) || marker.equals(arrivalMarker)){
							//Do nothing in special
						}else{
							//We are editing. On click, if it's an user marker we go to
							//Edit highlight activity
							if ( (routeMode == 1 || routeMode == 2) && isUserMarker) {
								launchHighLightEditIntent(s);								
							}else{ //Go to highlight detail (if there's some media to show)
								if(s.getHighlight()!=null && s.getHighlight().getMediaPath() != null){
									JSONObject hl_s;
									try {
										hl_s = JSONConverter.stepToJSONObject(s);
										if(hl_s != null){
											String s_j_string = hl_s.toString();
											//Check if it's a warned marker. If it is, we consider the warning acknowledged
//											if(proximityWarning != null && proximityWarning.markerIsBeingWarned(marker) ){
//												proximityWarning.acknowledgeWarning();
//											}
											Intent i = new Intent(DetailItineraryActivity.this,DetailHighLightActivity.class);
											i.putExtra("step_j", s_j_string);
											startActivity(i);
										}
									} catch (JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}								
								}
							}
						}
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
					selectedMarker = marker;
					View myContentView = getLayoutInflater().inflate(
							R.layout.custominfowindow, null);
					TextView snippet = (TextView) myContentView
							.findViewById(R.id.info_snippet);
					TextView title = (TextView) myContentView
							.findViewById(R.id.info_title);
					Step s = selectedRouteMarkers.get(marker);
					boolean isUserMarker = false;
					if (s == null) {
						if (routeInProgressMarkers == null) {
							isUserMarker = false;
						} else {
							s = routeInProgressMarkers.get(marker);
							isUserMarker = !(s == null);
						}
					}
					if (s != null) {
						if (isUserMarker) {
							if (s.getHighlight() == null) {
								title.setText(getString(R.string.empty_marker));
								snippet.setText(getString(R.string.click_add_data));
							} else {
								HighLight h1 = s.getHighlight();
								title.setText(h1.getName());
								snippet.setText(h1.getLongText());
								ImageView picture = (ImageView) myContentView
										.findViewById(R.id.info_pic);
								if (h1.getMediaPath() != null
										&& !h1.getMediaPath().trim()
												.equalsIgnoreCase("")) {
									String file = h1.getMediaPath();
									if (file.contains("mp4")) {
										file = file.replace("file://", "");
										Bitmap bm = ThumbnailUtils
												.createVideoThumbnail(
														file,
														android.provider.MediaStore.Video.Thumbnails.MINI_KIND);
										picture.setImageBitmap(bm);
									} else {
										file = file.replace("file://", "");
										//loadBitmapThumbnailToImageView(file, IMAGE_THUMBNAIL_WIDTH, IMAGE_THUMBNAIL_HEIGTH, picture);
										picture.setImageBitmap(Util.decodeSampledBitmapFromFile(
												file, 
												IMAGE_THUMBNAIL_WIDTH, 
												IMAGE_THUMBNAIL_HEIGTH));										//
									}
								} else {
									picture.setImageResource(R.drawable.ic_itinerary_icon);
								}
							}
						} else {
							HighLight h1 = s.getHighlight();
							title.setText(h1.getName());
							snippet.setText(h1.getLongText());
							ImageView picture = (ImageView) myContentView
									.findViewById(R.id.info_pic);
							if (h1.getMediaPath() != null
									&& !h1.getMediaPath().trim()
											.equalsIgnoreCase("")) {
								String file = h1.getMediaPath();
								if (file.contains("mp4")) {
									file = file.replace("file://", "");
									Bitmap bm = ThumbnailUtils
											.createVideoThumbnail(
													file,
													android.provider.MediaStore.Video.Thumbnails.MINI_KIND);
									picture.setImageBitmap(bm);
								} else {
									file = file.replace("file://", "");
									//loadBitmapThumbnailToImageView(file, IMAGE_THUMBNAIL_WIDTH, IMAGE_THUMBNAIL_HEIGTH, picture);
									picture.setImageBitmap(Util.decodeSampledBitmapFromFile(
											file, 
											IMAGE_THUMBNAIL_WIDTH, 
											IMAGE_THUMBNAIL_HEIGTH));
								}
							} else {
								picture.setImageResource(R.drawable.ic_itinerary_icon);
							}
						}						

					} else {
						// Is the current position marker
						title.setText(marker.getTitle());
						snippet.setText(marker.getSnippet());
					}
					return myContentView;
				}
			});
			mMap.setOnMapClickListener(new OnMapClickListener() {
				@Override
				public void onMapClick(LatLng point) {
					Step nearestStepToTap = getRouteInProgressNearestStep(
							point, TAP_TOLERANCE_DIST);
					if (nearestStepToTap == null) {
						selectedMarker = null;
					}											
					if(routeMode==1 || routeMode==2){
						if(selectedMarker!=null){
							showAddMarkerOnTapDialog(nearestStepToTap);
						}
					}
										
					// if(bogus_location != null){
					// bogus_location.remove();
					// }
					// bogus_location = mMap.addMarker(new MarkerOptions()
					// .position(new LatLng(point.latitude, point.longitude))
					// .title("Bogus location")
					// .snippet("Bogus location")
					// .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_erulet_pin)));
					// checkLocationIsWithinEffectRadius(point);
					// Snap to nearest step...
					// SnapCalculatorV2 sc = new SnapCalculatorV2();
					// LatLng pointInTrack =
					// sc.snapToCurrentTrack(fixReceiver.getStepsInProgress(),
					// point);
					// LineSegment perp = sc.getPerpendicularToNearest();
					// if(perp != null)
					// drawPerpLine(perp);
					// mMap.addMarker(new MarkerOptions()
					// .position(new LatLng(pointInTrack.latitude,
					// pointInTrack.longitude))
					// .title("Snapped")
					// .snippet(pointInTrack.latitude + " " +
					// pointInTrack.longitude)
					// .icon(BitmapDescriptorFactory
					// .defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
				}
			});
			mMap.setOnMarkerClickListener(new OnMarkerClickListener() {				
				@Override
				public boolean onMarkerClick(Marker marker) {
					// TODO Auto-generated method stub
					if(marker.isFlat()){
						return true;
					}
					return false;
				}
			});
		}
	}
	
	private void launchHighLightEditIntent(Step s){
		Intent i = new Intent(
				DetailItineraryActivity.this,
				EditHighLightActivity.class);
		stepBeingEditedId = s.getId();
		i.putExtra("lat",
				Double.toString(s.getLatitude()));
		i.putExtra("long",
				Double.toString(s.getLongitude()));
		i.putExtra("alt",
				Double.toString(s.getAltitude()));								
		i.putExtra("date",
				app.formatDateDayMonthYear(s.getAbsoluteTime()));
		if (s.getHighlight() != null) {
			i.putExtra("hlname", s.getHighlight()
					.getName());
			i.putExtra("hllongtext", s.getHighlight()
					.getLongText());
			i.putExtra("hlimagepath", s.getHighlight()
					.getMediaPath());
		}
		startActivityForResult(i,
				HIGHLIGHT_EDIT_REQUEST);
	}
	
	private boolean markerAlreadyOnStep(Step s) {
		if (routeInProgressMarkers == null
				|| routeInProgressMarkers.size() == 0) {
			return false;
		}
		Enumeration e = routeInProgressMarkers.elements();
		while (e.hasMoreElements()) {
			Step s_n = (Step) e.nextElement();
			if (s_n.equals(s)) {
				return true;
			}
		}
		return false;
	}

	private void showAddMarkerOnTapDialog(final Step s) {
		if (!markerAlreadyOnStep(s)) {
			if (routeInProgressMarkers == null) {
				routeInProgressMarkers = new Hashtable<Marker, Step>();
			}

			LatLng point = new LatLng(s.getLatitude(), s.getLongitude());

//			final Marker m = mMap.addMarker(new MarkerOptions()
//					.position(point)
//					.title("Marcador temporal")
//					.snippet("Afegim o no?")
//					.icon(BitmapDescriptorFactory
//							.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
			final Marker m = MapObjectsFactory.addEmptyUserMarker(
					mMap, 
					point, 
					getString(R.string.temp_marker), 
					getString(R.string.add_or_not));
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point,
					mMap.getCameraPosition().zoom));
			routeInProgressMarkers.put(m, s);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			AlertDialog dialog = builder.create();
			dialog.setCancelable(false);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			WindowManager.LayoutParams wmlp = dialog.getWindow()
					.getAttributes();
			wmlp.gravity = Gravity.TOP | Gravity.LEFT;
			wmlp.x = 100; // x position
			wmlp.y = 100; // y position
			dialog.setMessage(getString(R.string.add_marker_confirm));
			dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface paramDialogInterface,
								int paramInt) {
							if(selectedMarker!=null){
								selectedMarker.remove();
							}
							selectedMarker = m;							
							launchHighLightEditIntent(s);
						}
					});
			dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(
								DialogInterface paramDialogInterface,
								int paramInt) {
							m.remove();
							routeInProgressMarkers.remove(m);
						}
					});
			dialog.show();
		}
	}

	private Step getRouteInProgressNearestStep(LatLng point, float tolerance) {
		Step retVal = null;
		if (routeInProgress != null) {
			if (routeInProgress.getTrack() != null
					&& routeInProgress.getTrack().getSteps() != null
					&& routeInProgress.getTrack().getSteps().size() > 0) {
				ArrayList<Step> steps = (ArrayList<Step>) routeInProgress
						.getTrack().getSteps();
				float minDist = Float.MAX_VALUE;
				for (int i = 0; i < steps.size(); i++) {
					Step s = steps.get(i);

					Location stepLocation = new Location("");
					stepLocation.setLatitude(s.getLatitude());
					stepLocation.setLongitude(s.getLongitude());

					Location tapLocation = new Location("");
					tapLocation.setLatitude(point.latitude);
					tapLocation.setLongitude(point.longitude);

					float distance = stepLocation.distanceTo(tapLocation);
					if (distance <= tolerance && distance < minDist) {
						minDist = distance;
						retVal = s;
					}
				}
			}
		}
		return retVal;
	}

//	private void drawPerpLine(LineSegment perp) {
//		PolylineOptions rectOptions = new PolylineOptions();
//		rectOptions.zIndex(1);
//		rectOptions.color(Color.MAGENTA);
//		Point p1 = perp.getP1();
//		Point p2 = perp.getP2();
//		SphericalMercatorProjection sp = new SphericalMercatorProjection(6371);
//		rectOptions.add(sp.toLatLng(p1));
//		rectOptions.add(sp.toLatLng(p2));
//		perpPolyLine = mMap.addPolyline(rectOptions);
//	}

	private void setUpCamera() {
		// Step s = DataContainer.getRouteStarter(currentRoute, dataBaseHelper);
		// mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
		// LatLng(s.getLatitude(),s.getLongitude()) , 15));
		Track t = selectedRoute.getTrack();
		List<Step> steps = DataContainer.getTrackSteps(t, app.getDataBaseHelper());
		LatLngBounds.Builder b = new LatLngBounds.Builder();
		for (int i = 0; i < steps.size(); i++) {
			Step s = steps.get(i);
			b.include(new LatLng(s.getLatitude(), s.getLongitude()));
		}
		LatLngBounds bounds = b.build();
		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200, 200,
				20));
	}

//	private void checkLocationIsWithinEffectRadius(LatLng location) {
//		boolean found = false;
//		int i = 0;
//		while (i < highLightedSteps.size() && !found) {
//			Step s = highLightedSteps.get(i);
//			float[] results = new float[3];
//			Location.distanceBetween(location.latitude, location.longitude,
//					s.getLatitude(), s.getLongitude(), results);
//			if (results[0] <= s.getHighlight().getRadius()) {
//				Log.i("HIT", "Hit interest area");
//				if (interestAreaMarker != null) {
//					interestAreaMarker.remove();
//				}
//				interestAreaMarker = mMap
//						.addMarker(new MarkerOptions()
//								.position(
//										new LatLng(s.getLatitude(), s
//												.getLongitude()))
//								.title("Interesting point")
//								.snippet("Interesting point")
//								.icon(BitmapDescriptorFactory
//										.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//				// MarkerAnimationTest.animateMarker(interestAreaMarker);
//				MarkerAnimationTest.bounceMarker(interestAreaMarker);
//				interestStep = s;
//				found = true;
//			} else {
//				if (interestAreaMarker != null) {
//					interestAreaMarker.remove();
//					interestStep = null;
//				}
//			}
//			i++;
//		}
//	}	

	private void addMarkerIfNeeded(Step step) {
		// stepTable
		HighLight hl;
		if ((hl = step.getHighlight()) != null) {
			Marker m = MapObjectsFactory.addHighLightMarker(
					mMap, 
					new LatLng(step.getLatitude(), step.getLongitude()), 
					hl.getName(), 
					hl.getLongText(),
					hl.getType());
			selectedRouteMarkers.put(m, step);
		}
	}

	private void addPrecisionRadius(Step step) {
		// if (highLightedSteps == null) {
		// highLightedSteps = new ArrayList<Step>();
		// }
		// HighLight hl;
		// if ((hl = step.getHighlight()) != null) {
		CircleOptions copt = new CircleOptions();
		copt.center(new LatLng(step.getLatitude(), step.getLongitude()));
		copt.radius(step.getPrecision());
		copt.zIndex(1);
		copt.strokeColor(Color.BLACK);
		copt.strokeWidth(1);
		// Fill color of the circle
		// 0x represents, this is an hexadecimal code
		// 55 represents percentage of transparency. For 100% transparency,
		// specify 00.
		// For 0% transparency ( ie, opaque ) , specify ff
		// The remaining 6 characters(00ff00) specify the fill color
		copt.fillColor(0x556F6F6F);
		selectedRoutePoints.add(mMap.addCircle(copt));
		// highLightedSteps.add(step);
		// }
	}

//	private void drawUserRoute() {
//		if (fixReceiver != null) {
//			PolylineOptions rectOptions = new PolylineOptions();
//			List<Step> stepsInProgress = fixReceiver.getStepsInProgress();
//			if (stepsInProgress != null) {
//				for (int i = 0; i < stepsInProgress.size(); i++) {
//					Step step = stepsInProgress.get(i);
//					rectOptions.add(new LatLng(step.getLatitude(), step
//							.getLongitude()));
//					addPrecisionRadius(step);
//					addMarkerIfNeeded(step, BitmapDescriptorFactory.HUE_CYAN);
//				}
//				rectOptions.zIndex(2);
//				rectOptions.color(Color.GREEN);
//				mMap.addPolyline(rectOptions);
//			}
//		}
//	}
	
	private void clearSelectedRoute(){		
		if( selectedRoutePolyLine != null ){
			selectedRoutePolyLine.remove();
			selectedRoutePolyLine = null;
		}
		if(startMarker!=null){
			startMarker.remove();
		}
		if(arrivalMarker!=null){
			arrivalMarker.remove();
		}
		if( selectedRoutePoints != null ){
			for(int i = 0; i < selectedRoutePoints.size(); i++){
				Circle c = selectedRoutePoints.get(i);
				c.remove();
			}
			selectedRoutePoints.clear();
		}
	}

	private void updateSelectedRoute() {
		if (selectedRouteMarkers == null) {
			selectedRouteMarkers = new Hashtable<Marker, Step>();
		}
		clearSelectedRoute();
		PolylineOptions rectOptions = new PolylineOptions();
		Track t = selectedRoute.getTrack();
		selectedRouteSteps = DataContainer.getTrackSteps(t, app.getDataBaseHelper());
		refreshDecorations(selectedRouteSteps);
		for (int j = 0; j < selectedRouteSteps.size(); j++) {
			Step step = selectedRouteSteps.get(j);
			rectOptions
					.add(new LatLng(step.getLatitude(), step.getLongitude()));
			//Enable this maybe on options, obscures map too much
			//addPrecisionRadius(step);
			addMarkerIfNeeded(step);
		}
		rectOptions.zIndex(1);
		rectOptions.color(Color.GRAY);
		selectedRoutePolyLine = mMap.addPolyline(rectOptions);		
	}
	
	private void refreshDecorations(List<Step> steps){
		if( directionMarkers == null ){
			directionMarkers = new ArrayList<Marker>();
		}else{
			for(int i = 0; i < directionMarkers.size(); i++){
				directionMarkers.get(i).remove();
			}
			directionMarkers.clear();
		}
		LatLng lastPosition = null;
		if(steps != null){
			int freq;		
			if(steps.size() <= 10){
				freq = 1;
			}else{
				freq = 3;
			}
			for(int i = 0; i < steps.size(); i++){
				Step current = steps.get(i);			
				LatLng currentPosition = new LatLng(
						current.getLatitude(), 
						current.getLongitude()
						);
				if(i % freq == 0){
					if(currentPosition!=null && lastPosition !=null && !currentPosition.equals(lastPosition) ){
						double angle = SphericalUtil.computeHeading(lastPosition, currentPosition);
						double dist = SphericalUtil.computeDistanceBetween(lastPosition, currentPosition);
						LatLng markerPosition = SphericalUtil.computeOffset(lastPosition, dist/2, angle);
						directionMarkers.add(MapObjectsFactory.addDirectionMarker(mMap,markerPosition,angle));
					}
				}
				if(i == 0){
					startMarker = MapObjectsFactory.addStartMarker(mMap, currentPosition, getString(R.string.trip_start));
				}
				if(i == (steps.size() - 1)){
					arrivalMarker = MapObjectsFactory.addEndMarker(mMap, currentPosition, getString(R.string.trip_end));
				}
				lastPosition = new LatLng(currentPosition.latitude,currentPosition.longitude);			
			}
		}
	}

	private MapBoxOfflineTileProvider initTileProvider() {
		File sdcard = Environment.getExternalStorageDirectory();
		if(selectedRoute.getLocalCarto()!=null){
			File f = new File(sdcard,selectedRoute.getLocalCarto());
			//File f = new File(getCacheDir() + "/OSMPublicTransport_HiRes.mbtiles");
			if (f.exists()){
//				try {				
					//InputStream is = getAssets().open(
							//"OSMPublicTransport_HiRes.mbtiles");
//					FileInputStream is = new FileInputStream(f);
//					int size = is.available();
//					byte[] buffer = new byte[size];
//					is.read(buffer);
//					is.close();
//					FileOutputStream fos = new FileOutputStream(f);
//					fos.write(buffer);
//					fos.close();
//				} catch (Exception e) {
//					throw new RuntimeException(e);
//				}
				return new MapBoxOfflineTileProvider(f.getPath());
			}
		}
		return null;
	}

	public class FixReceiver extends BroadcastReceiver {

		private GoogleMap mMap;
		private ArrayList<Step> stepsInProgress;
		private Polyline trackInProgress;
		private List<Circle> pointsInProgress;
		private Hashtable<Marker, Step> selectedRouteMarkers;
		private CameraUpdate cu;
		private List<Step> warningList;
		private ProximityWarning proximityWarning;		

		public FixReceiver(GoogleMap mMap,ProximityWarning proximityWarning,Hashtable<Marker, Step> selectedRouteMarkers) {
			super();
			this.mMap = mMap;
			this.selectedRouteMarkers=selectedRouteMarkers;
			this.proximityWarning = proximityWarning;
			stepsInProgress = new ArrayList<Step>();
			pointsInProgress = new ArrayList<Circle>();
			warningList = new ArrayList<Step>();
		}

		public ArrayList<Step> getStepsInProgress() {
			return stepsInProgress;
		}
				
		public void setStepsInProgress(ArrayList<Step> steps) {
			stepsInProgress = steps;
		}
		
		public void clearTrackObjects(){
			if(trackInProgress!=null){
				trackInProgress.remove();
				trackInProgress = null;
			}
			if(pointsInProgress!=null){
				for(int i = 0; i < pointsInProgress.size(); i++){
					Circle c = pointsInProgress.get(i);
					c.remove();
				}
				pointsInProgress.clear();
			}
		}
		
		public void destroy(){
			stepsInProgress.clear();
			stepsInProgress = null;
			trackInProgress = null;
			pointsInProgress.clear();
			warningList.clear();
			warningList = null;
			pointsInProgress = null;
			cu = null;
			mMap = null;
		}

		public void updateTrackInProgress() {
			clearTrackObjects();
			int zIndexPolyLine = stepsInProgress != null ? stepsInProgress.size() + 1 : 1;
			PolylineOptions rectOptions = MapObjectsFactory.getRouteInProgressPolyLine(zIndexPolyLine);
			if (stepsInProgress.size() > 0) {
				for (int i = 0; i < stepsInProgress.size(); i++) {

					Step step = stepsInProgress.get(i);
					
					boolean last = (i == stepsInProgress.size()-1);
					
					CircleOptions copt = MapObjectsFactory.
							getRouteInProgressCircle(
									new LatLng(step.getLatitude(), step.getLongitude()), 
									step.getPrecision(),
									last,
									i);
					
					pointsInProgress.add(mMap.addCircle(copt));

					if(routeMode == 1 || routeMode == 2){
						rectOptions.add(new LatLng(step.getLatitude(), step
								.getLongitude()));
					}
				}
				if(routeMode == 1 || routeMode == 2){
					trackInProgress = mMap.addPolyline(rectOptions);
				}
			}
		}

		public Step getStepById(String id) {
			for (Step s : stepsInProgress) {
				if (s.getId().equalsIgnoreCase(id))
					return s;
			}
			return null;
		}

		private boolean locationExists(LatLng current, List<Step> steps) {
			if(steps!=null){
				for (int i = 0; i < steps.size(); i++) {
					Step thisStep = steps.get(i);
					if (thisStep.getLatitude() == current.latitude
							&& thisStep.getLongitude() == current.longitude) {
						return true;
					}
				}
			}
			return false;
		}
		
		private void checkNearbyMarkers(LatLng receivedLocation){
			Enumeration<Marker> eM = selectedRouteMarkers.keys();
			boolean found = false;
			while(eM.hasMoreElements() && !found){
				Marker m = eM.nextElement();
				Step s = selectedRouteMarkers.get(m);
				if(!warningList.contains(s)){
					float[] results = new float[3];
					Location.distanceBetween(
							receivedLocation.latitude, 
							receivedLocation.longitude,
							s.getLatitude(), 
							s.getLongitude(), 
							results);
					double effectivePopRadius = s.getPrecision();
					if(effectivePopRadius < Util.MINIMUM_POP_DISTANCE_RADIUS){
						effectivePopRadius = Util.MINIMUM_POP_DISTANCE_RADIUS;
					}					
					if (results[0] <= effectivePopRadius) {					
							Log.d("HIT", "Hit interest area - distance: " + results[0] + " radius: " + effectivePopRadius);
							found = true;
							proximityWarning.issueWarning(m);
							warningList.add(s);							
							m.showInfoWindow();
					}
				}
			}
		}
				
		@Override
		public void onReceive(Context context, Intent intent) {
			double lat = intent.getExtras().getDouble("lat", 0);
			double lng = intent.getExtras().getDouble("long", 0);
			double alt = intent.getExtras().getDouble("alt", 0);
			double accuracy = intent.getExtras().getDouble("acc", 0);
			//DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSZ");
			long currentTime = System.currentTimeMillis();
			Date time = new Date(currentTime);
			LatLng location = new LatLng(lat, lng);
			boolean locationExists = locationExists(location, stepsInProgress);
			// Point is same as last, we don't add it to the track
			if (Util.DEBUG) {
				Log.d("onReceive", "Received new location " + lat + " " + lng
						+ " t " + app.formatDateHoursMinutesSeconds(time) + " already logged");
			}
			if (!locationExists) {
				cu = CameraUpdateFactory.newLatLngZoom(location, 16);
				mMap.moveCamera(cu);
				// Aggressive save - save location as soon as is available
				if (routeMode == 1 || routeMode == 2) {

					Step s = new Step();
					s.setId(Long.toString(currentTime));
					s.setAbsoluteTime(time);
					s.setAbsoluteTimeMillis(currentTime);
					s.setAltitude(alt);
					s.setLatitude(lat);
					s.setLongitude(lng);
					s.setPrecision(accuracy);

					currentStep = s;
					stepsInProgress.add(s);
					int order = stepsInProgress.size() - 1;
					s.setOrder(order);
					DataContainer.addStepToTrack(s, routeInProgress.getTrack(),
							DataContainer.getAndroidId(getContentResolver()), 
							app.getDataBaseHelper());
				}				
				if (Util.DEBUG) {
					Log.d("onReceive", "Received new location " + lat + " "
							+ lng + " t " + app.formatDateHoursMinutesSeconds(time) );
				}
			}
			if(routeMode==1){
				checkNearbyMarkers(location);
			}
			updateTrackInProgress();
		}

		public void moveCameraToLastPosition() {
			if (cu != null) {
				mMap.moveCamera(cu);
			}
		}
		
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */        	
        	Toast.makeText(this,getString(R.string.error_gplay_connect), Toast.LENGTH_SHORT).show();
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		//Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		if (Util.DEBUG) {
			Log.d("onConnected", "Google play services connected");
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		if (Util.DEBUG) {
			Log.d("onDisconnected", "Google play services disconnected");
		}
	}

}
