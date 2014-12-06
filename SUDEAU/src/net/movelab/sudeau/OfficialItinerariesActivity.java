package net.movelab.sudeau;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class OfficialItinerariesActivity extends Activity {

    private OfficialRouteArrayAdapter routeArrayAdapter;
    private ListView listView;
    private EruletApp app;
    private Context context;

    String locale;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.official_itineraries);

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        context = getApplication();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

        listView = (ListView) findViewById(R.id.lv_my_routes);
        List<Route> routes = loadRoutes();

        routeArrayAdapter = new OfficialRouteArrayAdapter(this, locale, routes, app);
        listView.setAdapter(routeArrayAdapter);

    }

    private List<Route> loadRoutes() {
        List<Route> routes = DataContainer.getAllOfficialRoutes(app.getDataBaseHelper());
        for (Route r : routes) {
            DataContainer.refreshRoute(r, app.getDataBaseHelper());
            if (r.getTrack() != null)
                DataContainer.getTrackSteps(r.getTrack(), app.getDataBaseHelper());
        }
        return routes;
    }

    private void refreshListView() {
        List<Route> newRoutes = loadRoutes();
        routeArrayAdapter = new OfficialRouteArrayAdapter(this, locale, newRoutes, app);
        listView.setAdapter(routeArrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListView();
    }


}

class OfficialRouteArrayAdapter extends ArrayAdapter<Route> {

    HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
    private final Context context;
    private List<Route> routes;
    private EruletApp app;
    //private Route currentRoute;
    String locale;
    public Route selectedRoute;


    public OfficialRouteArrayAdapter(Context context, String locale, List<Route> objects, EruletApp app) {
        super(context, R.layout.local_route_list_item, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
        this.routes = objects;
        this.context = context;
        this.app = app;
        this.locale = locale;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.official_itineraries_item, parent, false);
        if(rowView != null){
        TextView nom = (TextView) rowView.findViewById(R.id.route_name);
        ImageButton download_start = (ImageButton) rowView.findViewById(R.id.download_start_button);
        ImageButton download_cancel = (ImageButton) rowView.findViewById(R.id.download_cancel_button);
        ImageButton download_refresh = (ImageButton) rowView.findViewById(R.id.download_refresh_button);
        ProgressBar progress_bar = (ProgressBar) rowView.findViewById(R.id.progress_bar);


        final Route currentRoute = routes.get(position);
        nom.setText(currentRoute.getName(locale));
        download_start.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        download_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        download_refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        }
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
