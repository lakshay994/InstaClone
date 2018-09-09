package com.example.lakshaysharma.instaclone.Home;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.Login.LoginActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.example.lakshaysharma.instaclone.Utils.MainfeedListAdapter;
import com.example.lakshaysharma.instaclone.Utils.MiddlePagerAdapter;
import com.example.lakshaysharma.instaclone.Utils.UniversalImageLoader;
import com.example.lakshaysharma.instaclone.Utils.ViewCommentsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

public class HomeActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener{

    @Override
    public void onLoadMoreItems() {

        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.view_pager_container + ":" + viewPager.getCurrentItem());

        if (fragment != null){
            fragment.displayRemainingPhotos();
        }
    }

    private static final String TAG = "HomeActivity";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FrameLayout frameLayout;
    private RelativeLayout relativeLayout;

    private final Context mContext = HomeActivity.this;
    private static final int ACTIVITY_INDEX = 0;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //initialize the views
        viewPager = findViewById(R.id.view_pager_container);
        tabLayout = findViewById(R.id.tabs);
        frameLayout = findViewById(R.id.container);
        relativeLayout = findViewById(R.id.relLayoutParent);

        initImageLoader();
        firebaseSetup();
        setupBottomNavigation();
        setupViewPager(viewPager);
    }

    // initiate the universal image loader
    public void initImageLoader(){

        UniversalImageLoader imageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(imageLoader.imageLoaderConfiguration());
    }

    /* Setup the fragments for the middle portion of the home */
    private void setupViewPager(ViewPager viewPager){

        Log.d(TAG, "setupViewPager: Setting Up the View Pager");

        MiddlePagerAdapter adapter = new MiddlePagerAdapter(getSupportFragmentManager());
        adapter.addFragments(new CameraFragment());
        adapter.addFragments(new HomeFragment());
        adapter.addFragments(new MessagesFragment());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        //set tab icons
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_iglogo);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_messages);
    }


    public void onCommentSelectedListener(Photo photo){

        ViewCommentsFragment fragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragments));
        transaction.commit();
    }


    public void hideLayout(){

        relativeLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout(){

        relativeLayout.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
    }

    /*set up the navigation animations*/
    private void setupBottomNavigation(){

        BottomNavigationViewEx navigation = findViewById(R.id.bottom_nav);

        Log.d(TAG, "setupBottomNavigation: Setting Up the Navigation Bar");
        BottomNavHelper.setupNav(navigation);

        Log.d(TAG, "setupBottomNavigation: Setting Up Nav Bar Function");
        BottomNavHelper.enableNavigation(mContext, this, navigation);

        //get the menu item position for highlighting
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_INDEX);
        menuItem.setChecked(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (frameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }


    /*
     * ---------------------------------FIREBASE----------------------------------------
     */

    public void firebaseSetup(){

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                checkCurrentUser(user);
                if (user != null){
                    Log.d(TAG, "onAuthStateChanged: Signed In: " + user.getUid());
                }
                else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out: ");
                }
            }
        };
    }

    public void checkCurrentUser(FirebaseUser user){
        if (user == null){
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewPager.setCurrentItem(1);
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
