package ca.mcgill.ecse211.arms;

import lejos.hardware.motor.NXTRegulatedMotor;

/**
 * Provides methods for controlling the motor that will grab the cans.
 * 
 * @author Julian Armour
 * @version 1
 * @since March 5, 2019
 */
public class Claw {
    private static final int CLAW_SPEED = 200;
    private NXTRegulatedMotor claw;
    private static int        RELEASED_ANGLE = 15;// TODO
    private static int        GRABBED_ANGLE  = 0; // TODO

    /**
     * @param clawMotor
     *            the motor used for the claw that grabs the cans
     * @author Julian Armour
     */
    public Claw(NXTRegulatedMotor clawMotor) {
        this.claw = clawMotor;
    }

    /**
     * Causes the claw arm to close around a can.
     * 
     * @author Julian Armour
     * @since March 5, 2019
     */
    public void grabCan() {
            claw.setSpeed(CLAW_SPEED);
            claw.rotateTo(GRABBED_ANGLE, false);
    }

    /**
     * Releases a can that's held in the claw.
     * 
     * @author Julian Armour
     * @since March 5, 2019
     */
    public void releaseCan() {
            claw.setSpeed(CLAW_SPEED);
            claw.rotateTo(RELEASED_ANGLE, false);
    }

}
