package com.gabriel.aranias.go4lunch_v2.ui.list;

import static com.gabriel.aranias.go4lunch_v2.utils.Constants.API_KEY;
import static com.gabriel.aranias.go4lunch_v2.utils.Constants.BASE_URL;
import static com.gabriel.aranias.go4lunch_v2.utils.Constants.permissionDenied;
import static com.gabriel.aranias.go4lunch_v2.utils.Constants.radius;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.FragmentListBinding;
import com.gabriel.aranias.go4lunch_v2.model.Place;
import com.gabriel.aranias.go4lunch_v2.model.map_list.GooglePlaceModel;
import com.gabriel.aranias.go4lunch_v2.model.map_list.GoogleResponseModel;
import com.gabriel.aranias.go4lunch_v2.service.place.RetrofitApi;
import com.gabriel.aranias.go4lunch_v2.service.place.RetrofitClient;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;
import com.gabriel.aranias.go4lunch_v2.utils.PlaceUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment implements OnItemClickListener<GooglePlaceModel> {

    private FragmentListBinding binding;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private ListAdapter adapter;
    private RetrofitApi retrofitApi;
    private List<GooglePlaceModel> googlePlaceModelList;
    private Place selectedPlace;

    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);

        retrofitApi = RetrofitClient.getRetrofitApi();
        googlePlaceModelList = new ArrayList<>();

        binding.listLocationFab.setOnClickListener(currentLocation -> getCurrentLocation());

        setChipListener();

        return binding.getRoot();
    }

    private void setChipListener() {
        binding.listPlaceGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId != -1) {
                Place place = PlaceUtils.placeTypes.get(checkedId - 1);
                binding.listPlaceType.setText(place.getName());
                selectedPlace = place;
                getPlaces(place.getPlaceType());
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new ListAdapter();
        binding.listRv.setAdapter(adapter);

        setUpLocationUpdate();
        initChipGroup();
    }

    private void initChipGroup() {
        for (Place placeModel : PlaceUtils.placeTypes) {
            Chip chip = new Chip(requireContext());
            chip.setText(placeModel.getName());
            chip.setId(placeModel.getId());
            chip.setPadding(8, 8, 8, 8);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(), R.color.red_light));
            chip.setChipIcon(ResourcesCompat.getDrawable(getResources(), placeModel.getDrawableId(), null));
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);

            binding.listPlaceGroup.addView(chip);
        }
    }

    private void setUpLocationUpdate() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    Log.d("TAG", "onLocationResult: " + location.getLatitude() + ","
                            + location.getLongitude());
                }
                super.onLocationResult(locationResult);
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        startLocationUpdate();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdate() {
        if (permissionDenied) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.getMainLooper()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(requireContext(), R.string.location_update, Toast.LENGTH_SHORT).show();
            }
        });
        getCurrentLocation();
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        if (permissionDenied) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location ->
                currentLocation = location);
    }

    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        Log.d("TAG", "stopLocationUpdate: success");
    }

    private void getPlaces(String placeName) {
        String url = BASE_URL + "nearbysearch/json?location="
                + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                + "&radius=" + radius + "&type=" + placeName + "&key=" + API_KEY;

        if (currentLocation != null) {
            retrofitApi.getNearByPlaces(url).enqueue(new Callback<GoogleResponseModel>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<GoogleResponseModel> call,
                                       @NonNull Response<GoogleResponseModel> response) {
                    Gson gson = new Gson();
                    String res = gson.toJson(response.body());
                    Log.d("TAG", "onResponse: " + res);
                    if (response.errorBody() == null) {
                        if (response.body() != null) {
                            if (response.body().getGooglePlaceModelList() != null &&
                                    response.body().getGooglePlaceModelList().size() > 0) {
                                adapter.updateRestaurantList(googlePlaceModelList, currentLocation, ListFragment.this);
                                adapter.notifyDataSetChanged();
                                googlePlaceModelList.clear();
                                googlePlaceModelList.addAll(response.body().getGooglePlaceModelList());
                            } else {
                                googlePlaceModelList.clear();
                                radius += 1000;
                                getPlaces(placeName);
                            }
                        }
                    } else {
                        Log.d("TAG", "onResponse: " + response.errorBody());
                        Toast.makeText(requireContext(), "Error: " + response.errorBody(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<GoogleResponseModel> call, @NonNull Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                }
            });
        }
    }

    @Override
    public void onItemClicked(GooglePlaceModel restaurant) {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null) {
            stopLocationUpdate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null) {
            startLocationUpdate();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}