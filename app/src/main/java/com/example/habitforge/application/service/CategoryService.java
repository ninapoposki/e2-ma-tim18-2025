package com.example.habitforge.application.service;
import android.content.Context;

import com.example.habitforge.application.model.Category;
import com.example.habitforge.application.model.Task;
import com.example.habitforge.data.repository.CategoryRepository;
import com.example.habitforge.data.repository.TaskRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public CategoryService(Context context) {
        this.categoryRepository = new CategoryRepository(context);
        this.taskRepository = TaskRepository.getInstance(context); // Task repo je singleton
    }


//    public void addCategory(Category category, OnCompleteListener<Void> callback) {
//        if (category.getName() == null || category.getName().trim().isEmpty()) {
//            callback.onComplete(Tasks.forException(
//                    new IllegalArgumentException("Category name is required!")
//            ));
//            return;
//        }
//        if (category.getColor() == null || category.getColor().trim().isEmpty()) {
//            callback.onComplete(Tasks.forException(
//                    new IllegalArgumentException("Category color is required!")
//            ));
//            return;
//        }
//
//        categoryRepository.getAllCategories(task -> {
//            if (task.isSuccessful() && task.getResult() != null) {
//                List<Category> existing = task.getResult();
//
//                for (Category c : existing) {
//                    if (c.getColor().equalsIgnoreCase(category.getColor())) {
//                        callback.onComplete(Tasks.forException(
//                                new IllegalArgumentException("A category with this color already exists!")
//                        ));
//                        return;
//                    }
//                }
//
//                categoryRepository.addCategory(category, callback);
//            } else {
//                callback.onComplete(Tasks.forException(
//                        new Exception("Failed to fetch categories for validation")
//                ));
//            }
//        });
//    }
public void addCategory(Category category, OnCompleteListener<Void> callback) {
    if (category.getName() == null || category.getName().trim().isEmpty()) {
        callback.onComplete(Tasks.forException(
                new IllegalArgumentException("Category name is required!")
        ));
        return;
    }
    if (category.getColor() == null || category.getColor().trim().isEmpty()) {
        callback.onComplete(Tasks.forException(
                new IllegalArgumentException("Category color is required!")
        ));
        return;
    }



    categoryRepository.getRemoteDb().fetchAllCategories(remoteTask -> {
        if (remoteTask.isSuccessful() && remoteTask.getResult() != null) {
            boolean colorExists = false;

            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : remoteTask.getResult()) {
                Category c = doc.toObject(Category.class);
                if (c.getColor().equalsIgnoreCase(category.getColor())) {
                    colorExists = true;
                    break;
                }
            }

            if (colorExists) {
                callback.onComplete(Tasks.forException(
                        new IllegalArgumentException("A category with this color already exists!")
                ));
            } else {
                categoryRepository.addCategory(category, callback);
            }

        } else {
            callback.onComplete(Tasks.forException(
                    new Exception("Failed to fetch categories from Firebase.")
            ));
        }
    });
}



    public void updateCategory(Category category, OnCompleteListener<Void> callback) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required!");
        }
        if (category.getColor() == null || category.getColor().trim().isEmpty()) {
            throw new IllegalArgumentException("Category color is required!");
        }

        categoryRepository.updateCategory(category, callback);
    }

    public void deleteCategory(String categoryId, OnCompleteListener<Void> callback) {

        taskRepository.getTaskById(categoryId, taskResult -> {
            if (taskResult.isSuccessful() && taskResult.getResult() != null) {
                Task foundTask = taskResult.getResult();

                if (foundTask != null && foundTask.getStatus() != null
                        && foundTask.getStatus().name().equals("ACTIVE")) {
                    // Aif it has active task-stop
                    callback.onComplete(com.google.android.gms.tasks.Tasks.forException(
                            new IllegalStateException("You cannot delete a category that has active tasks!")));
                    return;
                }
            }

            // Ako nema aktivnih taskova → slobodno briši
            categoryRepository.deleteCategory(categoryId, callback);
        });
    }


    public void getCategoryById(String id, OnCompleteListener<Category> callback) {
        categoryRepository.getCategoryById(id, callback);
    }

    public void getAllCategories(OnCompleteListener<List<Category>> callback) {
        categoryRepository.getAllCategories(callback);
    }
//    public void getCategoryByIdWithoutSave(String id, OnCompleteListener<Category> callback) {
//        categoryRepository.getRemoteDb().getCategoryById(id, callback);
//    }

}
