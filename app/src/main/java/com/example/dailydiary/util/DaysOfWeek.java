package com.example.dailydiary.util;

import java.util.LinkedHashSet;
import java.util.Set;

public final class DaysOfWeek {

    private DaysOfWeek() {
    }

    public static Set<Integer> parse(String raw) {
        Set<Integer> days = new LinkedHashSet<>();
        if (raw == null || raw.trim().isEmpty()) {
            return days;
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part.trim());
                if (value >= 1 && value <= 7) {
                    days.add(value);
                }
            } catch (NumberFormatException ignored) {
                // skip invalid token
            }
        }
        return days;
    }

    public static String serialize(Set<Integer> days) {
        if (days == null || days.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Integer day : days) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(day);
        }
        return builder.toString();
    }
}
