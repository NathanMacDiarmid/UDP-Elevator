package Iteration1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.File;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Floor implements Runnable {
    private Scheduler scheduler;
    private BufferedReader input;
    private ArrayList<InputData> elevatorQueue;
    private String instructions;

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
                String[] timeTemp = data[0].split("[:\\.]");
                long tempTime = Integer.parseInt(timeTemp[0]) * 3600000 + Integer.parseInt(timeTemp[1]) * '\uea60'
                        + Integer.parseInt(timeTemp[2]) * 1000 + Integer.parseInt(timeTemp[3]);
                elevatorQueue.add(new InputData(tempTime, Integer.parseInt(data[1]),
                        true));
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public boolean isGoingUp(String direction) {
        if (direction.equals("up")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    /**
     * The run method for the Agent class is inherited from the
     * Runnable interface. It runs the Thread when .start() is used
     */
    public void run() {
        this.readData();
        System.out.println(elevatorQueue);
        while (true) {
            // Change this to floor input from person
            Random rand = new Random();
            int firstIngredient = rand.nextInt((3 - 1) + 1) + 1;
            int secondIngredient = rand.nextInt((3 - 1) + 1) + 1;
            while (firstIngredient == secondIngredient) {
                secondIngredient = rand.nextInt((3 - 1) + 1) + 1;
            }

            scheduler.put(firstIngredient, secondIngredient);
            System.out.println("Agent just put the ingredient " + firstIngredient + " and second ingredient "
                    + secondIngredient + " on the table");

        }

    }

}
