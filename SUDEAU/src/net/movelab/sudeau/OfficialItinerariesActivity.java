package net.movelab.sudeau;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.RBB;
import net.movelab.sudeau.model.Route;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class OfficialItinerariesActivity extends Activity {

    private OfficialRouteArrayAdapter routeArrayAdapter;
    private ListView listView;
    private EruletApp app;
    private Context context;

    private ImageButton coreDataDownloadStart;
    private ImageButton coreDataDownloadCancel;
    private ImageButton coreDataDownloadRefresh;
    private ProgressBar coreDataProgressBar;
    private TextView coreDataLabel;

    private Intent startCoreDataDownloadIntent;

    String locale;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.official_itineraries);

        coreDataDownloadStart = (ImageButton) findViewById(R.id.core_data_download_start_button);
        coreDataDownloadRefresh = (ImageButton) findViewById(R.id.core_data_download_refresh_button);
        coreDataDownloadCancel = (ImageButton) findViewById(R.id.core_data_download_cancel_button);
        coreDataProgressBar = (ProgressBar) findViewById(R.id.core_data_progressBar);
        coreDataLabel = (TextView) findViewById(R.id.core_data_label);

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        context = getApplication();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

        listView = (ListView) findViewById(R.id.itinerary_list);
        List<Route> routes = loadRoutes();

        routeArrayAdapter = new OfficialRouteArrayAdapter(this, locale, routes, app);
        listView.setAdapter(routeArrayAdapter);

        setUpCoreDataButtonListeners();
        setUpCoreDataStatus();


        IntentFilter coreDataResponseFilter = new IntentFilter(Util.INTENT_CODE_CORE_DATE_RESPONSE);

        IntentFilter routeContentResponseFilter = new IntentFilter(Util.INTENT_CODE_ROUTE_CONTENT_RESPONSE);

        DownloadResultBroadcastReceiver mDownloadResultBroadcastReceiver =
                new DownloadResultBroadcastReceiver();
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadResultBroadcastReceiver,
                coreDataResponseFilter);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadResultBroadcastReceiver,
                routeContentResponseFilter);


    }


    public class DownloadResultBroadcastReceiver extends BroadcastReceiver {

        // Prevents instantiation
        private DownloadResultBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {

            String this_action = intent.getAction();

            if (this_action != null && this_action.equals(Util.INTENT_CODE_CORE_DATE_RESPONSE)) {

                coreDataProgressBar.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.GONE);

                int response_code = intent.getIntExtra(DownloadCoreData.OUTGOING_MESSAGE_KEY_RESPONSE_CODE, -1);
                if (response_code == DownloadCoreData.RESPONSE_CODE_FAIL) {

                    if (PropertyHolder.getLastUpdateGeneralMap() > 0L && PropertyHolder.getLastUpdateGeneralReferences() >= 0L) {
                        coreDataDownloadStart.setVisibility(View.GONE);
                        coreDataDownloadRefresh.setVisibility(View.VISIBLE);
                    } else {
                        coreDataDownloadStart.setVisibility(View.VISIBLE);
                        coreDataDownloadRefresh.setVisibility(View.GONE);
                    }


                } else if (response_code == DownloadCoreData.RESPONSE_CODE_SUCCESS) {
                    coreDataDownloadStart.setVisibility(View.GONE);
                    coreDataDownloadRefresh.setVisibility(View.VISIBLE);
                }

                refreshListView();

            } else if (this_action != null && this_action.contains(Util.INTENT_CODE_ROUTE_CONTENT_RESPONSE)) {
                // TODO check if better way to refresh list buttons - as is, they are based on propertyholder values.
                refreshListView();
            }
        }
    }

    private void setUpCoreDataButtonListeners() {

        coreDataDownloadCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startCoreDataDownloadIntent != null) {
                    stopService(startCoreDataDownloadIntent);
                    startCoreDataDownloadIntent = null;
                }

                PropertyHolder.setCoreDataStatus(PropertyHolder.STATUS_CODE_MISSING);

                coreDataProgressBar.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.GONE);
                if (PropertyHolder.getLastUpdateGeneralMap() > 0L && PropertyHolder.getLastUpdateGeneralReferences() >= 0L) {
                    coreDataDownloadStart.setVisibility(View.GONE);
                    coreDataDownloadRefresh.setVisibility(View.VISIBLE);
                } else {
                    coreDataDownloadStart.setVisibility(View.VISIBLE);
                    coreDataDownloadRefresh.setVisibility(View.GONE);
                }

            }

        });

        coreDataDownloadStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCoreDataDownloadIntent = new Intent(OfficialItinerariesActivity.this, DownloadCoreData.class);
                startService(startCoreDataDownloadIntent);

                coreDataProgressBar.setVisibility(View.VISIBLE);
                coreDataDownloadCancel.setVisibility(View.VISIBLE);
                coreDataDownloadStart.setVisibility(View.GONE);
                coreDataDownloadRefresh.setVisibility(View.GONE);

            }
        });

        coreDataDownloadRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startCoreDataDownloadIntent = new Intent(OfficialItinerariesActivity.this, DownloadCoreData.class);
                startService(startCoreDataDownloadIntent);

                coreDataProgressBar.setVisibility(View.VISIBLE);
                coreDataDownloadCancel.setVisibility(View.VISIBLE);
                coreDataDownloadStart.setVisibility(View.GONE);
                coreDataDownloadRefresh.setVisibility(View.GONE);

            }
        });


    }

    private void setUpCoreDataStatus() {

        int core_data_status = PropertyHolder.getCoreDataStatus();
        boolean core_data_available = PropertyHolder.getLastUpdateGeneralMap() > 0L && PropertyHolder.getLastUpdateGeneralReferences() >= 0L;

        if (core_data_available) {
            coreDataLabel.setTextColor(Color.WHITE);
        } else {
            coreDataLabel.setTextColor(Color.GRAY);
        }

        switch (core_data_status) {
            case PropertyHolder.STATUS_CODE_DOWNLOADING:
                coreDataProgressBar.setVisibility(View.VISIBLE);
                coreDataDownloadStart.setVisibility(View.GONE);
                coreDataDownloadRefresh.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.VISIBLE);
                break;
            case PropertyHolder.STATUS_CODE_MISSING:
                coreDataProgressBar.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.GONE);
                if (core_data_available) {
                    coreDataDownloadStart.setVisibility(View.GONE);
                    coreDataDownloadRefresh.setVisibility(View.VISIBLE);
                } else {
                    coreDataDownloadStart.setVisibility(View.VISIBLE);
                    coreDataDownloadRefresh.setVisibility(View.GONE);
                }
                break;
            case PropertyHolder.STATUS_CODE_QUEUED:
                coreDataProgressBar.setVisibility(View.VISIBLE);
                coreDataDownloadStart.setVisibility(View.GONE);
                coreDataDownloadRefresh.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.VISIBLE);
                break;
            case PropertyHolder.STATUS_CODE_READY:
                coreDataProgressBar.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.GONE);
                if (core_data_available) {
                    coreDataDownloadStart.setVisibility(View.GONE);
                    coreDataDownloadRefresh.setVisibility(View.VISIBLE);
                } else {
                    coreDataDownloadStart.setVisibility(View.VISIBLE);
                    coreDataDownloadRefresh.setVisibility(View.GONE);
                }
                break;
            default:
                coreDataProgressBar.setVisibility(View.GONE);
                coreDataDownloadCancel.setVisibility(View.GONE);
                if (core_data_available) {
                    coreDataDownloadStart.setVisibility(View.GONE);
                    coreDataDownloadRefresh.setVisibility(View.VISIBLE);
                } else {
                    coreDataDownloadStart.setVisibility(View.VISIBLE);
                    coreDataDownloadRefresh.setVisibility(View.GONE);
                }
                break;
        }
    }

    private List<Route> loadRoutes() {
        return DataContainer.getAllOfficialRoutes(app.getDataBaseHelper());
    }

    public void refreshListView() {
        List<Route> newRoutes = loadRoutes();
        routeArrayAdapter = new OfficialRouteArrayAdapter(this, locale, newRoutes, app);
        listView.setAdapter(routeArrayAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshListView();
        setUpCoreDataStatus();
    }


}

