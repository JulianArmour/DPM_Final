package ca.mcgill.ecse.sensors;

import lejos.robotics.SampleProvider;

/**
 * Provides methodology for getting the difference between two subsequent polls
 * from a light sensor sample provider.
 * 
 * @author Julian Armour, Alice Kazarine
 * @since March 4, 2019
 * @version 1
 */
public class LightDifferentialFilter extends Thread {

    private float          pastSample;
    private SampleProvider colorProvider;
    private float[]        sampleLSData;

    /**
     * 
     * @param LSprovider
     *            A light sensor sample provider
     * @param sampleLS
     *            A buffer for the light sensor sample provider
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