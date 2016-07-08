package com.example.dell.myreminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.WindowManager;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    MyDBHandler dbHandler;
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = intent.getExtras();
            dbHandler=MyDBHandler.getInstance(context);
            AlarmItems item=bundle.getParcelable(AlarmConstants.PAR_KEY);
            Boolean oneTime=!item.isRepeat();
            if(oneTime)
            {
                try {
                    item.setIsChecked(false);
                    dbHandler.toggleAlarm(item);
                }catch (Exception e)
                {
                    Log.w(AlarmConstants.ALARM_TAG, "AlarmReceiver1: " + e.getMessage());
                }
            }
            SharedPreferences shared= PreferenceManager.getDefaultSharedPreferences(context);

            Intent newIntent;
            newIntent = new Intent(context, DialogActivity.class);
            String x=shared.getString(context.getResources().getString(R.string.alarm_type_key), "0");
            if(!x.equals("0"))
                newIntent = new Intent(context, AlarmCalculation.class);
            newIntent.putExtra(AlarmConstants.PAR_KEY, item);
            newIntent.putExtra(AlarmConstants.ALARM_ONE_TIME, oneTime);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(newIntent);
        } catch (Exception e) {
            Log.w(AlarmConstants.ALARM_TAG, "AlarmReceiver2: " + e.getMessage());
        }
    }
}
