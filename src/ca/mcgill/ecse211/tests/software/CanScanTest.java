package ca.mcgill.ecse211.tests.software;

import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.arms.Claw;
import ca.mcgill.ecse211.arms.ColourArm;
import ca.mcgill.ecse211.detectors.CanColour;
import ca.mcgill.ecse211.detectors.ColourDetector;
import ca.mcgill.ecse211.detectors.WeightDetector;
import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.navigators.Navigator;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import ca.mcgill.ecse211.sensors.LightDifferentialFilter;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;
import ca.mcgill.ecse211.strategies.CanSearch;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;

public class CanScanTest {

    public static final double             WHEEL_RAD     = Main.WHEEL_RAD;
    public static final double             TRACK         = Main.TRACK;
    public static final int                SC            = 3;
    public static final float             TILE_LENGTH   = Main.TILE_SIZE;

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
    private static Navigator               navigator;

    private static CanColour               canColour     = CanColour.RED;

    public static int                      Red_LL_x      = 0;
    public static int                      Red_LL_y      = 0;
    public static int                      Red_UR_x      = 4;
    public static int                      Red_UR_y      = 9;

    public static int[]                    startzone_LL  = { Red_LL_x, Red_LL_y };
    public static int[]                    startzone_UR  = { Red_UR_x, Red_UR_y };

    public static int                      Island_LL_x   = 6;
    public static int                      Island_LL_y   = 0;
    public static int                      Island_UR_x   = 15;
    public static int                      Island_UR_y   = 9;

    public static int[]                    ILL           = { Island_LL_x, Island_LL_y };
    public static int[]                    IUR           = { Island_UR_x, Island_UR_y };

    public static int                      TNR_LL_x      = 4;
    public static int                      TNR_LL_y      = 8;
    public static int                      TNR_UR_x      = 6;
    public static int                      TNR_UR_y      = 9;

    public static int[]                    TLL           = { TNR_LL_x, TNR_LL_y };
    public static int[]                    TUR           = { TNR_UR_x, TNR_UR_y };

    public static int                      TNG_LL_x;
    public static int                      TNG_LL_y;
    public static int                      TNG_UR_x;
    public static int                      TNG_UR_y;

    public static int                      SZR_LL_x      = 8;
    public static int                      SZR_LL_y      = 6;
    public static int                      SRZ_UR_x      = 10;
    public static int                      SRZ_UR_y      = 8;

    public static int[]                    searchzone_LL = { SZR_LL_x, SZR_LL_y };
    public static int[]                    searchzone_UR = { SRZ_UR_x, SRZ_UR_y };

    public static int                      SZG_LL_x;
    public static int                      SZG_LL_y;
    public static int                      SZG_UR_x;
    public static int                      SZG_UR_y;
    private static Claw                    claw;
    private static EV3LargeRegulatedMotor  clawMotor;
    private static EV3MediumRegulatedMotor colourMotor;
    private static WeightDetector          weightDetector;
    private static ColourArm               colourArm;
    private static ColourDetector          colourDetector;
    private static Port                    sideLSPort;
    private static EV3ColorSensor          canColourSensor;
    private static SensorMode              canRGBProvider;
    private static float[]                 canRGBBuffer;
    private static CanSearch               canSearch;

    public static void main(String[] args) {
        // set up side ultrasonic sensor
        USPort = LocalEV3.get().getPort("S2");
        ultrasonicSensor = new EV3UltrasonicSensor(USPort);
        distanceProvider = ultrasonicSensor.getMode("Distance");
        USSample = new float[distanceProvider.sampleSize()];

        // set up left light sensor
        leftLSPort = LocalEV3.get().getPort("S3");
        leftLightSensor = new EV3ColorSensor(leftLSPort);
        leftLSProvider = leftLightSensor.getMode("Red");
        leftLSSample = new float[leftLSProvider.sampleSize()];

        // set up right light sensor
        rightLSPort = LocalEV3.get().getPort("S4");
        rightLightSensor = new EV3ColorSensor(rightLSPort);
        rightLSProvider = rightLightSensor.getMode("Red");
        rightLSSample = new float[rightLSProvider.sampleSize()];

        // set up colour light sensor
        sideLSPort = LocalEV3.get().getPort("S1");
        canColourSensor = new EV3ColorSensor(sideLSPort);
        canRGBProvider = canColourSensor.getMode("RGB");
        canRGBBuffer = new float[canRGBProvider.sampleSize()];

        // set up wheel motors
        leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
        rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
        clawMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
        colourMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));
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
        localizer = new Localization(
                movementController, odometer, medianDistanceSensor, leftLightDiff, rightLightDiff, SC
        );

        navigator = new Navigator(
                movementController, odometer, localizer, TLL, TUR, startzone_LL, startzone_UR, SC, ILL, IUR,
                searchzone_LL, searchzone_UR, TILE_LENGTH
        );
        claw = new Claw(clawMotor);
        colourArm = new ColourArm(colourMotor);
        weightDetector = new WeightDetector(clawMotor, movementController, TILE_LENGTH);
        colourDetector = new ColourDetector(colourArm, canRGBProvider);
        canSearch = new CanSearch(
                odometer, movementController, navigator, medianDistanceSensor, claw, weightDetector, colourDetector,
                canColour, searchzone_LL, searchzone_UR, TLL, TUR, ILL, IUR, SC, (float) (2*TILE_LENGTH), TILE_LENGTH
        );

        localEV3 = (LocalEV3) LocalEV3.get();

        // start test
        localEV3.getTextLCD().clear();
        
        canSearch.setScanPositions();
        
        odometer.setXYT(searchzone_LL[0] * TILE_LENGTH, searchzone_LL[1] * TILE_LENGTH, 270);
        
        float[] P_SZ_LL = new float[] { (float) (searchzone_LL[0] * TILE_LENGTH),
                (float) (searchzone_LL[1] * TILE_LENGTH) };
        float[] P_SZ_UR = new float[] { (float) (searchzone_UR[0] * TILE_LENGTH),
                (float) (searchzone_UR[1] * TILE_LENGTH) };
        
        float[] possiblePos = canSearch.fastCanScan(P_SZ_LL, P_SZ_UR, (double)355, (float) (2*TILE_LENGTH));
        if(possiblePos == null) {
        	System.out.println("NOT FOUND");
        } else {
        	boolean foundCan = canSearch.travelToCan(possiblePos);
    		if (foundCan) {
                System.out.println("Found the can!");
                claw.closeClaw();
                claw.openClaw();
                colourDetector.collectColourData(1);
                CanColour canColour = colourDetector.getCanColour(colourDetector.getColourSamples());
                System.out.println(canColour);
            }
    		
        }
        
        System.exit(0);
    }
}
