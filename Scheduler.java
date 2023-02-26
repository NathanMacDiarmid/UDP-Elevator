
import java.util.*;

public class Scheduler {

    /**
     * queueInUse represents when the elevator queue is being updated. True means it
     * is being updated and added to be the floor, false means it is not being
     * updated
     */
    private boolean queueInUse;

    /* noMoreRequests tracks when the floor subsysytem will send last request */
    private boolean noMoreRequests;

    /** currentFloor tracks where the elevator is */
    private int currentFloor;

    /* elevatorQueue is the queue of requests that are currently in the elevator */
    private ArrayList<InputData> elevatorQueue;

    /* requestQueue used as priority queue of requests */
    private ArrayList<InputData> requestQueue;

    /* floorQueue is to keep track of people waiting for elevator on each floor */
    private Map<Integer, ArrayList<InputData>> floorQueues = new HashMap<Integer, ArrayList<InputData>>() {
        {
            put(1, new ArrayList<InputData>());
            put(2, new ArrayList<InputData>());
            put(3, new ArrayList<InputData>());
            put(4, new ArrayList<InputData>());
            put(5, new ArrayList<InputData>());
            put(6, new ArrayList<InputData>());
            put(7, new ArrayList<InputData>());
        }
    };

    public Scheduler() {
        this.queueInUse = true;
        this.elevatorQueue = new ArrayList<InputData>();
        this.requestQueue = new ArrayList<InputData>();
        this.currentFloor = 0;
        this.noMoreRequests = false;
    }

    public void setQueueInUse(boolean queueInUse) {
        this.queueInUse = queueInUse;
    }

    public boolean isQueueInUse() {
        return queueInUse;
    }

    /**
     * getFloorRequest returns two values, the request sent by the floor subsystem,
     * and whether that request is the last one
     */
    public synchronized Map<InputData, Boolean> getFloorRequest() {
        while (queueInUse) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        InputData requestToGiveElevator = this.requestQueue.get(0); // TODO: maybe change requestQueue to currentRequest
                                                                    // (as a non arrayList) because we are only
                                                                    // accessing first element of this array list
        this.requestQueue.remove(0);
        queueInUse = true;
        notifyAll();

        Map<InputData, Boolean> returnVals = new HashMap<InputData, Boolean>() {
            {
                put(requestToGiveElevator, noMoreRequests);
            }
        };

        return returnVals;

    }

    public int moveElevator(ArrayList<InputData> queue, int currentFloor) {
        boolean reachedDestination = false;
        System.out.println("Scheduler: Floor queues:" + this.floorQueues.toString());

        // if the floor that the elevator is currently on has passengers waiting, pick
        // them up
        if ((currentFloor != 0) && (this.floorQueues.get(currentFloor).size() != 0)) {

            System.out.println("Scheduler: there are people waiting for the elevator on this floor: " + currentFloor
                    + " -> notfiy elevator to open doors ");
            this.elevatorQueue.addAll(this.floorQueues.get(currentFloor)); // this adds all requests to current elevator
            this.floorQueues.get(currentFloor).removeAll(elevatorQueue); // this removes all floor requests from current
                                                                         // floor because passenger(s) have entered
                                                                         // elevator
            updateFloor(currentFloor);
            return currentFloor; // do not move elevator

        }

        /*
         * next if takes care of the situation where the elevator has not picked up ANY
         * passenger(s)
         */
        if (this.elevatorQueue.size() == 0) { // if the elevator has not picked anyone up, go to floor of first request
            System.out.println("Scheduler: Elevator is empty"); // TODO: take this out before handing in

            if ((currentFloor < queue.get(0).getFloor())) { // if elevator is below floor of first requset, move up,
                                                            // else move down
                System.out.println("Scheduler: elevator is below initial floor of first request in queue -> moving up");
                updateFloor(currentFloor + 1);
                return currentFloor + 1; // move elevator up
            } else {
                System.out
                        .println("Scheduler: elevator is above initial floor of first request in queue -> moving down");
                updateFloor(currentFloor - 1);
                return currentFloor - 1; // move elevator down
            }
        }

        else { // else if elevator currently has passenger(s) in it that need to reach their
               // destination floor

            Iterator<InputData> iterator = this.elevatorQueue.iterator(); // go through the requests that are currently
                                                                          // in the elevator and check if current floor
                                                                          // is equal to any of the destination floors
                                                                          // of passenger(s) in the elevator
            while (iterator.hasNext()) {
                InputData currPassenger = iterator.next();
                if (currentFloor == currPassenger.getCarRequest()) {
                    System.out.println(
                            "Scheduler: elevator is at the destination of a passenger in the elevator -> notfiy elevator to open doors");
                    reachedDestination = true;
                    iterator.remove(); // remove from elevator queue because passenger left
                    queue.removeIf(request -> (request == currPassenger)); // remove from general main queue because
                                                                           // passenger left

                }

            }
            if (reachedDestination) {
                updateFloor(currentFloor);
                return currentFloor; // do not move to signal elevator to open/close doors
            }

            if (currentFloor > queue.get(0).getCarRequest()) { // if elevator is above floor of the the destination of
                                                               // the first request, move down, else move up

                System.out.println(
                        "Scheduler: elevator is above destination floor of first request in priority queue -> moving down");
                updateFloor(currentFloor - 1);
                return currentFloor - 1; // move elevator down

            } else {

                System.out.println(
                        "Scheduler: elevator is below destination floor of first request in priority queue -> moving up");
                updateFloor(currentFloor + 1);
                return currentFloor + 1; // move elevator up
            }

        }
    }

    private void updateFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    /**
     * The putter method for the Scheduler class puts the floor reqeuests that were
     * passed from Floor into {@link Scheduler#currentFloor} and
     * {@link Scheduler#nextFloor} respectfully
     * 
     * @param elevatorQueue the first InputData instance in the list of
     *                      commands that were passed from Floor that hold the
     *                      current time,
     *                      floor the elevator was requested on, floor the elevator
     *                      goes to,
     *                      and whether the elevator is going up or down.
     */
    public synchronized void putFloorRequest(InputData elevatorIntstruction, boolean lastRequest) {
        while (!queueInUse) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        this.floorQueues.get(elevatorIntstruction.getFloor()).add(elevatorIntstruction); // adds request to
                                                                                         // corresponding floor queue
        this.requestQueue.add(elevatorIntstruction); // adds request to main request queue
        this.noMoreRequests = lastRequest;

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

    /**
     * Getter method used for testing purposes
     * 
     * @return The request queue
     * @author Michael Kyrollos
     */
    public ArrayList<InputData> getRequestQueue() {
        return requestQueue;
    }

    /**
     * Getter method used for testing purposes
     * 
     * @return The queue for each floor
     * @author Michael Kyrollos
     */
    public Map<Integer, ArrayList<InputData>> getFloorQueues() {
        return floorQueues;
    }

    /**
     * Getter method used for testing purposes
     * 
     * @return True if there are no requests left.
     * @author Michael Kyrollos
     */
    public boolean isNoMoreRequests() {
        return noMoreRequests;
    }

}
