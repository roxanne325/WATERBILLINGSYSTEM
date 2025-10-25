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
            System.out.println("\n--- User Dashboard ---");
            System.out.println("1. View Bills");
            System.out.println("2. Make Payment");
            System.out.println("3. Logout");
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
                    viewBills();
                    break;
                case 2:
                    makePayment();
                    break;
                case 3:
                    System.out.println("Logging out...");
                    return; 
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        } while (choice != 3);
    }

    private void viewBills() {
        String sql = "SELECT bill_id, amount_due, due_date, status FROM tbl_bill WHERE user_id = ?";
        String[] headers = {"Bill ID", "Amount", "Due Date", "Status"};
        String[] columns = {"bill_id", "amount_due", "due_date", "status"};
        
        System.out.println("\n--- Your Bill Records ---");
        dbConfig.viewRecords(sql, headers, columns, this.userId); 
    }

    private void makePayment() {
        viewBills();
        
        System.out.print("Enter Bill ID for payment: ");
        int billId;
        try {
            billId = sc.nextInt();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid Bill ID.");
            sc.nextLine();
            return;
        }

        String billSql = "SELECT amount_due, status FROM tbl_bill WHERE bill_id = ? AND user_id = ?";
        List<Map<String, Object>> billDetails = dbConfig.fetchRecords(billSql, billId, this.userId);

        if (billDetails.isEmpty()) {
            System.out.println("Bill ID not found or does not belong to your account.");
            return;
        }

        Map<String, Object> bill = billDetails.get(0);
        double dueAmount = 0.0;
        Object amountObj = bill.get("amount_due");
        if (amountObj instanceof Number) {
             dueAmount = ((Number) amountObj).doubleValue();
        } else {
            System.out.println("Error: Could not read bill amount.");
            return;
        }
        
        String status = (String) bill.get("status");

        if (status.equals("Paid")) {
            System.out.println("This bill is already paid.");
            return;
        }
        
        System.out.printf("Amount Due: $%.2f\n", dueAmount);
        System.out.print("Enter Amount Paid: ");
        double amountPaid;
        try {
            amountPaid = sc.nextDouble();
            sc.nextLine();
        } catch (java.util.InputMismatchException e) {
            System.out.println("Invalid amount entered.");
            sc.nextLine();
            return;
        }
        
        if (amountPaid < dueAmount) {
            System.out.println("Insufficient amount. Payment failed.");
            return; 
        }
        
        System.out.print("Enter Payment Method (e.g., Cash, Card, Online): ");
        String paymentMethod = sc.nextLine();
        
        String updateBillSql = "UPDATE tbl_bill SET status = 'Paid' WHERE bill_id = ?";
        dbConfig.updateRecord(updateBillSql, billId);

        String paymentSql = "INSERT INTO tbl_payment (bill_id, amount_paid, payment_method) VALUES (?, ?, ?)";
        dbConfig.addRecord(paymentSql, billId, dueAmount, paymentMethod); 
        
        System.out.println("\n--- Payment Confirmation ---");
        System.out.println("Bill ID: " + billId);
        System.out.printf("Amount Paid: $%.2f\n", dueAmount);
        System.out.println("Payment successful! Thank you.");
    }
}