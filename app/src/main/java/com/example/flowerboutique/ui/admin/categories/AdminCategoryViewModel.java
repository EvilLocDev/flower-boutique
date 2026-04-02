package com.example.flowerboutique.ui.admin.categories;

import android.content.Intent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;

import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminCategoryViewModel extends ViewModel {
    private CollectionReference categoryCollection = new AppFirebase().getCategoriesCollection();
    private List<AdminCategoryAdapter.CategoryItem> categoryItems = new ArrayList<>();
    private AdminCategoryAdapter adapter = new AdminCategoryAdapter(categoryItems);
    private AppCompatActivity context;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    public AdminCategoryViewModel() {
        adapter.setOnDelete(categoryItem -> {
            categoryCollection.document(categoryItem.id).delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    categoryItems.remove(categoryItem);
                    adapter.notifyDataSetChanged();
                    if (context != null)
                        Toast.makeText(context, "Xóa thành công", Toast.LENGTH_LONG).show();
                }
            });
        });

        adapter.setOnClickView(categoryItem -> {
            if (activityResultLauncher != null) {
                Intent intent = new Intent(context, AdminCategoryDetailActivity.class);
                intent.putExtra("id", categoryItem.id);
                activityResultLauncher.launch(intent);
            } else {
                Intent intent = new Intent(context, AdminCategoryDetailActivity.class);
                intent.putExtra("id", categoryItem.id);
            }
        });

        loadData();
    }

    public ActivityResultLauncher<Intent> getActivityResultLauncher() {
        return activityResultLauncher;
    }

    public void setContext(AppCompatActivity context) {
        this.context = context;
        activityResultLauncher = context.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                o -> {
                    if (o.getResultCode() == AdminCategoryDetailActivity.RESULT_OK) {
                        String id = o.getData().getStringExtra("id");
                        if (id != null)
                            categoryCollection.document(id).get().addOnCompleteListener(v -> {
                                if (v.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = v.getResult();
                                    AdminCategoryAdapter.CategoryItem categoryItem = new AdminCategoryAdapter.CategoryItem(documentSnapshot.getId(), documentSnapshot.getString("name"));
                                    Optional<AdminCategoryAdapter.CategoryItem> matched = categoryItems.stream().filter(item -> item.id.equals(categoryItem.id)).findFirst();
                                    if (matched.isPresent())
                                        categoryItems.set(categoryItems.indexOf(matched.get()), categoryItem);
                                    else
                                        categoryItems.add(0, categoryItem);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                    }
                }
        );
    }

    private void loadData() {
        categoryCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                for (DocumentSnapshot category : snapshot.getDocuments()) {
                    categoryItems.add(new AdminCategoryAdapter.CategoryItem(category.getId(), category.getString("name")));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public AdminCategoryAdapter getAdapter() {
        return adapter;
    }
}
