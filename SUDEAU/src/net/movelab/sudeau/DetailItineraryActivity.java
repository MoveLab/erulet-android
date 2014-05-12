package net.movelab.sudeau;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import net.movelab.sudeau.TrackingFixGet.StopReceiver;
import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import net.movelab.sudeau.util.MarkerAnimationTest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class DetailItineraryActivity extends Activity {
	
	private GoogleMap mMap;	
	private DataBaseHelper dataBaseHelper;
	private Marker bogus_location;
	private Marker interestAreaMarker;
	private Step interestStep;
	private Route currentRoute;
	private ArrayList<Step> interestSteps;		
	private IntentFilter fixFilter;
	private FixReceiver fixReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_itinerary_map);
		setupView();
		Button btn_compass = (Button)findViewById(R.id.go_to_compass);
		btn_compass.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(DetailItineraryActivity.this,CompassActivity.class);
				startActivity(i);				
			}
		});
		
		Button btn_stop_tracking = (Button)findViewById(R.id.stop_tracking);
		btn_stop_tracking.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				stopTracking();
			}
		});
		
		startTracking();
	}
	
	private void startTracking(){
		Intent intent = new Intent(
				getString(R.string.internal_message_id) + Util.MESSAGE_SCHEDULE);
		sendBroadcast(intent);
				
		fixFilter = new IntentFilter(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_RECORDED);
		
		fixReceiver = new FixReceiver(mMap);
		registerReceiver(fixReceiver, fixFilter);
	}
	
	private void stopTracking(){
		Intent intent = new Intent(
				getString(R.string.internal_message_id) + Util.MESSAGE_UNSCHEDULE);
		sendBroadcast(intent);
	}
	
	private void setupView(){
		setUpDBIfNeeded();
		setCurrentRoute();
		setUpMapIfNeeded();		
		setUpCamera();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {	
		super.onConfigurationChanged(newConfig);
		setupView();
	}
	
	private void setCurrentRoute(){
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			String idRoute = extras.getString("idRoute");
			currentRoute = DataContainer.findRouteById(idRoute, dataBaseHelper);
		}
	}
	
	private void setUpDBIfNeeded() {
		if(dataBaseHelper == null){
			dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);			
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dataBaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        dataBaseHelper = null;
	    }
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
			if (mMap != null) {
				// addMarkers
				addRouteMarkersFromDB();
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);								
			
			mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {				
				@Override
				public void onInfoWindowClick(Marker marker) {
					// TODO Auto-generated method stub
					Toast.makeText(getApplicationContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
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
		            snippet.setText(marker.getSnippet());		            
		            ImageView picture = (ImageView)myContentView.findViewById(R.id.info_pic);
		            Drawable image = null;
		            byte[] b = DataContainer.getStepMedia(interestStep, dataBaseHelper);
		            if(b != null){
			            ByteArrayInputStream is = new ByteArrayInputStream(b);
			            image = Drawable.createFromStream(is, null);
			            picture.setImageDrawable(image);
		            }
		            return myContentView;
				}
			});
			mMap.setOnMapClickListener(new OnMapClickListener() {				
				@Override
				public void onMapClick(LatLng point) {
					if(bogus_location != null){
						bogus_location.remove();						
					}
					bogus_location = mMap.addMarker(new MarkerOptions()
					.position(new LatLng(point.latitude, point.longitude))
					.title("Bogus location")
					.snippet("Bogus location")					
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_erulet_pin)));
					checkLocationIsWithinEffectRadius(point);					
				}
			});
		}
	}
	
	private void setUpCamera(){
		Step s = DataContainer.getRouteStarter(currentRoute, dataBaseHelper);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(s.getLatitude(),s.getLongitude()) , 15));
	}
	
	private void checkLocationIsWithinEffectRadius(LatLng location){
		boolean found = false;
		int i = 0;
		while( i < interestSteps.size() && !found ){		
			Step s = interestSteps.get(i);
			float[] results = new float[3];
			Location.distanceBetween(
					location.latitude, 
					location.longitude, 
					s.getLatitude(), 
					s.getLongitude(), 
					results);
			if(results[0] <= s.getHighlight().getRadius()){
				Log.i("HIT","Hit interest area");
				if(interestAreaMarker != null){
					interestAreaMarker.remove();					
				}
				interestAreaMarker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(s.getLatitude(), s.getLongitude()))
				.title("Interesting point")
				.snippet("Interesting point")
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
				//MarkerAnimationTest.animateMarker(interestAreaMarker);
				MarkerAnimationTest.bounceMarker(interestAreaMarker);
				interestStep = s;
				found = true;
			}else{
				if(interestAreaMarker != null){
					interestAreaMarker.remove();
					interestStep = null;
				}
			}
			i++;
		}
	}
	
	private void addEffectRadiusIfNeeded(Step step){
		if(interestSteps == null){
			interestSteps = new ArrayList<Step>();
		}
		HighLight hl;
		if( (hl = step.getHighlight()) !=null ){
			CircleOptions copt = new CircleOptions();
			copt.center(new LatLng(step.getLatitude(), step.getLongitude()));
			copt.radius(hl.getRadius());
			copt.zIndex(1);
			copt.strokeColor(Color.BLACK);
			copt.strokeWidth(0.1f);
			// Fill color of the circle
            // 0x represents, this is an hexadecimal code
            // 55 represents percentage of transparency. For 100% transparency, specify 00.
            // For 0% transparency ( ie, opaque ) , specify ff
            // The remaining 6 characters(00ff00) specify the fill color
			copt.fillColor(0x55FF0701);			
			mMap.addCircle(copt);
			interestSteps.add(step);
		}
	}
	
	private void addRouteMarkersFromDB() {		
		PolylineOptions rectOptions = new PolylineOptions();
		Track t = currentRoute.getTrack();
		List<Step> steps = DataContainer.getTrackSteps(t, dataBaseHelper);
		for(int j=0; j < steps.size(); j++){
			Step step = steps.get(j);
			rectOptions.add(new LatLng(step.getLatitude(), step.getLongitude()));
			addEffectRadiusIfNeeded(step);
		}
		rectOptions.zIndex(1);
		rectOptions.color(Color.RED);								
		Polyline polyline = mMap.addPolyline(rectOptions);
	}
	
	private TileProvider initTileProvider() {
		File f = new File(getCacheDir() + "/OSMPublicTransport_HiRes.mbtiles");
		if (!f.exists())
			try {
				InputStream is = getAssets().open("OSMPublicTransport_HiRes.mbtiles");
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
		
		GoogleMap mMap;
		
		public FixReceiver(GoogleMap mMap){
			super();
			this.mMap=mMap;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			double lat = intent.getExtras().getDouble("lat", 0);
			double lng = intent.getExtras().getDouble("long", 0);
			DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSZ");
			Date time = new Date(System.currentTimeMillis());
			//Toast.makeText(getApplicationContext(), lat + " - " + lng, Toast.LENGTH_SHORT).show();
			LatLng location = new LatLng(lat, lng);
			Marker m = mMap.addMarker(new MarkerOptions()
			.position(location)
			.title("Location " + lat + " - " + lng + " " + df.format(time))
			.snippet("Location " + lat + " - " + lng + " " + df.format(time))
			.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location , 15));
			
		}
	}

	@SuppressLint("NewApi")
	private void saveCellData(Context context) {

		if (PropertyHolder.getShareData()) {

			String phoneTime = Util.iso8601(System.currentTimeMillis());
			String cid = "n";
			String lac = "n";
			String countryISO = "n";
			String sid = "n";
			String bid = "n";
			String nid = "n";
			String bsLat = "n";
			String bsLon = "n";

			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

			if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
				GsmCellLocation loc = (GsmCellLocation) tm.getCellLocation();
				if(loc != null){
				cid = Integer.toHexString(loc.getCid());
				lac = Integer.toHexString(loc.getLac());
				}
				countryISO = tm.getNetworkCountryIso();
			}
			if (tm.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {

				if (Build.VERSION.SDK_INT >= 5) {
					CdmaCellLocation loc = (CdmaCellLocation) tm
							.getCellLocation();
					if(loc != null){
					sid = Integer.toHexString(loc.getSystemId());
					bid = Integer.toHexString(loc.getBaseStationId());
					nid = Integer.toHexString(loc.getNetworkId());
					bsLat = Integer.toHexString(loc.getBaseStationLatitude());
					bsLon = Integer.toHexString(loc.getBaseStationLongitude());
					}
				}
			}

			if (cid.equals("n") && lac.equals("n") && countryISO.equals("n")
					&& bid.equals("n") && nid.equals("n") && sid.equals("n")
					&& bsLat.equals("n") && bsLon.equals("n")) {
				// do nothing
			} else {
				String thisCellInfo = phoneTime + "," + cid + "," + lac + ","
						+ countryISO + "," + bid + "," + nid + "," + sid + ","
						+ bsLat + "," + bsLon;

				ContentResolver ucr = getContentResolver();
				ucr.insert(Util.getUploadQueueUri(context),
						TrackingUploadContentValues.createUpload("CEL", thisCellInfo));

			}
		}

	}
	

}
