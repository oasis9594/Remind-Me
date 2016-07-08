package com.example.dell.myreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmActivityReceiver extends BroadcastReceiver {
    public AlarmActivityReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w(AlarmConstants.ALARM_TAG, "AlarmActivityReceiver");
        int x=intent.getExtras().getInt("Calling_Activity_Key");
        Intent myIntent;
        myIntent=new Intent(context, DialogActivity.class);
        if(x==2)
            myIntent=new Intent(context, AlarmCalculation.class);
        try
        {
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            if((x==1&&!DialogActivity.activityVisible)||(x==2&&!AlarmCalculation.activityVisible))
                context.startActivity(myIntent);
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, "AlarmActivityReceiver: "+e.getMessage());
        }
    }
}
