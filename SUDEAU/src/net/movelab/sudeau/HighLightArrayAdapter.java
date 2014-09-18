package net.movelab.sudeau;

import java.util.HashMap;
import java.util.List;

import net.movelab.sudeau.model.HighLight;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HighLightArrayAdapter extends ArrayAdapter<HighLight> {

	HashMap<HighLight, Integer> mIdMap = new HashMap<HighLight, Integer>();
	private final Context context;	
	private List<HighLight> highLights;		

	public HighLightArrayAdapter(Context context, List<HighLight> objects) {
		super(context, R.layout.highlight_list_item, objects);		
		for (int i = 0; i < objects.size(); ++i) {
			mIdMap.put(objects.get(i), i);
		}			
		this.highLights=objects;
		this.context=context;			
	}		
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final HighLight currentHighLight = highLights.get(position);
		    View rowView = inflater.inflate(R.layout.highlight_list_item, parent, false);		    
		    TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
		    TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
		    ImageView icon = (ImageView) rowView.findViewById(R.id.hl_lv_icon);
		    if(currentHighLight!=null){		    	
		    	icon.setImageBitmap( BitmapFactory.decodeResource(context.getResources(), R.drawable.pin_empty) );
		    }
		    firstLine.setText(currentHighLight.getName());
		    secondLine.setText(currentHighLight.getLongText());
		    return rowView;
	}
			
			
	@Override
    public long getItemId(int position) {
      HighLight item = getItem(position);
      return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }
}