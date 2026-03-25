package com.example.recipetracker;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetracker.database.Recipe;
import com.example.recipetracker.database.RecipeDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * DiscoverActivity — browse and filter all recipes by category
 */
public class DiscoverActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    private RecyclerView recyclerView;
    private ChipGroup chipGroupCategories;
    private BottomNavigationView bottomNav;
    private RecipeAdapter adapter;

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

        // --- UPDATED: Set up the RecyclerView with the RecipeAdapter ---
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this); // 'this' refers to the OnRecipeClickListener implementation below
        recyclerView.setAdapter(adapter);

        // --- UPDATED: Initial load (shows random recipes when screen first opens) ---
        loadRecipes(null);

        // --- UPDATED: Listen for category filter changes ---
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                // If no chip is selected, default back to random recipes
                loadRecipes(null);
            } else {
                // Get the text from the selected chip (e.g., "Breakfast", "Lunch")
                int chipId = checkedIds.get(0);
                Chip chip = findViewById(chipId);
                String category = chip.getText().toString();

                if (category.equalsIgnoreCase("All")) {
                    loadRecipes(null); // Load random if "All" is selected
                } else {
                    loadRecipes(category); // Filter by the specific category
                }
            }
        });

        setupBottomNav();
    }

    /**
     * Helper method to fetch recipes from the database.
     * @param category If null, loads random recipes. Otherwise, filters by category.
     */
    private void loadRecipes(String category) {
        if (category == null) {
            // --- UPDATED: Fetch random recipes using LiveData ---
            RecipeDatabase.getInstance(this)
                    .recipeDao()
                    .getRandomRecipes()
                    .observe(this, recipes -> adapter.setRecipes(recipes));
        } else {
            // --- UPDATED: Fetch recipes specifically for the chosen category ---
            RecipeDatabase.getInstance(this)
                    .recipeDao()
                    .getByCategory(category)
                    .observe(this, recipes -> adapter.setRecipes(recipes));
        }
    }

    /**
     * --- UPDATED: Implementation of OnRecipeClickListener ---
     * This runs when a user taps a recipe card in the list.
     */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
        startActivity(intent);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_discover);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_discover) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_favourites) {
                startActivity(new Intent(this, FavouritesActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
