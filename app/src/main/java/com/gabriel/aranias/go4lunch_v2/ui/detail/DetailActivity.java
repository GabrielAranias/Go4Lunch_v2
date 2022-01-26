package com.gabriel.aranias.go4lunch_v2.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gabriel.aranias.go4lunch_v2.BuildConfig;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivityDetailBinding;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private static final String API_KEY = BuildConfig.MAPS_API_KEY;
    private static final String EXTRA_RESTAURANT = "restaurant";
    private static final String FAV_FIELD = "favorite restaurants";
    private static final String LUNCH_SPOT_FIELD = "lunch spot";
    private PlacesClient placesClient;
    private final UserHelper userHelper = UserHelper.getInstance();

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
            boolean isShown = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    binding.detailCollapsingLayout.setTitle(getString(R.string.label_detail));
                    isShown = true;
                } else if (isShown) {
                    binding.detailCollapsingLayout.setTitle(" ");
                    isShown = false;
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
            initLikeBtn(restaurant);
            initLunchSpotFab(restaurant);
            getJoiningWorkmates();
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
        // Specify fields to return
        List<Place.Field> placeFields = Arrays.asList(Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
        // Construct request object, passing place id x field array
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(restaurant.getPlaceId(), placeFields);

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

    // Change initial state view if restaurant is liked
    private void initLikeBtn(NearbyPlaceModel restaurant) {
        userHelper.getUserCollection().document(
                userHelper.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            @SuppressWarnings("unchecked")
            List<String> favRestaurants = (List<String>) task.getResult().get(FAV_FIELD);
            if (favRestaurants != null) {
                for (String placeId : favRestaurants) {
                    if (placeId.equals(restaurant.getPlaceId())) {
                        binding.detailContent.detailLikeBtn.setText(R.string.detail_unlike);
                        binding.detailContent.detailLikeBtn.setIcon(ContextCompat.getDrawable
                                (getApplicationContext(), R.drawable.ic_baseline_star_24));
                    }
                }
            }
        });
        setLikeBtnListener(restaurant);
    }

    // Check if restaurant is already liked by user
    private void setLikeBtnListener(NearbyPlaceModel restaurant) {
        binding.detailContent.detailLikeBtn.setOnClickListener(view ->
                userHelper.getUserCollection().document(
                        userHelper.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
                    @SuppressWarnings("unchecked")
                    List<String> favRestaurants = (List<String>) task.getResult().get(FAV_FIELD);
                    likeRestaurant(restaurant);
                    if (favRestaurants != null) {
                        for (String placeId : favRestaurants) {
                            if (placeId.equals(restaurant.getPlaceId())) {
                                unlikeRestaurant(restaurant);
                            }
                        }
                    }
                }));
    }

    // Update view x add fav in Firestore
    private void likeRestaurant(NearbyPlaceModel restaurant) {
        binding.detailContent.detailLikeBtn.setText(R.string.detail_unlike);
        binding.detailContent.detailLikeBtn.setIcon(ContextCompat.getDrawable
                (getApplicationContext(), R.drawable.ic_baseline_star_24));
        userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid())
                .update(FAV_FIELD, FieldValue.arrayUnion(restaurant.getPlaceId()));
        Snackbar.make(binding.getRoot(), R.string.fav_add, Snackbar.LENGTH_SHORT).show();
    }

    // Update view x remove fav from Firestore
    private void unlikeRestaurant(NearbyPlaceModel restaurant) {
        binding.detailContent.detailLikeBtn.setText(R.string.detail_like);
        binding.detailContent.detailLikeBtn.setIcon(ContextCompat.getDrawable
                (getApplicationContext(), R.drawable.ic_baseline_star_border_24));
        userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid())
                .update(FAV_FIELD, FieldValue.arrayRemove(restaurant.getPlaceId()));
        Snackbar.make(binding.getRoot(), R.string.fav_remove, Snackbar.LENGTH_SHORT).show();
    }

    // Change initial state view if restaurant is lunch spot
    private void initLunchSpotFab(NearbyPlaceModel restaurant) {
        if (userHelper.getCurrentUser() != null) {
            userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() != null) {
                            String result = task.getResult().getString(LUNCH_SPOT_FIELD);
                            if (result != null) {
                                if (result.equals(restaurant.getPlaceId())) {
                                    binding.detailLunchSpotFab.setImageDrawable(ContextCompat.getDrawable(
                                            getApplicationContext(), R.drawable.ic_baseline_check_circle_24));
                                    binding.detailLunchSpotFab.getDrawable().setTint(getResources()
                                            .getColor(R.color.green));
                                }
                            }
                        }
                    });
        }
        setFabListener(restaurant);
    }

    // Check if restaurant is already lunch spot
    private void setFabListener(NearbyPlaceModel restaurant) {
        binding.detailLunchSpotFab.setOnClickListener(view ->
                userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid()).get()
                        .addOnCompleteListener(task -> {
                            if (task.getResult() != null) {
                                String result = task.getResult().getString(LUNCH_SPOT_FIELD);
                                isLunchSpot(result == null ||
                                        !result.equals(restaurant.getPlaceId()), restaurant);
                            }
                        }));
    }

    private void isLunchSpot(boolean b, NearbyPlaceModel restaurant) {
        int drawable;
        int color;
        String msg;
        // Update in Firestore
        Map<String, Object> data = new HashMap<>();
        if (b) {
            data.put(LUNCH_SPOT_FIELD, restaurant.getPlaceId());
            drawable = R.drawable.ic_baseline_check_circle_24;
            color = R.color.green;
            msg = getResources().getString(R.string.lunch_spot_add);
        } else {
            data.put(LUNCH_SPOT_FIELD, null);
            drawable = R.drawable.ic_baseline_lunch_dining_24;
            color = R.color.red_primary;
            msg = getResources().getString(R.string.lunch_spot_remove);
        }
        if (userHelper.getCurrentUser() != null) {
            userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid())
                    .set(data, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                binding.detailLunchSpotFab.setImageDrawable(ContextCompat.getDrawable(
                        getApplicationContext(), drawable));
                binding.detailLunchSpotFab.getDrawable().setTint(getResources().getColor(color));
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
            }).addOnFailureListener(e ->
                    Log.e("TAG", "Firestore failure " + e));
        }
    }

    private void getJoiningWorkmates() {
    }
}