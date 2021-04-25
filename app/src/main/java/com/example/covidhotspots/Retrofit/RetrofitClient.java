package com.example.covidhotspots.Retrofit;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {
    private static Retrofit instance;

    public static Retrofit getInstance() {
        if(instance == null)
            // Build Retrofit API client, adjusting the IP to either Local Host or the IP I am working on at the time of development

            //instance = new Retrofit.Builder().baseUrl("http://10.0.2.2:3000/")
            instance = new Retrofit.Builder().baseUrl("http://192.168.1.215:3000/")
            //instance = new Retrofit.Builder().baseUrl("http://192.168.0.11:3000/")
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create()).build();

            return instance;

    }
}

