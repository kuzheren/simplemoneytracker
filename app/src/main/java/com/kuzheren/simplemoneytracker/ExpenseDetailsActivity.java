package com.kuzheren.simplemoneytracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.kuzheren.simplemoneytracker.data.ExpenseRepository;
import com.kuzheren.simplemoneytracker.model.Expense;
import com.kuzheren.simplemoneytracker.util.Categories;
import com.kuzheren.simplemoneytracker.util.DateUtils;
import com.kuzheren.simplemoneytracker.util.MoneyFormat;

public class ExpenseDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "expense_id";

    private long expenseId = -1;
    private ImageView ivIcon;
    private TextView tvAmount;
    private TextView tvCategory;
    private TextView tvDate;
    private TextView tvDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_details);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        ivIcon = findViewById(R.id.iv_icon);
        tvAmount = findViewById(R.id.tv_amount);
        tvCategory = findViewById(R.id.tv_category);
        tvDate = findViewById(R.id.tv_date);
        tvDescription = findViewById(R.id.tv_description);
        MaterialButton btnEdit = findViewById(R.id.btn_edit);
        MaterialButton btnDelete = findViewById(R.id.btn_delete);

        toolbar.setNavigationOnClickListener(v -> finish());

        expenseId = getIntent().getLongExtra(EXTRA_ID, -1L);

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AddEditExpenseActivity.class);
            i.putExtra(AddEditExpenseActivity.EXTRA_ID, expenseId);
            startActivity(i);
        });
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Expense e = ExpenseRepository.get(this).getById(expenseId);
        if (e == null) {
            finish();
            return;
        }
        bind(e);
    }

    private void bind(Expense e) {
        ivIcon.setImageResource(Categories.iconFor(e.getCategory()));
        tvAmount.setText("−" + MoneyFormat.format(e.getAmount()));
        tvCategory.setText(e.getCategory());
        tvDate.setText(DateUtils.formatHuman(e.getDate()));
        if (e.getDescription() == null || e.getDescription().isEmpty()) {
            tvDescription.setVisibility(View.GONE);
        } else {
            tvDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(e.getDescription());
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    ExpenseRepository.get(this).delete(expenseId);
                    finish();
                })
                .show();
    }
}
