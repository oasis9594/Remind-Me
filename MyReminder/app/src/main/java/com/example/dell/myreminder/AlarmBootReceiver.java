package com.example.dell.myreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;

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
    }
}
