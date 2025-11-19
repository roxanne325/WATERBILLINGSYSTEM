package Main;

import java.util.*;
import java.time.*;
import Config.config;

public class User {
    private int userId;
    private String name;
    private Scanner scanner = new Scanner(System.in);
    private config cfg = new config();

    public User(int id, String name) {
        this.userId = id;
        this.name = name;
    }

    public void menu() {
        while (true) {
            System.out.println("\n=== USER DASHBOARD ===");
            System.out.println("1. View Profile");
            System.out.println("2. View My Meter Readings");
            System.out.println("3. View My Bills");
            System.out.println("4. Pay a Bill");
            System.out.println("5. Logout");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": viewProfile(); break;
                case "2": viewReadings(); break;
                case "3": viewBills(); break;
                case "4": payBill(); break;
                case "5": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void viewProfile() {
        String sql = "SELECT user_id, u_name, u_address, u_email, u_type, u_status FROM tbl_user WHERE user_id = ?";
        List<Map<String,Object>> rows = cfg.fetchRecords(sql, userId);
        if (rows.isEmpty()) { System.out.println("Profile not found."); return; }
        Map<String,Object> u = rows.get(0);
        System.out.println("---- Profile ----");
        System.out.println("User ID: " + u.get("user_id"));
        System.out.println("Name: " + u.get("u_name"));
        System.out.println("Address: " + u.get("u_address"));
        System.out.println("Email: " + u.get("u_email"));
        System.out.println("Type: " + u.get("u_type"));
        System.out.println("Status: " + u.get("u_status"));
    }

    private void viewReadings() {
        String sql = "SELECT reading_id, previous_reading, current_reading, consumption, reading_date FROM tbl_meter_reading WHERE user_id = ? ORDER BY reading_date DESC";
        String[] h = {"Reading ID","Prev","Current","Consumption","Date"};
        String[] c = {"reading_id","previous_reading","current_reading","consumption","reading_date"};
        cfg.viewRecords(sql, h, c, userId);
    }

    private void viewBills() {
        String sql = "SELECT bill_id, reading_id, billing_month, amount_due, due_date, status FROM tbl_bill WHERE user_id = ? ORDER BY due_date DESC";
        String[] h = {"Bill ID","Reading ID","Month","Amount","Due Date","Status"};
        String[] c = {"bill_id","reading_id","billing_month","amount_due","due_date","status"};
        cfg.viewRecords(sql, h, c, userId);
    }

    private void payBill() {
        System.out.print("Enter bill id to pay: ");
        String bidS = scanner.nextLine().trim();
        if (!Validators.isInteger(bidS)) { System.out.println("Invalid id."); return; }
        int bid = Integer.parseInt(bidS);

        String chk = "SELECT bill_id, amount_due, status FROM tbl_bill WHERE bill_id = ? AND user_id = ?";
        List<Map<String,Object>> rows = cfg.fetchRecords(chk, bid, userId);
        if (rows.isEmpty()) { System.out.println("Bill not found or not yours."); return; }
        Map<String,Object> bill = rows.get(0);
        if ("Paid".equalsIgnoreCase(String.valueOf(bill.get("status")))) { System.out.println("Bill already paid."); return; }
        double due = Double.parseDouble(String.valueOf(bill.get("amount_due")));
        System.out.println("Amount due: " + due);
        System.out.print("Amount to pay now: ");
        String amtS = scanner.nextLine().trim();
        if (!Validators.isDouble(amtS)) { System.out.println("Invalid amount."); return; }
        double amt = Double.parseDouble(amtS);
        if (amt <= 0) { System.out.println("Must be positive."); return; }
        if (amt > due) {
            System.out.print("Overpay and mark as paid? (y/n): ");
            String a = scanner.nextLine().trim();
            if (!"y".equalsIgnoreCase(a)) { System.out.println("Payment cancelled."); return; }
        }
        System.out.print("Payment method: ");
        String method = scanner.nextLine().trim();
        if (method.isEmpty()) method = "Cash";

        String insert = "INSERT INTO tbl_payment (bill_id, amount_paid, payment_method) VALUES (?, ?, ?)";
        int pid = cfg.addRecordAndGetId(insert, bid, amt, method);
        if (pid <= 0) { System.out.println("Payment failed."); return; }
        String upd = "UPDATE tbl_bill SET status = 'Paid' WHERE bill_id = ?";
        cfg.updateRecord(upd, bid);
        System.out.println("Payment successful. Thank you!");
    }
}
