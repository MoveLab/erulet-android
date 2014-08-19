package net.movelab.sudeau;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

public class DetailHighLightActivity extends Activity {
	
	private EruletApp app;
	private ProgressBar progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.detail_highlight_picture);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		Bundle extras = getIntent().getExtras();
		Step s = null;
		if (extras != null) {
			String step_json = extras.getString("step_j");
			try {
				s = JSONConverter.jsonObjectToStep(new JSONObject(step_json));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(s!=null){
			setupUI(s);
		}
	}
	
	private void setupUI(Step s){
		TextView datatxt =  (TextView)findViewById(R.id.tvHlData);
		TextView lattxt =  (TextView)findViewById(R.id.tvLatHl);
		TextView longtxt =  (TextView)findViewById(R.id.tvLongHl);
		TextView alttxt =  (TextView)findViewById(R.id.tvHlAlt);
		TextView nameTxt =  (TextView)findViewById(R.id.tvHlNameLabel);
		TextView descriptionTxt =  (TextView)findViewById(R.id.tvHlDescription);
		progressBar = (ProgressBar)findViewById(R.id.pbImageLoad);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);
		
		ImageView ivType = (ImageView)findViewById(R.id.highLightTypeIv);
		if(s.getHighlight()!=null){
			switch(s.getHighlight().getType()){
				case HighLight.ALERT:
					ivType.setImageResource(R.drawable.pin_warning);
					break;
				case HighLight.POINT_OF_INTEREST:
					ivType.setImageResource(R.drawable.pin_drop);
					break;
				case HighLight.WAYPOINT:
					ivType.setImageResource(R.drawable.pin_chart);
					break;
			}
		}
		
		ImageView ivPicture = (ImageView)findViewById(R.id.highLightPicture);
		VideoView ivVideo = (VideoView)findViewById(R.id.highLightVideo);
		if(s.getHighlight()!=null && s.getHighlight().getMediaPath()!=null && !s.getHighlight().getMediaPath().trim().equalsIgnoreCase("")){
			String pathName = s.getHighlight().getMediaPath().replace("file://", "");
			if(pathName.contains("mp4")){
				progressBar.setVisibility(View.GONE);
				ivPicture.setVisibility(View.GONE);
				ivVideo.setVideoURI(Uri.parse(pathName));
				ivVideo.setMediaController(new MediaController(this));
				ivVideo.setOnPreparedListener(new OnPreparedListener() {					
					@Override
					public void onPrepared(MediaPlayer mp) {
						mp.setLooping(true);
					}
				});
				ivVideo.requestFocus();
				ivVideo.start();
			}else{
				ivVideo.setVisibility(View.GONE);
				loadBitmapThumbnailToImageView(pathName, 384, 512, ivPicture,progressBar);
			}
//			Display display = getWindowManager().getDefaultDisplay();
//			Point size = new Point();
//			display.getSize(size);
//			int width = size.x;
//			int height = size.y;
			//ivPicture.setImageBitmap(Util.decodeSampledBitmapFromFile(pathName, 384, 512));			
		}
		
		String date = app.formatDateDayMonthYear(s.getAbsoluteTime()) + " " + app.formatDateHoursMinutesSeconds(new Date(s.getAbsoluteTimeMillis()));
		String lat = Double.toString(s.getLatitude());
		String llong = Double.toString(s.getLongitude());
		String alt = Double.toString(s.getAltitude());
		
		String name = getString(R.string.point_no_name);
		if(s.getHighlight()!=null && s.getHighlight().getName()!=null && !s.getHighlight().getName().trim().equalsIgnoreCase("")){
			name = s.getHighlight().getName();
		}
		String description = getString(R.string.point_no_description);		
		
		nameTxt.setText(getString(R.string.point_name) + " " + name);
		descriptionTxt.setText(getString(R.string.description) + " " + description);
		datatxt.setText(getString(R.string.date) + " " + date);
		lattxt.setText(getString(R.string.latitude) + " " +  lat);
		longtxt.setText(getString(R.string.longitude) + " " +  llong);
		alttxt.setText(getString(R.string.altitude) + " " +  alt);
	}
	
	private void loadBitmapThumbnailToImageView(
			String path, 
			int width, 
			int height, 
			ImageView imageView, ProgressBar progressBar){
		BitmapWorkerTask task = new BitmapWorkerTask(imageView,progressBar);
		task.execute(path, Integer.toString(width),Integer.toString(height));
		
	}

}
