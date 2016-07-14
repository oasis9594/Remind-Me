package com.example.dell.myreminder.Utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.dell.myreminder.Alarms.AlarmItems;

import java.util.ArrayList;

public class MyDBHandler extends SQLiteOpenHelper{

    // instance
    private static MyDBHandler sInstance;

    // Database Info
    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_NAME = "alarm";

    //Column Names
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_TITLE="title";
    private static final String COLUMN_HOURS="hours";
    private static final String COLUMN_MINUTES="minutes";
    private static final String COLUMN_SECONDS="seconds";
    private static final String COLUMN_CHECKED="checked";
    private static final String COLUMN_MONDAY="monday";
    private static final String COLUMN_TUESDAY="tuesday";
    private static final String COLUMN_WEDNESDAY="wednesday";
    private static final String COLUMN_THURSDAY="thursday";
    private static final String COLUMN_FRIDAY="friday";
    private static final String COLUMN_SATURDAY="saturday";
    private static final String COLUMN_SUNDAY="sunday";
    private static final String COLUMN_REPEAT="repeat";
    private static final String COLUMN_SNOOZE="snooze";


    //make your database instance a singleton instance across the entire application's lifecycle
    public static synchronized MyDBHandler getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        Log.w(AlarmConstants.ALARM_TAG, "getInstance");
        if (sInstance == null) {
            sInstance = new MyDBHandler(context.getApplicationContext());
            Log.w(AlarmConstants.ALARM_TAG, "Instance null");
        }
        return sInstance;
    }
    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w("MyApp", "onCreate");
        String query="CREATE TABLE "+TABLE_NAME+"("+
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_HOURS + " INTEGER, " +
                COLUMN_MINUTES + " INTEGER, " +
                COLUMN_SECONDS + " INTEGER, " +
                COLUMN_CHECKED + " INTEGER, " +
                COLUMN_REPEAT + " INTEGER, " +
                COLUMN_MONDAY + " INTEGER, " +
                COLUMN_TUESDAY + " INTEGER, " +
                COLUMN_WEDNESDAY + " INTEGER, " +
                COLUMN_THURSDAY + " INTEGER, " +
                COLUMN_FRIDAY + " INTEGER, " +
                COLUMN_SATURDAY + " INTEGER, " +
                COLUMN_SUNDAY + " INTEGER, " +
                COLUMN_SNOOZE + " INTEGER " +
                ");";
        try {
            db.execSQL(query);
        }catch (Exception e)
        {
            Log.w(AlarmConstants.ALARM_TAG, e.getMessage());
        }
        Log.w(AlarmConstants.ALARM_TAG, "onCreate Database");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
    public void addAlarm(AlarmItems item)
    {
        Log.w(AlarmConstants.ALARM_TAG, "addUser in database");
        int m_id;
        SQLiteDatabase db=getWritableDatabase();
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try{
            ContentValues values=new ContentValues();
            values.put(COLUMN_TITLE, item.getTitle());
            values.put(COLUMN_HOURS, item.getHours());
            values.put(COLUMN_MINUTES, item.getMinutes());
            values.put(COLUMN_SECONDS, item.getSeconds());
            values.put(COLUMN_CHECKED, item.isChecked());
            values.put(COLUMN_REPEAT, item.isRepeat());
            values.put(COLUMN_MONDAY, item.isMonday());
            values.put(COLUMN_TUESDAY, item.isTuesday());
            values.put(COLUMN_WEDNESDAY, item.isWednesday());
            values.put(COLUMN_THURSDAY, item.isThursday());
            values.put(COLUMN_FRIDAY, item.isFriday());
            values.put(COLUMN_SATURDAY, item.isSaturday());
            values.put(COLUMN_SUNDAY, item.isSunday());
            values.put(COLUMN_SNOOZE, item.getSnoozeTime());
            m_id=(int)db.insertOrThrow(TABLE_NAME, null, values);
            item.setId(m_id);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.w("MyApp", "Error while adding user to database");
        }finally {
            db.endTransaction();
        }
    }
    public void deleteAlarm(AlarmItems item)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.beginTransaction();
        try
        {
            db.delete(TABLE_NAME, COLUMN_ID + " = " + item.getId(), null);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.w(AlarmConstants.ALARM_TAG, "Error while deleting alarm from database");
        }
        finally {
            db.endTransaction();
        }
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=\"" + item.getId() + "\";");
    }
    public int toggleAlarm(AlarmItems item)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECKED, item.isChecked());

        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(item.getId()) });
    }
    public int updateAlarm(AlarmItems item)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECKED, item.isChecked());
        values.put(COLUMN_HOURS, item.getHours());
        values.put(COLUMN_MINUTES, item.getMinutes());
        values.put(COLUMN_SECONDS, item.getSeconds());
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_REPEAT, item.isRepeat());
        values.put(COLUMN_SNOOZE, item.getSnoozeTime());
        updateDays(item);
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(item.getId()) });
    }
    public ArrayList<AlarmItems> getAllAlarms() {
        ArrayList<AlarmItems> myItems = new ArrayList<>();

        // SELECT * FROM POSTS
        // LEFT OUTER JOIN USERS
        // ON POSTS.KEY_POST_USER_ID_FK = USERS.KEY_USER_ID
        String ALARM_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_NAME);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ALARM_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    AlarmItems newItem = new AlarmItems();
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                    int hours=cursor.getInt(cursor.getColumnIndex(COLUMN_HOURS));
                    int minutes=cursor.getInt(cursor.getColumnIndex(COLUMN_MINUTES));
                    int seconds=cursor.getInt(cursor.getColumnIndex(COLUMN_SECONDS));
                    int snoozeTime=cursor.getInt(cursor.getColumnIndex(COLUMN_SNOOZE));
                    boolean check;
                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_CHECKED)) != 0;
                    newItem.setIsChecked(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_REPEAT)) != 0;
                    newItem.setRepeat(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_MONDAY)) != 0;
                    newItem.setMonday(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_TUESDAY)) != 0;
                    newItem.setTuesday(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_WEDNESDAY)) != 0;
                    newItem.setWednesday(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_THURSDAY)) != 0;
                    newItem.setThursday(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_FRIDAY)) != 0;
                    newItem.setFriday(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_SATURDAY)) != 0;
                    newItem.setSaturday(check);

                    check = cursor.getInt(cursor.getColumnIndex(COLUMN_SUNDAY)) != 0;
                    newItem.setSunday(check);

                    newItem.setId(id);
                    newItem.setHours(hours);
                    newItem.setMinutes(minutes);
                    newItem.setSeconds(seconds);
                    newItem.setTitle(title);
                    newItem.setSnoozeTime(snoozeTime);
                    myItems.add(newItem);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("MyApp", "Error while trying to get alarms from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return myItems;
    }
    public void updateSnoozeTime(AlarmItems myItem)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SNOOZE, myItem.getSnoozeTime());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(myItem.getId())});
    }
    public void updateRepeat(AlarmItems myItem)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_REPEAT, myItem.isRepeat());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(myItem.getId())});
    }
    public void updateTitle(AlarmItems myItem)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, myItem.getTitle());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(myItem.getId())});
    }
    public void updateDays(AlarmItems myItem)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_MONDAY, myItem.isMonday());
        values.put(COLUMN_TUESDAY, myItem.isTuesday());
        values.put(COLUMN_WEDNESDAY, myItem.isWednesday());
        values.put(COLUMN_THURSDAY, myItem.isThursday());
        values.put(COLUMN_FRIDAY, myItem.isFriday());
        values.put(COLUMN_SATURDAY, myItem.isSaturday());
        values.put(COLUMN_SUNDAY, myItem.isSunday());
        db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(myItem.getId()) });
    }
}

