package net.movelab.sudeau;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

public class SyncBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PropertyHolder.init(context);
        String action = intent.getAction();
        AlarmManager syncAlarm = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent2Syc = new Intent(context, DownloadCoreData.class);
        PendingIntent pendingIntent2Sync = PendingIntent.getService(context,
                0, intent2Syc, 0);
        if (action != null) {
            if (action.contains(Intent.ACTION_POWER_CONNECTED)) {
                context.startService(intent2Syc);
            } else if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                    context.startService(intent2Syc);
                }
            } else if (action.contains(context.getResources().getString(
                    R.string.internal_message_id)
                    + Util.MESSAGE_START_SYNC)) {
                syncAlarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                        AlarmManager.INTERVAL_DAY, pendingIntent2Sync);
                PropertyHolder.setSyncAlarmOn(true);
            } else if (action.contains(Intent.ACTION_BOOT_COMPLETED)) {
                syncAlarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(),
                        AlarmManager.INTERVAL_DAY, pendingIntent2Sync);
                PropertyHolder.setSyncAlarmOn(true);
            }
        }
    }
}
