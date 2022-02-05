package com.gabriel.aranias.go4lunch_v2.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivitySettingBinding;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.gabriel.aranias.go4lunch_v2.utils.Constants;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    private final UserHelper userHelper = UserHelper.getInstance();
    private static boolean updatedName = false;
    private Boolean temporarySwitch;
    private Boolean switchValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        setUpListeners();
        initSwitch();
    }

    private void initToolbar() {
        setSupportActionBar(binding.settingToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setUpListeners() {
        // Update btn
        binding.settingUpdateBtn.setOnClickListener(v -> {
            if (binding.settingUpdateLayout.getVisibility() == View.GONE) {
                binding.settingUpdateLayout.setVisibility(View.VISIBLE);
            } else {
                binding.settingUpdateLayout.setVisibility(View.GONE);
            }
        });
        binding.settingOkBtn.setOnClickListener(view -> checkError());

        // Delete btn
        binding.settingDeleteBtn.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setMessage(R.string.delete_account_popup)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) ->
                        userHelper.deleteUser(SettingActivity.this)
                                .addOnCompleteListener(task -> startLoginActivity()
                                )
                )
                .setNegativeButton(android.R.string.cancel, null)
                .show());
    }

    private void checkError() {
        if (TextUtils.isEmpty(Objects.requireNonNull(binding.settingUpdateEditLayout.getEditText())
                .getText().toString())) {
            binding.settingUpdateEditText.setError(getString(R.string.new_name_needed));
        } else {
            binding.settingUpdateEditText.setError(null);
            userHelper.updateUsername(Objects.requireNonNull(
                    binding.settingUpdateEditText.getText()).toString())
                    .addOnSuccessListener(aVoid -> {
                        updatedName = true;
                        Toast.makeText(this, R.string.update_done, Toast.LENGTH_SHORT).show();
                        binding.settingUpdateLayout.setVisibility(View.GONE);
                    });
        }
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public static Boolean nameIsUpdated() {
        return updatedName;
    }

    private void initSwitch() {
        binding.settingToolbar.setNavigationOnClickListener(view -> {
            // When user leaves activity, save new switch value if changed
            if (temporarySwitch != switchValue) {
                Map<String, Object> data = new HashMap<>();
                data.put(Constants.NOTIFICATION_ENABLED_FIELD, temporarySwitch);
                // Save in Firestore
                userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid())
                        .set(data, SetOptions.merge()).addOnSuccessListener(unused ->
                        // Save x leave activity
                        this.onBackPressed())
                        .addOnFailureListener(e -> Log.e("TAG", "Firestore failure: " + e));
            } else {
                // No change
                this.onBackPressed();
            }
        });
        getSwitchValue();
        binding.settingSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                temporarySwitch = isChecked);
    }

    private void getSwitchValue() {
        userHelper.getUserCollection().document(userHelper.getCurrentUser().getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        Boolean isNotificationEnabled = (Boolean) task.getResult().
                                get(Constants.NOTIFICATION_ENABLED_FIELD);
                        if (isNotificationEnabled != null) {
                            // Check or uncheck switch, whether notifications are enabled or not
                            switchValue = isNotificationEnabled;
                            temporarySwitch = isNotificationEnabled;
                            binding.settingSwitch.setChecked(switchValue);
                            binding.settingSwitch.jumpDrawablesToCurrentState();
                        }
                    }
                });
    }
}