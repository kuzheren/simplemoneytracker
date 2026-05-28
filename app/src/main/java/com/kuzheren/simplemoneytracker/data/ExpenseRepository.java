package com.kuzheren.simplemoneytracker.data;

import android.content.Context;

import com.kuzheren.simplemoneytracker.model.Expense;

import java.util.List;
import java.util.Map;

public class ExpenseRepository {

    private static ExpenseRepository instance;

    public static synchronized ExpenseRepository get(Context context) {
        if (instance == null) {
            instance = new ExpenseRepository(context.getApplicationContext());
        }
        return instance;
    }

    private final ExpenseDbHelper db;

    private ExpenseRepository(Context appContext) {
        this.db = new ExpenseDbHelper(appContext);
    }

    public List<Expense> getAll() {
        return db.getAll();
    }

    public Expense getById(long id) {
        return db.getById(id);
    }

    public long add(Expense e) {
        return db.insertExpense(e);
    }

    public boolean update(Expense e) {
        return db.updateExpense(e) > 0;
    }

    public boolean delete(long id) {
        return db.deleteExpense(id) > 0;
    }

    public double monthTotal(String yearMonth) {
        return db.monthTotal(yearMonth);
    }

    public double allTimeTotal() {
        return db.allTimeTotal();
    }

    public List<String> getMonthsWithData() {
        return db.getMonthsWithData();
    }

    public Map<String, Double> getTotalsByCategory(String yearMonth) {
        return db.getTotalsByCategory(yearMonth);
    }

    public Map<String, Double> getTotalsByCategoryAllTime() {
        return db.getTotalsByCategoryAllTime();
    }
}
