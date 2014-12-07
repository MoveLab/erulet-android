package net.movelab.sudeau;

import android.accounts.Account;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.FileManifest;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Reference;
import net.movelab.sudeau.model.Route;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class Switchboard extends FragmentActivity {

    private Button btn_manual;
    private Button btn_security;
    private Button btn_routes;
    private Button btn_credits;
    private EruletApp app;


    Context context;

    DataBaseHelper dataBaseHelper;

    ContentResolver cr;
    Cursor c;

    static final int REGISTRATION_REQUEST = 1;

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


        // check route size and force db updates at top
        List<Route> routes = DataContainer.getAllOfficialRoutes(app.getDataBaseHelper());

        //    if (PropertyHolder.isFirstTime()) {

        // check that auto id is working
        //  showWelcomeDialog();
        //  PropertyHolder.setFirstTime(false);

        // if first time is called after this update has been written no need for sync fix
//            PropertyHolder.setNeedsSyncFix(false);

        //      }


        if(!PropertyHolder.isSyncAlarmOn()){
        context.sendBroadcast( new Intent(
                context.getString(R.string.internal_message_id)
                        + Util.MESSAGE_START_SYNC));
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
                if (Util.hasMinimumContents(context, app)) {
                    Intent i = new Intent(Switchboard.this,
                            ChooseItineraryActivity.class);
                    startActivity(i);
                } else {
                    showStillSyncingDialog();
                }
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
