/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.ui.fragments.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import com.db.chart.animation.Animation;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.samwolfand.oneprefs.Prefs;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.api.MultyApi;
import io.multy.model.DataManager;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.WalletsResponse;
import io.multy.ui.activities.CreateAssetActivity;
import io.multy.ui.activities.SeedActivity;
import io.multy.ui.adapters.WalletsAdapter;
import io.multy.ui.fragments.BaseFragment;
import io.multy.util.Constants;
import io.multy.util.FirstLaunchHelper;
import io.multy.util.JniException;
import io.multy.viewmodels.AssetsViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssetsFragment extends BaseFragment {

    public static final String TAG = AssetsFragment.class.getSimpleName();

    @BindView(R.id.recycler_wallets)
    RecyclerView recyclerWallets;
    @BindView(R.id.group_wallets_list)
    Group groupWalletsList;
    @BindView(R.id.group_create_description)
    Group groupCreateDescription;
    @BindView(R.id.button_add)
    FloatingActionButton buttonAdd;
    @BindView(R.id.container_create_restore)
    ConstraintLayout containerCreateRestore;
    @BindView(R.id.chart)
    LineChartView chartView;
    @BindView(R.id.logo)
    ImageView logo;

    private AssetsViewModel viewModel;
    private WalletsAdapter walletsAdapter;

    public static AssetsFragment newInstance() {
        return new AssetsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<WalletRealmObject> wallets = new ArrayList<>();
        WalletRealmObject wallet = new DataManager(getActivity()).getWallet();
        if (wallet != null) {
            wallets.add(wallet);
        }
        walletsAdapter = new WalletsAdapter(wallets);
        walletsAdapter = new WalletsAdapter(new ArrayList<>());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);
        subscribeViewModel();
        ButterKnife.bind(this, view);
        initialize();
        viewModel = ViewModelProviders.of(getActivity()).get(AssetsViewModel.class);
        viewModel.setContext(getActivity());

        if (new DataManager(getActivity()).getUserId() != null) {
            viewModel.rates.observe(this, currenciesRate -> walletsAdapter.updateRates(currenciesRate));
            viewModel.init(getLifecycle());
            viewModel.graphPoints.observe(this, graphPoints -> {

                float[] values = new float[graphPoints.size()];
                String[] stamps = new String[graphPoints.size()];
                Date date;
                SimpleDateFormat format = new SimpleDateFormat("kk", Locale.getDefault());

                for (int i = 0; i < graphPoints.size(); i++) {
                    values[i] = graphPoints.get(i).getPrice();
                    if (i % 20 == 0 || i == graphPoints.size() - 1) {
                        Log.d(TAG, "unix " + graphPoints.get(i).getDate());
                        date = new Date(graphPoints.get(i).getDate() * 1000L);
                        stamps[i] = format.format(date);
                    } else {
                        stamps[i] = "";
                    }
                }

                Log.v(TAG, "graph size " + values.length);
                Log.i(TAG, "graph values " + Arrays.toString(values));
                logo.animate().alpha(0.0f).setDuration(100).start();

                LineSet lineSet = new LineSet(stamps, values)
                        .setColor(getResources().getColor(R.color.colorPrimaryDark))
                        .setThickness(2)
                        .setGradientFill(new int[]{Color.parseColor("#FF007AFF"), Color.parseColor("#FF00B2FF")}, new float[]{0, 100})
                        .setSmooth(true)
                        .beginAt(0);
                chartView.reset();
                chartView.addData(lineSet);
                chartView.setAxisBorderValues(getMinValue(values), getMaxValue(values));
                chartView.show(new Animation().setInterpolator(new BounceInterpolator()).fromAlpha(0));

            });
        }
        return view;
    }

    public static float getMaxValue(float[] array) {
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > maxValue) {
                maxValue = array[i];
            }
        }
        return maxValue;
    }

    public static float getMinValue(float[] array) {
        float minValue = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] < minValue) {
                minValue = array[i];
            }
        }
        return minValue;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel.isFirstStart()) {
            groupWalletsList.setVisibility(View.GONE);
            containerCreateRestore.setVisibility(View.VISIBLE);
        } else {
            groupWalletsList.setVisibility(View.VISIBLE);
            containerCreateRestore.setVisibility(View.GONE);
            groupCreateDescription.setVisibility(View.GONE);
        }

        walletsAdapter.setData(viewModel.getWalletsFromDB());
        if (Prefs.contains(Constants.PREF_IS_FIRST_START)) {
            updateWallets();
        }
    }

    private void updateWallets() {
        DataManager dataManager = new DataManager(getActivity());
        MultyApi.INSTANCE.getWalletsVerbose().enqueue(new Callback<WalletsResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletsResponse> call, @NonNull Response<WalletsResponse> response) {
                if (response.body() != null && response.body().getWallets() != null) {
                    for (WalletRealmObject wallet : response.body().getWallets()) {
                        dataManager.updateWallet(wallet.getWalletIndex(), wallet.getAddresses(), wallet.calculateBalance(), wallet.calculatePendingBalance());
                    }
                }
            }

            @Override
            public void onFailure(Call<WalletsResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onPause() {
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager()
                .findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onPause();
    }

    private void initialize() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerWallets.setLayoutManager(layoutManager);
        recyclerWallets.setNestedScrollingEnabled(false);
        setAdapter();
    }

    private void subscribeViewModel() {
        viewModel = ViewModelProviders.of(this).get(AssetsViewModel.class);
    }

    private void setAdapter() {
        recyclerWallets.setAdapter(walletsAdapter);
    }

    private void showAddWalletActions() {
        WalletActionsDialog.Callback callback = new WalletActionsDialog.Callback() {
            @Override
            public void onCardAddClick() {
                onWalletAddClick();
            }

            @Override
            public void onCardImportClick() {
                onWalletImportClick();
            }
        };
        WalletActionsDialog dialog = (WalletActionsDialog) getChildFragmentManager().findFragmentByTag(WalletActionsDialog.TAG);
        if (dialog == null) {
            dialog = WalletActionsDialog.newInstance(callback);
        }
        dialog.show(getChildFragmentManager(), WalletActionsDialog.TAG);
    }

    private void onWalletImportClick() {

    }

    private void onWalletAddClick() {
        startActivity(new Intent(getContext(), CreateAssetActivity.class));
    }

    @OnClick(R.id.button_add)
    void onPlusClick() {
        showAddWalletActions();
    }

    @OnClick(R.id.button_create)
    void onCLickCreateWallet() {
        groupCreateDescription.setVisibility(View.VISIBLE);
        groupWalletsList.setVisibility(View.VISIBLE);
        containerCreateRestore.setVisibility(View.GONE);
        Prefs.putBoolean(Constants.PREF_IS_FIRST_START, false);

        try {
            FirstLaunchHelper.setCredentials(null, getActivity());
        } catch (JniException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.button_restore)
    void onCLickRestoreSeed() {
        groupCreateDescription.setVisibility(View.GONE);
        groupWalletsList.setVisibility(View.VISIBLE);
        startActivity(new Intent(getActivity(), SeedActivity.class).addCategory(Constants.EXTRA_RESTORE));
        Prefs.putBoolean(Constants.PREF_IS_FIRST_START, false);
    }
}
