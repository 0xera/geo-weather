package ru.aydarov.geoapp;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainViewModel extends ViewModel {
public static final String TAG = "dfffd";

    private MutableLiveData<Float> mMutableLiveData = new MutableLiveData<>();
    private Repo mRepo = new Repo();

    void loadWeather(Location location) {
        mRepo.getData(location.getLatitude(), location.getLongitude()).enqueue(new Callback<ResponseApi>() {
            @Override
            public void onResponse(Call<ResponseApi> call, Response<ResponseApi> response) {
                if (response.body() != null) {
                   mMutableLiveData.postValue(response.body().main.temp);
                }
            }

            @Override
            public void onFailure(Call<ResponseApi> call, Throwable t) {
                Log.d(TAG, "onFailure() called with: call = [" + call + "], t = [" + t + "]");
            }
        });
    }

    public MutableLiveData<Float> getMutableLiveData() {
        return mMutableLiveData;
    }
}
