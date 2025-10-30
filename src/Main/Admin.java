package Main;

import Config.config;
import java.util.Scanner;
import java.util.List;
import java.util.Map;

public class Admin {
    private final config dbConfig;
    private final Scanner sc;
    private final int adminId;

    public Admin(config dbConfig, Scanner sc, int adminId) {
        this.dbConfig = dbConfig;
        this.sc = sc;
        this.adminId = adminId;
    }

    public void adminDashboard(String adminName) {
        int choice;
        do {
            System.out.println("---");
            System.out.println("================= ADMIN MENU =================");
            System.out.println("1. Manage Users");
            System.out.println("2. Billing Cycle");
            System.out.println("3. Logout");
            System.out.println("-----------------------------------------------");
            System.out.print("Enter your choice: ");

            try {
                choice = sc.nextInt();
                sc.nextLine(); 
            } catch (java.util.InputMismatchException e) {
                sc.nextLine();
                choice = 0;
            }

            switch (choice) {
                case 1:
                    manageUsers();
                    break;
                case 2:
                    Billing billingApp = new Billing(dbConfig, sc, adminId);
                    billingApp.generateBills(); 
                    break;
                case 3:
                    System.out.println("Logging out...");
                    System.out.println("-----------------------------------------------------");
                    System.out.println("Returning to Main Menu...");
                    return;
                default:
            }
        } while (choice != 3); 
    }
    
    private void manageUsers() {
        System.out.println("================= USER MANAGEMENT =================");
        
        String sql = "SELECT user_id, u_name, u_email, u_status FROM tbl_user WHERE u_status = 'Pending'";
        String[] headers = {"ID", "Name", "Email", "Status"};
        String[] columns = {"user_id", "u_name", "u_email", "u_status"};

        dbConfig.viewRecords(sql, headers, columns);

        System.out.print("\nEnter User ID to update status: ");
        int userId;
        try {
            userId = sc.nextInt();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            sc.nextLine();
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to Admin Menu...");
            return;
        }

        System.out.print("Approve or Decline this user? (A/D): ");
        String statusInput = sc.nextLine().trim().toUpperCase();
        String newStatus;

        if (statusInput.equals("A")) {
            newStatus = "Approved";
        } else if (statusInput.equals("D")) {
            newStatus = "Declined";
        } else {
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to Admin Menu...");
            return;
        }

        String updateSql = "UPDATE tbl_user SET u_status = ? WHERE user_id = ?";
        dbConfig.updateRecord(updateSql, newStatus, userId);
        
        System.out.println("User account approved successfully!");
        System.out.println("-----------------------------------------------------");
        System.out.println("Returning to Admin Menu...");
    }
}