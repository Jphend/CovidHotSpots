package com.example.covidhotspots.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import com.example.covidhotspots.R;
import com.example.covidhotspots.SharedViewModel;
import com.example.covidhotspots.ui.login.LoginActivity;

public class SettingsFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private SwitchCompat heatmap;


    private SwitchCompat heatmapSimulation;
    private SwitchCompat displayAll;
    private SwitchCompat displayMine;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        logoutButton.setOnClickListener(v -> startActivity(intent));

        heatmap = view.findViewById(R.id.displayHeatmap);
        heatmap.setOnCheckedChangeListener((v, isChecked) -> sharedViewModel.setDisplayHeatmap(isChecked));

        heatmapSimulation = view.findViewById(R.id.displayHeatmapSimulation);

        heatmapSimulation.setOnCheckedChangeListener((v, isChecked) -> sharedViewModel.setDisplayHeatmapSimulation(isChecked));

        displayAll = view.findViewById(R.id.displayAll);

        displayAll.setOnCheckedChangeListener((buttonView, isChecked) -> sharedViewModel.setDisplayAll(isChecked));

        displayMine = view.findViewById(R.id.displayMy);

        displayMine.setOnCheckedChangeListener((buttonView, isChecked) -> sharedViewModel.setDisplayMine(isChecked));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getDisplayHeatmap().observe(getViewLifecycleOwner(), aBoolean -> heatmap.setChecked(aBoolean));

        sharedViewModel.getDisplayHeatmapSimulation().observe(getViewLifecycleOwner(), aBoolean -> heatmapSimulation.setChecked(aBoolean));

        sharedViewModel.getDisplayAll().observe(getViewLifecycleOwner(), aBoolean -> displayAll.setChecked(aBoolean));

        sharedViewModel.getDisplayMine().observe(getViewLifecycleOwner(), aBoolean -> displayMine.setChecked(aBoolean));

    }

}