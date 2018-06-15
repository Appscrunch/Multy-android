/*
 * Copyright 2018 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;
import io.multy.ui.activities.SplashActivity;
import io.multy.util.analytics.Analytics;
import io.multy.util.analytics.AnalyticsConstants;


public class ResetDataDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_settings_reset, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @OnClick(R.id.button_positive)
    public void onClickPositive() {
//        Analytics.getInstance(getActivity()).logSecuritySettings(AnalyticsConstants.SECURITY_SETTINGS_RESET_YES);
//        RealmManager.removeDatabase(getActivity());
//        Prefs.clear();
//        Realm.init(getActivity());
//
//        startActivity(new Intent(getActivity(), SplashActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
//        getActivity().finish();
//        System.exit(2);
//        Intent intent = new Intent();
//        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
//        intent.setData(uri);
        startActivity(new Intent(getContext(), SplashActivity.class).putExtra(SplashActivity.RESET_FLAG, true));
    }

    @OnClick(R.id.button_neutral)
    public void onClickNeutral() {
        Analytics.getInstance(getActivity()).logSecuritySettings(AnalyticsConstants.SECURITY_SETTINGS_RESET_NO);
        dismiss();
    }

}
