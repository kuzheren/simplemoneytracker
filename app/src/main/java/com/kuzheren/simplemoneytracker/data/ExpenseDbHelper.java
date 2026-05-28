package com.kuzheren.simplemoneytracker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kuzheren.simplemoneytracker.model.Expense;
import com.kuzheren.simplemoneytracker.util.Categories;
import com.kuzheren.simplemoneytracker.util.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "expenses.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE = "expenses";
    private static final String COL_ID = "id";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_CATEGORY = "category";
    private static final String COL_DATE = "date";
    private static final String COL_DESCRIPTION = "description";

    public ExpenseDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AMOUNT + " REAL NOT NULL, " +
                COL_CATEGORY + " TEXT NOT NULL, " +
                COL_DATE + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT" +
                ")");
        seedDemoData(db);
    }

    private void seedDemoData(SQLiteDatabase db) {
        insertDemo(db, 450.00, Categories.FOOD, "2026-05-28", "Обед в кафе");
        insertDemo(db, 80.00, Categories.TRANSPORT, "2026-05-28", "Метро");
        insertDemo(db, 1290.50, Categories.FOOD, "2026-05-27", "Продукты на неделю");
        insertDemo(db, 2500.00, Categories.ENTERTAINMENT, "2026-05-27", "Кино с друзьями");
        insertDemo(db, 750.00, Categories.HEALTH, "2026-05-25", "Аптека");
        insertDemo(db, 3200.00, Categories.CLOTHING, "2026-05-22", "Кроссовки");
        insertDemo(db, 18000.00, Categories.HOUSING, "2026-05-15", "Коммуналка");
        insertDemo(db, 320.00, Categories.TRANSPORT, "2026-05-10", "Такси домой");
        insertDemo(db, 890.00, Categories.FOOD, "2026-05-05", "Доставка пиццы");

        insertDemo(db, 17500.00, Categories.HOUSING, "2026-04-15", "Коммуналка за апрель");
        insertDemo(db, 5400.00, Categories.FOOD, "2026-04-12", "Продукты в Ашане");
        insertDemo(db, 1200.00, Categories.ENTERTAINMENT, "2026-04-10", "Подписка на стриминг");
        insertDemo(db, 2100.00, Categories.CLOTHING, "2026-04-08", "Футболка и носки");
        insertDemo(db, 450.00, Categories.TRANSPORT, "2026-04-03", "Заправка");
        insertDemo(db, 600.00, Categories.OTHER, "2026-04-01", "Подарок другу");
    }

    private static void insertDemo(SQLiteDatabase db, double amount, String category,
                                   String date, String description) {
        ContentValues cv = new ContentValues();
        cv.put(COL_AMOUNT, amount);
        cv.put(COL_CATEGORY, category);
        cv.put(COL_DATE, date);
        cv.put(COL_DESCRIPTION, description);
        db.insert(TABLE, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public long insertExpense(Expense e) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toValues(e);
        return db.insert(TABLE, null, cv);
    }

    public int updateExpense(Expense e) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = toValues(e);
        return db.update(TABLE, cv, COL_ID + " = ?", new String[]{String.valueOf(e.getId())});
    }

    public int deleteExpense(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Expense getById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE, null, COL_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return readRow(c);
            return null;
        }
    }

    public List<Expense> getAll() {
        SQLiteDatabase db = getReadableDatabase();
        List<Expense> result = new ArrayList<>();
        String orderBy = COL_DATE + " DESC, " + COL_ID + " DESC";
        try (Cursor c = db.query(TABLE, null, null, null, null, null, orderBy)) {
            while (c.moveToNext()) result.add(readRow(c));
        }
        return result;
    }

    public double monthTotal(String yearMonth) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE +
                " WHERE substr(" + COL_DATE + ", 1, 7) = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{yearMonth})) {
            return c.moveToFirst() && !c.isNull(0) ? c.getDouble(0) : 0d;
        }
    }

    public double allTimeTotal() {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE, null)) {
            return c.moveToFirst() && !c.isNull(0) ? c.getDouble(0) : 0d;
        }
    }

    public Map<String, Double> getTotalsByCategory(String yearMonth) {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") FROM " + TABLE +
                " WHERE substr(" + COL_DATE + ", 1, 7) = ?" +
                " GROUP BY " + COL_CATEGORY +
                " ORDER BY SUM(" + COL_AMOUNT + ") DESC";
        try (Cursor c = db.rawQuery(sql, new String[]{yearMonth})) {
            while (c.moveToNext()) result.put(c.getString(0), c.getDouble(1));
        }
        return result;
    }

    public Map<String, Double> getTotalsByCategoryAllTime() {
        SQLiteDatabase db = getReadableDatabase();
        Map<String, Double> result = new LinkedHashMap<>();
        String sql = "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") FROM " + TABLE +
                " GROUP BY " + COL_CATEGORY +
                " ORDER BY SUM(" + COL_AMOUNT + ") DESC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) result.put(c.getString(0), c.getDouble(1));
        }
        return result;
    }

    public List<String> getMonthsWithData() {
        SQLiteDatabase db = getReadableDatabase();
        List<String> months = new ArrayList<>();
        String sql = "SELECT DISTINCT substr(" + COL_DATE + ", 1, 7) AS ym FROM " + TABLE +
                " ORDER BY ym DESC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) months.add(c.getString(0));
        }
        return months;
    }

    private static ContentValues toValues(Expense e) {
        ContentValues cv = new ContentValues();
        cv.put(COL_AMOUNT, e.getAmount());
        cv.put(COL_CATEGORY, e.getCategory());
        cv.put(COL_DATE, e.getDate() != null ? e.getDate() : DateUtils.todayIso());
        cv.put(COL_DESCRIPTION, e.getDescription() == null ? "" : e.getDescription());
        return cv;
    }

    private static Expense readRow(Cursor c) {
        Expense e = new Expense();
        e.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        e.setAmount(c.getDouble(c.getColumnIndexOrThrow(COL_AMOUNT)));
        e.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
        e.setDate(c.getString(c.getColumnIndexOrThrow(COL_DATE)));
        e.setDescription(c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION)));
        return e;
    }
}
