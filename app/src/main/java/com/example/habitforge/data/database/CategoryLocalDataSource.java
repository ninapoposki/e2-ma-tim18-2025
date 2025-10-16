package com.example.habitforge.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habitforge.application.model.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class CategoryLocalDataSource {
    private final DatabaseHelper dbHelper;

    public CategoryLocalDataSource(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // --- INSERT ---
    public void insertCategory(Category c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("id", c.getId());
        cv.put("name", c.getName());
        cv.put("color", c.getColor());
        db.insert(DatabaseHelper.T_CATEGORIES, null, cv);
        db.close();
    }

    // --- GET BY ID ---
    public Category getCategoryById(String categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_CATEGORIES, null,
                "id = ?", new String[]{categoryId},
                null, null, null);
        Category category = null;
        if (c != null && c.moveToFirst()) {
            category = cursorToCategory(c);
            c.close();
        }
        db.close();
        return category;
    }

    // --- GET ALL ---
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(DatabaseHelper.T_CATEGORIES, null,
                null, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            do {
                categories.add(cursorToCategory(c));
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return categories;
    }

    // --- UPDATE ---
    public void updateCategory(Category c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", c.getName());
        cv.put("color", c.getColor());
        db.update(DatabaseHelper.T_CATEGORIES, cv,
                "id = ?", new String[]{c.getId()});
        db.close();
    }

    // --- DELETE ---
    public void deleteCategory(String categoryId, OnCompleteListener<Void> callback) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.T_CATEGORIES, "id = ?", new String[]{categoryId});
        db.close();
        if (callback != null) {
            callback.onComplete(Tasks.forResult(null));
        }
    }
    // --- HELPER ---
    private Category cursorToCategory(Cursor c) {
        Category category = new Category();
        category.setId(c.getString(c.getColumnIndexOrThrow("id")));
        category.setName(c.getString(c.getColumnIndexOrThrow("name")));
        category.setColor(c.getString(c.getColumnIndexOrThrow("color")));
        return category;
    }
}
