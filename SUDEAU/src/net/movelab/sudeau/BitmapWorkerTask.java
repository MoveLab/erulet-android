package net.movelab.sudeau;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Taken from 
 * https://developer.android.com/training/displaying-bitmaps/process-bitmap.html
 * and slightly tweaked
 * 
 * @author a.escobar
 *
 */

public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
	
	private final WeakReference<ImageView> imageViewReference;
    private String pathName;
    private int width;
    private int height;

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

	@Override
	protected Bitmap doInBackground(String... params) {
		pathName = params[0];
		width = Integer.parseInt(params[1]);
		height = Integer.parseInt(params[2]);
		return Util.decodeSampledBitmapFromFile(pathName, width, height);        
	}
	
	// Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
    	

}
