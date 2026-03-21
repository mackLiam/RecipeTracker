package com.example.recipetracker;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.recipetracker.database.Recipe;
import com.example.recipetracker.database.RecipeDatabase;
import com.example.recipetracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * AddEditRecipeActivity — Add a new recipe OR edit an existing one.

 * To open for ADDING a new recipe:
 *   startActivity(new Intent(this, AddEditRecipeActivity.class));
 *
 * To open for EDITING an existing recipe:
 *   Intent intent = new Intent(this, AddEditRecipeActivity.class);
 *   intent.putExtra(AddEditRecipeActivity.EXTRA_RECIPE_ID, recipe.id);
 *   startActivity(intent);
 *
 * What to build here:
 *  - Form fields: title, ingredients, steps, category, prep time
 *  - Save button — validates fields then inserts/updates DB
 *  - Delete button — only shown when editing
 *  - Pre-fills fields when editing
 */
public class AddEditRecipeActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private TextInputEditText etTitle, etIngredients, etSteps, etPrepTime;
    private Spinner spinnerCategory;
    private MaterialButton btnSave, btnDelete;

    private RecipeDatabase db;
    private SessionManager session;

    /** null = adding new recipe, non-null = editing existing */
    private Recipe recipeToEdit = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recipe);

        db      = RecipeDatabase.getInstance(this);
        session = new SessionManager(this);

        // Wire up views
        Toolbar toolbar = findViewById(R.id.toolbar);
        etTitle         = findViewById(R.id.et_title);
        etIngredients   = findViewById(R.id.et_ingredients);
        etSteps         = findViewById(R.id.et_steps);
        spinnerCategory = findViewById(R.id.spinner_category);
        etPrepTime      = findViewById(R.id.et_prep_time);
        btnSave         = findViewById(R.id.btn_save);
        btnDelete       = findViewById(R.id.btn_delete);

        // Setup category spinner with options
        String[] categories = {"Breakfast", "Lunch", "Dinner"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Check if we're editing
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId != -1) {
            // Edit mode: load recipe and pre-fill fields
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.edit_recipe_title);
            btnDelete.setVisibility(android.view.View.VISIBLE);

            new Thread(() -> {
                recipeToEdit = db.recipeDao().getById(recipeId);
                runOnUiThread(this::prefillFields);
            }).start();
        } else {
            // Add mode
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.add_recipe_title);
            btnDelete.setVisibility(android.view.View.GONE);
        }

        btnSave.setOnClickListener(v -> saveRecipe());
        btnDelete.setOnClickListener(v -> deleteRecipe());
    }

    /** Pre-fill all form fields when editing */
    private void prefillFields() {
        if (recipeToEdit == null) return;
        etTitle.setText(recipeToEdit.title);
        etIngredients.setText(recipeToEdit.ingredients);
        etSteps.setText(recipeToEdit.steps);
        etPrepTime.setText(recipeToEdit.prepTime);
        
        // Set spinner selection to the saved category
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
        if (adapter != null && recipeToEdit.category != null) {
            int position = adapter.getPosition(recipeToEdit.category);
            spinnerCategory.setSelection(position >= 0 ? position : 0);
        }
    }

    /** Validate → save to DB → close */
    private void saveRecipe() {
        String title       = getText(etTitle);
        String ingredients = getText(etIngredients);
        String steps       = getText(etSteps);
        String category    = (String) spinnerCategory.getSelectedItem();
        String prepTime    = getText(etPrepTime);

        // Basic validation
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ingredients.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_ingredients, Toast.LENGTH_SHORT).show();
            return;
        }
        if (steps.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_steps, Toast.LENGTH_SHORT).show();
            return;
        }
        if (prepTime.isEmpty()) {
            Toast.makeText(this, "Please enter a prep time in minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            if (recipeToEdit == null) {
                // INSERT new
                Recipe newRecipe = new Recipe(
                        title, ingredients, steps,
                        category, prepTime, session.getUsername()
                );
                db.recipeDao().insert(newRecipe);
            } else {
                // UPDATE existing
                recipeToEdit.title       = title;
                recipeToEdit.ingredients = ingredients;
                recipeToEdit.steps       = steps;
                recipeToEdit.category    = category;
                recipeToEdit.prepTime    = prepTime;
                db.recipeDao().update(recipeToEdit);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.saved_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    /** Delete recipe and go back */
    private void deleteRecipe() {
        if (recipeToEdit == null) return;
        new Thread(() -> {
            db.recipeDao().delete(recipeToEdit);
            runOnUiThread(() -> {
                Toast.makeText(this, R.string.deleted_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    /** Helper — safely get trimmed text from an EditText */
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
