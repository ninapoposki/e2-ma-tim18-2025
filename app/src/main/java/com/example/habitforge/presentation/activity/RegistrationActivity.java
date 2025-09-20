package com.example.habitforge.presentation.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.habitforge.R;
import com.example.habitforge.application.service.UserService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etRepeatPassword, etUsername;
    private TextInputLayout tilEmail, tilPassword, tilRepeatPassword, tilUsername;
    private Button btnRegister;
    private String selectedAvatar = null;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        userService = new UserService(this);

        tilEmail = findViewById(R.id.tilEmail);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilRepeatPassword = findViewById(R.id.tilRepeatPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        etUsername = findViewById(R.id.etUsername);
        btnRegister = findViewById(R.id.btnRegister);

        // avatar dugmici
        findViewById(R.id.btnAvatar1).setOnClickListener(v -> selectedAvatar = "avatar1");
        findViewById(R.id.btnAvatar2).setOnClickListener(v -> selectedAvatar = "avatar2");
        findViewById(R.id.btnAvatar3).setOnClickListener(v -> selectedAvatar = "avatar3");
        findViewById(R.id.btnAvatar4).setOnClickListener(v -> selectedAvatar = "avatar4");
        findViewById(R.id.btnAvatar5).setOnClickListener(v -> selectedAvatar = "avatar5");

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    private void validateAndRegister() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etRepeatPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        // Reset errors
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilRepeatPassword.setError(null);
        tilUsername.setError(null);

        // Validacija
        if (email.isEmpty()) {
            tilEmail.setError("Unesite email");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Neispravan email");
            return;
        }
        if (username.isEmpty()) {
            tilUsername.setError("Unesite korisničko ime");
            return;
        }
        if (password.isEmpty()) {
            tilPassword.setError("Unesite lozinku");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Lozinka mora imati bar 6 karaktera");
            return;
        }
        if (!password.equals(repeatPassword)) {
            tilRepeatPassword.setError("Lozinke se ne poklapaju");
            return;
        }
        if (selectedAvatar == null) {
            Toast.makeText(this, "Izaberite avatar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Registracija
        try {
            userService.registerUser(email, password, repeatPassword, username, selectedAvatar, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Registracija uspešna! Proverite email.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, "Greška: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}


//package com.example.habitforge.presentation.activity;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.habitforge.R;
//import com.example.habitforge.application.service.UserService;
//
////
//public class RegistrationActivity extends AppCompatActivity {
//    private EditText etEmail, etPassword, etRepeatPassword, etUsername;
//    private Button btnRegister;
//    private String selectedAvatar = null; // zapamti izbor avatara
//    private UserService userService;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_registration);
//
//        userService = new UserService(this);
//
//        // Poveži UI elemente
//        etEmail = findViewById(R.id.etEmail);
//        etPassword = findViewById(R.id.etPassword);
//        etRepeatPassword = findViewById(R.id.etRepeatPassword);
//        etUsername = findViewById(R.id.etUsername);
//        btnRegister = findViewById(R.id.btnRegister);
//
//        // Avatari (primer za 5 dugmića)
//        findViewById(R.id.btnAvatar1).setOnClickListener(v -> selectedAvatar = "avatar1");
//        findViewById(R.id.btnAvatar2).setOnClickListener(v -> selectedAvatar = "avatar2");
//        findViewById(R.id.btnAvatar3).setOnClickListener(v -> selectedAvatar = "avatar3");
//        findViewById(R.id.btnAvatar4).setOnClickListener(v -> selectedAvatar = "avatar4");
//        findViewById(R.id.btnAvatar5).setOnClickListener(v -> selectedAvatar = "avatar5");
//
//        // Registracija
//        btnRegister.setOnClickListener(v -> {
//            String email = etEmail.getText().toString().trim();
//            String password = etPassword.getText().toString().trim();
//            String repeatPassword = etRepeatPassword.getText().toString().trim();
//            String username = etUsername.getText().toString().trim();
//
//            try {
//                userService.registerUser(email, password, repeatPassword, username, selectedAvatar, task -> {
//                    if (task.isSuccessful()) {
//                        Toast.makeText(this, "Registracija uspešna! Proverite email za aktivaciju.", Toast.LENGTH_LONG).show();
//                        finish(); // možeš da prebaciš na login activity
//                    } else {
//                        Toast.makeText(this, "Greška: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//            } catch (IllegalArgumentException e) {
//                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }
//}
//
////import android.os.Bundle;
////
////import androidx.activity.EdgeToEdge;
////import androidx.appcompat.app.AppCompatActivity;
////import androidx.core.graphics.Insets;
////import androidx.core.view.ViewCompat;
////import androidx.core.view.WindowInsetsCompat;
////
////import com.example.habitforge.R;
////
//////public class RegistrationActivity extends AppCompatActivity {
//////
//////    @Override
//////    protected void onCreate(Bundle savedInstanceState) {
//////        super.onCreate(savedInstanceState);
//////        EdgeToEdge.enable(this);
//////        setContentView(R.layout.activity_registration);
//////        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//////            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//////            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//////            return insets;
//////        });
//////    }
////
////import android.os.Bundle;
////import android.widget.Button;
////import android.widget.EditText;
////import android.widget.Toast;
////
////import androidx.appcompat.app.AppCompatActivity;
////
////import com.example.habitforge.R;
////import com.example.habitforge.application.service.UserService;
////import com.google.android.gms.tasks.OnCompleteListener;
////import com.google.firebase.auth.AuthResult;
////
////    public class RegistrationActivity extends AppCompatActivity {
////
////        private EditText etEmail, etPassword, etRepeatPassword, etUsername;
////        private Button btnRegister;
////        private String selectedAvatar = null;
////        private UserService userService;
////
////        @Override
////        protected void onCreate(Bundle savedInstanceState) {
////            super.onCreate(savedInstanceState);
////            setContentView(R.layout.activity_registration);
////
////            etEmail = findViewById(R.id.etEmail);
////            etPassword = findViewById(R.id.etPassword);
////            etRepeatPassword = findViewById(R.id.etRepeatPassword);
////            etUsername = findViewById(R.id.etUsername);
////            btnRegister = findViewById(R.id.btnRegister);
////
////            userService = new UserService(this);
////
////            // avatar dugmici
////            findViewById(R.id.btnAvatar1).setOnClickListener(v -> selectedAvatar = "avatar1.png");
////            findViewById(R.id.btnAvatar2).setOnClickListener(v -> selectedAvatar = "avatar2.png");
////            findViewById(R.id.btnAvatar3).setOnClickListener(v -> selectedAvatar = "avatar3.png");
////            findViewById(R.id.btnAvatar4).setOnClickListener(v -> selectedAvatar = "avatar4.png");
////            findViewById(R.id.btnAvatar5).setOnClickListener(v -> selectedAvatar = "avatar5.png");
////
////            btnRegister.setOnClickListener(v -> {
////                String email = etEmail.getText().toString();
////                String password = etPassword.getText().toString();
////                String repeatPassword = etRepeatPassword.getText().toString();
////                String username = etUsername.getText().toString();
////
////                try {
////                    userService.registerUser(email, password, repeatPassword, username, selectedAvatar,
////                            (OnCompleteListener<AuthResult>) task -> {
////                                if (task.isSuccessful()) {
////                                    Toast.makeText(this, "Registracija uspešna! Proverite email za aktivaciju.", Toast.LENGTH_LONG).show();
////                                } else {
////                                    Toast.makeText(this, "Greška: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
////                                }
////                            });
////                } catch (IllegalArgumentException e) {
////                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
////                }
////            });
////        }
////    }
////
////}