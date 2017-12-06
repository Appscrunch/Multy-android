/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;

/**
 * Created by anschutz1927@gmail.com on 23.11.17.
 */

public class CreateAssetFragment extends BaseFragment {

    public static final String TAG = CreateAssetFragment.class.getSimpleName();

    @BindView(R.id.edit_name)
    EditText editTextWalletName;

    @BindView(R.id.edit_chain)
    EditText editTextWalletChain;

    @BindView(R.id.edit_option)
    EditText editTextWalletOption;

    @BindView(R.id.edit_currency)
    EditText editTextWalletCurrency;

    @BindView(R.id.text_create)
    TextView textViewCreateWallet;

    public static CreateAssetFragment newInstance() {
        return new CreateAssetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_assets_action_add, container, false);
        ButterKnife.bind(this, v);
        if (getActivity().getWindow() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE |
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        initialize();
        return v;
    }

    private void initialize() {
        editTextWalletName.addTextChangedListener(getEditWatcher());
        editTextWalletChain.addTextChangedListener(getEditWatcher());
        editTextWalletOption.addTextChangedListener(getEditWatcher());
        editTextWalletCurrency.addTextChangedListener(getEditWatcher());
    }

    private TextWatcher getEditWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    if (editTextWalletChain.getText().length() > 0 && editTextWalletCurrency.getText().length() > 0) {
                        textViewCreateWallet.setEnabled(true);
                        textViewCreateWallet.setBackgroundColor(Color.parseColor("#FF459FF9"));
                    }
                } else {
                    textViewCreateWallet.setEnabled(false);
                    textViewCreateWallet.setBackgroundColor(Color.parseColor("#BEC8D2"));
                }
            }
        };
    }

    @OnClick(R.id.text_create)
    void onCreateClick() {
        startActivity(new Intent(getContext(), AssetActivity.class));
        getActivity().finish();
        String creationAddress;

        try {
            creationAddress = NativeDataHelper.makeAccountAddress(new DataManager(getActivity()).getSeed().getSeed(), 0, 0);
            WalletRealmObject walletRealmObject = new WalletRealmObject();
            walletRealmObject.setName(editTextWalletName.getText().toString());
            walletRealmObject.setCurrency(0);
            walletRealmObject.setAddressIndex(0);
            walletRealmObject.setCreationAddress(creationAddress);

            MultyApi.INSTANCE.addWallet(getActivity(), walletRealmObject);
        } catch (JniException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.text_cancel)
    void onCancelClick() {
        getActivity().finish();
    }
}
