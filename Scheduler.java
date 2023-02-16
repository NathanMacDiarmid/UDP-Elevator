

import java.util.*;

public class Scheduler {
    private boolean elevatorAvailabile;
    private int currentFloor;
    private boolean passengerPickedUp;
    private ArrayList<InputData> elevatorQueue;

    public Scheduler() {
        this.elevatorAvailabile = true;
        this.elevatorQueue = new ArrayList<InputData>();
        this.currentFloor = 0;
        this.passengerPickedUp = false;
    }

    /**
     * The getter method for the Scheduler class gets the floor from {@link #currentFloor}
     * that were passed from the Elevator and goes to the next floor
     * @param currentFloor the floor the Elevator is currently at
     * @return the next floor the elevator will stop at
     */
    public synchronized int getFloorRequest(int currentFloor) {
    //public synchronized int getFloorRequest(Elevator currentElevator) {
        while (elevatorAvailabile) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        //System.out.println("Scheduler: Elevator has been notified to pick someone up on floor: " + this.initialFloor);
        if (currentFloor == elevatorQueue.get(0).getFloor()) {
            passengerPickedUp = true;
        }
        if (currentFloor == elevatorQueue.get(0).getCarRequest() && passengerPickedUp) {
            elevatorAvailabile = true;
            this.currentFloor = currentFloor;
            elevatorQueue.remove(0);
            passengerPickedUp = false;
            notifyAll(); 
            return currentFloor;
        } else if (currentFloor < elevatorQueue.get(0).getCarRequest() && passengerPickedUp) {
            elevatorAvailabile = false;
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor + 1;
        } else if (currentFloor > elevatorQueue.get(0).getCarRequest() && passengerPickedUp) {
            elevatorAvailabile = false;
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor - 1;
        } else if (currentFloor < elevatorQueue.get(0).getFloor()) {
            elevatorAvailabile = false;
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor + 1;
        } else {
            elevatorAvailabile = false;
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor - 1;
        }
    }


    /**
     * The putter method for the Scheduler class puts the floor reqeuests that were
     * passed from Floor into {@link Scheduler#currentFloor} and 
     * {@link Scheduler#nextFloor} respectfully
     * @param elevatorQueue the first InputData instance in the list of
     * commands that were passed from Floor that hold the current time,
     * floor the elevator was requested on, floor the elevator goes to,
     * and whether the elevator is going up or down.
     */
    public synchronized void putFloorRequest(InputData elevatorIntruction) {
        while(!elevatorAvailabile) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.elevatorQueue.add(elevatorIntruction);
        elevatorAvailabile = false;
        notifyAll();
    }
    

     /**
     * The two following methods are ONLY FOR TESTING PURPOSES and
     * should not be included in commercial product.
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getNextFloor() {
        return currentFloor;
    }
}


