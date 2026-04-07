package com.example.flowerboutique.ui.admin.products;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flowerboutique.databinding.ActivityAdminProductManagementBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Optional;

public class AdminProductManagementActivity extends AppCompatActivity {
    ActivityAdminProductManagementBinding binding;
    AdminProductManagementViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminProductManagementBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AdminProductManagementViewModel.class);

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == AdminProductDetailActivity.RESULT_OK && result.getData() != null) {
                String id = result.getData().getStringExtra("id");
                if (id != null) {
                    List<AdminProductManagementViewModel.Product> productList = viewModel.getProducts();
                    (new AppFirebase()).getProductsCollection().document(id).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot productDoc = task.getResult();
                            List<String> imageList = ((List<String>) productDoc.get("image"));
                            String image = imageList != null && imageList.get(0) != null ? imageList.get(0) : null;
                            AdminProductManagementViewModel.Product product = new AdminProductManagementViewModel.Product(productDoc.getId(), productDoc.getString("name"), productDoc.getLong("price"), productDoc.getBoolean("status"), image);
                            Optional<AdminProductManagementViewModel.Product> find = productList.stream().filter(item -> item.id.equals(productDoc.getId())).findFirst();
                            if (find.isPresent())
                                productList.set(productList.indexOf(find.get()), product);
                            else
                                productList.add(0, product);
                            viewModel.getProductAdapter().notifyDataSetChanged();
                        }
                    });
                }
            }
        });

        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel.getProductAdapter().setOnClick(product -> {
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminProductDetailActivity.class);
            intent.putExtra("id", product.id);
            activityResultLauncher.launch(intent);
        });

        binding.backBtn.setOnClickListener(v -> finish());
        binding.productList.setLayoutManager(new LinearLayoutManager(this));
        binding.productList.setAdapter(viewModel.getProductAdapter());
        binding.addProductBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductManagementActivity.this, AdminProductDetailActivity.class);
            activityResultLauncher.launch(intent);
        });
    }
}
