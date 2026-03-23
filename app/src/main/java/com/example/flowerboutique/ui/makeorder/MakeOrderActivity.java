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
import androidx.lifecycle.LiveData;
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
//import com.example.flowerboutique.ui.authen.Login;
import com.example.flowerboutique.ui.cart.CartItem;
import com.example.flowerboutique.ui.payment.ZaloPayPaymentActivity;
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
    private MakeOrderAdapter makeOrderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMakeOrderBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // 1. NHẬN DỮ LIỆU TỪ INTENT
        orderItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("list_cart_items");
        totalAmount = getIntent().getLongExtra("total_amount", 0);

        // 2. HIỂN THỊ TỔNG TIỀN
        binding.totalPrice.setText(numberFormat.format(totalAmount));

        // 3. SET UP RECYCLERVIEW VÀ ADAPTER
        if (orderItems != null && !orderItems.isEmpty()) {
            MakeOrderAdapter adapter = new MakeOrderAdapter(orderItems);
            binding.itemList.setLayoutManager(new LinearLayoutManager(this));
            binding.itemList.setAdapter(adapter);

            // Bật nút đặt hàng vì đã có sản phẩm
            binding.makeOrderBtn.setEnabled(true);
        } else {
            Toast.makeText(this, "Không có sản phẩm nào để thanh toán", Toast.LENGTH_SHORT).show();
            binding.makeOrderBtn.setEnabled(false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Khởi tạo dữ liệu từ Application
        application = BoutiqueApplication.getInstance();
        appFirebase = application.getAppFirebase();

        // 2. Kiểm tra đăng nhập (Bắt buộc phải đăng nhập mới cho thanh toán)
//        if (appFirebase.getFirebaseAuth().getCurrentUser() == null) {
//            Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục!", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(MakeOrderActivity.this, Login.class));
//            finish();
//            return;
//        }


        setupClickEvents();
    }

    private void setupClickEvents() {
        // Mở form thêm địa chỉ
        binding.addAddressBtn.setOnClickListener(v -> {
            binding.cityEdt.setText((String) address.get("city"));
            binding.districtEdt.setText((String) address.get("district"));
            binding.wardEdt.setText((String) address.get("ward"));
            binding.addressEdt.setText((String) address.get("address"));
            binding.phoneNumberEdt.setText(phoneNumber);

            isOpenInformInput = true;
            renderInform();
        });

        // Nhấn vào thẻ địa chỉ để sửa
        binding.addressDetail.setOnClickListener(v -> {
            isOpenInformInput = true;
            renderInform();
        });

        // Lưu thông tin người dùng nhập vào
        binding.saveInform.setOnClickListener(v -> saveInform());

        // Nút quay lại
        binding.backBtn.setOnClickListener(v -> finish());

        // NÚT KẾT HỢP FIREBASE & ZALOPAY
        binding.makeOrderBtn.setOnClickListener(v -> processOrderAndPayment());
    }

    // =================================================================================
    // CÁC HÀM XỬ LÝ LOGIC UI
    // =================================================================================

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

                // Cập nhật Text hiển thị
                binding.addressTv.setText(String.format("Địa chỉ: %s, %s, %s, %s",
                        address.get("address"), address.get("ward"), address.get("district"), address.get("city")));
                binding.phoneNumberTv.setText(String.format("Số điện thoại: %s", phoneNumber));
            } else {
                binding.addAddressBtn.setVisibility(View.VISIBLE);
                binding.addressDetail.setVisibility(View.GONE);
            }
        }
        // Khóa hoặc mở khóa nút Đặt hàng
        binding.makeOrderBtn.setEnabled(isFullInform);
    }

    private boolean isEnoughInformation() {
        return address.containsKey("city") && address.containsKey("district") &&
                address.containsKey("ward") && address.containsKey("address") &&
                phoneNumber != null && !phoneNumber.isEmpty();
    }

    private void saveInform() {
        String city = binding.cityEdt.getText() != null ? binding.cityEdt.getText().toString().trim() : "";
        String district = binding.districtEdt.getText() != null ? binding.districtEdt.getText().toString().trim() : "";
        String ward = binding.wardEdt.getText() != null ? binding.wardEdt.getText().toString().trim() : "";
        String addressInput = binding.addressEdt.getText() != null ? binding.addressEdt.getText().toString().trim() : "";
        String phoneNumberInp = binding.phoneNumberEdt.getText() != null ? binding.phoneNumberEdt.getText().toString().trim() : "";

        if (!city.isEmpty() && !district.isEmpty() && !ward.isEmpty() && !addressInput.isEmpty() && !phoneNumberInp.isEmpty()) {
            this.address.put("city", city);
            this.address.put("district", district);
            this.address.put("ward", ward);
            this.address.put("address", addressInput);
            this.phoneNumber = phoneNumberInp;

            this.isFullInform = isEnoughInformation();
            this.isOpenInformInput = false;
            renderInform();
        } else {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin nhận hàng", Toast.LENGTH_SHORT).show();
        }
    }

    // =================================================================================
    // HÀM XỬ LÝ LƯU FIREBASE VÀ CHUYỂN ZALOPAY
    // =================================================================================
