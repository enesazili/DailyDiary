package com.example.dailydiary.ui.dashboard;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Routine;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardRoutineAdapter extends RecyclerView.Adapter<DashboardRoutineAdapter.RoutineViewHolder> {

    public interface Listener {
        void onRoutineChecked(Routine routine, boolean completed);
    }

    private final Listener listener;
    private final List<Routine> routines = new ArrayList<>();
    private final Set<Long> completedIds = new HashSet<>();

    public DashboardRoutineAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Routine> newRoutines, Set<Long> completed) {
        routines.clear();
        if (newRoutines != null) {
            routines.addAll(newRoutines);
        }
        completedIds.clear();
        if (completed != null) {
            completedIds.addAll(completed);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);
        boolean isCompleted = completedIds.contains(routine.getId());
        holder.bind(routine, isCompleted, listener);
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox check;
        private final TextView title;
        private final TextView description;

        RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check_routine);
            title = itemView.findViewById(R.id.text_routine_title);
            description = itemView.findViewById(R.id.text_routine_description);
        }

        void bind(Routine routine, boolean isCompleted, Listener listener) {
            check.setOnCheckedChangeListener(null);
            check.setChecked(isCompleted);

            title.setText(routine.getTitle());
            title.setAlpha(isCompleted ? 0.55f : 1.0f);
            if (isCompleted) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (routine.getDescription() == null || routine.getDescription().trim().isEmpty()) {
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
                description.setText(routine.getDescription());
                description.setAlpha(isCompleted ? 0.45f : 0.8f);
            }

            check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onRoutineChecked(routine, isChecked);
                }
            });

            itemView.setOnClickListener(v -> check.setChecked(!check.isChecked()));
        }
    }
}
