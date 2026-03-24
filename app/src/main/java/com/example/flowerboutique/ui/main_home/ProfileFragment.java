package com.example.flowerboutique.ui.main_home;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.flowerboutique.BoutiqueApplication;
import com.example.flowerboutique.R;
import com.example.flowerboutique.ui.profile.LoginRegisterFragment;
import com.example.flowerboutique.ui.profile.ProfileMenuFragment;
import com.google.firebase.auth.FirebaseUser;


public class ProfileFragment extends Fragment {

    BoutiqueApplication application = BoutiqueApplication.getInstance();
    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_fragment_profile, container, false);
        renderFragment(application.getAppFirebase().getFirebaseAuth().getCurrentUser());
        return view;
    }

    private void renderFragment(FirebaseUser firebaseUser) {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (firebaseUser == null) {
            fragmentManager.beginTransaction().replace(R.id.main_profile, LoginRegisterFragment.class, null).commit();
        } else {
            fragmentManager.beginTransaction().replace(R.id.main_profile, ProfileMenuFragment.class, null).commit();
        }
    }
}