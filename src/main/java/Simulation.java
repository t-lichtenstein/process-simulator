import models.Patient;

import java.sql.SQLException;
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
        try {
            System.out.println("Start Thread " + currentThread().getId());
            waitSecondsUpTo(300);
            this.patient.create();
            this.patient.admissions.forEach(admission -> {
                try {
                    waitSecondsUpTo(100);
                    admission.create();
                    waitSecondsUpTo(30);
                    admission.diagnoses.forEach(diagnosis -> {
                        try {
                            waitSecondsUpTo(20);
                            diagnosis.create();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    waitSecondsUpTo(30);
                    admission.pharmacies.forEach(pharmacy -> {
                        try {
                            pharmacy.create();
                            pharmacy.prescriptions.forEach(drug -> {
                                waitSecondsUpTo(20);
                                try {
                                    drug.create();
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
