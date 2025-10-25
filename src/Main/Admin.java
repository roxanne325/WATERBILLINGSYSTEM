package Main;

import Config.config;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.sql.SQLException;

public class Admin {
    private final config dbConfig;
    private final Scanner sc;
    private final int adminId;

    public Admin(config dbConfig, Scanner sc, int adminId) {
        this.dbConfig = dbConfig;
        this.sc = sc;
        this.adminId = adminId;
    }

    public void adminDashboard() {
        int choice;
        do {
            System.out.println("\n--- Admin Dashboard ---");
            System.out.println("1. View All Users");
            System.out.println("2. Manage Users (CRUD & Status)");
            System.out.println("3. Generate Bills (Start Billing Cycle)"); 
            System.out.println("4. View All Bills"); 
            System.out.println("5. Logout"); 
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
                    viewAllUsers();
                    break;
                case 2:
                    userManagementMenu();
                    break;
                case 3:
                    Billing billingApp = new Billing(dbConfig, sc, adminId);
                    billingApp.generateBills(); 
                    break;
                case 4:
                    viewAllBills();
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 5); 
    }
    
    private void userManagementMenu() {
        int choice;
        do {
            System.out.println("\n--- User Management ---");
            System.out.println("1. View All Users (Including Pending)");
            System.out.println("2. Update User Status (Approve/Reject)");
            System.out.println("3. Edit User Details (Name, Address, Type)");
            System.out.println("4. Delete User");
            System.out.println("5. Back to Dashboard");
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
                    viewAllUsers();
                    break;
                case 2:
                    updateUserStatus();
                    break;
                case 3:
                    editUser();
                    break;
                case 4:
                    deleteUser(); 
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 5);
    }
    
    private void viewAllUsers() {
        String sql = "SELECT user_id, u_name, u_email, u_type, u_status, u_address FROM tbl_user";
        String[] headers = {"ID", "Name", "Email", "Type", "Status", "Address"};
        String[] columns = {"user_id", "u_name", "u_email", "u_type", "u_status", "u_address"};

        System.out.println("\n--- All Users ---");
        dbConfig.viewRecords(sql, headers, columns); 
    }

    private void updateUserStatus() {
        viewAllUsers();
        System.out.print("Enter User ID to update status: ");
        int userId;
        try {
            userId = sc.nextInt();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid User ID.");
            sc.nextLine();
            return;
        }

        System.out.print("Enter new status (Approved/Pending/Declined): ");
        String newStatus = sc.nextLine().trim();

        if (newStatus.equalsIgnoreCase("Approved") || newStatus.equalsIgnoreCase("Pending") || newStatus.equalsIgnoreCase("Declined")) {
            String sql = "UPDATE tbl_user SET u_status = ? WHERE user_id = ?";
            dbConfig.updateRecord(sql, newStatus, userId);
            System.out.println("User " + userId + " status updated to " + newStatus);
        } else {
            System.out.println("Invalid status entered. Must be 'Approved', 'Pending', or 'Declined'.");
        }
    }

    private void editUser() {
        viewAllUsers();
        System.out.print("Enter User ID to edit: ");
        int userId;
        try {
            userId = sc.nextInt();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid User ID.");
            sc.nextLine();
            return;
        }

        String checkSql = "SELECT user_id FROM tbl_user WHERE user_id = ?";
        if (dbConfig.fetchRecords(checkSql, userId).isEmpty()) {
            System.out.println("Error: User ID not found.");
            return;
        }

        System.out.print("Enter new Name (leave blank to keep current): ");
        String newName = sc.nextLine();
        System.out.print("Enter new Address (leave blank to keep current): ");
        String newAddress = sc.nextLine();
        System.out.print("Enter new User Type (Admin/User, leave blank to keep current): ");
        String newType = sc.nextLine();

        StringBuilder updateSql = new StringBuilder("UPDATE tbl_user SET ");
        List<Object> params = new ArrayList<>();
        boolean needsUpdate = false;
        
        if (!newName.isEmpty()) {
            updateSql.append("u_name = ?, ");
            params.add(newName);
            needsUpdate = true;
        }
        if (!newAddress.isEmpty()) {
            updateSql.append("u_address = ?, ");
            params.add(newAddress);
            needsUpdate = true;
        }
        if (!newType.isEmpty()) {
             if (newType.equalsIgnoreCase("Admin") || newType.equalsIgnoreCase("User")) {
                 updateSql.append("u_type = ?, ");
                 params.add(newType);
                 needsUpdate = true;
             } else {
                 System.out.println("Warning: Invalid User Type. Type not updated.");
             }
        }
        
        if (!needsUpdate) {
            System.out.println("No changes made.");
            return;
        }

        updateSql.setLength(updateSql.length() - 2); 
        updateSql.append(" WHERE user_id = ?");
        params.add(userId);

        dbConfig.updateRecord(updateSql.toString(), params.toArray());
        System.out.println("User " + userId + " details updated successfully.");
    }

    private void deleteUser() {
        viewAllUsers();
        System.out.print("Enter User ID to DELETE: ");
        int userId;
        try {
            userId = sc.nextInt();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid User ID.");
            sc.nextLine();
            return;
        }
        
        System.out.print("WARNING: Deleting user " + userId + " will remove ALL related bills and readings. Confirm (yes/no): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            String sqlDelete = "DELETE FROM tbl_user WHERE user_id = ?";  
            int rowsAffected = dbConfig.deleteRecord(sqlDelete, userId);

            if (rowsAffected > 0) {
                 System.out.println("Deletion of User ID " + userId + " completed.");
            } else {
                 System.out.println("Deletion failed. User ID not found.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }
    
    private void viewAllBills() {
        String sql = "SELECT T1.bill_id, T2.u_name, T1.amount_due, T1.due_date, T1.status " +
                     "FROM tbl_bill T1 JOIN tbl_user T2 ON T1.user_id = T2.user_id";
        
        String[] headers = {"Bill ID", "User Name", "Amount", "Due Date", "Status"};
        String[] columns = {"bill_id", "u_name", "amount_due", "due_date", "status"};

        System.out.println("\n--- All Bills ---");
        dbConfig.viewRecords(sql, headers, columns);
    }
}