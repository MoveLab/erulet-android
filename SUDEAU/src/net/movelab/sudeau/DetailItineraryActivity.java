package net.movelab.sudeau;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.geometry.LineSegment;
import net.movelab.sudeau.geometry.SnapCalculatorV2;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import net.movelab.sudeau.util.MarkerAnimationTest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import com.google.maps.android.geometry.Point;
import com.google.maps.android.projection.SphericalMercatorProjection;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DetailItineraryActivity extends Activity {

	//TODO Give user option to delete track for route with a single waypoint
	//TODO Modify route drawing so that it shows precision
	//TODO Unify geometry and marker drawing
	//TODO Enable pop-ups while tracking
	
	private GoogleMap mMap;
	private DataBaseHelper dataBaseHelper;
	private Marker bogus_location;
	private Marker interestAreaMarker;
	private Marker selectedMarker;
	private Step interestStep;
	private String stepBeingEditedId;
	private Step currentStep;
	//Route selected in choose itinerary activity
	private Route selectedRoute;
	private Route routeInProgress;
	private ArrayList<Step> interestSteps;
	private IntentFilter fixFilter;
	private FixReceiver fixReceiver;
	private Polyline perpPolyLine;
	private Hashtable<Marker, Step> currentRouteMarkers;
	private Hashtable<Marker, Step> userRouteMarkers;
	private Vibrator v;
	private String userId;
	private boolean tracking;

	private int routeMode;
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST + 1;
	
	static final int HIGHLIGHT_INFO_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_itinerary_map);
		v = (Vibrator) getBaseContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		setWorkingMode();
		setupView();
		
		Button btn_compass = (Button)findViewById(R.id.btn_compass_to_point);
		btn_compass.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 if(selectedMarker == null){
					 Toast.makeText(getApplicationContext(), "No hi ha cap marcador seleccionat. Si us plau, tria el marcador del mapa al que vols anar...", Toast.LENGTH_LONG).show();
				 }else{					 
					 Intent i = new Intent(DetailItineraryActivity.this,CompassActivity.class);
					 Step s = currentRouteMarkers.get(selectedMarker);
					 i.putExtra("idStep",s.getId());
					 startActivity(i);
				 }
			 }
		 });

		 Button btn_stop_tracking = (Button)findViewById(R.id.btn_stop_tracking);
		 btn_stop_tracking.setOnClickListener(new OnClickListener() {
			 @Override
			 public void onClick(View v) {
				 stopTracking();
			 }
		 });
		 
		Button btn_test = (Button)findViewById(R.id.btn_test_json);
		btn_test.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				//testJson();
				saveRoute();
			}
		});
		
		Button btn_add_content = (Button)findViewById(R.id.btn_add_content);
		btn_add_content.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				if(currentStep!=null){
					Intent i = new Intent(DetailItineraryActivity.this,EditHighLightActivity.class);
					stepBeingEditedId = currentStep.getId();
					//Step already has haighlight
					if(currentStep.getHighlight()!=null){
						i.putExtra("hlname",currentStep.getHighlight().getName());
						i.putExtra("hllongtext",currentStep.getHighlight().getLongText());
						i.putExtra("hlimagepath", currentStep.getHighlight().getImagePath());
					}
					i.putExtra("lat",Double.toString(currentStep.getLatitude()));
					i.putExtra("long",Double.toString(currentStep.getLongitude()));
					SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
					i.putExtra("date",spdf.format(currentStep.getAbsoluteTime()));					
			        startActivityForResult(i, HIGHLIGHT_INFO_REQUEST);				    
				}else{
					Toast.makeText(getApplicationContext(), "Encara no hi ha punts a la ruta, espera un moment...", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		checkLocationServicesStatus();
		startOrResumeTracking();
	}
	
	@Override
	public void onBackPressed() {
		if(routeMode==2 && tracking){
		    new AlertDialog.Builder(this)
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .setTitle("Finalització de ruta")
		    .setMessage("Estàs capturant una ruta. Si abandones la pantalla de mapa, s'aturarà la captura i es deixarà la ruta tal com està ara mateix. Confirmes que vols sortir?")
		    .setPositiveButton("Sí", new DialogInterface.OnClickListener(){
		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		        	stopTracking();
		            finish();    
		        }	
		    }).setNegativeButton("No", null)
		    .show();
		}else{
			finish();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode==HIGHLIGHT_INFO_REQUEST){
			if(resultCode==RESULT_OK){				
				String hlName = data.getStringExtra("hlName");
				String hlLongText = data.getStringExtra("hlLongText");
				String imagePath = data.getStringExtra("imagePath");
				HighLight hl = new HighLight();
				hl.setName(hlName);
				hl.setLongText(hlLongText);
				hl.setImagePath(imagePath);				
				Step s = fixReceiver.getStepById(stepBeingEditedId);
				hl.setRadius(s.getPrecision());
				if(s!=null){
					s.setHighlight(hl);
				}
				//Aggressively save highlight
				saveHighLight(hl);
				//Add marker for new highlight
				Marker m = mMap
						.addMarker(new MarkerOptions()
								.position(new LatLng(s.getLatitude(), s.getLongitude()))
								.title(hl.getName())
								.snippet(hl.getLongText())
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
				// BitmapDescriptorFactory.fromResource(R.drawable.ic_wp_pin)));
				// .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
				//currentRouteMarkers.put(m, s);
				if(userRouteMarkers==null){
					userRouteMarkers = new Hashtable<Marker, Step>();
				}
				userRouteMarkers.put(m, s);
			}else if(resultCode==RESULT_CANCELED){
				Toast.makeText(getApplicationContext(), "Cancel·lat per l'usuari", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void saveHighLight(HighLight h){
		Step s = DataContainer.findStepById(stepBeingEditedId, dataBaseHelper);
		if(s==null){
			//Something has gone very wrong
			if(IGlobalValues.DEBUG){
				Log.d("saveHighLight","Step id not found " + stepBeingEditedId);
			}
		}else{
			DataContainer.addHighLightToStep(s, h, userId, dataBaseHelper);
		}
	}
	
	private void testJson(){
		try {
			JSONObject redon = JSONConverter.routeToJSONObject(selectedRoute);
			Log.d("JSONOutput",redon.toString());
			JSONConverter.jsonToRoute(redon.toString());
			Toast.makeText(getApplicationContext(),redon.toString(), Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			dialog.setMessage("La localització del dispositiu no està activada; cal activar-la.");
			dialog.setPositiveButton("Accedir a activar localització",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(
								DialogInterface paramDialogInterface,
								int paramInt) {
							// TODO Auto-generated method stub
							Intent myIntent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							startActivity(myIntent);
							// get gps
						}
					});
			dialog.setNegativeButton("Cancel·lar",
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
		if (selectedRoute != null && !selectedRoute.isEcosystem()) {
			for (int i = 0; i < menu.size(); i++) {
				menu.getItem(i).setVisible(false);
			}
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(group1, first_id, first_id, "Dades ambientals");
		menu.add(group1, second_id, second_id, "Fotografia interactiva");
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
					InteractiveImageActivity.class);
			startActivity(i);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void startOrResumeTracking() {		
		if (routeMode > 1) {			
			Intent intent = new Intent(getString(R.string.internal_message_id)
					+ Util.MESSAGE_SCHEDULE);
			sendBroadcast(intent);
			fixFilter = new IntentFilter(getResources().getString(
					R.string.internal_message_id)
					+ Util.MESSAGE_FIX_RECORDED);			
			fixReceiver = new FixReceiver(mMap);
			registerReceiver(fixReceiver, fixFilter);
			tracking=true;
		}		
	}
	
	private Route createNewRoute(){
		ArrayList<Step> currentSteps = fixReceiver.getStepsInProgress();
		String android_id = Secure.getString(getBaseContext()
				.getContentResolver(), Secure.ANDROID_ID);		
		Track t = new Track();
		t.setSteps(currentSteps);
		for (int i = 0; i < currentSteps.size(); i++) {
			Step s = currentSteps.get(i);			
			s.setTrack(t);
		}
		// Save route
		Route r = new Route();		
		if (selectedRoute != null)
			r.setIdRouteBasedOn(selectedRoute.getId());
		r.setUserId(android_id);
		r.setTrack(t);
		return r;
	}

	private void saveRoute() {			
		Intent i = new Intent(DetailItineraryActivity.this,
				EditRouteActivity.class);
		String routeJson;		
		//Route r = createNewRoute();
		try {
			routeJson = JSONConverter.routeToJSONObject(routeInProgress).toString();
			i.putExtra("routeJson", routeJson );
			startActivity(i);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
					
	}

	private void stopTracking() {
		Intent intent = new Intent(getString(R.string.internal_message_id)
				+ Util.MESSAGE_UNSCHEDULE);
		sendBroadcast(intent);
		if (routeMode == 2) {
			saveRoute();
		}
		tracking=false;
	}

	private void setupView() {
		setUpDBIfNeeded();
		setUpRoutes();
		setUpMapIfNeeded();
		drawCurrentRouteFromDB();
		drawUserRoute();
		setUpCamera();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupView();
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
			selectedRoute = DataContainer.findRouteById(idRoute, dataBaseHelper);			 
		}
		//Create route entry in database
		if(routeInProgress==null && routeMode == 2){			
			routeInProgress = DataContainer.createEmptyRoute(dataBaseHelper, userId);			
		}
	}

	private void setUpDBIfNeeded() {
		if (dataBaseHelper == null) {
			dataBaseHelper = OpenHelperManager.getHelper(this,
					DataBaseHelper.class);
			userId = DataContainer.getAndroidId(getContentResolver());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dataBaseHelper != null) {
			OpenHelperManager.releaseHelper();
			dataBaseHelper = null;
		}
		if (fixReceiver != null)
			unregisterReceiver(fixReceiver);
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.mapDetail)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				TileProvider tileProvider = initTileProvider();
				TileOverlay tileOverlay = mMap
						.addTileOverlay(new TileOverlayOptions()
								.tileProvider(tileProvider));
				tileOverlay.setVisible(true);
			}
			if(mMap != null){
				if(routeMode==1){
					mMap.setMyLocationEnabled(true);
				}
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker marker) {
					// TODO Auto-generated method stub
					// Toast.makeText(getApplicationContext(),
					// marker.getSnippet(), Toast.LENGTH_SHORT).show();
					// If it's a route, we open the EcosystemDetailActivity
					Step s = currentRouteMarkers.get(marker);
					boolean isUserMarker = false;
					if(s == null){
						if(userRouteMarkers!=null){
							s = userRouteMarkers.get(marker);
							isUserMarker=true;
						}
					}
					if (s == null) {
						Intent i = new Intent(DetailItineraryActivity.this,
								DetailItineraryActivity.class);
						Route r = DataContainer.getRouteEcosystem(selectedRoute,
								dataBaseHelper);
						i.putExtra("idRoute", r.getId());
						i.putExtra("mode", 0);
						startActivity(i);
					} else {
						if (s.getReference() != null) {
							Intent i = new Intent(DetailItineraryActivity.this,
									HTMLViewerActivity.class);
							i.putExtra("idReference", s.getReference().getId());
							startActivity(i);
						}else{
							if(routeMode == 2){
								Intent i = new Intent(DetailItineraryActivity.this,EditHighLightActivity.class);
								stepBeingEditedId = s.getId();
								i.putExtra("lat",Double.toString(s.getLatitude()));
								i.putExtra("long",Double.toString(s.getLongitude()));								
								i.putExtra("alt",Double.toString(s.getAltitude()));
								SimpleDateFormat spdf = new SimpleDateFormat("dd/MM/yyyy");
								i.putExtra("date",spdf.format(s.getAbsoluteTime()));
								if(s.getHighlight()!=null){
									i.putExtra("hlname",s.getHighlight().getName());
									i.putExtra("hllongtext",s.getHighlight().getLongText());
									i.putExtra("hlimagepath", s.getHighlight().getImagePath());
								}
						        startActivityForResult(i, HIGHLIGHT_INFO_REQUEST);
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
					Step s = currentRouteMarkers.get(marker);
					boolean isUserMarker = false;
					if(s == null){
						if(userRouteMarkers==null){
							isUserMarker = false;
						}else{
							s = userRouteMarkers.get(marker);
							isUserMarker = !(s==null);
						}						
					}
					if (s != null) {						
						if(isUserMarker){
							if(s.getHighlight()==null){
								title.setText("Marcador buit");
								snippet.setText("Fes clic per afegir dades...");							
							}else{								
								HighLight h1 = s.getHighlight();
								title.setText(h1.getName());
								snippet.setText(h1.getLongText());
								ImageView picture = (ImageView) myContentView.findViewById(R.id.info_pic);
								if(h1.getImagePath()!=null && !h1.getImagePath().trim().equalsIgnoreCase("")){
									String imagePath = h1.getImagePath();							
									picture.setImageURI( Uri.parse(imagePath) );
								}else{												
									picture.setImageResource(R.drawable.ic_itinerary_icon);
								}
							}							
						}else{
							HighLight h1 = s.getHighlight();
							title.setText(h1.getName());
							snippet.setText(h1.getLongText());
							ImageView picture = (ImageView) myContentView.findViewById(R.id.info_pic);
							if(h1.getImagePath()!=null && !h1.getImagePath().trim().equalsIgnoreCase("")){
								String imagePath = h1.getImagePath();							
								picture.setImageURI( Uri.parse(imagePath) );
							}else{												
								picture.setImageResource(R.drawable.ic_itinerary_icon);
							}
						}
						
						v.vibrate(125);

						// Drawable image = null;
						// byte[] b = DataContainer.getStepMedia(interestStep,
						// dataBaseHelper);
						// if(b != null){
						// ByteArrayInputStream is = new
						// ByteArrayInputStream(b);
						// image = Drawable.createFromStream(is, null);
						// picture.setImageDrawable(image);
						// }
						
					} else {
						// Is ecosystem
						Route ecosystem = DataContainer.getRouteEcosystem(
								selectedRoute, dataBaseHelper);
						title.setText(ecosystem.getName());
						snippet.setText(ecosystem.getDescription());
						ImageView picture = (ImageView) myContentView
								.findViewById(R.id.info_pic);
						picture.setImageResource(R.drawable.ic_launcher);
					}
					return myContentView;
				}
			});
			mMap.setOnMapClickListener(new OnMapClickListener() {
				@Override
				public void onMapClick(LatLng point) {
					selectedMarker=null;	
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
		}
	}

	private void drawPerpLine(LineSegment perp) {
		PolylineOptions rectOptions = new PolylineOptions();
		rectOptions.zIndex(1);
		rectOptions.color(Color.MAGENTA);
		Point p1 = perp.getP1();
		Point p2 = perp.getP2();
		SphericalMercatorProjection sp = new SphericalMercatorProjection(6371);
		rectOptions.add(sp.toLatLng(p1));
		rectOptions.add(sp.toLatLng(p2));
		perpPolyLine = mMap.addPolyline(rectOptions);
	}

	private void setUpCamera() {
		// Step s = DataContainer.getRouteStarter(currentRoute, dataBaseHelper);
		// mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
		// LatLng(s.getLatitude(),s.getLongitude()) , 15));
		Track t = selectedRoute.getTrack();
		List<Step> steps = DataContainer.getTrackSteps(t, dataBaseHelper);
		LatLngBounds.Builder b = new LatLngBounds.Builder();
		for (int i = 0; i < steps.size(); i++) {
			Step s = steps.get(i);
			b.include(new LatLng(s.getLatitude(), s.getLongitude()));
		}
		LatLngBounds bounds = b.build();
		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 400, 400,
				20));
	}

	private void checkLocationIsWithinEffectRadius(LatLng location) {
		boolean found = false;
		int i = 0;
		while (i < interestSteps.size() && !found) {
			Step s = interestSteps.get(i);
			float[] results = new float[3];
			Location.distanceBetween(location.latitude, location.longitude,
					s.getLatitude(), s.getLongitude(), results);
			if (results[0] <= s.getHighlight().getRadius()) {
				Log.i("HIT", "Hit interest area");
				if (interestAreaMarker != null) {
					interestAreaMarker.remove();
				}
				interestAreaMarker = mMap
						.addMarker(new MarkerOptions()
								.position(
										new LatLng(s.getLatitude(), s
												.getLongitude()))
								.title("Interesting point")
								.snippet("Interesting point")
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
				// MarkerAnimationTest.animateMarker(interestAreaMarker);
				MarkerAnimationTest.bounceMarker(interestAreaMarker);
				interestStep = s;
				found = true;
			} else {
				if (interestAreaMarker != null) {
					interestAreaMarker.remove();
					interestStep = null;
				}
			}
			i++;
		}
	}

	private void addEcoSystemMarker(Route route) {
		Step step = DataContainer.getRouteStarter(route, dataBaseHelper);
		Marker m = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(step.getLatitude(), step.getLongitude()))
				.title(route.getName())
				.snippet(route.getDescription())
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
		// BitmapDescriptorFactory.fromResource(R.drawable.ic_eco_pin)));
		// .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
	}

	private void addMarkerIfNeeded(Step step, float hue) {
		// stepTable
		HighLight hl;
		if ((hl = step.getHighlight()) != null) {
			Marker m = mMap
					.addMarker(new MarkerOptions()
							.position(
									new LatLng(step.getLatitude(), step
											.getLongitude()))
							.title(hl.getName())
							.snippet(hl.getLongText())
							.icon(BitmapDescriptorFactory
									.defaultMarker(hue)));
			// BitmapDescriptorFactory.fromResource(R.drawable.ic_wp_pin)));
			// .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
			currentRouteMarkers.put(m, step);
		}
	}

	private void addEffectRadiusIfNeeded(Step step) {
		if (interestSteps == null) {
			interestSteps = new ArrayList<Step>();
		}
		HighLight hl;
		if ((hl = step.getHighlight()) != null) {
			CircleOptions copt = new CircleOptions();
			copt.center(new LatLng(step.getLatitude(), step.getLongitude()));
			copt.radius(hl.getRadius());
			copt.zIndex(1);
			copt.strokeColor(Color.BLACK);
			copt.strokeWidth(0.1f);
			// Fill color of the circle
			// 0x represents, this is an hexadecimal code
			// 55 represents percentage of transparency. For 100% transparency,
			// specify 00.
			// For 0% transparency ( ie, opaque ) , specify ff
			// The remaining 6 characters(00ff00) specify the fill color
			copt.fillColor(0x55FF0701);
			mMap.addCircle(copt);
			interestSteps.add(step);
		}
	}

	private void drawUserRoute() {
		if(fixReceiver!=null){
			PolylineOptions rectOptions = new PolylineOptions();
			List<Step> stepsInProgress = fixReceiver.getStepsInProgress();
			if(stepsInProgress!=null){
				for (int i = 0; i < stepsInProgress.size(); i++) {
					Step step = stepsInProgress.get(i);
					rectOptions
						.add(new LatLng(step.getLatitude(), step.getLongitude()));
					addEffectRadiusIfNeeded(step);
					addMarkerIfNeeded(step,BitmapDescriptorFactory.HUE_CYAN);
				}
				rectOptions.zIndex(2);
				rectOptions.color(Color.GREEN);
				mMap.addPolyline(rectOptions);
			}		
		}
	}
	
	private void drawCurrentRouteFromDB() {
		if (currentRouteMarkers == null) {
			currentRouteMarkers = new Hashtable<Marker, Step>();
		}
		PolylineOptions rectOptions = new PolylineOptions();
		Track t = selectedRoute.getTrack();
		List<Step> steps = DataContainer.getTrackSteps(t, dataBaseHelper);
		for (int j = 0; j < steps.size(); j++) {
			Step step = steps.get(j);
			rectOptions
					.add(new LatLng(step.getLatitude(), step.getLongitude()));
			addEffectRadiusIfNeeded(step);
			addMarkerIfNeeded(step,BitmapDescriptorFactory.HUE_BLUE);
		}
		rectOptions.zIndex(1);
		rectOptions.color(Color.GRAY);
		Polyline polyline = mMap.addPolyline(rectOptions);
		if (selectedRoute.getEco() != null) {
			// addEcoSystemMarker( currentRoute.getEco() );
			Route eco = DataContainer.getRouteEcosystem(selectedRoute,
					dataBaseHelper);
			addEcoSystemMarker(eco);
		}
	}

	private TileProvider initTileProvider() {
		File f = new File(getCacheDir() + "/OSMPublicTransport_HiRes.mbtiles");
		if (!f.exists())
			try {
				InputStream is = getAssets().open(
						"OSMPublicTransport_HiRes.mbtiles");
				int size = is.available();
				byte[] buffer = new byte[size];
				is.read(buffer);
				is.close();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buffer);
				fos.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		return new MapBoxOfflineTileProvider(f.getPath());
	}

	public class FixReceiver extends BroadcastReceiver {

		private GoogleMap mMap;
		private ArrayList<Step> stepsInProgress;
		private Polyline trackInProgress;

		public FixReceiver(GoogleMap mMap) {
			super();
			this.mMap = mMap;
			stepsInProgress = new ArrayList<Step>();
		}

		public ArrayList<Step> getStepsInProgress() {
			return stepsInProgress;
		}

		public void setStepsInProgress(ArrayList<Step> steps) {
			stepsInProgress = steps;
		}

		private void updateTrackInProgress() {
			PolylineOptions rectOptions = new PolylineOptions();
			rectOptions.zIndex(1);
			rectOptions.color(Color.GREEN);									
			if (stepsInProgress.size() > 1) {								
				for (int i = 0; i < stepsInProgress.size(); i++) {
					
					Step step = stepsInProgress.get(i);
					
					CircleOptions copt = new CircleOptions();
					copt.center(new LatLng(step.getLatitude(), step.getLongitude()));
					copt.radius(step.getPrecision());
					copt.zIndex(1);
					copt.strokeColor(Color.BLACK);
					copt.strokeWidth(0.1f);
					// Fill color of the circle
					// 0x represents, this is an hexadecimal code
					// 55 represents percentage of transparency. For 100% transparency,
					// specify 00.
					// For 0% transparency ( ie, opaque ) , specify ff
					// The remaining 6 characters(00ff00) specify the fill color
					copt.fillColor(0x55FF0701);
					mMap.addCircle(copt);
					
					rectOptions.add(new LatLng(step.getLatitude(), step
							.getLongitude()));
				}
				trackInProgress = mMap.addPolyline(rectOptions);
			}
		}
		
		public Step getStepById(String id){
			for(Step s : stepsInProgress){
				if(s.getId().equalsIgnoreCase(id))
					return s;
			}
			return null;
		}
		
		private boolean locationExists(LatLng current, List<Step> steps){
			for(int i = 0; i < steps.size(); i++){
				Step thisStep = steps.get(i);
				if( thisStep.getLatitude() == current.latitude &&  
						thisStep.getLongitude() == current.longitude ){
					return true;
				}
			}
			return false;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			double lat = intent.getExtras().getDouble("lat", 0);
			double lng = intent.getExtras().getDouble("long", 0);
			double alt = intent.getExtras().getDouble("alt", 0);
			double accuracy = intent.getExtras().getDouble("acc", 0);
			DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSZ");
			long currentTime = System.currentTimeMillis();
			Date time = new Date(currentTime);
			LatLng location = new LatLng(lat, lng);
//			boolean addNewStep = true;
			boolean locationExists = locationExists(location, stepsInProgress);
//			if (stepsInProgress.size() > 0) {
//				Step last = stepsInProgress.get(stepsInProgress.size() - 1);				
//				if (last.getLatitude() == location.latitude
//						&& last.getLongitude() == location.longitude) {
//					// Point is same as last, we don't add it to the track
			if (IGlobalValues.DEBUG) {
				Log.d("onReceive", "Received new location " + lat + " "
						+ lng + " t " + df.format(time)
						+ " already logged");
			}
//					addNewStep = false;
//				}
//			}
			if (!locationExists) {
				Marker m = mMap
						.addMarker(new MarkerOptions()
								.position(location)
								.title("Location " + lat + " - " + lng + " "
										+ df.format(time))
								.snippet(
										"Location " + lat + " - " + lng + " "
												+ df.format(time))
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));				
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));

				Step s = new Step();
				s.setId( Long.toString(currentTime) );
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
				
				//Aggressive save - save location as soon as is available
				if(routeMode==2){
					DataContainer.addStepToTrack(s, routeInProgress.getTrack(), userId, dataBaseHelper);
				}
				
				updateTrackInProgress();											
				if (IGlobalValues.DEBUG) {
					Log.d("onReceive", "Received new location " + lat + " "
							+ lng + " t " + df.format(time));
				}
				if(userRouteMarkers==null){
					userRouteMarkers = new Hashtable<Marker, Step>();
				}
				userRouteMarkers.put(m, s);
			}
		}
	}

}
