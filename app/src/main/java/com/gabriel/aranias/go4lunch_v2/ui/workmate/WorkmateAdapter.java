package com.gabriel.aranias.go4lunch_v2.ui.workmate;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.WorkmateItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class WorkmateAdapter extends RecyclerView.Adapter<WorkmateAdapter.ViewHolder> {

    private final ArrayList<User> workmates;
    private Context context;
    private final OnItemClickListener<User> listener;
    private final UserHelper userHelper = UserHelper.getInstance();

    public WorkmateAdapter(Context context, ArrayList<User> workmates,
                           OnItemClickListener<User> listener) {
        this.context = context;
        this.workmates = workmates;
        this.listener = listener;
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
            binding = itemBinding;

            binding.workmateItem.setOnClickListener(v ->
                    listener.onItemClicked(workmates.get(getAdapterPosition())));
        }

        // Display workmate list w/ info
        public void bindView(User workmate) {
            // Photo
            getPhoto(workmate);
            // Text
            getLunchSpotText(workmate);
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

        private void getLunchSpotText(User workmate) {
            userHelper.getUserCollection()
                    .whereEqualTo(Constants.USER_ID_FIELD, workmate.getUid())
                    .addSnapshotListener((value, error) -> {
                        if (error != null) {
                            Log.w("TAG", "Listen failed", error);
                            return;
                        }
                        for (QueryDocumentSnapshot doc : Objects.requireNonNull(value)) {
                            User user = doc.toObject(User.class);
                            if (user.getLunchSpotId() != null) {
                                // Workmate has chosen a lunch spot
                                binding.itemWorkmateLunchSpot.setText(context.getString(
                                        R.string.decided, user.getUsername()));
                                binding.itemWorkmateLunchSpotName.setVisibility(View.VISIBLE);
                                binding.itemWorkmateLunchSpotName.setText(context.getString(
                                        R.string.decided_lunch_spot, user.getLunchSpotName()));
                            } else {
                                // Workmate hasn't decided yet
                                binding.itemWorkmateLunchSpot.setText(context.getString(
                                        R.string.not_decided, user.getUsername()));
                                binding.itemWorkmateLunchSpot.setTextColor(context.getResources()
                                        .getColor(R.color.grey));
                            }
                        }
                    });
        }
    }
}
