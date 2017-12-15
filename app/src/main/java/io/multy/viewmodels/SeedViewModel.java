/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.viewmodels;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import io.multy.Multy;
import io.multy.model.DataManager;
import io.multy.model.entities.ByteSeed;
import io.multy.model.entities.DeviceId;
import io.multy.model.entities.Mnemonic;
import io.multy.model.entities.UserId;
import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by andre on 08.11.2017.
 */

public class SeedViewModel extends BaseViewModel {

    public MutableLiveData<ArrayList<String>> phrase = new MutableLiveData<>();
    public MutableLiveData<Integer> position = new MutableLiveData<>();
    public MutableLiveData<Boolean> failed = new MutableLiveData<>();
    public MutableLiveData<byte[]> binarySeed = new MutableLiveData<>();

    public void initData() {
        final int wordsPerOnce = 3;
        DataManager dataManager = new DataManager(Multy.getContext());
        final String mnemonic = dataManager.getMnemonic().getMnemonic();
        final ArrayList<String> phrase = new ArrayList<>(Arrays.asList(mnemonic.split(" ")));
        final ArrayList<String> phraseToShow = new ArrayList<>(phrase.size() / wordsPerOnce);
        final byte[] seed = dataManager.getSeed().getSeed();

        for (int i = 0; i < phrase.size(); i += wordsPerOnce) {
            Timber.i("words %s", TextUtils.join("\n", phrase.subList(i, i + 3)));
            phraseToShow.add(TextUtils.join("\n", phrase.subList(i, i + 3)));
        }

        position.setValue(0);
        binarySeed.setValue(seed);
        failed.setValue(false);
        this.phrase.setValue(phraseToShow);
    }

    public void restore(String phrase, Context context, Runnable callback){
        try {
            Timber.e("phrase %s", phrase);
            byte[] seed = NativeDataHelper.makeSeed(phrase);
            String userId = NativeDataHelper.makeAccountId(seed);
            final String deviceId = Settings.Secure.ANDROID_ID;
            DataManager dataManager = new DataManager(context);
            dataManager.deleteDatabase();
            dataManager.saveSeed(new ByteSeed(seed));
            dataManager.saveUserId(new UserId(userId));
            dataManager.setDeviceId(new DeviceId(deviceId));

            new DataManager(context).restore()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(walletList -> {
                        failed.setValue(false);
                        callback.run();
                    }, throwable -> {
                        failed.setValue(true);
                        Toast.makeText(context, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        throwable.printStackTrace();
                        callback.run();
                    });

        } catch (JniException e) {
            failed.setValue(true);
            callback.run();
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
