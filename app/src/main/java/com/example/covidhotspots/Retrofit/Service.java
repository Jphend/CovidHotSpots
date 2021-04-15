package com.example.covidhotspots.Retrofit;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Service {


    //API call for when the user registers
    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("email") String email,
                                    @Field("name") String username,
                                    @Field("password") String password);

    //API call for when the user logs in
    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("email") String email,
                                        @Field("password") String password);

    //API call for when the user clicks on a point on the map
    @POST("clickLocation")
    @FormUrlEncoded
    Observable<String> saveClickLocation(@Field("email") String email,
                                 //@Field("coordinates") LatLng latLng);
                                 //@Field("coordinates") double lat, @Field("coordinates") double lng);
                                 @Field("lat") double lat, @Field("lng") double lng);


    //API call for the user searching a locations
    @POST("searchLocation")
    @FormUrlEncoded
    Observable<String> saveSearchLocation(@Field("email") String email,
                                         @Field("lat") double lat, @Field("lng") double lng);
                                         //@Field("coordinates") LatLng latLng);

    //API call to get user locations
    @POST("/")
    @FormUrlEncoded
    Observable<String> getLocations(@Field("email") String email);

    //API call to show all locations
    @GET("/showAll")
    Observable<String> getAll();

    //API call to generate fake data
    //@POST("simulate")
    //@FormUrlEncoded
    //Observable<String> simulate(@Field("lat") double lat, @Field("lng") double lng);

}
