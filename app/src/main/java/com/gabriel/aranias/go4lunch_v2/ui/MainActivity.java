package com.gabriel.aranias.go4lunch_v2.ui;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivityMainBinding;
import com.gabriel.aranias.go4lunch_v2.databinding.HeaderNavigationDrawerBinding;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.ui.chat.ChatListFragment;
import com.gabriel.aranias.go4lunch_v2.ui.detail.DetailActivity;
import com.gabriel.aranias.go4lunch_v2.ui.list.ListFragment;
import com.gabriel.aranias.go4lunch_v2.ui.map.MapFragment;
import com.gabriel.aranias.go4lunch_v2.ui.workmate.WorkmateFragment;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.gabriel.aranias.go4lunch_v2.utils.PermissionUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private ActivityMainBinding binding;
    private HeaderNavigationDrawerBinding headerBinding;
    private final UserHelper userHelper = UserHelper.getInstance();
    private boolean permissionDenied;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View headerView = binding.mainNavigationDrawer.getHeaderView(0);
        headerBinding = HeaderNavigationDrawerBinding.bind(headerView);

        setSupportActionBar(binding.mainToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.nav_hungry));

        checkUser();
        initBottomNavigationView();
        initNavigationDrawer();
        enableDeviceLocation();
    }

    // Start LoginActivity if user hasn't signed in
    private void checkUser() {
        if (!userHelper.isCurrentUserLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Initial state if user has signed in
    private void initMapFragment() {
        Fragment fragment = new MapFragment();
        binding.mainToolbar.setTitle(R.string.nav_hungry);
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    // Configure bottom navigation menu + toolbar w/ appropriate labels
    private void initBottomNavigationView() {
        binding.mainBottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            final int mapId = R.id.nav_map;
            final int listId = R.id.nav_list;
            final int workmateId = R.id.nav_workmates;
            final int chatId = R.id.nav_chat;
            switch (item.getItemId()) {
                case mapId:
                    initMapFragment();
                    break;
                case listId:
                    binding.mainToolbar.setTitle(R.string.nav_hungry);
                    fragment = new ListFragment();
                    showFragment(fragment);
                    break;
                case workmateId:
                    binding.mainToolbar.setTitle(R.string.nav_workmates);
                    fragment = new WorkmateFragment();
                    showFragment(fragment);
                    break;
                case chatId:
                    binding.mainToolbar.setTitle(R.string.nav_chat);
                    fragment = new ChatListFragment();
                    showFragment(fragment);
                    break;
            }
            return true;
        });
    }

    // Configure navigation drawer menu
    private void initNavigationDrawer() {
        binding.mainToolbar.setNavigationOnClickListener(v -> binding.drawerLayout.open());

        getUserData();

        binding.mainNavigationDrawer.setNavigationItemSelectedListener(item -> {
            final int yourLunchId = R.id.nd_your_lunch;
            final int settingId = R.id.nd_settings;
            final int logOutId = R.id.nd_logout;
            switch (item.getItemId()) {
                case yourLunchId:
                    getLunchDetails();
                    break;
                case settingId:
                    startActivity(new Intent(this, SettingActivity.class));
                    break;
                case logOutId:
                    logOut();
                    break;
            }
            binding.drawerLayout.close();
            return true;
        });
    }

    // Display user info in drawer header
    private void getUserData() {
        FirebaseUser user = userHelper.getCurrentUser();
        if (user.getPhotoUrl() != null) {
            setProfilePicture(user.getPhotoUrl());
        } else {
            setDefaultProfilePicture();
        }
        setUserTextInfo(user);
        // Update username if changed via settings
        if (SettingActivity.nameIsUpdated()) {
            updateName();
        }
    }

    private void setProfilePicture(Uri photoUrl) {
        Glide.with(this)
                .load(photoUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(headerBinding.headerAvatar);
    }

    private void setDefaultProfilePicture() {
        Glide.with(this)
                .load(R.drawable.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(headerBinding.headerAvatar);
    }

    private void setUserTextInfo(FirebaseUser user) {
        String username = TextUtils.isEmpty(user.getDisplayName()) ?
                getString(R.string.no_username) : user.getDisplayName();
        String email = TextUtils.isEmpty(user.getEmail()) ?
                getString(R.string.no_user_email) : user.getEmail();

        headerBinding.headerUserName.setText(username);
        headerBinding.headerUserEmail.setText(email);
    }

    private void updateName() {
        userHelper.getUserData().addOnSuccessListener(user -> {
            String updatedName = user.getUsername();
            headerBinding.headerUserName.setText(updatedName);
        });
    }

    // If user has chosen a restaurant, display its details on click
    private void getLunchDetails() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(Constants.SAVED_LUNCH_SPOT, null);
        NearbyPlaceModel lunchSpot = gson.fromJson(json, NearbyPlaceModel.class);

        if (lunchSpot != null) {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(Constants.EXTRA_RESTAURANT, lunchSpot);
            startActivity(intent);
        } else {
            Snackbar.make(binding.getRoot(), R.string.not_selected, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void logOut() {
        userHelper.signOut(this).addOnCompleteListener(task -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // Enable location layer if fine location permission has been granted
    private void enableDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            initMapFragment();
        } else {
            // Permission to access location is missing; show rationale x request permission
            PermissionUtils.requestPermission(this, Constants.LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable location layer if permission has been granted
            enableDeviceLocation();
        } else {
            // Permission was denied; display missing permission error dialog when fragments resume
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    // Display dialog w/ error message explaining location permission is missing
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
}