package com.example.covidhotspots.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.covidhotspots.ui.login.LoginActivity;
import com.example.covidhotspots.R;
import com.example.covidhotspots.Retrofit.RetrofitClient;
import com.example.covidhotspots.Retrofit.Service;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rengwuxian.materialedittext.MaterialEditText;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    //private HomeViewModel homeViewModel;
    private GoogleMap mMap;
    private View mapView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Service service;
    private static String userEmail = LoginActivity.getEmail();
    private final ArrayList<LatLng> userCoordinates = LoginActivity.getCoordinates();
    private static Location lastKnown;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Retrofit retrofitClient = RetrofitClient.getInstance();
        service = retrofitClient.create(Service.class);
        mapView = inflater.inflate(R.layout.fragment_home, container, false);
        return mapView;
    }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);

            //Sets entire map padding, repositions location button but also repositions camera
            //mMap.setPadding(0,2100,0,0);

            //find location button
            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);

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
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                mMap.setMyLocationEnabled(true);

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                lastKnown = lastKnownLocation;

                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

            }
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

            if(!userCoordinates.isEmpty()) {
                for (LatLng coord : userCoordinates) {
                    mMap.addMarker(new MarkerOptions().position(coord));
                    //System.out.println(coord);
                }
            }

            MaterialEditText editSearch = requireView().findViewById(R.id.editText);

            editSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchLocation();
                    return true;
                }
                return false;
            });
        }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

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

    public void logout() {
        userEmail = null;
        openLoginPage();
        //finish();
    }

    private void openLoginPage() {
        Intent intent = new Intent(getContext(), LoginActivity.class);
        startActivity(intent);
    }

    public static Location getLastKnownLocation() {
        return lastKnown;
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