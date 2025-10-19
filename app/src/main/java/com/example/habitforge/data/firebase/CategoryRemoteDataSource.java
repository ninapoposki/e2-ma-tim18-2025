package com.example.habitforge.data.firebase;

import com.example.habitforge.application.model.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class CategoryRemoteDataSource {
    private final FirebaseFirestore db;

    public CategoryRemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    //---INSERT---
    public void saveCategoryDocument(Category category, OnCompleteListener<Void> listener) {
        if (category.getId() != null) {
            db.collection("categories")
                    .document(category.getId())
                    .set(category)
                    .addOnCompleteListener(listener);
        }
    }


    //---GET BY ID---
    public void fetchCategoryDocument(String categoryId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("categories")
                .document(categoryId)
                .get()
                .addOnCompleteListener(listener);
    }

    //---GET ALL---
    public void fetchAllCategories(OnCompleteListener<QuerySnapshot> listener) {
        db.collection("categories")
                .get()
                .addOnCompleteListener(listener);
    }

    //---DELETE---
    public void deleteCategoryDocument(String categoryId, OnCompleteListener<Void> listener) {
        db.collection("categories")
                .document(categoryId)
                .delete()
                .addOnCompleteListener(listener);
    }
}
