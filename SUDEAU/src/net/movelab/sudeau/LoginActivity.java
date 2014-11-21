package net.movelab.sudeau;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


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
        myWebView.loadUrl(UtilLocal.URL_LOGIN);

        myWebView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e("login", "page finished top. url is " + url);
                if(url.contains("/show_credentials/")){
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
            Log.i("login", "processing html top jsonstring: " + jsonstring);
            try {
                JSONObject j = new JSONObject(jsonstring);
                String username = j.optString("username");
                String token = j.optString("token");
                if (username != null && token != null) {
                    PropertyHolder.setUserName(username);
                    PropertyHolder.setUserKey(token);
                    Log.i("login", "username now = " + PropertyHolder.getUserName());
                    Log.i("login", "token now = " + PropertyHolder.getUserKey());
                    Util.toast(context, "You are now logged in as " + username);
                    finish();
                }
            } catch (JSONException e) {
                Log.e("login", "processing html; json exception");
            }
        }

    }


}
