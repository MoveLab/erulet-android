// Copied from Space Mapper. We will need to adapt this to make it work, but it could be useful. - JP

package net.movelab.sudeau;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Gets the amount of battery life remaining on this phone. This is taken
 * entirely from the Human Mobility Project code.
 * <p>
 * Dependencies: CountdownDisplay.java, FixGet.java
 * 
 * @author Necati E. Ozgencil
 */
public class PowerSensor extends BroadcastReceiver {
	/** The phone's remaining battery life */
	public static int PowerLevel = -1; // was 100
	private static PowerSensor ps = null;

	/**
	 * Whether the phone is currently charging or not. True if phone is
	 * charging.
	 */
	public static boolean IsPlugged = false;

	public static void init(Context context) {
		if (ps == null)
			ps = new PowerSensor();
		context.registerReceiver(ps, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	}

	/**
	 * Amount of battery life has changed from before. Make this new value
	 * available for the CountdownDisplay application to see and use.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
			int level = intent.getIntExtra("level", 0);
			int scale = intent.getIntExtra("scale", 100);
			PowerLevel = (int) Math.round(level * 100.0 / scale);
			IsPlugged = intent.getIntExtra("plugged", 0) != 0;
		}
	}
}
