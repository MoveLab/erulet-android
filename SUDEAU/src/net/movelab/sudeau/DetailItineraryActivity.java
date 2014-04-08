package net.movelab.sudeau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailItineraryActivity extends Activity {
	
	private GoogleMap mMap;
	private static final LatLng ROUTE_START = new LatLng(42.6274, 0.7633);
	private static final LatLng ROUTE_END = new LatLng(42.6382, 0.7750);	
	private static final LatLng ROUTE_MIDDLE = new LatLng(42.6354, 0.7663);
	
	private static float[] color_table = {								
		BitmapDescriptorFactory.HUE_RED,
		BitmapDescriptorFactory.HUE_ROSE,
		BitmapDescriptorFactory.HUE_ORANGE,
		BitmapDescriptorFactory.HUE_YELLOW,
		BitmapDescriptorFactory.HUE_GREEN,
		BitmapDescriptorFactory.HUE_CYAN,
		BitmapDescriptorFactory.HUE_BLUE,
		BitmapDescriptorFactory.HUE_AZURE,		
		BitmapDescriptorFactory.HUE_MAGENTA,
		BitmapDescriptorFactory.HUE_VIOLET
	};
	
	private static String[] POINT_OF_INTEREST_PICTURE = {
		"@drawable/detail_pic_0",
		null,
		"@drawable/detail_pic_2",
		null,
		null,
		null,
		null,
		null,		
		"@drawable/detail_pic_8",
		null,
		null,
		null,
		null,
		null,
		"@drawable/detail_pic_14"
	};
	
	private static String NO_PICTURE = "@drawable/no_picture"; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_itinerary_map);
		setUpMapIfNeeded();		
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
				addRouteMarkers();
			}
			mMap.setMapType(GoogleMap.MAP_TYPE_NONE);			
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ROUTE_MIDDLE,14));
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
					//TextView title = (TextView) myContentView.findViewById(R.id.info_title);
					//title.setText(marker.getTitle());
		            
					TextView snippet = (TextView) myContentView.findViewById(R.id.info_snippet);
		            snippet.setText(marker.getSnippet());
		            
		            ImageView picture = (ImageView)myContentView.findViewById(R.id.info_pic);
		            String str_pic = POINT_OF_INTEREST_PICTURE[ Integer.parseInt(marker.getTitle()) ];
		            if(str_pic == null){
		            	str_pic = NO_PICTURE;
		            }
		            int resId = getResources().getIdentifier(str_pic, null, getPackageName());
		            picture.setImageResource(resId);
		            return myContentView;
				}
			});
		}
	}
	
	private void addRouteMarkers() {
		PolylineOptions rectOptions = new PolylineOptions()
		.add(new LatLng(42.6274, 0.7633))
		.add(new LatLng(42.6281, 0.7617))
		.add(new LatLng(42.6292, 0.7608))
		.add(new LatLng(42.6300, 0.7594))
		.add(new LatLng(42.6309, 0.7597))
		.add(new LatLng(42.6321, 0.7609))
		.add(new LatLng(42.6334, 0.7629))
		.add(new LatLng(42.6347, 0.7645))
		.add(new LatLng(42.6354, 0.7663))
		.add(new LatLng(42.6357, 0.7684))
		.add(new LatLng(42.6357, 0.7702))
		.add(new LatLng(42.6360, 0.7716))
		.add(new LatLng(42.6370, 0.7727))
		.add(new LatLng(42.6375, 0.7740))
		.add(new LatLng(42.6382, 0.7750));
		rectOptions.zIndex(1);
		rectOptions.color(Color.RED);
				
		int i = 0;		
		int j = 0;
		for ( LatLng interestPoint : rectOptions.getPoints() ){
			if(i % 2 == 0){
				MarkerOptions mOpt = new MarkerOptions();
				mOpt.position(interestPoint);
				mOpt.title(Integer.toString(j));				
				if(POINT_OF_INTEREST_PICTURE[j] == null){
					mOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
					mOpt.snippet("No massa interès...");
				}else{
					mOpt.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
					mOpt.snippet("Una imatge!");
				}
				Marker m = mMap.addMarker(mOpt);
			}
			j++;
			i++;			
			if(i > 9) i=0;
		}			
		
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
	

}
