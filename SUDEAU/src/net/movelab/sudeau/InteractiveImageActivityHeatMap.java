package net.movelab.sudeau;

import java.io.File;
import java.util.List;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Box;
import net.movelab.sudeau.model.InteractiveImage;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;

public class InteractiveImageActivityHeatMap extends Activity implements View.OnTouchListener{
	
	private ImageView image;
	private AlertDialog dialog;
	private List<Box> envelopes;
	private EruletApp app;
	private static String TAG = "InteractiveImageViewHeatMap";
	private int scaledHeight;
	private int scaledWidth;
	private int originalHeight;
	private int originalWidth;
    HorizontalScrollView scrollView;
    String currentLocale;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.interactive_image_activity_heatmap);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        Context context = getApplicationContext();
        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);
        currentLocale = PropertyHolder.getLocale();

        image = (ImageView)findViewById(R.id.int_image);

        scrollView = (HorizontalScrollView) findViewById(R.id.heatMapScrollView);


		InteractiveImage interactiveImage = null;
		Bundle extras = getIntent().getExtras();		
		if (extras != null) {
			int imgId = extras.getInt("int_image_id");
            Log.i("IIMAP imgId: ", "" + imgId);

            if(imgId!= -1){
				interactiveImage = DataContainer.findInteractiveImageById(imgId, app.getDataBaseHelper());
				initBoxes(interactiveImage);
			}
		}

		if(interactiveImage!=null){
            DataContainer.refreshInteractiveImageForFileManifest(interactiveImage, app.getDataBaseHelper());
            if(interactiveImage.hasMediaFile()){
            Log.i("Interactive Image: HAS FILE", "yes? " + interactiveImage.hasMediaFile());
             String pathName = interactiveImage.getFileManifest().getPath();
                File f = new File(pathName);
            Log.i("IIMAP: ", f.getPath());
			if(f.exists()){
				int[] screenSize = Util.getScreenSize(getBaseContext());
				originalHeight = interactiveImage.getOriginalHeight();
				originalWidth = interactiveImage.getOriginalWidth();
                int downloadedWidth = 2*Util.getLargestScreenDimension(getApplicationContext()); // this is by definition of what I have on server
                int downloadedHeight = (int) (downloadedWidth * originalHeight)/originalWidth;
                // to avoid images that are to large and cause memory problems, I am doing the following: Scaled height will be the minimum of the actual downloaded height and the phone's smallest screen dimension. So if landscape, image is usually full height. But in portrait it is not (since otherwise it becomes huge in portrait).
				scaledHeight = Math.min(downloadedHeight, Math.min(screenSize[1], screenSize[0]));
				scaledWidth = Util.getScaledImageWidth(originalWidth, originalHeight, (float)scaledHeight);
                    image.setImageBitmap( Bitmap.createScaledBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()), scaledWidth, scaledHeight, false) );
			}
		}				
		image.setOnTouchListener(this);
	}}

	private int getBitmapXCoord(int screenXCoord){
		float result;
		result = (originalWidth * screenXCoord)/scaledWidth;
		return (int)result; 
	}
	
	private int getBitmapYCoord(int screenYCoord){
		float result;
		result = (originalHeight * screenYCoord)/scaledHeight;
		return (int)result;
	}
			
	private void initBoxes(InteractiveImage img){
		envelopes = DataContainer.getInteractiveImageBoxes(img, app.getDataBaseHelper());
	}
	
	private Box checkBoxes(int x, int y){
		for(int i = 0; i < envelopes.size(); i++){
			Box b = envelopes.get(i);
			if(b.isInside(x, y))
				return b;
		}
		return null;
	}
	
	public void showBubble(Box b){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		dialog = builder.create();
		dialog.setTitle( getString(R.string.info) );
		dialog.setMessage( Html.fromHtml(b.getMessage(currentLocale)) );
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        wmlp.x = 50;   //x position
        wmlp.y = 200;   //y position
        dialog.show();
	}


        @Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		int x = (int) event.getX();
		int y = (int) event.getY();
		Log.d(TAG,"Clicked on x " + x + " y " + y);
		switch(event.getAction()){				
		case MotionEvent.ACTION_DOWN:
			int bitmapX = getBitmapXCoord(x);
			int bitmapY = getBitmapYCoord(y);
			Log.d(TAG,"Bitmap coords x " + bitmapX + " y " + bitmapY);
			Box b = checkBoxes(bitmapX, bitmapY);
			if(b != null)
				showBubble(b);  
	        break;
	    case MotionEvent.ACTION_MOVE:
	        if(dialog!=null)
	          dialog.dismiss(); 
	         // do something
	        break;
	    case MotionEvent.ACTION_UP:
	    	if(dialog!=null)
	          dialog.dismiss();
	        // do something else
	        break;
		}				
		return false;
	}
}
