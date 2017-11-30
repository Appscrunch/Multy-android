/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.receive;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.zxing.WriterException;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.AssetRequestActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.viewmodels.AssetRequestViewModel;


public class RequestSummaryFragment extends BaseFragment {

    public static RequestSummaryFragment newInstance(){
        return new RequestSummaryFragment();
    }

    @BindView(R.id.image_qr)
    ImageView qr;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.balance_original)
    TextView balanceOriginal;
    @BindView(R.id.balance_currency)
    TextView balanceUsd;
    @BindView(R.id.balance_original_send)
    TextView balanceOriginalSend;
    @BindView(R.id.balance_usd_send)
    TextView balanceUsdSend;
    @BindView(R.id.request_amount)
    TextView requestAmount;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.wallet_name)
    TextView walletName;

    @BindInt(R.integer.zero)
    int zero;

    private AssetRequestViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request_summary, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(AssetRequestViewModel.class);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        address.setText(viewModel.getWallet().getAddress());
        walletName.setText(viewModel.getWallet().getName());
        balanceOriginal.setText(viewModel.getWallet().getBalanceWithCode());
        balanceUsd.setText(String.valueOf(viewModel.getWallet().getBalance()));

        if (viewModel.getAmount() != zero){
            requestAmount.setVisibility(View.INVISIBLE);
            balanceUsdSend.setVisibility(View.VISIBLE);
            balanceOriginalSend.setVisibility(View.VISIBLE);
            balanceUsdSend.setText(String.valueOf(viewModel.getAmount()));
            balanceOriginalSend.setText(String.valueOf(viewModel.getAmount()));
        }

        new Thread(() -> {
            try {
                final Bitmap bitmap = viewModel.generateQR(getActivity());
                getActivity().runOnUiThread(() -> qr.setImageBitmap(bitmap));
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @OnClick(R.id.request_amount)
    void onClickRequestAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.balance_original_send)
    void onClickBalanceOriginalSendAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.balance_usd_send)
    void onClickBalanceUsdSendAmount(){
        ((AssetRequestActivity) getActivity()).setFragment(R.string.receive, AmountChooserFragment.newInstance());
    }

    @OnClick(R.id.balance_currency)
    void onClickBalanceUsdAmount(){
        getFragmentManager().popBackStack();
    }
}
