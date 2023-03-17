package src;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

import javax.xml.transform.Source;

/**
Floor.java accepts events from InputData.java. Each event consists of the current time, the floor request, 
   the direction of travel and the car button pressed. These events are sent to Scheduler.java
*/ 
public class Floor  {

    // All requests will be stored in this sorted ArrayList
    private ArrayList<InputData> elevatorQueue;
    private boolean requestUpButton;
    private boolean requestDownButton;
    private String directionLamp;
    private boolean requestUpButtonLamp;
    private boolean requestDownButtonLamp;

    // Datagram stuff for sending and receiving over UDP
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;

    /**
     * Default constructor for Floor class
     * @param scheduler the Scheduler that is used as the middle man (Box class)
     * Also initializes {@link #elevatorQueue} ArrayList
     */
    public Floor() {
        this.elevatorQueue = new ArrayList<>();
        
        // Initializes the send and receive socket for the Floor
        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // The following methods are getters and setters for each of the attributes
    public Boolean getRequestUpButton() {
        return requestUpButton;
    }

    public void setRequestUpButton(Boolean requestUpButton) {
        this.requestUpButton = requestUpButton;
    }

    public Boolean getRequestDownButton() {
        return requestDownButton;
    }

    public void setRequestDownButton(Boolean requestDownButton) {
        this.requestDownButton = requestDownButton;
    }

    public String getDirectionLamp() {
        return directionLamp;
    }

    public void setDirectionLamp(String directionLamp) {
        this.directionLamp = directionLamp;
    }

    public Boolean getRequestUpButtonLamp() {
        return requestUpButtonLamp;
    }

    public void setRequestUpButtonLamp(Boolean isOn) {
        requestUpButtonLamp = isOn;
    }

    public Boolean getRequestDownButtonLamp() {
        return requestDownButtonLamp;
    }

    public void setRequestDownButtonLamp(Boolean isOn) {
        this.requestDownButtonLamp = isOn;
    }

    /**
     * The following method is ONLY FOR TESTING PURPOSES and
     * should not be included in commercial product.
     */
    public ArrayList<InputData> getElevatorQueue() {
        return elevatorQueue;
    }

    /**
     * Handles invalid input requests when parsing the data in data.txt
     * @param currentFloor the floor being requested from in the data.txt file
     * @param floorRequest the floor destination in the data.txt file
     * @param direction the direction the elevator is going
     * @return true if any of these inputs are invalid (negative, greater than 7 or not "up" or "down")
     * @return false otherwise
     * @author Nathan MacDiarmid 101098993
     */
    private boolean handleInputErrors(int currentFloor, int floorRequest, String direction) {
        if (currentFloor < 0 || currentFloor > 7) {
            return true;
        }

        if (floorRequest < 0 || floorRequest > 7) {
            return true;
        }

        if (!direction.equals("up") && !direction.equals("down")) {
            return true;
        }

        return false;
    }

    /**
     * Reads a file named data.txt that is in the same directory and parses through elevator requests. 
     * Creates a usable format for the rest scheduler.
     * @author Michael Kyrollos 101183521
     * @author Nathan MacDiarmid 101098993
     */
    public void readData(String filename) { //TODO: make sure that the destination floor is not equal to the initial floor of request

        String path = new File("").getAbsolutePath() + "/" + filename; //TODO: have a try-catch incase there is no data.txt file found

        try (Scanner input = new Scanner(new File(path))) {
            while (input.hasNextLine()) { // Check each value to verify if they are valid before adding them to elevatorQueue

                // Values are space-separated 
                String[] data = input.nextLine().split(" ");

                LocalTime time;
                int timeOfRequest;
                int currentFloor;
                int floorRequest;

                // Checks to make sure that only 4 pieces of data are passed from data.txt (time of request, current floor, direction, floor destination)
                // If more than 4 pieces are passed, it goes to the next line
                if (data.length > 4) {
                    continue;
                }

                // This try catch block handles if the input data are correct types, otherwise, goes to next line in data.txt
                try {
                    // Using the LocalTime class to parse through a standard time format of 'HH:MM:SS:XM'
                    time = LocalTime.parse((data[0]));

                    // converting the LocalTime to an integer, will stored as an int that represents the millisecond of the day
                    timeOfRequest = time.get(ChronoField.MILLI_OF_DAY);

                    //save current floor
                    currentFloor = Integer.parseInt(data[1]);

                    //save floor request
                    floorRequest = Integer.parseInt(data[3]);
                } catch (Exception e) {
                    continue;
                }

                // Skips input line if invalid input
                if (handleInputErrors(currentFloor, floorRequest, data[2])) {
                    continue;
                }

                //Creating new InputData class for every line in the txt, storing it in the elevator ArrayList
                elevatorQueue.add(new InputData(timeOfRequest, currentFloor, isGoingUp(data[2]), floorRequest));
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            e.printStackTrace();
        }

        // InputData.java implements the Comparable class, the 'sort' will be calling
        // the compareTo()
        // It is sorted in ascending order based on the 'timeOfRequest' of the request.
        Collections.sort(elevatorQueue);
        printInputData(elevatorQueue);
    }

    /**
     * Returns the direction that a given request is going in
     * Assumes that the data in the txt is in valid format and following our
     * standard
     * 
     * @param direction The Direction that has been parsed ("up" or "down")
     * @return true if "up" is the direction, false otherwise.
     * @author Michael Kyrollos
     */
    public boolean isGoingUp(String direction) {
        if (direction.equals("up")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Prints out the data that has been parsed from the "data.txt" file.
     * 
     * @author Michael Kyrollos
     */
    public void printInputData(ArrayList<InputData> queueToPrint) {
        for (InputData q : queueToPrint) {
            System.out.println(q);
        }
        System.out.println();
    }

    /**
     * Populates the elevatorQueue with the requests from the intput data file and calls the send a receive functions
     * @author Nathan MacDiarmid 101098993
     * @author Michael Kyrollos 101183521
     * @author Juanita Rodelo 101141857
     * @author Matthew Belanger 101144323
     */
    private void initiateFloor() {
        long startTime = System.currentTimeMillis();
        long firstRequestTime = elevatorQueue.get(0).getTimeOfRequest();
        boolean lastRequest = false; //tracks when the last request is being passed to the scheduler
        
        while(elevatorQueue.size() != 0) {
            if (elevatorQueue.size() == 1) { //If there is only one request left in the input file, set lastRequest to true
                lastRequest = true;
            }

            long timeOfR = elevatorQueue.get(0).getTimeOfRequest();
            long elapsedTime = System.currentTimeMillis() - startTime;

            if ((timeOfR - firstRequestTime) <= elapsedTime) {
                if (elevatorQueue.get(0).isDirectionUp()) {
                    setDirectionLamp("up");
                    setRequestUpButtonLamp(true);
                    setRequestUpButton(true);
                } else {
                    setDirectionLamp("down");
                    setRequestDownButtonLamp(true);
                    setRequestDownButton(true);
                }
                System.out.println("Floor: Someone on floor " + elevatorQueue.get(0).getFloor() + " has pressed the "
                        + getDirectionLamp() + " button...The " + getDirectionLamp() + " lamp is now on.");
                
                this.sendInstruction(getElevatorQueue().get(0), lastRequest);
                this.receiveAcknowledgement();

                elevatorQueue.remove(0);
                System.out.println("-------------------------------------------------------------------------------------------------");
            }
        }
    }

    /**
    * Sends an instruction to the scheduler via port 23
    * @param request the InputData request that holds request information
    * @param lastRequest whether the request is the last one in the elevatorQueue, boolean
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    */
    public void sendInstruction(InputData request, Boolean lastRequest) {
        // Prepares the message to be sent by forming a byte array
        String stringReq = request.toString();
        stringReq = stringReq + ": " + lastRequest;
        byte[] msg = stringReq.getBytes();
        System.out.println("Floor: sending a packet containing: " + stringReq);

        // Creates the DatagramPacket to be sent to port 23
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 23);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        int len = sendPacket.getLength();
        System.out.println("To host: " + sendPacket.getAddress() + ", on port: " + sendPacket.getPort() + ", with length: " + len);
        
        // Sends the DatagramPacket over port 23
        try {
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Floor: Packet sent.\n");
    }

    /**
    * Receives the acknowledgement from the Scheduler that it has received the message
    * and accepted the data.
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    */
    public void receiveAcknowledgement() {
        // Prepares the byte array for arrival
        // It is only of length 40 because the host only sends a byte
        // array of 40 in return ("The host has accepted the message.")
        byte data[] = new byte[40];
        receivePacket = new DatagramPacket(data, data.length);

        // Receives the DatagramPacket on the send and receive socket
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        int len = receivePacket.getLength(); //TODO: len (and other vars) could be a class variable, so we can use in receive and send
        System.out.println("Floor: Packet received from host: " + receivePacket.getAddress() + ", on port: " +  receivePacket.getPort() + ", with length: " + len);
        System.out.print("Containing: ");
        String received = new String(data,0,len);   
        System.out.println(received);
        System.out.println();
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Closes the open sockets when program ends
    */
    public void closeSocket() {
        sendReceiveSocket.close();
    }

    /**
     * THE FOLLOWING GETTERS AND SETTERS ARE FOR TESTING PURPOSES ONLY
     */
    public DatagramPacket getReceivePacket(){
        return this.receivePacket;
    }

    public DatagramPacket getSendPacket(){
        return this.sendPacket;
    }

    /**
     * Currently sends all requests in floor
     * Handles number of requests in queue abstractly
     * @param args
     */
    public static void main(String args[]) {
        System.out.println();
        Floor floor = new Floor();
        floor.readData("data.txt");
        floor.initiateFloor();
        floor.closeSocket();
    }
}