package com.example.covidhotspots.Retrofit;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Service {

    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("email") String email,
                                    @Field("name") String username,
                                    @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("email") String email,
                                        @Field("password") String password);

    @POST("clickLocation")
    @FormUrlEncoded
    Observable<String> saveClickLocation(@Field("email") String email,
                                 //@Field("coordinates") LatLng latLng);
                                 //@Field("coordinates") double lat, @Field("coordinates") double lng);
                                 @Field("lat") double lat, @Field("lng") double lng);


    @POST("searchLocation")
    @FormUrlEncoded
    Observable<String> saveSearchLocation(@Field("email") String email,
                                         @Field("lat") double lat, @Field("lng") double lng);
                                         //@Field("coordinates") LatLng latLng);

    @GET("/")
    Observable<String> getLocations();

}
