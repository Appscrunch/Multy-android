/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.send;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.socket.CurrenciesRate;
import io.multy.model.entities.Output;
import io.multy.model.entities.wallet.CurrencyCode;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetSendActivity;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.NumberFormatter;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetSendViewModel;


public class AmountChooserFragment extends BaseFragment {

    public static final String TAG = AmountChooserFragment.class.getSimpleName();

    public static AmountChooserFragment newInstance() {
        return new AmountChooserFragment();
    }

    @BindView(R.id.root)
    ConstraintLayout root;
    @BindView(R.id.group_send)
    Group groupSend;
    @BindView(R.id.input_balance_original)
    EditText inputOriginal;
    @BindView(R.id.input_balance_currency)
    EditText inputCurrency;
    @BindView(R.id.text_spendable)
    TextView textSpendable;
    @BindView(R.id.text_total)
    TextView textTotal;
    @BindView(R.id.text_max)
    TextView textMax;
    @BindView(R.id.switcher)
    SwitchCompat switcher;
    @BindView(R.id.container_input_original)
    ConstraintLayout containerInputOriginal;
    @BindView(R.id.container_input_currency)
    ConstraintLayout containerInputCurrency;

    private AssetSendViewModel viewModel;
    private CurrenciesRate currenciesRate;
    private boolean isAmountSwapped = false;
    private long transactionPrice;
    private long spendableSatoshi;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View view = inflater.inflate(R.layout.fragment_amount_chooser, container, false);
        ButterKnife.bind(this, view);
        viewModel = ViewModelProviders.of(getActivity()).get(AssetSendViewModel.class);
        setBaseViewModel(viewModel);
        currenciesRate = RealmManager.getSettingsDao().getCurrenciesRate();

        subscribeToUpdates();
        setupSwitcher();
        setupInputOriginal();
        setupInputCurrency();
        setAmountTotalWithFee();
        initSpendable();
        if (!viewModel.isAmountScanned()) {
            Analytics.getInstance(getActivity()).logSendChooseAmountLaunch(viewModel.getChainId());
        }
        return view;
    }

    private void subscribeToUpdates() {
        viewModel.transaction.observe(this, s -> {
            ((AssetSendActivity) getActivity()).setFragment(R.string.send_summary, R.id.container, SendSummaryFragment.newInstance());
        });
        AssetSendViewModel.transactionPrice.observe(this, transactionPrice -> {
            if (transactionPrice != null) {
                this.transactionPrice = transactionPrice;
                setTotalAmountForInput();
            }
        });
    }

    private void initSpendable() {
        long feePerByte = viewModel.getFee().getAmount();
        if (feePerByte < 20) {
            viewModel.getFee().setAmount(20);
        }

        spendableSatoshi = 0;

        for (WalletAddress walletAddress : viewModel.getWallet().getAddresses()) {
            for (Output output : walletAddress.getOutputs()) {
                if (output.getStatus() == Constants.TX_IN_BLOCK_INCOMING || output.getStatus() == Constants.TX_CONFIRMED_INCOMING) {
                    spendableSatoshi += Long.valueOf(output.getTxOutAmount());
                }
            }
        }
        textSpendable.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(spendableSatoshi)));
    }

    @Override
    public void onStart() {
        super.onStart();
        inputOriginal.requestFocus();
        inputOriginal.postDelayed(() -> showKeyboard(getActivity(), inputOriginal), 100);
    }

    @Override
    public void onStop() {
        super.onStop();
        inputOriginal.postDelayed(() -> {
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null && imm.isActive()) imm.hideSoftInputFromWindow(inputOriginal.getWindowToken(), 0);
            }
        }, 110);
