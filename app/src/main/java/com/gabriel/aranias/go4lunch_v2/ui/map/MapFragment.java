package com.gabriel.aranias.go4lunch_v2.ui.map;

import static com.gabriel.aranias.go4lunch_v2.utils.Constants.DEFAULT_ZOOM;
import static com.gabriel.aranias.go4lunch_v2.utils.Constants.KEY_CAMERA_POSITION;
import static com.gabriel.aranias.go4lunch_v2.utils.Constants.KEY_LOCATION;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gabriel.aranias.go4lunch_v2.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

public class MapFragment extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Retrieve location x camera position from saved instance state
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve view that renders map
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Construct FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Build map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMyLocationButtonClickListener(this);
        googleMap.setOnMyLocationClickListener(this);

        // Get current location of device x set position of map
        getDeviceLocation();
    }

    // Get best x most recent location of device
    private void getDeviceLocation() {
        @SuppressLint("MissingPermission") Task<Location> locationResult =
                fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(requireActivity(), task -> {
            if (task.isSuccessful()) {
                // Set map's camera position to current location of device
                lastKnownLocation = task.getResult();
                if (lastKnownLocation != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                }
            }
        });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that event isn't consumed x default behavior still occurs
        // (camera animates to user's current position)
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    // Save state of map when activity is paused
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }
}