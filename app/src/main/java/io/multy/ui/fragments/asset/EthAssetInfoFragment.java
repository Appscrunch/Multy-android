/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.asset;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samwolfand.oneprefs.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.events.TransactionUpdateEvent;
import io.multy.model.requests.HdTransactionRequestEntity;
import io.multy.model.responses.SingleWalletResponse;
import io.multy.storage.AssetsDao;
import io.multy.storage.RealmManager;
import io.multy.ui.activities.AssetActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.AssetTransactionsAdapter;
import io.multy.ui.adapters.EmptyTransactionsAdapter;
import io.multy.ui.adapters.EthTransactionsAdapter;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.BaseFragment;
import io.multy.ui.fragments.send.EthTransactionFeeFragment;
import io.multy.ui.fragments.send.SendSummaryFragment;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.WalletViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EthAssetInfoFragment extends BaseFragment implements AppBarLayout.OnOffsetChangedListener {

    public static final String TAG = AssetInfoFragment.class.getSimpleName();

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.text_value)
    TextView textBalanceOriginal;
    @BindView(R.id.text_amount)
    TextView textBalanceFiat;
    @BindView(R.id.text_wallet_name)
    TextView textWalletName;
    @BindView(R.id.text_address)
    TextView textAddress;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.group_empty_state)
    View groupEmptyState;
    @BindView(R.id.button_warn)
    FloatingActionButton buttonWarn;
    @BindView(R.id.container_available)
    View containerAvailableBalance;
    @BindView(R.id.text_available_value)
    TextView textAvailableValue;
    @BindView(R.id.text_available_fiat)
    TextView textAvailableFiat;
    @BindView(R.id.appbar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.container_info)
    View containerInfo;
    @BindView(R.id.card_addresses)
    View containerAddresses;

    private WalletViewModel viewModel;
    private AssetTransactionsAdapter transactionsAdapter;
    private AssetInfoFragment.SharingBroadcastReceiver receiver;

    public static EthAssetInfoFragment newInstance() {
        return new EthAssetInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transactionsAdapter = new AssetTransactionsAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_eth_asset_info, container, false);
        ButterKnife.bind(this, view);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletViewModel.class);
        setBaseViewModel(viewModel);
        receiver = new AssetInfoFragment.SharingBroadcastReceiver();
        viewModel.rates.observe(this, currenciesRate -> updateBalanceViews());
        viewModel.transactionUpdate.observe(this, transactionUpdateEntity -> {
            new Handler().postDelayed(this::refreshWallet, 300);
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_PULL, viewModel.getChainId());
            refreshWallet();
        });

        Wallet wallet = viewModel.getWallet(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
        viewModel.getWalletLive().observe(this, wallet1 -> EthAssetInfoFragment.this.setupWalletInfo(wallet1));
        viewModel.getWalletLive().setValue(wallet);
        Analytics.getInstance(getActivity()).logWalletLaunch(AnalyticsConstants.WALLET_SCREEN, viewModel.getChainId());

        hideAvailableAmount();
        return view;
    }

    private void refreshWallet() {
        swipeRefreshLayout.setRefreshing(false);
        viewModel.isLoading.postValue(true);

        if (!viewModel.getWalletLive().getValue().isValid()) {
            Wallet wallet = viewModel.getWallet(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            viewModel.getWalletLive().setValue(wallet);
        }

        final int walletIndex = viewModel.getWalletLive().getValue().getIndex();
        final int currencyId = viewModel.getWalletLive().getValue().getCurrencyId();
        final int networkId = viewModel.getWalletLive().getValue().getNetworkId();
        final long walletId = viewModel.getWalletLive().getValue().getId();

        MultyApi.INSTANCE.getWalletVerbose(walletIndex, currencyId, networkId).enqueue(new Callback<SingleWalletResponse>() {
            @Override
            public void onResponse(Call<SingleWalletResponse> call, Response<SingleWalletResponse> response) {
                viewModel.isLoading.postValue(false);
                if (response.isSuccessful() && response.body().getWallets() != null && response.body().getWallets().size() > 0) {
                    AssetsDao assetsDao = RealmManager.getAssetsDao();
                    assetsDao.saveWallet(response.body().getWallets().get(0));
                    viewModel.wallet.postValue(assetsDao.getWalletById(walletId));
                }

                updateBalanceViews();
                Wallet wallet = viewModel.wallet.getValue();
                requestTransactions(wallet.getCurrencyId(), wallet.getNetworkId(), wallet.getIndex());
            }

            @Override
            public void onFailure(Call<SingleWalletResponse> call, Throwable t) {
                viewModel.isLoading.setValue(false);
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().registerReceiver(receiver, new IntentFilter());
        }
        viewModel.subscribeSocketsUpdate();
        appBarLayout.addOnOffsetChangedListener(this);
        checkWarnVisibility();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() != null) {
            try {
                getActivity().unregisterReceiver(receiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        viewModel.unsubscribeSocketsUpdate();
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
//        final int total = appBarLayout.getTotalScrollRange();
//        final int offset = Math.abs(i);
//        final int percentage = offset == 0 ? 0 : offset / total;
//        containerInfo.setBackgroundColor((Integer) argbEvaluator.evaluate(colorOffset, colorTransparent, colorPrimaryDark));
        swipeRefreshLayout.setEnabled(i == 0);
//        containerInfo.setAlpha(1 - percentage);
    }

    private void initialize() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (transactionsAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setEnabled(false);
            recyclerView.setAdapter(new EmptyTransactionsAdapter());
            groupEmptyState.setVisibility(View.VISIBLE);
            setToolbarScrollFlag(0);
        } else {
            recyclerView.setAdapter(transactionsAdapter);
            swipeRefreshLayout.setEnabled(true);
            groupEmptyState.setVisibility(View.GONE);
            setToolbarScrollFlag(3);
        }

        checkWarnVisibility();
    }

    private void checkWarnVisibility() {
        if (Prefs.getBoolean(Constants.PREF_BACKUP_SEED)) {
            buttonWarn.setVisibility(View.GONE);
            buttonWarn.getLayoutParams().height = 0;
        }
    }

    private void setTransactionsState() {
        if (transactionsAdapter.getItemCount() == 0) {
            swipeRefreshLayout.setEnabled(false);
            recyclerView.setAdapter(new EmptyTransactionsAdapter());
            groupEmptyState.setVisibility(View.VISIBLE);
            setToolbarScrollFlag(0);
        } else {
            swipeRefreshLayout.setEnabled(true);
            groupEmptyState.setVisibility(View.GONE);
            setToolbarScrollFlag(3);
        }
    }

    private void setupWalletInfo(Wallet wallet) {
        initialize();
        requestTransactions(wallet.getCurrencyId(), wallet.getNetworkId(), wallet.getIndex());

        textWalletName.setText(wallet.getWalletName());
        textAddress.setText(wallet.getActiveAddress().getAddress()); //need test this so don't remove commented code below

        containerAddresses.setVisibility(viewModel.wallet.getValue().getCurrencyId() != NativeDataHelper.Blockchain.BTC.getValue() ?
                View.GONE : View.VISIBLE);

//        if (wallet.getAddresses() != null && !wallet.getAddresses().isEmpty()) {
//            textAddress.setText(wallet.getAddresses().get(wallet.getAddresses().size() - 1).getAddress());
//        } else {
//            textAddress.setText(wallet.getCreationAddress());
//        }

        updateBalanceViews();
    }

    private void updateBalanceViews() {
        Wallet wallet = viewModel.getWalletLive().getValue();
        if (!wallet.isValid()) {
            return;
        }

        final long balance = wallet.getBalanceNumeric().longValue();
        final long availableBalance = wallet.getAvailableBalanceNumeric().longValue();

//        if (balance == availableBalance) {
//            hideAvailableAmount();
//        } else {
//            showAvailableAmount();
//        }

        textAvailableFiat.setText(wallet.getAvailableFiatBalanceLabel());
        textBalanceFiat.setText(wallet.getFiatBalanceLabel());

        textBalanceOriginal.setText(wallet.getBalanceLabelTrimmed());
        textAvailableValue.setText(wallet.getAvailableBalanceLabel());
    }

    private void showAvailableAmount() {
        containerAvailableBalance.setVisibility(View.VISIBLE);
    }

    private void hideAvailableAmount() {
        containerAvailableBalance.setVisibility(View.GONE);
    }

    private void requestTransactions(final int currencyId, final int networkId, final int walletIndex) {
        viewModel.getTransactionsHistory(currencyId, networkId, walletIndex).observe(this, transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                try {
                    transactionsAdapter.setTransactions(transactions);
                    recyclerView.setAdapter(new EthTransactionsAdapter(transactions, viewModel.wallet.getValue().getId()));
                    groupEmptyState.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            setTransactionsState();
        });
    }

    private void setToolbarScrollFlag(int flag) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        params.setScrollFlags(flag);
    }

    private String getAddressToShare() {
        return viewModel.wallet.getValue().getActiveAddress().getAddress();
    }

    @OnClick(R.id.options)
    void onClickOptions() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SETTINGS, viewModel.getChainId());
        ((AssetActivity) getActivity()).setFragment(R.id.container_full, AssetSettingsFragment.newInstance());
    }

    @OnClick(R.id.card_addresses)
    void onClickAddress() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESSES, viewModel.getChainId());
        if (viewModel.getWalletLive().getValue() != null) {
            ((AssetActivity) getActivity()).setFragment(R.id.container_full,
                    AddressesFragment.newInstance(viewModel.getWalletLive().getValue().getId()));
        } else {
            Wallet wallet = viewModel.getWallet(getActivity().getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, 0));
            ((AssetActivity) getActivity()).setFragment(R.id.container_full,
                    AddressesFragment.newInstance(wallet.getId()));
        }
    }

    @OnClick(R.id.button_share)
    void onClickShare() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_SHARE, viewModel.getChainId());
        viewModel.share(getActivity(), getAddressToShare());
    }

    @OnClick(R.id.text_address)
    void onClickCopy() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_ADDRESS, viewModel.getChainId());
        viewModel.copyToClipboard(getActivity(), getAddressToShare());
    }

    @OnClick(R.id.close)
    void onClickClose() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
        getActivity().finish();
    }

    @OnClick(R.id.button_warn)
    void onClickWarn() {
        Analytics.getInstance(getActivity()).logWalletBackup(AnalyticsConstants.WALLET_BACKUP_SEED);
        startActivity(new Intent(getActivity(), SeedActivity.class));
    }

    @OnClick(R.id.text_value)
    void onClickBalance() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE, viewModel.getChainId());
    }

    @OnClick(R.id.text_coin)
    void onClickBalanceCurrency() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE, viewModel.getChainId());
    }

    @OnClick(R.id.text_amount)
    void onClickBalanceFiat() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE_FIAT, viewModel.getChainId());
    }

    @OnClick(R.id.text_money)
    void onClickBalanceFiatCurrency() {
        Analytics.getInstance(getActivity()).logWallet(AnalyticsConstants.WALLET_BALANCE_FIAT, viewModel.getChainId());
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTransactionUpdateEvent(TransactionUpdateEvent event) {
        refreshWallet();
    }

    public static class SharingBroadcastReceiver extends BroadcastReceiver {

        public SharingBroadcastReceiver() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null && intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT) != null) {
                String component = intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT).toString();
                String packageName = component.substring(component.indexOf("{") + 1, component.indexOf("/"));
                Analytics.getInstance(context).logWalletSharing(intent.getIntExtra(context.getString(R.string.chain_id), 1), packageName);
            }
        }
    }

    private void sendEth() {
        Wallet wallet = viewModel.getWalletLive().getValue();
        try {
            byte[] tx = NativeDataHelper.makeTransactionETH(RealmManager.getSettingsDao().getSeed().getSeed(), wallet.getIndex(), 0, wallet.getCurrencyId(), wallet.getNetworkId(),
                    String.valueOf(wallet.getAddresses().get(0).getAmount()), "10000000000000000", "81afbff278e84f4d4b80d4a1d04aecfc54fa0253", "21000", "5000000000", "1");
            MultyApi.INSTANCE.sendHdTransaction(new HdTransactionRequestEntity(wallet.getCurrencyId(), wallet.getNetworkId(),
                    new HdTransactionRequestEntity.Payload("", 0, wallet.getIndex(), "0x"+SendSummaryFragment.byteArrayToHex(tx), false))).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.i("wise", response.isSuccessful() + " success");
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
            String txString = SendSummaryFragment.byteArrayToHex(tx);
            Log.i("wise", "tx=" + txString);
        } catch (JniException e) {
            e.printStackTrace();
        }
    }
}
