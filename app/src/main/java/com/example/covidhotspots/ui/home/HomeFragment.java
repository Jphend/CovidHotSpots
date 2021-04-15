package com.example.covidhotspots.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.covidhotspots.R;
import com.example.covidhotspots.Retrofit.RetrofitClient;
import com.example.covidhotspots.Retrofit.Service;
import com.example.covidhotspots.SharedViewModel;
import com.example.covidhotspots.ui.login.LoginActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.rengwuxian.materialedittext.MaterialEditText;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Retrofit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private View mapView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Service service;
    private static final String userEmail = LoginActivity.getEmail();
    private static final List<LatLng> userCoordinates = LoginActivity.getCoordinates();
    private SwitchCompat heatmap;
    private SwitchCompat displayAll;
    private SwitchCompat displayMine;

    //When user allows permission, location manager request location updates which later allows the camera to be moved to the user
    //Also sets the user location to show up on the map and enables the location button
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMyLocationButtonClickListener(this);
            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Initialise retrofit client and begin the API service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        service = retrofitClient.create(Service.class);
        //grab settings view so setting switches can be found
        View settings = inflater.inflate(R.layout.fragment_settings, container, false);
        //initialise settings switches
        heatmap = settings.findViewById(R.id.displayHeatmap);
        displayAll = settings.findViewById(R.id.displayAll);
        displayMine = settings.findViewById(R.id.displayMy);

        mapView = inflater.inflate(R.layout.fragment_home, container, false);
        return mapView;
    }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            //Sets entire map padding, repositions location button but also repositions camera
            //mMap.setPadding(0,2100,0,0);

            //find location button
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // Position button at bottom right
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);

            locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    //mMap.clear();
                    //mMap.addMarker(new MarkerOptions().position(userLocation).title("Your Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }

            };

            //Check permissions, set user location and get last known location
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

            }

            //Add marker on map click
            mMap.setOnMapClickListener(point -> {

                mMap.addMarker(new MarkerOptions().position(point));

                double lat = point.latitude;
                double lng = point.longitude;

                compositeDisposable.add(service.saveClickLocation(userEmail, lat ,lng)

                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            //Toast.makeText(MapsActivity.this, ""+response, Toast.LENGTH_LONG).show();
                        }));
            });

            //Display all locations from array populated in Login Activity
            if(!userCoordinates.isEmpty()) {
                if(displayAll.isChecked()) {
                    for (LatLng coord : userCoordinates) {
                        mMap.addMarker(new MarkerOptions().position(coord));
                        //System.out.println(coord);
                    }
                }

            }

            // if the display my locations setting is checked, display current user's location entries
            if(displayMine.isChecked()) {
                compositeDisposable.add(service.getLocations(userEmail)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {
                            mMap.clear();
                            JSONArray arr = new JSONArray(res);
                            if(!(arr.isNull(0))) {
                                JSONObject obj = arr.getJSONObject(0);
                                String[] lngs = obj.getString("lng").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                                String[] lats = obj.getString("lat").replaceAll("\\[", "").replaceAll("\\]", "").split(",");

                                if (lats.length == lngs.length) {
                                    for (int i = 0; i < lats.length; i++) {
                                        String lat = lats[i];
                                        String lng = lngs[i];
                                        double a = Double.parseDouble(lat);
                                        double b = Double.parseDouble(lng);
                                        LatLng latLng = new LatLng(a, b);
                                        mMap.addMarker(new MarkerOptions().position(latLng));
                                    }
                                }
                            }

                        }));
            }

            //Set listener on search bar so search method is called when used
            MaterialEditText editSearch = requireView().findViewById(R.id.editText);

            editSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchLocation();
                    return true;
                }
                return false;
            });

            //if the heatmap setting is on, display heatmap
            if(heatmap.isChecked()) {
                addHeatMap();
            }

        }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    // Set ViewModel listener so that settings retain state
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SharedViewModel sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getDisplayHeatmap().observe(getViewLifecycleOwner(), aBoolean -> heatmap.setChecked(aBoolean));

        sharedViewModel.getDisplayAll().observe(getViewLifecycleOwner(), aBoolean -> displayAll.setChecked(aBoolean));

        sharedViewModel.getDisplayMine().observe(getViewLifecycleOwner(), aBoolean -> displayMine.setChecked(aBoolean));
    }

    //Create heat map
    private void addHeatMap() {
        mMap.clear();
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .data(userCoordinates)
                .build();
        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider)).setVisible(true);
    }

    @Override
    //Generated code to manage the map
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    //Search a user entered location
    public void searchLocation() {
        MaterialEditText editSearch = requireView().findViewById(R.id.editText);

        String location = Objects.requireNonNull(editSearch.getText()).toString();
        List<Address> addressList = new ArrayList<>();

        Geocoder geocoder = new Geocoder(getContext());
        try {
            addressList = geocoder.getFromLocationName(location, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        assert addressList != null;
        Address address = addressList.get(0);
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
        double lat = address.getLatitude();
        double lng = address.getLatitude();

        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        compositeDisposable.add(service.saveSearchLocation(userEmail, lat, lng)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {

                }));

    }

    @Override
    //Manage the location button click
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    // Get current location when location button clicked
    public void onMyLocationClick(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }
        }