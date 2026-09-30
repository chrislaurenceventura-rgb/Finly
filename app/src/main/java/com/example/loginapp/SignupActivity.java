package com.example.loginapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginapp.databinding.ActivitySignupBinding;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        binding.btnSignup.setOnClickListener(v -> {
            if (binding.etSignupName.getText() == null ||
                binding.etSignupEmail.getText() == null ||
                binding.etSignupPassword.getText() == null) return;

            String name = binding.etSignupName.getText().toString().trim();
            String studentId = binding.etSignupEmail.getText().toString().trim();
            String password = binding.etSignupPassword.getText().toString().trim();

            if (name.isEmpty() || studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignupActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (dbHelper.isEmailExists(studentId)) {
                Toast.makeText(SignupActivity.this, "Student ID already registered", Toast.LENGTH_SHORT).show();
            } else {
                boolean isInserted = dbHelper.addUser(name, studentId, password);
                if (isInserted) {
                    Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.tvLoginLink.setOnClickListener(v -> finish());
    }
}
