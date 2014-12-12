package net.movelab.sudeau;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.RBB;
import net.movelab.sudeau.model.Route;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class ChooseItineraryActivity extends FragmentActivity {

    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private ProgressDialog mProgressDialog;

	private GoogleMap mMap;
	private MapBoxOfflineTileProvider tileProvider;
	private Hashtable<Marker, RBB> routeTable;
	private Marker selectedMarker;
	private List<Route> routes;
	
	private String TITLE;
 	private String OPTION_1;
	private String OPTION_2;
	private String OPTION_3;
	
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST+1;
    private int third_id = Menu.FIRST+2;
    private int fourth_id = Menu.FIRST+3;
    private int fifth_id = Menu.FIRST+4;

	private ProgressBar progressBar;
    private RelativeLayout core_data_loading;

	private EruletApp app;

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
        core_data_loading = (RelativeLayout) findViewById(R.id.core_data_loading);

        if(PropertyHolder.getLastUpdateGeneralMap() > 0L && PropertyHolder.getLastUpdateGeneralReferences() >= 0L){
		setUpMapIfNeeded();
		setUpCamera();
        } else{
            waitForCoreData();
        }
	}

    private void waitForCoreData(){
        core_data_loading.setVisibility(View.VISIBLE);
        IntentFilter coreDataResponseFilter = new IntentFilter(Util.INTENT_CODE_CORE_DATE_RESPONSE);
        DownloadResultBroadcastReceiver mDownloadResultBroadcastReceiver =
                new DownloadResultBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadResultBroadcastReceiver,
                coreDataResponseFilter);
    }

    public class DownloadResultBroadcastReceiver extends BroadcastReceiver {

        // Prevents instantiation
        private DownloadResultBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {

            String this_action = intent.getAction();

            if (this_action != null && this_action.equals(Util.INTENT_CODE_CORE_DATE_RESPONSE)) {
                int response_code = intent.getIntExtra(DownloadCoreData.OUTGOING_MESSAGE_KEY_RESPONSE_CODE, -1);
            if (response_code == DownloadCoreData.RESPONSE_CODE_SUCCESS || (PropertyHolder.getLastUpdateGeneralMap() > 0L && PropertyHolder.getLastUpdateGeneralReferences() >= 0L)) {
                    core_data_loading.setVisibility(View.GONE);
                    setUpMapIfNeeded();
                    setUpCamera();
                    refreshMapView();
                }

        }
        }
    }


    @Override
	protected void onResume() {	
		super.onResume();
        currentLocale = PropertyHolder.getLocale();
        if(PropertyHolder.getLastUpdateGeneralMap() > 0L && PropertyHolder.getLastUpdateGeneralReferences() >= 0L){
        refreshMapView();
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem hide_highlights_item = menu.findItem(R.id.hide_my_highlights);
        if(hide_highlights_item != null){
            hide_highlights_item.setVisible(false);
        }
        MenuItem hide_routes_item = menu.findItem(R.id.hide_my_routes);
        if(hide_routes_item != null){
            hide_routes_item.setTitle(userItineraryMenuItemText(isUserItinerariesOn, context));
        }
        return true;
	}

    @Override
    public boolean onPrepareOptionsMenu (Menu menu){

        MenuItem hide_routes_item = (MenuItem) menu.findItem(R.id.hide_my_routes);
        if(hide_routes_item != null){
            hide_routes_item.setTitle(userItineraryMenuItemText(isUserItinerariesOn, context));
        }

        return true;
    }


    public static String userItineraryMenuItemText(boolean ison, Context this_context){
        if(ison)
            return this_context.getResources().getString(R.string.hide_my_routes);
        else
            return this_context.getResources().getString(R.string.show_my_routes);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case R.id.preferences:
                Intent i = new Intent(this, EruletPreferencesActivity.class);
                startActivity(i);
                return true;
     		case R.id.account:
                startActivity(new Intent(this, AccountActivity.class));
                return true;
            case R.id.holet_routes:
                startActivity(new Intent(this,
                        OfficialItinerariesActivity.class));
                return true;
            case R.id.my_routes:
                startActivity(new Intent(this,
                        MyItinerariesActivity.class));
                return true;
            case R.id.survey:
                Intent survey_intent = new Intent(this, SurveyActivity.class);
                survey_intent.putExtra(SurveyActivity.SURVEY_TYPE_KEY, "general_survey");
                startActivity(survey_intent);
            return true;
            case R.id.hide_my_routes:
                PropertyHolder.setUseritinerariesOn(!isUserItinerariesOn);
                isUserItinerariesOn = !isUserItinerariesOn;
                item.setTitle(userItineraryMenuItemText(isUserItinerariesOn, context));
                mMap.clear();
                mMap = null;
                setUpMapIfNeeded();
                refreshMapView();
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
            builder.setTitle(rbb.name);
            builder.setIcon(R.drawable.ic_pin_info);
            if(rbb.globalRating>=0)
                builder.setMessage(Html.fromHtml(rbb.description + "<br><br><b>" + getResources().getString(R.string.average_rating) + ":" + Float.toString(rbb.globalRating)+ "</b>"));
            else
                builder.setMessage(rbb.description);
            if(PropertyHolder.getRouteContentStatus(rbb.id) == PropertyHolder.STATUS_CODE_READY){
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
                    startActivity(intent);
                }
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
                }
            });
            }
            } else{
                builder.setNeutralButton(getString(R.string.download), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent startRouteContentDownloadIntent = new Intent(context, DownloadRouteContent.class);
                        startRouteContentDownloadIntent.putExtra(DownloadRouteContent.INCOMING_MESSAGE_KEY_ROUTE_ID, rbb.id);
                        startRouteContentDownloadIntent.putExtra(DownloadRouteContent.INCOMING_MESSAGE_KEY_ROUTE_SERVER_ID, DownloadRouteContent.SERVER_ID_CODE_GET_SERVER_ID);
                        context.startService(startRouteContentDownloadIntent);
                        dialogInterface.dismiss();
                        finish();
                        startActivity(new Intent(ChooseItineraryActivity.this,
                                OfficialItinerariesActivity.class));
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
        }
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// addMarkers
				addRouteMarkersFromDB();

				tileProvider = initTileProvider();
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

        // if no routes, send user to download activity:
        if(routesBareBones.size() == 0){
            startActivity(new Intent(ChooseItineraryActivity.this, OfficialItinerariesActivity.class));
        }

		if(routeTable == null){
			routeTable = new Hashtable<Marker, RBB>();
		}
		for(RBB rbb : routesBareBones){
			LatLng middle = DataContainer.getRouteMiddleFast(Integer.toString(rbb.trackId), app.getDataBaseHelper());
			if(middle!=null){
				Marker my_marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(middle.latitude, middle.longitude))
                        .icon(BitmapDescriptorFactory.fromResource(rbb.official?R.drawable.itinerary_marker:R.drawable.itinerary_marker_user)));
				routeTable.put(my_marker, rbb);
			}
		}
	}

	private MapBoxOfflineTileProvider initTileProvider() {
        File f = new File(PropertyHolder.getGeneralMapPath());
		if (f.exists()){
            return new MapBoxOfflineTileProvider(f.getPath());
		}else{
		}
		return null;
	}



}
