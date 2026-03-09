package com.example.recipetracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetracker.database.RecipeDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;

/**
 * DiscoverActivity — browse and filter all recipes by category
 *
 * What to build here:
 *  - Horizontal ChipGroup for category filters (All, Breakfast, Dinner…)
 *  - RecyclerView list of recipes (reuse RecipeAdapter)
 *  - Clicking a chip filters the list
 *  - Tapping a card opens RecipeDetailActivity
 *  - Bottom navigation
 */
public class DiscoverActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChipGroup chipGroupCategories;
    private BottomNavigationView bottomNav;

    // TODO: private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        Toolbar toolbar     = findViewById(R.id.toolbar);
        recyclerView        = findViewById(R.id.recycler_discover);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
        bottomNav           = findViewById(R.id.bottom_navigation);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.discover_title);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: set up adapter

        // Default: load random/all recipes
        RecipeDatabase.getInstance(this)
                .recipeDao()
                .getRandomRecipes()
                .observe(this, recipes -> {
                    // TODO: adapter.setRecipes(recipes);
                });

        // Filter by category when a chip is selected
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // TODO: get label of selected chip and call dao.getByCategory(label)
            // If "All" chip selected, load all recipes instead
        });

        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_discover);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_discover) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
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
