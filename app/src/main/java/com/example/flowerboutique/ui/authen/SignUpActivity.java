package com.example.flowerboutique.ui.authen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.makeorder.MakeOrderActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText edtName,edtEmail,edtPass,edtConfirmPass;
    private CheckBox chbConfirm;
    private Button btnSignUp;

    private TextView tvSignInLink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPass =findViewById(R.id.edtPassword);
        edtConfirmPass=findViewById(R.id.edtConfirmPassword);
        chbConfirm =findViewById(R.id.cbTerms);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignInLink =findViewById(R.id.tvSignInLink);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=edtName.getText().toString();
                String email=edtEmail.getText().toString();
                String pass = edtPass.getText().toString();
                String confirmPass = edtConfirmPass.getText().toString();

                if (pass.equals(confirmPass)){
                    registerByEmailAndPass(email,pass);
                    FirebaseUser user = mAuth.getCurrentUser();
                    // dat chuyen trang tam thoi, khi nao có main thi sua
                    Intent intent = new Intent();
                    intent.setClass(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);

                }
                else {
                    Toast.makeText(SignUpActivity.this," Mật khẩu không trùng khớp", Toast.LENGTH_LONG).show();
                }
            }
        });

        tvSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLogin = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intentLogin);
            }
        });

    }

private void registerByEmailAndPass(String email, String pass){
    mAuth = FirebaseAuth.getInstance();
    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this,task -> {
        if (task.isSuccessful()){
            Toast.makeText(this,"Đăng ký thành công "+task.getException().getMessage()
                    ,Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this,"Đăng ký thất bại "+task.getException().getMessage()
                    ,Toast.LENGTH_LONG).show();
        }
    });
}


}

