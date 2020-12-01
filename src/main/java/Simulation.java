import java.sql.Connection;
import java.util.concurrent.ThreadLocalRandom;

public class Simulation extends Thread {

    ProcessSimulator.SubjectMeta subjectMeta;
    Connection con;

    public Simulation(Connection con, ProcessSimulator.SubjectMeta meta) {
        this.subjectMeta = meta;
        this.con = con;
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
            subjectMeta.subject.create(this.con);
            System.out.println("End Thread " + currentThread().getId());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Thread " + currentThread().getId() + " stopped");
        }
    }
}
