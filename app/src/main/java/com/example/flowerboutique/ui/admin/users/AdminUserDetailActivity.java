package com.example.flowerboutique.ui.admin.users;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityAdminUserDetailBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminUserDetailActivity extends AppCompatActivity {
    private ActivityAdminUserDetailBinding binding;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "vn"));
    private final MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()).setTitleText("Chọn ngày sinh").build();
    private AlertDialog progressingDialog;
    private CollectionReference userCollection;
    private Uri imageUri;
    private String imageUrl;
    private ActivityResultLauncher<Intent> galleryResult;
    private String id;
    private Intent localIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        localIntent = getIntent();
        if (localIntent == null) finish();
        if (localIntent.getStringExtra("id") == null) finish();
        id = localIntent.getStringExtra("id");

        userCollection = new AppFirebase().getUsersCollection();
        progressingDialog = new AlertDialog.Builder(this).setView(getLayoutInflater().inflate(R.layout.dialog_loading, null)).setCancelable(false).create();
        galleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ChooseGalleryCallback());

        binding = ActivityAdminUserDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.ivBack.setOnClickListener(v -> finish());

        binding.birthdaySection.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), null));
        binding.birthday.setOnClickListener(v -> datePicker.show(getSupportFragmentManager(), null));
        datePicker.addOnPositiveButtonClickListener(v -> {
            binding.birthday.setText(dateFormat.format(new Date(v)));
        });
        binding.ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResult.launch(intent);
        });
        loadData();
        binding.btnCompleteProfile.setOnClickListener(v -> {
            if (imageUri != null) loadAvatar();
            else updateUser();
        });
    }

    private void loadData() {
        userCollection.document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                Picasso.get().load(snapshot.getString("avatar")).into(binding.ivAvatar);
                binding.edtName.setText(snapshot.getString("name"));
                binding.edtPhoneNumber.setText(snapshot.getString("phoneNumber"));
                binding.birthday.setText(snapshot.getDate("birthday") != null ? dateFormat.format(snapshot.getDate("birthday")) : null);
                binding.gender.setText(snapshot.getString("gender"), false);
                binding.role.setText(snapshot.getString("role"), false);
            } else finish();
        });
    }

    private void updateUser() {
        if (!progressingDialog.isShowing()) progressingDialog.show();
        String name = binding.edtName.getText() != null ? binding.edtName.getText().toString().trim() : null;
        String phoneNumber = binding.edtPhoneNumber.getText() != null ? binding.edtPhoneNumber.getText().toString().trim() : null;
        String gender = binding.gender.getText() != null ? binding.gender.getText().toString().trim() : null;
        String birthday = binding.birthday.getText() != null ? binding.birthday.getText().toString().trim() : null;
        String role = binding.role.getText() != null ? binding.role.getText().toString().trim() : null;
        if (name == null) {
            Toast.makeText(this, "Không để tên trống", Toast.LENGTH_LONG).show();
            progressingDialog.cancel();
            return;
        }
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", name);
        updatedData.put("phoneNumber", phoneNumber);
        updatedData.put("gender", gender);
        updatedData.put("role", role);
        try {
            updatedData.put("birthday", dateFormat.parse(birthday));
        } catch (ParseException ignored) {

        }
        if (imageUrl != null) updatedData.put("avatar", imageUrl);
        userCollection.document(id).update(updatedData).addOnCompleteListener(task -> {
            Toast.makeText(this, task.isSuccessful() ? "Lưu thành công" : "Lỗi", Toast.LENGTH_LONG).show();
            progressingDialog.cancel();
            Intent intent = new Intent();
            intent.putExtra("id", id);
            setResult(AdminUserDetailActivity.RESULT_OK, intent);
            finish();
        });
    }

    private void loadAvatar() {
        if (imageUri == null) return;
        MediaManager.get().upload(imageUri).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {
                progressingDialog.show();
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                imageUrl = MediaManager.get().url().transformation(new Transformation().fetchFormat("webp").width(180).quality("auto:eco")).generate((String) resultData.get("display_name"));
                updateUser();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressingDialog.cancel();
                Toast.makeText(AdminUserDetailActivity.this, "Lỗi", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        }).dispatch();
    }

    private class ChooseGalleryCallback implements ActivityResultCallback<ActivityResult> {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == AdminUserDetailActivity.RESULT_OK) {
                Intent data = o.getData();
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        binding.ivAvatar.setImageBitmap(bitmap);
                    } catch (IOException ignored) {
                    }
                }
            }

        }
    }
}
