package com.example.habitforge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.habitforge.application.model.Task;
import com.example.habitforge.application.model.User;
import com.example.habitforge.data.database.TaskLocalDataSource;
import com.example.habitforge.data.database.UserLocalDataSource;
import com.example.habitforge.presentation.activity.RegistrationActivity;
import com.google.firebase.FirebaseApp;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//        Button btnGoToRegister = findViewById(R.id.btnGoToRegister);
//
//        btnGoToRegister.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
//            startActivity(intent);
//        });
//
//        // --- Test ubacivanja korisnika ---
//        User testUser = new User();
//        testUser.setUserId("test123");
//        testUser.setEmail("test@example.com");
//        testUser.setUsername("Tester");
//        new UserLocalDataSource(this).insertUser(testUser);
//
//        // --- Test ubacivanja zadatka ---
//        Task testTask = new Task();
//        testTask.setId("task123");
//        testTask.setUserId("test123");
//        testTask.setName("Test Task");
//        new TaskLocalDataSource(this).addTask(testTask);
//    }
