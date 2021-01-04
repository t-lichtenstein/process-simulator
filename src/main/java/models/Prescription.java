package models;

import database.OracleConnector;

import java.sql.*;
import java.util.Random;

public class Prescription extends Model {

    static int globalIndex = 0;

    static int getIndex() {
        Prescription.globalIndex = Prescription.globalIndex + 1;
        return Prescription.globalIndex;
    }

    public String getName() {
        return this.name;
    }

    int id;
    String name;
    String dose_amount;
    String dose_unit;
    Pharmacy pharmacy;

    public Prescription(Pharmacy pharmacy, String name, String dose_amount, String dose_unit) {
        this.name = name;
        this.dose_amount = dose_amount;
        this.dose_unit = dose_unit;
        this.pharmacy = pharmacy;
    }

    @Override
    public void create() throws SQLException {
        Connection con = OracleConnector.getConnection();
        Prescription.indexLock.lock();
        try {
            String SQL = "INSERT INTO prescriptions(id, pharmacy_id, name, dose_amount, dose_unit) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            this.id = getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setInt(2, this.pharmacy.id);
            pstmt.setString(3, this.name);
            pstmt.setString(4, this.dose_amount);
            pstmt.setString(5, this.dose_unit);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created prescription " + id + " for pharmacy " + this.pharmacy.id);
            } else {
                System.out.println("An error occurred creating prescription " + id);
            }
        } finally {
            con.close();
            Prescription.indexLock.unlock();
        }
    }

    @Override
    public void delete() throws SQLException {
        String SQL = "DELETE FROM prescriptions WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        con.close();
        if (affectedRows > 0) {
            System.out.println("Deleted prescription " + id + " for pharmacy " + pharmacy.id);
        } else {
            System.out.println("An error occurred deleting prescription " + id);
        }
    }

    public void update() throws SQLException {
        String SQL = "UPDATE prescriptions SET dose_amount = ? WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);

        Double amount = null;
        try {
         amount = Double.parseDouble(this.dose_amount.replace("\"", ""));
        } catch (Exception e) {

        }
        String newAmount = "0";
        if (amount != null) {
          Random r = new Random();
          newAmount = Double.toString((amount * ((r.nextInt(100) + 50) / 100)));
        }

        pstmt.setString(1, newAmount);
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        con.close();
        if (affectedRows > 0) {
            System.out.println("Updated prescription " + id);
        } else {
            System.out.println("An error occurred updating pharmacy " + id);
        }
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String createTableSQL = "CREATE TABLE prescriptions (id INTEGER NOT NULL, pharmacy_id INTEGER NULL, name VARCHAR(255), dose_amount VARCHAR(32), dose_unit VARCHAR(32), PRIMARY KEY ( id ), CONSTRAINT fk_prescriptions_pharmacy FOREIGN KEY(pharmacy_id) REFERENCES pharmacy(id))";
        stmt.executeUpdate(createTableSQL);
        System.out.println("Created Prescriptions table");
    }

    public static void deleteTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String deleteTableSQL = "DROP TABLE prescriptions PURGE";
        try {
            stmt.executeUpdate(deleteTableSQL);
            System.out.println("Dropped Prescriptions table");
        } catch (SQLSyntaxErrorException e) {
            System.out.println("Could not delete Prescriptions");
        }
    }
}
