/*
 * Copyright 2017 Idealnaya rabota LLC
 * Licensed under Multy.io license.
 * See LICENSE for details
 */

package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import io.multy.R;

/**
 * Created by anschutz1927@gmail.com on 23.02.18.
 */

public class PrivateKeyDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = PrivateKeyDialogFragment.class.getSimpleName();

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.bottom_sheet_private_key, null);
        dialog.setContentView(view);
    }
}
