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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class represents the Elevator subsystem
 * It moves between floors based on instructions passed from data.txt
 */

public class Elevator {
    private int initialFloor = 0;
    private int newCurrentFloor = 0;
    private int prevCurrentFloor = 0;
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
    private Map<Integer, Boolean> floorButtons = new HashMap<Integer, Boolean>();

    /* floorButtonsLamps represent the lamps on the buttons inside the elevator */
    private Map<Integer, Boolean> floorButtonsLamps = new HashMap<Integer, Boolean>();

    /* floorQueue is to keep track of people waiting for this elevator on each floor */
    private Map<Integer, ArrayList<InputData>> floorQueues = new HashMap<Integer, ArrayList<InputData>>();

    /* closeDoorFaultByFloor maps the floor to whether a closed door fault will occur on that floor */
    private Map<Integer, Boolean> closeDoorFaultByFloor = new HashMap<Integer, Boolean>();

    /* openDoorFaultByFloor maps the floor to whether an open door fault will occur on that floor */
    private Map<Integer, Boolean> openDoorFaultByFloor = new HashMap<Integer, Boolean>();

    private boolean createElevatorStuckFault;
    private boolean elevatorIsStuck;

    /* elevatorQueue is the queue of requests that are currently in this elevator */
    private ArrayList<InputData> insideElevatorQueue;

    /* output will handle all calls to prints for the UI */
    private Output output;

    private long timeBetweenFloors;
    private long timeToLoadUnload;

    /**
     * Default constructor for Elevator
     * @param elevatorNum is the elevator car #
     * @param startFloor is the floor that the elevator starts on
     * @param direction is the starting direction of the elevator
     * @param numOfFloors is the number of floors that the elevator system will have
     * @param timeBetweenFloors is the time it should take the elevator to move between floors
     * @param timeToLoadUnload is the time it should take the elevator to load and unload people
     */
    public Elevator(int elevatorNum, int startFloor, String direction, int numOfFloors, long timeBetweenFloors, long timeToLoadUnload) {
        
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
        this.elevatorIsStuck = false;

        for (int i = 1; i < numOfFloors + 1; i++) {
            floorButtons.put(i, false);
            floorButtonsLamps.put(i, false);
            floorQueues.put(i, new ArrayList<InputData>());
            closeDoorFaultByFloor.put(i, false);
            openDoorFaultByFloor.put(i, false);
        }

        output = new Output();
        this.timeBetweenFloors = timeBetweenFloors;
        this.timeToLoadUnload = timeToLoadUnload;
       
    }

    /**
     * Gets the requestQueue (main queue of requests for this elevator)
     * @return an array list of requests
     */
    public ArrayList<InputData> getRequestQueue() {
        return requestQueue;
    }

    /**
     * Determines whether the elevator is stuck
     * @return true or false
     */
    public boolean getIsStuck() {
        return elevatorIsStuck;
    }

