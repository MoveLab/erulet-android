package net.movelab.sudeau;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CreditsActivity extends FragmentActivity {

    Context context = this;

    private static final String BEFORE_LEAVING_URL_OC = UtilLocal.URL_SERVULET + "oc/about/_mob/";
    private static final String BEFORE_LEAVING_URL_ES = UtilLocal.URL_SERVULET + "es/about/_mob/";
    private static final String BEFORE_LEAVING_URL_CA = UtilLocal.URL_SERVULET + "ca/about/_mob/";
    private static final String BEFORE_LEAVING_URL_FR = UtilLocal.URL_SERVULET + "fr/about/_mob/";
    private static final String BEFORE_LEAVING_URL_EN = UtilLocal.URL_SERVULET + "en/about/_mob/";

    private static final String BEFORE_LEAVING_URL_OFFLINE_OC = "file:///android_asset/about_oc.html";
    private static final String BEFORE_LEAVING_URL_OFFLINE_ES = "file:///android_asset/about_es.html";
    private static final String BEFORE_LEAVING_URL_OFFLINE_CA = "file:///android_asset/about_ca.html";
    private static final String BEFORE_LEAVING_URL_OFFLINE_FR = "file:///android_asset/about_fr.html";
    private static final String BEFORE_LEAVING_URL_OFFLINE_EN = "file:///android_asset/about_en.html";

    private WebView myWebView;

    String lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);

        // TODO languages
        lang = "ca";

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {


        // if (Util.isOnline(context)) {
        setContentView(R.layout.html_viewer_activity);

        myWebView = (WebView) findViewById(R.id.wb_webView);
        myWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

                if (lang.equals("ca"))
                    myWebView.loadUrl(BEFORE_LEAVING_URL_OFFLINE_CA);
                else if (lang.equals("es"))
                    myWebView.loadUrl(BEFORE_LEAVING_URL_OFFLINE_ES);
                else if (lang.equals("en"))
                    myWebView.loadUrl(BEFORE_LEAVING_URL_OFFLINE_EN);
                else if (lang.equals("fr"))
                    myWebView.loadUrl(BEFORE_LEAVING_URL_OFFLINE_FR);
                else
                    myWebView.loadUrl(BEFORE_LEAVING_URL_OFFLINE_OC);

            }

        });

        myWebView.getSettings().setAllowFileAccess(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        if (Build.VERSION.SDK_INT >= 7) {
            WebViewApi7.api7settings(myWebView, context);
        }

        if (!Util.isOnline(context)) { // loading offline only if not online
            myWebView.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        if (lang.equals("ca"))
            myWebView.loadUrl(BEFORE_LEAVING_URL_CA);
        else if (lang.equals("es"))
            myWebView.loadUrl(BEFORE_LEAVING_URL_ES);
        else if (lang.equals("en"))
            myWebView.loadUrl(BEFORE_LEAVING_URL_EN);
        else if (lang.equals("fr"))
            myWebView.loadUrl(BEFORE_LEAVING_URL_FR);
        else
            myWebView.loadUrl(BEFORE_LEAVING_URL_OC);

        super.onResume();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Util.isOnline(context)) {
            // Check if the key event was the Back button and if there's history
            if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
                myWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}
