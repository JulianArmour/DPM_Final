package ca.mcgill.ecse211.detectors;

import java.util.List;

import ca.mcgill.ecse211.arms.ArmController;

/**
 * Contains methods for collecting colour data from cans and determining the
 * most probable colour from this data
 * 
 * @author Julian Armour, Cedric Barre
 * @version 1
 * @since March 8 2019
 */
public class ColourDetector {
    
    private ArmController armController;

    public ColourDetector(ArmController armController) {
        this.armController = armController;
    }

    /**
     * This is the main entry point for starting the colour detection routine.
     * <p>
     * It should be called after {@link ArmController#grabCanOnFloor()}
     * 
     * @return a list of colour detected from the can
     * 
     * @author Julian Armour
     * @since March 8, 2019
     */
    public List<float[]> collectColourData() {
        // TODO implement this. This method uses the ArmController class.
        armController.moveArmToScanningPosition();
        // TODO start scanning routine
        return null;
    }
}
