import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the Elevator sub-system
 * It moves between floors based on instructions passed from data.txt
 */
public class Elevator implements Runnable {
    private int currentFloor = 0;
    private Scheduler scheduler;
    private Boolean motorMoving;
    private Boolean doorOpen;
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendAndReceiveSocket;
    private byte[] data = new byte[100];

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
        { // TODO: can we just have one map that represents buttons and their lights
          // (integer, boolean(pressed or not), String("light on"/"light off"))
            put(1, false);
            put(2, false);
            put(3, false);
            put(4, false);
            put(5, false);
            put(6, false);
            put(7, false);
        }
    };
 
    /**
     * Default constructor for Elevator
     * 
     * @param scheduler the Scheduler instance that needs to be passed (Box class)
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.requestQueue = new ArrayList<InputData>();
        try {
            this.sendAndReceiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // The following methods are getters and setters for each of the attributes
    public ArrayList<InputData> getRequestQueue() {
        return requestQueue;
    }

    public void setFloorButton(Integer floor, Boolean buttonPressed) {
        floorButtons.replace(floor, buttonPressed);
    }

    public void setFloorButtonLamps(Integer floor, Boolean buttonLampOn) {
        floorButtonsLamps.replace(floor, buttonLampOn);
    }

    public void setMotorMoving(Boolean motorMoving) {
        this.motorMoving = motorMoving;
    }

    public void setDoorOpen(Boolean doorOpen) {
        this.doorOpen = doorOpen;
    }

    public Boolean getMotorMoving() {
        return motorMoving;
    }

    public Boolean getDoorOpen() {
        return doorOpen;
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
  
    @Override
    /**
     * The run method for the Elevator class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     * @author Nathan MacDiarmid 101098993
     * @author Juanita Rodelo 101141857
     */
    public void run() {
        String currentThreadName = Thread.currentThread().getName();
        Boolean noMoreRequestsComing = false;
        int oldCurrentFloor; // this keeps track of the previous floor visited by elevator
        System.out.println(currentThreadName + ": Current floor is: " + this.currentFloor + "\n");

        while (true) {
            oldCurrentFloor = this.currentFloor;

            if (!noMoreRequestsComing) { // if there are more requests to grab

                Map<InputData, Boolean> request = scheduler.getFloorRequest(); // grab them
                for (InputData r : request.keySet()) {
                    requestQueue.add(r);
                    noMoreRequestsComing = request.get(r);
                }

            }

            if (requestQueue.size() > 0) { // if there are currently requests to service

                // ask scheduler to notify elevator when it has arrived at destination floor and/or picked someone up along the way
                this.currentFloor = scheduler.moveElevator(requestQueue, this.currentFloor);

                if (this.currentFloor == oldCurrentFloor) { // if oldCurrentFloor is equal to new current floor, elevator did not move
                    setMotorMoving(false);
                    System.out.println(currentThreadName + ": Motor stopped moving");
                    setDoorOpen(true);
                    System.out.println("Doors opening -> People are walking in/out");
                    this.sleep(2700); //sleep for the amount of time it takes to open the doors.
                    System.out.println("Doors are closing");
                    setDoorOpen(false);
                } else {
                    setMotorMoving(true);
                    System.out.println(currentThreadName + ": Motor moving");
                }
    
                this.sleep(7970); //sleep for the amount of time it takes to move between floors.
                System.out.println(currentThreadName + ": Current floor is now: " + this.currentFloor + "\n");
            }
        }
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Send method for the Elevator sends the emoji message
    * back to wherever the message received came from
    */
    public void sendSatus() {
        // Initializes byte array with only for bits
        byte[] msg = new byte[4];

        // Sets the byte array to a heart and smiley emoji
        if (data[1] == 1) {
            msg[0] = 0;
            msg[1] = 3;
            msg[2] = 0;
            msg[3] = 1;
        }
        // Sets the byte array to a diamond emoji
        if (data[1] == 2) {
            msg[0] = 0;
            msg[1] = 4;
            msg[2] = 0;
            msg[3] = 0;
        }

        // Initializes the DatagramPacket to be sent
        sendPacket = new DatagramPacket(msg, msg.length, receivePacket.getAddress(), receivePacket.getPort());

        System.out.println( "Elevator: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len));
        System.out.println();

        // Sends the DatagramPacket to the specified port
        // In this case, it sends it to where the initial message was sent from
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Elevator: packet sent");
   }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Sends the request for the data that is held by the Scheduler
    */
    public void sendRequest() {
        // Prepares the message to be sent by forming a byte array
        String message = "Can I get the deats?";
        byte[] msg = message.getBytes();

        System.out.println("Elevator: sending a packet containing: " + message);

        // Creates the DatagramPacket to be sent to port 23
        try {
            sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), 69);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);

        // Sends the DatagramPacket over port 23
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Elevator: Request sent.\n");
 }

   /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Receive method for Elevator receives the message
    * from wherever the message was sent from
    */
    public void receiveInstruction() {
        // Initializes the receive DatagramPacket
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Elevator: Waiting for Packet.\n");

        // Receives the DatagramPacket
        try {        
            System.out.println("Waiting...");
            sendAndReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Elevator: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );

        String received = new String(data,0,len);   
        System.out.println(received);
        System.out.println();
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Receives the acknowledgement from the Scheduler that it has received the message
    * and accepted the data.
    */
    public void receiveAcknowledgement() {
        // Prepares the byte array for arrival
        // It is only of length 40 because the host only sends a byte
        // array of 40 in return ("The host has accepted the message.")
        byte data[] = new byte[40];
        receivePacket = new DatagramPacket(data, data.length);
  
        // Receives the DatagramPacket on the send and receive socket
        try {
           sendAndReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
           e.printStackTrace();
           System.exit(1);
        }
        System.out.println("Elevator: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        String received = new String(data,0,len);   
        System.out.println(received);
        System.out.println();
    }
   
    /**
    * @author Nathan MacDiarmid 101098993
    * Closes the open sockets when program ends
    */
    public void closeSocket() {
        sendAndReceiveSocket.close();
    }

    /**
     * Currently sends all requests in floor
     * Handles number of requests in queue conretely (hard coded)
     * @param args
     */
    public static void main(String args[]) {
        Elevator elevator = new Elevator(null);
        for (int i = 0; i < 3; i++) {
            elevator.sendRequest();
            elevator.receiveInstruction();
            elevator.sendSatus();
            elevator.receiveAcknowledgement();
        }
        elevator.closeSocket();
    }
}
