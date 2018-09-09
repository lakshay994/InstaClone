package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.DataModels.Comment;
import com.example.lakshaysharma.instaclone.DataModels.DBLikes;
import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.DataModels.UserSettings;
import com.example.lakshaysharma.instaclone.Home.HomeActivity;
import com.example.lakshaysharma.instaclone.Profile.ProfileActivity;
import com.example.lakshaysharma.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainfeedListAdapter extends ArrayAdapter<Photo> {

    public interface OnLoadMoreItemsListener{

        void onLoadMoreItems();
    }

    OnLoadMoreItemsListener mOnLoadMoreItemsListener;

    private static final String TAG = "MainfeedListAdapter";

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private int mLayoutResource;
    private DatabaseReference mRef;
    private String currentUser = "";

    public MainfeedListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Photo> objects) {
        super(context, resource, objects);

        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutResource = resource;
        mContext = context;
        mRef = FirebaseDatabase.getInstance().getReference();

    }

    static class ViewHolder{

        private static SquareImageView mMainImage;
        private  ImageView mPlainHeart, mRedHeart, mBackArrow, mCommentIcon;
        private CircleImageView mProfilePhoto;
        private TextView mDatePosted, mComment, mUsername, mLikedBy, mCaption;
        String likesString;
        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();
        Likes mLikes;
        Photo photo;
        StringBuilder users;
        boolean likedByUser;
        GestureDetector gestureDetector;

    }


    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null){

            convertView = mLayoutInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();

            holder.mMainImage = convertView.findViewById(R.id.photoMainImage);
            holder.mPlainHeart = convertView.findViewById(R.id.photoLikePlain);
            holder.mRedHeart = convertView.findViewById(R.id.photoLikeRed);
            holder.mCaption = convertView.findViewById(R.id.photoCaption);
            holder.mProfilePhoto = convertView.findViewById(R.id.photoProfilePhoto);
            holder.mDatePosted = convertView.findViewById(R.id.photoTimeDetails);
            holder.mUsername = convertView.findViewById(R.id.photoDisplayName);
            holder.mLikedBy = convertView.findViewById(R.id.photoLikeText);
            holder.mCommentIcon = convertView.findViewById(R.id.photoComment);
            holder.mComment = convertView.findViewById(R.id.photoViewComments);

            holder.mLikes = new Likes(holder.mRedHeart, holder.mPlainHeart);
            holder.photo = getItem(position);
            holder.gestureDetector = new GestureDetector(mContext, new GestureListener(holder));
            holder.users = new StringBuilder();

            convertView.setTag(holder);

        }
        else {

            holder = (ViewHolder) convertView.getTag();
        }

        getCurrentUSer();
        getLikesString(holder);
        List<Comment> comments = getItem(position).getComments();
        holder.mComment.setText("View " + comments.size() + " Comments");
        holder.mCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: opening comments thread");
                ((HomeActivity)mContext).onCommentSelectedListener(getItem(position));

                ((HomeActivity)mContext).hideLayout();
            }
        });


        String timeDiff = getTimeStampDifference(getItem(position));
        if (timeDiff == "0"){
            holder.mDatePosted.setText("Today");
        }
        else {
            holder.mDatePosted.setText(timeDiff + " days ago");
        }


        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(getItem(position).getImage_path(), holder.mMainImage);

        holder.mCaption.setText(getItem(position).getCaption());

        // set the profile image and username

        Query query = mRef.child(mContext.getString(R.string.user_account_settings))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    holder.mUsername.setText(data.getValue(UserAccountSettings.class).getUsername());
                    holder.mUsername.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Log.d(TAG, "onClick: navigating to user profile" +  holder.user.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_class), mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);

                        }
                    });

                    imageLoader.displayImage(data.getValue(UserAccountSettings.class).getProfile_photo(), holder.mProfilePhoto);
                    holder.mProfilePhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Log.d(TAG, "onClick: navigating to user profile" +  holder.user.getUsername());
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(mContext.getString(R.string.calling_class), mContext.getString(R.string.home_activity));
                            intent.putExtra(mContext.getString(R.string.intent_user), holder.user);
                            mContext.startActivity(intent);

                        }
                    });

                    holder.settings = data.getValue(UserAccountSettings.class);
                    holder.mComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity)mContext).onCommentSelectedListener(getItem(position));
                            ((HomeActivity)mContext).hideLayout();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query userQuery = mRef.child(mContext.getString(R.string.users))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(getItem(position).getUser_id());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){

                    holder.user = data.getValue(User.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (reachedEndOfList(position)){
            onLoadMoreData();
        }

        return convertView;
    }

    public boolean reachedEndOfList(int Position){
        return Position == getCount() -1;
    }

    public void onLoadMoreData(){

        try {

            mOnLoadMoreItemsListener = (OnLoadMoreItemsListener) getContext();

        }catch (ClassCastException e){
            Log.e(TAG, "onLoadMoreData: " + e.getMessage() );
        }

        try {

            mOnLoadMoreItemsListener.onLoadMoreItems();

        }catch (NullPointerException e){
            Log.e(TAG, "onLoadMoreData: " + e.getMessage() );
        }
    }


    public class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener{

        ViewHolder mHolder;
        public GestureListener(ViewHolder holder) {
            mHolder = holder;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            Query query = mRef.child(mContext.getString(R.string.photo))
                    .child(mHolder.photo.getPhoto_id()).child(mContext.getString(R.string.likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        if (mHolder.likedByUser && data.getValue(DBLikes.class).getUser_id()
                                .equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            String photoKey = data.getKey();
                            mRef.child(mContext.getString(R.string.photo))
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.likes))
                                    .child(photoKey).removeValue();

                            mRef.child(mContext.getString(R.string.user_photos))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(mHolder.photo.getPhoto_id())
                                    .child(mContext.getString(R.string.likes))
                                    .child(photoKey).removeValue();

                            mHolder.mLikes.toggleLikes();
                            getLikesString(mHolder);
                        }

                        else if (!mHolder.likedByUser){

                            addNewLike(mHolder);
                        }

                    }

                    if (!dataSnapshot.exists()){

                        addNewLike(mHolder);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            return true;
        }
    }


    private void addNewLike(ViewHolder holder){

        Log.d(TAG, "addNewLike: Adding New Like");

        String likeKey = mRef.push().getKey();

        DBLikes likes = new DBLikes();
        likes.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

        mRef.child(mContext.getString(R.string.photo))
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.likes))
                .child(likeKey).setValue(likes);

        mRef.child(mContext.getString(R.string.user_photos))
                .child(holder.photo.getUser_id())
                .child(holder.photo.getPhoto_id())
                .child(mContext.getString(R.string.likes))
                .child(likeKey).setValue(likes);

        holder.mLikes.toggleLikes();
        getLikesString(holder);

    }


    private void getLikesString(final ViewHolder holder){

        Log.d(TAG, "getLikesString: getting Likes String");

        try {

            Query query = mRef.child(mContext.getString(R.string.photo))
                    .child(holder.photo.getPhoto_id()).child(mContext.getString(R.string.likes));

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Log.d(TAG, "onDataChange: **##Likes Snapshot**##" + dataSnapshot);

                    holder.users = new StringBuilder();
                    if (dataSnapshot.exists()){

                        for (DataSnapshot data: dataSnapshot.getChildren()){

                            Log.d(TAG, "onDataChange: Likes Found");

                            Query query = mRef.child(mContext.getString(R.string.users))
                                    .orderByChild(mContext.getString(R.string.user_id))
                                    .equalTo(data.getValue(DBLikes.class).getUser_id());

                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot data: dataSnapshot.getChildren()){

                                        Log.d(TAG, "onDataChange: found likes:" + data.getValue(User.class).getUsername());

                                        holder.users.append(data.getValue(User.class).getUsername() + ",");

                                    }

                                    String[] splitUsers = holder.users.toString().split(",");

                                    if (holder.users.toString().contains(currentUser + ",")){
                                        holder.likedByUser = true;
                                    }else {
                                        holder.likedByUser = false;
                                    }


                                    if (splitUsers.length == 1){

                                        holder.likesString = "Liked By " + splitUsers[0];

                                    }
                                    else if (splitUsers.length == 2){

                                        holder.likesString = "Liked By " + splitUsers[0] + " and " + splitUsers[1];

                                    }
                                    else if (splitUsers.length == 3){

                                        holder.likesString = "Liked By " + splitUsers[0] + " " + splitUsers[1] +
                                                " and " + splitUsers[2];

                                    }
                                    else if (splitUsers.length >= 4){

                                        holder.likesString = "Liked By " + splitUsers[0] + " " + splitUsers[1] +
                                                " " + splitUsers[2] + " and " + (splitUsers.length - 3) + " others";

                                    }
                                    setupLikesString(holder, holder.likesString);

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                    }
                    else if (!dataSnapshot.exists()){
                        Log.d(TAG, "onDataChange: No Likes found");

                        holder.likesString = "";
                        holder.likedByUser = false;
                        setupLikesString(holder, holder.likesString);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }catch (NullPointerException e){
            Log.e(TAG, "getLikesString: Null Pointer" + e.getMessage() );
            holder.likesString = "";
            holder.likedByUser = false;

            setupLikesString(holder, holder.likesString);
        }

    }


    private void setupLikesString(final ViewHolder holder, String likeString){

        if (holder.likedByUser){
            holder.mPlainHeart.setVisibility(View.GONE);
            holder.mRedHeart.setVisibility(View.VISIBLE);
            holder.mRedHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.gestureDetector.onTouchEvent(event);
                }
            });
        }
        else {
            holder.mPlainHeart.setVisibility(View.VISIBLE);
            holder.mRedHeart.setVisibility(View.GONE);
            holder.mRedHeart.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return holder.gestureDetector.onTouchEvent(event);
                }
            });
        }

        holder.mLikedBy.setText(likeString);

    }

    private String getTimeStampDifference(Photo photo){

        Log.d(TAG, "getTimeStampDifference: calculating the timestamp");

        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp;
        String photoTimeStamp = photo.getDate_created();
        try {

            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60/ 24)));

        }catch (ParseException e){
            Log.e(TAG, "getTimeStampDifference: " + e.getMessage() );
            difference = "0";
        }

        return difference;

    }


    private void getCurrentUSer(){

        Query query = mRef.child(mContext.getString(R.string.users))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: getting current username: ");
                    currentUser = data.getValue(UserAccountSettings.class).getUsername();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
