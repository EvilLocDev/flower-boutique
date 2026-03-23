package com.example.flowerboutique.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.authen.LoginActivity;
import com.example.flowerboutique.ui.authen.SignUpActivity;

public class LoginRegisterFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_register, container, false);
        Context context = getContext();
        Button signInBtn = view.findViewById(R.id.sign_in_btn);
        Button register = view.findViewById(R.id.register_btn);
        if (context != null) {
            signInBtn.setOnClickListener(v -> {
                context.startActivity(new Intent(context, LoginActivity.class));
            });

            register.setOnClickListener(v -> {
                context.startActivity(new Intent(context, SignUpActivity.class));
            });
        }
        return view;
    }
}