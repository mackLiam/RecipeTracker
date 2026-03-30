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
    private ChipGroup chipGroupPrepTime;
    private BottomNavigationView bottomNav;
    private RecipeAdapter adapter;

    // Which category chip is selected, null means "All"
    private String currentCategory = null;
    // Which prep time chip is selected, 0 means no filter
    private int currentMaxMins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);

        Toolbar toolbar     = findViewById(R.id.toolbar);
        recyclerView        = findViewById(R.id.recycler_discover);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
        chipGroupPrepTime   = findViewById(R.id.chip_group_prep_time);
        bottomNav           = findViewById(R.id.bottom_navigation);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.discover_title);

        // --- UPDATED: Set up the RecyclerView with the RecipeAdapter ---
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this); // 'this' refers to the OnRecipeClickListener implementation below
        recyclerView.setAdapter(adapter);

        loadRecipes();

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = null;
            } else {
                Chip chip = findViewById(checkedIds.get(0));
                String label = chip.getText().toString();
                // "All" chip means no category filter
                if (label.equalsIgnoreCase("All")) {
                    currentCategory = null;
                } else {
                    currentCategory = label;
                }
            }
            loadRecipes();
        });

        chipGroupPrepTime.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentMaxMins = 0;
            } else {
                int chipId = checkedIds.get(0);
                if (chipId == R.id.chip_time_10) {
                    currentMaxMins = 10;
                } else if (chipId == R.id.chip_time_20) {
                    currentMaxMins = 20;
                } else if (chipId == R.id.chip_time_30) {
                    currentMaxMins = 30;
                } else {
                    currentMaxMins = 0;
                }
            }
            loadRecipes();
        });

        setupBottomNav();
    }

    private void loadRecipes() {
        RecipeDatabase db = RecipeDatabase.getInstance(this);

        // No filters — show random recipes
        if (currentCategory == null && currentMaxMins == 0) {
            db.recipeDao().getRandomRecipes().observe(this, recipes -> {
                adapter.setRecipes(recipes);
            });

        // Only prep time filter is active
        } else if (currentCategory == null) {
            db.recipeDao().getAllWithMaxPrepTime(currentMaxMins).observe(this, recipes -> {
                adapter.setRecipes(recipes);
            });

        // Only category filter is active
        } else if (currentMaxMins == 0) {
            db.recipeDao().getByCategory(currentCategory).observe(this, recipes -> {
                adapter.setRecipes(recipes);
            });

        // Both filters are active
        } else {
            db.recipeDao().getByCategoryAndMaxPrepTime(currentCategory, currentMaxMins).observe(this, recipes -> {
                adapter.setRecipes(recipes);
            });
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
