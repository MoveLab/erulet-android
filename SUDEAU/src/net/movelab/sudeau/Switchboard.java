package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class Switchboard extends Activity {
	
	private Button btn_manual;
	private Button btn_security;
	private Button btn_routes;
	private Button btn_credits;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_switchboard);
		initButtons();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.switchboard, menu);
		return true;
	}

	private void initButtons() {
		btn_manual = (Button) findViewById(R.id.btn_manual);
		btn_manual.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Intent i = new Intent(Switchboard.this, ManualActivity.class);
				Intent i = new Intent(Switchboard.this, EditRouteActivity.class);
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
