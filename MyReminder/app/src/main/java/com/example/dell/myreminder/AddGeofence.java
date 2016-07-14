package com.example.dell.myreminder;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;

public class AddGeofence extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    Toolbar toolbar;
    TextView text_geo_name, text_geo_radius, geo_add_colorMark;
    EditText edit_geo_name;
    Spinner geo_rad_spinner, geo_col_spinner;
    GeofenceDBHelper dbHelper;
    Button geo_pick;

    GeofencingObject item, mitem;
    AppCompatActivity activity;
    int radius;int color;
    Bundle bundle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_geofence);
        toolbar=(Toolbar)findViewById(R.id.add_geo_toolbar);
        text_geo_name=(TextView)findViewById(R.id.text_geo_name);
        text_geo_radius=(TextView)findViewById(R.id.text_geo_radius);
        geo_add_colorMark=(TextView)findViewById(R.id.geo_add_colorMark);
        edit_geo_name=(EditText)findViewById(R.id.edit_geo_name);
        geo_rad_spinner=(Spinner)findViewById(R.id.geo_rad_spinner);
        geo_col_spinner=(Spinner)findViewById(R.id.geo_col_spinner);
        geo_pick=(Button)findViewById(R.id.geo_pick);
        dbHelper=GeofenceDBHelper.getInstance(this);

        bundle=getIntent().getExtras();

        activity=this;
        if(bundle==null)
            toolbar.setTitle("New Task");
        else
            toolbar.setTitle("Update Task");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.radius, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        geo_rad_spinner.setAdapter(adapter);
        geo_rad_spinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> myAdapter = ArrayAdapter.createFromResource(this,
                R.array.colors, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        geo_col_spinner.setAdapter(myAdapter);
        geo_col_spinner.setOnItemSelectedListener(this);
        color=0;
        radius=50;

        if(bundle!=null)
        {
            int x=bundle.getInt(GeofenceUtils.ID_KEY);
            mitem=dbHelper.getGeofence(x);
            color=mitem.getMarkerColor();
            geo_col_spinner.setSelection(color);
            radius=(int)mitem.getRadius();
            switch (radius)
            {
                case 50: geo_rad_spinner.setSelection(0);
                    break;
                case 100: geo_rad_spinner.setSelection(1);
                    break;
                case 150: geo_rad_spinner.setSelection(2);
                    break;
                case 200: geo_rad_spinner.setSelection(3);
                    break;
                case 250: geo_rad_spinner.setSelection(4);
                    break;
                case 300: geo_rad_spinner.setSelection(5);
                    break;
                default: geo_rad_spinner.setSelection(0);
            }
            edit_geo_name.setText(mitem.getTitle());
        }
        geo_pick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s=edit_geo_name.getText().toString();
                if(s.equals(""))
                {
                    Toast.makeText(getApplicationContext(), "Enter Task Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                item=new GeofencingObject();

                item.setTitle(s);
                item.setMarkerColor(color);
                item.setRadius(radius);
                item.setPlaySound(true);
                if(bundle==null)
                {
                    item.setAddress("");
                    item.setLatitude(0.0);
                    item.setLongitude(0.0);
                    item.setTransitionType(0);
                    dbHelper.addGeofence(item);
                }
                else {
                    item.setId(mitem.getId());
                    item.setAddress(mitem.getAddress());
                    item.setLatitude(mitem.getLatitude());
                    item.setLongitude(mitem.getLongitude());
                    item.setTransitionType(mitem.getTransitionType());
                    dbHelper.updateGeofence(item);
                }
                Log.w(GeofenceUtils.getTag(), ""+item.getTitle()+" "+item.getRadius()+" "+item.getId());
                //TODO: Call Geocoding Maps Activity and make appropriate changes
                Intent intent=new Intent(activity, GeocodingMapsActivity.class);
                intent.putExtra(GeofenceUtils.P_KEY, item);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String s;
        if(parent.getId()==R.id.geo_rad_spinner) {
            s=getResources().getStringArray(R.array.radius)[position];
            radius=0;
            for(int i=0;i<s.length();i++)
            {
                if(s.charAt(i)>='0'&&s.charAt(i)<='9')
                {
                    Log.w(GeofenceUtils.getTag(), ""+s.charAt(i));
                    radius=radius*10+(int)s.charAt(i)-48;
                }
            }
            Log.w(GeofenceUtils.getTag(), radius+"");
        }
        else {
            color=position;
            Log.w(GeofenceUtils.getTag(), "color: "+ getResources().getStringArray(R.array.colors)[position]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
