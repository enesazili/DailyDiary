package com.example.dailydiary.ui.calendar;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayHolder> {

    public interface Listener {
        void onDayClicked(String localDate);
    }

    private final Listener listener;
    private final List<CalendarViewModel.CalendarCell> cells = new ArrayList<>();

    public CalendarAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<CalendarViewModel.CalendarCell> newCells) {
        cells.clear();
        if (newCells != null) {
            cells.addAll(newCells);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new DayHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayHolder holder, int position) {
        holder.bind(cells.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    static class DayHolder extends RecyclerView.ViewHolder {
        private final TextView textDay;
        private final View dotTask;
        private final View dotRoutine;
        private final View dotJournal;

        DayHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.text_day);
            dotTask = itemView.findViewById(R.id.dot_task);
            dotRoutine = itemView.findViewById(R.id.dot_routine);
            dotJournal = itemView.findViewById(R.id.dot_journal);
        }

        void bind(CalendarViewModel.CalendarCell cell, Listener listener) {
            Context context = itemView.getContext();
            String day = cell.localDate.substring(8);
            if (day.startsWith("0")) {
                day = day.substring(1);
            }
            textDay.setText(day);

            if (cell.today) {
                textDay.setBackgroundResource(R.drawable.bg_today_circle);
                textDay.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                textDay.setAlpha(1.0f);
            } else {
                textDay.setBackground(null);
                TypedValue typedValue = new TypedValue();
                if (cell.inMonth) {
                    context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
                    textDay.setTextColor(ContextCompat.getColor(context, typedValue.resourceId != 0 ? typedValue.resourceId : android.R.color.black));
                    textDay.setAlpha(1.0f);
                } else {
                    context.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
                    textDay.setTextColor(ContextCompat.getColor(context, typedValue.resourceId != 0 ? typedValue.resourceId : android.R.color.darker_gray));
                    textDay.setAlpha(0.35f);
                }
            }

            dotTask.setVisibility(cell.marker.hasTask() ? View.VISIBLE : View.INVISIBLE);
            dotRoutine.setVisibility(cell.marker.hasRoutine() ? View.VISIBLE : View.INVISIBLE);
            dotJournal.setVisibility(cell.marker.hasJournal() ? View.VISIBLE : View.INVISIBLE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDayClicked(cell.localDate);
                }
            });
        }
    }
}
