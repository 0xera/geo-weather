package ru.aydarov.geoapp;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class Repo {
    public static final String API_KEY = "0d99c86fa9c7f024e420128517ef0a35";
    public static final String METRIC = "metric";
    private Retrofit mRetrofit;
    private Api mApi;

    public Retrofit getClient() {

        if (mRetrofit == null) {
            mRetrofit = new Retrofit.Builder()
                    .baseUrl("http://api.openweathermap.org/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(new Gson()))
                    .build();
        }
        return mRetrofit;
    }

    public Api getApi() {
        if (mApi == null)
            mApi = getClient().create(Api.class);
        return mApi;
    }

    public Call<ResponseApi> getData(double lat, double lon) {
        return getApi().getWhether(lat, lon, METRIC, API_KEY);
    }

    public interface Api {

        @GET("data/2.5/weather?")
        Call<ResponseApi> getWhether(@Query("lat") double lat, @Query("lon") double lon, @Query("units") String metric, @Query("appid") String app_id);
    }
}

