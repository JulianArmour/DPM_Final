package ca.mcgill.ecse211;

import java.util.Map;

import ca.mcgill.ecse211.WiFiClient.WifiConnection;
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
import ca.mcgill.ecse211.strategies.Beeper;
import ca.mcgill.ecse211.strategies.CanSearch;
import ca.mcgill.ecse211.strategies.TimeTracker;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;

public class Main {
    private static final String            SERVER_IP               = "192.168.2.8"; // for beta and competition
//    private static final String            SERVER_IP               = "192.168.43.112"; // for personal testing

    private static final int               TEAM_NUMBER             = 3;

    // Enable/disable printing of debug info from the WiFi class
    private static final boolean           ENABLE_DEBUG_WIFI_PRINT = true;

    public static final float              TILE_SIZE               = 30.48f;
    public static final double             WHEEL_RAD               = 2.07;
    public static final double             TRACK                   = 8.98;
    // distance from the light back light sensors to the wheel-base
    public static double                   LT_SENSOR_TO_WHEELBASE  = 9.2;
    // distance from the ultrasonic sensor to the "thumb" of the claw
    public static double                   US_SENSOR_TO_CLAW       = 1.0;
    // median filter window width
    private static int                     MEDIAN_FILTER_WINDOW    = 5;

    // parameters sent through wifi:

    // starting zone coordinates
    private static int[]                   startzone_LL;
    private static int[]                   startzone_UR;

    // island coordinates
    private static int[]                   island_LL;
    private static int[]                   island_UR;

    // tunnel coordinates
    private static int[]                   tunnel_LL;
    private static int[]                   tunnel_UR;

    // search zone coordinates
    private static int[]                   searchzone_LL;
    private static int[]                   searchzone_UR;

    // the corner the robot will start in
    private static int                     startingCorner;

    // the can colour being searched for
    private static CanColour               canColour;

    private static EV3MediumRegulatedMotor colourMotor;
    private static EV3LargeRegulatedMotor  clawMotor;
    private static EV3LargeRegulatedMotor  leftMotor;
    private static EV3LargeRegulatedMotor  rightMotor;

    private static final TextLCD           lcd                     = LocalEV3.get().getTextLCD();

    // needed to set up the sensors
    private static Port                    USPort;
    private static EV3UltrasonicSensor     UltrasonicSensor;
    private static SensorMode              DistanceProvider;
    private static float[]                 USSample;
    private static Port                    backLeftLSPort;
    private static EV3ColorSensor          backLeftLS;
    private static SensorMode              backLeftLSProvider;
    private static float[]                 backLeftLSSample;
    private static Port                    backRightLSPort;
    private static EV3ColorSensor          backRightLS;
    private static SensorMode              backRightLSProvider;
    private static float[]                 backRightLSSample;
    private static Port                    sideLSPort;
    private static EV3ColorSensor          canColourSensor;
    private static SensorMode              canRGBProvider;
    private static float[]                 canRGBBuffer;

    // class instances
    private static MovementController      movementController;
    private static Odometer                odometer;
    private static LightDifferentialFilter leftLightDifferentialFilter;
    private static LightDifferentialFilter rightLightDifferentialFilter;
    private static MedianDistanceSensor    medianDistanceSensor;
    private static Localization            localizer;
    private static Navigator               navigator;
    private static CanSearch               canSearch;
    private static WeightDetector          weightDetector;
    private static Claw                    claw;
    private static ColourArm               colourArm;
    private static ColourDetector          colourDetector;
    private static TimeTracker timeTracker;

