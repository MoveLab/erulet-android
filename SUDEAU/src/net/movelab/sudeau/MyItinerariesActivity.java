package net.movelab.sudeau;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MyItinerariesActivity extends Activity {

    //TODO Add date and time of creation of the route
    //TODO Modify layout so that it acommodates landscape orientation better
    private MyRouteArrayAdapter routeArrayAdapter;
    private ListView listView;
    private EruletApp app;
    private Context context;

    String locale;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_itineraries);

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        context = getApplication();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

        listView = (ListView) findViewById(R.id.lv_my_routes);
        String userId = PropertyHolder.getUserId();
        List<Route> myRoutes = loadRoutes(userId);

        routeArrayAdapter = new MyRouteArrayAdapter(this, locale, myRoutes, app);
        listView.setAdapter(routeArrayAdapter);

    }

    private List<Route> loadRoutes(String userId) {
        return DataContainer.getUserRoutes(app.getDataBaseHelper(), userId);
    }

    private void refreshListView(String userId) {
        List<Route> newRoutes = loadRoutes(userId);
        routeArrayAdapter = new MyRouteArrayAdapter(this, locale, newRoutes, app);
        listView.setAdapter(routeArrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userId = PropertyHolder.getUserId();
        refreshListView(userId);
    }


}

class MyRouteArrayAdapter extends ArrayAdapter<Route> {

    HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
    private final Context context;
    private List<Route> routes;
    private EruletApp app;
    //private Route currentRoute;
    String locale;
    public Route selectedRoute;


