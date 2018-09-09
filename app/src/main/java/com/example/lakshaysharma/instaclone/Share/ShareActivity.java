package com.example.lakshaysharma.instaclone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.example.lakshaysharma.instaclone.Utils.MiddlePagerAdapter;
import com.example.lakshaysharma.instaclone.Utils.Permissions;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class ShareActivity extends AppCompatActivity {

    private static final String TAG = "ShareActivity";

    public final Context mContext = ShareActivity.this;
    private static final int ACTIVITY_INDEX = 2;
    private static final int PERMISSION_REQ = 2;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        if (checkPermissionsArray(Permissions.PERMISSIONS)){

            setupViewPager();

        } else {
            verifyPermissions(Permissions.PERMISSIONS);
            setupViewPager();
        }

        //setupBottomNavigation();
    }

    public boolean checkPermissionsArray(String[] persmissions){

        Log.d(TAG, "checkPermissionsArray: Checking Permissions Array");

        for (int i = 0; i< persmissions.length; i++){

            String check = persmissions[i];
            if (!checkSinglePermission(check)){
                return false;
            }

        }
        return true;

    }

    public boolean checkSinglePermission(String permission){

        Log.d(TAG, "checkSinglePermission: Checking Single Permission " + permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkSinglePermission: Permission wasn't granted for" + permission);
            return false;
        }

        return true;
    }

    public void verifyPermissions(String[] permissions){

        Log.d(TAG, "verifyPermissions: Asking for Permissions " + permissions);

        ActivityCompat.requestPermissions(ShareActivity.this, permissions, PERMISSION_REQ);

    }

    private void setupViewPager(){

        Log.d(TAG, "setupViewPager: Setting Up View Pager");

        MiddlePagerAdapter pagerAdapter = new MiddlePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragments(new GalleryFragment());
        pagerAdapter.addFragments(new PhotoFragment());

        mViewPager = findViewById(R.id.view_pager_container);
        mViewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = findViewById(R.id.shareBottomtabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));

    }

    public int getTabNumber(){

        return mViewPager.getCurrentItem();

    }

    public int getTask(){

        return getIntent().getFlags();

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
