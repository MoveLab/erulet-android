package net.movelab.sudeau;

import net.movelab.sudeau.model.HighLight;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
			GoogleMap mMap, String title, String snippet
			){
		return mMap.addMarker(new MarkerOptions()
		.title(title)
		.snippet(snippet)
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
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_empty)));
	}
	
	public static Marker addHighLightMarker(
			GoogleMap mMap,
			LatLng position,
			String title,
			String snippet,
			int hlType
			){
						
		BitmapDescriptor bm = null;
		switch(hlType){
			case HighLight.WAYPOINT:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_chart);
				break;			
			case HighLight.ALERT:
				bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
				break;
			case HighLight.POINT_OF_INTEREST_OFFICIAL:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_drop);
				break;
			case HighLight.POINT_OF_INTEREST_SHARED:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_shared);
				break;
			case HighLight.POINT_OF_INTEREST:
				bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
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
	
	public static CircleOptions getRouteInProgressCircle(LatLng position, double radius) {
		
		CircleOptions copt = new CircleOptions();
		copt.center(position);
		copt.radius(radius);
		copt.zIndex(1);
		copt.strokeColor(Color.BLACK);
		copt.strokeWidth(2);
		// Fill color of the circle
		// 0x represents, this is an hexadecimal code
		// 55 represents percentage of transparency. For 100%
		// transparency,
		// specify 00.
		// For 0% transparency ( ie, opaque ) , specify ff
		// The remaining 6 characters(00ff00) specify the fill color
		copt.fillColor(0xFF00FFFF);
		return copt;
		
	}
	
	public static PolylineOptions getRouteInProgressPolyLine() { 
		PolylineOptions rectOptions = new PolylineOptions();
		rectOptions.zIndex(1);
		rectOptions.color(Color.GREEN);
		return rectOptions;
	}
				 
}
