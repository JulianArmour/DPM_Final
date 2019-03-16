package ca.mcgill.ecse211.localizers;

import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.sensors.LightDifferentialFilter;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;

/**
 * Provides the methods for the initial localization and localizing on the fly.
 * 
 * @author Julian Armour
 * @since March 4, 2019
 * @version 2
 */
public class Localization {

    private static final long       US_POLL_PERIOD              = 100;
    private static final int        MAX_DIST                    = 255;
    private static final double     EDGE_THRESHOLD              = 55.0;
    private static int              LIGHT_POLLING_PERIOD        = 20;
    private static float            FIRST_DIFFERENCE_THRESHOLD  = 4.0f;
    private static float            SECOND_DIFFERENCE_THRESHOLD = 1.5f;
    private static final int 		SLOW_SPEED					= 45;
    private static final int 		FAST_SPEED					= 140;

    private MovementController      movCon;
    private Odometer                odo;
    private MedianDistanceSensor    med;
    private LightDifferentialFilter dLTleft;
    private LightDifferentialFilter dLTright;
    private int                     startingCorner;

    public Localization(MovementController movementController, Odometer odometer,
            MedianDistanceSensor medianDistanceSensor, LightDifferentialFilter leftLightDiff,
            LightDifferentialFilter rightLightDiff, int startingCorner) {

        this.movCon = movementController;
        this.odo = odometer;
        this.med = medianDistanceSensor;
        this.dLTleft = leftLightDiff;
        this.dLTright = rightLightDiff;
        this.startingCorner = startingCorner;
    }

