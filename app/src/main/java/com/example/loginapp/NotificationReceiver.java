package com.example.loginapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_TYPE = "extra_notification_type";
    public static final String EXTRA_PLAN_ID = "extra_plan_id";
    public static final String EXTRA_PLAN_NAME = "extra_plan_name";

    public static final String TYPE_DAILY = "type_daily";
    public static final String TYPE_DEADLINE = "type_deadline";
    public static final String TYPE_UPCOMING_DEADLINE = "type_upcoming_deadline";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String type = intent.getStringExtra(EXTRA_TYPE);
        int planId = intent.getIntExtra(EXTRA_PLAN_ID, -1);
        String planName = intent.getStringExtra(EXTRA_PLAN_NAME);

        if (planId == -1 || planName == null || planName.isEmpty()) {
            return;
        }

        String title = "";
        String message = "";
        int notificationId = 0;

        if (type != null) {
            switch (type) {
                case TYPE_DAILY:
                    title = "Savings Reminder";
                    message = "Don't forget to add to your " + planName + " savings today.";
                    notificationId = planId * 1000 + 1;
                    // Reschedule next day's alarm for repeating daily reminder
                    NotificationScheduler.scheduleDailyReminder(context, planId, planName);
                    break;

                case TYPE_DEADLINE:
                    title = "Savings Plan Deadline";
                    message = "Today is the deadline for your " + planName + " savings plan.";
                    notificationId = planId * 1000 + 2;
                    break;

                case TYPE_UPCOMING_DEADLINE:
                    title = "Deadline Tomorrow";
                    message = "Your " + planName + " savings plan is due tomorrow.";
                    notificationId = planId * 1000 + 3;
                    break;
            }
        }

        if (!title.isEmpty()) {
            NotificationHelper.showNotification(context, title, message, notificationId, planId);
        }
    }
}
