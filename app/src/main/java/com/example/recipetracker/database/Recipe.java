package com.example.recipetracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Recipe — the central data model for the whole app.
 *
 * Do NOT add or rename fields without telling everyone,
 * because all screens depend on this class.
 *
 * If you add a field, also add it to RecipeDao and run the app
 * once so Room rebuilds the database.
 */
@Entity(tableName = "recipes")
public class Recipe {

    @PrimaryKey(autoGenerate = true)
    public int id;

    /** Display title shown on cards and detail screen */
    public String title;

    /** Comma-separated or newline-separated list of ingredients */
    public String ingredients;

    /** Newline-separated cooking steps */
    public String steps;

    /** e.g. "Breakfast", "Dinner", "Dessert" */
    public String category;

    /** e.g. "30 mins" — free text, just display it */
    public String prepTime;

    /**
     * Username of whoever added this recipe.
     * Used to decide if the Edit button should be visible.
     * Set this to the logged-in username when saving.
     */
    public String addedByUser;

    /** true = show in Favourites screen */
    public boolean isFavourite;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public Recipe() { }

    public Recipe(String title, String ingredients, String steps,
                  String category, String prepTime, String addedByUser) {
        this.title        = title;
        this.ingredients  = ingredients;
        this.steps        = steps;
        this.category     = category;
        this.prepTime     = prepTime;
        this.addedByUser  = addedByUser;
        this.isFavourite  = false;
    }
}
