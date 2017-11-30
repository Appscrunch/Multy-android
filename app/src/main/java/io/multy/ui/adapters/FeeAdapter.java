/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.adapters;


import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.model.entities.BitcoinWallet;
import io.multy.model.entities.Wallet;

public class FeeAdapter extends RecyclerView.Adapter<FeeAdapter.FeeHolder> {

    private int[] iconIds;
    private List<String> names;
    private List<String> blockIds;
    private List<Wallet> wallets;
    private OnFeeClickListener listener;
    private int prevPosition;
    private ImageView prevMark;

    public FeeAdapter(Context context, OnFeeClickListener listener) {
        this.listener = listener;
        wallets = new ArrayList<>();
        iconIds = new int[]{R.drawable.ic_very_fast, R.drawable.ic_fast, R.drawable.ic_medium, R.drawable.ic_slow, R.drawable.ic_very_slow, R.drawable.ic_custom};
        names = Arrays.asList(context.getResources().getStringArray(R.array.fees));
        blockIds = Arrays.asList(context.getResources().getStringArray(R.array.blocks));
        for (int i = 0; i < 6; i++) {
            wallets.add(new BitcoinWallet("Fast " + i + " hour", "", i));
        }
        prevPosition = -1;
    }

    @Override
    public FeeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fee, parent, false);
        return new FeeHolder(view);
    }

    @Override
    public void onBindViewHolder(FeeHolder holder, int position) {
        holder.bind(wallets.get(position));
    }

    @Override
    public int getItemCount() {
        return wallets.size();
    }


    public class FeeHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root)
        ConstraintLayout root;
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.name)
        TextView name;
        @BindView(R.id.balance_original)
        TextView balanceOriginal;
        @BindView(R.id.blocks)
        TextView blocks;
        @BindView(R.id.divider)
        View divider;
        @BindView(R.id.mark)
        ImageView mark;

        public FeeHolder(View itemView) {
            super(itemView);
            setIsRecyclable(false);
            ButterKnife.bind(this, itemView);
        }

        void bind(final Wallet wallet) {
            name.setText(names.get(getAdapterPosition()));
            balanceOriginal.setText(wallet.getBalanceWithCode());
            image.setImageResource(iconIds[getAdapterPosition()]);
            blocks.setText(blockIds.get(getAdapterPosition()));
            if (getAdapterPosition() == getItemCount() - 1){
                divider.setVisibility(View.GONE);
                balanceOriginal.setVisibility(View.GONE);
            }
            root.setOnClickListener(view -> {
                if (prevPosition != getAdapterPosition()) {
                    mark.setVisibility(View.VISIBLE);
                    if (prevMark != null){
                        prevMark.setVisibility(View.GONE);
                        notifyItemChanged(prevPosition);
                    }
                    notifyItemChanged(getAdapterPosition());
                    prevMark = mark;
                    prevPosition = getAdapterPosition();
                } else {
                    mark.setVisibility(View.GONE);
                    prevPosition = -1;
                    notifyItemChanged(getAdapterPosition());
                    prevMark = null;
                }
                listener.onFeeClick(wallet);
            });
        }
    }

    public interface OnFeeClickListener {
        void onFeeClick(Wallet wallet);
    }
}
