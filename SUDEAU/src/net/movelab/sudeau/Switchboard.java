package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class Switchboard extends Activity {

	private Button btn_info;
	private Button btn_routes;
	private Button btn_spc_eco;
	private Button btn_refinfo;
	private Button btn_other_usrs;
	private Button btn_dbtest;

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
		btn_info = (Button) findViewById(R.id.btn_info);
		btn_info.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this, InfoActivity.class);
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
		btn_spc_eco = (Button) findViewById(R.id.btn_spc_eco);
		btn_spc_eco.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this, PlaceInfoActivity.class);
				startActivity(i);
			}
		});
		btn_refinfo = (Button) findViewById(R.id.btn_refinfo);
		btn_refinfo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this,
						ReferenceInfoActivity.class);
				startActivity(i);
			}
		});
		btn_other_usrs = (Button) findViewById(R.id.btn_other_usrs);
		btn_other_usrs.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this,
						OtherUsersActivity.class);
				startActivity(i);
			}
		});
		
		btn_dbtest = (Button) findViewById(R.id.btn_dbtest);
		btn_dbtest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(Switchboard.this,
						DriveActivity.class);
				startActivity(i);
			}
		});

		
	}

}
