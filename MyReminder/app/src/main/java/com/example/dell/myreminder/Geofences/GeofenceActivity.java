package com.example.dell.myreminder.Geofences;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dell.myreminder.MainActivity;
import com.example.dell.myreminder.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.ArrayList;

public class GeofenceActivity extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        ResultCallback<LocationSettingsResult> {

    RecyclerView recyclerView;
    Toolbar geofenceToolbar;
    GeofenceCustomAdapter customAdapter;
    GeofenceDBHelper dbHelper;
    ArrayList<GeofencingObject> geofencingObjects;

    GoogleApiClient googleApiClient;
    FloatingActionButton FAB;
    Location mLastLocation;
    double latitude, longitude;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    LocationRequest mLocationRequest;
    LocationSettingsRequest mLocationSettingsRequest;
    public GeofenceActivity() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.w(GeofenceUtils.getTag(), "onCreateView1");
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.activity_geofence, container, false);
        // get the listview
        recyclerView = (RecyclerView) view.findViewById(R.id.geofenceActivityList);

        Log.w(GeofenceUtils.getTag(), "onCreateView2");
        geofenceToolbar=(Toolbar)view.findViewById(R.id.geofenceActivityToolbar);

        //Hide the toolbar of main activity
        Toolbar mtoolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        mtoolbar.setVisibility(View.GONE);

        Log.w(GeofenceUtils.getTag(), "onCreateView3");
        try {
            geofenceToolbar.setTitle("Location Reminder");
            geofenceToolbar.setTitleTextColor(Color.WHITE);
        }catch (Exception e)
        {
            Log.w(GeofenceUtils.getTag(), e.getMessage());
        }
        Log.w(GeofenceUtils.getTag(), "onCreateView4");
        ((AppCompatActivity) getActivity()).setSupportActionBar(geofenceToolbar);
        geofenceToolbar.showOverflowMenu();
        setHasOptionsMenu(true);
        geofenceToolbar.setNavigationIcon(R.drawable.ic_nav_icon);
        FAB=(FloatingActionButton)view.findViewById(R.id.myLocFab);
        dbHelper=GeofenceDBHelper.getInstance(getActivity());
        Log.w(GeofenceUtils.getTag(), "Getting all geofences!!!");
        geofencingObjects=dbHelper.getAllGeofences();
        Log.w(GeofenceUtils.getTag(), "gotAllGeofences!!!...building googleApiClient");

        /*
        LocationManager lm = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setMessage(getContext().getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(getContext().getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(getContext().getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                }
            });
            dialog.show();
        }*/
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        checkLocationSettings();

        Log.w(GeofenceUtils.getTag(), "Creating list adapter");
        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        customAdapter=new GeofenceCustomAdapter(geofencingObjects, getActivity(), googleApiClient);
        recyclerView.setAdapter(customAdapter);
        Log.w(GeofenceUtils.getTag(), "Created list adapter");

        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call activity
                Intent intent=new Intent(getActivity(), AddGeofence.class);
                startActivity(intent);
            }
        });
        return view;
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    public void createLocationRequest()
    {
        //Create a location Request
        Log.i(GeofenceUtils.getTag(), "createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);//sets the rate in milliseconds at which your app prefers to receive location updates
        mLocationRequest.setFastestInterval(10000);//sets fastest rate in milliseconds at which your app can handle location updates
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Sets the priority
        mLocationRequest.setSmallestDisplacement(5);
    }
    public void buildLocationSettingsRequest() {
        //Build a location request
        Log.i(GeofenceUtils.getTag(), "buildLocationSettingsRequest");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    public void checkLocationSettings()
    {
        Log.i(GeofenceUtils.getTag(), "checkLocationSettings");
        //check whether the current location settings are satisfied
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        mLocationSettingsRequest);
        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult result) {
        Log.i(GeofenceUtils.getTag(), "locationSettings");
        final Status status = result.getStatus();
        final LocationSettingsStates state = result.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startLocationUpdates();
                Log.i(GeofenceUtils.getTag(), "All location settings are satisfied.");
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. But could be fixed by showing the user
                // a dialog.
                Log.i(GeofenceUtils.getTag(), "Location settings are not satisfied. Show the user a dialog to" +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            getActivity(), REQUEST_CHECK_SETTINGS);
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

    protected void startLocationUpdates() {
        Log.i(GeofenceUtils.getTag(), "startLocationUpdates");
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient, mLocationRequest, this);
            Log.i(GeofenceUtils.getTag(), "startLocationUpdates");
        } catch (SecurityException e) {
            Log.i(GeofenceUtils.getTag(), "LocationUpdates Failed");
        }

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        Log.i(GeofenceUtils.getTag(), "stopLocationUpdates");
        try{
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
        catch (RuntimeException e)
        {
            Log.i(GeofenceUtils.getTag(), "stopLocationUpdates Failed, "+e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(GeofenceUtils.getTag(), "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(GeofenceUtils.getTag(), "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
        Log.i(GeofenceUtils.getTag(), "Connected");
        if (mLastLocation == null) {
            try {
                Log.i(GeofenceUtils.getTag(), "Location null");
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                Location locationB = new Location("point B");
                for(GeofencingObject o: geofencingObjects) {
                    locationB.setLatitude(o.getLatitude());
                    locationB.setLongitude(o.getLongitude());
                    Log.d(GeofenceUtils.getTag(), o.getLatitude()+" "+o.getLongitude());
                    if(mLastLocation!=null)
                    o.setDistance(mLastLocation.distanceTo(locationB));
                    Log.d(GeofenceUtils.getTag(), o.getLatitude()+" "+o.getLongitude());
                }
                customAdapter.notifyDataSetChanged();
                startLocationUpdates();
            } catch (SecurityException e){
                Log.i(GeofenceUtils.getTag(), "SecurityException: "+e.toString());
            }
        }
        else
        {
            Location locationB = new Location("point B");
            for(GeofencingObject o: geofencingObjects) {
                locationB.setLatitude(o.getLatitude());
                locationB.setLongitude(o.getLongitude());
                o.setDistance(mLastLocation.distanceTo(locationB));
            }
            customAdapter.notifyDataSetChanged();
            startLocationUpdates();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;
        Toast.makeText(getActivity() , "Location Changed", Toast.LENGTH_SHORT).show();
        Log.i(GeofenceUtils.getTag(), "Location Changed");
        latitude=mLastLocation.getLatitude();
        longitude=mLastLocation.getLongitude();

        Location locationB = new Location("point B");
        for(GeofencingObject o: geofencingObjects) {
            locationB.setLatitude(o.getLatitude());
            locationB.setLongitude(o.getLongitude());
            o.setDistance(mLastLocation.distanceTo(locationB));
        }
        customAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        Log.i(GeofenceUtils.getTag(), "onStart");
        super.onStart();
        // Connect the client.
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        Log.i(GeofenceUtils.getTag(), "onStop");
        // Disconnecting the client invalidates it.
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        Log.i(GeofenceUtils.getTag(), "onPause");
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("Activity Key", 3);
        super.onSaveInstanceState(outState);
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
                ((MainActivity)getActivity()).openDrawer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
