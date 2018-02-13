/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util.analytics;


public class AnalyticsConstants {

    // First Launch
    public static final String FIRST_LAUNCH = "Screen First Launch";
    public static final String FIRST_LAUNCH_CREATE_WALLET = "Button Create First Wallet - First Launch SC";
    public static final String FIRST_LAUNCH_RESTORE_WALLET = "Button Restore Multy - First Launch SC";

    // Create Wallet Screen
    public static final String CREATE_WALLET = "Screen Create Wallet";
    public static final String CREATE_WALLET_CREATE = "Button Create Wallet - Create Wallet SC";
    public static final String CREATE_WALLET_CHAIN = "Button Chain Id - Create Wallet SC";
    public static final String CREATE_WALLET_FIAT_CLICKED = "Button Fiat Id - Create Wallet SC";
    public static final String CREATE_WALLET_FIAT_SELECTED = "Event Fiat Id $FIAT_ID Selected"; // ?
    public static final String CREATE_WALLET_CANCEL = "Button Cancel - Create Wallet SC";

    // View Seed Phrase Screen
    public static final String SEED_PHRASE = "Screen View Phrase";
    public static final String SEED_PHRASE_CLOSE = "Button Close - View Phrase SC";
    public static final String SEED_PHRASE_REPEAT = "Button Repeat Seed - View Phrase SC";

    // Restore Seed
    public static final String SEED_PHRASE_RESTORE = "Screen Restore Seed";
    public static final String SEED_PHRASE_RESTORE_CANCEL = "Button Cancel - Restore Seed SC";

    // Success Restore Seed
    public static final String SEED_PHRASE_RESTORE_SUCCESS = "Screen Success Restore Seed";
    public static final String SEED_PHRASE_RESTORE_SUCCESS_CANCEL = "Button Cancel - Success Restore Seed SC";
    public static final String SEED_PHRASE_RESTORE_SUCCESS_OK = "Button Great - Success Restore Seed SC";
    public static final String SEED_PHRASE_RESTORE_SUCCESS_BACKUP = "Event Seed Backuped";

    // Fail Restore Seed
    public static final String SEED_PHRASE_RESTORE_FAIL = "Screen Fail Restore Seed";
    public static final String SEED_PHRASE_RESTORE_FAIL_CANCEL = "Button Cancel - Fail Restore Seed SC";
    public static final String SEED_PHRASE_RESTORE_TRY_AGAIN = "Button Try again - Fail Restore Seed SC";
    public static final String SEED_PHRASE_RESTORE_FAIL_BACKUP = "Event Seed Backup Failed";

    // Main Screen
    public static final String MAIN_SCREEN = "Screen Main";
    public static final String MAIN_SCREEN_CLOSE = "Button Close - Main SC";
    public static final String TAB_MAIN = "Tab Main - Main SC";
    public static final String TAB_ACTIVITY = "Tab Activity - Main SC";
    public static final String TAB_CONTACTS = "Tab Contacts - Main SC";
    public static final String TAB_SETTINGS = "Tab Settings - Main SC";
    public static final String MAIN_FAST_OPERATIONS = "Button Fast Operations - Main SC";
    public static final String MAIN_CREATE_WALLET = "Button Create Wallet - Main SC";
    public static final String MAIN_LOGO = "Button Logo - Main SC";
    public static final String MAIN_PULL_WALLETS = "Pull Wallets - Main SC";
    public static final String MAIN_WALLET_CLICK = "Button Wallet Open - $CHAIN_ID - Main SC"; // ?
    public static final String MAIN_BACKUP_SEED = "Button Backup Seed - Main SC";

    // Activity Screen
    public static final String ACTIVITY_SCREEN = "Screen Activity";
    public static final String ACTIVITY_SCREEN_CLOSE = "Button Close - Activity SC";

    // Fast Operation Screen
    public static final String FAST_OPERATIONS = "Screen Fast Operation";
    public static final String FAST_OPERATIONS_CLOSE = "Button Close - Fast Operation SC";
    public static final String FAST_OPERATIONS_SEND = "Button Send - Fast Operation SC";
    public static final String FAST_OPERATIONS_RECEIVE = "Button Receive - Fast Operation SC";
    public static final String FAST_OPERATIONS_NFC = "Button NFC - Fast Operation SC";
    public static final String FAST_OPERATIONS_SCAN = "Button Scan - Fast Operation SC";
    public static final String FAST_OPERATIONS_PERMISSION_GRANTED = "Button Scan Got Permission - Fast Operation SC";
    public static final String FAST_OPERATIONS_PERMISSION_DENIED = "Button Scan Denied Permission - Fast Operation SC";

