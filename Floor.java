import java.io.FileNotFoundException;
import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

/**
Floor.java accepts events from InputData.java. Each event consists of the current time, the floor request, 
   the direction of travel and the car button pressed. These events are sent to Scheduler.java
   */ 
public class Floor implements Runnable {

    private Scheduler scheduler;
    // All requests will be stored in this sorted ArrayList
    private ArrayList<InputData> elevatorQueue;
    private boolean requestUpButton;
    private boolean requestDownButton;
    private String directionLamp;
    private boolean requestUpButtonLamp;
    private boolean requestDownButtonLamp;

    /**
     * Default constructor for Floor class
     * @param scheduler the Scheduler that is used as the middle man (Box class)
     * Also initializes {@link #elevatorQueue} ArrayList
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.elevatorQueue = new ArrayList<>();
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
     * Reads a file named data.txt that is in the same directory and parses through elevator data. 
     * Creates a usable format for the rest scheduler.
     * @author Michael Kyrollos 101183521
     * @author Nathan MacDiarmid 101098993
     */
    public void readData(String filename) {
        String path = new File("").getAbsolutePath() + "/" + filename;

        try (Scanner input = new Scanner(new File(path))) {
            while (input.hasNextLine()) { //TODO: check each value to verify if they are valid before adding them to elevatorQueue

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
                    timeOfRequest = time.get(ChronoField.MILLI_OF_DAY); //TODO: should we declare these variables before we initialize them?

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
     * Determines the direction button pressed based on String representation
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

    @Override
    /**
     * The run method for the Floor class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     * @author Nathan MacDiarmid 101098993
     * @author Michael Kyrollos 101183521
     * @author Juanita Rodelo 101141857
     * @author Matthew Belanger 101144323
     */
    public void run() {
        this.readData("data.txt");
        initiateFloor();
    }

    /**
     * The functionality behind the {@link #run()} method
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
                        + getDirectionLamp() + " button...The " + getDirectionLamp() + " lamp is now on");
                scheduler.putFloorRequest(elevatorQueue.get(0), lastRequest);
                elevatorQueue.remove(0);
            }
        }
    }
}
