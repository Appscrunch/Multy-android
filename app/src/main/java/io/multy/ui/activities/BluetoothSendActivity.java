/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.adapters.BlueoothDevicesPagerAdapter;
import io.multy.ui.adapters.WalletsPagerAdapter;

public class BluetoothSendActivity extends BaseActivity {

    public static void show(Context context) {
        Intent intent = new Intent(context, BluetoothSendActivity.class);
        context.startActivity(intent);
    }

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.wallets_view_pager)
    ViewPager walletsViewPager;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_send);
        ButterKnife.bind(this);
        viewPager.setClipToPadding(false);
        viewPager.setPadding(250, 0, 250, 0);
//        viewPager.setPageMargin(50);
        viewPager.setAdapter(new BlueoothDevicesPagerAdapter(getSupportFragmentManager()));
        walletsViewPager.setClipToPadding(false);
        walletsViewPager.setPadding(250, 0, 250, 0);
//        walletsViewPager.setAdapter(new WalletsPagerAdapter(getSupportFragmentManager()));
        walletsViewPager.setPageMargin(100);
    }
}
