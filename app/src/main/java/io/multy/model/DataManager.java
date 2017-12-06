/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.samwolfand.oneprefs.Prefs;

import java.util.List;

import io.multy.api.MultyApi;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.RootKey;
import io.multy.model.entities.Token;
import io.multy.model.entities.UserId;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.model.responses.AuthResponse;
import io.multy.model.responses.ExchangePriceResponse;
import io.multy.storage.DatabaseHelper;
import io.multy.util.Constants;
import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        MultyApi.INSTANCE.auth(userId, deviceId, password).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                Prefs.putString(Constants.PREF_AUTH, response.body().getToken());
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
            }
        });;
    }

    public Observable<ExchangePriceResponse> getExchangePrice(String originalCurrency, String currency){
        return MultyApi.INSTANCE.getExchangePrice(originalCurrency, currency);
    }

    public void saveRootKey(RootKey key) {
        database.saveRootKey(key);
    }

    public RootKey getRootKey() {
        return database.getRootKey();
    }

    public void saveToken(Token token) {
        database.saveToken(token);
    }

    public Token getToken() {
        return database.getToken();
    }

    public void saveUserId(UserId userId) {
        database.saveUserId(userId);
    }

    public UserId getUserId() {
        return database.getUserId();
    }

    public void saveSeed(ByteSeed seed) {
        database.saveSeed(seed);
    }

    public ByteSeed getSeed() {
        return database.getSeed();
    }

    public void saveWallet(WalletRealmObject wallet) {
        database.saveWallet(wallet);
    }

    public WalletRealmObject getWallet() {
        return database.getWallet();
    }

    public void setMnemonic(Mnemonic mnemonic) {
        database.setMnemonic(mnemonic);
    }

    public Mnemonic getMnemonic() {
        return database.getMnemonic();
    }

}
