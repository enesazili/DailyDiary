package com.example.dailydiary.ui.routines;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.ScheduleType;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.RoutineHolder> {

    public interface Listener {
        void onCompletionChanged(Routine routine, boolean completed);
        void onActiveChanged(Routine routine, boolean active);
        void onEdit(Routine routine);
        void onDelete(Routine routine);
    }

    private final Listener listener;
    private final List<Routine> routines = new ArrayList<>();
    private Set<Long> completedIds = new HashSet<>();
    private String today;

    public RoutinesAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(String today, List<Routine> values, Set<Long> completedRoutineIds) {
        this.today = today;
        routines.clear();
        if (values != null) {
            routines.addAll(values);
        }
        completedIds = completedRoutineIds == null ? new HashSet<>() : new HashSet<>(completedRoutineIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoutineHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineHolder holder, int position) {
        Routine routine = routines.get(position);
        holder.bind(routine, routine.appliesOn(today), completedIds.contains(routine.getId()), listener);
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    static class RoutineHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox completion;
        private final TextView title;
        private final TextView details;
        private final SwitchMaterial active;
        private final View edit;
        private final View delete;

        RoutineHolder(@NonNull View itemView) {
            super(itemView);
            completion = itemView.findViewById(R.id.check_routine_completion);
            title = itemView.findViewById(R.id.text_routine_title);
            details = itemView.findViewById(R.id.text_routine_details);
            active = itemView.findViewById(R.id.switch_routine_active);
            edit = itemView.findViewById(R.id.button_edit_routine);
            delete = itemView.findViewById(R.id.button_delete_routine);
        }

        void bind(Routine routine, boolean scheduledToday, boolean completed, Listener listener) {
            completion.setOnCheckedChangeListener(null);
            completion.setChecked(completed);
            completion.setEnabled(scheduledToday);
            title.setText(routine.getTitle());
            title.setAlpha(completed ? 0.55f : 1f);
            title.setPaintFlags(completed ? title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            details.setText(detailText(itemView, routine, scheduledToday));
            active.setOnCheckedChangeListener(null);
            active.setChecked(routine.isActive());
            active.setOnCheckedChangeListener((button, checked) -> listener.onActiveChanged(routine, checked));
            completion.setOnCheckedChangeListener((button, checked) -> {
                if (scheduledToday) {
                    listener.onCompletionChanged(routine, checked);
                }
            });
            edit.setOnClickListener(v -> listener.onEdit(routine));
            delete.setOnClickListener(v -> listener.onDelete(routine));
            itemView.setOnClickListener(v -> listener.onEdit(routine));
        }

        private String detailText(View view, Routine routine, boolean scheduledToday) {
            int scheduleRes = routine.getScheduleType() == ScheduleType.WEEKDAYS
                    ? R.string.routine_schedule_weekdays
                    : routine.getScheduleType() == ScheduleType.CUSTOM
                    ? R.string.routine_schedule_custom : R.string.routine_schedule_every_day;
            int statusRes = !routine.isActive() ? R.string.routine_inactive
                    : scheduledToday ? R.string.routine_today : R.string.routine_not_today;
            String info = view.getContext().getString(scheduleRes) + " · "
                    + view.getContext().getString(statusRes);
            return routine.getDescription() == null || routine.getDescription().isEmpty()
                    ? info : routine.getDescription() + " · " + info;
        }
    }
}
