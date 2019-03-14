package ca.mcgill.ecse211.strategies;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;

/**
 * This class contains the search algorithm used to find the cans in the search
 * zone and approach them in such a way that it is easy to grab onto them.
 * 
 * 
 */
public class CanSearch {

    protected static final long  CAN_SCAN_PERIOD = 100;
    private Odometer             odo;
    private MovementController   movCon;
    private MedianDistanceSensor USData;
    private float[]              P_SZ_LL, P_SZ_UR, P_TUN_LL, P_TUN_UR, P_ISL_LL, P_ISL_UR;
    private int                  startCorner;
    private float                TILE_LENGTH;
    private float                deltaX, deltaY;
    private float                SCAN_RADIUS     = TILE_LENGTH * 3;
    private float[]              nextPos;
    private LinkedList<float[]>  scanningPoints  = new LinkedList<float[]>();             // TODO implement as queue or
                                                                                          // stack

    /**
     * 
     * @param odometer
     * @param movementController
     * @param USData
     * @param searchzone_LL
     * @param searchzone_UR
     * @param tunnel_LL
     * @param ISLAND_LL
     * @param ISLAND_UR
     * @param startingCorner
     * @param TILE_LENGTH
     */
    public CanSearch(Odometer odometer, MovementController movementController, MedianDistanceSensor USData,
            int[] searchzone_LL, int[] searchzone_UR, int[] tunnel_LL, int[] tunnel_UR, int[] ISLAND_LL,
            int[] ISLAND_UR, int startingCorner, double tileLength) {

        this.odo = odometer;
        this.movCon = movementController;
        this.USData = USData;
        this.P_SZ_LL = new float[] { (float) (searchzone_LL[0] * tileLength), (float) (searchzone_LL[1] * tileLength) };
        this.P_SZ_UR = new float[] { (float) (searchzone_UR[0] * tileLength), (float) (searchzone_UR[1] * tileLength) };
        this.P_TUN_LL = new float[] { (float) (tunnel_LL[0] * tileLength), (float) (tunnel_LL[1] * tileLength) };
        this.P_TUN_UR = new float[] { (float) (tunnel_UR[0] * tileLength), (float) (tunnel_UR[1] * tileLength) };
        this.P_ISL_LL = new float[] { (float) (ISLAND_LL[0] * tileLength), (float) (ISLAND_LL[1] * tileLength) };
        this.P_ISL_UR = new float[] { (float) (ISLAND_UR[0] * tileLength), (float) (ISLAND_UR[1] * tileLength) };
        this.startCorner = startingCorner;
        this.TILE_LENGTH = (float) tileLength;
    }

