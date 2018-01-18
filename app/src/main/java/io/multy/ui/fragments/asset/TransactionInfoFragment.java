/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 16.01.18.
 */

public class TransactionInfoFragment extends BaseFragment {

    public static final String TAG = TransactionInfoFragment.class.getSimpleName();
    private WalletViewModel viewModel;

    public static TransactionInfoFragment newInstance() {
        return new TransactionInfoFragment();
    }

    public TransactionInfoFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = getLayoutInflater().inflate(R.layout.fragment_transaction_info, container, false);
        ButterKnife.bind(this, v);
        viewModel = ViewModelProviders.of(this).get(WalletViewModel.class);
        return v;
    }
}
