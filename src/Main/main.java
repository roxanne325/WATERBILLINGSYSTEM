package Main;

import Config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {
        config dbConfig = new config();
        Scanner sc = new Scanner(System.in);

        dbConfig.connectDB(); 

        int choice;
        do {
            System.out.println("\n--- Water Billing System ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            try {
                choice = sc.nextInt();
                sc.nextLine(); 
            } catch (java.util.InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine(); 
                choice = 0;
            }

            switch (choice) {
                case 1:
                    handleLogin(dbConfig, sc);
                    break;
                case 2:
                    handleRegistration(dbConfig, sc);
                    break;
                case 3:
                    System.out.println("Exiting application. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 3);
        
        sc.close();
    }

    private static void handleRegistration(config dbConfig, Scanner sc) {
        System.out.println("\n--- Account Registration ---");
        
        String finalUserType;
        do {
            System.out.print("Registering as (U)ser or (A)dmin? Enter U or A: ");
            String userTypeInput = sc.nextLine().trim().toUpperCase();
            if (userTypeInput.equals("A")) {
                finalUserType = "Admin";
            } else if (userTypeInput.equals("U")) {
                finalUserType = "User";
            } else {
                System.out.println("Invalid input. Please enter 'U' or 'A'.");
                finalUserType = null;
            }
        } while (finalUserType == null);
        
        String userStatus = "Pending"; 

        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Address: ");
        String address = sc.nextLine();        
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        
        String checkSql = "SELECT user_id FROM tbl_user WHERE u_email = ?";
        if (!dbConfig.fetchRecords(checkSql, email).isEmpty()) {
            System.out.println("Registration Failed: This email is already registered.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        
        String hashedPassword = config.hashPassword(password);
        
        String sql = "INSERT INTO tbl_user (u_name, u_address, u_email, u_pass, u_type, u_status) VALUES (?, ?, ?, ?, ?, ?)";
        
        dbConfig.addRecord(sql, name, address, email, hashedPassword, finalUserType, userStatus);
        
        System.out.println("Registration Successful! Account type: " + finalUserType + ".");
        System.out.println("Your account is currently PENDING approval. Log in after an administrator approves it.");
    }

    private static void handleLogin(config dbConfig, Scanner sc) {
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String hashedPassword = config.hashPassword(password);
        
        String sql = "SELECT user_id, u_type, u_status FROM tbl_user WHERE u_email = ? AND u_pass = ?";
        List<Map<String, Object>> result = dbConfig.fetchRecords(sql, email, hashedPassword);

        if (result.isEmpty()) {
            System.out.println("Login Failed: Invalid email or password.");
            return;
        }

        Map<String, Object> user = result.get(0);
        int userId = (int) user.get("user_id");
        String userType = (String) user.get("u_type");
        String userStatus = (String) user.get("u_status");

        if (!userStatus.equals("Approved")) {
            System.out.println("Login Failed: Your account status is " + userStatus + ". Please wait for approval.");
            return;
        }

        System.out.println("Login Successful as " + userType + "!");

        if (userType.equals("Admin")) {
            Admin adminApp = new Admin(dbConfig, sc, userId);
            adminApp.adminDashboard();
        } else if (userType.equals("User")) {
            User userApp = new User(dbConfig, sc, userId);
            userApp.userDashboard();
        }
    }
}