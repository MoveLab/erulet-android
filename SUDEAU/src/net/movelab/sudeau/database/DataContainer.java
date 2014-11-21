package net.movelab.sudeau.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.movelab.sudeau.EruletApp;
import net.movelab.sudeau.model.Box;
//import net.movelab.sudeau.model.EruMedia;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;
import net.movelab.sudeau.model.Track;

import android.content.SharedPreferences;
import android.util.Log;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class DataContainer {

    /**
     * UUID for Route
     *
     * @return
     */
    public static String getRouteId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Highlight UUID
     *
     * @return
     */
    public static String getHighLightId() {
        return UUID.randomUUID().toString();
    }

    private static List<Integer> getHighLightIds(List<HighLight> userHighLights) {
        ArrayList<Integer> retVal = new ArrayList<Integer>();
        for (int i = 0; i < userHighLights.size(); i++) {
            retVal.add(userHighLights.get(i).getId());
        }
        return retVal;
    }

    private static List<Integer> getRouteIds(List<Route> userRoutes) {
        ArrayList<Integer> retVal = new ArrayList<Integer>();
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
                                         int routeBasedOnId) {
        String idTrack = getTrackId();
        Track t = new Track();
        db.getTrackDataDao().create(t);
        Route r = new Route();
        r.setIdRouteBasedOn(routeBasedOnId);
        // TODO put into localized strings
        r.setName(lang, "La meva ruta");
        r.setDescription(lang, "La meva descripcio");
        r.setUserId(userId);
        r.setTrack(t);
        db.getRouteDataDao().create(r);
        return r;
    }

    public static FileManifest createFileManifest(String path, DataBaseHelper db) {
        FileManifest fm = new FileManifest();
        fm.setPath(path);
        db.getFileManifestDataDao().create(fm);
        return fm;
    }


    public static void addStepToTrack(Step s, Track t, String userId,
                                      DataBaseHelper db) {
        // Track is already created
        s.setTrack(t);
        String stepId = getStepId();
        db.getStepDataDao().create(s);
// This is causing problems -- and according to ormlite docs, adding s to t after s has been created is a mistake. Just add t to s as above.
//        t.getSteps().add(s);
    }

    public static void addHighLightToStep(Step s, HighLight h, String userId,
                                          DataBaseHelper db) {
        // Step already exists
        h.setStep(s);
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
        if (hl != null && db != null) {
            db.getFileManifestDataDao().refresh(hl.getFileManifest());
        } else {
        }
        return hl;
    }


    public static InteractiveImage refreshInteractiveImageForFileManifest(InteractiveImage ii, DataBaseHelper db) {
        db.getFileManifestDataDao().refresh(ii.getFileManifest());
        return ii;
    }

    public static List<FileManifest> getReferenceFiles(Reference reference, DataBaseHelper db) {
        db.getReferenceDataDao().refresh(reference);
        ArrayList<FileManifest> retVal = new ArrayList<FileManifest>();
        Iterator<FileManifest> fmIt = reference.getFileManifests().iterator();
        while (fmIt.hasNext()) {
            FileManifest f = fmIt.next();
            retVal.add(f);
        }
        return retVal;
    }

    public static Step refreshStepForTrack(Step s, DataBaseHelper db) {
        db.getTrackDataDao().refresh(s.getTrack());
        return s;
    }

    public static Route refreshRouteForTrack(Route r, DataBaseHelper db) {
        db.getTrackDataDao().refresh(r.getTrack());
        return r;
    }

    public static Track refreshTrackForRoute(Track t, DataBaseHelper db) {
        db.getRouteDataDao().refresh(t.getRoute());
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
        // TODO check why we are storing route id in shared preferences to begin with
        SharedPreferences.Editor ed = app.getPrefsEditor();
        ed.remove("" + r.getId());
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
        ed.remove("" + h.getId());
        ed.commit();
    }

    /**
     * Track UUID
     *
     * @return
     */
    public static String getTrackId() {
        return UUID.randomUUID().toString();
    }

    private static List<Integer> getTrackIds(List<Track> tracks) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
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

    public static String getStepId() {
        return UUID.randomUUID().toString();
    }

    private static List<Integer> getStepIds(List<Step> steps) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (int i = 0; i < steps.size(); i++) {
            ids.add(steps.get(i).getId());
        }
        return ids;
    }

    public static InteractiveImage findInteractiveImageById(int idImage,
                                                            DataBaseHelper db) {
        InteractiveImage i = db.getInteractiveImageDataDao()
                .queryForId(idImage);
        return i;
    }


    public static HighLight findHighLightById(int idHighLight, DataBaseHelper db) {
        HighLight h = db.getHlDataDao().queryForId(idHighLight);
        return h;
    }

    public static Route findRouteById(int idRoute, DataBaseHelper db) {
        Route r = db.getRouteDataDao().queryForId(idRoute);
        return r;
    }

   public static List<Route> findRelatedRoutesById(int idRoute, DataBaseHelper db) {
       List<Route> result = new ArrayList<Route>();
       QueryBuilder<Route, Integer> queryBuilder = db.getRouteDataDao()
               .queryBuilder();
       Where<Route, Integer> where = queryBuilder.where();
       try {
           where.eq("idRouteBasedOn", idRoute);
           PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
           result = db.getRouteDataDao().query(preparedQuery);
           Log.i("related routes,", "ran query. result size: " + result.size());
           return result;
       } catch (SQLException e) {
           Log.i("related routes,", "exception in findRelatedRoutesById");

           // TODO Auto-generated catch block
           e.printStackTrace();
       }
       return result;
   }

    public static Step findStepById(int idStep, DataBaseHelper db) {
        Step s = db.getStepDataDao().queryForId(idStep);
        return s;
    }

    public static Reference findReferenceById(int idReference,
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
        QueryBuilder<Route, Integer> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where<Route, Integer> where = queryBuilder.where();
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
        QueryBuilder<Route, Integer> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where<Route, Integer> where = queryBuilder.where();
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

    public static List<Route> getRoutesWithRatingsNotUploaded(DataBaseHelper db) {
        List<Route> routes = new ArrayList<Route>();
        QueryBuilder<Route, Integer> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where<Route, Integer> where = queryBuilder.where();
        try {
            where.eq("userRatingUploaded", false);
            where.and();
            where.ge("userRating", 0);
            PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
            routes = db.getRouteDataDao().query(preparedQuery);
            return routes;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return routes;
    }

    public static List<HighLight> getHighlightsWithRatingsNotUploaded(DataBaseHelper db) {
        List<HighLight> highlights = new ArrayList<HighLight>();
        QueryBuilder<HighLight, Integer> queryBuilder = db.getHlDataDao()
                .queryBuilder();
        Where<HighLight, Integer> where = queryBuilder.where();
        try {
            where.eq("userRatingUploaded", false);
            where.and();
            where.ge("userRating", 0);
            PreparedQuery<HighLight> preparedQuery = queryBuilder.prepare();
            highlights = db.getHlDataDao().query(preparedQuery);
            return highlights;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return highlights;
    }



    public static List<String[]> getAllRoutesBareBones(DataBaseHelper db, String locale) {
        List<String[]> results = null;
        GenericRawResults<String[]> rawResults;
        if (locale.equals("es")) {
            rawResults = db.getRouteDataDao().queryRaw("select id,trackId,name_es,description_es  from route");
        } else if (locale.equals("ca")) {
            rawResults = db.getRouteDataDao().queryRaw("select id,trackId,name_ca,description_ca  from route");
        } else if (locale.equals("fr")) {
            rawResults = db.getRouteDataDao().queryRaw("select id,trackId,name_fr,description_fr  from route");
        } else if (locale.equals("en")) {
            rawResults = db.getRouteDataDao().queryRaw("select id,trackId,name_en,description_en  from route");
        } else {
            rawResults = db.getRouteDataDao().queryRaw("select id,trackId,name_oc,description_oc  from route");
        }
        try {
             results = rawResults.getResults();
        } catch (SQLException e) {
// TODO
        }
        return results;
    }


    public static Route findRouteByServerId(int server_id, DataBaseHelper db) {
        Route result = null;
        List<Route> resultList = new ArrayList<Route>();
        QueryBuilder<Route, Integer> queryBuilder = db.getRouteDataDao()
                .queryBuilder();
        Where where = queryBuilder.where();
        try {
            where.eq("server_id", server_id);
            PreparedQuery<Route> preparedQuery = queryBuilder.prepare();
            resultList = db.getRouteDataDao().query(preparedQuery);
            if (resultList != null && resultList.size() > 0) {
                result = resultList.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Step findStepByServerId(int server_id, DataBaseHelper db) {
        Step result = null;
        List<Step> resultList = new ArrayList<Step>();
        QueryBuilder<Step, Integer> queryBuilder = db.getStepDataDao()
                .queryBuilder();
        Where where = queryBuilder.where();
        try {
            where.eq("server_id", server_id);
            PreparedQuery<Step> preparedQuery = queryBuilder.prepare();
            resultList = db.getStepDataDao().query(preparedQuery);
            if (resultList != null && resultList.size() > 0) {
                result = resultList.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Reference findReferenceByServerId(int server_id, DataBaseHelper db) {
        Reference result = null;
        List<Reference> resultList = new ArrayList<Reference>();
        QueryBuilder<Reference, Integer> queryBuilder = db.getReferenceDataDao()
                .queryBuilder();
        Where where = queryBuilder.where();
        try {
            where.eq("server_id", server_id);
            PreparedQuery<Reference> preparedQuery = queryBuilder.prepare();
            resultList = db.getReferenceDataDao().query(preparedQuery);
            if (resultList != null && resultList.size() > 0) {
                result = resultList.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static InteractiveImage findInteractiveImageByServerId(int server_id, DataBaseHelper db) {
        InteractiveImage result = null;
        List<InteractiveImage> resultList = new ArrayList<InteractiveImage>();
        QueryBuilder<InteractiveImage, Integer> queryBuilder = db.getInteractiveImageDataDao()
                .queryBuilder();
        Where where = queryBuilder.where();
        try {
            where.eq("server_id", server_id);
            PreparedQuery<InteractiveImage> preparedQuery = queryBuilder.prepare();
            resultList = db.getInteractiveImageDataDao().query(preparedQuery);
            if (resultList != null && resultList.size() > 0) {
                result = resultList.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static Track findTrackByServerId(int server_id, DataBaseHelper db) {
        Track result = null;
        List<Track> resultList = new ArrayList<Track>();
        QueryBuilder<Track, Integer> queryBuilder = db.getTrackDataDao()
                .queryBuilder();
        Where where = queryBuilder.where();
        try {
            where.eq("server_id", server_id);
            PreparedQuery<Track> preparedQuery = queryBuilder.prepare();
            resultList = db.getTrackDataDao().query(preparedQuery);
            if (resultList != null && resultList.size() > 0) {
                result = resultList.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public static HighLight findHighlightByServerId(int server_id, DataBaseHelper db) {
        HighLight result = null;
        List<HighLight> resultList;
        QueryBuilder<HighLight, Integer> queryBuilder = db.getHlDataDao()
                .queryBuilder();
        Where<HighLight, Integer> where = queryBuilder.where();
        try {
            where.eq("server_id", server_id);
            PreparedQuery<HighLight> preparedQuery = queryBuilder.prepare();
            resultList = db.getHlDataDao().query(preparedQuery);
            if (resultList != null && resultList.size() > 0) {
                result = resultList.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
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

    public static Step getRouteStarterFast(String trackId, DataBaseHelper db) {
        try {
            List<Step> steps = db.getStepDataDao().query(
                    db.getStepDataDao().queryBuilder().where()
                            .eq("trackId", trackId).and().eq("order", 1).prepare());
            if (steps != null && steps.size() > 0) {
                return steps.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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


    public static List<InteractiveImage> getHighlightIIs(HighLight h, DataBaseHelper db) {
        db.getHlDataDao().refresh(h);
        ArrayList<InteractiveImage> retVal = new ArrayList<InteractiveImage>();
        Iterator<InteractiveImage> iis = h.getInteractiveImages().iterator();
        while (iis.hasNext()) {
            InteractiveImage this_ii = iis.next();
            retVal.add(this_ii);
        }
        return retVal;
    }

    public static List<Reference> getHighlightReferences(HighLight h, DataBaseHelper db) {
        db.getHlDataDao().refresh(h);
        ArrayList<Reference> retVal = new ArrayList<Reference>();
        Iterator<Reference> refs = h.getReferences().iterator();
        while (refs.hasNext()) {
            Reference this_reference = refs.next();
            retVal.add(this_reference);
        }
        return retVal;
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
        Log.d("data container edit route", "top");
        Log.d("data container edit route", "editedRoute getNameOC " + r.getName("oc"));
        dataBaseHelper.getRouteDataDao().update(r);
    }

    public static void updateReference(Reference ref, DataBaseHelper dataBaseHelper) {
        dataBaseHelper.getReferenceDataDao().update(ref);
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
            if (t.getName() == null || t.getName().equals("")) {
                //TODO change this once all track languages are in. Should be a line for each language.
                t.setName(editedRoute.getName("ca"));
            }
            try {
                dataBaseHelper.getTrackDataDao().createOrUpdate(t);
            } catch (RuntimeException ex) {
            }

            if (t.getSteps() != null) {
                List<Step> currentSteps = (List<Step>) t.getSteps();
                for (int i = 0; i < currentSteps.size(); i++) {
                    Step s = currentSteps.get(i);
                    s.setTrack(t);

                    try {
                        dataBaseHelper.getStepDataDao().createOrUpdate(s);

                    } catch (RuntimeException ex) {
                    }
                    if (s.getHighlights() != null) {
                        for (HighLight h : s.getHighlights()) {
                            //	s.getHighlights().add(h);
                            h.setStep(s);

                            try {
                                dataBaseHelper.getHlDataDao().createOrUpdate(h);


                            } catch (RuntimeException ex) {
                            }
                            if (h.getReferences() != null) {

                                for (Reference ref : h.getReferences()) {
                                    ref.setHighlight(h);
                                    try {
                                        dataBaseHelper.getReferenceDataDao().createOrUpdate(ref);
                                    } catch (RuntimeException ex) {
                                    }

                                    if (ref.getFileManifests() != null) {
                                        for (FileManifest fm : ref.getFileManifests()) {
                                            fm.setReference(ref);
                                            dataBaseHelper.getFileManifestDataDao().createOrUpdate(fm);
                                        }
                                    }
                                }

                            }
                            if (h.getInteractiveImages() != null) {

                                for (InteractiveImage ii : h.getInteractiveImages()) {
                                    ii.setHighlight(h);
                                    try {
                                        dataBaseHelper.getInteractiveImageDataDao().createOrUpdate(ii);
                                    } catch (RuntimeException ex) {
                                    }

                                    if (ii.getBoxes() != null) {
                                        for (Box b : ii.getBoxes()) {

                                            b.setInteractiveImage(ii);

                                            try {
                                                dataBaseHelper.getBoxDataDao().createOrUpdate(b);
                                            } catch (RuntimeException ex) {
                                            }

                                        }
                                    }

                                }

                            }


                        }
                    }

                }
            }
        }

        try {
            dataBaseHelper.getRouteDataDao().createOrUpdate(editedRoute);
        } catch (RuntimeException ex) {
        }
    }

}
