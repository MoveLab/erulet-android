package net.movelab.sudeau.database;

import java.io.IOException;
import java.sql.Ref;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.movelab.sudeau.EruletApp;
import net.movelab.sudeau.Util;
import net.movelab.sudeau.model.Box;
//import net.movelab.sudeau.model.EruMedia;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.Settings.Secure;
import android.util.Log;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class DataContainer {

    /**
     * Incremental unique route id
     *
     * @param db
     * @param userId
     * @return
     */
    public static String getRouteId(DataBaseHelper db, String userId) {
        QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where<Route, String> where = queryBuilder.where();
        String retVal = null;
        try {
            where.eq("userId", userId);
            PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
            List<Route> userRoutes = db.getRouteDataDao().query(preparedQuery);
            if (userRoutes != null) {
                // int c = userRoutes.size() + 1;
                List<String> routeIds = getRouteIds(userRoutes);
                int c = getMaxCounter(routeIds);
                retVal = "R_" + userId + "_" + c;
            } else {
                retVal = "R_" + userId + "_1";
            }
            Log.d("getRouteId", "Returning route id" + retVal);
            return retVal;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }

    /**
     * Incremental unique route id
     *
     * @param db
     * @param userId
     * @return
     */
    public static String getHighLightId(DataBaseHelper db, String userId) {
        QueryBuilder<HighLight, String> queryBuilder = db.getHlDataDao()
                .queryBuilder();
        Where<HighLight, String> where = queryBuilder.where();
        String retVal = null;
        try {
            where.like("id", "%" + userId + "%");
            PreparedQuery<HighLight> preparedQuery = queryBuilder.prepare();
            List<HighLight> userHighLights = db.getHlDataDao().query(
                    preparedQuery);
            if (userHighLights != null) {
                // int c = userRoutes.size() + 1;
                List<String> highLightIds = getHighLightIds(userHighLights);
                int c = getMaxCounter(highLightIds);
                retVal = "H_" + userId + "_" + c;
            } else {
                retVal = "H_" + userId + "_1";
            }
            return retVal;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }

    private static List<String> getHighLightIds(List<HighLight> userHighLights) {
        ArrayList<String> retVal = new ArrayList<String>();
        for (int i = 0; i < userHighLights.size(); i++) {
            retVal.add(userHighLights.get(i).getId());
        }
        return retVal;
    }

    private static List<String> getRouteIds(List<Route> userRoutes) {
        ArrayList<String> retVal = new ArrayList<String>();
        for (int i = 0; i < userRoutes.size(); i++) {
            retVal.add(userRoutes.get(i).getId());
        }
        return retVal;
    }

    /**
     * Creates an empty route/track entry to the database, and returns the route
     * object
     */
    public static Route createEmptyRoute(String lang, DataBaseHelper db, String userId,
                                         String routeBasedOnId) {
        String idTrack = getTrackId(db, userId);
        Log.d("createEmptyRoute", "Getting track id " + idTrack);
        Track t = new Track();
        t.setId(idTrack);
        db.getTrackDataDao().create(t);
        String idRoute = getRouteId(db, userId);
        Log.d("createEmptyRoute", "Getting route id " + idRoute);
        Route r = new Route();
        r.setId(idRoute);
        r.setIdRouteBasedOn(routeBasedOnId);
        // TODO put into localized strings
        r.setName(lang, "La meva ruta");
        r.setDescription(lang, "La meva descripcio");
        r.setUserId(userId);
        r.setTrack(t);
        db.getRouteDataDao().create(r);
        Log.d("createEmptyRoute", "Route " + idRoute + " saved");
        return r;
    }

    public static FileManifest createFileManifest(String path, DataBaseHelper db) {
        FileManifest fm = new FileManifest();
        fm.setPath(path);
        Log.d("created FileManifest", "path: " + fm.getPath());
        db.getFileManifestDataDao().create(fm);
        Log.d("created FileManifest", "id " + fm.getId());
        return fm;
    }


    public static void addStepToTrack(Step s, Track t, String userId,
                                      DataBaseHelper db) {
        // Track is already created
        s.setTrack(t);
        String stepId = getStepId(db, userId);
        s.setId(stepId);
        db.getStepDataDao().create(s);
// This is causing problems -- and according to ormlite docs, adding s to t after s has been created is a mistake. Just add t to s as above.
//        t.getSteps().add(s);
    }

    public static void addHighLightToStep(Step s, HighLight h, String userId,
                                          DataBaseHelper db) {
        // Step already exists
        h.setStep(s);
        String hlId = getHighLightId(db, userId);
        h.setId(hlId);
        db.getHlDataDao().create(h);
//		s.getHighlights().add(h);
//		s.setHighlight(h);
        db.getStepDataDao().update(s);
    }

    public static Route refreshRoute(Route r, DataBaseHelper db) {
        db.getRouteDataDao().refresh(r);
        return r;
    }

    public static HighLight refreshHighlightForFileManifest(HighLight hl, DataBaseHelper db) {
        db.getFileManifestDataDao().refresh(hl.getFileManifest());
        Log.i("REFRESH", hl.getId());
        return hl;
    }

    public static Step refreshStepForTrack(Step s, DataBaseHelper db) {
        db.getTrackDataDao().refresh(s.getTrack());
        Log.i("REFRESH", s.getId());
        return s;
    }

    public static Route refreshRouteForTrack(Route r, DataBaseHelper db) {
        db.getTrackDataDao().refresh(r.getTrack());
        Log.i("REFRESH", r.getId());
        return r;
    }

    public static Track refreshTrackForRoute(Track t, DataBaseHelper db) {
        db.getRouteDataDao().refresh(t.getRoute());
        Log.i("REFRESH", t.getId());
        return t;
    }


    public static Reference refreshReference(Reference r, DataBaseHelper db) {
        db.getReferenceDataDao().refresh(r);
        return r;
    }

    public static void deleteRouteCascade(Route r, EruletApp app) {
        if (r.getTrack() != null) {
            deleteTrackCascade(r.getTrack(), app);
        }
        if (r.getReference() != null) {
            deleteReference(r.getReference(), app);
        }
        app.getDataBaseHelper().getRouteDataDao().delete(r);
        SharedPreferences.Editor ed = app.getPrefsEditor();
        ed.remove(r.getId());
        ed.commit();
    }

    public static void deleteInteractiveImageCascade(InteractiveImage img, EruletApp app) {
        app.getDataBaseHelper().getInteractiveImageDataDao().refresh(img);
        if (img.getBoxes() != null) {
            List<Box> boxes = getInteractiveImageBoxes(img, app.getDataBaseHelper());
            for (Box b : boxes) {
                app.getDataBaseHelper().getBoxDataDao().delete(b);
            }
        }
        app.getDataBaseHelper().getInteractiveImageDataDao().delete(img);
    }

    public static void deleteTrackCascade(Track t, EruletApp app) {
        if (t.getSteps() != null) {
            List<Step> steps = getTrackSteps(t, app.getDataBaseHelper());
            for (Step s : steps) {
                deleteStepCascade(s, app);
            }
        }
        app.getDataBaseHelper().getTrackDataDao().delete(t);
    }

    public static void deleteStepCascade(Step s, EruletApp app) {
//		if (s.getHighlight() != null) {
//			deleteHighLight(s.getHighlight(), app);
//		}
        if (s.getHighlights() != null) {
            for (HighLight h : s.getHighlights()) {
                deleteHighLight(h, app);
            }
        }
        if (s.getReference() != null) {
            deleteReference(s.getReference(), app);
        }
        app.getDataBaseHelper().getStepDataDao().delete(s);
    }

    public static void deleteReference(Reference r, EruletApp app) {
        app.getDataBaseHelper().getReferenceDataDao().delete(r);
    }

    public static void deleteHighLight(HighLight h, EruletApp app) {
        if (h.getReferences() != null) {
            for (Reference ref : h.getReferences()) {
                app.getDataBaseHelper().getReferenceDataDao().delete(ref);
            }
        }
        if (h.getInteractiveImages() != null) {
            for (InteractiveImage ii : h.getInteractiveImages()) {
                deleteInteractiveImageCascade(ii, app);
            }
        }
        app.getDataBaseHelper().getHlDataDao().delete(h);
        SharedPreferences.Editor ed = app.getPrefsEditor();
        ed.remove(h.getId());
        ed.commit();
    }

    /**
     * Incremental unique track id
     *
     * @param db
     * @param userId
     * @return
     */
    public static String getTrackId(DataBaseHelper db, String userId) {
        QueryBuilder<Track, String> queryBuilder = db.getTrackDataDao()
                .queryBuilder();
        Where<Track, String> where = queryBuilder.where();
        String retVal = null;
        try {
            where.like("id", "%" + userId + "%");
            PreparedQuery<Track> preparedQuery = queryBuilder.prepare();
            List<Track> trackRoutes = db.getTrackDataDao().query(preparedQuery);
            if (trackRoutes != null) {
                List<String> ids = getTrackIds(trackRoutes);
                int c = getMaxCounter(ids);
                // int c = trackRoutes.size() + 1;
                retVal = "T_" + userId + "_" + c;
            } else {
                retVal = "T_" + userId + "_1";
            }
            Log.d("getTrackId", "Returning track id" + retVal);
            return retVal;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }

    private static List<String> getTrackIds(List<Track> tracks) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < tracks.size(); i++) {
            ids.add(tracks.get(i).getId());
        }
        return ids;
    }

    private static int getMaxCounter(List<String> ids) {
        // Each string has the structure [CHARACTER]_[Android_id]_[Counter]
        // We want to extract the counter
        if (ids.size() == 0) {
            return 1;
        } else {
            int max = 0;
            for (int i = 0; i < ids.size(); i++) {
                String counter_s = ids.get(i).split("_")[2];
                int counter = Integer.parseInt(counter_s);
                if (counter > max)
                    max = counter;
            }
            return max + 1;
        }
    }

    public static String getStepId(DataBaseHelper db, String userId) {
        QueryBuilder<Step, String> queryBuilder = db.getStepDataDao()
                .queryBuilder();
        Where<Step, String> where = queryBuilder.where();
        String retVal = null;
        try {
            where.like("id", "%" + userId + "%");
            PreparedQuery<Step> preparedQuery = queryBuilder.prepare();
            List<Step> userSteps = db.getStepDataDao().query(preparedQuery);
            if (userSteps != null) {
                List<String> stepIds = getStepIds(userSteps);
                // int c = userSteps.size() + 1;
                int c = getMaxCounter(stepIds);
                retVal = "S_" + userId + "_" + c;
            } else {
                retVal = "S_" + userId + "_1";
            }
            Log.d("getTrackId", "Returning step id" + retVal);
            return retVal;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return retVal;
    }

    private static List<String> getStepIds(List<Step> steps) {
        ArrayList<String> ids = new ArrayList<String>();
        for (int i = 0; i < steps.size(); i++) {
            ids.add(steps.get(i).getId());
        }
        return ids;
    }

    public static void editRoute(Route editedRoute, DataBaseHelper db) {
        db.getRouteDataDao().update(editedRoute);
    }

    public static InteractiveImage findInteractiveImageById(String idImage,
                                                            DataBaseHelper db) {
        InteractiveImage i = db.getInteractiveImageDataDao()
                .queryForId(idImage);
        return i;
    }

    public static HighLight findHighLightById(String idHighLight, DataBaseHelper db) {
        HighLight h = db.getHlDataDao().queryForId(idHighLight);
        return h;
    }

    public static Route findRouteById(String idRoute, DataBaseHelper db) {
        Route r = db.getRouteDataDao().queryForId(idRoute);
        return r;
    }

    public static Step findStepById(String idStep, DataBaseHelper db) {
        Step s = db.getStepDataDao().queryForId(idStep);
        return s;
    }

    public static Reference findReferenceById(String idReference,
                                              DataBaseHelper db) {
        Reference r = db.getReferenceDataDao().queryForId(idReference);
        return r;
    }

    public static List<Track> getAllTracks(DataBaseHelper db) {
        List<Track> tracks = db.getTrackDataDao().queryForAll();
        return tracks;
    }

    public static List<Route> getUserRoutes(DataBaseHelper db, String userId) {
        List<Route> userRoutes = new ArrayList<Route>();
        QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where<Route, String> where = queryBuilder.where();
        String retVal = null;
        try {
            where.eq("userId", userId);
            PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
            userRoutes = db.getRouteDataDao().query(preparedQuery);
            return userRoutes;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return userRoutes;
    }

    public static List<Route> getAllRoutes(DataBaseHelper db) {
        List<Route> routes = db.getRouteDataDao().queryForAll();
        return routes;
    }

    public static List<Route> getAllOfficialRoutes(DataBaseHelper db) {
        List<Route> officialRoutes = new ArrayList<Route>();
        QueryBuilder<Route, String> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where<Route, String> where = queryBuilder.where();
        try {
            where.eq("official", true);
            PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
            officialRoutes = db.getRouteDataDao().query(preparedQuery);
            return officialRoutes;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return officialRoutes;
    }


//	public static HighLight getHighLightStep(Step s, DataBaseHelper db) {
//		HighLight h = s.getHighlight();
//		if (h == null)
//			return null;
//		else
//			db.getHlDataDao().refresh(h);
//		return h;
//	}

    public static Reference getReferenceStep(Step s, DataBaseHelper db) {
        Reference r = s.getReference();
        db.getReferenceDataDao().refresh(r);
        return r;
    }

//	QueryBuilder<Track, String> queryBuilder = db.getTrackDataDao()
//			.queryBuilder();
//	Where<Track, String> where = queryBuilder.where();
//	String retVal = null;
//	try {
//		where.like("id", "%" + userId + "%");
//		PreparedQuery<Track> preparedQuery = queryBuilder.prepare();
//		List<Track> trackRoutes = db.getTrackDataDao().query(preparedQuery);
//		if (trackRoutes != null) {
//			List<String> ids = getTrackIds(trackRoutes);
//			int c = getMaxCounter(ids);
//			// int c = trackRoutes.size() + 1;
//			retVal = "T_" + userId + "_" + c;
//		} else {
//			retVal = "T_" + userId + "_1";
//		}			
//		Log.d("getTrackId", "Returning track id" + retVal);			
//		return retVal;
//	} catch (SQLException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	return retVal;

    public static Step getRouteStarterFast(Route route, DataBaseHelper db) {
        if (route.getTrack() != null) {
            try {
                List<Step> steps = db.getStepDataDao().query(
                        db.getStepDataDao().queryBuilder().where()
                                .eq("trackId", route.getTrack().getId()).and().eq("order", 1).prepare());
                if (steps != null && steps.size() > 0) {
                    return steps.get(0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Step getRouteStarter(Route route, DataBaseHelper db) {
        Track t = route.getTrack();
        db.getTrackDataDao().refresh(t);
        List<Step> steps = getTrackSteps(t, db);
        if (steps != null && steps.size() > 0) {
            return steps.get(0);
        }
        return null;
    }

    public static List<Step> getRouteSteps(Route route, DataBaseHelper db) {
        Track t = route.getTrack();
        db.getTrackDataDao().refresh(t);
        return getTrackSteps(t, db);
    }

    public static List<Box> getInteractiveImageBoxes(InteractiveImage img,
                                                     DataBaseHelper db) {
        db.getInteractiveImageDataDao().refresh(img);
        ArrayList<Box> retVal = new ArrayList<Box>();
        Iterator<Box> boxIt = img.getBoxes().iterator();
        while (boxIt.hasNext()) {
            Box b = boxIt.next();
            retVal.add(b);
        }
        return retVal;
    }


    public static Step refreshStep(Step s, DataBaseHelper db) {
        db.getStepDataDao().refresh(s);
        return s;
    }

    public static List<HighLight> getStepHighLights(Step step, DataBaseHelper db) {
        db.getStepDataDao().refresh(step);
        ArrayList<HighLight> retVal = new ArrayList<HighLight>();
        Iterator<HighLight> hlIt = step.getHighlights().iterator();
        while (hlIt.hasNext()) {
            HighLight h = hlIt.next();
            retVal.add(h);
        }
        return retVal;
    }

    public static List<Step> getTrackSteps(Track track, DataBaseHelper db) {
        db.getTrackDataDao().refresh(track);
        ArrayList<Step> retVal = new ArrayList<Step>();
        Iterator<Step> stepIt = track.getSteps().iterator();
        while (stepIt.hasNext()) {
            Step s = stepIt.next();
//			if (s.getHighlight() != null) {
//				db.getHlDataDao().refresh(s.getHighlight());
//			}
            if (s.getHighlights() != null) {
                for (HighLight h : s.getHighlights()) {
                    db.getHlDataDao().refresh(h);
                }
            }
            retVal.add(s);
        }
        Collections.sort(retVal);
        return retVal;
    }

    public static List<Step> getTrackOrderedSteps(Track track, DataBaseHelper db) {
        db.getTrackDataDao().refresh(track);
        ArrayList<Step> retVal = new ArrayList<Step>();
        Iterator<Step> stepIt = track.getSteps().iterator();
        while (stepIt.hasNext()) {
            Step s = stepIt.next();
            if (s.getOrder() != -1) {
                retVal.add(s);
            }
        }
        Collections.sort(retVal);
        return retVal;
    }

    public static void updateRoute(Route r, DataBaseHelper dataBaseHelper) {
        dataBaseHelper.getRouteDataDao().update(r);
    }

    public static void updateHighLight(HighLight h, DataBaseHelper dataBaseHelper) {
        dataBaseHelper.getHlDataDao().update(h);
    }
    public static void updateFileManifest(FileManifest fm, DataBaseHelper dataBaseHelper) {
        dataBaseHelper.getFileManifestDataDao().update(fm);
    }
    public static void updateInteractiveImage(InteractiveImage ii, DataBaseHelper dataBaseHelper) {
        dataBaseHelper.getInteractiveImageDataDao().update(ii);
    }

    public static void insertRoute(Route editedRoute,
                                   DataBaseHelper dataBaseHelper, String userId) {
        // Save track
        // Track t = new Track();
        if (editedRoute.getTrack() != null) {
            Track t = editedRoute.getTrack();
            if (t.getId() == null || t.getId().equals("")) {
                t.setId(
                        DataContainer.getTrackId(dataBaseHelper, userId));
            }
            if (t.getName() == null || t.getName().equals("")) {
                //TODO change this once all track languages are in. Should be a line for each language.
                t.setName(editedRoute.getName("ca"));
            }
            try {
                dataBaseHelper.getTrackDataDao().create(t);


            } catch (RuntimeException ex) {
                Log.e("Inserting track", "Insert error " + ex.toString());
            }

            if (t.getSteps() != null) {
                List<Step> currentSteps = (List<Step>) t.getSteps();
                for (int i = 0; i < currentSteps.size(); i++) {
                    Step s = currentSteps.get(i);
                    if (s.getId() == null) {
                        s.setId(DataContainer.getStepId(dataBaseHelper, userId));
                    }
                    s.setTrack(t);

                    if (s.getHighlights() != null) {
                        for (HighLight h : s.getHighlights()) {
                            if (h.getId() == null) {
                                h.setId(DataContainer.getHighLightId(dataBaseHelper, userId));
                                Log.e("HIGHLIGHT MISSING ID", h.getName());

                            }
                            //	s.getHighlights().add(h);
                            h.setStep(s);

                            if (h.getReferences() != null) {

                                for (Reference ref : h.getReferences()) {
                                    try {
                                        dataBaseHelper.getReferenceDataDao().create(ref);
                                    } catch (RuntimeException ex) {
                                        Log.e("Inserting reference",
                                                "Insert error " + ex.toString());
                                    }
                                }

                            }
                            if (h.getInteractiveImages() != null) {

                                for (InteractiveImage ii : h.getInteractiveImages()) {

                                    if (ii.getBoxes() != null) {
                                        for (Box b : ii.getBoxes()) {
                                            try {
                                                dataBaseHelper.getBoxDataDao().create(b);
                                            } catch (RuntimeException ex) {
                                                Log.e("Inserting box",
                                                        "Insert error " + ex.toString());
                                            }

                                        }
                                    }

                                    try {
                                        dataBaseHelper.getInteractiveImageDataDao().create(ii);
                                    } catch (RuntimeException ex) {
                                        Log.e("Inserting ii",
                                                "Insert error " + ex.toString());
                                    }
                                }

                            }

                            try {
                                dataBaseHelper.getHlDataDao().create(h);

                            } catch (RuntimeException ex) {
                                Log.e("Inserting step",
                                        "Insert error " + ex.toString());
                            }
                        }
                    }
                    try {
                        dataBaseHelper.getStepDataDao().create(s);
                    } catch (RuntimeException ex) {
                        Log.e("Inserting step", "Insert error " + ex.toString());
                    }
                }
            }
        }
        if (editedRoute.getId() == null || editedRoute.getId().equals("")) {
            editedRoute.setId(DataContainer.getRouteId(dataBaseHelper, userId));
        }

        try {
            dataBaseHelper.getRouteDataDao().create(editedRoute);
        } catch (RuntimeException ex) {
            Log.e("Inserting route", "Insert error " + ex.toString());
        }
    }

}
