package com.example.dell.myreminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;


public class ContactUs extends Fragment {

    Handler handler;
    TextView mailAddress;

    Animation in;
    Animation out;

    boolean fadeOut,clicked;
    public ContactUs() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Contact Us");
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        Toolbar mtoolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        mtoolbar.setVisibility(View.GONE);


        Toolbar toolbar=(Toolbar)view.findViewById(R.id.contactActivityToolbar);
        toolbar.setTitle("Contact Us");
        toolbar.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_nav_icon);
        toolbar.showOverflowMenu();

        fadeOut = true;
        clicked = false;
        handler = new Handler();

        mailAddress = (TextView) view.findViewById(R.id.mailAddress);
        mailAddress.setTextColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
        mailAddress.setText("mayanknarula96@gmail.com\noasis9594@gmail.com");


        out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(2000);

        in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(2000);
        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOut = true;
                mailAddress.setText("mayanknarula96@gmail.com\noasis9594@gmail.com");
                mailAddress.startAnimation(out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        out.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOut = true;
                mailAddress.setText("oasis9594@gmail.com\nmayanknarula96@gmail.com");
                mailAddress.startAnimation(in);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationStart(Animation arg0) {

            }
        });

        mailAddress.startAnimation(out);
        handler.postDelayed(mFadeOut, 1000);

        mailAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked = true;
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"oasis9594@gmail.com", "mayanknarula96@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Write your Subject Here");
                email.putExtra(Intent.EXTRA_TEXT, "Give a brief description of your problem here.. ");

                //need this to prompts email client only
                email.setType("message/rfc822");

                startActivity(Intent.createChooser(email, "Choose an Email client :"));
            }

        });
        clicked = false;
        return view;
    }

    private Runnable mFadeOut = new Runnable(){

        @Override
        public void run() {
            in.setDuration(2000);
            mailAddress.startAnimation(in);
        }
    };


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
