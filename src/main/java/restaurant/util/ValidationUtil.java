package restaurant.util;

public class ValidationUtil {

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) return false;
        String phoneRegex = "^\\d{10,15}$";
        return phone.matches(phoneRegex);
    }

    public static boolean isPositiveNumber(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            double value = Double.parseDouble(s);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            int value = Integer.parseInt(s);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPrice(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            double value = Double.parseDouble(s);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
