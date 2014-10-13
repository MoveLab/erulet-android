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

public class OtherItinerariesActivity extends Activity {

    String locale;

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.other_itineraries);

		ListView listView = (ListView) findViewById(R.id.lv_other_routes);
		ArrayList<Route> sharedRoutes = new ArrayList<Route>();

        Context context = getApplication();
        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

        // TODO i18n string
        Route r1 = new Route();
		r1.setName(locale, "Una ruta");

		Route r2 = new Route();
		r2.setName(locale, "Una altra ruta");
		
		Route r3 = new Route();
		r3.setName(locale, "I una altra");

		sharedRoutes.add(r1);
		sharedRoutes.add(r2);
		sharedRoutes.add(r3);
		
		SharedRouteArrayAdapter routeArrayAdapter = new SharedRouteArrayAdapter(this, sharedRoutes);
		listView.setAdapter(routeArrayAdapter);

	}
	
}

class SharedRouteArrayAdapter extends ArrayAdapter<Route> {

	HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
	private final Context context;
	private List<Route> routes;

	public SharedRouteArrayAdapter(Context context, List<Route> objects) {
		super(context, R.layout.shared_route_list_item, objects);		
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
		    View rowView = inflater.inflate(R.layout.shared_route_list_item, parent, false);
		    TextView nom = (TextView) rowView.findViewById(R.id.route_name);		    		   
//		    Route r = routes.get(position);
//		    nom.setText(r.getName());		    
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

