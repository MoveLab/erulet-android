package net.movelab.sudeau;

        import android.app.Activity;
        import android.content.Context;
        import android.content.Intent;
        import android.content.res.Resources;
        import android.os.Build;
        import android.os.Bundle;
        import android.view.KeyEvent;
        import android.support.v4.app.FragmentActivity;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.webkit.WebSettings;
        import android.webkit.WebView;
        import android.webkit.WebViewClient;
        import net.movelab.sudeau.R;

/**
 * Main activity that user interacts with while performing the search..
 *
 * @author John R.B. Palmer
 */
public class ManualActivity extends FragmentActivity {

    Context context = this;

    private static final String MANUAL_URL_OC = UtilLocal.URL_SERVULET + "oc/manual/_mob/";
    private static final String MANUAL_URL_ES = UtilLocal.URL_SERVULET + "es/manual/_mob/";
    private static final String MANUAL_URL_CA = UtilLocal.URL_SERVULET + "ca/manual/_mob/";
    private static final String MANUAL_URL_FR = UtilLocal.URL_SERVULET + "fr/manual/_mob/";
    private static final String MANUAL_URL_EN = UtilLocal.URL_SERVULET + "en/manual/_mob/";

    private static final String MANUAL_URL_OFFLINE_OC = "file:///android_asset/manual_oc.html";
    private static final String MANUAL_URL_OFFLINE_ES = "file:///android_asset/manual_es.html";
    private static final String MANUAL_URL_OFFLINE_CA = "file:///android_asset/manual_ca.html";
    private static final String MANUAL_URL_OFFLINE_FR = "file:///android_asset/manual_fr.html";
    private static final String MANUAL_URL_OFFLINE_EN = "file:///android_asset/manual_en.html";

    private WebView myWebView;

    String lang;
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PropertyHolder.isInit())
            PropertyHolder.init(context);


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
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

                if (lang.equals("ca"))
                    myWebView.loadUrl(MANUAL_URL_OFFLINE_CA);
                else if (lang.equals("es"))
                    myWebView.loadUrl(MANUAL_URL_OFFLINE_ES);
                else if (lang.equals("en"))
                    myWebView.loadUrl(MANUAL_URL_OFFLINE_EN);
                else if (lang.equals("fr"))
                    myWebView.loadUrl(MANUAL_URL_OFFLINE_FR);
                else
                    myWebView.loadUrl(MANUAL_URL_OFFLINE_OC);

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
            myWebView.loadUrl(MANUAL_URL_CA);
        else if (lang.equals("es"))
            myWebView.loadUrl(MANUAL_URL_ES);
        else if (lang.equals("en"))
            myWebView.loadUrl(MANUAL_URL_EN);
        else if (lang.equals("fr"))
            myWebView.loadUrl(MANUAL_URL_FR);
        else
            myWebView.loadUrl(MANUAL_URL_OC);

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
