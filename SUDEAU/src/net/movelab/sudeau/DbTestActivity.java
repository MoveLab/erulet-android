package net.movelab.sudeau;

import java.util.Iterator;
import java.util.List;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.Preference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;
import net.movelab.sudeau.model.User;
import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

public class DbTestActivity extends Activity {
	
	private DataBaseHelper dataBaseHelper;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (dataBaseHelper != null) {
	        OpenHelperManager.releaseHelper();
	        dataBaseHelper = null;
	    }
	}
	
	private DataBaseHelper getHelper(){
		if(dataBaseHelper==null){
			dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);
		}
		return dataBaseHelper;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.database_test);
		DataBaseHelper db = getHelper();
		RuntimeExceptionDao<User, String> userDataDao = db.getUserDataDao();
		RuntimeExceptionDao<Preference, String> preferenceDataDao = db.getPreferenceDataDao();
		RuntimeExceptionDao<Route, String> routeDataDao = db.getRouteDataDao();
		RuntimeExceptionDao<Track, String> trackDataDao = db.getTrackDataDao();
		RuntimeExceptionDao<Step, String> stepDataDao = db.getStepDataDao();
		RuntimeExceptionDao<HighLight, String> hlDataDao = db.getHlDataDao();
		
		Log.d("Insert","Inserting user records...");
		//Unique android id 
		//User u1 = new User(Secure.getString(this.getContentResolver(),Secure.ANDROID_ID),"User1");		
		User u1 = new User("USER_0","User1");
		try{
			userDataDao.create(u1);
		}catch(RuntimeException ex){
			Log.e("Inserting user","Insert error " + ex.toString());
		}
		
		Track t = new Track("TRACK1","A track");			
		try{
			trackDataDao.create(t);			
		}catch(RuntimeException ex){
			Log.e("Inserting track","Insert error " + ex.toString());
		}
		
		Preference p1 = new Preference();
		p1.setId("1");
		p1.setKey("key1");
		p1.setValue("value1");
		p1.setUser(u1);
		
		Preference p2 = new Preference();
		p2.setId("2");
		p2.setKey("key2");
		p2.setValue("value2");
		p2.setUser(u1);
				
		try{
			preferenceDataDao.create(p1);
			preferenceDataDao.create(p2);
		}catch(RuntimeException ex){
			Log.e("Inserting preference","Insert error " + ex.toString());
		}
		
		HighLight h1 = new HighLight("hl1","hl1",10d);
		HighLight h2 = new HighLight("hl2","hl2",20d);
		try{
			hlDataDao.create(h1);
			hlDataDao.create(h2);
		}catch(RuntimeException ex){
			Log.e("Inserting preference","Insert error " + ex.toString());
		}
		
		Step s1 = new Step("step1","step1",1.0d,30d,0d,10d,1,t,h1);		
		Step s2 = new Step("step2","step2",2.0d,15d,0d,10d,2,t,h2);
		
		try{
			stepDataDao.create(s1);
			stepDataDao.create(s2);
		}catch(RuntimeException ex){
			Log.e("Inserting step","Insert error " + ex.toString());
		}
		
		Route r = new Route();
		r.setId("ROUTE1");
		r.setName("A route");
		r.setUser(u1);
		r.setTrack(t);
		try{
			routeDataDao.create(r);			
		}catch(RuntimeException ex){
			Log.e("Inserting route","Insert error " + ex.toString());
		}									
							
		Log.d("Select", "Selecting inserted records...");
		List<User> users = userDataDao.queryForAll();
		StringBuilder results = new StringBuilder();
		for(int i=0; i < users.size(); i++){
			User u = users.get(i);
			results.append(u.toString() + " ");			
			Iterator<Preference> pr = u.getPreferences().iterator();			
			while(pr.hasNext()){
				Preference p = pr.next();
				results.append(p.toString() + " ");
			}
			Iterator<Route> ri = u.getRoutes().iterator();
			while(ri.hasNext()){
				Route r_n = ri.next();
				results.append(r_n.toString() + " ");
				if(r_n.getTrack()!=null){
					Track track = r_n.getTrack();
					trackDataDao.refresh(track);
					results.append(track.toString() + " ");
					Iterator<Step> si = track.getSteps().iterator();
					while(si.hasNext()){
						Step step = si.next();
						results.append(step.toString() + " ");
						if(step.getHighlight()!=null){
							HighLight hl = step.getHighlight();
							hlDataDao.refresh(hl);
							results.append(hl.toString() + " ");
						}
					}
				}
			}
		}
		TextView tv = (TextView) findViewById(R.id.tvDatabaseTest);
		tv.setText(results.toString());
	}
		

}
