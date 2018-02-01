/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.Fee;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.FeeAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.viewmodels.AssetSendViewModel;

public class TransactionFeeFragment extends BaseFragment implements FeeAdapter.OnFeeClickListener {

    public static TransactionFeeFragment newInstance() {
        return new TransactionFeeFragment();
    }

    @BindView(R.id.scrollview)
    ScrollView scrollView;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.switcher)
    SwitchCompat switcher;
    @BindView(R.id.text_donation_allow)
    TextView textDonationAllow;
    @BindView(R.id.text_donation_summ)
    TextView textDonationSumm;
    @BindView(R.id.input_donation)
    EditText inputDonation;
    @BindView(R.id.text_fee_currency)
    TextView textFeeCurrency;
    @BindView(R.id.group_donation)
    Group groupDonation;
    @BindString(R.string.donation_format_pattern)
    String formatPattern;

//    private FeeAdapter adapter;

    private AssetSendViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction_fee, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        inputDonation.setOnFocusChangeListener((view1, hasFocus) -> {
            if (hasFocus) {
                scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 500);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        recyclerView.setAdapter(new FeeAdapter(getActivity(), this, viewModel.getFee()));
        setupSwitcher();
        setupInput();
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        if (viewModel.getFee() != null) {
            if (switcher.isChecked()) {
                viewModel.setDonationAmount(inputDonation.getText().toString());
            } else {
                viewModel.setDonationAmount(null);
            }
            ((AssetSendActivity) getActivity()).setFragment(R.string.send_amount, R.id.container, AmountChooserFragment.newInstance());
            if (viewModel.isAmountScanned()) {
                ((AssetSendActivity) getActivity()).setFragment(R.string.send_summary, R.id.container, SendSummaryFragment.newInstance());
            }
        } else {
            Toast.makeText(getActivity(), R.string.choose_transaction_speed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFeeClick(Fee fee) {
        viewModel.saveFee(fee);
    }

    private void setupSwitcher() {
        switcher.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                textDonationAllow.setBackground(getResources().getDrawable(R.drawable.shape_top_round_white, null));
                groupDonation.setVisibility(View.VISIBLE);
            } else {
                textDonationAllow.setBackground(getResources().getDrawable(R.drawable.shape_squircle_white, null));
                groupDonation.setVisibility(View.GONE);
                hideKeyboard(getActivity());
            }
        });
    }

    private void setupInput() {
        textFeeCurrency.setText(new DecimalFormat(formatPattern)
                .format(Double.parseDouble(inputDonation.getText().toString()) * viewModel.getCurrenciesRate().getBtcToUsd()));
        textFeeCurrency.append(Constants.SPACE);
        textFeeCurrency.append(CurrencyCode.USD.name());

        inputDonation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!TextUtils.isEmpty(charSequence)) {
                    textFeeCurrency.setText(new DecimalFormat(formatPattern)
                            .format(Double.parseDouble(charSequence.toString()) * viewModel.getCurrenciesRate().getBtcToUsd()));
                    textFeeCurrency.append(Constants.SPACE);
                    textFeeCurrency.append(CurrencyCode.USD.name());
                } else {
                    textFeeCurrency.setText(Constants.SPACE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}
