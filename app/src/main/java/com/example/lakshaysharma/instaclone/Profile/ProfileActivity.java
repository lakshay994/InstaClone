package com.example.lakshaysharma.instaclone.Profile;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ProgressBar;

import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.ViewCommentsFragment;
import com.example.lakshaysharma.instaclone.Utils.ViewPostFragment;
import com.example.lakshaysharma.instaclone.Utils.ViewProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.OnGridImageSelectedListener,
        ViewPostFragment.OnCommentThreadSelectedListener, ViewProfileFragment.OnGridImageSelectedListener {

    private static final String TAG = "ProfileActivity";

    private static Toolbar toolbar;
    private static ProgressBar mProgressBar;
    private static CircleImageView profilePhoto;

    private final Context mContext = ProfileActivity.this;
    private static final int ACTIVITY_INDEX = 4;
    private static final int WIDTH_COL = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initTransaction();

    }

    private void initTransaction(){
        Log.d(TAG, "initTransaction: trying to inflate profile");

        if (getIntent().hasExtra(getString(R.string.calling_class))){

            if (getIntent().hasExtra(getString(R.string.intent_user))){
                Log.d(TAG, "initTransaction: inflating searched activity");

                User user = getIntent().getParcelableExtra(getString(R.string.intent_user));
                if (!user.getUser_id().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                    ViewProfileFragment profileFragment = new ViewProfileFragment();
                    Bundle args = new Bundle();
                    args.putParcelable(getString(R.string.intent_user), getIntent().getParcelableExtra(getString(R.string.intent_user)));
                    profileFragment.setArguments(args);

                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.profileContainer, profileFragment);
                    transaction.addToBackStack(getString(R.string.view_profile_fragment));
                    transaction.commit();
                }
                else {

                    Log.d(TAG, "initTransaction: inflating users activity");
                    ProfileFragment fragment = new ProfileFragment();
                    FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.profileContainer, fragment);
                    transaction.addToBackStack(getString(R.string.profile_fragment));
                    transaction.commit();
                }
            }
        }
        else {

            Log.d(TAG, "initTransaction: inflating users activity");
            ProfileFragment fragment = new ProfileFragment();
            FragmentTransaction transaction = ProfileActivity.this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.profileContainer, fragment);
            transaction.addToBackStack(getString(R.string.profile_fragment));
            transaction.commit();

        }

    }

    @Override
    public void onGridImageSelectedListener(Photo photo, int activityNumber) {
        Log.d(TAG, "onGridImageSelectedListener: An Image Selected from the grid view" + photo.toString());

        ViewPostFragment viewPostFragment = new ViewPostFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.photo), photo);
        bundle.putInt(getString(R.string.activity_number), activityNumber);
        viewPostFragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.profileContainer, viewPostFragment);
        fragmentTransaction.addToBackStack(getString(R.string.view_post_fragmemnt));
        fragmentTransaction.commit();
    }

    @Override
    public void onCommentThreadSelectedListener(Photo photo) {

        ViewCommentsFragment commentsFragment = new ViewCommentsFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        commentsFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.profileContainer, commentsFragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragments));
        transaction.commit();
    }
}
