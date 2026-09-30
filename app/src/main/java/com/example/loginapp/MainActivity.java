package com.example.loginapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.loginapp.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private SavingPlansAdapter adapter;
    private List<SavingPlan> planList;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        userId = getSharedPreferences("SavingslyPrefs", MODE_PRIVATE).getInt("userId", -1);
        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.rvPlans.setLayoutManager(new LinearLayoutManager(this));
        planList = new ArrayList<>();
        adapter = new SavingPlansAdapter(planList, dbHelper, plan -> {
            Intent intent = new Intent(MainActivity.this, PlanDetailsActivity.class);
            intent.putExtra("planId", plan.getId());
            intent.putExtra("planName", plan.getName());
            intent.putExtra("planTarget", plan.getTargetAmount());
            intent.putExtra("planFrequency", plan.getFrequency());
            intent.putExtra("planEndDate", plan.getEndDate());
            intent.putExtra("planAllowance", plan.getAllowanceAmount());
            startActivity(intent);
        });
        binding.rvPlans.setAdapter(adapter);

        checkNotificationPermission();
        NotificationScheduler.rescheduleAllNotifications(this);

        setupNotifications();

        setupAnalyticsToggle();

        binding.btnDashboardAddSaving.setOnClickListener(v -> 
            startActivity(new Intent(MainActivity.this, AddPlanActivity.class))
        );

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                binding.cardSavingsOverview.setVisibility(View.VISIBLE);
                binding.layoutActivePlans.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_plans) {
                binding.cardSavingsOverview.setVisibility(View.GONE);
                binding.layoutActivePlans.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_progress) {
                binding.cardSavingsOverview.setVisibility(View.VISIBLE);
                binding.layoutActivePlans.setVisibility(View.GONE);
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(MainActivity.this, AddPlanActivity.class));
                return true;
            } else if (itemId == R.id.nav_more) {
                Toast.makeText(this, "FINLY - Financialfriendly App", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        loadDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) {
            return "Good morning";
        } else if (hour >= 12 && hour < 17) {
            return "Good afternoon";
        } else {
            return "Good evening";
        }
    }

    private void loadDashboard() {
        String userName = dbHelper.getUserName(userId);
        if (userName == null || userName.isEmpty()) {
            userName = "User";
        }
        binding.tvGreeting.setText(String.format(Locale.US, "%s,\n%s! ", getGreeting(), userName));
        binding.tvStudentInfo.setVisibility(View.GONE);

        int streak = dbHelper.calculateStreak(userId);
        binding.tvStreakDays.setText(String.format(Locale.US, "%d %s", streak, streak == 1 ? "Day" : "Days"));

        planList.clear();
        double totalTargetAll = 0;
        double totalSavedAll = 0;

        Cursor cursor = dbHelper.getPlans(userId);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_NAME));
                double target = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_TARGET));
                String start = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_START_DATE));
                String end = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_END_DATE));
                String freq = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_FREQUENCY));
                double allowance = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_ALLOWANCE));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_PRIORITY));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PLAN_NOTES));

                planList.add(new SavingPlan(id, userId, name, target, start, end, freq, allowance, priority, notes));
                totalTargetAll += target;
                totalSavedAll += dbHelper.getTotalSavedForPlan(id);
            } while (cursor.moveToNext());
            cursor.close();
        }

        binding.tvTotalSavings.setText(String.format(Locale.US, "₱ %.2f", totalSavedAll));
        binding.tvTotalTarget.setText(String.format(Locale.US, "₱ %.2f", totalTargetAll));

        double overallPercent = (totalTargetAll > 0) ? (totalSavedAll / totalTargetAll) * 100 : 0;
        binding.tvTotalProgressPercent.setText(String.format(Locale.US, "%.0f%%", overallPercent));
        binding.totalProgressCircular.setProgress((int) overallPercent);
        binding.tvCompletionRate.setText(String.format(Locale.US, "%.0f%%", overallPercent));

        adapter.notifyDataSetChanged();

        updateAnalyticsChart();
    }

    private boolean isMonthlyAnalyticsMode = true;

    private void setupAnalyticsToggle() {
        binding.toggleAnalyticsMode.check(R.id.btn_chart_monthly);
        binding.toggleAnalyticsMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isMonthlyAnalyticsMode = (checkedId == R.id.btn_chart_monthly);
                updateAnalyticsChart();
            }
        });
    }

    private void updateAnalyticsChart() {
        List<SavingsAnalytics.ChartDataPoint> dataPoints;
        if (isMonthlyAnalyticsMode) {
            dataPoints = dbHelper.getMonthlySavingsForUser(userId);
        } else {
            dataPoints = dbHelper.getPlanBreakdownForUser(userId);
        }

        binding.analyticsChartView.setData(dataPoints);

        double sum = 0;
        double highest = 0;
        for (SavingsAnalytics.ChartDataPoint dp : dataPoints) {
            sum += dp.getValue();
            if (dp.getValue() > highest) {
                highest = dp.getValue();
            }
        }
        int count = dataPoints.isEmpty() ? 1 : dataPoints.size();
        double avg = sum / count;

        binding.tvAvgMonthlySavings.setText(String.format(Locale.US, "₱ %.2f", avg));
        binding.tvHighestPeriod.setText(String.format(Locale.US, "₱ %.2f", highest));
    }

    private void setupNotifications() {
        boolean hasNotification = getSharedPreferences("SavingslyPrefs", MODE_PRIVATE).getBoolean("hasNotification", true);
        binding.vNotificationBadge.setVisibility(hasNotification ? View.VISIBLE : View.GONE);

        binding.flNotificationContainer.setOnClickListener(v -> {
            if (binding.vNotificationBadge.getVisibility() == View.VISIBLE) {
                binding.vNotificationBadge.setVisibility(View.GONE);
                getSharedPreferences("SavingslyPrefs", MODE_PRIVATE).edit().putBoolean("hasNotification", false).apply();
                Snackbar.make(binding.getRoot(), "Notification marked as read", Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No new notifications", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNotificationPermission() {
        NotificationHelper.createNotificationChannel(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
}
