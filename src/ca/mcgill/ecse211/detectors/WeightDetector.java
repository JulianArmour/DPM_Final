package ca.mcgill.ecse211.detectors;

import java.rmi.RemoteException;

import lejos.remote.ev3.RMISampleProvider;

/**
 * Provides methodology for determining if a can is heavy
 * 
 * @author Julian Armour
 * @since March 8, 2019
 * @version 1
 */
public class WeightDetector {

    private RMISampleProvider touchSampler;

    /**
     * 
     * @param rmiTouchSampleProvider
     *            a remote touch sensor
     */
    public WeightDetector(RMISampleProvider rmiTouchSampleProvider) {
        this.touchSampler = rmiTouchSampleProvider;
    }

    /**
     * @return true if the can is heavy
     * 
     * @author Julian Armour
     * @since March 8 2019
     */
    public boolean isCanHeavy() {
        try {
            if (touchSampler.fetchSample()[0] == 1) {
                return true;
            } else {
                return false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

}
