package net.movelab.sudeau;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.video_activity);	
		VideoView view = (VideoView)findViewById(R.id.highLightVideo);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			MediaController mediaController = new MediaController(this);
       		mediaController.setAnchorView(view);
       		view.setMediaController(mediaController);
			String path = extras.getString("video_uri");
			view.setVideoURI(Uri.parse(path));
			view.start();
		}
	}

    @Override
    public void onBackPressed() {

        finish();
    }


}
