package com.example.flowerboutique.ui.makeorder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.databinding.ActivityMakeOrderBinding;
import com.example.flowerboutique.ui.cart.CartItem;
import com.example.flowerboutique.ui.payment.VNPayPaymentActivity;
import com.example.flowerboutique.utils.firebase.AppFirebase;

public class MakeOrderActivity extends AppCompatActivity {

    private ActivityMakeOrderBinding binding;
    private BoutiqueApplication application;
    private ArrayList<CartItem> orderItems;
    private final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "vn"));

    private HashMap<String, Object> address = new HashMap<>();
    private String phoneNumber;
    private boolean isOpenInformInput = false;
    private boolean isFullInform = false;
    private Long totalAmount = 0L;

    private AppFirebase appFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMakeOrderBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // 1. NHẬN DỮ LIỆU TỪ INTENT
        if (getIntent().hasExtra("list_cart_items")) {
            orderItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("list_cart_items");
        }
        totalAmount = getIntent().getLongExtra("total_amount", 0);

        // 2. HIỂN THỊ TỔNG TIỀN
        binding.totalPrice.setText(numberFormat.format(totalAmount));

        // 3. SET UP RECYCLERVIEW
        setupRecyclerView();

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        application = BoutiqueApplication.getInstance();
        appFirebase = application.getAppFirebase();

        setupClickEvents();
    }

    private void setupRecyclerView() {
        if (orderItems != null && !orderItems.isEmpty()) {
            MakeOrderAdapter adapter = new MakeOrderAdapter(orderItems);
            binding.itemList.setLayoutManager(new LinearLayoutManager(this));
            binding.itemList.setAdapter(adapter);
            binding.makeOrderBtn.setEnabled(isFullInform); // Chỉ bật khi đủ thông tin địa chỉ
        } else {
            Toast.makeText(this, "Không có sản phẩm nào để thanh toán", Toast.LENGTH_SHORT).show();
            binding.makeOrderBtn.setEnabled(false);
        }
    }

    private void setupClickEvents() {
        binding.addAddressBtn.setOnClickListener(v -> {
            isOpenInformInput = true;
            renderInform();
        });

        binding.addressDetail.setOnClickListener(v -> {
            isOpenInformInput = true;
            renderInform();
        });

        binding.saveInform.setOnClickListener(v -> saveInform());
        binding.backBtn.setOnClickListener(v -> finish());

        // XỬ LÝ THANH TOÁN VNPAY
        binding.makeOrderBtn.setOnClickListener(v -> processOrderAndPayment());
    }

    private void renderInform() {
        if (isOpenInformInput) {
            binding.addAddressBtn.setVisibility(View.GONE);
            binding.addAddress.setVisibility(View.VISIBLE);
            binding.addressDetail.setVisibility(View.GONE);
        } else {
            binding.addAddress.setVisibility(View.GONE);
            if (isFullInform) {
                binding.addAddressBtn.setVisibility(View.GONE);
                binding.addressDetail.setVisibility(View.VISIBLE);
                binding.addressTv.setText(String.format("Địa chỉ: %s, %s, %s, %s",
                        address.get("address"), address.get("ward"), address.get("district"), address.get("city")));
                binding.phoneNumberTv.setText(String.format("Số điện thoại: %s", phoneNumber));
            } else {
                binding.addAddressBtn.setVisibility(View.VISIBLE);
                binding.addressDetail.setVisibility(View.GONE);
            }
        }
        binding.makeOrderBtn.setEnabled(isFullInform && orderItems != null && !orderItems.isEmpty());
    }

    private void saveInform() {
        String city = binding.cityEdt.getText().toString().trim();
        String district = binding.districtEdt.getText().toString().trim();
        String ward = binding.wardEdt.getText().toString().trim();
        String addressInput = binding.addressEdt.getText().toString().trim();
        String phoneNumberInp = binding.phoneNumberEdt.getText().toString().trim();

        if (!city.isEmpty() && !district.isEmpty() && !ward.isEmpty() && !addressInput.isEmpty() && !phoneNumberInp.isEmpty()) {
            this.address.put("city", city);
            this.address.put("district", district);
            this.address.put("ward", ward);
            this.address.put("address", addressInput);
            this.phoneNumber = phoneNumberInp;

            this.isFullInform = true;
            this.isOpenInformInput = false;
            renderInform();
        } else {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin nhận hàng", Toast.LENGTH_SHORT).show();
        }
    }

    // =================================================================================
    // HÀM XỬ LÝ LƯU FIREBASE VÀ CHUYỂN VNPAY
    // =================================================================================
    private void processOrderAndPayment() {
        FirebaseUser user = appFirebase.getFirebaseAuth().getCurrentUser(); // Hoặc lấy UID tùy logic của bạn

        if (!isFullInform) {
            Toast.makeText(this, "Vui lòng cập nhật địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.makeOrderBtn.setEnabled(false);
        binding.makeOrderBtn.setText("Đang khởi tạo giao dịch VNPay...");

        // 1. Chuẩn bị dữ liệu sản phẩm
        List<HashMap<String, Object>> orderProducts = orderItems.stream().map(product -> {
            HashMap<String, Object> result = new HashMap<>();
            result.put("name", product.getName());
            result.put("product", appFirebase.getProductsCollection().document(product.getId()));
            result.put("unitPrice", product.getPrice());
            result.put("quantity", product.getQuantity());
            return result;
        }).collect(Collectors.toList());

        // 2. Đóng gói đơn hàng
        HashMap<String, Object> order = new HashMap<>();
        // Nếu user == null thì để "guest", ngược lại lấy UID
        order.put("user_id", user != null ? user.getUid() : "guest");
        order.put("status", "pending_payment"); // Trạng thái chờ thanh toán VNPay
        order.put("created_date", FieldValue.serverTimestamp());
        order.put("address", address);
        order.put("phone_number", phoneNumber);
        order.put("products", orderProducts);
        order.put("total_price", totalAmount);
        order.put("payment_method", "vnpay");

        // 3. Đẩy lên Firestore
        appFirebase.getOrdersCollection().add(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String newOrderId = task.getResult().getId();

                // 4. Xóa giỏ hàng local
                new Thread(() -> {
                    if (application.getRoomDB() != null) {
                        application.getRoomDB().cartDAO().deleteAll();
                    }
                }).start();

                // 5. Chuyển sang màn hình thanh toán VNPay
                Intent intent = new Intent(MakeOrderActivity.this, VNPayPaymentActivity.class);
                intent.putExtra("orderId", newOrderId);
                intent.putExtra("totalPrice", totalAmount);
                startActivity(intent);
                finish();

            } else {
                binding.makeOrderBtn.setEnabled(true);
                binding.makeOrderBtn.setText("Thử lại");
                Toast.makeText(this, "Lỗi kết nối Server!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}