package net.movelab.sudeau;

import java.text.DecimalFormat;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.DetailItineraryActivity.FixReceiver;
import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
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
	private DataBaseHelper dataBaseHelper;
	private TextView tvHeading;
	private TextView tvLocation;
	private TextView tvNav;
	private TextView tvDist;
	private DecimalFormat df;
    
	private void setUpDBIfNeeded() {
		if (dataBaseHelper == null) {
			dataBaseHelper = OpenHelperManager.getHelper(this,
					DataBaseHelper.class);
		}
	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compass);
        df = new DecimalFormat("#.00"); 
        setUpDBIfNeeded();
        startTracking();
        setNavPoint();

        // 
        image = (ImageView) findViewById(R.id.imageViewCompass);
        navArrow = (ImageView) findViewById(R.id.imageViewNav);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);        
        tvLocation = (TextView) findViewById(R.id.tvCurrentLoc);
        tvNav = (TextView) findViewById(R.id.tvNavLoc);
        tvDist = (TextView) findViewById(R.id.tvDist);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
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
    
    public void setNavPoint(){
    	Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String idStep = extras.getString("idStep");
			Step s = DataContainer.findStepById(idStep, dataBaseHelper);
			navLocation = new Location("");//provider name is unecessary
			navLocation.setLatitude(s.getLatitude());//your coords of course
			navLocation.setLongitude(s.getLongitude());
		}
    }
    
    public void startTracking(){
    	Intent intent = new Intent(getString(R.string.internal_message_id)
				+ Util.MESSAGE_SCHEDULE);
		sendBroadcast(intent);

		fixFilter = new IntentFilter(getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_FIX_RECORDED);

		fixReceiver = new CompassFixReceiver();
		registerReceiver(fixReceiver, fixFilter);
    }
    
    @Override
    protected void onDestroy() {    
    	super.onDestroy();
    	mSensorManager.unregisterListener(this);
    	Intent intent = new Intent(getString(R.string.internal_message_id)
				+ Util.MESSAGE_UNSCHEDULE);
		sendBroadcast(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
        Intent intent = new Intent(getString(R.string.internal_message_id)
				+ Util.MESSAGE_UNSCHEDULE);
		sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    	    	
        // get the angle around the z-axis rotated (North bearing)
    	float sensorDeltaDegree = Math.round(event.values[0]);    	
    	if( currentLocation!=null && navLocation!=null ){    		
    		currentBearingDegree = currentLocation.bearingTo(navLocation) - sensorDeltaDegree;
    		tvDist.setText("Distància: " + currentLocation.distanceTo(navLocation));
    		tvLocation.setText("Ubicació actual: " + df.format(currentLocation.getLatitude()) + "-" + df.format(currentLocation.getLongitude()));
    		tvNav.setText("Ubicació destí: " + df.format(navLocation.getLatitude()) + "-" + df.format(navLocation.getLongitude()));
    	}                        

        tvHeading.setText("Orientació: " + Float.toString(sensorDeltaDegree) + " graus");
        
        RotateAnimation raB = new RotateAnimation(
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
        raB.setDuration(50);
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
