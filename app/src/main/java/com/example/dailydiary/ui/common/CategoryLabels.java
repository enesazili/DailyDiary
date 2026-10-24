package com.example.dailydiary.ui.common;

import android.content.Context;

import com.example.dailydiary.R;
import com.example.dailydiary.domain.model.TaskCategories;

public final class CategoryLabels {

    private CategoryLabels() {
    }

    public static String label(Context context, String categoryId) {
        if (TaskCategories.DERS.equals(categoryId)) {
            return context.getString(R.string.category_ders);
        }
        if (TaskCategories.PROJE.equals(categoryId)) {
            return context.getString(R.string.category_proje);
        }
        if (TaskCategories.KISISEL.equals(categoryId)) {
            return context.getString(R.string.category_kisisel);
        }
        if (TaskCategories.SPOR.equals(categoryId)) {
            return context.getString(R.string.category_spor);
        }
        if (TaskCategories.DIGER.equals(categoryId)) {
            return context.getString(R.string.category_diger);
        }
        return categoryId;
    }
}
