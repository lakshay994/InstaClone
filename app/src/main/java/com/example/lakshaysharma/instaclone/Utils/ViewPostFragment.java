package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.DataModels.DBLikes;
import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.DataModels.UserSettings;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.BottomNavHelper;
import com.example.lakshaysharma.instaclone.Utils.FirebaseUtils;
import com.example.lakshaysharma.instaclone.Utils.SquareImageView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostFragment extends Fragment{

    public interface OnCommentThreadSelectedListener{
        public void onCommentThreadSelectedListener(Photo photo);
    }

    private OnCommentThreadSelectedListener mOnCommentThreadSelectedListener;

    private static final String TAG = "ViewPostFragment";

    private Context mContext;

    private Photo mPhoto;
    private int ACTIVITY_INDEX = 0;
    private boolean mLikedByUser;
    private int commentLength;

    private UserAccountSettings userAccountSettings;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabse;
    private DatabaseReference mRef;
    private FirebaseUtils firebaseUtils;
    private GestureDetector mGestureDetector;
    private Likes mLikes;
    private StringBuilder mUsersString;
    private String mLikesString;
    private User mCurrentUser;

    private static SquareImageView mMainImage;
    private  ImageView mPlainHeart, mRedHeart, mBackArrow, mCommentIcon;
    private CircleImageView mProfilePhoto;
    private TextView mDatePosted, mComment, mUsername, mLikedBy, mCaption;
    private  BottomNavigationViewEx bottomNavigationViewEx;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);

        mContext = getActivity();
        mMainImage = view.findViewById(R.id.photoMainImage);
        bottomNavigationViewEx = view.findViewById(R.id.bottom_nav);
        mPlainHeart = view.findViewById(R.id.photoLikePlain);
        mRedHeart = view.findViewById(R.id.photoLikeRed);
        mCaption = view.findViewById(R.id.photoCaption);
        mProfilePhoto = view.findViewById(R.id.photoProfilePhoto);
        mBackArrow = view.findViewById(R.id.photoClose);
        mDatePosted = view.findViewById(R.id.photoTimeDetails);
        mUsername = view.findViewById(R.id.photoDisplayName);
        mLikedBy = view.findViewById(R.id.photoLikeText);
        mCommentIcon = view.findViewById(R.id.photoComment);
        mComment = view.findViewById(R.id.photoViewComments);

        mGestureDetector = new GestureDetector(getActivity(), new GestureListener());
        mLikes = new Likes(mPlainHeart, mRedHeart);

        firebaseSetup();
        setupBottomNavigation();

        return view;
    }



    private void init(){

        try {

            mPhoto = getPhotoFromBundle();
            commentLength = mPhoto.getComments().size();
            ACTIVITY_INDEX = getActivityNumberFromBundle();
            UniversalImageLoader.setImage(mPhoto.getImage_path(), mMainImage, null, "");
            getPhotoDetails();
            getCurrentUser();

        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: " + e.getMessage());
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        if (isAdded()){
            init();
        }
    }

    private void setupWidgets(){

        String timeStampDiff = getTimeStampDifference();
        if (timeStampDiff != "0" && timeStampDiff != "1"){
            mDatePosted.setText(timeStampDiff + " DAYS AGO");
        }
        else if(timeStampDiff == "1"){
            mDatePosted.setText(timeStampDiff + " DAY AGO");
        }
        else {
            mDatePosted.setText("TODAY");
        }

        Log.d(TAG, "setupWidgets: Trying to setup details , Photo" + userAccountSettings.getProfile_photo() );
        Log.d(TAG, "setupWidgets: Username " + userAccountSettings.getDisplay_name());
        UniversalImageLoader.setImage(userAccountSettings.getProfile_photo(), mProfilePhoto, null, "");
        mUsername.setText(userAccountSettings.getDisplay_name());
        mLikedBy.setText(mLikesString);
        mCaption.setText(mPhoto.getCaption());

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        mCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the comments");

                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);

            }
        });

        if (commentLength == 1){
            mComment.setText("View 1 Comment");
        }
        else if (commentLength > 0){
            mComment.setText("View All " + commentLength + " Comments");
        }
        else if (commentLength == 0){
            mComment.setText("No comments yet");
        }


        mComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnCommentThreadSelectedListener.onCommentThreadSelectedListener(mPhoto);
            }
        });

        if (mLikedByUser){
            mRedHeart.setVisibility(View.VISIBLE);
            mPlainHeart.setVisibility(View.GONE);
            mRedHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });

        }
        else {

            mRedHeart.setVisibility(View.GONE);
            mPlainHeart.setVisibility(View.VISIBLE);
            mPlainHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });

        }

    }

    private Photo getPhotoFromBundle(){

        Bundle bundle = this.getArguments();
        if (bundle != null){
            return bundle.getParcelable(getString(R.string.photo));
        }
        else {
            return null;
        }

    }

    private int getActivityNumberFromBundle(){

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getInt(getString(R.string.activity_number));
        }
        else {
            return 0;
        }
    }

    private String getTimeStampDifference(){

        Log.d(TAG, "getTimeStampDifference: calculating the timestamp");

        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp;
        String photoTimeStamp = mPhoto.getDate_created();
        try {

            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60/ 24)));

        }catch (ParseException e){
            Log.e(TAG, "getTimeStampDifference: " + e.getMessage() );
            difference = "0";
        }

        return difference;

    }


    private void getPhotoDetails(){

        Log.d(TAG, "getPhotoDetails: getting photo details");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.user_account_settings))
                      .orderByChild(getString(R.string.user_id)).equalTo(mPhoto.getUser_id());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Query Data" + data.toString());
                    userAccountSettings = data.getValue(UserAccountSettings.class);
                }
                //setupWidgets();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
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


    private void getCurrentUser(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child(getString(R.string.users))
                .orderByChild(getString(R.string.user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Query Data" + data.toString());
                    mCurrentUser = data.getValue(User.class);
                }
                getLikesString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Query Cancelled");
            }
        });

    }


    private void getLikesString(){

        Log.d(TAG, "getLikesString: getting Likes String");

        Query query = mRef.child(getString(R.string.photo))
                .child(mPhoto.getPhoto_id()).child(getString(R.string.likes));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d(TAG, "onDataChange: **##Likes Snapshot**##" + dataSnapshot);

                mUsersString = new StringBuilder();
                if (dataSnapshot.exists()){

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        Log.d(TAG, "onDataChange: Likes Found");

                        Query query = mRef.child(getString(R.string.users))
                                .orderByChild(getString(R.string.user_id))
                                .equalTo(data.getValue(DBLikes.class).getUser_id());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot data: dataSnapshot.getChildren()){

                                    Log.d(TAG, "onDataChange: found likes:" + data.getValue(User.class).getUsername());

                                    mUsersString.append(data.getValue(User.class).getUsername() + ",");

                                }

                                String[] splitUsers = mUsersString.toString().split(",");

                                if (mUsersString.toString().contains(mCurrentUser.getUsername() + ",")){
                                    mLikedByUser = true;
                                }else {
                                    mLikedByUser = false;
                                }


                                if (splitUsers.length == 1){

                                    mLikesString = "Liked By " + splitUsers[0];

                                }
                                else if (splitUsers.length == 2){

                                    mLikesString = "Liked By " + splitUsers[0] + " and " + splitUsers[1];

                                }
                                else if (splitUsers.length == 3){

                                    mLikesString = "Liked By " + splitUsers[0] + " " + splitUsers[1] +
                                            " and " + splitUsers[2];

                                }
                                else if (splitUsers.length >= 4){

                                    mLikesString = "Liked By " + splitUsers[0] + " " + splitUsers[1] +
                                            " " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";

                                }
                                setupWidgets();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                }
                else if (!dataSnapshot.exists()){
                    Log.d(TAG, "onDataChange: No Likes found");

                    mLikesString = "";
                    mLikedByUser = false;
                    setupWidgets();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    public class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Query query = mRef.child(getString(R.string.photo))
                    .child(mPhoto.getPhoto_id()).child(getString(R.string.likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        if (mLikedByUser && data.getValue(DBLikes.class).getUser_id()
                                             .equals(mAuth.getCurrentUser().getUid())){
                            String photoKey = data.getKey();
                            mRef.child(getString(R.string.photo))
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.likes))
                                    .child(photoKey).removeValue();

                            mRef.child(getString(R.string.user_photos))
                                    .child(mAuth.getCurrentUser().getUid())
                                    .child(mPhoto.getPhoto_id())
                                    .child(getString(R.string.likes))
                                    .child(photoKey).removeValue();

                            mLikes.toggleLikes();
                            getLikesString();
                        }

                        else if (!mLikedByUser){

                            addNewLike();
                        }

                    }

                    if (!dataSnapshot.exists()){

                        addNewLike();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }



    private void addNewLike(){

        Log.d(TAG, "addNewLike: Adding New Like");

        String likeKey = mRef.push().getKey();

        DBLikes likes = new DBLikes();
        likes.setUser_id(mAuth.getCurrentUser().getUid());

        mRef.child(getString(R.string.photo))
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.likes))
                .child(likeKey).setValue(likes);

        mRef.child(getString(R.string.user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(getString(R.string.likes))
                .child(likeKey).setValue(likes);

        mLikes.toggleLikes();
        getLikesString();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            mOnCommentThreadSelectedListener = (OnCommentThreadSelectedListener) getActivity();

        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: Class Cast Exception" + e.getMessage() );
        }
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
