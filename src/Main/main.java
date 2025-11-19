package Main;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import Config.config;

public class main {
    private static Scanner scanner = new Scanner(System.in);
    private static config cfg = new config();

    public static void main(String[] args) {
        System.out.println("=== WELCOME TO WATER BILLING SYSTEM ===");
        ensureDefaultAdminExists();

        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Login");
            System.out.println("2. Register (Customer)");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");
            String opt = scanner.nextLine().trim();

            switch (opt) {
                case "1":
                    login();
                    break;
                case "2":
                    registerUser();
                    break;
                case "3":
                    System.out.println("Goodbye.");
                    System.exit(0);
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void ensureDefaultAdminExists() {
        String sql = "SELECT COUNT(*) as cnt FROM tbl_user WHERE u_type = 'Admin'";
        List<Map<String, Object>> rows = cfg.fetchRecords(sql);
        int cnt = 0;
        if (!rows.isEmpty()) {
            Object o = rows.get(0).get("cnt");
            if (o != null) cnt = Integer.parseInt(String.valueOf(o));
        }
        if (cnt == 0) {
            String defaultPass = "Admin@123";
            String hashed = config.hashPassword(defaultPass);
            String insert = "INSERT INTO tbl_user (u_name, u_address, u_email, u_pass, u_type, u_status) VALUES (?, ?, ?, ?, 'Admin', 'Active')";
            cfg.addRecord(insert, "System Admin", "N/A", "admin@admin.com", hashed);
            System.out.println("Default admin created: admin@admin.com (password: Admin@123) — please change after login.");
        }
    }

    private static void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("Email and password required.");
            return;
        }

        String sql = "SELECT user_id, u_name, u_email, u_pass, u_type, u_status FROM tbl_user WHERE u_email = ?";
        List<Map<String, Object>> users = cfg.fetchRecords(sql, email);
        if (users.isEmpty()) {
            System.out.println("No user found with that email.");
            return;
        }
        Map<String, Object> user = users.get(0);
        String storedHash = (String) user.get("u_pass");
        String inputHash = config.hashPassword(password);
        if (storedHash == null || !storedHash.equals(inputHash)) {
            System.out.println("Incorrect password.");
            return;
        }
        String status = (String) user.get("u_status");
        if (!"Approved".equalsIgnoreCase(status)) {
        System.out.println("Your account is not approved yet. Current status: " + status);
        return;
        }

        int userId = Integer.parseInt(String.valueOf(user.get("user_id")));
        String userType = (String) user.get("u_type");
        String name = (String) user.get("u_name");
        System.out.println("Login successful. Welcome, " + name + " (" + userType + ")");

        if ("Admin".equalsIgnoreCase(userType)) {
            Admin admin = new Admin(userId, name);
            admin.menu();
        } else {
            User u = new User(userId, name);
            u.menu();
        }
    }

    private static void registerUser() {
        System.out.println("=== Register New Customer ===");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Address: ");
        String address = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Contact Number: ");
        String contact = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        List<String> errors = new ArrayList<>();
        if (name.isEmpty() || name.length() < 3) errors.add("Name must be at least 3 characters.");
        if (address.isEmpty()) errors.add("Address required.");
        if (!Validators.isValidEmail(email)) errors.add("Invalid email format.");
        if (!Validators.isValidContact(contact)) errors.add("Contact must be 7-15 digits.");
        if (!Validators.isValidPassword(password)) errors.add("Password must be min 8 chars, include uppercase, lowercase, digit and special char.");

        String chk = "SELECT user_id FROM tbl_user WHERE u_email = ?";
        List<Map<String, Object>> exists = cfg.fetchRecords(chk, email);
        if (!exists.isEmpty()) errors.add("Email already in use.");

        if (!errors.isEmpty()) {
            System.out.println("Registration failed due to following:");
            for (String e : errors) System.out.println("- " + e);
            return;
        }

        String hashed = config.hashPassword(password);
        String sql = "INSERT INTO tbl_user (u_name, u_address, u_email, u_pass, u_type, u_status) VALUES (?, ?, ?, ?, 'User', 'Pending')";

        cfg.addRecord(sql, name, address + " | Contact: " + contact, email, hashed);
        System.out.println("Registration successful. You may now login.");
    }
}
