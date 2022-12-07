package com.example.geslock;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.geslock.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get app preferences
        SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);

        // set saved theme
        AppCompatDelegate.setDefaultNightMode(pref.getInt("theme", MODE_NIGHT_FOLLOW_SYSTEM));

        // set saved language
        int languageIndex = pref.getInt("language", 2);
        Locale language;
        switch (languageIndex) {
            case 0:
                language = Locale.ENGLISH;
                break;
            case 1:
                language = Locale.CHINESE;
                break;
            default:
                language = Locale.getDefault();
        }
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(language);
        resources.updateConfiguration(configuration, displayMetrics);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolBar;
        setSupportActionBar(toolbar);

        BottomNavigationView nav = binding.navView;
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(nav, navController);
        nav.setItemIconTintList(null);
    }
}