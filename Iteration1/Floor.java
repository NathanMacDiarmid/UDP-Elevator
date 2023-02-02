package Iteration1;

import java.io.FileNotFoundException;
import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class Floor implements Runnable {
    private Scheduler scheduler;
    // All requests will be stored in this sorted ArrayList
    private ArrayList<InputData> elevatorQueue;

    /**
     * Default constructor for Floor class
     * @param scheduler the Scheduler that is used as the middle man (Box class)
     * Also initializes {@link #elevatorQueue} ArrayList
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
        elevatorQueue = new ArrayList<>();
    }

    /**
     * Reads a file named data.txt that is in the same directory and parses through elevator data. 
     * Creates a usable format for the rest scheduler. 
     */
    public void readData() {
        try (Scanner input = new Scanner(new File(new File("").getAbsolutePath() + "\\src\\Iteration1\\data.txt"))) {
            while (input.hasNextLine()) {
                // Values are space-separated 
                String[] data = input.nextLine().split(" ");
                // Using the LocalTime class to parse through a standard time format of 'HH:MM:SS:XM'
                LocalTime time = LocalTime.parse((data[0]));
                // converting the LocalTime to an integer, will stored as an int 
                int l = time.get(ChronoField.MILLI_OF_DAY);
                //Creating new InputData class for every line in the txt, storing it in the elevator ArrayList
                elevatorQueue.add(new InputData(l, Integer.parseInt(data[1]), isGoingUp(data[2]),
                        Integer.parseInt(data[3])));
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            e.printStackTrace();
        }
        // InputData.java implements the Comparable class, the 'sort' will be calling the compareTo()
        // It is sorted in ascending order based on the 'currentTime' of the request. 
        Collections.sort(elevatorQueue);
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
    }

    @Override
    /**
     * The run method for the Floor class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        this.readData();
        printInputData(elevatorQueue);

       while(elevatorQueue.size() != 0) {
            scheduler.put(elevatorQueue.get(0));
            elevatorQueue.remove(0);
       }
       System.exit(1);
    }
}
