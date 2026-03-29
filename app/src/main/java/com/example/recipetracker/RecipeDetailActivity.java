package com.example.recipetracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.recipetracker.database.Recipe;
import com.example.recipetracker.database.RecipeDatabase;
import com.example.recipetracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

/**
 * RecipeDetailActivity — shows full recipe info and handles sharing/favoriting
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private TextView tvTitle, tvCategory, tvPrepTime, tvIngredients, tvSteps;
    private ImageView ivDetailImage;
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
        ivDetailImage     = findViewById(R.id.iv_detail_image);
        fabFavourite      = findViewById(R.id.fab_favourite);
        btnEdit           = findViewById(R.id.btn_edit);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recipe Details");
        }

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
        
        // Show prep time (ensure "mins" isn't doubled in UI)
        String time = currentRecipe.prepTime;
        if (time != null && !time.toLowerCase().contains("min")) {
            time += " mins";
        }
        tvPrepTime.setText(time);
        
        tvIngredients.setText(currentRecipe.ingredients);
        tvSteps.setText(currentRecipe.steps);

        // Load recipe image
        if (currentRecipe.imageUrl != null && !currentRecipe.imageUrl.isEmpty()) {
            ivDetailImage.setVisibility(View.VISIBLE);
            if (currentRecipe.imageUrl.startsWith("http")) {
                Glide.with(this).load(currentRecipe.imageUrl).into(ivDetailImage);
            } else if (currentRecipe.imageUrl.startsWith("/")) {
                Glide.with(this).load(new File(currentRecipe.imageUrl)).into(ivDetailImage);
            } else {
                int resId = getResources().getIdentifier(
                        currentRecipe.imageUrl, "drawable", getPackageName());
                if (resId != 0) {
                    ivDetailImage.setImageResource(resId);
                } else {
                    ivDetailImage.setVisibility(View.GONE);
                }
            }
        } else {
            ivDetailImage.setVisibility(View.GONE);
        }

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
        if (currentUser != null && currentUser.equals(currentRecipe.createdBy)) {
            btnEdit.setVisibility(android.view.View.VISIBLE);
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddEditRecipeActivity.class);
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
            fabFavourite.setImageTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFFD700)); // gold
        } else {
            fabFavourite.setImageResource(android.R.drawable.btn_star_big_off);
            fabFavourite.setImageTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFFFFFF)); // white
        }
    }

    /**
     * Share the recipe details as plain text
     */
    private void shareRecipe() {
        if (currentRecipe == null) return;

        // Clean up prep time for share body
        String time = currentRecipe.prepTime;
        if (time != null && !time.toLowerCase().contains("min")) {
            time += " mins";
        }

        String shareBody = "Check out this recipe: " + currentRecipe.title +
                "\n\nPrep Time: " + time +
                "\n\nIngredients:\n" + currentRecipe.ingredients +
                "\n\nSteps:\n" + currentRecipe.steps;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Recipe: " + currentRecipe.title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        
        startActivity(Intent.createChooser(shareIntent, "Share recipe via"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            shareRecipe();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
