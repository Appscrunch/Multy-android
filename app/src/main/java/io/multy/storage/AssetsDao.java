/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;

import io.multy.Multy;
import io.multy.encryption.MasterKeyGenerator;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.multy.util.MyRealmMigration;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import java.util.List;

import io.multy.model.entities.wallet.WalletAddress;
import io.multy.model.entities.wallet.WalletRealmObject;
import io.reactivex.annotations.NonNull;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;


public class AssetsDao {

    private Realm realm;

    public AssetsDao() {
        try {
            realm = Realm.getInstance(getRealmConfiguration(Multy.getContext()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private RealmConfiguration getRealmConfiguration(Context context) throws Exception {
        if (MasterKeyGenerator.generateKey(context) != null) {
            return new RealmConfiguration.Builder()
                    .encryptionKey(MasterKeyGenerator.generateKey(context))
                    .schemaVersion(2)
                    .migration(new MyRealmMigration())
                    .build();
        } else {
            return new RealmConfiguration.Builder().build();
        }
    }

//     public AssetsDao(@NonNull Realm realm) {
//         this.realm = realm;
//     }

    public void saveWallet(WalletRealmObject wallet) {
        realm.executeTransaction(realm -> saveSingleWallet(wallet));
    }

    public void saveWallets(List<WalletRealmObject> wallets) {
        realm.executeTransaction(realm -> {
            for (WalletRealmObject wallet : wallets) {
                saveSingleWallet(wallet);
            }
        });
    }

    private void saveSingleWallet(WalletRealmObject wallet) {
        final int index = wallet.getWalletIndex();
        final String name = wallet.getName();
        final double balance = wallet.calculateBalance();
        final double pendingBalance = wallet.calculatePendingBalance();

        WalletRealmObject savedWallet = getWalletById(index);
        if (savedWallet == null) {
            savedWallet = new WalletRealmObject();
            savedWallet.setWalletIndex(index);
        }
        savedWallet.setName(name);
        savedWallet.setAddresses(new RealmList<>());
        for (WalletAddress walletAddress : wallet.getAddresses()) {
            savedWallet.getAddresses().add(realm.copyToRealm(walletAddress));
        }
        savedWallet.setBalance(balance);
        savedWallet.setPendingBalance(pendingBalance);
        realm.insertOrUpdate(savedWallet);
    }

    public RealmResults<WalletRealmObject> getWallets() {
        return realm.where(WalletRealmObject.class).findAll();
    }

    public void saveAddress(WalletRealmObject wallet, WalletAddress address) {
        realm.executeTransaction(realm -> {
            wallet.getAddresses().add(address);
            realm.insertOrUpdate(wallet);
        });

    }

    public WalletRealmObject getWalletById(int id) {
        return realm.where(WalletRealmObject.class).equalTo("walletIndex", id).findFirst();
    }

    public void removeWallet(int id) {
        WalletRealmObject wallet = getWalletById(id);
        realm.executeTransaction(realm -> wallet.deleteFromRealm());
    }

    public void delete(@NonNull final RealmObject object) {
        realm.executeTransaction(realm -> object.deleteFromRealm());
    }

    public void deleteAll() {
        realm.executeTransaction(realm -> realm.where(WalletRealmObject.class).findAll().deleteAllFromRealm());
    }
}
