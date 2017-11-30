/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.BitcoinWallet;
import io.multy.model.entities.Wallet;


public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.WalletHolder> {

    private List<Wallet> wallets;
    private OnWalletClickListener listener;

    public WalletAdapter(OnWalletClickListener listener) {
        this.listener = listener;
        wallets = new ArrayList<>();

        // TODO remove with real data
        for (int i = 0; i < 4; i++){
            wallets.add(new BitcoinWallet("My Wallet " + i, "address - " + i, i * 1000));
        }
    }

    public WalletAdapter(List<Wallet> wallets, OnWalletClickListener listener) {
        this.listener = listener;
        this.wallets = wallets;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallet, parent, false);
        return new WalletHolder(view);
    }

    @Override
    public void onBindViewHolder(WalletHolder holder, int position) {
        holder.bind(wallets.get(position));
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }


    public class WalletHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout root;
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.balance_original)
        TextView balanceOriginal;
        @BindView(R.id.balance_currency)
        TextView balanceUsd;

        public WalletHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Wallet wallet) {
            name.setText(wallet.getName());
            balanceOriginal.setText(wallet.getBalanceWithCode());
            balanceUsd.setText(wallet.getBalanceWithCode());
            root.setOnClickListener(view -> listener.onWalletClick(wallet));
        }
    }

    public interface OnWalletClickListener{
        void onWalletClick(Wallet wallet);
    }
}