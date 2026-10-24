package com.example.dailydiary.ui.more;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.dailydiary.R;
import com.example.dailydiary.data.settings.SettingsStore;
import com.example.dailydiary.util.LocalDates;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MoreFragment extends Fragment {

    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Insights / Feature Cards
        MaterialCardView cardStatistics = view.findViewById(R.id.card_more_statistics);
        MaterialCardView cardGoals = view.findViewById(R.id.card_more_goals);
        MaterialCardView cardAchievements = view.findViewById(R.id.card_more_achievements);

        cardStatistics.setOnClickListener(v -> showFeatureDialog(
                R.string.more_statistics_title,
                R.string.more_statistics_subtitle
        ));
        cardGoals.setOnClickListener(v -> showFeatureDialog(
                R.string.more_goals_title,
                R.string.more_goals_subtitle
        ));
        cardAchievements.setOnClickListener(v -> showFeatureDialog(
                R.string.more_achievements_title,
                R.string.more_achievements_subtitle
        ));

        // Settings Views
        SwitchMaterial switchNotifications = view.findViewById(R.id.switch_settings_notifications);
        MaterialCardView cardDayEnd = view.findViewById(R.id.card_settings_day_end);
        TextView textDayEndTime = view.findViewById(R.id.text_settings_day_end_time);
        MaterialCardView cardTheme = view.findViewById(R.id.card_settings_theme);
        TextView textTheme = view.findViewById(R.id.text_settings_theme);

        // 1. Notifications Switch
        switchNotifications.setChecked(viewModel.areNotificationsEnabled());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setNotificationsEnabled(isChecked);
            Toast.makeText(requireContext(),
                    isChecked ? R.string.reminder_enabled : R.string.routine_inactive,
                    Toast.LENGTH_SHORT).show();
        });

        // 2. Day End Reminder Time
        updateDayEndTimeLabel(textDayEndTime);
        cardDayEnd.setOnClickListener(v -> {
            int currentHour = viewModel.getDayEndHour();
            int currentMinute = viewModel.getDayEndMinute();
            new TimePickerDialog(requireContext(), (picker, hourOfDay, minute) -> {
                viewModel.setDayEndTime(hourOfDay, minute);
                updateDayEndTimeLabel(textDayEndTime);
            }, currentHour, currentMinute, true).show();
        });

        // 3. Theme Selector
        updateThemeLabel(textTheme);
        cardTheme.setOnClickListener(v -> showThemeDialog(textTheme));
    }

    private void updateDayEndTimeLabel(TextView textView) {
        String formatted = LocalDates.formatTime(viewModel.getDayEndHour(), viewModel.getDayEndMinute());
        textView.setText(getString(R.string.settings_day_end_subtitle, formatted));
    }

    private void updateThemeLabel(TextView textView) {
        String mode = viewModel.getThemeMode();
        if (SettingsStore.THEME_LIGHT.equals(mode)) {
            textView.setText(R.string.settings_theme_light);
        } else if (SettingsStore.THEME_DARK.equals(mode)) {
            textView.setText(R.string.settings_theme_dark);
        } else {
            textView.setText(R.string.settings_theme_system);
        }
    }

    private void showThemeDialog(TextView themeLabel) {
        String[] options = new String[]{
                getString(R.string.settings_theme_system),
                getString(R.string.settings_theme_light),
                getString(R.string.settings_theme_dark)
        };
        String currentMode = viewModel.getThemeMode();
        int selectedIndex = 0;
        if (SettingsStore.THEME_LIGHT.equals(currentMode)) {
            selectedIndex = 1;
        } else if (SettingsStore.THEME_DARK.equals(currentMode)) {
            selectedIndex = 2;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_theme_title)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    String selectedMode = SettingsStore.THEME_SYSTEM;
                    if (which == 1) {
                        selectedMode = SettingsStore.THEME_LIGHT;
                    } else if (which == 2) {
                        selectedMode = SettingsStore.THEME_DARK;
                    }
                    viewModel.setThemeMode(selectedMode);
                    updateThemeLabel(themeLabel);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showFeatureDialog(int titleRes, int descriptionRes) {
        new AlertDialog.Builder(requireContext())
                .setTitle(titleRes)
                .setMessage(getString(descriptionRes) + "\n\n" + getString(R.string.feature_coming_soon))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
