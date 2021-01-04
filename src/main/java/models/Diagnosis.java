package models;

import database.OracleConnector;

import java.sql.*;

public class Diagnosis extends Model {

    static int globalIndex = 0;

    static int getIndex() {
        Diagnosis.globalIndex = Diagnosis.globalIndex + 1;
        return Diagnosis.globalIndex;
    }

    public int id;
    public String icdCode;
    public Admission admission;

    public Diagnosis(Admission admission, String icdCode) {
        this.admission = admission;
        this.icdCode = icdCode;
    }

    @Override
    public void create() throws SQLException {
        Connection con = OracleConnector.getConnection();
        Prescription.indexLock.lock();
        try {
            String SQL = "INSERT INTO diagnoses(id, admission_id, icd_code) VALUES(?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            this.id = getIndex();
            pstmt.setInt(1, this.id);
            pstmt.setInt(2, this.admission.id);
            pstmt.setString(3, this.icdCode);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Created diagnosis " + id + " for admission " + admission.id);
            } else {
                System.out.println("An error occurred creating diagnosis " + id);
            }
        } finally {
            con.close();
            Diagnosis.indexLock.unlock();
        }
    }

    @Override
    public void delete() throws SQLException {
        String SQL = "DELETE FROM diagnoses WHERE id = ?";
        Connection con = OracleConnector.getConnection();
        PreparedStatement pstmt = con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, this.id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted diagnosis " + id);
        } else {
            System.out.println("An error occurred deleting diagnosis " + id);
        }
        con.close();
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String createTableSQL = "CREATE TABLE diagnoses (id INTEGER NOT NULL, admission_id INTEGER NULL, icd_code VARCHAR(64), PRIMARY KEY ( id ), CONSTRAINT fk_diagnoses_admissions FOREIGN KEY(admission_id) REFERENCES admissions(id))";
        stmt.executeUpdate(createTableSQL);
        System.out.println("Created Diagnoses table");
    }

    public static void deleteTable(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        String deleteTableSQL = "DROP TABLE diagnoses PURGE";
        try {
            stmt.executeUpdate(deleteTableSQL);
            System.out.println("Dropped Diagnoses table");
        } catch (SQLSyntaxErrorException e) {
            System.out.println("Could not delete Diagnoses");
        }
    }
}
