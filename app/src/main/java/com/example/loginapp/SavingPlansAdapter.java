package com.example.loginapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;
import java.util.Locale;

public class SavingPlansAdapter extends RecyclerView.Adapter<SavingPlansAdapter.ViewHolder> {

    public interface OnPlanClickListener {
        void onPlanClick(SavingPlan plan);
    }

    private final List<SavingPlan> plans;
    private final DatabaseHelper dbHelper;
    private final OnPlanClickListener listener;

    public SavingPlansAdapter(List<SavingPlan> plans, DatabaseHelper dbHelper, OnPlanClickListener listener) {
        this.plans = plans;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saving_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SavingPlan plan = plans.get(position);
        holder.tvName.setText(plan.getName());

        double totalSaved = dbHelper.getTotalSavedForPlan(plan.getId());
        double percent = (plan.getTargetAmount() > 0) ? (totalSaved / plan.getTargetAmount()) * 100 : 0;

        holder.tvAmounts.setText(String.format(Locale.US, "₱ %.2f / ₱ %.2f", totalSaved, plan.getTargetAmount()));
        holder.progressBar.setProgress((int) percent);
        holder.tvPercent.setText(String.format(Locale.US, "%.0f%%", percent));

        holder.itemView.setOnClickListener(v -> listener.onPlanClick(plan));
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmounts, tvPercent;
        LinearProgressIndicator progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_plan_name);
            tvAmounts = itemView.findViewById(R.id.tv_plan_amounts);
            tvPercent = itemView.findViewById(R.id.tv_plan_percent);
            progressBar = itemView.findViewById(R.id.plan_progress_bar);
        }
    }
}
