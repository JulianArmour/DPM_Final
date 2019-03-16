package ca.mcgill.ecse211.detectors;

import ca.mcgill.ecse211.navigators.MovementController;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * Provides methodology for determining if a can is heavy
 * 
 * @author Julian Armour
 * @since March 13, 2019
 * @version 2
 */
public class WeightDetector {
    // constants
    private static final int       HEAVY_CAN_THRESHOLD = 30;   // TODO
    private static final int       DETECT_SPEED        = 1000;
    private static final int       DETECT_ACC          = 10000;
    // dependencies
    private EV3LargeRegulatedMotor clawMotor;
    private MovementController     movementController;
    // fields
    private double                 tileLength;

    /**
     * 
     * @param clawMotor
     *            The motor used for the mechanical claw in front of the robot
     * @param movementController
     * @param tileSize
     *            The length of the tiles
     */
    public WeightDetector(EV3LargeRegulatedMotor clawMotor, MovementController movementController, double tileSize) {
        this.clawMotor = clawMotor;
        this.movementController = movementController;
        this.tileLength = tileSize;
    }

    /**
     * Jerks the robot back which causes the claw to open up past a certain
     * threshold when holding a heavy can. The robot will return to it's original
     * position after this motion.
     * 
     * @return true if the can is heavy
     * 
     * @author Julian Armour
     * @since March 14 2019
     */
    public boolean canIsHeavy() {
        int initTacho = clawMotor.getTachoCount();
        clawMotor.flt();
        movementController.driveDistance(-tileLength / 2, DETECT_SPEED, DETECT_ACC, false);
        int dTacho = Math.abs(clawMotor.getTachoCount() - initTacho);
        movementController.driveDistance(tileLength / 2, false);
        return (dTacho > HEAVY_CAN_THRESHOLD);
    }

}
