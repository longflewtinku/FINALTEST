package com.linkly.payment.utilities;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.linkly.libui.IUIDisplay;

public class ConfirmDialogFragment extends DialogFragment {

    private static final String ARG_CONFIRMATION_MESSAGE_ID = "confirmationMessageId";

    private ConfirmDialogListener confirmDialogListener;

    public interface ConfirmDialogListener {
        void onDialogPositiveClick();

        void onDialogNegativeClick();
    }

    public ConfirmDialogFragment() {
        // Required empty public constructor
    }

    public static ConfirmDialogFragment newInstance(int messageId, ConfirmDialogListener confirmDialogListener) {
        ConfirmDialogFragment fragment = new ConfirmDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_CONFIRMATION_MESSAGE_ID, messageId);
        fragment.setArguments(bundle);
        fragment.setConfirmDialogListener(confirmDialogListener);
        return fragment;
    }

    private void setConfirmDialogListener(ConfirmDialogListener confirmDialogListener) {
        this.confirmDialogListener = confirmDialogListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage(getConfirmationMessageId())
                .setPositiveButton(IUIDisplay.String_id.STR_YES.getId(), (dialog, id) -> confirmDialogListener.onDialogPositiveClick())
                .setNegativeButton(IUIDisplay.String_id.STR_NO.getId(), (dialog, id) -> confirmDialogListener.onDialogNegativeClick());
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private int getConfirmationMessageId() {
        if (getArguments() != null) {
            return getArguments().getInt(ARG_CONFIRMATION_MESSAGE_ID);
        }
        return IUIDisplay.String_id.STR_CONFIRM.getId();
    }
}
