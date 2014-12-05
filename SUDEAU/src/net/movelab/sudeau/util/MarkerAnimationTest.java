package net.movelab.sudeau.util;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.view.animation.BounceInterpolator;

import com.google.android.gms.maps.model.Marker;

public class MarkerAnimationTest {
	
	public static void animateMarker(final Marker marker){
		final float initR = 0;
		final Handler handler = new Handler();		
		handler.post(new Runnable(){
			float angle = initR;
			int sign = 1;
			@Override
			public void run() {
				// TODO Auto-generated method stub							
				//if(angle >= 360) angle = 0;
				angle = angle + 1 * sign;
				if(angle >= 20 || angle <= -20){ 
					sign = sign * -1; 
				}
				marker.setRotation(angle);
				handler.postDelayed(this, 1);
			}			
		});
	}
	
	public static void bounceMarker(final Marker marker){
		final ValueAnimator va = ValueAnimator.ofFloat(20, 1);
		va.setDuration(1500);
		va.setInterpolator(new BounceInterpolator());
		va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
		    @Override
		    public void onAnimationUpdate(ValueAnimator animation) {
		        marker.setAnchor(0.5f, (Float) animation.getAnimatedValue());
		    }
		});
		va.start();
	}

}
