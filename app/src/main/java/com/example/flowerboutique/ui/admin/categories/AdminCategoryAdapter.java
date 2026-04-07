package com.example.flowerboutique.ui.admin.categories;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;

import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder> {
    List<CategoryItem> categoryItems;
    OnClick onClickView, onDelete;

    public AdminCategoryAdapter (List<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryItem categoryItem = categoryItems.get(position);
        holder.name.setText(categoryItem.name);
        if (onDelete != null) holder.deleteBtn.setOnClickListener(v -> onDelete.onClick(categoryItem));
        if (onClickView != null) holder.view.setOnClickListener(v -> onClickView.onClick(categoryItem));
    }

    @Override
    public int getItemCount() {
        return categoryItems.size();
    }

    public void setOnClickView(OnClick onClickView) {
        this.onClickView = onClickView;
    }

    public void setOnDelete(OnClick onDelete) {
        this.onDelete = onDelete;
    }

    static public interface OnClick {
        void onClick(CategoryItem categoryItem);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView deleteBtn;
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            name = itemView.findViewById(R.id.name);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }

    public static class CategoryItem {
        String id;
        String name;

        public CategoryItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
