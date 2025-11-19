package Main;

import java.util.regex.*;

public class Validators {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern CONTACT = Pattern.compile("^[0-9\\-\\+\\s]{7,15}$");
    private static final Pattern INTEGER = Pattern.compile("^\\d+$");
    private static final Pattern DOUBLE = Pattern.compile("^[0-9]+(\\.[0-9]+)?$");
    private static final Pattern PASSWORD = Pattern.compile("^(?=.{8,})(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*\\W).*$");

    public static boolean isValidEmail(String s) {
        if (s == null) return false;
        return EMAIL.matcher(s).matches();
    }

    public static boolean isValidContact(String s) {
        if (s == null) return false;
        return CONTACT.matcher(s).matches();
    }

    public static boolean isValidPassword(String s) {
        if (s == null) return false;
        return PASSWORD.matcher(s).matches();
    }

    public static boolean isInteger(String s) {
        if (s == null) return false;
        return INTEGER.matcher(s).matches();
    }

    public static boolean isDouble(String s) {
        if (s == null) return false;
        return DOUBLE.matcher(s).matches();
    }
}


