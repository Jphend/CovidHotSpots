package com.example.covidhotspots.ui.simulation;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.covidhotspots.R;
import com.example.covidhotspots.Retrofit.RetrofitClient;
import com.example.covidhotspots.Retrofit.Service;
import com.example.covidhotspots.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Retrofit;

public class SimulationFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private View mapView;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Service service;
    private static final Location lastKnown = HomeFragment.getLastKnownLocation();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        Retrofit retrofitClient = RetrofitClient.getInstance();
        service = retrofitClient.create(Service.class);
        mapView = inflater.inflate(R.layout.fragment_simulation, container, false);
        return mapView;
    }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);

            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);

            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // Position button at bottom right
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            rlp.setMargins(0, 0, 30, 30);

            locate();

            Button show = requireView().findViewById(R.id.showAllButton);

            show.setOnClickListener((v) -> compositeDisposable.add(service.getAll()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        //Toast.makeText(MapsActivity.this, ""+response, Toast.LENGTH_LONG).show();
                        JSONArray arr = new JSONArray(response);
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
                                //coords.add(latLng);
                            }
                        }
                    })));

            RadioButton button = requireView().findViewById(R.id.showAllButton);


            View.OnClickListener listener = v -> {

            };

            button.setOnClickListener(listener);


        }
    //};

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.SimulateMap);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void locate() {
        LatLng userLocation = new LatLng(SimulationFragment.lastKnown.getLatitude(), SimulationFragment.lastKnown.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
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