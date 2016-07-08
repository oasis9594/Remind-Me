package com.example.dell.myreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

import java.util.ArrayList;

public class GeofenceDBHelper extends SQLiteOpenHelper{
    // instance
    private static GeofenceDBHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "mygeofence.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_NAME = "Geofence";

    //Column Names
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_TITLE="title";
    private static final String COLUMN_LATITUDE="latitude";
    private static final String COLUMN_LONGITUDE="longitude";
    private static final String COLUMN_ADDRESS="address";
    private static final String COLUMN_CHECKED="checked";
    private static final String COLUMN_RADIUS="radius";
    private static final String COLUMN_TRANSITION="transition";
    private static final String COLUMN_MARKER="marker";
    private static final String COLUMN_SOUND="sound";

    private static String getTag()
    {
        return GeocodingMapsActivity.TAG;
    }
    //make your database instance a singleton instance across the entire application's lifecycle
    public static synchronized GeofenceDBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        Log.w(getTag(), "getInstance");
        if (sInstance == null) {
            sInstance = new GeofenceDBHelper(context.getApplicationContext());
            Log.w(getTag(), "Instance null");
        }
        return sInstance;
    }
    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private GeofenceDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.w(getTag(), "onCreate");
        String query="CREATE TABLE "+TABLE_NAME+"("+
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL, " +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_CHECKED + " INTEGER, " +
                COLUMN_RADIUS + " REAL, " +
                COLUMN_TRANSITION + " INTEGER, "+
                COLUMN_MARKER + " INTEGER, "+
                COLUMN_SOUND + " INTEGER "+
                ");";
        try {
            db.execSQL(query);
        }catch (Exception e)
        {
            Log.w(getTag(), e.getMessage());
        }
        Log.w(getTag(), "onCreate Database");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public void addGeofence(GeofencingObject item)
    {
        Log.w(GeofenceUtils.getTag(), "addGeofence in database");
        int m_id;
        SQLiteDatabase db=getWritableDatabase();
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try{
            ContentValues values=new ContentValues();
            values.put(COLUMN_TITLE, item.getTitle());
            values.put(COLUMN_LATITUDE, item.getLatitude());
            values.put(COLUMN_LONGITUDE, item.getLongitude());
            values.put(COLUMN_ADDRESS, item.getAddress());
            values.put(COLUMN_CHECKED, item.isChecked());
            values.put(COLUMN_RADIUS, item.getRadius());
            values.put(COLUMN_TRANSITION, item.getTransitionType());
            values.put(COLUMN_MARKER, item.getMarkerColor());
            values.put(COLUMN_SOUND, item.isPlaySound());

            m_id=(int)db.insertOrThrow(TABLE_NAME, null, values);
            item.setId(m_id);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.w(GeofenceUtils.getTag(), "Error while adding geofence to database");
        }finally {
            db.endTransaction();
        }
    }
    public void deleteGeofence(GeofencingObject item)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        db.beginTransaction();
        try
        {
            db.delete(TABLE_NAME, COLUMN_ID + " = " + item.getId(), null);
            db.setTransactionSuccessful();
        }catch (Exception e) {
            Log.w(getTag(), "Error while deleting alarm from database");
        }
        finally {
            db.endTransaction();
        }
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + "=\"" + item.getId() + "\";");
    }
    public int toggleGeofence(GeofencingObject item)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CHECKED, item.isChecked());

        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(item.getId()) });
    }

    public int updateGeofence(GeofencingObject item)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_LATITUDE, item.getLatitude());
        values.put(COLUMN_LONGITUDE, item.getLongitude());
        values.put(COLUMN_ADDRESS, item.getAddress());
        values.put(COLUMN_CHECKED, item.isChecked());
        values.put(COLUMN_RADIUS, item.getRadius());
        values.put(COLUMN_TRANSITION, item.getTransitionType());
        values.put(COLUMN_MARKER, item.getMarkerColor());
        values.put(COLUMN_SOUND, item.isPlaySound());
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[] { String.valueOf(item.getId()) });
    }

    public ArrayList<GeofencingObject> getAllGeofences() {
        ArrayList<GeofencingObject> myItems = new ArrayList<>();

        String GEOFENCE_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_NAME);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(GEOFENCE_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    GeofencingObject newItem = new GeofencingObject();
                    int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE));
                    double latitude=cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                    double longitude=cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                    int transition=cursor.getInt(cursor.getColumnIndex(COLUMN_TRANSITION));
                    double radius=cursor.getDouble(cursor.getColumnIndex(COLUMN_RADIUS));
                    boolean check = cursor.getInt(cursor.getColumnIndex(COLUMN_CHECKED)) != 0;

                    String address = cursor.getString(cursor.getColumnIndex(COLUMN_ADDRESS));

                    int color=cursor.getInt(cursor.getColumnIndex(COLUMN_MARKER));
                    newItem.setMarkerColor(color);
                    newItem.setId(id);
                    newItem.setChecked(check);
                    newItem.setLatitude(latitude);
                    newItem.setLongitude(longitude);
                    newItem.setAddress(address);
                    newItem.setRadius(radius);
                    newItem.setTitle(title);
                    newItem.setTransitionType(transition);
                    myItems.add(newItem);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(getTag(), "Error while trying to get geofences from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return myItems;
    }
    public GeofencingObject getGeofence(int id)
    {
        GeofencingObject item=new GeofencingObject();
        SQLiteDatabase db = getReadableDatabase();
        String query="SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_ID +"=\"" + id + "\";";
        Cursor c=db.rawQuery(query, null);
        try {
            c.moveToFirst();
            item.setId(id);
            item.setAddress(c.getString(c.getColumnIndex(COLUMN_ADDRESS)));
            item.setTitle(c.getString(c.getColumnIndex(COLUMN_TITLE)));
            item.setLatitude(c.getDouble(c.getColumnIndex(COLUMN_LATITUDE)));
            item.setLongitude(c.getDouble(c.getColumnIndex(COLUMN_LONGITUDE)));
            item.setMarkerColor(c.getInt(c.getColumnIndex(COLUMN_MARKER)));
            item.setRadius(c.getDouble(c.getColumnIndex(COLUMN_RADIUS)));
            item.setTransitionType(c.getInt(c.getColumnIndex(COLUMN_TRANSITION)));
            int b=c.getInt(c.getColumnIndex(COLUMN_CHECKED));
            if(b==1)
                item.setChecked(true);
            else
                item.setChecked(false);
            b=c.getInt(c.getColumnIndex(COLUMN_SOUND));
            if(b==1)
                item.setPlaySound(true);
            else
                item.setPlaySound(false);
            c.close();
            return item;
        } catch (Exception e) {
            Log.w(GeofenceUtils.getTag(), "Error while getting data");
            return null;
        }
    }
    public void updateTitle(GeofencingObject myItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, myItem.getTitle());

        db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(myItem.getId())});
    }
}