    /**
     * Sets the floor button to be pressed or not pressed and turn on lamp of button if pressed
     * @param floor is the floor button to press
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
     * @return the new current floor after movement (depending on requests)
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

        output.printFloor(elevatorNum, currentFloor);

        // if the floor that the elevator is currently on has passengers waiting -> pick them up
        if ((currentFloor != 0) && (this.floorQueues.get(currentFloor).size() != 0)) {
            output.printElevatorFloorRequest(elevatorNum);
            pickedPplUp = true;
            this.insideElevatorQueue.addAll(this.floorQueues.get(currentFloor)); //this adds all requests to current elevator
            sizeAfterPickup = insideElevatorQueue.size();
            numOfPeopleInsideElev += (sizeAfterPickup - sizeBeforePickup); //number of people inside elevator should increase by the amount of people that just walked in
            this.floorQueues.get(currentFloor).removeAll(insideElevatorQueue); //this removes all floor requests from current floor because passenger(s) have entered elevator
        }

        // else if takes care of the situation where the elevator has not picked up ANY passenger(s)
        else if (this.insideElevatorQueue.size() == 0) {

            if ((currentFloor < requestQueue.get(0).getFloor())) { //if elevator is below floor of first request, move up, else move down
                output.printDirection(elevatorNum, true);
                direction = "up";
                elevatorMoveTiming();
                currentFloor = currentFloor + 1; //move elevator up
            } else {
                output.printDirection(elevatorNum, false);
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
                    output.printDestinationReached(elevatorNum);
                    reachedDestination = true;
                    iterator.remove(); //remove from elevator queue because passenger left
                    numOfPeopleInsideElev--;
                    requestQueue.removeIf(request -> (request == currPassenger)); //remove from general main queue because passenger left
                    numOfPeopleServiced++;
                }
            }

            //if the current floor has an open fault happening
            if (openDoorFaultByFloor.get(currentFloor)) {
                doorNotOpen = true;
            }
            //if the current floor has a close fault happening
            if (closeDoorFaultByFloor.get(currentFloor)) {
                doorNotClose = true;
            }

            //if we have reached the desitnation floor of one or more passengers AND there are people on this floor waiting for the elevator -> stop elevator and let them off and pick up passengeres waiting
            if (reachedDestination && pickedPplUp) {
                //output.printOpenDoors(elevatorNum);
                stopElevator(doorNotOpen);
                output.printUserTransition(elevatorNum, 2); //2 indicates both users walking in and out
                startElevator(doorNotClose);
            } else if (reachedDestination) {
                //output.printOpenDoors(elevatorNum);                
                stopElevator(doorNotOpen);
                output.printUserTransition(elevatorNum, 1); //1 indicates users walking out                
                startElevator(doorNotClose);

            } else if (pickedPplUp) {
                //output.printOpenDoors(elevatorNum);                
                stopElevator(doorNotOpen);
                output.printUserTransition(elevatorNum, 0); //0 indicates users walking in                
                startElevator(doorNotClose);
            }

            //if there are people in the elevator after dropping one or more passengers off, start going to their destination floor
            if (this.insideElevatorQueue.size() > 0) {

                //if elevator is above floor of the the destination of the first request, move down, else move up
                //we won't have to check if we are at the destination floor because the previous if statement took care of that
                if (currentFloor > insideElevatorQueue.get(0).getCarRequest()) {
                    output.printDirection(elevatorNum, false);
                    direction = "down";
                    elevatorMoveTiming();
                    currentFloor = currentFloor - 1; //move elevator down
                } else {
                    output.printDirection(elevatorNum, true);
                    direction = "up";
                    elevatorMoveTiming();
                    currentFloor = currentFloor + 1; //move elevator up
                }

            }

        }

        return currentFloor;
    }

    /**
     * Handle the timer for moving floors, timeout will activate if we take longer than the timeout
     * time to move between floors.
     * @author Matthew Belanger 101144323
     */
    public void elevatorMoveTiming() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                output.printElevatorStuckFault();
                elevatorIsStuck = true;
            }
        }, 9000); // delay for 9 seconds

        if (createElevatorStuckFault) { //Take too long to move to create fault (10 seconds)
            this.sleep(10000);
        } else { //Normal moving of the elevator (7.9 seconds)
            this.sleep(timeBetweenFloors); 
        }
        timer.cancel();
    }

    /**
     * This makes the program sleep for a provided duration of time
     * @param time in milliseconds (2000 is 2 seconds)
     * @author Nathan MacDiarmid 101098993
     */
    private void sleep(long time) {
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
        if (doorNotOpenError) {
            output.printDoorError(elevatorNum, true); //true if door is not opening
            this.sleep(4000);
            output.printDoorFixed(elevatorNum);
        }
        output.printDoorUpdate(elevatorNum, true);
        setDoorOpen(true);
        this.sleep(timeToLoadUnload); //sleep for the amount of time it takes to open the doors.

    }

    /**
     * Stops the motor, opens door, let's people walk in/out, and closes doors
     * @author Juanita Rodelo 101141857
     */
    public void startElevator(boolean doorNotCloseError) {

        if (doorNotCloseError) {
            output.printDoorError(elevatorNum, false); //false if doors not closing
            this.sleep(4000);
            output.printDoorFixed(elevatorNum);
            //Handle error
        }
        output.printDoorUpdate(elevatorNum, false);
        setDoorOpen(false);
        setMotorMoving(true);
    }

    /**
     * Prepares the message to send to the scheduler
     * @return the string to send the scheduler
     */
    public String prepareStatus() {

        String message = "";

        if (elevatorIsStuck) { //if the elevator has a stuck fault, send scheduler back the last request so that a different elevator can handle it
            InputData requestToSendBack = this.requestQueue.get(requestQueue.size() - 1); //send the last request in the requestQueue
            requestToSendBack.setElevatorStuckError(false);
            message = requestToSendBack.toString() + ": false";

            //Stop this elevator from handling any more requests
            this.noMoreRequests = true;
            this.requestQueue.clear();

        } else if (firstRequest) {
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
        } else{ // No move performed but we still requests
            message = "Elevator car #: " + elevatorNum
                    + " Floor: " + prevCurrentFloor
                    + " Num of people: " + numOfPeopleInsideElev
                    + " Serviced: " + numOfPeopleServiced
                    + " Direction: " + direction;
        }
        
        return message;
    }

    /**
    * Sends the status of the current elevator to the scheduler
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * @author Matthew Belanger 101144323
    */
    public void sendStatus() {

        String message = prepareStatus();

        byte[] msg = message.getBytes();
        // Creates the DatagramPacket to be sent to port 23
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 69);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Sends the DatagramPacket over port 23
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
    * Receives message from scheduler (either an instruction or no request)
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    */
    public void receiveInstruction() {

        // Initializes the receive DatagramPacket
        receivePacket = new DatagramPacket(data, data.length);
        // Receives the DatagramPacket
        try {
            sendAndReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        int len = receivePacket.getLength();
        String received = new String(data, 0, len);
        
        saveReceivedMessage(received);
    }

    /**
    * Parses the message received from the scheduler and saves it if it contains a request
    * @author Michael Kyrollos 101183521
    */
    public void saveReceivedMessage(String message) {
        //System.out.println("in save received message");
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
                time = LocalTime.parse((matcher.group(1)));
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
                this.requestQueue.add(request); // adds request to main request queue 

                //save whether the request has a faXult occuring
                this.closeDoorFaultByFloor.replace(request.getFloor(), request.getDoorNotCloseError());
                this.openDoorFaultByFloor.replace(request.getFloor(), request.getDoorNotOpenError());

                if (elevatorStuckError) {
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

    /**
     * Determines whether there are more requests that this elevator should expect
     * @return
     */
    public boolean isNoMoreRequests() {
        return noMoreRequests;
    }

    /**
     * THE FOLLOWING GETTERS ARE FOR TESTING PURPOSES ONLY
     */
    public DatagramPacket getReceivePacket() {
        return this.receivePacket;
    }

    /**
     * THE FOLLOWING GETTERS ARE FOR TESTING PURPOSES ONLY
     */
    public boolean isElevatorIsStuck() {
        return elevatorIsStuck;
    }

    public static void main(String args[]) {
        System.out.println(); 
        final int NUM_OF_FLOORS = 22;
        final long TIME_BETWEEN_FLOORS = 7900;
        final long TIME_TO_LOAD_UNLOAD = 2700;

        /*This maps an elevator instance to their finished status (true when done, false when not done) */
        LinkedHashMap<Elevator, Boolean> elevatorsFinished = new LinkedHashMap<>();
        Elevator elevator1 = new Elevator(1, 1, "up", NUM_OF_FLOORS, TIME_BETWEEN_FLOORS, TIME_TO_LOAD_UNLOAD);
        Elevator elevator2 = new Elevator(2, 1, "up", NUM_OF_FLOORS, TIME_BETWEEN_FLOORS, TIME_TO_LOAD_UNLOAD);
        Elevator elevator3 = new Elevator(3, 1, "up", NUM_OF_FLOORS, TIME_BETWEEN_FLOORS, TIME_TO_LOAD_UNLOAD);
        Elevator elevator4 = new Elevator(4, 1, "up", NUM_OF_FLOORS, TIME_BETWEEN_FLOORS, TIME_TO_LOAD_UNLOAD);
        elevatorsFinished.put(elevator1, false);
        elevatorsFinished.put(elevator2, false);
        elevatorsFinished.put(elevator3, false);
        elevatorsFinished.put(elevator4, false);

        // Save the system time at which the elevators start running
        long startTime = System.currentTimeMillis();

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
                    if (currElevator.getIsStuck()) {
                        currElevator.output.printStuckError(currElevator.elevatorNum);
                     } 
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
                    if (currElevator.getIsStuck()) {
                         currElevator.output.printStuckError(currElevator.elevatorNum);
                    } 
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

        // Save the system time at which the elevators stop running and output the total time it took to service all requests
        long stopTime = System.currentTimeMillis();
        long runTimeInSeconds = (stopTime - startTime) / 1000;
        elevator1.output.printTimeElapsed(runTimeInSeconds);
        elevator1.closeSocket();
        elevator2.closeSocket();
        elevator3.closeSocket();
        elevator4.closeSocket();
    }
}