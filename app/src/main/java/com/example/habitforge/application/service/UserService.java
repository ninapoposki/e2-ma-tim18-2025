package com.example.habitforge.application.service;

import android.content.Context;

import com.example.habitforge.application.model.User;
import com.example.habitforge.data.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;

public class UserService {
    private final UserRepository userRepository;


    public UserService(Context context) {
        this.userRepository = new UserRepository(context);
    }

    public void registerUser(String email, String password, String repeatPassword,
                             String username, String avatar,
                             OnCompleteListener<AuthResult> callback) {

        // validacija unosa
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Neispravna email adresa!");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Lozinka mora imati barem 6 karaktera!");
        }
        if (!password.equals(repeatPassword)) {
            throw new IllegalArgumentException("Lozinke se ne poklapaju!");
        }
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Korisničko ime je obavezno!");
        }
        if (avatar == null || avatar.isEmpty()) {
            throw new IllegalArgumentException("Morate izabrati avatara!");
        }

        // kreiraj user objekat
        User newUser = new User(null, email, username, avatar);

        // prosledi repozitorijumu
        userRepository.signUpUser(email, password, newUser, callback);
    }
    public void loginUser(String email, String password, OnCompleteListener<AuthResult> callback) {
        userRepository.login(email, password, callback);
    }

    public boolean isUserActivated() {
        return userRepository.isUserActivated();
    }

    public void activateUser(String userId, OnCompleteListener<Void> listener) {
        userRepository.activateUser(userId, listener);
    }
//koriscenje odece
    public void useClothing(User user, String equipmentId, Runnable onSuccess) {
        userRepository.useClothing(user, equipmentId, onSuccess);
    }
//Nakon aktivacije jednokratnih napitaka, njihovo dejstvo će biti potrošeno u prvoj narednoj borbi sa bosom.
    public void useAllActivePotions(User user, Runnable onSuccess) {
        userRepository.useAllActivePotions(user, onSuccess);
    }

}
