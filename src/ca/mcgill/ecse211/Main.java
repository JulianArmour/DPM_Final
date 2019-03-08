package ca.mcgill.ecse211;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import ca.mcgill.ecse211.arms.ArmController;
import ca.mcgill.ecse211.arms.Claw;
import ca.mcgill.ecse211.arms.Elbow;
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
    // the corner the robot will start in, downloaded via wifi
    private static int               startingCorner;
    private static RemoteEV3         remoteEv3;
    private static RMIRegulatedMotor elbowMotor;
    private static RMIRegulatedMotor clawMotor;
    private static RMISampleProvider touchSensor;
    private static ArmController armController;

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
        
        

        // close remote motors before the program ends
        try {
            elbowMotor.close();
            clawMotor.close();
        } catch (RemoteException e) {
            System.out.println("Could not close remote ev3's motor ports");
        }

    }
}
