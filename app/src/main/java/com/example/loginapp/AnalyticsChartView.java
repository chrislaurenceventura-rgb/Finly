package com.example.loginapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalyticsChartView extends View {

    private List<SavingsAnalytics.ChartDataPoint> dataPoints = new ArrayList<>();
    private Paint barPaint;
    private Paint barHighlightPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint valueTextPaint;
    private Paint tooltipPaint;
    private Paint tooltipTextPaint;
    private Paint emptyPaint;

    private int selectedIndex = -1;
    private RectF[] barRects = new RectF[0];

    public AnalyticsChartView(Context context) {
        super(context);
        init(context);
    }

    public AnalyticsChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AnalyticsChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int greenColor = ContextCompat.getColor(context, R.color.finly_green);
        int blueColor = ContextCompat.getColor(context, R.color.finly_blue);

        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(greenColor);

        barHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barHighlightPaint.setColor(Color.parseColor("#66BB6A"));

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(2f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#757575"));
        textPaint.setTextSize(28f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        valueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valueTextPaint.setColor(blueColor);
        valueTextPaint.setTextSize(26f);
        valueTextPaint.setTextAlign(Paint.Align.CENTER);
        valueTextPaint.setFakeBoldText(true);

        tooltipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipPaint.setColor(blueColor);

        tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tooltipTextPaint.setColor(Color.WHITE);
        tooltipTextPaint.setTextSize(28f);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setFakeBoldText(true);

        emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emptyPaint.setColor(Color.GRAY);
        emptyPaint.setTextSize(32f);
        emptyPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(List<SavingsAnalytics.ChartDataPoint> dataPoints) {
        this.dataPoints = dataPoints != null ? dataPoints : new ArrayList<>();
        this.selectedIndex = -1;
        this.barRects = new RectF[this.dataPoints.size()];
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (dataPoints == null || dataPoints.isEmpty()) {
            canvas.drawText("No savings activity yet", width / 2f, height / 2f, emptyPaint);
            return;
        }

        float paddingLeft = 40f;
        float paddingRight = 40f;
        float paddingTop = 60f;
        float paddingBottom = 70f;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        double maxVal = 0;
        for (SavingsAnalytics.ChartDataPoint dp : dataPoints) {
            if (dp.getValue() > maxVal) {
                maxVal = dp.getValue();
            }
        }
        if (maxVal == 0) maxVal = 1;

        // Baseline
        float baselineY = height - paddingBottom;
        canvas.drawLine(paddingLeft, baselineY, width - paddingRight, baselineY, gridPaint);

        int count = dataPoints.size();
        float stepX = chartWidth / count;
        float barWidth = Math.min(stepX * 0.5f, 60f);

        if (barRects.length != count) {
            barRects = new RectF[count];
        }

        for (int i = 0; i < count; i++) {
            SavingsAnalytics.ChartDataPoint dp = dataPoints.get(i);
            float centerX = paddingLeft + (i + 0.5f) * stepX;
            float barLeft = centerX - barWidth / 2f;
            float barRight = centerX + barWidth / 2f;

            float barHeightRatio = (float) (dp.getValue() / maxVal);
            float barTop = baselineY - (chartHeight * barHeightRatio);

            if (barHeightRatio > 0 && (baselineY - barTop) < 12f) {
                barTop = baselineY - 12f;
            }

            RectF barRect = new RectF(barLeft, barTop, barRight, baselineY);
            barRects[i] = barRect;

            Paint currentBarPaint = (i == selectedIndex) ? barHighlightPaint : barPaint;
            canvas.drawRoundRect(barRect, 12f, 12f, currentBarPaint);

            canvas.drawText(dp.getLabel(), centerX, height - 20f, textPaint);

            if (dp.getValue() > 0) {
                String valStr = formatValueShort(dp.getValue());
                canvas.drawText(valStr, centerX, barTop - 12f, valueTextPaint);
            }
        }

        if (selectedIndex >= 0 && selectedIndex < dataPoints.size()) {
            SavingsAnalytics.ChartDataPoint dp = dataPoints.get(selectedIndex);
            RectF rect = barRects[selectedIndex];

            String tooltipText = String.format(Locale.US, "₱ %.2f", dp.getValue());
            float textWidth = tooltipTextPaint.measureText(tooltipText);
            float tooltipW = textWidth + 32f;
            float tooltipH = 50f;

            float tooltipX = rect.centerX();
            float tooltipY = rect.top - 60f;

            if (tooltipY < 30f) tooltipY = rect.top + 70f;

            RectF tooltipRect = new RectF(
                    tooltipX - tooltipW / 2f,
                    tooltipY - tooltipH / 2f,
                    tooltipX + tooltipW / 2f,
                    tooltipY + tooltipH / 2f
            );

            canvas.drawRoundRect(tooltipRect, 16f, 16f, tooltipPaint);
            canvas.drawText(tooltipText, tooltipX, tooltipY + 10f, tooltipTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            for (int i = 0; i < barRects.length; i++) {
                if (barRects[i] != null && barRects[i].contains(x, y)) {
                    selectedIndex = i;
                    performClick();
                    invalidate();
                    return true;
                }
            }

            selectedIndex = -1;
            performClick();
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private String formatValueShort(double value) {
        if (value >= 1000) {
            return String.format(Locale.US, "₱%.1fk", value / 1000.0);
        }
        return String.format(Locale.US, "₱%.0f", value);
    }
}
