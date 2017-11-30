package io.multy;

import android.app.Application;
import android.content.ContextWrapper;
import android.util.Base64;

import com.samwolfand.oneprefs.Prefs;

import io.multy.util.Constants;
import io.multy.util.JniException;
import io.multy.util.NativeDataHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import io.realm.Realm;

public class Multy extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .setDefaultIntValue(-1)
                .setDefaultBooleanValue(false)
                .setDefaultStringValue("")
                .build();

        if (Prefs.getBoolean(Constants.PREF_FIRST_SUCCESSFUL_START, true)) {
            try {
                final String mnemonic = NativeDataHelper.makeMnemonic();
                final byte[] seed = NativeDataHelper.makeSeed(mnemonic);

                Prefs.putString("seed", Base64.encodeToString(seed, Base64.DEFAULT));
                Prefs.putString("mnemonic", mnemonic);

                Prefs.putBoolean(Constants.PREF_FIRST_SUCCESSFUL_START, false);
            } catch (JniException e) {
                e.printStackTrace();
                //TODO show CRITICAL EXCEPTION HERE. Can the app work without seed?
            }
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("montseratt_regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }
}