//        inputOriginal.clearFocus();
//        inputCurrency.clearFocus();
    }

    @OnClick(R.id.button_next)
    void onClickNext() {
        if (!TextUtils.isEmpty(inputOriginal.getText()) && isParsable(inputOriginal.getText().toString()) && Double.valueOf(inputOriginal.getText().toString()) != 0) {
            boolean invalid;
            if (switcher.isChecked()) {
                invalid = getFeePlusDonation() + CryptoFormatUtils.btcToSatoshi(inputOriginal.getText().toString()) > spendableSatoshi;
            } else {
                invalid = CryptoFormatUtils.btcToSatoshi(inputOriginal.getText().toString()) > spendableSatoshi;
            }

            if (invalid) {
                Toast.makeText(getActivity(), R.string.error_balance, Toast.LENGTH_LONG).show();
            } else {
                viewModel.setAmount(Double.valueOf(inputOriginal.getText().toString()));
                viewModel.signTransaction();
                if (AmountChooserFragment.this.getActivity() != null) {
                    ((AssetSendActivity) AmountChooserFragment.this.getActivity()).setFragment(R.string.send_summary, R.id.container, SendSummaryFragment.newInstance());
                } else {
                    viewModel.errorMessage.call();
                }
            }
        } else {
            Toast.makeText(getActivity(), R.string.choose_amount, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.image_swap)
    void onClickImageSwap() {
        if (isAmountSwapped) {
            inputOriginal.requestFocus();
        } else {
            inputCurrency.requestFocus();
        }
        Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_SWAP, viewModel.getChainId());
    }

    private void checkCommas() {
        String input = inputCurrency.getText().toString();
        if (!input.equals("") && input.contains(",")) {
            inputCurrency.setText(input.replaceAll(",", "."));
        }

        input = inputOriginal.getText().toString();
        if (!input.equals("") && input.contains(",")) {
            inputOriginal.setText(input.replaceAll(",", "."));
        }
    }

    @OnClick(R.id.text_max)
    void onClickMax() {
        if (spendableSatoshi - transactionPrice < 0) {
            return;
        }

        if (textMax.isSelected()) {
            textMax.setSelected(false);
        } else {
            textMax.setSelected(true);
        }
        switcher.setChecked(false);
        String maxSpendableBtc = CryptoFormatUtils.satoshiToBtc(spendableSatoshi);
        if (maxSpendableBtc.contains(",")) {
            maxSpendableBtc = maxSpendableBtc.replaceAll(",", ".");
        }
        inputOriginal.setText(maxSpendableBtc);
        inputCurrency.setText(CryptoFormatUtils.satoshiToUsd(spendableSatoshi));
//        inputCurrency.setText(NumberFormatter.getInstance().format(viewModel.getWallet().getBalance() * viewModel.getCurrenciesRate().getBtcToUsd()));
        textTotal.setText(CryptoFormatUtils.satoshiToUsd(spendableSatoshi) + " BTC");
        setTotalAmountWithWallet();
    }

    private void animateOriginalBalance() {
        containerInputOriginal.animate().scaleY(1.5f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        containerInputOriginal.animate().scaleX(1.5f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleY(1f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleX(1f).setInterpolator(new AccelerateInterpolator()).setDuration(300);
        inputOriginal.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_main));
        inputCurrency.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_grey));
        isAmountSwapped = false;
    }

    private void animateCurrencyBalance() {
        containerInputOriginal.animate().scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        containerInputOriginal.animate().scaleX(1f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleY(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        containerInputCurrency.animate().scaleX(1.5f).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(300);
        inputOriginal.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_grey));
        inputCurrency.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_main));
        isAmountSwapped = true;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupInputOriginal() {
        if (viewModel.getAmount() != 0) {
            inputOriginal.setText(NumberFormatter.getInstance().format(viewModel.getAmount()));
        }

        inputOriginal.setOnTouchListener((v, event) -> {
            inputOriginal.setSelection(inputOriginal.getText().length());
            if (!inputOriginal.hasFocus()) {
                inputOriginal.requestFocus();
                return true;
            }
            showKeyboard(getActivity(), v);
            Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_CRYPTO, viewModel.getChainId());
            return true;
        });

        inputOriginal.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateOriginalBalance();
                inputOriginal.setSelection(inputOriginal.getText().length());
                if (!TextUtils.isEmpty(inputOriginal.getText().toString())) {
                    setTotalAmountForInput();
                }
            }
        });

        inputOriginal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!isAmountSwapped) { // if currency input is main
                    if (!TextUtils.isEmpty(charSequence)) {
                        if (isParsable(charSequence.toString())) {
                            inputCurrency.setText(NumberFormatter.getFiatInstance().format(viewModel.getCurrenciesRate().getBtcToUsd() * Double.parseDouble(charSequence.toString())));
                            setTotalAmountForInput();
                        }
                    } else {
                        setEmptyTotalWithFee();
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
                    }
                }
                checkMaxLengthAfterPoint(inputOriginal, 9, i, i2);
                checkMaxLengthBeforePoint(inputOriginal, 6, i, i1, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkForPointAndZeros(editable.toString(), inputOriginal);

                long amountSatoshi = CryptoFormatUtils.btcToSatoshi(inputOriginal.getText().toString());
                if (amountSatoshi != -1) {
                    viewModel.scheduleUpdateTransactionPrice(amountSatoshi);
                }
            }
        });
    }

    private void setupInputCurrency() {
        if (viewModel.getAmount() != 0) {
            inputCurrency.setText(NumberFormatter.getFiatInstance().format(viewModel.getAmount() * currenciesRate.getBtcToUsd()));
        }

        inputCurrency.setOnTouchListener((v, event) -> {
            inputCurrency.setSelection(inputCurrency.getText().length());
            if (!inputCurrency.hasFocus()) {
                inputCurrency.requestFocus();
                return true;
            }
            showKeyboard(getActivity(), v);
            Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_FIAT, viewModel.getChainId());
            return true;
        });

        inputCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                animateCurrencyBalance();
                inputCurrency.setSelection(inputCurrency.getText().length());
                if (!TextUtils.isEmpty(inputCurrency.getText().toString())) {
                    setTotalAmountForInput();
                }
            }
        });

        inputCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (isAmountSwapped && currenciesRate != null) {
                    if (!TextUtils.isEmpty(charSequence)) {
                        if (isParsable(charSequence.toString())) {
                            inputOriginal.setText(NumberFormatter.getInstance()
                                    .format(Double.parseDouble(charSequence.toString()) / currenciesRate.getBtcToUsd()));
                            setTotalAmountForInput();
                        }
                    } else {
                        setEmptyTotalWithFee();
                        inputCurrency.getText().clear();
                        inputOriginal.getText().clear();
//                        textTotal.getEditableText().clear();
                    }
                }
                checkMaxLengthAfterPoint(inputCurrency, 3, i, i2);
                if (isAmountSwapped) {
                    checkMaxLengthBeforePoint(inputCurrency, 9, i, i1, i2);
                } else {
                    checkMaxLengthBeforePoint(inputCurrency, 10, i, i1, i2);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)
                        && editable.toString().length() == 0
                        && editable.toString().contains(".")) {
                    String result = editable.toString().replaceAll(".", "");
                    inputOriginal.setText(result);
                }
            }
        });
    }

    private void setupSwitcher() {
        switcher.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            viewModel.setPayForCommission(isChecked);
            if (isChecked) {
                checkCommas();
                if (!inputOriginal.getText().toString().equals("") && Double.parseDouble(inputOriginal.getText().toString()) * Math.pow(10, 8) + transactionPrice >= spendableSatoshi) {
                    viewModel.errorMessage.setValue("You reached spendable amount");
                    switcher.setChecked(false);
                }
                Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_COMMISSION_ENABLED, viewModel.getChainId());
            } else {
                Analytics.getInstance(getActivity()).logSendChooseAmount(AnalyticsConstants.SEND_AMOUNT_COMMISSION_DISABLED, viewModel.getChainId());
            }
            setTotalAmountForInput();
        });
    }

    /**
     * Sets total spending amount from wallet.
     * Checks if amount are set in original currency or usd, eur, etc.
     */
    private void setTotalAmountWithWallet() {
        if (isAmountSwapped) {
            textTotal.setText(NumberFormatter.getFiatInstance().format(String.valueOf(viewModel.getWallet().getBalance() * currenciesRate.getBtcToUsd())));
            textTotal.append(Constants.SPACE);
            textTotal.append(CurrencyCode.USD.name());
        } else {
            textTotal.setText(String.format("%s BTC", CryptoFormatUtils.satoshiToBtc(spendableSatoshi)));
        }
    }

    private void setTotalAmountForInput() {
        try {
            if (isAmountSwapped) {                                  // if currency input is main we set total balance in currency (usd, eur, etc.)
                if (!TextUtils.isEmpty(inputCurrency.getText())) {  // checks input for value to not parse null
                    if (switcher.isChecked()) {                     // if pay for commission is checked we add fee and donation to total amount
                        textTotal.setText(NumberFormatter.getFiatInstance()
                                .format(Double.parseDouble(inputCurrency.getText().toString()) + (Double.parseDouble(CryptoFormatUtils.satoshiToBtc(getFeePlusDonation())) * currenciesRate.getBtcToUsd())));
                    } else {                                        // if pay for commission is unchecked we add just value from input to total amount
                        textTotal.setText(NumberFormatter.getFiatInstance().format(Double.parseDouble(inputCurrency.getText().toString())));
                    }
                    textTotal.append(Constants.SPACE);
                    textTotal.append(CurrencyCode.USD.name());
                } else {
                    setEmptyTotalWithFee();
                }
            } else {
                if (!TextUtils.isEmpty(inputOriginal.getText())) { // checks input for value to not parse null
                    if (switcher.isChecked()) {                    // if pay for commission is checked we add fee and donation to total amount

                        textTotal.setText(NumberFormatter.getInstance()
                                .format(Double.parseDouble(inputOriginal.getText().toString()) + Double.parseDouble(CryptoFormatUtils.satoshiToBtc(getFeePlusDonation()))));
                    } else {                                       // if pay for commission is unchecked we add just value from input to total amount
                        textTotal.setText(inputOriginal.getText());
                    }
                    textTotal.append(Constants.SPACE);
                    textTotal.append(CurrencyCode.BTC.name());
                } else {
                    setEmptyTotalWithFee();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private long getFeePlusDonation() {
        return Long.valueOf(viewModel.getDonationSatoshi()) + transactionPrice;
    }

    private boolean isParsable(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setEmptyTotalWithFee() {
        if (switcher.isChecked()) {
            if (isAmountSwapped) {
                textTotal.setText(NumberFormatter.getFiatInstance().format(viewModel.getDonationAmount() == null ? 0 : Double.parseDouble(viewModel.getDonationAmount())
                        * currenciesRate.getBtcToUsd()));
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.USD.name());
            } else {
                textTotal.setText(NumberFormatter.getInstance().format(viewModel.getDonationAmount() == null ? 0 : Double.parseDouble(viewModel.getDonationAmount())));
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.BTC.name());
            }
        } else {
            textTotal.getEditableText().clear();
        }
    }

    private void setAmountTotalWithFee() {
        if (switcher.isChecked()) {
            if (isAmountSwapped) {
                textTotal.setText(NumberFormatter.getFiatInstance().format(((viewModel.getDonationAmount() == null ? 0 : Double.parseDouble(viewModel.getDonationAmount())))
                        * currenciesRate.getBtcToUsd()));
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.USD.name());
            } else {
                textTotal.setText(NumberFormatter.getInstance().format((viewModel.getDonationAmount() == null ? 0 : Double.parseDouble(viewModel.getDonationAmount()))));
                textTotal.append(Constants.SPACE);
                textTotal.append(CurrencyCode.BTC.name());
            }
        } else {
            inputOriginal.setText("");
            inputCurrency.setText("");
            textTotal.getEditableText().clear();
        }
    }

    private void checkForPointAndZeros(String input, EditText inputView) {
        int selection = inputView.getSelectionStart();
        if (!TextUtils.isEmpty(input) && input.length() == 1 && input.contains(".")) {
            String result = input.replaceAll(".", "0.");
            inputView.setText(result);
            inputView.setSelection(result.length());
        } else if (!TextUtils.isEmpty(input) && input.startsWith("00")) {
            inputView.setText(input.substring(1, input.length()));
            inputView.setSelection(selection - 1);
        }
    }

    private void checkMaxLengthBeforePoint(EditText input, int max, int start, int end, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.length() > max) {
            if (amount.contains(".")) {
                if (amount.indexOf(".") > max) {
                    if (start != 0 && end != amount.length() && count == amount.length()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(amount.substring(0, start));
                        stringBuilder.append(amount.substring(start + count, amount.length()));
                        input.setText(stringBuilder.toString());
                        if (start <= input.getText().length()) {
                            input.setSelection(start);
                        } else {
                            input.setSelection(input.getText().length());
                        }
                    } else {
                        input.setText(amount.substring(0, amount.length() - 1));
                        input.setSelection(input.getText().length());
                    }
                }
            } else {
                if (start != 0 && end != amount.length() && count == amount.length()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(amount.substring(0, start));
                    stringBuilder.append(amount.substring(start + count, amount.length()));
                    input.setText(stringBuilder.toString());
                    input.setSelection(start);
                } else {
                    input.setText(amount.substring(0, amount.length() - 1));
                    input.setSelection(input.getText().length());
                }
            }
        }
    }

    private void checkMaxLengthAfterPoint(EditText input, int max, int start, int count) {
        String amount = input.getText().toString();
        if (!TextUtils.isEmpty(amount) && amount.contains(".")) {
            if (amount.length() - amount.indexOf(".") > max) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(amount.substring(0, start));
                stringBuilder.append(amount.substring(start + count, amount.length()));
                input.setText(stringBuilder.toString());
                input.setSelection(start);
            }
        }
    }
}