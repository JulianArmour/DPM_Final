package ca.mcgill.ecse211.strategies;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.arms.Claw;
import ca.mcgill.ecse211.detectors.CanColour;
import ca.mcgill.ecse211.detectors.ColourDetector;
import ca.mcgill.ecse211.detectors.WeightDetector;
import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.navigators.Navigator;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;
import lejos.utility.Delay;

/**
 * This class contains the search algorithm used to find the cans in the search
 * zone and approach them in such a way that it is easy to grab onto them.
 * 
 * @author Julian Armour, Alice Kazarine
 * @since March 5, 2019
 * @version 2
 */
public class CanSearch {

    private static final long    CAN_SCAN_PERIOD = 30;
    private Odometer             odo;
    private MovementController   movCon;
    private Navigator            navigator;
    private MedianDistanceSensor USData;
    private Claw                 claw;
    private WeightDetector       weightDetector;
    private ColourDetector       colourDetector;
    private Localization         localizer;
    private TimeTracker          timeTracker;
    private int[]                SZ_LL, SZ_UR;
    private float[]              P_TUN_LL, P_TUN_UR, P_ISL_LL, P_ISL_UR;
    private int                  startCorner;
    private float                TILE_LENGTH;
    private float                SCAN_RADIUS;
    private List<float[]>        scanningPoints  = new LinkedList<float[]>();
    private List<float[]>        dumpingPoints   = new LinkedList<float[]>();
    private float[]              P_SZ_LL;
    private float[]              P_SZ_UR;
    private int                  currentScanPoint;
    private CanColour            searchCanColour;

    /**
     * 
     * @param odometer
     *            the {@link Odometer}
     * @param movementController
     *            the {@link MovementController}
     * @param navigator
     *            the {@link Navigator}
     * @param USData
     *            the {@link MedianDistanceSensor}
     * @param claw
     *            the {@link Claw}
     * @param weightDetector
     *            the {@link WeightDetector}
     * @param colourDetector
     *            the {@link ColourDetector}
     * @param searchCanColour
     *            the {@link CanColour} to search for
     * @param searchzone_LL
     *            the coordinate position of the searchzone's lower left
     * @param searchzone_UR
     *            the coordinate position of the searchzone's upper right
     * @param tunnel_LL
     *            the coordinate position of the tunnel's lower left
     * @param tunnel_UR
     *            the coordinate position of the tunnel's upper right
     * @param ISLAND_LL
     *            the coordinate position of the island's lower left
     * @param ISLAND_UR
     *            the coordinate position of the island's upper right
     * @param startingCorner
     *            the starting corner ID
     * @param scanRadius
     *            the maximum distance (in # of tiles) for detecting a can when
     *            scanning
     * @param tileLength
     *            the length of a tile (in cm)
     */
    public CanSearch(
            Odometer odometer, MovementController movementController, Navigator navigator, MedianDistanceSensor USData,
            Claw claw, WeightDetector weightDetector, ColourDetector colourDetector, Localization localizer,
            TimeTracker timeTracker, CanColour searchCanColour, int[] searchzone_LL, int[] searchzone_UR,
            int[] tunnel_LL, int[] tunnel_UR, int[] ISLAND_LL, int[] ISLAND_UR, int startingCorner, float scanRadius,
            double tileLength
    ) {
        this.odo = odometer;
        this.movCon = movementController;
        this.navigator = navigator;
        this.USData = USData;
        this.claw = claw;
        this.weightDetector = weightDetector;
        this.colourDetector = colourDetector;
        this.localizer = localizer;
        this.timeTracker = timeTracker;
        this.SZ_LL = searchzone_LL;
        this.SZ_UR = searchzone_UR;
        this.P_SZ_LL = new float[] { (float) (SZ_LL[0] * tileLength), (float) (SZ_LL[1] * tileLength) };
        this.P_SZ_UR = new float[] { (float) (SZ_UR[0] * tileLength), (float) (SZ_UR[1] * tileLength) };
        this.P_TUN_LL = new float[] { (float) (tunnel_LL[0] * tileLength), (float) (tunnel_LL[1] * tileLength) };
        this.P_TUN_UR = new float[] { (float) (tunnel_UR[0] * tileLength), (float) (tunnel_UR[1] * tileLength) };
        this.P_ISL_LL = new float[] { (float) (ISLAND_LL[0] * tileLength), (float) (ISLAND_LL[1] * tileLength) };
        this.P_ISL_UR = new float[] { (float) (ISLAND_UR[0] * tileLength), (float) (ISLAND_UR[1] * tileLength) };
        this.startCorner = startingCorner;
        this.searchCanColour = searchCanColour;
        this.TILE_LENGTH = (float) tileLength;
        this.SCAN_RADIUS = scanRadius;
        this.currentScanPoint = 0;
    }

