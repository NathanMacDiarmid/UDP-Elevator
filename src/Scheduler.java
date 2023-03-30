package src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the scheduler that manages the requests coming from the floor to the elevators
 */
public class Scheduler {

    /* noMoreRequests tracks when the floor subsysytem will send last request */
    private boolean noMoreRequests;

    private boolean elevatorsExecutingInstructions = false;

    /* requestQueue used as priority queue of requests */
    private ArrayList<InputData> requestQueue;

    /* elevatorAndTheirPorts maps elevator car numbers with their port*/
    Map<Integer, Integer> elevatorAndTheirPorts = new HashMap<Integer, Integer>();

    private DatagramPacket sendPacket, receivePacket23, receivePacket69;
    private DatagramSocket sendAndReceiveSocket, receiveSocket23, receiveSocket69;
    private byte[] data = new byte[200];

    /* Keeps track of the number of elevator cars */
    private int numOfCars;

    /* This maps the elevator number with their current floor and the number people currently in it*/
    private Map<Integer, int[]> elevatorsInfo;

    /**
     * Default constructor for Scheduler
     * Initializes all of the attributes
     */
    public Scheduler(int numOfCars) {

        this.numOfCars = numOfCars;
        this.requestQueue = new ArrayList<InputData>();
        this.noMoreRequests = false;
        this.elevatorsInfo = new HashMap<Integer, int[]>();
        //System.out.println("Scheduler: num of cars = " + this.numOfCars);

        //for each elevator car, initialize their information fields
        for (int i = 0; i < this.numOfCars; i++) {
            this.elevatorsInfo.put(i + 1, new int[5]);
            this.elevatorsInfo.get(i + 1)[0] = 0; //initial floor: 0
            this.elevatorsInfo.get(i + 1)[1] = 0; //initial number of people: 0
            this.elevatorsInfo.get(i + 1)[2] = 0; // intial direction: 0 (down)
            this.elevatorsInfo.get(i + 1)[3] = 0; // initial amount of people serviced: 0
            this.elevatorsInfo.get(i + 1)[4] = 0; // initial amount of requests sent to this elevator: 0
        }

        //creates sockets to receive from floor and elevators
        try {
            this.sendAndReceiveSocket = new DatagramSocket();
            this.receiveSocket23 = new DatagramSocket(23);
            this.receiveSocket69 = new DatagramSocket(69);
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Gets the number of elevator cars
     * @return the number of cars that the Scheduler is controlling
     * @author Juanita Rodelo 101141857
     * @author Amanda Piazza 101143004
     */
    public int getNumOfCars() {
        return this.numOfCars;
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
     * gets whether there are more requests
     * 
     * @return True if there are no requests left.
     * @author Michael Kyrollos
     */
    public boolean isNoMoreRequests() {
        return noMoreRequests;
    }

    /**
     * receives message containing requests from the floor
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     */
    public void receiveInstructionFromFloor() {
        // Initializes the DatagramPacket to be received from the floor
        receivePacket23 = new DatagramPacket(data, data.length);
        System.out.println("Scheduler: Waiting for Packet from Floor.");

        // Receives the DatagramPacket from the floor
        try {
            receiveSocket23.receive(receivePacket23);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1); //TODO: should the system exit in this case?
        }

        int len = receivePacket23.getLength();
        String received = new String(data, 0, len);
        System.out.println("Scheduler: Packet received from Floor host: " + receivePacket23.getAddress() + ", on port: "
                + receivePacket23.getPort() + ", with length: " + len);
        System.out.print("Containing: ");

        translateStringInstruction(received, false);
        System.out.println(received);
        System.out.println();
    }

    /**
     * Sends acknowledgement to the floor that the data has been
     * received and accepted by the scheduler
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     */
    public void sendFloorAcknowledgement() {
        String message = "The Scheduler has accepted the message.";
        byte[] msg = message.getBytes();

        // Initializes the DatagramPacket to send to the floor
        sendPacket = new DatagramPacket(msg, msg.length,
                receivePacket23.getAddress(), receivePacket23.getPort());

        int len = sendPacket.getLength();
        System.out.println("Scheduler: Sending packet acknowledgment to Floor host: " + sendPacket.getAddress()
                + ", on port: " + sendPacket.getPort() + len);
        System.out.println("To host: " + sendPacket.getAddress() + ", on port: " + sendPacket.getPort());
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(), 0, len));
        System.out.println();

        // Sends the DatagramPacket to the floor
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Receives Elevators status and calls function to save elevator information
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     * @author Matthew Belanger 101144323
     * @return port that the packet was received on (to keep track of ports of all elevator cars)
     */
    public int receiveElevatorStatus() {

        // Initializes the DatagramPacket to be received from the Server
        byte[] request = new byte[200];
        receivePacket69 = new DatagramPacket(request, request.length);
        System.out.println("Scheduler: Waiting for Packet from elevator.");

        // Receives the DatagramPacket from the Server
        try {
            receiveSocket69.receive(receivePacket69);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        int len = receivePacket69.getLength();
        String received = new String(request);
        System.out.println("Scheduler: Packet received from Elevator host:" + receivePacket69.getAddress()
                + ", on port: " + receivePacket69.getPort() + ", with length: " + len);
        System.out.print("Containing: ");
        System.out.println(received);

        //Check if this is a normal messsga or an emergency message and handle accordingly
        if (received.split(" ", 2)[0].equals("InputData")) {
            /** This will add the request back to the queue of requests so it can be handled
            *  by another elevator, if we have all ready recived all requests from floor
            * class then we will have to reset elevatorsExecutingInstructions so it
            * can execute this last instruction.
            */

            if (noMoreRequests) {
                this.elevatorsExecutingInstructions = false;
            }
            translateStringInstruction(received, true);
            data = request; //So the request can be sent to another elevator

            return -1; //Return -1 so that the scheduler knows there was a problem with this elevator
        }

        saveElevatorStatus(received);
        System.out.println();

        return receivePacket69.getPort();

    }

    /**
     * Saves the status received from the elevator into the Map 'elevatorsInfo'
     * @param input string received from floor
     * @author Michael Kyrollos 101183521
     * @author Amanda Piazza 101143004
     */
    public void saveElevatorStatus(String input) {

        // split by whitespace
        // receiving it in format 'Elevator car #: 2 Floor: 2 Num of people: 3 Serviced: 1 '
        String[] tokens = input.split(" ");
        int car = Integer.parseInt(tokens[3]); //TODO: maybe have a try-catch around this parsing, we are checking this before sending it in the floor so idk if this is necessary to double check
        int floorNum = Integer.parseInt(tokens[5]);
        int numPeople = Integer.parseInt(tokens[9].trim());
        int peopleServiced = Integer.parseInt(tokens[11].trim());
        String direction = tokens[13];
        int directionValue;

        if (direction.equals("down")) {
            directionValue = 0;
        } else {
            directionValue = 1;
        }

        //save elevator location in elevatorsInfo
        this.elevatorsInfo.get(car)[0] = floorNum;
        this.elevatorsInfo.get(car)[1] = numPeople;
        this.elevatorsInfo.get(car)[2] = directionValue;
        this.elevatorsInfo.get(car)[3] = peopleServiced;
    }

    /**
     * Determines the best elevator to send request to
     * @return returns the elevator # of the elevator to send request to
     * @author Nathan MacDiarmid 101098993
     * @author Matthew Belanger 101144323
     */
    public int getElevatorForRequest() {

        InputData currentRequest = this.requestQueue.get(0);
        System.out.println("Scheduler deciding where to send request: " + currentRequest.toString() + "\n");

        //initially sets this to elevator #1 as base case
        int chosenElevator = 1;
        //initially set to the difference of the first elevator's current floor with the request's current floor
        int bestFloorDifference = this.elevatorsInfo.get(1)[0] - currentRequest.getFloor();
        Boolean directionMatches;
        //int elevatorDistanceDifference = 0;

        //if elevator #1's direction is up
        if (this.elevatorsInfo.get(1)[2] == 1) {
            if (currentRequest.getIsDirectionUp()) { //if request is also going up
                directionMatches = true;
            } else {
                directionMatches = false;
            }
        } else { //elavator #1 is going down
            if (currentRequest.getIsDirectionUp()) { //if request is going up
                directionMatches = false;
            } else {
                directionMatches = true;
            }
        }

        //Iterate through the rest of the elevators to compare their distance difference and direction to determine most efficient elevator to send to request to
        for (int currElevator = 2; currElevator < this.elevatorsInfo.size() + 1; currElevator++) {
            Boolean directionIsUp;
            int elevatorDistanceDifference = this.elevatorsInfo.get(currElevator)[0] - currentRequest.getFloor();

            if (this.elevatorsInfo.get(currElevator)[2] == 1) { //if elevator is going up
                directionIsUp = true;
            } else {
                directionIsUp = false;
            }

            if (currentRequest.getIsDirectionUp() == directionIsUp) { //if direction of current request is up and the direction of the elevator is up

                //if the the elevator is below the floor of the request
                if (elevatorDistanceDifference <= 0) {
                    //if the previous elevator is also below the floor of the request and it's direction is the same as the request
                    if (bestFloorDifference <= 0 && directionMatches) {
                        //check if the previous floor difference is less than the current elevator's floor difference
                        if (Math.abs(elevatorDistanceDifference) < Math.abs(bestFloorDifference)) {
                            //then current elevator is closer to request than the previous
                            chosenElevator = currElevator;
                            bestFloorDifference = elevatorDistanceDifference;
                            directionMatches = directionIsUp;
                        }
                    }
                }
            } else if (!currentRequest.getIsDirectionUp() == !directionIsUp) { //else direction of current request is down and direction of elevator is down

                //if the the elevator is above the floor of the request
                if (elevatorDistanceDifference >= 0) {
                    //if the previous elevator is also above the floor of the request and it's direction is the same as the request
                    if (bestFloorDifference >= 0 && directionMatches) {
                        //check if the previous floor difference is less than the current elevator's floor difference
                        if (Math.abs(elevatorDistanceDifference) < Math.abs(bestFloorDifference)) {
                            //then current elevator is closer to request than the previous
                            chosenElevator = currElevator;
                            bestFloorDifference = elevatorDistanceDifference;
                            directionMatches = directionIsUp;
                        }
                    }
                }
            } else { //else direction of request does not match direction of elevator -> in this case only take floor difference into consideration

                if (!directionMatches) {
                    //check if the previous floor difference is less than the current elevator's floor difference
                    if (Math.abs(elevatorDistanceDifference) < Math.abs(bestFloorDifference)) {
                        chosenElevator = currElevator;
                        bestFloorDifference = elevatorDistanceDifference;
                        directionMatches = directionIsUp;
                    }
                }
            }
        }

        //remove request from queue because elevator now has it and will service it
        this.requestQueue.remove(0);

        return chosenElevator;
    }

    /**
    * Sends instruction to the elevator that was returned by getElevatorForRequest() through respective elevator port 
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    */
    public void sendToElevators() {

        int elevatorToSendRequest = 0;
        byte[] msgToSend;
        int portOfElevator;
        String message;

        Set<Integer> elevatorNums = elevatorAndTheirPorts.keySet();
        Iterator<Integer> keyIterator = elevatorNums.iterator();

        if (!elevatorsExecutingInstructions) { //if there are requests for the elevator to service
            elevatorToSendRequest = getElevatorForRequest();
            elevatorsInfo.get(elevatorToSendRequest)[4]++; // increments amount of requests that elevator is sent
        } else { //if there are no elevators to service 
            elevatorToSendRequest = -1; //set elevator to an invalid elevator value 
        }

        //Iterate through map that holds elevators and their ports
        while (keyIterator.hasNext()) {
            Integer currElevatorNum = keyIterator.next();
            portOfElevator = elevatorAndTheirPorts.get(currElevatorNum);

            //if the current elevator does not match the elevator # to send request to
            if (currElevatorNum != elevatorToSendRequest) {
                if (isNoMoreRequests()) { //check if there are no more requests coming from Floor //TODO: I don't think this is necessary 
                    message = "No more requests";
                    msgToSend = message.getBytes();
                } else {
                    message = "No current requests"; //current floor has no requests 
                    msgToSend = message.getBytes();
                }
            } else { //the elevator recieving the request matches the current elevator in iterator                
                msgToSend = data;
            }

            // Initializes the DatagramPacket to be sent to the server
            sendPacket = new DatagramPacket(msgToSend, msgToSend.length, receivePacket69.getAddress(), portOfElevator);

            int len = sendPacket.getLength();
            System.out.println("Scheduler: Sending packet to host: " + sendPacket.getAddress() + ", on port: "
                    + portOfElevator + ", with length: " + len);
            System.out.print("Containing: ");
            System.out.println(new String(sendPacket.getData(), 0, len)); //TODO: make sure above and below sending and receiving methods do this instead of creating a string just to print it

            // Sends the DatagramPacket to the Server
            try {
                sendAndReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
            System.out.println("Scheduler: packet sent to Elevator #: " + currElevatorNum + "\n");
        }
        // Clears the request pipeline
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        this.elevatorsExecutingInstructions = noMoreRequests;
    }

    /**
    * Parses the request coming from the floor and saves it in requestQueue and other variables
    * @author Michael Kyrollos 101183521
    * @author Amanda Piazza 101143004
    * @author Matthew Belanger 101144323
    */
    public void translateStringInstruction(String instruction, boolean ignoreLastRequest) {
        InputData request;
        int currentTime;
        int floor;
        boolean isDirectionUp;
        int carButton;
        boolean lastRequest;
        boolean doorNotOpenError;
        boolean doorNotCloseError;
        boolean elevatorStuckError;
        Pattern pattern = Pattern.compile(
                "\\[currentTime=(\\d+:\\d+:\\d+\\.\\d+), floor=(\\d+), isDirectionUp=(\\w+), car button=(\\d+), doorNotOpenError=(\\w+), doorNotCloseError=(\\w+), elevatorStuckError=(\\w+)\\]: (\\w+)");
        Matcher matcher = pattern.matcher(instruction);
        LocalTime time;
        //parse through the pattern to extract data for the given request 
        if (matcher.find()) {
            time = LocalTime.parse((matcher.group(1))); //TODO: maybe we should have try-catches around these to double check
            currentTime = time.get(ChronoField.MILLI_OF_DAY);
            floor = Integer.parseInt(matcher.group(2));
            isDirectionUp = Boolean.parseBoolean(matcher.group(3));
            carButton = Integer.parseInt(matcher.group(4));
            doorNotOpenError = Boolean.parseBoolean(matcher.group(5));
            doorNotCloseError = Boolean.parseBoolean(matcher.group(6));
            elevatorStuckError = Boolean.parseBoolean(matcher.group(7));
            lastRequest = Boolean.parseBoolean(matcher.group(8));

            request = new InputData(currentTime, floor, isDirectionUp, carButton, doorNotOpenError, doorNotCloseError,
                    elevatorStuckError);

            //Add request to elevatorQueue
            this.requestQueue.add(request);

            // If we are getting a request sent back from an elevator then ignore the lastRequest value
            if (!ignoreLastRequest) {
                this.noMoreRequests = lastRequest;
            }
        }

    }

    /**
    * Closes the open sockets when program ends
    * @author Nathan MacDiarmid 101098993
    */
    public void closeSockets() {
        sendAndReceiveSocket.close();
        receiveSocket23.close();
        receiveSocket69.close();
    }

    /**
     * THE FOLLOWING GETTERS AND SETTERS ARE FOR TESTING PURPOSES ONLY
     */
    public DatagramPacket getReceivePacket23() {
        return this.receivePacket23;
    }

    public DatagramPacket getReceivePacket69() {
        return this.receivePacket69;
    }

    public void elevatorAndTheirPortsPut(int i, int elevatorPort) {
        this.elevatorAndTheirPorts.put(i, elevatorPort);
    }

    public Map<Integer, int[]> getElevatorInfo() {
        return elevatorsInfo;
    }

    public static void main(String args[]) {
        System.out.println();
        int elevatorPort;
        Scheduler scheduler = new Scheduler(2);
        int elevatorsDone = 0;

        //while all elevators are not done sending requests and receving (because they're done servicing all their requests)
        while (elevatorsDone <= scheduler.getNumOfCars()) {

            // Check if request received is last request, if true stop receiving more instructions
            if (!scheduler.noMoreRequests) {
                scheduler.receiveInstructionFromFloor();
                scheduler.sendFloorAcknowledgement();
            }

            // Call receive requests method n times where n is the number of elevators
            for (int i = 1; i < scheduler.getNumOfCars() + 1; i++) {

                // if the amount of requests sent to an elevator is equal the amount of people that elevator has serviced AND theres no more requests coming from floor
                if ((scheduler.getElevatorInfo().get(i)[3] == scheduler.getElevatorInfo().get(i)[4]
                        && scheduler.isNoMoreRequests())) {
                    elevatorsDone++; //increment the number of elevators that have completely finished serivicing their requests
                } else {
                    elevatorPort = scheduler.receiveElevatorStatus();

                    //If -1 was returned there was an error with this elevator and we need to remove it
                    if (elevatorPort == -1) {
                        scheduler.numOfCars -= 1;
                        scheduler.elevatorAndTheirPorts.remove(i);
                        scheduler.elevatorsInfo.remove(i);
                    } else {
                        scheduler.elevatorAndTheirPorts.put(i, elevatorPort); //save elevator port in Map
                    }
                }
            }

            scheduler.sendToElevators();
            System.out.println(
                    "-------------------------------------------------------------------------------------------------");
        }
        scheduler.closeSockets();
    }
}