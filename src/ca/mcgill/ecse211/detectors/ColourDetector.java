package ca.mcgill.ecse211.detectors;

import java.util.ArrayList;
import java.util.Collections;
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
	
	private static final float RCAN_RMEAN = 0.8920f;
	private static final float RCAN_GMEAN = 0.3672f;
	private static final float RCAN_BMEAN = 0.2668f;
	
	private static final float BCAN_RMEAN = 0.4212f;
	private static final float BCAN_GMEAN = 0.5010f;
	private static final float BCAN_BMEAN = 0.7568f;
	
	private static final float YCAN_RMEAN = 0.8515f;
	private static final float YCAN_GMEAN = 0.5303f;
	private static final float YCAN_BMEAN = 0.2114f;
	
	private static final float GCAN_RMEAN = 0.5435f;
	private static final float GCAN_GMEAN = 0.7849f; 
	private static final float GCAN_BMEAN = 0.4489f;

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
    	float RMean = 0.0f;
		float GMean = 0.0f;
		float BMean = 0.0f;
		float NRMean, NGMean, NBMean;
		float[][] data = new float[colourSamples.size()][3];
	
		for(int i = 0; i < colourSamples.size(); i++) {
			data[i] = (float[]) colourSamples.get(i);
			RMean += data[i][0];
			GMean += data[i][1];
			BMean += data[i][2];
		}
		RMean /= data.length;
		GMean /= data.length;
		BMean /= data.length;
		
		NRMean = (float) (RMean / Math.sqrt(Math.pow(RMean, 2) + Math.pow(GMean, 2) + Math.pow(BMean, 2)));
		NGMean = (float) (GMean / Math.sqrt(Math.pow(RMean, 2) + Math.pow(GMean, 2) + Math.pow(BMean, 2)));
		NBMean = (float) (BMean / Math.sqrt(Math.pow(RMean, 2) + Math.pow(GMean, 2) + Math.pow(BMean, 2)));
		
		System.out.println("NR: " + NRMean);
		System.out.println("NG: " + NGMean);
		System.out.println("NB: " + NBMean);
		
		return colorMatch(NRMean, NGMean, NBMean);
		
		
    }
    /**
	 * Method to calculate the distance of the normalized experimental RGB data to the characterized data for each can color in order to find which color 
	 * the experimental data is closer to   
	 * @param RMean Normalized R values of the experimental data
	 * @param GMean Normalized G values of the experimental data
	 * @param BMean Normalized B values of the experimental data
	 * @return The integer corresponding to the numerical representation of the can color
	 */
	private static CanColour colorMatch(float RMean, float GMean, float BMean) {
		Float dRCan, dBCan, dYCan, dGCan;
		float min;
		CanColour dataCanColor;
		ArrayList<Float> dArray = new ArrayList<Float>();
		
		dRCan = (float) Math.sqrt(Math.pow((RMean - RCAN_RMEAN), 2) + Math.pow((GMean - RCAN_GMEAN), 2) + Math.pow((BMean - RCAN_BMEAN), 2));
		dBCan = (float) Math.sqrt(Math.pow((RMean - BCAN_RMEAN), 2) + Math.pow((GMean - BCAN_GMEAN), 2) + Math.pow((BMean - BCAN_BMEAN), 2));
		dYCan = (float) Math.sqrt(Math.pow((RMean - YCAN_RMEAN), 2) + Math.pow((GMean - YCAN_GMEAN), 2) + Math.pow((BMean - YCAN_BMEAN), 2));
		dGCan = (float) Math.sqrt(Math.pow((RMean - GCAN_RMEAN), 2) + Math.pow((GMean - GCAN_GMEAN), 2) + Math.pow((BMean - GCAN_BMEAN), 2));
		
		dArray.add(dRCan);
		dArray.add(dBCan);
		dArray.add(dYCan);
		dArray.add(dGCan);
		
		min = Collections.min(dArray);
		
		System.out.println("min: " + min);
		
		if(min == dRCan) dataCanColor = CanColour.RED;
		else if(min == dYCan) dataCanColor = CanColour.YELLOW;
		else if(min == dGCan) dataCanColor = CanColour.GREEN;
		else dataCanColor = CanColour.BLUE;
		
		return dataCanColor;
		
		
		
	}
	
}
