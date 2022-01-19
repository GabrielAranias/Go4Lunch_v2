package com.gabriel.aranias.go4lunch_v2.ui.map;

import static com.gabriel.aranias.go4lunch_v2.utils.Constants.permissionDenied;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.FragmentMapBinding;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private GoogleMap map;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private FirebaseAuth firebaseAuth;
    private Marker currentMarker;

    public MapFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        binding.locationFab.setOnClickListener(currentLocation -> getCurrentLocation());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Build map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            permissionDenied = false;
            setUpMap();
        }
    }

    @SuppressLint("MissingPermission")
    private void setUpMap() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionDenied = true;
            return;
        }
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setTiltGesturesEnabled(true);

        setUpLocationUpdate();
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
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionDenied = true;
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
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionDenied = true;
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            currentLocation = location;
            moveCameraToLocation(location);
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
}