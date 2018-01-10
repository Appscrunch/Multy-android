/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.storage;

import android.content.Context;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;

import io.multy.encryption.CryptoUtils;
import io.multy.encryption.MasterKeyGenerator;
import io.multy.util.Constants;
import io.multy.util.EntropyProvider;

import static io.multy.encryption.CryptoUtils.sha;

public class SecurePreferencesHelper {

    public static void putString(Context context, String key, String value) {
        byte[] mk = MasterKeyGenerator.generateKey(context);
        final byte[] pass = Base64.encode(sha(sha(mk)), Base64.NO_WRAP);
        final String passPhrase = new String(pass);
        final byte[] salt = sha(mk);

        try {
            SecretKey secretKey = EntropyProvider.generateKey(passPhrase.toCharArray(), salt);
            final byte[] iv = Base64.decode(Prefs.getString(Constants.PREF_IV), Base64.NO_WRAP);
            final byte[] encryptedValue = CryptoUtils.encrypt(iv, secretKey.getEncoded(), value.getBytes());
            final String encodedString = new String(Base64.encode(encryptedValue, Base64.NO_WRAP));
            Prefs.putString(key, encodedString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Nullable
    public static String getString(Context context, String key) {
        byte[] mk = MasterKeyGenerator.generateKey(context);
        final byte[] pass = Base64.encode(sha(sha(mk)), Base64.NO_WRAP);
        final String passPhrase = new String(pass);
        final byte[] salt = sha(mk);
        try {
            SecretKey secretKey = EntropyProvider.generateKey(passPhrase.toCharArray(), salt);
            final byte[] iv = Base64.decode(Prefs.getString(Constants.PREF_IV), Base64.NO_WRAP);
            final String encryptedString = Prefs.getString(key);
            byte[] encrypted = Base64.decode(encryptedString, Base64.NO_WRAP);
            final String result = new String(CryptoUtils.decrypt(iv, secretKey.getEncoded(), encrypted));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static long getLong(Context context, String key) {
        String longString = getString(context, key);
        long result = 0;

        if (longString == null) {
            //TODO is not exist or something is broken
        }

        try {
            result = Long.parseLong(longString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
