package com.example.habitforge.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habitforge.R;
import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.service.CategoryService;
import com.example.habitforge.presentation.adapter.CategoryAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class CategoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private CategoryService categoryService;
    private MaterialButton btnAddCategory;
    private List<Category> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        categoryService = new CategoryService(this);
        recyclerView = findViewById(R.id.recyclerCategories);
        btnAddCategory = findViewById(R.id.btnAddCategory);

        adapter = new CategoryAdapter(this, categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onEdit(Category category) {
                Intent intent = new Intent(CategoryListActivity.this, AddCategoryActivity.class);
                intent.putExtra("editCategoryId", category.getId());
                startActivity(intent);
            }


            @Override
            public void onDelete(Category category) {
                new androidx.appcompat.app.AlertDialog.Builder(CategoryListActivity.this)
                        .setTitle("Delete category")
                        .setMessage("Are you sure you want to delete \"" + category.getName() + "\"?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            categoryService.deleteCategory(category.getId(), task -> {
                                if (task.isSuccessful()) {
                                    categoryList.remove(category);
                                    adapter.updateList(categoryList);
                                    Toast.makeText(CategoryListActivity.this, "Deleted " + category.getName(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(CategoryListActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        })
                        .setNegativeButton("No", null)
                        .show();
            }

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAddCategory.setOnClickListener(v ->
                startActivity(new Intent(this, AddCategoryActivity.class))
        );

        loadCategories();
    }

    private void loadCategories() {
        categoryService.getAllCategories(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                categoryList = task.getResult();
                adapter.updateList(categoryList);
            } else {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadCategories();  // ssvako vracanje na ekran refreshuje kategorije
    }

}
