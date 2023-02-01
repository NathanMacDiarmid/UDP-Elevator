package Iteration1;

public class Elevator implements Runnable {
    private int currentFloor = 0;
    private Scheduler scheduler;

    /**
     * Default constructor for Chef
     * @param table the Table that is used as the middle man (Box class)
     * @param ingredient the ingredient that the Chef supplies to finish the sandwhich
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    /**
     * The run method for the Chef class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        while (true) {
            this.currentFloor = scheduler.get(this.currentFloor);
            System.out.println("Elevator has gotten stuff - " + "The current floor is now " + this.currentFloor);
        }
    }
}
