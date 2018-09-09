package com.example.lakshaysharma.instaclone.Likes;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class LikeActivity extends AppCompatActivity {

    private static final String TAG = "LikeActivity";

    private BottomNavigationViewEx navigation;

    private final Context mContext = LikeActivity.this;
    private static final int ACTIVITY_INDEX = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        navigation = findViewById(R.id.bottom_nav);

        setupBottomNavigation();
    }

    /*set up the navigation animations*/
    public void setupBottomNavigation(){

        Log.d(TAG, "setupBottomNavigation: Setting Up the Navigation Bar");
        BottomNavHelper.setupNav(navigation);

        Log.d(TAG, "setupBottomNavigation: Setting Up Nav Bar Highlight Function");
        BottomNavHelper.enableNavigation(mContext, this, navigation);

        //get the menu item position for highlighting
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_INDEX);
        menuItem.setChecked(true);
    }
}