    /**
     * Sets the scan locations depending on the starting corner, the tunnel
     * position, the LL and the UR. It also sets all the dumping points used for
     * discarding cans
     * 
     * @author Alice Kazarine
     * @since March 26, 2019
     */
    public void setScanPositions() {
        // calculates the width and the height of the padded search area
        float deltaX = (SZ_UR[0] - SZ_LL[0]) * TILE_LENGTH;
        float deltaY = (SZ_UR[1] - SZ_LL[1]) * TILE_LENGTH;

        int nYPoints = Math.round(deltaY / SCAN_RADIUS);
        int nXPoints = Math.round(deltaX / SCAN_RADIUS);

        for (int i = 0; i < nXPoints; i++) {
            for (int j = 0; j < nYPoints; j++) {
                float[] nextPos = new float[2];

                nextPos[1] = SZ_LL[1] * TILE_LENGTH + j * SCAN_RADIUS;
                if (startCorner == 1 || startCorner == 2) {
                    nextPos[0] = SZ_UR[0] * TILE_LENGTH - i*SCAN_RADIUS;
                    // set the dumping points depending on searchpoints
                    if (i == 0) {
                        dumpingPoints.add(new float[] { nextPos[0] + TILE_LENGTH / 2, nextPos[1] });
                    }
                } else {// startCorner = 0 or 3
                    nextPos[0] = SZ_LL[0] * TILE_LENGTH + i * SCAN_RADIUS;
                    // set the dumping points depending on searchpoints
                    if (i == 0) {
                        dumpingPoints.add(new float[] { nextPos[0] - TILE_LENGTH / 2, nextPos[1] });
                    }
                }
                scanningPoints.add(nextPos);
            }
        }
        System.out.println("Created " + scanningPoints.size() + " scan points.");
    }

    /**
     * 
     * @return all the scanning positions
     * 
     * @author Julian Armour
     */
    public List<float[]> getScanningPoints() {
        return scanningPoints;
    }

    /**
     * 
     * @return all the possible dumping positions
     * 
     * @author Julian Armour
     */
    public List<float[]> getDumpingPoints() {
        return dumpingPoints;
    }

    /**
     * @return the current scan point
     * 
     * @author Julian Armour
     * @since March 26, 2019
     */
    public float[] getCurrentScanPoint() {
        return scanningPoints.get(currentScanPoint);
    }

