package net.movelab.sudeau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.google.android.gms.maps.model.TileProvider;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class ChooseItineraryActivity extends Activity {

	private GoogleMap mMap;
//	private static final LatLng MY_POINT = new LatLng(41.66, 1.54);
//	private static final LatLng VALL_ARAN_1 = new LatLng(42.74, 0.79);
//	private static final LatLng VALL_ARAN_2 = new LatLng(42.73, 0.82);
	private static final LatLng ESTANH_REDON = new LatLng(42.64, 0.78);
	private Button goToRouteBtn;
	private DataBaseHelper dataBaseHelper;
	private Hashtable<Marker, Route> routeTable;
	private Marker selectedMarker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_itinerary_map);
		setUpDBIfNeeded();
		setUpMapIfNeeded();
		setUpCamera();
		initControls();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dataBaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        dataBaseHelper = null;
	    }
	}
	
	private void setUpDBIfNeeded() {
		if(dataBaseHelper == null){
			dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);
			DataContainer.loadSampleData(dataBaseHelper, this.getBaseContext());
		}
	}

	private void initControls() {
		goToRouteBtn = (Button) findViewById(R.id.go_to_route_btn);
		goToRouteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(selectedMarker==null){
					Toast.makeText(getApplicationContext(), "No hi ha cap ruta seleccionada; si us plau tria una ruta...", Toast.LENGTH_LONG).show();
				}else{
					Intent i = new Intent(ChooseItineraryActivity.this,
							DetailItineraryActivity.class);
					Route r = routeTable.get(selectedMarker);
					i.putExtra("idRoute",r.getId());
					startActivity(i);
				}
			}
		});
	}

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
				TileProvider tileProvider = initTileProvider();
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
					return false;
				}
			});
		}
	}
	
	private void setUpCamera(){
		LatLngBounds bounds = new LatLngBounds(ESTANH_REDON, ESTANH_REDON);
		if(routeTable.size()>0){
			if(routeTable.size()==1){
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ESTANH_REDON, 11));
			}else{
				Enumeration<Marker> markers = routeTable.keys();
				while(markers.hasMoreElements()){
					Marker m = markers.nextElement();
					bounds = bounds.including(m.getPosition());
					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));
				}
			}									
		}		
	}

	private void addRouteMarkersFromDB() {
		// custom icon
		List<Route> routes = DataContainer.getAllRoutes(dataBaseHelper);
		if(routeTable == null){
			routeTable = new Hashtable<Marker, Route>();
		}
		for(int i=0; i < routes.size(); i++){			
			Route r = routes.get(i);
			Step start = DataContainer.getRouteStarter(r, dataBaseHelper);
			Marker my_marker = mMap.addMarker(new MarkerOptions()
			.position(new LatLng(start.getLatitude(), start.getLongitude()))
			.title(r.getName())
			.snippet(r.getDescription())
			.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
			routeTable.put(my_marker, r);
		}
//		Marker my_marker = mMap.addMarker(new MarkerOptions()
//				.position(VALL_ARAN_1)
//				.title("Itinerari 1")
//				.snippet("Descripció breu de l'itinerari 1!")
//				.icon(BitmapDescriptorFactory
//						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//		Marker my_marker_2 = mMap.addMarker(new MarkerOptions()
//				.position(VALL_ARAN_2)
//				.title("Itinerari 2")
//				.snippet("Descripció breu de l'itinerari 2!")
//				.icon(BitmapDescriptorFactory
//						.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//		Marker my_marker_3 = mMap.addMarker(new MarkerOptions()
//				.position(ESTANH_REDON)
//				.title("Estanh Redon")
//				.snippet("Descripció breu de l'itinerari 3!")
//				.icon(BitmapDescriptorFactory
//						.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
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

}
