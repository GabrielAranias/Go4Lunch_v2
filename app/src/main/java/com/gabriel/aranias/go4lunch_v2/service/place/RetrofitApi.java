package com.gabriel.aranias.go4lunch_v2.service.place;

import com.gabriel.aranias.go4lunch_v2.model.map_list.GoogleResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface RetrofitApi {

    @GET
    Call<GoogleResponseModel> getNearByPlaces(@Url String url);
}