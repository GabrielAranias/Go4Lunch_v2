package com.gabriel.aranias.go4lunch_v2.ui.list;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.gabriel.aranias.go4lunch_v2.BuildConfig;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.FragmentListBinding;
import com.gabriel.aranias.go4lunch_v2.model.CustomPlace;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbySearchResponse;
import com.gabriel.aranias.go4lunch_v2.service.place.RetrofitApi;
import com.gabriel.aranias.go4lunch_v2.service.place.RetrofitClient;
import com.gabriel.aranias.go4lunch_v2.ui.detail.DetailActivity;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;
import com.gabriel.aranias.go4lunch_v2.utils.PlaceUtils;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFragment extends Fragment implements OnItemClickListener<NearbyPlaceModel> {

    private FragmentListBinding binding;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private ListAdapter adapter;
    private RetrofitApi retrofitApi;
    private List<NearbyPlaceModel> nearbyPlaceModelList;
    private int radius = 1000;
    private CustomPlace selectedPlace;
    private PlacesClient placesClient;
    private AutocompleteSessionToken token;

    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);

        retrofitApi = RetrofitClient.getRetrofitApi();
        nearbyPlaceModelList = new ArrayList<>();

        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(requireActivity());
        token = AutocompleteSessionToken.newInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.listRv.setHasFixedSize(true);
        adapter = new ListAdapter();
        binding.listRv.setAdapter(adapter);

        binding.listLocationFab.setOnClickListener(currentLocation -> getCurrentLocation());

        setUpLocationUpdate();
        initChipGroup();
        setChipListener();
    }

    private void initChipGroup() {
        for (CustomPlace placeModel : PlaceUtils.placeTypes) {
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

    private void setChipListener() {
        binding.listPlaceGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId != -1) {
                CustomPlace place = PlaceUtils.placeTypes.get(checkedId - 1);
                binding.listPlaceType.setText(place.getName());
                selectedPlace = place;
                getPlaces(place.getPlaceType());
            }
        });
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
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
        if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) && currentLocation != null) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                    + "&radius=" + radius + "&type=" + placeName + "&key=" + BuildConfig.MAPS_API_KEY;

            retrofitApi.getNearbyPlaces(url).enqueue(new Callback<NearbySearchResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<NearbySearchResponse> call,
                                       @NonNull Response<NearbySearchResponse> response) {
                    Gson gson = new Gson();
                    String res = gson.toJson(response.body());
                    Log.d("TAG", "onResponse: " + res);
                    if (response.errorBody() == null) {
                        if (response.body() != null) {
                            if (response.body().getNearbyPlaceModelList() != null &&
                                    response.body().getNearbyPlaceModelList().size() > 0) {
                                adapter.updateRestaurantList(response.body().getNearbyPlaceModelList(),
                                        currentLocation, ListFragment.this);
                                adapter.notifyDataSetChanged();
                            } else {
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
                public void onFailure(@NonNull Call<NearbySearchResponse> call, @NonNull Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                }
            });
        }
    }

    @Override
    public void onItemClicked(NearbyPlaceModel restaurant) {
        Intent intent = new Intent(requireActivity(), DetailActivity.class);
        intent.putExtra(Constants.EXTRA_RESTAURANT, restaurant);
        startActivity(intent);
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.search, menu);
        SearchManager searchManager = (SearchManager) requireActivity()
                .getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
        searchView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_search));
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (currentLocation != null) {

                    // Create object from 2 points around user location (southwest x northeast)
                    RectangularBounds bounds = RectangularBounds.newInstance(
                            new LatLng(currentLocation.getLatitude() - 500,
                                    currentLocation.getLongitude() - 500),
                            new LatLng(currentLocation.getLatitude() + 500,
                                    currentLocation.getLongitude() + 500));

                    // Use builder to create FindAutocompletePredictionsRequest
                    FindAutocompletePredictionsRequest request =
                            FindAutocompletePredictionsRequest.builder()
                                    .setLocationRestriction(bounds)
                                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                                    .setSessionToken(token)
                                    .setQuery(newText)
                                    .build();

                    placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {

                            // Refresh Recycler View w/ predicted restaurants
                            getPredictions(prediction);

                        }
                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e("TAG", apiException.getMessage());
                        }
                    });
                }
                return false;
            }
        });
    }

    private void getPredictions(AutocompletePrediction prediction) {
        if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) && currentLocation != null) {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                    + "&radius=" + radius + "&type=restaurant" + "&key=" + BuildConfig.MAPS_API_KEY;

            retrofitApi.getNearbyPlaces(url).enqueue(new Callback<NearbySearchResponse>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<NearbySearchResponse> call,
                                       @NonNull Response<NearbySearchResponse> response) {
                    Gson gson = new Gson();
                    String res = gson.toJson(response.body());
                    Log.d("TAG", "onResponse: " + res);
                    if (response.errorBody() == null) {
                        if (response.body() != null) {
                            if (response.body().getNearbyPlaceModelList() != null &&
                                    response.body().getNearbyPlaceModelList().size() > 0) {
                                for (NearbyPlaceModel predictedPlace :
                                        response.body().getNearbyPlaceModelList()) {
                                    if (predictedPlace.getPlaceId().equals(prediction.getPlaceId())) {
                                        nearbyPlaceModelList.clear();
                                        nearbyPlaceModelList.add(predictedPlace);
                                    }
                                }
                                adapter.updateRestaurantList(nearbyPlaceModelList, currentLocation,
                                        ListFragment.this);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Log.d("TAG", "onResponse: " + response.errorBody());
                        Toast.makeText(requireContext(), "Error: " + response.errorBody(),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<NearbySearchResponse> call, @NonNull Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                }
            });
        }
    }
}