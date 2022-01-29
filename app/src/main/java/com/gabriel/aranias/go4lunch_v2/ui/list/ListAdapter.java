package com.gabriel.aranias.go4lunch_v2.ui.list;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.RestaurantItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<NearbyPlaceModel> restaurants;
    private OnItemClickListener<NearbyPlaceModel> listener;
    private Location currentLocation;
    private Context context;
    private final UserHelper userHelper = UserHelper.getInstance();

    public void updateRestaurantList(List<NearbyPlaceModel> restaurants, Location currentLocation,
                                     OnItemClickListener<NearbyPlaceModel> listener) {
        this.restaurants = restaurants;
        this.currentLocation = currentLocation;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RestaurantItemBinding binding = RestaurantItemBinding.inflate(inflater, parent, false);
        context = parent.getContext();

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindView(restaurants.get(position));
    }

    @Override
    public int getItemCount() {
        return restaurants == null ? 0 : restaurants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final RestaurantItemBinding binding;

        public ViewHolder(@NonNull RestaurantItemBinding itemBinding) {
            super(itemBinding.getRoot());
            binding = itemBinding;

            binding.restaurantItem.setOnClickListener(v ->
                    listener.onItemClicked(restaurants.get(getAdapterPosition())));
        }

        // Display restaurant list w/ info
        public void bindView(NearbyPlaceModel restaurant) {
            // Name x address
            binding.itemRestaurantName.setText(restaurant.getName());
            binding.itemRestaurantAddress.setText(restaurant.getVicinity());
            // Photo
            getPhoto(restaurant);
            // Rating
            getRating(restaurant);
            // Distance
            getDistance(restaurant);
            // Opening hours
            getOpeningStatus(restaurant);
            // #workmates
            getWorkmateNumber(restaurant);
        }

        private void getPhoto(NearbyPlaceModel restaurant) {
            if (restaurant.getPhotos() != null && restaurant.getPhotos().size() > 0) {
                Picasso.get()
                        .load(restaurant.getPhotos().get(0).getPhotoUrl())
                        .fit()
                        .centerCrop()
                        .error(R.drawable.image_not_available)
                        .into(binding.itemRestaurantPhoto);
            } else {
                Picasso.get()
                        .load(R.drawable.image_not_available)
                        .fit()
                        .centerCrop()
                        .into(binding.itemRestaurantPhoto);
            }
        }

        private void getRating(NearbyPlaceModel restaurant) {
            if (restaurant.getRating() != null) {
                if (restaurant.getRating() >= 4)
                    binding.itemRating.setImageResource(R.drawable.star_three);
                if (restaurant.getRating() < 4 && restaurant.getRating() >= 3)
                    binding.itemRating.setImageResource(R.drawable.star_two);
                if (restaurant.getRating() < 3)
                    binding.itemRating.setImageResource(R.drawable.star_one);
            }
        }

        private void getDistance(NearbyPlaceModel restaurant) {
            Location endPoint = new Location("restaurantLocation");
            endPoint.setLatitude(restaurant.getGeometry().getLocation().getLat());
            endPoint.setLongitude(restaurant.getGeometry().getLocation().getLng());
            long distance = (long) currentLocation.distanceTo(endPoint);
            binding.itemDistance.setText(context.getString(R.string.distance, distance));
        }

        private void getOpeningStatus(NearbyPlaceModel restaurant) {
            if (restaurant.getOpeningHours() != null) {
                if (restaurant.getOpeningHours().getOpenNow().toString().equals("true")) {
                    binding.itemOpeningHours.setText(R.string.open);
                    binding.itemOpeningHours.setTextColor(context.getResources().getColor(R.color.green));
                } else {
                    binding.itemOpeningHours.setText(R.string.closed);
                    binding.itemOpeningHours.setTextColor(context.getResources().getColor(R.color.red_dark));
                }
            } else {
                binding.itemOpeningHours.setText(R.string.no_opening_hours);
                binding.itemOpeningHours.setTextColor(context.getResources().getColor(R.color.black));
            }
        }

        // Display #workmates lunching in each place
        private void getWorkmateNumber(NearbyPlaceModel restaurant) {
            final Integer[] workmateNumber = {0};
            userHelper.getUserCollection().get().addOnCompleteListener(task -> {
                if (task.getResult() != null) {
                    for (DocumentSnapshot documentSnapshot : task.getResult()) {
                        String placeId = documentSnapshot.getString(Constants.LUNCH_SPOT_FIELD);
                        if (placeId != null) {
                            if (placeId.equals(restaurant.getPlaceId())) {
                                workmateNumber[0]++;
                                if (workmateNumber[0] > 0) {
                                    binding.itemNbWorkmatesIcon.setVisibility(View.VISIBLE);
                                    binding.itemNbWorkmatesCounter.setText(context.getString(
                                            R.string.workmate_counter, workmateNumber[0]));
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
