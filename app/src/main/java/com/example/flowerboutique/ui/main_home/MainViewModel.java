package com.example.flowerboutique.ui.main_home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.viewpager2.widget.ViewPager2;

public class MainViewModel extends AndroidViewModel {
    public MainViewModel(Application application) {
        super(application);
    }

    static class PageChange extends ViewPager2.OnPageChangeCallback {
        Callback callback;

        public PageChange(Callback callback) {
            super();
            this.callback = callback;
        }

        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            callback.doCallback(position);
        }

        interface Callback {
            void doCallback(int position);
        }
    }
}
