package com.example.habitforge.presentation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    private final Context context;
    private List<Category> categories;
    private final OnCategoryActionListener listener;

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryActionListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    public void updateList(List<Category> newList) {
        this.categories = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category c = categories.get(position);
        holder.tvName.setText(c.getName());
        holder.colorView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(c.getColor())
        ));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(c));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(c));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View colorView;
        TextView tvName;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.viewCategoryColor);
            tvName = itemView.findViewById(R.id.tvCategoryName);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);

        }
    }
}
