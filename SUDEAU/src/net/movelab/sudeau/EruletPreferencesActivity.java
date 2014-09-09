package net.movelab.sudeau;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class EruletPreferencesActivity extends Activity {
	
	private RadioGroup rbPreferredLocaleSelector;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mPrefEditor;
	private EruletApp app;
	private int formerSelectedRadioButton;
    private EditText etFixInterval;
    private Button bOK;
    Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        context = getApplicationContext();
		setContentView(R.layout.preferences);
		if (app == null) {
            app = (EruletApp) getApplicationContext();
        }		
		mPreferences = app.getPrefs();
	    mPrefEditor = mPreferences.edit();
	    rbPreferredLocaleSelector = (RadioGroup)findViewById(R.id.radioLang);
		updateRadioButtonState();

        etFixInterval = (EditText) findViewById(R.id.etFixInterval);
        bOK = (Button) findViewById(R.id.bOK);
        bOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = etFixInterval.getText().toString();
                if(!input.matches("")){
                    PropertyHolder.init(context);
                    PropertyHolder.setAlarmInterval(1000*Long.parseLong(input, 10));
                };
                finish();
            }
        });
	}
	
	private void updateRadioButtonState(){
		String pref_locale = mPreferences.getString("pref_locale", "");
		if(pref_locale.equalsIgnoreCase("ca")){
			rbPreferredLocaleSelector.check(R.id.rbCatala);
			formerSelectedRadioButton = R.id.rbCatala;
		}else if(pref_locale.equalsIgnoreCase("ar")){
			rbPreferredLocaleSelector.check(R.id.rbAranes);
			formerSelectedRadioButton = R.id.rbAranes;
		}else if(pref_locale.equalsIgnoreCase("es")){
			rbPreferredLocaleSelector.check(R.id.rbSpanish);
			formerSelectedRadioButton = R.id.rbSpanish;
		}else if(pref_locale.equalsIgnoreCase("fr")){
			rbPreferredLocaleSelector.check(R.id.rbFrench);
			formerSelectedRadioButton = R.id.rbFrench;
		}else if(pref_locale.equalsIgnoreCase("en")){
			rbPreferredLocaleSelector.check(R.id.rbEnglish);
			formerSelectedRadioButton = R.id.rbEnglish;
		}else{
			rbPreferredLocaleSelector.check(R.id.rbDevice);
			formerSelectedRadioButton = R.id.rbDevice;
		}
	}
	
	public void onRadioButtonClicked(View view){
		
		final boolean checked = ((RadioButton) view).isChecked();
	    final int selectedRadioButtonId = view.getId();	    	    
	    //Toast.makeText(getApplicationContext(), getString(R.string.language_restart), Toast.LENGTH_LONG).show();
	    
	    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            switch (which){
	            case DialogInterface.BUTTON_POSITIVE:
	            	switch(selectedRadioButtonId) {
		            case R.id.rbCatala:
			            if (checked){	            	
			            	mPrefEditor.putString("pref_locale", "ca");
			            	mPrefEditor.commit();	            	
			            }
			            break;
			        case R.id.rbAranes:
			            if (checked){
			            	mPrefEditor.putString("pref_locale", "ar");
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
		        	formerSelectedRadioButton = selectedRadioButtonId;
	            	app.applyLocaleSettings();
	        	    app.restart();	                

	            case DialogInterface.BUTTON_NEGATIVE:
	                //No button clicked
	            	rbPreferredLocaleSelector.check(formerSelectedRadioButton);	            	
	                break;
	            }
	        }
	    };

	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(getString(R.string.restart_needed))
	    	.setPositiveButton(getString(R.string.restart_now), dialogClickListener)
	        .setNegativeButton(getString(R.string.cancel), dialogClickListener).show();		
	}
}
