/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.realm.RealmResults;

public class WalletsPagerAdapter extends PagerAdapter {

    @BindView(R.id.text_balance_original)
    TextView balance;

    @BindView(R.id.text_balance_currency)
    TextView currency;

    @BindView(R.id.text_name)
    TextView walletName;

    private FragmentManager fragmentManager;
    private OnWalletClickListener listener;
    private View.OnTouchListener onTouchListener;
    private RealmResults<Wallet> wallets;

    public WalletsPagerAdapter(FragmentManager fragmentManager, OnWalletClickListener listener) {
        this.fragmentManager = fragmentManager;
        this.listener = listener;
    }

    public WalletsPagerAdapter(FragmentManager fragmentManager, OnWalletClickListener listener, View.OnTouchListener onTouchListener) {
        this.fragmentManager = fragmentManager;
        this.listener = listener;
        this.onTouchListener = onTouchListener;
        this.wallets = RealmManager.getAssetsDao().getWallets();
    }

    @Override
    public int getCount() {
        return wallets.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return object == view;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View layout = LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_wallet_bluetooth, container, false);
        container.addView(layout);
        ButterKnife.bind(this, layout);
        walletName.setText(wallets.get(position).getWalletName());
        balance.setText(wallets.get(position).getBalance());
        layout.setOnTouchListener(null);
//        circleView.generatePaintColor(position);
        layout.setOnLongClickListener(v -> {
            listener.onWalletClick();
            layout.setOnTouchListener(onTouchListener);
            return false;
        });
        return layout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public Wallet getWalletByPosition(int position) {
        return wallets.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface OnWalletClickListener {
        void onWalletClick();
    }

}
