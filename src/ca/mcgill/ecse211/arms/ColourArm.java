package ca.mcgill.ecse211.arms;

import ca.mcgill.ecse211.detectors.ColourDetector;
import lejos.hardware.motor.EV3MediumRegulatedMotor;

/**
 * Provides the methods for moving the colour detection light sensor around the
 * can.
 * 
 * @author Julian Armour
 * @since March 13, 2019
 * @version 1
 */
public class ColourArm {
    private static final int        INIT_SCAN_POS = 0;
    private static final int        FIN_SCAN_POS  = -228;
    private static final int SCAN_SPEED = 90;

    private EV3MediumRegulatedMotor colourMotor;

    public ColourArm(EV3MediumRegulatedMotor colourMotor) {
        this.colourMotor = colourMotor;
        colourMotor.resetTachoCount();
    }

    /**
     * Moves the colour motor around the can and back.
     * <p>
     * This should be called from {@link ColourDetector#collectColourData(int)}
     * 
     * @param numberOfScans
     *            The number of scan to perform.
     * 
     * @author Julian Armour
     * @since March 13, 2019
     */
    public void scan(int numberOfScans) {
        for (int i = 0; i < numberOfScans; i++) {
            colourMotor.setSpeed(SCAN_SPEED);
            colourMotor.rotateTo(FIN_SCAN_POS, false);
            colourMotor.rotateTo(INIT_SCAN_POS, false);
        }
    }
}
