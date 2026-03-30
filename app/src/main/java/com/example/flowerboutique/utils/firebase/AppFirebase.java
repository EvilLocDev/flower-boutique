package com.example.flowerboutique.utils.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser; // Thêm import này
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AppFirebase {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public CollectionReference getProductsCollection() {
        return firestore.collection("products");
    }

    public CollectionReference getCategoriesCollection() {
        return firestore.collection("categories");
    }

    public CollectionReference getUsersCollection() {
        return firestore.collection("users");
    }

    public CollectionReference getOrdersCollection() {
        return firestore.collection("orders");
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    // =========================================================
    // HÀM MỚI THÊM VÀO ĐỂ LẤY TÊN HOẶC EMAIL USER HIỆN TẠI
    // =========================================================
    public String getCurrentName() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Nếu có tên hiển thị (DisplayName) thì trả về tên
            if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                return user.getDisplayName();
            }
            // Nếu không có tên, trả về Email. Nếu không có cả Email thì trả về UID
            return user.getEmail() != null ? user.getEmail() : user.getUid();
        }
        // Trả về "Guest" nếu người dùng chưa đăng nhập
        return "Guest";
    }
}