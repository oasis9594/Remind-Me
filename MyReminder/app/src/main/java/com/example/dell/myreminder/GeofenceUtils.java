package com.example.dell.myreminder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofenceUtils {
    private static final int base=100000, mul=1;
    private static final int mod=999999937;

    public static final String TITLE_KEY="TITLE_KEY", ID_KEY="ID_KEY";
    public static final String P_KEY="GeofencingObjectParcel";
    public static final int black=0, blue=1, chrome=2, green=3, red=4, pink=5, violet=6;

    static int hashFunction(int x) {
        return base + (mul*x)%mod;
    }
    public static PendingIntent getGeofenceTransitionPendingIntent(Context context, int id, String title) {
        Intent intent = new Intent(context, GeofenceWakefulReceiver.class);
        intent.putExtra(TITLE_KEY, title);
        intent.putExtra(ID_KEY, id);
        Log.w(getTag(), "getGeofenceTransitionPendingIntent");
        return PendingIntent.getBroadcast(context, hashFunction(id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static int getMarker(int color)
    {
        switch (color)
        {
            case 0: return R.drawable.ic_location_chrome;
            case 2: return R.drawable.ic_location_black;
            case 1: return R.drawable.ic_location_blue;
            case 3: return R.drawable.ic_location_green;
            case 4: return R.drawable.ic_location_pink;
            case 5: return R.drawable.ic_location_red;
            case 6: return R.drawable.ic_location_violet;
            default: return R.drawable.ic_location_chrome;
        }
    }
    public static int getColor(int id)
    {
        switch (id)
        {
            case 0: return R.color.chrome;
            case 1: return R.color.black;
            case 2: return R.color.cyan;
            case 3: return R.color.green;
            case 5: return R.color.red;
            case 4: return R.color.pink;
            case 6: return R.color.violet;
            default: return R.color.chrome;
        }
    }
    public static String getTag() {
        return GeocodingMapsActivity.TAG;
    }
}
