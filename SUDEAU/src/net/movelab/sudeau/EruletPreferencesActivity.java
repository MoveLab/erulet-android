package net.movelab.sudeau;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;


public class EruletPreferencesActivity extends Activity {
	
	private RadioGroup rbPreferredLocaleSelector;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mPrefEditor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.preferences);
		mPreferences = getSharedPreferences("EruletPreferences", MODE_PRIVATE);
	    mPrefEditor = mPreferences.edit();
	    rbPreferredLocaleSelector = (RadioGroup)findViewById(R.id.radioLang);
		updateRadioButtonState();
	}
	
	private void updateRadioButtonState(){
		String pref_locale = mPreferences.getString("pref_locale", "");
		if(pref_locale.equalsIgnoreCase("ca")){
			rbPreferredLocaleSelector.check(R.id.rbCatala);
		}else if(pref_locale.equalsIgnoreCase("gsc")){
			rbPreferredLocaleSelector.check(R.id.rbAranes);
		}else if(pref_locale.equalsIgnoreCase("es")){
			rbPreferredLocaleSelector.check(R.id.rbSpanish);
		}else if(pref_locale.equalsIgnoreCase("fr")){
			rbPreferredLocaleSelector.check(R.id.rbFrench);
		}else if(pref_locale.equalsIgnoreCase("en")){
			rbPreferredLocaleSelector.check(R.id.rbEnglish);
		}else{
			rbPreferredLocaleSelector.check(R.id.rbCatala);
		}
	}
	
	public void onRadioButtonClicked(View view){
	    boolean checked = ((RadioButton) view).isChecked();	    	    
	    switch(view.getId()) {
	        case R.id.rbCatala:
	            if (checked){	            	
	            	mPrefEditor.putString("pref_locale", "ca");
	            	mPrefEditor.commit();	            	
	            }
	            break;
	        case R.id.rbAranes:
	            if (checked){
	            	mPrefEditor.putString("pref_locale", "gsc");
	            	mPrefEditor.commit();	            	
	            }
	            break;
	        case R.id.rbSpanish:
	            if (checked){
	            	mPrefEditor.putString("pref_locale", "es");
	            	mPrefEditor.commit();	            	
	            }
	            break;
	        case R.id.rbFrench:
	            if (checked){
	            	mPrefEditor.putString("pref_locale", "fr");
	            	mPrefEditor.commit();	            	
	            }
	            break;
	        case R.id.rbEnglish:
	            if (checked){
	            	mPrefEditor.putString("pref_locale", "en");
	            	mPrefEditor.commit();	            	
	            }
	            break;
	        default:
	        	if(checked){
	        		mPrefEditor.putString("pref_locale", "");
	            	mPrefEditor.commit();	            	
	        	}
	        	break;
	    }
	    Toast.makeText(getApplicationContext(), getString(R.string.restart_needed), Toast.LENGTH_LONG).show();
	}
}
