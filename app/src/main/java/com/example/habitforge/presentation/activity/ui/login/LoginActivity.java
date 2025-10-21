//package com.example.habitforge.presentation.activity.ui.login;
//
//import android.app.Activity;
//
//import androidx.lifecycle.Observer;
//import androidx.lifecycle.ViewModelProvider;
//
//import android.os.Bundle;
//
//import androidx.annotation.Nullable;
//import androidx.annotation.StringRes;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.inputmethod.EditorInfo;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.example.habitforge.R;
//import com.example.habitforge.application.service.UserService;
//import com.example.habitforge.presentation.activity.ui.login.LoginViewModel;
//import com.example.habitforge.presentation.activity.ui.login.LoginViewModelFactory;
//import com.example.habitforge.databinding.ActivityLoginBinding;
//
//public class LoginActivity extends AppCompatActivity {
//
//    private LoginViewModel loginViewModel;
//    private ActivityLoginBinding binding;
//    private UserService userService;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        binding = ActivityLoginBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        userService = new UserService(this);
//
//        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
//                .get(LoginViewModel.class);
//
//        final EditText usernameEditText = binding.username;
//        final EditText passwordEditText = binding.password;
//        final Button loginButton = binding.login;
//        final ProgressBar loadingProgressBar = binding.loading;
//
//        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
//            @Override
//            public void onChanged(@Nullable LoginFormState loginFormState) {
//                if (loginFormState == null) {
//                    return;
//                }
//                loginButton.setEnabled(loginFormState.isDataValid());
//                if (loginFormState.getUsernameError() != null) {
//                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
//                }
//                if (loginFormState.getPasswordError() != null) {
//                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
//                }
//            }
//        });
//
//        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
//            @Override
//            public void onChanged(@Nullable LoginResult loginResult) {
//                if (loginResult == null) {
//                    return;
//                }
//                loadingProgressBar.setVisibility(View.GONE);
//                if (loginResult.getError() != null) {
//                    showLoginFailed(loginResult.getError());
//                }
//                if (loginResult.getSuccess() != null) {
//                    updateUiWithUser(loginResult.getSuccess());
//                }
//                setResult(Activity.RESULT_OK);
//
//                //Complete and destroy login activity once successful
//                finish();
//            }
//        });
//
//        TextWatcher afterTextChangedListener = new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                // ignore
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // ignore
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
//                        passwordEditText.getText().toString());
//            }
//        };
//        usernameEditText.addTextChangedListener(afterTextChangedListener);
//        passwordEditText.addTextChangedListener(afterTextChangedListener);
//        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    loginViewModel.login(usernameEditText.getText().toString(),
//                            passwordEditText.getText().toString());
//                }
//                return false;
//            }
//        });
//
//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                loadingProgressBar.setVisibility(View.VISIBLE);
//                loginViewModel.login(usernameEditText.getText().toString(),
//                        passwordEditText.getText().toString());
//            }
//        });
//    }
//
//    private void updateUiWithUser(LoggedInUserView model) {
//        String welcome = getString(R.string.welcome) + model.getDisplayName();
//        // TODO : initiate successful logged in experience
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
//    }
//
//    private void showLoginFailed(@StringRes Integer errorString) {
//        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
//    }
//}

package com.example.habitforge.presentation.activity.ui.login;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitforge.R;
import com.example.habitforge.application.service.UserService;
import com.example.habitforge.application.session.SessionManager;
import com.example.habitforge.data.repository.UserRepository;
import com.example.habitforge.databinding.ActivityLoginBinding;
import com.example.habitforge.presentation.activity.AddTaskActivity;
import com.example.habitforge.presentation.activity.NavigationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private UserService userService;
   private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userService = new UserService(this);
        userRepository = new UserRepository(this);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        // Praćenje validnosti forme
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) return;
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null)
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                if (loginFormState.getPasswordError() != null)
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        // Praćenje rezultata login-a
        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult == null) return;
                if (loginResult.getError() != null) showLoginFailed(loginResult.getError());
                if (loginResult.getSuccess() != null) updateUiWithUser(loginResult.getSuccess());
            }
        });

        // TextWatcher za dinamičku validaciju
        
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
            }
            return false;
        });

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    // --- Metoda za login preko Firebase i update aktivacije ---
    private void attemptLogin() {
        String email = binding.username.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        binding.loading.setVisibility(View.VISIBLE);
//        userService.loginUserAndSavePlayerId(email, password, new UserRepository.GenericCallback() {
//            @Override
//            public void onComplete(boolean success) {
//                runOnUiThread(() -> { // UI thread za loading i početni toast
//                    binding.loading.setVisibility(View.GONE);
//
//                    if (success) {
//                        if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
//                            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//                            userService.activateUser(userId, activateTask -> {
//                                runOnUiThread(() -> { // UI thread za Toast i navigaciju
//                                    if (activateTask.isSuccessful()) {
//                                        Toast.makeText(LoginActivity.this, "Prijava uspešna!", Toast.LENGTH_LONG).show();
//                                        SessionManager session = new SessionManager(LoginActivity.this);
//                                        session.saveSession(FirebaseAuth.getInstance().getCurrentUser());
//                                        Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    } else {
//                                        Toast.makeText(LoginActivity.this, "Greška pri aktivaciji naloga!", Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                            });
//
//                        } else {
//                            Toast.makeText(LoginActivity.this, "Email nije verifikovan!", Toast.LENGTH_LONG).show();
//                        }
//                    } else {
//                        Toast.makeText(LoginActivity.this, "Pogrešan email ili lozinka!", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        });



        userService.loginUser(email, password, task -> {
            binding.loading.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                // Provera da li je email verifikovan
                if (task.getResult().getUser().isEmailVerified()) {
                    // Update Firestore isActive = true
                    userService.activateUser(task.getResult().getUser().getUid(), activateTask -> {
                        if (activateTask.isSuccessful()) {
                            Toast.makeText(this, "Prijava uspešna!", Toast.LENGTH_LONG).show();
                            SessionManager session = new SessionManager(LoginActivity.this);
                            session.saveSession(task.getResult().getUser());
                            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                            startActivity(intent);

                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(task1 -> {
                                        if (!task1.isSuccessful()) {
                                            Log.w("FCM", "Fetching FCM token failed", task.getException());
                                            return;
                                        }

                                        // Dobijeni token
                                        String token = task1.getResult();
                                        Log.d("FCM", "FCM Token: " + token);

                                        // Sačuvaj token u Firestore i u User objekt
                                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        userRepository.updateUserFcmToken(currentUserId, token);
                                    });





                            finish(); // idi dalje u aplikaciju
                        } else {
                            Toast.makeText(this, "Greška pri aktivaciji naloga!", Toast.LENGTH_LONG).show();
                        }
                    });

                } else {
                    Toast.makeText(this, "Email nije verifikovan!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Pogrešan email ili lozinka!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
