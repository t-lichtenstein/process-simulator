package models;

import enums.Gender;

import java.sql.*;

public class Subject extends Model {

    static int globalIndex = 0;

    static int getIndex() {
        Subject.globalIndex = Subject.globalIndex + 1;
        return Subject.globalIndex;
    }

    public int id;
    public Gender gender;

    public Subject(Gender gender) {
        this.gender = gender;
    }

    @Override
    public void create(Connection con) throws SQLException {
        String SQL = "INSERT INTO subjects(id, gender) VALUES(?, ?)";
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        Subject.indexLock.lock();
        try {
            this.id = Subject.getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setString(2, this.gender == Gender.F ? "F" : "M");
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created subject " + id);
            } else {
                System.out.println("An error occurred creating subject " + id);
            }
        } finally {
            Subject.indexLock.unlock();
        }
    }

    @Override
    public void delete(Connection con) throws SQLException {
        String SQL = "DELETE FROM subjects WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted subject " + id);
        } else {
            System.out.println("An error occurred deleting subject " + id);
        }
    }

    @Override
    public void update(Connection con) throws SQLException {
        String SQL = "UPDATE subjects SET gender = ? WHERE id = ?";
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, this.gender == Gender.F ? "F" : "M");
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated subject " + id);
        } else {
            System.out.println("An error occurred updating subject " + id);
        }
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
