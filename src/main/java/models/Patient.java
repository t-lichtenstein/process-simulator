package models;

import database.OracleConnector;
import enums.Gender;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Patient extends Model {

    static int globalIndex = 0;

    static int getIndex() {
        Patient.globalIndex = Patient.globalIndex + 1;
        return Patient.globalIndex;
    }

    public int id;
    public Gender gender;
    public List<Admission> admissions;

    public Patient(Gender gender) {
        this.gender = gender;
        this.admissions = new ArrayList<>();
    }

    public void addAdmission(Admission admission) {
        this.admissions.add(admission);
    }

    @Override
    public void create() throws SQLException {
        Connection con = OracleConnector.getConnection();
        Prescription.indexLock.lock();
        try {
            String SQL = "INSERT INTO subjects(id, gender) VALUES(?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            this.id = Patient.getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setString(2, this.gender == Gender.F ? "F" : "M");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created subject " + id);
            } else {
                System.out.println("An error occurred creating subject " + id);
            }
        } finally {
            con.close();
            Patient.indexLock.unlock();
        }
    }

    @Override
    public void delete() throws SQLException {
        String SQL = "DELETE FROM subjects WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted subject " + id);
        } else {
            System.out.println("An error occurred deleting subject " + id);
        }
        con.close();
    }

    public void update() throws SQLException {
        String SQL = "UPDATE subjects SET gender = ? WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, this.gender == Gender.F ? "F" : "M");
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated subject " + id);
        } else {
            System.out.println("An error occurred updating subject " + id);
        }
        con.close();
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String createTableSQL = "CREATE TABLE subjects (id INTEGER NOT NULL, gender VARCHAR(1), PRIMARY KEY ( id ))";
        stmt.executeUpdate(createTableSQL);
        System.out.println("Created Subjects table");
    }

    public static void deleteTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String deleteTableSQL = "DROP TABLE subjects PURGE";
        try {
            stmt.executeUpdate(deleteTableSQL);
            System.out.println("Dropped Subjects table");
        } catch (SQLSyntaxErrorException e) {
            System.out.println("Could not delete Subjects");
        }
    }
}
