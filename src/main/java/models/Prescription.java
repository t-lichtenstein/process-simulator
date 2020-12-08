package models;

import database.OracleConnector;

import java.sql.*;

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
            String SQL = "INSERT INTO drugs(id, pharmacy_id, name, dose_amount, dose_unit) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            this.id = getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setInt(2, this.pharmacy.id);
            pstmt.setString(3, this.name);
            pstmt.setString(4, this.dose_amount);
            pstmt.setString(5, this.dose_unit);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created pharmacy " + id);
            } else {
                System.out.println("An error occurred creating pharmacy " + id);
            }
        } finally {
            con.close();
            Prescription.indexLock.unlock();
        }
    }

    @Override
    public void delete() throws SQLException {
        String SQL = "DELETE FROM drugs WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        con.close();
        if (affectedRows > 0) {
            System.out.println("Deleted pharmacy " + id + " for pharmacy " + pharmacy.id);
        } else {
            System.out.println("An error occurred deleting pharmacy " + id);
        }
    }

    @Override
    public void update() throws SQLException {
        String SQL = "UPDATE drugs SET dose_amount = ? WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, this.dose_amount);
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        con.close();
        if (affectedRows > 0) {
            System.out.println("Updated pharmacy " + id);
        } else {
            System.out.println("An error occurred updating pharmacy " + id);
        }
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String createTableSQL = "CREATE TABLE drugs (id INTEGER NOT NULL, pharmacy_id INTEGER NULL, name VARCHAR(255), dose_amount VARCHAR(32), dose_unit VARCHAR(32), PRIMARY KEY ( id ), CONSTRAINT fk_drugs_pharmacy FOREIGN KEY(pharmacy_id) REFERENCES pharmacy(id))";
        stmt.executeUpdate(createTableSQL);
        System.out.println("Created Drugs table");
    }

    public static void deleteTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String deleteTableSQL = "DROP TABLE drugs PURGE";
        try {
            stmt.executeUpdate(deleteTableSQL);
            System.out.println("Dropped Drugs table");
        } catch (SQLSyntaxErrorException e) {
            System.out.println("Could not delete Drugs");
        }
    }
}
