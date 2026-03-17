package com.example.recipetracker.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * RecipeDatabase — Room database setup.
 *
 * You never call this class directly.
 * To get the DAO in any Activity, do:
 *
 * RecipeDao dao = RecipeDatabase.getInstance(this).recipeDao();
 *
 * Then run DB operations on a background thread:
 *
 *   new Thread(() -> {
 *       dao.insert(myRecipe);
 *   }).start();
 *
 * For reading, use LiveData + observe() so you stay on the main thread.
 */
@Database(entities = {Recipe.class}, version = 1, exportSchema = false)
public abstract class RecipeDatabase extends RoomDatabase {

    public abstract RecipeDao recipeDao();

    // Singleton — one database instance for the whole app
    private static volatile RecipeDatabase INSTANCE;

    public static RecipeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RecipeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            RecipeDatabase.class,
                            "recipe_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