class OfficialRouteArrayAdapter extends ArrayAdapter<Route> {

    HashMap<Route, Integer> mIdMap = new HashMap<Route, Integer>();
    private final Context context;
    private List<Route> routes;
    private EruletApp app;
    //private Route currentRoute;
    String locale;
    public Route selectedRoute;


    public OfficialRouteArrayAdapter(Context context, String locale, List<Route> objects, EruletApp app) {
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
        View rowView = inflater.inflate(R.layout.official_itineraries_item, parent, false);
        if (rowView != null) {
            final TextView nom = (TextView) rowView.findViewById(R.id.route_name);
            final ImageButton download_start = (ImageButton) rowView.findViewById(R.id.download_start_button);
            final ImageButton download_cancel = (ImageButton) rowView.findViewById(R.id.download_cancel_button);
            final ImageButton download_refresh = (ImageButton) rowView.findViewById(R.id.download_refresh_button);
            final ImageButton delete = (ImageButton) rowView.findViewById(R.id.delete_button);
            final ProgressBar progress_bar = (ProgressBar) rowView.findViewById(R.id.progress_bar);


            final Route currentRoute = routes.get(position);
            nom.setText(currentRoute.getName(locale));
            setUpRouteContentStatus(currentRoute, nom, progress_bar, download_start, download_refresh, download_cancel, delete);
            nom.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showItineraryOptions(currentRoute, progress_bar, download_start, download_cancel, download_refresh, delete);
                }
            });
            delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PropertyHolder.setRouteContentStatus(currentRoute.getId(), PropertyHolder.STATUS_CODE_CANCELLED);
                    progress_bar.setVisibility(View.GONE);
                    download_cancel.setVisibility(View.GONE);
                    download_start.setVisibility(View.VISIBLE);
                    download_refresh.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    nom.setTextColor(Color.GRAY);


                    // TODO make this more generalizable and robust to changes in directory structure.
                    Util.deleteRecursive(new File(Environment.getExternalStorageDirectory().getPath(), Util.baseFolder + "/" + Util.routeMediaFolder + "/route_" + currentRoute.getServerId()));

                    Util.deleteRecursive(new File(currentRoute.getLocalCarto()));

                    currentRoute.setLocalCartoLastUpdated(0L);
                    currentRoute.setRouteContentLastUpdated(0L);
                    DataContainer.updateRoute(currentRoute, app.getDataBaseHelper());

                }
            });
            download_start.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent startRouteContentDownloadIntent = new Intent(context, DownloadRouteContent.class);
                    startRouteContentDownloadIntent.putExtra(DownloadRouteContent.OUTGOING_MESSAGE_KEY_ROUTE_ID, currentRoute.getId());
                    context.startService(startRouteContentDownloadIntent);

                    progress_bar.setVisibility(View.VISIBLE);
                    download_cancel.setVisibility(View.VISIBLE);
                    download_start.setVisibility(View.GONE);
                    download_refresh.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);


                }
            });
            download_cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    PropertyHolder.setRouteContentStatus(currentRoute.getId(), PropertyHolder.STATUS_CODE_CANCELLED);

                    progress_bar.setVisibility(View.GONE);
                    download_cancel.setVisibility(View.GONE);

                    if (currentRoute.getLocalCartoLastUpdated() > 0L && currentRoute.getRouteContentLastUpdated() > 0L) {
                        download_start.setVisibility(View.GONE);
                        download_refresh.setVisibility(View.VISIBLE);
                        delete.setVisibility(View.VISIBLE);

                    } else {
                        download_start.setVisibility(View.VISIBLE);
                        download_refresh.setVisibility(View.GONE);
                        delete.setVisibility(View.GONE);

                    }

                }
            });
            download_refresh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent startRouteContentDownloadIntent = new Intent(context, DownloadRouteContent.class);
                    startRouteContentDownloadIntent.putExtra(DownloadRouteContent.OUTGOING_MESSAGE_KEY_ROUTE_ID, currentRoute.getId());
                    context.startService(startRouteContentDownloadIntent);

                    progress_bar.setVisibility(View.VISIBLE);
                    download_cancel.setVisibility(View.VISIBLE);
                    download_start.setVisibility(View.GONE);
                    download_refresh.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);

                }
            });
        }
        return rowView;
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


    private void setUpRouteContentStatus(Route route, TextView title, ProgressBar progress_bar, ImageButton start_button, ImageButton refresh_button, ImageButton cancel_button, ImageButton delete_button) {

        int route_content_status = PropertyHolder.getRouteContentStatus(route.getId());
        boolean route_content_available = route.getLocalCartoLastUpdated() > 0L && route.getRouteContentLastUpdated() > 0L;

        if (route_content_available) {
            title.setTextColor(Color.WHITE);
        } else {
            title.setTextColor(Color.GRAY);
        }

        switch (route_content_status) {
            case PropertyHolder.STATUS_CODE_DOWNLOADING:
                progress_bar.setVisibility(View.VISIBLE);
                start_button.setVisibility(View.GONE);
                refresh_button.setVisibility(View.GONE);
                cancel_button.setVisibility(View.VISIBLE);
                delete_button.setVisibility(View.GONE);
                break;
            case PropertyHolder.STATUS_CODE_MISSING:
                progress_bar.setVisibility(View.GONE);
                cancel_button.setVisibility(View.GONE);
                if (route_content_available) {
                    start_button.setVisibility(View.GONE);
                    refresh_button.setVisibility(View.VISIBLE);
                    delete_button.setVisibility(View.VISIBLE);
                } else {
                    start_button.setVisibility(View.VISIBLE);
                    refresh_button.setVisibility(View.GONE);
                    delete_button.setVisibility(View.GONE);
                }
                break;
            case PropertyHolder.STATUS_CODE_QUEUED:
                progress_bar.setVisibility(View.VISIBLE);
                start_button.setVisibility(View.GONE);
                refresh_button.setVisibility(View.GONE);
                cancel_button.setVisibility(View.VISIBLE);
                delete_button.setVisibility(View.GONE);
                break;
            case PropertyHolder.STATUS_CODE_READY:
                progress_bar.setVisibility(View.GONE);
                cancel_button.setVisibility(View.GONE);
                if (route_content_available) {
                    start_button.setVisibility(View.GONE);
                    refresh_button.setVisibility(View.VISIBLE);
                    delete_button.setVisibility(View.VISIBLE);
                } else {
                    start_button.setVisibility(View.VISIBLE);
                    refresh_button.setVisibility(View.GONE);
                    delete_button.setVisibility(View.GONE);
                }
                break;
            default:
                progress_bar.setVisibility(View.GONE);
                cancel_button.setVisibility(View.GONE);
                if (route_content_available) {
                    start_button.setVisibility(View.GONE);
                    refresh_button.setVisibility(View.VISIBLE);
                    delete_button.setVisibility(View.VISIBLE);

                } else {
                    start_button.setVisibility(View.VISIBLE);
                    refresh_button.setVisibility(View.GONE);
                    delete_button.setVisibility(View.GONE);
                }
                break;
        }
    }

    public void showItineraryOptions(Route route, ProgressBar pg, ImageButton ds, ImageButton dc, ImageButton dr, ImageButton d){
        String OPTION_1 = context.getString(R.string.trip_option_1);
        String OPTION_2 = context.getString(R.string.trip_option_2);
        final int this_route_id = route.getId();
        final ProgressBar progress_bar = pg;
        final ImageButton download_start = ds;
        final ImageButton download_cancel = dc;
        final ImageButton download_refresh = dr;
        final ImageButton delete = d;

        CharSequence[] items = null;
            items = new CharSequence[]{OPTION_1,OPTION_2};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(route.getName(locale));
        builder.setIcon(R.drawable.ic_pin_info);
        if(route.getGlobalRating()>=0)
            builder.setMessage(Html.fromHtml(route.getDescription(locale) + "<br><br><b>" + context.getString(R.string.average_rating) + ":" + Float.toString(route.getGlobalRating()) + "</b>"));
        else
            builder.setMessage(route.getDescription(locale));
        if(PropertyHolder.getRouteContentStatus(route.getId()) == PropertyHolder.STATUS_CODE_READY){
            builder.setNegativeButton(context.getString(R.string.trip_option_1), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(context,
                            DetailItineraryActivity.class);
                    // rbb[0] is route id as string
                    intent.putExtra("idRoute", this_route_id);
                    intent.putExtra("mode",0);
                    dialogInterface.dismiss();
                    context.startActivity(intent);				                }
            });
            builder.setNeutralButton(context.getString(R.string.trip_option_2), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(context,
                            DetailItineraryActivity.class);
                    // rbb[0] is route ID as string
                    intent.putExtra("idRoute", this_route_id);
                    intent.putExtra("mode",1);
                    dialogInterface.dismiss();
                    ((Activity)context).finish();
                    context.startActivity(intent);
                }
            });
        } else{
            builder.setNeutralButton(context.getString(R.string.download), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent startRouteContentDownloadIntent = new Intent(context, DownloadRouteContent.class);
                    startRouteContentDownloadIntent.putExtra(DownloadRouteContent.OUTGOING_MESSAGE_KEY_ROUTE_ID, this_route_id);
                    context.startService(startRouteContentDownloadIntent);
                    progress_bar.setVisibility(View.VISIBLE);
                    download_cancel.setVisibility(View.VISIBLE);
                    download_start.setVisibility(View.GONE);
                    download_refresh.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                    dialogInterface.dismiss();
                }
            });
        }

        builder.show();
    }


}