    public static void main(String[] args) {
        // init motors
        leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
        rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
        colourMotor = new EV3MediumRegulatedMotor(LocalEV3.get().getPort("B"));
        clawMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

        // set up side ultrasonic sensor
        USPort = LocalEV3.get().getPort("S2");
        UltrasonicSensor = new EV3UltrasonicSensor(USPort);
        DistanceProvider = UltrasonicSensor.getMode("Distance");
        USSample = new float[DistanceProvider.sampleSize()];

        // set up back-left light sensor
        backLeftLSPort = LocalEV3.get().getPort("S3");
        backLeftLS = new EV3ColorSensor(backLeftLSPort);
        backLeftLSProvider = backLeftLS.getMode("Red");
        backLeftLSSample = new float[backLeftLSProvider.sampleSize()];

        // set up back-right light sensor
        backRightLSPort = LocalEV3.get().getPort("S4");
        backRightLS = new EV3ColorSensor(backRightLSPort);
        backRightLSProvider = backRightLS.getMode("Red");
        backRightLSSample = new float[backRightLSProvider.sampleSize()];

        // set up colour light sensor
        sideLSPort = LocalEV3.get().getPort("S1");
        canColourSensor = new EV3ColorSensor(sideLSPort);
        canRGBProvider = canColourSensor.getMode("RGB");
        canRGBBuffer = new float[canRGBProvider.sampleSize()];

        // starts odometer
        try {
            odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK, WHEEL_RAD);
        } catch (OdometerExceptions e) {
            System.out.println("Could not setup odometer.");
        }
        Thread odoThread = new Thread(odometer);
        odoThread.start();
        

        // set up of all the class instances
        leftLightDifferentialFilter = new LightDifferentialFilter(backLeftLSProvider, backLeftLSSample);
        rightLightDifferentialFilter = new LightDifferentialFilter(backRightLSProvider, backRightLSSample);
        
        medianDistanceSensor = new MedianDistanceSensor(DistanceProvider, USSample, odometer, MEDIAN_FILTER_WINDOW);
        
        movementController = new MovementController(leftMotor, rightMotor, WHEEL_RAD, TRACK, odometer);
        
        timeTracker = new TimeTracker(45, 300);// when 45 seconds are remaining, go to searchZone_UR
        
        localizer = new Localization(
                movementController, odometer, medianDistanceSensor, leftLightDifferentialFilter,
                rightLightDifferentialFilter, startingCorner
        );
        
        
        // At this point we need wifi data
        getWifiData();
        
        // start the time tracker
        timeTracker.start();
        
        navigator = new Navigator(
                movementController, odometer, localizer, tunnel_LL, tunnel_UR, startzone_LL, startzone_UR,
                startingCorner, island_LL, island_UR, searchzone_LL, searchzone_UR, TILE_SIZE
        );
        colourArm = new ColourArm(colourMotor);
        claw = new Claw(clawMotor);
        colourDetector = new ColourDetector(colourArm, canRGBProvider);
        
        canSearch = new CanSearch(
                odometer, movementController, navigator, medianDistanceSensor, claw, weightDetector, colourDetector,
                localizer, timeTracker, canColour, searchzone_LL, searchzone_UR, tunnel_LL, tunnel_UR, island_LL, island_UR,
                startingCorner, (float) (2 * TILE_SIZE), TILE_SIZE
        );
        weightDetector = new WeightDetector(clawMotor, movementController, TILE_SIZE);
        
