package com.example.dailydiary.ui.calendar;

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

public class CalendarFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CalendarViewModel viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);
        TextView monthTitle = view.findViewById(R.id.text_calendar_month);
        CalendarAdapter adapter = new CalendarAdapter(date -> {
            Bundle arguments = new Bundle();
            arguments.putString("localDate", date);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_calendarFragment_to_dayDetailFragment, arguments);
        });
        androidx.recyclerview.widget.RecyclerView calendar = view.findViewById(R.id.list_calendar_days);
        calendar.setAdapter(adapter);
        MaterialButton previous = view.findViewById(R.id.button_previous_month);
        MaterialButton next = view.findViewById(R.id.button_next_month);
        previous.setOnClickListener(v -> viewModel.previousMonth());
        next.setOnClickListener(v -> viewModel.nextMonth());
        viewModel.getMonth().observe(getViewLifecycleOwner(), value -> {
            if (value != null) monthTitle.setText(LocalDates.monthTitle(value[0], value[1]));
        });
        viewModel.getCells().observe(getViewLifecycleOwner(), adapter::submit);
    }
}
