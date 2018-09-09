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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.Profile.AccountSettingsActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Utils.FilePaths;
import com.example.lakshaysharma.instaclone.Utils.FileSearch;
import com.example.lakshaysharma.instaclone.Utils.ImageGridAdapter;
import com.example.lakshaysharma.instaclone.Utils.Permissions;
import com.example.lakshaysharma.instaclone.Utils.SquareImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private static final String TAG = "GalleryFragment";

    private ImageView closeButton;
    private SquareImageView galleryImage;
    private TextView nextButton;
    private GridView galleryGrid;
    private ProgressBar galleryProgressBar;
    private Spinner directorySpinner;

    private ArrayList<String> mDIRECTORIES;
    private ArrayList<String> directoryNames;
    private static final int COLUMN_NUM = 3;
    private static String mAppend = "file:/";
    private String mSelectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        galleryImage = (SquareImageView) view.findViewById(R.id.shareImagePreview);
        galleryGrid = view.findViewById(R.id.shareGridView);
        galleryProgressBar = view.findViewById(R.id.shareProgressBar);
        directorySpinner = view.findViewById(R.id.galleryList);
        nextButton = view.findViewById(R.id.galleryNext);
        closeButton = view.findViewById(R.id.galleryClose);

        mDIRECTORIES = new ArrayList<>();
        directoryNames = new ArrayList<>();

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "onClick: Closing Gallery");
                getActivity().finish();
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRootTask()){

                    Intent nextActivity = new Intent(getActivity(), NextActivity.class);
                    nextActivity.putExtra(getString(R.string.selected_image), mSelectedImage);
                    startActivity(nextActivity);

                }
                else {

                    Intent nextActivity = new Intent(getActivity(), AccountSettingsActivity.class);
                    nextActivity.putExtra(getString(R.string.selected_image), mSelectedImage);
                    nextActivity.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile));
                    startActivity(nextActivity);
                    getActivity().finish();

                }
            }
        });

        init();
        
        return view;
    }

    private void init(){

        FilePaths filePaths = new FilePaths();

        if (FileSearch.getDirectoryPaths(filePaths.PICTURES) != null){
            mDIRECTORIES = FileSearch.getDirectoryPaths(filePaths.PICTURES);
        }

        mDIRECTORIES.add(filePaths.CAMERA);

        for (int i=0; i<mDIRECTORIES.size(); i++){

            int index = mDIRECTORIES.get(i).lastIndexOf("/");
            String name = mDIRECTORIES.get(i).substring(index).replace("/", "");
            directoryNames.add(name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, directoryNames);
        directorySpinner.setAdapter(adapter);

        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: Directory Selected: " + mDIRECTORIES.get(position));

                setupGridView(mDIRECTORIES.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupGridView(String directory){

        final ArrayList<String> imageURLs = FileSearch.getFilePaths(directory);

        // setup image width
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/COLUMN_NUM;
        galleryGrid.setColumnWidth(imageWidth);

        ImageGridAdapter gridAdapter = new ImageGridAdapter(getActivity(), R.layout.layout_grid_imageview, mAppend, imageURLs);
        galleryGrid.setAdapter(gridAdapter);

        try {

            setImage(imageURLs.get(0), mAppend);
            mSelectedImage = imageURLs.get(0);

        }catch (ArrayIndexOutOfBoundsException exc){
            Log.e(TAG, "setupGridView: No Image at Array index 0" + exc.getMessage());
        }

        galleryGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                setImage(imageURLs.get(position), mAppend);
                mSelectedImage = imageURLs.get(position);

            }
        });

    }

    private void setImage(String imageURL, String append){

        Log.d(TAG, "setImage: Trying to set the image");
        Log.d(TAG, "setImage: Image URL: " + append + imageURL);

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(append + imageURL, galleryImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                galleryProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                galleryProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                galleryProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                galleryProgressBar.setVisibility(View.INVISIBLE);
            }
        });
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
