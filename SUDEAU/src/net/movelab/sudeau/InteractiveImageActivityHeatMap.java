package net.movelab.sudeau;

import java.io.File;
import java.util.List;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Box;
import net.movelab.sudeau.model.InteractiveImage;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class InteractiveImageActivityHeatMap extends Activity implements View.OnTouchListener{
	
	private ImageView heatMap;
	private ImageView image;
	private AlertDialog dialog;
	private List<Box> envelopes;
	private EruletApp app;
	private static String TAG = "InteractiveImageViewHeatMap";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.interactive_image_activity_heatmap);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		heatMap = (ImageView)findViewById(R.id.int_image_areas);
		image = (ImageView)findViewById(R.id.int_image);
		
		InteractiveImage interactiveImage = null;
		Bundle extras = getIntent().getExtras();		
		if (extras != null) {
			String imgId = extras.getString("int_image_id");
			if(imgId!=null){
				interactiveImage = DataContainer.findInteractiveImageById(imgId, app.getDataBaseHelper());
			}
		}
		//image.setImageResource(R.drawable.redon_panorama);
		if(interactiveImage!=null && 
				interactiveImage.getMediaPath()!= null && 
				!interactiveImage.getMediaPath().equalsIgnoreCase("")){
			File sdcard = Environment.getExternalStorageDirectory();
			File f = new File(sdcard,interactiveImage.getMediaPath());
			if(f.exists()){
				image.setImageBitmap( 
				Util.decodeSampledBitmapFromFile(
						f.getAbsolutePath(), 
						interactiveImage.getOriginalWidth(), 
						interactiveImage.getOriginalHeight())
				);
			}						
		}		
		if(interactiveImage!=null && 
				interactiveImage.getHeatPath()!= null && 
				!interactiveImage.getHeatPath().equalsIgnoreCase("")){
			File sdcard = Environment.getExternalStorageDirectory();
			File f = new File(sdcard,interactiveImage.getHeatPath());
			if(f.exists()){
				heatMap.setImageBitmap( 
				Util.decodeSampledBitmapFromFile(f.getAbsolutePath(), 
						interactiveImage.getOriginalWidth(), 
						interactiveImage.getOriginalHeight())
				);
			}						
		}
		if(interactiveImage!=null){
			initBoxes(interactiveImage);
		}
		image.setOnTouchListener(this);
//		image.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View arg0, MotionEvent arg1) {
//				//Log.d(TAG,"Imageview widthxheight " + image.getDrawable().getIntrinsicWidth() + "x" + image.getDrawable().getIntrinsicHeight() );
//				Log.d(TAG,"Touch on image");
//				return false;
//			}
//		});
//		
//		heatMap.setOnTouchListener( new OnTouchListener() {			
//			@Override
//			public boolean onTouch(View arg0, MotionEvent event) {
//				// TODO Auto-generated method stub
//				//Log.d(TAG,"Imageview widthxheight " + heatMap.getDrawable().getIntrinsicWidth() + "x" + heatMap.getDrawable().getIntrinsicHeight() );
//				Log.d(TAG,"Touch on heatmap");
//				return false;
////				int x = (int) event.getX();
////				int y = (int) event.getY();
////				Log.d(TAG,"Clicked on " + "x:" + x + " y:" + y );
////				switch(event.getAction()){				
////				case MotionEvent.ACTION_DOWN:
////					Box b = checkBoxes(x, y);
////					if(b != null)
////						showBubble(b);  
////			        break;
////			    case MotionEvent.ACTION_MOVE:
////			        if(dialog!=null)
////			          dialog.dismiss(); 
////			         // do something
////			        break;
////			    case MotionEvent.ACTION_UP:
////			        // do something else
////			        break;
////				}				
////				return false;
//			}
//		});
	}
	
	private int getImageColor(int x, int y){
		heatMap.setDrawingCacheEnabled(true);
		heatMap.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
	            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		heatMap.layout(0, 0, heatMap.getMeasuredWidth(), heatMap.getMeasuredHeight());
		heatMap.buildDrawingCache(true);
		Bitmap b = Bitmap.createBitmap(heatMap.getDrawingCache());
		Log.d("TAG","Click on x " + x + " y " + y);
		Log.d("TAG","Image size x " + b.getWidth() + " y " + b.getHeight());
		return b.getPixel(x, y);
	}
	
	private void initBoxes(InteractiveImage img){
		envelopes = DataContainer.getInteractiveImageBoxes(img, app.getDataBaseHelper());
	}
	
	private Box checkBoxes(int x, int y){
		int clickedColor = getImageColor(x, y);
		for(int i = 0; i < envelopes.size(); i++){
			Box b = envelopes.get(i);
			Log.d("TAG","Clicked color - " + clickedColor + " box color - " + b.getColor());
		}
		return null;
	}
	
	public void showBubble(Box b){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		dialog = builder.create();
		dialog.setTitle( getString(R.string.info) );
		dialog.setMessage( b.getMessage() );
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
		Log.d(TAG,"Heat image color " + getImageColor(x, y));
		return false;
	}
}
