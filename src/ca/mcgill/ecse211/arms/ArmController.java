package ca.mcgill.ecse211.arms;

import ca.mcgill.ecse211.detectors.ColourDetector;

/**
 * Provides the methods for all needed arm movement routines.
 * 
 * @author Julian Armour
 * @since March 8 2019
 * @version 1
 *
 */
public class ArmController {

    private Claw  claw;
    private Elbow elbow;

    public ArmController(Claw claw, Elbow elbow) {
        this.claw = claw;
        this.elbow = elbow;
    }

    /**
     * Positions the arm in such a way that it is ready to grab a can in front of
     * the robot. This method assumes that the can is properly located in front of
     * the robot so that the arm movements will work.
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void grabCanOnFloor() {
        claw.releaseCan();
        elbow.lowerArmToFloor();
        claw.grabCan();
    }

    /**
     * <b>This method assumes the claw is already holding a can</b>. It will place
     * the can held in the claw into the basket and release the claw.
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void dropCanInBasket() {
        elbow.raiseArmToBasket();
        claw.releaseCan();
    }

    /**
     * Moves the arm into a position that doesn't obstruct the ultrasonic sensor.
     * This is the position that the arm is in when placing a can into the basket.
     * <p>
     * Alias for {@link #dropCanInBasket()}
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void moveArmToIdlePosition() {
        dropCanInBasket();
    }

    /**
     * To be called before the colour scanning routine. This moves the arm into the
     * proper position for colour scanning.
     * <p>
     * This method should only be called from
     * {@link ColourDetector#collectColourData()}
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public void moveArmToScanningPosition() {
        elbow.moveArmToStartColourScan();
    }

    /**
     * Performs the movements needed for the {@link ColourDetector} data collection.
     * <p>
     * This method should only be called from
     * {@link ColourDetector#collectColourData()}
     * 
     * @param numberOfScans
     *            the can will be scanned 2 x numberOfScans.
     * @author Julian Armour
     * @since March 8 2019
     */
    public void performScanMovement(int numberOfScans) {
        for (int i = 0; i < numberOfScans; i++) {
            elbow.moveArmToEndColourScan();
            elbow.moveArmToStartColourScan();
        }
    }

}
