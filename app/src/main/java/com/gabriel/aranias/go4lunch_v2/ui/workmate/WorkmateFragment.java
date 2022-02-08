package com.gabriel.aranias.go4lunch_v2.ui.workmate;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.FragmentWorkmateBinding;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.model.nearby.NearbyPlaceModel;
import com.gabriel.aranias.go4lunch_v2.service.place.PlaceHelper;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.ui.detail.DetailActivity;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.gabriel.aranias.go4lunch_v2.utils.OnItemClickListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class WorkmateFragment extends Fragment implements OnItemClickListener<User> {

    private final UserHelper userHelper = UserHelper.getInstance();
    private final PlaceHelper placeHelper = PlaceHelper.getInstance();
    private FragmentWorkmateBinding binding;
    private WorkmateAdapter adapter;
    private ArrayList<User> workmates;
    private ProgressDialog progressDialog;

    public WorkmateFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWorkmateBinding.inflate(inflater, container, false);

        progressDialog = new ProgressDialog(requireActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(requireContext().getString(R.string.fetching_data));
        progressDialog.show();

        initData();
        EventChangeListener();

        return binding.getRoot();
    }

    private void initData() {

        binding.workmateRv.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        binding.workmateRv.setLayoutManager(manager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(
                binding.workmateRv.getContext(), manager.getOrientation());
        binding.workmateRv.addItemDecoration(itemDecoration);

        workmates = new ArrayList<>();

        adapter = new WorkmateAdapter(requireActivity(), workmates, this);
        binding.workmateRv.setAdapter(adapter);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void EventChangeListener() {
        userHelper.getUserCollection()
                .orderBy(Constants.USERNAME_FIELD, Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Log.e("TAG", "Firestore error: " + error.getMessage());
                        return;
                    }
                    workmates.clear();
                    for (DocumentChange dc : Objects.requireNonNull(value).getDocumentChanges()) {
                        User user = dc.getDocument().toObject(User.class);
                        switch (dc.getType()) {
                            case ADDED:
                                // Add all workmates except current user
                                if (!user.getUid().equals(userHelper.getCurrentUser().getUid())) {
                                    workmates.add(user);
                                }
                                break;
                            case REMOVED:
                                workmates.remove(user);
                                break;
                            case MODIFIED:
                                workmates.remove(user);
                                workmates.add(user);
                                break;
                        }
                        adapter.notifyDataSetChanged();
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    @Override
    public void onItemClicked(User workmate) {
        placeHelper.getPlaceCollection()
                .whereEqualTo(Constants.USER_ID_FIELD, workmate.getUid())
                .addSnapshotListener(((value, error) -> {
                    if (error != null) {
                        Log.w("TAG", "Listen failed", error);
                        return;
                    }
                    for (QueryDocumentSnapshot doc : Objects.requireNonNull(value)) {
                        if (doc.contains(Constants.USER_ID_FIELD)) {
                            NearbyPlaceModel lunchSpot = doc.toObject(NearbyPlaceModel.class);
                            Intent intent = new Intent(requireActivity(), DetailActivity.class);
                            intent.putExtra(Constants.EXTRA_RESTAURANT, lunchSpot);
                            startActivity(intent);
                        }
                    }
                }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}