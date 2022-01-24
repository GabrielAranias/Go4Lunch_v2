package com.gabriel.aranias.go4lunch_v2.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.gabriel.aranias.go4lunch_v2.BuildConfig;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivityDetailBinding;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private static final String API_KEY = BuildConfig.MAPS_API_KEY;
    private static final String EXTRA_RESTAURANT = "restaurant";
    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), API_KEY);
        placesClient = Places.createClient(this);

        initToolbar();
        getRestaurantDetails();
    }

    private void initToolbar() {
        setSupportActionBar(binding.detailToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        binding.detailAppbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    binding.detailCollapsingLayout.setTitle(getString(R.string.label_detail));
                    isShow = true;
                } else if (isShow) {
                    binding.detailCollapsingLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    // Set up restaurant detail components to be displayed
    private void getRestaurantDetails() {
        Intent intent = this.getIntent();
        if (intent.getExtras() != null) {
            NearbyPlaceModel restaurant = (NearbyPlaceModel) intent.getSerializableExtra(EXTRA_RESTAURANT);

            displayDetails(restaurant);
            getDetailsApi(restaurant);
        }
    }

    private void displayDetails(NearbyPlaceModel restaurant) {
        // Name x address
        binding.detailContent.detailRestaurantName.setText(restaurant.getName());
        binding.detailContent.detailAddress.setText(restaurant.getVicinity());
        // Photo
        getPhoto(restaurant);
        // Rating
        getRating(restaurant);
    }

    private void getPhoto(NearbyPlaceModel restaurant) {
        if (restaurant.getPhotos() != null && restaurant.getPhotos().size() > 0) {
            Picasso.get()
                    .load(restaurant.getPhotos().get(0).getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .error(R.drawable.image_not_available)
                    .into(binding.detailPhoto);
        } else {
            Picasso.get()
                    .load(R.drawable.image_not_available)
                    .fit()
                    .centerCrop()
                    .into(binding.detailPhoto);
        }
    }

    private void getRating(NearbyPlaceModel restaurant) {
        if (restaurant.getRating() != null) {
            if (restaurant.getRating() >= 4)
                binding.detailContent.detailRating.setImageResource(R.drawable.star_three);
            if (restaurant.getRating() < 4 && restaurant.getRating() >= 3)
                binding.detailContent.detailRating.setImageResource(R.drawable.star_two);
            if (restaurant.getRating() < 3)
                binding.detailContent.detailRating.setImageResource(R.drawable.star_one);
        }
    }

    // Set up listeners for call x website btn
    private void getDetailsApi(NearbyPlaceModel restaurant) {
        // Define place id
        String placeId = restaurant.getPlaceId();
        // Specify fields to return
        List<Place.Field> placeFields = Arrays.asList(Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        // Construct request object, passing place id x field array
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i("TAG", "CustomPlace found: " + place.getName());
            // Start intent on call btn click
            if (place.getPhoneNumber() != null) {
                binding.detailContent.detailCallBtn.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel: " + place.getPhoneNumber()));
                    startActivity(intent);
                });
            } else {
                Snackbar.make(binding.getRoot(), R.string.no_phone_number, Snackbar.LENGTH_SHORT).show();
            }
            // Start intent on website btn click
            if (place.getWebsiteUri() != null) {
                binding.detailContent.detailWebsiteBtn.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, place.getWebsiteUri());
                    startActivity(intent);
                });
            } else {
                Snackbar.make(binding.getRoot(), R.string.no_website, Snackbar.LENGTH_SHORT).show();
            }

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("TAG", "CustomPlace not found: " + exception.getMessage());
                int statusCode = apiException.getStatusCode();
                Log.e("TAG", "Error: " + statusCode);
            }
        });
    }
}