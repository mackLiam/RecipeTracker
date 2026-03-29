package com.example.recipetracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipetracker.database.Recipe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * RecipeAdapter — Adapter for displaying recipes in RecyclerView
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_card, parent, false);
        return new RecipeViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Update the list of recipes
     */
    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes != null ? newRecipes : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for recipe items
     */
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        private TextView tvTitle, tvCategory, tvPrepTime, tvCreatedBy;
        private ImageView ivRecipeImage;
        private OnRecipeClickListener listener;

        public RecipeViewHolder(@NonNull View itemView, OnRecipeClickListener listener) {
            super(itemView);
            this.listener = listener;
            tvTitle = itemView.findViewById(R.id.tv_card_title);
            tvCategory = itemView.findViewById(R.id.tv_card_category);
            tvPrepTime = itemView.findViewById(R.id.tv_card_prep_time);
            tvCreatedBy = itemView.findViewById(R.id.tv_card_created_by);
            ivRecipeImage = itemView.findViewById(R.id.iv_recipe_image);
        }

        public void bind(Recipe recipe) {
            tvTitle.setText(recipe.title);
            tvCategory.setText(recipe.category);
            tvPrepTime.setText(recipe.prepTime);

            // Show creator label
            if ("System".equals(recipe.createdBy)) {
                tvCreatedBy.setText("System Recipe");
            } else if (recipe.createdBy != null && !recipe.createdBy.isEmpty()) {
                tvCreatedBy.setText("By " + recipe.createdBy);
            } else {
                tvCreatedBy.setText("");
            }

            Context context = itemView.getContext();

            // Check if we have an image URL, file path, or drawable name
            if (recipe.imageUrl != null && !recipe.imageUrl.isEmpty()) {
                if (recipe.imageUrl.startsWith("http")) {
                    // It's a web URL
                    Glide.with(context)
                            .load(recipe.imageUrl)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivRecipeImage);
                } else if (recipe.imageUrl.startsWith("/")) {
                    // It's a file path to a user-selected photo
                    Glide.with(context)
                            .load(new File(recipe.imageUrl))
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivRecipeImage);
                } else {
                    // It's a local drawable name (e.g., "avocado_toast")
                    int imageResId = context.getResources().getIdentifier(
                            recipe.imageUrl, "drawable", context.getPackageName());

                    if (imageResId != 0) {
                        ivRecipeImage.setImageResource(imageResId);
                    } else {
                        ivRecipeImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                }
            } else {
                ivRecipeImage.setImageResource(R.drawable.ic_launcher_background);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRecipeClick(recipe);
                }
            });
        }
    }
}
