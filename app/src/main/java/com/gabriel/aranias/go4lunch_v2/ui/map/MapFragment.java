package com.gabriel.aranias.go4lunch_v2.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.FragmentMapBinding;
import com.gabriel.aranias.go4lunch_v2.model.CustomPlace;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbySearchResponse;
import com.gabriel.aranias.go4lunch_v2.service.place.RetrofitApi;
import com.gabriel.aranias.go4lunch_v2.service.place.RetrofitClient;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.ui.detail.DetailActivity;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.gabriel.aranias.go4lunch_v2.utils.LoadingDialog;
import com.gabriel.aranias.go4lunch_v2.utils.PlaceUtils;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap map;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private FirebaseAuth firebaseAuth;
    private Marker currentMarker;
    private LoadingDialog loadingDialog;
    private RetrofitApi retrofitApi;
    private List<NearbyPlaceModel> nearbyPlaceModelList;
    private int radius = 5000;
    private CustomPlace selectedPlace;
    private final UserHelper userHelper = UserHelper.getInstance();
    private PlacesClient placesClient;

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        loadingDialog = new LoadingDialog(requireActivity());
        retrofitApi = RetrofitClient.getRetrofitApi();
        nearbyPlaceModelList = new ArrayList<>();

        Places.initialize(requireContext(), Constants.API_KEY);
        placesClient = Places.createClient(requireActivity());

        // Build map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.mapLocationFab.setOnClickListener(currentLocation -> getCurrentLocation());

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

            binding.mapPlaceGroup.addView(chip);
        }
    }

    private void setChipListener() {
        binding.mapPlaceGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId != -1) {
                CustomPlace place = PlaceUtils.placeTypes.get(checkedId - 1);
                binding.mapPlaceType.setText(place.getName());
                selectedPlace = place;
                getPlaces(place.getPlaceType());
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setUpMap();
        }
    }

    @SuppressLint({"MissingPermission", "PotentialBehaviorOverride"})
    private void setUpMap() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(true);
        map.setOnInfoWindowClickListener(currentMarker ->
                getRestaurantDetails((NearbyPlaceModel) currentMarker.getTag()));

        setUpLocationUpdate();
    }

    private void getRestaurantDetails(NearbyPlaceModel restaurant) {
        Intent intent = new Intent(requireActivity(), DetailActivity.class);
        intent.putExtra(Constants.EXTRA_RESTAURANT, restaurant);
        startActivity(intent);
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
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            currentLocation = location;
            if (currentLocation != null) {
                moveCameraToLocation(currentLocation);
            }
        });
    }

    private void moveCameraToLocation(Location location) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(
                location.getLatitude(), location.getLongitude()), 17);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getDisplayName());

        if (currentMarker != null) {
            currentMarker.remove();
        }
        currentMarker = map.addMarker(markerOptions);
        Objects.requireNonNull(currentMarker).setTag(703);
        map.animateCamera(cameraUpdate);
    }

    private void stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        Log.d("TAG", "stopLocationUpdate: success");
    }

    private void getPlaces(String placeName) {
        if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) && currentLocation != null) {
            loadingDialog.startLoading();
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                    + currentLocation.getLatitude() + "," + currentLocation.getLongitude()
                    + "&radius=" + radius + "&type=" + placeName + "&key=" + Constants.API_KEY;

            retrofitApi.getNearbyPlaces(url).enqueue(new Callback<NearbySearchResponse>() {
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
                                nearbyPlaceModelList.clear();
                                map.clear();
                                for (int i = 0; i < response.body().getNearbyPlaceModelList().size(); i++) {
                                    nearbyPlaceModelList.add(response.body().getNearbyPlaceModelList().get(i));
                                    countWorkmates(response.body().getNearbyPlaceModelList().get(i));
                                }
                            } else {
                                map.clear();
                                nearbyPlaceModelList.clear();
                                radius += 1000;
                                getPlaces(placeName);
                            }
                        }
                    } else {
                        Log.d("TAG", "onResponse: " + response.errorBody());
                        Toast.makeText(requireContext(), "Error: " + response.errorBody(),
                                Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.stopLoading();
                }

                @Override
                public void onFailure(@NonNull Call<NearbySearchResponse> call, @NonNull Throwable t) {
                    Log.d("TAG", "onFailure: " + t);
                    loadingDialog.stopLoading();
                }
            });
        }
    }

    // Count joining workmates in each place to set different marker colors
    private void countWorkmates(NearbyPlaceModel restaurant) {
        userHelper.getUserCollection().get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    String placeId = doc.getString(Constants.LUNCH_SPOT_ID_FIELD);
                    if (placeId != null) {
                        // Set marker color x add it to map
                        Drawable background;
                        if (placeId.equals(restaurant.getPlaceId())) {
                            background = ContextCompat.getDrawable(
                                    requireContext(), R.drawable.marker_green);
                        } else {
                            background = ContextCompat.getDrawable(
                                    requireContext(), R.drawable.marker_red);
                        }
                        addMarker(restaurant, background);
                    }
                }
            }
        });
    }

    private void addMarker(NearbyPlaceModel restaurant, Drawable background) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(restaurant.getGeometry().getLocation().getLat(),
                        restaurant.getGeometry().getLocation().getLng()))
                .title(restaurant.getName())
                .snippet(restaurant.getVicinity());
        markerOptions.icon(getCustomIcon(background));
        Objects.requireNonNull(map.addMarker(markerOptions)).setTag(restaurant);
    }

    private BitmapDescriptor getCustomIcon(Drawable background) {
        Objects.requireNonNull(background).setBounds(0, 0, background.getIntrinsicWidth(),
                background.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(),
                background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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
            if (currentMarker != null) {
                currentMarker.remove();
            }
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
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.background_search));

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
                            new LatLng(currentLocation.getLatitude() - 0.003,
                                    currentLocation.getLongitude() - 0.01),
                            new LatLng(currentLocation.getLatitude() + 0.003,
                                    currentLocation.getLongitude() + 0.01));

                    // Use builder to create FindAutocompletePredictionsRequest
                    FindAutocompletePredictionsRequest request =
                            FindAutocompletePredictionsRequest.builder()
                                    .setLocationRestriction(bounds)
                                    .setTypeFilter(TypeFilter.ESTABLISHMENT)
                                    .setQuery(newText)
                                    .build();

                    placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                        for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {

                            map.clear();
                            getPlaceLocation(prediction);

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

    private void getPlaceLocation(AutocompletePrediction prediction) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(
                prediction.getPlaceId(), placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            LatLng latLng = place.getLatLng();

            // Refresh map w/ autocomplete prediction list pins
            if (latLng != null) {
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude))
                        .title(place.getName())
                        .snippet(place.getAddress()));
            }
        });
    }
}