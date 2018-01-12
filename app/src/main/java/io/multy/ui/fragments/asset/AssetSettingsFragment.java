/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.dialogs.SimpleDialogFragment;
import io.multy.viewmodels.WalletViewModel;

/**
 * Created by anschutz1927@gmail.com on 07.12.17.
 */

public class AssetSettingsFragment extends BaseFragment {

    public static final String TAG = AssetSettingsFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText name;

    private WalletViewModel viewModel;
    private boolean isInProcess = false;

    public static AssetSettingsFragment newInstance() {
        Bundle args = new Bundle();
        AssetSettingsFragment fragment = new AssetSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public AssetSettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_asset_settings, container, false);
        ButterKnife.bind(this, v);
        viewModel.getWalletLive().observe(this, walletRealmObject -> {
            if (walletRealmObject != null && walletRealmObject.getName() != null) {
                name.setText(walletRealmObject.getName());
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        viewModel.isLoading.setValue(false);
        super.onDestroy();
    }

    private void saveSettings() {
        if (isInProcess || name.getText().toString().isEmpty() || viewModel.getWalletLive() == null ||
                viewModel.getWalletLive().getValue() == null ||
                name.getText().toString().equals(viewModel.getWalletLive().getValue().getName())) {
            return;
        }
        isInProcess = true;
        name.setEnabled(false);
        viewModel.updateWalletSetting(name.getText().toString()).observe(this, isUpdated -> {
            if (isUpdated == null || !isUpdated) {
                isInProcess = false;
                Toast.makeText(getActivity(), "Error, changes not applied!", Toast.LENGTH_SHORT).show();
                name.setEnabled(true);
                return;
            }
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void choiceCurrencyToConvert() {

    }

    private void showMyPrivateKey() {

    }

    private void deleteWallet() {
        isInProcess = true;
        viewModel.removeWallet().observe(this, isRemoved -> {
            if (isRemoved != null && isRemoved) {
                Toast.makeText(getActivity(), R.string.wallet_removed, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
            else if (isRemoved != null) {
                Toast.makeText(getActivity(), "Error, changes not applied!", Toast.LENGTH_SHORT).show();
                isInProcess = false;
            }
        });
    }

    @OnClick(R.id.button_cancel)
    void onClickCancel() {
        if (!isInProcess) {
            getActivity().onBackPressed();
        }
    }

    @OnClick(R.id.button_save)
    void onClickSave() {
        if (!isInProcess) {
            saveSettings();
        }
    }

    @OnClick(R.id.button_currency)
    void onClickCurrency() {
        if (!isInProcess) {
            choiceCurrencyToConvert();
        }
    }

    @OnClick(R.id.button_key)
    void onClickKey() {
        if (!isInProcess) {
            showMyPrivateKey();
        }
    }

    @OnClick(R.id.button_delete)
    void onClickDelete(View view) {
        if (!isInProcess) {
            view.setEnabled(false);
            view.postDelayed(() -> view.setEnabled(true), 500);
            SimpleDialogFragment dialogConfirmation = SimpleDialogFragment
                    .newInstance(R.string.delete_wallet, R.string.delete_confirm, v -> deleteWallet());
            dialogConfirmation.show(getChildFragmentManager(), SimpleDialogFragment.class.getSimpleName());
        }
    }
}
