package net.movelab.sudeau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.Reference;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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
		
	private String getReferenceString(){
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			String idReference = extras.getString("idReference");
            Log.d("Reference ID: ", idReference);
            Reference r = DataContainer.findReferenceById(idReference, app.getDataBaseHelper());
            if(r != null && r.getTextContent() != null && r.getTextContent() != ""){
                Log.d("Reference URL: ", "file://" + r.getTextContent());

                File f = new File(r.getTextContent());


                Log.i("html ", f.getPath());

                StringBuilder html_text = new StringBuilder();

                try {
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), Charset.forName("ISO-8859-1")));
                    String line;

                    while ((line = br.readLine()) != null) {
                        html_text.append(line);
                    }
                }
                catch (IOException e) {
                    //You'll need to add proper error handling here
                }

                String modified_html_text = html_text.toString().replace("../", "../../../../" );
//                String modified_html_text = html_text.toString().replace("../", "file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Util.baseFolder + "/" );

                Log.i("htmltext ", modified_html_text);
			return modified_html_text;
            } else
                return null;
		}
		return null;
	}
	
	private void loadHTML(){

        wv.loadData(getReferenceString(), "text/html; charset=UTF-8", null);
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
                Log.i("URL", url);
	            view.loadUrl(url);
	            return true;
	        }
	    }
	}
	
}
