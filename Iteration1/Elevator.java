package Iteration1;

import java.util.ArrayList;

public class Elevator implements Runnable {
    private int floor;
    private Scheduler scheduler;

    /**
     * Default constructor for Chef
     * @param table the Table that is used as the middle man (Box class)
     * @param ingredient the ingredient that the Chef supplies to finish the sandwhich
     */
    public Elevator(Scheduler scheduler, int floor) {
        this.scheduler = scheduler;
        this.floor = floor;
    }

    @Override
    /**
     * The run method for the Chef class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        while (true) {
            ArrayList<Integer> ingredients = scheduler.get(this.floor);
            System.out.println("I made a sandwhich with given ingredients: " + ingredients + " and my " + this.floor);
        }
    }
}
