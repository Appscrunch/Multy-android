/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.activities;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.samwolfand.oneprefs.Prefs;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.model.entities.BluetoothReceiver;
import io.multy.model.entities.ReceiveModel;
import io.multy.model.entities.wallet.Wallet;
import io.multy.storage.RealmManager;
import io.multy.ui.adapters.BlueoothDevicesPagerAdapter;
import io.multy.ui.adapters.WalletsPagerAdapter;
import io.multy.ui.fragments.main.FastOperationsFragment;
import io.multy.util.AnimationUtils;
import io.multy.util.BluetoothHelper;
import io.multy.util.Constants;
import io.multy.util.CryptoFormatUtils;
import io.multy.util.DevicesPagerTransformer;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;
import io.realm.RealmResults;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class FastOperationsActivity extends BaseActivity implements WalletsPagerAdapter.OnWalletClickListener{

    @BindView(R.id.bluetooth_image)
    PulsatorLayout rippleLayout;

    @BindView(R.id.wallets_view_pager)
    ViewPager walletsViewPager;

    @BindView(R.id.receivers_view_pager)
    ViewPager receiversViewPager;

    @BindView(R.id.controls)
    View group;

    @BindView(R.id.button_send)
    View buttonSend;

    @BindView(R.id.receivers_group)
    Group receiversGroup;

    @BindView(R.id.text_address)
    TextView address;

    @BindView(R.id.text_amount_receive)
    TextView amount;

    @BindView(R.id.text_amount)
    TextView sendAmount;

    @BindView(R.id.currency_amount)
    TextView currencyAmount;


    private Socket socket;
    private ScanCallback scanCallback;
    private boolean isDiscovering = false;
    private boolean isCollapsed = false;

    private WalletsPagerAdapter walletsPagerAdapter;
    private BlueoothDevicesPagerAdapter receivesAdapter;

    private ArrayList<BluetoothReceiver> bluetoothReceivers = new ArrayList<>();

    View.OnTouchListener onTouchListener;

    private Emitter.Listener onConnect = args -> runOnUiThread(() -> {});

    private Emitter.Listener onDisconnect = args -> runOnUiThread(() -> {});

    private Emitter.Listener onNewReceiver = args -> {

    };

    private float btnSendStartX;
    private float btnSendStartY;

    private BluetoothAdapter bluetoothAdapter;
    private AdvertiseCallback advertiseCallback;

    public static void show(Context context) {
        Intent intent = new Intent(context, FastOperationsActivity.class);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_fast_operations);
        ButterKnife.bind(this);
        rippleLayout.start();
        buttonSend.setVisibility(View.GONE);
        initReceiversViewPager();
        initViews();
        setupWalletsViewPager();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rippleLayout.animate().alpha(0.0f).setDuration(600).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        rippleLayout.setVisibility(View.GONE);
                        showReceiversGroup();
                    }
                });
            }
        }, 5000);
    }

    private void showReceiversGroup() {
        receiversGroup.animate().alpha(1.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                receiversGroup.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initViews() {
//        buttonSend.onTouchEvent(new MotionEvent());
        receiversViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateReceiverView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        receivesAdapter.addReceiver(new BluetoothReceiver("hsjdhdskajhdUhkjuhUUH", 1, 0.001, "15"));
        receivesAdapter.addReceiver(new BluetoothReceiver("gghjYGhghsjdhdsajhdUhkjuhUUH", 1, 0.301, "15"));
        receivesAdapter.addReceiver(new BluetoothReceiver("AvnAeefAdhsjdhdskajhdUhkjuhUUH", 1, 1.1, "15"));
        onTouchListener =  new View.OnTouchListener() {
            float dY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
//                        dX = buttonSend.getX() - event.getRawX();
                        dY = buttonSend.getY() - event.getRawY();
                        if (btnSendStartY == 0.0f) {
//                            btnSendStartX = v.getX();
                            btnSendStartY = v.getY();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        buttonSend.animate().y(event.getRawY() + dY - dp2px(80)).setDuration(0).start();

                        if (buttonSend.getVisibility() == View.GONE) {
                            buttonSend.setVisibility(View.VISIBLE);
                            break;
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        if (event.getRawY() < receiversViewPager.getY() + receiversViewPager.getHeight() && event.getRawY() > receiversViewPager.getY()) {
                            sendOperation();
                        }
                        buttonSend.animate().y(btnSendStartY).setDuration(0).start();
                        buttonSend.setVisibility(View.GONE);
                        group.setAlpha(1.0f);
                        walletsPagerAdapter.notifyDataSetChanged();
                        break;
                }
                return true;
            }
        };

    }

    private void updateReceiverView(int position) {
        amount.setText(receivesAdapter.getItemForPosition(position).getAmount() + " BTC");
        address.setText(receivesAdapter.getItemForPosition(position).getId());
    }

    private boolean isWalletsAvailable() {
        RealmResults<Wallet> wallets = RealmManager.getAssetsDao().getWallets();
        return wallets != null && wallets.size() > 0;
    }

    private void initReceiversViewPager() {
        receiversViewPager.setClipChildren(false);
        receivesAdapter = new BlueoothDevicesPagerAdapter(getSupportFragmentManager());
        receiversViewPager.setPageMargin(dp2px(50));
        receiversViewPager.setPageTransformer(false, new DevicesPagerTransformer(this));
        receiversViewPager.setAdapter(receivesAdapter);
    }

    private void setupWalletsViewPager() {
        walletsViewPager.setClipToPadding(false);

        walletsPagerAdapter = new WalletsPagerAdapter(getSupportFragmentManager(), this, onTouchListener);
        walletsViewPager.setAdapter(walletsPagerAdapter);
        walletsViewPager.setPageMargin(100);
    }

    @Override
    public void onWalletClick() {
        group.setAlpha(0.5f);
        buttonSend.setSelected(true);
        sendAmount.setText(String.valueOf(receivesAdapter.getItemForPosition(receiversViewPager.getCurrentItem()).getAmount()) + " BTC");
        currencyAmount.setText("54 USD");
        View currentView = walletsViewPager.getFocusedChild();
        currentView.animate().scaleX(1.3f).scaleY(1.3f).setDuration(300);
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

    //TODO create a logic of send
    private void sendOperation() {
        Toast.makeText(this, "Sending from " + walletsViewPager.getCurrentItem() + " wallet to " + receiversViewPager.getCurrentItem() + " receiver", Toast.LENGTH_SHORT).show();
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
        socket.on(Constants.EVENT_NEW_RECEIVER, onNewReceiver);
        socket.connect();
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
        startDiscoveryDevices();
    }

    private void startDiscoveryDevices(){

//        TODO
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                            socket.emit(Constants.EVENT_SENDER_ON, "", new Ack() {
                                @Override
                                public void call(Object... args) {

                                }
                            });




            }
        };

        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);

    }

    private int dp2px(float dipValue) {
        float m = getResources().getDisplayMetrics().density;
        return (int) (dipValue * m + 0.5f);
    }

}
