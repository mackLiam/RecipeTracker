package com.example.recipetracker;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.recipetracker.database.Recipe;
import com.example.recipetracker.database.RecipeDatabase;
import com.example.recipetracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * RecipeDetailActivity — shows full recipe info
 *
 * How to launch this from any other screen:
 *   Intent intent = new Intent(this, RecipeDetailActivity.class);
 *   intent.putExtra("recipe_id", recipe.id);
 *   startActivity(intent);
 *
 * What to build here:
 *  - Title, category, prep time
 *  - Ingredients list
 *  - Steps list
 *  - Favourite toggle button (heart icon FAB)
 *  - Edit button — only visible if addedByUser == current user
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private TextView tvTitle, tvCategory, tvPrepTime, tvIngredients, tvSteps;
    private FloatingActionButton fabFavourite;
    private MaterialButton btnEdit;

    private Recipe currentRecipe;
    private RecipeDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        db      = RecipeDatabase.getInstance(this);
        session = new SessionManager(this);

        // Wire up views
        Toolbar toolbar   = findViewById(R.id.toolbar);
        tvTitle           = findViewById(R.id.tv_recipe_title);
        tvCategory        = findViewById(R.id.tv_category);
        tvPrepTime        = findViewById(R.id.tv_prep_time);
        tvIngredients     = findViewById(R.id.tv_ingredients);
        tvSteps           = findViewById(R.id.tv_steps);
        fabFavourite      = findViewById(R.id.fab_favourite);
        btnEdit           = findViewById(R.id.btn_edit);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get recipe ID passed from the previous screen
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId == -1) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load recipe on background thread
        new Thread(() -> {
            currentRecipe = db.recipeDao().getById(recipeId);
            runOnUiThread(this::populateUI);
        }).start();
    }

    private void populateUI() {
        if (currentRecipe == null) return;

        tvTitle.setText(currentRecipe.title);
        tvCategory.setText(currentRecipe.category);
        tvPrepTime.setText(currentRecipe.prepTime);
        tvIngredients.setText(currentRecipe.ingredients);
        tvSteps.setText(currentRecipe.steps);

        // Favourite icon state
        updateFavouriteIcon();

        // Toggle favourite on click
        fabFavourite.setOnClickListener(v -> {
            currentRecipe.isFavourite = !currentRecipe.isFavourite;
            new Thread(() -> {
                db.recipeDao().update(currentRecipe);
                runOnUiThread(this::updateFavouriteIcon);
            }).start();
        });

        // Only show Edit button if this user added the recipe
        String currentUser = session.getUsername();
        if (currentUser.equals(currentRecipe.addedByUser)) {
            btnEdit.setVisibility(android.view.View.VISIBLE);
            btnEdit.setOnClickListener(v -> {
                android.content.Intent intent =
                        new android.content.Intent(this, AddEditRecipeActivity.class);
                intent.putExtra(AddEditRecipeActivity.EXTRA_RECIPE_ID, currentRecipe.id);
                startActivity(intent);
            });
        } else {
            btnEdit.setVisibility(android.view.View.GONE);
        }
    }

    private void updateFavouriteIcon() {
        if (currentRecipe.isFavourite) {
            fabFavourite.setImageResource(android.R.drawable.btn_star_big_on);
            fabFavourite.setColorFilter(
                    android.graphics.Color.parseColor("#FFC107"),
                    android.graphics.PorterDuff.Mode.SRC_IN
            );
        } else {
            fabFavourite.setImageResource(android.R.drawable.btn_star_big_off);
            fabFavourite.clearColorFilter();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
