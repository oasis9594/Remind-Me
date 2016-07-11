package com.example.dell.myreminder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG="MApp";
    NavigationView navigationView;
    String name, email, mPath;
    CircleImageView upload;
    TextView userName, userEmail;
    private static final String KEY_NAME="userdetails.UserName",
            KEY_EMAIL="userdetails.UserEmail",
            NULL_VALUE="userdetails.nullValue",
            KEY_IMAGEPATH="userdetails.ProfileImagePath";
    int act=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My Reminder");
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Bundle bundle=getIntent().getExtras();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        View hView =  navigationView.getHeaderView(0);
        upload=(CircleImageView) hView.findViewById(R.id.profile_image);
        userName=(TextView)hView.findViewById(R.id.headerName);
        userEmail=(TextView)hView.findViewById(R.id.headerEmail);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInput(v);
            }
        });
        getFromSharedPrefs();
        if(mPath!=null)
            loadImageFromStorage();
        if(bundle!=null)
        {
            updateFromBundle(bundle);
        }
        else if(savedInstanceState!=null)
            updateFromBundle(savedInstanceState);
        else
            callAlarmFragment();

    }

    public void getFromSharedPrefs()
    {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);

        name=sharedPref.getString(KEY_NAME, NULL_VALUE);
        if(!(name.equals(NULL_VALUE)))
            userName.setText(name);

        email=sharedPref.getString(KEY_EMAIL, NULL_VALUE);
        if(!(email.equals(NULL_VALUE)))
            userEmail.setText(email);

        mPath=sharedPref.getString(KEY_IMAGEPATH, null);
    }

    private void loadImageFromStorage()
    {
        try {
            File f=new File(mPath);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            upload.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    public void updateFromBundle(Bundle bundle)
    {
        switch (bundle.getInt("Activity Key"))
        {
            case 1:
                callAlarmFragment();
                break;
            case 2:
                callReminderFragment();
                break;
            case 3:
                callGeofenceFragment();
                break;
            case 4:
                callContactUsFragment();
                break;
        }
    }
    public void callAlarmFragment()
    {
        act=1;
        AlarmActivity fragment = new AlarmActivity();
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, "AlarmFragment").commit();
        navigationView.getMenu().getItem(0).setChecked(true);
    }
    public void callGeofenceFragment()
    {
        act=3;
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        GeofenceActivity fragment=new GeofenceActivity();
        fragmentTransaction.replace(R.id.fragment_container, fragment, "GeofenceFragment").commit();
        navigationView.getMenu().getItem(2).setChecked(true);
    }
    public void callReminderFragment()
    {
        act=2;
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        ReminderListFragment fragment=new ReminderListFragment();
        fragmentTransaction.replace(R.id.fragment_container, fragment, "GeofenceFragment").commit();
        navigationView.getMenu().getItem(1).setChecked(true);
    }
    public void callContactUsFragment()
    {
        act=4;
        FragmentTransaction fragmentTransaction =
                getSupportFragmentManager().beginTransaction();
        ContactUs fragment=new ContactUs();
        fragmentTransaction.replace(R.id.fragment_container, fragment, "GeofenceFragment").commit();
        navigationView.getMenu().getItem(1).setChecked(true);
    }
    public void UserInput(View v)
    {
        Intent intent=new Intent(this, UserDetails.class).putExtra("Activity Key", act);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.w(AlarmConstants.ALARM_TAG, "onOptionsItemSelected: MainActivity");
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.alarm) {
            //Replacing the main content with Alarm Fragment
            callAlarmFragment();

        } else if (id == R.id.task) {
            callReminderFragment();

        } else if (id == R.id.loc_reminder_id) {
            callGeofenceFragment();

        } else if(id==R.id.nav_contact) {
            callContactUsFragment();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void openDrawer()
    {
        Log.i(AlarmConstants.ALARM_TAG, "openDrawer");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
        Log.i(AlarmConstants.ALARM_TAG, "openDrawer");
    }

}
