package Iteration1;

public class InputData implements Comparable<InputData> {
    // represents the time the request was sent 
    private long currentTime;
    // the floor at which the request was sent
    private int floor;
    // the direction the elevator will go once arrived at the floor(true if going up)
    private boolean isDirectionUp;
    // the floor the elevator will go to once it arrives at the 'floor'
    private int carRequest;

    /** 
     * Constructor for the InputData class, takes in all the values per line in the txt file. 
     */
    public InputData(long currentTime, int floor, boolean isDirectionUp, int carRequest) {
        this.currentTime = currentTime;
        this.floor = floor;
        this.isDirectionUp = isDirectionUp;
        this.carRequest = carRequest;
    }

    /**
     * Getter for the currentTime field. 
     * @return long The time at which the request was sent  
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Getter for floor field. 
     * @return int The floor at which the request was sent
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Getter for isDirectionUp field. 
     * @return true if the elevator is going up 
     */
    public boolean isDirectionUp() {
        return isDirectionUp;
    }

    /**
     * Getter for the carRequest field. 
     * @return int The floor the elevator will go to once it arrives at the 'floor'
     */
    public int getCarRequest() {
        return this.carRequest;
    }

    @Override
    /** 
     * Allows the represented data in InputData.java to be printed. 
     * Time format is converted from an int to a more human-readable format. 
     * @return String Representation of 1 line of data that has been parsed
    */
    public String toString() {
        // Division to convert int to a readable format 
        long millis = currentTime % 1000;
        long second = (currentTime / 1000) % 60;
        long minute = (currentTime / (1000 * 60)) % 60;
        long hour = (currentTime / (1000 * 60 * 60)) % 24;
        // adding String formatting 
        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        return "InputData [currentTime=" + time + ", floor=" + floor + ", isDirectionUp="
                + isDirectionUp + ", car button=" + carRequest + "]";
    }

    @Override
     /** 
     * Allows the InputData.java classes to be compared to each other via the time the request was sent. 
     * For now, the method will sort only by current time and in ascending - possibility for expansion later. 
     * @return int Representation of 1 line of data that has been parsed
    */
    public int compareTo(InputData o) {
        int compareTime = (int) (((InputData) o).getCurrentTime());
        return (int) this.currentTime - compareTime;
    }
}
