package ca.mcgill.ecse211.arms;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Provides methods for controlling the motor that will grab the cans.
 * 
 * @author Julian Armour
 * @version 3
 * @since March 14, 2019
 */
public class Claw {

    private EV3LargeRegulatedMotor claw;
    private static final int       CLAW_SPEED = 360;
    private static int             RELEASED_ANGLE = 0;
    private static int             GRABBED_ANGLE  = 176;
    private static int             NEUTRAL_ANGLE = 175; //TODO

    
    /**
     * @param clawMotor
     *            the motor used for the claw that grabs the cans
     * @author Julian Armour
     */
    public Claw(EV3LargeRegulatedMotor clawMotor) {
        this.claw = clawMotor;
        clawMotor.resetTachoCount();
    }

    /**
     * Causes the claw arm to close around a can.
     * 
     * @author Julian Armour
     * @since March 5, 2019
     */
    public void closeClaw() {
        claw.setSpeed(CLAW_SPEED);
        claw.rotateTo(GRABBED_ANGLE, false);
    }
    
    /**
     * Causes the claw to rest in front of a can for weight detection.
     * 
     * @author Julian Armour
     * @since March 16, 2019
     */
    public void closeClawForWeighing() {
        claw.setSpeed(CLAW_SPEED);
        claw.rotateTo(NEUTRAL_ANGLE, false);
    }

    /**
     * Releases a can that's held in the claw.
     * 
     * @author Julian Armour
     * @since March 5, 2019
     */
    public void openClaw() {
        claw.setSpeed(CLAW_SPEED);
        claw.rotateTo(RELEASED_ANGLE, false);
    }

}
