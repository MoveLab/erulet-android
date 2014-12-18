package net.movelab.sudeau;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;


public class Switchboard extends FragmentActivity {

    private Button btn_manual;
    private Button btn_security;
    private Button btn_routes;
    private Button btn_credits;
    private EruletApp app;


    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        setContentView(R.layout.activity_switchboard);
        initButtons();

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);


        if(!PropertyHolder.isSyncAlarmOn()){
        context.sendBroadcast( new Intent(
                context.getString(R.string.internal_message_id)
                        + Util.MESSAGE_START_SYNC));
        }

        if(!PropertyHolder.isGoogleMapsOfflineReady() && Util.isOnline(context)){
        // do initial connect to Google Play Services so that map works offline
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status == ConnectionResult.SUCCESS) {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            if(supportMapFragment != null){
            GoogleMap gm = supportMapFragment.getMap();
            if(gm != null){
                PropertyHolder.setGoogleMapsOfflineReady(true);
                gm = null;
            }
            }
        }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PropertyHolder.getTripInProgressTracking() >= 0) {
            Intent intent = new Intent(Switchboard.this,
                    DetailItineraryActivity.class);
            startActivity(intent);
            finish();
        } else {
            getCoreData();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem hide_highlights_item = menu.findItem(R.id.hide_my_highlights);
        if (hide_highlights_item != null) {
            hide_highlights_item.setVisible(false);
        }
        MenuItem hide_routes_item = menu.findItem(R.id.hide_my_routes);
        if (hide_routes_item != null) {
            hide_routes_item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                Intent i = new Intent(Switchboard.this, EruletPreferencesActivity.class);
                startActivity(i);
                break;
            case R.id.account:
                startActivity(new Intent(Switchboard.this, AccountActivity.class));
                   break;
            case R.id.holet_routes:
                Intent officialItineraryIntent = new Intent(Switchboard.this,
                        OfficialItinerariesActivity.class);
                startActivity(officialItineraryIntent);
                break;
            case R.id.my_routes:
                Intent i1 = new Intent(Switchboard.this,
                        MyItinerariesActivity.class);
                startActivity(i1);
                break;
            case R.id.survey:
                Intent survey_intent = new Intent(Switchboard.this, SurveyActivity.class);
                survey_intent.putExtra(SurveyActivity.SURVEY_TYPE_KEY, "general_survey");
                startActivity(survey_intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initButtons() {
        btn_manual = (Button) findViewById(R.id.btn_manual);
        btn_manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Intent i = new Intent(Switchboard.this, ManualActivity.class);
                Intent i = new Intent(Switchboard.this, ManualActivity.class);
                startActivity(i);
            }
        });
        btn_security = (Button) findViewById(R.id.btn_security_info);
        btn_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Switchboard.this,
                        BeforeLeaving.class);
                startActivity(i);
            }
        });
        btn_routes = (Button) findViewById(R.id.btn_routes);
        btn_routes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                    Intent i = new Intent(Switchboard.this,
                            ChooseItineraryActivity.class);
                    startActivity(i);
            }
        });
        btn_credits = (Button) findViewById(R.id.btn_credits);
        btn_credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(Switchboard.this,
                        CreditsActivity.class);
                startActivity(i);
            }
        });

    }


    private void showStillSyncingDialog() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                Switchboard.this);
        builderSingle.setIcon(R.drawable.ic_launcher);
        builderSingle.setTitle(getResources().getString(R.string.still_syncing_title));
        builderSingle.setMessage(getResources().getString(R.string.still_syncing_message));
        builderSingle.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builderSingle.setNeutralButton(getResources().getString(R.string.sync_later),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Switchboard.this,
                                ChooseItineraryActivity.class);
                        startActivity(i);
                    }
                });
        builderSingle.setPositiveButton(getResources().getString(R.string.sync_now),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startInitialSync();
                    }
                });

        builderSingle.show();
    }

    private void showWelcomeDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(
                Switchboard.this);
        b.setIcon(R.drawable.ic_launcher);
        b.setTitle(getResources().getString(R.string.welcome_title));
        b.setMessage(getResources().getString(R.string.welcome_message));
        b.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startInitialSync();
                dialogInterface.dismiss();
            }
        });
        b.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        b.show();
    }


    private void getCoreData() {
        Intent coreDataDownloadIntent = new Intent(Switchboard.this, DownloadCoreData.class);
        startService(coreDataDownloadIntent);

    }

    private void startInitialSync() {

        if (Util.isOnline(context)) {
            getCoreData();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.internet_needed_title));
            builder.setIcon(R.drawable.ic_erulet_new);
            builder.setMessage(getResources().getString(R.string.internet_needed_message_content));
            builder.setCancelable(true);
            builder.show();
        }

    }

}
