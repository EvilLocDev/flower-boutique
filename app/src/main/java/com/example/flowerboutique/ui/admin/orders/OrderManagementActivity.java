package com.example.flowerboutique.ui.admin.orders;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityOrderManagementBinding;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class OrderManagementActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    ActivityOrderManagementBinding binding;
    List<OrderAdapter.OrderOverview> orderList = new ArrayList<>();
    OrderAdapter adapter;
    AppFirebase appFirebase = BoutiqueApplication.getInstance().getAppFirebase();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderManagementBinding.inflate(getLayoutInflater());
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == AdminOrderDetailActivity.RESULT_OK) {
                if (result.getData() != null && result.getData().getStringExtra("id") != null) {
                    appFirebase.getOrdersCollection().document(result.getData().getStringExtra("id")).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Optional<OrderAdapter.OrderOverview> findItem = orderList.stream().filter(item -> item.id.equals(task.getResult().getId())).findFirst();
                            findItem.ifPresent(item -> orderList.set(orderList.indexOf(item), deserializeItem(task.getResult())));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        });
        setContentView(binding.getRoot());

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backBtn.setOnClickListener(v -> finish());
        binding.orders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        binding.orders.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        appFirebase.getOrdersCollection()
                .orderBy("created_date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    orderList.clear();
                    if (task.isSuccessful()) {
                        task.getResult().forEach(doc -> {
                            orderList.add(deserializeItem(doc));
                        });
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private OrderAdapter.OrderOverview deserializeItem(DocumentSnapshot doc) {
        List<HashMap<String, Object>> products = (List<HashMap<String, Object>>) doc.get("products");
        if (products == null) return null;

        String id = doc.getId();
        String name = products.stream().map(p -> (String) p.get("name")).reduce("", (a, b) -> a + ", " + b);
        String date = doc.getDate("created_date") != null ? dateFormat.format(doc.getDate("created_date")) : "N/A";
        String status = doc.getString("status");
        long totalPrice = products.stream()
                .mapToLong(p -> ((Long) p.get("unitPrice")) * ((Long) p.get("quantity")))
                .sum();
        String price = currencyFormat.format(totalPrice);

        return new OrderAdapter.OrderOverview(id, name, date, status, price);
    }

    @Override
    public void onOrderClick(String orderId) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("orderId", orderId);
        activityResultLauncher.launch(intent);
    }
}