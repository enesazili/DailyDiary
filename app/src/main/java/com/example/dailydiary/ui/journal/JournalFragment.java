package com.example.dailydiary.ui.journal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.dailydiary.R;
import com.example.dailydiary.util.LocalDates;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class JournalFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_journal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String date = getArguments() == null ? null : getArguments().getString("localDate");
        if (date == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        JournalViewModel viewModel = new ViewModelProvider(this).get(JournalViewModel.class);
        TextView title = view.findViewById(R.id.text_journal_date);
        TextInputEditText input = view.findViewById(R.id.input_journal_text);
        TextView summary = view.findViewById(R.id.text_journal_summary);
        MaterialButton save = view.findViewById(R.id.button_save_journal);
        MaterialButton back = view.findViewById(R.id.button_journal_back);
        title.setText(LocalDates.displayLong(date));
        back.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        viewModel.setLocalDate(date);
        viewModel.getState().observe(getViewLifecycleOwner(), state -> {
            if (!input.hasFocus()) {
                input.setText(state.dayLog == null ? "" : state.dayLog.getFreeText());
            }
            String stored = state.dayLog == null ? null : state.dayLog.getSummaryText();
            summary.setText(stored == null || stored.trim().isEmpty()
                    ? state.autoSummary : stored);
        });
        save.setOnClickListener(v -> {
            String note = input.getText() == null ? "" : input.getText().toString();
            viewModel.saveNote(note);
            android.widget.Toast.makeText(requireContext(), R.string.journal_saved_toast, android.widget.Toast.LENGTH_SHORT).show();
        });
    }
}
