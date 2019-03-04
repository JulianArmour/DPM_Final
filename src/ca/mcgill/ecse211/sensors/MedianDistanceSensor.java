package ca.mcgill.ecse211.sensors;

import java.util.Arrays;

import ca.mcgill.ecse211.odometer.Odometer;
import lejos.robotics.SampleProvider;

/**
 * USSensor is the driver for the ultrasonic sensor. It calls the ultrasonic
 * sensor to poll a sample. It then calculates and stores the median distance
 * for the previous n samples.
 * 
 * @author Julian Armour, Alice Kazarine
 * @since 2019-02-01
 * @version 2
 */
public class MedianDistanceSensor {
    private float[]        pastData;
    private float          median;
    private SampleProvider usSampler;
    private float[]        USData;

    /**
     * Creates a MedianDistanceSensor object
     * 
     * @param USSampleProvider
     *            The ultrasonic sample provider
     * @param USSample
     *            The data storage array for the sample
     */

    /**
     * 
     * @param USSampleProvider
     *            The ultrasonic's sample provider
     * @param USSample
     *            The buffer for the ultrasonic's sample provider
     * @param odometer
     *            The odometer
     * @param size
     *            The size of the past data used to calculate the median
     */
    public MedianDistanceSensor(SampleProvider USSampleProvider, float[] USSample, Odometer odometer, int size) {
        this.pastData = new float[size];
        this.median = 255;
        this.usSampler = USSampleProvider;
        this.USData = USSample;
        flush();
    }

    /**
     * Overrides old median data with new data
     */
    public void flush() {
        for (int i = 0; i < pastData.length; i++) {
            fetchAndFilter();
            try {
                Thread.sleep(50);
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
        // shift the past data to the left in the array
        for (int i = 0; i < pastData.length - 1; i++) {
            pastData[i] = pastData[i + 1];
        }
        // add the sample to end of pastData
        pastData[pastData.length - 1] = (USData[0] * 100);
        // calculate the median
        median = calculateMedian(pastData.clone());
    }

    /**
     * 
     * @return The current filtered distance
     */
    public float getFilteredDistance() {
        fetchAndFilter();
        return median;
    }

    /**
     * Finds the median in a list
     * 
     * @param data
     *            An array for finding the median in.
     * @return The calulated median
     */
    private static float calculateMedian(float[] data) {
        Arrays.sort(data);
        return data[(data.length / 2) + 1];
    }

    /**
     * 
     * @return the size of the median buffer.
     */
    public int bufferSize() {
        return pastData.length;
    }
}