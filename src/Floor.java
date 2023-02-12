package src;

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

    public Boolean setRequestUpButtonLamp(){
        return requestUpButtonLamp;
    }

    public Boolean getRequestDownButtonLamp(){
        return requestDownButtonLamp;
    }

    public Boolean setRequestDownButtonLamp(){
        return requestDownButtonLamp;
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
        String path = new File("").getAbsolutePath() + "/src/" + "data.txt";

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
                //save requested floor direction
                String directionRequest = data[2];
                //save floor request
                int floorRequest = Integer.parseInt(data[3]);
                //Creating new InputData class for every line in the txt, storing it in the elevator ArrayList
                elevatorQueue.add(new InputData(timeOfRequest, currentFloor, directionRequest, floorRequest));
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("before sort:");
        printInputData(elevatorQueue);
        // InputData.java implements the Comparable class, the 'sort' will be calling the compareTo()
        // It is sorted in ascending order based on the 'timeOfRequest' of the request. 
        Collections.sort(elevatorQueue);
        System.out.println("after sort:");
        printInputData(elevatorQueue);
    }

    /** 
     * Prints out the data that has been parsed from the "data.txt" file. 
     */
    public void printInputData(ArrayList<InputData> queueToPrint) {
        for (InputData q : queueToPrint) {
            System.out.println(q);
        }
    }

    @Override
    /**
     * The run method for the Floor class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
         
        this.readData();
        //get current time
        //when current time = time from the input data
        //turn on sensors
        System.out.println("timeOfRequest = " + elevatorQueue.get(0).getTimeOfRequest());
        System.out.println("fake current time: " + LocalDateTime.now().get(ChronoField.MILLI_OF_DAY));
        while(elevatorQueue.size() != 0) {
            //long currentTimeInMillliSec = LocalDateTime.now().get(ChronoField.MILLI_OF_DAY); //uncomment at the end
            long currentTimeInMillliSec = elevatorQueue.get(0).getTimeOfRequest(); //for testing purposes only
            long timeOfR = elevatorQueue.get(0).getTimeOfRequest();

            if(timeOfR == currentTimeInMillliSec){ //if request is ready to be scheduled
                scheduler.putFloorRequest(elevatorQueue.get(0)); //TODO: the way we send the data needs to change
                elevatorQueue.remove(0);
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
