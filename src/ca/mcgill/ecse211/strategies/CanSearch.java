package ca.mcgill.ecse211.strategies;

import java.io.ObjectOutputStream.PutField;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.navigators.MovementController;
import ca.mcgill.ecse211.odometer.Odometer;
import ca.mcgill.ecse211.sensors.MedianDistanceSensor;

/*
 * This class contains the search algorithm used to find the cans in the search zone and approach them 
 * in such a way that it is easy to grab onto them. 
 * 
 * 
 */
public class CanSearch {
	
	protected static final long CAN_SCAN_PERIOD = 100;
    private Odometer odo;
	private MovementController movCon;
	private MedianDistanceSensor  USData;
	private float[] PLL,PUR,PTUN, PISL_LL, PISL_UR;
	private int startCorner;
	private float TILE_LENGTH;
	private float deltaX, deltaY;
	private float SCAN_RADIUS = TILE_LENGTH*3;
	private float[] nextPos;
	
	private LinkedList<float[]> scanningPoints = new LinkedList<float[]>();
	
	public CanSearch(Odometer odometer, MovementController movementController, MedianDistanceSensor USData,float[] PLL, float[] PUR, float[] PTUNEL, float[] PISLAND_LL,float [] PISLAND_UR, int startingCorner, float TILE_LENGTH) {
		
		this.odo = odometer;
		this.movCon = movementController;
		this.USData = USData;
		this.PLL = PLL;
		this.PUR = PUR;
		this.PTUN = PTUNEL;
		this.PISL_LL = PISLAND_LL;
		this.PISL_UR = PISLAND_UR;
		this.startCorner = startingCorner;
		
	}
		
	
	/*
	 * Sets the scan locations depending on the starting corner, the tunnel position, the LL and the UR 
	 */
	public void setScanPositions() {
		// TODO add all possibilities ST ==0,1,2,3
		
		deltaX = PISL_UR[0] - PLL[0];
		deltaY = PISL_UR[1] - PISL_LL[1];
		
		
		if (startCorner == 1) {

			//calculate positions as float[]
			float[] firstPos = {PISL_UR[0]-TILE_LENGTH/2 , PISL_LL[1]+TILE_LENGTH/2};
			scanningPoints.add(firstPos);
			
			for(int i=1; i <3; i++) {
				
				nextPos[0] = PISL_UR[0] - i*(deltaX/SCAN_RADIUS)*TILE_LENGTH;
				
				for(int j=0; j<3; j++) {
					
					if (j == 0) {
						nextPos[1] = PISL_LL[1] + TILE_LENGTH/2;
					}
					
					nextPos[1] = PISL_LL[1] + j*(deltaY/SCAN_RADIUS)*TILE_LENGTH;
					
					//now that nextPos[0] and nextPos[1] are defined, add them to the list
					scanningPoints.add(nextPos);
				}
			}
		}
		
		if (startCorner == 3) {
			
			float[] firstPos = {PISL_LL[0]+TILE_LENGTH/2 , PISL_UR[1]-TILE_LENGTH/2};
			scanningPoints.add(firstPos);
			
			for(int i=1;i<3;i++) {
				
				nextPos[1] = PISL_UR[1] - i*(deltaY/SCAN_RADIUS)*TILE_LENGTH;
				
				for(int j=0; j<3; j++) {
					if (j==0) {
					nextPos[0] = PISL_LL[0] + TILE_LENGTH/2;
					}
					
					nextPos[0] = PISL_LL[0] + j*(deltaX/SCAN_RADIUS)*TILE_LENGTH;
					
					scanningPoints.add(nextPos);
				}
			}
			
		}
		
		
	}
	
	/*
	 * Goes to current scan position and scans to detect a can
	 */
	public void getCanPosition() {


		if(startCorner == 1) {
			movCon.travelTo(scanningPoints.getFirst()[0], scanningPoints.getFirst()[1], false);
			movCon.turnTo(90);
			movCon.rotateAngle(360, false, true);
			
			while(USData.getFilteredDistance() > SCAN_RADIUS) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


		}


	}
	
	/**
	 * 
	 * @param searchLL lower left of the search zone
	 * @param searchUR upper right of the search zone
	 * @return the position of a can in the scan radius, or <code>null</code> if a can is not found
	 */
	public double[] fastCanScan(double[] searchLL, double[] searchUR) {
	    double[] robotPos = odo.getXYT();
	    // start rotating clockwise
	    // scan for positions that are within the search zone
	    final double finalHeading = (robotPos[2] - 1.0) % 360.0;
	    
	    final boolean[] atFinalHeading = {false}; // anonymous class trick for outer-scope variables
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
        double[] position = new double[2];
        while (!atFinalHeading[0]) {
            double dist = (double) USData.getFilteredDistance();
            if (dist <= scanRadius) {
                double angle = odo.getXYT()[2];
                position[0] = dist * Math.sin(Math.toRadians(angle)) + robotPos[0];
                position[1] = dist * Math.cos(Math.toRadians(angle)) + robotPos[1];
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
                    position[0] = meanDist * Math.sin(Math.toRadians(angle)) + robotPos[0];
                    position[1] = meanDist * Math.cos(Math.toRadians(angle)) + robotPos[1];
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
     * 
     * @param robotPos
     * @param searchLL
     * @param searchUR
     * @return a list containing detected positions of cans
     * @author Julian Armour
     */
    public List<double[]> canScan(double[] searchLL, double[] searchUR) {
        double[] robotPos = odo.getXYT();
        
        final List<double[]> angleDistData = new LinkedList<double[]>();
        // anonymous class for polling distance data while the robot rotates
        Thread distancePoller = new Thread() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    double angle = odo.getXYT()[2];
                    double dist = (double) USData.getFilteredDistance();
                    if (dist <= scanRadius) {
                        angleDistData.add(new double[] { angle, dist });
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

        List<double[]> positionData = new LinkedList<double[]>();
        Iterator<double[]> it = angleDistData.iterator();
        while (it.hasNext()) {
            double[] dat = it.next();
            double[] position = new double[] { dat[1] * Math.sin(Math.toRadians(dat[0])) + robotPos[0],
                    dat[1] * Math.cos(Math.toRadians(dat[0])) + robotPos[1] };
            
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
    private boolean inSearchZone(double[] position, double[] searchLL, double[] searchUR) {
        if (position[0] > searchUR[0] || position[0] < searchLL[0]) {
            return false;
        } else if (position[1] > searchUR[1] || position[1] < searchLL[1]) {
            return false;
        } else {
            return true;
        }
    }

    /*
	 * approaches the position where a potential can was detected,
	 * performs a second scan, 
	 * takes position for grabbing
	 */
	public void secondaryScan() {
		
	}
	
	

}