    /**
     * Sets the scan locations depending on the starting corner, the tunnel
     * position, the LL and the UR
     * 
     * @author Alice Kazarine
     */
    public void setScanPositions() {
        // TODO add all possibilities ST == 0,1,2,3

        deltaX = P_ISL_UR[0] - P_SZ_LL[0];
        deltaY = P_ISL_UR[1] - P_ISL_LL[1];

        if (startCorner == 1 || startCorner == 2) {

            // calculate positions as float[]
            float[] firstPos = { P_ISL_UR[0] - TILE_LENGTH / 2, P_ISL_LL[1] + TILE_LENGTH / 2 };
            scanningPoints.add(firstPos);

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    float[] nextPos = new float[2];

                    if (i == 0 && j == 0) {
                        System.out.println("BOTH 0");
                        nextPos[0] = P_ISL_UR[0] - TILE_LENGTH / 2;
                        nextPos[1] = P_ISL_LL[1] + TILE_LENGTH / 2;
                    }

                    else if (i == 0) {
                        System.out.println("i is 0");
                        nextPos[0] = P_ISL_UR[0] - TILE_LENGTH / 2;
                        nextPos[1] = P_ISL_LL[1] + j * (deltaY / SCAN_RADIUS) * TILE_LENGTH;
                    }

                    else if (j == 0) {
                        System.out.println("j is 0");
                        nextPos[1] = P_ISL_LL[1] + TILE_LENGTH / 2;
                        nextPos[0] = P_ISL_UR[0] - i * (deltaX / SCAN_RADIUS) * TILE_LENGTH;

                    }

                    else {
                        System.out.println("none of them are 0");
                        nextPos[0] = P_ISL_UR[0] - i * (deltaX / SCAN_RADIUS) * TILE_LENGTH;

                        nextPos[1] = P_ISL_LL[1] + j * (deltaY / SCAN_RADIUS) * TILE_LENGTH;
                    }
                    scanningPoints.add(nextPos);
                }
            }
        }

        if (startCorner == 3 || startCorner == 0) {

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    float[] nextPos = new float[2];

                    if (i == 0 && j == 0) {

                        nextPos[0] = P_ISL_LL[0] + TILE_LENGTH / 2;
                        nextPos[1] = P_ISL_LL[1] + TILE_LENGTH / 2;
                    }
                    if (i == 0) {

                        nextPos[0] = P_ISL_LL[0] + TILE_LENGTH / 2;
                        nextPos[1] = P_ISL_LL[1] + j * (deltaY / SCAN_RADIUS) * TILE_LENGTH;
                    }
                    if (j == 0) {

                        nextPos[1] = P_ISL_LL[1] + TILE_LENGTH / 2;
                        nextPos[0] = P_ISL_LL[0] + i * (deltaX / SCAN_RADIUS) * TILE_LENGTH;
                    } else {

                        nextPos[0] = P_ISL_LL[0] + i * (deltaX / SCAN_RADIUS) * TILE_LENGTH;
                        nextPos[1] = P_ISL_LL[1] + j * (deltaY / SCAN_RADIUS) * TILE_LENGTH;
                    }

                    scanningPoints.add(nextPos);

                }
            }

        }
    }

    /**
     * Goes to current scan position and scans to detect a can
     * 
     * @author Alice Kazarine
     */
    public void scanCurrentZone() {
        int currentPos = 0;

        if (startCorner == 1) {

            movCon.travelTo(scanningPoints.get(currentPos)[0], scanningPoints.get(currentPos)[1], false);
            movCon.turnTo(90);
            float[] canPos = fastCanScan(P_SZ_LL, P_SZ_UR);

            if (canPos != null) {
                travelToCan(canPos);
            } else {
                currentPos += 1;
                scanCurrentZone();

            }
            // TODO what do you do after you get to the can?
        }

        // TODO finish this
    }

    /**
     * Causes the robot to travel to the general location of a detected can.
     * 
     * @param canPos
     *            the general position of a can, which the robot will travel to.
     * @author Julian Armour
     * @since March 5, 2019
     */
    public void travelToCan(float[] canPos) {
        double[] robotPos = odo.getXYT();
        // travel robot 10 cm in front of can
        movCon.turnTo(movCon.calculateAngle(robotPos[0], robotPos[1], canPos[0], canPos[1]));
        movCon.driveDistance(movCon.calculateDistance(robotPos[0], robotPos[1], canPos[0], canPos[1]) - 10, false);
        // rotate counter-clockwise 45 degrees
        movCon.rotateAngle(45, false, false);
        // rotate clockwise until the can is detected
        USData.flush();
        movCon.rotateAngle(45, true, true);
        // check for cans
        while (USData.getFilteredDistance() >= 12) {
            try {
                Thread.sleep(CAN_SCAN_PERIOD / 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        movCon.stopMotors();
        // rotate back 15 degrees to account for ultrasonic arc
        movCon.rotateAngle(15, true, false);
        // move forward until to appropriate distance for gripping the can
        USData.flush();
        float dist = USData.getFilteredDistance();
        if (dist < TILE_LENGTH) {
            movCon.driveDistance(dist - Main.US_SENSOR_TO_CLAW, false);
        }
    }

    /**
     * Scans for cans, if it finds one it returns it's position. If it does not find
     * a can, it returns <code>null</code>
     * 
     * @param searchLL
     *            lower left of the search zone
     * @param searchUR
     *            upper right of the search zone
     * @return the position of a can in the scan radius, or <code>null</code> if a
     *         can is not found
     */
    public float[] fastCanScan(float[] searchLL, float[] searchUR) {
        double[] robotPos = odo.getXYT();
        // start rotating clockwise
        // scan for positions that are within the search zone
        final double finalHeading = (robotPos[2] - 1.0) % 360.0;

        final boolean[] atFinalHeading = { false }; // anonymous class trick for outer-scope variables
        Runnable rotater = new Runnable() {
            @Override
            public void run() {
                movCon.turnClockwiseTo(finalHeading, false);
                if (!Thread.interrupted()) {
                    atFinalHeading[0] = true;
                }
            }
        };

        USData.flush();
        Thread rotT = new Thread(rotater);
        rotT.start(); // start rotating
        float[] position = new float[2];
        while (!atFinalHeading[0]) {
            double dist = (double) USData.getFilteredDistance();
            if (dist <= SCAN_RADIUS) {
                double angle = odo.getXYT()[2];
                position[0] = (float) (dist * Math.sin(Math.toRadians(angle)) + robotPos[0]);
                position[1] = (float) (dist * Math.cos(Math.toRadians(angle)) + robotPos[1]);
                if (inSearchZone(position, searchLL, searchUR)) {
                    rotT.interrupt();
                    movCon.stopMotors();

                    // more checks to see if it is really a can
                    double meanDist = 0;
                    for (int i = 0; i < 10; i++) {
                        meanDist += (double) USData.getFilteredDistance();
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    meanDist /= 10;
                    angle = odo.getXYT()[2];
                    position[0] = (float) (meanDist * Math.sin(Math.toRadians(angle)) + robotPos[0]);
                    position[1] = (float) (meanDist * Math.cos(Math.toRadians(angle)) + robotPos[1]);
                    if (inSearchZone(position, searchLL, searchUR)) {
                        // true positive, return
                        return position;
                    } else {
                        // false positive, keep scanning
                        rotT = new Thread(rotater);
                        rotT.start(); // start rotating again
                    }
                }
            }

            try {
                Thread.sleep(CAN_SCAN_PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Scans for cans by rotating 360-degrees and returns all potential locations of
     * cans.
     * 
     * @param searchLL
     * @param searchUR
     * @return a list containing detected positions of cans
     * @author Julian Armour
     */
    public List<float[]> canScan(float[] searchLL, float[] searchUR) {
        double[] robotPos = odo.getXYT();

        final List<float[]> angleDistData = new LinkedList<float[]>();
        // anonymous class for polling distance data while the robot rotates
        Thread distancePoller = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    float angle = (float) odo.getXYT()[2];
                    float dist = USData.getFilteredDistance();
                    if (dist <= SCAN_RADIUS) {
                        angleDistData.add(new float[] { angle, dist });
                    }

                    try {
                        Thread.sleep(CAN_SCAN_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        };

        USData.flush();
        distancePoller.start();
        movCon.rotateAngle(360, true, false);
        distancePoller.interrupt();

        List<float[]> positionData = new LinkedList<float[]>();
        Iterator<float[]> it = angleDistData.iterator();
        while (it.hasNext()) {
            float[] dat = (float[]) it.next();
            float[] position = new float[] { (float) (dat[1] * Math.sin(Math.toRadians(dat[0])) + robotPos[0]),
                    (float) (dat[1] * Math.cos(Math.toRadians(dat[0])) + robotPos[1]) };

            if (inSearchZone(position, searchLL, searchUR)) {
                positionData.add(position);
            }
        }

        return positionData;
    }

    /**
     * @param position
     *            the position being checked for
     * @param searchLL
     *            the lower left of a zone
     * @param searchUR
     *            the upper right of a zone
     * @return true if the position is in the zone, false if it's not.
     * 
     * @author Julian Armour
     */
    private boolean inSearchZone(float[] position, float[] searchLL, float[] searchUR) {
        if (position[0] > searchUR[0] || position[0] < searchLL[0]) {
            return false;
        } else if (position[1] > searchUR[1] || position[1] < searchLL[1]) {
            return false;
        } else {
            return true;
        }
    }
}
