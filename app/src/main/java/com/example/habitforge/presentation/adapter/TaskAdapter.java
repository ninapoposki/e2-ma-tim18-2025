package com.example.habitforge.presentation.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.presentation.activity.TaskDetailsActivity;

import java.util.List;
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private List<Task> tasks;
    private final List<Category> categories;


    public TaskAdapter(Context context, List<Task> tasks, List<Category> categories) {
        this.context = context;
        this.tasks = tasks;
        this.categories = categories;
    }
    private Category findCategoryById(String id) {
        if (categories == null) return null;
        for (Category c : categories) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }


    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false);
        return new TaskViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        // Naziv zadatka
        holder.name.setText(task.getName());
        holder.xp.setText("⭐ XP: " + task.getXp());

        // 👇 STATUS sa emoji-em
//        String statusEmoji;
        // 👇 Sigurnosna provera za null status
        String statusEmoji = "⚙️";
        String statusText = "UNKNOWN";

        if (task.getStatus() != null) {
            switch (task.getStatus()) {
                case COMPLETED:
                    statusEmoji = "✅";
                    statusText = "COMPLETED";
                    break;
                case ACTIVE:
                    statusEmoji = "🟢";
                    statusText = "ACTIVE";
                    break;
                case UNCOMPLETED:
                    statusEmoji = "⚠️";
                    statusText = "UNCOMPLETED";
                    break;
                case PAUSED:
                    statusEmoji = "⏸️";
                    statusText = "PAUSED";
                    break;
                case CANCELED:
                    statusEmoji = "❌";
                    statusText = "CANCELED";
                    break;
            }
        }

//        holder.status.setText(statusEmoji + " " + statusText);

        holder.status.setText(statusEmoji + " " + (task.getStatus() != null ? statusText : "UNKNOWN"));

        Category category = findCategoryById(task.getCategoryId());
        if (category != null) {
            holder.categoryName.setText(category.getName());
            try {
                holder.categoryColor.setBackgroundColor(android.graphics.Color.parseColor(category.getColor()));
            } catch (Exception e) {
                holder.categoryColor.setBackgroundColor(android.graphics.Color.LTGRAY);
            }
        } else {
            holder.categoryName.setText("(No category)");
            holder.categoryColor.setBackgroundColor(android.graphics.Color.DKGRAY);
        }



        // Klik za otvaranje detalja
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailsActivity.class);
            intent.putExtra("taskId", task.getId());
            context.startActivity(intent);
        });


    }
    public void updateCategories(List<Category> newCategories) {
        this.categories.clear();
        this.categories.addAll(newCategories);
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }



    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView name, xp, status, categoryName;
        View categoryColor;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.taskName);
            xp = itemView.findViewById(R.id.taskXp);
            status = itemView.findViewById(R.id.taskStatus);
            categoryName = itemView.findViewById(R.id.taskCategoryName);
            categoryColor = itemView.findViewById(R.id.taskCategoryColor);
        }
    }

}
