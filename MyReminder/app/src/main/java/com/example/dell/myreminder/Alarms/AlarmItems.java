package com.example.dell.myreminder.Alarms;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmItems implements Parcelable{
    private String title;
    private int hours, minutes, seconds;
    private boolean isChecked;
    private int id;
    boolean repeat;


    int snoozeTime;
    boolean sunday, monday, tuesday, wednesday, thursday, friday, saturday;

    public int getSnoozeTime() {
        return snoozeTime;
    }

    public void setSnoozeTime(int snoozeTime) {
        this.snoozeTime = snoozeTime;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setDay(int day) {
        switch (day)
        {
            case 0:
                if(isSunday())
                    setSunday(false);
                else
                    setSunday(true);
                break;
            case 1:
                if(isMonday())
                    setMonday(false);
                else
                    setMonday(true);
                break;
            case 2:
                if(isTuesday())
                    setTuesday(false);
                else
                    setTuesday(true);
                break;
            case 3:
                if(isWednesday())
                    setWednesday(false);
                else
                    setWednesday(true);
                break;
            case 4:
                if(isThursday())
                    setThursday(false);
                else
                    setThursday(true);
                break;
            case 5:
                if(isFriday())
                    setFriday(false);
                else
                    setFriday(true);
                break;
            case 6:
                if(isSaturday())
                    setSaturday(false);
                else
                    setSaturday(true);
                break;
        }
    }
    public void setDay(int day, boolean b)
    {
        switch (day)
        {
            case 0:
                setSunday(b);
                break;
            case 1:
                setMonday(b);
                break;
            case 2:setTuesday(b);
                break;
            case 3:
                setWednesday(b);
                break;
            case 4:
                setThursday(b);
                break;
            case 5:
                setFriday(b);
                break;
            case 6:
                setSaturday(b);
                break;
        }
    }
    public boolean isDay(int day)
    {
        boolean b=false;
        switch (day)
        {
            case 0:
                b=isSunday();
                break;
            case 1:
                b=isMonday();
                break;
            case 2:
                b=isTuesday();
                break;
            case 3:
                b=isWednesday();
                break;
            case 4:
                b=isThursday();
                break;
            case 5:
                b=isFriday();
                break;
            case 6:
                b=isSaturday();
                break;
        }
        return b;
    }
    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AlarmItems() {
        setSnoozeTime(15);
        setTitle("Alarm");
    }
    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public boolean toggle() {
        return isChecked=!isChecked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeInt(this.hours);
        dest.writeInt(this.minutes);
        dest.writeInt(this.seconds);
        dest.writeByte(isChecked ? (byte) 1 : (byte) 0);
        dest.writeInt(this.id);
        dest.writeByte(repeat ? (byte) 1 : (byte) 0);
        dest.writeInt(this.snoozeTime);
        dest.writeByte(sunday ? (byte) 1 : (byte) 0);
        dest.writeByte(monday ? (byte) 1 : (byte) 0);
        dest.writeByte(tuesday ? (byte) 1 : (byte) 0);
        dest.writeByte(wednesday ? (byte) 1 : (byte) 0);
        dest.writeByte(thursday ? (byte) 1 : (byte) 0);
        dest.writeByte(friday ? (byte) 1 : (byte) 0);
        dest.writeByte(saturday ? (byte) 1 : (byte) 0);
    }

    public AlarmItems(Parcel in) {
        this.title = in.readString();
        this.hours = in.readInt();
        this.minutes = in.readInt();
        this.seconds = in.readInt();
        this.isChecked = in.readByte() != 0;
        this.id = in.readInt();
        this.repeat = in.readByte() != 0;
        this.snoozeTime = in.readInt();
        this.sunday = in.readByte() != 0;
        this.monday = in.readByte() != 0;
        this.tuesday = in.readByte() != 0;
        this.wednesday = in.readByte() != 0;
        this.thursday = in.readByte() != 0;
        this.friday = in.readByte() != 0;
        this.saturday = in.readByte() != 0;
    }

    public static final Creator<AlarmItems> CREATOR = new Creator<AlarmItems>() {
        public AlarmItems createFromParcel(Parcel source) {
            return new AlarmItems(source);
        }

        public AlarmItems[] newArray(int size) {
            return new AlarmItems[size];
        }
    };
}
