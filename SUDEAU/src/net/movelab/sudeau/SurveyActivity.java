package net.movelab.sudeau;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class SurveyActivity extends FragmentActivity {

    Context context = this;

    private WebView myWebView;

    String lang;
    String survey_type;
    String route_server_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);

        Intent i = getIntent();
        if(i.hasExtra("survey_type")){
            survey_type = i.getStringExtra("survey_type");
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
        myWebView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("ok")){
                    Util.toast(context, "Thank you!");
                    finish();
                    return true;
                }
                return false;
            }

        });

        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

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
        b.setTitle("Eth Holet Needs the Internet");
        b.setMessage("To complete the survey, you need to have an internet connection. Please check your settings and return to this activity when you have a connection");
        b.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });
        b.show();
    }


}
