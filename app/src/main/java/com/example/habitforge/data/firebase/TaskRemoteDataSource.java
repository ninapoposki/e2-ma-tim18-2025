package com.example.habitforge.data.firebase;

import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.example.habitforge.application.model.Task;

import java.util.List;

public class TaskRemoteDataSource {

    private final FirebaseFirestore db;

    public TaskRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    // --- SAVE TASK ---
    public void saveTask(Task task, OnCompleteListener<Void> listener) {
        if (task.getId() != null) {
            db.collection("tasks").document(task.getId())
                    .set(task)
                    .addOnCompleteListener(listener);
        }
    }

    // --- DELETE TASK ---
    public void deleteTask(String taskId, OnCompleteListener<Void> listener) {
        db.collection("tasks").document(taskId)
                .delete()
                .addOnCompleteListener(listener);
    }

    // --- FETCH TASK BY ID ---
    public void fetchTaskById(String taskId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("tasks").document(taskId)
                .get()
                .addOnCompleteListener(listener);
    }

    // --- FETCH ALL TASKS FOR USER ---
    public void fetchTasksForUser(String userId, OnCompleteListener<QuerySnapshot> listener) {
        db.collection("tasks")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(listener);
    }

    // --- FETCH ALL TASKS ---
    public void fetchAllTasks(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("tasks")
                .get()
                .addOnCompleteListener(listener);
    }
}
