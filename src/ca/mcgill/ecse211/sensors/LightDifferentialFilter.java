package ca.mcgill.ecse211.sensors;

import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

/**
 * Provides methodology for getting the difference between two sequential light
 * sensor samples.
 * 
 * @author Alice Kazarine
 * @since Feb 12, 2019
 * @version 1
 *
 */
public class LightDifferentialFilter extends Thread {

    private float          pastSample;
    private SampleProvider colorProvider;
    private float[]        sampleLSData;

    /**
     * 
     * @param LSprovider
     *            a sample provider for a {@link EV3ColorSensor}
     * @param sampleLS
     *            a buffer for the sample provider
     */
    public LightDifferentialFilter(SampleProvider LSprovider, float[] sampleLS) {
        this.pastSample = 0;
        this.colorProvider = LSprovider;
        this.sampleLSData = sampleLS;
        getDeltaL(); // get an initial intensity
    }

    /**
     * Fetches new samples to get rid of old ones.
     */
    public void flush() {
        getDeltaL();
    }

    /**
     * Calculates the difference between two sequential light sensor sample polls.
     * 
     * @return the difference between two sequential light sensor sample polls.
     */
    public float getDeltaL() {

        colorProvider.fetchSample(sampleLSData, 0);

        // calculate the difference between current and past light intensity
        float deltaL = (100 * sampleLSData[0] - pastSample);

        // store the last data in past Data
        pastSample = 100 * sampleLSData[0];

        return deltaL;
    }

}