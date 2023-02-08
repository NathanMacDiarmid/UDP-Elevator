package Iteration1;

public class Scheduler {
    private boolean elevatorAvailabile = true;
    private int currentFloor = 0;
    private int nextFloor = 0;

    /**
     * The getter method for the Scheduler class gets the floor from {@link #currentFloor}
     * that were passed from the Elevator and goes to the next floor
     * @param currentFloor the floor the Elevator is currently at
     * @return the next floor the elevator will stop at
     */
    public synchronized int get(int currentFloor) {
        while (elevatorAvailabile) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Elevator is picking someone up on floor - " + this.currentFloor);
        this.currentFloor = currentFloor;
        elevatorAvailabile = true;
        notifyAll();
        return nextFloor;
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

        /**
     * The two following methods are ONLY FOR TESTING PURPOSES and
     * should not be included in commercial product.
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getNextFloor() {
        return nextFloor;
    }
}

