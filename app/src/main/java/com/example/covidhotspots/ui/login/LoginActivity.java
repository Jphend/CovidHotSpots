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
import com.rengwuxian.materialedittext.MaterialEditText;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private MaterialEditText editEmail, editPassword;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Service service;
    private static String userEmail;

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

        //API call to log user in
        compositeDisposable.add(service.loginUser(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_LONG).show();
                    if(response.contains("Success")) {
                        setEmail(email);
                        startActivity(intent);
                    }
                    else {
                        Toast.makeText(LoginActivity.this, "Login failed, try again", Toast.LENGTH_LONG).show();
                        Intent intent2 = new Intent(this, LoginActivity.class);
                        startActivity(intent2);
                        finish();
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

}