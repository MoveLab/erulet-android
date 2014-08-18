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
	
	public static BitmapDescriptor getUserBitmapDescriptor(int hlType){
		BitmapDescriptor bm = null;
		switch(hlType){
			case HighLight.WAYPOINT:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_chart);
				break;			
			case HighLight.ALERT:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_warning);
				break;
			case HighLight.POINT_OF_INTEREST_OFFICIAL:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_drop);
				break;
			case HighLight.POINT_OF_INTEREST_SHARED:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_shared);
				break;
			case HighLight.POINT_OF_INTEREST:
				bm = BitmapDescriptorFactory.fromResource(R.drawable.pin_drop);
				break;
			default:
				bm = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
				break;
		}
		return bm;
	}
	
	public static Marker addHighLightMarker(
			GoogleMap mMap,
			LatLng position,
			String title,
			String snippet,
			int hlType
			){
						
		BitmapDescriptor bm = getUserBitmapDescriptor(hlType);								
		
		return mMap.addMarker(new MarkerOptions()
			.position(position)
			.title(title)
			.snippet(snippet)
			.icon(bm)
		);		
	}
	
	public static CircleOptions getRouteInProgressCircle(
			LatLng position, 
			double radius, 
			boolean last,
			int zIndex) {
		
		CircleOptions copt = new CircleOptions();
		copt.center(position);
		copt.radius(radius);
		copt.zIndex(zIndex);
		copt.strokeColor(Color.BLACK);
		if(last){			
			copt.strokeWidth(4);
		}else{
			copt.strokeWidth(1);
		}
		// Fill color of the circle
		// 0x represents, this is an hexadecimal code
		// 55 represents percentage of transparency. For 100%
		// transparency,
		// specify 00.
		// For 0% transparency ( ie, opaque ) , specify ff
		// The remaining 6 characters(00ff00) specify the fill color
		copt.fillColor(0x8000FFFF);
		return copt;
		
	}	
	
	public static PolylineOptions getRouteInProgressPolyLine(int zIndex) { 
		PolylineOptions rectOptions = new PolylineOptions();
		rectOptions.zIndex(zIndex);
		rectOptions.color(Color.GREEN);
		return rectOptions;
	}

	public static Marker addStartMarker(GoogleMap mMap, LatLng position, String start_text) {
		return mMap.addMarker(new MarkerOptions()		
		.position(position)
		.title(start_text)
		.anchor(0.5f, 0.5f)
		//Allow navigation to start and end markers
		//.flat(true)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.red)));
	}

	public static Marker addEndMarker(GoogleMap mMap, LatLng position, String end_text) { 
		return mMap.addMarker(new MarkerOptions()		
		.position(position)
		.title(end_text)
		.anchor(0.5f, 0.5f)
		//Allow navigation to start and end markers
		//.flat(true)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.green)));
	}

	public static Marker addDirectionMarker(GoogleMap mMap,
			LatLng position, double angle) {
		Marker m = mMap.addMarker(new MarkerOptions()		
		.position(position)		
		.anchor(0.5f, 0.5f)
		.rotation((float)angle)
		.flat(true)
		.icon(BitmapDescriptorFactory.fromResource(R.drawable.triangle_red_s)));		
		return m;
	}
				 
}
