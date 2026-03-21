package com.example.flowerboutique;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO: Kiểm tra và sửa lại các dòng import dưới đây cho đúng với cấu trúc thư mục của bạn
import com.example.flowerboutique.db.RoomDB; // File cấu hình Database
import com.example.flowerboutique.db.entities.CartEntity; // Model thực thể của giỏ hàng trong Room
import com.example.flowerboutique.ui.cart.CartItem; // Model giỏ hàng hiển thị trên UI
import com.example.flowerboutique.utils.firebase.AppFirebase; // File cấu hình Firebase

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPaySDK;

public class BoutiqueApplication extends Application {

    // Biến Singleton
    static private BoutiqueApplication instance;

    // Các công cụ và dữ liệu dùng chung
    private AppFirebase appFirebase;
    private RoomDB roomDB;
    private LiveData<List<CartEntity>> cartEntitiesLiveData;
    private final MutableLiveData<List<CartItem>> cartItemsLiveData = new MutableLiveData<>(new ArrayList<>());

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Khởi tạo Firebase mặc định
        FirebaseApp.initializeApp(this);

        // 2. Lưu instance
        BoutiqueApplication.instance = this;

        // 3. Khởi tạo ZaloPay
        // Lưu ý: Đảm bảo bạn đã khai báo ZALO_PAY_APP_ID trong file build.gradle hoặc local.properties
        ZaloPaySDK.init(Integer.parseInt(BuildConfig.ZALO_PAY_APP_ID), Environment.SANDBOX);

        // 4. Khởi tạo Cloudinary (Upload ảnh)
        // Lưu ý: Đảm bảo bạn đã khai báo các khóa này trong file cấu hình
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", BuildConfig.CLOUDINARY_NAME);
        config.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        config.put("api_secret", BuildConfig.CLOUDINARY_KEY_SECRET);
        config.put("secure", true);
        MediaManager.init(this, config);

        // 5. Khởi tạo AppFirebase và Room Database
        appFirebase = new AppFirebase();
        roomDB = Room.databaseBuilder(this.getApplicationContext(), RoomDB.class, "flowerboutique_db")
                .allowMainThreadQueries()
                .build();

        // 6. Lắng nghe và đồng bộ dữ liệu Giỏ hàng từ RoomDB và Firebase
        cartEntitiesLiveData = roomDB.cartDAO().getAllInCart();

        cartEntitiesLiveData.observeForever(data -> {
            List<CartItem> cartItems = new ArrayList<>();
            List<String> productsId = data.stream().map(CartEntity::getProduct_id).collect(Collectors.toList());

            // Nếu giỏ hàng rỗng
            if (productsId.isEmpty()) {
                cartItemsLiveData.setValue(cartItems);
                return;
            }

            // Gọi lên Firebase lấy chi tiết sản phẩm
            appFirebase.getProductsCollection().whereIn(FieldPath.documentId(), productsId).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) return;
                QuerySnapshot result = task.getResult();
                List<CartEntity> productsInCart = cartEntitiesLiveData.getValue();

                if (productsInCart != null) {
                    result.getDocuments().forEach(product -> {
                        Optional<CartEntity> cartEntity = productsInCart.stream()
                                .filter(p -> p.getProduct_id().equals(product.getId()))
                                .findFirst();

                        if (!cartEntity.isPresent()) {
                            cartItemsLiveData.setValue(cartItems);
                            return;
                        }

                        // Lấy danh sách hình ảnh, chọn ảnh đầu tiên (index 0)
                        List<String> images = (List<String>) product.get("image");
                        String imageUrl = (images != null && !images.isEmpty()) ? images.get(0) : "";

                        // Tạo đối tượng CartItem
                        cartItems.add(new CartItem(
                                product.getId(),
                                imageUrl,
                                product.getString("name"),
                                product.getLong("price"),
                                cartEntity.get().getQuantity(),
                                cartEntity.get().getId()
                        ));
                    });
                }
                // Cập nhật lên LiveData để giao diện tự động đổi
                cartItemsLiveData.setValue(cartItems);
            });
        });
    }

    // ==========================================
    // CÁC HÀM GETTER ĐỂ GỌI TỪ ACTIVITY/FRAGMENT
    // ==========================================

    public static BoutiqueApplication getInstance() {
        return instance;
    }

    public MutableLiveData<List<CartItem>> getCartItemsLiveData() {
        return cartItemsLiveData;
    }

    public RoomDB getRoomDB() {
        return roomDB;
    }

    public AppFirebase getAppFirebase() {
        return appFirebase;
    }
}