package com.zawww.e_simaksi.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtil {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatDate(String isoDateString) {
        try {
            OffsetDateTime odt = OffsetDateTime.parse(isoDateString);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm", new Locale("id", "ID"));
            return odt.atZoneSameInstant(ZoneId.systemDefault()).format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            // return original string if parsing fails
            return isoDateString;
        }
    }
}
