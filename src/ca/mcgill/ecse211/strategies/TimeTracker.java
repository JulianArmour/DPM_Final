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
    private int  outOfTimeThreshold;
    private int  timeLimit;

    /**
     * 
     * @param outOfTimeThreshold
     *            The time threshold (in seconds) for when to go to the final
     *            destination (e.g. starting square) at the end of the competition
     *            run.
     *            <p>
     *            For example, outOfTimeThreshold = 45 means the robot should go to
     *            the final destination when there are less than 45 seconds
     *            remaining.
     * @param timeLimit
     *            how long the competition run lasts (in sec).
     */
    public TimeTracker(int outOfTimeThreshold, int timeLimit) {
        this.outOfTimeThreshold = outOfTimeThreshold;
        this.timeLimit = timeLimit;
    }

    /**
     * Starts the clock for tracking time.
     * 
     * @author Julian Armour
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * 
     * @return how many seconds have elapsed since start() was called.
     * 
     * @author Julian Armour
     */
    public int elapsedSeconds() {
        return (int) ((System.currentTimeMillis() - startTime) / 1000);
    }

    public boolean outOfTime() {
        if (timeLimit - elapsedSeconds() < outOfTimeThreshold) {
            return true;
        } else {
            return false;
        }
    }
}
