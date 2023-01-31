package Iteration1;

import java.util.PriorityQueue;

public class InputData {
    private long currentTime;
    private int floor;

    private boolean isDirectionUp;

    public long getCurrentTime() {
        return currentTime;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isDirectionUp() {
        return isDirectionUp;
    }

    public InputData(long currentTime, int floor, boolean isDirectionUp) {
        this.currentTime = currentTime;
        this.floor = floor;
        this.isDirectionUp = isDirectionUp;
    }

    @Override
    public String toString() {
        return "InputData [currentTime=" + currentTime + ", floor=" + floor + ", isDirectionUp=" + isDirectionUp + "]";
    }
}