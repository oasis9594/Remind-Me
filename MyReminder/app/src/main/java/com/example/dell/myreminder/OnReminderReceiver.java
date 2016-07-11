package com.example.dell.myreminder;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class OnReminderReceiver extends WakefulBroadcastReceiver {

    private static final String TAG = AlarmConstants.ALARM_TAG;

    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received wake up from alarm manager.");

        mContext=context;
        // Retrieving row id on recieve
        SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(mContext);
        if(!sharedPreferences.getBoolean("notifications_reminder", true))
            return;
        long rowId = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);

        Intent notificationIntent = new Intent(mContext, ReminderEditActivity.class);
        notificationIntent.putExtra(RemindersDbAdapter.KEY_ROWID, rowId);
        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Log.w(GeofenceUtils.getTag(), "sendNotification");
        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(mContext.getString(R.string.notify_new_task_title))
                .setContentText(mContext.getString(R.string.notify_new_task_message))
                .setContentIntent(notificationPendingIntent);
        Uri uri;
        try{
            SharedPreferences getAlarms = PreferenceManager.
                    getDefaultSharedPreferences(mContext);
            String reminders = getAlarms.getString("ringtone_notification", "default ringtone");
            if(reminders.equals("default ringtone"))
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            else
                uri = Uri.parse(reminders);
        }catch (Exception e)
        {
            Log.e(AlarmConstants.ALARM_TAG, e.getMessage());
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        if(uri==null)
        {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        builder.setSound(uri);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify((int)rowId, builder.build());
    }
}

