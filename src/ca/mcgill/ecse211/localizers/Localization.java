package ca.mcgill.ecse211.localizers;

import ca.mcgill.ecse.sensors.MedianDistanceSensor;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;

/**
 * Provides the methods for the initial localization and localizing on the fly.
 * 
 * @author Julian Armour
 * @since March 4, 2019
 * @version 2
 */
public class Localization {

    private static final long    US_POLL_PERIOD = 300;
    private static final int     MAX_DIST       = 255;
    private static final double  EDGE_THRESHOLD = 55.0;

    private MovementController   movementController;
    private Odometer             odometer;
    private MedianDistanceSensor med;

    public Localization(MovementController movementController, Odometer odometer,
            MedianDistanceSensor medianDistanceSensor) {

        this.movementController = movementController;
        this.odometer = odometer;
        this.med = medianDistanceSensor;
    }

    /**
     * The subroutine for correcting the odometer's angle. If the robot is placed at
     * a corner tile with walls on each side of the corner, then the robot will
     * "scan" while rotating. It is looking for a large drop in distance measured by
     * the {@link DifferentialDistancePoller}. It will record at what angles these
     * large differences in distance occured at and use them to calculate the
     * {@link Odometer}'s angle error.
     */
    public void usLocalize() {
        double fallingEdge = 404; // 404: the angle has not been found
        double risingEdge = 404;

        // get new data in median buffer
        med.flush();

        // make the robot rotate counter-clockwise until it sees a large change in
        // distance
        movementController.rotateAngle(720, false, true);
        // move out of valley
        while (Math.min(med.getFilteredDistance(), MAX_DIST) < 55) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // then start moving clockwise
        movementController.rotateAngle(720, true, true);

        while (Math.min(med.getFilteredDistance(), MAX_DIST) > EDGE_THRESHOLD) {
            // let some time pass between each sample
            try {
                Thread.sleep(US_POLL_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fallingEdge = odometer.getXYT()[2];

        while (Math.min(med.getFilteredDistance(), MAX_DIST) < EDGE_THRESHOLD) {
            // let some time pass between each sample
            try {
                Thread.sleep(US_POLL_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        risingEdge = odometer.getXYT()[2];

        double alpha = fallingEdge;
        double beta = risingEdge;

        // the odometer's error, to be added to the current odometer's angle later
        double dTheta;

        if (alpha <= beta) {
            dTheta = 355 - (alpha + beta) / 2;
            System.out.println("alpha <= beta");
        } else {
            dTheta = 165 - (alpha + beta) / 2; // increase in ccw direction
            System.out.println("alpha > beta");
        }

        /*
         * correct the odometer. Note: the motor's didn't need to be stopped to perform
         * the update since the odometer is thread-safe.
         */
        odometer.update(0, 0, dTheta);
        // face "North"
        movementController.turnTo(0.0);
    }
}
