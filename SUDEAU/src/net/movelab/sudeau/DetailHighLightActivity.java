package net.movelab.sudeau;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory.Options;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

public class DetailHighLightActivity extends Activity {
	
	private EruletApp app;
	private ProgressBar progressBar;	
	private RatingBar myRating;
	private Step step;
	private int screenWidth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.detail_highlight_picture);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		screenWidth = Util.getScreenSize(getBaseContext())[0];
		String highLight_id = null;
		Bundle extras = getIntent().getExtras();		
		if (extras != null) {
			String step_json = extras.getString("step_j");
			highLight_id = extras.getString("highlight_id");
            String route_id = extras.getString("route_id");
            try {
				step = JSONConverter.jsonObjectToStep(new JSONObject(step_json), route_id);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(step!=null){
			setupUI(step,highLight_id);
		}
	}		
	
	private void setupUI(Step s, String idHighLight){
		TextView datatxt =  (TextView)findViewById(R.id.tvHlData);
		TextView lattxt =  (TextView)findViewById(R.id.tvLatHl);
		TextView longtxt =  (TextView)findViewById(R.id.tvLongHl);
		TextView alttxt =  (TextView)findViewById(R.id.tvHlAlt);
		TextView nameTxt =  (TextView)findViewById(R.id.tvHlNameLabel);
		View picSeparator = findViewById(R.id.picture_separator);
		TextView descriptionTxt =  (TextView)findViewById(R.id.tvHlDescription);
		RatingBar globalRating = (RatingBar)findViewById(R.id.ratBarGlobal);
		globalRating.setStepSize(1.0f);
		
		myRating = (RatingBar)findViewById(R.id.ratBarUser);
		myRating.setStepSize(1.0f);
		
		final HighLight hl = getHighLightFromStep(s, idHighLight);
		
		if(hl != null){
			float userRating = app.getPrefs().getInt(hl.getId(), 0);
			myRating.setRating(userRating);
		}
		
		myRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				if(hl!=null){
					SharedPreferences.Editor mPrefEditor = app.getPrefsEditor(); 
					mPrefEditor.putInt(hl.getId(), (int)rating);
	            	mPrefEditor.commit();         
				}
			}
		});
		
		progressBar = (ProgressBar)findViewById(R.id.pbImageLoad);
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);
		
		ImageView ivType = (ImageView)findViewById(R.id.highLightTypeIv);
		if(hl!=null){
			switch(hl.getType()){
				case HighLight.ALERT:
					ivType.setImageResource(R.drawable.pin_warning);
					break;
				case HighLight.POINT_OF_INTEREST:
					ivType.setImageResource(R.drawable.pin_drop);
					break;
				case HighLight.POINT_OF_INTEREST_OFFICIAL:
					ivType.setImageResource(R.drawable.pin_drop);
					break;
				case HighLight.WAYPOINT:
					ivType.setImageResource(R.drawable.pin_chart);
					break;
			}
		}
		
		ImageView ivPicture = (ImageView)findViewById(R.id.highLightPicture);
		VideoView ivVideo = (VideoView)findViewById(R.id.highLightVideo);
		if(hl!=null && hl.getMediaPath()!=null && !hl.getMediaPath().trim().equalsIgnoreCase("")){
			String pathName = hl.getMediaPath().replace("file://", "");
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
				Options options = Util.getImageOptions(pathName);
				float adjustedW = (float)screenWidth*0.75f;
				int bitmapHeight = Util.getScaledImageHeight(options.outWidth, options.outHeight, adjustedW);
				loadBitmapThumbnailToImageView(pathName, (int)adjustedW, bitmapHeight, ivPicture,progressBar);
			}			
		}else{
			progressBar.setVisibility(View.GONE);
			ivVideo.setVisibility(View.GONE);
			ivPicture.setVisibility(View.GONE);
			picSeparator.setVisibility(View.GONE);
		}
		
		String date = "";
		if(s.getAbsoluteTime() != null){
			date = app.formatDateDayMonthYear(s.getAbsoluteTime()) + " " + app.formatDateHoursMinutesSeconds(new Date(s.getAbsoluteTimeMillis()));
		}
		String lat = Double.toString(s.getLatitude());
		String llong = Double.toString(s.getLongitude());
		String alt = Double.toString(s.getAltitude());
		
		String name = getString(R.string.point_no_name);
		if(hl!=null && hl.getName()!=null){
			if(!hl.getName().trim().equalsIgnoreCase("")){
				name = hl.getName();
			}
			globalRating.setRating( hl.getGlobalRating() );
		}
		String description = getString(R.string.point_no_description);
        if(hl!=null && hl.getLongText()!=null){
            if(!hl.getLongText().trim().equalsIgnoreCase("")){
                description = hl.getLongText();
            }
        }

		nameTxt.setText(getString(R.string.point_name) + " " + name);
		descriptionTxt.setText(getString(R.string.description) + " " + description);
		datatxt.setText(getString(R.string.date) + " " + date);
		lattxt.setText(getString(R.string.latitude) + " " +  lat);
		longtxt.setText(getString(R.string.longitude) + " " +  llong);
		alttxt.setText(getString(R.string.altitude) + " " +  alt);

        LinearLayout iibuttonarea = (LinearLayout) findViewById(R.id.iibuttonarea);
        LinearLayout refbuttonarea = (LinearLayout) findViewById(R.id.refbuttonarea);


        if (hl.getInteractiveImages() != null) {
            iibuttonarea.setVisibility(View.VISIBLE);
            ArrayList<InteractiveImage> these_iis = new ArrayList<InteractiveImage>(hl.getInteractiveImages());
            for(InteractiveImage ii : these_iis){
                Button iiButton = new Button(this);
                iiButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                iiButton.setPadding(5, 5, 5, 5);
                iiButton.setText("Interactive Image");
                iiButton.setGravity(Gravity.CENTER);
                iiButton.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);

                final String this_ii_id = ii.getId();

                iibuttonarea.addView(iiButton);
                iiButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(
                                DetailHighLightActivity.this,
                                InteractiveImageActivityHeatMap.class);
                        i.putExtra("int_image_id", this_ii_id);
                        startActivity(i);
                    }
               });

            }
        }
            // Interactive image
        if (hl.getReferences() != null) {
            refbuttonarea.setVisibility(View.VISIBLE);
            ArrayList<Reference> refs = new ArrayList<Reference>(hl.getReferences());
            for(Reference ref : hl.getReferences()){
                Button refButton = new Button(this);
                refButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                refButton.setPadding(5, 5, 5, 5);
                refButton.setText("Reference: " + ref.getName());
                refButton.setGravity(Gravity.CENTER);
                refButton.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                final String this_ref_id = ref.getId();

                refbuttonarea.addView(refButton);
                refButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
           Intent i = new Intent(DetailHighLightActivity.this, HTMLViewerActivity.class);
            i.putExtra("idReference", this_ref_id);
            startActivity(i);
            }
                });

            }
	}}
	
	private void loadBitmapThumbnailToImageView(
			String path, 
			int width, 
			int height, 
			ImageView imageView, ProgressBar progressBar){
		BitmapWorkerTask task = new BitmapWorkerTask(imageView,progressBar);
		task.execute(path, Integer.toString(width),Integer.toString(height));
		
	}
	
	private HighLight getHighLightFromStep(Step s, String idHighLight){
		if(s.hasHighLights()){
			List<HighLight> highLights = DataContainer.getStepHighLights(s, app.getDataBaseHelper()); 
			for( HighLight h : highLights ){
				if(h.getId().equalsIgnoreCase(idHighLight)){
					return h;
				}
			}
		}
		return null;
	}

}
