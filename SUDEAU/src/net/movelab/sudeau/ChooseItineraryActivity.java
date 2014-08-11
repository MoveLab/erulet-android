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
import android.os.Environment;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
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
	private MapBoxOfflineTileProvider tileProvider;
//	private static final LatLng MY_POINT = new LatLng(41.66, 1.54);
//	private static final LatLng VALL_ARAN_1 = new LatLng(42.74, 0.79);
//	private static final LatLng VALL_ARAN_2 = new LatLng(42.73, 0.82);		
	//private DataBaseHelper dataBaseHelper;
	private Hashtable<Marker, Route> routeTable;
	private Marker selectedMarker;
	
	//private static final String TITLE = "Tria la opció de ruta que prefereixes:";
	private String TITLE = getString(R.string.trip_option);
	private String OPTION_1 = getString(R.string.trip_option_1);
	private String OPTION_2 = getString(R.string.trip_option_2);
	private String OPTION_3 = getString(R.string.trip_option_3);
	
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST+1;
	
	private EruletApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_itinerary_map);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
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
	
	private void refreshMapView(){
		mMap.clear();
		if(tileProvider!=null){
			tileProvider.close();
		}
		tileProvider = initTileProvider();
		TileOverlay tileOverlay = mMap
				.addTileOverlay(new TileOverlayOptions()
						.tileProvider(tileProvider));
		tileOverlay.setVisible(true);
		addRouteMarkersFromDB();
		setUpCamera();
	}

	private void addRouteMarkersFromDB() {
		// custom icon
		List<Route> routes = DataContainer.getAllRoutes(app.getDataBaseHelper());
		if(routeTable == null){
			routeTable = new Hashtable<Marker, Route>();
		}
		for(int i=0; i < routes.size(); i++){			
			Route r = routes.get(i);
			Step start = DataContainer.getRouteStarter(r, app.getDataBaseHelper());
			if(start!=null){
//				Marker my_marker = mMap.addMarker(new MarkerOptions()
//				.position(new LatLng(start.getLatitude(), start.getLongitude()))
//				.title(r.getName())
//				.snippet(null)
//				.icon(BitmapDescriptorFactory
//						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				Marker my_marker = MapObjectsFactory.addStartRouteMarker(mMap, new LatLng(start.getLatitude(), start.getLongitude()), r.getName());
				routeTable.put(my_marker, r);
			}
		}
	}

	private MapBoxOfflineTileProvider initTileProvider() {		
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
