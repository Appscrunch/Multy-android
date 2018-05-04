/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.BluetoothReceiver;
import io.multy.ui.fragments.dialogs.DonateDialog;
import io.multy.util.CircleView;

public class BlueoothDevicesPagerAdapter extends PagerAdapter {

    @BindView(R.id.circle_view)
    CircleView circleView;


    private ArrayList<BluetoothReceiver> bluetoothReceivers = new ArrayList<>();

    private FragmentManager fragmentManager;


    public BlueoothDevicesPagerAdapter(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getCount() {
        return bluetoothReceivers.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    public void addReceiver(BluetoothReceiver receiver) {
        bluetoothReceivers.add(receiver);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View layout = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_circle, container, false);
        container.addView(layout);
        ButterKnife.bind(this, layout);
        circleView.generatePaintColor();
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public BluetoothReceiver getItemForPosition(int position) {
        return bluetoothReceivers.get(position);
    }


}
