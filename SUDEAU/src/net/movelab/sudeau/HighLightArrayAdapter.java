package net.movelab.sudeau;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.movelab.sudeau.model.HighLight;

import java.util.HashMap;
import java.util.List;

public class HighLightArrayAdapter extends ArrayAdapter<HighLight> {

    HashMap<HighLight, Integer> mIdMap = new HashMap<HighLight, Integer>();
    private final Context context;
    private List<HighLight> highLights;
    String currentLocale;

    public HighLightArrayAdapter(Context context, List<HighLight> objects) {
        super(context, R.layout.highlight_list_item, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
        this.highLights = objects;
        this.context = context;

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        currentLocale = PropertyHolder.getLocale();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final HighLight currentHighLight = highLights.get(position);
        View rowView = inflater.inflate(R.layout.highlight_list_item, parent, false);
        if (rowView != null) {
            TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
            TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
            ImageView icon = (ImageView) rowView.findViewById(R.id.hl_lv_icon);
            if (currentHighLight != null) {
                icon.setImageBitmap(
                        MapObjectsFactory.getBitmap(currentHighLight.getType(), context)
                );

                firstLine.setText(currentHighLight.getName(currentLocale));
                secondLine.setText(currentHighLight.getLongText(currentLocale));
            }
        }
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