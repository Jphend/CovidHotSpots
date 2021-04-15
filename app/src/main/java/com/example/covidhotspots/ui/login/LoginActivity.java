package com.example.covidhotspots.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.covidhotspots.MainActivity;
import com.example.covidhotspots.R;
import com.example.covidhotspots.Retrofit.RetrofitClient;
import com.example.covidhotspots.Retrofit.Service;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.gms.maps.model.LatLng;
import com.rengwuxian.materialedittext.MaterialEditText;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private MaterialEditText editEmail, editPassword;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Service service;
    private static String userEmail;
    private static List<LatLng> userCoordinates = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Retrofit retrofitClient = RetrofitClient.getInstance();
        service = retrofitClient.create(Service.class);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> loginUser(Objects.requireNonNull(editEmail.getText()).toString(), Objects.requireNonNull(editPassword.getText()).toString()));

        // When create account text is clicked, open up pop up menu allowing user to enter details
        TextView createAccount = findViewById(R.id.createAccount);
        createAccount.setOnClickListener(view -> {
            View register_layout = LayoutInflater.from(LoginActivity.this)
                    .inflate(R.layout.register_layout, null);

            new MaterialStyledDialog.Builder(LoginActivity.this)
                    .setIcon(R.drawable.ic_account)
                    .setTitle("REGISTRATION")
                    .setDescription("Please fill all fields")
                    .setCustomView(register_layout)
                    .setNegativeText("CANCEL")
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .setPositiveText("REGISTER")
                    .onPositive((dialog, which) -> {
                        MaterialEditText editRegisterEmail = register_layout.findViewById(R.id.editEmail);
                        MaterialEditText editRegisterName = register_layout.findViewById(R.id.editName);
                        MaterialEditText editRegisterPassword = register_layout.findViewById(R.id.editPassword);

                        if(TextUtils.isEmpty(Objects.requireNonNull(editRegisterEmail.getText()).toString()))
                        {
                            Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(TextUtils.isEmpty(Objects.requireNonNull(editRegisterName.getText()).toString()))
                        {
                            Toast.makeText(LoginActivity.this, "Name cannot be empty", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(TextUtils.isEmpty(Objects.requireNonNull(editRegisterPassword.getText()).toString()))
                        {
                            Toast.makeText(LoginActivity.this, "Password cannot be empty", Toast.LENGTH_LONG).show();
                            return;
                        }
                        registerUser(editRegisterEmail.getText().toString(), editRegisterName.getText().toString(), editRegisterPassword.getText().toString());
                    }).show();
        });
    }

    private void loginUser(String email, String password) {
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "Email cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(getApplicationContext(), "Password cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        //Intent allows for navigation to the new activity that is declared
        Intent intent = new Intent(this, MainActivity.class);
        ArrayList<LatLng> coords = new ArrayList<>();

        //System.out.println(email);

        //API call to log user in
        compositeDisposable.add(service.loginUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_LONG).show();
                    if(response.contains("Success")) {
                        setEmail(email);

                        compositeDisposable.add(service.getAll()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(res -> {
                                    //Toast.makeText(MapsActivity.this, ""+response, Toast.LENGTH_LONG).show();

                                    //Parse result into, 1st a JSON array, 2nd a JSON object
                                    JSONArray arr = new JSONArray(res);
                                    JSONObject obj = arr.getJSONObject(0);
                                    String[] lngs = obj.getString("lng").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
                                    String[] lats = obj.getString("lat").replaceAll("\\[", "").replaceAll("\\]", "").split(",");

                                    //if lats and lngs arrays are the same size (should always be true)
                                    if (lats.length == lngs.length) {
                                        for (int i = 0; i < lats.length; i++) {
                                            String lat = lats[i];
                                            String lng = lngs[i];
                                            double a = Double.parseDouble(lat);
                                            double b = Double.parseDouble(lng);
                                            LatLng latLng = new LatLng(a, b);
                                            //mMap.addMarker(new MarkerOptions().position(latLng));
                                            coords.add(latLng);
                                        }
                                    }
                                    setCoordinates(coords);
                                    startActivity(intent);
                                }));
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Login failed, try again", Toast.LENGTH_LONG).show();
                    }
                    finish();
                }));
    }

    private void registerUser(String email,String name, String password) {
        compositeDisposable.add(service.registerUser(email, name, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_LONG).show()));
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    private void setEmail(String email) {
        userEmail = email;
    }

    public static String getEmail() {
        return userEmail;
    }

    private void setCoordinates(ArrayList<LatLng> coordinates) {
        userCoordinates = coordinates;
    }

    public static List<LatLng> getCoordinates() {
        return userCoordinates;
    }

}