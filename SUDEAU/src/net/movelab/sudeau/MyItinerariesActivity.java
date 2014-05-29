package net.movelab.sudeau;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.movelab.sudeau.model.Route;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MyItinerariesActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_itineraries);

		ListView listView = (ListView) findViewById(R.id.lv_my_routes);
		ArrayList<Route> myRoutes = new ArrayList<Route>();

		Route r1 = new Route();
		r1.setName("Una ruta");
		r1.setDescription("La meva primera ruta");

		Route r2 = new Route();
		r2.setName("Una altra ruta");
		r2.setDescription("La meva segona ruta");
		
		Route r3 = new Route();
		r3.setName("I una altra");
		r3.setDescription("La meva tercera ruta");

		myRoutes.add(r1);
		myRoutes.add(r2);
		myRoutes.add(r3);
		
		MyRouteArrayAdapter routeArrayAdapter = new MyRouteArrayAdapter(this, myRoutes);
		listView.setAdapter(routeArrayAdapter);

	}

}

class MyRouteArrayAdapter extends ArrayAdapter<Route> {

	HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
	private final Context context;
	private List<Route> routes;

	public MyRouteArrayAdapter(Context context, List<Route> objects) {
		super(context, R.layout.local_route_list_item, objects);		
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
		this.routes=objects;
		this.context=context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.local_route_list_item, parent, false);
		    TextView nom = (TextView) rowView.findViewById(R.id.route_name);
		    TextView description = (TextView) rowView.findViewById(R.id.route_description);		    
		    Route r = routes.get(position);
		    nom.setText(r.getName());
		    description.setText(r.getDescription());
		    // change the icon for Windows and iPhone		    

		    return rowView;
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
