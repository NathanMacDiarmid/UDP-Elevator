

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class Floor implements Runnable {
    private Scheduler scheduler;
    // All requests will be stored in this sorted ArrayList
    private ArrayList<InputData> elevatorQueue;
    private Boolean requestUpButton;
    private Boolean requestDownButton;
    private String directionLamp;
    private Boolean requestUpButtonLamp;
    private Boolean requestDownButtonLamp;
    private Boolean arrivingSensor;
   
    


    /**
     * Default constructor for Floor class
     * @param scheduler the Scheduler that is used as the middle man (Box class)
     * Also initializes {@link #elevatorQueue} ArrayList
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
        elevatorQueue = new ArrayList<>();
    }

    public Boolean getRequestUpButton(){
        return requestUpButton;
    }

    public void setRequestUpButton(Boolean requestUpButton){
        this.requestUpButton = requestUpButton;
    }

    public Boolean getRequestDownButton(){
        return requestDownButton;
    }

    public void setRequestDownButton(Boolean requestDownButton){
        this.requestDownButton = requestDownButton;
    }

    public String getDirectionLamp(){
        return directionLamp;
    }

    public void setDirectionLamp(String directionLamp){ 
        this.directionLamp = directionLamp; 
    }

    public Boolean getRequestUpButtonLamp(){
        return requestUpButtonLamp;
    }

    public void setRequestUpButtonLamp(Boolean isOn){
        requestUpButtonLamp = isOn;
    }

    public Boolean getRequestDownButtonLamp(){
        return requestDownButtonLamp;
    }

    public void setRequestDownButtonLamp(Boolean isOn){
        this.requestDownButtonLamp = isOn;
    }
    
    public Boolean getArrivingSensor(){
        return arrivingSensor;
    }

    public void setArrivingSensor(Boolean arrivingSensor){
        this.arrivingSensor = arrivingSensor;
    }

    /**
     * Reads a file named data.txt that is in the same directory and parses through elevator data. 
     * Creates a usable format for the rest scheduler. 
     */
    public void readData() {
        String path = new File("").getAbsolutePath() + "/" + "data.txt";

        try (Scanner input = new Scanner(new File(path))) {
            while (input.hasNextLine()) { //TODO: check each value to verify if they are valid before adding them to elevatorQueue
                // Values are space-separated 
                String[] data = input.nextLine().split(" ");
                // Using the LocalTime class to parse through a standard time format of 'HH:MM:SS:XM'
                LocalTime time = LocalTime.parse((data[0]));
                // converting the LocalTime to an integer, will stored as an int that represents the millisecond of the day
                int timeOfRequest = time.get(ChronoField.MILLI_OF_DAY); //TODO: should we declare these variables before we initialize them?
                //save current floor
                int currentFloor = Integer.parseInt(data[1]);
                //save floor request
                int floorRequest = Integer.parseInt(data[3]);
                //Creating new InputData class for every line in the txt, storing it in the elevator ArrayList
                elevatorQueue.add(new InputData(timeOfRequest, currentFloor, isGoingUp(data[2]), floorRequest));
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            e.printStackTrace();
        }
        // InputData.java implements the Comparable class, the 'sort' will be calling the compareTo()
        // It is sorted in ascending order based on the 'timeOfRequest' of the request. 
        Collections.sort(elevatorQueue);
        printInputData(elevatorQueue);
    }

    /**
     * Determines the direction button pressed based on String representation 
     * Assumes that the data in the txt is in valid format and following our standard 
     * @param direction The Direction that has been parsed ("up" or "down")
     * @return true if "up" is the direction, false otherwise. 
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
     */
    public void printInputData(ArrayList<InputData> queueToPrint) {
        for (InputData q : queueToPrint) {
            System.out.println(q);
        }
        System.out.println();
    }

    @Override
    /**
     * The run method for the Floor class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
         
        this.readData();
        long startTime = System.currentTimeMillis();
        long firstRequestTime = elevatorQueue.get(0).getTimeOfRequest();
        
        while(elevatorQueue.size() != 0) {
            
            long timeOfR = elevatorQueue.get(0).getTimeOfRequest();
            long elapsedTime = System.currentTimeMillis() - startTime;
            //System.out.println("Elapsed Time: " + elapsedTime);

            if((timeOfR - firstRequestTime) <= elapsedTime){
                if(elevatorQueue.get(0).isDirectionUp()){ 
                        setDirectionLamp("up"); //TODO: make null if no movemement 
                        setRequestUpButtonLamp(true); //TODO: turn these off when request has been fulfilled
                        setRequestUpButton(true);
                }else{
                        setDirectionLamp("down");
                        setRequestDownButtonLamp(true);
                        setRequestDownButton(true);
                }
                System.out.println("Floor: Someone on floor " + elevatorQueue.get(0).getFloor() + " has pressed the " + getDirectionLamp() + " button...The " + getDirectionLamp() + " lamp is now on");

                scheduler.putFloorRequest(elevatorQueue.get(0));
                elevatorQueue.remove(0);
            }

            try {
                Thread.sleep(1000); //sleep for the amount of time it takes to move from floor to floor
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
       
       System.exit(1);
    }

    /**
     * The following method is ONLY FOR TESTING PURPOSES and
     * should not be included in commercial product.
     */
    public ArrayList<InputData> getElevatorQueue(){
        return elevatorQueue;
    }
}
