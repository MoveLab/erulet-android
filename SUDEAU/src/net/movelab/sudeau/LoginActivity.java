package net.movelab.sudeau;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

    boolean flag = false;
    Context context = this;
    String current_url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        WebView myWebView = (WebView) findViewById(R.id.wvRegistration);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(UtilLocal.URL_LOGIN);


        myWebView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("login", "should override loading top. url is " + url);
                if(url.contains("show_credentials")) {
                    Log.i("login", "should override loading inside");

                    new GetJSONResponseTask().execute(context);
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

    public class GetJSONResponseTask extends AsyncTask<Context, Integer, Boolean> {

        ProgressDialog prog;

        int myProgress;
        int resultFlag;

        int OFFLINE = 0;
        int UPLOAD_ERROR = 1;
        int DATABASE_ERROR = 2;
        int SUCCESS = 3;
        int JSON_ERROR = 4;

        HttpResponse response;
        JSONObject responseJson;
        int statusCode = -1;
        int server_id = -1;

        private String username = "";
        private String token = "";

        @Override
        protected void onPreExecute() {

            resultFlag = SUCCESS;

            prog = new ProgressDialog(context);
            prog.setTitle("Uploading itinerary");
            prog.setIndeterminate(false);
            prog.setMax(100);
            prog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            prog.show();

            myProgress = 0;

        }

        protected Boolean doInBackground(Context... context) {

            try{
                URL aURL = new URL(current_url);
                HttpURLConnection conn = (HttpURLConnection) aURL.openConnection();
                conn.setRequestMethod("GET");
                conn.setReadTimeout(10000); // millis
                conn.setConnectTimeout(15000); // millis
                conn.setDoOutput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                String response = Util.parseInputStream(context[0], is);
                Log.i("login", "response string = " + response);

                try{
                    JSONObject j = new JSONObject(response);
                    username = j.optString("username");
                    token = j.optString("token");
                    if(username != null && token != null){
                        PropertyHolder.setUserName(username);
                        PropertyHolder.setUserKey(token);

                        Log.i("login", "username now = " + PropertyHolder.getUserName());
                        Log.i("login", "token now = " + PropertyHolder.getUserKey());

                        return true;
                    }
                } catch(JSONException e){
                    Log.e("login", "should override; json exception");
                }
            } catch(IOException e){
                Log.e("login", "should override; io exception");
            }

            return false;
        }

        protected void onProgressUpdate(Integer... progress) {

            prog.setProgress(progress[0]);
        }

        protected void onPostExecute(Boolean result) {

            prog.dismiss();

            if (result) {
                Util.toast(
                        context,"Logged in as " + username);
                finish();

            } else {
//TODO
            }

        }
    }


}
