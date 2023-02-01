package Iteration1;

import java.io.FileNotFoundException;
import java.io.File;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.*;

public class Floor implements Runnable {
    private Scheduler scheduler;
    private ArrayList<InputData> elevatorQueue;

    /**
     * Default constructor for Agent class
     * 
     * @param table the Table that is used as the middle man (Box class)
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
        elevatorQueue = new ArrayList<>();
    }

    public void readData() {
        try (Scanner input = new Scanner(new File("Iteration1/data.txt"))) {
            while (input.hasNextLine()) {
                String[] data = input.nextLine().split(" ");
                LocalTime time = LocalTime.parse((data[0]));
                int l = time.get(ChronoField.MILLI_OF_DAY);
                elevatorQueue.add(new InputData(l, Integer.parseInt(data[1]), isGoingUp(data[2]),
                        Integer.parseInt(data[3])));
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Collections.sort(elevatorQueue);
    }

    public boolean isGoingUp(String direction) {
        if (direction.equals("up")) {
            return true;
        } else {
            return false;
        }
    }

    public void sortList(ArrayList<InputData> queueToPrint) {

    }

    @Override
    /**
     * The run method for the Agent class is inherited from the
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

    public void printInputData(ArrayList<InputData> queueToPrint) {
        for (InputData q : queueToPrint) {
            System.out.println(q);
        }
    }
}
