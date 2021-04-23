package com.example.covidhotspots.ui.simulation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.covidhotspots.R;
import com.example.covidhotspots.SharedViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class SimulationFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private View mapView;
    private final ArrayList<LatLng> coordinates = new ArrayList<>();
    private SwitchCompat heatmap;
    private final List<Marker> markers = new ArrayList<>();
    private Marker marker;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View settings = inflater.inflate(R.layout.fragment_settings, container, false);
        heatmap = settings.findViewById(R.id.displayHeatmap);

        mapView = inflater.inflate(R.layout.fragment_simulation, container, false);
        return mapView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationButtonClickListener(this);

            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // Position button at bottom right
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);

            LocationManager locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            LocationListener locationListener = location -> {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                //Get the bounds of the current screen
                LatLngBounds curScreen = googleMap.getProjection()
                        .getVisibleRegion().latLngBounds;
                //loop through the marker list and if the current screen contains the marker position, make the marker visible
                for(Marker mar : markers) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    marker.setVisible(curScreen.contains(mar.getPosition()));
                }
            };
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                mMap.setMyLocationEnabled(true);

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

            }


            //Random number in between 50,000 to 100,000
            //int num = (int) (Math.random() * (100000 - 50000)) + 50000;
            //Toast.makeText(requireContext(), "Adding " + num + " Markers to map", Toast.LENGTH_LONG).show();


            for(int i = 0; i<50000; i++) {
                //Roughly the latitudes and longitudes of the UK
                double lat = 50 + (Math.random() * (60 - 50));
                double lng = -5 + (Math.random() * (2 - -5));
                LatLng latLng = new LatLng(lat, lng);
                coordinates.add(latLng);
                i++;
            }

            //If the array coordinates is not empty, loop through it and add a marker to another array list
            if (!coordinates.isEmpty()) {
                for (LatLng coord : coordinates) {
                    marker = mMap.addMarker(new MarkerOptions().position(coord));
                    markers.add(marker);
                    //System.out.println(coord);
                }
                //if the heatmap setting is checked, add the overlay
                if(heatmap.isChecked()) {
                    addHeatMap();
                }
            }
        }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedViewModel sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getDisplayHeatmapSimulation().observe(getViewLifecycleOwner(), aBoolean -> heatmap.setChecked(aBoolean));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.SimulateMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void addHeatMap() {
        mMap.clear();
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .data(coordinates)
                .build();
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider)).setVisible(true);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
}