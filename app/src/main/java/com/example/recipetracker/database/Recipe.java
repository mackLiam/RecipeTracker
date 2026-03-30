package com.example.recipetracker.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Recipe entity — represents a single recipe in the database
 */
@Entity(tableName = "recipes")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "ingredients")
    public String ingredients;

    @ColumnInfo(name = "steps")
    public String steps;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "prep_time")
    public String prepTime;

    @ColumnInfo(name = "created_by")
    public String createdBy;

    // Alias for compatibility
    public String addedByUser;

    @ColumnInfo(name = "is_favourite")
    public boolean isFavourite;

    @ColumnInfo(name = "image_url")
    public String imageUrl;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "difficulty")
    public String difficulty;

    /**
     * Constructor for creating new recipes
     */
    public Recipe(String title, String ingredients, String steps, String category, String prepTime, String createdBy) {
        this.title = title;
        this.ingredients = ingredients;
        this.steps = steps;
        this.category = category;
        this.prepTime = prepTime;
        this.createdBy = createdBy;
        this.addedByUser = createdBy; // Alias for compatibility
        this.isFavourite = false;
        this.imageUrl = null;
        this.createdAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", prepTime='" + prepTime + '\'' +
                '}';
    }
}
