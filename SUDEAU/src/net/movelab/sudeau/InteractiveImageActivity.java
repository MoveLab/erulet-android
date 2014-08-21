package net.movelab.sudeau;

import java.io.File;
import java.util.List;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Box;
import net.movelab.sudeau.model.InteractiveImage;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.View.OnTouchListener;

public class InteractiveImageActivity extends Activity {
	
	private ImageView interactiveImageView;
	private AlertDialog dialog;
	private List<Box> envelopes;
	private EruletApp app;
	private static String TAG = "InteractiveImageView";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interactive_image_activity);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		interactiveImageView = (ImageView)findViewById(R.id.iv_interactive);
		//interactiveImageView.setImageResource(R.drawable.redon_panorama);
		InteractiveImage interactiveImage = null;
		Bundle extras = getIntent().getExtras();		
		if (extras != null) {
			String imgId = extras.getString("int_image_id");
			if(imgId!=null){
				interactiveImage = DataContainer.findInteractiveImageById(imgId, app.getDataBaseHelper());
			}
		}
		if(interactiveImage!=null && 
				interactiveImage.getMediaPath()!= null && 
				!interactiveImage.getMediaPath().equalsIgnoreCase("")){
			File sdcard = Environment.getExternalStorageDirectory();
			File f = new File(sdcard,interactiveImage.getMediaPath());
			if(f.exists()){
				interactiveImageView.setImageBitmap( 
				Util.decodeSampledBitmapFromFile(f.getAbsolutePath(), 1500, 479)
				);
			}						
		}		
		if(interactiveImage!=null){
			initBoxes(interactiveImage);
		}
				
		
		interactiveImageView.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub
				Log.d(TAG,"Imageview widthxheight " + interactiveImageView.getDrawable().getIntrinsicWidth() + "x" + interactiveImageView.getDrawable().getIntrinsicHeight() );
				int x = (int) event.getX();
				int y = (int) event.getY();
				Log.d(TAG,"Clicked on " + "x:" + x + " y:" + y );
				switch(event.getAction()){				
				case MotionEvent.ACTION_DOWN:
					Box b = checkBoxes(x, y);
					if(b != null)
						showBubble(b);  
			        break;
			    case MotionEvent.ACTION_MOVE:
			        if(dialog!=null)
			          dialog.dismiss(); 
			         // do something
			        break;
			    case MotionEvent.ACTION_UP:
			        // do something else
			        break;
				}				
				return false;
			}
		});
	}		
	
	private void initBoxes(InteractiveImage img){
		envelopes = DataContainer.getInteractiveImageBoxes(img, app.getDataBaseHelper());
	}
	
	private Box checkBoxes(int x, int y){
		for(int i = 0; i < envelopes.size(); i++){
			Box b = envelopes.get(i);
//			if(b.isInside(x, y))
//				return b;
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

}
