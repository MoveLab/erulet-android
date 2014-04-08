package net.movelab.sudeau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseItineraryActivity extends Activity {

	private GoogleMap mMap;
	private static final LatLng MY_POINT = new LatLng(41.66, 1.54);
	private static final LatLng VALL_ARAN_1 = new LatLng(42.74, 0.79);
	private static final LatLng VALL_ARAN_2 = new LatLng(42.73, 0.82);
	private static final LatLng ESTANH_REDON = new LatLng(42.64, 0.78);
	private Button goToRouteBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_itinerary_map);
		setUpMapIfNeeded();
		initControls();
	}
	
	private void initControls(){
		goToRouteBtn = (Button) findViewById(R.id.go_to_route_btn);
		goToRouteBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(ChooseItineraryActivity.this,DetailItineraryActivity.class);
				startActivity(i);
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
				addRouteMarkers();
			}
			if (mMap != null) {
				TileProvider tileProvider = initTileProvider();
				TileOverlay tileOverlay = mMap
						.addTileOverlay(new TileOverlayOptions()
								.tileProvider(tileProvider));
				tileOverlay.setVisible(true);
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);			
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(VALL_ARAN_1,11));			
		}
	}

	private void addRouteMarkers() {
		// custom icon
		Marker my_marker = mMap.addMarker(new MarkerOptions()
				.position(VALL_ARAN_1)
				.title("Itinerari 1")
				.snippet("Descripció breu de l'itinerari 1!")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		Marker my_marker_2 = mMap.addMarker(new MarkerOptions()
				.position(VALL_ARAN_2)
				.title("Itinerari 2")
				.snippet("Descripció breu de l'itinerari 2!")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
		Marker my_marker_3 = mMap.addMarker(new MarkerOptions()
				.position(ESTANH_REDON)
				.title("Estanh Redon")
				.snippet("Descripció breu de l'itinerari 3!")
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
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

}
