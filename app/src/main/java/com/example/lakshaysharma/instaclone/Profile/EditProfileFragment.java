package com.example.lakshaysharma.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.DataModels.UserSettings;
import com.example.lakshaysharma.instaclone.Home.HomeActivity;
import com.example.lakshaysharma.instaclone.Login.LoginActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Share.ShareActivity;
import com.example.lakshaysharma.instaclone.Utils.Alerts;
import com.example.lakshaysharma.instaclone.Utils.ConfirmPasswordDailog;
import com.example.lakshaysharma.instaclone.Utils.FirebaseUtils;
import com.example.lakshaysharma.instaclone.Utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDailog.OnConfirmPasswordListener {

    private static final String TAG = "EditProfileFragment";

    private Context mContext;

    private FirebaseUtils firebaseUtils;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mRef;
    private String userID;
    private UserSettings mUserSettings;
    private Alerts alerts;

    private static CircleImageView profilePhoto;
    private static EditText mUsername, mDescription, mEmail, mPhone,
            mDisplayName, mWebsite;
    private static TextView mChangeProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fargment_edit_profile, container, false);
        Log.d(TAG, "onCreateView: Inside Edit PRofile Activity");

        profilePhoto = view.findViewById(R.id.profileImage);
        mUsername = view.findViewById(R.id.changeUsername);
        mDisplayName = view.findViewById(R.id.changeDisplayname);
        mWebsite = view.findViewById(R.id.changeWebsite);
        mDescription = view.findViewById(R.id.changeDescription);
        mEmail = view.findViewById(R.id.changeEmail);
        mPhone = view.findViewById(R.id.changePhoneNo);
        mChangeProfilePhoto = view.findViewById(R.id.changeProfilePhoto);
        mContext = getActivity();
        firebaseUtils = new FirebaseUtils(mContext);
        alerts = new Alerts(mContext);


        ImageView backArrow = view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ImageView saveChanges = view.findViewById(R.id.saveProfile);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSettings();
            }
        });

        firebaseSetup();

        return view;
    }


    private void setUpWidgets(UserSettings settings){

        mUserSettings = settings;

        UserAccountSettings accountSettings = settings.getAccountSettings();
        User user = settings.getUser();

        UniversalImageLoader.setImage(accountSettings.getProfile_photo(), profilePhoto,
                                        null, "");
        mUsername.setText(accountSettings.getUsername());
        mDisplayName.setText(accountSettings.getDisplay_name());
        mDescription.setText(accountSettings.getDescription());
        mWebsite.setText(accountSettings.getWebsite());
        mEmail.setText(user.getEmail());
        mPhone.setText(String.valueOf(user.getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changeDP = new Intent(getActivity(), ShareActivity.class);
                changeDP.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(changeDP);
                getActivity().finish();

            }
        });

    }


    private void saveAccountSettings(){

        String displayName = mDisplayName.getText().toString().trim();
        final String username = mUsername.getText().toString().trim();
        String description = mDescription.getText().toString().trim();
        String website = mWebsite.getText().toString().trim();
        final String email = mEmail.getText().toString().trim();
        long phone = Long.parseLong(mPhone.getText().toString());


        if (!TextUtils.isEmpty(username) && !mUserSettings.getAccountSettings().getUsername().equals(username)){

            checkIfUserNameExists(username);

        }

        if (!TextUtils.isEmpty(email) && !mUserSettings.getUser().getEmail().equals(email)){

            ConfirmPasswordDailog passwordDailog = new ConfirmPasswordDailog();
            passwordDailog.show(getFragmentManager(), getString(R.string.confirm_password));
            passwordDailog.setTargetFragment(EditProfileFragment.this, 1);

        }

        if (!mUserSettings.getAccountSettings().getDescription().equals(description)){

            firebaseUtils.updateUserAccountSetting(null, null, description, 0);

        }

        if (!TextUtils.isEmpty(displayName) && !mUserSettings.getUser().getEmail().equals(displayName)){

            firebaseUtils.updateUserAccountSetting(displayName, null, null, 0);

        }

        if (!TextUtils.isEmpty(website) && !mUserSettings.getUser().getEmail().equals(website)){

            firebaseUtils.updateUserAccountSetting(null, website, null, 0);

        }

        if (!(phone == 0) && !mUserSettings.getUser().getEmail().equals(phone)){

            firebaseUtils.updateUserAccountSetting(null, null, null, phone);

        }

        alerts.makeToast("Profile Updated");
        getActivity().finish();

    }


    private void checkIfUserNameExists(final String username){

        Query query = mRef.child(getString(R.string.users))
                      .orderByChild(getString(R.string.username))
                      .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){

                    firebaseUtils.updateUsername(username);
                    alerts.makeToast("Settings Saved");

                }
                else {

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        if (data.exists()){

                            Log.d(TAG, "onDataChange: Found A Match"
                                    + data.getValue(User.class).getUsername());
                            alerts.setAlert("Username already exists.\nPlease try a new username.");
                        }

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    public void onConfirmPasswordListener(String password) {

        Log.d(TAG, "onConfirmPasswordListener: Got Password " + password);
        final String email = mEmail.getText().toString().trim();

        Query query = mRef.child(getString(R.string.users))
                      .orderByChild(getString(R.string.user_email))
                      .equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){

                    firebaseUtils.updateUserEmail(email);
                    alerts.makeToast("Email Updated");


                }
                else {

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        if (data.exists()){

                            Log.d(TAG, "onDataChange: Found A Matching Email"
                                    + data.getValue(User.class).getEmail());
                            alerts.setAlert("Email already exists.\nPlease try a new email.");
                        }

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    /*
     * ---------------------------------FIREBASE----------------------------------------
     */

    public void firebaseSetup() {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged: Signed In: " + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out: ");
                }
            }
        };

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                setUpWidgets(firebaseUtils.getUserSettings(dataSnapshot));

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
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);

        }
    }

}
