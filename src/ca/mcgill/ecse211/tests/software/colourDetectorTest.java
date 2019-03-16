package ca.mcgill.ecse211.tests.software;

import java.util.List;

import ca.mcgill.ecse211.arms.ColourArm;
import ca.mcgill.ecse211.detectors.ColourDetector;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.SensorMode;

public class colourDetectorTest {
	
	private static ColourArm arm;
	private static EV3MediumRegulatedMotor colourMotor;
	private static ColourDetector colDet;
	private static Port sideLSPort;
    private static EV3ColorSensor canColourSensor;
    private static SensorMode canRGBProvider;
    private static final int numberOfScans = 1;
    
    
    private static List<float[]> colourData;


	public static void main(String args[]) {
		colourMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));
		arm = new ColourArm(colourMotor);
		sideLSPort = LocalEV3.get().getPort("S1");
        canColourSensor = new EV3ColorSensor(sideLSPort);
        canRGBProvider = canColourSensor.getMode("RGB");
        colDet = new ColourDetector(arm, canRGBProvider);
        
        Button.waitForAnyPress();
        
        colDet.collectColourData(numberOfScans);
        colourData = colDet.getColourSamples();
        
        float[] tempArray;
        for(int i = 0; i < colourData.size(); i++) {
        	tempArray = colourData.get(i);
        	System.out.println(tempArray[0] + " 	" + tempArray[1] + " 	" + tempArray[2]);
        }
	}
}
