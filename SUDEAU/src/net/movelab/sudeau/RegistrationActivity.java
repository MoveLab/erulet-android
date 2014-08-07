package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class RegistrationActivity extends Activity {
	
	private static String REGISTRATION_URL = "http://www.google.com";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.registration);
		WebView myWebView = (WebView) findViewById(R.id.wvRegistration);
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		myWebView.loadUrl(REGISTRATION_URL);
	}
	
	public void registrationOk(){
		setResult(RESULT_OK);
		finish();
	}
	
	public void registrationFailed(){
		Intent intentMessage = new Intent();
		intentMessage.putExtra("REGISTRATION_FAIL", "Why did registration fail?");
		setResult(RESULT_CANCELED);
		finish();
	}
	
	public void registrationCanceled(){
		setResult(RESULT_CANCELED);
		finish();
	}

}
