package com.example.flowerboutique.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.flowerboutique.databinding.ItemCategoryBinding;
import com.example.flowerboutique.db.entities.CategoryEntity;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryEntity> list;

    public CategoryAdapter(List<CategoryEntity> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity item = list.get(position);
        holder.binding.tvCategoryName.setText(item.getName());

        // Dùng Glide để load ảnh từ Firebase của Lộc
        Glide.with(holder.itemView.getContext())
                .load(item.getThumbnail())
                .into(holder.binding.imgCategory);
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryBinding binding;
        public CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}