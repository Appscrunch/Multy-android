/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.wallet.Wallet;

public class MyWalletsAdapter extends RecyclerView.Adapter<MyWalletsAdapter.Holder> {

    private final static DecimalFormat format = new DecimalFormat("#.##");
    private List<Wallet> data;
    private CurrenciesRate rates;
    private OnWalletClickListener listener;

    public MyWalletsAdapter(OnWalletClickListener listener, List<Wallet> data) {
        this.listener = listener;
        this.data = data;
    }

    public MyWalletsAdapter(List<Wallet> data) {
        this.data = data;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_asset_item, parent, false));
    }

    public void updateRates(CurrenciesRate rates) {
        this.rates = rates;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Wallet wallet = data.get(position);

        holder.name.setText(data.get(position).getWalletName());
        holder.imagePending.setVisibility(wallet.isPending() ? View.VISIBLE : View.GONE);

        try {
            holder.amount.setText(wallet.getBalanceLabel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            holder.amountFiat.setText(wallet.getFiatBalanceLabel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.imageChain.setImageResource(wallet.getIconResourceId());
        holder.itemView.setOnClickListener(view -> listener.onWalletClick(data.get(position)));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Wallet> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setAmount(int position, double amount) {
//        data.get(position).setBalance(amount);
        notifyItemChanged(position);
    }

    public void setListener(OnWalletClickListener listener) {
        this.listener = listener;
    }

    public List<Wallet> getData() {
        return data;
    }

    public Wallet getItem(int position) {
        return data.get(position);
    }

    class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_name)
        TextView name;
        @BindView(R.id.text_amount)
        TextView amount;
        @BindView(R.id.text_amount_fiat)
        TextView amountFiat;
        @BindView(R.id.text_currency)
        TextView currency;
        @BindView(R.id.image_chain)
        ImageView imageChain;
        @BindView(R.id.image_pending)
        ImageView imagePending;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }
}
