/*
 *  Copyright 2017 Idealnaya rabota LLC
 *  Licensed under Multy.io license.
 *  See LICENSE for details
 */

package io.multy.util;


/**
 * Every method should throw JniException.
 * Also jni exception should be considered while typing new method inside JNI
 */
public class NativeDataHelper {

    static {
        System.loadLibrary("core_jnid");
    }

    public enum Currency {
        BITCOIN, ETHEREUM
    }

    public enum AddressType {
        ADDRESS_EXTERNAL,
        ADDRESS_INTERNAL
    }

    public static native String makeMnemonic() throws JniException;

    public static native byte[] makeSeed(String mnemonic) throws JniException;

    public static native byte[] makeAccountAddress(int index, int currency) throws JniException;

    public static native String makeAccountId(byte[] seed) throws JniException;

    public static native String makeTransaction() throws JniException;

    public static native int runTest();

}
