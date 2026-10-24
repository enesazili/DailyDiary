package com.example.dailydiary.ui.calendar;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.Routine;
import com.example.dailydiary.domain.model.Task;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DayDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER = 0;
    private static final int TASK = 1;
    private static final int ROUTINE = 2;
    private final List<Item> items = new ArrayList<>();

    public void submit(List<Task> tasks, List<Routine> routines, Set<Long> completedRoutineIds) {
        items.clear();
        items.add(new Item(HEADER, null, null, false, R.string.day_detail_tasks));
        if (tasks != null) {
            for (Task task : tasks) items.add(new Item(TASK, task, null, task.isCompleted(), 0));
        }
        items.add(new Item(HEADER, null, null, false, R.string.day_detail_routines));
        if (routines != null) {
            for (Routine routine : routines) {
                boolean completed = completedRoutineIds != null && completedRoutineIds.contains(routine.getId());
                items.add(new Item(ROUTINE, null, routine, completed, 0));
            }
        }
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) { return items.get(position).type; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (type == HEADER) {
            return new HeaderHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        return new CheckHolder(inflater.inflate(R.layout.item_check_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).bind(item.headerRes);
        } else {
            ((CheckHolder) holder).bind(item);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        private final TextView header;
        HeaderHolder(View itemView) { super(itemView); header = itemView.findViewById(R.id.text_header); }
        void bind(int textRes) { header.setText(textRes); }
    }

    static class CheckHolder extends RecyclerView.ViewHolder {
        private final MaterialCheckBox check;
        private final TextView title;
        private final TextView subtitle;
        CheckHolder(View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.check_item);
            title = itemView.findViewById(R.id.text_title);
            subtitle = itemView.findViewById(R.id.text_subtitle);
        }
        void bind(Item item) {
            check.setOnCheckedChangeListener(null);
            check.setEnabled(false);
            check.setChecked(item.completed);
            String text = item.task != null ? item.task.getTitle() : item.routine.getTitle();
            String detail = item.task != null ? item.task.getNotes() : item.routine.getDescription();
            title.setText(text);
            title.setAlpha(item.completed ? 0.55f : 1f);
            title.setPaintFlags(item.completed ? title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            if (detail == null || detail.isEmpty()) {
                subtitle.setVisibility(View.GONE);
            } else {
                subtitle.setVisibility(View.VISIBLE);
                subtitle.setText(detail);
            }
        }
    }

    private static class Item {
        final int type;
        final Task task;
        final Routine routine;
        final boolean completed;
        final int headerRes;
        Item(int type, Task task, Routine routine, boolean completed, int headerRes) {
            this.type = type;
            this.task = task;
            this.routine = routine;
            this.completed = completed;
            this.headerRes = headerRes;
        }
    }
}
