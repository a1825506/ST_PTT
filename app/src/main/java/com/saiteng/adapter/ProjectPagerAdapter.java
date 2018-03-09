package com.saiteng.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.saiteng.fragment.ChannelFragment;
import com.saiteng.fragment.ChatFragment;
import com.saiteng.fragment.GroupsFragment;
import com.saiteng.fragment.MapFragment;

public class ProjectPagerAdapter extends FragmentPagerAdapter {

    private static String[] titles = {"地图","频道","成员","通讯"};

    private Context mcontext;

    public ProjectPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mcontext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new MapFragment();

                break;

            case 1:
                fragment = new ChannelFragment();

                break;
            case 2:
                fragment = new GroupsFragment();
                break;
//            case 3:
//                fragment = new VideoFragment();
//                break;
            case 3:
                fragment = new ChatFragment();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return titles.length;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
