package ca.mcgill.ecse211.arms;

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
        claw.releaseCan(); // open the claw
        elbow.lowerArmToFloor();
        claw.grabCan();
    }

    public void dropCanInBasket() {

    }

}
