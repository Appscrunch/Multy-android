/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model;

import android.content.Context;

import java.util.List;

import io.multy.api.MultyApi;
import io.multy.model.entities.Wallet;
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.storage.DatabaseHelper;
import io.reactivex.Observable;

/**
 * Created by Ihar Paliashchuk on 10.11.2017.
 * ihar.paliashchuk@gmail.com
 */

public class DataManager {

    private DatabaseHelper database;

    public DataManager(Context context) {
        this.database = new DatabaseHelper(context);
    }

    public List<Wallet> getWallets(){
       return database.getWallets();
    }

    public void saveRequestWallet(Wallet wallet){
//        database.saveWallets();
    }

    public void auth(String userId, String deviceId, String password){
        MultyApi.INSTANCE.auth(userId, deviceId, password);
    }

    public Observable<ExchangePriceResponse> getExchangePrice(String originalCurrency, String currency){
        return MultyApi.INSTANCE.getExchangePrice(originalCurrency, currency);
    }
}
