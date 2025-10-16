package com.example.habitforge.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.habitforge.application.model.Category;
import com.example.habitforge.data.database.CategoryLocalDataSource;
import com.example.habitforge.data.firebase.CategoryRemoteDataSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository {
    private final CategoryLocalDataSource localDb;
    private final CategoryRemoteDataSource remoteDb;

    public CategoryRepository(Context context) {
        this.localDb = new CategoryLocalDataSource(context);
        this.remoteDb = new CategoryRemoteDataSource();
    }

    public void addCategory(Category category, OnCompleteListener<Void> callback){
        remoteDb.saveCategoryDocument(category, remoteTask -> {
            if (remoteTask.isSuccessful()) {
                localDb.insertCategory(category);
            }
            callback.onComplete(remoteTask);
        });
    }

    public void updateCategory(Category category, OnCompleteListener<Void> callback) {
        Log.d("DEBUG_FIREBASE", "updateCategory called for " + category.getName());

        localDb.updateCategory(category);
        remoteDb.saveCategoryDocument(category, task -> {
            if (task.isSuccessful()) {
                Log.d("DEBUG_FIREBASE", "updateCategory SUCCESS Firebase");
            } else {
                Log.e("DEBUG_FIREBASE", "updateCategory FAILED: " + task.getException());
            }
            callback.onComplete(task);
        });
    }

    public void deleteCategory(String categoryId, OnCompleteListener<Void> callback) {
        Log.d("DEBUG_FIREBASE", "deleteCategory called for ID " + categoryId);

        remoteDb.deleteCategoryDocument(categoryId, remoteTask -> {
            if (remoteTask.isSuccessful()) {
                Log.d("DEBUG_FIREBASE", "deleteCategory SUCCESS Firebase");
                localDb.deleteCategory(categoryId, callback);
            } else {
                Log.e("DEBUG_FIREBASE", "deleteCategory FAILED: " + remoteTask.getException());
            }
            callback.onComplete(remoteTask);
        });
    }


    public void getCategoryById(String categoryId,OnCompleteListener<Category> callback){
        Category cached=localDb.getCategoryById(categoryId);

        if(cached!=null){
            callback.onComplete(Tasks.forResult(cached));
        }

        remoteDb.fetchCategoryDocument(categoryId,remoteTask->{
            if(remoteTask.isSuccessful()&&remoteTask.getResult()!=null&&remoteTask.getResult().exists()){
                Category cloudCategory = remoteTask.getResult().toObject(Category.class);
                if (cloudCategory != null) {
                    localDb.insertCategory(cloudCategory); // update cache
                    callback.onComplete(Tasks.forResult(cloudCategory));
                }
            } else if (cached == null) {
                callback.onComplete(Tasks.forException(remoteTask.getException()));

            }
        });
    }
    public CategoryRemoteDataSource getRemoteDb() {
        return remoteDb;
    }


    public void getAllCategories(OnCompleteListener<List<Category>> callback){
        List<Category> cached=localDb.getAllCategories();

        if(!cached.isEmpty()){
            callback.onComplete(Tasks.forResult(cached));
        }
        remoteDb.fetchAllCategories(remoteTask -> {
            if (remoteTask.isSuccessful() && remoteTask.getResult() != null) {
                List<Category> remoteCategories = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : remoteTask.getResult()) {
                    Category c = doc.toObject(Category.class);
                    remoteCategories.add(c);
                    localDb.insertCategory(c);
                }
                callback.onComplete(Tasks.forResult(remoteCategories));
            } else if (cached.isEmpty()) {
                callback.onComplete(Tasks.forException(remoteTask.getException()));
            }
        });
    }
}
