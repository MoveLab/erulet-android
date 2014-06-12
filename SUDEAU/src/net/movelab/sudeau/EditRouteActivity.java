package net.movelab.sudeau;

import org.json.JSONException;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditRouteActivity extends Activity {
	
	private Route editedRoute;
	private EditText routeName;
	private EditText routeDescription;
	private DataBaseHelper dataBaseHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.edit_route);
		setUpDBIfNeeded();		
		setEditedRoute();
		initInterface();		
	}
	
	private void setUpDBIfNeeded() {
		if (dataBaseHelper == null) {
			dataBaseHelper = OpenHelperManager.getHelper(this,
					DataBaseHelper.class);
		}
	}
	
	private void save(){
		if(editedRoute.getId()!=null){ //Edit
			editedRoute.setName( routeName.getText().toString() );
			editedRoute.setDescription( routeDescription.getText().toString() );
			DataContainer.editRoute(editedRoute,dataBaseHelper);
			Toast.makeText(getApplicationContext(),"Canvis desats amb èxit...", Toast.LENGTH_LONG).show();
		}else{ //Insert
			
		}
	}
	
	private void cancel(){
		
	}
	
	private void initInterface(){
		Button btn_save = (Button)findViewById(R.id.btn_editRoute_save);
		btn_save.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View arg0) {				
				save();
			}
		});
		Button btn_cancel = (Button)findViewById(R.id.btn_editRoute_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) { 
				cancel();
			}
		});
		routeName = (EditText)findViewById(R.id.et_RouteName);
		routeDescription = (EditText)findViewById(R.id.et_RouteDescription);
		routeName.setText(editedRoute.getName());
		routeDescription.setText(editedRoute.getDescription());
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
