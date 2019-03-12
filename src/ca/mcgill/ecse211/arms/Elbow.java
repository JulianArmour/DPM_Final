package ca.mcgill.ecse211.arms;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;

/**
 * Provides the methods for controlling the robotic arm this is used to grab
 * cans.
 * 
 * @author Julian Armour
 * @version 1
 * @since March 8 2019
 */
public class Elbow {
    private static final int  FAST_SPEED         = 0;
    private NXTRegulatedMotor elbow;
    // angle for when the arm is resting in front of the ev3
    private static int        LOWERED_ANGLE      = 1;  // TODO
    // angle for dropping the can
    private static int        RAISED_ANGLE       = 2;  // TODO
    // angle for starting colour detection
    private static int        START_COLOUR_ANGLE = 3;  // TODO
    // angle for ending colour detection
    private static int        END_COLOUR_ANGLE   = 4;  // TODO
    // the speed for scanning colour data
    private static int        SCAN_SPEED         = 100;

    /**
     * @param elbowMotor
     *            the motor used for the elbow that puts cans in and out of the
     *            basket, and scanning can colours
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public Elbow(NXTRegulatedMotor elbowMotor) {
        this.elbow = elbowMotor;
    }

    /**
     * Lowers the elbow arm until it is in-front of the ev3
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void lowerArmToFloor() {
            elbow.setSpeed(FAST_SPEED);
            elbow.rotateTo(LOWERED_ANGLE, false);
    }

    /**
     * Raises the elbow arm to the basket
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void raiseArmToBasket() {
            elbow.setSpeed(FAST_SPEED);
            elbow.rotateTo(RAISED_ANGLE, false);
    }

    /**
     * Moves the elbow arm to the angle needed to start scanning the can for colour
     * data.
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void moveArmToStartColourScan() {
            elbow.setSpeed(FAST_SPEED);
            elbow.rotateTo(START_COLOUR_ANGLE, false);
    }

    /**
     * Moves the elbow arm to the angle at the end of the colour scan
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void moveArmToEndColourScan() {
            elbow.setSpeed(FAST_SPEED);
            elbow.rotateTo(END_COLOUR_ANGLE, false);
    }

    /**
     * Moves the elbow up to the ending angle at the end of the colour scan, but
     * does so slowly.
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void scanMotionUp() {
            elbow.setSpeed(SCAN_SPEED);
            elbow.rotateTo(END_COLOUR_ANGLE, false);
    }

    /**
     * Moves the elbow down to the angle at the starting angle of the colour scan,
     * but does so slowly.
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void scanMotionDown() {
            elbow.setSpeed(SCAN_SPEED);
            elbow.rotateTo(START_COLOUR_ANGLE, false);
    }

}
