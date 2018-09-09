package com.example.lakshaysharma.instaclone.Login;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.Alerts;
import com.example.lakshaysharma.instaclone.Utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private EditText mEmail, mPassword, mName;
    private Button mRegisterButton;
    private ProgressBar mProgressBar;
    private static Alerts alerts;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUtils mFirebaseUtils;
    private DatabaseReference mRef;
    private FirebaseDatabase mFirebaseDatabase;

    private String email;
    private String password;
    private String username;
    private String append = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mContext = RegisterActivity.this;
        mEmail = findViewById(R.id.registerEmail);
        mPassword = findViewById(R.id.registerPassword);
        mName = findViewById(R.id.registerName);
        mRegisterButton = findViewById(R.id.registerButton);
        mProgressBar = findViewById(R.id.registerProgressBar);
        mFirebaseUtils = new FirebaseUtils(mContext);
        alerts = new Alerts(mContext);

        mProgressBar.setVisibility(View.GONE);

        firebaseSetup();

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Checking If the inputs are empty");

                email = mEmail.getText().toString().trim();
                password = mPassword.getText().toString().trim();
                username = mName.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    mFirebaseUtils.registerNewUser(email, password, username);
                }
                else {
                    alerts.setAlert("All fields must be filled");
                }
            }
        });
    }



    /*
     * ----------------------------FIREBASE--------------------------------------
     */



    private void firebaseSetup(){

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){
                    Log.d(TAG, "onAuthStateChanged: Signed In: " + user.getUid());

                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            checkIfUserNameExists(username);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    finish();
                }
                else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mAuth.getCurrentUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void checkIfUserNameExists(final String username){

        Log.d(TAG, "checkIfUserNameExists: checking if username exists");
        final FirebaseUser user = mAuth.getCurrentUser();

        Query query = mRef.child(getString(R.string.users))
                .orderByChild(getString(R.string.username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){

                    // add user and account settings;
                    mFirebaseUtils.addUser(user, email, username, "", "", "");
                    Log.d(TAG, "onDataChange: user added");
                    alerts.makeToast("User Added to the Database");

                    mAuth.signOut();
                }
                else {

                    for (DataSnapshot data: dataSnapshot.getChildren()){

                        if (data.exists()){

                            Log.d(TAG, "onDataChange: Found A Match"
                                    + data.getValue(User.class).getUsername());
                            append = mRef.push().getKey().substring(3, 10);
                            Log.d(TAG, "onDataChange: Appending String to username" + append);
                        }

                    }

                    String userName;
                    userName = username + append;

                    // add user and account settings;
                    mFirebaseUtils.addUser(user, email, userName, "", "", "");
                    Log.d(TAG, "onDataChange: user added");
                    alerts.makeToast("User Added to the Database");

                    mAuth.signOut();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
