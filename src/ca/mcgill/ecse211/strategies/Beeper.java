package ca.mcgill.ecse211.strategies;

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
            Sound.systemSound(true, 4);
        }
    }
    
}
