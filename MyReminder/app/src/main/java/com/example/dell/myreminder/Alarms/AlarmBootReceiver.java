package com.example.dell.myreminder.Alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.example.dell.myreminder.Utility.MyDBHandler;
import com.example.dell.myreminder.Utility.MyUtils;
import com.example.dell.myreminder.Reminders.ReminderEditActivity;
import com.example.dell.myreminder.Reminders.ReminderManager;
import com.example.dell.myreminder.Reminders.RemindersDbAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AlarmBootReceiver extends BroadcastReceiver {
    MyDBHandler dbHandler;
    public AlarmBootReceiver() {
    }
    ArrayList<AlarmItems> alarmItems;
    @Override
    public void onReceive(Context context, Intent intent) {
        dbHandler=MyDBHandler.getInstance(context);
        alarmItems=dbHandler.getAllAlarms();
        for (AlarmItems myItem:alarmItems)
        {
            if(myItem.isChecked())
            {
                MyUtils.alarmUtil(myItem, context);
            }
        }
        ReminderManager reminderMgr = new ReminderManager(context);

        RemindersDbAdapter dbHelper = new RemindersDbAdapter(context);
        dbHelper.open();

        Cursor cursor = dbHelper.fetchAllReminders();

        if(cursor != null) {
            cursor.moveToFirst();

            int rowIdColumnIndex = cursor.getColumnIndex(RemindersDbAdapter.KEY_ROWID);
            int dateTimeColumnIndex = cursor.getColumnIndex(RemindersDbAdapter.KEY_DATE_TIME);

            // loop that fetches from database using cursor and passes date and time in a calendar object to set reminder

            while(!cursor.isAfterLast()) {

                Long rowId = cursor.getLong(rowIdColumnIndex);
                String dateTime = cursor.getString(dateTimeColumnIndex);            // date and time stored as Strings in DB

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat format = new SimpleDateFormat(ReminderEditActivity.DATE_TIME_FORMAT, Locale.ENGLISH);

                try {
                    java.util.Date date = format.parse(dateTime);           // Create a date object by formatting 'dateTime' String
                    cal.setTime(date);                                      // Set Time( and Date) in Calendar

                    reminderMgr.setReminder(rowId, cal);
                } catch (java.text.ParseException e) {
                    Log.e("OnBootReceiver", e.getMessage(), e);
                }

                cursor.moveToNext();
            }
            cursor.close() ;
        }

        dbHelper.close();
    }
}
