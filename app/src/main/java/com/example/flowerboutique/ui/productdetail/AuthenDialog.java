package com.example.flowerboutique.ui.productdetail;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.authen.LoginActivity;
import com.example.flowerboutique.ui.authen.SignUpActivity;

public class AuthenDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_require_auth, null);
        view.findViewById(R.id.sign_up_btn).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), SignUpActivity.class));
        });
        view.findViewById(R.id.login_btn).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), LoginActivity.class));
        });
        return new AlertDialog.Builder(getContext()).setView(view).create();
    }
}
