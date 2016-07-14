package com.example.dell.myreminder.Alarms;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.SystemClock;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dell.myreminder.Utility.AlarmConstants;
import com.example.dell.myreminder.R;
import com.example.dell.myreminder.Utility.RingtonePlayingService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class DialogActivity extends Activity {

    Button cancel;
    Button snooze;
    Boolean oneTime;
    ImageView alarmImage;
    TextView alarmText;
    AlarmItems myItem;
    int cntr = 4535342;
    int id=23846;//Notification id
    int snoozeAlarm;
    String mPath;
    public static boolean activityVisible;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    BroadcastReceiver myReceiver;

    public static final String CANCEL_ACTION="cancel", SNOOZE_ACTION="snooze", KEY_IMAGEPATH="userdetails.ProfileImagePath";
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.w(AlarmConstants.ALARM_TAG, "Dialog Activity");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        try{
            setContentView(R.layout.activity_dialog);
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, "DialogActivity1: "+e.getMessage());
        }
        Log.w(AlarmConstants.ALARM_TAG, "Dialog Activity");
        cancel = (Button) findViewById(R.id.dialogCancel);
        snooze = (Button) findViewById(R.id.dialogSnooze);
        alarmImage = (ImageView) findViewById(R.id.dialogAlarm);
        alarmText = (TextView) findViewById(R.id.dialogText);
        snoozeAlarm=15*60*1000;

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);
        mPath=sharedPref.getString(KEY_IMAGEPATH, null);

        if(mPath!=null)
            loadImageFromStorage();
        try {
            text = "Alarm";
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                myItem=bundle.getParcelable(AlarmConstants.PAR_KEY);
                text = myItem.getTitle();
                oneTime=bundle.getBoolean(AlarmConstants.ALARM_ONE_TIME);
                snoozeAlarm=myItem.getSnoozeTime()*60*1000;
            }
            else {
                Log.w(AlarmConstants.ALARM_TAG, "bundle is null in dialog activity");
            }
            alarmText.setText(text);
        }
        catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, "DialogActivity2: "+e.getMessage());
            Log.w(AlarmConstants.ALARM_TAG, String.valueOf(snoozeAlarm));
            text="Alarm";
            alarmText.setText("Alarm");
        }
        registerReceiver();
        startRingtone();
        showNotification();
    }

    private void showNotification() {
        Log.w(AlarmConstants.ALARM_TAG, "startNotification");
        // Instantiate a Builder object.
        mBuilder = new NotificationCompat.Builder(this);
        // Creates an Intent for the Receiver
        Intent notifyIntent = new Intent(this, AlarmActivityReceiver.class);
        notifyIntent.putExtra("Calling_Activity_Key", 1);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        0,
                        notifyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // Puts the PendingIntent into the notification builder
        mBuilder.setContentIntent(notifyPendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setContentTitle("YO!!!!");
        mBuilder.setContentText("Want to stop playing?").setColor(Color.parseColor("#ffffff"));
        mBuilder.setTicker("Alarm");
        mBuilder.setSmallIcon(R.drawable.ic_alarmclockpink);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        //Add Action
        //Cancel
        Intent yesReceive = new Intent();
        yesReceive.setAction(CANCEL_ACTION);
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 0, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_cancel, "Cancel", pendingIntentYes);
        //Snooze
        Intent snoozeReceive = new Intent();
        yesReceive.setAction(SNOOZE_ACTION);
        PendingIntent pendingIntentSnooze = PendingIntent.getBroadcast(this, 0, snoozeReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.addAction(R.drawable.ic_alarm_snooze_n, "Snooze", pendingIntentSnooze);

        // Notifications are issued by sending them to the
        // NotificationManager system service.
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // Builds an anonymous Notification object from the builder, and
        // passes it to the NotificationManager
        mNotificationManager.notify(id, mBuilder.build());
        Log.w(AlarmConstants.ALARM_TAG, "startNotification");
    }

    private void dismissNotification(int id){
        mNotificationManager.cancel(id);
    }
    public void CancelAlarm(View v) {
        cancelAlarmUtil();
    }
    public void cancelAlarmUtil()
    {
        stopRingtone();
        dismissNotification(id);
        stopActivity();
    }

    public void SnoozeAlarm(View v) {
        snoozeAlarmUtil();
    }
    public void snoozeAlarmUtil()
    {
        SharedPreferences shared=getSharedPreferences("myConstants", Context.MODE_PRIVATE);
        cntr=shared.getInt("CounterDialog", cntr);
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        Intent myIntent = new Intent(this, AlarmReceiver.class);
        myIntent.putExtra(AlarmConstants.PAR_KEY, myItem);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, cntr, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                snoozeAlarm, pendingIntent);
        cntr += 1;
        cntr %= AlarmConstants.MOD_VALUE;
        SharedPreferences.Editor editor=shared.edit();
        if(shared.contains("CounterDialog"))
            editor.remove("CounterDialog");
        editor.putInt("CounterDialog", cntr);
        editor.apply();
        Log.w(AlarmConstants.ALARM_TAG, "snoozeAlarm dialog");
        dismissNotification(id);
        finish();
    }
    public void onAttachedToWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    @Override
    public void onDestroy()
    {
        Log.w(AlarmConstants.ALARM_TAG, "onDestroy");
        stopRingtone();
        dismissNotification(id);
        unRegisterReceiver();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    protected void registerReceiver() {
        Log.w(AlarmConstants.ALARM_TAG, "registerReceiver");
        try{
            IntentFilter filter = new IntentFilter();
            filter.addAction(CANCEL_ACTION);
            myReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action=intent.getAction();
                    if(action.equals(DialogActivity.CANCEL_ACTION))
                    {
                        Log.w("RingtoneService", "MyReceiver Cancel Action");
                        cancelAlarmUtil();
                    }
                    else if(action.equals(SNOOZE_ACTION))
                    {
                        snoozeAlarmUtil();
                    }
                }
            };
            this.registerReceiver(myReceiver, filter);
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, e.getMessage()+" onStart");
        }
    }
    protected void unRegisterReceiver() {
        Log.w("RingtoneService", "unRegisterReceiver");
        this.unregisterReceiver(this.myReceiver);
    }
    public void startRingtone()
    {
        if(!isMyServiceRunning(RingtonePlayingService.class))
        {
            Intent startIntent = new Intent(this, RingtonePlayingService.class);
            startService(startIntent);
        }
    }
    public void stopRingtone()
    {
        if(isMyServiceRunning(RingtonePlayingService.class))
        {
            Intent stopIntent=new Intent(this, RingtonePlayingService.class);
            stopService(stopIntent);
        }
    }
    public void stopActivity()
    {
        finish();
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void loadImageFromStorage()
    {
        try {
            File f=new File(mPath);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            alarmImage.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStop() {
        Log.w(AlarmConstants.ALARM_TAG, "onStop");
        activityVisible=false;
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.w(AlarmConstants.ALARM_TAG, "onStart");
        activityVisible=true;
        super.onStart();
    }
}


