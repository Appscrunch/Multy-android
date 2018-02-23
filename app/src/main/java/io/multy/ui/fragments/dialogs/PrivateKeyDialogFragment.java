/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.Multy;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 23.02.18.
 */

public class PrivateKeyDialogFragment extends BottomSheetDialogFragment {

    public static PrivateKeyDialogFragment getInstance(WalletAddress address) {
        PrivateKeyDialogFragment fragment = new PrivateKeyDialogFragment();
        fragment.setAddress(address);
        return fragment;
    }

    @BindView(R.id.text_key)
    TextView textKey;

    private WalletAddress address;
    private WalletViewModel viewModel;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_private_key, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);

        if (!TextUtils.isEmpty(getPrivateKey())) {
            textKey.setText(getPrivateKey());
        } else {
            dismiss();
        }
    }

    @OnClick(R.id.button_copy)
    public void onClickCopy() {
        if (!TextUtils.isEmpty(getPrivateKey())) {
            viewModel.copyToClipboard(getActivity(), getPrivateKey());
        }
    }

    @OnClick(R.id.button_share)
    public void onClickShare() {
        if (!TextUtils.isEmpty(getPrivateKey())) {
            viewModel.share(getActivity(), getPrivateKey());
        }
    }

    @OnClick(R.id.button_cancel)
    public void onClickCancel() {
        dismiss();
    }

    private void setAddress(WalletAddress address) {
        this.address = address;
    }

    private String getPrivateKey() {
        try {
            byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
            int walletIndex = viewModel.getWalletLive().getValue().getWalletIndex();
            int addressIndex = address.getIndex();
            int currency = NativeDataHelper.Currency.BTC.getValue();
            return NativeDataHelper.getMyPrivateKey(seed, walletIndex, addressIndex, currency);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Toast.makeText(Multy.getContext(), "Error while build private key ;(", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
