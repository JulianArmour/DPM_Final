package ca.mcgill.ecse211.tests.software;

import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.LightDifferentialFilter;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;

public class InitialLocalizationTest {

    public static final double             WHEEL_RAD = 2.2;
    public static final double             TRACK     = 17.3;
    public static final int                SC        = 2;

    private static Port                    USPort;
    private static EV3UltrasonicSensor     ultrasonicSensor;
    private static SensorMode              distanceProvider;
    private static float[]                 USSample;
    private static Odometer                odometer;
    private static EV3LargeRegulatedMotor  leftMotor;
    private static EV3LargeRegulatedMotor  rightMotor;
    private static MovementController      movementController;
    private static MedianDistanceSensor    medianDistanceSensor;
    private static LocalEV3                localEV3;
    private static LightDifferentialFilter leftLightDiff;
    private static Localization            localizer;
    private static Port                    leftLSPort;
    private static Port                    rightLSPort;
    private static EV3ColorSensor          leftLightSensor;
    private static SensorMode              leftLSProvider;
    private static float[]                 leftLSSample;
    private static EV3ColorSensor          rightLightSensor;
    private static SensorMode              rightLSProvider;
    private static float[]                 rightLSSample;
    private static LightDifferentialFilter rightLightDiff;

    public static void main(String[] args) {
        // set up side ultrasonic sensor
        USPort = LocalEV3.get().getPort("S2");
        ultrasonicSensor = new EV3UltrasonicSensor(USPort);
        distanceProvider = ultrasonicSensor.getMode("Distance");
        USSample = new float[distanceProvider.sampleSize()];

        // set up left light sensor
        leftLSPort = LocalEV3.get().getPort("S4");
        leftLightSensor = new EV3ColorSensor(leftLSPort);
        leftLSProvider = leftLightSensor.getMode("Red");
        leftLSSample = new float[leftLSProvider.sampleSize()];

        // set up right light sensor
        rightLSPort = LocalEV3.get().getPort("S1");
        rightLightSensor = new EV3ColorSensor(rightLSPort);
        rightLSProvider = rightLightSensor.getMode("Red");
        rightLSSample = new float[rightLSProvider.sampleSize()];

        // set up wheel motors
        leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
        rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
        // starts odometer
        try {
            odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
        } catch (OdometerExceptions e) {
            System.out.println("Could not get odometer.");
        }
        Thread odoThread = new Thread(odometer);
        odoThread.start();
        // initialize instances
        movementController = new MovementController(leftMotor, rightMotor, WHEEL_RAD, TRACK, odometer);
        medianDistanceSensor = new MedianDistanceSensor(distanceProvider, USSample, odometer, 5);
        leftLightDiff = new LightDifferentialFilter(leftLSProvider, leftLSSample);
        rightLightDiff = new LightDifferentialFilter(rightLSProvider, rightLSSample);
        localizer = new Localization(movementController, odometer, medianDistanceSensor, leftLightDiff, rightLightDiff,
                SC);
        localEV3 = (LocalEV3) LocalEV3.get();

        // start test
        localEV3.getTextLCD().clear();
        System.out.println("Press any button to start.");
//        Button.waitForAnyPress();
//         movementController.rotateAngle(360, true, false);
        localizer.initialUSLocalization();
        localizer.initialLightLocalization();
        System.out.println(odometer.getXYT()[0]+","+odometer.getXYT()[1]+","+odometer.getXYT()[2]);
        System.exit(0);
    }
}
