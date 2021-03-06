package net.movelab.sudeau;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SurveyActivity extends FragmentActivity {

    Context context = this;

    private WebView myWebView;

    String lang;
    String survey_type;
    String route_server_id = "";

    public static String SURVEY_TYPE_KEY = "survey_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);

        Intent i = getIntent();
        if(i.hasExtra("survey_type")){
            survey_type = i.getStringExtra(SURVEY_TYPE_KEY);
        }
        if(i.hasExtra("route_server_id")){
            route_server_id = i.getStringExtra("route_server_id");
        }

    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {

        lang = PropertyHolder.getLocale();

        // if (Util.isOnline(context)) {
        setContentView(R.layout.html_viewer_activity);
        myWebView = (WebView) findViewById(R.id.wb_webView);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.e("login", "page started top. url is " + url);
                if(url.contains("/ok/")){
                    Util.toast(context, getResources().getString(R.string.survey_submitted));
                    finish();
                }}

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("login", "page finished top. url is " + url);
                if(url.contains("/ok/")){
                    Util.toast(context, getResources().getString(R.string.survey_submitted));
                    finish();
            }}

        });


        if (Build.VERSION.SDK_INT >= 7) {
            WebViewApi7.api7settings(myWebView, context);
        }


        if (!Util.isOnline(context)) { // loading offline only if not online

            showOfflineDialog();

        } else{

        String this_url = UtilLocal.URL_SERVULET + lang + "/survey/mob/" + survey_type + "/" + route_server_id;
        myWebView.loadUrl(this_url);

        }

        super.onResume();

    }


    public void showOfflineDialog(){
        AlertDialog.Builder b = new AlertDialog.Builder(
                SurveyActivity.this);
        b.setIcon(R.drawable.ic_launcher);
        b.setTitle(getResources().getString(R.string.internet_needed_title));
        b.setMessage(getResources().getString(R.string.internet_needed_message));
        b.setNeutralButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        b.show();
    }


}
