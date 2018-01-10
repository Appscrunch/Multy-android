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

    public WalletRealmObject getWalletById(int id) {
        return realm.where(WalletRealmObject.class).equalTo("walletIndex", id).findFirst();
    }

    public void removeWallet(int id) {
        WalletRealmObject wallet = getWalletById(id);
        realm.executeTransaction(realm -> wallet.deleteFromRealm());
    }
}
