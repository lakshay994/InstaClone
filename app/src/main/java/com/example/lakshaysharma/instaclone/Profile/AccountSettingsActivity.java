package com.example.lakshaysharma.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.example.lakshaysharma.instaclone.Utils.FirebaseUtils;
import com.example.lakshaysharma.instaclone.Utils.SectionsPagerAdapter_ProfileSettings;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingsActivity";
    private Context mContext;
    private static final int ACTIVITY_INDEX = 4;

    private static ListView listView;
    public static SectionsPagerAdapter_ProfileSettings sectionAdapter;
    private static ViewPager viewPager;
    private static RelativeLayout relativeLayout;

    private FirebaseUtils firebaseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);
        mContext = AccountSettingsActivity.this;

        listView = findViewById(R.id.lvAccountSettings);
        ImageView backArrow = findViewById(R.id.backArrow);
        viewPager = findViewById(R.id.view_pager_container);
        relativeLayout = findViewById(R.id.rellayouttop_accounts);
        firebaseUtils = new FirebaseUtils(mContext);

        Log.d(TAG, "onCreate: Started");

        setUpFragments();
        setupBottomNavigation();
        getIncomingIntent();

        // setup the back arrow key
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: GOing back to the profile activity.");
                finish();
            }
        });

        // setup the settings list
        setUpSettingList();

    }

    public void setUpFragments(){

        sectionAdapter = new SectionsPagerAdapter_ProfileSettings(getSupportFragmentManager());
        sectionAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile));
        sectionAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out));
    }

    public void setViewPager(int fragmentNumber){

        relativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: Setting up ViewPager\n");
        viewPager.setAdapter(sectionAdapter);
        viewPager.setCurrentItem(fragmentNumber);
    }

    public void setUpSettingList(){

        Log.d(TAG, "setUpSettingList: Setting up the Settings list");

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile));
        options.add(getString(R.string.sign_out));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewPager(position);
            }
        });
    }

    private void getIncomingIntent(){
        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.selected_image))
                || intent.hasExtra(getString(R.string.camera_bitmap))){

            Log.d(TAG, "getIncomingIntent: new incoming image URL");
            if (intent.getStringExtra(getString(R.string.return_to_fragment)).equals("Edit Profile")){

                if (intent.hasExtra(getString(R.string.selected_image))){

                    String imageURL = intent.getStringExtra(getString(R.string.selected_image));
                    firebaseUtils.uploadImages(getString(R.string.profile_photo),
                            null, 0, imageURL, null);
                }
                else if (intent.hasExtra(getString(R.string.camera_bitmap))){

                    firebaseUtils.uploadImages(getString(R.string.profile_photo),
                            null, 0, null,
                            (Bitmap) intent.getParcelableExtra(getString(R.string.camera_bitmap)));
                }
            }

        }

        if (intent.hasExtra(getString(R.string.calling_class))){
            Log.d(TAG, "getIncomingIntent: Recieved incoming edit intent");

            setViewPager(sectionAdapter.getFragmentNumber(getString(R.string.edit_profile)));
        }

    }

    /*set up the navigation animations*/
    public void setupBottomNavigation(){

        BottomNavigationViewEx navigation = findViewById(R.id.bottom_nav);

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
