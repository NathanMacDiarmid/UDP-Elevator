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

public class Scheduler {

    /* noMoreRequests tracks when the floor subsysytem will send last request */
    private boolean noMoreRequests;
    private boolean elevatorsExecutingInstructions = false;

    /* requestQueue used as priority queue of requests */
    private ArrayList<InputData> requestQueue;

    /* elevatorAndTheirPorts used to map Elevator car number with their port*/
    Map<Integer, Integer> elevatorAndTheirPorts = new HashMap<Integer, Integer>();

    private DatagramPacket sendPacket, receivePacket23, receivePacket69;
    private DatagramSocket sendAndReceiveSocket, receiveSocket23, receiveSocket69;
    private byte[] data = new byte[100];
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
        System.out.println("Scheduler: num of cars = " + this.numOfCars);

        //for each elevator car, initialize current floor to 0
        for (int i = 0; i < this.numOfCars; i++) {
            //Initializing number of elevators in Map:
            this.elevatorsInfo.put(i + 1, new int[3]);
            this.elevatorsInfo.get(i + 1)[0] = 0; //initial floor: 0
            this.elevatorsInfo.get(i + 1)[1] = 0; //initial number of people: 0
            this.elevatorsInfo.get(i + 1)[2] = 0; // intial direction 0 (down)
        }

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
     * Gets number of cars
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
     * Getter method used for testing purposes
     * 
     * @return True if there are no requests left.
     * @author Michael Kyrollos
     */
    public boolean isNoMoreRequests() {
        return noMoreRequests;
    }

