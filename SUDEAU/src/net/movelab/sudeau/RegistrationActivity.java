package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class RegistrationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        WebView myWebView = (WebView) findViewById(R.id.wvRegistration);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(UtilLocal.URL_REGISTRATION);


        myWebView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("TOKEN 1:", "url is " + url);
                if(url.contains("token=")){
                    String[] token_chop =  url.split("token=")[1].split("username=");
                    String token = token_chop[0];
                    String username = token_chop[1];
                            //.substring(0, token_chop[1].length()-1);
                    Log.i("parsed as: ", "token:" + token + ", username:" + username);
                    finish();
                    return true;
                }
                return false;
            }



        });

    }

    public void registrationOk() {
        setResult(RESULT_OK);
        finish();
    }

    public void registrationFailed() {
        Intent intentMessage = new Intent();
        intentMessage.putExtra("REGISTRATION_FAIL", "Why did registration fail?");
        setResult(RESULT_CANCELED);
        finish();
    }

    public void registrationCanceled() {
        setResult(RESULT_CANCELED);
        finish();
    }

}
