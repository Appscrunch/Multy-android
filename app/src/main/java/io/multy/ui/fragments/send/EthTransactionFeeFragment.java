/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.Fee;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.adapters.EthFeeAdapter;
import io.multy.ui.adapters.MyFeeAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;

public class EthTransactionFeeFragment extends BaseFragment
        implements MyFeeAdapter.OnCustomFeeClickListener, EthFeeAdapter.OnItemClickListener {

    public static EthTransactionFeeFragment newInstance() {
        return new EthTransactionFeeFragment();
    }

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindString(R.string.donation_format_pattern)
    String formatPattern;

    private AssetSendViewModel viewModel;
    private EthFeeAdapter feeAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feeAdapter = new EthFeeAdapter();
        feeAdapter.setOnItemClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eth_transaction_fee, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);

        viewModel.speeds.observe(this, speeds -> setAdapter());
        viewModel.requestFeeRates(viewModel.getWallet().getCurrencyId(), viewModel.getWallet().getNetworkId());
        Analytics.getInstance(getActivity()).logTransactionFeeLaunch(viewModel.getChainId());
        return view;
    }

    private void setAdapter() {
        recyclerView.setAdapter(new MyFeeAdapter(viewModel.speeds.getValue().asList(), this));
    }

    @Override
    public void onDestroyView() {
        hideKeyboard(getActivity());
        super.onDestroyView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_SET_GAS && data.getExtras() != null) {
            String gasPrice = data.getExtras().getString(Constants.GAS_PRICE, "");
            String gasLimit = data.getExtras().getString(Constants.GAS_LIMIT, "");
            if (!TextUtils.isEmpty(gasPrice) && !TextUtils.isEmpty(gasLimit)) {
                feeAdapter.setCustomFee(gasPrice, gasLimit);
            }
            //todo insert selected rate into viewmodel
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClickCustom() {
        logTransactionFee(5);
        if (getActivity() != null) {
            EthCustomSpeedFragment fragment = (EthCustomSpeedFragment) getActivity().getSupportFragmentManager()
                    .findFragmentByTag(EthCustomSpeedFragment.TAG);
            if (fragment == null) {
                fragment = EthCustomSpeedFragment.getInstance();
            }
            fragment.setTargetFragment(this, Constants.REQUEST_CODE_SET_GAS);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment)
                    .addToBackStack(EthCustomSpeedFragment.TAG).commit();
        }
    }

    @Override
    public void onClickFee(int position) {
        //todo insert selected rate into viewmodel
        logTransactionFee(position);
    }

    public void showCustomFeeDialog(long currentValue) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_custom_fee, null);
        dialogBuilder.setView(dialogView);

        final TextInputEditText input = dialogView.findViewById(R.id.input_custom);
        input.setText(currentValue == -1 ? String.valueOf(20) : String.valueOf(currentValue));

        dialogBuilder.setTitle(R.string.custom_fee);
        dialogBuilder.setPositiveButton(R.string.done, (dialog, whichButton) -> {
            ((MyFeeAdapter) recyclerView.getAdapter()).setCustomFee(Long.valueOf(input.getText().toString()));
            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_SET, viewModel.getChainId());
        });
        dialogBuilder.setNegativeButton(R.string.cancel, (dialog, whichButton) -> {
            Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM_CANCEL, viewModel.getChainId());
        });
        dialogBuilder.create().show();
    }

    @Override
    public void onClickCustomFee(long currentValue) {
        showCustomFeeDialog(currentValue);
        logTransactionFee(5);
    }

    @Override
    public void logTransactionFee(int position) {
        switch (position) {
            case 0:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_VERY_FAST, viewModel.getChainId());
                break;
            case 1:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_FAST, viewModel.getChainId());
                break;
            case 2:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_MEDIUM, viewModel.getChainId());
                break;
            case 3:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_SLOW, viewModel.getChainId());
                break;
            case 4:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_VERY_SLOW, viewModel.getChainId());
                break;
            case 5:
                Analytics.getInstance(getActivity()).logTransactionFee(AnalyticsConstants.TRANSACTION_FEE_CUSTOM, viewModel.getChainId());
                break;
        }
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        Fee selectedFee = ((MyFeeAdapter) recyclerView.getAdapter()).getSelectedFee();

        if (selectedFee != null) {
            viewModel.setFee(selectedFee);
            ((AssetSendActivity) getActivity()).setFragment(R.string.send_amount, R.id.container, AmountChooserFragment.newInstance());

            if (viewModel.isAmountScanned()) {
                ((AssetSendActivity) getActivity()).setFragment(R.string.send_summary, R.id.container, SendSummaryFragment.newInstance());
            }
        } else {
            Toast.makeText(getActivity(), R.string.choose_transaction_speed, Toast.LENGTH_SHORT).show();
        }
    }
}
