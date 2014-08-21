package net.movelab.sudeau;

import com.google.android.gms.maps.model.Marker;

import android.app.KeyguardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * Wakes up phone if is sleeping, and issues some kind of warning signal (beep and/or vibration)
 * 
 * @author a.escobar
 *
 */
public class ProximityWarning {
	
	private EruletApp app;
	private Vibrator vibrator;
//	private WakeLock wl;
	CountDownTimer countDown;
//	private Marker signaledMarker;
	private MediaPlayer mediaPlayer;
	//10 seconds before automatic lock release
	private static long LOCK_RELEASE_INTERVAL = 10000;
	private static long VIBRATION_TIME = 1750;
	private static String TAG = "ProximityWarning";
	
	public ProximityWarning(EruletApp app){
		this.app = app;		
	}
	
//	private boolean isScreenLocked(){
//		KeyguardManager myKM = (KeyguardManager) app.getSystemService(Context.KEYGUARD_SERVICE);
//		if( myKM.inKeyguardRestrictedInputMode()) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//	
//	private boolean isScreenOn(){
//		PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);
//		return pm.isScreenOn();
//	}
	
	private void startAlert(){
		// This works perfectly, but field test proved me that it's not a good idea
//		PowerManager pm = (PowerManager) app.getSystemService(Context.POWER_SERVICE);		
//		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "WAKE_UP_CALL");
//		wl.acquire();
//		if (Util.DEBUG) {
//			Log.d(TAG, "WakeLock acquired!");
//		}
		if(countDown!=null){
			countDown.cancel();
		}else{			
			countDown = new CountDownTimer(LOCK_RELEASE_INTERVAL,1000) {
				@Override
				public void onTick(long millisUntilFinished) {
					// TODO Auto-generated method stub
					//Do nothing on tick
//					if(Util.DEBUG){
//						Log.d(TAG, millisUntilFinished + " remaining");
//					}
				}				
				@Override
				public void onFinish() {
					// TODO Auto-generated method stub
					acknowledgeWarning();
				}
			};
			countDown.start();
		}
	}
	
	private void playWarningTone(){
		if(mediaPlayer == null){
			mediaPlayer = MediaPlayer.create(app.getBaseContext(), R.raw.tone1);
		}
		mediaPlayer.start();		
	}
	
	private void vibrate(){
		if(vibrator == null){
			vibrator = (Vibrator) app.getBaseContext().getSystemService(
					Context.VIBRATOR_SERVICE);
		}
		vibrator.vibrate(VIBRATION_TIME);
	}
	
	/**
	 * Release resources
	 */
	public void acknowledgeWarning(){		
//		if(wl!=null && wl.isHeld()){
//			wl.release();
//			if (Util.DEBUG) {
//				Log.d("ProximityWarning", "WakeLock released!");
//			}
//		}
		if(mediaPlayer!=null){
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if(vibrator!=null){
			vibrator = null;
		}
//		if(signaledMarker!=null){
//			signaledMarker.hideInfoWindow();
//		}
//		signaledMarker = null;
	}
	
	public void issueWarning(Marker m){
//		signaledMarker = m;		
		startAlert();				
		playWarningTone();
		vibrate();
	}
	
	
//	public boolean markerIsBeingWarned(Marker m){
//		if(signaledMarker != null && signaledMarker.equals(m))
//			return true;
//		return false;
//	}
//
//	public Marker getWarnedMarker() {
//		return signaledMarker;
//	}
//
//	public void setWarnedMarker(Marker warnedMarker) {
//		this.signaledMarker = warnedMarker;
//	}

}
