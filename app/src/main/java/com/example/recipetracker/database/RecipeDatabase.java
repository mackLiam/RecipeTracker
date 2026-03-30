package com.example.recipetracker.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.Executors;

@Database(entities = {Recipe.class}, version = 1, exportSchema = false)
public abstract class RecipeDatabase extends RoomDatabase {

    public abstract RecipeDao recipeDao();

    private static volatile RecipeDatabase INSTANCE;

    public static RecipeDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RecipeDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    RecipeDatabase.class,
                                    "recipe_database"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Executors.newSingleThreadExecutor().execute(() -> {
                RecipeDao dao = INSTANCE.recipeDao();

                // --- UPDATE: Put the URL here for each recipe ---
                
                Recipe recipe1 = new Recipe(
                        "Avocado Toast",
                        "2 slices Sourdough Bread, 1 ripe Avocado, Red pepper flakes, Lemon juice, Salt",
                        "1. Toast the bread. 2. Mash avocado with a squeeze of lemon and salt. 3. Spread on toast. 4. Sprinkle with flakes.",
                        "Breakfast",
                        "5 mins",
                        "System"
                );
                // PASTE THE URL HERE:
                recipe1.imageUrl = "avocado_toast";
                recipe1.difficulty = "Easy";

                Recipe recipe2 = new Recipe(
                        "Classic Beef Tacos",
                        "500g Ground Beef, 8 Taco shells, 1 tbsp Taco seasoning, Shredded Lettuce, Cheddar cheese",
                        "1. Brown the beef in a pan. 2. Stir in seasoning and a splash of water. 3. Warm the shells. 4. Assemble with toppings.",
                        "Dinner",
                        "15 mins",
                        "System"
                );
                recipe2.imageUrl = "beef_tacos";
                recipe2.difficulty = "Easy";

                Recipe recipe3 = new Recipe(
                        "Chicken Quesadilla",
                        "2 Flour Tortillas, 150g Cooked Chicken (shredded), 100g Monterey Jack cheese, 1 tbsp Butter",
                        "1. Place cheese and chicken on one tortilla. 2. Top with the second tortilla. 3. Grill in a buttered pan until golden on both sides.",
                        "Lunch",
                        "10 mins",
                        "System"
                );
                recipe3.imageUrl = "chicken_quesadilla";
                recipe3.difficulty = "Easy";

                Recipe recipe4 = new Recipe(
                        "Garlic Butter Shrimp",
                        "400g Shrimp (peeled), 4 cloves Garlic, 50g Butter, Lemon juice, Parsley",
                        "1. Melt butter in a skillet. 2. Sauté minced garlic until fragrant. 3. Add shrimp and cook until pink. 4. Add lemon and parsley.",
                        "Dinner",
                        "12 mins",
                        "System"
                );
                recipe4.imageUrl = "garlic_butter_shrimp";
                recipe4.difficulty = "Medium";

                Recipe recipe5 = new Recipe(
                        "Vegetable Stir-Fry",
                        "1 Broccoli head, 2 Carrots, 1 Bell pepper, 3 tbsp Soy sauce, 1 tsp Ginger",
                        "1. Chop vegetables. 2. Sauté ginger in a hot wok. 3. Add vegetables and stir-fry until tender-crisp. 4. Pour in soy sauce and toss.",
                        "Lunch",
                        "15 mins",
                        "System"
                );
                recipe5.imageUrl = "vegetable_stir_fry";
                recipe5.difficulty = "Medium";

                Recipe recipe6 = new Recipe(
                        "Chocolate Mug Cake",
                        "4 tbsp Flour, 2 tbsp Cocoa powder, 2 tbsp Sugar, 3 tbsp Milk, 1 tbsp Oil",
                        "1. Whisk dry ingredients in a mug. 2. Stir in milk and oil until smooth. 3. Microwave on high for 90 seconds. 4. Let cool slightly.",
                        "Dessert",
                        "3 mins",
                        "System"
                );
                recipe6.imageUrl = "chocolate_mug_cake";
                recipe6.difficulty = "Easy";

                Recipe recipe7 = new Recipe(
                        "Caprese Skewers",
                        "12 Cherry tomatoes, 12 Mini mozzarella balls, Fresh Basil leaves, Balsamic glaze",
                        "1. Thread a tomato, a basil leaf, and a mozzarella ball onto a toothpick. 2. Repeat for all. 3. Drizzle with balsamic glaze.",
                        "Snack",
                        "10 mins",
                        "System"
                );
                recipe7.imageUrl = "caprese_skewers";
                recipe7.difficulty = "Easy";

                Recipe recipe8 = new Recipe(
                        "Banana Pancakes",
                        "1 ripe Banana, 2 Eggs, 1/4 tsp Cinnamon, Butter for frying",
                        "1. Mash banana in a bowl. 2. Whisk in eggs and cinnamon until well combined. 3. Heat butter in a pan. 4. Pour small circles and flip when golden.",
                        "Breakfast",
                        "10 mins",
                        "System"
                );
                recipe8.imageUrl = "banana_pancakes";
                recipe8.difficulty = "Medium";

                dao.insert(recipe1);
                dao.insert(recipe2);
                dao.insert(recipe3);
                dao.insert(recipe4);
                dao.insert(recipe5);
                dao.insert(recipe6);
                dao.insert(recipe7);
                dao.insert(recipe8);
            });
        }
    };
}
