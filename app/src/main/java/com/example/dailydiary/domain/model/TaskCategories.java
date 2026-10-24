package com.example.dailydiary.domain.model;

import java.util.Arrays;
import java.util.List;

public final class TaskCategories {

    public static final String DERS = "DERS";
    public static final String PROJE = "PROJE";
    public static final String KISISEL = "KISISEL";
    public static final String SPOR = "SPOR";
    public static final String DIGER = "DIGER";

    private TaskCategories() {
    }

    public static List<String> builtInIds() {
        return Arrays.asList(DERS, PROJE, KISISEL, SPOR, DIGER);
    }

    public static String defaultId() {
        return KISISEL;
    }
}
