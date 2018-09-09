package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.lakshaysharma.instaclone.DataModels.Photo;
import com.example.lakshaysharma.instaclone.DataModels.User;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.DataModels.UserSettings;
import com.example.lakshaysharma.instaclone.Profile.AccountSettingsActivity;
import com.example.lakshaysharma.instaclone.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class FirebaseUtils {

    private static final String TAG = "FirebaseUtils";

    private Context mContext;
    private double uploadProgress = 0;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;
    private StorageReference mStorageRef;
    private FirebaseUser user;
    private Alerts alerts;
    private String userId;

    public FirebaseUtils(Context context) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mContext = context;
        alerts = new Alerts(context);

    }



    /*
     *  register new user to the database
     */
    public void registerNewUser(String email, String password, String username){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Log.d(TAG, "onComplete: Registering New User");

                if ((task.isSuccessful())){
                    Log.d(TAG, "onComplete: New User Registered");
                    Toast.makeText(mContext, "New User Registered", Toast.LENGTH_LONG).show();
                    verifyUser();
                    Log.d(TAG, "onComplete: Verification email sent");
                }
                else {
                    Toast.makeText(mContext, "Registration failed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    /*
     *  add initial user data to populate the user's database
     */
    public void addUser(FirebaseUser dbUser, String email, String username, String description, String website, String profilePhoto){

        Log.d(TAG, "addUser: Trying to add user to the database");
        User user = new User(dbUser.getUid(), 1, email, StringManipulation.condenseUsername(username));
        UserAccountSettings accountSettings = new UserAccountSettings(
                description, username, 0, 0, 0, profilePhoto,
                StringManipulation.condenseUsername(username), website, dbUser.getUid());

        mRef.child("users").child(dbUser.getUid()).setValue(user);
        mRef.child("user_account_settings").child(dbUser.getUid()).setValue(accountSettings);

    }


    /*
     *  send verification email to the user's email address
     */
    public void verifyUser(){

        user = mAuth.getCurrentUser();

        if (user != null){
            Log.d(TAG, "verifyUser: Trying to send verfification email");
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        //alerts.setAlert("Verification Email has been sent. \nPlease verify your " + "email in order to sign in.");
                    }
                    else {
                        Log.d(TAG, "onComplete: endEmailVerification() not complete");
                    }
                }
            });
        }
    }

    public UserSettings getUserSettings(DataSnapshot dataSnapshot){

        userId = mAuth.getCurrentUser().getUid();

        Log.d(TAG, "getUserSettings: Retrieving User Settings");

        User user = new User();
        UserAccountSettings accountSettings = new UserAccountSettings();

        for (DataSnapshot data: dataSnapshot.getChildren()){

            Log.d(TAG, "getUserSettings: " + data);

            if (data.getKey().equals(mContext.getString(R.string.user_account_settings))){

                try {
                    accountSettings.setUsername(data.child(userId)
                            .getValue(UserAccountSettings.class).getUsername());

                    accountSettings.setDisplay_name(data.child(userId)
                            .getValue(UserAccountSettings.class).getDisplay_name());

                    accountSettings.setDescription(data.child(userId)
                            .getValue(UserAccountSettings.class).getDescription());

                    accountSettings.setFollowers(data.child(userId)
                            .getValue(UserAccountSettings.class).getFollowers());

                    accountSettings.setFollowing(data.child(userId)
                            .getValue(UserAccountSettings.class).getFollowing());

                    accountSettings.setPosts(data.child(userId)
                            .getValue(UserAccountSettings.class).getPosts());

                    accountSettings.setProfile_photo(data.child(userId)
                            .getValue(UserAccountSettings.class).getProfile_photo());

                    accountSettings.setWebsite(data.child(userId)
                            .getValue(UserAccountSettings.class).getWebsite());


                    Log.d(TAG, "getUserSettings: Retrieved User Account Settings" + accountSettings.toString());

                } catch (NullPointerException e){
                    Log.e(TAG, "getUserSettings: Account Settings", e);
                }
            }

            if (data.getKey().equals(mContext.getString(R.string.users))){

                try {

                    user.setUsername(data.child(userId).getValue(User.class).getUsername());

                    user.setEmail(data.child(userId).getValue(User.class).getEmail());

                    user.setPhone_number(data.child(userId).getValue(User.class).getPhone_number());

                    user.setUser_id(data.child(userId).getValue(User.class).getUser_id());

                    Log.d(TAG, "getUserSettings: Retrieved User " + user.toString());

                }catch (NullPointerException e){

                    Log.e(TAG, "getUserSettings: Users", e);
                }
            }
        }

        return new UserSettings(user, accountSettings);
    }

    public void updateUsername(String username){

        userId = mAuth.getCurrentUser().getUid();

        mRef.child(mContext.getString(R.string.users))
                .child(userId)
                .child(mContext.getString(R.string.username))
                .setValue(username);

        mRef.child(mContext.getString(R.string.user_account_settings))
                .child(userId)
                .child(mContext.getString(R.string.username))
                .setValue(username);

    }

    public void updateUserEmail(String email){

        Log.d(TAG, "updateUserEmail: Updating User Email");

        userId = mAuth.getCurrentUser().getUid();

        mRef.child(mContext.getString(R.string.users))
                .child(userId)
                .child(mContext.getString(R.string.user_email))
                .setValue(email);

        mAuth.getCurrentUser().updateEmail(email);
        verifyUser();
    }

    public void updateUserAccountSetting(String display_name, String website,
                                         String description, long phone){

        if (display_name != null){

            mRef.child(mContext.getString(R.string.user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.display_name))
                    .setValue(display_name);

        }

        if (description != null){

            mRef.child(mContext.getString(R.string.user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.description))
                    .setValue(description);

        }

        if (website != null){

            mRef.child(mContext.getString(R.string.user_account_settings))
                    .child(userId)
                    .child(mContext.getString(R.string.website))
                    .setValue(website);

        }

        if (phone != 0){

            mRef.child(mContext.getString(R.string.users))
                    .child(userId)
                    .child(mContext.getString(R.string.phone_number))
                    .setValue(phone);

        }

    }

    public int getImageCount(DataSnapshot dataSnapshot){

        int count = 0;

        for (DataSnapshot data: dataSnapshot.child(mContext.getString(R.string.user_photos))
                                .child(mAuth.getCurrentUser().getUid()).getChildren()){
            count++;
        }

        return count;
    }


    public void uploadImages(String photoType, final String description, int imageCount, String imageURL, Bitmap bitmap){

        Log.d(TAG, "uploadImages: Trying to upload new image");
        FilePaths filePaths = new FilePaths();
        ImageManager imageManager = new ImageManager();
        String userId = mAuth.getCurrentUser().getUid();

        if (photoType.equals(mContext.getString(R.string.regular_photo))){

            Log.d(TAG, "uploadImages: Uploading Regular Photo");

            final StorageReference storageReference = mStorageRef.child(filePaths.FIREBASE_STORAGE_PATH
                                                + userId + "/photo" + (imageCount + 1));

            if (bitmap == null){
                bitmap = imageManager.getImageBitmap(imageURL);
            }

            byte[] imageBytes = imageManager.getBytesFromBitmap(bitmap, 100);

            UploadTask uploadTask = storageReference.putBytes(imageBytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    alerts.makeToast("Upload Successful");
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadURL = uri;
                            Log.d(TAG, "onSuccess: Download URL: " + downloadURL);
                            addPhotoToDatabase(description, downloadURL.toString());
                        }
                    });



                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: Upload Task Failed");
                    alerts.makeToast("Upload Failed");

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) /
                                             taskSnapshot.getTotalByteCount();

                    if (progress - 15 > uploadProgress){

                        alerts.makeShortToast("Upload Progress: " + String.format("%.0f", progress) + "%");
                        uploadProgress = progress;
                    }

                }
            });
        }
        else if (photoType.equals(mContext.getString(R.string.profile_photo))){

            Log.d(TAG, "uploadImages: Uploading Profile Photo");

            final StorageReference storageReference = mStorageRef.child(filePaths.FIREBASE_STORAGE_PATH
                    + userId + "/profile_photo");

            if (bitmap == null){
                bitmap = imageManager.getImageBitmap(imageURL);
            }

            byte[] imageBytes = imageManager.getBytesFromBitmap(bitmap, 100);

            UploadTask uploadTask = storageReference.putBytes(imageBytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    alerts.makeToast("Upload Successful");
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadURL = uri;
                            Log.d(TAG, "onSuccess: Download URL: " + downloadURL);
                            addProfilePhotoToDatabase(downloadURL.toString());
                        }
                    });

                    ((AccountSettingsActivity)mContext).setViewPager(0);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d(TAG, "onFailure: Upload Task Failed");
                    alerts.makeToast("Upload Failed");

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double progress = (100 * taskSnapshot.getBytesTransferred()) /
                            taskSnapshot.getTotalByteCount();

                    if (progress - 15 > uploadProgress){

                        alerts.makeShortToast("Upload Progress: " + String.format("%.0f", progress) + "%");
                        uploadProgress = progress;
                    }

                }
            });

        }
    }

    private void addPhotoToDatabase(String description, String downloadURL){

        Log.d(TAG, "addPhotoToDatabase: Adding Photo to the Database");
        Photo photo = new Photo();

        String photoID = mRef.child(mContext.getString(R.string.photo)).push().getKey();

        String tags = StringManipulation.getTags(description);

        photo.setCaption(description);
        photo.setDate_created(getTimeStamp());
        photo.setImage_path(downloadURL);
        photo.setPhoto_id(photoID);
        photo.setUser_id(mAuth.getCurrentUser().getUid());
        photo.setTags(tags);

        mRef.child(mContext.getString(R.string.user_photos))
                .child(mAuth.getCurrentUser().getUid()).child(photoID).setValue(photo);

        mRef.child(mContext.getString(R.string.photo)).child(photoID).setValue(photo);

    }

    private String getTimeStamp(){

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
        return sdf.format(new Date());

    }

    private void addProfilePhotoToDatabase(String downloadURL){

        mRef.child(mContext.getString(R.string.user_account_settings))
                .child(mAuth.getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(downloadURL);

    }
}


































