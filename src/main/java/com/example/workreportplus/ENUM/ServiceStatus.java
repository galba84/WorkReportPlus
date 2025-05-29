package com.example.workreportplus.ENUM;

import com.google.api.services.sheets.v4.model.Color;

/**
 * @author Alex
 * @date 27.05.2025 23:28
 */
public enum ServiceStatus {
    FIRSTDAY, // 30 + green
    THIRTY,   // 30 + non-white
    HUNDRED,  // 100 + any color
    NO,       // "" + white
    L,        // "л"
    UNKNOWN;  // fallback

    public static ServiceStatus from(String value, Color bg) {
        value = value == null ? "" : value.trim().toLowerCase();

        if ("л".equals(value)) {
            return L;
        }

        if ("30".equals(value)) {
            if (isGreen(bg)) return FIRSTDAY;
            if (!isWhite(bg)) return THIRTY;
        }

        if ("100".equals(value) && !isWhite(bg)) {
            return HUNDRED;
        }

        if ("".equals(value) && isWhite(bg)) {
            return NO;
        }

        return UNKNOWN;
    }

    private static boolean isGreen(Color color) {
        if (color == null) return false;
        return round(color.getRed()) == 0 &&
                round(color.getGreen()) == 1 &&
                round(color.getBlue()) == 0;
    }

    private static boolean isWhite(Color color) {
        if (color == null) return true;
        return round(color.getRed()) == 1 &&
                round(color.getGreen()) == 1 &&
                round(color.getBlue()) == 1;
    }

    private static float round(Float f) {
        return f == null ? 0 : Math.round(f * 100f) / 100f;
    }
}