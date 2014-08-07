package net.movelab.sudeau;

import net.movelab.sudeau.model.HighLight;
import net.movelab.sudeau.model.Step;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Switchboard extends Activity {
	
	private Button btn_manual;
	private Button btn_security;
	private Button btn_routes;
	private Button btn_credits;
	private EruletApp app;
	private SharedPreferences mPreferences;
	
	private int group1 = 1;
	private int first_id = Menu.FIRST;
	private int second_id = Menu.FIRST + 1;
	
	static final int REGISTRATION_REQUEST = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (app == null) { app = (EruletApp) getApplicationContext(); }
		setContentView(R.layout.activity_switchboard);
		initButtons();
		//See if we need to display the user registration
		mPreferences = getSharedPreferences("EruletPreferences", MODE_PRIVATE);
        
        boolean firstTime = mPreferences.getBoolean("first_time", true);
        
        tryToRegister();
                
        if (firstTime) {
        	//Show user manual, maybe?
        	//Toast.makeText(this, "First time!", Toast.LENGTH_SHORT).show();
        	mPreferences.edit().putBoolean("first_time", false).apply();
        }
                
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REGISTRATION_REQUEST) {
			if(resultCode == RESULT_OK){
				//The user registered ok
				//We write it to preferences, along with any other necessary data
				mPreferences.edit().putBoolean("registered_user", true).apply();				
				//And here we store all necessary tokens/etc for the user
			} else if (resultCode == RESULT_CANCELED) {
				//No luck
				/** ************************************************************
				 *  ************************************************************
				 *               JUST FOR TEST
				 *               REMOVE THIS ASAP
				 *  ************************************************************
				 *  ************************************************************
				 */
				mPreferences.edit().putBoolean("registered_user", true).apply();
			}
		}
	}	
	
	
	private void tryToRegister(){
		boolean userIsRegistered = mPreferences.getBoolean("registered_user", false);        
        if(!userIsRegistered){        	
        	if(Util.isOnline(getBaseContext())){
        		Intent intent = new Intent(this, RegistrationActivity.class);
				startActivityForResult(intent,REGISTRATION_REQUEST);
        	}else{
        		Toast.makeText(this, "No tens accés a xarxa inalàmbrica.", Toast.LENGTH_SHORT).show();
        	}
        }else{
        	Toast.makeText(this, "Ja estàs registrat.", Toast.LENGTH_SHORT).show();
        }
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(group1, first_id, first_id, "Configuració");
		menu.add(group1, second_id, second_id, "Registrar usuari");
		//getMenuInflater().inflate(R.menu.switchboard, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 1:
				Intent i = new Intent(Switchboard.this,EruletPreferencesActivity.class);				
				startActivity(i);
				break;
			case 2:
				tryToRegister();
				break;
			default:				
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void initButtons() {
		btn_manual = (Button) findViewById(R.id.btn_manual);
		btn_manual.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Intent i = new Intent(Switchboard.this, ManualActivity.class);
				Intent i = new Intent(Switchboard.this, ManualActivity.class);
				startActivity(i);
			}
		});			
		btn_security = (Button) findViewById(R.id.btn_security_info);
		btn_security.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this,
						SecurityRules.class);
				startActivity(i);
			}
		});
		btn_routes = (Button) findViewById(R.id.btn_routes);
		btn_routes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this,
						ChooseItineraryActivity.class);
				startActivity(i);
			}
		});
		btn_credits = (Button) findViewById(R.id.btn_credits);
		btn_credits.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this,
						CreditsActivity.class);
				startActivity(i);
			}
		});
		
	}

}
