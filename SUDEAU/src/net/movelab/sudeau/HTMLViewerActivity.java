package net.movelab.sudeau;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import net.movelab.sudeau.database.DataBaseHelper;
import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Reference;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class HTMLViewerActivity extends Activity {
		
	private int group1 = 1;
	private int first_id = Menu.FIRST;	
	private EruletApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_viewer_activity);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		loadHTML();
	}
	
	
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item){
//		switch(item.getItemId()){
//			case 1:							
//				Intent i = new Intent(HTMLViewerActivity.this,VideoPlayActivity.class);				
//				startActivity(i);
//			    return true;		
//			default:
//				break;
//		}
//		return super.onOptionsItemSelected(item);
//	}
		
	private String getReferenceURI(){
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			String idReference = extras.getString("idReference");
			Reference r = DataContainer.findReferenceById(idReference, app.getDataBaseHelper());
			File file = new File(Environment.getExternalStorageDirectory(), Util.baseFolder + "/" + Util.othersFolder);
			return "file:///" + file.getAbsolutePath() + "/" + r.getTextContent();
			//return "file:///android_asset/" + r.getTextContent();
		}
		return null;
	}
	
	private void loadHTML(){		 
        WebView wv;  
        wv = (WebView) findViewById(R.id.wb_webView);  
        wv.loadUrl(getReferenceURI());
	}
	
	/**
	 * This allows navigation between a webview and a link opening another webview
	 */
	@Override
	public void onBackPressed() {
		WebView wv;  
        wv = (WebView) findViewById(R.id.wb_webView);
        if(wv.canGoBack()){
        	wv.goBack();
        }else{
        	super.onBackPressed();
        }
	}
	
}
