package net.movelab.sudeau.model;


// route bare bones. Just simple class for storing query results
public class RBB {
    public int id;
    public int trackId;
    public String name;
    public String description;
    public float globalRating;

	public RBB(int id, int trackId, String name, String description, float globalRating){
        this.id = id;
        this.trackId = trackId;
        this.name = name;
        this.description = description;
        this.globalRating = globalRating;
	}

}
