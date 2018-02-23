/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.Multy;
import io.multy.R;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.util.NativeDataHelper;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 23.02.18.
 */

public class PrivateKeyDialogFragment extends BottomSheetDialogFragment {

    @BindView(R.id.text_key)
    TextView textKey;

    private WalletAddress address;

    public static PrivateKeyDialogFragment getInstance(WalletAddress address) {
        PrivateKeyDialogFragment fragment = new PrivateKeyDialogFragment();
        fragment.setAddress(address);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View view = View.inflate(getContext(), R.layout.bottom_sheet_private_key, null);
        ButterKnife.bind(this, view);
        dialog.setContentView(view);
        try {
            WalletViewModel viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
            byte[] seed = RealmManager.getSettingsDao().getSeed().getSeed();
            int walletIndex = viewModel.getWalletLive().getValue().getWalletIndex();
            int addressIndex = address.getIndex();
            int currency = NativeDataHelper.Currency.BTC.getValue();
            String privateKey = NativeDataHelper.getMyPrivateKey(seed, walletIndex, addressIndex, currency);
            textKey.setText(privateKey);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Toast.makeText(Multy.getContext(), "Error while build private key ;(",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setAddress(WalletAddress address) {
        this.address = address;
    }
}
