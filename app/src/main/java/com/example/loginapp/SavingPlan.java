package com.example.loginapp;

public class SavingPlan {
    private int id;
    private int userId;
    private String name;
    private double targetAmount;
    private String startDate;
    private String endDate;
    private String frequency;
    private double allowanceAmount;
    private String priority;
    private String notes;

    public SavingPlan(int id, int userId, String name, double targetAmount, String startDate, String endDate, String frequency, double allowanceAmount, String priority, String notes) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.frequency = frequency;
        this.allowanceAmount = allowanceAmount;
        this.priority = priority;
        this.notes = notes;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public double getTargetAmount() { return targetAmount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getFrequency() { return frequency; }
    public double getAllowanceAmount() { return allowanceAmount; }
    public String getPriority() { return priority; }
    public String getNotes() { return notes; }
}
