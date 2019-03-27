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
        nShortBeeps(5);
    }
    
    public static void arrivedAtSearchUR() {
        nShortBeeps(5);
    }
    
    public static void localized() {
        nShortBeeps(1);
    }
    
    public static void foundCan() {
        nShortBeeps(10);
    }
    
    private static void nShortBeeps(int n) {
        for (int i = 0; i < n; i++) {
            Sound.beep();
            Sound.pause(100);
        }
    }
    
    private static void nLongBeeps(int n) {
        for (int i = 0; i < n; i++) {
            Sound.buzz();
            Sound.pause(100);
        }
    }

    public static void colourAndWeightBeep(boolean canIsHeavy, CanColour canColour) {
        if (canIsHeavy) {
            switch (canColour) {
            case RED:
                nLongBeeps(4);
                break;
            case YELLOW:
                nLongBeeps(3);
                break;
            case GREEN:
                nLongBeeps(2);
                break;
            case BLUE:
                nLongBeeps(1);
                break;
            }
        } else {
            switch (canColour) {
            case RED:
                nShortBeeps(4);
                break;
            case YELLOW:
                nShortBeeps(3);
                break;
            case GREEN:
                nShortBeeps(2);
                break;
            case BLUE:
                nShortBeeps(1);
                break;
            }
        }
    }
    
}
