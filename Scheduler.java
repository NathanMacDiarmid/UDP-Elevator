
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

public class Scheduler {

    /**
     * queueInUse represents when the elevator queue is being updated. True means it
     * is being updated and added to be the floor, false means it is not being
     * updated
     */
    private boolean queueInUse;

    /* noMoreRequests tracks when the floor subsysytem will send last request */
    private boolean noMoreRequests;

    /** currentFloor tracks where the elevator is */
    private int currentFloor;

    /* elevatorQueue is the queue of requests that are currently in the elevator */
    private ArrayList<InputData> elevatorQueue;

    /* requestQueue used as priority queue of requests */
    private ArrayList<InputData> requestQueue;

    private DatagramPacket sendPacket, receivePacket23, receivePacket69;
    private DatagramSocket sendAndReceiveSocket, receiveSocket23, receiveSocket69;
    private byte[] data = new byte[100];

    /* floorQueue is to keep track of people waiting for elevator on each floor */
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

    /**
     * Default constructor for Scheduler
     * Initializes all of the attributes
     */
    public Scheduler() {
        this.queueInUse = true;
        this.elevatorQueue = new ArrayList<InputData>();
        this.requestQueue = new ArrayList<InputData>();
        this.currentFloor = 0;
        this.noMoreRequests = false;
        try {
            this.sendAndReceiveSocket = new DatagramSocket();
            this.receiveSocket23 = new DatagramSocket(23);
            this.receiveSocket69 = new DatagramSocket(69);
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setQueueInUse(boolean queueInUse) {
        this.queueInUse = queueInUse;
    }

    public boolean isQueueInUse() {
        return queueInUse;
    }

    /**
     * The two following methods are ONLY FOR TESTING PURPOSES and
     * should not be included in commercial product.
     */
    public int getCurrentFloor() {
        return currentFloor;
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
     * @return The queue for each floor
     * @author Michael Kyrollos
     */
    public Map<Integer, ArrayList<InputData>> getFloorQueues() {
        return floorQueues;
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
     * getFloorRequest returns two values, the request sent by the floor subsystem,
     * and whether that request is the last one
     * @author Juanita Rodelo 101141857
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     */
    public synchronized Map<InputData, Boolean> getFloorRequest() {
        while (queueInUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        InputData requestToGiveElevator = this.requestQueue.get(0);
        this.requestQueue.remove(0);
        queueInUse = true;
        notifyAll();

        Map<InputData, Boolean> returnVals = new HashMap<InputData, Boolean>() {
            {
                put(requestToGiveElevator, noMoreRequests);
            }
        };
    
        return returnVals;
    }

    /**
     * Moves the elevator from one floor to another while handling multiple requests
     * @param queue the queue of people to get off the elevator passed from elevator
     * @param currentFloor the current floor passed from the elevator
     * @return the current floor incremented by 1 lower or higher, depending on request
     * @author Juanita Rodelo 101141857
     * @author Matthew Belanger 101144323
     * @author Amanda Piazza 101143004
     */
    public int moveElevator(ArrayList<InputData> queue, int currentFloor) {
        boolean reachedDestination = false;
        System.out.println("Scheduler: Floor queues:" + this.floorQueues.toString());

        //if the floor that the elevator is currently on has passengers waiting, pick them up
        if ((currentFloor != 0) && (this.floorQueues.get(currentFloor).size() != 0)) {
            
            System.out.println("Scheduler: there are people waiting for the elevator on this floor: " + currentFloor + " -> notfiy elevator to open doors ");
            this.elevatorQueue.addAll(this.floorQueues.get(currentFloor)); //this adds all requests to current elevator
            this.floorQueues.get(currentFloor).removeAll(elevatorQueue); //this removes all floor requests from current floor because passenger(s) have entered elevator
            return currentFloor; //do not move elevator
        }
        
        /* next if takes care of the situation where the elevator has not picked up ANY passenger(s) */
        if (this.elevatorQueue.size() == 0){ //if the elevator has not picked anyone up, go to floor of first request
            System.out.println("Scheduler: Elevator is empty");

            if ((currentFloor < queue.get(0).getFloor())) { //if elevator is below floor of first requset, move up, else move down
                System.out.println("Scheduler: elevator is below initial floor of first request in queue -> moving up");
                return currentFloor + 1; //move elevator up
            } else { 
                System.out.println("Scheduler: elevator is above initial floor of first request in queue -> moving down");
                return currentFloor - 1; //move elevator down
            }
        } else { //else if elevator currently has passenger(s) in it that need to reach their destination floor

            Iterator<InputData> iterator = this.elevatorQueue.iterator(); //go through the requests that are currently in the elevator and check if current floor is equal to any of the destination floors of passenger(s) in the elevator

            while (iterator.hasNext()) {  
                InputData currPassenger = iterator.next();

                if (currentFloor == currPassenger.getCarRequest()) {
                    System.out.println("Scheduler: elevator is at the destination of a passenger in the elevator -> notfiy elevator to open doors");
                    reachedDestination = true;
                    iterator.remove(); //remove from elevator queue because passenger left
                    queue.removeIf(request -> (request == currPassenger)); //remove from general main queue because passenger left
                }
            }

            if (reachedDestination) {
                return currentFloor; //do not move to signal elevator to open/close doors
            }

            if (currentFloor > queue.get(0).getCarRequest()) { //if elevator is above floor of the the destination of the first request, move down, else move up
                System.out.println("Scheduler: elevator is above destination floor of first request in priority queue -> moving down");
                return currentFloor - 1; //move elevator down
            } else {
                System.out.println("Scheduler: elevator is below destination floor of first request in priority queue -> moving up");
                return currentFloor + 1; //move elevator up
            }

        }
    }
 
    /**
     * The putter method for the Scheduler class puts the floor reqeuests that were
     * passed from Floor into {@link Scheduler#currentFloor} and
     * {@link Scheduler#nextFloor} respectfully
     * @param elevatorQueue the first InputData instance in the list of
     * commands that were passed from Floor that hold the
     * current time, floor the elevator was requested on, floor 
     * the elevator goes to, and whether the elevator is going up or down.
     * @author Juanita Rodelo 101141857
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     */
    public synchronized void putFloorRequest(InputData elevatorIntstruction, boolean lastRequest) {
        while (!queueInUse) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.floorQueues.get(elevatorIntstruction.getFloor()).add(elevatorIntstruction); // adds request to corresponding floor queue
        this.requestQueue.add(elevatorIntstruction); // adds request to main request queue
        this.noMoreRequests = lastRequest;
        queueInUse = false;
        notifyAll();
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Send method that sends the instruction to the Elevator to the specified port
    * In this case, the specified port is port 69
    */
    public void sendToElevator() {
        // Initializes the DatagramPacket to be sent to the server
        sendPacket = new DatagramPacket(data, receivePacket23.getLength(),
        receivePacket23.getAddress(), receivePacket69.getPort());

        System.out.println( "Scheduler: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len));
        System.out.println();

        // Sends the DatagramPacket to the Server
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scheduler: packet sent to Elevator");

        // Clears the request pipeline
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Sends to acknowledgement to the Elevator that the data has been
    * received and accepted by the Scheduler.
    */
    public void sendElevatorAcknowledgement() {
        String message = "The Scheduler is sending deats shortly.";
        byte[] msg = message.getBytes(); 

        // Initializes the DatagramPacket to send to the Server
        sendPacket = new DatagramPacket(msg, msg.length,
        receivePacket69.getAddress(), receivePacket69.getPort());

        System.out.println( "Scheduler: Sending packet acknowledgment to Elevator:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len));
        System.out.println();

        // Sends the DatagramPacket to the Client
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
    * Receive method for Scheduler that receives from the Elevator
    */
    public void receiveFromElevator() {
        // Initializes the receive DatagramPacket to be able to receive the message
        receivePacket69 = new DatagramPacket(data, data.length);
        System.out.println("Scheduler: Waiting for Packet.\n");

        // Receives the DatagramPacket
        try {        
            System.out.println("Waiting...");
            sendAndReceiveSocket.receive(receivePacket69);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scheduler: Packet received from Elevator:");
        System.out.println("From host: " + receivePacket69.getAddress());
        System.out.println("Host port: " + receivePacket69.getPort());
        int len = receivePacket69.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );

        String received = new String(data,0,len);   
        System.out.println(received);
        System.out.println();
    }

    /**
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     * Receives the Elevators request for information before
     * being sent to the Elevator
     */
    public void receiveElevatorRequest() {
        // Initializes the DatagramPacket to be received from the Server
        byte[] request = new byte[20];
        receivePacket69 = new DatagramPacket(request, request.length);
        System.out.println("Scheduler: Waiting for Packet.\n");

        // Receives the DatagramPacket from the Server
        try {        
            System.out.println("Waiting...");
            receiveSocket69.receive(receivePacket69);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scheduler: Packet received from Elevator:");
        System.out.println("From host: " + receivePacket69.getAddress());
        System.out.println("Host port: " + receivePacket69.getPort());
        int len = receivePacket69.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );

        String received = new String(request);   
        System.out.println(received + "\n");
    }

    /**
     * @author Nathan MacDiarmid 101098993
     * @author Amanda Piazza 101143004
     * Send method for Shceduler that sends the message to the Floor
     */
    public void sendToFloor() {
        // Initializes the DatagramPacket to send to the Floor
        sendPacket = new DatagramPacket(data, receivePacket69.getLength(),
        receivePacket23.getAddress(), receivePacket23.getPort());

        System.out.println( "Scheduler: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len));
        System.out.println();

        // Sends the DatagramPacket to the Floor
        try {
            sendAndReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Scheduler: packet sent to Floor");
        for (int i = 0; i < data.length; i++) {
            data[i] = 0;
        }
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

        System.out.println( "Scheduler: Sending packet acknowledgment to Floor:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        int len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len));
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
     * Receive method for Host that receives the message from the floor
     */
    public void receiveFromFloor() {
        // Initializes the DatagramPacket to be received from the floor
        receivePacket23 = new DatagramPacket(data, data.length);
        System.out.println("Scheduler: Waiting for Packet.\n");

        // Receives the DatagramPacket from the floor
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
        System.out.println("Host port: " + receivePacket23.getPort());
        int len = receivePacket23.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );

        String received = new String(data,0,len);   
        System.out.println(received);
        System.out.println();
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
        System.out.println("Host port: " + receivePacket23.getPort());
        int len = receivePacket23.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );

        String received = new String(request);   
        System.out.println(received + "\n");
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
     * Currently sends all requests in floor
     * Handles number of requests in queue conretely (hard coded)
     * @param args
     */
    public static void main(String args[]) {
        Scheduler scheduler = new Scheduler();
        for (int i = 0; i < 3; i++) {
            scheduler.receiveFromFloor();
            scheduler.sendFloorAcknowledgement();
            scheduler.receiveElevatorRequest();
            scheduler.sendToElevator();
            scheduler.receiveFromElevator();
            scheduler.sendElevatorAcknowledgement();
            scheduler.receiveFloorRequest();
            scheduler.sendToFloor();
        }
        scheduler.closeSockets();
    }
}
