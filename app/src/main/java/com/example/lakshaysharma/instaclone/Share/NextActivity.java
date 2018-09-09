package com.example.lakshaysharma.instaclone.Share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.Home.HomeActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.Alerts;
import com.example.lakshaysharma.instaclone.Utils.FirebaseUtils;
import com.example.lakshaysharma.instaclone.Utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabse;
    private DatabaseReference mRef;
    private FirebaseUtils firebaseUtils;
    private Alerts alerts;

    private static ImageView back, shareImage;
    private static TextView share;
    private static EditText description;

    private static Context mContext;
    private static String imageURL;
    private static String mAppend = "file:/";
    private static int imageCount = 0;
    private Intent intent;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        back = findViewById(R.id.nextClose);
        shareImage = findViewById(R.id.nextImage);
        share = findViewById(R.id.nextShare);
        description = findViewById(R.id.nextDescription);
        mContext = NextActivity.this;
        firebaseUtils = new FirebaseUtils(mContext);
        alerts = new Alerts(mContext);

        setImage();
        firebaseSetup();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alerts.makeToast("Uploading Image....");
                String desc = description.getText().toString().trim();

                if (intent.hasExtra(getString(R.string.selected_image))){

                    imageURL = getIntent().getStringExtra(getString(R.string.selected_image));
                    firebaseUtils.uploadImages(getString(R.string.regular_photo), desc, imageCount, imageURL, null);


                }
                else if (intent.hasExtra(getString(R.string.camera_bitmap))){

                    bitmap = intent.getParcelableExtra(getString(R.string.camera_bitmap));
                    firebaseUtils.uploadImages(getString(R.string.regular_photo), desc, imageCount, null, bitmap);

                }

            }
        });

    }

    private void setImage(){

        intent = getIntent();

        if (intent.hasExtra(getString(R.string.selected_image))){

            imageURL = getIntent().getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "onCreate: Image URL Received " + imageURL);
            UniversalImageLoader.setImage(imageURL, shareImage, null, mAppend);

        }
        else if (intent.hasExtra(getString(R.string.camera_bitmap))){

            bitmap = intent.getParcelableExtra(getString(R.string.camera_bitmap));
            Log.d(TAG, "setImage: got new camera image");
            shareImage.setImageBitmap(bitmap);

        }

    }




    /*
     * ---------------------------------FIREBASE----------------------------------------
     */

    public void firebaseSetup(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabse = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabse.getReference();
        Log.d(TAG, "onDataChange: Image Count: " + imageCount);


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

                imageCount = firebaseUtils.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: Image Count: " + imageCount);

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
