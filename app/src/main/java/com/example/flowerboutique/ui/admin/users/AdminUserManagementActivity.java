package com.example.flowerboutique.ui.admin.users;

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

import com.example.flowerboutique.databinding.ActivityAdminUserManagementBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Optional;

public class AdminUserManagementActivity extends AppCompatActivity {
    ActivityAdminUserManagementBinding binding;
    AdminUserManagementViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAdminUserManagementBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(AdminUserManagementViewModel.class);
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == AdminUserDetailActivity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getStringExtra("id") != null) {
                    String id = data.getStringExtra("id");
                    (new AppFirebase()).getUsersCollection().document(id).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot user = task.getResult();
                            AdminUserManagementViewModel.OverviewUser newUser = new AdminUserManagementViewModel.OverviewUser(user.getId(), user.getString("name"), user.getString("role"), user.getString("avatar"));
                            Optional<AdminUserManagementViewModel.OverviewUser> find = viewModel.getUsers().stream().filter(item -> item.id.equals(user.getId())).findFirst();
                            find.ifPresent(item -> viewModel.getUsers().set(viewModel.getUsers().indexOf(item), newUser));
                            viewModel.adapter.notifyDataSetChanged();
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

        binding.backBtn.setOnClickListener(v -> finish());
        binding.userList.setLayoutManager(new LinearLayoutManager(this));
        viewModel.adapter.setOnClick(user -> {
            Intent intent = new Intent(AdminUserManagementActivity.this, AdminUserDetailActivity.class);
            intent.putExtra("id", user.id);
            activityResultLauncher.launch(intent);
        });
        binding.userList.setAdapter(viewModel.getAdapter());
    }
}
