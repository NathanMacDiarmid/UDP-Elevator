package src;

public class Output {

    public Output(){

    }

    public void printElevatorFloorRequest(int elevatorNum, int currentFloor){
         System.out.println("Elevator #" + elevatorNum
                    + ": There are people waiting for the elevator on this floor: " + currentFloor);
    }

    public void printDirection(boolean up, int elevatorNum){
        if(up == true){
            System.out.println(
                        "Elevator #" + elevatorNum + ": Is below initial floor of first request in queue -> moving up");
                
        }else{
            System.out.println("Elevator #" + elevatorNum
                        + ": Is above initial floor of first request in queue -> moving down");
        }
    }

    public void printDestinationReached(int elevatorNum){
        System.out.println(
                            "Elevator #" + elevatorNum + ": Is at the destination of a passenger in the elevator");
                    
    }

    public void printOpenDoors(int elevatorNum){
        System.out.println("Elevator #" + elevatorNum + " -> Notfiy elevator to open doors");
                
    }

    public void printUserTransition(int elevatorNum, int bothInOut){
        if(bothInOut == 2){
             System.out.println("Elevator #" + elevatorNum + " -> People are walking in and out");
        }else if(bothInOut == 1){
            System.out.println("Elevator #" + elevatorNum + " -> People are walking out");
        }else if(bothInOut == 0){
            System.out.println("Elevator #" + elevatorNum + " -> People are walking in");
                
        }
       
                
    }

    public void printElevatorStuckFault(){
        System.out.println("Timeout has occured while the elevator is trying to move");
        System.out.println("Therefore we are stuck, activating emergency routine now");
    }
    public void printElevatorStatus(int elevatorNum, boolean isDone){
        if(isDone){
            System.out.println("Elevator #" + elevatorNum + " is done");                 
        }else{
            System.out.println("Elevator #" + elevatorNum + " is STUCK");                   
        }
    }

    public void printMotorUpdate(int elevatorNum, boolean isStopping){
        if(isStopping){
            System.out.println("Elevator #" + elevatorNum + ": Motor stopped moving");
        }else{
            System.out.println("Elevator #" + elevatorNum + ": Motor is moving again");
        }
        
    }

    public void printDoorError(int elevatorNum, boolean isOpen){
        if(isOpen){
            System.out.println("Elevator #" + elevatorNum + ": has failed to open doors");
        }else{
            System.out.println("Elevator #" + elevatorNum + ": has failed to close doors");            
        }

        System.out.println("Elevator #" + elevatorNum + " -> fixing door");
    }

    public void printDoorUpdate(int elevatorNum, boolean isOpen){
        if(isOpen){
            System.out.println("Elevator #" + elevatorNum + ": Doors opening");
        }else{
            System.out.println("Elevator #" + elevatorNum + ": Doors are closing");
        }
       
    }

    public void printRequestUpdate(int elevatorNum){
        System.out.println("Elevator #" + elevatorNum + ": Request sent.\n");
    }

    public void printTimeElapsed(long runTimeInSeconds){
        System.out.println("The elevators took " + runTimeInSeconds + " seconds to finish servicing all of the requests");

    }
    
    
}
