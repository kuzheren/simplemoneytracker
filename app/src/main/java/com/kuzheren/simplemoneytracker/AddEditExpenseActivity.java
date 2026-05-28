package com.kuzheren.simplemoneytracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kuzheren.simplemoneytracker.data.ExpenseRepository;
import com.kuzheren.simplemoneytracker.model.Expense;
import com.kuzheren.simplemoneytracker.util.Categories;
import com.kuzheren.simplemoneytracker.util.DateUtils;

import java.util.Calendar;

public class AddEditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "expense_id";

    private TextInputLayout tilAmount;
    private TextInputEditText etAmount;
    private TextInputEditText etDate;
    private TextInputEditText etDescription;
    private ChipGroup chipGroup;

    private long editingId = -1;
    private String selectedDate;
    private String selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        tilAmount = findViewById(R.id.til_amount);
        etAmount = findViewById(R.id.et_amount);
        etDate = findViewById(R.id.et_date);
        etDescription = findViewById(R.id.et_description);
        chipGroup = findViewById(R.id.chip_group_categories);
        MaterialButton btnSave = findViewById(R.id.btn_save);

        toolbar.setNavigationOnClickListener(v -> finish());

        editingId = getIntent().getLongExtra(EXTRA_ID, -1L);
        boolean editing = editingId > 0;
        toolbar.setTitle(editing ? R.string.title_edit_expense : R.string.title_new_expense);

        buildCategoryChips();

        if (editing) {
            Expense e = ExpenseRepository.get(this).getById(editingId);
            if (e != null) {
                etAmount.setText(stripTrailingZeros(e.getAmount()));
                etDescription.setText(e.getDescription());
                setDate(e.getDate());
                selectCategory(e.getCategory());
            } else {
                setDate(DateUtils.todayIso());
            }
        } else {
            setDate(DateUtils.todayIso());
        }

        etDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> save());
    }

    private void buildCategoryChips() {
        chipGroup.removeAllViews();
        for (String cat : Categories.ALL) {
            Chip chip = new Chip(this);
            chip.setText(cat);
            chip.setTag(cat);
            chip.setCheckable(true);
            chipGroup.addView(chip);
        }
        chipGroup.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) {
                selectedCategory = null;
            } else {
                Chip c = group.findViewById(ids.get(0));
                selectedCategory = c != null ? (String) c.getTag() : null;
            }
        });
    }

    private void selectCategory(String cat) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip c = (Chip) chipGroup.getChildAt(i);
            if (cat.equals(c.getTag())) {
                c.setChecked(true);
                selectedCategory = cat;
                return;
            }
        }
    }

    private void setDate(String iso) {
        selectedDate = iso;
        etDate.setText(DateUtils.formatHuman(iso));
    }

    private void showDatePicker() {
        Calendar c = DateUtils.parseToCalendar(selectedDate);
        DatePickerDialog dlg = new DatePickerDialog(this,
                (view, year, month, day) -> setDate(DateUtils.toIso(year, month, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dlg.show();
    }

    private void save() {
        tilAmount.setError(null);
        String raw = etAmount.getText() == null ? "" : etAmount.getText().toString().trim().replace(',', '.');
        if (TextUtils.isEmpty(raw)) {
            tilAmount.setError(getString(R.string.err_amount_empty));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            tilAmount.setError(getString(R.string.err_amount_invalid));
            return;
        }
        if (amount <= 0) {
            tilAmount.setError(getString(R.string.err_amount_non_positive));
            return;
        }
        if (selectedCategory == null) {
            Toast.makeText(this, R.string.toast_choose_category, Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText() == null ? "" : etDescription.getText().toString().trim();

        ExpenseRepository repo = ExpenseRepository.get(this);
        if (editingId > 0) {
            Expense e = repo.getById(editingId);
            if (e != null) {
                e.setAmount(amount);
                e.setCategory(selectedCategory);
                e.setDate(selectedDate);
                e.setDescription(description);
                repo.update(e);
            }
        } else {
            repo.add(new Expense(0, amount, selectedCategory, selectedDate, description));
        }

        Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private static String stripTrailingZeros(double v) {
        if (Math.abs(v - Math.rint(v)) < 0.005) {
            return String.valueOf((long) v);
        }
        return String.valueOf(v);
    }
}
