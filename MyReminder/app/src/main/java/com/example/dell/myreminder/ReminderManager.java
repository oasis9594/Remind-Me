package com.example.dell.myreminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class ReminderManager {

    private Context mContext;                   // Current app context
    private AlarmManager mAlarmManager;         // using app import

    public ReminderManager(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }

    // set a pending intent for given row id to wake up alarm manager

    public void setReminder(Long taskId, Calendar when) {

        Intent i = new Intent(mContext, OnReminderReceiver.class);
        i.putExtra(RemindersDbAdapter.KEY_ROWID, (long)taskId);

        PendingIntent pi = PendingIntent.getBroadcast(mContext,(int)((long)taskId)%AlarmConstants.BASE_VALUE, i, PendingIntent.FLAG_ONE_SHOT);

        mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
    }
}
