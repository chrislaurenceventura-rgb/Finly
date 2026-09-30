package com.example.loginapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.loginapp.databinding.ActivityPlanDetailsBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlanDetailsActivity extends AppCompatActivity {

    private ActivityPlanDetailsBinding binding;
    private DatabaseHelper dbHelper;
    private int planId;
    private String planName, planFrequency, planEndDate;
    private double planTarget, planAllowance;

    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlanDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        planId = getIntent().getIntExtra("planId", -1);
        planName = getIntent().getStringExtra("planName");
        planTarget = getIntent().getDoubleExtra("planTarget", 0);
        planFrequency = getIntent().getStringExtra("planFrequency");
        planEndDate = getIntent().getStringExtra("planEndDate");
        planAllowance = getIntent().getDoubleExtra("planAllowance", 0);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.menu_plan_details);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_plan) {
                confirmDeletePlan();
                return true;
            }
            return false;
        });

        updateUI();

        binding.btnAddSaving.setOnClickListener(v -> showAddSavingDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCountdown();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCountdown();
    }

    private void startCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    private void stopCountdown() {
        if (countdownRunnable != null) {
            countdownHandler.removeCallbacks(countdownRunnable);
        }
    }

    private void updateUI() {
        binding.tvDetailName.setText(planName);
        binding.tvDetailTarget.setText(String.format(Locale.US, "Target: ₱ %.2f", planTarget));

        double totalSaved = dbHelper.getTotalSavedForPlan(planId);
        double percent = (planTarget > 0) ? (totalSaved / planTarget) * 100 : 0;

        binding.tvDetailPercent.setText(String.format(Locale.US, "%.0f%%", percent));
        binding.circularProgress.setProgress((int) percent);

        calculateGenerator(totalSaved);
    }

    private void calculateGenerator(double totalSaved) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date endDate = sdf.parse(planEndDate);
            Date today = new Date();

            long diffInMs = endDate.getTime() - today.getTime();

            if (diffInMs <= 0) {
                binding.tvTimeLeft.setText(getString(R.string.goal_reached));
                binding.tvSuggestedAmount.setText(getString(R.string.plan_expired));
                return;
            }

            long days = TimeUnit.MILLISECONDS.toDays(diffInMs);
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMs) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMs) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMs) % 60;

            String countdownText = String.format(Locale.US, "%dd %02dh %02dm %02ds left", days, hours, minutes, seconds);
            binding.tvTimeLeft.setText(countdownText);

            double remainingTarget = planTarget - totalSaved;
            if (remainingTarget <= 0) {
                binding.tvSuggestedAmount.setText("Target achieved! Well done.");
                return;
            }

            String unit;
            double divisor;

            switch (planFrequency) {
                case "Weekly":
                    unit = "week";
                    divisor = 7.0;
                    break;
                case "Monthly":
                    unit = "month";
                    divisor = 30.44;
                    break;
                default:
                    unit = "day";
                    divisor = 1.0;
                    break;
            }

            double intervalsLeft = diffInMs / (divisor * 24 * 60 * 60 * 1000.0);
            if (intervalsLeft < 1) intervalsLeft = 1;

            double timeSuggested = remainingTarget / intervalsLeft;

            if (planAllowance > 0) {
                double multiplier;
                double allowanceDivisor;

                if ("Daily".equals(planFrequency)) {
                    multiplier = 0.10;
                    allowanceDivisor = 7.0;
                } else if ("Monthly".equals(planFrequency)) {
                    multiplier = 0.30;
                    allowanceDivisor = 1.0 / 4.34;
                } else {
                    multiplier = 0.20;
                    allowanceDivisor = 1.0;
                }

                double allowanceSuggested = (planAllowance / allowanceDivisor) * multiplier;
                double suggested = Math.max(allowanceSuggested, timeSuggested);

                if (timeSuggested > allowanceSuggested) {
                    binding.tvSuggestedAmount.setText(String.format(Locale.US, "Suggested: ₱ %.2f / %s (Required for Date)", suggested, unit));
                } else {
                    binding.tvSuggestedAmount.setText(String.format(Locale.US, "Suggested: ₱ %.2f / %s", suggested, unit));
                }
            } else {
                binding.tvSuggestedAmount.setText(String.format(Locale.US, "Suggested: ₱ %.2f / %s", timeSuggested, unit));
            }

        } catch (ParseException e) {
            Log.e("PlanDetailsActivity", "Date parse error", e);
        }
    }

    private void showAddSavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Savings");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter amount");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String amountStr = input.getText().toString();
            if (!amountStr.isEmpty()) {
                double amount = Double.parseDouble(amountStr);
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

                dbHelper.addSaving(planId, amount, date);
                updateUI();
                Toast.makeText(this, "Savings added!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void confirmDeletePlan() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Plan")
                .setMessage("Are you sure you want to delete '" + planName + "'? All savings entries for this plan will also be removed.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    NotificationScheduler.cancelPlanNotifications(this, planId);
                    boolean deleted = dbHelper.deletePlan(planId);
                    if (deleted) {
                        Toast.makeText(this, "Plan deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete plan", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