    /**
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     * Receive method for Host that receives the message from the floor
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
            System.exit(1);
        }

        System.out.println("Scheduler: Packet received from Floor:");
        System.out.println("From host: " + receivePacket23.getAddress() + ", on port: " + receivePacket23.getPort());
        int len = receivePacket23.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        String received = new String(data, 0, len);
        translateStringInstruction(received);
        System.out.println(received);
        System.out.println();
    }

    /**
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     * Sends to acknowledgement to the floor that the data has been
     * received and accepted by the scheduler.
     */
    public void sendFloorAcknowledgement() {
        String message = "The Scheduler has accepted the message.";
        byte[] msg = message.getBytes();

        // Initializes the DatagramPacket to send to the floor
        sendPacket = new DatagramPacket(msg, msg.length,
                receivePacket23.getAddress(), receivePacket23.getPort());

        System.out.println("Scheduler: Sending packet acknowledgment to Floor:");
        System.out.println("To host: " + sendPacket.getAddress() + ", on port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
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
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     * Receives the Elevators request for information before
     * being sent to the Elevator.
     * Return port that the packet was received on (to keep track of ports of all elevator cars)
     */
    public int receiveElevatorRequest() {

        // Initializes the DatagramPacket to be received from the Server
        byte[] request = new byte[100];
        receivePacket69 = new DatagramPacket(request, request.length);
        System.out.println("Scheduler: Waiting for Packet from elevator.\n");

        // Receives the DatagramPacket from the Server
        try {
            receiveSocket69.receive(receivePacket69);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scheduler: Packet received from Elevator:");
        System.out.println("From host: " + receivePacket69.getAddress() + ", on port: " + receivePacket69.getPort());
        int len = receivePacket69.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        String received = new String(request);
        System.out.println(received);
        saveElevatorStatus(received);
        System.out.println();

        return receivePacket69.getPort();

    }

    /**
     * This method will save the message received from the elevators into the Map 'elevatorLocations'
     * @param input string received from floor
     * @author Michael Kyrollos 101183521
     * @author Amanda Piazza 101143004
     */
    public void saveElevatorStatus(String input) {
        // split by whitespace
        // receiving it in format 'Elevator car #: 2 Floor: 2 Num of people: 3 Serviced: 1 '

        String[] tokens = input.split(" ");
        int car = Integer.parseInt(tokens[3]);
        int floorNum = Integer.parseInt(tokens[5]);
        int numPeople = Integer.parseInt(tokens[9].trim());
        String direction = tokens[13];
        int directionValue;
        if (direction.equals("down")) {
            directionValue = 0;
        } else {
            directionValue = 1;
        }

        //save elevator location in elevatorLocations()
        this.elevatorsInfo.get(car)[0] = floorNum;
        this.elevatorsInfo.get(car)[1] = numPeople;
        //set direction
        this.elevatorsInfo.get(car)[2] = directionValue;
    }

    /**
     * getElevatorToSendRequest determines closest elevator (while ensuring all elevators hold roughly the same number of people)
     * @return returns the elevator # of the elevator to send request to
     * @author Nathan MacDiarmid 101098993
     * @author Matthew Belanger 101144323
     */
    public int getElevatorToSendRequest() {
        InputData currentRequest = this.requestQueue.get(0);
        System.out.println("Scheduler deciding where to send request: " + currentRequest.toString());

        int closestElevatorNum = 1;
        int currentBestFloorDifference = this.elevatorsInfo.get(1)[0] - currentRequest.getFloor();
        Boolean currentDirectionIsCorrect;
        int elevatorsDistanceDifference = 0;

        //Compare elevator #1's info
        if (this.elevatorsInfo.get(1)[2] == 1) { //if elevator is going up
            if (currentRequest.getIsDirectionUp()) { //if passenger of request is also going up
                currentDirectionIsCorrect = true; //current elevator direction matches direction of requestt direction
            } else {
                currentDirectionIsCorrect = false; //else car moving in different direction of request
            }
        } else { //elavator is going down
            if (currentRequest.getIsDirectionUp()) { //if passenger of request is going up
                currentDirectionIsCorrect = false;
            } else {
                currentDirectionIsCorrect = true; //current elevator direction matches direction of request
            }
        }

        //Iterate through the rest of the elevators
        for (int i = 2; i < this.elevatorsInfo.size() + 1; i++) {
            Boolean directionIsUp;
            elevatorsDistanceDifference = this.elevatorsInfo.get(i)[0] - currentRequest.getFloor();

            if (this.elevatorsInfo.get(i)[2] == 1) { //if elevator is going up
                directionIsUp = true;
            } else {
                directionIsUp = false;
            }

            if (currentRequest.getIsDirectionUp() == directionIsUp) { //if direction of current request is up

                //if the difference between the floor that the elevator is on and the floor of the request is less than 0
                //elevator is below
                if (elevatorsDistanceDifference <= 0) {
                    //if the previous elevator # is also below the floor of the request is also less than 0
                    if (currentBestFloorDifference <= 0 && currentDirectionIsCorrect) {
                        //next elevator is closer to request
                        if (Math.abs(elevatorsDistanceDifference) < Math.abs(currentBestFloorDifference)) {
                            closestElevatorNum = i; //update elevator to next elevator
                            currentBestFloorDifference = elevatorsDistanceDifference;
                            currentDirectionIsCorrect = directionIsUp;
                        }
                    }
                }
            } else if (!currentRequest.getIsDirectionUp() == !directionIsUp) {

                //if the difference between the floor that the elevator is on and the floor of the request is less than 0
                //elevator is above
                if (elevatorsDistanceDifference >= 0) {
                    //next elevator can service elevator (going in same direction)
                    if (currentBestFloorDifference >= 0 && currentDirectionIsCorrect) {
                        //next elevator is closer to request
                        if (Math.abs(elevatorsDistanceDifference) < Math.abs(currentBestFloorDifference)) {
                            closestElevatorNum = i; //update elevator to next elevator
                            currentBestFloorDifference = elevatorsDistanceDifference;
                            currentDirectionIsCorrect = directionIsUp;
                        }
                    }
                }
            } else {
                if (!currentDirectionIsCorrect) {
                    //next elevator is closer to request
                    if (Math.abs(elevatorsDistanceDifference) < Math.abs(currentBestFloorDifference)) {
                        closestElevatorNum = i; //update elevator to next elevator
                        currentBestFloorDifference = elevatorsDistanceDifference;
                        currentDirectionIsCorrect = directionIsUp;
                    }
                }
            }
        }

        //remove request from queue because elevator now has it and will service it
        this.requestQueue.remove(0);

        return closestElevatorNum;
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Send method that sends the instruction to the Elevator to the specified port
    * In this case, the specified port is port 69
    */
    public void sendToElevators() {
        int elevatorToSendRequest = 0;
        if (!elevatorsExecutingInstructions) {
            elevatorToSendRequest = getElevatorToSendRequest();
        } else {
            elevatorToSendRequest = -1;
        }

        byte[] msgToSend;
        int portOfElevator;

        Set<Integer> elevatorNums = elevatorAndTheirPorts.keySet();
        Iterator<Integer> keyIterator = elevatorNums.iterator();

        //Iterator through map that holds elevators and their ports
        while (keyIterator.hasNext()) {
            Integer currElevatorNum = keyIterator.next();
            System.out.println(
                    "Scheduler: current elevator we are responding to: " + elevatorAndTheirPorts.get(currElevatorNum));
            portOfElevator = elevatorAndTheirPorts.get(currElevatorNum);

            //send nothing
            if (currElevatorNum != elevatorToSendRequest) { //if the elevator car number does not match elevator servicing request
                String message = "No current requests"; //current floor has no requests 
                msgToSend = message.getBytes();
            } else { //send request                
                msgToSend = data;
            }

            // Initializes the DatagramPacket to be sent to the server
            sendPacket = new DatagramPacket(msgToSend, msgToSend.length,
                    receivePacket69.getAddress(), portOfElevator);

            System.out.println("Scheduler: Sending packet:");
            System.out.println("To host: " + sendPacket.getAddress() + ", on port: " + portOfElevator);
            int len = sendPacket.getLength();
            System.out.println("Length: " + len);
            System.out.print("Containing: ");
            System.out.println(new String(sendPacket.getData(), 0, len));
            System.out.println("Scheduler: packet sent to Elevator #: " + currElevatorNum + "\n");

            // Sends the DatagramPacket to the Server
            try {
                sendAndReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        // Clears the request pipeline
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
        this.elevatorsExecutingInstructions = noMoreRequests;
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Receives the Floors request for information before
    * being sent to the Floor
    */

    public void receiveFloorRequest() {
        // Initializes the DatagramPacket to be received from the Floor
        byte[] request = new byte[20];
        receivePacket23 = new DatagramPacket(request, request.length);
        System.out.println("Scheduler: Waiting for Packet.\n");

        // Receives the DatagramPacket from the Floor
        try {
            System.out.println("Waiting...");
            receiveSocket23.receive(receivePacket23);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scheduler: Packet received from Floor:");
        System.out.println("From host: " + receivePacket23.getAddress());
        int len = receivePacket23.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        String received = new String(request);
        System.out.println(received + "\n");
    }

    /**
    * translateStringInstruction parses the request coming from the floor
    * @author Michael Kyrollos 101183521
    * @author Amanda Piazza 101143004
    */
    public void translateStringInstruction(String instruction) {
        // String format: InputData [currentTime=16:48:10.0, floor=6, isDirectionUp=false, car button=3]: true

        InputData request;
        int currentTime;
        int floor;
        boolean isDirectionUp;
        int carButton;
        boolean lastRequest;
        Pattern pattern = Pattern.compile(
                "\\[currentTime=(\\d+:\\d+:\\d+\\.\\d+), floor=(\\d+), isDirectionUp=(\\w+), car button=(\\d+)\\]: (\\w+)");
        Matcher matcher = pattern.matcher(instruction);
        LocalTime time;

        //parse through the pattern to extract data for the given request 
        if (matcher.find()) {
            time = LocalTime.parse((matcher.group(1)));
            currentTime = time.get(ChronoField.MILLI_OF_DAY);
            floor = Integer.parseInt(matcher.group(2));
            isDirectionUp = Boolean.parseBoolean(matcher.group(3));
            carButton = Integer.parseInt(matcher.group(4));
            lastRequest = Boolean.parseBoolean(matcher.group(5));

            request = new InputData(currentTime, floor, isDirectionUp, carButton);

            //Add request to elevatorQueue
            this.requestQueue.add(request); // adds request to main request queue
            this.noMoreRequests = lastRequest;
        }
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * Closes the open sockets when program ends
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

    /**
     * Currently sends all requests in floor
     * Handles number of requests in queue conretely (hard coded)
     * @param args
     */
    public static void main(String args[]) {
        int elevatorPort;
        Scheduler scheduler = new Scheduler(2);

        while (true) {
            // Check if request received is last request, if true stop receiving more instructions
            if (!scheduler.noMoreRequests) {
                scheduler.receiveInstructionFromFloor();
                scheduler.sendFloorAcknowledgement();
            }

            // Call receive requests method n times where n is the number of elevators
            for (int i = 1; i < scheduler.getNumOfCars() + 1; i++) {
                elevatorPort = scheduler.receiveElevatorRequest();
                scheduler.elevatorAndTheirPorts.put(i, elevatorPort);
            }

            scheduler.sendToElevators();
        }
    }
}