package ca.mcgill.ecse211.strategies;

/**
 * Provides a means to track how many seconds have elapsed since the robot
 * started the competition run.
 * 
 * @author Julian Armour
 * @version 1
 * @since March 16, 2019
 */
public class TimeTracker {
    private long startTime;

    /**
     * Starts the clock for tracking time.
     * 
     * @author Julian Armour
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * calculates how many seconds have elapsed since start() was called.
     * 
     * @return how many seconds have elapsed since start() was called.
     * 
     * @author Julian Armour
     */
    public int elapsedSeconds() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }
}
