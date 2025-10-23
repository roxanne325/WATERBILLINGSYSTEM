
package Config;

import java.sql.*;
import java.util.*;

public class config {

    //=================================================
    // DATABASE CONNECTION
    //=================================================
    public static Connection connectDB() {
        Connection con = null;
        try {
            Class.forName("org.sqlite.JDBC"); // Load the SQLite JDBC driver
            con = DriverManager.getConnection("jdbc:sqlite:WaterbillingDB.db"); // Connect to SQLite DB
            // System.out.println("Connection Successful");
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
        }
        return con;
    }

    //=================================================
    // ADD RECORD METHOD
    //=================================================
    public void addRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) pstmt.setInt(i + 1, (Integer) values[i]);
                else if (values[i] instanceof Double) pstmt.setDouble(i + 1, (Double) values[i]);
                else if (values[i] instanceof Float) pstmt.setFloat(i + 1, (Float) values[i]);
                else if (values[i] instanceof Long) pstmt.setLong(i + 1, (Long) values[i]);
                else if (values[i] instanceof Boolean) pstmt.setBoolean(i + 1, (Boolean) values[i]);
                else if (values[i] instanceof java.util.Date)
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                else if (values[i] instanceof java.sql.Date) pstmt.setDate(i + 1, (java.sql.Date) values[i]);
                else if (values[i] instanceof java.sql.Timestamp)
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
                else pstmt.setString(i + 1, values[i].toString());
            }

            pstmt.executeUpdate();
            System.out.println("Record added successfully!");

        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    //=================================================
    // VIEW RECORDS METHOD (Dynamic)
    //=================================================
    public void viewRecords(String sqlQuery, String[] columnHeaders, String[] columnNames) {
        if (columnHeaders.length != columnNames.length) {
            System.out.println("Error: Mismatch between column headers and column names.");
            return;
        }

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
             ResultSet rs = pstmt.executeQuery()) {

            StringBuilder headerLine = new StringBuilder();
            headerLine.append("--------------------------------------------------------------------------------\n| ");
            for (String header : columnHeaders) {
                headerLine.append(String.format("%-20s | ", header));
            }
            headerLine.append("\n--------------------------------------------------------------------------------");
            System.out.println(headerLine.toString());

            while (rs.next()) {
                StringBuilder row = new StringBuilder("| ");
                for (String colName : columnNames) {
                    String value = rs.getString(colName);
                    row.append(String.format("%-20s | ", value != null ? value : ""));
                }
                System.out.println(row.toString());
            }
            System.out.println("--------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error retrieving records: " + e.getMessage());
        }
    }

    //=================================================
    // UPDATE RECORD METHOD
    //=================================================
    public void updateRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) pstmt.setInt(i + 1, (Integer) values[i]);
                else if (values[i] instanceof Double) pstmt.setDouble(i + 1, (Double) values[i]);
                else if (values[i] instanceof Float) pstmt.setFloat(i + 1, (Float) values[i]);
                else if (values[i] instanceof Long) pstmt.setLong(i + 1, (Long) values[i]);
                else if (values[i] instanceof Boolean) pstmt.setBoolean(i + 1, (Boolean) values[i]);
                else if (values[i] instanceof java.util.Date)
                    pstmt.setDate(i + 1, new java.sql.Date(((java.util.Date) values[i]).getTime()));
                else if (values[i] instanceof java.sql.Date) pstmt.setDate(i + 1, (java.sql.Date) values[i]);
                else if (values[i] instanceof java.sql.Timestamp)
                    pstmt.setTimestamp(i + 1, (java.sql.Timestamp) values[i]);
                else pstmt.setString(i + 1, values[i].toString());
            }

            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");

        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

    //=================================================
    // DELETE RECORD METHOD
    //=================================================
    public void deleteRecord(String sql, Object... values) {
        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                if (values[i] instanceof Integer) pstmt.setInt(i + 1, (Integer) values[i]);
                else pstmt.setString(i + 1, values[i].toString());
            }

            pstmt.executeUpdate();
            System.out.println("Record deleted successfully!");

        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    //=================================================
    // FETCH RECORDS METHOD (Return List<Map>)
    //=================================================
    public List<Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
        List<Map<String, Object>> records = new ArrayList<>();

        try (Connection conn = this.connectDB();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }

            ResultSet rs = pstmt.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                records.add(row);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }

        return records;
    }
}
