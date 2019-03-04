package ca.mcgill.ecse.sensors;
import java.util.Arrays;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.robotics.SampleProvider;


/**
 * USSensor is the driver for the ultrasonic sensor. It  calls the
 * ultrasonic sensor to poll a sample. It then calculates and stores the median distance
 * for the previous 5 samples.
 * 
 * @author Julian Armour, Alice Kazarine
 * @version 1.0
 * @since 2019-02-01
 */

public class UltrasonicMedianFilter {
	
    private int[] pastData;
    private int median;
    private SampleProvider usSampler;
    private float[] USData;

    /**
     * Creates a MedianDistanceSensor object
     * 
     * @param USSampleProvider The ultrasonic sample provider
     * @param USSample         The data storage array for the sample
     */
    public UltrasonicMedianFilter(SampleProvider USSampleProvider, float[] USSample, Odometer odometer) {
        // moving median filter of length 5
        this.pastData = new int[] { 255, 255, 255, 255, 255 };
        this.median = 255;
        this.usSampler = USSampleProvider;
        this.USData = USSample;
    }
    
    /**
     * Overrides old median data with new data
     */
    public void flush() {
        for (int i = 0; i < pastData.length; i++) {
            fetchAndFilter();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Polls a sample from the ultrasonic sensor and filters it.
     */
    private void fetchAndFilter() {
        usSampler.fetchSample(USData, 0);
        // System.out.println(USData[0]*100);
        // shift the past data to the left in the array
        for (int i = 0; i < pastData.length - 1; i++) {
            pastData[i] = pastData[i + 1];
        }
        // add the sample to end of pastData
        pastData[pastData.length - 1] = (int) (USData[0] * 100);
        // calculate the median
        median = calculateMedian(pastData.clone());
    }

    /**
     * 
     * @return The current filtered distance
     */
    public int getFilteredDistance() {
        fetchAndFilter();
        return median;
    }

    /**
     * Finds the median in a list
     * 
     * @param data An array for finding the median in.
     * @return The median
     */
    private static int calculateMedian(int[] data) {
        Arrays.sort(data);
        return data[data.length / 2];
    }

	
}