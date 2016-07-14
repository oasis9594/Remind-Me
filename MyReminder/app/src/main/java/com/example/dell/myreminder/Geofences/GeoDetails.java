package com.example.dell.myreminder.Geofences;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.dell.myreminder.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rey.material.widget.Button;

public class GeoDetails extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    Toolbar toolbar;
    Marker marker;
    GeofencingObject geofence;
    AppCompatActivity activity;
    int id, kms, ms;
    GeofenceDBHelper dbHelper;
    TextView edit_det_name, det_rad_spinner, det_address_text;
    Button editDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_details);
        toolbar=(Toolbar)findViewById(R.id.det_geo_toolbar);

        activity=this;
        edit_det_name=(TextView)findViewById(R.id.edit_det_name);
        det_rad_spinner=(TextView)findViewById(R.id.det_rad_spinner);
        det_address_text=(TextView)findViewById(R.id.det_address_text);

        editDetails=(Button)findViewById(R.id.det_pick);
        Bundle bundle=getIntent().getExtras();
        id=bundle.getInt(GeofenceUtils.ID_KEY);
        dbHelper=GeofenceDBHelper.getInstance(this);
        geofence=dbHelper.getGeofence(id);

        toolbar.setTitle("My Task");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        edit_det_name.setText(geofence.getTitle());
        kms=(int)geofence.getRadius()/1000;
        ms=((int)geofence.getRadius())%1000;
        Log.w(GeofenceUtils.getTag(),geofence.getRadius()+"");
        String s = "";
        if(kms!=0)
        {
            s=kms+"km ";
        }
        s= s+ms+"m";
        det_rad_spinner.setText(s);
        det_address_text.setText(geofence.getAddress());
        editDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call Activity
                Intent intent=new Intent(activity, AddGeofence.class);
                intent.putExtra(GeofenceUtils.ID_KEY, geofence.getId());
                startActivity(intent);
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap=googleMap;
            LatLng mLocation = new LatLng(geofence.getLatitude(), geofence.getLongitude());
            marker = mMap.addMarker(new MarkerOptions().position(mLocation).draggable(false));
            marker.setIcon(BitmapDescriptorFactory.fromResource(GeofenceUtils.getMarker(geofence.getMarkerColor())));
            CameraUpdate zoom=CameraUpdateFactory.newLatLngZoom(mLocation, 18);
            mMap.animateCamera(zoom);
        }
}
