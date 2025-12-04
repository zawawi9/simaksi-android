package com.zawww.e_simaksi.util;

public class TextUtil {
    public static String formatStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "";
        }
        String[] words = status.split("_");
        StringBuilder formattedStatus = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                formattedStatus.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formattedStatus.toString().trim();
    }
}
