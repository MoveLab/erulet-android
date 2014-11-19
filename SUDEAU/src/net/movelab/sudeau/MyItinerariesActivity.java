package net.movelab.sudeau;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
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
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MyItinerariesActivity extends Activity {
	
	//TODO Add date and time of creation of the route
	//TODO Modify layout so that it acommodates landscape orientation better			
	private MyRouteArrayAdapter routeArrayAdapter;
	private ListView listView;
	private EruletApp app;
    private Context context;

	String locale;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_itineraries);
		
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        context = getApplication();
        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

		listView = (ListView) findViewById(R.id.lv_my_routes);
		String userId = PropertyHolder.getUserId();
		List<Route> myRoutes = loadRoutes(userId);
		
		routeArrayAdapter = new MyRouteArrayAdapter(this, locale, myRoutes,app);
		listView.setAdapter(routeArrayAdapter);			

	}
	
	private List<Route> loadRoutes(String userId){
		List<Route> myRoutes = DataContainer.getUserRoutes(app.getDataBaseHelper(),userId);
		for(Route r : myRoutes){
			DataContainer.refreshRoute(r, app.getDataBaseHelper());
			if(r.getTrack()!=null)
				DataContainer.getTrackSteps(r.getTrack(), app.getDataBaseHelper());
		}
		return myRoutes;
	}
	
	private void refreshListView(String userId){
		List<Route> newRoutes = loadRoutes(userId);
		routeArrayAdapter = new MyRouteArrayAdapter(this, locale, newRoutes,app);
		listView.setAdapter(routeArrayAdapter);
	}
	
	@Override
	protected void onResume() { 
		super.onResume();
		String userId = PropertyHolder.getUserId();
		refreshListView(userId);
	}




}

class MyRouteArrayAdapter extends ArrayAdapter<Route> {

	HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
	private final Context context;	
	private List<Route> routes;
	private EruletApp app;
	//private Route currentRoute;
    String locale;
    public Route selectedRoute;


    public MyRouteArrayAdapter(Context context, String locale, List<Route> objects,EruletApp app) {
		super(context, R.layout.local_route_list_item, objects);		
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}
		this.routes=objects;
		this.context=context;
		this.app=app;
        this.locale = locale;
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
		    ImageButton upload = (ImageButton) rowView.findViewById(R.id.route_upload);
		    final Route currentRoute = routes.get(position);
		    nom.setText(currentRoute.getName(locale));
		    description.setText(currentRoute.getDescription(locale));
		    edit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                    Log.i("route check", "name_" + locale + " = " + currentRoute.getName(locale));
					Intent i = new Intent(context,EditRouteActivity.class);
					i.putExtra("routeId", currentRoute.getId());
					context.startActivity(i);
				}
			});
		    delete.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {					
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
					 
			        // Setting Dialog Title
			        alertDialog.setTitle(app.getResources().getString(R.string.confirm_delete));
			 
			        // Setting Dialog Message
			        alertDialog.setMessage( app.getResources().getString(R.string.about_to_delete) + "\n" + currentRoute.getName(locale) +  "\n" + app.getResources().getString(R.string.really_continue));
			 
			        // Setting Icon to Dialog
			        alertDialog.setIcon(R.drawable.ic_delete);
			 
			        // Setting Positive "Yes" Button
			        alertDialog.setPositiveButton(app.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog,int which) {
			            	remove(currentRoute);			            	
			            }
			        });
			 
			        // Setting Negative "NO" Button
			        alertDialog.setNegativeButton(app.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	// 	Write your code here to invoke NO event			            	
			            	dialog.cancel();
			            }
			        });
			 
			        // Showing Alert Message
			        alertDialog.show();					
				}
			});
        upload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedRoute = currentRoute;
                new ItineraryUploadTask().execute(context);

            }
        });

        return rowView;
	}
	
	@Override
	public void remove(Route object) { 
		super.remove(object);
		DataContainer.deleteRouteCascade(object, app);
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

    public class ItineraryUploadTask extends AsyncTask<Context, Integer, Boolean> {

        ProgressDialog prog;

        int myProgress;
        int resultFlag;

        int OFFLINE = 0;
        int UPLOAD_ERROR = 1;
        int DATABASE_ERROR = 2;
        int SUCCESS = 3;
        int JSON_ERROR = 4;

        HttpResponse response;
        JSONObject responseJson;
        int statusCode = -1;
        int server_id = -1;


        @Override
        protected void onPreExecute() {

            resultFlag = SUCCESS;

            prog = new ProgressDialog(context);
            prog.setTitle("Uploading itinerary");
            prog.setIndeterminate(false);
            prog.setMax(100);
            prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            prog.show();

            myProgress = 0;

        }

        protected Boolean doInBackground(Context... context) {

            try{
                JSONObject this_route_json = JSONConverter.userRouteToServerJSONObject(selectedRoute, app);
                response = Util.postJSON(this_route_json, UtilLocal.URL_USER_ROUTES, context[0]);
                statusCode = Util.getResponseStatusCode(response);
                responseJson = Util.parseResponse(context[0], response);
                if(statusCode >= 200 && statusCode < 300){
                    resultFlag = SUCCESS;
                    server_id = responseJson.optInt("server_id", -1);
                    selectedRoute.setServerId(server_id);
                    DataContainer.updateRoute(selectedRoute, app.getDataBaseHelper());
                }
                else{
                    resultFlag = UPLOAD_ERROR;
                }
            } catch (JSONException e){
                resultFlag = JSON_ERROR;
            }
            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

            prog.setProgress(progress[0]);
        }

        protected void onPostExecute(Boolean result) {

            prog.dismiss();

            if (result && resultFlag == SUCCESS) {
                Util.toast(
                        context,"Uploaded: Server ID " + server_id);

                //TODO remove this just for testing
                buildCustomAlert("server response: " + responseJson.toString());

            } else {
                if (resultFlag == OFFLINE) {
                    buildCustomAlert("You do not have an internet connection right now. Please try again later.");
                }else if (resultFlag == UPLOAD_ERROR) {
                    buildCustomAlert("Upload error. JSON: " + responseJson.toString());
                }else if (resultFlag == DATABASE_ERROR) {
                    buildCustomAlert("Database error");
                }else if (resultFlag == JSON_ERROR) {
                    buildCustomAlert("Json error");
                }

            }

        }
    }


    public void buildCustomAlert(String message) {

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.custom_alert);

        dialog.setCancelable(false);

        TextView alertText = (TextView) dialog.findViewById(R.id.alertText);
        alertText.setText(message);

        Button positive = (Button) dialog.findViewById(R.id.alertOK);
        Button negative = (Button) dialog.findViewById(R.id.alertCancel);
        negative.setVisibility(View.GONE);

        positive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();

            }
        });

        dialog.show();

    }



}
