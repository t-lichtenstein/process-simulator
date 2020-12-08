import database.OracleConnector;
import enums.Gender;
import models.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

public class ProcessSimulator {

    public static final String dataPath = "/.../mimic_data/";

    private Connection con;

    public ProcessSimulator(Connection con) {
        this.con = con;
    }

    public static void createTables(Connection con) throws SQLException {
        Patient.createTable(con);
        Admission.createTable(con);
        Diagnosis.createTable(con);
        Pharmacy.createTable(con);
        Prescription.createTable(con);
    }

    public static void dropTables(Connection con) throws SQLException {
        Prescription.deleteTable(con);
        Pharmacy.deleteTable(con);
        Diagnosis.deleteTable(con);
        Admission.deleteTable(con);
        Patient.deleteTable(con);
    }

    /*
    public void updateInvoice(int id) throws SQLException {
        String SQL = "UPDATE INVOICES SET state = 'sent' WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated invoice " + id + " state to 'sent'");
        } else {
            System.out.println("An error occurred updating invoice " + id);
        }
    }

    public void updateOrder(int id, OrderState state) throws SQLException {
        String stateValue = "";
        switch(state) {
            case CREATED: stateValue = "created"; break;
            case CHECKING: stateValue = "checking"; break;
            case CONFIRMED: stateValue = "confirmed"; break;
            case PAID: stateValue = "paid"; break;
            case FINISHED: stateValue = "finished"; break;
        }
        if (stateValue.equals("")) {
            System.out.println("Order state change to invalid state");
            return;
        }

        String SQL = "UPDATE ORDERS SET state = ? WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setString(1, stateValue);
        pstmt.setInt(2, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Updated order " + id + " state to '" + stateValue + "'");
        } else {
            System.out.println("An error occurred updating order " + id);
        }
    }

    public void addInvoice(int id, int orderId) throws SQLException {
        String SQL = "INSERT INTO INVOICES(id, order_id, state) VALUES(?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        pstmt.setInt(2, orderId);
        pstmt.setString(3, "created");
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Created invoice " + id);
        } else {
            System.out.println("An error occurred creating invoice " + id);
        }
    }

    public void addOrder(int id, int userId) throws SQLException {
        String SQL = "INSERT INTO ORDERS(id, user_id, state) VALUES(?, ?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        pstmt.setInt(2, userId);
        pstmt.setString(3, "created");
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Created order " + id);
        } else {
            System.out.println("An error occurred creating order " + id);
        }
    }

    public void addUser(int id, String name) throws SQLException {
        String SQL = "INSERT INTO USERS(id, name) VALUES(?, ?)";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        pstmt.setString(2, name);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Created user " + id);
        } else {
            System.out.println("An error occurred creating user " + id);
        }
    }

    public void deleteOrder(int id) throws SQLException {
        String SQL = "DELETE FROM ORDERS WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted order " + id);
        } else {
            System.out.println("An error occurred deleting order " + id);
        }
    }

    public void deleteInvoice(int id) throws SQLException {
        String SQL = "DELETE FROM INVOICES WHERE id = ?";
        PreparedStatement pstmt = this.con.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, id);
        int affectedRows = pstmt.executeUpdate();
        if (affectedRows > 0) {
            System.out.println("Deleted invoice " + id);
        } else {
            System.out.println("An error occurred deleting invoice " + id);
        }
    }
     */

    public static void reset(Connection con) throws SQLException {
        ProcessSimulator.dropTables(con);
        ProcessSimulator.createTables(con);
    }

    public static void importData(Map<Integer, Patient> patients) throws Exception {
        Map<Integer, Admission> admissions = new HashMap();
        Map<Integer, Pharmacy> pharmacies = new HashMap();
        BufferedReader csvReader;
        String row;

        System.out.println("\nStart data import");

        // Read patients
        System.out.print("Importing patients... ");
        csvReader = new BufferedReader(new FileReader(ProcessSimulator.dataPath + "patients.csv"));
        csvReader.readLine();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            int patientId = Integer.parseInt(data[0]);
            Gender gender = data[1].equals("F") ? Gender.F : Gender.M;

            Patient patient = new Patient(gender);
            patients.put(patientId, patient);
        }
        csvReader.close();
        System.out.println("Done!");

        // Read admissions
        System.out.print("Importing admissions... ");
        csvReader = new BufferedReader(new FileReader(ProcessSimulator.dataPath + "admissions.csv"));
        csvReader.readLine();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            int patientId = Integer.parseInt(data[0]);
            int admissionId = Integer.parseInt(data[1]);

            Patient patient = patients.get(patientId);
            if (patient == null) {
                throw new Exception("Patient " + patientId + " does not exist");
            }

            Admission admission = new Admission(patient);
            patient.addAdmission(admission);
            admissions.put(admissionId, admission);
        }
        csvReader.close();
        System.out.println("Done!");

        // Read pharmacy
        System.out.print("Importing pharmacy... ");
        csvReader = new BufferedReader(new FileReader(ProcessSimulator.dataPath + "pharmacy.csv"));
        csvReader.readLine();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            int admissionId = Integer.parseInt(data[0]);
            int pharmacyId = Integer.parseInt(data[1]);
            String frequency = data[2];

            Admission admission = admissions.get(admissionId);
            if (admission == null) {
                throw new Exception("Admission " + admissionId + " does not exist");
            }

            Pharmacy pharmacy = new Pharmacy(admission, frequency);
            admission.addPharmacy(pharmacy);
            pharmacies.put(pharmacyId, pharmacy);
        }
        csvReader.close();
        System.out.println("Done!");

        // Read diagnoses
        System.out.print("Importing diagnoses... ");
        csvReader = new BufferedReader(new FileReader(ProcessSimulator.dataPath + "diagnoses.csv"));
        csvReader.readLine();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            int admissionId = Integer.parseInt(data[0]);
            String icdCode = data[1];

            Admission admission = admissions.get(admissionId);
            if (admission == null) {
                throw new Exception("Admission " + admissionId + " does not exist");
            }

            Diagnosis diagnosis = new Diagnosis(admission, icdCode);
            admission.addDiagnosis(diagnosis);
        }
        csvReader.close();
        System.out.println("Done!");

        // Read prescriptions
        System.out.print("Importing prescriptions... ");
        csvReader = new BufferedReader(new FileReader(ProcessSimulator.dataPath + "prescriptions.csv"));
        csvReader.readLine();
        while ((row = csvReader.readLine()) != null) {
            String[] data = row.split(",");
            int pharmacyId = Integer.parseInt(data[0]);
            String name = data[1];
            String dose_amount = data[2];
            String dose_unit = data[3];
            Pharmacy pharmacy = pharmacies.get(pharmacyId);
            if (pharmacy == null) {
                throw new Exception("Admission " + pharmacyId + " does not exist");
            }

            Prescription prescription = new Prescription(pharmacy, name, dose_amount, dose_unit);
            pharmacy.addPrescription(prescription);
        }
        csvReader.close();
        System.out.println("Done!");
        System.out.println("\nFinished data import");
    }

    public static void main(String[] args) {

        try{
            ProcessSimulator.reset(OracleConnector.getConnection());

            Map<Integer, Patient> patients = new HashMap();
            ProcessSimulator.importData(patients);

            List<Simulation> simulations = new ArrayList<>();

            Long startTime = System.currentTimeMillis();

            patients.values().forEach(subjectMeta -> {
                Simulation simulation = new Simulation(subjectMeta);
                simulations.add(simulation);
                simulation.start();
            });

            simulations.forEach(simulation -> {
                try {
                    simulation.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Finished simulation in " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
