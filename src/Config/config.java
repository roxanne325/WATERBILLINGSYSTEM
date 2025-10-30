
package Config;

import java.sql.*;
import java.util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class config {
    public Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); 
            con = DriverManager.getConnection("jdbc:sqlite:WaterbillingDB.db");
            System.out.println("Connection Successful");
            createTables(con);
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
        return con;
    }

    private void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            
            String sqlUser = "CREATE TABLE IF NOT EXISTS tbl_user ("
                    + "user_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "u_name TEXT NOT NULL,"
                    + "u_address TEXT NOT NULL,"
                    + "u_email TEXT UNIQUE NOT NULL,"
                    + "u_pass TEXT NOT NULL,"
                    + "u_type TEXT NOT NULL DEFAULT 'User'," 
                    + "u_status TEXT NOT NULL DEFAULT 'Pending'" 
                    + ")";
            stmt.execute(sqlUser);

            String sqlReading = "CREATE TABLE IF NOT EXISTS tbl_meter_reading ("
                    + "reading_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER NOT NULL,"
                    + "previous_reading DOUBLE NOT NULL,"
                    + "current_reading DOUBLE NOT NULL,"
                    + "consumption DOUBLE NOT NULL,"
                    + "reading_date DATE NOT NULL,"
                    + "FOREIGN KEY (user_id) REFERENCES tbl_user(user_id)"
                    + ")";
            stmt.execute(sqlReading);

            String sqlBill = "CREATE TABLE IF NOT EXISTS tbl_bill ("
                    + "bill_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER NOT NULL,"         
                    + "reading_id INTEGER NOT NULL,"
                    + "billing_month TEXT NOT NULL,"
                    + "amount_due DOUBLE NOT NULL,"      
                    + "due_date DATE NOT NULL,"
                    + "status TEXT NOT NULL DEFAULT 'Unpaid'," 
                    + "FOREIGN KEY (user_id) REFERENCES tbl_user(user_id)," 
                    + "FOREIGN KEY (reading_id) REFERENCES tbl_meter_reading(reading_id)"
                    + ")";
            stmt.execute(sqlBill);
            
            String sqlPayment = "CREATE TABLE IF NOT EXISTS tbl_payment ("
                    + "payment_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "bill_id INTEGER NOT NULL,"
                    + "amount_paid DOUBLE NOT NULL,"      
                    + "payment_method TEXT NOT NULL,"
                    + "payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (bill_id) REFERENCES tbl_bill(bill_id)"
                    + ")";
            stmt.execute(sqlPayment);
        }
    }

    private void setPreparedStatementValues(PreparedStatement pstmt, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            pstmt.setObject(i + 1, values[i]); 
        }
    }
    
    public void addRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

public int addRecordAndGetId(String sql, Object... values) {
    int generatedId = -1;
    try (Connection conn = this.connectDB(); 
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        setPreparedStatementValues(pstmt, values);
        pstmt.executeUpdate();

        try (ResultSet rs = pstmt.getGeneratedKeys()) {
            if (rs.next()) {
                generatedId = rs.getInt(1); 
            }
        }
    } catch (SQLException e) {
        System.out.println("Error adding record and getting ID: " + e.getMessage());
    }
    return generatedId;
}
    
    public void updateRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setPreparedStatementValues(pstmt, values);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

public int deleteRecord(String sql, Object... values) {
    int rowsAffected = 0;
    try (Connection conn = this.connectDB();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        setPreparedStatementValues(pstmt, values); 

        rowsAffected = pstmt.executeUpdate();
        
        if (rowsAffected > 0) {
            System.out.println("Record deleted successfully!");
        } else {
             System.out.println("No record found to delete.");
        }
    } catch (SQLException e) {
        System.out.println("Error deleting record: " + e.getMessage());
    }
    return rowsAffected;
}
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames, Object... values) {
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) { 

            setPreparedStatementValues(pstmt, values); 
            
            try (ResultSet rs = pstmt.executeQuery()) { 
                
                StringBuilder headerLine = new StringBuilder();
                headerLine.append("------------------------------------------------------------------------------------------------------------------\n| ");
                for (String header : columnHeaders) {
                    headerLine.append(String.format("%-20s | ", header));
                }
                headerLine.append("\n------------------------------------------------------------------------------------------------------------------");

                System.out.println(headerLine.toString());

                boolean foundRecords = false;
                while (rs.next()) {
                    foundRecords = true;
                    StringBuilder row = new StringBuilder("| ");
                    for (String colName : columnNames) {
                        String value = rs.getString(colName);
                        row.append(String.format("%-20s | ", value != null ? value : "")); 
                    }
                    System.out.println(row.toString());
                }
                System.out.println("------------------------------------------------------------------------------------------------------------------");

                if (!foundRecords) {
                     System.out.println("| No records found.                                                                                              |");
                     System.out.println("------------------------------------------------------------------------------------------------------------------");
                }
                
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            setPreparedStatementValues(pstmt, values);

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i)); 
                    }
                    records.add(row);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }
        return records;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("Error hashing password: " + e.getMessage());
            return null;
        }
    }
}