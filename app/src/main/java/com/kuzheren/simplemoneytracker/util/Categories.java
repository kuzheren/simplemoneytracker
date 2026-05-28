package com.kuzheren.simplemoneytracker.util;

import com.kuzheren.simplemoneytracker.R;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Categories {

    public static final String FOOD = "Еда";
    public static final String TRANSPORT = "Транспорт";
    public static final String HOUSING = "Жильё";
    public static final String ENTERTAINMENT = "Развлечения";
    public static final String HEALTH = "Здоровье";
    public static final String CLOTHING = "Одежда";
    public static final String OTHER = "Прочее";

    public static final String[] ALL = {
            FOOD, TRANSPORT, HOUSING, ENTERTAINMENT, HEALTH, CLOTHING, OTHER
    };

    private static final Map<String, Integer> ICONS;

    static {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put(FOOD, R.drawable.ic_cat_food);
        m.put(TRANSPORT, R.drawable.ic_cat_transport);
        m.put(HOUSING, R.drawable.ic_cat_housing);
        m.put(ENTERTAINMENT, R.drawable.ic_cat_entertainment);
        m.put(HEALTH, R.drawable.ic_cat_health);
        m.put(CLOTHING, R.drawable.ic_cat_clothing);
        m.put(OTHER, R.drawable.ic_cat_other);
        ICONS = Collections.unmodifiableMap(m);
    }

    public static int iconFor(String category) {
        Integer res = ICONS.get(category);
        return res != null ? res : R.drawable.ic_cat_other;
    }

    private Categories() {}
}
