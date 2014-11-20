// Copied from Space Mapper. Will need to be adapted.

package net.movelab.sudeau;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

/**
 * Schedules alarm to run FixGet service upon boot up of the phone, if a
 * registered user. Mostly taken from the Human Mobility Project code.
 * 
 * @author Chang Y. Chung
 * @author Necati E. Ozgencil
 * @author John R.B. Palmer
 */
public class TrackingBroadcastReceiver extends BroadcastReceiver {
	/**
	 * Responds to Android system broadcast that the phone device has just
	 * powered on. If the user is indeed logged in, schedules alarm manager to
	 * begin running the FixGet service at regular intervals, and sets the
	 * SERVICE_ON flag to true in the shared preferences. Uses PropertyHolder to
	 * retrieve the stored alarm interval, if any.
	 */

	@Override
	public void onReceive(Context context, Intent intent) {
		PropertyHolder.init(context);

		String action = intent.getAction();
		AlarmManager startFixGetAlarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent2FixGet = new Intent(context, TrackingFixGet.class);
		PendingIntent pendingIntent2FixGet = PendingIntent.getService(context,
				0, intent2FixGet, 0);

		AlarmManager startFileUploaderAlarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent2FileUploader = new Intent(context, FileUploader.class);
		PendingIntent pendingIntent2FileUploader = PendingIntent.getService(
				context, 0, intent2FileUploader, 0);

		AlarmManager stopFixGetAlarm = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent2StopFixGet = new Intent(context.getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_STOP_FIXGET);
		PendingIntent pendingFixGetStop = PendingIntent.getBroadcast(context,
				0, intent2StopFixGet, 0);

		if (action.contains(context.getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_UNSCHEDULE)) {
			startFixGetAlarm.cancel(pendingIntent2FixGet);
			PropertyHolder.setServiceOn(false);
			cancelNotification(context);
		} else if (action.contains(context.getResources().getString(
				R.string.internal_message_id)
				+ Util.MESSAGE_SCHEDULE)) {
			long alarmInterval = PropertyHolder.getAlarmInterval();
			int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
			long triggerTime = SystemClock.elapsedRealtime();
			startFixGetAlarm.setRepeating(alarmType, triggerTime,
					alarmInterval, pendingIntent2FixGet);
			stopFixGetAlarm.setRepeating(alarmType, triggerTime
					+ Util.LISTENER_WINDOW, alarmInterval, pendingFixGetStop);
			Util.countingFrom = triggerTime;
			PropertyHolder.setServiceOn(true);
			createNotification(context);


			long uploadAlarmInterval = Util.UPLOAD_INTERVAL;
			startFileUploaderAlarm.setRepeating(alarmType, triggerTime,
					uploadAlarmInterval, pendingIntent2FileUploader);

		} else if (action.contains(Intent.ACTION_BOOT_COMPLETED)) {

            Log.i("boot completed", "top");

			if (PropertyHolder.isServiceOn()) {
                Log.i("boot completed", "intent");

                Intent intent2broadcast = new Intent(
						context.getString(R.string.internal_message_id)
								+ Util.MESSAGE_SCHEDULE);
				context.sendBroadcast(intent2broadcast);
			}

		} else if (action.contains("ACTION_SHUTDOWN")
				|| action.contains("QUICKBOOT_POWEROFF")) {

		} else {
			// do nothing
		}
	}

	@SuppressWarnings("deprecation")
	public void createNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		int thisnotification_icon =  R.drawable.ic_stat_launcher;
		Notification notification = new Notification(thisnotification_icon,
				context.getResources().getString(
						R.string.tracking_notification_initial),
				System.currentTimeMillis());
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
//		Intent intent = new Intent(context, DetailItineraryActivity.class);

//		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		notification.setLatestEventInfo(context, context.getResources()
				.getString(R.string.tracking_notification_subject), context
				.getResources().getString(R.string.tracking_notification),
				null);
		notificationManager.notify(Util.TRACKING_NOTIFICATION, notification);

	}

	public void cancelNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Util.TRACKING_NOTIFICATION);

	}

}
