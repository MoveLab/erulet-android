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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class EditHighLightActivity extends Activity {	
	
	static final int REQUEST_IMAGE_CAPTURE = 1;
	private ImageButton btn_picture;	
	private File currentPhoto;
	private Bitmap thumbnail;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.highlight_activity);
		setUpInterface();
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String lat = extras.getString("lat");
			String llong = extras.getString("long");
			String date = extras.getString("date");
			TextView datatxt =  (TextView)findViewById(R.id.tvHlData);
			TextView lattxt =  (TextView)findViewById(R.id.tvLatHl);
			TextView longtxt =  (TextView)findViewById(R.id.tvLongHl);
			datatxt.setText(date);
			lattxt.setText(lat);
			longtxt.setText(llong);
		}
	}
	
	private void createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	    currentPhoto = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    //mCurrentPhotoPath = "file://" + image.getAbsolutePath();	    	   
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
	}
	
	private void createThumbnail() throws FileNotFoundException{		                
        int width = btn_picture.getMeasuredWidth();
        int height = btn_picture.getMeasuredHeight();
        FileInputStream fis;		
		fis = new FileInputStream(currentPhoto);
		thumbnail = BitmapFactory.decodeStream(fis);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, width-10, height-10, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);		                          
	}
	
	private void setUpInterface(){
		btn_picture = (ImageButton)findViewById(R.id.btnPicture);		
		Button btn_save = (Button)findViewById(R.id.btnHlSave);
		Button btn_cancel = (Button)findViewById(R.id.btnHlCancel);
		final TextView tvName = (TextView)findViewById(R.id.txtNameHl);
		final TextView tvLongText = (TextView)findViewById(R.id.txtLongHl);
		btn_picture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dispatchTakePictureIntent();
			}
		});
		btn_save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				returnIntent.putExtra("hlName", tvName.getText().toString());
				returnIntent.putExtra("hlLongText", tvLongText.getText().toString());
				if(currentPhoto!=null){
					returnIntent.putExtra("imagePath", Uri.fromFile(currentPhoto).toString());
				}else{
					returnIntent.putExtra("imagePath", "");
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
	}
	
}
