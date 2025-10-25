package Main;

import Config.config;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;

public class Billing {
    private final config dbConfig;
    private final Scanner sc;
    private final int adminId;

    public Billing(config dbConfig, Scanner sc, int adminId) {
        this.dbConfig = dbConfig;
        this.sc = sc;
        this.adminId = adminId;
    }

    public void generateBills() {
        System.out.println("\n--- Start Billing Cycle ---");
        
        String sqlUsers = "SELECT user_id, u_name FROM tbl_user WHERE u_type = 'User' AND u_status = 'Approved'";
        List<Map<String, Object>> usersToBill = dbConfig.fetchRecords(sqlUsers);

        if (usersToBill.isEmpty()) {
            System.out.println("No approved users found to generate bills for.");
            return;
        }
        
        System.out.println("Found " + usersToBill.size() + " approved users. Starting bill generation...");
        
        int successfulBills = 0;

        for (Map<String, Object> user : usersToBill) {
            int userId = (int) user.get("user_id");
            String userName = (String) user.get("u_name");

            System.out.println("\nProcessing bill for: " + userName + " (ID: " + userId + ")");

            String sqlLastReading = "SELECT current_reading FROM tbl_meter_reading WHERE user_id = ? ORDER BY reading_date DESC LIMIT 1";
            List<Map<String, Object>> lastReadingResult = dbConfig.fetchRecords(sqlLastReading, userId);
            
            double previousReading = 0.0;
            if (!lastReadingResult.isEmpty()) {
                Object readingObj = lastReadingResult.get(0).get("current_reading");
                if (readingObj instanceof Number) {
                    previousReading = ((Number) readingObj).doubleValue();
                }
            }
            System.out.printf("   Previous Reading: %.2f units.\n", previousReading);

            double currentReading;
            while (true) {
                System.out.print("   Enter Current Meter Reading: ");
                try {
                    currentReading = sc.nextDouble();
                    sc.nextLine(); 
                    if (currentReading < previousReading) {
                        System.out.println("   Error: Current reading cannot be less than the previous reading. Try again.");
                    } else {
                        break; 
                    }
                } catch (java.util.InputMismatchException e) {
                    System.out.println("   Invalid input. Please enter a valid number.");
                    sc.nextLine();
                }
            }

            double consumption = currentReading - previousReading;
            System.out.printf("   Calculated Consumption: %.2f units.\n", consumption);

            double baseFee = 5.00;
            double ratePerUnit = 1.50;
            double amountDue = baseFee + (consumption * ratePerUnit);
            
            String billingMonth = LocalDate.now().getMonth().toString() + "-" + LocalDate.now().getYear();
            String dueDate = LocalDate.now().plusDays(30).toString(); 

            String sqlInsertReading = "INSERT INTO tbl_meter_reading (user_id, previous_reading, current_reading, consumption, reading_date) VALUES (?, ?, ?, ?, DATE('now'))";
            int readingId = dbConfig.addRecordAndGetId(sqlInsertReading, userId, previousReading, currentReading, consumption);

            if (readingId > 0) {
                String sqlInsertBill = "INSERT INTO tbl_bill (user_id, reading_id, billing_month, amount_due, due_date, status) VALUES (?, ?, ?, ?, ?, 'Unpaid')";
                dbConfig.addRecord(sqlInsertBill, userId, readingId, billingMonth, amountDue, dueDate);
                
                System.out.printf("   Bill generated successfully! Amount: $%.2f (Due: %s)\n", amountDue, dueDate);
                successfulBills++;
            } else {
                System.out.println("   Failed to record meter reading. Bill generation skipped.");
            }
        }
        
        System.out.println("\n--- Billing Cycle Complete: " + successfulBills + " bills generated. ---");
    }
}

