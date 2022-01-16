package com.gabriel.aranias.go4lunch_v2.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivityMainBinding;
import com.gabriel.aranias.go4lunch_v2.databinding.HeaderNavigationDrawerBinding;
import com.gabriel.aranias.go4lunch_v2.service.UserHelper;
import com.gabriel.aranias.go4lunch_v2.ui.map.MapFragment;
import com.gabriel.aranias.go4lunch_v2.ui.workmate.WorkmateFragment;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HeaderNavigationDrawerBinding headerBinding;
    private final UserHelper userHelper = UserHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        View headerView = binding.mainNavigationDrawer.getHeaderView(0);
        headerBinding = HeaderNavigationDrawerBinding.bind(headerView);

        checkUser();
        initBottomNavigationView();
        initNavigationDrawer();
        initMapFragment();
    }

    private void checkUser() {
        if (!userHelper.isCurrentUserLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    // Initial state
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
        if (userHelper.isCurrentUserLoggedIn()) {
            FirebaseUser user = userHelper.getCurrentUser();
            if (user.getPhotoUrl() != null) {
                setProfilePicture(user.getPhotoUrl());
            } else {
                setDefaultProfilePicture();
            }
            setUserTextInfo(user);
            updateUserData();
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

    // Set data w/ user info
    private void updateUserData() {
        userHelper.getUserData().addOnSuccessListener(user -> {
           String username = TextUtils.isEmpty(user.getUsername()) ?
           getString(R.string.no_username) : user.getUsername();
           headerBinding.headerUserName.setText(username);
        });
    }

    private void getLunchDetails() {
    }

    private void logOut() {
        userHelper.signOut(this).addOnCompleteListener(task -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}