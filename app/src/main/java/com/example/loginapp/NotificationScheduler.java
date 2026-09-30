package com.example.loginapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationScheduler {

    private static final String TAG = "NotificationScheduler";

    public static void schedulePlanNotifications(Context context, SavingPlan plan) {
        if (plan == null) return;

        scheduleDailyReminder(context, plan.getId(), plan.getName());
        scheduleDeadlineReminders(context, plan.getId(), plan.getName(), plan.getEndDate());
    }

    public static void scheduleDailyReminder(Context context, int planId, String planName) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 20); // 8:00 PM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        int requestCode = planId * 1000 + 1;
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_TYPE, NotificationReceiver.TYPE_DAILY);
        intent.putExtra(NotificationReceiver.EXTRA_PLAN_ID, planId);
        intent.putExtra(NotificationReceiver.EXTRA_PLAN_NAME, planName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        setAlarm(context, calendar.getTimeInMillis(), pendingIntent);
    }

    public static void scheduleDeadlineReminders(Context context, int planId, String planName, String endDateStr) {
        if (endDateStr == null || endDateStr.isEmpty()) return;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            Date endDate = sdf.parse(endDateStr);
            if (endDate == null) return;

            // 1. Deadline Reminder (8:00 AM on Deadline date)
            Calendar deadlineCal = Calendar.getInstance();
            deadlineCal.setTime(endDate);
            deadlineCal.set(Calendar.HOUR_OF_DAY, 8); // 8:00 AM
            deadlineCal.set(Calendar.MINUTE, 0);
            deadlineCal.set(Calendar.SECOND, 0);
            deadlineCal.set(Calendar.MILLISECOND, 0);

            if (deadlineCal.getTimeInMillis() > System.currentTimeMillis()) {
                int requestCode = planId * 1000 + 2;
                Intent intent = new Intent(context, NotificationReceiver.class);
                intent.putExtra(NotificationReceiver.EXTRA_TYPE, NotificationReceiver.TYPE_DEADLINE);
                intent.putExtra(NotificationReceiver.EXTRA_PLAN_ID, planId);
                intent.putExtra(NotificationReceiver.EXTRA_PLAN_NAME, planName);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                setAlarm(context, deadlineCal.getTimeInMillis(), pendingIntent);
            }

            // 2. Upcoming Deadline Reminder (8:00 AM 1 day before Deadline date)
            Calendar upcomingCal = (Calendar) deadlineCal.clone();
            upcomingCal.add(Calendar.DAY_OF_YEAR, -1);

            if (upcomingCal.getTimeInMillis() > System.currentTimeMillis()) {
                int requestCode = planId * 1000 + 3;
                Intent intent = new Intent(context, NotificationReceiver.class);
                intent.putExtra(NotificationReceiver.EXTRA_TYPE, NotificationReceiver.TYPE_UPCOMING_DEADLINE);
                intent.putExtra(NotificationReceiver.EXTRA_PLAN_ID, planId);
                intent.putExtra(NotificationReceiver.EXTRA_PLAN_NAME, planName);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                setAlarm(context, upcomingCal.getTimeInMillis(), pendingIntent);
            }

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing end date for plan notifications", e);
        }
    }

    public static void cancelPlanNotifications(Context context, int planId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        int[] offsets = {1, 2, 3};
        for (int offset : offsets) {
            int requestCode = planId * 1000 + offset;
            Intent intent = new Intent(context, NotificationReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
            );

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }

    public static void rescheduleAllNotifications(Context context) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
            List<SavingPlan> plans = dbHelper.getAllPlans();
            for (SavingPlan plan : plans) {
                schedulePlanNotifications(context, plan);
            }
        }
    }

    private static void setAlarm(Context context, long triggerAtMillis, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }
}
