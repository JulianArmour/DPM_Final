package ca.mcgill.ecse211.tests.software;

import ca.mcgill.ecse211.detectors.WeightDetector;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.NXTRegulatedMotor;

public class GrabCanTest {

    public static final double            WHEEL_RAD   = 2.2;
    public static final double            TRACK       = 17.3;
    public static final int               SC          = 2;
    private static final float            TILE_LENGTH = 30.48f;

    private static Odometer               odometer;
    private static EV3LargeRegulatedMotor leftMotor;
    private static EV3LargeRegulatedMotor rightMotor;
    private static MovementController     movementController;
    private static LocalEV3               localEV3;
    private static NXTRegulatedMotor      elbowMotor;
    private static EV3LargeRegulatedMotor clawMotor;
    private static WeightDetector         weightDetector;

    public static void main(String[] args) throws InterruptedException {

        // set up wheel motors
        leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
        rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
        // set up arm motors
      //  elbowMotor = new NXTRegulatedMotor(LocalEV3.get().getPort("C"));
      //  elbowMotor.resetTachoCount();
        clawMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
        clawMotor.resetTachoCount();
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
        localEV3 = (LocalEV3) LocalEV3.get();
        weightDetector = new WeightDetector(clawMotor, movementController, TILE_LENGTH);

        // start test
        localEV3.getTextLCD().clear();

        while (true) {
            Button.waitForAnyPress();
            Thread.sleep(2000);
            if (weightDetector.canIsHeavy()) {
                Sound.twoBeeps();
            } else {
                Sound.beep();
            }
        }
    }
}
