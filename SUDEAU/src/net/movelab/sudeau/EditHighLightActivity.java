package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.movelab.sudeau.model.HighLight;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

public class EditHighLightActivity extends Activity {

    //TODO Enable default name for highlight
    //TODO Create input validation

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_VIDEO_CAPTURE = 2;

    private ImageButton btn_picture;
    private ImageButton btn_video;
    private File currentPhoto;
    private File currentVideo;
    private Bitmap thumbnail;
    private Bitmap videoThumbnail;
    private EditText tvName;
    private EditText tvLongText;
    private RadioGroup hlTypeRg;
    private RadioButton rbWp;
    private RadioButton rbPoi;
    private RadioButton rbWarning;
    private RadioGroup imageOrVideo;
    private RadioButton rbImage;
    private RadioButton rbVideo;
    private RadioButton rbNone;

    //State values
    private int selectedHlType = HighLight.POINT_OF_INTEREST;

    String lat = "";
    String llong = "";
    String alt = "";
    String date = "";
    String name = "";
    String longText = "";
    String imagePath = "";
    int hlType = 0;

    private EruletApp app;
    private int editedHighLightId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        // To stop keyboard from popping up immediately and blocking everything
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.highlight_activity);

        setUpInterface();

        // If savedInstanceState is null, then this activity has just been launched from the detailItineraryActivity and we need to get highlight information from the bundle of extras sent with the intent.
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                lat = extras.getString("lat");
                llong = extras.getString("long");
                alt = extras.getString("alt");
                date = extras.getString("date");
                name = extras.getString("hlname");
                longText = extras.getString("hllongtext");
                imagePath = extras.getString("hlimagepath");
                editedHighLightId = extras.getInt("hlid", -1);
                hlType = extras.getInt("hltype");
            }
        } else {
            lat = savedInstanceState.getString("lat");
            llong = savedInstanceState.getString("long");
            alt = savedInstanceState.getString("alt");
            date = savedInstanceState.getString("date");
            name = savedInstanceState.getString("hlname");
            longText = savedInstanceState.getString("hllongtext");
            imagePath = savedInstanceState.getString("hlimagepath");
            editedHighLightId = savedInstanceState.getInt("hlid", -1);
            hlType = savedInstanceState.getInt("hltype");
        }
        // Now set interface using values from either savedInstanceState or intent bundle:
        if (hlType != 0) {
            checkHighLightType(hlType);
        }
        if (name != null) {
            tvName.setText(name);
        }
        if (longText != null) {
            tvLongText.setText(longText);
        }
        if (imagePath != null && !imagePath.trim().equalsIgnoreCase("")) {
            Uri uri = Uri.parse(imagePath);
            if (imagePath.contains("mp4")) {
                currentVideo = new File(uri.getPath());
                try {
                    createVideoThumbnail();
                    btn_video.setImageBitmap(videoThumbnail);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                rbVideo.setChecked(true);
                btn_picture.setVisibility(View.GONE);
                btn_video.setVisibility(View.VISIBLE);
            } else {
                currentPhoto = new File(uri.getPath());
                try {
                    createThumbnail();
                    btn_picture.setImageBitmap(thumbnail);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                rbImage.setChecked(true);
                btn_picture.setVisibility(View.VISIBLE);
                btn_video.setVisibility(View.GONE);
            }
        } else {
            rbNone.setChecked(true);
            btn_picture.setVisibility(View.GONE);
            btn_video.setVisibility(View.GONE);
        }
        TextView datatxt = (TextView) findViewById(R.id.tvHlData);
        TextView lattxt = (TextView) findViewById(R.id.tvLatHl);
        TextView longtxt = (TextView) findViewById(R.id.tvLongHl);
        TextView alttxt = (TextView) findViewById(R.id.tvHlAlt);
        datatxt.setText(getString(R.string.date) + date);
        lattxt.setText(getString(R.string.latitude) + lat);
        longtxt.setText(getString(R.string.longitude) + llong);
        alttxt.setText(getString(R.string.altitude) + alt);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("lat", lat);
        savedInstanceState.putString("long", llong);
        savedInstanceState.putString("alt", alt);
        savedInstanceState.putString("date", date);

        if (tvName.getText() != null) {
            name = tvName.getText().toString();
        }
        savedInstanceState.putString("hlname", name);

        if (tvLongText.getText() != null) {
            longText = tvLongText.getText().toString();
        }
        savedInstanceState.putString("hllongtext", longText);

        if (imageOrVideo.getCheckedRadioButtonId() == R.id.rbVideo && currentVideo != null) {
            imagePath = Uri.fromFile(currentVideo).toString();
        } else if (imageOrVideo.getCheckedRadioButtonId() == R.id.rbImage && currentPhoto != null) {
            imagePath = Uri.fromFile(currentPhoto).toString();
        }
        savedInstanceState.putString("hlimagepath", imagePath);

        savedInstanceState.putInt("hlid", editedHighLightId);

        int selectedType = hlTypeRg.getCheckedRadioButtonId();
        switch (selectedType) {
            case R.id.rbWayPoint:
                savedInstanceState.putInt("hltype", HighLight.WAYPOINT);
                break;
            case R.id.rbPOI:
                savedInstanceState.putInt("hltype", HighLight.POINT_OF_INTEREST);
                break;
            case R.id.rbWarning:
                savedInstanceState.putInt("hltype", HighLight.ALERT);
                break;
            default:
                savedInstanceState.putInt("hltype", HighLight.WAYPOINT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        checkHighLightType(selectedHlType);
        if (currentVideo != null) {
            if (videoThumbnail != null) {
                rbVideo.setChecked(true);
                btn_picture.setVisibility(View.GONE);
                btn_video.setVisibility(View.VISIBLE);
            }
        } else if (currentPhoto != null) {
            if (thumbnail != null) {
                rbImage.setChecked(true);
                btn_picture.setVisibility(View.VISIBLE);
                btn_video.setVisibility(View.GONE);
            }
        } else {
            rbNone.setChecked(true);
            btn_picture.setVisibility(View.GONE);
            btn_video.setVisibility(View.GONE);
        }
    }

    private void checkHighLightType(int hlType) {
        switch (hlType) {
            case HighLight.POINT_OF_INTEREST:
                rbPoi.setChecked(true);
                break;
            case HighLight.WAYPOINT:
                rbWp.setChecked(true);
                break;
            case HighLight.ALERT:
                rbWarning.setChecked(true);
                break;
            default:
                rbWp.setChecked(true);
                break;
        }
    }

    public void onRadioButtonHlClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.rbPOI:
                selectedHlType = HighLight.POINT_OF_INTEREST;
                break;
            case R.id.rbWarning:
                selectedHlType = HighLight.ALERT;
                break;
            case R.id.rbWayPoint:
                selectedHlType = HighLight.WAYPOINT;
                break;
        }
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.rbImage:
                if (checked) {
                    btn_picture.setVisibility(View.VISIBLE);
                    btn_video.setVisibility(View.GONE);
                }
                break;
            case R.id.rbVideo:
                if (checked) {
                    btn_picture.setVisibility(View.GONE);
                    btn_video.setVisibility(View.VISIBLE);
                }
                break;
            default:
                if (checked) {
                    btn_picture.setVisibility(View.GONE);
                    btn_video.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void createVideoFile() throws IOException {
        String imageFileName = "Holet_" + app.formatDateMediaTimestamp(new Date()) + "_";
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File storageDir = new File(Environment.getExternalStorageDirectory(), Util.baseFolder + "/" + Util.routeMediaFolder);
        currentVideo = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );
        //Update media gallery with image
        MediaScannerConnection.scanFile(this, new String[]{currentVideo.getPath()},
                new String[]{"video/mp4"}, null);
    }

    private void createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "Holet_" + app.formatDateMediaTimestamp(new Date()) + "_";
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = new File(Environment.getExternalStorageDirectory(), Util.baseFolder + "/" + Util.routeMediaFolder);
        currentPhoto = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //Update media gallery with image
        MediaScannerConnection.scanFile(this, new String[]{currentPhoto.getPath()},
                new String[]{"image/jpeg"}, null);
    }

    private void dispatchCaptureVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            try {
                createVideoFile();
            } catch (IOException ex) {
                currentVideo = null;
                Toast.makeText(getApplicationContext(), getString(R.string.error_video_capture), Toast.LENGTH_LONG).show();
            }
            if (currentVideo != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(currentVideo));
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                currentPhoto = null;
                Toast.makeText(getApplicationContext(), getString(R.string.error_image_capture), Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (currentPhoto != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(currentPhoto));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                createThumbnail();
                btn_picture.setImageBitmap(thumbnail);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            try {
                createVideoThumbnail();
                btn_video.setImageBitmap(videoThumbnail);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void createVideoThumbnail() throws FileNotFoundException {
        videoThumbnail = ThumbnailUtils.createVideoThumbnail(currentVideo.getAbsolutePath(), android.provider.MediaStore.Video.Thumbnails.MICRO_KIND);
    }

    private void createThumbnail() throws FileNotFoundException {
        thumbnail = Util.decodeSampledBitmapFromFile(currentPhoto.getAbsolutePath(), 96, 96);
    }

    private void setUpInterface() {
        btn_picture = (ImageButton) findViewById(R.id.btnPicture);
        btn_video = (ImageButton) findViewById(R.id.btnVideo);

        rbWp = (RadioButton) findViewById(R.id.rbWayPoint);
        rbPoi = (RadioButton) findViewById(R.id.rbPOI);
        rbWarning = (RadioButton) findViewById(R.id.rbWarning);

        rbImage = (RadioButton) findViewById(R.id.rbImage);
        rbVideo = (RadioButton) findViewById(R.id.rbVideo);
        rbNone = (RadioButton) findViewById(R.id.rbImgVidNull);

        Button btn_save = (Button) findViewById(R.id.btnHlSave);
        Button btn_cancel = (Button) findViewById(R.id.btnHlCancel);
        tvName = (EditText) findViewById(R.id.txtNameHl);
        tvLongText = (EditText) findViewById(R.id.txtLongHl);
        btn_picture.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        btn_video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchCaptureVideoIntent();
            }
        });
        btn_save.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                if(tvName.getText() != null){
                    returnIntent.putExtra("hlName", tvName.getText().toString());
                }
                if(tvLongText.getText() != null){
                    returnIntent.putExtra("hlLongText", tvLongText.getText().toString());
                }
                returnIntent.putExtra("hlid", editedHighLightId);
                int selectedRb = imageOrVideo.getCheckedRadioButtonId();
                switch (selectedRb) {
                    case R.id.rbImage:
                        if (currentPhoto != null) {
                            returnIntent.putExtra("imagePath", currentPhoto.getPath());
                            Log.i("Highlight Image Path", currentPhoto.getPath());
                        } else {
                            returnIntent.putExtra("imagePath", "");
                        }
                        break;
                    case R.id.rbVideo:
                        if (currentVideo != null) {
                            returnIntent.putExtra("imagePath", currentVideo.getPath());
                        } else {
                            returnIntent.putExtra("imagePath", "");
                        }
                        break;
                    default:
                        //TODO maybe if currentVideo or currentImage aren't null, issue warning
                        returnIntent.putExtra("imagePath", "");
                        break;
                }
                int selectedType = hlTypeRg.getCheckedRadioButtonId();
                switch (selectedType) {
                    case R.id.rbWayPoint:
                        returnIntent.putExtra("hlType", HighLight.WAYPOINT);
                        break;
                    case R.id.rbPOI:
                        returnIntent.putExtra("hlType", HighLight.POINT_OF_INTEREST);
                        break;
                    case R.id.rbWarning:
                        returnIntent.putExtra("hlType", HighLight.ALERT);
                        break;
                    default:
                        returnIntent.putExtra("hlType", HighLight.WAYPOINT);
                        break;
                }
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
        imageOrVideo = (RadioGroup) findViewById(R.id.rgImageVideo);
        hlTypeRg = (RadioGroup) findViewById(R.id.rgHighLightType);
        tvName.requestFocus();
    }

}
