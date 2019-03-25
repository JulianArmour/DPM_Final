package ca.mcgill.ecse211.strategies;

import ca.mcgill.ecse211.detectors.CanColour;
import lejos.hardware.Sound;

/**
 * Provides methods for beeping sequences for certain checkpoints.
 * 
 * @author Julian Armour
 *
 */
public class Beeper {
    
    public static void arrivedAtSearchLL() {
        nBeeps(5);
    }
    
    public static void arrivedAtSearchUR() {
        nBeeps(5);
    }
    
    public static void localized() {
        nBeeps(1);
    }
    
    public static void foundCan() {
        nBeeps(10);
    }
    
    private static void nBeeps(int n) {
        for (int i = 0; i < n; i++) {
            Sound.beep();
            Sound.pause(100);
        }
    }

    public static void colourAndWeightBeep(boolean canIsHeavy, CanColour canColour) {
        // TODO Auto-generated method stub
        
    }
    
}
