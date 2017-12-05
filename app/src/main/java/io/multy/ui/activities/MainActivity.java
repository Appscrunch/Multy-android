/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.branch.referral.Branch;
import io.multy.R;
import io.multy.ui.fragments.main.AssetsFragment;
import io.multy.util.Constants;
import io.multy.util.NativeDataHelper;


public class MainActivity extends BaseActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private boolean isFirstFragmentCreation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;

        setupFooter();
        setFragment(R.id.inner_container, AssetsFragment.newInstance());
//        initBranchIO();

//        startActivity(new Intent(this, SeedActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBranchIO();
    }

    private void initBranchIO(){
        Branch branch = Branch.getInstance(getApplicationContext());

        branch.initSession((referringParams, error) -> {
            Log.i(getClass().getSimpleName(), "branch io link");
            if (error == null) {
                String qrCode = referringParams.optString(Constants.DEEP_LINK_QR_CODE);
                if (!TextUtils.isEmpty(qrCode)) {
                    Log.i(getClass().getSimpleName(), "branch io link exist");
                    getIntent().putExtra(Constants.DEEP_LINK_QR_CODE, qrCode);
                }

//                {"session_id":"465086124545736122","identity_id":"465073643500779705","link":"https://zn0o.test-app.link?%24identity_id=465073643500779705",
//                  "data":"{\"$og_title\":\"QR_CODE\",\"$publicly_indexable\":\"true\",\"~creation_source\":2,\"$og_description\":\"Multi cryptocurrency and assets open-source wallet\",
//                  \"+referrer\":\"com.skype.raider\",\"+click_timestamp\":1512122667,\"QR_CODE\":\"bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\",
//                  \"source\":\"android\",\"$identity_id\":\"465073643500779705\",\"$og_image_url\":\"http://multy.io/wp-content/uploads/2017/11/logo-1.png\",
//                  \"~feature\":\"Share\",\"+match_guaranteed\":false,\"$desktop_url\":\"http://multy.io\",\"~tags\":[\"bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\"],
//                  \"$canonical_identifier\":\"QR_CODE/bitcoin:1GLY7sDe7a6xsewDdUNA6F8CEoAxQsHV37\",\"+clicked_branch_link\":true,\"$one_time_use\":false,
//                  \"~id\":\"465075453422808951\",\"+is_first_session\":false,\"~referring_link\":\"https://zn0o.test-app.link/7kshikidwI\"}","device_fingerprint_id":"465073643483986343"}
            } else {
                Log.i(getClass().getSimpleName(), error.getMessage());
            }
        }, this.getIntent().getData(), this);
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
        switch (tab.getPosition()) {
            case Constants.POSITION_ASSETS:
//                setFragment(R.id.full_container, AssetsFragment.newInstance());
                startActivity(new Intent(this, AssetSendActivity.class));
                break;
            case Constants.POSITION_FEED:
//                setFragment(R.id.full_container, FeedFragment.newInstance());
                startActivity(new Intent(this, AssetRequestActivity.class));
                break;
            case Constants.POSITION_CONTACTS:
//                setFragment(R.id.full_container, ContactsFragment.newInstance());
                startActivity(new Intent(this, AssetSendActivity.class));
                break;
            case Constants.POSITION_SETTINGS:
//                setFragment(R.id.full_container, SettingsFragment.newInstance());
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
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_assets));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_feed));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_main));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_contacts));
        tabLayout.addTab(tabLayout.newTab().setCustomView(R.layout.footer_settings));
        tabLayout.addOnTabSelectedListener(this);
    };

    private void unCheckAllTabs() {
        tabLayout.getTabAt(0).getCustomView().setSelected(false);
        tabLayout.getTabAt(1).getCustomView().setSelected(false);
        tabLayout.getTabAt(3).getCustomView().setSelected(false);
        tabLayout.getTabAt(4).getCustomView().setSelected(false);
    }
}
