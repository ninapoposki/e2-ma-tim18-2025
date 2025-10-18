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
import com.example.habitforge.application.session.EquipmentInitializer;
import com.example.habitforge.data.database.TaskLocalDataSource;
import com.example.habitforge.data.database.UserLocalDataSource;
import com.example.habitforge.presentation.activity.RegistrationActivity;
import com.example.habitforge.presentation.activity.ui.login.LoginActivity;
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
        Button btnGoToLogin = findViewById(R.id.btnGoToLogin);
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        EquipmentInitializer initializer = new EquipmentInitializer();
        initializer.initializeEquipment();

    }
}
