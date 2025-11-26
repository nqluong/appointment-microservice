package org.project.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class để xử lý các thao tác liên quan đến tên
 */
public final class NameUtils {

    private static final Pattern BS_PATTERN = Pattern.compile("(?i)\\bBS\\.?\\b");

    private NameUtils() {
        // Private constructor để ngăn khởi tạo
    }

    /**
     * Chuẩn hóa tên đầy đủ của bác sĩ để danh hiệu "BS." xuất hiện ở đầu.
     *
     * Ví dụ:
     * - "Hồ BS. Thị Vân" -> "BS. Hồ Thị Vân"
     * - "Nguyễn Văn An BS" -> "BS. Nguyễn Văn An"
     * - "BS Trần Thị Bình" -> "BS. Trần Thị Bình"
     *
     * @param fullName Tên đầy đủ của bác sĩ
     * @return Tên đã được chuẩn hóa với "BS." ở đầu
     */
    public static String formatDoctorFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return fullName;
        }

        String normalized = fullName.trim().replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("\\s*\\.\\s*", " ");

        normalized = normalized.trim().replaceAll("\\s+", " ");

        Matcher matcher = BS_PATTERN.matcher(normalized);
        if (matcher.find()) {
            // Xóa bỏ "BS" hoặc "BS." ở bất kỳ vị trí nào
            String nameWithoutTitle = matcher.replaceAll("").trim().replaceAll("\\s+", " ");

            // Thêm "BS." vào đầu tên
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

