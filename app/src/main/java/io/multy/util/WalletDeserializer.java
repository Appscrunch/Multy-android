/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.multy.model.entities.Output;
import io.multy.model.entities.wallet.BtcWallet;
import io.multy.model.entities.wallet.EthWallet;
import io.multy.model.entities.wallet.Wallet;
import io.multy.model.entities.wallet.WalletAddress;

public class WalletDeserializer implements JsonDeserializer<List<Wallet>> {

    @Override
    public List<Wallet> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        List<Wallet> wallets = new ArrayList<>();
        Gson gson = new Gson();
        try {
//            JSONObject mainJson = new JSONObject(json.toString());
            JSONArray jsonArray = new JSONArray(json.toString());
            int currencyId;
            String jsonItemString;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonItemString = jsonArray.getJSONObject(i).toString();
                Wallet wallet = gson.fromJson(jsonItemString, Wallet.class);
                currencyId = wallet.getCurrencyId();

                switch (currencyId) {
                    case 60:
                        wallet.setEthWallet(gson.fromJson(jsonItemString, EthWallet.class));
                        break;
                    case 0:
                        BtcWallet btcWallet = gson.fromJson(jsonItemString, BtcWallet.class);
                        btcWallet.calculateAvailableBalance();
                        wallet.setBalance(String.valueOf(btcWallet.calculateBalance()));
                        wallet.setBtcWallet(btcWallet);
                        break;
                }
                wallets.add(wallet);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return wallets;
    }

