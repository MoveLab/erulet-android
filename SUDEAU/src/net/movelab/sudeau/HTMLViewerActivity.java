package net.movelab.sudeau;

import java.io.File;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Reference;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HTMLViewerActivity extends Activity {
		
	private int group1 = 1;
	private int first_id = Menu.FIRST;	
	private EruletApp app;
	WebView wv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_viewer_activity);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
		wv = (WebView) findViewById(R.id.wb_webView);
		wv.setWebViewClient(new MyWebViewClient());
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
            if(r != null && r.getTextContent() != null && r.getTextContent() != ""){
			return r.getTextContent();
                    //"file:///" + file.getAbsolutePath() + "/" + r.getTextContent();
			//return "file:///android_asset/" + r.getTextContent();
            } else
                return null;
		}
		return null;
	}
	
	private void loadHTML(){		                   
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
	
	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        if (url.contains("mp4")) {
	        	Intent ihtml = new Intent(HTMLViewerActivity.this,
						VideoPlayActivity.class);
				ihtml.putExtra("videourl", url);
				startActivity(ihtml);
	            return false;
	        }else{
	            view.loadUrl(url);
	            return true;
	        }
	    }
	}
	
}
