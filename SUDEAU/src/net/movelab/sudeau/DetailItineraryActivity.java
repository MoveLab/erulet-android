package net.movelab.sudeau;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Property;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.SphericalUtil;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;

public class DetailItineraryActivity extends Activity implements
        GooglePlayServicesClient.ConnectionCallbacks,
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
    // MINI_KIND
    static final int IMAGE_THUMBNAIL_WIDTH = 384;
    static final int IMAGE_THUMBNAIL_HEIGTH = 512;
    static final String TAG = "DetailItineraryActivity";

    private LocationClient locationClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private EruletApp app;
    private MapObjectsFactory mObjFactory;
    private CountDownTimer countDown;
    private ProximityWarning proximityWarning;
    public int screenWidth = 200;
    String locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        // | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.detail_itinerary_map);
        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        Context context = getApplication();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

        Display display = getWindowManager().getDefaultDisplay();
        screenWidth = display.getWidth();  // deprecated

        proximityWarning = new ProximityWarning(app);
        mObjFactory = new MapObjectsFactory();
        // Check availability of google play services
        int status = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are
            // not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
                    requestCode);
            dialog.show();
        } else { // Proceed normally

            setWorkingMode();
            setupView();

            btn_compass = (ImageButton) findViewById(R.id.btn_compass);
            btn_compass.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(DetailItineraryActivity.this,
                            CompassActivity.class);
                    if (selectedMarker != null) {
                        Step s = selectedRouteMarkers.get(selectedMarker);
                        if (s != null) {
                            i.putExtra("idStep", s.getId());
                        }
                        if (selectedMarker.equals(startMarker)) {
                            if (selectedRouteSteps != null) {
                                i.putExtra("idStep", selectedRouteSteps.get(0)
                                        .getId());
                            }
                        }
                        if (selectedMarker.equals(arrivalMarker)) {
                            if (selectedRouteSteps != null) {
                                i.putExtra(
                                        "idStep",
                                        selectedRouteSteps.get(
                                                selectedRouteSteps.size() - 1)
                                                .getId());
                            }
                        }
                    }
                    i.putExtra("wasTracking", app.isTrackingServiceOn());
                    startActivity(i);
                    // }
                }
            });

            btn_stop_tracking = (ImageButton) findViewById(R.id.btn_stop_tracking);
            btn_stop_tracking.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(DetailItineraryActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(getString(R.string.finish_trip))
                            .setMessage(getString(R.string.finish_trip_long))
                            .setPositiveButton(getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            stopTracking();
                                        }
                                    })
                            .setNegativeButton(getString(R.string.no),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
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
                        Toast.makeText(getApplicationContext(),
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
        // if (routeMode == 1 && tracking) {
        // stopTracking();
        // }
        if ((routeMode == 1 || routeMode == 2) && app.isTrackingServiceOn()) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.finish_trip))
                    .setMessage(getString(R.string.finish_trip_leave_long))
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    stopTracking();
                                    finish();
                                }
                            }).setNegativeButton(getString(R.string.no), null)
                    .show();
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
                String hlId = data.getStringExtra("hlid");
                int hlType = data.getIntExtra("hlType", HighLight.WAYPOINT);
                HighLight hl = null;
                if (hlId == null) {
                    hl = new HighLight();
                } else {
                    hl = DataContainer.findHighLightById(hlId, app.getDataBaseHelper());
                }
                hl.setName(hlName);
                hl.setLongText(hlLongText);
                hl.setMediaPath(imagePath);
                FileManifest new_file_manifest = new FileManifest();
                new_file_manifest.setPath(imagePath);
                hl.setFileManifest(new_file_manifest);
                hl.setType(hlType);
                Step s = fixReceiver.getStepById(stepBeingEditedId);
                hl.setRadius(s.getPrecision());
                if (s != null) {
                    if (hlId == null) {
                        // Aggressively save highlight
                        //s.getHighlights().add(hl);
                        saveHighLight(hl);
                    } else {
                        updateHighLight(hl);
                    }
                    DataContainer.refreshStep(s, app.getDataBaseHelper());
                    DataContainer.getStepHighLights(s, app.getDataBaseHelper());
                }
                // This is a cheap way to refresh the info window content
                if (selectedMarker != null) {
                    selectedMarker.setIcon(MapObjectsFactory
                            .getBWUserBitmapDescriptor(hlType));
                    selectedMarker.hideInfoWindow();
                    selectedMarker.showInfoWindow();
                    // removeTemporalMarkers(selectedMarker);
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.user_cancel), Toast.LENGTH_LONG)
                        .show();
            }
        }
        if (requestCode == END_TRIP) { //
            // Clear map
            fixReceiver.clearTrackObjects();
            // Remove route in progress markers
            clearRouteMarkers();
            // Set working mode to map
            routeMode = 0;
            // Adjust interface
            configureUI();
            // Load saved route (not strictly necessary...)
            selectedRoute = routeInProgress;
            // Free resources from fixReceiver, we don't need it anymore
            fixReceiver.destroy();
            fixReceiver = null;
            // Re-display selected route
            resetSelectedRouteMarkers();
            updateSelectedRoute();
        }
    }

    private void clearRouteMarkers() {
        if (routeInProgressMarkers != null) {
            Enumeration<Marker> e = routeInProgressMarkers.keys();
            while (e.hasMoreElements()) {
                Marker m = e.nextElement();
                m.remove();
            }
            routeInProgressMarkers.clear();
        }
    }

    private void resetSelectedRouteMarkers() {
        Enumeration<Marker> e = selectedRouteMarkers.keys();
        while (e.hasMoreElements()) {
            e.nextElement().remove();
        }
        selectedRouteMarkers.clear();
    }

    private void saveHighLight(HighLight h) {
        Step s = DataContainer.findStepById(stepBeingEditedId,
                app.getDataBaseHelper());
        if (s == null) {
            // Something has gone very wrong
            Log.d("saveHighLight", "Step id not found " + stepBeingEditedId);
        } else {
            DataContainer.addHighLightToStep(s, h,
                    PropertyHolder.getUserId(),
                    app.getDataBaseHelper());
        }
    }

    private void updateHighLight(HighLight h) {
        DataContainer.updateHighLight(h, app.getDataBaseHelper());
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

//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		if (selectedRoute != null) {
////			if (selectedRoute.getReference() == null) {
////				menu.getItem(0).setVisible(false);
////			}
//			// if(selectedRoute.getInteractiveImage()==null){
//			// menu.getItem(1).setVisible(false);
//			// }
//			// for (int i = 0; i < menu.size(); i++) {
//			// menu.getItem(i).setVisible(false);
//			// }
//		}
//		return true;
//	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
////		menu.add(group1, first_id, first_id, getString(R.string.envir_data));
////		menu.add(group1, second_id, second_id,
////				getString(R.string.interactive_pic));
//		return super.onCreateOptionsMenu(menu);
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case 1:
//			Intent ihtml = new Intent(DetailItineraryActivity.this,
//					HTMLViewerActivity.class);
//			ihtml.putExtra("idReference", selectedRoute.getReference().getId());
//			startActivity(ihtml);
//			return true;
//			// case 2:
//			// Intent i = new Intent(DetailItineraryActivity.this,
//			// InteractiveImageActivityHeatMap.class);
//			// i.putExtra("int_image_id",
//			// selectedRoute.getInteractiveImage().getId());
//			// startActivity(i);
//			// return true;
//		default:
//			break;
//		}
//		return super.onOptionsItemSelected(item);
//	}

    private void startOrResumeTracking() {
        if (routeMode > 0) {
            app.startTrackingService();
            fixFilter = new IntentFilter(getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_FIX_RECORDED);
            fixReceiver = new FixReceiver(mMap, proximityWarning,
                    selectedRouteMarkers);
            registerReceiver(fixReceiver, fixFilter);
        }
    }

    private void singlePositionFix() {
        checkLocationServicesStatus();
        Location current = locationClient.getLastLocation();
        if (current != null) {
            addTemporalPositionMarker(current);
        } else {
            Toast.makeText(this, getString(R.string.still_lost),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void addTemporalPositionMarker(Location current) {
        if (countDown != null) {
            countDown.cancel();
        } else {
            countDown = new CountDownTimer(2000, 2000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    // TODO Auto-generated method stub
                    // Do nothing on tick
                }

                @Override
                public void onFinish() {
                    // TODO Auto-generated method stub
                    lastPositionMarker.remove();
                    lastPositionMarker = null;
                }
            };
        }
        LatLng currentLatLng = new LatLng(current.getLatitude(),
                current.getLongitude());
        CameraUpdate cu = null;
        if (mMap.getCameraPosition() != null) {
            cu = CameraUpdateFactory.newLatLngZoom(currentLatLng,
                    mMap.getCameraPosition().zoom);
        } else {
            cu = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16);
        }
        if (lastPositionMarker == null) {
            lastPositionMarker = mObjFactory.addLastPositionMarker(mMap,
                    getString(R.string.you_are_here_exp),
                    getString(R.string.more_or_less_par));
        }
        lastPositionMarker.setPosition(currentLatLng);
        lastPositionMarker.showInfoWindow();
        if (cu != null) {
            mMap.moveCamera(cu);
        }
        countDown.start();
    }

    // private Route createNewRoute() {
    // ArrayList<Step> currentSteps = fixReceiver.getStepsInProgress();
    // String android_id = Secure.getString(getBaseContext()
    // .getContentResolver(), Secure.ANDROID_ID);
    // Track t = new Track();
    // t.setSteps(currentSteps);
    // for (int i = 0; i < currentSteps.size(); i++) {
    // Step s = currentSteps.get(i);
    // s.setTrack(t);
    // }
    // // Save route
    // Route r = new Route();
    // if (selectedRoute != null)
    // r.setIdRouteBasedOn(selectedRoute.getId());
    // r.setUserId(android_id);
    // r.setTrack(t);
    // return r;
    // }

    private void saveRoute() {
        if (routeInProgress.getTrack() != null
                && routeInProgress.getTrack().getSteps() != null
                && routeInProgress.getTrack().getSteps().size() < 2) {

            // Route has less than 2 steps - offer the chance to directly delete

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage(getString(R.string.trip_with_less_than_2_steps));
            dialog.setPositiveButton(getString(R.string.discard_trip),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            // Delete route and go to itinerary selection
                            DataContainer.deleteRouteCascade(routeInProgress,
                                    app);
                            finish();
                        }
                    });
            dialog.setNegativeButton(getString(R.string.save),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            // Go to save route
                            startSaveRouteInProgressIntent();
                        }
                    });
            dialog.show();

        } else {
            // Route has more than 2 steps - still offer the chance to directly delete

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            // TODO externalize and translate
            dialog.setMessage("Would you like to save the track you just created or discard it?");
            dialog.setPositiveButton(getString(R.string.discard_trip),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            // Delete route and go to itinerary selection
                            DataContainer.deleteRouteCascade(routeInProgress,
                                    app);
                            finish();
                        }
                    });
            dialog.setNegativeButton(getString(R.string.save),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            // Go to save route
                            startSaveRouteInProgressIntent();
                        }
                    });
            dialog.show();
        }
    }

    private void startSaveRouteInProgressIntent() {
        Intent i = new Intent(DetailItineraryActivity.this,
                EditRouteActivity.class);
        String routeJson;
        try {
            routeJson = JSONConverter.routeToJSONObject(routeInProgress)
                    .toString();
            i.putExtra("routeJson", routeJson);
            startActivityForResult(i, END_TRIP);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopTracking() {
        // Intent intent = new Intent(getString(R.string.internal_message_id)
        // + Util.MESSAGE_UNSCHEDULE);
        // sendBroadcast(intent);
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
                // if(s.getHighlight()!=null){
                // hlType = s.getHighlight().getType();
                // m = MapObjectsFactory.addUserHighLightMarker(
                // mMap,
                // m.getPosition(),
                // m.getTitle(),
                // m.getSnippet(),
                // hlType);
                // }
                if (s.getHighlights() != null) {
                    List<HighLight> highLights = DataContainer
                            .getStepHighLights(s, app.getDataBaseHelper());
                    if (s.hasSingleHighLight()) {
                        hlType = highLights.get(0).getType();
                    } else {
                        hlType = HighLight.CONTAINER_N;
                    }
                    m = MapObjectsFactory.addUserHighLightMarker(mMap,
                            m.getPosition(), m.getTitle(), m.getSnippet(),
                            hlType);
                }
                Log.d(TAG, "refresh markers added " + hlType);
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
            selectedRoute = DataContainer.findRouteById(idRoute,
                    app.getDataBaseHelper());
        }
        // Create route entry in database
        // We create a route also in mode 1, though we only draw the points the
        // user hits
        if (routeInProgress == null && (routeMode == 1 || routeMode == 2)) {
            // This is very important for the ratings system
            String idRouteBasedOn = selectedRoute != null ? selectedRoute
                    .getId() : null;
            routeInProgress = DataContainer.createEmptyRoute(locale,
                    app.getDataBaseHelper(),
                    PropertyHolder.getUserId(),
                    idRouteBasedOn);
        }
    }

    // private void setUpDBIfNeeded() {
    // if (dataBaseHelper == null) {
    // dataBaseHelper = OpenHelperManager.getHelper(this,
    // DataBaseHelper.class);
    // userId = DataContainer.getAndroidId(getContentResolver());
    // }
    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if (dataBaseHelper != null) {
        // OpenHelperManager.releaseHelper();
        // dataBaseHelper = null;
        // }
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
            mMap = ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.mapDetail)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                tileProvider = initTileProvider();
                if (tileProvider != null) {
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
                    if (isUserMarker) {
                        if ((routeMode == 1 || routeMode == 2) && isUserMarker) {
                            // We are editing. On click, if it's an user marker
                            // we go to
                            // Edit highlight activity
                            // �$$�
                            if (s.hasHighLights()) {
                                // Show multiple highlight dialog
                                // add new
                                // edit existing
                                showEditMultipleHighLightDialog(s);
                            } else {
                                // New highlight
                                launchHighLightEditIntent(s, null);
                            }
                        } else { // Go to highlight detail (if there's some
                            // media to show)
                            if (s != null && s.getHighlights() != null) {
                                List<HighLight> highlights = DataContainer.getStepHighLights(s, app.getDataBaseHelper());
                                if (s.hasSingleHighLight()) {
                                    HighLight h = highlights.get(0);
                                    JSONObject hl_s;
                                    try {
                                        hl_s = JSONConverter
                                                .stepToJSONObject(s);
                                        if (hl_s != null) {
                                            String s_j_string = hl_s.toString();
                                            Intent i = new Intent(
                                                    DetailItineraryActivity.this,
                                                    DetailHighLightActivity.class);
                                            i.putExtra("step_j", s_j_string);
                                            i.putExtra("highlight_id", h.getId());
                                            startActivity(i);
                                        }
                                    } catch (JSONException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                } else { // if multiple highlights, open
                                    // highlight menu
                                    // �$$�
                                }
                            }
                        }
                    } else { // Is not user marker
                        if (marker.equals(startMarker)
                                || marker.equals(arrivalMarker)) {
                            // //Do nothing in special
                        } else if (s != null && s.hasHighLights()) {
                            List<HighLight> highlights = DataContainer
                                    .getStepHighLights(s,
                                            app.getDataBaseHelper());
                            if (s != null && s.hasSingleHighLight()) {
                                HighLight h = highlights.get(0);
                                // if highlight has single interactive image or single reference
                                JSONObject hl_s;
                                try {
                                    hl_s = JSONConverter
                                            .stepToJSONObject(s);
                                    if (hl_s != null) {
                                        String s_j_string = hl_s.toString();
                                        Intent i = new Intent(
                                                DetailItineraryActivity.this,
                                                DetailHighLightActivity.class);
                                        i.putExtra("step_j", s_j_string);
                                        if (s != null) {
                                            DataContainer.refreshStepForTrack(s, app.getDataBaseHelper());
                                            Track t = s.getTrack();
                                            if (t != null) {
                                                DataContainer.refreshTrackForRoute(t, app.getDataBaseHelper());
                                                Route r = t.getRoute();
                                                if (r != null) {
                                                    i.putExtra("route_id", r.getId());
                                                }
                                            }
                                        }
                                        if (h != null) {
                                            i.putExtra("highlight_id", h.getId());
                                        }
                                        startActivity(i);
                                    }
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                            } else { // Multiple HighLights
                                // �$$� Open multiple highlights menu
                                if (s != null) {
                                    Intent i = new Intent(
                                            DetailItineraryActivity.this,
                                            MultipleHighLightSelection.class);
                                    i.putExtra("step_id", s.getId());

                                        DataContainer.refreshStepForTrack(s, app.getDataBaseHelper());
                                        Track t = s.getTrack();
                                        if (t != null) {
                                            DataContainer.refreshTrackForRoute(t, app.getDataBaseHelper());
                                            Route r = t.getRoute();
                                            if(r!=null)
                                                i.putExtra("route_id", r.getId());
                                    startActivity(i);
                                }}
                            }
                        }
                    }
                    // Step s = selectedRouteMarkers.get(marker);
                    // boolean isUserMarker = false;
                    // if (s == null) {
                    // if (routeInProgressMarkers != null) {
                    // s = routeInProgressMarkers.get(marker);
                    // isUserMarker = true;
                    // }
                    // }
                    // if (s != null && s.getReference() != null) {
                    // //Interactive image
                    // Reference r =
                    // DataContainer.refreshReference(s.getReference(),
                    // app.getDataBaseHelper());
                    // if(r.getName()!= null && r.getName().contains("jpg")){
                    // Intent i = new Intent(DetailItineraryActivity.this,
                    // InteractiveImageActivityHeatMap.class);
                    // i.putExtra("int_image_id", r.getTextContent());
                    // startActivity(i);
                    // }else{
                    // Intent i = new Intent(DetailItineraryActivity.this,
                    // HTMLViewerActivity.class);
                    // i.putExtra("idReference", r.getId());
                    // startActivity(i);
                    // }
                    // } else {
                    // //Might be start/end markers
                    // if(marker.equals(startMarker) ||
                    // marker.equals(arrivalMarker)){
                    // //Do nothing in special
                    // }else{
                    // //We are editing. On click, if it's an user marker we go
                    // to
                    // //Edit highlight activity
                    // if ( (routeMode == 1 || routeMode == 2) && isUserMarker)
                    // {
                    // //�$$�
                    // //launchHighLightEditIntent(s);
                    // }else{ //Go to highlight detail (if there's some media to
                    // show)
                    // if(s != null && s.getHighlights()!=null){
                    // if(s.hasSingleHighLight()){
                    // JSONObject hl_s;
                    // try {
                    // hl_s = JSONConverter.stepToJSONObject(s);
                    // if(hl_s != null){
                    // String s_j_string = hl_s.toString();
                    // Intent i = new
                    // Intent(DetailItineraryActivity.this,DetailHighLightActivity.class);
                    // i.putExtra("step_j", s_j_string);
                    // startActivity(i);
                    // }
                    // } catch (JSONException e) {
                    // // TODO Auto-generated catch block
                    // e.printStackTrace();
                    // }
                    // }else{ //if multiple highlights, open highlight menu
                    // //�$$�
                    // }
                    // }
                    // }
                    // }
                    // }
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
                    float maxwidthF = (float) screenWidth / 2;
                    int maxwidth = (int) Math.round(maxwidthF);
                    snippet.setMaxWidth(maxwidth);
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
                            if (s.hasSingleHighLight()) {
                                List<HighLight> highLights = DataContainer
                                        .getStepHighLights(s,
                                                app.getDataBaseHelper());
                                HighLight h1 = highLights.get(0);
                                // HighLight h1 = s.getHighlight();
                                title.setText(h1.getName());
                                snippet.setText(h1.getLongText());
                                ImageView picture = (ImageView) myContentView
                                        .findViewById(R.id.info_pic);
                                DataContainer.refreshHighlightForFileManifest(h1, app.getDataBaseHelper());
                                if (h1.hasMediaFile()) {
                                    String file = h1.getFileManifest().getPath();
                                    if (file.contains("mp4")) {
                                        file = file.replace("file://", "");
                                        Bitmap bm = ThumbnailUtils
                                                .createVideoThumbnail(
                                                        file,
                                                        android.provider.MediaStore.Video.Thumbnails.MINI_KIND);
                                        picture.setImageBitmap(bm);
                                    } else {
                                        file = file.replace("file://", "");
                                        picture.setImageBitmap(Util
                                                .decodeSampledBitmapFromFile(
                                                        file,
                                                        IMAGE_THUMBNAIL_WIDTH,
                                                        IMAGE_THUMBNAIL_HEIGTH)); //
                                    }
                                } else {
                                    picture.setImageResource(R.drawable.ic_itinerary_icon);
                                }
                            } else { // Has multiple highlights
                                // �$$�
                                title.setText("Multiples elements d'd'interès");
                                snippet.setText("Fes clic per veure la llista...");
                            }
                        } else { // Not user marker
                            if (s.hasSingleHighLight()) {
                                List<HighLight> highLights = DataContainer
                                        .getStepHighLights(s,
                                                app.getDataBaseHelper());
                                // (List<HighLight>) s.getHighlights();
                                // HighLight h1 = s.getHighlight();
                                HighLight h1 = highLights.get(0);
                                title.setText(h1.getName());
                                snippet.setText(h1.getLongText());
                                ImageView picture = (ImageView) myContentView
                                        .findViewById(R.id.info_pic);
                                DataContainer.refreshHighlightForFileManifest(h1, app.getDataBaseHelper());
                                if (h1.hasMediaFile()) {
                                    String file = h1.getFileManifest().getPath();
                                    if (file.contains("mp4")) {
                                        file = file.replace("file://", "");
                                        Bitmap bm = ThumbnailUtils
                                                .createVideoThumbnail(
                                                        file,
                                                        android.provider.MediaStore.Video.Thumbnails.MINI_KIND);
                                        picture.setImageBitmap(bm);
                                    } else {
                                        file = file.replace("file://", "");
                                        // loadBitmapThumbnailToImageView(file,
                                        // IMAGE_THUMBNAIL_WIDTH,
                                        // IMAGE_THUMBNAIL_HEIGTH, picture);
                                        picture.setImageBitmap(Util
                                                .decodeSampledBitmapFromFile(
                                                        file,
                                                        IMAGE_THUMBNAIL_WIDTH,
                                                        IMAGE_THUMBNAIL_HEIGTH));
                                    }
                                } else {
                                    picture.setImageResource(R.drawable.ic_itinerary_icon);
                                }
                            } else { // Multiple highlights
                                // �$$�
                                title.setText("Multiples elements d'interès");
                                snippet.setText("Fes clic per veure la llista...");
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
                    Step nearestStepToTap = null;
                    // If route mode == 0, there's no route in progress
                    if (routeMode == 1 || routeMode == 2) {
                        nearestStepToTap = getRouteInProgressNearestStep(point,
                                TAP_TOLERANCE_DIST);
                    }
                    if (nearestStepToTap == null) {
                        selectedMarker = null;
                    }
                    if (routeMode == 1 || routeMode == 2) {
                        if (selectedMarker != null) {
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
                    if (marker.isFlat()) {
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void launchHighLightEditIntent(Step s, HighLight h) {
        Intent i = new Intent(DetailItineraryActivity.this,
                EditHighLightActivity.class);
        stepBeingEditedId = s.getId();
        i.putExtra("lat", Double.toString(s.getLatitude()));
        i.putExtra("long", Double.toString(s.getLongitude()));
        i.putExtra("alt", Double.toString(s.getAltitude()));
        i.putExtra("date", app.formatDateDayMonthYear(s.getAbsoluteTime()));
        if (h != null) {
            i.putExtra("hlid", h.getId());
            i.putExtra("hlname", h.getName());
            i.putExtra("hllongtext", h.getLongText());
            DataContainer.refreshHighlightForFileManifest(h, app.getDataBaseHelper());
            if (h.hasMediaFile()) {
                i.putExtra("hlimagepath", h.getFileManifest().getPath());
            }
        }
        startActivityForResult(i, HIGHLIGHT_EDIT_REQUEST);
    }

    private Marker markerAlreadyOnStep(Step s) {
        if (routeInProgressMarkers == null
                || routeInProgressMarkers.size() == 0) {
            return null;
        }
        // Enumeration<Step> e = routeInProgressMarkers.elements();
        Enumeration<Marker> e = routeInProgressMarkers.keys();
        while (e.hasMoreElements()) {
            Marker m = e.nextElement();
            Step s_n = routeInProgressMarkers.get(m);
            if (s_n.equals(s)) {
                return m;
            }
        }
        return null;
    }

    private void showEditMultipleHighLightDialog(final Step s) {
        List<HighLight> highLights = DataContainer.getStepHighLights(s, app.getDataBaseHelper());
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                DetailItineraryActivity.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle("Tria per editar, o afegeix nou...");
        final HighLightArrayAdapter arrayAdapter = new HighLightArrayAdapter(DetailItineraryActivity.this, highLights);

        builderSingle.setNegativeButton("cancel·lar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setPositiveButton("Afegir nou punt d'interès",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launchHighLightEditIntent(s, null);
                    }
                });

        builderSingle.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HighLight selected = arrayAdapter.getItem(which);
                        launchHighLightEditIntent(s, selected);
                    }
                });
        builderSingle.show();
    }

    private void showAddMarkerOnTapDialog(final Step s) {
        Marker alreadyThereMarker;
        if ((alreadyThereMarker = markerAlreadyOnStep(s)) == null) {
            if (routeInProgressMarkers == null) {
                routeInProgressMarkers = new Hashtable<Marker, Step>();
            }

            LatLng point = new LatLng(s.getLatitude(), s.getLongitude());

            final Marker m = MapObjectsFactory.addEmptyUserMarker(mMap, point,
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
            dialog.setButton(DialogInterface.BUTTON_POSITIVE,
                    getString(R.string.yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface paramDialogInterface,
                                int paramInt) {
                            if (selectedMarker != null) {
                                selectedMarker.remove();
                            }
                            selectedMarker = m;
                            // �$$�
                            launchHighLightEditIntent(s, null);
                        }
                    });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.no),
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
        } else { // There's a marker on the step
            selectedMarker = alreadyThereMarker;
            if (s.hasHighLights()) {
                // Show multiple highlight dialog
                showEditMultipleHighLightDialog(s);
            } else {
                // New highlight
                launchHighLightEditIntent(s, null);
            }
        }
    }

    private Step getRouteInProgressNearestStep(LatLng point, float tolerance) {
        Step retVal = null;
        if (routeInProgress != null) {
            if (routeInProgress.getTrack() != null
                    && routeInProgress.getTrack().getSteps() != null
                    && routeInProgress.getTrack().getSteps().size() > 0) {
                // Check if steps is initialized - strange condition right after
                // stopping track
                //*** BUG HERE: Can't cast lazyforeigncollection
//                ArrayList<Step> steps = (ArrayList<Step>) routeInProgress
//                        .getTrack().getSteps();
                // Instead we will try:

                DataContainer.refreshRouteForTrack(routeInProgress,app.getDataBaseHelper());
                Track t = routeInProgress.getTrack();
                List<Step> steps = DataContainer.getTrackSteps(t, app.getDataBaseHelper());

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

    // private void drawPerpLine(LineSegment perp) {
    // PolylineOptions rectOptions = new PolylineOptions();
    // rectOptions.zIndex(1);
    // rectOptions.color(Color.MAGENTA);
    // Point p1 = perp.getP1();
    // Point p2 = perp.getP2();
    // SphericalMercatorProjection sp = new SphericalMercatorProjection(6371);
    // rectOptions.add(sp.toLatLng(p1));
    // rectOptions.add(sp.toLatLng(p2));
    // perpPolyLine = mMap.addPolyline(rectOptions);
    // }

    private void setUpCamera() {
        // Step s = DataContainer.getRouteStarter(currentRoute, dataBaseHelper);
        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new
        // LatLng(s.getLatitude(),s.getLongitude()) , 15));
        Track t = selectedRoute.getTrack();
        List<Step> steps = DataContainer.getTrackSteps(t,
                app.getDataBaseHelper());
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (int i = 0; i < steps.size(); i++) {
            Step s = steps.get(i);
            b.include(new LatLng(s.getLatitude(), s.getLongitude()));
        }
        LatLngBounds bounds = b.build();
        Util.fitMapViewToBounds(mMap, getBaseContext(), bounds, 20);
        // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200, 200,
        // 20));
    }

    // private void checkLocationIsWithinEffectRadius(LatLng location) {
    // boolean found = false;
    // int i = 0;
    // while (i < highLightedSteps.size() && !found) {
    // Step s = highLightedSteps.get(i);
    // float[] results = new float[3];
    // Location.distanceBetween(location.latitude, location.longitude,
    // s.getLatitude(), s.getLongitude(), results);
    // if (results[0] <= s.getHighlight().getRadius()) {
    // Log.i("HIT", "Hit interest area");
    // if (interestAreaMarker != null) {
    // interestAreaMarker.remove();
    // }
    // interestAreaMarker = mMap
    // .addMarker(new MarkerOptions()
    // .position(
    // new LatLng(s.getLatitude(), s
    // .getLongitude()))
    // .title("Interesting point")
    // .snippet("Interesting point")
    // .icon(BitmapDescriptorFactory
    // .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
    // // MarkerAnimationTest.animateMarker(interestAreaMarker);
    // MarkerAnimationTest.bounceMarker(interestAreaMarker);
    // interestStep = s;
    // found = true;
    // } else {
    // if (interestAreaMarker != null) {
    // interestAreaMarker.remove();
    // interestStep = null;
    // }
    // }
    // i++;
    // }
    // }

    private void addMarkerIfNeeded(Step step) {
        // stepTable
        HighLight hl;
        if (step.hasHighLights()) {
            List<HighLight> highLights = DataContainer.getStepHighLights(step,
                    app.getDataBaseHelper());
            Marker m;
            if (step.hasSingleHighLight()) {
                hl = highLights.get(0);
                // Marker m = MapObjectsFactory.addHighLightMarker(
                m = MapObjectsFactory.addOfficialHighLightMarker(mMap,
                        new LatLng(step.getLatitude(), step.getLongitude()),
                        hl.getName(), hl.getLongText(), hl.getType());
            } else { // Multiple highlights
                // �$$�
                m = MapObjectsFactory.addOfficialHighLightMarker(mMap,
                        new LatLng(step.getLatitude(), step.getLongitude()),
                        "Multiples punts d'interes", "", HighLight.CONTAINER_N);
            }
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

    // private void drawUserRoute() {
    // if (fixReceiver != null) {
    // PolylineOptions rectOptions = new PolylineOptions();
    // List<Step> stepsInProgress = fixReceiver.getStepsInProgress();
    // if (stepsInProgress != null) {
    // for (int i = 0; i < stepsInProgress.size(); i++) {
    // Step step = stepsInProgress.get(i);
    // rectOptions.add(new LatLng(step.getLatitude(), step
    // .getLongitude()));
    // addPrecisionRadius(step);
    // addMarkerIfNeeded(step, BitmapDescriptorFactory.HUE_CYAN);
    // }
    // rectOptions.zIndex(2);
    // rectOptions.color(Color.GREEN);
    // mMap.addPolyline(rectOptions);
    // }
    // }
    // }

    private void clearSelectedRoute() {
        if (selectedRoutePolyLine != null) {
            selectedRoutePolyLine.remove();
            selectedRoutePolyLine = null;
        }
        if (startMarker != null) {
            startMarker.remove();
        }
        if (arrivalMarker != null) {
            arrivalMarker.remove();
        }
        if (selectedRoutePoints != null) {
            for (int i = 0; i < selectedRoutePoints.size(); i++) {
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
        selectedRouteSteps = DataContainer.getTrackSteps(t,
                app.getDataBaseHelper());
        refreshDecorations(selectedRouteSteps);
        for (int j = 0; j < (selectedRouteSteps.size() - 1); j++) {
            Step step = selectedRouteSteps.get(j);
            if (step.getOrder() != -1) {
                rectOptions
                        .add(new LatLng(step.getLatitude(), step.getLongitude()));
                // Enable this maybe on options, obscures map too much
                // addPrecisionRadius(step);
            }
            addMarkerIfNeeded(step);
        }
        rectOptions.zIndex(1);
        rectOptions.color(Color.GRAY);
        selectedRoutePolyLine = mMap.addPolyline(rectOptions);
    }

    private void refreshDecorations(List<Step> steps) {
        if (directionMarkers == null) {
            directionMarkers = new ArrayList<Marker>();
        } else {
            for (int i = 0; i < directionMarkers.size(); i++) {
                directionMarkers.get(i).remove();
            }
            directionMarkers.clear();
        }
        LatLng lastPosition = null;
        if (steps != null) {
            int freq;
            if (steps.size() <= 10) {
                freq = 1;
            } else {
                freq = steps.size() / 5;
            }
            for (int i = 0; i < steps.size(); i++) {
                Step current = steps.get(i);
                LatLng currentPosition = new LatLng(current.getLatitude(),
                        current.getLongitude());
                if (i % freq == 0) {
                    if (currentPosition != null && lastPosition != null
                            && !currentPosition.equals(lastPosition)) {
                        double angle = SphericalUtil.computeHeading(
                                lastPosition, currentPosition);
                        double dist = SphericalUtil.computeDistanceBetween(
                                lastPosition, currentPosition);
                        LatLng markerPosition = SphericalUtil.computeOffset(
                                lastPosition, dist / 2, angle);
                        directionMarkers
                                .add(MapObjectsFactory.addDirectionMarker(mMap,
                                        markerPosition, angle));
                    }
                }
                if (i == 0) {
                    startMarker = MapObjectsFactory.addStartMarker(mMap,
                            currentPosition, getString(R.string.trip_start));
                }
                if (i == (steps.size() - 1)) {
                    arrivalMarker = MapObjectsFactory.addEndMarker(mMap,
                            currentPosition, getString(R.string.trip_end));
                }
                lastPosition = new LatLng(currentPosition.latitude,
                        currentPosition.longitude);
            }
        }
    }

    private MapBoxOfflineTileProvider initTileProvider() {
        String mapPath = selectedRoute.getLocalCarto();
        if (mapPath != null && !mapPath.isEmpty()) {
            File f = new File(mapPath);
            Log.e("CARTO", f.getPath());
            if (f.exists()) {
                Log.e("CARTO EXISTS", f.getPath());
                return new MapBoxOfflineTileProvider(f.getPath());
            } else {
                Log.d(TAG,
                        "Fitxer cartografia no trobat " + f.getAbsolutePath());
            }
        }
        // If not returned by now, try the general map path instead
        mapPath = PropertyHolder.getGeneralMapPath();
        if (mapPath != null && !mapPath.isEmpty()) {
            File f = new File(mapPath);
            Log.e("CARTO", f.getPath());
            if (f.exists()) {
                Log.e("CARTO EXISTS", f.getPath());
                return new MapBoxOfflineTileProvider(f.getPath());
            } else {
                Log.d(TAG,
                        "Fitxer cartografia no trobat " + f.getAbsolutePath());
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

        public FixReceiver(GoogleMap mMap, ProximityWarning proximityWarning,
                           Hashtable<Marker, Step> selectedRouteMarkers) {
            super();
            this.mMap = mMap;
            this.selectedRouteMarkers = selectedRouteMarkers;
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

        public void clearTrackObjects() {
            if (trackInProgress != null) {
                trackInProgress.remove();
                trackInProgress = null;
            }
            if (pointsInProgress != null) {
                for (int i = 0; i < pointsInProgress.size(); i++) {
                    Circle c = pointsInProgress.get(i);
                    c.remove();
                }
                pointsInProgress.clear();
            }
        }

        public void destroy() {
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
            int zIndexPolyLine = stepsInProgress != null ? stepsInProgress
                    .size() + 1 : 1;
            PolylineOptions rectOptions = MapObjectsFactory
                    .getRouteInProgressPolyLine(zIndexPolyLine);
            if (stepsInProgress != null && stepsInProgress.size() > 0) {
                for (int i = 0; i < stepsInProgress.size(); i++) {

                    Step step = stepsInProgress.get(i);

                    boolean last = (i == stepsInProgress.size() - 1);

                    CircleOptions copt = MapObjectsFactory
                            .getRouteInProgressCircle(
                                    new LatLng(step.getLatitude(), step
                                            .getLongitude()), step
                                    .getPrecision(), last, i);
                    pointsInProgress.add(mMap.addCircle(copt));

                    if (routeMode == 1 || routeMode == 2) {
                        rectOptions.add(new LatLng(step.getLatitude(), step
                                .getLongitude()));
                    }
                }
                if (routeMode == 1 || routeMode == 2) {
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
            if (steps != null) {
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

        private void checkNearbyMarkers(LatLng receivedLocation) {
            Enumeration<Marker> eM = selectedRouteMarkers.keys();
            boolean found = false;
            while (eM.hasMoreElements() && !found) {
                Marker m = eM.nextElement();
                Step s = selectedRouteMarkers.get(m);
                if (!warningList.contains(s)) {
                    float[] results = new float[3];
                    Location.distanceBetween(receivedLocation.latitude,
                            receivedLocation.longitude, s.getLatitude(),
                            s.getLongitude(), results);
                    double effectivePopRadius = s.getPrecision();
                    if (effectivePopRadius < Util.MINIMUM_POP_DISTANCE_RADIUS) {
                        effectivePopRadius = Util.MINIMUM_POP_DISTANCE_RADIUS;
                    }
                    if (results[0] <= effectivePopRadius) {
                        Log.d("HIT", "Hit interest area - distance: "
                                + results[0] + " radius: " + effectivePopRadius);
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
            // DateFormat df = new SimpleDateFormat("HH:mm:ss.SSSZ");
            long currentTime = System.currentTimeMillis();
            Date time = new Date(currentTime);
            LatLng location = new LatLng(lat, lng);
            boolean locationExists = locationExists(location, stepsInProgress);
            // Point is same as last, we don't add it to the track

            Log.d("onReceive", "Received new location " + lat + " " + lng
                    + " t " + app.formatDateHoursMinutesSeconds(time)
                    + " already logged");

            if (!locationExists) {
                if (mMap != null && mMap.getCameraPosition() != null) {
                    cu = CameraUpdateFactory.newLatLngZoom(location,
                            mMap.getCameraPosition().zoom);
                } else {
                    cu = CameraUpdateFactory.newLatLngZoom(location, 16);
                }
                if (mMap != null && cu != null) {
                    mMap.moveCamera(cu);
                }
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
                    int order = stepsInProgress.size();
                    s.setOrder(order);
                    DataContainer.refreshRouteForTrack(routeInProgress, app.getDataBaseHelper());
                    Track t = routeInProgress.getTrack();
                    DataContainer.addStepToTrack(s, t,
                            PropertyHolder.getUserId(),
                            app.getDataBaseHelper());
                }
                Log.d("onReceive", "Received new location " + lat + " " + lng
                        + " t " + app.formatDateHoursMinutesSeconds(time));
            }
            if (routeMode == 1) {
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
                connectionResult.startResolutionForResult(this,
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
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
            Toast.makeText(this, getString(R.string.error_gplay_connect),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        Log.d("onConnected", "Google play services connected");
    }

    @Override
    public void onDisconnected() {
        // TODO Auto-generated method stub
        Log.d("onDisconnected", "Google play services disconnected");
    }

}
