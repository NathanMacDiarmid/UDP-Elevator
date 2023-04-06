package src;

public class InputData implements Comparable<InputData> {
    // represents the time the request was sent
    private long timeOfRequest;
    // the floor at which the request was sent
    private int floor;
    // the direction the elevator will go once arrived at the floor (up, down,
    // up/down)
    private boolean isDirectionUp;
    // the floor the elevator will go to once it arrives at the 'floor'
    private int carRequest;

    private boolean doorNotOpenError;
    private boolean doorNotCloseError;
    private boolean elevatorStuckError;

    /**
     * Constructor for the InputData class, takes in all the values per line in the
     * txt file.
     * @param timeOfRequest - time of the requst coming in
     * @param floor - floor that the request is coming from
     * @param isDirectionUp - direction that the request is going to
     * @param carRequest - destination floor
     * @param doorNotOpenError - specifies whether this request will trigger a open door stuck fault
     * @param doorNotCloseError - specifies whether this request will trigger a closed door stuck fault
     * @param elevatorStuckError - specifies whether this request will trigger a elevator stuck fault
     */
    public InputData(long timeOfRequest, int floor, boolean isDirectionUp, int carRequest, boolean doorNotOpenError,
            boolean doorNotCloseError, boolean elevatorStuckError) {
        this.timeOfRequest = timeOfRequest;
        this.floor = floor;
        this.isDirectionUp = isDirectionUp;
        this.carRequest = carRequest;
        this.doorNotOpenError = doorNotOpenError;
        this.doorNotCloseError = doorNotCloseError;
        this.elevatorStuckError = elevatorStuckError;
    }

    /**
     * gets whether the direction of the request up
     * @return true if directin is up, false is the direction is down
     */
    public Boolean getIsDirectionUp() {
        return isDirectionUp;
    }

    /**
     * Gets the time of request
     * 
     * @return long - time at which the request was executed
     */
    public long getTimeOfRequest() {
        return timeOfRequest;
    }

    /**
     * Gets initial floor of request
     * 
     * @return int
     */
    public int getFloor() {
        return floor;
    }

    /**
     * Gets direction of request.
     * @return true if the elevator is going up
     */
    public boolean isDirectionUp() {
        return isDirectionUp;
    }

    /**
     * Gets the destination floor of request
     * @return floor of destination 
     */
    public int getCarRequest() {
        return this.carRequest;
    }

    /**
     * Determine whether this request will generate a door open fault
     * @return true if yes, false if no
     */
    public boolean getDoorNotOpenError() {
        return doorNotOpenError;
    }

    /**
     * Determine whether this request will generate a closed open fault
     * @return true if yes, false if no
     */
    public boolean getDoorNotCloseError() {
        return doorNotCloseError;
    }

    /**
     * Determine whether this request will generate an elevator stuck fault
     * @return true if yes, false if no
     */
    public boolean getElevatorStuckError() {
        return elevatorStuckError;
    }

    /**
     * Sets whether this request will generate a door open fault
     * @return true if yes, false if no
     */
    public void setElevatorStuckError(boolean setting) {
        this.elevatorStuckError = setting;
    }

    @Override
    /**
     * Allows the represented data in InputData.java to be printed.
     * Time format is converted from an int to a more human-readable format.
     * 
     * @return String Representation of 1 line of data that has been parsed
     */
    public String toString() {
        // Division to convert int to a readable format
        long millis = timeOfRequest % 1000;
        long second = (timeOfRequest / 1000) % 60;
        long minute = (timeOfRequest / (1000 * 60)) % 60;
        long hour = (timeOfRequest / (1000 * 60 * 60)) % 24;
        // adding String formatting
        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        return "InputData [currentTime=" + time + ", floor=" + floor + ", isDirectionUp="
                + isDirectionUp + ", car button=" + carRequest + ", doorNotOpenError=" + doorNotOpenError
                + ", doorNotCloseError="
                + doorNotCloseError + ", elevatorStuckError=" + elevatorStuckError + "]";
    }

    @Override
    /**
     * Allows the InputData.java classes to be compared to each other via the time
     * the request was sent.
     * For now, the method will sort only by current time and in ascending -
     * possibility for expansion later.
     * 
     * @return int Representation of 1 line of data that has been parsed
     */
    public int compareTo(InputData o) {
        int compareTime = (int) (((InputData) o).getTimeOfRequest());
        return (int) this.timeOfRequest - compareTime;
    }
    
}
