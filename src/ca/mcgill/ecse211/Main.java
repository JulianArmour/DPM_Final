package ca.mcgill.ecse211;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import ca.mcgill.ecse211.arms.ArmController;
import ca.mcgill.ecse211.arms.Claw;
import ca.mcgill.ecse211.arms.Elbow;
import ca.mcgill.ecse211.detectors.ColourDetector;
import ca.mcgill.ecse211.detectors.WeightDetector;
import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.navigators.Navigator;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerData;
import ca.mcgill.ecse211.sensors.LightDifferentialFilter;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;
import ca.mcgill.ecse211.strategies.CanSearch;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.remote.ev3.RMIRegulatedMotor;
import lejos.remote.ev3.RMISampleProvider;
import lejos.remote.ev3.RemoteEV3;

public class Main {
    private static final String      remoteIP               = "1.1.1.1";
    public static final double       TILE_SIZE              = 30.48;
    public static final double       WHEEL_RAD              = 2.2;
    public static final double       TRACK                  = 11.9;
    // distance from the light back light sensors to the wheel-base
    public static double             LT_SENSOR_TO_WHEELBASE = 11.9;
    // distance from the ultrasonic sensor to the "thumb" of the claw
    public static double             US_SENSOR_TO_CLAW      = 3.0;
    //median filter window width
    private static int 				MEDIAN_FILTER_WINDOW 	= 5;
    
    // the corner the robot will start in, downloaded via wifi
    private static int               startingCorner;
   
    //parameters sent through wifi
    public static int Red_LL_x;
    public static int Red_LL_y;
    public static int Red_UR_x;
    public static int Red_UR_y;
    public static int Green_LL_x;
    public static int Green_LL_y;
    public static int Green_UR_x;
    public static int Green_UR_y;
    public static int Island_LL_x;
    public static int Island_LL_y;
    public static int Island_UR_x;
    public static int Island_UR_y;
    
    public static int TNR_LL_x;
    public static int TNR_LL_y;
    public static int TNR_UR_x;
    public static int TNR_UR_y;
    
    public static int TNG_LL_x;
    public static int TNG_LL_y;
    public static int TNG_UR_x;
    public static int TNG_UR_y;
    
   public static int SZR_LL_x;
   public static int SZR_LL_y;
   public static int SRZ_UR_x;
   public static int SRZ_UR_y;
   
   public static int SZG_LL_x;
   public static int SZG_LL_y;
   public static int SZG_UR_x;
   public static int SZG_UR_y;
    
    
 
   
    
    
    private static RemoteEV3         remoteEv3;
    private static RMIRegulatedMotor elbowMotor;
    private static RMIRegulatedMotor clawMotor;
    private static RMISampleProvider touchSensor;
    private static ArmController armController;

    private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
    private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
    private static final TextLCD lcd = LocalEV3.get().getTextLCD();
    
    //needed to set up the sensors
    private static Port USPort;
    private static EV3UltrasonicSensor UltrasonicSensor;
    private static SensorMode DistanceProvider;
    private static float[] USSample;
    private static Port backLeftLSPort;
    private static EV3ColorSensor backLeftLS;
    private static SensorMode backLeftLSProvider;
    private static float[] backLeftLSSample;
    private static Port backRightLSPort;
    private static EV3ColorSensor backRightLS;
    private static SensorMode backRightLSProvider;
    private static float[] backRightLSSample;
    private static Port sideLSPort;
    private static EV3ColorSensor sideLS;
    private static SensorMode sideLSProvider;
    private static float[] sideLSSample;
    
    //class instances 
    
    private static MovementController movementController;
    private static Odometer odometer;
    private static LightDifferentialFilter leftLightDifferentialFilter;
    private static LightDifferentialFilter rightLightDifferentialFilter;
    private static MedianDistanceSensor medianDistanceSensor;
    private static Localization localization;
    private static Navigator navigator;
    private static CanSearch canSearch;
    private static WeightDetector weightDetector;
    
    
    
    public static void main(String[] args) {
        // init remote ev3
        while (remoteEv3 == null) {
            try {
                remoteEv3 = new RemoteEV3(remoteIP);
            } catch (RemoteException | MalformedURLException | NotBoundException e) {
                System.out.println("Could not connect to other EV3");
                continue;
            }
        }
        // init remote motors
        elbowMotor = remoteEv3.createRegulatedMotor("A", 'N');// L = EV3LargeRegulatedMotor, N = NXTRegulatedMotor
        clawMotor = remoteEv3.createRegulatedMotor("B", 'N');
        // init remote touch-sensor sampler
        touchSensor = remoteEv3.createSampleProvider("S1", "lejos.hardware.sensor.EV3TouchSensor", "Touch");
        // init arm controller
        armController = new ArmController(new Claw(clawMotor), new Elbow(elbowMotor));
        
        // set up side ultrasonic sensor
        USPort = LocalEV3.get().getPort("S2");
        UltrasonicSensor = new EV3UltrasonicSensor(USPort);
        DistanceProvider = UltrasonicSensor.getMode("Distance");
        USSample = new float[DistanceProvider.sampleSize()];
        
        // set up back-left light sensor
        backLeftLSPort = LocalEV3.get().getPort("S1");
        backLeftLS = new EV3ColorSensor(backLeftLSPort);
        backLeftLSProvider = backLeftLS.getMode("Red");
        backLeftLSSample = new float[backLeftLSProvider.sampleSize()];

        // set up back-right light sensor
        backRightLSPort = LocalEV3.get().getPort("S4");
        backRightLS = new EV3ColorSensor(backRightLSPort);
        backRightLSProvider = backRightLS.getMode("Red");
        backRightLSSample = new float[backRightLSProvider.sampleSize()];
        
        //set up side light sensor
        sideLSPort = LocalEV3.get().getPort("S3");
        sideLS = new EV3ColorSensor(sideLSPort);
        sideLSProvider = sideLS.getMode("RGB");
        sideLSSample = new float[sideLSProvider.sampleSize()];
        
        //starts odometer
        odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
        Thread odoThread = new Thread(odometer);
        odoThread.start();
        
        //set up of all the class instances
        
        
        leftLightDifferentialFilter = new LightDifferentialFilter(backLeftLSProvider, backLeftLSSample);
        rightLightDifferentialFilter = new LightDifferentialFilter(backRightLSProvider, backRightLSSample);
        medianDistanceSensor = new MedianDistanceSensor(DistanceProvider, USSample, odometer, MEDIAN_FILTER_WINDOW);
        localization = new Localization(movementController, odometer, medianDistanceSensor, leftLightDifferentialFilter, rightLightDifferentialFilter, startingCorner);
        movementController = new MovementController(leftMotor, rightMotor, WHEEL_RAD, TRACK, odometer);
        navigator = new Navigator(movementController, odometer, localization, TLL, TUR, STZLL, STZUR, startingCorner, ILL, IUR);
        canSearch = new CanSearch(odometer, movementController, medianDistanceSensor, PLL, PUR, PTUNEL, PISLAND_LL, PISLAND_UR, startingCorner, TILE_SIZE);
        weightDetector = new WeightDetector(touchSensor);
        
        
        
        
        
        

        // close remote motors before the program ends
        try {
            elbowMotor.close();
            clawMotor.close();
        } catch (RemoteException e) {
            System.out.println("Could not close remote ev3's motor ports");
        }

    }
}
