package net.movelab.sudeau;

import java.util.Hashtable;

import org.json.JSONException;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.RouteInfoFormatter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class EditRouteActivity extends Activity {

    private Route editedRoute;
    private EditText routeName;
    private EditText routeDescription;
    private EruletApp app;
    private RatingBar myRating;
    private SharedPreferences.Editor mPrefEditor;
    private boolean changed;
    Context context;

    String locale;
    //TODO Improve default name, allow for multiple non-colliding defaults
    //TODO Create input validation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        changed = false;
        setContentView(R.layout.edit_route);
        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        context = getApplication();
        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();


        mPrefEditor = app.getPrefs().edit();
        setEditedRoute();
        initInterface();
    }

    @Override
    public void onBackPressed() {
        if (changed) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.finish_edit_trip))
                    .setMessage(
                            getString(R.string.finish_edit_trip_leave_long))
                    .setPositiveButton(getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    save(PropertyHolder.getUserId());
                                    Intent returnIntent = new Intent();
                                    setResult(RESULT_OK, returnIntent);
                                    finish();
                                }
                            }).setNegativeButton(getString(R.string.no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Intent returnIntent = new Intent();
                            setResult(RESULT_CANCELED, returnIntent);
                            finish();
                        }
                    }).show();
        } else {
            finish();
        }
    }

    private int save(String userId) {

        if(routeName.getText() != null){
        editedRoute.setName(locale, routeName.getText().toString());
        }

        if(routeDescription.getText() != null){
        editedRoute.setDescription(locale, routeDescription.getText().toString());
        }
        DataContainer.updateRoute(editedRoute, app.getDataBaseHelper());
        Toast.makeText(context, getString(R.string.save_succesful), Toast.LENGTH_LONG).show();
        return editedRoute.getId();
    }

    private void initInterface() {

        routeName = (EditText) findViewById(R.id.et_RouteName);
        routeDescription = (EditText) findViewById(R.id.et_RouteDescription);
        routeName.setText(editedRoute.getName(locale));
        routeDescription.setText(editedRoute.getDescription(locale));

        TextView ratingLabel = (TextView) findViewById(R.id.tvUserRating);
        String rating_text = getString(R.string.your_rating);
        if(editedRoute.getGlobalRating() >=0){
            rating_text = getResources().getString(R.string.average_rating_of_followed_route) + ": " + editedRoute.getGlobalRating() + "\n" + rating_text;
        }
        ratingLabel.setText(rating_text);



        myRating = (RatingBar) findViewById(R.id.ratBarUserRoute);
        myRating.setStepSize(1.0f);
        int userRating = 0;
        if(editedRoute.getUserRating() >= 0){
            userRating = editedRoute.getUserRating();
        }
        myRating.setRating(userRating);

        myRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                Route route_followed = DataContainer.findRouteByServerId(editedRoute.getIdRouteBasedOn(), app.getDataBaseHelper());
                if(route_followed != null){
                route_followed.setUserRating((int)rating);
                route_followed.setUserRatingTime(System.currentTimeMillis());
                route_followed.setUserRatingUploaded(false);
                app.getDataBaseHelper().getRouteDataDao().update(route_followed);
                }
            }
        });


        routeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changed = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
                changed = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }
        });

        routeDescription.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changed = true;

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                changed = true;
            }
        });

        RouteInfoFormatter rif = new RouteInfoFormatter(editedRoute, app.getApplicationContext());

        TextView tvTotalDist = (TextView) findViewById(R.id.tvTotalDist);
        TextView tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
        TextView tvAvgSpeed = (TextView) findViewById(R.id.tvAvgSpeed);
        TextView tvAvgSampleDistance = (TextView) findViewById(R.id.tvAvgSampleDistance);
        TextView tvPointsNumber = (TextView) findViewById(R.id.tvPointsNumber);
        TextView tvHighLightsNumber = (TextView) findViewById(R.id.tvHighLightsNumber);
        TextView tvRamp = (TextView) findViewById(R.id.tvRamp);

        tvTotalDist.setText(rif.getFormattedTotalDistance());
        tvTotalTime.setText(rif.getFormattedTotalTime());
        tvAvgSpeed.setText(rif.getFormattedAverageSpeedKmH());
        tvAvgSampleDistance.setText(rif.getFormattedAverageSampleDistanceMeters());
        tvPointsNumber.setText(rif.getFormattedNumberPointsInTrack());
        tvHighLightsNumber.setText(rif.getFormattedNumberHighlights());
        tvRamp.setText(rif.getFormattedRamp());


        Button btn_save = (Button) findViewById(R.id.btnHlSave);
        Button btn_cancel = (Button) findViewById(R.id.btnHlCancel);
        btn_save.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                    save(PropertyHolder.getUserId());
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
            }
        });
        btn_cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED, returnIntent);
                finish();

            }
        });
    }

    private void setEditedRoute() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int routeId = extras.getInt("routeId");
                editedRoute = DataContainer.findRouteById(routeId, app.getDataBaseHelper());
        }
    }


}
