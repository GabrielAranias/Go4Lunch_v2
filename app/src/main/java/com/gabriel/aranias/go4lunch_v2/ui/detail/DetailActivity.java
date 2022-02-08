package com.gabriel.aranias.go4lunch_v2.ui.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivityDetailBinding;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.service.place.PlaceHelper;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private PlacesClient placesClient;
    private final UserHelper userHelper = UserHelper.getInstance();
    private final PlaceHelper placeHelper = PlaceHelper.getInstance();
    private SharedPreferences prefs;
    private DetailAdapter adapter;
    private ArrayList<User> workmates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Places.initialize(getApplicationContext(), Constants.API_KEY);
        placesClient = Places.createClient(this);

        prefs = getSharedPreferences(Constants.SHARED_PREFERENCES, MODE_PRIVATE);

        workmates = new ArrayList<>();
        binding.detailContent.detailWorkmateRv.setHasFixedSize(true);
        adapter = new DetailAdapter(this, workmates);
        binding.detailContent.detailWorkmateRv.setAdapter(adapter);

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
            NearbyPlaceModel restaurant =
                    (NearbyPlaceModel) intent.getSerializableExtra(Constants.EXTRA_RESTAURANT);

            displayDetails(restaurant);
            getDetailsApi(restaurant);
            initLikeBtn(restaurant);
            initLunchSpotFab(restaurant);
            getJoiningWorkmates(restaurant);
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

    // Change initial state view if restaurant is liked
    private void initLikeBtn(NearbyPlaceModel restaurant) {
        userHelper.getUserCollection().document(
                userHelper.getCurrentUser().getUid()).get().addOnCompleteListener(task -> {
            @SuppressWarnings("unchecked")
            List<String> favRestaurants = (List<String>) task.getResult().get(Constants.FAV_FIELD);
            if (favRestaurants != null) {
                for (String restaurantId : favRestaurants) {
                    if (restaurantId.equals(restaurant.getPlaceId())) {
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
                    List<String> favRestaurants = (List<String>) task.getResult()
                            .get(Constants.FAV_FIELD);
                    likeRestaurant(restaurant);
                    if (favRestaurants != null) {
                        for (String restaurantId : favRestaurants) {
                            if (restaurantId.equals(restaurant.getPlaceId())) {
                                unlikeRestaurant(restaurant);
                            }
                        }
                    }
                }));
    }

    private void likeRestaurant(NearbyPlaceModel restaurant) {
        // Update view
        binding.detailContent.detailLikeBtn.setText(R.string.detail_unlike);
        binding.detailContent.detailLikeBtn.setIcon(ContextCompat.getDrawable
                (getApplicationContext(), R.drawable.ic_baseline_star_24));
        // Add place to user fav list in Firestore
        userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid())
                .update(Constants.FAV_FIELD, FieldValue.arrayUnion(restaurant.getPlaceId()));
        Snackbar.make(binding.getRoot(), R.string.fav_add, Snackbar.LENGTH_SHORT).show();
    }

    private void unlikeRestaurant(NearbyPlaceModel restaurant) {
        // Update view
        binding.detailContent.detailLikeBtn.setText(R.string.detail_like);
        binding.detailContent.detailLikeBtn.setIcon(ContextCompat.getDrawable
                (getApplicationContext(), R.drawable.ic_baseline_star_border_24));
        // Remove place from user fav list in Firestore
        userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid())
                .update(Constants.FAV_FIELD, FieldValue.arrayRemove(restaurant.getPlaceId()));
        Snackbar.make(binding.getRoot(), R.string.fav_remove, Snackbar.LENGTH_SHORT).show();
    }

    // Change initial state view if restaurant has been chosen as lunch spot
    private void initLunchSpotFab(NearbyPlaceModel restaurant) {
        userHelper.getUserData().addOnSuccessListener(user -> {
            String lunchSpotId = user.getLunchSpotId();
            if (lunchSpotId != null) {
                if (lunchSpotId.equals(restaurant.getPlaceId())) {
                    binding.detailLunchSpotFab.setImageDrawable(ContextCompat.getDrawable(
                            getApplicationContext(), R.drawable.ic_baseline_check_circle_24));
                    binding.detailLunchSpotFab.getDrawable().setTint(getResources()
                            .getColor(R.color.green));
                }
            }
        });
        setFabListener(restaurant);
    }

    // Check if restaurant has already been chosen as lunch spot
    private void setFabListener(NearbyPlaceModel restaurant) {
        binding.detailLunchSpotFab.setOnClickListener(view ->
                userHelper.getUserData().addOnSuccessListener(user -> {
                    String lunchSpotId = user.getLunchSpotId();
                    isLunchSpot(lunchSpotId == null ||
                            !lunchSpotId.equals(restaurant.getPlaceId()), restaurant);
                }));
    }

    private void isLunchSpot(boolean b, NearbyPlaceModel restaurant) {
        int drawable;
        int color;
        String msg;
        if (b) {
            // If user has already chosen a lunch spot beforehand, delete it from Firestore
            deletePreviousLunchSpot(restaurant);
            // Add lunch spot to Firestore 'places' collection
            createPlace(restaurant);
            // Update info in Firestore 'users' collection
            userHelper.updateLunchSpotId(restaurant.getPlaceId());
            userHelper.updateLunchSpotName(restaurant.getName());
            userHelper.updateLunchSpotAddress(restaurant.getVicinity());
            // Save chosen lunch spot to shared prefs
            saveLunchSpot(restaurant);

            drawable = R.drawable.ic_baseline_check_circle_24;
            color = R.color.green;
            msg = getResources().getString(R.string.lunch_spot_add);
        } else {
            // Delete lunch spot from Firestore 'places' collection
            placeHelper.getPlaceCollection().document(restaurant.getDocId()).delete();
            // Update info in Firestore 'users' collection
            userHelper.updateLunchSpotId(null);
            userHelper.updateLunchSpotName(null);
            userHelper.updateLunchSpotAddress(null);
            // Clear shared prefs
            removeLunchSpot();

            drawable = R.drawable.ic_baseline_lunch_dining_24;
            color = R.color.red_primary;
            msg = getResources().getString(R.string.lunch_spot_remove);
        }
        userHelper.getUserData().addOnSuccessListener(user -> {
            binding.detailLunchSpotFab.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), drawable));
            binding.detailLunchSpotFab.getDrawable().setTint(getResources().getColor(color));
            Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
        });
    }

    private void saveLunchSpot(NearbyPlaceModel lunchSpot) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().apply();
        Gson gson = new Gson();
        String json = gson.toJson(lunchSpot);
        editor.putString(Constants.SAVED_LUNCH_SPOT, json);
        editor.apply();
    }

    private void removeLunchSpot() {
        prefs.edit().clear().apply();
    }

    private void deletePreviousLunchSpot(NearbyPlaceModel restaurant) {
        placeHelper.getPlaceCollection().get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                for (DocumentSnapshot doc : task.getResult()) {
                    if (doc.exists() && doc.contains(Constants.USER_ID_FIELD)) {
                        String userId = doc.getString(Constants.USER_ID_FIELD);
                        if (Objects.requireNonNull(userId).equals(userHelper.getCurrentUser().getUid())) {
                            NearbyPlaceModel place = doc.toObject(NearbyPlaceModel.class);
                            if (place != null && !(place.getPlaceId().equals(restaurant.getPlaceId()))) {
                                placeHelper.getPlaceCollection().document(doc.getId()).delete();
                            }
                        }
                    }
                }
            }
        });
    }

    private void createPlace(NearbyPlaceModel lunchSpot) {
        DocumentReference ref = placeHelper.getPlaceCollection().document();
        ref.set(lunchSpot);
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot doc = task.getResult();
                if (doc.exists()) {
                    NearbyPlaceModel place = doc.toObject(NearbyPlaceModel.class);
                    Objects.requireNonNull(place).setDocId(doc.getId());
                    if (place.getPlaceId().equals(lunchSpot.getPlaceId())) {
                        lunchSpot.setDocId(doc.getId());
                    }
                    addUserId(lunchSpot);
                }
            }
        });
    }

    private void addUserId(NearbyPlaceModel lunchSpot) {
        placeHelper.getPlaceCollection().document(lunchSpot.getDocId())
                .update(Constants.USER_ID_FIELD, userHelper.getCurrentUser().getUid())
                .addOnSuccessListener(aVoid ->
                        Log.d("TAG", "DocumentSnapshot successfully updated"))
                .addOnFailureListener(e -> Log.w("TAG", "Error updating document", e));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getJoiningWorkmates(NearbyPlaceModel restaurant) {
        if (workmates.size() > 0) {
            workmates.clear();
        }
        userHelper.getUserCollection().get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                    String placeId = documentSnapshot.getString(Constants.LUNCH_SPOT_ID_FIELD);
                    if (placeId != null) {
                        if (placeId.equals(restaurant.getPlaceId())) {
                            User user = documentSnapshot.toObject(User.class);
                            if (!Objects.requireNonNull(user).getUid().equals(
                                    userHelper.getCurrentUser().getUid())) {
                                workmates.add(user);
                                binding.detailContent.detailNoWorkmateTv.setVisibility(View.GONE);
                                binding.detailContent.detailWorkmateRv.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged();
        });
    }
}