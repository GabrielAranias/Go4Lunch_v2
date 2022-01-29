package com.gabriel.aranias.go4lunch_v2.ui.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.WorkmateItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;

import java.util.ArrayList;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    private final ArrayList<User> workmates;
    private Context context;
    private final OnItemClickListener<User> listener;

    public DetailAdapter(Context context, ArrayList<User> workmates,
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
            this.binding = itemBinding;

            this.binding.workmateItem.setOnClickListener(v ->
                    listener.onItemClicked(workmates.get(getAdapterPosition())));
        }

        // Display workmate list
        public void bindView(User workmate) {
                binding.itemWorkmateLunchSpot.setText(context.getString(
                        R.string.joining, workmate.getUsername()));
                binding.itemWorkmateLunchSpot.setTextColor(context.getResources()
                        .getColor(R.color.black));
        }
    }
}