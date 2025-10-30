package Main;

import Config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class User {

    private final config dbConfig;
    private final Scanner sc;
    private final int userId; 

    public User(config dbConfig, Scanner sc, int userId) {
        this.dbConfig = dbConfig;
        this.sc = sc;
        this.userId = userId;
    }

    public void userDashboard() {
        int choice;
        do {
            System.out.println("================= USER MENU =================");
            System.out.println("1. View Bills");
            System.out.println("2. Make Payment");
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
                    viewBills();
                    break;
                case 2:
                    makePayment();
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

    private void viewBills() {
        System.out.println("================= BILL RECORDS =================");
        
        String sql = "SELECT T1.bill_id, T3.reading_date, T3.consumption, T1.amount_due, T1.status " +
                     "FROM tbl_bill T1 " +
                     "JOIN tbl_meter_reading T3 ON T1.reading_id = T3.reading_id " +
                     "WHERE T1.user_id = ?";
        
        String[] headers = {"Bill ID", "Reading Date", "Consumption", "Amount", "Status"};
        String[] columns = {"bill_id", "reading_date", "consumption", "amount_due", "status"};

        dbConfig.viewRecords(sql, headers, columns, this.userId);

        System.out.println("\nReturning to User Menu...");
    }

    private void makePayment() {
        System.out.println("================= PAYMENT SECTION =================");
        System.out.print("Enter Bill ID to Pay: ");
        int billId;
        try {
            billId = sc.nextInt();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            sc.nextLine();
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to User Menu...");
            return;
        }

        String billSql = "SELECT amount_due, status FROM tbl_bill WHERE bill_id = ? AND user_id = ?";
        List<Map<String, Object>> bills = dbConfig.fetchRecords(billSql, billId, this.userId);

        if (bills.isEmpty()) {
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to User Menu...");
            return;
        }

        Map<String, Object> bill = bills.get(0);
        
        Object amountDueObject = bill.get("amount_due");
        double amountDue;
        
        try {
            if (amountDueObject instanceof Double) {
                amountDue = (Double) amountDueObject;
            } else if (amountDueObject instanceof Integer) {
                amountDue = ((Integer) amountDueObject).doubleValue();
            } else {
                amountDue = Double.parseDouble(amountDueObject.toString());
            }
        } catch (NumberFormatException | NullPointerException e) {
            System.out.println("\nError: Could not read the bill amount.");
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to User Menu...");
            return;
        }
        
        String status = (String) bill.get("status");

        if (status.equalsIgnoreCase("PAID")) {
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to User Menu...");
            return;
        }

        System.out.print("Enter Payment Method (Cash/GCash): ");
        String method = sc.nextLine();
        System.out.print("Enter Amount Paid: ");
        double amountPaid;
        try {
            amountPaid = sc.nextDouble(); 
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            sc.nextLine();
            System.out.println("-----------------------------------------------------");
            System.out.println("Returning to User Menu...");
            return;
        }

        System.out.println("Verifying payment...");
        
        if (amountPaid >= amountDue) {
            String updateBillSql = "UPDATE tbl_bill SET status = 'PAID' WHERE bill_id = ?";
            dbConfig.updateRecord(updateBillSql, billId);

            String paymentSql = "INSERT INTO tbl_payment (bill_id, amount_paid, payment_method) VALUES (?, ?, ?)";
            dbConfig.addRecord(paymentSql, billId, amountPaid, method);

            System.out.println("\nPayment successful!");
            System.out.println("Bill #" + billId + " has been marked as PAID.");
        } else {
            System.out.println("\nPayment Failed: Amount paid is less than amount due.");
        }
        
        System.out.println("-----------------------------------------------------");
        System.out.println("Returning to User Menu...");
    }
}
