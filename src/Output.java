package src;

/**
 * This class manages the output represenation of the Elevators
 */
public class Output {

    /**
     * Default constructor
     */
    public Output(){

    }

    /**
     * Output for elevator to stop at current floor with requests
     * @param elevatorNum denotes the elevator cart #
     * @param currentFloor denotes the current floor
     */
    public void printElevatorFloorRequest(int elevatorNum, int currentFloor){
         System.out.println("Elevator #" + elevatorNum
                    + ": There are people waiting for the elevator on this floor: " + currentFloor);
    }

    /**
     * Output to notify which direction the elevator is moving in 
     * @param up specifies the direction the elevator is moving in
     * @param elevatorNum denotes the elevator cart #
     */
    public void printDirection(boolean up, int elevatorNum){
        if(up == true){
            System.out.println(
                        "Elevator #" + elevatorNum + ": Is below initial floor of first request in queue -> moving up");
                
        }else{
            System.out.println("Elevator #" + elevatorNum
                        + ": Is above initial floor of first request in queue -> moving down");
        }
    }

    /**
     * Output to notify which elevator has reached the destination of a passanger 
     * @param elevatorNum denotes the elevator cart #
     */
    public void printDestinationReached(int elevatorNum){
        System.out.println(
                            "Elevator #" + elevatorNum + ": Is at the destination of a passenger in the elevator");                    
    }

    /**
     * Output to notify when the doors are opening
     * @param elevatorNum denotes the elevator cart #
     */
    public void printOpenDoors(int elevatorNum){
        System.out.println("Elevator #" + elevatorNum + " -> Notfiy elevator to open doors");
                
    }

    /**
     * Output for the users current state
     * @param elevatorNum denotes the elevator cart #
     * @param bothInOut represents type of transition 
     */
    public void printUserTransition(int elevatorNum, int bothInOut){
        if(bothInOut == 2){
             System.out.println("Elevator #" + elevatorNum + " -> People are walking in and out");
        }else if(bothInOut == 1){
            System.out.println("Elevator #" + elevatorNum + " -> People are walking out");
        }else if(bothInOut == 0){
            System.out.println("Elevator #" + elevatorNum + " -> People are walking in");                
        }                 
    }

    /**
     * Outputs a fault occuring which is the elevator cart is stuck
     */
    public void printElevatorStuckFault(){
        System.out.println("Timeout has occured while the elevator is trying to move");
        System.out.println("Therefore we are stuck, activating emergency routine now");
    }

    /**
     * Outputs the elevator fault error message
     * @param elevatorNum
     * @param isDone
     */
    public void printElevatorErrorStatus(int elevatorNum){
        System.out.println("Elevator #" + elevatorNum + " is STUCK");                
    }

    /**
     * Outputs if there is a fault in the doors opening/closing and fixing the fault
     * @param elevatorNum denotes the elevator cart #
     * @param isOpen represents if the doors failed to open or close
     */
    public void printDoorError(int elevatorNum, boolean isOpen){
        if(isOpen){
            System.out.println("Elevator #" + elevatorNum + ": has failed to open doors");
        }else{
            System.out.println("Elevator #" + elevatorNum + ": has failed to close doors");            
        }

        System.out.println("Elevator #" + elevatorNum + " -> fixing door");
    }

    /**
     * Outputs the message for the elevator being fixed
     * @param elevatorNum
     */
    public void printDoorFixed(int elevatorNum){
        System.out.println("Elevator #" + elevatorNum + " -> doors have been fixed");
    }

    /**
     * Outputs the status of the elevator doors
     * @param elevatorNum denotes the elevator cart #
     * @param isOpen represents if doors are opening or closing
     */
    public void printDoorUpdate(int elevatorNum, boolean isOpen){
        if(isOpen){
            System.out.println("Elevator #" + elevatorNum + ": Doors opening");
        }else{
            System.out.println("Elevator #" + elevatorNum + ": Doors are closing");
        }       
    }

    /**
     * Outputs the runtime of the elevators service in seconds 
     * @param runTimeInSeconds
     */
    public void printTimeElapsed(long runTimeInSeconds){
        System.out.println("The elevators took " + runTimeInSeconds + " seconds to finish servicing all of the requests");

    } 
    
}
