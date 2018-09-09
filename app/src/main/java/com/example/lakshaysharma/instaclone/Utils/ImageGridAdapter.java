package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import com.example.lakshaysharma.instaclone.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;


public class ImageGridAdapter extends ArrayAdapter<String>{

    Context mContext;
    LayoutInflater mInflater;
    int mLayoutResource;
    String mAppend;
    ArrayList<String> imageURLs;

    public ImageGridAdapter(Context mContext, int mLayoutResource, String mAppend, ArrayList<String> imageURLs) {
        super(mContext, mLayoutResource, imageURLs);
        this.mContext = mContext;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mLayoutResource = mLayoutResource;
        this.mAppend = mAppend;
        this.imageURLs = imageURLs;
    }

    private static class ViewHolder{
        SquareImageView image;
        ProgressBar progressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(mLayoutResource, parent, false);
            holder = new ViewHolder();
            holder.image = convertView.findViewById(R.id.gridImageView);
            holder.progressBar = convertView.findViewById(R.id.gridImageProgressBar);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        String imageURL = getItem(position);
        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(mAppend + imageURL, holder.image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (holder.progressBar != null){
                    holder.progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (holder.progressBar != null){
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (holder.progressBar != null){
                    Log.d(TAG, "onLoadingComplete: Complete");
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (holder.progressBar != null){
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        return convertView;
    }
}
