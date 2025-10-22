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
        taskRepository.getAllTasks(taskResult -> {
            if (taskResult.isSuccessful() && taskResult.getResult() != null) {
                List<Task> tasks = taskResult.getResult();

                boolean hasActiveTask = false;

                for (Task task : tasks) {
                    if (task.getCategoryId() != null &&
                            task.getCategoryId().equals(categoryId) &&
                            task.getStatus() != null &&
                            task.getStatus().name().equalsIgnoreCase("ACTIVE")) {

                        hasActiveTask = true;
                        break;
                    }
                }

                if (hasActiveTask) {
                    callback.onComplete(Tasks.forException(
                            new IllegalStateException("You cannot delete this category because it has active tasks!")
                    ));
                } else {
                    categoryRepository.deleteCategory(categoryId, callback);
                }
            } else {
                callback.onComplete(Tasks.forException(
                        new Exception("Failed to check active tasks before deleting category.")
                ));
            }
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
