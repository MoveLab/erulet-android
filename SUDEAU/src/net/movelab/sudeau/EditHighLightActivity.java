package net.movelab.sudeau;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class EditHighLightActivity extends Activity {	
	
	//TODO Enable default name for highlight
	//TODO Allow adding video/pictures from album
	//TODO Correctly scale image thumbnail (especially on edit)	
	//TODO Create input validation
	
	static final int REQUEST_IMAGE_CAPTURE = 1;
	static final int REQUEST_VIDEO_CAPTURE = 2;
	
	private ImageButton btn_picture;
	private ImageButton btn_video;
	private File currentPhoto;
	private File currentVideo;
	private Bitmap thumbnail;	
	private Bitmap videoThumbnail;
	private TextView tvName;
	private TextView tvLongText;
	private RadioGroup imageOrVideo;
	private RadioButton rbImage;
	private RadioButton rbVideo;
	private RadioButton rbNone;
	
	private EruletApp app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		setContentView(R.layout.highlight_activity);
		setUpInterface();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String lat = extras.getString("lat");
			String llong = extras.getString("long");
			String alt = extras.getString("alt");
			String date = extras.getString("date");
			String name = extras.getString("hlname");
			String longText = extras.getString("hllongtext");
			String imagePath = extras.getString("hlimagepath");
			if(name!=null){
				tvName.setText(name);
			}
			if(longText!=null){
				tvLongText.setText(longText);
			}
			if(imagePath!=null){
				Uri uri = Uri.parse(imagePath);
				if(imagePath.contains("mp4")){					
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
				}else{										
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
			}else{
				rbNone.setChecked(true);
				btn_picture.setVisibility(View.GONE);
	    		btn_video.setVisibility(View.GONE);
			}
			TextView datatxt =  (TextView)findViewById(R.id.tvHlData);
			TextView lattxt =  (TextView)findViewById(R.id.tvLatHl);
			TextView longtxt =  (TextView)findViewById(R.id.tvLongHl);
			TextView alttxt =  (TextView)findViewById(R.id.tvHlAlt);
			datatxt.setText("Data: " + date);
			lattxt.setText("Latitud: " + lat);
			longtxt.setText("Longitud: " + llong);
			alttxt.setText("Altitud: " + alt);
		}
	}
		
	public void onRadioButtonClicked(View view){
	    boolean checked = ((RadioButton) view).isChecked();	    
	    switch(view.getId()) {
	        case R.id.rbImage:
	            if (checked){	            	
	            	btn_picture.setVisibility(View.VISIBLE);
	        		btn_video.setVisibility(View.GONE);
	            }
	            break;
	        case R.id.rbVideo:
	            if (checked){
	            	btn_picture.setVisibility(View.GONE);
	        		btn_video.setVisibility(View.VISIBLE);
	            }
	            break;
	        default:
	        	if(checked){
	        		btn_picture.setVisibility(View.GONE);
	        		btn_video.setVisibility(View.GONE);
	        	}
	        	break;
	    }	    
	}
	
	private void createVideoFile() throws IOException {		
	    String imageFileName = "Erulet_" + app.formatDateMediaTimestamp(new Date()) + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);	    
	    currentVideo = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".mp4",         /* suffix */
	        storageDir      /* directory */
	    );
	    //Update media gallery with image
	    MediaScannerConnection.scanFile(this, new String[] { currentVideo.getPath() }, 
	    		new String[] { "video/mp4" }, null);
	}
	
	private void createImageFile() throws IOException {
	    // Create an image file name
	    String imageFileName = "Erulet_" + app.formatDateMediaTimestamp(new Date()) + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);	    
	    currentPhoto = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );
	    //Update media gallery with image
	    MediaScannerConnection.scanFile(this, new String[] { currentPhoto.getPath() }, 
	    		new String[] { "image/jpeg" }, null);
	}

	private void dispatchCaptureVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
		takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
	    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
	    	try{
	    		createVideoFile();
	    	} catch (IOException ex) {
	    		currentVideo=null;
	    		Toast.makeText(getApplicationContext(), "Error capturant video...", Toast.LENGTH_LONG).show();
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
	        	currentPhoto=null;
	        	Toast.makeText(getApplicationContext(), "Error capturant imatge...", Toast.LENGTH_LONG).show();
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
//	        Bundle extras = data.getExtras();
//	        Bitmap imageBitmap = (Bitmap) extras.get("data");	        
//	        btn_picture.setImageBitmap(imageBitmap);
	    	//File file = new File(mCurrentPhotoPath);
	    	try {	    		
				//Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(currentPhoto));
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
	
	private void createVideoThumbnail() throws FileNotFoundException{
		videoThumbnail = ThumbnailUtils.createVideoThumbnail( currentVideo.getAbsolutePath(), android.provider.MediaStore.Video.Thumbnails.MINI_KIND);		
    }
	
	private void createThumbnail() throws FileNotFoundException{		                        
        FileInputStream fis;		
		fis = new FileInputStream(currentPhoto);
		thumbnail = BitmapFactory.decodeStream(fis);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, 96, 96, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);		
	}
	
	private void setUpInterface(){
		btn_picture = (ImageButton)findViewById(R.id.btnPicture);
		btn_video = (ImageButton)findViewById(R.id.btnVideo);
		rbImage = (RadioButton)findViewById(R.id.rbImage);
		rbVideo = (RadioButton)findViewById(R.id.rbVideo);
		rbNone = (RadioButton)findViewById(R.id.rbImgVidNull);
		Button btn_save = (Button)findViewById(R.id.btnHlSave);
		Button btn_cancel = (Button)findViewById(R.id.btnHlCancel);
		tvName = (TextView)findViewById(R.id.txtNameHl);
		tvLongText = (TextView)findViewById(R.id.txtLongHl);
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
				returnIntent.putExtra("hlName", tvName.getText().toString());
				returnIntent.putExtra("hlLongText", tvLongText.getText().toString());
				int selectedRb = imageOrVideo.getCheckedRadioButtonId();
				switch(selectedRb){
					case R.id.rbImage:
						if(currentPhoto!=null){
							returnIntent.putExtra("imagePath", Uri.fromFile(currentPhoto).toString());
						}else{
							returnIntent.putExtra("imagePath", "");
						}
						break;
					case R.id.rbVideo:
						if(currentVideo!=null){
							returnIntent.putExtra("imagePath", Uri.fromFile(currentVideo).toString());
						}else{
							returnIntent.putExtra("imagePath", "");
						}
						break;
					default:
						//TODO maybe if currentVideo or currentImage aren't null, issue warning
						returnIntent.putExtra("imagePath", "");
						break;
				}				
				setResult(RESULT_OK,returnIntent);
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
		imageOrVideo = (RadioGroup)findViewById(R.id.rgImageVideo);
		tvName.requestFocus();
	}
	
}
