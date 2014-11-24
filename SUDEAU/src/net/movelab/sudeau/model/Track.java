package net.movelab.sudeau.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "track")
public class Track {

    @DatabaseField(generatedId=true)
    private int id;
    @DatabaseField
    private int server_id;
	@DatabaseField
	private String name;
	@ForeignCollectionField
	private Collection<Step> steps;
    @DatabaseField(foreign = true, columnName = "routeId")
    private Route route;
	private Reference reference;

	public Track(){
		steps = new ArrayList<Step>();
        this.server_id = -1;
    }
	
	public Track(int server_id) {
		this.server_id=server_id;
		steps = new ArrayList<Step>();
	}
	
	public Track(int server_id, String name) {
		this.server_id=server_id;
		this.name=name;
		steps = new ArrayList<Step>();
	}

	public Reference getReference() {
		return reference;
	}

	public void setReference(Reference reference) {
		this.reference = reference;
	}

	public Collection<Step> getSteps() {
		return steps;
	}

    public void setId(int id){
        this.id = id;
    }

	public void setSteps(Collection<Step> steps) {
		this.steps = steps;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

    public int getId() {
        return id;
    }

    public int getServerId() {
        return server_id;
    }

    public void setServerId(int server_id) {
        this.server_id = server_id;

    }


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}		
	
	@Override
	public String toString(){
		return "TRACK " + id + " " + name;
	}

}
