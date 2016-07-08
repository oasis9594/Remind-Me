package com.example.dell.myreminder;

import android.os.Parcel;
import android.os.Parcelable;


public class GeofencingObject implements Parcelable {
    double latitude;
    double longitude;
    int id;
    String address;
    String title;
    double radius;
    double distance;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    int transitionType;//Enter, Exit, Dwell
    boolean checked, playSound;

    public boolean isPlaySound() {
        return playSound;
    }

    public void setPlaySound(boolean playSound) {
        this.playSound = playSound;
    }

    int markerColor;

    public int getMarkerColor() {
        return markerColor;
    }

    public void setMarkerColor(int markerColor) {
        this.markerColor = markerColor;
    }

    GeofencingObject(){
        setTitle(GeofenceUtils.P_KEY);
        setAddress("");
        setDistance(-1.0);
    }
    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(int transitionType) {
        this.transitionType = transitionType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeInt(this.id);
        dest.writeString(this.address);
        dest.writeString(this.title);
        dest.writeDouble(this.radius);
        dest.writeInt(this.transitionType);
        dest.writeInt(this.markerColor);
        dest.writeDouble(this.distance);
        dest.writeByte(checked ? (byte) 1 : (byte) 0);
        dest.writeByte(playSound ? (byte) 1 : (byte) 0);
    }

    protected GeofencingObject(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.id = in.readInt();
        this.address = in.readString();
        this.title = in.readString();
        this.radius = in.readDouble();
        this.transitionType = in.readInt();
        this.markerColor=in.readInt();
        this.distance=in.readDouble();
        this.checked = in.readByte() != 0;
        this.playSound = in.readByte() != 0;
    }

    public static final Parcelable.Creator<GeofencingObject> CREATOR = new Parcelable.Creator<GeofencingObject>() {
        @Override
        public GeofencingObject createFromParcel(Parcel source) {
            return new GeofencingObject(source);
        }

        @Override
        public GeofencingObject[] newArray(int size) {
            return new GeofencingObject[size];
        }
    };
}
