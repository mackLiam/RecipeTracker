package com.example.recipetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetracker.database.RecipeDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * FavouritesActivity — shows recipes the user has hearted
 *
 * What to build here:
 *  - RecyclerView list of favourited recipes (reuse RecipeAdapter)
 *  - Empty state message when list is empty
 *  - Tapping a card opens RecipeDetailActivity
 *  - Bottom navigation (same as MainActivity)
 */
public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private BottomNavigationView bottomNav;

    // TODO: private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        recyclerView    = findViewById(R.id.recycler_favourites);
        tvEmpty         = findViewById(R.id.tv_empty);
        bottomNav       = findViewById(R.id.bottom_navigation);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Favourites");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // TODO: set up adapter

        // Observe favourites from DB
        RecipeDatabase.getInstance(this)
                .recipeDao()
                .getFavourites()
                .observe(this, recipes -> {
                    if (recipes == null || recipes.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        // TODO: adapter.setRecipes(recipes);
                    }
                });

        setupBottomNav();
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_favourites);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_favourites) {
                return true;
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_discover) {
                startActivity(new Intent(this, DiscoverActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
