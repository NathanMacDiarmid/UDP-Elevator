package src;

import java.util.HashMap;
import java.util.Map;

public class Elevator implements Runnable {
    private int currentFloor = 0;
    private Scheduler scheduler;
    private Map<Integer, Boolean> floorButtons = new HashMap<>() {{
        put(1, false);
        put(2, false);
        put(3, false);
        put(4, false);
        put(5, false);
        put(6, false);
        put(7, false);
    }};
    private Map<Integer, Boolean> floorButtonsLamps = new HashMap<>() {{ //TODO: can we just have one map that represents buttons and their lights (integer, boolean(pressed or not), String("light on"/"light off"))
        put(1, false);
        put(2, false);
        put(3, false);
        put(4, false);
        put(5, false);
        put(6, false);
        put(7, false);
    }};

    private Boolean motorMoving; //TODO: should this be a string to specify which direction it's going in
    private Boolean doorOpen;

    /**
     * Default constructor for Elevator
     * @param scheduler the Scheduler instance that needs to be passed (Box class)
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setFloorButton(Integer floor, Boolean buttonPressed){ //TODO: maybe add getters later on if needed
        floorButtons.replace(floor, buttonPressed);
    }

    public void setFloorButtonLamps(Integer floor, Boolean buttonLampOn){ //TODO: maybe add getters later on if needed
        floorButtonsLamps.replace(floor, buttonLampOn);
    }

    public void setMotorMoving(Boolean motorMoving){
        this.motorMoving = motorMoving;
    }

    public void setDoorOpen(Boolean doorOpen){
        this.doorOpen = doorOpen;
    }
    


    @Override
    /**
     * The run method for the Elevator class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        while (true) {
            //get request, sleep for 7 sec (time to go between floor), get again to check if there are more requests
    
            this.currentFloor = scheduler.getFloorRequest(this.currentFloor);
            
            System.out.println("Elevator has dropped passenger off on floor - " + this.currentFloor + "\n");
        }
    }
}
