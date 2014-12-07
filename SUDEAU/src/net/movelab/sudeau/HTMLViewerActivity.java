package net.movelab.sudeau;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import net.movelab.sudeau.database.DataContainer;
import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.InteractiveImage;
import net.movelab.sudeau.model.Reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

public class HTMLViewerActivity extends FragmentActivity {

    private int group1 = 1;
    private int first_id = Menu.FIRST;
    private EruletApp app;
    WebView wv;
    String base_url;
    boolean firstload;
    String locale;
    boolean onInitialPage = false;
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
            Log.d("Highlight ID: ", "" + idHighLight);
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


                Log.i("ratings", "htmlview top of code");
                if(hl != null){
                    Log.i("ratings", "hl not null");

                    final ImageButton ratingButton = (ImageButton) findViewById(R.id.ratingButton);
                    ratingButton.setVisibility(View.VISIBLE);
                    ratingButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // custom dialog
                            final Dialog dialog = new Dialog(thisContext);
                            dialog.setContentView(R.layout.rating_bar_dialog);
                            dialog.setTitle(getResources().getString(R.string.highlight_ratings));
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
                                    Intent ratingUploadIntent = new Intent(HTMLViewerActivity.this, UploadRatings.class);
                                    startService(ratingUploadIntent);

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


            } else{
// TODO fallback on normal view
            } }else{
// TODO fallback on normal view
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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


                String modified_html_text = html_text.toString().replace("../", "file://" + Environment.getExternalStorageDirectory().getPath() + "/" + Util.baseFolder + "/" ).replace("</head>","</head><body>").replace("</html>", "</body></html>").replace("’", "&#39;").replace("`", "&#39;").replace("\u0092", "&#39;");

                Log.i("htmltext ", modified_html_text);

                html_list.add(modified_html_text);

            }
        }

//                String modified_html_text = html_text.toString().replace("../", "" );
        return html_list;
    }

    private void loadHTML(){

        // for now we are simply going to use the first reference and ignore the rest. But for future TODO we can add some navigation to the others
        if(getReferenceString() !=null && getReferenceString().size() >0){
        wv.loadDataWithBaseURL(base_url, getReferenceString().get(0), "text/html","utf-8", null);
        }
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

    /**
     * This allows navigation between a webview and a link opening another webview
     */
    @Override
    public void onBackPressed() {
        WebView wv;
        wv = (WebView) findViewById(R.id.wb_webView);
        if(wv.canGoBack() && !onInitialPage){
                loadHTML();
                onInitialPage = true;
        }
        else{
            super.onBackPressed();
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i("OVERRIDE URL LOADING TOP", url);
            view.loadUrl(url);
            if (url.contains("mp4")) {
                Log.i("OVERRIDE URL LOADING MP4", url);
                Intent ihtml = new Intent(HTMLViewerActivity.this,
                        VideoPlayActivity.class);
                ihtml.putExtra("video_uri", url);
                onInitialPage = true;
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
