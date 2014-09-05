package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_activity);	
		VideoView view = (VideoView)findViewById(R.id.highLightVideo);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			//String path = "file:///sdcard/test_n.mp4";
			String path = extras.getString("videourl");
			view.setVideoURI(Uri.parse(path));
			view.start();
		}
	}
	
}
