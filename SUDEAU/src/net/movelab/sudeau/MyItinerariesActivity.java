package net.movelab.sudeau;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.view.View.OnClickListener;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MyItinerariesActivity extends Activity {
	
	private DataBaseHelper dataBaseHelper;		
	private MyRouteArrayAdapter routeArrayAdapter;
	private ListView listView;
	
	private void setUpDBIfNeeded() {
		if (dataBaseHelper == null) {
			dataBaseHelper = OpenHelperManager.getHelper(this,
					DataBaseHelper.class);
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_itineraries);
		
		setUpDBIfNeeded();

		listView = (ListView) findViewById(R.id.lv_my_routes);
		String android_id = DataContainer.getAndroidId(getBaseContext().getContentResolver());
		List<Route> myRoutes = loadRoutes(android_id);
		
		routeArrayAdapter = new MyRouteArrayAdapter(this, myRoutes,dataBaseHelper);
		listView.setAdapter(routeArrayAdapter);

	}
	
	private List<Route> loadRoutes(String android_id){
		List<Route> myRoutes = DataContainer.getUserRoutes(dataBaseHelper,android_id);
		for(Route r : myRoutes){
			DataContainer.refreshRoute(r, dataBaseHelper);
			if(r.getTrack()!=null)
				DataContainer.getTrackSteps(r.getTrack(), dataBaseHelper);
		}
		return myRoutes;
	}
	
	private void refreshListView(String android_id){
		List<Route> newRoutes = loadRoutes(android_id);
		routeArrayAdapter = new MyRouteArrayAdapter(this, newRoutes,dataBaseHelper);
		listView.setAdapter(routeArrayAdapter);
	}
	
	@Override
	protected void onResume() { 
		super.onResume();
		String android_id = DataContainer.getAndroidId(getBaseContext().getContentResolver());
		refreshListView(android_id);
	}

}

class MyRouteArrayAdapter extends ArrayAdapter<Route> {

	HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
	private final Context context;
	private final DataBaseHelper dataBaseHelper;
	private List<Route> routes;
	private Route currentRoute;

	public MyRouteArrayAdapter(Context context, List<Route> objects, 
			DataBaseHelper dataBaseHelper) {
		super(context, R.layout.local_route_list_item, objects);		
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
		this.routes=objects;
		this.context=context;
		this.dataBaseHelper=dataBaseHelper;
	}		
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.local_route_list_item, parent, false);		    
		    TextView nom = (TextView) rowView.findViewById(R.id.route_name);
		    TextView description = (TextView) rowView.findViewById(R.id.route_description);
		    ImageButton edit = (ImageButton) rowView.findViewById(R.id.route_edit);		    
		    ImageButton delete = (ImageButton) rowView.findViewById(R.id.route_delete);
		    currentRoute = routes.get(position);
		    nom.setText(currentRoute.getName());
		    description.setText(currentRoute.getDescription());
		    edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i = new Intent(context,EditRouteActivity.class);
					String routeJson;
					try {
						routeJson = JSONConverter.routeToJSONObject(currentRoute).toString();
						i.putExtra("routeJson", routeJson );
						context.startActivity(i);						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		    delete.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {					
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					 
			        // Setting Dialog Title
			        alertDialog.setTitle("Confirmar esborrat...");
			 
			        // Setting Dialog Message
			        alertDialog.setMessage("S'esborrarà la ruta! Segur que vols continuar?");
			 
			        // Setting Icon to Dialog
			        alertDialog.setIcon(R.drawable.ic_delete);
			 
			        // Setting Positive "Yes" Button
			        alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog,int which) {
			            	remove(currentRoute);			            	
			            }
			        });
			 
			        // Setting Negative "NO" Button
			        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	// 	Write your code here to invoke NO event			            	
			            	dialog.cancel();
			            }
			        });
			 
			        // Showing Alert Message
			        alertDialog.show();					
				}
			});		    

		    return rowView;
	}
	
	@Override
	public void remove(Route object) { 
		super.remove(object);
		DataContainer.deleteRouteCascade(object, dataBaseHelper);
	}
	
	@Override
    public long getItemId(int position) {
      Route item = getItem(position);
      return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

}
