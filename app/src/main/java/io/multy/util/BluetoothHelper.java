/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;


import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;

import java.util.UUID;

import io.multy.storage.RealmManager;

public class BluetoothHelper {

    public static AdvertiseSettings buildAdvertiseSettings() {

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        settingsBuilder.setConnectable(false);
        return settingsBuilder.build();
    }

    public static AdvertiseData buildAdvertiseData() {
        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("00000000-0000-0000-0000-000000"+ RealmManager.getSettingsDao().getUserId().getUserId()));
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(pUuid);
        return dataBuilder.build();
    }

}
