package com.example.lakshaysharma.instaclone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.lakshaysharma.instaclone.Profile.AccountSettingsActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.Permissions;

public class PhotoFragment extends Fragment {

    private static final String TAG = "PhotoFragment";

    private static final int CAMERA_REQ_CODE = 1;
    private static final int PHOTO_FRAG_NUM = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        Log.d(TAG, "onCreateView: Started");

        Button camera = view.findViewById(R.id.shareOpenCamera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((ShareActivity)getActivity()).getTabNumber() == PHOTO_FRAG_NUM){
                    if (((ShareActivity)getActivity()).checkSinglePermission(Permissions.CAMERA_PERSMISSION[0])){

                        Log.d(TAG, "onClick: Starting Camera");

                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQ_CODE);
                    }
                    else {
                        startActivity(new Intent(getActivity(), ShareActivity.class).setFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        ));
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQ_CODE){

            Log.d(TAG, "onActivityResult: Photo Taken");
            Log.d(TAG, "onActivityResult: Navigating to final screen");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            if (isRootTask()){

                try {
                    Log.d(TAG, "onActivityResult: got new image from camera " + bitmap);

                    Intent nextActivity = new Intent(getActivity(), NextActivity.class);
                    nextActivity.putExtra(getString(R.string.camera_bitmap), bitmap);
                    startActivity(nextActivity);
                    Log.d(TAG, "onActivityResult: going to account settings activity");
                }catch (NullPointerException exc){
                    Log.e(TAG, "onActivityResult: " + exc.getMessage() );
                }

            }
            else {
                try {
                    Log.d(TAG, "onActivityResult: got new image from camera " + bitmap);

                    Intent nextActivity = new Intent(getActivity(), AccountSettingsActivity.class);
                    nextActivity.putExtra(getString(R.string.camera_bitmap), bitmap);
                    nextActivity.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile));
                    startActivity(nextActivity);
                    Log.d(TAG, "onActivityResult: going to account settings activity");
                    getActivity().finish();

                }catch (NullPointerException exc){
                    Log.e(TAG, "onActivityResult: " + exc.getMessage() );
                }

            }

        }
    }

    public boolean isRootTask(){

        if (((ShareActivity)getActivity()).getTask() == 0){
            return true;
        }
        else {
            return false;
        }

    }
}
