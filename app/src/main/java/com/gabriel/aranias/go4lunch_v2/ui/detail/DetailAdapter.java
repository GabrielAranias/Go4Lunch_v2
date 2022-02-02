package com.gabriel.aranias.go4lunch_v2.ui.detail;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.WorkmateItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.User;

import java.util.ArrayList;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    private final ArrayList<User> workmates;
    private Context context;

    public DetailAdapter(Context context, ArrayList<User> workmates) {
        this.context = context;
        this.workmates = workmates;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        WorkmateItemBinding binding = WorkmateItemBinding.inflate(inflater, parent, false);
        context = parent.getContext();

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindView(workmates.get(position));
    }

    @Override
    public int getItemCount() {
        return workmates == null ? 0 : workmates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final WorkmateItemBinding binding;

        public ViewHolder(@NonNull WorkmateItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding;
        }

        // Display joining workmate list
        public void bindView(User workmate) {
            // Text
            binding.itemWorkmateLunchSpot.setText(context.getString(
                    R.string.joining, workmate.getUsername()));
            binding.itemWorkmateLunchSpot.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            // Photo
            getPhoto(workmate);
        }

        private void getPhoto(User workmate) {
            if (workmate.getPictureUrl() != null) {
                Glide.with(context)
                        .load(workmate.getPictureUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.itemWorkmateAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.itemWorkmateAvatar);
            }
        }
    }
}
