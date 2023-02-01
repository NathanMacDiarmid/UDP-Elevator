package Iteration1;

import java.util.ArrayList;
import java.lang.Math;

public class Scheduler {
    private boolean elevatorAvailabile = true;
    private int currentFloor = 0;
    private int nextFloor = 0;

    /**
     * The getter method for the Table class gets items from {@link #ingredients}
     * that were passed from the Agent and gives them to the 
     * correct Chef (Jam, Bread, or Peanut Butter)
     * @param ingredient the ingredient that the Chef supplies
     * (1 - bread, 2 - jam, 3 - peanut butter)
     * @return the ingredients that the Agent passed and stored on the Table
     */
    public synchronized int get(int currentFloor) {
        while (elevatorAvailabile) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        /*try {
            // Sleeps for 6.2 seconds per floor, multiplies by 1000 to get to miliseconds
            Thread.sleep((long) Math.abs(((elevatorQueue.getFloor() - elevatorQueue.getCarRequest() + floor) * 6.2) * 1000));
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        System.out.println(this.currentFloor);
        System.out.println(this.nextFloor);
        this.currentFloor = currentFloor;
        elevatorAvailabile = true;
        notifyAll();
        return nextFloor;
    }

    /**
     * The putter method for the Table class puts the items that were
     * passed from Agent into {@link #ingredients}, missing one of the
     * three ingredients. Both ingredients are randomly gernerated numbers
     * @param firstIngredient the first ingredient that the Agent supplies
     * @param secondIngredient the second ingredient that the Agent supplies
     */
    public synchronized void put(InputData elevatorQueue) {
        while(!elevatorAvailabile) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.currentFloor = elevatorQueue.getFloor();
        this.nextFloor = elevatorQueue.getCarRequest();
        elevatorAvailabile = false;
        notifyAll();
    }
}
