package com.kuzheren.simplemoneytracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kuzheren.simplemoneytracker.adapter.ExpenseAdapter;
import com.kuzheren.simplemoneytracker.data.ExpenseRepository;
import com.kuzheren.simplemoneytracker.model.Expense;
import com.kuzheren.simplemoneytracker.util.Categories;
import com.kuzheren.simplemoneytracker.util.DateUtils;
import com.kuzheren.simplemoneytracker.util.MoneyFormat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String FILTER_ALL = "__ALL__";

    private TextView tvMonth;
    private TextView tvMonthTotal;
    private TextView tvEmpty;
    private RecyclerView rv;
    private ChipGroup chipGroup;
    private ImageButton btnPrev;
    private ImageButton btnNext;
    private ExpenseAdapter adapter;

    private String currentCategory = FILTER_ALL;
    private String displayedYearMonth;
    private boolean allTimeMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvMonth = findViewById(R.id.tv_month);
        tvMonthTotal = findViewById(R.id.tv_month_total);
        tvEmpty = findViewById(R.id.tv_empty);
        rv = findViewById(R.id.rv_expenses);
        chipGroup = findViewById(R.id.chip_group_categories);
        btnPrev = findViewById(R.id.btn_prev_month);
        btnNext = findViewById(R.id.btn_next_month);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        adapter = new ExpenseAdapter(this::openDetails);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        displayedYearMonth = DateUtils.currentYearMonth();

        setupChips();

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

        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditExpenseActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_statistics) {
            Intent i = new Intent(this, StatisticsActivity.class);
            i.putExtra(StatisticsActivity.EXTRA_YEAR_MONTH, displayedYearMonth);
            i.putExtra(StatisticsActivity.EXTRA_ALL_TIME, allTimeMode);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void setupChips() {
        chipGroup.removeAllViews();
        addChip(getString(R.string.filter_all), FILTER_ALL, true);
        for (String cat : Categories.ALL) {
            addChip(cat, cat, false);
        }
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            Chip selected = group.findViewById(checkedIds.get(0));
            if (selected != null) {
                currentCategory = (String) selected.getTag();
                refresh();
            }
        });
    }

    private void addChip(String text, String tag, boolean checked) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setTag(tag);
        chip.setCheckable(true);
        chip.setChecked(checked);
        chipGroup.addView(chip);
    }

    private void refresh() {
        ExpenseRepository repo = ExpenseRepository.get(this);

        if (allTimeMode) {
            tvMonth.setText(R.string.all_time);
            tvMonthTotal.setText("−" + MoneyFormat.format(repo.allTimeTotal()));
        } else {
            tvMonth.setText(DateUtils.formatMonthLabel(displayedYearMonth));
            tvMonthTotal.setText("−" + MoneyFormat.format(repo.monthTotal(displayedYearMonth)));
        }

        List<Expense> all = repo.getAll();
        List<Expense> filtered = new ArrayList<>();
        for (Expense e : all) {
            boolean monthOk = allTimeMode || displayedYearMonth.equals(DateUtils.yearMonthOf(e.getDate()));
            boolean catOk = FILTER_ALL.equals(currentCategory) || currentCategory.equals(e.getCategory());
            if (monthOk && catOk) filtered.add(e);
        }

        List<Object> rows = new ArrayList<>();
        String lastDate = null;
        double dayTotal = 0;
        int headerIndex = -1;
        for (int i = 0; i < filtered.size(); i++) {
            Expense e = filtered.get(i);
            if (!e.getDate().equals(lastDate)) {
                if (headerIndex >= 0) {
                    rows.set(headerIndex, new ExpenseAdapter.Header(lastDate, dayTotal));
                }
                lastDate = e.getDate();
                dayTotal = 0;
                rows.add(new ExpenseAdapter.Header(lastDate, 0));
                headerIndex = rows.size() - 1;
            }
            dayTotal += e.getAmount();
            rows.add(e);
        }
        if (headerIndex >= 0) {
            rows.set(headerIndex, new ExpenseAdapter.Header(lastDate, dayTotal));
        }

        adapter.submit(rows);

        boolean empty = filtered.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void openDetails(long id) {
        Intent i = new Intent(this, ExpenseDetailsActivity.class);
        i.putExtra(ExpenseDetailsActivity.EXTRA_ID, id);
        startActivity(i);
    }
}
