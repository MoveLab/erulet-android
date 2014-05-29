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
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class HTMLViewerActivity extends Activity {
		
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private DataBaseHelper dataBaseHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_viewer_activity);
		setUpDB();
		loadHTML();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(group1,first_id,first_id,"Reproduïr video...");			
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			case 1:							
				Intent i = new Intent(HTMLViewerActivity.this,VideoPlayActivity.class);				
				startActivity(i);
			    return true;		
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void setUpDB() {
		if(dataBaseHelper == null){
			dataBaseHelper = OpenHelperManager.getHelper(this,DataBaseHelper.class);			
		}
	}
	
	private String getReferenceURI(){
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			String idReference = extras.getString("idReference");
			Reference r = DataContainer.findReferenceById(idReference, dataBaseHelper);
			return "file:///android_asset/" + r.getTextContent();
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
