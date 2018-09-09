package com.example.lakshaysharma.instaclone.Utils;

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
import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.DataModels.UserSettings;
import com.example.lakshaysharma.instaclone.Profile.AccountSettingsActivity;
import com.example.lakshaysharma.instaclone.Profile.ProfileActivity;
import com.example.lakshaysharma.instaclone.R;
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

public class ViewProfileFragment extends Fragment {

    private static final String TAG = "ViewProfileFragment";
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
                     mPosts, mFollowers, mFollowing, mFollow, mUnfollow, mEditProfile;
    private CircleImageView mProfilePhoto;
    private ProgressBar mProgressBar;
    private GridView mGridView;
    private Toolbar mToolbar;
    private ImageView mProfileMenu;
    private BottomNavigationViewEx bottomNavigationViewEx;

    private Context mContext;
    private User mUser;

    public interface OnGridImageSelectedListener{
        void onGridImageSelectedListener(Photo photo, int activityNumber);
    }

    OnGridImageSelectedListener mOnGridImageSelectedListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container,false);

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
        mFollow = view.findViewById(R.id.profileFollow);
        mUnfollow = view.findViewById(R.id.profileUnfollow);
        mEditProfile = view.findViewById(R.id.profileEdit);
        mContext = getActivity();
        firebaseUtils = new FirebaseUtils(mContext);

        mProgressBar.setVisibility(View.GONE);

        try {

            mUser = getUserFromBundle();
            init();
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: " + e.getMessage() );
            getActivity().getSupportFragmentManager().popBackStack();
        }

        firebaseSetup();
        setupBottomNavigation();
        setupToolbar();
        isFollowing();
        getFollowersCount();
        getFollowingCount();
        getPostsCount();
        //editprofile();

        mFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: following the user");

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .child(getString(R.string.user_id))
                        .setValue(mUser.getUser_id());

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(getString(R.string.user_id))
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());

                setFollowing();
            }
        });

        mUnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: unfollowing the user");

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.following))
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(mUser.getUser_id())
                        .removeValue();

                FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.followers))
                        .child(mUser.getUser_id())
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .removeValue();

                setUnfollowing();
            }
        });

        return view;
    }


    private void init(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.user_account_settings))
                .orderByChild(getString(R.string.user_id))
                .equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: found a user: " + data.getValue(UserAccountSettings.class));
                    UserSettings settings = new UserSettings();
                    settings.setUser(mUser);
                    settings.setAccountSettings(data.getValue(UserAccountSettings.class));
                    setupProfileWidgets(settings);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Query query1 = reference.child(getString(R.string.user_photos))
                .child(mUser.getUser_id());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<Photo> photos = new ArrayList<>();

                for (DataSnapshot data: dataSnapshot.getChildren()) {

                    Photo photo = new Photo();
                    Map<String, Object> objectMap = (HashMap<String, Object>) data.getValue();

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
                setupImageGrid(photos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });
    }


    private void setupImageGrid(final ArrayList<Photo> photos){

        ArrayList<String> imageURLs = new ArrayList<>();
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


    private User getUserFromBundle(){

        Bundle bundle = this.getArguments();
        if (bundle != null){
            return bundle.getParcelable(getString(R.string.intent_user));
        }
        else {
            return null;
        }

    }


    private void setupProfileWidgets(UserSettings userSettings){

        UserAccountSettings accountSettings = userSettings.getAccountSettings();

        UniversalImageLoader.setImage(accountSettings.getProfile_photo(),
                                        mProfilePhoto, mProgressBar, "");

        mDisplayName.setText(accountSettings.getDisplay_name());
        mUsername.setText(accountSettings.getUsername());
        mWebsite.setText(accountSettings.getWebsite());
        mDescription.setText(accountSettings.getDescription());
        mPosts.setText(String.valueOf(accountSettings.getPosts()));
        mFollowing.setText(String.valueOf(accountSettings.getFollowing()));
        mFollowers.setText(String.valueOf(accountSettings.getFollowers()));

    }

    private void setFollowing(){

        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.VISIBLE);
        mEditProfile.setVisibility(View.GONE);

    }

    private void setUnfollowing(){

        mFollow.setVisibility(View.VISIBLE);
        mUnfollow.setVisibility(View.GONE);
        mEditProfile.setVisibility(View.GONE);

    }

    private void setEditProfile(){

        mFollow.setVisibility(View.GONE);
        mUnfollow.setVisibility(View.GONE);
        mEditProfile.setVisibility(View.VISIBLE);

    }


    private void isFollowing(){

        setUnfollowing();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(getString(R.string.user_id))
                .equalTo(mUser.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: found a user: " + data.getValue());
                    setFollowing();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void getFollowersCount(){

        followersCount = 0;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.followers))
                .child(mUser.getUser_id());

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
                .child(mUser.getUser_id());

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
                .child(mUser.getUser_id());

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


    /*private void editprofile(){

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
    }*/



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

