package com.example.flowerboutique.ui.admin.products;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.Transformation;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityAdminProductDetailBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminProductDetailActivity extends AppCompatActivity {
    ActivityAdminProductDetailBinding binding;
    CollectionReference productCollection, categoryCollection;
    List<DocumentSnapshot> categories;
    DocumentReference selectedCategory;
    AlertDialog progressingDialog;
    Uri imageUri;
    String imageUrl;
    String id;

    private ActivityResultLauncher<Intent> galleryResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ChooseGallery()
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminProductDetailBinding.inflate(getLayoutInflater());
        categoryCollection = BoutiqueApplication.getInstance().getAppFirebase().getCategoriesCollection();
        productCollection = BoutiqueApplication.getInstance().getAppFirebase().getProductsCollection();
        progressingDialog = new AlertDialog.Builder(this).setView(getLayoutInflater().inflate(R.layout.dialog_loading, null)).setCancelable(false).create();

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(v -> finish());
        binding.statusSwitch.setChecked(true);
        if (getIntent() != null)
            id = getIntent().getStringExtra("id");

        if (id != null)
            bindDataToViewForUpdatingProduct();
        else
            bindDataToViewForAddingProduct();

        binding.image.setOnClickListener(v -> {
            startChooseImage();
        });

        binding.addImgBtn.setOnClickListener(v -> {
            startChooseImage();
        });

        binding.saveBtn.setOnClickListener(v -> {
            if (imageUri != null) uploadToCloudinary();
            else saveProduct();
        });

        binding.categoryEdt.setOnItemClickListener((parent, view, position, id) -> {
            CategoryItem categoryItem = (CategoryItem) parent.getItemAtPosition(position);
            selectedCategory = categoryItem.doc;
        });

        categoryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                categories = snapshot.getDocuments();
                List<CategoryItem> docs = categories.stream().map(doc -> new CategoryItem(doc.getString("name"), doc.getReference())).collect(Collectors.toList());
                binding.categoryEdt.setAdapter(new CategoryAdapter(this, docs));
            }
        });
    }

    private void bindDataToViewForUpdatingProduct() {
        binding.headerTitle.setText("Sửa sản phẩm");
        progressingDialog.show();
        productCollection.document(id).get().addOnCompleteListener(task -> {
            progressingDialog.cancel();
            if (task.isSuccessful()) {
                DocumentSnapshot snapshot = task.getResult();
                String image = ((List<String>) snapshot.get("image")).get(0);
                binding.nameEdt.setText(snapshot.getString("name"));
                binding.priceEdt.setText(String.valueOf(snapshot.getLong("price")));
                binding.descriptionEdt.setText(snapshot.getString("description"));
                binding.statusSwitch.setChecked(snapshot.getBoolean("status").booleanValue());
                if (image != null) {
                    Picasso.get().load(image).into(binding.image);
                    binding.addImgBtn.setVisibility(View.GONE);
                } else {
                    binding.image.setVisibility(View.GONE);
                    binding.addImgBtn.setVisibility(View.VISIBLE);
                }
                ((List<DocumentReference>) snapshot.get("categories")).get(0).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        DocumentSnapshot categorySnapshot = task1.getResult();
                        binding.categoryEdt.setText(categorySnapshot.getString("name"), false);
                        selectedCategory = categorySnapshot.getReference();
                    }
                });
            }
        });
    }

    private void bindDataToViewForAddingProduct() {
        binding.addImgBtn.setVisibility(View.VISIBLE);
        binding.image.setVisibility(View.GONE);
        binding.headerTitle.setText("Thêm sản phẩm");
    }

    private void saveProduct() {
        if (!progressingDialog.isShowing())
            progressingDialog.show();
        Editable name = binding.nameEdt.getText(),
                price = binding.priceEdt.getText(),
                description = binding.descriptionEdt.getText();
        if (selectedCategory == null || name == null || name.toString().trim().isEmpty() || price == null || price.toString().trim().isEmpty() || description == null || description.toString().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            progressingDialog.cancel();
            return;
        }

        if (id == null && imageUrl == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_LONG).show();
            progressingDialog.cancel();
            return;
        }

        HashMap<String, Object> updatedData = new HashMap<>();
        if (imageUrl != null)
            updatedData.put("image", new ArrayList<String>(List.of(imageUrl)));

        try {
            updatedData.put("price", Long.valueOf(price.toString().trim()));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            progressingDialog.cancel();
            return;
        }

        if (id == null) {
            updatedData.put("created_date", FieldValue.serverTimestamp());
        }
        updatedData.put("name", name.toString().trim());
        updatedData.put("updated_date", FieldValue.serverTimestamp());
        updatedData.put("description", description.toString().trim());
        updatedData.put("categories", new ArrayList(List.of(selectedCategory)));
        updatedData.put("status", binding.statusSwitch.isChecked());
        (id != null ? productCollection.document(id).update(updatedData) : productCollection.add(updatedData)).addOnCompleteListener(v -> {
            progressingDialog.cancel();
            if (v.isSuccessful()) {
                Toast.makeText(this, "Cập nhập sản phẩm thành công.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("id", id != null ? id : ((DocumentReference) v.getResult()).getId());
                setResult(AdminProductDetailActivity.RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Lỗi", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startChooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryResult.launch(intent);
    }

    private void uploadToCloudinary() {
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
                imageUrl = MediaManager.get().url().transformation(new Transformation().width(300).quality("auto:eco").fetchFormat("webp")).generate((String) resultData.get("display_name"));
                saveProduct();
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                progressingDialog.cancel();
                Toast.makeText(AdminProductDetailActivity.this, "Lỗi", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {

            }
        }).dispatch();
    }

    private static class CategoryItem {
        String name;
        DocumentReference doc;

        public CategoryItem(String name, DocumentReference doc) {
            this.name = name;
            this.doc = doc;
        }

        @NonNull
        @Override
        public String toString() {
            return this.name;
        }
    }

    static private class CategoryAdapter extends ArrayAdapter<CategoryItem> {
        private final List<CategoryItem> categoryItems;
        private final Context context;

        public CategoryAdapter(Context context, List<CategoryItem> categoryItems) {
            super(context, android.R.layout.simple_list_item_1, categoryItems);
            this.categoryItems = categoryItems;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            CategoryItem category = categoryItems.get(position);
            TextView textView = convertView.findViewById(android.R.id.text1);
            textView.setText(category.name);
            return convertView;
        }
    }

    private class ChooseGallery implements ActivityResultCallback<ActivityResult> {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == AdminProductDetailActivity.RESULT_OK) {
                Intent data = o.getData();
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        binding.image.setImageBitmap(bitmap);
                        binding.image.setVisibility(View.VISIBLE);
                        binding.addImgBtn.setVisibility(View.GONE);
                    } catch (IOException ignored) {
                    }
                }
            }

        }
    }
}
