package com.example.loginapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginapp.databinding.ActivityAddPlanBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddPlanActivity extends AppCompatActivity {

    private ActivityAddPlanBinding binding;
    private DatabaseHelper dbHelper;
    private Calendar calendarStart, calendarEnd;
    private int userId;
    private String frequency = "Weekly";
    private String priority = "Medium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        userId = getSharedPreferences("SavingslyPrefs", MODE_PRIVATE).getInt("userId", -1);

        calendarStart = Calendar.getInstance();
        calendarEnd = Calendar.getInstance();

        // Initial values
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        binding.etStartDate.setText(sdf.format(calendarStart.getTime()));
        binding.tgFrequency.check(R.id.btn_weekly);
        binding.tgPriority.check(R.id.btn_medium);

        setupListeners();
        updatePreview();
    }

    private void setupListeners() {
        binding.etEndDate.setOnClickListener(v -> showDatePicker(false));
        binding.etStartDate.setOnClickListener(v -> showDatePicker(true));

        binding.tgFrequency.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_daily) frequency = "Daily";
                else if (checkedId == R.id.btn_weekly) frequency = "Weekly";
                else if (checkedId == R.id.btn_monthly) frequency = "Monthly";
                updatePreview();
            }
        });

        binding.tgPriority.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_low) priority = "Low";
                else if (checkedId == R.id.btn_medium) priority = "Medium";
                else if (checkedId == R.id.btn_high) priority = "High";
            }
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updatePreview();
            }
        };

        binding.etTargetAmount.addTextChangedListener(watcher);
        binding.etCurrentSavings.addTextChangedListener(watcher);
        binding.etAllowance.addTextChangedListener(watcher);
        binding.etEndDate.addTextChangedListener(watcher);

        binding.btnCreatePlan.setOnClickListener(v -> savePlan());
        binding.toolbar.setOnClickListener(v -> finish());
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? calendarStart : calendarEnd;
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            if (isStart) binding.etStartDate.setText(sdf.format(cal.getTime()));
            else binding.etEndDate.setText(sdf.format(cal.getTime()));
            updatePreview();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updatePreview() {
        if (binding.etTargetAmount.getText() == null ||
            binding.etCurrentSavings.getText() == null ||
            binding.etEndDate.getText() == null ||
            binding.etAllowance.getText() == null) return;

        String targetStr = binding.etTargetAmount.getText().toString();
        String currentStr = binding.etCurrentSavings.getText().toString();
        String endDateStr = binding.etEndDate.getText().toString();
        String allowanceStr = binding.etAllowance.getText().toString();

        double target = targetStr.isEmpty() ? 0 : Double.parseDouble(targetStr);
        double current = currentStr.isEmpty() ? 0 : Double.parseDouble(currentStr);
        double allowance = allowanceStr.isEmpty() ? 0 : Double.parseDouble(allowanceStr);

        double remaining = target - current;
        binding.tvPreviewTarget.setText(String.format(Locale.US, "₱ %.0f", target));
        binding.tvPreviewCurrent.setText(String.format(Locale.US, "₱ %.0f", current));
        binding.tvPreviewRemaining.setText(String.format(Locale.US, "₱ %.0f", Math.max(0, remaining)));

        double timeSuggested = 0;
        if (!endDateStr.isEmpty() && remaining > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                Date end = sdf.parse(endDateStr);
                Date start = calendarStart.getTime();
                long diff = end.getTime() - start.getTime();
                long days = TimeUnit.MILLISECONDS.toDays(diff);

                double divisor;
                if ("Daily".equals(frequency)) divisor = 1.0;
                else if ("Monthly".equals(frequency)) divisor = 30.44;
                else divisor = 7.0;

                double intervals = days / divisor;
                if (intervals < 1) intervals = 1;
                timeSuggested = remaining / intervals;
            } catch (ParseException e) {
                Log.e("AddPlanActivity", "Date parse error", e);
            }
        }

        if (allowance > 0) {
            double multiplier;
            double allowanceDivisor;
            if ("Daily".equals(frequency)) {
                multiplier = 0.10;
                allowanceDivisor = 7.0;
            } else if ("Monthly".equals(frequency)) {
                multiplier = 0.30;
                allowanceDivisor = 1.0 / 4.34;
            } else {
                multiplier = 0.20;
                allowanceDivisor = 1.0;
            }
            double allowanceSuggested = (allowance / allowanceDivisor) * multiplier;
            double suggested = Math.max(allowanceSuggested, timeSuggested);

            if (timeSuggested > allowanceSuggested) {
                binding.tvSuggestedLabel.setText(String.format(Locale.US, "%s Savings (Required for Date)", frequency));
            } else {
                binding.tvSuggestedLabel.setText(String.format(Locale.US, "%s Savings", frequency));
            }
            binding.tvSuggestedAmount.setText(String.format(Locale.US, "₱ %.2f", suggested));
        } else {
            binding.tvSuggestedAmount.setText(String.format(Locale.US, "₱ %.2f", timeSuggested));
            binding.tvSuggestedLabel.setText(timeSuggested > 0 ? String.format(Locale.US, "Time-based %s Savings", frequency) : "Enter allowance or date");
        }
    }

    private void savePlan() {
        String name = binding.etPlanName.getText().toString().trim();
        String targetStr = binding.etTargetAmount.getText().toString().trim();
        String currentStr = binding.etCurrentSavings.getText().toString().trim();
        String allowanceStr = binding.etAllowance.getText().toString().trim();
        String endDate = binding.etEndDate.getText().toString().trim();
        String startDate = binding.etStartDate.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();

        if (name.isEmpty() || targetStr.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double target = Double.parseDouble(targetStr);
        double current = currentStr.isEmpty() ? 0 : Double.parseDouble(currentStr);
        double allowance = allowanceStr.isEmpty() ? 0 : Double.parseDouble(allowanceStr);

        long id = dbHelper.addPlan(userId, name, target, startDate, endDate, frequency, allowance, priority, notes);

        if (id != -1) {
            if (current > 0) {
                dbHelper.addSaving((int) id, current, startDate);
            }
            SavingPlan createdPlan = new SavingPlan((int) id, userId, name, target, startDate, endDate, frequency, allowance, priority, notes);
            NotificationScheduler.schedulePlanNotifications(this, createdPlan);
            Toast.makeText(this, "Plan created successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error creating plan", Toast.LENGTH_SHORT).show();
        }
    }
}
