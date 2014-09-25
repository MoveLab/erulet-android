package net.movelab.sudeau;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MultipleHighLightSelection extends Activity {
	
	private HighLightArrayAdapter highLightArrayAdapter;
	private Step step;
	private ListView listView;
	private EruletApp app;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiple_highlight_selection);
		
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

		listView = (ListView) findViewById(R.id.lv_multiple_activities);
		Bundle extras = getIntent().getExtras();
		String step_id = "-1";
		if(extras!=null){
			step_id = extras.getString("step_id");
		}
		List<HighLight> highLights = loadHighLights(step_id);
		
		highLightArrayAdapter = new HighLightArrayAdapter(this, highLights);
		listView.setAdapter(highLightArrayAdapter);
				
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				HighLight h = (HighLight) parent.getItemAtPosition(position);
				switch(h.getType()){
					case HighLight.INTERACTIVE_IMAGE:
						Intent i = new Intent(MultipleHighLightSelection.this,
								InteractiveImageActivityHeatMap.class);			
						i.putExtra("int_image_id", h.getInteractiveImage().getId());
						startActivity(i);
						break;
					case HighLight.REFERENCE:
						Reference r = DataContainer.refreshReference(h.getReference(), app.getDataBaseHelper());
						if(r!=null){
							Intent ir = new Intent(MultipleHighLightSelection.this,
									HTMLViewerActivity.class);
							ir.putExtra("idReference", r.getId());
							startActivity(ir);
						}
						break;
					case HighLight.POINT_OF_INTEREST:
						launchStandardUserPointInfo(h.getId());
						break;
					case HighLight.WAYPOINT:
						launchStandardUserPointInfo(h.getId());
						break;						
					case HighLight.ALERT:
						launchStandardUserPointInfo(h.getId());
						break;
					default:
						break;
				}
			}			
		});		 
	}
	
	private void launchStandardUserPointInfo(String hl_id){
		JSONObject hl_s;
		try {							
			hl_s = JSONConverter.stepToJSONObject(step);
			if (hl_s != null) {
				String s_j_string = hl_s.toString();
				Intent istep = new Intent(
						MultipleHighLightSelection.this,
						DetailHighLightActivity.class);
				istep.putExtra("step_j", s_j_string);
				istep.putExtra("highlight_id", hl_id);
				//highLight_id = extras.getString("highlight_id");
				startActivity(istep);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<HighLight> loadHighLights(String step_id){
		List<HighLight> retVal = new ArrayList<HighLight>();
		if(step_id!=null){
			step = DataContainer.findStepById(step_id, app.getDataBaseHelper());
			if(step!=null){			
				retVal = DataContainer.getStepHighLights(step, app.getDataBaseHelper());
			}
		}
		return retVal;		
	}
		

}
