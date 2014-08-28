package net.movelab.sudeau;

import java.text.DecimalFormat;

import com.google.android.gms.common.data.DataBufferUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.DetailItineraryActivity.FixReceiver;
import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Step;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

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
    
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);        

        // 
        image = (ImageView) findViewById(R.id.imageViewCompass);
        navArrow = (ImageView) findViewById(R.id.imageViewNav);

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
        setNavPoint();
        checkLocationServicesStatus();        
        adjustUI();
        startTrackingMaybe();
    }
    
    private void adjustUI(){
    	if(!userRequestedNavigation()){    		    	
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
			double lat = intent.getExtras().getDouble("lat", 0);
			double lng = intent.getExtras().getDouble("long", 0);
			
			currentLocation = new Location("");//provider name is unecessary
			currentLocation.setLatitude(lat);//your coords of course
			currentLocation.setLongitude(lng);
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
			String idStep = extras.getString("idStep");
			if(idStep != null && !idStep.equalsIgnoreCase("")){
				Step s = DataContainer.findStepById(idStep, app.getDataBaseHelper());
				navLocation = new Location("");//provider name is unecessary
				navLocation.setLatitude(s.getLatitude());//your coords of course
				navLocation.setLongitude(s.getLongitude());
				if(s.getHighlight()!=null){
					DataContainer.getHighLightStep(s, app.getDataBaseHelper());
					if(s.getHighlight().getName() == null || 
							s.getHighlight().getName().equalsIgnoreCase("")){
						tvWpName.setText(getString(R.string.name_null));					
					}else{
						tvWpName.setText(getString(R.string.name) + " " + s.getHighlight().getName());
					}								
				}else{
					tvWpName.setText(getString(R.string.point_no_name));					
				}
			}
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
        // If user requested navigation, we resume listening for position updates
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
    		tvDist.setText(getString(R.string.distance) + currentLocation.distanceTo(navLocation) + " metres");
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
        navArrow.startAnimation(raB);
        image.startAnimation(ra);
        currentCompassDegree = -sensorDeltaDegree;        
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

}
