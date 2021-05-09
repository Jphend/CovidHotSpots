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
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.covidhotspots.R;
import com.example.covidhotspots.Retrofit.RetrofitClient;
import com.example.covidhotspots.Retrofit.Service;
import com.example.covidhotspots.SharedViewModel;
import com.example.covidhotspots.ui.login.LoginActivity;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.Task;
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
    private Service service;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static final String userEmail = LoginActivity.getEmail();

    private static final ArrayList<LatLng> allCoordinates = LoginActivity.getCoordinates();

    private SwitchCompat heatmap;
    private SwitchCompat displayAll;
    private SwitchCompat displayMine;

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

        View welcome_layout = LayoutInflater.from(requireContext())
                .inflate(R.layout.welcome_layout, null);

        new MaterialStyledDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_simulate)
                .setTitle("WELCOME")
                .setCustomView(welcome_layout)
                .onNegative((dialog, which) -> dialog.dismiss())
                .setPositiveText("OK")
                .onPositive((dialog, which) -> {
                            TextView welcomeText = welcome_layout.findViewById(R.id.welcome);

                }).show();



        return mapView;
    }

    //When user allows permission, location manager request location updates which later allows the camera to be moved to the user
    //Also sets the user location to show up on the map and enables the location button
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                mMap.setMyLocationEnabled(true);
            }
        }
    }

        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationButtonClickListener(this);

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

            locationListener = location -> {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            };

            //Check permissions, set user location and get last known location, enables location features
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                //LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                FusedLocationProviderClient myProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());
                try {
                    Task<Location> location = myProviderClient.getLastLocation();
                    location.addOnCompleteListener(task -> {
                        if(task.isSuccessful()) {
                            Location currentLocation = task.getResult();
                            LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        }
                        else{
                            System.out.println("fail");
                        }
                    });
                }
                catch (SecurityException e) {
                    System.out.println(e.getMessage());
                }
                mMap.setMyLocationEnabled(true);
            }

            //Add marker on map click
            mMap.setOnMapClickListener(point -> {
                //Add a marker to the map
                mMap.addMarker(new MarkerOptions().position(point));
                //get the latitude and longitude of the marker
                double lat = point.latitude;
                double lng = point.longitude;
                // pass the latitude and longitude to the backend save click location function found in index.js
                compositeDisposable.add(service.saveClickLocation(userEmail, lat ,lng)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {

                        }));
            });

            //Display all locations
            if(!allCoordinates.isEmpty()) {
                if(displayAll.isChecked()) {
                    for (LatLng coord:allCoordinates) {
                        mMap.addMarker(new MarkerOptions().position(coord));
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
                            //Assign string result to JSON array (result if populated has only one very long entry
                            JSONArray arr = new JSONArray(res);
                            //If it is not null
                            if(!(arr.isNull(0))) {
                                //assign to JSON object
                                JSONObject obj = arr.getJSONObject(0);
                                //Split longitude values into string array, cutting off the opening and closing array brackets and splitting by commas
                                String[] lngs = obj.getString("lng").replaceAll("\\[", "")
                                        .replaceAll("]", "").split(",");
                                //Same for latitude values
                                String[] lats = obj.getString("lat").replaceAll("\\[", "")
                                        .replaceAll("]", "").split(",");
                                //If latitude and longitude arrays match in length
                                if (lats.length == lngs.length) {
                                    for (int i = 0; i < lats.length; i++) {
                                        String lat = lats[i];
                                        String lng = lngs[i];
                                        //Parse the string in the array and assign to a double
                                        double a = Double.parseDouble(lat);
                                        double b = Double.parseDouble(lng);
                                        //Create a LatLng value from the doubles
                                        LatLng latLng = new LatLng(a, b);
                                        //Place on map
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
        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getDisplayHeatmap().observe(getViewLifecycleOwner(), aBoolean -> heatmap.setChecked(aBoolean));
        sharedViewModel.getDisplayAll().observe(getViewLifecycleOwner(), aBoolean -> displayAll.setChecked(aBoolean));
        sharedViewModel.getDisplayMine().observe(getViewLifecycleOwner(), aBoolean -> displayMine.setChecked(aBoolean));
    }

    //Create heat map
    private void addHeatMap() {
        mMap.clear();
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .data(allCoordinates)
                .build();
        Objects.requireNonNull(mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider))).setVisible(true);
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
        //find search bar layout
        MaterialEditText editSearch = requireView().findViewById(R.id.editText);
        //assign entry in seach bar to a string
        String location = Objects.requireNonNull(editSearch.getText()).toString();
        //create an array of addresses
        List<Address> addressList = new ArrayList<>();
        //Create a geocoder that will find the geolocation from an address and add it to the list
        Geocoder geocoder = new Geocoder(getContext());
        try {
            addressList = geocoder.getFromLocationName(location, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
        //as long as address is not null assign the first result from the address list as an address
        assert addressList != null;
        Address address = addressList.get(0);
        //Get the lat/lng values from the address
        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        //add a map marker at the given location and move the map to be centred on the location
        mMap.addMarker(new MarkerOptions().position(latLng).title(location));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        double lat = address.getLatitude();
        double lng = address.getLatitude();

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