package com.gabriel.aranias.go4lunch_v2.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ActivitySettingBinding;
import com.gabriel.aranias.go4lunch_v2.service.UserHelper;

import java.util.Objects;

public class SettingActivity extends AppCompatActivity {

    private ActivitySettingBinding binding;
    private final UserHelper userHelper = UserHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initToolbar();
        setUpListeners();
    }

    private void initToolbar() {
        setSupportActionBar(binding.settingToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setUpListeners() {
        // Update btn
        binding.settingUpdateBtn.setOnClickListener(view ->
                binding.settingUpdateLayout.setVisibility(View.VISIBLE));
        binding.settingOkBtn.setOnClickListener(view ->
                userHelper.updateUsername(Objects.requireNonNull(
                        binding.settingUpdateEditText.getText()).toString())
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, R.string.update_done, Toast.LENGTH_SHORT).show();
                            binding.settingUpdateLayout.setVisibility(View.GONE);
                        }));

        // Delete btn
        binding.settingDeleteBtn.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setMessage(R.string.popup_delete_confirmation)
                .setPositiveButton(R.string.yes, (dialogInterface, i) ->
                        userHelper.deleteUser(SettingActivity.this)
                                .addOnCompleteListener(task -> startLoginActivity()
                                )
                )
                .setNegativeButton(R.string.no, null)
                .show());
    }

    private void startLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}