        run();
    }
    
    public static void run() {

        // clear the screen
        lcd.clear();
        // set the scan positions for the search zone
        canSearch.setScanPositions();
        // next, localize
        localizer.initialUSLocalization();
        localizer.initialLightLocalization();
        Beeper.localized();
        // go to the tunnel
        claw.closeClaw();
        navigator.travelToTunnel(true);
        // travel through the tunnel
        navigator.travelThroughTunnel(true);
        // travel to search zone LL
        navigator.travelToSearchZoneLL();
        Beeper.arrivedAtSearchLL();
        // start scanning for cans
        canSearch.scanZones();
        // FYI: travel to searchzone_UR is incorporated in scanZones() !
        System.exit(0);
    }

    
    @SuppressWarnings("rawtypes")
    private static void getWifiData() {
        // Initialize WifiConnection class
        WifiConnection conn = new WifiConnection(SERVER_IP, TEAM_NUMBER, ENABLE_DEBUG_WIFI_PRINT);

        // Connect to server and get the data, catching any errors that might occur
        try {
            Map data = conn.getData();
            
            // set starting corner and can colour being searched for
            int redTeam = ((Long) data.get("RedTeam")).intValue();
            int island_LL_x, island_LL_y, island_UR_x, island_UR_y;
            int tunnel_LL_x, tunnel_LL_y, tunnel_UR_x, tunnel_UR_y;
            int searchzone_LL_y, searchzone_LL_x, searchzone_UR_x, searchzone_UR_y;
            int startzone_LL_x, startzone_LL_y, startzone_UR_x, startzone_UR_y;

            redTeam = 3;//TODO Set for demo, remove later!
            if (redTeam == TEAM_NUMBER) {
                startingCorner = ((Long) data.get("RedCorner")).intValue();
//                canColour = CanColour.RED;
                //TODO this switch is to be changed after the beta demo
                switch (((Long) data.get("GreenTeam")).intValue()) {
                    case 1:
                        canColour = CanColour.BLUE;
                        break;
                    case 2:
                        canColour = CanColour.GREEN;
                        break;
                    case 3:
                        canColour = CanColour.YELLOW;
                        break;
                    case 4:
                        canColour = CanColour.RED;
                        break;
                }
                canColour = CanColour.BLUE;
                island_LL_x = ((Long) data.get("Island_LL_x")).intValue();
                island_LL_y = ((Long) data.get("Island_LL_y")).intValue();
                island_UR_x = ((Long) data.get("Island_UR_x")).intValue();
                island_UR_y = ((Long) data.get("Island_UR_y")).intValue();

                tunnel_LL_x = ((Long) data.get("TNR_LL_x")).intValue();
                tunnel_LL_y = ((Long) data.get("TNR_LL_y")).intValue();
                tunnel_UR_x = ((Long) data.get("TNR_UR_x")).intValue();
                tunnel_UR_y = ((Long) data.get("TNR_UR_y")).intValue();

                searchzone_LL_x = ((Long) data.get("SZR_LL_x")).intValue();
                searchzone_LL_y = ((Long) data.get("SZR_LL_y")).intValue();
                searchzone_UR_x = ((Long) data.get("SZR_UR_x")).intValue();
                searchzone_UR_y = ((Long) data.get("SZR_UR_y")).intValue();

                startzone_LL_x = ((Long) data.get("Red_LL_x")).intValue();
                startzone_LL_y = ((Long) data.get("Red_LL_y")).intValue();
                startzone_UR_x = ((Long) data.get("Red_UR_x")).intValue();
                startzone_UR_y = ((Long) data.get("Red_UR_y")).intValue();
            } else {
                startingCorner = ((Long) data.get("GreenCorner")).intValue();
                canColour = CanColour.GREEN;
                island_LL_x = ((Long) data.get("Island_LL_x")).intValue();
                island_LL_y = ((Long) data.get("Island_LL_y")).intValue();
                island_UR_x = ((Long) data.get("Island_UR_x")).intValue();
                island_UR_y = ((Long) data.get("Island_UR_y")).intValue();

                tunnel_LL_x = ((Long) data.get("TNG_LL_x")).intValue();
                tunnel_LL_y = ((Long) data.get("TNG_LL_y")).intValue();
                tunnel_UR_x = ((Long) data.get("TNG_UR_x")).intValue();
                tunnel_UR_y = ((Long) data.get("TNG_UR_y")).intValue();

                searchzone_LL_x = ((Long) data.get("SZG_LL_x")).intValue();
                searchzone_LL_y = ((Long) data.get("SZG_LL_y")).intValue();
                searchzone_UR_x = ((Long) data.get("SZG_UR_x")).intValue();
                searchzone_UR_y = ((Long) data.get("SZG_UR_y")).intValue();

                startzone_LL_x = ((Long) data.get("Red_LL_x")).intValue();
                startzone_LL_y = ((Long) data.get("Red_LL_y")).intValue();
                startzone_UR_x = ((Long) data.get("Red_UR_x")).intValue();
                startzone_UR_y = ((Long) data.get("Red_UR_y")).intValue();
            }
            island_LL = new int[] { island_LL_x, island_LL_y };
            island_UR = new int[] { island_UR_x, island_UR_y };

            tunnel_LL = new int[] { tunnel_LL_x, tunnel_LL_y };
            tunnel_UR = new int[] { tunnel_UR_x, tunnel_UR_y };

            searchzone_LL = new int[] { searchzone_LL_x, searchzone_LL_y };
            searchzone_UR = new int[] { searchzone_UR_x, searchzone_UR_y };

            startzone_LL = new int[] { startzone_LL_x, startzone_LL_y };
            startzone_UR = new int[] { startzone_UR_x, startzone_UR_y };
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        
        
    }
    
}
