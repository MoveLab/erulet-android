package net.movelab.sudeau;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


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

        // To stop keyboard from popping up immediately and blocking everything
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        if (app == null) {
            app = (EruletApp) getApplicationContext();
        }
        if(!PropertyHolder.isInit())
            PropertyHolder.init(context);
		mPreferences = app.getPrefs();
	    mPrefEditor = mPreferences.edit();
	    rbPreferredLocaleSelector = (RadioGroup)findViewById(R.id.radioLang);
		updateRadioButtonState();


        String version_name = "";
        TextView version_text = (TextView) findViewById(R.id.version_text);
        try {
            PackageInfo manager=getPackageManager().getPackageInfo(getPackageName(), 0);
            version_name = manager.versionName;
            version_text.setText("Holet version: " + version_name);
        } catch (PackageManager.NameNotFoundException e) {
            //Handle exception
        }


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
		String pref_locale = PropertyHolder.getLocale();
		if(pref_locale.equalsIgnoreCase(PropertyHolder.CATALAN)){
			rbPreferredLocaleSelector.check(R.id.rbCatala);
			formerSelectedRadioButton = R.id.rbCatala;
		}else if(pref_locale.equalsIgnoreCase(PropertyHolder.ARANESE)){
			rbPreferredLocaleSelector.check(R.id.rbAranes);
			formerSelectedRadioButton = R.id.rbAranes;
		}else if(pref_locale.equalsIgnoreCase(PropertyHolder.SPANISH)){
			rbPreferredLocaleSelector.check(R.id.rbSpanish);
			formerSelectedRadioButton = R.id.rbSpanish;
		}else if(pref_locale.equalsIgnoreCase(PropertyHolder.FRENCH)){
			rbPreferredLocaleSelector.check(R.id.rbFrench);
			formerSelectedRadioButton = R.id.rbFrench;
		}else if(pref_locale.equalsIgnoreCase(PropertyHolder.ENGLISH)){
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
                            PropertyHolder.setLocale(PropertyHolder.CATALAN);
			            }
			            break;
			        case R.id.rbAranes:
			            if (checked){
                            PropertyHolder.setLocale(PropertyHolder.ARANESE);
			            }
			            break;
			        case R.id.rbSpanish:
			            if (checked){
                            PropertyHolder.setLocale(PropertyHolder.SPANISH);
			            }
			            break;
			        case R.id.rbFrench:
			            if (checked){
                            PropertyHolder.setLocale(PropertyHolder.FRENCH);
			            }
			            break;
			        case R.id.rbEnglish:
			            if (checked){
                            PropertyHolder.setLocale(PropertyHolder.ENGLISH);
			            }
			            break;
			        default:
			        	if(checked){
                            PropertyHolder.setLocale("");
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
