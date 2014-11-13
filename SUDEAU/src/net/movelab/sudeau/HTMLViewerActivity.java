package net.movelab.sudeau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class HTMLViewerActivity extends Activity {

	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private EruletApp app;
	WebView wv;
    String base_url;
    boolean firstload;
    String locale;
    private HighLight hl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_viewer_activity);

        Context context = getApplicationContext();

        if (app == null) {
            app = (EruletApp) context;
        }

        firstload = true;

        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);
        locale = PropertyHolder.getLocale();

		wv = (WebView) findViewById(R.id.wb_webView);
        wv.setWebChromeClient(new WebChromeClient());
		wv.setWebViewClient(new MyWebViewClient());
        wv.getSettings().setAllowFileAccess(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);


        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            int idHighLight = extras.getInt("highlight_id");
            Log.d("Highlight ID: ", "" + idHighLight);
            // TODO make sure to pass highlight id when calling this activity now!!!
            hl = DataContainer.findHighLightById(idHighLight, app.getDataBaseHelper());
            if (hl.getReferences() != null) {

        // interactive images
        if (hl.getInteractiveImages() != null && hl.getInteractiveImages().size() > 0) {

            LinearLayout iibuttonarea = (LinearLayout) findViewById(R.id.iibuttonarea);
            iibuttonarea.setVisibility(View.VISIBLE);
            for(InteractiveImage ii : hl.getInteractiveImages()){

                Button iiButton = new Button(this);
                iiButton.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
                iiButton.setPadding(10, 10, 10, 10);
                iiButton.setText("Interactive Image");
                iiButton.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.pin_interactiveimage, 0, 0, 0);
                iiButton.setGravity(Gravity.CENTER);
                iiButton.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);

                final int this_ii_id = ii.getId();

                iibuttonarea.addView(iiButton);
                iiButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(
                                HTMLViewerActivity.this,
                                InteractiveImageActivityHeatMap.class);
                        i.putExtra("int_image_id", this_ii_id);
                        startActivity(i);
                    }
                });

            }
        }



        loadHTML();

            } else{
// TODO fallback on normal view
            } }else{
// TODO fallback on normal view
            }
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

	private ArrayList<String> getReferenceString(){
                ArrayList<String> html_list = new ArrayList<String>();
                for(Reference ref : hl.getReferences()){
                    final int this_ref_id = ref.getId();
                    Reference r = DataContainer.findReferenceById(this_ref_id, app.getDataBaseHelper());
                    Log.d("Ref html path", r.getHtmlPath(locale));
                    if(r != null && r.getHtmlPath(locale) != null && !r.getHtmlPath(locale).isEmpty()){
                        Log.d("Reference URL: ", "file://" + r.getHtmlPath(locale));
                        String[] url_chop = r.getHtmlPath(locale).split("/");
                        base_url = "file://";
                        for(int i = 0; i < (url_chop.length-1); i++){
                            base_url += url_chop[i] + "/";
                        }

                        Log.d("base URL: ", base_url);

                        File f = new File(r.getHtmlPath(locale));

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


                        String modified_html_text = html_text.toString().replace("../", "file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Util.baseFolder + "/" ).replace("</head>","</head><body>").replace("</html>", "</body></html>").replace("holet-ref-style.css", "erholet-ref-style.css");

                        Log.i("htmltext ", modified_html_text);

                        html_list.add(modified_html_text);

                    }
            }

//                String modified_html_text = html_text.toString().replace("../", "" );
			return html_list;
	}

	private void loadHTML(){

        // for now we are simply going to use the first reference and ignore the rest. But for future TODO we can add some navigation to the others
        wv.loadDataWithBaseURL(base_url, getReferenceString().get(0), "text/html","utf-8", null);
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
