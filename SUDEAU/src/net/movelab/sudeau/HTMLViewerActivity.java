package net.movelab.sudeau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HTMLViewerActivity extends FragmentActivity {

	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private EruletApp app;
	WebView wv;
    String base_url;
    boolean firstload;
    ArrayList<HtmlPathPair> html_list;
    String locale;
    private HighLight hl;
    Context context;
    Context thisContext = this;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html_viewer_activity);

        context = getApplicationContext();

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


        LinearLayout buttonArea = (LinearLayout) findViewById(R.id.buttonArea);
        buttonArea.setVisibility(View.VISIBLE);

        Bundle extras = getIntent().getExtras();
        if(extras!=null){
            int idHighLight = extras.getInt("highlight_id");
            // TODO make sure to pass highlight id when calling this activity now!!!
            hl = DataContainer.findHighLightById(idHighLight, app.getDataBaseHelper());
            if (hl.getReferences() != null) {

        // interactive images
        final Collection these_interactive_images = hl.getInteractiveImages();
        if (these_interactive_images != null && these_interactive_images.size() > 0) {

            ImageButton iiButton = (ImageButton) findViewById(R.id.iibutton);
            iiButton.setVisibility(View.VISIBLE);

            final CharSequence[] ii_items = new CharSequence[these_interactive_images.size()];
            final int[] ii_ids = new int[these_interactive_images.size()];

            int i = 0;
            for(InteractiveImage ii : hl.getInteractiveImages()){
                ii_items[i] = getString(R.string.interactive_pic) + (i + 1);
                ii_ids[i] = ii.getId();
                i++;
            }

            iiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInteractiveImageDialog(ii_items, ii_ids);
                }
                });

            }


                if(hl != null){
                    final ImageButton ratingButton = (ImageButton) findViewById(R.id.ratingButton);
                    ratingButton.setVisibility(View.VISIBLE);
                    ratingButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // custom dialog
                            final Dialog dialog = new Dialog(thisContext);
                            dialog.setContentView(R.layout.rating_bar_dialog);
                            dialog.setTitle(getResources().getString(R.string.highlight_ratings_title));
                            TextView ratingLabel = (TextView) dialog.findViewById(R.id.tvGlobalRating);
                            if(hl.getGlobalRating() >=0){
                                ratingLabel.setText(getResources().getString(R.string.average_rating) + ": " + String.format("%.2f", hl.getGlobalRating()));
                            }
                            RatingBar myRating = (RatingBar)dialog.findViewById(R.id.ratBarUser);
                            myRating.setStepSize(1.0f);

                            int userRating = 0;
                            if(hl.getUserRating() >= 0){
                                userRating = hl.getUserRating();
                            }
                            myRating.setRating(userRating);

                            myRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                                @Override
                                public void onRatingChanged(RatingBar ratingBar, float rating,
                                                            boolean fromUser) {
                                    hl.setUserRating((int)rating);
                                    hl.setUserRatingTime(System.currentTimeMillis());
                                    hl.setUserRatingUploaded(false);
                                }

                            });
                        Button saveButton = (Button) dialog.findViewById(R.id.alertOK);
                            saveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    app.getDataBaseHelper().getHlDataDao().update(hl);
                                    dialog.dismiss();
                                }
                            });
                            Button cancelButton = (Button) dialog.findViewById(R.id.alertCancel);
                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    });
                }

           html_list = getReferenceString();

           ArrayList<String> video_list = getVideoUris(html_list);

                // videos
                if (video_list != null && video_list.size() > 0) {

                    ImageButton videoButton = (ImageButton) findViewById(R.id.videobutton);
                    videoButton.setVisibility(View.VISIBLE);

                    final CharSequence[] video_labels = new CharSequence[video_list.size()];
                    final String[] video_uris = new String[video_list.size()];

                    int i = 0;
                    for(String video_uri : video_list){

                        Log.i("video", video_uri);
                        //TODO localize string
                        video_labels[i] = "Video" + (i + 1);
                        video_uris[i] = video_uri;
                        i++;
                    }

                    videoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showVideoDialog(video_labels, video_uris);
                        }
                    });

                }


                loadHTML(html_list);

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

    private ArrayList<String> getVideoUris(ArrayList<HtmlPathPair> html_list){
        ArrayList<String> video_list = new ArrayList<String>();
        for(HtmlPathPair html_path_pair : html_list){
            String[] s1 = html_path_pair.getHtmlString().split(".mp4");
            for(int i = 0; i < s1.length; i = i + 2){
                // TODO finish this
                if(false){
                    String this_path = html_path_pair.getHtmlPath();
                    video_list.add((new File(this_path, s1[i].split("href=\"")[s1[i].split("href=\"").length - 1] + ".mp4")).getAbsolutePath());
                }
            }
            }
        return video_list;
        }

    private class HtmlPathPair{

        private String html_string;
        private String html_path;

        public HtmlPathPair(String html_string, String html_path){
            this.html_path = html_path;
            this.html_string = html_string;
        }

        public String getHtmlString(){
            return html_string;
        }
        public String getHtmlPath(){
            return html_path;
        }

    }

    private ArrayList<HtmlPathPair> getReferenceString(){
                ArrayList<HtmlPathPair> html_list = new ArrayList<HtmlPathPair>();
                for(Reference ref : hl.getReferences()){
                    final int this_ref_id = ref.getId();
                    Reference r = DataContainer.findReferenceById(this_ref_id, app.getDataBaseHelper());
                    if(r != null && r.getHtmlPath(locale) != null && !r.getHtmlPath(locale).isEmpty()){
                        String[] url_chop = r.getHtmlPath(locale).split("/");
                        base_url = "file://";
                        for(int i = 0; i < (url_chop.length-1); i++){
                            base_url += url_chop[i] + "/";
                        }

                        File f = new File(r.getHtmlPath(locale));
                        String this_path = f.getPath();
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

                        html_list.add(new HtmlPathPair(modified_html_text, this_path));

                    }
            }

//                String modified_html_text = html_text.toString().replace("../", "" );
			return html_list;
	}

	private void loadHTML(ArrayList<HtmlPathPair> html_list){

        // for now we are simply going to use the first reference and ignore the rest. But for future TODO we can add some navigation to the others
        wv.loadDataWithBaseURL(base_url, html_list.get(0).getHtmlString(), "text/html","utf-8", null);
	}


    public void showInteractiveImageDialog(CharSequence[] ii_items, int[] ii_ids){

        final int[] these_ii_ids = ii_ids;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.choose_interactive_image))
                .setItems(ii_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(
                                HTMLViewerActivity.this,
                                InteractiveImageActivityHeatMap.class);
                        i.putExtra("int_image_id", these_ii_ids[which]);
                        dialog.dismiss();
                        startActivity(i);
                    }
                });

        builder.show();

    }

    public void showVideoDialog(CharSequence[] video_labels, String[] video_uris){

        final String[] these_video_uris = video_uris;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //TODO localize
        builder.setTitle("Choose video")
                .setItems(video_labels, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(
                                HTMLViewerActivity.this,
                                VideoPlayActivity.class);
                        i.putExtra("video_uri", these_video_uris[which]);
                        dialog.dismiss();
                        startActivity(i);
                    }
                });

        builder.show();

    }


    /**
	 * This allows navigation between a webview and a link opening another webview
	 */
	@Override
	public void onBackPressed() {
		WebView wv;
        wv = (WebView) findViewById(R.id.wb_webView);
        if(wv.canGoBack() && !wv.getUrl().equals("about:blank")){
        	wv.goBack();
            if(wv.getUrl().equals("about:blank")){
                loadHTML(html_list);
            }
        }else{
        	super.onBackPressed();
        }
	}

	private class MyWebViewClient extends WebViewClient {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
	        if (url.contains("mp4")) {
                Intent ihtml = new Intent(HTMLViewerActivity.this,
						VideoPlayActivity.class);
				ihtml.putExtra("videourl", url);
				startActivity(ihtml);
	            return false;
	        }else{
	            return true;
	        }
	    }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // this is a hack to get the images to display when first loaded
            if(firstload){
            loadHTML(html_list);
            firstload = false;
            }

        }


    }
}
