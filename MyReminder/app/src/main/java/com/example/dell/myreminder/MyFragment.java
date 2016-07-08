package com.example.dell.myreminder;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class MyFragment extends Fragment {

    private TextView myText;
    private ImageView myImage;
    int img_id;
    String text;
    public MyFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_my, container, false);
        Bundle bundle=this.getArguments();
        Toolbar mtoolbar=(Toolbar)getActivity().findViewById(R.id.toolbar);
        mtoolbar.setVisibility(View.VISIBLE);
        myText=(TextView)view.findViewById(R.id.myText);
        myImage=(ImageView)view.findViewById(R.id.myImage);
        if(bundle!=null)
        {
            img_id=bundle.getInt("img_id", R.drawable.icon_user_default);
            text=bundle.getString("text", "Home");
        }
        else
        {
            img_id=R.drawable.ic_contact;
            text="Home";
        }
        myText.setText(text);
        myImage.setImageResource(img_id);
        return view;
    }

}
