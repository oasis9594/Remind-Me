package com.example.dell.myreminder;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class UserDetails extends AppCompatActivity {

    int load = 1;
    ImageView upload;
    EditText userName, userEmail, userAddress;
    String name, email, address, mPath;
    private static final String KEY_ADDRESS="userdetails.UserAddress",
            KEY_NAME="userdetails.UserName", KEY_EMAIL="userdetails.UserEmail",
            NULL_VALUE="userdetails.nullValue", TAG="userdetails.TAG",
            KEY_IMAGEPATH="userdetails.ProfileImagePath";
    private boolean isImageChanged;
    Bitmap bm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        userName=(EditText)findViewById(R.id.UserNameText);
        userEmail=(EditText)findViewById(R.id.UserEmailText);
        userAddress=(EditText)findViewById(R.id.UserAddressText);
        upload = ( ImageView )findViewById(R.id.imageView);
        isImageChanged=false;
        getFromSharedPrefs();
        if(mPath!=null)
            loadImageFromStorage();
    }

    public void getFromSharedPrefs()
    {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);

        name=sharedPref.getString(KEY_NAME, NULL_VALUE);
        if(!(name.equals(NULL_VALUE)))
            userName.setText(name);

        email=sharedPref.getString(KEY_EMAIL, NULL_VALUE);
        if(!(email.equals(NULL_VALUE)))
            userEmail.setText(email);

        address=sharedPref.getString(KEY_ADDRESS, NULL_VALUE);
        if(!(address.equals(NULL_VALUE)))
            userAddress.setText(address);

        mPath=sharedPref.getString(KEY_IMAGEPATH, null);
    }
    public void EditProfile(View v) {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, load);
    }

    public void SaveProfile(View v)
    {
        if(isImageChanged)
            saveImageIntoFile(bm);
        saveIntoSharedPrefs();
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == load && resultCode == RESULT_OK && null != data )
        {
            Uri selectedImageUri = data.getData();
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            try{
                BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri), null, o);
            }catch (FileNotFoundException e)
            {
                Log.w(TAG, "InputStream 1 : "+e.getMessage());
            }


            // The new size we want to scale to
            final int REQUIRED_SIZE = 140;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            try{
                bm=BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri), null, o2);
            }catch (FileNotFoundException e) {
                Log.w(TAG, "InputStream 1 : "+e.getMessage());
            }

            upload.setImageBitmap(bm);
            isImageChanged=true;
        }
    }
    public void saveImageIntoFile(Bitmap bm)
    {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");
        if (mypath.exists()) {
            mypath.delete();
            mypath=new File(directory,"profile.jpg");
        }
        if (mypath.exists()) {
            mypath.delete();
            mypath=new File(directory,"profile.jpg");
        }
        mPath=mypath.getAbsolutePath();
        FileOutputStream fos=null;
        try {
            fos =  new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            Log.w(TAG, "fos exception: "+e.getMessage());
        }
    }
    public void saveIntoSharedPrefs()
    {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_user_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_NAME, userName.getText().toString());
        editor.putString(KEY_EMAIL, userEmail.getText().toString());
        editor.putString(KEY_ADDRESS, userAddress.getText().toString());
        if(mPath!=null)
            editor.putString(KEY_IMAGEPATH, mPath);
        editor.apply();
    }
    private void loadImageFromStorage()
    {
        try {
            File f=new File(mPath);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            upload.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}