import database.OracleConnector;
import enums.Gender;
import models.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;
import java.util.*;

public class ProcessSimulator {

    public static final String dataPath = "/.../mimic_data/";

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
                continue;
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
                continue;
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
                continue;
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
                continue;
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
