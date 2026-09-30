package com.example.loginapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.loginapp.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        binding.btnLogin.setOnClickListener(v -> {
            if (binding.etStudentId.getText() == null || binding.etLoginPassword.getText() == null) return;
            String studentId = binding.etStudentId.getText().toString().trim();
            String password = binding.etLoginPassword.getText().toString().trim();

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                int userId = dbHelper.getUserId(studentId, password);
                if (userId != -1) {
                    if (binding.cbRemember.isChecked()) {
                        getSharedPreferences("SavingslyPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("userId", userId)
                                .putBoolean("rememberMe", true)
                                .apply();
                    } else {
                        getSharedPreferences("SavingslyPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("userId", userId)
                                .apply();
                    }
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.tvSignupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}
