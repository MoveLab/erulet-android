package net.movelab.sudeau;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.movelab.sudeau.database.DataBaseHelper;
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

public class RouteDrawerWorkerTask extends AsyncTask<EruletApp, Void, List<PolylineOptions>> {
	
	private final WeakReference<GoogleMap> mMapReference;
	private final WeakReference<ProgressBar> progressBarReference;

	public RouteDrawerWorkerTask(GoogleMap mMap, ProgressBar progressBar){
		this.mMapReference=new WeakReference<GoogleMap>(mMap);
		this.progressBarReference=new WeakReference<ProgressBar>(progressBar);
	}
	
	
	@Override
	protected List<PolylineOptions> doInBackground(EruletApp... app) {
		List<PolylineOptions> retVal = new ArrayList<PolylineOptions>();		       
            List<Track> tracks = DataContainer.getAllTracks(app[0].getDataBaseHelper());
            int[] colors = {Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.WHITE};
            int colorCounter = 0;
        for(Track t : tracks){
				List<Step> selectedRouteSteps = DataContainer.getTrackOrderedSteps(t, app[0].getDataBaseHelper());
				PolylineOptions rectOptions = new PolylineOptions();
                PolylineOptions rectOptionsBg = new PolylineOptions();
				for(Step step : selectedRouteSteps) {
					rectOptions.add(new LatLng(step.getLatitude(), step.getLongitude()));
                    rectOptionsBg.add(new LatLng(step.getLatitude(), step.getLongitude()));
				}
				rectOptions.zIndex(1);
                rectOptions.width(5);
				rectOptions.color(colors[colorCounter % colors.length]);
                rectOptionsBg.zIndex(1);
                rectOptionsBg.width(7);
                rectOptionsBg.color(Color.BLACK);
                retVal.add(rectOptionsBg);
                retVal.add(rectOptions);
                colorCounter++;
			}
			return retVal;
	}
	
	@Override
	protected void onPostExecute(List<PolylineOptions> result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
			final GoogleMap mMap = mMapReference.get();
			if(mMap!=null){
				for(PolylineOptions pl : result){
					mMap.addPolyline(pl);
				}
			}
		final ProgressBar progressBar = progressBarReference.get();
        if(progressBar!=null){
        	progressBar.setVisibility(View.GONE);
        }
	}

}
