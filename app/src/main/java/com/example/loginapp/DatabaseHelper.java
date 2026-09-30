package com.example.loginapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Savingsly.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";

    // Plans Table
    public static final String TABLE_PLANS = "plans";
    public static final String COLUMN_PLAN_ID = "id";
    public static final String COLUMN_PLAN_USER_ID = "user_id";
    public static final String COLUMN_PLAN_NAME = "name";
    public static final String COLUMN_PLAN_TARGET = "target_amount";
    public static final String COLUMN_PLAN_START_DATE = "start_date";
    public static final String COLUMN_PLAN_END_DATE = "end_date";
    public static final String COLUMN_PLAN_FREQUENCY = "frequency";
    public static final String COLUMN_PLAN_ALLOWANCE = "allowance_amount";
    public static final String COLUMN_PLAN_PRIORITY = "priority";
    public static final String COLUMN_PLAN_NOTES = "notes";

    // Savings Table
    public static final String TABLE_SAVINGS = "savings";
    public static final String COLUMN_SAVING_ID = "id";
    public static final String COLUMN_SAVING_PLAN_ID = "plan_id";
    public static final String COLUMN_SAVING_AMOUNT = "amount";
    public static final String COLUMN_SAVING_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_NAME + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
                + COLUMN_USER_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_PLANS_TABLE = "CREATE TABLE " + TABLE_PLANS + "("
                + COLUMN_PLAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PLAN_USER_ID + " INTEGER,"
                + COLUMN_PLAN_NAME + " TEXT,"
                + COLUMN_PLAN_TARGET + " REAL,"
                + COLUMN_PLAN_START_DATE + " TEXT,"
                + COLUMN_PLAN_END_DATE + " TEXT,"
                + COLUMN_PLAN_FREQUENCY + " TEXT,"
                + COLUMN_PLAN_ALLOWANCE + " REAL,"
                + COLUMN_PLAN_PRIORITY + " TEXT,"
                + COLUMN_PLAN_NOTES + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_PLAN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "))";
        db.execSQL(CREATE_PLANS_TABLE);

        String CREATE_SAVINGS_TABLE = "CREATE TABLE " + TABLE_SAVINGS + "("
                + COLUMN_SAVING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SAVING_PLAN_ID + " INTEGER,"
                + COLUMN_SAVING_AMOUNT + " REAL,"
                + COLUMN_SAVING_DATE + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_SAVING_PLAN_ID + ") REFERENCES " + TABLE_PLANS + "(" + COLUMN_PLAN_ID + "))";
        db.execSQL(CREATE_SAVINGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public int getUserId(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    public String getUserName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_NAME};
        String selection = COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {userId + ""};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public long addPlan(int userId, String name, double target, String startDate, String endDate, String frequency, double allowance, String priority, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAN_USER_ID, userId);
        values.put(COLUMN_PLAN_NAME, name);
        values.put(COLUMN_PLAN_TARGET, target);
        values.put(COLUMN_PLAN_START_DATE, startDate);
        values.put(COLUMN_PLAN_END_DATE, endDate);
        values.put(COLUMN_PLAN_FREQUENCY, frequency);
        values.put(COLUMN_PLAN_ALLOWANCE, allowance);
        values.put(COLUMN_PLAN_PRIORITY, priority);
        values.put(COLUMN_PLAN_NOTES, notes);

        return db.insert(TABLE_PLANS, null, values);
    }

    public Cursor getPlans(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PLANS, null, COLUMN_PLAN_USER_ID + " = ?", new String[]{String.valueOf(userId)}, null, null, null);
    }

    public long addSaving(int planId, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SAVING_PLAN_ID, planId);
        values.put(COLUMN_SAVING_AMOUNT, amount);
        values.put(COLUMN_SAVING_DATE, date);

        return db.insert(TABLE_SAVINGS, null, values);
    }

    public double getTotalSavedForPlan(int planId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SAVING_AMOUNT + ") FROM " + TABLE_SAVINGS + " WHERE " + COLUMN_SAVING_PLAN_ID + " = ?", new String[]{String.valueOf(planId)});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public int calculateStreak(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Get all unique saving dates for this user, ordered by date descending
        String query = "SELECT DISTINCT " + COLUMN_SAVING_DATE + " FROM " + TABLE_SAVINGS + " WHERE " + COLUMN_SAVING_PLAN_ID + " IN (SELECT " + COLUMN_PLAN_ID + " FROM " + TABLE_PLANS + " WHERE " + COLUMN_PLAN_USER_ID + " = ?)" + " ORDER BY " + COLUMN_SAVING_DATE + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        if (cursor == null) {
            return 0;
        }

        if (!cursor.moveToFirst()) {
            cursor.close();
            return 0;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar cal = Calendar.getInstance();

        // Get Today's date string
        String todayStr = sdf.format(cal.getTime());

        // Get Yesterday's date string
        cal.add(Calendar.DATE, -1);
        String yesterdayStr = sdf.format(cal.getTime());

        int streak = 0;
        String firstDate = cursor.getString(0);

        // Streak is active if the last entry was today or yesterday
        if (firstDate.equals(todayStr) || firstDate.equals(yesterdayStr)) {
            streak = 1;
            String currentDateStr = firstDate;

            while (cursor.moveToNext()) {
                String prevDateStr = cursor.getString(0);
                try {
                    Date current = sdf.parse(currentDateStr);
                    Date prev = sdf.parse(prevDateStr);

                    long diff = current.getTime() - prev.getTime();
                    long diffDays = diff / (24 * 60 * 60 * 1000);

                    if (diffDays == 1) {
                        streak++;
                        currentDateStr = prevDateStr;
                    } else {
                        break; // Gap found
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }

        cursor.close();
        return streak;
    }

    public boolean deletePlan(int planId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SAVINGS, COLUMN_SAVING_PLAN_ID + " = ?", new String[]{String.valueOf(planId)});
        int rows = db.delete(TABLE_PLANS, COLUMN_PLAN_ID + " = ?", new String[]{String.valueOf(planId)});
        return rows > 0;
    }

    public SavingPlan getPlanById(int planId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANS, null, COLUMN_PLAN_ID + " = ?", new String[]{String.valueOf(planId)}, null, null, null);

        SavingPlan plan = null;
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAN_ID));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_NAME));
            double target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLAN_TARGET));
            String start = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_START_DATE));
            String end = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_END_DATE));
            String freq = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_FREQUENCY));
            double allowance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLAN_ALLOWANCE));
            String priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_PRIORITY));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_NOTES));

            plan = new SavingPlan(id, userId, name, target, start, end, freq, allowance, priority, notes);
            cursor.close();
        }
        return plan;
    }

    public java.util.List<SavingPlan> getAllPlans() {
        java.util.List<SavingPlan> plans = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAN_ID));
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PLAN_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_NAME));
                double target = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLAN_TARGET));
                String start = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_START_DATE));
                String end = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_END_DATE));
                String freq = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_FREQUENCY));
                double allowance = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PLAN_ALLOWANCE));
                String priority = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_PRIORITY));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PLAN_NOTES));

                plans.add(new SavingPlan(id, userId, name, target, start, end, freq, allowance, priority, notes));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return plans;
    }

    public java.util.List<SavingsAnalytics.ChartDataPoint> getMonthlySavingsForUser(int userId) {
        java.util.List<SavingsAnalytics.ChartDataPoint> points = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT substr(" + COLUMN_SAVING_DATE + ", 1, 7) AS month_period, SUM(" + COLUMN_SAVING_AMOUNT + ") AS total " +
                "FROM " + TABLE_SAVINGS + " WHERE " + COLUMN_SAVING_PLAN_ID + " IN (SELECT " + COLUMN_PLAN_ID + " FROM " + TABLE_PLANS + " WHERE " + COLUMN_PLAN_USER_ID + " = ?) " +
                "GROUP BY month_period ORDER BY month_period ASC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String period = cursor.getString(0);
                double total = cursor.getDouble(1);

                String displayLabel = period;
                if (period != null && period.length() >= 7) {
                    try {
                        Date d = new SimpleDateFormat("yyyy-MM", Locale.US).parse(period);
                        if (d != null) {
                            displayLabel = new SimpleDateFormat("MMM", Locale.US).format(d);
                        }
                    } catch (Exception ignored) {}
                }

                points.add(new SavingsAnalytics.ChartDataPoint(displayLabel, total));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return points;
    }

    public java.util.List<SavingsAnalytics.ChartDataPoint> getPlanBreakdownForUser(int userId) {
        java.util.List<SavingsAnalytics.ChartDataPoint> points = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT p." + COLUMN_PLAN_NAME + ", COALESCE(SUM(s." + COLUMN_SAVING_AMOUNT + "), 0) AS total " +
                "FROM " + TABLE_PLANS + " p LEFT JOIN " + TABLE_SAVINGS + " s ON p." + COLUMN_PLAN_ID + " = s." + COLUMN_SAVING_PLAN_ID + " " +
                "WHERE p." + COLUMN_PLAN_USER_ID + " = ? GROUP BY p." + COLUMN_PLAN_ID;

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                double total = cursor.getDouble(1);

                if (name != null && name.length() > 8) {
                    name = name.substring(0, 7) + "..";
                }

                points.add(new SavingsAnalytics.ChartDataPoint(name != null ? name : "Plan", total));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return points;
    }
}
