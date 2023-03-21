package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class represents the Elevator sub-system
 * It moves between floors based on instructions passed from data.txt
 */
public class Elevator {
    private int initialFloor = 0;
    /* After moving elevator newCurrentFloor will be updated */
    int newCurrentFloor = 0;
    int prevCurrentFloor = 0;
    private int elevatorNum = 0;
    private int numOfPeopleInsideElev = 0;
    private int numOfPeopleServiced = 0;
    private String direction = null;
    private boolean motorMoving;
    private boolean doorOpen;
    private boolean firstRequest = true;
    private boolean noMoreRequests = false;
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendAndReceiveSocket;
    private byte[] data = new byte[300];

    /* requestQueue is used as priority queue of requests */
    private ArrayList<InputData> requestQueue;

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
        {
            // (integer, boolean(pressed or not), String("light on " /"light off"))  

            put(1, false);
            put(2, false);
            put(3, false);
            put(4, false);
            put(5, false);
            put(6, false);
            put(7, false);
        }
    };

    /* floorQueue is to keep track of people waiting for this elevator on each floor */
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

    private Map<Integer, Boolean> closeDoorFaultByFloor = new HashMap<Integer, Boolean>() {
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

    private Map<Integer, Boolean> openDoorFaultByFloor = new HashMap<Integer, Boolean>() {
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

    private boolean createElevatorStuckFault;

    /* elevatorQueue is the queue of requests that are currently in this elevator */
    private ArrayList<InputData> insideElevatorQueue;

    /**
     * Default constructor for Elevator
     * @param elevatorNum is the elevator car #
     * @param startFloor is the floor that the elevator starts on
     * @param direction is the starting direction of the elevator
     */
    public Elevator(int elevatorNum, int startFloor, String direction) {
        this.elevatorNum = elevatorNum;
        this.initialFloor = startFloor;
        this.direction = direction;
        this.requestQueue = new ArrayList<InputData>();
        try {
            this.sendAndReceiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
        this.insideElevatorQueue = new ArrayList<InputData>();
        this.createElevatorStuckFault = false;
    }

    /*
     * Gets the requestQueue (main queue of requests for this elevator)
     */
    public ArrayList<InputData> getRequestQueue() {
        return requestQueue;
    }

    /**
     * Sets the floor button to be pressed or not pressed and turn on lamp of button if pressed
     * @param floor # button
     * @param buttonPressed true if pressed, false if not
     */
    public void setFloorButton(Integer floor, Boolean buttonPressed) {
        floorButtons.replace(floor, buttonPressed);
        setFloorButtonLamps(floor, buttonPressed);
    }

    /**
     * Sets the floor button lamps to be on when pressed or off when not pressed
     * @param floor # button
     * @param buttonPressed true if pressed, false if not
     */
    public void setFloorButtonLamps(Integer floor, Boolean buttonLampOn) {
        floorButtonsLamps.replace(floor, buttonLampOn);
    }

    /**
     * Sets whether the motor is moving or stopped
     * @param motorMoving true when moving, false when stopped
     */
    public void setMotorMoving(Boolean motorMoving) {
        this.motorMoving = motorMoving;
    }

    /**
     * Sets whether doors are opening or closing
     * @param doorOpen true when opening, false when closing
     */
    public void setDoorOpen(Boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    /**
     * Gets current motor status
     * @return true if motor is moving, false if not
     */
    public Boolean getMotorMoving() {
        return motorMoving;
    }

    /**
     * Gets current door status
     * @return true is doors are open, false if not
     */
    public Boolean getDoorOpen() {
        return doorOpen;
    }

    /**
     * Moves the elevator to take care of requests in requestQueue
     * @return the current floor after movement (depending on requests)
     * @author Juanita Rodelo 101141857
     * @author Matthew Belanger 101144323
     * @author Amanda Piazza 101143004
     */
    public int moveElevator(int currentFloor) {
        int sizeBeforePickup = (insideElevatorQueue != null) ? insideElevatorQueue.size() : 0; //if insideElevatorQueue is null, assign size of elevator to 0
        boolean reachedDestination = false;
        boolean pickedPplUp = false;
        int sizeAfterPickup;
        boolean doorNotOpen = false;
        boolean doorNotClose = false;

        // if the floor that the elevator is currently on has passengers waiting -> pick them up
        if ((currentFloor != 0) && (this.floorQueues.get(currentFloor).size() != 0)) {

            System.out.println("Elevator #" + elevatorNum
                    + ": There are people waiting for the elevator on this floor: " + currentFloor);
            pickedPplUp = true;
            this.insideElevatorQueue.addAll(this.floorQueues.get(currentFloor)); //this adds all requests to current elevator
            sizeAfterPickup = insideElevatorQueue.size();
            numOfPeopleInsideElev += (sizeAfterPickup - sizeBeforePickup); //number of people inside elevator should increase by the amount of people that just walked in
            this.floorQueues.get(currentFloor).removeAll(insideElevatorQueue); //this removes all floor requests from current floor because passenger(s) have entered elevator
        }

        // else if takes care of the situation where the elevator has not picked up ANY passenger(s)
        else if (this.insideElevatorQueue.size() == 0) {

            if ((currentFloor < requestQueue.get(0).getFloor())) { //if elevator is below floor of first request, move up, else move down
                System.out.println(
                        "Elevator #" + elevatorNum + ": Is below initial floor of first request in queue -> moving up");
                direction = "up";
                elevatorMoveTiming();
                currentFloor = currentFloor + 1; //move elevator up
            } else {
                System.out.println("Elevator #" + elevatorNum
                        + ": Is above initial floor of first request in queue -> moving down");
                direction = "down";
                elevatorMoveTiming();
                currentFloor = currentFloor - 1; //move elevator down
            }

        }

        //if elevator currently has passenger(s) in it 
        if (this.insideElevatorQueue.size() > 0) {

            //go through the requests that are currently in the elevator
            Iterator<InputData> iterator = this.insideElevatorQueue.iterator();
            while (iterator.hasNext()) {

                InputData currPassenger = iterator.next();
                //check if current floor is equal to any of the destination floors of passenger(s) in the elevator
                if (currentFloor == currPassenger.getCarRequest()) {

                    System.out.println(
                            "Elevator #" + elevatorNum + ": Is at the destination of a passenger in the elevator");
                    reachedDestination = true;
                    iterator.remove(); //remove from elevator queue because passenger left
                    numOfPeopleInsideElev--;
                    requestQueue.removeIf(request -> (request == currPassenger)); //remove from general main queue because passenger left
                    numOfPeopleServiced++;
                }
            }

            //if the current floor has an open fault happening
            if(openDoorFaultByFloor.get(currentFloor)){
                doorNotOpen = true;
            }
            //if the current floor has a close fault happening
            if(closeDoorFaultByFloor.get(currentFloor)){
                doorNotClose = true;
            }
            
            //if we have reached the desitnation floor of one or more passengers AND there are people on this floor waiting for the elevator -> stop elevator and let them off and pick up passengeres waiting
            if (reachedDestination && pickedPplUp) {
                
                System.out.println("Elevator #" + elevatorNum + " -> Notfiy elevator to open doors");
                stopElevator(doorNotOpen);
                System.out.println("Elevator #" + elevatorNum + " -> People are walking in and out");
                startElevator(doorNotClose);
            } else if (reachedDestination) {
                System.out.println("Elevator #" + elevatorNum + " -> Notfiy elevator to open doors");
                stopElevator(doorNotOpen);
                System.out.println("Elevator #" + elevatorNum + " -> People are walking out");
                startElevator(doorNotClose);

            } else if (pickedPplUp) {
                System.out.println("Elevator #" + elevatorNum + " -> Notfiy elevator to open doors");
                stopElevator(doorNotOpen);
                System.out.println("Elevator #" + elevatorNum + " -> People are walking in");
                startElevator(doorNotClose);
            }

            //if there are people in the elevator after dropping one or more passengers off, start going to their destination floor
            if (this.insideElevatorQueue.size() > 0) {

                //if elevator is above floor of the the destination of the first request, move down, else move up
                //we won't have to check if we are at the destination floor because the previous if statement took care of that
                if (currentFloor > insideElevatorQueue.get(0).getCarRequest()) {
                    System.out.println("Elevator #" + elevatorNum
                            + ": is above destination floor of first request in priority queue -> moving down");
                    direction = "down";
                    elevatorMoveTiming();
                    currentFloor = currentFloor - 1; //move elevator down
                } else {
                    System.out.println("Elevator #" + elevatorNum
                            + ": is below destination floor of first request in priority queue -> moving up");
                    direction = "up";
                    elevatorMoveTiming();
                    currentFloor = currentFloor + 1; //move elevator up
                }

            }

        }

        return currentFloor;
    }

    /**
     * This method will handle the timing as well as the timer for
     * moving floors, timeout will activate if we take longer then the timeout
     * time to move between floors.
     */
    public void elevatorMoveTiming(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run(){
                System.out.println("Timeout");
            }
        }, 3000);

        if(createElevatorStuckFault){
            this.sleep(5000);
        }
        else{
            this.sleep(1000);
        }
        timer.cancel();
    }

    /**
     * This makes the program sleep for a provided duration of time
     * @param time in milliseconds (2000 is 2 seconds)
     * @author Nathan MacDiarmid 101098993
     */
    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops the motor, opens door, let's people walk in/out, and closes doors
     * @author Juanita Rodelo 101141857
     */
    public void stopElevator(boolean doorNotOpenError) {

        setMotorMoving(false);
        System.out.println("Elevator #" + elevatorNum + ": Motor stopped moving");
        
        if(doorNotOpenError){
            System.out.println("Elevator #" + elevatorNum + ": has failed to open doors");
            System.out.println("Elevator #" + elevatorNum + " -> fixing door");                  
            this.sleep(4000); 
            System.out.println("Elevator #" + elevatorNum + " -> doors have been fixed"); 
            //Handle error
        }
        System.out.println("Elevator #" + elevatorNum + ": Doors opening"); 
        setDoorOpen(true);        
        this.sleep(2700); //sleep for the amount of time it takes to open the doors.

    }

    /**
     * Stops the motor, opens door, let's people walk in/out, and closes doors
     * @author Juanita Rodelo 101141857
     */
    public void startElevator(boolean doorNotCloseError) {

        if(doorNotCloseError){             
            System.out.println("Elevator #" + elevatorNum + ": has failed to close doors");
            System.out.println("Elevator #" + elevatorNum + " -> fixing door");                  
            this.sleep(4000); 
            System.out.println("Elevator #" + elevatorNum + " -> doors have been fixed"); 
            //Handle error
        }      
        System.out.println("Elevator #" + elevatorNum + ": Doors are closing"); 
        setDoorOpen(false);
        System.out.println("Elevator #" + elevatorNum + ": Motor is moving again");
        setMotorMoving(true);
    }

    public String prepareStatus() {

        String message = "";

        if (firstRequest) { //TODO: message is the same in both conditions so change
            // Prepares the message to be sent by forming a byte array
            message = "Elevator car #: " + elevatorNum
                    + " Floor: " + initialFloor
                    + " Num of people: " + numOfPeopleInsideElev
                    + " Serviced: " + numOfPeopleServiced
                    + " Direction: " + this.direction;
            prevCurrentFloor = initialFloor;
        } else if (requestQueue.size() > 0) {

            newCurrentFloor = moveElevator(prevCurrentFloor);
            message = "Elevator car #: " + elevatorNum
                    + " Floor: " + newCurrentFloor
                    + " Num of people: " + numOfPeopleInsideElev
                    + " Serviced: " + numOfPeopleServiced
                    + " Direction: " + direction;

            prevCurrentFloor = newCurrentFloor;
        }

        return message;

    }

    /**
    * Sends the status of the current elevator to the scheduler
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    */
    public void sendStatus() {

        String message = prepareStatus();

        byte[] msg = message.getBytes();
        System.out.println("Elevator car #" + elevatorNum + " is sending a packet containing: " + message);

        // Creates the DatagramPacket to be sent to port 23
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 69);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int len = sendPacket.getLength();
        System.out.println(
                "To host: " + sendPacket.getAddress() + ", on port: " + sendPacket.getPort() + ", with length: " + len);

        // Sends the DatagramPacket over port 23
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Elevator #" + elevatorNum + ": Request sent.\n");
    }

    /**
    * Receives message from scheduler (either an instruction or no request)
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    */
    public void receiveInstruction() {

        // Initializes the receive DatagramPacket
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Elevator " + elevatorNum + ": Waiting for Packet.");

        // Receives the DatagramPacket
        try {
            sendAndReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        int len = receivePacket.getLength();
        System.out.println("Elevator #" + elevatorNum + " received a packet from host: " + receivePacket.getAddress()
                + ", on port: " + receivePacket.getPort() + ", with length: " + len);
        System.out.print("Containing: ");
        String received = new String(data, 0, len); //TODO: make this a class variable
        System.out.println(received);

        saveReceivedMessage(received);
        System.out.println("Elevator - requestQueue: " + requestQueue.toString());
        System.out.println("Elevator - floorQueues: " + floorQueues.toString() + "\n");
    }

    /**
    * Parses the message received from the scheduler and saves it if it contains a request
    * @author Michael Kyrollos 101183521
    */
    public void saveReceivedMessage(String message) {
        System.out.println("in save received message");
        InputData request;
        int currentTime;
        int floor;
        boolean isDirectionUp;
        boolean doorNotOpenError;
        boolean doorNotCloseError;
        boolean elevatorStuckError;
        int carButton;
        Pattern pattern = Pattern.compile(
                "\\[currentTime=(\\d+:\\d+:\\d+\\.\\d+), floor=(\\d+), isDirectionUp=(\\w+), car button=(\\d+), doorNotOpenError=(\\w+), doorNotCloseError=(\\w+), elevatorStuckError=(\\w+)\\]: (\\w+)");
        Matcher matcher = pattern.matcher(message);
        LocalTime time;

        // Checks if the message received is no more requests so that the elevator instance knows
        if (message.equals("No more requests")) {
            noMoreRequests = true;
        }

        //If message received from scheduler is not "no current requests", then it holds a request and we must save all input data info
        if (!message.equals("No current requests")) {
            firstRequest = false;

            if (matcher.find()) {
                time = LocalTime.parse((matcher.group(1))); //TODO: might want to have a try-catch around this parsing
                currentTime = time.get(ChronoField.MILLI_OF_DAY);
                floor = Integer.parseInt(matcher.group(2));
                isDirectionUp = Boolean.parseBoolean(matcher.group(3));
                carButton = Integer.parseInt(matcher.group(4));
                doorNotOpenError = Boolean.parseBoolean(matcher.group(5));
                doorNotCloseError = Boolean.parseBoolean(matcher.group(6));
                elevatorStuckError = Boolean.parseBoolean(matcher.group(7));
                // lastRequest = Boolean.parseBoolean(matcher.group(8));
                request = new InputData(currentTime, floor, isDirectionUp, carButton, doorNotOpenError,
                        doorNotCloseError, elevatorStuckError);

                //Add request to elevatorQueue
                this.floorQueues.get(request.getFloor()).add(request); // adds request to corresponding floor queue
                System.out.println("adding to requestQueue");
                this.requestQueue.add(request); // adds request to main request queue 
                
                //save whether the request has a faXult occuring
                this.closeDoorFaultByFloor.replace(request.getFloor(), request.getDoorNotCloseError());
                this.openDoorFaultByFloor.replace(request.getFloor(), request.getDoorNotOpenError());
                
                if(elevatorStuckError){
                    createElevatorStuckFault = true;
                }
                
            }
            
        }
    }
    

    /**
     * Closes the open sockets when program ends
     * @author Nathan MacDiarmid 101098993 
    */
    public void closeSocket() {
        sendAndReceiveSocket.close();
    }

    /**
     * Gets size of requestQueue
     * @return size
     */
    public int getSizeOfRequestQueue() {
        return requestQueue.size();
    }

    public boolean isNoMoreRequests() {
        return noMoreRequests;
    }

    /**
     * THE FOLLOWING GETTER IS FOR TESTING PURPOSES ONLY
     */
    public DatagramPacket getReceivePacket() {
        return this.receivePacket;
    }

    public static void main(String args[]) {
        System.out.println();

        /*This maps an elevator instance to their finished status (true when done, false when not done) */
        LinkedHashMap<Elevator, Boolean> elevatorsFinished = new LinkedHashMap<>();
        Elevator elevator1 = new Elevator(1, 2, "up");
        Elevator elevator2 = new Elevator(2, 4, "up");
        elevatorsFinished.put(elevator1, false);
        elevatorsFinished.put(elevator2, false);

        //while all elevators aren't done
        while (elevatorsFinished.containsValue(false)) {

            Iterator<Map.Entry<Elevator, Boolean>> elevatorsIterator1 = elevatorsFinished.entrySet().iterator();
            Iterator<Map.Entry<Elevator, Boolean>> elevatorsIterator2 = elevatorsFinished.entrySet().iterator();

            //the following two while loops have the same logic, but the first handles the sending of the elevator status and the second handles the receiving
            //in order for all elevators to first send all of their status's and then receive

            //iterate through all elevators and check if they're done to know whether they should keep sending their status
            while (elevatorsIterator1.hasNext()) {

                boolean elevatorDone = false;
                Map.Entry<Elevator, Boolean> currElevatorStatus = elevatorsIterator1.next();
                Elevator currElevator = currElevatorStatus.getKey();

                //if current elevator is done accepting requests from schedule and has completed servicing all of it's requests -> elevator is done
                if (currElevator.isNoMoreRequests() && currElevator.getRequestQueue().size() == 0) {
                    System.out.println("Elevator #" + currElevator.elevatorNum + " is done");
                    elevatorsFinished.replace(currElevator, true);
                    elevatorDone = true;

                }

                //only if elevator is not done -> send status, else don't send anything
                if (!elevatorDone) {
                    //if currElevator is not done: send
                    currElevator.sendStatus();
                }
            }

            //iterate through all elevators and check if they're done to know whether they should keep receving messages
            while (elevatorsIterator2.hasNext()) {

                boolean elevatorDone = false;
                Map.Entry<Elevator, Boolean> currElevatorStatus = elevatorsIterator2.next();
                Elevator currElevator = currElevatorStatus.getKey();

                //if current elevator is done accepting requests from schedule and has completed servicing all of it's requests -> elevator is done
                if (currElevator.isNoMoreRequests() && currElevator.getRequestQueue().size() == 0) {
                    System.out.println("Elevator #" + currElevator.elevatorNum + " is done");
                    elevatorsFinished.replace(currElevator, true);
                    elevatorDone = true;
                }

                //only if elevator is not done -> receive message
                if (!elevatorDone) {
                    //if currElevator is not done: receive
                    currElevator.receiveInstruction();
                }

            }
            System.out.println(
                    "-------------------------------------------------------------------------------------------------");

        }
        elevator1.closeSocket();
        elevator2.closeSocket();
    }
}