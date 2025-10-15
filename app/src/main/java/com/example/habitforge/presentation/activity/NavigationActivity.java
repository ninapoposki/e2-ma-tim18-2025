//
//package com.example.habitforge.presentation.activity;
//
package com.example.habitforge.presentation.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.habitforge.R;
import com.example.habitforge.application.session.SessionManager;
import com.example.habitforge.databinding.NavigationActivityBinding;
import com.example.habitforge.presentation.activity.ui.login.LoginActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class NavigationActivity extends AppCompatActivity {

    private NavigationActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = NavigationActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Postavi Toolbar
        setSupportActionBar(binding.appBarNavigation.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Postavi hamburger dugme (ActionBarDrawerToggle)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                binding.appBarNavigation.toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState(); // ovo je bitno da se prikaže hamburger ikona

        // Klik listener za meni
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                // Otvori ProfileActivity
               // startActivity(new Intent(NavigationActivity.this, ProfileActivity.class));
                drawer.closeDrawers();
                return true;
            } else if (id == R.id.nav_home) {
                // Home logika ili fragment
                drawer.closeDrawers();
                return true;
            }
            return false;
        });


        Button logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Firebase logout
            SessionManager session = new SessionManager(this);
            session.clearSession(); // Lokalno obriši

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Fab dugme
        binding.appBarNavigation.fab.setOnClickListener(view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }



}


//import android.content.Intent;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.appcompat.app.ActionBarDrawerToggle;
//
//import com.example.habitforge.R;
//import com.example.habitforge.databinding.NavigationActivityBinding;
//import com.google.android.material.navigation.NavigationView;
//import com.google.android.material.snackbar.Snackbar;
//
//public class NavigationActivity extends AppCompatActivity {
//
//    private NavigationActivityBinding binding;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = NavigationActivityBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.appBarNavigation.toolbar);
//
//        binding.appBarNavigation.fab.setOnClickListener(view ->
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .setAnchorView(R.id.fab).show()
//        );
//
//        DrawerLayout drawer = binding.drawerLayout;
//        NavigationView navigationView = binding.navView;
//
//        // Klik listener za meni
//        navigationView.setNavigationItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_profile) {
//                // Otvori ProfileActivity
//                Intent intent = new Intent(NavigationActivity.this, ProfileActivity.class);
//                startActivity(intent);
//                drawer.closeDrawers();
//                return true;
//            } else if (id == R.id.nav_home) {
//                // Home logika ili fragment
//                drawer.closeDrawers();
//                return true;
//            }
//            return false;
//        });
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.navigation, menu);
//        return true;
//    }
//}
//
////package com.example.habitforge.presentation.activity;
//
//import android.os.Bundle;
//import android.view.View;
//import android.view.Menu;
//
//import com.google.android.material.snackbar.Snackbar;
//import com.google.android.material.navigation.NavigationView;
//
//import androidx.navigation.NavController;
//import androidx.navigation.Navigation;
//import androidx.navigation.ui.AppBarConfiguration;
//import androidx.navigation.ui.NavigationUI;
//import androidx.drawerlayout.widget.DrawerLayout;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.habitforge.databinding.NavigationActivityBinding;
//
//public class NavigationActivity extends AppCompatActivity {
//
//    private AppBarConfiguration mAppBarConfiguration;
//    private NavigationActivityBinding binding;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        binding = NavigationActivityBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        setSupportActionBar(binding.appBarNavigation.toolbar);
//        binding.appBarNavigation.fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null)
//                        .setAnchorView(R.id.fab).show();
//            }
//        });
//        DrawerLayout drawer = binding.drawerLayout;
//        NavigationView navigationView = binding.navView;
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
//                .setOpenableLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_navigation);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
//}