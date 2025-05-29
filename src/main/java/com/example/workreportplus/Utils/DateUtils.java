/**
 * @author Alex Sereda
 * @date 28.05.2025 9:20
 */
package com.example.workreportplus.Utils;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class DateUtils {

    static final Locale UKRAINIAN = new Locale("uk", "UA");

    public static String formatUkrainianMonthYear(LocalDate date) {
        String month = date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, UKRAINIAN);
        int year = date.getYear();
        return capitalizeFirstLetter(month) + " " + year;
    }

    private static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase(UKRAINIAN) + input.substring(1);
    }

    // Example usage
    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2025, 5, 1);
        System.out.println(formatUkrainianMonthYear(date));  // Травень 2025
    }
}