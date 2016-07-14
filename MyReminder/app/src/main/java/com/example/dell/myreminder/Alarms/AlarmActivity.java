package com.example.dell.myreminder.Alarms;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.support.design.widget.FloatingActionButton;

import android.widget.ImageView;
import android.widget.Toast;

import com.example.dell.myreminder.Utility.AlarmConstants;
import com.example.dell.myreminder.MainActivity;
import com.example.dell.myreminder.Utility.MyDBHandler;
import com.example.dell.myreminder.Utility.MyUtils;
import com.example.dell.myreminder.R;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;


public class AlarmActivity extends Fragment implements TimePickerDialog.OnTimeSetListener{

    int load = 2;
    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<AlarmItems> alarmItems;

    //DEFINING A CUSTOM ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    CustomAdapterAlarm myAlarmAdapter;

    FloatingActionButton FAB;
    TimePickerDialog tpd;
    Calendar now;
    Toolbar toolbar, mtoolbar;
    CollapsingToolbarLayout collapsingToolbar;

    //RecyclerView
    private RecyclerView alarmView;
    private RecyclerView.LayoutManager mLayoutManager;

    //Database
    MyDBHandler dbHandler;

    //AlarmManager
    private AlarmManager alarmManager;

    private PendingIntent pendingIntent;
    Intent myIntent;
    ImageView headerView;
    String mPath=null;
    Bitmap bm;
    public static final String KEY_IMAGEPATH="userdetails.HeaderImagePath";
    public AlarmActivity() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.activity_alarm, container, false);
        toolbar = (Toolbar) view.findViewById(R.id.anim_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        try {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, "AlarmActivity.setDisplayHomeAsUpEnabled null");
        }
        toolbar.setNavigationIcon(R.drawable.ic_nav_icon);

        //Get header image path
        SharedPreferences sharedPref = getContext().getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);
        mPath=sharedPref.getString(KEY_IMAGEPATH, null);

        //Hide the toolbar of main activity
        mtoolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        mtoolbar.setVisibility(View.GONE);

        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle("My Alarms");

        headerView=(ImageView) collapsingToolbar.findViewById(R.id.header);
        //Set image
        if(mPath!=null)
            loadImageFromStorage();
        collapsingToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, load);
            }
        });

        toolbar.showOverflowMenu();
        setHasOptionsMenu(true);

        now=Calendar.getInstance();
        Log.w("MApp", "onCreateView");
        dbHandler=MyDBHandler.getInstance(getActivity());
        alarmItems=new ArrayList<>();
        alarmItems=dbHandler.getAllAlarms();
        Log.w("MApp", "gotAllAlarms");
        alarmView=(RecyclerView)view.findViewById(R.id.myListView);
        FAB=(FloatingActionButton)view.findViewById(R.id.mfab);

        alarmManager=(AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // alarmView.setHasFixedSize(true);
        Log.w("MApp", "onCreateView");
        // use a linear layout manager
        mLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        alarmView.setLayoutManager(mLayoutManager);
        Log.w("MApp", "onCreateView");
        // specify an adapter (see also next example)
        myAlarmAdapter = new CustomAdapterAlarm(alarmItems, getActivity());
        alarmView.setAdapter(myAlarmAdapter);

        Toast.makeText(getActivity().getApplicationContext(), "hmm", Toast.LENGTH_SHORT).show();
        //set OnClickListener for FAB; Note onClick from xml cannot be used in fragments
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("MApp", "FAB clicked");
                Toast.makeText(getActivity(), "FAB clicked", Toast.LENGTH_SHORT).show();
                tpd = TimePickerDialog.newInstance(
                        AlarmActivity.this,
                        now.get(Calendar.HOUR),
                        now.get(Calendar.MINUTE),
                        true
                );
                tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
                Log.w("MApp", "FAB clicked");
            }

        });

        return view;
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        AlarmItems newItem=new AlarmItems();
        newItem.setHours(hourOfDay);
        newItem.setIsChecked(true);
        newItem.setMinutes(minute);
        newItem.setSeconds(second);
        newItem.setRepeat(false);
        newItem.setId(0);
        newItem.setTitle("Alarm: " + String.valueOf(alarmItems.size() + 1));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        now=Calendar.getInstance();
        int day=now.get(Calendar.DAY_OF_WEEK);
        day-=1;//0- based indexing
        long extra_time=0;
        long s1= calendar.getTimeInMillis();
        long s2=now.getTimeInMillis();
        if(s1<s2)
        {
            day+=1;
            extra_time+=24*3600*1000;
        }

        dbHandler.addAlarm(newItem);//Add item in database
        Log.w("MApp", "onTimeSet");

        myIntent = new Intent(getActivity(), AlarmReceiver.class);
        myIntent.putExtra(AlarmConstants.PAR_KEY, newItem);
        //Using id as request code
        pendingIntent = MyUtils.getPendingIntent(newItem.getId(), getContext(), myIntent, day);//flag if alarm with same id exists then cancel that alarm

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + extra_time,
                pendingIntent);

        alarmItems.add(newItem);//add item in in ArrayList
        myAlarmAdapter.notifyDataSetChanged();//Add in view
        Log.w("MApp", "onTimeSet");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection

        switch (item.getItemId()) {
            case android.R.id.home: //Menu icon
                Log.i(AlarmConstants.ALARM_TAG, "onOptionsItemSelected");
                ((MainActivity)getActivity()).openDrawer();
                Log.i(AlarmConstants.ALARM_TAG, "onOptionsItemSelected");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("Actvity Key", 1);
        super.onSaveInstanceState(outState);
    }
    public void saveImageIntoFile(Bitmap bm)
    {
        ContextWrapper cw = new ContextWrapper(getContext().getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"header.jpg");
        if (mypath.exists()) {
            mypath.delete();
            mypath=new File(directory,"header.jpg");
        }
        mPath=mypath.getAbsolutePath();
        FileOutputStream fos=null;
        try {
            fos =  new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.w(AlarmConstants.ALARM_TAG, "fos exception: "+e.getMessage());
        }
    }
    public void saveIntoSharedPrefs()
    {
        SharedPreferences sharedPref = getContext().getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(mPath!=null)
            editor.putString(KEY_IMAGEPATH, mPath);
        editor.apply();
    }
    private void loadImageFromStorage()
    {
        try {
            File f=new File(mPath);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            headerView.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == load && resultCode == Activity.RESULT_OK && null != data )
        {
            Uri selectedImageUri = data.getData();
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            try{
                BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri), null, o);
            }catch (FileNotFoundException e)
            {
                Log.w(AlarmConstants.ALARM_TAG, "InputStream 1 : "+e.getMessage());
            }


            // The new size we want to scale to
            final int REQUIRED_HEIGHT=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 192, getResources().getDisplayMetrics());
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            final int REQUIRED_WIDTH = size.x;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_WIDTH
                        || height_tmp / 2 < REQUIRED_HEIGHT) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            try{
                bm=BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImageUri), null, o2);
            }catch (FileNotFoundException e) {
                Log.w(AlarmConstants.ALARM_TAG, "InputStream 1 : "+e.getMessage());
            }

            headerView.setImageBitmap(bm);
            saveImageIntoFile(bm);
            saveIntoSharedPrefs();
        }
    }
}
