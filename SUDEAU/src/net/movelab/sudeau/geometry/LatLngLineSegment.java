package net.movelab.sudeau.geometry;

import com.google.android.gms.maps.model.LatLng;

public class LatLngLineSegment {

	private LatLng l1;
	private LatLng l2;
	
	public LatLngLineSegment(LatLng l1, LatLng l2){
		this.l1=l1;
		this.l2=l2;
	}

	public LatLng getL1() {
		return l1;
	}

	public void setL1(LatLng l1) {
		this.l1 = l1;
	}

	public LatLng getL2() {
		return l2;
	}

	public void setL2(LatLng l2) {
		this.l2 = l2;
	}

}
