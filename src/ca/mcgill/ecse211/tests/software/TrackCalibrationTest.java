package ca.mcgill.ecse211.tests.software;

import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.odometer.OdometerExceptions;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class TrackCalibrationTest {
    public static final float             TILE_SIZE              = 30.48f;
    public static final double            WHEEL_RAD              = 2.07;
    public static double                  TRACK_CW               = 8.815;
    public static double                  TRACK_CCW              = 8.89;
    // distance from the light back light sensors to the wheel-base
    public static double                  LT_SENSOR_TO_WHEELBASE = 9.2;
    // distance from the ultrasonic sensor to the "thumb" of the claw
    public static double                  US_SENSOR_TO_CLAW      = 1.0;
    private static EV3LargeRegulatedMotor leftMotor;
    private static EV3LargeRegulatedMotor rightMotor;

    private static final TextLCD          lcd                    = LocalEV3.get().getTextLCD();

    // class instances
    private static MovementController     movementController;
    private static Odometer               odometer;

    public static void main(String[] args) {
        // init motors
        leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
        rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
        // starts odometer
        try {
            odometer = Odometer.getOdometer(leftMotor, rightMotor, TRACK_CW, WHEEL_RAD);
        } catch (OdometerExceptions e) {
            System.out.println("Could not setup odometer.");
        }
        Thread odoThread = new Thread(odometer);
        odoThread.start();

        movementController = new MovementController(leftMotor, rightMotor, WHEEL_RAD, TRACK_CW, TRACK_CCW, odometer);

        run();
    }

    /**
     * Top-level driver for the competition.
     */
    private static void run() {
        // clear the screen
        lcd.clear();
        boolean calibratingLeftTrack = true;
        while (true) {
            if (calibratingLeftTrack) {
                System.out.println("Calibrating left track");
            } else {
                System.out.println("Calibrating right track");
            }
            switch (Button.waitForAnyPress()) {
            case Button.ID_LEFT:
                odometer.setTheta(0);
                movementController.rotateAngle(720, false);
                System.out.println("TRACK_CCW = " + TRACK_CCW);
                System.out.println("ODO: " + odometer.getXYT()[2]);
                break;
            case Button.ID_RIGHT:
                odometer.setTheta(0);
                movementController.rotateAngle(720, true);
                System.out.println("TRACK_CW = " + TRACK_CW);
                System.out.println("ODO: " + odometer.getXYT()[2]);
                break;
            case Button.ID_DOWN:
                if (calibratingLeftTrack) {
                    TRACK_CCW -= 0.01;
                    System.out.println("TRACK_CCW = " + TRACK_CCW);
                } else {
                    TRACK_CW -= 0.01;
                    System.out.println("TRACK_CW = " + TRACK_CW);
                }
                movementController = new MovementController(
                        leftMotor, rightMotor, WHEEL_RAD, TRACK_CW, TRACK_CCW, odometer
                );
                break;
            case Button.ID_UP:
                if (calibratingLeftTrack) {
                    TRACK_CCW += 0.01;
                    System.out.println("TRACK_CCW = " + TRACK_CCW);
                } else {
                    TRACK_CW += 0.01;
                    System.out.println("TRACK_CW = " + TRACK_CW);
                }
                movementController = new MovementController(
                        leftMotor, rightMotor, WHEEL_RAD, TRACK_CW, TRACK_CCW, odometer
                );
                break;
            case Button.ID_ENTER:
                calibratingLeftTrack = !calibratingLeftTrack;
                break;
            default:
                break;
            }
        }
    }
}