    /**
     * Scans for remaining cans
     * 
     * @return <code>true</code> if it all the zones have been scanned or the time
     *         limit has been reached
     * 
     * @author Alice Kazarine, Julian Armour
     * @since March 26, 2019
     */
    public boolean scanZones() {
        System.out.println("SCAN RADIUS: " + SCAN_RADIUS);
        while (currentScanPoint < scanningPoints.size()) {
            movCon.travelTo(scanningPoints.get(currentScanPoint)[0], scanningPoints.get(currentScanPoint)[1], false);
            localizer.quickLocalization();
            movCon.driveDistance(-Main.LT_SENSOR_TO_WHEELBASE);
            float[] canPos = fastCanScan(P_SZ_LL, P_SZ_UR, 359, SCAN_RADIUS);
            if (canPos != null) {
            	System.out.println("Found a can");
                boolean foundTheCan = travelToCan(canPos);
                if (foundTheCan) {
                    claw.closeClawForWeighing();
                    claw.openClaw();
                    colourDetector.collectColourData(1);
                    CanColour canColour = colourDetector.getCanColour(colourDetector.getColourSamples());
                    claw.closeClaw();
                    boolean canIsHeavy = weightDetector.canIsHeavy();
                    claw.closeClaw();

                    // beep depending on canColour and canIsHeavy
                    Beeper.colourAndWeightBeep(canIsHeavy, canColour);

                    // if this is the can colour we're looking for
                    if (canColour == searchCanColour) {
                        // go back to current scanning point
                        float[] currentScanPoint = getCurrentScanPoint();
                        movCon.travelTo(currentScanPoint[0], currentScanPoint[1], false);
                        claw.closeClaw();
                        System.out.println("Found can, returning home");
                        navigator.travelBackToStartingCorner();
                        return false;
                    } else {
                        // wrong colour, discard it outside the search zone
                        System.out.println("Dumping can");
                        dumpCan();
                        continue;
                    }
                } else {
                    continue;
                }
            } else {
                currentScanPoint += 1;
            }
        }
        if (currentScanPoint >= scanningPoints.size()) {
            System.out.println("All scanning points scanned, returning home");
            claw.closeClaw();
            navigator.travelBackToStartingCorner();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Discards a can at the nearest dumping zone.
     * 
     * @author Julian Armour
     * @since March 26, 2019
     */
    private void dumpCan() {
        navigator.travelToNearestDumpingPoint();
        claw.openClaw();
        movCon.driveDistance(-TILE_LENGTH / 2, false);
        claw.closeClaw();
        movCon.rotateAngle(180, true);
        localizer.quickLocalization();
    }

    /**
     * Causes the robot to travel to the general location of a detected can.
     * 
     * @param canPos
     *            the general position of a can, which the robot will travel to.
     * @return <code>true</code> if it found and traveled to a can,
     *         <code>false</code> if it didn't find a can.
     * 
     * @author Julian Armour
     * @since March 5, 2019
     */
    public boolean travelToCan(float[] canPos) {
        double[] robotPos = odo.getXYT();
        // travel robot ~12 cm in front of can
        movCon.turnTo(movCon.calculateAngle(robotPos[0], robotPos[1], canPos[0], canPos[1]));
        movCon.driveDistance(movCon.calculateDistance(robotPos[0], robotPos[1], canPos[0], canPos[1]) - 12, false);
        claw.openClaw();
        movCon.rotateAngle(90, false, false);
        canPos = fastCanScan(P_SZ_LL, P_SZ_UR, 180, 20);
        if (canPos == null) {
            return false;
        } else {
            // move forward until to appropriate distance for gripping the can
            USData.flush();
            float dist = USData.getFilteredDistance();
            if (dist < TILE_LENGTH) {
                movCon.driveDistance(dist - Main.US_SENSOR_TO_CLAW, false);
            }
            return true;
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
     * @param sweepAngle
     *            the angle the robot will rotate. 0-359
     * @param scanRadius
     *            the maximum distance for detecting a can
     * @return the position of a can in the scan radius, or <code>null</code> if a
     *         can is not found
     * 
     * @author Julian Armour
     * @since March 19, 2019
     */
    public float[] fastCanScan(float[] searchLL, float[] searchUR, double sweepAngle, float scanRadius) {
        claw.openClaw();
        double[] robotPos = odo.getXYT();
        // start rotating clockwise
        // scan for positions that are within the search zone
        final double finalHeading = (robotPos[2] + sweepAngle) % 360.0;

        final boolean[] atFinalHeading = { false }; // anonymous class trick for outer-scope variables
        Runnable rotater = new Runnable() {
            @Override
            public void run() {
//            	System.out.println("Odometer reading before spinning: " + odo.getXYT()[2]);
//            	System.out.println("Final heading: " + finalHeading);
                movCon.turnClockwiseTo(finalHeading, false);
                // the robot is only at the final heading if it wasn't interrupted by detecting
                // a can
                if (!Thread.interrupted()) {
                    atFinalHeading[0] = true;
                }
            }
        };

        USData.flush();
        Delay.msDelay(2000);
        Thread rotT = new Thread(rotater);
        rotT.start(); // start rotating
        System.out.println("Starting rotation");
        float[] position = new float[2];
        while (!atFinalHeading[0]) {
            float dist = USData.getFilteredDistance();
            if (dist <= scanRadius) {
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
                        Delay.msDelay(2000);
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
     * @deprecated use {@link #fastCanScan(float[], float[], double, float)}
     *             instead. Scans for cans by rotating 360-degrees and returns all
     *             potential locations of cans.
     * 
     * @param searchLL
     * @param searchUR
     * @return a list containing detected positions of cans
     * @author Julian Armour
     */
    public List<float[]> canScan(float[] searchLL, float[] searchUR) {
        claw.openClaw();

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
                    Delay.msDelay(CAN_SCAN_PERIOD);
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
