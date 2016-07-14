package com.example.dell.myreminder.Reminders;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dell.myreminder.Utility.AlarmConstants;
import com.example.dell.myreminder.Geofences.GeofenceUtils;
import com.example.dell.myreminder.Utility.MyUtils;
import com.example.dell.myreminder.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ReminderAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public ReminderAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = mLayoutInflater.inflate(R.layout.reminder_row, parent, false);
        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        String title = c.getString(c.getColumnIndexOrThrow(RemindersDbAdapter.KEY_TITLE));
        String dateTime = c.getString(c.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DATE_TIME));

        Random rand=new Random();
        int x=rand.nextInt(7);
        /**
         * Next set the title of the entry.
         */
        TextView title_text = (TextView) v.findViewById(R.id.myTitle);
        if (title_text != null) {
            title_text.setText(title);
        }
        /**
         * Set Date
         */
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        try {
            Date date = format.parse(dateTime);
            DateFormat df = new SimpleDateFormat("dd MMMM, yyyy", Locale.ENGLISH);
            String sDate=df.format(date);
            df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String sTime=df.format(date);
            TextView date_text = (TextView) v.findViewById(R.id.myDate);
            TextView time_text = (TextView) v.findViewById(R.id.myTime);
            if (date_text != null) {
                date_text.setText(sDate);
            }
            if(time_text!=null)
            {
                time_text.setText(sTime);
            }
            TextView time_left_text= (TextView) v.findViewById(R.id.timeLeftReminder);
            Calendar now=Calendar.getInstance();
            Calendar future=Calendar.getInstance();
            future.setTime(date);
            long diff=future.getTimeInMillis()-now.getTimeInMillis();
            long days = diff / (24 * 60 * 60 * 1000);
            diff=diff%(24 * 60 * 60 * 1000);
            long hours=0;
            if(diff!=0)
            hours=diff/(60 * 60 * 1000);
            diff=diff%(60 * 60 * 1000);
            long minutes=0;
            if(diff!=0)
            minutes=diff/(60 * 1000);
            diff=diff%(60 * 1000);
            long seconds=0;
            if(diff!=0)
                seconds=diff/1000;
            String timeLeft="";
            if(days>1)
                timeLeft = String.valueOf(days)+" days "+String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
            else if(days==1)
                timeLeft = String.valueOf(days)+" day "+String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
            else if(seconds>=0)
                timeLeft = String.valueOf(hours)+"h "+String.valueOf(minutes)+"m left";
            if(seconds<0)
            {
                assert title_text != null;
                title_text.setPaintFlags(title_text.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else
            {
                assert title_text != null;
                title_text.setPaintFlags(title_text.getPaintFlags()& (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            time_left_text.setText(timeLeft);
            time_left_text.setTextColor(ContextCompat.getColor(context, GeofenceUtils.getColor(x)));
        } catch (ParseException e) {
            Log.w(AlarmConstants.ALARM_TAG, e.getMessage());
        }

        /**
         * Display the paper clip icon
         */
        ImageView myCover = (ImageView) v.findViewById(R.id.myCover);
        myCover.setImageResource(MyUtils.getAlarmImage(x));

        View z=v.findViewById(R.id.remDividerLine);
        z.setBackgroundColor(ContextCompat.getColor(context, GeofenceUtils.getColor(x)));
    }
}
