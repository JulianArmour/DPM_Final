package ca.mcgill.ecse211.strategies;

import ca.mcgill.ecse211.detectors.CanColour;
import lejos.hardware.Sound;

/**
 * Provides methods for beeping sequences when certain checkpoints are reached
 * in the competition.
 * 
 * @author Julian Armour
 * @version 2
 *
 */
public class Beeper {

    /**
     * Performs a beep sequence for when the initial localizatin is completed.
     * 
     * @author Julian Armour
     * @since 1
     */
    public static void localized() {
        nShortBeeps(3);
    }

    /**
     * 
     * @param n
     *            The number of short beeps to perform
     * 
     * @since 2
     * @author Julian Armour
     */
    private static void nShortBeeps(int n) {
        for (int i = 0; i < n; i++) {
            Sound.beep();
            Sound.pause(100);
        }
    }

    /**
     * 
     * @param n
     *            The number of long beeps to perform
     * 
     * @since 2
     * @author Julian Armour
     */
    private static void nLongBeeps(int n) {
        for (int i = 0; i < n; i++) {
            Sound.buzz();
            Sound.pause(100);
        }
    }

    /**
     * Performs a beep sequence determined by the weight and colour of a can.
     * 
     * @param canIsHeavy
     *            <code>true</code> if the can is heavy.
     * @param canColour
     *            the colour of the can
     */
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

    /**
     * Performs a beeping sequence for when the robot arrives at the search zone for the first time.
     */
    public static void arrivedAtSearchZone() {
        nShortBeeps(3);
    }

    /**
     * Performs a beeping sequence for when the robot drops off cans at the starting tile.
     */
    public static void droppedOffCans() {
        nShortBeeps(5);
    }

}
