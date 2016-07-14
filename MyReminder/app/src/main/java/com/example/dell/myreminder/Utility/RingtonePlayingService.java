package com.example.dell.myreminder.Utility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

public class RingtonePlayingService extends Service {
    int ringmode;
    Ringtone ringtone;
    AudioManager audioManager;
    int volumeAlarm;
    int volumeRing;
    MediaPlayer mediaPlayer;
    public RingtonePlayingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri alert;
        try{
            SharedPreferences getAlarms = PreferenceManager.
                    getDefaultSharedPreferences(getBaseContext());
            String alarms = getAlarms.getString("ringtone", "default ringtone");
            if(alarms.equals("default ringtone"))
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            else
                alert = Uri.parse(alarms);
        }catch (Exception e)
        {
            Log.e(AlarmConstants.ALARM_TAG, e.getMessage());
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        if(alert == null){
            // alert is null, using backup
            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if(alert == null){  //
                // alert backup is null, using 2nd backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), alert);
        volumeAlarm=audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        volumeRing=audioManager.getStreamVolume(AudioManager.STREAM_RING);
        ringmode=audioManager.getRingerMode();
        int maxVolumeAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        int maxVolumeRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolumeAlarm, 0);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolumeRing, 0);
        try{

            mediaPlayer=new MediaPlayer();
            mediaPlayer.setDataSource(this, alert);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch (IOException e)
        {
            //Log error message
            Log.e(AlarmConstants.ALARM_TAG, "MediaPlayer error: e.getMessage()");

            //play using RingtoneManager if something wents wrong
            ringtone.play();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if(mediaPlayer.isPlaying())
            mediaPlayer.stop();
        if (ringtone.isPlaying())
            ringtone.stop();
        Log.w("RingtoneService", "cancelAlarm dialog");
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, volumeAlarm, AudioManager.FLAG_ALLOW_RINGER_MODES);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, volumeRing, AudioManager.FLAG_ALLOW_RINGER_MODES);
        audioManager.setRingerMode(ringmode);
        super.onDestroy();
    }
}
