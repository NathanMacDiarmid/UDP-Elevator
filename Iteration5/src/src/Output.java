package src;

/**
 * This class manages the output represenation of the Elevators
 */
public class Output {

    /**
     * Output prints where each elevator is in real time (floor to floor)
     * @param elevatorNum
     * @param currentFloor
     */
    public void printFloor(int elevatorNum, int currentFloor) {
        System.out.println("Elevator #" + elevatorNum + ": on Floor #" + currentFloor);
    }

    /**
     * Output for elevator to stop at current floor with requests
     * @param elevatorNum denotes the elevator cart #
     * @param currentFloor denotes the current floor
     */
    public void printElevatorFloorRequest(int elevatorNum) {
        System.out.println("Elevator #" + elevatorNum
                + ": There are people waiting for the elevator on this floor: ");
    }

    /**
     * Output to notify which direction the elevator is moving in 
     * @param up specifies the direction the elevator is moving in
     * @param elevatorNum denotes the elevator cart #
     */
    public void printDirection(int elevatorNum, boolean directionUp) {

        if (directionUp == true) {
            System.out.println(
                    "Elevator #" + elevatorNum + " -> going UP" + "\n");
        } else {
            System.out.println("Elevator #" + elevatorNum + " -> going DOWN" + "\n");
        }
    }

    /**
     * Output to notify which elevator has reached the destination of a passanger 
     * @param elevatorNum denotes the elevator cart #
     */
    public void printDestinationReached(int elevatorNum) {
        System.out.println(
                "Elevator #" + elevatorNum + ": Is at the destination floor of a passenger(s) in the elevator");
    }

    /**
     * Output for the users current state
     * @param elevatorNum denotes the elevator cart #
     * @param bothInOut represents type of transition 
     */
    public void printUserTransition(int elevatorNum, int bothInOut) {

        if (bothInOut == 2) {
            System.out.println("Elevator #" + elevatorNum + ": People are walking in and out");
        } else if (bothInOut == 1) {
            System.out.println("Elevator #" + elevatorNum + ": People are walking out");
        } else if (bothInOut == 0) {
            System.out.println("Elevator #" + elevatorNum + ": People are walking in");
        }
    }

    /**
     * Outputs a fault occuring which is the elevator cart is stuck
     */
    public void printElevatorStuckFault() {
        System.out.println(
                "Timeout has occured while the elevator is trying to move -> activating emergency routine now");
    }

    /**
     * Outputs the elevator fault error message
     * @param elevatorNum
     * @param isDone
     */
    public void printStuckError(int elevatorNum) {
        System.out.println("Elevator #" + elevatorNum + " is STUCK");
    }

    /**
     * Outputs if there is a fault in the doors opening/closing and fixing the fault
     * @param elevatorNum denotes the elevator cart #
     * @param isOpen represents if the doors failed to open or close
     */
    public void printDoorError(int elevatorNum, boolean isOpen) {
        if (isOpen) {
            System.out.println("Elevator #" + elevatorNum + ": has failed to open doors");
        } else {
            System.out.println("Elevator #" + elevatorNum + ": has failed to close doors");
        }

        System.out.println("Elevator #" + elevatorNum + " -> fixing door");
    }

    /**
     * Outputs the message for the elevator being fixed
     * @param elevatorNum
     */
    public void printDoorFixed(int elevatorNum) {
        System.out.println("Elevator #" + elevatorNum + " -> doors have been fixed");
    }

    /**
     * Outputs the status of the elevator doors
     * @param elevatorNum denotes the elevator cart #
     * @param isOpen represents if doors are opening or closing
     */
    public void printDoorUpdate(int elevatorNum, boolean isOpen) {
        if (isOpen) {
            System.out.println("Elevator #" + elevatorNum + ": Doors opening");
        } else {
            System.out.println("Elevator #" + elevatorNum + ": Doors are closing");
        }
    }

    /**
     * Outputs the runtime of the elevators service in seconds 
     * @param runTimeInSeconds
     */
    public void printTimeElapsed(long runTimeInSeconds) {
        System.out
                .println("The elevators took " + runTimeInSeconds + " seconds to finish servicing all of the requests");

    }

}
