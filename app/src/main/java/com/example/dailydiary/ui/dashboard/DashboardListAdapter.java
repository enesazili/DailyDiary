package com.example.dailydiary.ui.dashboard;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.Objects;

public class DashboardListAdapter extends ListAdapter<DashboardItem, RecyclerView.ViewHolder> {

    public interface Listener {
        void onTaskChecked(Task task, boolean completed);

        void onRoutineChecked(Routine routine, boolean completed);
    }

    private final Listener listener;

    public DashboardListAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<DashboardItem> DIFF =
            new DiffUtil.ItemCallback<DashboardItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull DashboardItem a, @NonNull DashboardItem b) {
                    return a.getKey().equals(b.getKey()) && a.getType() == b.getType();
                }

                @Override
                public boolean areContentsTheSame(@NonNull DashboardItem a, @NonNull DashboardItem b) {
                    if (a.getType() != b.getType()) {
                        return false;
                    }
                    if (a instanceof DashboardItem.Header && b instanceof DashboardItem.Header) {
                        return Objects.equals(((DashboardItem.Header) a).title,
                                ((DashboardItem.Header) b).title);
                    }
                    if (a instanceof DashboardItem.TaskRow && b instanceof DashboardItem.TaskRow) {
                        return Objects.equals(((DashboardItem.TaskRow) a).task,
                                ((DashboardItem.TaskRow) b).task);
                    }
                    if (a instanceof DashboardItem.RoutineRow && b instanceof DashboardItem.RoutineRow) {
                        DashboardItem.RoutineRow left = (DashboardItem.RoutineRow) a;
                        DashboardItem.RoutineRow right = (DashboardItem.RoutineRow) b;
                        return left.completed == right.completed
                                && Objects.equals(left.routine, right.routine);
                    }
                    return false;
                }
            };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == DashboardItem.HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderHolder(view);
        }
        if (viewType == DashboardItem.ROUTINE) {
            View view = inflater.inflate(R.layout.item_check_row, parent, false);
            return new RoutineHolder(view);
        }
        View view = inflater.inflate(R.layout.item_check_row, parent, false);
        return new TaskHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DashboardItem item = getItem(position);
        if (holder instanceof HeaderHolder && item instanceof DashboardItem.Header) {
            ((HeaderHolder) holder).bind((DashboardItem.Header) item);
        } else if (holder instanceof TaskHolder && item instanceof DashboardItem.TaskRow) {
            ((TaskHolder) holder).bind(((DashboardItem.TaskRow) item).task, listener);
        } else if (holder instanceof RoutineHolder && item instanceof DashboardItem.RoutineRow) {
            DashboardItem.RoutineRow row = (DashboardItem.RoutineRow) item;
            ((RoutineHolder) holder).bind(row.routine, row.completed, listener);
        }
    }

    static void applyCompleted(TextView title, boolean completed) {
        title.setAlpha(completed ? 0.55f : 1f);
        if (completed) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        private final TextView text;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text_header);
        }

        void bind(DashboardItem.Header header) {
            text.setText(header.title);
        }
    }

    static class TaskHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox check;
        private final TextView title;
        private final TextView subtitle;

        TaskHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check_item);
            title = itemView.findViewById(R.id.text_title);
            subtitle = itemView.findViewById(R.id.text_subtitle);
        }

        void bind(Task task, Listener listener) {
            check.setOnCheckedChangeListener(null);
            check.setChecked(task.isCompleted());
            title.setText(task.getTitle());
            applyCompleted(title, task.isCompleted());
            if (task.getNotes() == null || task.getNotes().isEmpty()) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(task.getNotes());
            }
            check.setOnCheckedChangeListener((buttonView, isChecked) ->
                    listener.onTaskChecked(task, isChecked));
        }
    }

    static class RoutineHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox check;
        private final TextView title;
        private final TextView subtitle;

        RoutineHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check_item);
            title = itemView.findViewById(R.id.text_title);
            subtitle = itemView.findViewById(R.id.text_subtitle);
        }

        void bind(Routine routine, boolean completed, Listener listener) {
            check.setOnCheckedChangeListener(null);
            check.setChecked(completed);
            title.setText(routine.getTitle());
            applyCompleted(title, completed);
            if (routine.getDescription() == null || routine.getDescription().isEmpty()) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(routine.getDescription());
            }
            check.setOnCheckedChangeListener((buttonView, isChecked) ->
                    listener.onRoutineChecked(routine, isChecked));
        }
    }
}
