package Iteration1;

public class Elevator implements Runnable {
    private int currentFloor = 0;
    private Scheduler scheduler;

    /**
     * Default constructor for Elevator
     * @param scheduler the Scheduler instance that needs to be passed (Box class)
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    /**
     * The run method for the Elevator class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        while (true) {
            this.currentFloor = scheduler.get(this.currentFloor);
            System.out.println("Elevator has dropped passenger off on floor - " + this.currentFloor + "\n");
        }
    }
}

