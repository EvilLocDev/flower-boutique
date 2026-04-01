package com.example.flowerboutique.ui.main_home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.flowerboutique.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.flowerboutique.ui.cart.CartActivity;
import com.example.flowerboutique.ui.categorydetail.CategoryDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class HomeLinearAdapter extends RecyclerView.Adapter<HomeLinearAdapter.ViewHolder> {
    @NonNull
    @Override
    public HomeLinearAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_home_linear, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        loadUserProfile(holder);

        holder.seeMoreCategories.setOnClickListener(v -> {
            BottomNavigationView bottomNav = holder.rootView.getRootView().findViewById(R.id.bottom_navigation);
            bottomNav.setSelectedItemId(R.id.nav_categories);
        });

        holder.cartBtn.setOnClickListener(v -> {
            Intent intent = new Intent(holder.context, CartActivity.class);
            holder.context.startActivity(intent);
        });

        holder.weddingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(holder.context, CategoryDetailActivity.class);
            intent.putExtra("id", "hoa_cuoi");
            holder.context.startActivity(intent);
        });

        holder.anniBtn.setOnClickListener(v -> {
            Intent intent = new Intent(holder.context, CategoryDetailActivity.class);
            intent.putExtra("id", "hoa_ky_niem");
            holder.context.startActivity(intent);
        });

        holder.congrateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(holder.context, CategoryDetailActivity.class);
            intent.putExtra("id", "hoa_chuc_mung");
            holder.context.startActivity(intent);
        });

        holder.cayCanhBtn.setOnClickListener(v -> {
            Intent intent = new Intent(holder.context, CategoryDetailActivity.class);
            intent.putExtra("id", "cay_canh");
            holder.context.startActivity(intent);
        });
    }

    private void loadUserProfile(ViewHolder holder) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users")
                    .document(currentUser.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String name = document.getString("name");
                            String avatar = document.getString("avatar");

                            holder.profileName.setText(name != null ? name : "User");
                            if (avatar != null && !avatar.isEmpty()) {
                                Picasso.get().load(avatar)
                                        .placeholder(R.drawable.ic_placeholder)
                                        .error(R.drawable.ic_placeholder)
                                        .into(holder.profileImage);
                            }
                        }
                    });
        } else {
            holder.profileName.setText("Username");
            holder.profileImage.setImageResource(R.drawable.ic_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView seeMoreCategories;
        private final TextView profileName;
        private final ImageView profileImage;
        private final Button cartBtn;
        private final View rootView;
        private final Context context;
        private final Button weddingBtn;
        private final Button anniBtn;
        private final Button congrateBtn;
        private final Button cayCanhBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.seeMoreCategories = itemView.findViewById(R.id.see_more_categories);
            this.profileName = itemView.findViewById(R.id.profile_name);
            this.profileImage = itemView.findViewById(R.id.profile_image);
            this.cartBtn = itemView.findViewById(R.id.cart_btn);
            this.weddingBtn = itemView.findViewById(R.id.wedding_btn);
            this.anniBtn = itemView.findViewById(R.id.birthday_btn);
            this.congrateBtn = itemView.findViewById(R.id.congratulate_btn);
            this.cayCanhBtn = itemView.findViewById(R.id.office_btn);
            this.rootView = itemView.getRootView();
            this.context = itemView.getContext();
        }
    }
}
