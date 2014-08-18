package net.movelab.sudeau;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

public class DetailHighLightActivity extends Activity {
	
	private EruletApp app;
	
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
		if(s.getHighlight()!=null && s.getHighlight().getMediaPath()!=null && !s.getHighlight().getMediaPath().trim().equalsIgnoreCase("")){
			String pathName = s.getHighlight().getMediaPath().replace("file://", "");
//			Display display = getWindowManager().getDefaultDisplay();
//			Point size = new Point();
//			display.getSize(size);
//			int width = size.x;
//			int height = size.y;
			ivPicture.setImageBitmap(Util.decodeSampledBitmapFromFile(pathName, 384, 512));
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

}
