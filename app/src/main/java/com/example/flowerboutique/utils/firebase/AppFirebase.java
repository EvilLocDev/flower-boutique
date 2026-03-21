package com.example.flowerboutique.utils.firebase;

import com.google.firebase.auth.FirebaseAuth;
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
}
