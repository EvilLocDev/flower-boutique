package com.example.flowerboutique.ui.cart;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.flowerboutique.R;

public class AddCartDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_cart_successfully, null);
        view.findViewById(R.id.continue_btn).setOnClickListener(v -> {
            getDialog().cancel();
        });
        view.findViewById(R.id.to_cart_btn).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
            getDialog().cancel();
        });
        return new AlertDialog.Builder(requireContext()).setView(view).create();
    }

    public static String TAG = "AddCartDialog";
}
