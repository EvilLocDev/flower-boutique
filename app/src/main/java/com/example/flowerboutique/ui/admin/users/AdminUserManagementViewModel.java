package com.example.flowerboutique.ui.admin.users;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flowerboutique.R;
import com.example.flowerboutique.utils.firebase.AppFirebase;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminUserManagementViewModel extends ViewModel {
    CollectionReference userCollection;
    FirebaseAuth firebaseAuth;
    List<OverviewUser> users = new ArrayList<>();
    OverviewUserAdapter adapter = new OverviewUserAdapter(users);

    public AdminUserManagementViewModel() {
        AppFirebase appFirebase = new AppFirebase();
        userCollection = appFirebase.getUsersCollection();
        firebaseAuth = appFirebase.getFirebaseAuth();
        loadData();
    }

    private void loadData() {
        userCollection.orderBy("role").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                snapshot.getDocuments().forEach(user -> {
                    users.add(new OverviewUser(user.getId(), user.getString("name"), user.getString("role"), user.getString("avatar")));
                });
                adapter.notifyDataSetChanged();
            }
        });
    }

    public List<OverviewUser> getUsers() {
        return users;
    }

    public OverviewUserAdapter getAdapter() {
        return adapter;
    }

    static class OverviewUserAdapter extends RecyclerView.Adapter<OverviewUserAdapter.OverviewUserViewHolder> {
        List<OverviewUser> users;
        OnClickUserHandler onClick;

        public OverviewUserAdapter(List<OverviewUser> users) {
            this.users = users;
        }

        public void setOnClick(OnClickUserHandler onClick) {
            this.onClick = onClick;
        }

        @NonNull
        @Override
        public OverviewUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new OverviewUserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull OverviewUserViewHolder holder, int position) {
            OverviewUser user = users.get(position);
            holder.nameTv.setText(user.name);
            holder.idTv.setText(String.format("ID: %s", user.id));
            holder.roleTv.setText(getRole(user.role));
            if (user.avatar != null && !user.avatar.isEmpty()) {
                Picasso.get().load(user.avatar).placeholder(R.drawable.ic_menu_profile).error(R.drawable.ic_menu_profile).into(holder.avatarIv);
            } else {
                holder.avatarIv.setImageResource(R.drawable.ic_menu_profile);
            }
            if (onClick != null) holder.view.setOnClickListener(v -> onClick.onClickUser(user));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static interface OnClickUserHandler {
            void onClickUser(OverviewUser user);
        }

        private String getRole(String role) {
            if (Objects.equals(role, "admin"))
                return "Quản trị viên";
            else if (Objects.equals(role, "customer"))
                return "Người dùng";
            return "Không xác định";
        }

        static class OverviewUserViewHolder extends RecyclerView.ViewHolder {
            TextView nameTv, idTv, roleTv;
            ShapeableImageView avatarIv;
            ImageView deleteBtn;
            View view;
            public OverviewUserViewHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;
                nameTv = itemView.findViewById(R.id.name_tv);
                idTv = itemView.findViewById(R.id.user_id);
                roleTv = itemView.findViewById(R.id.role);
                avatarIv = itemView.findViewById(R.id.avatar);
                deleteBtn = itemView.findViewById(R.id.delete_btn);
            }
        }
    }

    static class OverviewUser {
        String id;
        String name;
        String role;
        String avatar;

        public OverviewUser(String id, String name, String role, String avatar) {
            this.name = name;
            this.id = id;
            this.role = role;
            this.avatar = avatar;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }
    }
}
