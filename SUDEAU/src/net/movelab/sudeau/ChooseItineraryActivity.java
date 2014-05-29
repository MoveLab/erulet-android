package net.movelab.sudeau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Notifications;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
	private Button goToRouteBtn;	
	private DataBaseHelper dataBaseHelper;
	private Hashtable<Marker, Route> routeTable;
	private Marker selectedMarker;
	
	private static final String TITLE = "Tria la opció de ruta que prefereixes:";
	private static final String OPTION_1 = "Vull visualitzar la ruta, sense fer un seguiment de la meva posició.";
	private static final String OPTION_2 = "Vull recórrer la ruta, fent un seguiment en tot moment de la meva posició.";
	private static final String OPTION_3 = "Vull recórrer la ruta, fent un seguiment en tot moment i desar la meva pròpia versió de la ruta.";
	
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST+1;

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
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(group1,first_id,first_id,"Els meus itineraris...");		
		menu.add(group1,second_id,second_id,"Itineraris compartits...");
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
		if (dataBaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        dataBaseHelper = null;
	    }
	}
	
	public void showItineraryOptions(){		
		final CharSequence[] items = {OPTION_1,OPTION_2,OPTION_3};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
					startActivity(i);
				}
			}
		);
		builder.show();
	}
	
	private void setUpDBIfNeeded() {
		if(dataBaseHelper == null){
			dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);
			//DataContainer.loadSampleData(dataBaseHelper, this.getBaseContext());
			DataContainer.loadRedon(dataBaseHelper, this.getBaseContext());
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
					showItineraryOptions();										
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
					Route r = routeTable.get(marker);					
					return false;
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
		            snippet.setText(r.getDescription());
		            title.setText(r.getName());
		            ImageView picture = (ImageView)myContentView.findViewById(R.id.info_pic);
		            picture.setImageResource(R.drawable.ic_itinerary_icon);		            
		            return myContentView;
				}
			});
						
		}
	}
	
	private void setUpCamera(){				
		LatLngBounds bounds = new LatLngBounds.Builder().include(IGlobalValues.ESTANH_REDON).build();                
		if(routeTable.size()>0){
			if(routeTable.size()==1){
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(IGlobalValues.ESTANH_REDON, 11));
			}else{
				Enumeration<Marker> markers = routeTable.keys();
				while(markers.hasMoreElements()){
					Marker m = markers.nextElement();
					bounds = bounds.including(m.getPosition());
					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,400,400,20));
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
			.snippet(null)
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
