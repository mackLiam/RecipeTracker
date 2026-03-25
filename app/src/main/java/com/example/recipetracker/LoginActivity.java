package com.example.recipetracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipetracker.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * LoginActivity — first screen the user sees.
 * Just asks for a name, saves it via SessionManager, then goes to MainActivity.
 * If the user already has a saved name, skip this screen entirely.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private MaterialButton btnContinue;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);

        // If already logged in, skip straight to home
        if (session.isLoggedIn()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        etName      = findViewById(R.id.et_name);
        btnContinue = findViewById(R.id.btn_continue);

        btnContinue.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name to continue", Toast.LENGTH_SHORT).show();
                return;
            }
            session.createSession(name);
            goToMain();
        });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // so pressing back doesn't return to login
    }
}