//điều kiện cộng thêm || user == null
    private void processOrderAndPayment() {
        FirebaseUser user = appFirebase.getFirebaseAuth().getCurrentUser();
        if (!isEnoughInformation()) {
            Toast.makeText(this, "Thông tin chưa đầy đủ hoặc lỗi đăng nhập!", Toast.LENGTH_LONG).show();
            return;
        }

        if (orderItems == null || orderItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng rỗng, không thể đặt hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đổi nút thành trạng thái đang xử lý để tránh bấm 2 lần
        binding.makeOrderBtn.setEnabled(false);
        binding.makeOrderBtn.setText("Đang tạo đơn hàng...");

        // 1. Chuẩn bị dữ liệu danh sách sản phẩm từ orderItems
        List<HashMap<String, Object>> orderProducts = orderItems.stream().map(product -> {
            HashMap<String, Object> result = new HashMap<>();
            result.put("name", product.getName());
            result.put("product", appFirebase.getProductsCollection().document(product.getId()));
            result.put("unitPrice", product.getPrice());
            result.put("quantity", product.getQuantity());
            return result;
        }).collect(Collectors.toList());
// appFirebase.getUsersCollection().document(user.getUid())
        // 2. Đóng gói toàn bộ đơn hàng
        HashMap<String, Object> order = new HashMap<>();
        order.put("user", "1");
        order.put("status", "paying"); // Trạng thái đang thanh toán ZaloPay
        order.put("created_date", FieldValue.serverTimestamp());
        order.put("updated_date", FieldValue.serverTimestamp());
        order.put("completed_date", null);
        order.put("payment_date", null);
        order.put("address", address);
        order.put("phone_number", phoneNumber);
        order.put("products", orderProducts);
        order.put("total_price", totalAmount);

        // 3. Đẩy lên Firebase Firestore
        appFirebase.getOrdersCollection().add(order).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String newOrderId = task.getResult().getId(); // Lấy ID đơn hàng Firebase vừa tạo

                // 4. Xóa sạch giỏ hàng trong Room Database (Chạy Thread ẩn để mượt UI)
                new Thread(() -> {
                    if (application.getRoomDB() != null) {
                        application.getRoomDB().cartDAO().deleteAll();
                    }
                }).start();

                // 5. Mở ZaloPay và truyền OrderID + Tổng tiền qua bên đó
                Intent intent = new Intent(MakeOrderActivity.this, ZaloPayPaymentActivity.class);
                intent.putExtra("orderId", newOrderId);
                intent.putExtra("totalPrice", totalAmount); // Truyền tổng tiền để ZaloPay tạo hóa đơn chính xác
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // Tránh người dùng bấm Back quay lại màn hình MakeOrder cũ
                startActivity(intent);

                // Kết thúc màn hình đặt hàng
                finish();

            } else {
                // Nếu lưu Firebase thất bại
                binding.makeOrderBtn.setEnabled(true);
                binding.makeOrderBtn.setText("Đặt hàng");
                Toast.makeText(this, "Lỗi khi tạo đơn hàng trên hệ thống!", Toast.LENGTH_LONG).show();
            }
        });
    }
}