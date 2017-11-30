/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetSendViewModel;

public class SendSummaryFragment extends BaseFragment {

    public static SendSummaryFragment newInstance(){
        return new SendSummaryFragment();
    }

    @BindView(R.id.receiver_balance_original)
    TextView receiverBalanceOriginal;
    @BindView(R.id.receiver_balance_usd)
    TextView receiverBalanceUsd;
    @BindView(R.id.receiver_address)
    TextView receiverAddress;
    @BindView(R.id.wallet_name)
    TextView walletName;
    @BindView(R.id.sender_balance_original)
    TextView senderBalanceOriginal;
    @BindView(R.id.sender_balance_currency)
    TextView senderBalanceCurrency;
    @BindView(R.id.transaction_fee_speed)
    TextView feeSpeed;
    @BindView(R.id.transaction_fee_amount)
    TextView feeAmount;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send_summary, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);

        setInfo();

        return view;
    }

    @OnClick(R.id.btn_next)
    void onClickNext(){
        AssetSendDialogFragment dialog = new AssetSendDialogFragment();
        dialog.show(getActivity().getFragmentManager(), null);
    }

    private void setInfo(){
        receiverBalanceOriginal.setText(String.valueOf(viewModel.getAmount()));
        receiverBalanceUsd.setText(String.valueOf(viewModel.getAmount()));
        receiverAddress.setText(viewModel.getReceiverAddress().getValue());
        walletName.setText(viewModel.getWallet().getName());
        senderBalanceOriginal.setText(viewModel.getWallet().getBalanceWithCode());
        senderBalanceCurrency.setText(viewModel.getWallet().getBalanceWithCode());
        feeSpeed.setText(viewModel.getFee().getName());
        feeAmount.setText(String.valueOf(viewModel.getFee().getBalance()));
    }

}
