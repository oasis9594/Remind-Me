package com.example.dell.myreminder;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class ContactUs extends Fragment {


    public ContactUs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        Toolbar toolbar=(Toolbar)view.findViewById(R.id.contactActivityToolbar);
        toolbar.setTitle("My Reminders");
        //Hide the toolbar of main activity
        Toolbar mtoolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        mtoolbar.setVisibility(View.GONE);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_nav_icon);
        setHasOptionsMenu(true);
        toolbar.showOverflowMenu();
        return view;
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
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("Activity Key", 4);
        super.onSaveInstanceState(outState);
    }
}
