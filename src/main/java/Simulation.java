import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

public class Simulation extends Thread {

    ProcessSimulator.SubjectMeta subjectMeta;

    public Simulation(ProcessSimulator.SubjectMeta meta) {
        this.subjectMeta = meta;
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
            waitSecondsUpTo(60);
            subjectMeta.subject.create();
            subjectMeta.admissions.values().forEach(admissionMeta -> {
                try {
                    waitSecondsUpTo(20);
                    admissionMeta.admission.create();
                    waitSecondsUpTo(40);
                    admissionMeta.diagnoses.stream().forEach(diagnosis -> {
                        try {
                            waitSecondsUpTo(10);
                            diagnosis.create();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    });
                    waitSecondsUpTo(20);
                    admissionMeta.pharmacies.values().forEach(pharmacyMeta -> {
                        try {
                            pharmacyMeta.pharmacy.create();
                            pharmacyMeta.drugs.stream().forEach(drug -> {
                                waitSecondsUpTo(10);
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
