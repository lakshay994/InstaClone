package com.example.lakshaysharma.instaclone.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/* This class acts as the adapter for the view pager layout to swipe b/w pages*/
public class MiddlePagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "MiddlePagerAdapter";

    private List<Fragment> mFragmentlist = new ArrayList<>();

    public MiddlePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentlist.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentlist.size();
    }

    public void addFragments(Fragment fragment){

        mFragmentlist.add(fragment);
    }
}
