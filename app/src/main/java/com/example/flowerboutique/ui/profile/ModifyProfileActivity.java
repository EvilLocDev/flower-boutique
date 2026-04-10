package com.example.flowerboutique.ui.profile;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityModifyProfileBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ModifyProfileActivity extends AppCompatActivity {

    private ActivityModifyProfileBinding binding;
    private final AppFirebase appFirebase = new AppFirebase();
    private final FirebaseUser user = appFirebase.getFirebaseAuth().getCurrentUser();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "vn"));
    MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()).setTitleText("Chọn ngày sinh").build();

    private android.net.Uri imageUri;
    private final androidx.activity.result.ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            imageUri = uri;
                            binding.ivAvatar.setImageURI(uri);
                        }
                    });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModifyProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (user == null) {
            finish();
            return;
        }

        initProfile();

        datePicker.addOnPositiveButtonClickListener(v -> {
            binding.birthday.setText(dateFormat.format(new Date(v)));
        });

        binding.ivBack.setOnClickListener(v -> finish());

        binding.birthdaySection.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), null));
        binding.birthday.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), null));

        binding.ivAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        binding.btnCompleteProfile.setOnClickListener(v -> {
            String name = binding.edtName.getText().toString().trim();
            String phoneNumber = binding.edtPhoneNumber.getText().toString().trim();
            String gender = binding.gender.getText().toString().trim();
            String birthday = binding.birthday.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Không để tên trống", Toast.LENGTH_LONG).show();
                return;
            }

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("name", name);
            updatedData.put("phoneNumber", phoneNumber);
            updatedData.put("gender", gender);
            try {
                if (!birthday.isEmpty()) {
                    updatedData.put("birthday", dateFormat.parse(birthday));
                }
            } catch (ParseException ignored) {}

            if (imageUri != null) {
                Toast.makeText(this, "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show();
                uploadAvatarToCloudinary(imageUri, updatedData);
            } else {
                saveProfileToFirestore(updatedData);
            }
        });

        binding.ivAvatar.setClickable(true);
        binding.ivAvatar.setFocusable(true);
    }

    private void initProfile() {
        appFirebase.getUsersCollection().document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot result = task.getResult();
                String avatarUrl = result.getString("avatar");
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Picasso.get().load(avatarUrl).placeholder(R.drawable.ic_menu_profile).into(binding.ivAvatar);
                }
                binding.edtName.setText(result.getString("name"));
                binding.edtPhoneNumber.setText(result.getString("phoneNumber"));
                binding.gender.setText(result.getString("gender"), false);
                binding.birthday.setText(result.getDate("birthday") == null ? null : dateFormat.format(result.getDate("birthday")));
            }
        });
    }

    private void uploadAvatarToCloudinary(android.net.Uri uri, Map<String, Object> updatedData) {
        com.cloudinary.android.MediaManager.get().upload(uri)
                .option("folder", "avatars")
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String publicUrl = (String) resultData.get("secure_url");
                        updatedData.put("avatar", publicUrl);
                        saveProfileToFirestore(updatedData);
                        setResult(RESULT_OK);
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(ModifyProfileActivity.this, "Lỗi upload ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                }).dispatch();
    }

    private void saveProfileToFirestore(Map<String, Object> updatedData) {
        appFirebase.getUsersCollection().document(user.getUid())
                .update(updatedData)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, task.isSuccessful() ? "Cập nhật thành công" : "Cập nhật lỗi", Toast.LENGTH_LONG).show();
                    if (task.isSuccessful()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
    }
}