    private final static String JSON = "{\n" +
            "  \"code\": 200,\n" +
            "  \"message\": \"OK\",\n" +
            "  \"topindexes\": [\n" +
            "    {\n" +
            "      \"currencyid\": 0,\n" +
            "      \"networkid\": 0,\n" +
            "      \"topindex\": 3\n" +
            "    },\n" +
            "    {\n" +
            "      \"currencyid\": 60,\n" +
            "      \"networkid\": 0,\n" +
            "      \"topindex\": 1\n" +
            "    }\n" +
            "  ],\n" +
            "  \"wallets\": [\n" +
            "    {\n" +
            "      \"currencyid\": 60,\n" +
            "      \"networkid\": 0,\n" +
            "      \"walletindex\": 0,\n" +
            "      \"walletname\": \"mY awesome Ethereum wallet\",\n" +
            "      \"lastactiontime\": 1234553,\n" +
            "      \"dateofcreation\": 12312312,\n" +
            "      \"nonce\": 1,\n" +
            "      \"balance\": 123456789123456789,\n" +
            "      \"pending\": \"true\",\n" +
            "      \"addresses\": [\n" +
            "        {\n" +
            "          \"lastActionTime\": 1517320476,\n" +
            "          \"address\": \"0xMiGR2q7nSFawjifK5vhixSAeHoH2Do5yKu\",\n" +
            "          \"addressindex\": 0,\n" +
            "          \"amount\": 123456789012345678\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"currencyid\": 0,\n" +
            "      \"walletindex\": 0,\n" +
            "      \"walletname\": \"leshhhhha\",\n" +
            "      \"lastactiontime\": 1517322474,\n" +
            "      \"dateofcreation\": 1517320476,\n" +
            "      \"addresses\": [\n" +
            "        {\n" +
            "          \"lastActionTime\": 1517320476,\n" +
            "          \"address\": \"miGR2q7nSFawjifK5vhixSAeHoH2Do5yKu\",\n" +
            "          \"addressindex\": 0,\n" +
            "          \"amount\": 130000000,\n" +
            "          \"spendableoutputs\": [\n" +
            "            {\n" +
            "              \"txid\": \"01d47c5995f9f0ea5f9f8e2a99be2c5c0f0ab7d18af899261ac136059b33e09d\",\n" +
            "              \"txoutid\": 0,\n" +
            "              \"txoutamount\": 130000000,\n" +
            "              \"txoutscript\": \"76a9141e287510abb23911b17dcf0a55e2aa84966231b988ac\",\n" +
            "              \"address\": \"miGR2q7nSFawjifK5vhixSAeHoH2Do5yKu\",\n" +
            "              \"userid\": \"2bPz7hrZ1wAMUASVQMq1iMPTA5tvqjrsVcf4rGpSWyJ619YYzG\",\n" +
            "              \"walletindex\": 0,\n" +
            "              \"addressindex\": 0,\n" +
            "              \"txstatus\": 2,\n" +
            "              \"stockexchangerate\": [\n" +
            "                {\n" +
            "                  \"exchanges\": {\n" +
            "                    \"eur_btc\": 0,\n" +
            "                    \"usd_btc\": 0.00009417854283537195,\n" +
            "                    \"eth_btc\": 0.10689699649810791,\n" +
            "                    \"eth_usd\": 1135.949951171875,\n" +
            "                    \"eth_eur\": 0,\n" +
            "                    \"btc_usd\": 10618.1298828125\n" +
            "                  },\n" +
            "                  \"timestamp\": 1517320472,\n" +
            "                  \"stock_exchange\": \"Poloniex\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"exchanges\": {\n" +
            "                    \"eur_btc\": 0.00011664759372189893,\n" +
            "                    \"usd_btc\": 0.00009510187277319754,\n" +
            "                    \"eth_btc\": 0.10696999728679657,\n" +
            "                    \"eth_usd\": 1125,\n" +
            "                    \"eth_eur\": 913.8499755859375,\n" +
            "                    \"btc_usd\": 10515.0400390625\n" +
            "                  },\n" +
            "                  \"timestamp\": 1517320472,\n" +
            "                  \"stock_exchange\": \"Gdax\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"currencyid\": 0,\n" +
            "      \"walletindex\": 1,\n" +
            "      \"walletname\": \"Personal\",\n" +
            "      \"lastactiontime\": 1517322474,\n" +
            "      \"dateofcreation\": 1517322474,\n" +
            "      \"addresses\": [\n" +
            "        {\n" +
            "          \"lastActionTime\": 1517322474,\n" +
            "          \"address\": \"mnFuUUTBjrYS4SaAYpMxqQMKhu1E7fTgR6\",\n" +
            "          \"addressindex\": 0,\n" +
            "          \"amount\": 4978940,\n" +
            "          \"spendableoutputs\": [\n" +
            "            {\n" +
            "              \"txid\": \"ad94a40e1989303b62e613ab67a301b5cfb0eb5b988febf6b5542973c89e01c3\",\n" +
            "              \"txoutid\": 0,\n" +
            "              \"txoutamount\": 4978940,\n" +
            "              \"txoutscript\": \"76a91449f04968d246a0728af15972c5524c46623f6c9988ac\",\n" +
            "              \"address\": \"mnFuUUTBjrYS4SaAYpMxqQMKhu1E7fTgR6\",\n" +
            "              \"userid\": \"2bPz7hrZ1wAMUASVQMq1iMPTA5tvqjrsVcf4rGpSWyJ619YYzG\",\n" +
            "              \"walletindex\": 1,\n" +
            "              \"addressindex\": 0,\n" +
            "              \"txstatus\": 2,\n" +
            "              \"stockexchangerate\": [\n" +
            "                {\n" +
            "                  \"exchanges\": {\n" +
            "                    \"eur_btc\": 0,\n" +
            "                    \"usd_btc\": 0.0000943649908832729,\n" +
            "                    \"eth_btc\": 0.10753200203180313,\n" +
            "                    \"eth_usd\": 1138.989990234375,\n" +
            "                    \"eth_eur\": 0,\n" +
            "                    \"btc_usd\": 10597.150390625\n" +
            "                  },\n" +
            "                  \"timestamp\": 1517322473,\n" +
            "                  \"stock_exchange\": \"Poloniex\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"exchanges\": {\n" +
            "                    \"eur_btc\": 0.00011651888453649891,\n" +
            "                    \"usd_btc\": 0.00009501187648456056,\n" +
            "                    \"eth_btc\": 0.10739000141620636,\n" +
            "                    \"eth_usd\": 1127.5,\n" +
            "                    \"eth_eur\": 917.8900146484375,\n" +
            "                    \"btc_usd\": 10525\n" +
            "                  },\n" +
            "                  \"timestamp\": 1517322473,\n" +
            "                  \"stock_exchange\": \"Gdax\"\n" +
            "                }\n" +
            "              ]\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"lastActionTime\": 1517323545,\n" +
            "          \"address\": \"mpcoJoFaLg4StZEap3rfP3j9c2vHFyMdxh\",\n" +
            "          \"addressindex\": 1,\n" +
            "          \"amount\": 0,\n" +
            "          \"spendableoutputs\": [\n" +
            "            \n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"lastActionTime\": 1517324057,\n" +
            "          \"address\": \"mtB3R4yyk6A6hzvXJ57xKXzk1M3TcXCJJz\",\n" +
            "          \"addressindex\": 2,\n" +
            "          \"amount\": 0,\n" +
            "          \"spendableoutputs\": [\n" +
            "            \n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"currencyid\": 0,\n" +
            "      \"walletindex\": 2,\n" +
            "      \"walletname\": \"Personal\",\n" +
            "      \"lastactiontime\": 1517322480,\n" +
            "      \"dateofcreation\": 1517322480,\n" +
            "      \"addresses\": [\n" +
            "        {\n" +
            "          \"lastActionTime\": 1517322480,\n" +
            "          \"address\": \"miiu7vfVGHF273MwJYyu4VEsvqPn7BhYsx\",\n" +
            "          \"addressindex\": 0,\n" +
            "          \"amount\": 0,\n" +
            "          \"spendableoutputs\": [\n" +
            "            \n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
