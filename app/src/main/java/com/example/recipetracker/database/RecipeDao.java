package com.example.recipetracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * RecipeDao — Data Access Object for Recipe CRUD operations
 */
@Dao
public interface RecipeDao {

    /**
     * Insert a new recipe
     */
    @Insert
    void insert(Recipe recipe);

    /**
     * Update an existing recipe
     */
    @Update
    void update(Recipe recipe);

    /**
     * Delete a recipe
     */
    @Delete
    void delete(Recipe recipe);

    /**
     * Get recipe by ID
     */
    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    Recipe getById(int id);

    /**
     * Get all recipes as LiveData
     */
    @Query("SELECT * FROM recipes ORDER BY created_at DESC")
    LiveData<List<Recipe>> getAllRecipes();

    /**
     * Get recipes by category
     */
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY created_at DESC")
    LiveData<List<Recipe>> getByCategory(String category);

    /**
     * Search recipes by title, category, or ingredients
     */
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' " +
            "OR category LIKE '%' || :query || '%' " +
            "OR ingredients LIKE '%' || :query || '%' " +
            "ORDER BY created_at DESC")
    LiveData<List<Recipe>> searchRecipes(String query);

    /**
     * Get all favourite recipes
     */
    @Query("SELECT * FROM recipes WHERE is_favourite = 1 ORDER BY created_at DESC")
    LiveData<List<Recipe>> getFavouriteRecipes();

    /**
     * Alternative method name for getFavouriteRecipes
     */
    @Query("SELECT * FROM recipes WHERE is_favourite = 1 ORDER BY created_at DESC")
    LiveData<List<Recipe>> getFavourites();

    /**
     * Get random recipes for discovery
     */
    @Query("SELECT * FROM recipes ORDER BY RANDOM() LIMIT 10")
    LiveData<List<Recipe>> getRandomRecipes();

    /**
     * Toggle favourite status
     */
    @Query("UPDATE recipes SET is_favourite = :isFavourite WHERE id = :recipeId")
    void setFavourite(int recipeId, boolean isFavourite);

    /**
     * Delete all recipes
     */
    @Query("DELETE FROM recipes")
    void deleteAll();

    @Query("SELECT * FROM recipes WHERE created_by = :username ORDER BY created_at DESC")
    LiveData<List<Recipe>> getRecipesByUser(String username);
}
