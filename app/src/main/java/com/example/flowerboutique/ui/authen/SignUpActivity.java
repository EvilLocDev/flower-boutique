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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.UserInfor;
import com.example.flowerboutique.ui.main_home.MainActivity;
import com.example.flowerboutique.ui.makeorder.MakeOrderActivity;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    Button btnSignup, backBtn;
    CheckBox cbTerms;
    EditText edtEmail, edtPassword, edtComfirmPass, etName;
    private AppFirebase appFirebase = new AppFirebase();
    private final BoutiqueApplication application = BoutiqueApplication.getInstance();
    private MutableLiveData<Boolean> isProgressing = new MutableLiveData<>(false);
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initLayout();

        loadingDialog = new AlertDialog.Builder(this).setView(getLayoutInflater().inflate(R.layout.dialog_loading, null)).create();

        btnSignup.setOnClickListener(v -> {
            onSignUp();
        });

        isProgressing.observe(this, v -> {
            if (v) {
                loadingDialog.show();
            } else {
                loadingDialog.cancel();
            }
        });

        TextView tvSignInLink = findViewById(R.id.tvSignInLink);
        tvSignInLink.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
        backBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void onSignUp() {
        isProgressing.setValue(true);
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtComfirmPass.getText().toString();
        String name = etName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            isProgressing.setValue(false);
            Toast.makeText(SignUpActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isProgressing.setValue(false);
            Toast.makeText(SignUpActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            isProgressing.setValue(false);
            Toast.makeText(SignUpActivity.this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            isProgressing.setValue(false);
            Toast.makeText(SignUpActivity.this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            isProgressing.setValue(false);
            Toast.makeText(SignUpActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            isProgressing.setValue(false);
            Toast.makeText(this, "Vui lòng đồng ý điều khoản trước khi đăng ký", Toast.LENGTH_SHORT).show();
            return;
        }

        appFirebase.getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("created_date", FieldValue.serverTimestamp());
                        userData.put("updated_date", FieldValue.serverTimestamp());
                        userData.put("avatar", "https://res.cloudinary.com/flower-store/image/upload/q_auto:low,w_100,h_100,f_webp/v1733504138/mrxglys3bp4jjxyliapm.png");
                        userData.put("birthday", null);
                        userData.put("phoneNumber", null);
                        userData.put("gender", null);
                        userData.put("status", "active");
                        userData.put("role", "customer");
                        appFirebase.getUsersCollection().document(user.getUid()).set(userData).addOnCompleteListener(this, firestoreTask -> {
                            if (firestoreTask.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Đăng ký thành công", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Lỗi!!!", Toast.LENGTH_SHORT).show();
                            }
                            isProgressing.setValue(false);
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        });
                    } else {
                        isProgressing.setValue(false);
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthUserCollisionException)
                            Toast.makeText(this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initLayout() {
        btnSignup = findViewById(R.id.btnSignUp);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtComfirmPass = findViewById(R.id.edtConfirmPassword);
        etName = findViewById(R.id.edtName);
        backBtn = findViewById(R.id.iv_back);
        cbTerms = findViewById(R.id.cbTerms);
    }

}
