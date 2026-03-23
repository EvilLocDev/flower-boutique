package com.example.flowerboutique.ui.main_home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.flowerboutique.ui.cart.CartActivity;

import java.util.List;

public class MainTabAdapter extends FragmentStateAdapter {
    List<Class<? extends Fragment>> pages = List.of(HomeFragment.class, CategoryFragment.class,
            CartFragment.class, ProfileFragment.class);

    public MainTabAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return pages.get(position).newInstance();
        } catch (IllegalAccessException | InstantiationException ignored) {
        }
        return new HomeFragment();
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}
