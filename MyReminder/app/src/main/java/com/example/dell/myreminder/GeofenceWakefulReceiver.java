package com.example.dell.myreminder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceWakefulReceiver extends WakefulBroadcastReceiver {
    public GeofenceWakefulReceiver() {
    }
    Context mContext;
    @Override
    public void onReceive(Context context, Intent intent)
    {
        mContext=context;
        Log.w(GeofenceUtils.getTag(), "GeofenceBroadcastReceiver");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = "geofencingEvent has error";
            Log.e(GeofenceUtils.getTag(), errorMessage);
            return;
        }
        String title=intent.getStringExtra(GeofenceUtils.TITLE_KEY);
        int id=intent.getIntExtra(GeofenceUtils.ID_KEY, 0);
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            /*String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );*/

            // Send notification and log the transition details.
            sendNotification(title, id);
            Log.i(GeofenceUtils.getTag(), title+" "+id);
        } else {
            // Log the error.
            Log.e(GeofenceUtils.getTag(), context.getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }
    private void sendNotification(String notificationDetails, int id) {
        // Create an explicit content Intent that starts the main Activity
        Log.w(GeofenceUtils.getTag(), "sendNotification");
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        notificationIntent.putExtra("Activity Key", 3);

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
                .setContentTitle(notificationDetails)
                .setContentText(mContext.getString(R.string.geofence_transition_notification_text))
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
        mNotificationManager.notify(id, builder.build());
    }
}