    // QR Screen
    public static final String QR_SCREEN = "Screen QR";
    public static final String QR_SCREEN_CLOSE = "Button Close - QR SCC";

    // Wallet Screen
    public static final String WALLET_SCREEN = "Screen Wallet $CHAIN_ID";
    public static final String WALLET_CLOSE = "Button Close $CHAIN_ID - Wallet SC";
    public static final String WALLET_SETTINGS = "Button Settings $CHAIN_ID - Wallet SC";
//    public static final String WALLET_BALANCE = "Button Crypto $CHAIN_ID - Wallet SC";
//    public static final String WALLET_BALANCE_FIAT = "Button Crypto $CHAIN_ID - Wallet SC";
    public static final String WALLET_ADDRESS = "Button Address $CHAIN_ID - Wallet SC";
    public static final String WALLET_SHARE = "Button Share $CHAIN_ID - Wallet SC";
    public static final String WALLET_SHARED = "Shared with $CHAIN_ID $APP_NAME - Wallet SC";
    public static final String WALLET_ADDRESSES = "Button All Addresses $CHAIN_ID - Wallet SC";
    public static final String WALLET_SEND = "Button Send $CHAIN_ID - Wallet SC";
    public static final String WALLET_RECEIVE = "Button Receive $CHAIN_ID - Wallet SC";
    public static final String WALLET_EXCHANGE = "Button Exchange $CHAIN_ID - Wallet SC";
    public static final String WALLET_PULL = "Pull Wallet $CHAIN_ID - Wallet SC";
    public static final String WALLET_TRANSACTION = "Button Transaction $CHAIN_ID - Wallet SC";
    public static final String WALLET_BACKUP_SEED = "Button Backup Seed - Wallet SC";

    // Wallet Addresses Screen
    public static final String WALLET_ADDRESSES_SCREEN = "Screen Wallet Addresses $CHAIN_ID";
    public static final String WALLET_ADDRESSES_CLOSE = "Button Close $CHAIN_ID - Wallet Addresses SC";
    public static final String WALLET_ADDRESSES_CLICK = "Button Address $CHAIN_ID - Wallet Addresses SC";

    // Wallet Transactions Screen
    public static final String WALLET_TRANSACTIONS_SCREEN = "Screen Transaction $CHAIN_ID State $STATE";
    public static final String WALLET_TRANSACTIONS_CLOSE = "Button Close $CHAIN_ID - Transaction SC";
    public static final String WALLET_TRANSACTIONS_BLOCKCHAIN = "Button View $CHAIN_ID State $STATE - Transaction SC";

    // Wallet Settings Screen
    public static final String WALLET_SETTINGS_SCREEN = "Screen Wallet Settings $CHAIN_ID";
    public static final String WALLET_SETTINGS_CLOSE = "Button Close $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_RENAME = "Button Rename $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_SAVE = "Button Save $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_FIAT = "Button Fiat $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_KEY = "Button Show Key $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_DELETE = "Button Delete $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_DELETE_YES = "Event Wallet Deleted $CHAIN_ID - Wallet Settings SC";
    public static final String WALLET_SETTINGS_DELETE_NO = "Event Wallet Delete Canceled $CHAIN_ID - Wallet Settings SC";

    // No Internet Screen
    public static final String NO_INTERNET_SCREEN = "Screen No Internet";
    public static final String NO_INTERNET_CLOSE = "Button Close - No Internet SC";
    public static final String NO_INTERNET_CHECK = "Button Check - No Internet SC";

    // Send To Screen
    public static final String SEND_TO_SCREEN = "Screen Send To";
    public static final String SEND_TO_CLOSE = "Button Close - Send To SC";
    public static final String SEND_TO_ADDRESS_BOOK = "Button Address Book - Send To SC";
    public static final String SEND_TO_WIRELESS = "Button Wireless Scan - Send To SC";
    public static final String SEND_TO_QR = "Button Scan QR - Send To SC";

    // Send From Screen
    public static final String SEND_FROM_SCREEN = "Screen Send From";
    public static final String SEND_FROM_CLOSE = "Button Close - Send From SC";
    public static final String SEND_FROM_WALLET_CLICK = "Button Wallet $CHAIN_ID - Send From SC";
}
