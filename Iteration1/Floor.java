package Iteration1;

import java.util.Random;

public class Floor implements Runnable {
    private Scheduler scheduler;

    /**
     * Default constructor for Agent class
     * @param table the Table that is used as the middle man (Box class)
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    /**
     * The run method for the Agent class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        while (true) {
            // Generates the two random ingredients to supply the table
            
            // Change this to floor input from person
            Random rand = new Random();
            int firstIngredient = rand.nextInt((3 - 1) + 1) + 1;
            int secondIngredient = rand.nextInt((3 - 1) + 1) + 1;
            while (firstIngredient == secondIngredient) {
                secondIngredient = rand.nextInt((3 - 1) + 1) + 1;
            }

            scheduler.put(firstIngredient, secondIngredient);
            System.out.println("Agent just put the ingredient " + firstIngredient + " and second ingredient " + secondIngredient + " on the table");
        }
    }
}
