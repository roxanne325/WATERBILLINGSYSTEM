package Main;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import Config.config;

public class Admin {
    private int adminId;
    private String adminName;
    private Scanner scanner = new Scanner(System.in);
    private config cfg = new config();
    private Billing billing = new Billing();

    public Admin(int id, String name) {
        this.adminId = id;
        this.adminName = name;
    }

    public void menu() {
        while (true) {
            System.out.println("\n=== ADMIN DASHBOARD ===");
            System.out.println("1. Manage Users");
            System.out.println("2. Add Meter Reading");
            System.out.println("3. Generate Bill (for reading)");
            System.out.println("4. View Reports");
            System.out.println("5. Record Payment");
            System.out.println("6. Change my password");
            System.out.println("7. Logout");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": manageUsers(); break;
                case "2": addMeterReading(); break;
                case "3": generateBillMenu(); break;
                case "4": viewReports(); break;
                case "5": recordPayment(); break;
                case "6": changePassword(); break;
                case "7": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void manageUsers() {
        while (true) {
            System.out.println("\n--- Manage Users ---");
            System.out.println("1. List Users");
            System.out.println("2. Add User");
            System.out.println("3. Edit User");
            System.out.println("4. Delete User");
            System.out.println("5. Back");
            System.out.print("Choose: ");
            String opt = scanner.nextLine().trim();
            switch (opt) {
                case "1": listUsers(); break;
                case "2": addUser(); break;
                case "3": editUser(); break;
                case "4": deleteUser(); break;
                case "5": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    private void listUsers() {
        String sql = "SELECT user_id, u_name, u_email, u_type, u_status FROM tbl_user";
        String[] headers = {"User ID","Name","Email","Type","Status"};
        String[] cols = {"user_id","u_name","u_email","u_type","u_status"};
        cfg.viewRecords(sql, headers, cols);
    }

    private void addUser() {
        System.out.println("=== Add New User ===");
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Address: ");
        String addr = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Type (User/Admin): ");
        String type = scanner.nextLine().trim();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        System.out.print("Status (Pending/Approved): ");
        String status = scanner.nextLine().trim();
        if (!"Pending".equalsIgnoreCase(status) && !"Approved".equalsIgnoreCase(status)) {
        System.out.println("Invalid status. Defaulting to Pending.");
        status = "Pending";
        }

        List<String> errors = new ArrayList<>();
        if (name.length() < 3) errors.add("Name too short.");
        if (addr.isEmpty()) errors.add("Address required.");
        if (!Validators.isValidEmail(email)) errors.add("Invalid email.");
        if (!"User".equalsIgnoreCase(type) && !"Admin".equalsIgnoreCase(type)) errors.add("Type must be User or Admin.");
        if (!Validators.isValidPassword(pass)) errors.add("Weak password.");

        String chk = "SELECT user_id FROM tbl_user WHERE u_email = ?";
        if (!cfg.fetchRecords(chk, email).isEmpty()) errors.add("Email already exists.");

        if (!errors.isEmpty()) {
            System.out.println("Cannot add user:");
            errors.forEach(e -> System.out.println("- " + e));
            return;
        }

        String hashed = config.hashPassword(pass);
        String sql = "INSERT INTO tbl_user (u_name, u_address, u_email, u_pass, u_type, u_status) VALUES (?, ?, ?, ?, ?, ?)";
        cfg.addRecord(sql, name, addr, email, hashed, type, status);
        System.out.println("User added successfully.");
    }

    private void editUser() {
        System.out.print("Enter user id to edit: ");
        String id = scanner.nextLine().trim();
        if (!Validators.isInteger(id)) { System.out.println("Invalid id."); return; }
        String get = "SELECT user_id, u_name, u_address, u_email, u_type, u_status FROM tbl_user WHERE user_id = ?";
        List<Map<String, Object>> rows = cfg.fetchRecords(get, Integer.parseInt(id));
        if (rows.isEmpty()) { System.out.println("User not found."); return; }
        Map<String, Object> u = rows.get(0);
        System.out.println("Current Name: " + u.get("u_name"));
        System.out.print("New name (blank to keep): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = (String) u.get("u_name");

        System.out.println("Current Address: " + u.get("u_address"));
        System.out.print("New address (blank to keep): ");
        String addr = scanner.nextLine().trim();
        if (addr.isEmpty()) addr = (String) u.get("u_address");

        System.out.println("Current Type: " + u.get("u_type"));
        System.out.print("New type (User/Admin): ");
        String type = scanner.nextLine().trim();
        if (type.isEmpty()) type = (String) u.get("u_type");
        if (!"User".equalsIgnoreCase(type) && !"Admin".equalsIgnoreCase(type)) { System.out.println("Invalid type."); return; }

        System.out.println("Current Status: " + u.get("u_status"));
        System.out.print("New status (Pending/Approved): ");
        String status = scanner.nextLine().trim();
        if (status.isEmpty()) status = (String) u.get("u_status");
        if (!"Pending".equalsIgnoreCase(status) && !"Approved".equalsIgnoreCase(status)) {
        System.out.println("Invalid status.");
        return;
    }

        String upd = "UPDATE tbl_user SET u_name = ?, u_address = ?, u_type = ?, u_status = ? WHERE user_id = ?";
        cfg.updateRecord(upd, name, addr, type, status, Integer.parseInt(id));

        System.out.println("User updated.");
    }

    private void deleteUser() {
        System.out.print("Enter user id to delete: ");
        String id = scanner.nextLine().trim();
        if (!Validators.isInteger(id)) { System.out.println("Invalid id."); return; }
        System.out.print("Are you sure? Type DELETE to confirm: ");
        String confirm = scanner.nextLine().trim();
        if (!"DELETE".equals(confirm)) { System.out.println("Canceled."); return; }

        String del = "DELETE FROM tbl_user WHERE user_id = ?";
        int rows = cfg.deleteRecord(del, Integer.parseInt(id));
        if (rows > 0) System.out.println("User removed.");
    }

    private void addMeterReading() {
        System.out.println("=== Add Meter Reading ===");
        System.out.print("User ID: ");
        String uidStr = scanner.nextLine().trim();
        if (!Validators.isInteger(uidStr)) { System.out.println("Invalid user id."); return; }
        int uid = Integer.parseInt(uidStr);
        String chkUser = "SELECT user_id FROM tbl_user WHERE user_id = ?";
        if (cfg.fetchRecords(chkUser, uid).isEmpty()) { System.out.println("User not found."); return; }

        System.out.print("Previous reading (numeric): ");
        String prevS = scanner.nextLine().trim();
        System.out.print("Current reading (numeric): ");
        String currS = scanner.nextLine().trim();
        if (!Validators.isDouble(prevS) || !Validators.isDouble(currS)) { System.out.println("Readings must be numeric."); return; }
        double prev = Double.parseDouble(prevS);
        double curr = Double.parseDouble(currS);
        if (prev < 0 || curr < 0) { System.out.println("Readings cannot be negative."); return; }
        if (curr < prev) { System.out.println("Current reading cannot be less than previous."); return; }

        double consumption = curr - prev;
        LocalDate date = LocalDate.now();
        String insert = "INSERT INTO tbl_meter_reading (user_id, previous_reading, current_reading, consumption, reading_date) VALUES (?, ?, ?, ?, ?)";
        int readingId = cfg.addRecordAndGetId(insert, uid, prev, curr, consumption, date.toString());
        if (readingId > 0) System.out.println("Meter reading saved. ID: " + readingId);
        else System.out.println("Failed to save reading.");
    }

    private void generateBillMenu() {
        System.out.print("Enter reading id to generate bill for: ");
        String ridS = scanner.nextLine().trim();
        if (!Validators.isInteger(ridS)) { System.out.println("Invalid id."); return; }
        int rid = Integer.parseInt(ridS);

        String sql = "SELECT reading_id, user_id, consumption, reading_date FROM tbl_meter_reading WHERE reading_id = ?";
        List<Map<String,Object>> rows = cfg.fetchRecords(sql, rid);
        if (rows.isEmpty()) { System.out.println("Reading not found."); return; }
        Map<String,Object> r = rows.get(0);
        int uid = Integer.parseInt(String.valueOf(r.get("user_id")));
        double consumption = Double.parseDouble(String.valueOf(r.get("consumption")));
        String readingDate = String.valueOf(r.get("reading_date"));

        LocalDate d;
        try {
            d = LocalDate.parse(readingDate);
        } catch (Exception ex) {
            d = LocalDate.now();
        }
        String billingMonth = d.getMonth().toString() + " " + d.getYear();

        String chk = "SELECT bill_id FROM tbl_bill WHERE reading_id = ?";
        if (!cfg.fetchRecords(chk, rid).isEmpty()) {
            System.out.println("Bill already generated for this reading.");
            return;
        }

        double rate = promptRatePerUnit();
        double amount = billing.calculateAmount(consumption, rate);
        LocalDate dueDate = LocalDate.now().plusDays(15);
        String insertBill = "INSERT INTO tbl_bill (user_id, reading_id, billing_month, amount_due, due_date, status) VALUES (?, ?, ?, ?, ?, 'Unpaid')";
        int billId = cfg.addRecordAndGetId(insertBill, uid, rid, billingMonth, amount, dueDate.toString());
        if (billId > 0) System.out.println("Bill generated. Bill ID: " + billId + " Amount: " + amount);
        else System.out.println("Failed to generate bill.");
    }

    private double promptRatePerUnit() {
        while (true) {
            System.out.print("Enter rate per cubic (e.g., 20.0): ");
            String r = scanner.nextLine().trim();
            if (Validators.isDouble(r)) {
                double val = Double.parseDouble(r);
                if (val <= 0) { System.out.println("Rate must be positive."); continue; }
                return val;
            }
            System.out.println("Invalid rate.");
        }
    }

    private void viewReports() {
        System.out.println("\n--- Reports ---");
        System.out.println("1. All Users");
        System.out.println("2. Meter Readings");
        System.out.println("3. Bills");
        System.out.println("4. Payments");
        System.out.print("Choose: ");
        String opt = scanner.nextLine().trim();
        switch (opt) {
            case "1":
                listUsers(); break;
            case "2":
                String sqlR = "SELECT reading_id, user_id, previous_reading, current_reading, consumption, reading_date FROM tbl_meter_reading ORDER BY reading_date DESC";
                String[] hR = {"Reading ID","User ID","Prev","Curr","Consumption","Date"};
                String[] cR = {"reading_id","user_id","previous_reading","current_reading","consumption","reading_date"};
                cfg.viewRecords(sqlR, hR, cR);
                break;
            case "3":
                String sqlB = "SELECT bill_id, user_id, reading_id, billing_month, amount_due, due_date, status FROM tbl_bill ORDER BY due_date DESC";
                String[] hB = {"Bill ID","User ID","Reading ID","Month","Amount","Due Date","Status"};
                String[] cB = {"bill_id","user_id","reading_id","billing_month","amount_due","due_date","status"};
                cfg.viewRecords(sqlB, hB, cB);
                break;
            case "4":
                String sqlP = "SELECT payment_id, bill_id, amount_paid, payment_method, payment_date FROM tbl_payment ORDER BY payment_date DESC";
                String[] hP = {"Payment ID","Bill ID","Amount Paid","Method","Date"};
                String[] cP = {"payment_id","bill_id","amount_paid","payment_method","payment_date"};
                cfg.viewRecords(sqlP, hP, cP);
                break;
            default:
                System.out.println("Invalid.");
        }
    }

    private void recordPayment() {
        System.out.print("Enter bill id to record payment for: ");
        String bidS = scanner.nextLine().trim();
        if (!Validators.isInteger(bidS)) { System.out.println("Invalid id."); return; }
        int bid = Integer.parseInt(bidS);
        String chk = "SELECT bill_id, amount_due, status FROM tbl_bill WHERE bill_id = ?";
        List<Map<String,Object>> rows = cfg.fetchRecords(chk, bid);
        if (rows.isEmpty()) { System.out.println("Bill not found."); return; }
        Map<String,Object> bill = rows.get(0);
        String status = String.valueOf(bill.get("status"));
        double amountDue = Double.parseDouble(String.valueOf(bill.get("amount_due")));
        if ("Paid".equalsIgnoreCase(status)) { System.out.println("Bill already paid."); return; }

        System.out.println("Amount due: " + amountDue);
        System.out.print("Amount being paid now: ");
        String amtS = scanner.nextLine().trim();
        if (!Validators.isDouble(amtS)) { System.out.println("Invalid amount."); return; }
        double amt = Double.parseDouble(amtS);
        if (amt <= 0) { System.out.println("Must be positive."); return; }
        if (amt > amountDue) {
            System.out.print("Amount greater than due. Accept overpayment and mark as paid? (y/n): ");
            String a = scanner.nextLine().trim();
            if (!"y".equalsIgnoreCase(a)) { System.out.println("Payment cancelled."); return; }
        }

        System.out.print("Payment method (Cash/Card/Online): ");
        String method = scanner.nextLine().trim();
        if (method.isEmpty()) method = "Cash";

        String ins = "INSERT INTO tbl_payment (bill_id, amount_paid, payment_method) VALUES (?, ?, ?)";
        int pid = cfg.addRecordAndGetId(ins, bid, amt, method);
        if (pid <= 0) { System.out.println("Payment record failed."); return; }

        String upd = "UPDATE tbl_bill SET status = 'Paid' WHERE bill_id = ?";
        cfg.updateRecord(upd, bid);
        System.out.println("Payment recorded and bill marked Paid.");
    }

    private void changePassword() {
        System.out.print("Enter current password: ");
        String cur = scanner.nextLine();
        String sql = "SELECT u_pass FROM tbl_user WHERE user_id = ?";
        List<Map<String,Object>> rows = cfg.fetchRecords(sql, adminId);
        if (rows.isEmpty()) { System.out.println("User not found."); return; }
        String stored = String.valueOf(rows.get(0).get("u_pass"));
        if (!config.hashPassword(cur).equals(stored)) { System.out.println("Incorrect current password."); return; }

        System.out.print("Enter new password: ");
        String np = scanner.nextLine();
        if (!Validators.isValidPassword(np)) { System.out.println("Weak password."); return; }
        String hash = config.hashPassword(np);
        String upd = "UPDATE tbl_user SET u_pass = ? WHERE user_id = ?";
        cfg.updateRecord(upd, hash, adminId);
        System.out.println("Password changed.");
    }
}
