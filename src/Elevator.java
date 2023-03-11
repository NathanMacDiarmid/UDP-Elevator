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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Iterator;
/**
 * This class represents the Elevator sub-system
 * It moves between floors based on instructions passed from data.txt
 */
public class Elevator {
    private int currentFloor = 0;
    private int elevatorNum = 0;
    private int numOfPeople = 0;
    private String direction = null;
    private int numOfPeopleServiced = 0;
    private Boolean motorMoving;
    private Boolean doorOpen;
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendAndReceiveSocket;
    private boolean firstRequest = true;
    private byte[] data = new byte[250];

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

    /* elevatorQueue is the queue of requests that are currently in this elevator */
    private ArrayList<InputData> elevatorQueue; 

    
    /**
     * Default constructor for Elevator
     * 
     * @param scheduler the Scheduler instance that needs to be passed (Box class)
     */
    public Elevator(int elevatorNum, int startFloor, String direction) { 
        this.elevatorNum = elevatorNum;
        this.currentFloor = startFloor;
        this.direction = direction;
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
     * Moves the elevator from one floor to another while handling multiple requests
     * @param queue the queue of people to get off the elevator passed from elevator
     * @param currentFloor the current floor passed from the elevator
     * @return the current floor incremented by 1 lower or higher, depending on request
     * @author Juanita Rodelo 101141857
     * @author Matthew Belanger 101144323
     * @author Amanda Piazza 101143004
     */
    public int moveElevator() {
        boolean reachedDestination = false;
        System.out.println("Elevator:  queues:" + requestQueue.toString());

        //if the floor that the elevator is currently on has passengers waiting, pick them up
        if ((currentFloor != 0) && (this.floorQueues.size() != 0)) {
            //TODO GOING IN move at wrong spot
            //TODO FIRST THING:::::: floor queue is making elevator queue null which is causing error on 142 in addAll
            System.out.println("Elevator: there are people waiting for the elevator on this floor: " + currentFloor + " -> notfiy elevator to open doors ");
            this.elevatorQueue.addAll(this.floorQueues.get(currentFloor)); //this adds all requests to current elevator
            this.floorQueues.get(currentFloor).removeAll(elevatorQueue); //this removes all floor requests from current floor because passenger(s) have entered elevator
            return currentFloor; //do not move elevator
        }
        
        // next if takes care of the situation where the elevator has not picked up ANY passenger(s)
        if (this.elevatorQueue.size() == 0){ //if the elevator has not picked anyone up, go to floor of first request
            System.out.println("Scheduler: Elevator is empty");

            if ((currentFloor < requestQueue.get(0).getFloor())) { //if elevator is below floor of first requset, move up, else move down
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
                    requestQueue.removeIf(request -> (request == currPassenger)); //remove from general main queue because passenger left
                }
            }

            if (reachedDestination) {
                return currentFloor; //do not move to signal elevator to open/close doors
            }

            if (currentFloor > elevatorQueue.get(0).getCarRequest()) { //if elevator is above floor of the the destination of the first request, move down, else move up
                System.out.println("Scheduler: elevator is above destination floor of first request in priority queue -> moving down");
                return currentFloor - 1; //move elevator down
            } else {
                System.out.println("Scheduler: elevator is below destination floor of first request in priority queue -> moving up");
                return currentFloor + 1; //move elevator up
            }

        }
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
  
  // run() commented out, it is no longer needed
    // @Override
    /**
     * The run method for the Elevator class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     * @author Nathan MacDiarmid 101098993
     * @author Juanita Rodelo 101141857
     */
    // public void run() {
    //     String currentThreadName = Thread.currentThread().getName();
    //     Boolean noMoreRequestsComing = false;
    //     int oldCurrentFloor; // this keeps track of the previous floor visited by elevator
    //     System.out.println(currentThreadName + ": Current floor is: " + this.currentFloor + "\n");

    //     while (true) {
    //         oldCurrentFloor = this.currentFloor;

    //         if (!noMoreRequestsComing) { // if there are more requests to grab

    //             Map<InputData, Boolean> request = scheduler.getFloorRequest(); // grab them
    //             for (InputData r : request.keySet()) {
    //                 requestQueue.add(r);
    //                 noMoreRequestsComing = request.get(r);
    //             }

    //         }

    //         if (requestQueue.size() > 0) { // if there are currently requests to service

    //             // ask scheduler to notify elevator when it has arrived at destination floor and/or picked someone up along the way
    //             this.currentFloor = scheduler.moveElevator(requestQueue, this.currentFloor);

    //             if (this.currentFloor == oldCurrentFloor) { // if oldCurrentFloor is equal to new current floor, elevator did not move
    //                 setMotorMoving(false);
    //                 System.out.println(currentThreadName + ": Motor stopped moving");
    //                 setDoorOpen(true);
    //                 System.out.println("Doors opening -> People are walking in/out");
    //                 this.sleep(2700); //sleep for the amount of time it takes to open the doors.
    //                 System.out.println("Doors are closing");
    //                 setDoorOpen(false);
    //             } else {
    //                 setMotorMoving(true);
    //                 System.out.println(currentThreadName + ": Motor moving");
    //             }
    
    //             this.sleep(7970); //sleep for the amount of time it takes to move between floors.
    //             System.out.println(currentThreadName + ": Current floor is now: " + this.currentFloor + "\n");
    //         }
    //     }
   // }

    public void stopElevator(){

        setMotorMoving(false);
        System.out.println("Elevator # " + elevatorNum + ": Motor stopped moving");
        setDoorOpen(true);
        System.out.println("Doors opening -> People are walking in/out");
        this.sleep(2700); //sleep for the amount of time it takes to open the doors.
        System.out.println("Doors are closing");
        setDoorOpen(false);
    }

    /**
    * @author Nathan MacDiarmid 101098993
    * @author Amanda Piazza 101143004
    * Sends the request for the data that is held by the Scheduler
    */
    public void sendRequest() {
        String message = null;
        int oldCurrentFloor = currentFloor;
        System.out.println("Elevator #" + elevatorNum + ": First request? " + firstRequest);
        message = "Elevator car #: " + this.elevatorNum 
                        + " Floor: " + this.currentFloor 
                        + " Num of people: " + this.numOfPeople 
                        + " Serviced: " + this.numOfPeopleServiced
                        + " Direction: " + this.direction;
        /* 
        if(firstRequest){
            // Prepares the message to be sent by forming a byte array
            message = "Elevator car #: " + this.elevatorNum 
                        + " Floor: " + this.currentFloor 
                        + " Num of people: " + this.numOfPeople 
                        + " Serviced: " + this.numOfPeopleServiced;
        }else{
            currentFloor = moveElevator();

            //If elevator didn't move, stop motor and open doors
            if(oldCurrentFloor == currentFloor){
                stopElevator();
            }else{
                message = "Elevator car #: " + elevatorNum 
                        + " Floor: " + currentFloor 
                        + " Num of people: " + numOfPeople 
                        + " Serviced: " + numOfPeopleServiced;
            }
        
        }
        */
        
        byte[] msg = message.getBytes();

        System.out.println("Elevator car #"+ elevatorNum + " is sending a packet containing: " + message);

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
        int len;
        // Initializes the receive DatagramPacket
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Elevator " + elevatorNum +  ": Waiting for Packet.");

        // Receives the DatagramPacket
        try {        
            sendAndReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Elevator: Packet received in elevator:" + elevatorNum);
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );
        String received = new String(data,0,len);   
        System.out.println(received);
        
        saveReceivedMessage(received);
        System.out.println("Elevator - requestQueue: " + requestQueue.toString());
        System.out.println("Elevator - floorQueues: " + requestQueue.toString() + "\n");

        
    }

    public void saveReceivedMessage(String message){
        InputData request;
        int currentTime;
        int floor;
        boolean isDirectionUp;
        int carButton;
        boolean lastRequest;
        Pattern pattern = Pattern.compile("\\[currentTime=(\\d+:\\d+:\\d+\\.\\d+), floor=(\\d+), isDirectionUp=(\\w+), car button=(\\d+)\\]: (\\w+)");
        Matcher matcher = pattern.matcher(message);
        LocalTime time;

        //If message received from scheduler is not "no current requests", then it holds a request and we must save all input data info
        if(!message.equals("No current requests")){

            firstRequest = false; //if we have received a request, firstRequest = false
            if (matcher.find()) { //TODO: add try-catch around this parsing
                time = LocalTime.parse((matcher.group(1)));
                currentTime = time.get(ChronoField.MILLI_OF_DAY);
                floor = Integer.parseInt(matcher.group(2));
                isDirectionUp = Boolean.parseBoolean(matcher.group(3));
                carButton = Integer.parseInt(matcher.group(4));
                lastRequest =  Boolean.parseBoolean(matcher.group(5));

                request = new InputData(currentTime, floor, isDirectionUp, carButton);
                
                //Add request to elevatorQueue
                this.floorQueues.get(request.getFloor()).add(request); // adds request to corresponding floor queue
                this.requestQueue.add(request); // adds request to main request queue
                this.numOfPeople ++;
            
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
        Elevator elevator1 = new Elevator(1, 2, "up");
        Elevator elevator2 = new Elevator(2, 4, "up");
        
        for (int i = 0; i < 4; i++) { //TODO: make an infinite while loop
            elevator1.sendRequest();
            elevator2.sendRequest();
            elevator1.receiveInstruction();
            elevator2.receiveInstruction();
            //elevator.sendSatus();
            //elevator.receiveAcknowledgement();
        }
        elevator1.closeSocket();
        elevator2.closeSocket();
    }
}
