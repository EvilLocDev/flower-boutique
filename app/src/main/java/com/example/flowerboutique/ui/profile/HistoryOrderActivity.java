package com.example.flowerboutique.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.HistoryOrderAdapter;
import com.example.flowerboutique.R;
import com.example.flowerboutique.db.entities.OrderModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerHistoryOrder;
    private HistoryOrderAdapter adapter;
    private List<OrderModel> orderList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        recyclerHistoryOrder = findViewById(R.id.recyclerHistoryOrder);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
         btnBack=findViewById(R.id.back_btn);
        orderList = new ArrayList<>();
        adapter = new HistoryOrderAdapter(this, orderList, new HistoryOrderAdapter.OnOrderActionListener() {
            @Override
            public void onViewDetail(OrderModel order) {
                Toast.makeText(HistoryOrderActivity.this, "Xem chi tiết: " + order.getDocumentId(), Toast.LENGTH_SHORT).show();

                // Ví dụ mở màn hình chi tiết
                // Intent intent = new Intent(HistoryOrderActivity.this, OrderDetailActivity.class);
                // intent.putExtra("orderId", order.getDocumentId());
                // startActivity(intent);
            }

            @Override
            public void onBuyAgain(OrderModel order) {
                Toast.makeText(HistoryOrderActivity.this, "Mua lại đơn: " + order.getDocumentId(), Toast.LENGTH_SHORT).show();

                // Ở đây bạn có thể duyệt products rồi thêm lại vào giỏ hàng
            }
        });

        recyclerHistoryOrder.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistoryOrder.setAdapter(adapter);

        loadUserOrders();
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadUserOrders() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("orders")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();

                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            OrderModel order = document.toObject(OrderModel.class);
                            order.setDocumentId(document.getId());
                            orderList.add(order);
                        } catch (Exception e) {
                            Log.e("ORDER_PARSE", "Lỗi parse document: " + document.getId(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIRESTORE", "Lỗi lấy order", e);
                    Toast.makeText(this, "Không lấy được lịch sử đơn hàng", Toast.LENGTH_SHORT).show();
                });
    }
}