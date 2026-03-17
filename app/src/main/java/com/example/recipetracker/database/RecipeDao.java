package com.example.recipetracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * RecipeDao — all database queries live here.
 *
 * If you need a new query for your screen, add it here.
 * Don't write raw SQL elsewhere in the app — keep it all in this file.
 *
 * LiveData means the UI automatically updates when the DB changes.
 * You observe it in your Activity like:
 *
 *   db.recipeDao().getAllRecipes().observe(this, recipes -> {
 *       // update your list here
 *   });
 */
@Dao
public interface RecipeDao {

    // -------------------------------------------------------
    // INSERT / UPDATE / DELETE
    // -------------------------------------------------------

    @Insert
    void insert(Recipe recipe);

    @Update
    void update(Recipe recipe);

    @Delete
    void delete(Recipe recipe);

    // -------------------------------------------------------
    // SELECT — used by Home screen
    // -------------------------------------------------------

    /** All recipes, newest first */
    @Query("SELECT * FROM recipes ORDER BY id DESC")
    LiveData<List<Recipe>> getAllRecipes();

    /** Search by title or category (used by search bar) */
    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' " +
           "OR category LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<Recipe>> searchRecipes(String query);

    /** Filter by category (used by filter chips) */
    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY id DESC")
    LiveData<List<Recipe>> getByCategory(String category);

    // -------------------------------------------------------
    // SELECT — used by Favourites screen
    // -------------------------------------------------------

    @Query("SELECT * FROM recipes WHERE isFavourite = 1 ORDER BY title ASC")
    LiveData<List<Recipe>> getFavourites();

    // -------------------------------------------------------
    // SELECT — used by Detail screen
    // -------------------------------------------------------

    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    Recipe getById(int id);

    // -------------------------------------------------------
    // SELECT — used by Discover screen
    // -------------------------------------------------------

    /** Random sample of recipes for the Discover page */
    @Query("SELECT * FROM recipes ORDER BY RANDOM() LIMIT 10")
    LiveData<List<Recipe>> getRandomRecipes();
}
