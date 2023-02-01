package Iteration1;

import java.sql.Time;

public class InputData implements Comparable<InputData> {
    private long currentTime;
    private int floor;
    private boolean isDirectionUp;
    private int carRequest;

    public InputData(long currentTime, int floor, boolean isDirectionUp, int carRequest) {
        this.currentTime = currentTime;
        this.floor = floor;
        this.isDirectionUp = isDirectionUp;
        this.carRequest = carRequest;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public int getFloor() {
        return floor;
    }

    public boolean isDirectionUp() {
        return isDirectionUp;
    }

    public int getCarRequest() {
        return this.carRequest;
    }

    public String getTimeConversion(long time) {
        Time t = new Time(time);
        return t.toString();
    }

    @Override
    public String toString() {
        long millis = currentTime % 1000;
        long second = (currentTime / 1000) % 60;
        long minute = (currentTime / (1000 * 60)) % 60;
        long hour = (currentTime / (1000 * 60 * 60)) % 24;
        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        return "InputData [currentTime=" + time + ", floor=" + floor + ", isDirectionUp="
                + isDirectionUp + ", car button=" + carRequest + "]";
    }

    @Override
    public int compareTo(InputData o) {
        // TODO Auto-generated method stub
        int compareTime = (int) (((InputData) o).getCurrentTime());
        return (int) this.currentTime - compareTime;
    }
}