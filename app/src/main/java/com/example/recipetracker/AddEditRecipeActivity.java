package com.example.recipetracker;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.recipetracker.database.Recipe;
import com.example.recipetracker.database.RecipeDatabase;
import com.example.recipetracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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

    private TextInputEditText etTitle, etIngredients, etSteps, etPrepTime, etAddedBy;
    private Spinner spinnerCategory;
    private MaterialButton btnSave, btnDelete, btnAddPhoto;
    private ImageView ivRecipePhoto;

    private RecipeDatabase db;
    private SessionManager session;

    /** null = adding new recipe, non-null = editing existing */
    private Recipe recipeToEdit = null;

    /** File path of newly selected photo, null if no change */
    private String selectedImagePath = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String path = copyImageToInternalStorage(uri);
                    if (path != null) {
                        selectedImagePath = path;
                        Glide.with(this).load(new File(path)).into(ivRecipePhoto);
                    } else {
                        Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show();
                    }
                }
            });

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
        btnAddPhoto     = findViewById(R.id.btn_add_photo);
        ivRecipePhoto   = findViewById(R.id.iv_recipe_photo);
        etAddedBy       = findViewById(R.id.et_added_by);

        btnAddPhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Setup category spinner with options
        String[] categories = {"Breakfast", "Lunch", "Dinner"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Pre-fill "Added by" with current username (editable)
        etAddedBy.setText(session.getUsername());

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

        // Show the saved creator (overrides the session default set in onCreate)
        if (recipeToEdit.createdBy != null) {
            etAddedBy.setText(recipeToEdit.createdBy);
        }

        // Show existing photo if present
        if (recipeToEdit.imageUrl != null && !recipeToEdit.imageUrl.isEmpty()) {
            File imageFile = new File(recipeToEdit.imageUrl);
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).into(ivRecipePhoto);
            }
        }
    }

    /** Validate → save to DB → close */
    private void saveRecipe() {
        String title       = getText(etTitle);
        String ingredients = getText(etIngredients);
        String steps       = getText(etSteps);
        String category    = (String) spinnerCategory.getSelectedItem();
        String prepTime    = getText(etPrepTime);
        String addedBy     = getText(etAddedBy);

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
                        category, prepTime,
                        addedBy.isEmpty() ? session.getUsername() : addedBy
                );
                if (selectedImagePath != null) {
                    newRecipe.imageUrl = selectedImagePath;
                }
                db.recipeDao().insert(newRecipe);
            } else {
                // UPDATE existing
                recipeToEdit.title       = title;
                recipeToEdit.ingredients = ingredients;
                recipeToEdit.steps       = steps;
                recipeToEdit.category    = category;
                recipeToEdit.prepTime    = prepTime;
                recipeToEdit.createdBy   = addedBy.isEmpty() ? recipeToEdit.createdBy : addedBy;
                if (selectedImagePath != null) {
                    recipeToEdit.imageUrl = selectedImagePath;
                }
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

    /** Copy a content URI image into the app's internal storage and return the file path */
    private String copyImageToInternalStorage(Uri sourceUri) {
        try {
            String filename = "recipe_photo_" + System.currentTimeMillis() + ".jpg";
            File destFile = new File(getFilesDir(), filename);
            try (InputStream in = getContentResolver().openInputStream(sourceUri);
                 OutputStream out = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
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
