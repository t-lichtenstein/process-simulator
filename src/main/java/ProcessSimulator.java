import enums.Gender;
import models.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

public class ProcessSimulator {

    private Connection con;

    public ProcessSimulator(Connection con) {
        this.con = con;
    }

    public static void createTables(Connection con) throws SQLException {
        Subject.createTable(con);
        Admission.createTable(con);
        Diagnosis.createTable(con);
        Pharmacy.createTable(con);
        Drug.createTable(con);
    }

    public static void dropTables(Connection con) throws SQLException {
        Drug.deleteTable(con);
        Pharmacy.deleteTable(con);
        Diagnosis.deleteTable(con);
        Admission.deleteTable(con);
        Subject.deleteTable(con);
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

    static class PharmacyMeta {
        Pharmacy pharmacy;
        Set<Drug> drugs = new HashSet<>();

        public PharmacyMeta(Pharmacy pharmacy) {
            this.pharmacy = pharmacy;
        }
    }

    static class AdmissionMeta {
        Admission admission;
        Set<Diagnosis> diagnoses = new HashSet();
        Map<Integer, PharmacyMeta> pharmacies = new HashMap();

        public AdmissionMeta(Admission admission) {
            this.admission = admission;
        }
    }

    static class SubjectMeta {
        Subject subject;
        Map<Integer, AdmissionMeta> admissions = new HashMap();

        public SubjectMeta(Subject subject) {
            this.subject = subject;
        }
    }


    public static void main(String[] args) {

        String dbIp = "192.168.56.101";
        String dbPort = "1521";
        String dbUser = "system";
        String dbPassword = "oracle";


        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("Connect to database");
            Connection con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@" + dbIp + ":" + dbPort + "/orcl",dbUser,dbPassword);
            System.out.println("Connected");
            ProcessSimulator.reset(con);

            // subject_id
            Map<Integer, SubjectMeta> subjects = new HashMap();

            BufferedReader csvReader = new BufferedReader(new FileReader("/home/.../Desktop/patient_data.csv"));
            String row;
            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                String[] data = row.split(",");
                int subjectId = Integer.parseInt(data[0]);
                Gender gender = data[1].equals("F") ? Gender.F : Gender.M;
                int admissionId = Integer.parseInt(data[3]);
                String icdCode = data[7].replaceAll("\"", "");
                int pharmacyId = Integer.parseInt(data[9]);
                String frequency = data[10].replaceAll("\"", "");
                String drugName = data[12].replaceAll("\"", "");
                String drugVal = data[13].replaceAll("\"", "");
                String drugUnit = data[14].replaceAll("\"", "");


                subjects.putIfAbsent(subjectId, new SubjectMeta(new Subject(gender)));

                SubjectMeta subjectMeta = subjects.get(subjectId);

                subjectMeta.admissions.putIfAbsent(admissionId, new AdmissionMeta(new Admission(subjectMeta.subject)));

                AdmissionMeta admissionMeta = subjectMeta.admissions.get(admissionId);

                admissionMeta.diagnoses.add(new Diagnosis(admissionMeta.admission, icdCode));
                admissionMeta.pharmacies.putIfAbsent(pharmacyId, new PharmacyMeta(new Pharmacy(admissionMeta.admission, frequency)));
                PharmacyMeta pharmacyMeta = admissionMeta.pharmacies.get(pharmacyId);
                pharmacyMeta.drugs.add(new Drug(pharmacyMeta.pharmacy, drugName, drugVal, drugUnit));
            }
            csvReader.close();

            List<Simulation> simulations = new ArrayList<>();

            Long startTime = System.currentTimeMillis();

            subjects.values().forEach(subjectMeta -> {
                Simulation simulation = new Simulation(con, subjectMeta);
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
            con.close();

            System.out.println("Finished simulation in " + ((System.currentTimeMillis() - startTime) / 1000) + " s");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
