package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.lakshaysharma.instaclone.DataModels.Comment;
import com.example.lakshaysharma.instaclone.DataModels.DBLikes;
import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ViewCommentsFragment extends Fragment {

    private static final String TAG = "ViewCommentsFragment";
    private Context mContext;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabse;
    private DatabaseReference mRef;
    Alerts alerts;

    private Photo mPhoto;
    private ArrayList<Comment> mCommentsList;

    private ImageView mBackArrow, mCheckMark;
    private EditText mComment;
    private ListView mListview;

    public ViewCommentsFragment(){
        super();
        setArguments(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_comments, container, false);

        mContext = getActivity();
        mBackArrow = view.findViewById(R.id.commentsBackArrow);
        mCheckMark = view.findViewById(R.id.commentPostComment);
        mComment = view.findViewById(R.id.commentAddComment);
        mListview = view.findViewById(R.id.commentsListView);
        mCommentsList = new ArrayList<>();
        alerts = new Alerts(getActivity());

        try {
            mPhoto = getPhotoFromBundle();
            Log.d(TAG, "onCreateView: got photo from bundle " + mPhoto.toString());


            firebaseSetup();
            setupFirstComment();
            setupWidgets();

        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: " + e.getMessage());
        }

        return view;
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

    private void setupFirstComment(){

        Comment firstComment = new Comment();
        firstComment.setComment(mPhoto.getCaption());
        firstComment.setDate_created(mPhoto.getDate_created());
        firstComment.setUser_id(mPhoto.getUser_id());

        mCommentsList.add(firstComment);

    }


    private void setupWidgets(){

        Log.d(TAG, "setupWidgets: setting up widgets");

        mCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Trying to add a new comment");

                if (!mComment.getText().toString().trim().equals("")){

                    addNewComment(mComment.getText().toString().trim());
                    mComment.setText("");
                    hideKeyboard();

                }
                else {
                    alerts.makeToast("Comment Cannot be Empty");
                }
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().getSupportFragmentManager().popBackStack();

            }
        });

        setupCommentList();

    }

    private void setupCommentList(){

        CommentListAdapter adapter = new CommentListAdapter(mContext, R.layout.layout_comments,
                mCommentsList);
        mListview.setAdapter(adapter);

    }


    public void addNewComment(String newComment){

        Log.d(TAG, "addNewComment: Adding a new Comment");

        Comment comment = new Comment();
        String commentID = mRef.push().getKey();

        comment.setComment(newComment);
        comment.setUser_id(mAuth.getCurrentUser().getUid());
        comment.setDate_created(getTimeStamp());

        mRef.child(mContext.getString(R.string.photo))
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.comments))
                .child(commentID)
                .setValue(comment);

        mRef.child(mContext.getString(R.string.user_photos))
                .child(mPhoto.getUser_id())
                .child(mPhoto.getPhoto_id())
                .child(mContext.getString(R.string.comments))
                .child(commentID)
                .setValue(comment);
    }



    private String getTimeStamp(){

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
        return sdf.format(new Date());

    }

    private void hideKeyboard(){

        View view = getActivity().getCurrentFocus();

        if (view != null){

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }




    /*
     * ---------------------------------FIREBASE----------------------------------------
     */

    public void firebaseSetup(){

        Log.d(TAG, "firebaseSetup: setting up firebase auth");

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

        mRef.child(mContext.getString(R.string.photo))
            .child(mPhoto.getPhoto_id())
            .child(mContext.getString(R.string.comments))
            .addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Query query = mRef.child(mContext.getString(R.string.photo))
                            .orderByChild(mContext.getString(R.string.photo_id))
                            .equalTo(mPhoto.getPhoto_id());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {

                            Log.d(TAG, "onDataChange: comments found");

                            for (DataSnapshot data: dataSnapshot1.getChildren()) {

                                Photo photo = new Photo();
                                Map<String, Object> objectMap = (HashMap<String, Object>) data.getValue();

                                photo.setTags(objectMap.get(mContext.getString(R.string.tags)).toString());
                                photo.setUser_id(objectMap.get(mContext.getString(R.string.user_id)).toString());
                                photo.setPhoto_id(objectMap.get(mContext.getString(R.string.photo_id)).toString());
                                photo.setImage_path(objectMap.get(mContext.getString(R.string.image_path)).toString());
                                photo.setDate_created(objectMap.get(mContext.getString(R.string.date_created)).toString());
                                photo.setCaption(objectMap.get(mContext.getString(R.string.caption)).toString());

                                mCommentsList.clear();
                                setupFirstComment();

                                for (DataSnapshot singleSnapshot: data.child(mContext.getString(R.string.comments)).getChildren()){
                                    Log.d(TAG, "onDataChange: comment snapshot " + singleSnapshot);

                                    Comment commentEntry = new Comment();
                                    commentEntry.setComment(singleSnapshot.getValue(Comment.class).getComment());
                                    commentEntry.setUser_id(singleSnapshot.getValue(Comment.class).getUser_id());
                                    commentEntry.setDate_created(singleSnapshot.getValue(Comment.class).getDate_created());
                                    mCommentsList.add(commentEntry);
                                }

                                photo.setComments(mCommentsList);
                                mPhoto = photo;
                                setupCommentList();

                                /*List<DBLikes> likes = new ArrayList<>();
                                for (DataSnapshot singleSnapshot: data.child(getString(R.string.likes)).getChildren()){

                                    DBLikes like = new DBLikes();
                                    like.setUser_id(singleSnapshot.getValue(DBLikes.class).getUser_id());
                                    likes.add(like);
                                }*/
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled: Query Cancelled");
                        }
                    });
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
