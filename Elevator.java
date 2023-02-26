
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Elevator implements Runnable {
    private int currentFloor = 0;
    private Scheduler scheduler;

    /* floorButtons represent the buttons inside the elevator */
    private Map<Integer, Boolean> floorButtons = new HashMap<Integer, Boolean>() {
        {
            put(1, false);
            put(2, false);
            put(3, false);
            put(4, false);
            put(5, false);
            put(6, false);
            put(7, false);
        }
    };

    /* floorButtonsLamps represent the lamps on the buttons inside the elevator */
    private Map<Integer, Boolean> floorButtonsLamps = new HashMap<Integer, Boolean>() {
        { // TODO: can we just have one map that represents buttons and their lights
          // (integer, boolean(pressed or not), String("light on"/"light off"))
            put(1, false);
            put(2, false);
            put(3, false);
            put(4, false);
            put(5, false);
            put(6, false);
            put(7, false);
        }
    };

    private Boolean motorMoving;

    private Boolean doorOpen;

    /* requestQueue is used as priority queue of requests */
    private ArrayList<InputData> requestQueue;

    public ArrayList<InputData> getRequestQueue() {
        return requestQueue;
    }

    /**
     * Default constructor for Elevator
     * 
     * @param scheduler the Scheduler instance that needs to be passed (Box class)
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.requestQueue = new ArrayList<InputData>();
    }

    public void setFloorButton(Integer floor, Boolean buttonPressed) {
        floorButtons.replace(floor, buttonPressed);
    }

    public void setFloorButtonLamps(Integer floor, Boolean buttonLampOn) {
        floorButtonsLamps.replace(floor, buttonLampOn);
    }

    public void setMotorMoving(Boolean motorMoving) {
        this.motorMoving = motorMoving;
    }

    public void setDoorOpen(Boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    @Override
    /**
     * The run method for the Elevator class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {

        String currentThreadName = Thread.currentThread().getName();
        Boolean noMoreRequestsComing = false;
        int oldCurrentFloor; // this keeps track of the previous floor visited by elevator
        System.out.println(currentThreadName + ": Current floor is: " + this.currentFloor + "\n");

        while (true) {

            oldCurrentFloor = this.currentFloor;

            if (!noMoreRequestsComing) { // if there are more requests to grab

                Map<InputData, Boolean> request = scheduler.getFloorRequest(); // grab them
                for (InputData r : request.keySet()) {
                    requestQueue.add(r);
                    noMoreRequestsComing = request.get(r);
                }

            }

            if (requestQueue.size() > 0) { // if there are currently requests to service

                this.currentFloor = scheduler.moveElevator(requestQueue, this.currentFloor); // ask scheduler to notify
                                                                                             // elevator when it has
                                                                                             // arrived at destination
                                                                                             // floor and/or picked
                                                                                             // someone up along the way

                if (this.currentFloor == oldCurrentFloor) { // if oldCurrentFloor is equal to new current floor,
                                                            // elevator did not move
                    setMotorMoving(false);
                    System.out.println(currentThreadName + ": Motor stopped moving");
                    setDoorOpen(true);
                    System.out.println("Doors opening -> People are walking in/out");

                    try {
                        Thread.sleep(1000); // sleep for the amount of time it takes to open/close doors. Small time for
                                            // testing
                        // Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    System.out.println("Doors are closing");
                    setDoorOpen(false);
                } else {

                    setMotorMoving(true);
                    System.out.println(currentThreadName + ": Motor moving");

                }
                System.out.println(currentThreadName + ": Current floor is now: " + this.currentFloor + "\n");

                try {
                    Thread.sleep(3000); // sleep for the amount of time it takes to move between floors. Small time for
                                        // testing
                    // Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

    }

    public Boolean getMotorMoving() {
        return motorMoving;
    }

    public Boolean getDoorOpen() {
        return doorOpen;
    }

}
