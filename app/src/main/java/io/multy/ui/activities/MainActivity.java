/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;


import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.ui.fragments.main.ContactsFragment;
import io.multy.ui.fragments.main.FeedFragment;
import io.multy.ui.fragments.main.SettingsFragment;
import io.multy.util.Constants;


public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private boolean isFirstFragmentCreation;
    private int lastTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        setupFooter();
        setFragment(R.id.inner_container, AssetsFragment.newInstance());

//        startActivity(new Intent(this, SeedActivity.class));
//        NativeDataHelper.runTest();
    }

    private void setFragment(@IdRes int container, Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unCheckAllTabs();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        changeStateLastTab(lastTabPosition, false);
        lastTabPosition = tab.getPosition();
        changeStateLastTab(lastTabPosition, true);
        switch (tab.getPosition()) {
            case Constants.POSITION_ASSETS:
                setFragment(R.id.inner_container, AssetsFragment.newInstance());
                break;
            case Constants.POSITION_FEED:
                setFragment(R.id.inner_container, FeedFragment.newInstance());
                break;
            case Constants.POSITION_CONTACTS:
                setFragment(R.id.inner_container, ContactsFragment.newInstance());
                break;
            case Constants.POSITION_SETTINGS:
                setFragment(R.id.inner_container, SettingsFragment.newInstance());
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    private void setupFooter() {
        tabLayout.addOnTabSelectedListener(this);
    }

    private void changeStateLastTab(int position, boolean mustEnable) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab == null) {
            return;
        }
        View view = tab.getCustomView();
        if (view == null) {
            return;
        }
        TextView title = view.findViewById(R.id.title);
        ImageView image = view.findViewById(R.id.image);
        int filterColor;
        if (mustEnable) {
            filterColor = ContextCompat.getColor(this, R.color.tab_active);
        }
        else {
            filterColor = ContextCompat.getColor(this, R.color.tab_inactive);
        }
        title.setTextColor(filterColor);
        image.setColorFilter(filterColor, PorterDuff.Mode.SRC_IN);
    }

    private void unCheckAllTabs() {
        tabLayout.getTabAt(0).getCustomView().setSelected(false);
        tabLayout.getTabAt(1).getCustomView().setSelected(false);
        tabLayout.getTabAt(3).getCustomView().setSelected(false);
        tabLayout.getTabAt(4).getCustomView().setSelected(false);
    }

    @OnClick(R.id.fast_operations)
    void onFastOperationsClick() {

    }
}
