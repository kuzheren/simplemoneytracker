package com.kuzheren.simplemoneytracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.kuzheren.simplemoneytracker.data.ExpenseRepository;
import com.kuzheren.simplemoneytracker.util.Categories;
import com.kuzheren.simplemoneytracker.util.DateUtils;
import com.kuzheren.simplemoneytracker.util.MoneyFormat;

import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    public static final String EXTRA_YEAR_MONTH = "year_month";
    public static final String EXTRA_ALL_TIME = "all_time";

    private TextView tvMonth;
    private TextView tvTotal;
    private TextView tvEmpty;
    private LinearLayout container;

    private String displayedYearMonth;
    private boolean allTimeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        tvMonth = findViewById(R.id.tv_month);
        tvTotal = findViewById(R.id.tv_total);
        tvEmpty = findViewById(R.id.tv_empty);
        container = findViewById(R.id.container_categories);
        ImageButton btnPrev = findViewById(R.id.btn_prev_month);
        ImageButton btnNext = findViewById(R.id.btn_next_month);

        toolbar.setNavigationOnClickListener(v -> finish());

        allTimeMode = getIntent().getBooleanExtra(EXTRA_ALL_TIME, false);
        String passed = getIntent().getStringExtra(EXTRA_YEAR_MONTH);
        displayedYearMonth = passed != null ? passed : DateUtils.currentYearMonth();

        btnPrev.setOnClickListener(v -> {
            if (allTimeMode) {
                allTimeMode = false;
                displayedYearMonth = DateUtils.currentYearMonth();
            } else {
                displayedYearMonth = DateUtils.shiftYearMonth(displayedYearMonth, -1);
            }
            refresh();
        });
        btnNext.setOnClickListener(v -> {
            if (allTimeMode) {
                allTimeMode = false;
                displayedYearMonth = DateUtils.currentYearMonth();
            } else {
                displayedYearMonth = DateUtils.shiftYearMonth(displayedYearMonth, 1);
            }
            refresh();
        });
        tvMonth.setOnClickListener(v -> {
            allTimeMode = !allTimeMode;
            refresh();
        });

        refresh();
    }

    private void refresh() {
        ExpenseRepository repo = ExpenseRepository.get(this);

        Map<String, Double> totals;
        double overall;
        if (allTimeMode) {
            tvMonth.setText(R.string.all_time);
            totals = repo.getTotalsByCategoryAllTime();
            overall = repo.allTimeTotal();
        } else {
            tvMonth.setText(DateUtils.formatMonthLabel(displayedYearMonth));
            totals = repo.getTotalsByCategory(displayedYearMonth);
            overall = repo.monthTotal(displayedYearMonth);
        }

        tvTotal.setText("−" + MoneyFormat.format(overall));

        container.removeAllViews();
        if (totals.isEmpty() || overall <= 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        tvEmpty.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            String category = entry.getKey();
            double amount = entry.getValue();
            double share = overall > 0 ? amount / overall : 0;

            View row = inflater.inflate(R.layout.item_category_stat, container, false);
            ImageView ivIcon = row.findViewById(R.id.iv_icon);
            TextView tvCategory = row.findViewById(R.id.tv_category);
            TextView tvAmount = row.findViewById(R.id.tv_amount);
            TextView tvPercent = row.findViewById(R.id.tv_percent);
            ProgressBar pb = row.findViewById(R.id.pb_share);

            ivIcon.setImageResource(Categories.iconFor(category));
            tvCategory.setText(category);
            tvAmount.setText(MoneyFormat.format(amount));
            int percent = (int) Math.round(share * 100);
            tvPercent.setText(percent + "%");
            pb.setProgress((int) Math.round(share * 1000));

            container.addView(row);
        }
    }
}
