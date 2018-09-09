package com.example.lakshaysharma.instaclone.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SectionsPagerAdapter_ProfileSettings extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumber = new HashMap<>();
    private final HashMap<Integer, String> mFragmentName = new HashMap<>();

    public SectionsPagerAdapter_ProfileSettings(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String fragmentName){
        mFragmentList.add(fragment);
        mFragments.put(fragment, mFragmentList.size() - 1);
        mFragmentNumber.put(fragmentName, mFragmentList.size() - 1);
        mFragmentName.put(mFragmentList.size() - 1, fragmentName);
    }

    public Integer getFragmentNumber(Fragment fragment){
        if (mFragmentNumber.containsKey(fragment)){
            return mFragmentNumber.get(fragment);
        }
        else return null;
    }

    public Integer getFragmentNumber(String fragmentName){
        if (mFragmentNumber.containsKey(fragmentName)){
            return mFragmentNumber.get(fragmentName);
        }
        else return null;
    }

    public String getFragmentName(Integer fragmentNumber){
        if (mFragmentName.containsKey(fragmentNumber)){
            return mFragmentName.get(fragmentNumber);
        }
        else return null;
    }

}
