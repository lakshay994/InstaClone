package com.example.lakshaysharma.instaclone.Login;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.lakshaysharma.instaclone.Home.HomeActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.Alerts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Context mContext;
    private EditText mEmail, mPassword;
    private Button mLoginButton;
    private ProgressBar mProgressBar;
    private TextView mSignUp;
    private Alerts alerts;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = LoginActivity.this;
        mEmail = findViewById(R.id.loginEmail);
        mPassword = findViewById(R.id.loginPassword);
        mLoginButton = findViewById(R.id.loginButton);
        mProgressBar = findViewById(R.id.loginProgressBar);
        mSignUp = findViewById(R.id.loginCreateAccount);
        alerts = new Alerts(mContext);

        Log.d(TAG, "onCreate: On Create Started");

        mProgressBar.setVisibility(View.GONE);

        firebaseSetup();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Login Button Clicked");

                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    loginInit(email, password);
                }
                else {
                    alerts.setAlert("Email/Password cannot be empty");
                }
            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(mContext, RegisterActivity.class));
            }
        });

    }



    /*
     * ----------------------------FIREBASE--------------------------------------
     */



    private void firebaseSetup(){

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null){
                    Log.d(TAG, "onAuthStateChanged: Signed In: " + user.getUid());
                    Log.d(TAG, "onAuthStateChanged: Changing to Home Activity");
                }
                else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                }
            }
        };
    }

    private void loginInit(String email, String password){

        Log.d(TAG, "loginInit: Logging in with the provided credentials");
        mProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                FirebaseUser user = mAuth.getCurrentUser();

                if (task.isSuccessful()){
                    try {
                        if (user.isEmailVerified()){
                            Log.d(TAG, "onComplete: Login" + task.isSuccessful());
                            startActivity(new Intent(mContext, HomeActivity.class));
                        } else {
                            Log.d(TAG, "onComplete: Email not verified" );
                            mProgressBar.setVisibility(View.GONE);
                            alerts.setAlert("Please verify your email in order to successfully log in");
                        }

                    }catch (NullPointerException e){
                        Log.d(TAG, "onComplete: " + e.getMessage());
                    }
                }
                else {
                    Log.d(TAG, "onComplete: Login Failed " + task.getException());
                    mProgressBar.setVisibility(View.GONE);
                    alerts.makeToast("Login Failed");
                }
            }
        });
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
}
