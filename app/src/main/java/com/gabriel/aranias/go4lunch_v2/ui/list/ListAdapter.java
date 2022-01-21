package com.gabriel.aranias.go4lunch_v2.ui.list;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.RestaurantItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.map_list.GooglePlaceModel;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private List<GooglePlaceModel> restaurants;
    private OnItemClickListener<GooglePlaceModel> listener;
    private Location currentLocation;
    private Context context;

    public void updateRestaurantList(List<GooglePlaceModel> restaurants, Location currentLocation,
                                     OnItemClickListener<GooglePlaceModel> listener) {
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

        public ViewHolder(@NonNull RestaurantItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            Objects.requireNonNull(this.binding.restaurantItem).setOnClickListener(v ->

                    listener.onItemClicked(restaurants.get(getAdapterPosition())));
        }

        // Display restaurant list w/ info
        public void bindView(GooglePlaceModel restaurant) {
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
            getWorkmateNumber();
        }

        private void getPhoto(GooglePlaceModel restaurant) {
            if (restaurant.getPhotos() != null && restaurant.getPhotos().size() > 0) {

                Picasso.get()
                        .load(restaurant.getPhotos().get(0).getPhotoUrl())
                        .centerCrop()
                        .resize(50, 50)
                        .error(R.drawable.image_not_available)
                        .into(binding.itemRestaurantPhoto);
            } else {
                Picasso.get()
                        .load(R.drawable.image_not_available)
                        .centerCrop()
                        .resize(50, 50)
                        .into(binding.itemRestaurantPhoto);
            }
        }

        private void getRating(GooglePlaceModel restaurant) {
            if (restaurant.getRating() != null) {
                if (restaurant.getRating() >= 4)
                    binding.itemRating.setImageResource(R.drawable.star_three);
                if (restaurant.getRating() < 4 && restaurant.getRating() >= 3)
                    binding.itemRating.setImageResource(R.drawable.star_two);
                if (restaurant.getRating() < 3)
                    binding.itemRating.setImageResource(R.drawable.star_one);
            }
        }

        private void getDistance(GooglePlaceModel restaurant) {
            Location endPoint = new Location("restaurantLocation");
            endPoint.setLatitude(restaurant.getGeometry().getLocation().getLat());
            endPoint.setLongitude(restaurant.getGeometry().getLocation().getLng());
            int distance = (int) currentLocation.distanceTo(endPoint);
            binding.itemDistance.setText(context.getString(R.string.distance, distance));
        }

        private void getOpeningStatus(GooglePlaceModel restaurant) {
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
    }

    private void getWorkmateNumber() {
    }
}
