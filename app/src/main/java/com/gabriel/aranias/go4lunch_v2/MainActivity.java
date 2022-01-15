package com.gabriel.aranias.go4lunch_v2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.gabriel.aranias.go4lunch_v2.databinding.ActivityMainBinding;
import com.gabriel.aranias.go4lunch_v2.map.MapFragment;
import com.gabriel.aranias.go4lunch_v2.workmate.WorkmateFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initBottomNavigationView();
        initNavigationDrawer();
        initMapFragment();
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
            switch(item.getItemId()) {
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

        binding.mainNavigationDrawer.setNavigationItemSelectedListener(item -> {
            final int yourLunchId = R.id.nd_your_lunch;
            final int settingId = R.id.nd_settings;
            final int logOutId = R.id.nd_logout;
            switch(item.getItemId()) {
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

    private void getLunchDetails() {
    }

    private void logOut() {
    }

}