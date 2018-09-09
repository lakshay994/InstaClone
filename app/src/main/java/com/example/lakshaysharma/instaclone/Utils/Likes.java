package com.example.lakshaysharma.instaclone.Utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Likes {

    private static final String TAG = "LIKES";

    private ImageView redHeart, plainHeart;
    private static final AccelerateInterpolator AccelerateInterpolator = new AccelerateInterpolator();
    private static final DecelerateInterpolator DecelerateInterpolator = new DecelerateInterpolator();

    public Likes(ImageView plainHeart, ImageView reHeart){

        this.plainHeart = plainHeart;
        this.redHeart = reHeart;

    }


    public void toggleLikes(){

        Log.d(TAG, "toggleLikes: toggling likes");

        AnimatorSet animator = new AnimatorSet();

        if(redHeart.getVisibility() == View.VISIBLE){

            Log.d(TAG, "toggleLikes: toggling red heart off");

            redHeart.setScaleX(0.1f);
            redHeart.setScaleX(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(redHeart, "scaleY", 1f, 0f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(AccelerateInterpolator);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(redHeart,"scaleX", 1f, 0f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(AccelerateInterpolator);

            redHeart.setVisibility(View.GONE);
            plainHeart.setVisibility(View.VISIBLE);

            animator.playTogether(scaleDownY, scaleDownX);

        }

        else if (redHeart.getVisibility() == View.GONE){

            Log.d(TAG, "toggleLikes: toggling red heart on");

            redHeart.setScaleY(0.1f);
            redHeart.setScaleY(0.1f);

            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(redHeart, "scaleY", 0.1f, 1f);
            scaleDownY.setDuration(300);
            scaleDownY.setInterpolator(DecelerateInterpolator);

            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(redHeart, "scaleX", 0.1f, 1f);
            scaleDownX.setDuration(300);
            scaleDownX.setInterpolator(DecelerateInterpolator);

            redHeart.setVisibility(View.VISIBLE);
            plainHeart.setVisibility(View.GONE);

            animator.playTogether(scaleDownY, scaleDownX);
        }

        animator.start();
    }

}
