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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerboutique.R;
import com.example.flowerboutique.UserInfor;
import com.example.flowerboutique.ui.makeorder.MakeOrderActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText edtName,edtEmail,edtPass,edtConfirmPass;
    private CheckBox chbConfirm;
    private Button btnSignUp;
    private FirebaseFirestore database;
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

        //khoi tao database
        database = FirebaseFirestore.getInstance();

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
                    registerByEmailAndPass(email,pass,name);
                    FirebaseUser user = mAuth.getCurrentUser();

                    if (user!=null){

                        // them du lieu nguoi dung vao firestore
                        UserInfor infor =new UserInfor(user.getUid(),name,"user");
                        database.collection("users").document(infor.getUid()).set(infor.convertToHashmap())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(SignUpActivity.this, "Đăng ký thành công thông tin người dùng", Toast.LENGTH_SHORT).show();
                            }
                        }
                        )
                                // truong hop them du lieu that bi tren firestore
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUpActivity.this, "Tạo thông tin thất bại", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                    else {
                        // truong hop user chua duoc tao tu auth
                        Toast.makeText(SignUpActivity.this, "Người dùng chưa được tạo", Toast.LENGTH_SHORT).show();

                    }

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

        // chuyen lien ket sang trang dang nhap
        tvSignInLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentLogin = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intentLogin);
            }
        });

    }
//ham dang ky tai khoan tren firebase authebtication
private void registerByEmailAndPass(String email, String pass,String name){
    mAuth = FirebaseAuth.getInstance();
    mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(this,task -> {
        if (task.isSuccessful()){
            return;
        }
        else {
            Toast.makeText(this,"Tạo tài khoản không thành công "+task.getException().getMessage()
                    ,Toast.LENGTH_LONG).show();
        }
    });
}

}

