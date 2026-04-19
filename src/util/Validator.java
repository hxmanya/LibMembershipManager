package util;

import java.util.regex.Pattern;

public class Validator {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[\\d\\s\\-()]{7,15}$");

    private static final Pattern ID_PATTERN =
        Pattern.compile("^[A-Za-z0-9_-]{3,20}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidId(String id) {
        return id != null && ID_PATTERN.matcher(id.trim()).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
