package com.example.dell.myreminder.Alarms;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myreminder.Utility.AlarmConstants;
import com.example.dell.myreminder.R;
import com.example.dell.myreminder.Utility.RingtonePlayingService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;

public class AlarmCalculation extends AppCompatActivity {

    int a, b, c, d, x, y;
    Random rand;
    Button button;
    TextView equation_id, alarmText;
    ImageView myImage;
    EditText answer;
    String mPath=null;

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    public static boolean activityVisible;
    int id=23846;//Notification id

    public static final String KEY_IMAGEPATH="userdetails.ProfileImagePath";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_alarm_calculation);
        button=(Button) findViewById(R.id.button);
        equation_id=(TextView)findViewById(R.id.equation_id);
        answer=(EditText)findViewById(R.id.answer_id);
        myImage=(ImageView)findViewById(R.id.myImage);
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);
        mPath=sharedPref.getString(KEY_IMAGEPATH, null);

        if(mPath!=null)
            loadImageFromStorage();
        rand=new Random();
        SharedPreferences shared= PreferenceManager.getDefaultSharedPreferences(this);
        if(shared.getString(getResources().getString(R.string.alarm_type_key), "-1").equals("1"))
        {
            a=getRandomNumber(0, 9);
            b=getRandomNumber(0, 9);
            c=getRandomNumber(0, 9);
            d=getRandomNumber(0, 9);
        }
        else
        {
            a=getRandomNumber(10, 99);
            b=getRandomNumber(10, 99);
            c=getRandomNumber(10, 99);
            d=getRandomNumber(10, 99);
        }
        x=a*b+c*d;

        equation_id.setText(a+" x "+b+" + "+c+" x "+d);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    y=Integer.parseInt(answer.getText().toString());
                    if(x!=y)
                    {
                        Toast.makeText(getApplicationContext(), "Wrong Answer. Try Again!!!!!", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        cancelAlarmUtil();
                    }
                }catch (Exception e)
                {
                    Log.e(AlarmConstants.ALARM_TAG, e.getMessage());
                    Toast.makeText(getApplicationContext(), "Invalid Number", Toast.LENGTH_SHORT).show();
                }
            }
        });
        startRingtone();
        showNotification();
    }
    public int getRandomNumber(int min, int max)
    {
        return rand.nextInt((max - min) + 1) + min;
    }

    public void onAttachedToWindow() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
    private void showNotification() {
        Log.w(AlarmConstants.ALARM_TAG, "startNotification");
        // Instantiate a Builder object.
        mBuilder = new NotificationCompat.Builder(this);
        // Creates an Intent for the Receiver
        Intent notifyIntent = new Intent(this, AlarmActivityReceiver.class);
        notifyIntent.putExtra("Calling_Activity_Key", 2);
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
        mBuilder.setContentText("CLick to retutm to app").setColor(Color.parseColor("#ffffff"));
        mBuilder.setTicker("Alarm");
        mBuilder.setSmallIcon(R.drawable.ic_alarmclockpink);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        // Notifications are issued by sending them to the
        // NotificationManager system service.
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        // Builds an anonymous Notification object from the builder, and
        // passes it to the NotificationManager
        mNotificationManager.notify(id, mBuilder.build());
        Log.w(AlarmConstants.ALARM_TAG, "startNotification");
    }

    //So as to prevent activity from getting destroyed
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    private void dismissNotification(int id){
        mNotificationManager.cancel(id);
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
    public void cancelAlarmUtil()
    {
        stopRingtone();
        dismissNotification(id);
        stopActivity();
    }
    @Override
    public void onDestroy()
    {
        Log.w(AlarmConstants.ALARM_TAG, "onDestroy");
        stopRingtone();
        dismissNotification(id);
        super.onDestroy();
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
    private void loadImageFromStorage()
    {
        try {
            File f=new File(mPath);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            myImage.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
