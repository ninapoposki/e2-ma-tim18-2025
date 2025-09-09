package com.example.habitforge.presentation.activity;
//
//import android.os.Bundle;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.example.habitforge.R;
//
////public class RegistrationActivity extends AppCompatActivity {
////
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        EdgeToEdge.enable(this);
////        setContentView(R.layout.activity_registration);
////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
////            return insets;
////        });
////    }
//
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.habitforge.R;

//
//import com.example.habitforge.R;
//import com.example.habitforge.application.service.UserService;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.firebase.auth.AuthResult;
//
    public class RegistrationActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    }
//
//        private EditText etEmail, etPassword, etRepeatPassword, etUsername;
//        private Button btnRegister;
//        private String selectedAvatar = null;
//        private UserService userService;
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState) {
//            super.onCreate(savedInstanceState);
//            setContentView(R.layout.activity_registration);
//
//            etEmail = findViewById(R.id.etEmail);
//            etPassword = findViewById(R.id.etPassword);
//            etRepeatPassword = findViewById(R.id.etRepeatPassword);
//            etUsername = findViewById(R.id.etUsername);
//            btnRegister = findViewById(R.id.btnRegister);
//
//            userService = new UserService(this);
//
//            // avatar dugmici
//            findViewById(R.id.btnAvatar1).setOnClickListener(v -> selectedAvatar = "avatar1.png");
//            findViewById(R.id.btnAvatar2).setOnClickListener(v -> selectedAvatar = "avatar2.png");
//            findViewById(R.id.btnAvatar3).setOnClickListener(v -> selectedAvatar = "avatar3.png");
//            findViewById(R.id.btnAvatar4).setOnClickListener(v -> selectedAvatar = "avatar4.png");
//            findViewById(R.id.btnAvatar5).setOnClickListener(v -> selectedAvatar = "avatar5.png");
//
//            btnRegister.setOnClickListener(v -> {
//                String email = etEmail.getText().toString();
//                String password = etPassword.getText().toString();
//                String repeatPassword = etRepeatPassword.getText().toString();
//                String username = etUsername.getText().toString();
//
//                try {
//                    userService.registerUser(email, password, repeatPassword, username, selectedAvatar,
//                            (OnCompleteListener<AuthResult>) task -> {
//                                if (task.isSuccessful()) {
//                                    Toast.makeText(this, "Registracija uspešna! Proverite email za aktivaciju.", Toast.LENGTH_LONG).show();
//                                } else {
//                                    Toast.makeText(this, "Greška: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                                }
//                            });
//                } catch (IllegalArgumentException e) {
//                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
//
//}