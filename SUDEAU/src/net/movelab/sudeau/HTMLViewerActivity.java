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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HTMLViewerActivity extends Activity {

	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private EruletApp app;
	WebView wv;
    String base_url;
    boolean firstload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_viewer_activity);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }

        firstload = true;

		wv = (WebView) findViewById(R.id.wb_webView);
        wv.setWebChromeClient(new WebChromeClient());
		wv.setWebViewClient(new MyWebViewClient());
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);

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
            if(r != null && r.getTextContent() != null && !r.getTextContent().isEmpty()){
                Log.d("Reference URL: ", "file://" + r.getTextContent());

                String[] url_chop = r.getTextContent().split("/");
                base_url = "file://";
                for(int i = 0; i < (url_chop.length-1); i++){
                    base_url += url_chop[i] + "/";
                }

                Log.d("base URL: ", base_url);

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

//                String modified_html_text = html_text.toString().replace("../", "" );
                String modified_html_text = html_text.toString().replace("../", "file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Util.baseFolder + "/" ).replace("</head>","</head><body>").replace("</html>", "</body></html>").replace("holet-ref-style.css", "erholet-ref-style.css");



                Log.i("htmltext ", modified_html_text);
			return modified_html_text;
            } else
                return null;
		}
		return null;
	}

	private void loadHTML(){

        wv.loadDataWithBaseURL(base_url, getReferenceString(), "text/html","utf-8", null);
	}

	/**
	 * This allows navigation between a webview and a link opening another webview
	 */
	@Override
	public void onBackPressed() {
		WebView wv;
        wv = (WebView) findViewById(R.id.wb_webView);
        Log.i("WV BACK: ", wv.getUrl());
        if(wv.canGoBack() && !wv.getUrl().equals("about:blank")){
        	wv.goBack();
            Log.i("WV BACKED: ", wv.getUrl());
            if(wv.getUrl().equals("about:blank")){
                Log.i("WV BACKED LOADING: ", wv.getUrl());
                loadHTML();
            }
        }else{
        	super.onBackPressed();
        }
	}

	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("OVERRIDE URL LOADING TOP", url);
            view.loadUrl(url);
	        if (url.contains("mp4")) {
                Log.i("OVERRIDE URL LOADING MP4", url.toString());
                Intent ihtml = new Intent(HTMLViewerActivity.this,
						VideoPlayActivity.class);
				ihtml.putExtra("videourl", url);
				startActivity(ihtml);
	            return false;
	        }else{
                Log.i("OVERRIDE URL LOADING ELSE", url);
	            return true;
	        }
	    }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("ON PAGE FINISHED", "");
            super.onPageFinished(view, url);
            // this is a hack to get the images to display when first loaded
            if(firstload){
            loadHTML();
            firstload = false;
            }

        }


    }
}
