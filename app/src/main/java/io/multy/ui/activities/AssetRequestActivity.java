/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.List;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.ReceiveModel;
import io.multy.ui.fragments.AddressesFragment;
import io.multy.ui.fragments.receive.AmountChooserFragment;
import io.multy.ui.fragments.receive.RequestSummaryFragment;
import io.multy.ui.fragments.receive.WalletChooserFragment;
import io.multy.util.BluetoothHelper;
import io.multy.util.Constants;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.multy.viewmodels.AssetRequestViewModel;


public class AssetRequestActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindInt(R.integer.zero)
    int zero;
    @BindInt(R.integer.one_negative)
    int oneNegative;

    private Socket socket;
    private AdvertiseCallback advertiseCallback;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isReceive = false;

    private boolean isFirstFragmentCreation;
    private AssetRequestViewModel viewModel;

    private Emitter.Listener onConnect = args -> runOnUiThread(() -> {});

    private Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {});

    private Emitter.Listener onPaymentSend = args -> {
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_request);
        ButterKnife.bind(this);
        isFirstFragmentCreation = true;
        viewModel = ViewModelProviders.of(this).get(AssetRequestViewModel.class);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        startFlow();
    }

    private void initBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        advertiseCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }
            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);

            }
        };
        bluetoothAdapter.enable();
    }

    public void receiveByBluetooth() {
        BluetoothLeAdvertiser advertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (!isReceive) {
            AdvertiseSettings settings = BluetoothHelper.buildAdvertiseSettings();
            AdvertiseData advertiseData = BluetoothHelper.buildAdvertiseData();
            advertiser.startAdvertising(settings, advertiseData, advertiseCallback);
            ReceiveModel receiveModel = new ReceiveModel(viewModel.getWalletAddress(), viewModel.getAmount());
            socket.emit(Constants.EVENT_RECEIVER_ON, new Gson().toJson(receiveModel));
            isReceive = true;
        }

    }

    private void initializeSockets(){
        try {
            IO.Options options = new IO.Options();
            options.path = "/socket.io";
            options.transports = new String[] { WebSocket.NAME };
            socket = IO.socket(Constants.BASE_URL, options);
        } catch (URISyntaxException e){
            Log.e("SOCKET","Wrong url:"+e);
        }


        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Constants.EVENT_PAYMENT_SEND, onPaymentSend);
        socket.connect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > zero) {
            List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
            for (Fragment backStackFragment : backStackFragments) {
                if (backStackFragment instanceof AddressesFragment) {
                    toolbar.setTitle(R.string.receive_summary);
                }
                if (backStackFragment instanceof AmountChooserFragment) {
                    toolbar.setTitle(R.string.receive_summary);
                }
                if (backStackFragment instanceof RequestSummaryFragment) {
                    toolbar.setTitle(R.string.receive);
                }
                if (backStackFragment instanceof WalletChooserFragment) {
                    toolbar.setTitle(R.string.receive_summary);
                }
            }
        }
        logCancel();
        super.onBackPressed();
    }

    @OnClick(R.id.button_cancel)
    void ocLickCancel() {
        logCancel();
        finish();
    }

    private void startFlow() {
        if (getIntent().hasExtra(Constants.EXTRA_WALLET_ID)) {
            if (getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, -1) != -1) {
                AssetRequestViewModel viewModel = ViewModelProviders.of(this).get(AssetRequestViewModel.class);
//                viewModel.setContext(this);
                viewModel.getWallet(getIntent().getLongExtra(Constants.EXTRA_WALLET_ID, -1));
                viewModel.getWalletLive().observe(this, walletRealmObject -> setFragment(R.string.receive_summary, RequestSummaryFragment.newInstance()));
            } else {
                Toast.makeText(this, "Invalid wallet index", Toast.LENGTH_SHORT).show();
            }
        } else {
            setFragment(R.string.receive, WalletChooserFragment.newInstance());
        }
    }

    public void setFragment(@StringRes int title, Fragment fragment) {
        toolbar.setTitle(title);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment);

        if (!isFirstFragmentCreation) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        isFirstFragmentCreation = false;
        transaction.commit();
    }

    private void logCancel() {
        List<Fragment> backStackFragments = getSupportFragmentManager().getFragments();
        for (Fragment backStackFragment : backStackFragments) {
            if (backStackFragment instanceof AddressesFragment) {
//                    Analytics.getInstance(this).logWalletAddresses(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            }
            if (backStackFragment instanceof RequestSummaryFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logReceiveSummary(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            }
            if (backStackFragment instanceof WalletChooserFragment && backStackFragment.isVisible()) {
                Analytics.getInstance(this).logReceive(AnalyticsConstants.BUTTON_CLOSE, viewModel.getChainId());
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        initializeSockets();
        initBluetooth();
    }

    @Override
    protected void onPause(){
        super.onPause();

        stopDiscovering();
        closeSockets();
    }

    private void closeSockets(){
        if (socket!=null){
            socket.disconnect();
            socket.off(Constants.CONNECTED);
            socket.off(Constants.NEW_SENDER);
            socket.off(Constants.EVENT_PAYMENT_SEND);
        }
    }

    private void stopDiscovering(){
        if (bluetoothAdapter != null){
            bluetoothAdapter.cancelDiscovery();

//            bluetoothAdapter.getBluetoothLeScanner().stopScan(advertiseCallback);
        }
    }


}
