package ca.mcgill.ecse211.detectors;

import java.util.LinkedList;
import java.util.List;

import ca.mcgill.ecse211.arms.ArmController;
import lejos.robotics.SampleProvider;
import lejos.utility.Timer;
import lejos.utility.TimerListener;

/**
 * Contains methods for collecting colour data from cans and determining the
 * most probable colour from this data
 * 
 * @author Julian Armour, Cedric Barre
 * @version 1
 * @since March 8 2019
 */
public class ColourDetector implements TimerListener {

    private static final int COLOUR_POLL_PERIOD = 50;
    private ArmController    armController;
    private SampleProvider   colourSampler;

    private List<float[]>    colourSamples;
    private Timer            colourPoller;

    /**
     * 
     * @param armController
     *            The robot's arm controller
     * @param colourSampler
     *            The RGB colour sample provider
     */
    public ColourDetector(ArmController armController, SampleProvider colourSampler) {
        this.armController = armController;
        this.colourSampler = colourSampler;
        this.colourPoller = new Timer(COLOUR_POLL_PERIOD, this);
    }

    /**
     * This is the main entry point for starting the colour detection routine.
     * <p>
     * It should be called after {@link ArmController#grabCanOnFloor()}
     * <p>
     * The colour data can then be retrieved with {@link #getColourSamples()}
     * 
     * @param numberOfScans
     *            The can will be scanned 2 x numberOfScans.
     * 
     * @author Julian Armour
     * @since March 8, 2019
     */
    public void collectColourData(int numberOfScans) {
        armController.moveArmToScanningPosition();
        // initialize a new list
        colourSamples = new LinkedList<float[]>();
        // start polling colour data
        colourPoller.start();
        // start arm scan movement routine
        armController.performScanMovement(numberOfScans);
        // stop polling colour data
        colourPoller.stop();
    }

    /**
     * 
     * @return The colour samples from the previous scan
     */
    public List<float[]> getColourSamples() {
        return colourSamples;
    }

    /**
     * Fetches a colour sample and adds it to a list.
     * 
     * @author Julian Armour
     * @since March 12, 2019
     */
    @Override
    public void timedOut() {
        float[] sample = new float[colourSampler.sampleSize()];
        colourSampler.fetchSample(sample, 0);
        colourSamples.add(sample);
    }

    /**
     * Given a list of colour samples, this method will calculate and return the
     * detected colour of a can.
     * 
     * @param colourSamples
     *            A list of colour samples
     * @return The detected colour of the can
     * 
     * @author Cedric Barre
     * @since TODO
     */
    public CanColour getCanColour(List colourSamples) {
        // TODO Implement this
        return null;
    }
}
