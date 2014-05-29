package net.movelab.sudeau;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

public class InteractiveImageActivity extends Activity {
	
	private ImageView interactiveImage;
	private AlertDialog dialog;
	private ArrayList<Box> envelopes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interactive_image_activity);
		
		interactiveImage = (ImageView)findViewById(R.id.iv_interactive);  
		int resID = getResources().getIdentifier("redon_panorama", "drawable",  getPackageName());
		interactiveImage.setImageResource(resID);
		initBoxes();
		
		interactiveImage.setOnTouchListener( new OnTouchListener() {			
			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				// TODO Auto-generated method stub				
				int x = (int) event.getX();
				int y = (int) event.getY();
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
			        // do somethig
			        break;
				}				
				return false;
			}
		});
	}		
	
	private void initBoxes(){
		envelopes = new ArrayList<InteractiveImageActivity.Box>();
		Box tuc = new Box(251,231,357,45);
		tuc.setMessage("Tuc deth Pòrt de Vielha, 2605m");
		Box tartera = new Box(176,352,282,285);
		tartera.setMessage("TARTERA: és una extensió de roca fragmentada. La fragmentació augmenta la " + 
				"superfície de roca exposada a l'intemperie. Per tant, facilita la dissolució de les sals minerals de les " + 
				"roques, que van a parar a l'aigua de l'estany. Les tarteres ocupen un 36% de la conca del Redon.");
		Box serra = new Box(1165,198,1265,117);
		serra.setMessage("Serra de Fontfreda");
		Box prats = new Box(1841,467,1981,336);
		prats.setMessage("PRATS ALPINS: Formats per herbes de port baix. Depenent de l'orientació dominen gramínies (Festuco) " + 
				"o ciperàcies (Corex). Poden ocupar zones amb fort pendent, on les seves arrels ajuden a fixar un sòl molt " + 
				"orgànic i poc profund, sovint de menys de 30 cm. Les plantes i microbis que viuen al sòl tenen una forta " + 
				"influència sobre l'aigua que s'escorre. Els prats ocupen el 45% de la conca del Redon.");
		Box roca = new Box(2350,464,2468,339);
		roca.setMessage("ROCA EXPOSADA: La roca mare de la conca queda en superfície en afloraments i escarpaments. " + 
				"L'aigua circula ràpidament i el temps de contacte en aquestes zones és curt. Les zones de roca nua ocupen el " + 
				"19% de la conca del Redon.");
		Box sarra = new Box(2393,123,2748,50);
		sarra.setMessage("Tuc de Sarrahèra, 2630m");
		envelopes.add(tuc);		
		envelopes.add(tartera);
		envelopes.add(serra);
		envelopes.add(prats);
		envelopes.add(roca);
		envelopes.add(sarra);
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
		dialog.setTitle("Informació");
		dialog.setMessage( b.getMessage() );
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.LEFT;
        wmlp.x = 50;   //x position
        wmlp.y = 200;   //y position
        dialog.show();
	}
	
	private class Box{
		
		private int minX;
		private int minY;
		private int maxX;
		private int maxY;
		private String message;
		
		public Box(int minX, int minY, int maxX,int maxY){
			this.minX=minX;
			this.minY=minY;
			this.maxX=maxX;
			this.maxY=maxY;
		}
		
		public String getMessage(){
			return message;
		}
		
		public void setMessage(String message){
			this.message=message;
		}
		
		public boolean isInside(int x, int y){
			if( x <= maxX && x >= minX && y >= maxY && y <= minY )
				return true;
			return false;
		}
	}

}
