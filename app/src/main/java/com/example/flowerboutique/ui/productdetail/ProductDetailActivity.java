package com.example.flowerboutique.ui.productdetail;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.databinding.ActivityProductDetailBinding;
import com.example.flowerboutique.db.RoomDB;
import com.example.flowerboutique.ui.cart.AddCartDialog;
import com.example.flowerboutique.ui.cart.CartActivity;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateField;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private RoomDB roomDB;
    private final AppFirebase appFirebase = new AppFirebase();
    private final Locale locale = new Locale("vi", "vn");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
    private final DecimalFormat decimalFormat = new DecimalFormat("#.#");
    private AuthenDialog loginDialog;
    private DialogFragment addReviewDialog;

    private RecyclerView reviewsRecyclerView;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        roomDB = BoutiqueApplication.getInstance().getRoomDB();

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reviewsRecyclerView = findViewById(R.id.reviews_recycler_view);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(new ArrayList<>());
        reviewsRecyclerView.setAdapter(reviewAdapter);
        loginDialog = new AuthenDialog();
        addReviewDialog = new AddReviewDialog((reviewText, rate) -> {
            String productId = getIntent().getStringExtra("id");
            if (productId != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference productRef = db.collection("products").document(productId);


                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DocumentReference userRef = db.collection("users").document(userId);


                Timestamp currentDate = Timestamp.now();
                Review newReview = new Review(reviewText, rate, userRef, currentDate, currentDate);

                productRef.collection("reviews").add(newReview)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(ProductDetailActivity.this, "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                            fetchProduct(); // Refresh reviews and product average rating
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProductDetailActivity.this, "Đánh giá thất bại!", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        binding.backBtn.setOnClickListener(v -> finish());
        binding.cartBtn.setOnClickListener(v -> {
            Intent cartIntent = new Intent(this, CartActivity.class);
            cartIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(cartIntent);
        });

        binding.addReviewBtn.setOnClickListener(v -> {
            showAddReviewDialog();
        });

        fetchProduct();
    }

    private void fetchProduct() {
        Intent intent = getIntent();
        String id = intent.getStringExtra("id");
        if (id == null || id.isEmpty()) {
            failFetch(null);
            return;
        }

        appFirebase.getProductsCollection().document(id).get().addOnSuccessListener(snapshot -> {
            List<String> images = (List<String>) snapshot.get("image");
            List<DocumentReference> categories = (List<DocumentReference>) snapshot.get("categories");
            String productName = snapshot.getString("name");
            String description = snapshot.getString("description");
            Long price = snapshot.getLong("price");

            if (images == null || images.isEmpty() || categories == null || categories.isEmpty() || productName == null || description == null || price == null) {
                failFetch(id);
                return;
            }

            categories.get(0).get().addOnSuccessListener(categorySnapshot -> {
                binding.productCategory.setText(categorySnapshot.getString("name"));
            });

            snapshot.getReference().collection("reviews")
                    .aggregate(AggregateField.average("rate"))
                    .get(AggregateSource.SERVER)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().get(AggregateField.average("rate")) != null) {
                            double averageRate = task.getResult().get(AggregateField.average("rate"));
                            binding.rate.setText(decimalFormat.format(averageRate));
                        } else {

                            binding.rate.setText("Chưa có đánh giá");
                        }
                    });

            binding.productName.setText(productName);
            binding.productDescription.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
            binding.productPrice.setText(currencyFormat.format(price));

            Picasso.get().load(images.get(0)).into(binding.productImg);

            binding.addCartBtn.setOnClickListener(v -> {
                roomDB.cartDAO().increaseProductInCart(snapshot.getId());
                AddCartDialog dialog = new AddCartDialog();
                dialog.show(getSupportFragmentManager(), AddCartDialog.TAG);
            });


            appFirebase.getProductsCollection()
                    .document(id) // Sử dụng id của sản phẩm
                    .collection("reviews").get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Review> reviews = queryDocumentSnapshots.toObjects(Review.class);
                        reviewAdapter.setReviews(reviews);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ProductDetailActivity", "Lỗi khi lấy đánh giá", e);
                        Toast.makeText(ProductDetailActivity.this, "Lỗi khi lấy đánh giá", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(command -> {
            failFetch(id);
        });
    }

    private void failFetch(@Nullable String id) {
        Toast.makeText(this, "Xin lỗi vì có sự cố!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showAddReviewDialog() {
        if (appFirebase.getFirebaseAuth().getCurrentUser() != null)
            addReviewDialog.show(getSupportFragmentManager(), "AddReviewDialog");
        else
            loginDialog.show(getSupportFragmentManager(), "RequireAuthenDialog");
    }
}