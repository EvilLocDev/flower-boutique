package com.example.flowerboutique.ui.productdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.google.firebase.firestore.DocumentReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.contentTextView.setText(review.getContent());

        DocumentReference userRef = review.getUser();
        if (userRef != null) {
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String userName = documentSnapshot.getString("name");
                    String avatarUrl = documentSnapshot.getString("avatar");

                    if (userName != null) {
                        holder.userNameTextView.setText(userName);
                    }

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Picasso.get()
                                .load(avatarUrl)
                                .placeholder(R.drawable.ic_menu_profile)
                                .error(R.drawable.ic_menu_profile)
                                .into(holder.avatarImageView);
                    } else {
                        holder.avatarImageView.setImageResource(R.drawable.ic_menu_profile);
                    }
                }
            });
        }

        // Hiển thị sao
        float rate = (float) review.getRate();
        holder.ratingBar.setRating(rate);
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public void setReviews(List<Review> reviews) {
        this.reviewList = reviews;
        notifyDataSetChanged();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView contentTextView, userNameTextView;
        RatingBar ratingBar;
        ImageView avatarImageView;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            contentTextView = itemView.findViewById(R.id.review_content);
            userNameTextView = itemView.findViewById(R.id.user_name);
            ratingBar = itemView.findViewById(R.id.review_rate);
            avatarImageView = itemView.findViewById(R.id.review_avatar);
        }
    }
}