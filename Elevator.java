

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
            String currentThreadName = Thread.currentThread().getName();
            // This passes the current floor the elevator instance is on to the scheduler to be used as a variable
            // Reassigns the current floor in this elevator instance to the incremented variable
            // ex: passes in currentFloor = 1, reassigns currentFloor = 2
            this.currentFloor = scheduler.getFloorRequest(this.currentFloor);
            setDoorOpen(true);
            System.out.println(currentThreadName + ": Doors are opening");
            System.out.println(currentThreadName + ": Current floor is now: " + currentFloor);

            //Simulate elvator moving time (using small time for testing)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //TODO: sleep for the amount of time it takes someone to get into the elevator?
            /*floorButtons.replace(floorToGoTo, true);
            floorButtonsLamps.replace(floorToGoTo, true);
            System.out.println(currentThreadName + ": Floor button " + floorToGoTo + " has been pressed and the button light is on");
            setDoorOpen(false);
            System.out.println(currentThreadName + ": Doors are closing");
            setMotorMoving(true);
            System.out.println(Thread.currentThread().getName() + ": Started to move");
            currentFloor = floorToGoTo;
            setDoorOpen(true);
            System.out.println(currentThreadName + ": Doors are opening");
            System.out.println(currentThreadName + ": passenger has been dropped off on floor: " + this.currentFloor);
            setDoorOpen(false);
            System.out.println(currentThreadName + ": Doors are closing\n");*/
        }
    }
}
