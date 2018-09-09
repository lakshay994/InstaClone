package com.example.lakshaysharma.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.DataModels.Comment;
import com.example.lakshaysharma.instaclone.DataModels.DBLikes;
import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.DataModels.UserSettings;
import com.example.lakshaysharma.instaclone.Home.HomeActivity;
import com.example.lakshaysharma.instaclone.Login.LoginActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.example.lakshaysharma.instaclone.Utils.FirebaseUtils;
import com.example.lakshaysharma.instaclone.Utils.ImageGridAdapter;
import com.example.lakshaysharma.instaclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final int ACTIVITY_INDEX = 4;
    private static final int NUM_GRID_COLUMS = 3;
    private static int followingCount;
    private static int followersCount;
    private static int postsCount;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabse;
    private DatabaseReference mRef;
    private FirebaseUtils firebaseUtils;

    private TextView mDisplayName, mUsername, mWebsite, mDescription,
                     mPosts, mFollowers, mFollowing, mEditProfile;
    private CircleImageView mProfilePhoto;
    private ProgressBar mProgressBar;
    private GridView mGridView;
    private Toolbar mToolbar;
    private ImageView mProfileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    private Context mContext;

    public interface OnGridImageSelectedListener{
        void onGridImageSelectedListener(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container,false);

        mDisplayName = view.findViewById(R.id.profile_name);
        mUsername = view.findViewById(R.id.profileUsername);
        mDescription = view.findViewById(R.id.profile_desc);
        mWebsite = view.findViewById(R.id.profile_website);
        mPosts = view.findViewById(R.id.tvPosts);
        mFollowers = view.findViewById(R.id.tcFollowers);
        mFollowing = view.findViewById(R.id.tcFollowing);
        mProfilePhoto = view.findViewById(R.id.profileImage);
        mProgressBar = view.findViewById(R.id.progressBar);
        mGridView = view.findViewById(R.id.gridView);
        mToolbar = view.findViewById(R.id.profileToolbar);
        bottomNavigationViewEx = view.findViewById(R.id.bottom_nav);
        mProfileMenu = view.findViewById(R.id.menuAccountSettings);
        mEditProfile = view.findViewById(R.id.tvEditProfile);

        mContext = getActivity();
        firebaseUtils = new FirebaseUtils(mContext);

        mProgressBar.setVisibility(View.GONE);

        firebaseSetup();
        setupBottomNavigation();
        setupToolbar();
        setupGridView();
        getFollowersCount();
        getFollowingCount();
        getPostsCount();
        editprofile();

        return view;
    }


    private void setupProfileWidgets(UserSettings userSettings){

        UserAccountSettings accountSettings = userSettings.getAccountSettings();

        UniversalImageLoader.setImage(accountSettings.getProfile_photo(),
                                        mProfilePhoto, mProgressBar, "");

        mDisplayName.setText(accountSettings.getDisplay_name());
        mUsername.setText(accountSettings.getUsername());
        mWebsite.setText(accountSettings.getWebsite());
        mDescription.setText(accountSettings.getDescription());

    }



    private void setupToolbar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(mToolbar);

        mProfileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, AccountSettingsActivity.class));
            }
        });
    }


    /*set up the navigation animations*/
    public void setupBottomNavigation(){

        Log.d(TAG, "setupBottomNavigation: Setting Up the Navigation Bar");
        BottomNavHelper.setupNav(bottomNavigationViewEx);

        Log.d(TAG, "setupBottomNavigation: Setting Up Nav Bar Highlight Function");
        BottomNavHelper.enableNavigation(mContext, getActivity(), bottomNavigationViewEx);

        //get the menu item position for highlighting
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_INDEX);
        menuItem.setChecked(true);
    }


    private void editprofile(){

        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Navigating to Edit Profile");
                Intent edit = new Intent(mContext, AccountSettingsActivity.class);
                edit.putExtra(getString(R.string.calling_class), getString(R.string.profile_activity));
                startActivity(edit);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }



    private void setupGridView(){

        final ArrayList<Photo> photos = new ArrayList<>();
        final ArrayList<String> imageURLs = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.user_photos))
                      .child(mAuth.getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()) {

                    Log.d(TAG, "onDataChange: found user_photos" + data.toString());

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = new HashMap<>();
                    objectMap = (HashMap<String, Object>) data.getValue();

                    photo.setTags(objectMap.get(getString(R.string.tags)).toString());
                    photo.setUser_id(objectMap.get(getString(R.string.user_id)).toString());
                    photo.setPhoto_id(objectMap.get(getString(R.string.photo_id)).toString());
                    photo.setImage_path(objectMap.get(getString(R.string.image_path)).toString());
                    photo.setDate_created(objectMap.get(getString(R.string.date_created)).toString());
                    photo.setCaption(objectMap.get(getString(R.string.caption)).toString());


                    List<Comment> commentList = new ArrayList<>();
                    for (DataSnapshot singleSnapshot: data.child(mContext.getString(R.string.comments)).getChildren()){
                        Log.d(TAG, "onDataChange: comment snapshot " + singleSnapshot);

                        Comment commentEntry = new Comment();
                        commentEntry.setComment(singleSnapshot.getValue(Comment.class).getComment());
                        commentEntry.setUser_id(singleSnapshot.getValue(Comment.class).getUser_id());
                        commentEntry.setDate_created(singleSnapshot.getValue(Comment.class).getDate_created());
                        commentList.add(commentEntry);
                    }

                    photo.setComments(commentList);

                    List<DBLikes> likes = new ArrayList<>();
                    for (DataSnapshot singleSnapshot: data.child(getString(R.string.likes)).getChildren()){

                        DBLikes like = new DBLikes();
                        like.setUser_id(singleSnapshot.getValue(DBLikes.class).getUser_id());
                        likes.add(like);

                    }

                    photo.setLikes(likes);
                    photos.add(photo);

                }
                for (int i=0; i<photos.size(); i++){
                    imageURLs.add(photos.get(i).getImage_path());
                }

                int gridWidth = getResources().getDisplayMetrics().widthPixels;
                int imageWidth = gridWidth/NUM_GRID_COLUMS;
                mGridView.setColumnWidth(imageWidth);

                ImageGridAdapter gridAdapter = new ImageGridAdapter(mContext, R.layout.layout_grid_imageview,"", imageURLs);
                mGridView.setAdapter(gridAdapter);

                mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridImageSelectedListener(photos.get(position), ACTIVITY_INDEX);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }



    private void getFollowersCount(){

        followersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: found followers ");
                    followersCount++;
                }
                mFollowers.setText("" + followersCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowingCount(){

        followingCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: found following ");
                    followingCount++;

                }
                mFollowing.setText("" + followingCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void getPostsCount(){

        postsCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: found posts ");
                    postsCount++;

                }
                mPosts.setText("" + postsCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {

        try {
            mOnGridImageSelectedListener = (OnGridImageSelectedListener) getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastExc" + e.getMessage());
        }

        super.onAttach(context);
    }


    /*
     * ---------------------------------FIREBASE----------------------------------------
     */

    public void firebaseSetup(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabse = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabse.getReference();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){
                    Log.d(TAG, "onAuthStateChanged: Signed In: " + user.getUid());
                }
                else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out: ");
                }
            }
        };


        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                setupProfileWidgets(firebaseUtils.getUserSettings(dataSnapshot));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);

        }
    }
}

