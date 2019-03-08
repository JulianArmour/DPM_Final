package ca.mcgill.ecse211.detectors;

import java.rmi.RemoteException;

import lejos.remote.ev3.RMISampleProvider;

public class WeightDetector {

    private RMISampleProvider touchSampler;
    
    public WeightDetector(RMISampleProvider rmiSampleProvider) {
        this.touchSampler = rmiSampleProvider;
    }
    
    /**
     * @return true the can is heavy
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
