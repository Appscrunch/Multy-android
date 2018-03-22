/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import java.util.List;

import io.multy.model.entities.wallet.BtcWallet;
import io.multy.model.entities.wallet.RecentAddress;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.util.NativeDataHelper;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class AssetsDao {

    private Realm realm;

    public AssetsDao(@NonNull Realm realm) {
        this.realm = realm;
    }

    public void saveWallet(Wallet wallet) {
        realm.executeTransaction(realm -> saveSingleWallet(wallet));
    }

    public void saveWallets(List<Wallet> wallets) {
        realm.executeTransaction(realm -> {
            for (Wallet wallet : wallets) {
                saveSingleWallet(wallet);
            }
        });
    }

    private void saveSingleWallet(Wallet wallet) {
        final int index = wallet.getIndex();
        final String name = wallet.getWalletName();
        final String balance = wallet.getBalance();

        Wallet savedWallet = getWalletById(index);
        if (savedWallet == null) {
            savedWallet = new Wallet();
        }
        savedWallet.setIndex(index);
        savedWallet.setWalletName(name);
        savedWallet.setBalance(balance);

        if (wallet.getCurrencyId() == NativeDataHelper.Blockchain.BTC.getValue()) {
            savedWallet.setBtcWallet(wallet.getBtcWallet().asRealmObject(realm));
        } else {
            //consider ETH here. will switch
            savedWallet.setEthWallet(wallet.getEthWallet().asRealmObject(realm));
        }

        realm.insertOrUpdate(savedWallet);
    }

    public RealmResults<Wallet> getWallets() {
        return realm.where(Wallet.class).findAll();
    }

    public RealmResults<Wallet> getWaleltByBlockchain(int blockChainId) {
        return realm.where(Wallet.class).equalTo("currencyid", blockChainId).findAll();
    }

    public void saveBtcAddress(int walletIndex, WalletAddress address) {
        realm.executeTransaction(realm -> {
            Wallet wallet = getWalletById(walletIndex);
            wallet.getBtcWallet().getAddresses().add(realm.copyToRealm(address));
            realm.insertOrUpdate(wallet);
        });
    }

    public void delete(@NonNull final RealmObject object) {
        realm.executeTransaction(realm -> object.deleteFromRealm());
    }

    public void deleteAll() {
        realm.executeTransaction(realm -> realm.where(Wallet.class).findAll().deleteAllFromRealm());
    }

    public Wallet getWalletById(int id) {
        return realm.where(Wallet.class).equalTo("index", id).findFirst();
    }

    public void updateWalletName(int id, String newName) {
        realm.executeTransaction(realm1 -> {
            Wallet wallet = getWalletById(id);
            wallet.setWalletName(newName);
            realm1.insertOrUpdate(wallet);
        });
    }

    public void removeWallet(int id) {
        realm.executeTransaction(realm -> {
            Wallet wallet = getWalletById(id);
            wallet.deleteFromRealm();
        });
    }

    public void saveRecentAddress(RecentAddress recentAddress) {
        realm.executeTransaction(realm -> realm.insertOrUpdate(recentAddress));
    }

    public RealmResults<RecentAddress> getRecentAddresses() {
        return realm.where(RecentAddress.class).findAll();
    }
}
