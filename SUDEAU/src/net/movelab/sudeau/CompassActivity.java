package net.movelab.sudeau;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.Step;

import java.text.DecimalFormat;
import java.util.List;

public class CompassActivity extends Activity implements SensorEventListener {
	
	// define the display assembly compass picture
    private ImageView image;
    private ImageView navArrow;
    // record the compass picture angle turned
    private float currentCompassDegree = 0f;
    private float currentBearingDegree = 0f;
    // device sensor manager
    private SensorManager mSensorManager;    
    private Location currentLocation;
    private Location navLocation;
    private IntentFilter fixFilter;
	private CompassFixReceiver fixReceiver;	
	private TextView tvWpName;	
	private TextView tvBearing;
	private TextView tvLocation;
	private TextView tvNav;
	private TextView tvDist;
	private DecimalFormat df;
	private EruletApp app;
    String currentLocale;

    // master control to disable or enable red arrow navidatio -- currently off because not yet fully working.
    private boolean allowNavigation = false;

    private Display display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);        

        // 
        image = (ImageView) findViewById(R.id.imageViewCompass);
        navArrow = (ImageView) findViewById(R.id.imageViewNav);
        navArrow.setVisibility(View.GONE);

        // TextView that will tell the user what degree is he heading
        tvBearing = (TextView) findViewById(R.id.tvBearing);        
        tvLocation = (TextView) findViewById(R.id.tvCurrentLoc);
        tvNav = (TextView) findViewById(R.id.tvNavLoc);
        tvDist = (TextView) findViewById(R.id.tvDist);
        tvWpName = (TextView) findViewById(R.id.tvCompassName);        

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        df = new DecimalFormat("0.00"); 
        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        Context context = getApplicationContext();
        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);
        currentLocale = PropertyHolder.getLocale();

        display = ((WindowManager)getApplicationContext().getSystemService(android.content.Context.WINDOW_SERVICE)).getDefaultDisplay();

        if(allowNavigation){
        setNavPoint();
        checkLocationServicesStatus();
        startTrackingMaybe();
        }



        adjustUI();
    }
    
    private void adjustUI(){
    	if(!allowNavigation || !userRequestedNavigation()){
    		TextView tvCompassActivityLabel = (TextView) findViewById(R.id.tvHighLightDetailLabel);
    		tvCompassActivityLabel.setText(getString(R.string.compass));
    		tvLocation.setVisibility(View.GONE);
            tvNav.setVisibility(View.GONE);
            tvDist.setVisibility(View.GONE);
            tvWpName.setVisibility(View.GONE);
    	}
    }
    
    public class CompassFixReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
            if(intent.getExtras() != null){
			double lat = intent.getExtras().getDouble("lat", 0);
			double lng = intent.getExtras().getDouble("long", 0);
						currentLocation = new Location("");//provider name is unecessary
			currentLocation.setLatitude(lat);//your coords of course
			currentLocation.setLongitude(lng);
            }
		}
    	
    }
    
    private void checkLocationServicesStatus() {
    	if(userRequestedNavigation()){
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
								// TODO Auto-generated method stub
								Intent myIntent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivity(myIntent);
								// get gps
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
	}
    
    
    public void setNavPoint(){
    	Bundle extras = getIntent().getExtras();
		if (extras != null) {
			int idStep = extras.getInt("idStep");
			if(idStep != -1){
				Step s = DataContainer.findStepById(idStep, app.getDataBaseHelper());
                if(s != null){
				navLocation = new Location("");
				navLocation.setLatitude(s.getLatitude());
				navLocation.setLongitude(s.getLongitude());
				if(s.hasHighLights()){
					List<HighLight> highLights = DataContainer.getStepHighLights(s, app.getDataBaseHelper());					
					if(s.hasSingleHighLight()){ //Single highlight
						HighLight hl = highLights.get(0);
						if(hl.getName(currentLocale) == null || hl.getName(currentLocale).equalsIgnoreCase("")){
							tvWpName.setText(getString(R.string.name_null));					
						}else{
							tvWpName.setText(getString(R.string.name) + " " + hl.getName(currentLocale));
						}
					}else{ //Multiple highlights
						tvWpName.setText(getString(R.string.name) + " " + Util.getMultipleHighLightsNameLabel(highLights, currentLocale) );
					}							
				}else{
					tvWpName.setText(getString(R.string.point_no_name));					
				}
			}}
		}
    }
    
    private boolean lastActivityWasTracking(){
    	Bundle extras = getIntent().getExtras();
    	if(extras!=null){
    		return extras.getBoolean("wasTracking");
    	}
    	return false;
    }
    
    private boolean userRequestedNavigation(){
    	if(navLocation==null){
    		return false;
    	}else{
    		return true;
    	}
    }
    
    public void startTrackingMaybe(){
    	if(lastActivityWasTracking()){
    		if(userRequestedNavigation()){  		//Last activity was tracking and user requests nav
    			// No need to start tracking service
    			// Just register listeners
    			registerLocationListeners();
    		}else{ 									//Last activity was tracking but no nav requested
    			// No need to start tracking service
    			// or
    			// Register any listener
    			navArrow.setVisibility(View.GONE);
    		}
    	}else{
			if(userRequestedNavigation()){ 			//Last activity was NOT tracking and user requests nav
				// We need to start the tracking service (was down)
    			app.startTrackingService();
    			// Also register listeners
    			// unRegisterLocationListeners();
    			registerLocationListeners();
    		}else{ 									//Last activity was NOT tracking and user DOES NOT request nav
    			//No need to start location service
    			// No need to start tracking service
    			// or
    			// Register any listener
    			navArrow.setVisibility(View.GONE);
    		}
    	}    	    
    }
    
    private void stopTrackingMaybe(){
    	if(lastActivityWasTracking()){
    		if(userRequestedNavigation()){  		//Last activity was tracking and user requests nav
    			// No need to stop tracking service
    			// Just unregister listeners    			
				unRegisterLocationListeners();    			
    		}else{ 									//Last activity was tracking but no nav requested
    			// No need to start tracking service
    			// or
    			// Register any listener    			
    		}
    	}else{
			if(userRequestedNavigation()){ 			//Last activity was NOT tracking and user requests nav    		
    			// Unregister listeners				
				unRegisterLocationListeners();				
				// We stop the tracking service (we leave it down as it was before)    			
    			app.stopTrackingService();
    		}else{ 									//Last activity was NOT tracking and user DOES NOT request nav
    			//No need to stop location service
    			// or
    			// Unregister any listener
    		}
    	}
    }
    
    private void registerLocationListeners(){    
    	if(fixFilter == null){
	    	fixFilter = new IntentFilter(getResources().getString(
					R.string.internal_message_id)
					+ Util.MESSAGE_FIX_RECORDED);
    	}
    	if(fixReceiver == null){
    		fixReceiver = new CompassFixReceiver();
    	}
		registerReceiver(fixReceiver, fixFilter);
    }
    
    private void unRegisterLocationListeners(){
    	try{
    		unregisterReceiver(fixReceiver);
    		fixReceiver = null;
    	}catch(RuntimeException rex){
    		
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();    	
    	mSensorManager.unregisterListener(this);
    	//stopTrackingMaybe();
    }        

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);

        // check if device has been reoriented
        if(display.getRotation()!= Surface.ROTATION_0){
//TODO figure this out
//mSensorManager.remapCoordinateSystem();
        }

        if(allowNavigation)
            startTrackingMaybe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
        stopTrackingMaybe();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated (North bearing)
    	float sensorDeltaDegree = Math.round(event.values[0]);    	
    	if( currentLocation!=null && navLocation!=null ){    		
    		currentBearingDegree = currentLocation.bearingTo(navLocation) - sensorDeltaDegree;
    		tvDist.setText(getString(R.string.distance) + currentLocation.distanceTo(navLocation) + " " + getResources().getString(R.string.meters));
    		tvLocation.setText(getString(R.string.current_location) + df.format(currentLocation.getLatitude()) + " - " + df.format(currentLocation.getLongitude()));
    		tvNav.setText(getString(R.string.destination) + df.format(navLocation.getLatitude()) + " - " + df.format(navLocation.getLongitude()));
    	}                        

        tvBearing.setText(getString(R.string.bearing) + Float.toString(sensorDeltaDegree) + " " + getString(R.string.degrees));
        
        RotateAnimation raB = null;        
    	raB = new RotateAnimation(
        		currentBearingDegree, 
                -sensorDeltaDegree,
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF,
                0.5f);        

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentCompassDegree, 
                -sensorDeltaDegree,
                Animation.RELATIVE_TO_SELF, 0.5f, 
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        raB.setDuration(500);
        ra.setDuration(100);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);
        raB.setFillAfter(true);

        // Start the animation
        if( currentLocation!=null && navLocation!=null ){
        navArrow.setVisibility(View.VISIBLE);
        navArrow.startAnimation(raB);}
        image.startAnimation(ra);
        currentCompassDegree = -sensorDeltaDegree;        
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

}
