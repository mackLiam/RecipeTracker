package com.example.recipetracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetracker.database.RecipeDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * MainActivity — Home Screen
 *
 * What to build here:
 *  - RecyclerView grid of recipe cards
 *  - Search bar that filters results live
 *  - FAB to go to AddEditRecipeActivity
 *  - Bottom navigation (same on every screen)
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;

    // TODO: create a RecipeAdapter class for the RecyclerView
    // private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire up views
        recyclerView = findViewById(R.id.recycler_recipes);
        searchBar    = findViewById(R.id.edit_search);
        fabAdd       = findViewById(R.id.fab_add_recipe);
        bottomNav    = findViewById(R.id.bottom_navigation);

        // Grid: 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // TODO: Set up adapter and attach to recyclerView

        // Load all recipes from DB and show them
        RecipeDatabase.getInstance(this)
                .recipeDao()
                .getAllRecipes()
                .observe(this, recipes -> {
                    // TODO: adapter.setRecipes(recipes);
                });

        // Live search — fires every time the user types
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                RecipeDatabase.getInstance(MainActivity.this)
                        .recipeDao()
                        .searchRecipes(query)
                        .observe(MainActivity.this, recipes -> {
                            // TODO: adapter.setRecipes(recipes);
                        });
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // FAB → Add recipe screen
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRecipeActivity.class);
            startActivity(intent);
        });

        // Bottom nav
        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // already here
            } else if (id == R.id.nav_discover) {
                startActivity(new Intent(this, DiscoverActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_favourites) {
                startActivity(new Intent(this, FavouritesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
