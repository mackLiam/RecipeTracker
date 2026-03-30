package com.example.recipetracker;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetracker.database.RecipeDatabase;
import com.example.recipetracker.util.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

/**
 * MainActivity — Home Screen
 *
 * What to build here:
 *  - RecyclerView grid of recipe cards
 *  - Search bar that filters results live
 *  - FAB to go to AddEditRecipeActivity
 *  - Bottom navigation (same on every screen)
 *  - Dynamic greeting based on time of day
 *  - Empty state when no recipes exist
 */
public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private TextInputEditText searchBar;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNav;
    private LinearLayout emptyStateContainer;
    private TextView greetingText;
    private RecipeAdapter adapter;

    // Keeps track of which sort the user picked
    private String currentSort = "newest";
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire up views
        recyclerView = findViewById(R.id.recycler_recipes);
        searchBar = findViewById(R.id.edit_search);
        fabAdd = findViewById(R.id.fab_add_recipe);
        bottomNav = findViewById(R.id.bottom_navigation);
        emptyStateContainer = findViewById(R.id.empty_state_container);
        greetingText = findViewById(R.id.tv_greeting);

        TextView btnLogout = findViewById(R.id.btn_logout);        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Set dynamic greeting based on time of day
        setDynamicGreeting();

        // Grid: 2 columns
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Set up adapter with click listener
        adapter = new RecipeAdapter(recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Load all recipes from DB and show them
        SessionManager session = new SessionManager(this);
        currentUsername = session.getUsername();

        loadSortedRecipes();

        // Sort button → popup menu
        ImageButton btnSort = findViewById(R.id.btn_sort);
        btnSort.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Newest first");
            popup.getMenu().add("A-Z");
            popup.getMenu().add("Oldest first");
            popup.setOnMenuItemClickListener(item -> {
                String selected = item.getTitle().toString();
                if (selected.equals("Newest first")) {
                    currentSort = "newest";
                } else if (selected.equals("A-Z")) {
                    currentSort = "az";
                } else {
                    currentSort = "oldest";
                }
                loadSortedRecipes();
                return true;
            });
            popup.show();
        });

        // Live search — fires every time the user types
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadSortedRecipes();
                } else {
                    RecipeDatabase.getInstance(MainActivity.this)
                            .recipeDao()
                            .searchRecipes(query)
                            .observe(MainActivity.this, recipes -> {
                                adapter.setRecipes(recipes);
                                updateEmptyState(recipes != null && !recipes.isEmpty());
                            });
                }
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

    private void loadSortedRecipes() {
        RecipeDatabase db = RecipeDatabase.getInstance(this);

        if (currentSort.equals("az")) {
            // Sort alphabetically A-Z
            db.recipeDao().getRecipesByUserAlphabetical(currentUsername).observe(this, recipes -> {
                adapter.setRecipes(recipes);
                updateEmptyState(recipes != null && !recipes.isEmpty());
            });
        } else if (currentSort.equals("oldest")) {
            // Sort by oldest first
            db.recipeDao().getRecipesByUserOldest(currentUsername).observe(this, recipes -> {
                adapter.setRecipes(recipes);
                updateEmptyState(recipes != null && !recipes.isEmpty());
            });
        } else {
            // Default: newest first
            db.recipeDao().getRecipesByUser(currentUsername).observe(this, recipes -> {
                adapter.setRecipes(recipes);
                updateEmptyState(recipes != null && !recipes.isEmpty());
            });
        }
    }

    private void setDynamicGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String timeGreeting;
        if (hour >= 5 && hour < 12) {
            timeGreeting = "Good morning";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "Good afternoon";
        } else {
            timeGreeting = "Good evening";
        }

        SessionManager session = new SessionManager(this);
        greetingText.setText(timeGreeting + ", " + session.getUsername() + "! 👋");
    }

    /**
     * Show/hide empty state based on whether recipes exist
     */
    private void updateEmptyState(boolean hasRecipes) {
        if (hasRecipes) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateContainer.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
        }
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
