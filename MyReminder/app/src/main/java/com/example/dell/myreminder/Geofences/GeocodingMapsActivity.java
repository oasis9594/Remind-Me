package com.example.dell.myreminder.Geofences;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myreminder.MainActivity;
import com.example.dell.myreminder.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GeocodingMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener,
        ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks,
        ResultCallback<LocationSettingsResult>, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private double mlatitude, mlongitude;
    Marker marker;
    EditText addressText;
    String address, city, state, postalCode, country, knownName;


    protected static final int REQUEST_CHECK_SETTINGS = 0x1, PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    Location mLastLocation;
    FloatingActionButton MyLocationButton;

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final String TAG = "geocodingtesting.TAG";
    private LocationSettingsRequest mLocationSettingsRequest;
    private ImageButton clearButton;
    GeofencingObject item;
    GeofenceDBHelper dbHelper;
    int itemAction;

    Toolbar toolbar;
    protected ArrayList<Geofence> mGeofenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geocoding_maps);

        toolbar=(Toolbar)findViewById(R.id.mapToolbar);if (!checkPlayServices()) {
            Toast.makeText(this, "Device not compatible", Toast.LENGTH_SHORT).show();
            finish();
        }
        Bundle extras=getIntent().getExtras();
        if(extras==null)
        {
            itemAction=-1;
            Log.w(TAG, "Extras are null");
            finish();
        }
        else
        {
            try{
                item=extras.getParcelable(GeofenceUtils.P_KEY);
            }catch (Exception e)
            {
                Log.w(GeofenceUtils.getTag(), e.getMessage());
            }
            if (item != null) {
                if(item.getLatitude()==0.0&&item.getLongitude()==0.0) {
                    //New item
                    itemAction=0;
                }
                else {
                    //Update item
                    Log.w(TAG, "Title : "+item.getTitle());
                    itemAction=1;
                }
                Log.w(TAG, "Item Action: "+itemAction);
            }
            else {
                Log.w(TAG, "Item null: ");
            }
        }
        dbHelper=GeofenceDBHelper.getInstance(this);
        assert toolbar != null;
        toolbar.setTitle("Pick a location");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        address="";
        addressText = (EditText) findViewById(R.id.addressText);
        if(itemAction==1) {
            address=item.getAddress();
            if (addressText != null) {
                addressText.setText(address);
            }
        }
        addressText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Log.w(TAG, "onEditorAction");
                    address = addressText.getText().toString();
                    LatLng latLng = getLocationFromAddress(GeocodingMapsActivity.this, address);
                    if (latLng != null) {
                        mlatitude = latLng.latitude;
                        mlongitude = latLng.longitude;
                        marker.setPosition(latLng);//match this behavior to your 'Send' (or Confirm) button
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                        mMap.animateCamera(cameraUpdate);
                    } else {
                        Log.w(TAG, "onEditorAction: address null");
                    }
                }
                return false;
            }
        });
        if(savedInstanceState!=null)
        {
            //For handling screen rotations
            mlatitude=savedInstanceState.getDouble("Latitude");
            mlongitude=savedInstanceState.getDouble("Longitude");
            Log.w(TAG, "itemAction savedInstanceState: "+itemAction);
        }
        else
        {
            mlatitude=item.getLatitude();
            mlongitude=item.getLongitude();
        }

        clearButton=(ImageButton)findViewById(R.id.clear_text);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressText.setText("");
            }
        });

        MyLocationButton=(FloatingActionButton)findViewById(R.id.fab);
        MyLocationButton.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        MyLocationButton.setRippleColor(Color.parseColor("#f5f5f5"));
        MyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGoogleApiClient.isConnected()) {
                    LatLng latLng = getCurrentLocation();
                    if (latLng != null) {
                        setAddress();
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
                        mMap.animateCamera(cameraUpdate);
                    }

                }
            }
        });
        MyLocationButton.setVisibility(View.GONE);
        mGeofenceList=new ArrayList<>();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.w(TAG, "onCreate");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.w(TAG, "onMapReady1");
        mMap = googleMap;
        enableMyLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.setPosition(latLng);
                mlatitude=latLng.latitude;
                mlongitude=latLng.longitude;
                setAddress();
            }
        });
        Log.w(TAG, "onMapReady2");
        LatLng mLocation=null;
        if(mGoogleApiClient.isConnected()&&itemAction==0)
            mLocation = getCurrentLocation();
        Log.w(TAG, "onMapReady3");
        if (itemAction==0&&mLocation != null) {
            mlatitude = mLocation.latitude;
            mlongitude = mLocation.longitude;
        }
        else if(itemAction==1) {
            mLocation=new LatLng(mlatitude, mlongitude);
        }
        else
            mLocation = new LatLng(0.0, 0.0);

        Log.w(TAG, "onMapReady3");
        if(mGoogleApiClient.isConnected())
        setAddress();
        Log.w(TAG, "onMapReady4");
        marker = mMap.addMarker(new MarkerOptions().position(mLocation).draggable(true));
        Log.w(GeofenceUtils.getTag(), item.getMarkerColor()+"");
        marker.setIcon(BitmapDescriptorFactory.fromResource(GeofenceUtils.getMarker(item.getMarkerColor())));
        mMap.setOnMarkerDragListener(this);
        if(itemAction==1)
        {
            CameraUpdate cameraUpdate=CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLatitude(), item.getLongitude()), 18);
            CameraUpdateFactory.newLatLngZoom(mLocation, 18);
            mMap.animateCamera(cameraUpdate);

        }
        Log.w(TAG, "onMapReady5");
    }

    private void enableMyLocation() {
        Log.w(TAG, "enableMyLocation1");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            MyLocationButton.setVisibility(View.VISIBLE);
        }
        Log.w(TAG, "enableMyLocation2");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    public void createLocationRequest() {
        //Create a location Request
        Log.i(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);//sets the rate in milliseconds at which your app prefers to receive location updates
        mLocationRequest.setFastestInterval(10000);//sets fastest rate in milliseconds at which your app can handle location updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Sets the priority
        mLocationRequest.setSmallestDisplacement(20);
    }

    public void buildLocationSettingsRequest() {
        //Build a location request
        Log.i(TAG, "buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    public void checkLocationSettings() {
        Log.i(TAG, "checkLocationSettings");
        //check whether the current location settings are satisfied
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        Log.i(TAG, "locationSettings");
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:

                Log.i(TAG, "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            GeocodingMapsActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way to fix the
                // settings so we won't show the dialog.
                break;
        }
    }
    public void fetchAddress() throws IOException {
        Log.w(TAG, "fetchAddress 1");
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(mlatitude, mlongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            int idx=addresses.get(0).getMaxAddressLineIndex();
        address = "";
        for(int i=0;i<=idx;i++)
        {
            address+=addresses.get(0).getAddressLine(i);
            if(i!=idx)
                address+=", ";
        }
         // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        city = addresses.get(0).getLocality();
        state = addresses.get(0).getAdminArea();
        country = addresses.get(0).getCountryName();
        postalCode = addresses.get(0).getPostalCode();
        knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            Log.w(TAG, "fetchAddress 2");
    }

    @Override
    public void onMarkerDragStart(Marker marker) { }

    @Override
    public void onMarkerDrag(Marker marker) { }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.w(TAG, "onMarkerDragEnd");
        LatLng location=marker.getPosition();
        mlatitude=location.latitude;
        mlongitude=location.longitude;
        setAddress();
    }
    private void setAddress()
    {
        Log.w(TAG, "setAddress 1");
        try{
            fetchAddress();
        }catch (IOException e)
        {
            Log.w(TAG, "onMarkerDragEnd fetch address");
            Toast.makeText(this,"onMarkerDragEnd fetch address" ,Toast.LENGTH_SHORT ).show();
        }
        addressText.setText(address);
        Log.w(TAG, "setAddress 1");
    }

    private boolean checkPlayServices() {
        Log.w(TAG, "checkPlayServices 1");
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }

        return true;
    }
    @Override
    public void onConnected(Bundle bundle) {
        Log.w(TAG, "onConnected 1");
        LatLng latLng = getCurrentLocation();
        if (latLng != null && itemAction==0) {
            //Show current location when activity is first created and item is a new item
            mlatitude = latLng.latitude;
            mlongitude = latLng.longitude;
            setAddress();
            marker.setPosition(latLng);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
            mMap.animateCamera(cameraUpdate);
        }
        Log.w(TAG, "onConnected 2");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended 1");
        mGoogleApiClient.connect();
        Log.w(TAG, "onConnectionSuspended 2");
    }
    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed");
    }
    public LatLng getCurrentLocation()
    {
        Log.w(TAG, "getCurrentLocation 1");
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }catch (SecurityException e)
        {
            Log.w(TAG, "onConnected: "+e.getMessage());
        }
        Log.w(TAG, "getCurrentLocation 2");
        if(mLastLocation!=null)
        return new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        else return new LatLng(0.0, 0.0);
    }
    public LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return p1;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.w(TAG, "onSaveInstanceState");
        outState.putBoolean("firstTime", false);
        outState.putDouble("Latitude", mlatitude);
        outState.putDouble("Longitude", mlongitude);
        super.onSaveInstanceState(outState);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_satellite) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
        else if(id == R.id.action_normalview) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
        else if(id == R.id.action_hybrid) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        else if(id == R.id.action_terrain) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }
        return super.onOptionsItemSelected(item);
    }

    public void CreateGeofence(View view) {
        Log.w(TAG, "CreateGeofence1");
        if(addressText.getText().toString()=="") {
            Toast.makeText(this, "Enter Address", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            item.setAddress(addressText.getText().toString());
        }

        Log.w(TAG, "CreateGeofence2");

        item.setLatitude(mlatitude);
        item.setLongitude(mlongitude);
        item.setChecked(true);
        item.setTransitionType(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        dbHelper.updateGeofence(item);
        Log.w(TAG, "CreateGeofence3 " + item.getId() + item.getTitle() + item.getAddress() + item.getLatitude() + item.getLongitude() + item.getRadius());

        try{
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(item.getId() + "GEOFENCE")

                        // Set the circular region of this geofence.
                .setCircularRegion(
                        item.getLatitude(),
                        item.getLongitude(),
                        (float) item.getRadius()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                        // Set the transition types of interest. Alerts are only generated for these
                        // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)

                        // Create the geofence.
                .build());

        }catch (Exception e)
        {
            Log.w(TAG, "some exception: " + e.getMessage());

            if(itemAction==0) {
                dbHelper.deleteGeofence(item);
                item.setTitle(GeofenceUtils.P_KEY);
            }
            return;
        }
        Log.w(TAG, "CreateGeofence4");

        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(),
                    GeofenceUtils.getGeofenceTransitionPendingIntent(this, item.getId(), item.getTitle()));
        }catch (SecurityException e) {
            Toast.makeText(this, "Location Services Unavailable!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.w(TAG, "CreateGeofence6");
        Intent intent=new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("Activity Key", 3);
        startActivity(intent);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }
    public void onBackPressed() {
        if(item.getAddress().equals(""))
        dbHelper.deleteGeofence(item);
        super.onBackPressed();
    }
}
