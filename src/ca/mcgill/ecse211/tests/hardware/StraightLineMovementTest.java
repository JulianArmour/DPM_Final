package ca.mcgill.ecse211.tests.hardware;

import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class StraightLineMovementTest {

    public static final double            WHEEL_RAD   = 2.07;
    public static final double            TRACK_CW    = Main.TRACK_CW;
    public static final double            TRACK_CCW   = Main.TRACK_CCW;
    public static final double            TILE_LENGTH = Main.TILE_SIZE;

    private static Odometer               odometer;
    private static EV3LargeRegulatedMotor leftMotor;
    private static EV3LargeRegulatedMotor rightMotor;
    private static MovementController     movementController;

    public static void main(String[] args) {

        // set up wheel motors
        leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
        rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
        // starts odometer
        try {
            odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK_CW, WHEEL_RAD);
        } catch (OdometerExceptions e) {
            System.out.println("Could not get odometer.");
        }
        Thread odoThread = new Thread(odometer);
        odoThread.start();
        // initialize instances
        movementController = new MovementController(leftMotor, rightMotor, WHEEL_RAD, TRACK_CW, TRACK_CCW, odometer);

        movementController.driveDistance(TILE_LENGTH * 10);
        System.exit(0);

    }
}
