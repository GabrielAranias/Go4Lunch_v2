package com.gabriel.aranias.go4lunch_v2.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.WorkmateItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    private final ArrayList<User> workmates;
    private Context context;
    private final OnItemClickListener<User> listener;

    public ChatListAdapter(Context context, ArrayList<User> workmates,
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
            binding.itemWorkmateLunchSpot.setText(workmate.getUsername());
            binding.itemWorkmateLunchSpot.setTextSize(20);
            binding.itemWorkmateLunchSpot.setTextColor(context.getResources().getColor(
                    R.color.red_primary));
        }
    }
}
