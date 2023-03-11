
/**
 * @author Nathan MacDiarmid 101098993
 */
public class Main {
    public static void main(String[] args) {

        Thread floor, elevator1;
        Scheduler scheduler = new Scheduler();

        floor = new Thread(new Floor(scheduler), "floor");
        elevator1 = new Thread(new Elevator(scheduler), "elevator1");

        floor.start();
        elevator1.start();
    }
}

