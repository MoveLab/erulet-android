package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
	
	private int group1 = 1;
	private int first_id = Menu.FIRST;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (app == null) { app = (EruletApp) getApplicationContext(); }
		setContentView(R.layout.activity_switchboard);
		initButtons();
		//See if we need to display the user registration
        SharedPreferences preferences = getSharedPreferences("EruletPreferences", MODE_PRIVATE);
        boolean firstTime = preferences.getBoolean("first_time", true);
        
        if (firstTime) {
            //Intent intent = new Intent(this, RegistrationActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            intent.putExtra("first_time", true);
//            startActivity(intent);
        	Toast.makeText(this, "First time!", Toast.LENGTH_SHORT).show();
            preferences.edit().putBoolean("first_time", false).apply();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(group1, first_id, first_id, "Configuració");
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
