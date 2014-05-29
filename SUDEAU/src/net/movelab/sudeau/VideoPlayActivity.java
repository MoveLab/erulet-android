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
		VideoView view = (VideoView)findViewById(R.id.videoView1);		
		String path = "file:///sdcard/test_n.mp4";
		view.setVideoURI(Uri.parse(path));
		view.start();
	}
	
}
