package net.movelab.sudeau;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapObjectsFactory {		
	
	public static Marker addStartRouteMarker(
			GoogleMap mMap,
			LatLng position,
			String text){
		return mMap.addMarker(new MarkerOptions()
		.position(position)
		.title(text)
		.snippet(null)
		.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
	}
	
	public static Marker addLastPositionMarker(
			GoogleMap mMap
			){
		return mMap.addMarker(new MarkerOptions()
		.title("Ets aquí!")
		.snippet("(més o menys)")
		.position(new LatLng(-27, 133))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_erulet_new)));
	}
	
	public static Marker addEmptyUserMarker(
			GoogleMap mMap,
			LatLng position,
			String title,
			String snippet
			){
		
		return mMap.addMarker(new MarkerOptions()
		.position(position)
		.title(title)
		.snippet(snippet)
		.icon(BitmapDescriptorFactory
				.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
	}
	
	public static Marker addHighLightMarker(
			GoogleMap mMap,
			LatLng position,
			String title,
			String snippet,
			int hlType
			){
		
//		BitmapDescriptor bm = BitmapDescriptorFactory.
//			defaultMarker(BitmapDescriptorFactory.HUE_BLUE); 
		
		//BitmapDescriptor bm = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker);				
		BitmapDescriptor bm = null;
		switch(hlType){
			case 0:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_chart);
				break;
			case 1:				
				break;
			case 2:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_drop);
				break;
			default:
				bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
				break;
		}		
				
				
		
		return mMap.addMarker(new MarkerOptions()
			.position(position)
			.title(title)
			.snippet(snippet)
			.icon(bm)
		);		
	}
				 
}
