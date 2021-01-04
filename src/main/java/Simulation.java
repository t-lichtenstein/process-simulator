import models.Patient;
import models.Pharmacy;
import models.Prescription;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Simulation extends Thread {

    Patient patient;

    public Simulation(Patient patient) {
        this.patient = patient;
    }

    public static void waitSecondsUpTo(int seconds) {
        long waitingTime = ThreadLocalRandom.current().nextLong(0, seconds * 1000);
        try {
            Thread.sleep(waitingTime);
        } catch (InterruptedException e) {
            System.out.println("Thread " + currentThread().getId() + " was interrupted");
        }
    }

    public static boolean decide(double threshold) {
        double decision = ThreadLocalRandom.current().nextDouble(0, 1);
        return decision <= threshold;
    }

    public void run() {

        List<Pharmacy> activePharmacies = new ArrayList<>();
        List<Prescription> activePrescriptions = new ArrayList<>();

        try {
            System.out.println("Start Thread " + currentThread().getId());
            waitSecondsUpTo(200);
            this.patient.create();
            this.patient.admissions.forEach(admission -> {
                try {
                    waitSecondsUpTo(10);
                    admission.create();
                    waitSecondsUpTo(4);
                    admission.diagnoses.forEach(diagnosis -> {
                        try {
                            waitSecondsUpTo(6);
                            diagnosis.create();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    waitSecondsUpTo(9);
                    activePharmacies.forEach(pharmacy -> {
                        if (Simulation.decide(0.1)) {
                            try {
                                waitSecondsUpTo(6);
                                pharmacy.update();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    waitSecondsUpTo(4);
                    activePrescriptions.forEach(prescription -> {
                        if (Simulation.decide(0.2)) {
                            try {
                                waitSecondsUpTo(6);
                                prescription.update();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else if (Simulation.decide(0.2)) {
                            try {
                                waitSecondsUpTo(6);
                                prescription.delete();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    waitSecondsUpTo(10);
                    admission.pharmacies.forEach(pharmacy -> {
                        try {
                            pharmacy.create();
                            activePharmacies.add(pharmacy);
                            pharmacy.prescriptions.forEach(prescription -> {
                                waitSecondsUpTo(2);
                                try {
                                    prescription.create();
                                    activePrescriptions.add(prescription);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("End Thread " + currentThread().getId());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Thread " + currentThread().getId() + " stopped");
        }
    }
}
