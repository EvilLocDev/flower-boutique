package com.example.flowerboutique.ui.orders;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CustomerOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private Button btnBack;
    private List<CustomerOrderAdapter.CustomerOrderOverview> orderList = new ArrayList<>();
    private CustomerOrderAdapter adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "vn"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_orders);

        rvOrders = findViewById(R.id.orders);
        btnBack = findViewById(R.id.back_btn);
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerOrderAdapter(orderList);
        rvOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        fetchUserOrders();
    }

    private void fetchUserOrders() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy danh sách đơn hàng theo user_id
        firestore.collection("orders")
                .whereEqualTo("user_id", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        orderList.clear();
                        task.getResult().getDocuments().forEach(doc -> {
                            List<HashMap<String, Object>> listProduct = (List<HashMap<String, Object>>) doc.get("products");
                            if (listProduct == null || listProduct.isEmpty()) return;

                            String id = doc.getId();
                            String state = doc.getString("status");
                            String date = doc.getDate("created_date") == null ? "Mới đây" : dateFormat.format(doc.getDate("created_date"));

                            // Nối tên các sản phẩm lại với nhau
                            String joinedNames = listProduct.stream()
                                    .map(p -> p.get("name") != null ? p.get("name").toString() : "Sản phẩm")
                                    .collect(Collectors.joining(", "));

                            // Tính giá (Ưu tiên lấy tổng giá lưu sẵn, nếu không có thì tự nhân)
                            Long totalPrice = doc.getLong("total_price");
                            if (totalPrice == null) {
                                totalPrice = listProduct.stream().mapToLong(p -> {
                                    long up = p.get("unitPrice") != null ? ((Number) p.get("unitPrice")).longValue() : 0L;
                                    long q = p.get("quantity") != null ? ((Number) p.get("quantity")).longValue() : 0L;
                                    return up * q;
                                }).sum();
                            }
                            String price = currencyFormat.format(totalPrice);

                            orderList.add(new CustomerOrderAdapter.CustomerOrderOverview(id, joinedNames, date, state, price));
                        });
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("FirebaseError", "Lỗi tải đơn hàng: ", task.getException());
                    }
                });
    }
}