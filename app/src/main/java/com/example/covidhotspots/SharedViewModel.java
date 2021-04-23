package com.example.covidhotspots;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Boolean> displayHeatmap = new MutableLiveData<>();


    private final MutableLiveData<Boolean> displayHeatmapSimulation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayAll = new MutableLiveData<>();
    private final MutableLiveData<Boolean> displayMine = new MutableLiveData<>();

    public SharedViewModel() {

    }

    public void setDisplayHeatmap(Boolean val) {
        displayHeatmap.setValue(val);
    }

    public LiveData<Boolean> getDisplayHeatmap() {
        return displayHeatmap;
    }

    public void setDisplayAll(Boolean val) {
        displayAll.setValue(val);
    }

    public LiveData<Boolean> getDisplayAll() {
        return displayAll;
    }

    public void setDisplayMine(Boolean val) {
        displayMine.setValue(val);
    }

    public LiveData<Boolean> getDisplayMine() {
        return displayMine;
    }

    public void setDisplayHeatmapSimulation(Boolean val) {
        displayHeatmapSimulation.setValue(val);
    }

    public LiveData<Boolean> getDisplayHeatmapSimulation() {
        return displayHeatmapSimulation;
    }


}