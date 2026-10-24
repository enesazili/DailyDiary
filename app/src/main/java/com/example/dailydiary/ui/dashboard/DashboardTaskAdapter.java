package com.example.dailydiary.ui.dashboard;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Task;
import com.example.dailydiary.domain.model.TaskCategories;
import com.example.dailydiary.ui.common.CategoryLabels;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    public interface Listener {
        void onTaskChecked(Task task, boolean completed);
    }

    private static final class DisplayItem {
        final int type;
        final String categoryHeader;
        final Task task;

        DisplayItem(String categoryHeader) {
            this.type = TYPE_HEADER;
            this.categoryHeader = categoryHeader;
            this.task = null;
        }

        DisplayItem(Task task) {
            this.type = TYPE_TASK;
            this.categoryHeader = null;
            this.task = task;
        }
    }

    private final Listener listener;
    private final List<DisplayItem> items = new ArrayList<>();

    public DashboardTaskAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(Context context, List<Task> tasks) {
        items.clear();
        if (tasks != null && !tasks.isEmpty()) {
            Map<String, List<Task>> grouped = new LinkedHashMap<>();
            for (String id : TaskCategories.builtInIds()) {
                grouped.put(id, new ArrayList<>());
            }
            List<Task> extras = new ArrayList<>();
            for (Task task : tasks) {
                if (grouped.containsKey(task.getCategory())) {
                    grouped.get(task.getCategory()).add(task);
                } else {
                    extras.add(task);
                }
            }
            for (Map.Entry<String, List<Task>> entry : grouped.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    items.add(new DisplayItem(CategoryLabels.label(context, entry.getKey())));
                    for (Task task : entry.getValue()) {
                        items.add(new DisplayItem(task));
                    }
                }
            }
            if (!extras.isEmpty()) {
                items.add(new DisplayItem(CategoryLabels.label(context, TaskCategories.DIGER)));
                for (Task task : extras) {
                    items.add(new DisplayItem(task));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_dashboard_task_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_dashboard_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(item.categoryHeader);
        } else if (holder instanceof TaskViewHolder && item.task != null) {
            ((TaskViewHolder) holder).bind(item.task, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerText;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.text_category_header);
        }

        void bind(String title) {
            headerText.setText(title);
        }
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox check;
        private final TextView title;
        private final TextView notes;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check_task);
            title = itemView.findViewById(R.id.text_task_title);
            notes = itemView.findViewById(R.id.text_task_notes);
        }

        void bind(Task task, Listener listener) {
            check.setOnCheckedChangeListener(null);
            check.setChecked(task.isCompleted());

            title.setText(task.getTitle());
            boolean isCompleted = task.isCompleted();
            title.setAlpha(isCompleted ? 0.55f : 1.0f);
            if (isCompleted) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }

            if (task.getNotes() == null || task.getNotes().trim().isEmpty()) {
                notes.setVisibility(View.GONE);
            } else {
                notes.setVisibility(View.VISIBLE);
                notes.setText(task.getNotes());
                notes.setAlpha(isCompleted ? 0.45f : 0.8f);
            }

            check.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskChecked(task, isChecked);
                }
            });

            itemView.setOnClickListener(v -> check.setChecked(!check.isChecked()));
        }
    }
}
