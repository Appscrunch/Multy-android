/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.adapters.BlueoothDevicesPagerAdapter;
import io.multy.ui.adapters.WalletsPagerAdapter;
import io.multy.ui.fragments.BaseFragment;

public class BluetoothSendFragment extends BaseFragment {

    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.wallets_view_pager)
    ViewPager walletsViewPager;

    public static BluetoothSendFragment newInstance() {
        return new BluetoothSendFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_bluetooth_send, container, false);
        ButterKnife.bind(this, view);
        setupUi();
        return view;
    }

    private void setupUi() {
        viewPager.setClipToPadding(false);
        viewPager.setPadding(250, 0, 250, 0);
//        viewPager.setPageMargin(50);
        viewPager.setAdapter(new BlueoothDevicesPagerAdapter(getChildFragmentManager()));
        walletsViewPager.setClipToPadding(false);
        walletsViewPager.setPadding(250, 0, 250, 0);
//        walletsViewPager.setAdapter(new WalletsPagerAdapter(getChildFragmentManager()));
        walletsViewPager.setPageMargin(100);
    }

}
