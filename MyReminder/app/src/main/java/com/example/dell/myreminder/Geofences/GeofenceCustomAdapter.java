package com.example.dell.myreminder.Geofences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.myreminder.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.rey.material.widget.Button;

import java.util.ArrayList;

public class GeofenceCustomAdapter extends RecyclerView.Adapter<GeofenceCustomAdapter.ViewHolder>{

    ArrayList<GeofencingObject> myGeofenceItems;
    Activity mContext;
    GoogleApiClient googleApiClient;
    protected ArrayList<Geofence> mGeofenceList;
    GeofenceDBHelper dbHelper;
    public GeofenceCustomAdapter(ArrayList<GeofencingObject> myDataset, Activity context, GoogleApiClient googleApiClient) {
        myGeofenceItems = myDataset;
        this.mContext=context;
        this.googleApiClient=googleApiClient;
        dbHelper=GeofenceDBHelper.getInstance(context);
        mGeofenceList=new ArrayList<>();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView locMarker;
        Button deleteLoc, completeLoc;
        TextView LocDescription;
        TextView LocAddress, LocDistance;
        LinearLayout layout;

        public IMyViewHolderClicks mListener;
        public ViewHolder(View customView, IMyViewHolderClicks listener) {
            super(customView);
            mListener=listener;

            locMarker=(ImageView)customView.findViewById(R.id.myLocImage);
            LocAddress=(TextView)customView.findViewById(R.id.LocAddress);
            LocDescription=(TextView)customView.findViewById(R.id.locDescTextView);
            LocDistance=(TextView)customView.findViewById(R.id.LocDistance);
            layout=(LinearLayout)customView.findViewById(R.id.childLocLayout);

            deleteLoc=(Button)layout.findViewById(R.id.LocDelete);
            completeLoc=(Button)layout.findViewById(R.id.LocComplete);

            deleteLoc.setOnClickListener(this);
            completeLoc.setOnClickListener(this);
            customView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if(v instanceof Button) {
                if (v.getId() == R.id.LocDelete)
                    mListener.deleteGeofence((Button) v, pos);
                else
                    mListener.completeGeofence((Button) v, pos);
            }
            else
                mListener.showGeofence(v, pos);
        }
        public interface IMyViewHolderClicks{
            void showGeofence(View view, int pos);
            void deleteGeofence(Button b, int pos);
            void completeGeofence(Button b, int pos);
        }
    }
    @Override
    public GeofenceCustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View customView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.geofence_custom_adapter, parent, false);
        ViewHolder vh=new ViewHolder(customView, new ViewHolder.IMyViewHolderClicks() {
            GeofencingObject myObject;
            @Override
            public void showGeofence(View view, int pos) {
                //Call activity
                myObject=myGeofenceItems.get(pos);
                Intent intent=new Intent(mContext, GeoDetails.class);
                intent.putExtra(GeofenceUtils.ID_KEY, myObject.getId());
                mContext.startActivity(intent);
            }

            @Override
            public void deleteGeofence(Button b,final int pos) {
                myObject=myGeofenceItems.get(pos);
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete this task?");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(googleApiClient.isConnected())
                            removeGeofence(myObject);//remove geofence
                        dbHelper.deleteGeofence(myObject);//remove from database
                        myGeofenceItems.remove(pos);//remove from view
                        notifyItemRemoved(pos);
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
            public void completeGeofence(Button b, int pos) {
                myObject=myGeofenceItems.get(pos);
                myObject.setChecked(!myObject.isChecked());//update object
                dbHelper.toggleGeofence(myObject);//update in database
                if(myObject.isChecked()) {
                    try{
                        mGeofenceList.add(new Geofence.Builder()
                                // Set the request ID of the geofence. This is a string to identify this
                                // geofence.
                                .setRequestId(myObject.getId() + "GEOFENCE")

                                // Set the circular region of this geofence.
                                .setCircularRegion(
                                        myObject.getLatitude(),
                                        myObject.getLongitude(),
                                        (float) myObject.getRadius()
                                )
                                .setExpirationDuration(GeocodingMapsActivity.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                                // Set the transition types of interest. Alerts are only generated for these
                                // transition. We track entry transitions.
                                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER )

                                // Create the geofence.
                                .build());
                    }catch (Exception e)
                    {
                        Log.w(GeofenceUtils.getTag(), "some exception: " + e.getMessage());
                    }
                    try {
                        LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(),
                                GeofenceUtils.getGeofenceTransitionPendingIntent(mContext, myObject.getId(), myObject.getTitle()));
                    }
                    catch (SecurityException e) {
                        Toast.makeText(mContext, "Security Exception: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                        myObject.setTitle(GeofenceUtils.P_KEY);
                    }
                }
                else {
                    if(googleApiClient.isConnected())
                        removeGeofence(myObject);
                }
                notifyItemChanged(pos);
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(GeofenceCustomAdapter.ViewHolder holder, int position) {
        GeofencingObject geofence=myGeofenceItems.get(position);

        holder.LocDescription.setText(geofence.getTitle());
        holder.LocAddress.setText(geofence.getAddress());
        holder.locMarker.setImageResource(GeofenceUtils.getMarker(geofence.getMarkerColor()));
        holder.LocDistance.setTextColor(ContextCompat.getColor(mContext , GeofenceUtils.getColor(geofence.getMarkerColor())));
        holder.deleteLoc.setTextColor(ContextCompat.getColor(mContext , GeofenceUtils.getColor(geofence.getMarkerColor())));
        holder.completeLoc.setTextColor(ContextCompat.getColor(mContext , GeofenceUtils.getColor(geofence.getMarkerColor())));
        if(!geofence.isChecked())//Geofence disabled and task completed
        {
            //strikethrough text
            holder.LocDescription.setPaintFlags(holder.LocDescription.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            holder.completeLoc.setText(R.string.task_not_completed);
        }
        else
        {
            holder.LocDescription.setPaintFlags(holder.LocDescription.getPaintFlags()& (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.completeLoc.setText(R.string.task_completed);
        }

        if(geofence.getDistance()!=-1.0)
        {
            double d=geofence.getDistance();
            int kms=(int)d/1000;
            int meters=(int)d%1000;
            String s="";
            if(kms!=0)
                s+=kms+"km ";
            s+=meters+"m";
            holder.LocDistance.setText(s);
        }
        else
            holder.LocDescription.setText("");

    }

    @Override
    public int getItemCount() {
        Log.w(GeofenceUtils.getTag(), "getItemCount");
        return myGeofenceItems.size();
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
    public void removeGeofence(GeofencingObject item) {
        if (googleApiClient.isConnected()) {
            LocationServices.GeofencingApi.removeGeofences(
                    googleApiClient,
                    // This is the same pending intent that was used in addGeofences().
                    GeofenceUtils.getGeofenceTransitionPendingIntent(mContext, item.getId(), item.getTitle())
            );
        } else {
            Toast.makeText(mContext, "Not connected", Toast.LENGTH_SHORT).show();
        }
    }
}
