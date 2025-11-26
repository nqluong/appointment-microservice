package org.project.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class NameUtils {

    private static final Pattern BS_PATTERN = Pattern.compile("(?i)\\bBS\\.?\\b");

    private NameUtils() {

    }


    public static String formatDoctorFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return fullName;
        }

        String normalized = fullName.trim().replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("\\s*\\.\\s*", " ");

        normalized = normalized.trim().replaceAll("\\s+", " ");

        Matcher matcher = BS_PATTERN.matcher(normalized);
        if (matcher.find()) {
            String nameWithoutTitle = matcher.replaceAll("").trim().replaceAll("\\s+", " ");

            if (nameWithoutTitle.isEmpty()) {
                return "BS.";
            }
            return "BS. " + nameWithoutTitle;
        }

        return normalized;
    }


    public static boolean hasDoctorTitle(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        return BS_PATTERN.matcher(fullName).find();
    }


    public static String removeDoctorTitle(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return fullName;
        }

        String normalized = fullName.trim().replaceAll("\\s+", " ");
        Matcher matcher = BS_PATTERN.matcher(normalized);

        if (matcher.find()) {
            return matcher.replaceAll("").trim().replaceAll("\\s+", " ");
        }

        return normalized;
    }
}