    public MyRouteArrayAdapter(Context context, String locale, List<Route> objects, EruletApp app) {
        super(context, R.layout.local_route_list_item, objects);
        for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
        }
        this.routes = objects;
        this.context = context;
        this.app = app;
        this.locale = locale;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.local_route_list_item, parent, false);
        TextView nom = (TextView) rowView.findViewById(R.id.route_name);
        TextView description = (TextView) rowView.findViewById(R.id.route_description);
        ImageButton edit = (ImageButton) rowView.findViewById(R.id.route_edit);
        ImageButton delete = (ImageButton) rowView.findViewById(R.id.route_delete);
        ImageButton upload = (ImageButton) rowView.findViewById(R.id.route_upload);
        final Route currentRoute = routes.get(position);
        nom.setText(currentRoute.getName(locale));
        description.setText(currentRoute.getDescription(locale));
        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("route check", "name_" + locale + " = " + currentRoute.getName(locale));
                Intent i = new Intent(context, EditRouteActivity.class);
                i.putExtra("routeId", currentRoute.getId());
                context.startActivity(i);
            }
        });
        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

                // Setting Dialog Title
                alertDialog.setTitle(app.getResources().getString(R.string.confirm_delete));

                // Setting Dialog Message
                alertDialog.setMessage(app.getResources().getString(R.string.about_to_delete) + "\n" + currentRoute.getName(locale) + "\n" + app.getResources().getString(R.string.really_continue));

                // Setting Icon to Dialog
                alertDialog.setIcon(R.drawable.ic_delete);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton(app.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        remove(currentRoute);
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton(app.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // 	Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();
            }
        });
        upload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedRoute = currentRoute;
                new ItineraryUploadTask().execute(context);

            }
        });

        return rowView;
    }

    @Override
    public void remove(Route object) {
        super.remove(object);
        DataContainer.deleteRouteCascade(object, app);
    }

    @Override
    public long getItemId(int position) {
        Route item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public class ItineraryUploadTask extends AsyncTask<Context, Integer, Boolean> {

        ProgressDialog prog;

        int myProgress;
        int resultFlag;

        int OFFLINE = 0;
        int UPLOAD_ERROR = 1;
        int DATABASE_ERROR = 2;
        int SUCCESS = 3;
        int JSON_ERROR = 4;

        HttpResponse response;
        JSONObject responseJson;
        int statusCode = -1;
        int server_id = -1;


        @Override
        protected void onPreExecute() {

            resultFlag = SUCCESS;

            prog = new ProgressDialog(context);
            prog.setTitle(context.getResources().getString(R.string.uploading));
            prog.setIndeterminate(false);
            prog.setMax(100);
            prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            prog.show();

            myProgress = 0;

        }

        protected Boolean doInBackground(Context... context) {

            // TODO deal with updating routes already uploaded. Since only flat route  content could change, post these to flat user route endpoint
            if(selectedRoute.getServerId()>=0){
            try {
                JSONObject this_route_json = JSONConverter.userRouteToServerJSONObject(selectedRoute, app);
                response = Util.postJSON(this_route_json, UtilLocal.URL_USER_ROUTES, context[0]);
                statusCode = Util.getResponseStatusCode(response);
                responseJson = Util.parseResponse(context[0], response);
                if (statusCode >= 200 && statusCode < 300) {
                    resultFlag = SUCCESS;
                    server_id = responseJson.optInt("server_id", -1);
                    selectedRoute.setServerId(server_id);
                    DataContainer.updateRoute(selectedRoute, app.getDataBaseHelper());

                    if (responseJson.has("track")) {
                        JSONObject this_track = responseJson.optJSONObject("track");
                        if (this_track.has("steps")) {
                            JSONArray these_steps = this_track.optJSONArray("steps");
                            for (int i = 0; i < these_steps.length(); i++) {
                                JSONObject this_step = these_steps.optJSONObject(i);
                                if (this_step != null && this_step.has("highlights")) {
                                    JSONArray these_highlights = this_step.getJSONArray("highlights");
                                    for (int j = 0; j < these_highlights.length(); j++) {
                                        JSONObject this_highlight = these_highlights.optJSONObject(j);
                                        if (this_highlight != null && this_highlight.has("id_on_creator_device") && this_highlight.has("server_id")) {
                                            int this_highlight_local_id = this_highlight.optInt("id_on_creator_device");
                                            int this_highlight_server_id = this_highlight.optInt("server_id");
                                            HighLight highlight_in_need_of_server_id = DataContainer.findHighLightById(this_highlight_local_id, app.getDataBaseHelper());
                                            if (highlight_in_need_of_server_id != null) {
                                                highlight_in_need_of_server_id.setServerId(this_highlight_server_id);
                                                DataContainer.updateHighLight(highlight_in_need_of_server_id, app.getDataBaseHelper());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    resultFlag = UPLOAD_ERROR;
                }
            } catch (JSONException e) {
                resultFlag = JSON_ERROR;
            }


            int media_post_response = 0;
            // now highlight media
            Collection<HighLight> these_highlights = selectedRoute.getHighlights(app.getDataBaseHelper());
            for (HighLight h : these_highlights) {
                DataContainer.refreshHighlightForFileManifest(h, app.getDataBaseHelper());
                FileManifest this_fm = h.getFileManifest();
                File media_file = new File(this_fm.getPath());
                Log.i("media", this_fm.getPath());
                Log.i("media", media_file.getAbsolutePath());
                media_post_response = Util.postMedia(context[0], media_file.getAbsolutePath(), media_file.getName(), h.getServerId());
                if (media_post_response < 200 || media_post_response >= 300) {
                    resultFlag = UPLOAD_ERROR;
                }
            }
            }

            // trigger ratings uploads

            Intent ratingUploadIntent = new Intent(context[0], UploadRatings.class);
            context[0].startService(ratingUploadIntent);


            return true;
        }

        protected void onProgressUpdate(Integer... progress) {

            prog.setProgress(progress[0]);
        }

        protected void onPostExecute(Boolean result) {

            prog.dismiss();

            if (result && resultFlag == SUCCESS) {
                Util.toast(
                        context, context.getResources().getString(R.string.uploaded));
            } else {
                if (resultFlag == OFFLINE) {
                    buildCustomAlert(context.getResources().getString(R.string.offline_try_again));
                } else if (resultFlag == UPLOAD_ERROR) {
                    buildCustomAlert(context.getResources().getString(R.string.error_try_again) + responseJson.toString());
                    Log.e("upload error", responseJson.toString());
                } else if (resultFlag == DATABASE_ERROR) {
                    buildCustomAlert(context.getResources().getString(R.string.error_try_again) + responseJson.toString());
                    Log.e("database error", "error");
                } else if (resultFlag == JSON_ERROR) {
                    buildCustomAlert(context.getResources().getString(R.string.error_try_again) + responseJson.toString());
                    Log.e("json error", "error");
                }

            }

        }
    }


    public void buildCustomAlert(String message) {

        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.custom_alert);

        dialog.setCancelable(false);

        TextView alertText = (TextView) dialog.findViewById(R.id.alertText);
        alertText.setText(message);

        Button positive = (Button) dialog.findViewById(R.id.alertOK);
        Button negative = (Button) dialog.findViewById(R.id.alertCancel);
        negative.setVisibility(View.GONE);

        positive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.cancel();

            }
        });

        dialog.show();

    }


}
