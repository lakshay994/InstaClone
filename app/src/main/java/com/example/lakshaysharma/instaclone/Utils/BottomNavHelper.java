package com.example.lakshaysharma.instaclone.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;

import com.example.lakshaysharma.instaclone.Home.HomeActivity;
import com.example.lakshaysharma.instaclone.Likes.LikeActivity;
import com.example.lakshaysharma.instaclone.Profile.ProfileActivity;
import com.example.lakshaysharma.instaclone.R;
import com.example.lakshaysharma.instaclone.Search.SearchActivity;
import com.example.lakshaysharma.instaclone.Share.ShareActivity;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

public class BottomNavHelper {

    private static final String TAG = "BottomNavHelper";

    public static void setupNav(BottomNavigationViewEx navigationViewEx){

        Log.d(TAG, "setupNav: setting up Nav bar animation rules");
        
        navigationViewEx.enableItemShiftingMode(false);
        navigationViewEx.enableAnimation(false);
        navigationViewEx.enableShiftingMode(false);
        navigationViewEx.setTextVisibility(false);
    }

    public static void enableNavigation(final Context context, final Activity callingActivity, BottomNavigationViewEx viewEx){

        Log.d(TAG, "enableNavigation: Navigation Highligh Method");

        viewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_house: /* Menu Index = 0 */
                        context.startActivity(new Intent(context, HomeActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_search: /* Menu Index = 1 */
                        context.startActivity(new Intent(context, SearchActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_circle: /* Menu Index = 2 */
                        context.startActivity(new Intent(context, ShareActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_alert: /* Menu Index = 3 */
                        context.startActivity(new Intent(context, LikeActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.ic_android: /* Menu Index = 4 */
                        context.startActivity(new Intent(context, ProfileActivity.class));
                        callingActivity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }

                return false;
            }
        });
    }
}
