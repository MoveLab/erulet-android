package net.movelab.sudeau;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

public class RouteDrawerWorkerTask extends AsyncTask<String, Void, List<PolylineOptions>> {
	
	private final WeakReference<GoogleMap> mMapReference;
	private final WeakReference<EruletApp> appReference;
	private final WeakReference<ProgressBar> progressBarReference;

	public RouteDrawerWorkerTask(GoogleMap mMap, EruletApp app, ProgressBar progressBar){
		this.mMapReference=new WeakReference<GoogleMap>(mMap);
		this.appReference=new WeakReference<EruletApp>(app);
		this.progressBarReference=new WeakReference<ProgressBar>(progressBar);
	}
	
	
	@Override
	protected List<PolylineOptions> doInBackground(String... params) {		
		List<PolylineOptions> retVal = new ArrayList<PolylineOptions>();		       
		try {
			JSONArray routeList = new JSONArray(params[0]);
			for(int i = 0; i < routeList.length(); i++){
				Route selectedRoute = JSONConverter.jsonObjectToRoute( routeList.getJSONObject(i) );
				Track t = selectedRoute.getTrack();
				final EruletApp app = appReference.get();
				List<Step> selectedRouteSteps = DataContainer.getTrackOrderedSteps(t,app.getDataBaseHelper());
				PolylineOptions rectOptions = new PolylineOptions();
				for (int j = 0; j < selectedRouteSteps.size(); j++) {
					Step step = selectedRouteSteps.get(j);
					rectOptions.add(new LatLng(step.getLatitude(), step.getLongitude()));
				}
				rectOptions.zIndex(1);
				rectOptions.color(Color.RED);
				retVal.add(rectOptions);
			}
			return retVal;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(List<PolylineOptions> result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		if(mMapReference!=null){
			final GoogleMap mMap = mMapReference.get();
			if(mMap!=null){
				for( int i = 0; i<result.size();i++ ){
					mMap.addPolyline(result.get(i));
				}
			}
		}
		final ProgressBar progressBar = progressBarReference.get();
        if(progressBar!=null){
        	progressBar.setVisibility(View.GONE);
        }
	}

}
