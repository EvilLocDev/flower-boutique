package com.example.flowerboutique.ui.admin.categories;

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
import com.example.flowerboutique.databinding.ActivityAdminCategoryDetailBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminCategoryDetailActivity extends AppCompatActivity {
    private ActivityAdminCategoryDetailBinding binding;
    private String id, imageUrl;
    private Uri imageUri;
    private CollectionReference categoryCollection;
    private AlertDialog progressingDialog;
    private ActivityResultLauncher<Intent> galleryResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminCategoryDetailBinding.inflate(getLayoutInflater());
        categoryCollection = new AppFirebase().getCategoriesCollection();
        progressingDialog = new AlertDialog.Builder(this).setView(getLayoutInflater().inflate(R.layout.dialog_loading, null)).setCancelable(false).create();
        galleryResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new AdminCategoryDetailActivity.ChooseGalleryCallback());

        if (getIntent() != null && getIntent().getStringExtra("id") != null)
            id = getIntent().getStringExtra("id");

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadData();
        renderUI();
    }

    private void loadData() {
        if (id != null) {
            categoryCollection.document(id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    binding.nameEdt.setText(doc.getString("name"));
                    Picasso.get().load(doc.getString("thumbnail")).into(binding.image);
                    binding.statusSwitch.setChecked(Boolean.TRUE.equals(doc.getBoolean("status")));
                } else {
                    finish();
                }
            });
        }
    }

    private void saveData() {
        if (!progressingDialog.isShowing()) progressingDialog.show();
        String name = binding.nameEdt.getText() == null ? null : binding.nameEdt.getText().toString().trim();
        boolean isActive = binding.statusSwitch.isChecked();
        if (name == null || name.isEmpty()) {
            progressingDialog.cancel();
            Toast.makeText(this, "Tên không được trống", Toast.LENGTH_LONG).show();
            return;
        }

        if (imageUrl == null && id == null) {
            progressingDialog.cancel();
            Toast.makeText(this, "Vui lòng chọn ảnh danh mục", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        if (imageUrl != null)
            updatedData.put("thumbnail", imageUrl);
        updatedData.put("name", name);
        updatedData.put("status", isActive);
        (id != null ? categoryCollection.document(id).update(updatedData) : categoryCollection.add(updatedData)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Lưu danh mục thành công", Toast.LENGTH_LONG).show();
                progressingDialog.cancel();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("id", task.getResult() instanceof DocumentReference ? ((DocumentReference) task.getResult()).getId() : id);
                setResult(AdminCategoryDetailActivity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void uploadAvatar() {
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
                imageUrl = MediaManager.get().url().transformation(new Transformation().fetchFormat("webp").width(400).quality("auto:eco")).generate((String) resultData.get("display_name"));
                saveData();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressingDialog.cancel();
                Toast.makeText(AdminCategoryDetailActivity.this, "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        }).dispatch();
    }

    private void renderUI() {
        binding.title.setText(id != null ? "Chỉnh sửa danh mục" : "Thêm danh mục");
        binding.backBtn.setOnClickListener(v -> {
            finish();
        });
        binding.image.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryResult.launch(intent);
        });
        binding.saveBtn.setOnClickListener(v -> {
            if (imageUri != null)
                uploadAvatar();
            else
                saveData();
        });
    }

    private class ChooseGalleryCallback implements ActivityResultCallback<ActivityResult> {
        @Override
        public void onActivityResult(ActivityResult o) {
//            if (o.getResultCode() == AdminUserDetailActivity.RESULT_OK) {
            if (o.getResultCode() == RESULT_OK) {
                Intent data = o.getData();
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        binding.image.setImageBitmap(bitmap);
                    } catch (IOException ignored) {
                    }
                }
            }

        }

    }
}
