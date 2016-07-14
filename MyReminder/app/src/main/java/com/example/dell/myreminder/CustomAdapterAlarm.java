package com.example.dell.myreminder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.ImageButton;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;

import com.rey.material.widget.Switch;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class CustomAdapterAlarm extends RecyclerView.Adapter<CustomAdapterAlarm.ViewHolder>
        implements TimePickerDialog.OnTimeSetListener{
    ArrayList<AlarmItems> myAlarmItems;
    MyDBHandler dbHandler;
    TimePickerDialog tpd;
    private Activity mContext;
    private int mpos;
    private AlarmManager alarmManager;
    int snoozeTime;
    boolean type=false;

    ArrayAdapter<CharSequence> adapter;


    public CustomAdapterAlarm(ArrayList<AlarmItems> myDataset, Activity context) {
        myAlarmItems = myDataset;
        this.mContext=context;
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(mContext,
                R.array.SnoozeTimes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }
    public Intent getIntent(AlarmItems item)
    {
        Intent intent=new Intent(mContext, AlarmReceiver.class);
        intent.putExtra(AlarmConstants.PAR_KEY, item);
        return intent;
    }
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        Log.w("MApp", "Time Changed");
        AlarmItems myItem=myAlarmItems.get(mpos);
        //Make changes in current item
        myItem.setHours(hourOfDay);
        myItem.setMinutes(minute);
        myItem.setSeconds(second);

        //update system alarm
        if(myItem.isChecked())
        {
            //Cancel current alarm
            for(int i=0;i<7;i++)
            {
                Intent intent=getIntent(myItem);
                alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, i));
            }
            //Update alarm
            MyUtils.alarmUtil(myItem, mContext);
        }
        //Make changes in database
        dbHandler.updateAlarm(myAlarmItems.get(mpos));
        Log.w(AlarmConstants.ALARM_TAG, "onTimeSet database updated");
        //Notify these changes to adapter
        notifyItemChanged(mpos);
        Log.w(AlarmConstants.ALARM_TAG, "onTimeSet view updated");
    }
    private void setAlarm(AlarmItems myItem, int day)
    {

        Calendar now=Calendar.getInstance();
        long s1=now.getTimeInMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, day+1);
        calendar.set(Calendar.HOUR_OF_DAY, myItem.getHours());
        calendar.set(Calendar.MINUTE, myItem.getMinutes());
        calendar.set(Calendar.SECOND, myItem.getSeconds());
        Intent intent=getIntent(myItem);
        long s2 = calendar.getTimeInMillis();
        long extra_time = 0;
        if (s2 < s1) {
            extra_time += 7 * 24 * 3600 * 1000;
        }
        PendingIntent pIntent=MyUtils.getPendingIntent(myItem.getId(), mContext, intent, day);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + extra_time,
                7 * AlarmManager.INTERVAL_DAY, pIntent);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, Switch.OnCheckedChangeListener {

        TextView myAlarmTitle;
        TextView myAlarmTime;
        TextView myAlarmTimeLeft;
        ImageView myImage;
        Switch mySwitch;
        ImageButton trashButton;
        ImageButton snoozeButton;
        CheckBox repeatAlarm;
        LinearLayout childlayout;
        CardView cardView;
        Button mon, tue, wed, thu, fri, sat, sun;

        public IMyViewHolderClicks mListener;

        public ViewHolder(View customView, IMyViewHolderClicks listener) {
            super(customView);

            mListener=listener;

            myAlarmTitle = (TextView) customView.findViewById(R.id.myAlarmTitle);
            myAlarmTime = (TextView) customView.findViewById(R.id.myAlarmTime);
            myAlarmTimeLeft = (TextView) customView.findViewById(R.id.myAlarmTimeLeft);
            myImage = (ImageView) customView.findViewById(R.id.myImage);
            mySwitch = (Switch) customView.findViewById(R.id.mySwitch);
            trashButton=(ImageButton)customView.findViewById(R.id.alarmTrash);
            snoozeButton=(ImageButton)customView.findViewById(R.id.snoozeButton);
            repeatAlarm=(CheckBox)customView.findViewById(R.id.repeatAlarm);
            childlayout=(LinearLayout)customView.findViewById(R.id.childLayout);
            cardView=(CardView)customView.findViewById(R.id.card_view);

            sun=(Button)childlayout.findViewById(R.id.sunday_Button);
            mon=(Button)childlayout.findViewById(R.id.monday_Button);
            tue=(Button)childlayout.findViewById(R.id.tuesday_Button);
            wed=(Button)childlayout.findViewById(R.id.wednesday_Button);
            thu=(Button)childlayout.findViewById(R.id.thursday_Button);
            fri=(Button)childlayout.findViewById(R.id.friday_Button);
            sat=(Button)childlayout.findViewById(R.id.saturday_Button);

            mySwitch.setOnCheckedChangeListener(this);
            customView.setOnClickListener(this);
            trashButton.setOnClickListener(this);
            snoozeButton.setOnClickListener(this);
            repeatAlarm.setOnClickListener(this);
            myAlarmTitle.setOnClickListener(this);

            sun.setOnClickListener(this);
            mon.setOnClickListener(this);
            tue.setOnClickListener(this);
            wed.setOnClickListener(this);
            thu.setOnClickListener(this);
            fri.setOnClickListener(this);
            sat.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();
            if(v instanceof ImageButton){
                if(v.getId()==R.id.alarmTrash)
                    mListener.trashClick((ImageButton)v, position);
                else
                    mListener.snoozeClick((ImageButton) v, position);
            }
            else if(v instanceof CheckBox)
            {
                if(v.getId()==R.id.repeatAlarm)
                mListener.repeatClick((CheckBox)v, position, childlayout, cardView);
            }
            else if(v instanceof Button)
            {
                switch (v.getId())
                {
                    case R.id.sunday_Button:mListener.sunClick((Button) v, position);
                        break;
                    case R.id.monday_Button:mListener.monClick((Button) v, position);
                        break;
                    case R.id.tuesday_Button:mListener.tueClick((Button) v, position);
                        break;
                    case R.id.wednesday_Button:mListener.wedClick((Button) v, position);
                        break;
                    case R.id.thursday_Button:mListener.thuClick((Button) v, position);
                        break;
                    case R.id.friday_Button:mListener.friClick((Button) v, position);
                        break;
                    case R.id.saturday_Button:mListener.satClick((Button) v, position);
                        break;
                    default:Log.w(AlarmConstants.ALARM_TAG, "Some Error in ViewHolder");
                }
            }
            else if(v instanceof TextView)
            {
                mListener.titleClick((TextView)v, position);
            }
            else {
                mListener.onClickAlarm(v, position);
            }
        }

        @Override
        public void onCheckedChanged(Switch view, boolean checked) {
            mListener.onSwitchToggle(view, getAdapterPosition());
        }

        public interface IMyViewHolderClicks {
            void onSwitchToggle(Switch s, int pos);
            void onClickAlarm(View view, int pos);
            void trashClick(ImageButton b, int pos);
            void snoozeClick(ImageButton b, int pos);
            void repeatClick(CheckBox c, int pos, LinearLayout layout, CardView cardView);
            void monClick(Button b, int pos);
            void tueClick(Button b, int pos);
            void wedClick(Button b, int pos);
            void thuClick(Button b, int pos);
            void friClick(Button b, int pos);
            void satClick(Button b, int pos);
            void sunClick(Button b, int pos);
            void titleClick(TextView b, int pos);
        }
    }


    public void deleteItem(int index) {
        //Delete alarm
        for(int i=0;i<7;i++)
        {
            Intent intent = getIntent(myAlarmItems.get(index));
            try{
                alarmManager.cancel(MyUtils.getPendingIntent(myAlarmItems.get(index).getId(), mContext, intent, i));
            }catch (Exception e){
                Log.w(AlarmConstants.ALARM_TAG, "deleteItem "+i+" "+e.getMessage());
            }
        }
        //Delete from database
        dbHandler.deleteAlarm(myAlarmItems.get(index));
        //Delete from List
        myAlarmItems.remove(index);
        //Delete from view by notifying adapter
        notifyItemRemoved(index);
    }
    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent,
                                               final int viewType) {
        View customView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_row_alarm, parent, false);

        alarmManager=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);

        dbHandler=MyDBHandler.getInstance(customView.getContext());
        ViewHolder vh = new ViewHolder(customView, new ViewHolder.IMyViewHolderClicks() {
            public void onSwitchToggle(Switch s, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                boolean b = myItem.toggle();
                //Update in database
                dbHandler.toggleAlarm(myItem);

                if(b) {
                    Toast.makeText(s.getContext(), "Alarm On", Toast.LENGTH_SHORT).show();
                    MyUtils.alarmUtil(myItem, mContext);
                    //notifyItemChanged(pos);
                }
                else {
                    Toast.makeText(s.getContext(), "Alarm Off", Toast.LENGTH_SHORT).show();
                    for(int i=0;i<7;i++)
                    {
                        Intent intent=getIntent(myItem);
                        alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, i));
                    }
                    //notifyItemChanged(pos);
                }
            }
            //trashClick DONE
            public void trashClick(ImageButton b, final int pos)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete this alarm?");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(pos);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }

            @Override
            public void snoozeClick(ImageButton b, final int pos) {

                AlarmItems myItem=myAlarmItems.get(pos);
                snoozeTime=myItem.getSnoozeTime();
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.snooze_header)
                        .setSingleChoiceItems(R.array.SnoozeTimes, getCheckedTime() ,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                switch (which)
                                {
                                    case 0:
                                        snoozeTime=5;
                                        break;
                                    case 1:
                                        snoozeTime=10;
                                        break;
                                    case 2:
                                        snoozeTime=15;
                                        break;
                                    case 3:
                                        snoozeTime=30;
                                        break;
                                    case 4:
                                        snoozeTime=45;
                                        break;
                                    case 5:
                                        snoozeTime=60;
                                        break;
                                }
                                myAlarmItems.get(pos).setSnoozeTime(snoozeTime);
                                dbHandler.updateSnoozeTime(myAlarmItems.get(pos));
                                notifyItemChanged(pos);
                            }
                        });
                builder.create();
                builder.show();
            }


            @Override
            public void repeatClick(CheckBox c, int pos,final LinearLayout childLayout,final CardView cardView) {
                AlarmItems myItem=myAlarmItems.get(pos);
                Log.w(AlarmConstants.ALARM_TAG, "repeatClick");
                if(!myItem.isRepeat())
                {
                    Log.w(AlarmConstants.ALARM_TAG, "Checkbox is set");
                    myItem.setRepeat(true);
                    boolean flag=false;
                    for(int i=0;i<7;i++)
                    {
                        if(myItem.isDay(i))
                        {
                            flag=true;
                            if(myItem.isChecked())
                                setAlarm(myItem, i);
                        }
                    }
                    if(!flag)
                    {
                        Log.w(AlarmConstants.ALARM_TAG, "Repeat flag is false");
                        for(int i=1;i<6;i++)
                        {
                            myItem.setDay(i, true);
                            if(myItem.isChecked())
                                setAlarm(myItem, i);
                        }
                    }
                }
                else
                {
                    Log.w(AlarmConstants.ALARM_TAG, "Checkbox is not set");

                    myItem.setRepeat(false);
                    for(int i=0;i<7;i++)
                    {
                        Intent intent=getIntent(myItem);
                        alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, i));//Cancel Alarm
                    }
                    Calendar calendar=Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, myItem.getHours());
                    calendar.set(Calendar.MINUTE, myItem.getMinutes());
                    calendar.set(Calendar.SECOND, myItem.getSeconds());
                    Calendar now=Calendar.getInstance();
                    int day=now.get(Calendar.DAY_OF_WEEK);
                    day--;
                    long t1=now.getTimeInMillis();
                    long t2=calendar.getTimeInMillis();
                    long extra_time=0;
                    if(t2<t1)
                    {
                        extra_time=24*3600*1000;
                        day++;
                    }
                    Intent intent=getIntent(myItem);
                    PendingIntent pIntent=MyUtils.getPendingIntent(myItem.getId(), mContext, intent, day);
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + extra_time,
                            7 * AlarmManager.INTERVAL_DAY, pIntent);
                }
                dbHandler.updateRepeat(myItem);
                Log.w(AlarmConstants.ALARM_TAG, "repeat changed in database");
                notifyItemChanged(pos);
            }

            String result="";
            EditText editTitle;
            @Override
            public void titleClick(TextView b, int pos) {
                final AlarmItems myItem=myAlarmItems.get(pos);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                LayoutInflater inflater = mContext.getLayoutInflater();

                final int position=pos;
                Log.w(AlarmConstants.ALARM_TAG, "titleClick1");
                View view=inflater.inflate(R.layout.change_alarm_title, null);
                editTitle=(EditText)view.findViewById(R.id.alarmDialogTitle);
                Log.w(AlarmConstants.ALARM_TAG, "titleClick2");
                builder.setView(view);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result = editTitle.getText().toString();
                        myItem.setTitle(result);
                        dbHandler.updateTitle(myItem);
                        Log.w(AlarmConstants.ALARM_TAG, "Title Changed in database");
                        for(int i=0;i<7;i++)
                        {
                            Intent intent=getIntent(myItem);
                            alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, i));
                        }
                        MyUtils.alarmUtil(myItem, mContext);
                        notifyItemChanged(position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create();
                builder.show();
            }


            @Override
            public void sunClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isSunday())
                {
                    myItem.setSunday(false);//Update View
                    dbHandler.updateDays(myItem);//Update Database
                    Log.w(AlarmConstants.ALARM_TAG, "sunClick changed in database");
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 0));//Cancel Alarm
                }
                else
                {
                    myItem.setSunday(true);
                    setAlarm(myItem, 0);
                }
                notifyItemChanged(pos);
            }

            @Override
            public void monClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isMonday())
                {
                    myItem.setMonday(false);//Update View
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 1));//Cancel Alarm
                }
                else
                {
                    myItem.setMonday(true);
                    setAlarm(myItem,1);
                }
                dbHandler.updateDays(myItem);//Update Database
                Log.w(AlarmConstants.ALARM_TAG, "monClick changed in database");
                notifyItemChanged(pos);
            }
            @Override
            public void tueClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isTuesday())
                {
                    myItem.setTuesday(false);//Update View
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 2));//Cancel Alarm
                }
                else
                {
                    myItem.setTuesday(true);
                    setAlarm(myItem,2);
                }
                dbHandler.updateDays(myItem);//Update Database
                Log.w(AlarmConstants.ALARM_TAG, "tueClick changed in database");
                notifyItemChanged(pos);
            }

            @Override
            public void wedClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isWednesday())
                {
                    myItem.setWednesday(false);//Update View
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 3));//Cancel Alarm
                }
                else
                {
                    myItem.setWednesday(true);
                    setAlarm(myItem,3);
                }
                dbHandler.updateDays(myItem);//Update Database
                Log.w(AlarmConstants.ALARM_TAG, "wedClick changed in database");
                notifyItemChanged(pos);
            }

            @Override
            public void thuClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isThursday())
                {
                    myItem.setThursday(false);//Update View
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 4));//Cancel Alarm
                }
                else
                {
                    myItem.setThursday(true);
                    setAlarm(myItem,4);
                }
                dbHandler.updateDays(myItem);//Update Database
                Log.w(AlarmConstants.ALARM_TAG, "thuClick changed in database");
                notifyItemChanged(pos);
            }

            @Override
            public void friClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isFriday())
                {
                    myItem.setFriday(false);//Update View
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 5));//Cancel Alarm
                }
                else
                {
                    myItem.setFriday(true);
                    setAlarm(myItem,5);
                }
                dbHandler.updateDays(myItem);//Update Database
                Log.w(AlarmConstants.ALARM_TAG, "friClick changed in database");
                notifyItemChanged(pos);
            }

            @Override
            public void satClick(Button b, int pos) {
                AlarmItems myItem=myAlarmItems.get(pos);
                if(myItem.isSaturday())
                {
                    myItem.setSaturday(false);//Update View
                    Intent intent=getIntent(myItem);
                    alarmManager.cancel(MyUtils.getPendingIntent(myItem.getId(), mContext, intent, 6));//Cancel Alarm
                }
                else
                {
                    myItem.setSaturday(true);
                    setAlarm(myItem,6);
                }
                dbHandler.updateDays(myItem);//Update Database
                Log.w(AlarmConstants.ALARM_TAG, "satClick changed in database");
                notifyItemChanged(pos);
            }

            public void onClickAlarm(View view, int pos) {
                mpos=pos;
                tpd = TimePickerDialog.newInstance(
                        CustomAdapterAlarm.this,
                        myAlarmItems.get(pos).getHours(),
                        myAlarmItems.get(pos).getMinutes(),
                        true
                );
                tpd.show(mContext.getFragmentManager(), "Timepickerdialog");
            }
        });
        return vh;
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AlarmItems myItem = myAlarmItems.get(position);
        int hours=myItem.getHours();
        int minutes=myItem.getMinutes();
        int seconds=myItem.getSeconds();
        String time_left="";
        boolean flag=false;
        if(myItem.isRepeat())
        {
            for(int i=0;i<7;i++){
                if(myItem.isDay(i)) {
                    flag=true;
                    break;
                }
            }
            if(flag)
            time_left=MyUtils.getRemainingTime(myItem);
        }
        else
            time_left=MyUtils.getRemainingTime(hours, minutes, seconds);
        String time=MyUtils.getTimeFormat(hours, minutes, seconds);

        holder.myAlarmTitle.setText(myItem.getTitle());
        holder.myAlarmTime.setText(time);
        holder.mySwitch.setChecked(myItem.isChecked());
        if(myItem.isChecked())
            holder.myAlarmTimeLeft.setText(time_left);
        else
            holder.myAlarmTimeLeft.setText("");
        holder.myImage.setImageResource(R.drawable.ic_alarmclockpink);
        holder.repeatAlarm.setChecked(myItem.isRepeat());

        if(myItem.isMonday()) {
            holder.mon.setBackgroundResource(R.drawable.my_button_pressed);
            holder.mon.setTextColor(Color.WHITE);
        }
        else {
            holder.mon.setBackgroundResource(R.drawable.my_button);
            holder.mon.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        if(myItem.isTuesday()) {
            holder.tue.setBackgroundResource(R.drawable.my_button_pressed);
            holder.tue.setTextColor(Color.WHITE);
        }
        else {
            holder.tue.setBackgroundResource(R.drawable.my_button);
            holder.tue.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        if(myItem.isWednesday()) {
            holder.wed.setBackgroundResource(R.drawable.my_button_pressed);
            holder.wed.setTextColor(Color.WHITE);
        }
        else {
            holder.wed.setBackgroundResource(R.drawable.my_button);
            holder.wed.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        if(myItem.isThursday()) {
            holder.thu.setBackgroundResource(R.drawable.my_button_pressed);
            holder.thu.setTextColor(Color.WHITE);
        }
        else {
            holder.thu.setBackgroundResource(R.drawable.my_button);
            holder.thu.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        if(myItem.isFriday()) {
            holder.fri.setBackgroundResource(R.drawable.my_button_pressed);
            holder.fri.setTextColor(Color.WHITE);
        }
        else {
            holder.fri.setBackgroundResource(R.drawable.my_button);
            holder.fri.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        if(myItem.isSaturday()) {
            holder.sat.setBackgroundResource(R.drawable.my_button_pressed);
            holder.sat.setTextColor(Color.WHITE);
        }
        else {
            holder.sat.setBackgroundResource(R.drawable.my_button);
            holder.sat.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        if(myItem.isSunday()) {
            holder.sun.setBackgroundResource(R.drawable.my_button_pressed);
            holder.sun.setTextColor(Color.WHITE);
        }
        else {
            holder.sun.setBackgroundResource(R.drawable.my_button);
            holder.sun.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }
        holder.sun.setPressed(myItem.isSunday());
        snoozeTime=myItem.getSnoozeTime();

        try{
            if(myItem.isRepeat()) {
                Log.w(AlarmConstants.ALARM_TAG, "onBindViewholder Repeat is true");
                float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mContext.getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.childlayout.getLayoutParams();
                params.height = (int) pixels;
                holder.childlayout.setLayoutParams(params);
                holder.childlayout.setVisibility(View.VISIBLE);
                holder.childlayout.setEnabled(true);
            }
            else {
                Log.w(AlarmConstants.ALARM_TAG, "onBindViewholder Repeat is false");
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.childlayout.getLayoutParams();
                params.height = 0;
                holder.childlayout.setVisibility(View.INVISIBLE);
                holder.childlayout.setLayoutParams(params);
                holder.childlayout.setEnabled(false);
            }
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, e.getMessage());
        }
        //Writing code of expanding or collapsing view twice because of sum bug....don't know why writing this code twice works
        try{
            if(myItem.isRepeat()) {
                Log.w(AlarmConstants.ALARM_TAG, "onBindViewholder Repeat is true");
                float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mContext.getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.childlayout.getLayoutParams();
                params.height = (int) pixels;
                holder.childlayout.setLayoutParams(params);
                holder.childlayout.setVisibility(View.VISIBLE);
                holder.childlayout.setEnabled(true);
            }
            else {
                Log.w(AlarmConstants.ALARM_TAG, "onBindViewholder Repeat is false");
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.childlayout.getLayoutParams();
                params.height = 0;
                holder.childlayout.setVisibility(View.INVISIBLE);
                holder.childlayout.setLayoutParams(params);
                holder.childlayout.setEnabled(false);
            }
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        Log.w("MApp", "getItemCount");
        return myAlarmItems.size();
    }
    public int getCheckedTime()
    {
        switch (snoozeTime)
        {
            case 5: return 0;
            case 10: return 1;
            case 15: return 2;
            case 30: return 3;
            case 45: return 4;
            case 60: return 5;
            default: return 2;
        }
    }
}
