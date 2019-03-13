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

    private EV3LargeRegulatedMotor clawMotor;
    private MovementController     movementController;

    public WeightDetector(EV3LargeRegulatedMotor clawMotor, MovementController movementController) {
        this.clawMotor = clawMotor;
        this.movementController = movementController;
    }

    /**
     * @return true if the can is heavy
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public boolean isCanHeavy() {
        // TODO
    }

}
