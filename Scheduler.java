

import java.util.*;

public class Scheduler {
    private boolean queueInUse;
    private int currentFloor;
    private boolean passengerPickedUp;
    private ArrayList<InputData> elevatorQueue;

    public Scheduler() {
        this.queueInUse = true;
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
        while (queueInUse) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }

        //System.out.println("Scheduler: Elevator has been notified to pick someone up on floor: " + this.initialFloor);

        // This checks if the passenger is picked up
        if (currentFloor == elevatorQueue.get(0).getFloor()) {
            passengerPickedUp = true;
        }
        // This checks if elevator has arrived on the destination floor
        if (currentFloor == elevatorQueue.get(0).getCarRequest() && passengerPickedUp) {
            queueInUse = true;
            this.currentFloor = currentFloor;
            elevatorQueue.remove(0);
            passengerPickedUp = false;
            notifyAll(); 
            return currentFloor;
        // This checks if the elevators current floor is lower than its destination floor, if so, it goes up one floor  
        // Also checks if the passenger has already been picked up (go immediately to destination floor)  
        } else if (currentFloor < elevatorQueue.get(0).getCarRequest() && passengerPickedUp) {
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor + 1;
        // This checks if the elevators current floor is higher than its destination floor, if so, it goes down one floor
        // Also checks if the passenger has already been picked up (go immediately to destination floor)
        } else if (currentFloor > elevatorQueue.get(0).getCarRequest() && passengerPickedUp) {
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor - 1;
        // This checks if the elevator current floor is lower than the floor the elevator was requested from, if so, it goes up one floor
        // This implies that the passenger was not picked up
        } else if (currentFloor < elevatorQueue.get(0).getFloor()) {
            this.currentFloor = currentFloor;
            notifyAll(); 
            return currentFloor + 1;
        // This is the default, but it checks if the current floor is higher than the floor the elevator was requested from, if so, it goes down one floor
        // This also implies that the passenger has not been picked up
        } else {
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
    public synchronized void putFloorRequest(InputData elevatorIntstruction) {
        while(!queueInUse) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.elevatorQueue.add(elevatorIntstruction);
        queueInUse = false;
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


