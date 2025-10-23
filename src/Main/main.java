
package Main;

import Config.config;
import java.util.*;

public class main {
    
    public static void main(String[] args) {
        config db = new config();
        Scanner sc = new Scanner(System.in);

        System.out.println("===============================================");
        System.out.println("=== WELCOME TO THE WATER BILLING SYSTEM ===");
        System.out.println("===============================================");

        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Admin Dashboard");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    login(db, sc);
                    break;

                case 2:
                    register(db, sc);
                    break;

                case 3:
                    System.out.println("\n🔑 Admin access required.");
                    System.out.print("Enter Admin Email: ");
                    String email = sc.nextLine();
                    System.out.print("Enter Admin Password: ");
                    String pass = sc.nextLine();

                    String query = "SELECT * FROM tbl_user WHERE u_email = ? AND u_pass = ? AND u_type = 'Admin'";
                    List<Map<String, Object>> adminCheck = db.fetchRecords(query, email, pass);
                    if (!adminCheck.isEmpty()) {
                        System.out.println("✅ Welcome Admin!");
                        adminMenu(db, sc);
                    } else {
                        System.out.println("❌ Invalid Admin credentials!");
                    }
                    break;

                case 4:
                    System.out.println("👋 Exiting... Thank you for using the Water Billing System!");
                    sc.close();
                    return;

                default:
                    System.out.println("⚠️ Invalid choice! Please select 1, 2, 3, or 4.");
                    break;
            }
        }
    }

    public static void login(config db, Scanner sc) {
        System.out.println("\n=== LOGIN ===");
        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String query = "SELECT * FROM tbl_user WHERE u_email = ? AND u_pass = ?";
        List<Map<String, Object>> userList = db.fetchRecords(query, email, password);

        if (userList.isEmpty()) {
            System.out.println("❌ Invalid email or password!");
            return;
        }

        Map<String, Object> user = userList.get(0);
        String userType = user.get("u_type").toString();
        String userName = user.get("u_name").toString();
        String userStatus = user.get("u_status").toString();

        if (userStatus.equalsIgnoreCase("Pending")) {
            System.out.println("⚠️ Your account is still pending approval. Please wait for admin approval.");
            return;
        }

        if (userStatus.equalsIgnoreCase("Declined")) {
            System.out.println("❌ Your account has been declined. Please contact the administrator.");
            return;
        }

        System.out.println("\n✅ Login Successful! Welcome, " + userName + " (" + userType + ")");

        if (userType.equalsIgnoreCase("Admin")) {
            adminMenu(db, sc);
        } else {
            userMenu(db, sc);
        }
    }

    public static void register(config db, Scanner sc) {
        System.out.println("\n=== USER REGISTRATION ===");

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Address: ");
        String address = sc.nextLine();

        System.out.print("Enter Contact: ");
        String contact = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        String checkEmail = "SELECT * FROM tbl_user WHERE u_email = ?";
        List<Map<String, Object>> exists = db.fetchRecords(checkEmail, email);
        if (!exists.isEmpty()) {
            System.out.println("⚠️ Email already exists! Please use a different email.");
            return;
        }

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        System.out.print("Enter Type (Admin/User): ");
        String type = sc.nextLine();

        String sql = "INSERT INTO tbl_user (u_name, u_address, u_contact, u_email, u_pass, u_type, u_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        db.addRecord(sql, name, address, contact, email, password, type, "Pending");

        System.out.println("🎉 Registration Successful! Your status is currently 'Pending'.");
    }

    public static void adminMenu(config db, Scanner sc) {
        while (true) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. Add User");
            System.out.println("2. View Users");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. Update Status (Approve/Decline)");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addUser(db, sc);
                    break;
                case 2:
                    viewUsers(db);
                    break;
                case 3:
                    updateUser(db, sc);
                    break;
                case 4:
                    deleteUser(db, sc);
                    break;
                case 5:
                    updateStatus(db, sc);
                    break;
                case 6:
                    System.out.println("🔒 Logging out...");
                    return;
                default:
                    System.out.println("⚠️ Invalid choice!");
                    break;
            }
        }
    }

    public static void userMenu(config db, Scanner sc) {
        while (true) {
            System.out.println("\n=== USER DASHBOARD ===");
            System.out.println("1. Add User");
            System.out.println("2. View Users");
            System.out.println("3. Update User");
            System.out.println("4. Delete User");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addUser(db, sc);
                    break;
                case 2:
                    viewUsers(db);
                    break;
                case 3:
                    updateUser(db, sc);
                    break;
                case 4:
                    deleteUser(db, sc);
                    break;
                case 5:
                    System.out.println("👋 Logging out...");
                    return;
                default:
                    System.out.println("⚠️ Invalid choice! Please select a valid option.");
                    break;
            }
        }
    }

    public static void addUser(config db, Scanner sc) {
        System.out.println("\n=== ADD USER ===");

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Address: ");
        String address = sc.nextLine();

        System.out.print("Enter Contact: ");
        String contact = sc.nextLine();

        System.out.print("Enter Email: ");
        String email = sc.nextLine();

        String check = "SELECT * FROM tbl_user WHERE u_email = ?";
        List<Map<String, Object>> duplicate = db.fetchRecords(check, email);
        if (!duplicate.isEmpty()) {
            System.out.println("⚠️ Email already exists!");
            return;
        }

        System.out.print("Enter Password: ");
        String pass = sc.nextLine();

        System.out.print("Enter Type (Admin/User): ");
        String type = sc.nextLine();

        String sql = "INSERT INTO tbl_user (u_name, u_address, u_contact, u_email, u_pass, u_type, u_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        db.addRecord(sql, name, address, contact, email, pass, type, "Pending");

        System.out.println("✅ User added successfully with status 'Pending'!");
    }

    public static void viewUsers(config db) {
        System.out.println("\n=== VIEW USERS ===");
        String query = "SELECT * FROM tbl_user";
        String[] headers = {"ID", "Name", "Address", "Contact", "Email", "Password", "Type", "Status"};
        String[] cols = {"u_id", "u_name", "u_address", "u_contact", "u_email", "u_pass", "u_type", "u_status"};
        db.viewRecords(query, headers, cols);
    }

    public static void updateUser(config db, Scanner sc) {
        System.out.println("\n=== UPDATE USER ===");
        viewUsers(db);

        System.out.print("Enter ID of user to update: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter New Name: ");
        String name = sc.nextLine();
        System.out.print("Enter New Address: ");
        String address = sc.nextLine();
        System.out.print("Enter New Contact: ");
        String contact = sc.nextLine();
        System.out.print("Enter New Email: ");
        String email = sc.nextLine();
        System.out.print("Enter New Password: ");
        String pass = sc.nextLine();
        System.out.print("Enter New Type (Admin/User): ");
        String type = sc.nextLine();

        String sql = "UPDATE tbl_user SET u_name = ?, u_address = ?, u_contact = ?, u_email = ?, u_pass = ?, u_type = ? WHERE u_id = ?";
        db.updateRecord(sql, name, address, contact, email, pass, type, id);

        System.out.println("✅ User updated successfully!");
    }

    public static void deleteUser(config db, Scanner sc) {
        System.out.println("\n=== DELETE USER ===");
        viewUsers(db);

        System.out.print("Enter ID of user to delete: ");
        int id = sc.nextInt();

        String sql = "DELETE FROM tbl_user WHERE u_id = ?";
        db.deleteRecord(sql, id);

        System.out.println("✅ User deleted successfully!");
    }

    public static void updateStatus(config db, Scanner sc) {
        System.out.println("\n=== UPDATE USER STATUS ===");
        viewUsers(db);

        System.out.print("Enter ID of user to update status: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter new status (Pending/Approved/Declined): ");
        String status = sc.nextLine();

        String sql = "UPDATE tbl_user SET u_status = ? WHERE u_id = ?";
        db.updateRecord(sql, status, id);

        System.out.println("✅ User status updated to '" + status + "'!");
    }
}