    /**
     * Will make the robot perform a quick subroutine to correct the robot's
     * heading. The robot will move forward until the black lines are detected. It
     * performs two passes, the first one is fast to get a decent but imperfect
     * correction. The second is much slower and much more accurate.
     */
    public void quickThetaCorrection() {
    	
        for (int i = 0; i < 2; i++) {
            boolean RLineDetected = false;
            boolean LLineDetected = false;

            float threshold;
            if (i == 0) {
                threshold = FIRST_DIFFERENCE_THRESHOLD;
            } else {
                threshold = SECOND_DIFFERENCE_THRESHOLD;
            }

            // get rid of old light sensor data
            dLTright.flush();
            dLTleft.flush();
            if (i == 0) {
                // first pass: move fast
                movCon.driveForward(FAST_SPEED);
            } else {
                // second pass: move much slower
                movCon.driveForward(SLOW_SPEED);
            }

            float deltaR = 0f;
            float deltaL = 0f;
            while (!RLineDetected || !LLineDetected) {
                // poll right sensor
                if (!RLineDetected) {
                    deltaR = (dLTright.getDeltaL());
                }
                // poll left sensor
                if (!LLineDetected) {
                    deltaL = (dLTleft.getDeltaL());
                }

                if (Math.abs(deltaR) > threshold) {
                    RLineDetected = true;
                    // System.out.println("right sensor detected line");
                    // System.out.println(RLineDetected);
                    // System.out.println(LLineDetected);
                    movCon.stopMotor(true, true);
                    // System.out.println(deltaR);
                }

                if (Math.abs(deltaL) > threshold) {
                    LLineDetected = true;
                    // System.out.println("left sensor detected line");
                    // System.out.println(RLineDetected);
                    // System.out.println(LLineDetected);
                    movCon.stopMotor(false, true);
                    // System.out.println(deltaL);
                }

                try {
                    Thread.sleep(LIGHT_POLLING_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            movCon.stopMotors();
            // System.out.println("left: "+deltaL+" right: "+deltaR);
            if (i < 1) {
                movCon.driveDistance(-2.5);
            }
        }

        odo.setTheta(movCon.roundAngle());
    }

    /**
     * performs a {@link #quickThetaCorrection()}, but will also correct either the
     * x or y position for the {@link Odometer}.
     */
    public void quickLocalization() {
        // turn to the nearest right angle
        movCon.turnTo(movCon.roundAngle());
        // perform quick theta correction
        quickThetaCorrection();
        // update the x or y position of the odometer (depending on orientation)
        switch (movCon.roundAngle()) {
        case 0:
            odo.setY(MovementController.roundDistance(odo.getXYT()[1], Main.TILE_SIZE) + Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 90:
            odo.setX(MovementController.roundDistance(odo.getXYT()[0], Main.TILE_SIZE) + Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 180:
            odo.setY(MovementController.roundDistance(odo.getXYT()[1], Main.TILE_SIZE) - Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 270:
            odo.setX(MovementController.roundDistance(odo.getXYT()[0], Main.TILE_SIZE) - Main.LT_SENSOR_TO_WHEELBASE);
            break;
        default:
            break;
        }
    }

    /**
     * performs two {@link #quickLocalization()} routines to completely update the
     * odometer's position and angle.
     */
    public void completeQuickLocalization() {
        quickLocalization();
        movCon.driveDistance(-1 * Main.LT_SENSOR_TO_WHEELBASE, false);
        movCon.rotateAngle(90, false, false);
        quickLocalization();
        movCon.driveDistance(-1 * Main.LT_SENSOR_TO_WHEELBASE, false);
    }

    /**
     * light localization routine to be called after
     * {@link #initialUSLocalization()}.
     */
    public void initialLightLocalization() {
        quickThetaCorrection();

        // set the x-position
        switch (startingCorner) {
        case 0:
            odo.setY(Main.TILE_SIZE + Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 1:
            odo.setX(14 * Main.TILE_SIZE - Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 2:
            odo.setY(8 * Main.TILE_SIZE - Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 3:
            odo.setX(Main.TILE_SIZE + Main.LT_SENSOR_TO_WHEELBASE);
            break;
        default:
            break;
        }

        movCon.driveDistance(-1 * Main.LT_SENSOR_TO_WHEELBASE, FAST_SPEED, 1000, false);
        movCon.rotateAngle(90, true, false);
        quickThetaCorrection();

        // set the y-position
        switch (startingCorner) {
        case 0:
            odo.setX(Main.TILE_SIZE + Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 1:
            odo.setY(Main.TILE_SIZE + Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 2:
            odo.setX(14 * Main.TILE_SIZE - Main.LT_SENSOR_TO_WHEELBASE);
            break;
        case 3:
            odo.setY(8 * Main.TILE_SIZE - Main.LT_SENSOR_TO_WHEELBASE);
            break;
        default:
            break;
        }
        movCon.driveDistance(-1 * Main.LT_SENSOR_TO_WHEELBASE, FAST_SPEED, 1000, false);
    }

    /**
     * The subroutine for correcting the odometer's angle. If the robot is placed at
     * a corner tile with walls on each side of the corner, then the robot will
     * "scan" while rotating. It is looking for a large drop in distance measured by
     * the {@link MedianDistanceSensor}. It will record at what angles these
     * large differences in distance occured at and use them to calculate the
     * {@link Odometer}'s angle error.
     */
    public void initialUSLocalization() {
        double fallingEdge = 404; // 404: the angle has not been found
        double risingEdge = 404;

        // get new data in median buffer
        med.flush();

        // make the robot rotate counter-clockwise until it sees a large change in
        // distance
        movCon.rotateAngle(720, false, true);
        // move out of valley
        while (Math.min(med.getFilteredDistance(), MAX_DIST) < 55) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // then start moving clockwise
        movCon.rotateAngle(720, true, true);

        while (Math.min(med.getFilteredDistance(), MAX_DIST) > EDGE_THRESHOLD) {
            // let some time pass between each sample
            try {
                Thread.sleep(US_POLL_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fallingEdge = odo.getXYT()[2];

        while (Math.min(med.getFilteredDistance(), MAX_DIST) < EDGE_THRESHOLD) {
            // let some time pass between each sample
            try {
                Thread.sleep(US_POLL_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        risingEdge = odo.getXYT()[2];

        double alpha = fallingEdge;
        double beta = risingEdge;

        // the odometer's error, to be added to the current odometer's angle later
        double dTheta;

        if (alpha <= beta) {
            dTheta = 255 - (alpha + beta) / 2;
            System.out.println("alpha <= beta");
        } else {
            dTheta = 85 - (alpha + beta) / 2; // increase in ccw direction
            System.out.println("alpha > beta");
        }

        /*
         * correct the odometer. Note: the motor's didn't need to be stopped to perform
         * the update since the odometer is thread-safe.
         */
        odo.update(0, 0, dTheta);
        // face "North"
        movCon.turnTo(0.0);

        switch (startingCorner) {
        case 0:
            odo.setTheta(0);
            break;
        case 1:
            odo.setTheta(270);
            break;
        case 2:
            odo.setTheta(180);
            break;
        case 3:
            odo.setTheta(90);
            break;
        default:
            break;
        }
    }

}
