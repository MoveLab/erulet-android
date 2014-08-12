package net.movelab.sudeau;

import org.json.JSONException;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.RouteInfoFormatter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditRouteActivity extends Activity {
	
	private Route editedRoute;
	private EditText routeName;
	private EditText routeDescription;		
	private EruletApp app;
	private boolean changed;
	
	//TODO Improve default name, allow for multiple non-colliding defaults
	//TODO Create input validation

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		changed = false;
		setContentView(R.layout.edit_route);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		setEditedRoute();
		initInterface();		
	}
	
	@Override
	public void onBackPressed() {		
		if(changed){
			new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(getString(R.string.finish_edit_trip))
			.setMessage(
					getString(R.string.finish_edit_trip_leave_long))
			.setPositiveButton(getString(R.string.yes),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							save(DataContainer.getAndroidId(getContentResolver()));
							finish();
						}
					}).setNegativeButton(getString(R.string.no), 
						new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {							
							finish();
						}
					}).show();
		}else{
			finish();
		}
	}		
	
	private void save(String android_id){
		editedRoute.setName( routeName.getText().toString() );
		editedRoute.setDescription( routeDescription.getText().toString() );				
		DataContainer.editRoute(editedRoute,app.getDataBaseHelper());
		Toast.makeText(getApplicationContext(),getString(R.string.save_succesful), Toast.LENGTH_LONG).show();		
	}		
	
	private void initInterface(){
//		Button btn_save = (Button)findViewById(R.id.btn_editRoute_save);
//		btn_save.setOnClickListener(new OnClickListener() {			
//			@Override
//			public void onClick(View arg0) {				
//				save(DataContainer.getAndroidId(getContentResolver()));
//			}
//		});		
		
		routeName = (EditText)findViewById(R.id.et_RouteName);
		routeDescription = (EditText)findViewById(R.id.et_RouteDescription);
		routeName.setText(editedRoute.getName());
		routeDescription.setText(editedRoute.getDescription());
		
		routeName.addTextChangedListener(new TextWatcher() {			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub			
				changed = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub				
			}					
		});
		
		routeDescription.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				changed = true;
			}
		});
		
		RouteInfoFormatter rif = new RouteInfoFormatter(editedRoute);
		
		TextView tvTotalDist = (TextView)findViewById(R.id.tvTotalDist);
		TextView tvTotalTime = (TextView)findViewById(R.id.tvTotalTime);
		TextView tvPointsNumber = (TextView)findViewById(R.id.tvPointsNumber);
		TextView tvHighLightsNumber = (TextView)findViewById(R.id.tvHighLightsNumber);
		TextView tvRamp = (TextView)findViewById(R.id.tvRamp);
		
		tvTotalDist.setText(rif.getFormattedTotalDistance());
		tvTotalTime.setText(rif.getFormattedTotalTime());
		tvPointsNumber.setText(rif.getFormattedNumberPointsInTrack());
		tvHighLightsNumber.setText(rif.getFormattedNumberHighlights());
		tvRamp.setText(rif.getFormattedRamp());
	}
	
	private void setEditedRoute(){
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String routeJson = extras.getString("routeJson");
			try {
				editedRoute = JSONConverter.jsonToRoute(routeJson);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
}
