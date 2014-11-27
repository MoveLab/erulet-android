package net.movelab.sudeau;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends Activity {

    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        WebView myWebView = (WebView) findViewById(R.id.wvRegistration);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(Util.getLocalizedLoginUrl(context));

        myWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("login", "page finished top. url is " + url);
                if (url.contains("/show_credentials/")) {
                    view.loadUrl("javascript:window.HTMLOUT.processHTML(document.getElementById('credentials').innerHTML);");
                }
            }

        });


    }

    public void registrationOk() {
        setResult(RESULT_OK);
        finish();
    }

    public void registrationFailed() {
        Intent intentMessage = new Intent();
        intentMessage.putExtra("REGISTRATION_FAIL", "fail");
        setResult(RESULT_CANCELED);
        finish();
    }

    public void registrationCanceled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processHTML(String jsonstring) {
            try {
                JSONObject j = new JSONObject(jsonstring);
                String username = j.optString("username");
                String token = j.optString("token");
                if (username != null && token != null) {
                    PropertyHolder.setUserName(username);
                    PropertyHolder.setUserKey(token);
                    Util.toast(context, getResources().getString(R.string.now_logged_in_as) + " " + username);
                    finish();
                }
            } catch (JSONException e) {
                Log.e("login", "processing html; json exception");
            }
        }

    }


}
