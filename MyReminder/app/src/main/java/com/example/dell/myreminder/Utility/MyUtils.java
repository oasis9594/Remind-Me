package com.example.dell.myreminder.Utility;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.dell.myreminder.Alarms.AlarmItems;
import com.example.dell.myreminder.Alarms.AlarmReceiver;
import com.example.dell.myreminder.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MyUtils {
    public static int hashCode(int id, int day)
    {
        return (id*7+AlarmConstants.BASE_VALUE+day)% AlarmConstants.MOD_VALUE;
    }
    public static PendingIntent getPendingIntent(int id, Context mContext, Intent intent, int day)
    {
        return PendingIntent.getBroadcast(mContext,
                hashCode(id, day),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    public static String getTimeFormat(int h, int m, int s)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(); //calendar object with current date
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE,m);
        calendar.set(Calendar.SECOND, s);
        return simpleDateFormat.format(calendar.getTime());
    }
    public static String getRemainingTime(AlarmItems myItem)
    {
        int m, h, d, minutes=0, hours=0, seconds, cur_day;
        m=myItem.getMinutes();
        h=myItem.getHours();
        int s=myItem.getSeconds();
        Calendar now=Calendar.getInstance();
        seconds=s-now.get(Calendar.SECOND);
        cur_day=now.get(Calendar.DAY_OF_WEEK);
        cur_day--;
        int i, x, y=6;
        d=7;
        for(i=cur_day;i<cur_day+7;i++)
        {
            if(myItem.isDay(i%7))
            {
                /*Calendar calendar=Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_WEEK, x+1);
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, m);
                calendar.set(Calendar.SECOND, s);*/
                x=i-cur_day;
                if(x!=0&&x<y)
                    y=x;
                if(x<d)
                    d=x;
            }

        }
        if(seconds<0) {
            minutes--;
        }
        minutes+=m-now.get(Calendar.MINUTE);
        if(minutes<0){
            minutes+=60;
            hours--;
        }
        hours+=h-now.get(Calendar.HOUR_OF_DAY);
        if(hours<0)
        {
            hours+=24;
            d=(d+6)%7;
            y=(y+6)%7;
            if(y<d)
                d=y;
        }
        if(d>1)
            return String.valueOf(d)+" days"+String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
        else if(d==1)
            return String.valueOf(d)+" day"+String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
        else
            return String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
    }
    public static String getRemainingTime(int h, int m, int s){
        int minutes=0, hours=0;
        Log.w("MApp", "getRemainingTime");
        int seconds=s-Calendar.getInstance().get(Calendar.SECOND);
        if(seconds<0) {
            minutes--;
        }
        minutes+=m-Calendar.getInstance().get(Calendar.MINUTE);
        if(minutes<0){
            minutes+=60;
            hours--;
        }
        hours+=h-Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(hours<0)
            hours+=24;
        return String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
    }
    public static void alarmUtil(AlarmItems myItem, Context mContext)
    {
        AlarmManager alarmManager=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        Log.w(AlarmConstants.ALARM_TAG, "alarmUtil");
        Calendar now=Calendar.getInstance();
        long s1=now.getTimeInMillis();
        Calendar calendar=Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, myItem.getHours());
        calendar.set(Calendar.MINUTE, myItem.getMinutes());
        calendar.set(Calendar.SECOND, myItem.getSeconds());

        Intent intent=new Intent(mContext, AlarmReceiver.class);
        intent.putExtra(AlarmConstants.PAR_KEY, myItem);

        if(myItem.isRepeat()) {//Alarm is repeating
            for (int i = 0; i < 7; i++) {
                if (myItem.isDay(i)) {
                    calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                    long s2 = calendar.getTimeInMillis();
                    long extra_time = 0;
                    if (s2 < s1) {
                        extra_time += 7 * 24 * 3600 * 1000;
                    }
                    PendingIntent pendingIntent =getPendingIntent(myItem.getId(), mContext, intent, i);
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, s2 + extra_time,
                            7*AlarmManager.INTERVAL_DAY, pendingIntent);
                }
            }
        }
        else//If alarm is non-repeating
        {
            long s2=calendar.getTimeInMillis();
            long extra_time=0;
            int day=now.get(Calendar.DAY_OF_WEEK);
            day-=1;
            if(s2<s1)
            {
                extra_time+=24*3600*1000;
                day+=1;
            }
            PendingIntent pendingIntent=getPendingIntent(myItem.getId(), mContext, intent, day);
            alarmManager.set(AlarmManager.RTC_WAKEUP, s2 + extra_time, pendingIntent);
        }
        Log.w(AlarmConstants.ALARM_TAG, "alarmUtil");
    }
    public static int getAlarmImage(int color)
    {
        switch (color)
        {
            case 0: return R.drawable.ic_alarmclock_chrome;
            case 1: return R.drawable.ic_alarmclock_black;
            case 2: return R.drawable.ic_alarmclock_cyan;
            case 3: return R.drawable.ic_alarmclock_green;
            case 4: return R.drawable.ic_alarmclockpink;
            case 5: return R.drawable.ic_alarmclock_red;
            case 6: return R.drawable.ic_alarmclock_violet;
            default: return R.drawable.ic_alarmclockpink;
        }
    }
}
