package com.example.lakshaysharma.instaclone.Home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.lakshaysharma.instaclone.DataModels.Comment;
import com.example.lakshaysharma.instaclone.DataModels.DBLikes;
import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.MainfeedListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mMainfeedListAdapter;
    private int mResults;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = view.findViewById(R.id.homeListView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();
        return view;
    }

    private void getFollowing(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query userQuery = reference.child(getString(R.string.following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){

                    mFollowing.add(data.child(getString(R.string.user_id)).getValue().toString());
                }
                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                getPhotos();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getPhotos(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (int i=0; i<mFollowing.size(); i++){

            final int count = i;
            Query userQuery = reference.child(getString(R.string.user_photos))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.user_id))
                    .equalTo(mFollowing.get(i));
            userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) data.getValue();

                        photo.setTags(objectMap.get(getString(R.string.tags)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.user_id)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.photo_id)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.image_path)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.date_created)).toString());
                        photo.setCaption(objectMap.get(getString(R.string.caption)).toString());

                        List<Comment> commentList = new ArrayList<>();
                        for (DataSnapshot singleSnapshot: data.child(getString(R.string.comments)).getChildren()){
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

                        mPhotos.add(photo);
                    }

                    if (count >= mFollowing.size() - 1){
                        setmPhotos();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }


    private void setmPhotos(){

        mPaginatedPhotos = new ArrayList<>();

        if (mPhotos != null){
            try {

                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mPhotos.size();

                if (iterations > 10){
                    iterations = 10;
                }

                mResults = 10;
                for (int i = 0; i<iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mMainfeedListAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPhotos);
                mListView.setAdapter(mMainfeedListAdapter);

            }catch (NullPointerException exc){
                Log.e(TAG, "setmPhotos: " + exc.getMessage() );
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "setmPhotos: " + e.getMessage() );
            }
        }
    }


    public void displayRemainingPhotos(){

        try {

            if (mPhotos.size() > mResults && mPhotos.size() > 0){

                int iteration;
                if (mPhotos.size() > (mResults + 1)){

                    iteration = 10;
                }else {

                    iteration = mPhotos.size() - mResults;
                }

                for (int i = mResults; i < mResults + iteration; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mMainfeedListAdapter.notifyDataSetChanged();
            }

        }catch (NullPointerException e){
            Log.e(TAG, "displayRemainingPhotos: " + e );
        }catch (IndexOutOfBoundsException exc){
            Log.e(TAG, "displayRemainingPhotos: " + exc );
        }
    }
}
