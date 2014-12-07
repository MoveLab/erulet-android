package net.movelab.sudeau;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory.Options;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.VideoView;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.JSONConverter;
import net.movelab.sudeau.model.Route;
import net.movelab.sudeau.model.Step;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

public class DetailHighLightActivity extends Activity {

    private EruletApp app;
    private ProgressBar progressBar;
    private RatingBar myRating;
    private Step step;
    private int screenWidth;
    String currentLocale;
    boolean ratingChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_highlight_picture);
        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        Context context = getApplicationContext();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        currentLocale = PropertyHolder.getLocale();

        screenWidth = Util.getScreenSize(getBaseContext())[0];
        int highLight_id = -1;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String step_json = extras.getString("step_j");
            highLight_id = extras.getInt("highlight_id");
            int route_id = extras.getInt("route_id");

            Route this_route = DataContainer.findRouteById(route_id,
                    app.getDataBaseHelper());
            try {
                step = JSONConverter.jsonObjectToStep(new JSONObject(step_json), app.getDataBaseHelper());
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (step != null) {
            setupUI(step, highLight_id);
        }
    }

    private void setupUI(Step s, int idHighLight) {
        TextView datatxt = (TextView) findViewById(R.id.tvHlData);
        TextView lattxt = (TextView) findViewById(R.id.tvLatHl);
        TextView longtxt = (TextView) findViewById(R.id.tvLongHl);
        TextView alttxt = (TextView) findViewById(R.id.tvHlAlt);
        TextView nameTxt = (TextView) findViewById(R.id.tvHlNameLabel);
        View picSeparator = findViewById(R.id.picture_separator);
        TextView descriptionTxt = (TextView) findViewById(R.id.tvHlDescription);


        myRating = (RatingBar) findViewById(R.id.ratBarUser);
        myRating.setStepSize(1.0f);

        final HighLight hl = DataContainer.findHighLightById(idHighLight, app.getDataBaseHelper());

        TextView ratingLabel = (TextView) findViewById(R.id.tvUserRating);
        String rating_text = getString(R.string.your_rating);
        if (hl.getGlobalRating() >= 0) {
            rating_text = getResources().getString(R.string.average_rating) + ": " + String.format("%.2f", hl.getGlobalRating()) + "\n\n" + rating_text;
        }
        ratingLabel.setText(rating_text);


        if (hl != null) {
            int userRating = 0;
            if (hl.getUserRating() >= 0) {
                userRating = hl.getUserRating();
            }
            myRating.setRating(userRating);

            myRating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating,
                                            boolean fromUser) {
                    hl.setUserRating((int) rating);
                    hl.setUserRatingTime(System.currentTimeMillis());
                    hl.setUserRatingUploaded(false);
                    ratingChange = true;
                }

            });
        }
        progressBar = (ProgressBar) findViewById(R.id.pbImageLoad);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);

        ImageView ivType = (ImageView) findViewById(R.id.highLightTypeIv);
        if (hl != null) {
            switch (hl.getType()) {
                case HighLight.ALERT:
                    ivType.setImageResource(R.drawable.pin_warning);
                    break;
                case HighLight.POINT_OF_INTEREST:
                    ivType.setImageResource(R.drawable.pin_drop);
                    break;
                case HighLight.POINT_OF_INTEREST_OFFICIAL:
                    ivType.setImageResource(R.drawable.pin_drop);
                    break;
                case HighLight.WAYPOINT:
                    ivType.setImageResource(R.drawable.pin_chart);
                    break;
            }
        }

        ImageView ivPicture = (ImageView) findViewById(R.id.highLightPicture);
        VideoView ivVideo = (VideoView) findViewById(R.id.highLightVideo);

        DataContainer.refreshHighlightForFileManifest(hl, app.getDataBaseHelper());
        if (hl != null && hl.hasMediaFile()) {
            String pathName = hl.getFileManifest().getPath();
            if (pathName.contains("mp4")) {
                progressBar.setVisibility(View.GONE);
                ivPicture.setVisibility(View.GONE);
                File videoFile = new File(pathName);
                Uri videoUri = Uri.fromFile(videoFile);
                ivVideo.setVideoURI(videoUri);
                ivVideo.setMediaController(new MediaController(this));
                ivVideo.setOnPreparedListener(new OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                    }
                });
                ivVideo.requestFocus();
                ivVideo.start();

            } else {
                ivVideo.setVisibility(View.GONE);
                Options options = Util.getImageOptions(pathName);
                float adjustedW = (float) screenWidth * 0.75f;
                int bitmapHeight = Util.getScaledImageHeight(options.outWidth, options.outHeight, adjustedW);
                loadBitmapThumbnailToImageView(pathName, (int) adjustedW, bitmapHeight, ivPicture, progressBar);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            ivVideo.setVisibility(View.GONE);
            ivPicture.setVisibility(View.GONE);
            picSeparator.setVisibility(View.GONE);
            if (hl != null && hl.getMediaUrl() != null) {
                String this_media_url = hl.getMediaUrl();
                LinearLayout webviewLL = (LinearLayout) findViewById(R.id.highLightWebviewLL);
                webviewLL.setVisibility(View.VISIBLE);
                WebView wv = (WebView) findViewById(R.id.highLightWebview);
                String html_string = "";
                if(this_media_url.contains(".mp4")){
                    html_string = "<video width=\"100%\"><source src=\"" + UtilLocal.URL_SERVULET + this_media_url + "\" type=\"video/mp4\"></video>";
                } else{
                html_string = "<img src=\"" + UtilLocal.URL_SERVULET + this_media_url + "\" width=\"100%\">";
            }
                wv.loadData(html_string, "text/html", "utf-8");
            }
        }

        String date = "";
        if (s.getAbsoluteTime() != null) {
            date = app.formatDateDayMonthYear(s.getAbsoluteTime()) + " " + app.formatDateHoursMinutesSeconds(new Date(s.getAbsoluteTimeMillis()));
        }
        String lat = Double.toString(s.getLatitude());
        String llong = Double.toString(s.getLongitude());
        String alt = Double.toString(s.getAltitude());

        String name = getString(R.string.point_no_name);
        if (hl != null && hl.getName(currentLocale) != null) {
            if (!hl.getName(currentLocale).trim().equalsIgnoreCase("")) {
                name = hl.getName(currentLocale);
            }
//			globalRating.setRating( hl.getGlobalRating() );
        }
        String description = getString(R.string.point_no_description);
        if (hl != null && hl.getLongText(currentLocale) != null) {
            if (!hl.getLongText(currentLocale).trim().equalsIgnoreCase("")) {
                description = hl.getLongText(currentLocale);
            }
        }

        nameTxt.setText(getString(R.string.point_name) + " " + name);
        descriptionTxt.setText(getString(R.string.description) + " " + description);
        datatxt.setText(getString(R.string.date) + " " + date);
        lattxt.setText(getString(R.string.latitude) + " " + lat);
        longtxt.setText(getString(R.string.longitude) + " " + llong);
        alttxt.setText(getString(R.string.altitude) + " " + alt);

        Button saveButton = (Button) findViewById(R.id.btnHlSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ratingChange) {
                    app.getDataBaseHelper().getHlDataDao().update(hl);
                    Intent ratingUploadIntent = new Intent(DetailHighLightActivity.this, UploadRatings.class);
                    startService(ratingUploadIntent);
                }
                finish();
            }
        });

    }

    private void loadBitmapThumbnailToImageView(
            String path,
            int width,
            int height,
            ImageView imageView, ProgressBar progressBar) {
        BitmapWorkerTask task = new BitmapWorkerTask(imageView, progressBar);
        task.execute(path, Integer.toString(width), Integer.toString(height));

    }


}
