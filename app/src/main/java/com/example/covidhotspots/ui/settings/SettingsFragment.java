package com.example.covidhotspots.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceFragmentCompat;
import com.example.covidhotspots.MainActivity;
import com.example.covidhotspots.R;
import com.example.covidhotspots.ui.home.HomeFragment;
import com.example.covidhotspots.ui.login.LoginActivity;

import java.util.Objects;

public class SettingsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        logoutButton.setOnClickListener(v -> startActivity(intent));
        return view;
    }

//    @Override
//    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
//        setPreferencesFromResource(R.xml.root_preferences, rootKey);
//    }

}