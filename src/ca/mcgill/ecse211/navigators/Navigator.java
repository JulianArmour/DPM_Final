package ca.mcgill.ecse211.navigators;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.ecse211.Main;
import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.odometer.*;
import ca.mcgill.ecse211.strategies.CanSearch;

/**
 * Provides the methods for navigation tasks
 * 
 * @author Cedric Barre, Julian Armour
 * @since 25th of March 2019
 * @version 2
 */
public class Navigator {
    // constants

    // dependencies
    private MovementController move;
    private Odometer           odo;
    private Localization       localizer;
    private CanSearch          canSearcher;
    // fields
    private float              tileSize;
    private int                TLLX, TLLY, TURX, TURY, STZLLX, STZLLY, STZURX, STZURY, ILLX, ILLY, IURX, IURY;
    private int                bridgeTileLength;
    private int                SC;
    private double             lightSensorToWheelbase;
    private int[]              searchZoneLL;
    private int[]              searchZoneUR;

    /**
     * @param move
     * @param odo
     * @param localizer
     * @param tunnelLL
     * @param TUR
     * @param STZLL
     * @param STZUR
     * @param SC
     * @param ILL
     * @param IUR
     * @param tileSize
     */
    public Navigator (
            MovementController move, Odometer odo, Localization localizer, int[] tunnelLL, int TUR[], int STZLL[],
            int STZUR[], int SC, int ILL[], int IUR[], int[] searchZoneLL, int[] searchZoneUR, float tileSize
    ) {
        this.move = move;
        this.odo = odo;
        this.TLLX = tunnelLL[0]; // Set tunnel coordinates
        this.TLLY = tunnelLL[1];
        this.TURX = TUR[0];
        this.TURY = TUR[1];
        this.STZLLX = STZLL[0]; // Set starting zone coordinates
        this.STZLLY = STZLL[1];
        this.STZURX = STZUR[0];
        this.STZURY = STZUR[1];
        this.ILLX = ILL[0]; // Set search zone coordinates
        this.ILLY = ILL[1];
        this.IURX = IUR[0];
        this.IURY = IUR[1];
        // set search zone coordinates
        this.searchZoneLL = searchZoneLL;
        this.searchZoneUR = searchZoneUR;
        this.localizer = localizer;
        this.SC = SC;
        this.lightSensorToWheelbase = Main.LT_SENSOR_TO_WHEELBASE;
        // Calculate bridge length from coordinates
        this.bridgeTileLength = (Math.abs(TLLX - TURX) > Math.abs(TLLY - TURY)) ? Math.abs(TLLX - TURX)
                : Math.abs(TLLY - TURY);
        this.tileSize = tileSize;

    }
    
    /**
     * @param canSearch a {@link CanSearch} object
     * 
     * @author Julian Armour
     * @since March 26, 2019
     */
    public void setCanSearcher(CanSearch canSearch) {
        this.canSearcher = canSearch;
    }

    /**
     * @deprecated was used for the demo
     * 
     * Causes the robot to travel to the upper right corner of the search zone. This
     * method is meant to be called from within the search zone.
     * 
     * @author Julian Armour
     * @since March 16, 2019
     */
    public void travelToSearchZoneUR() {
        // travel to the first scan zone, which is outside the searchzone. This is a
        // safe movement since there shouldn't be any cans in the way
        float[] safePoint = canSearcher.getScanningPoints().get(0);
        move.travelTo(safePoint[0], safePoint[1], false);
        // face north and correct odometer
        move.turnTo(0);
        localizer.quickLocalization();
        // now move to the searchzone upper right's y-coordinate + half a tile
        move.travelTo(odo.getXYT()[0], (searchZoneUR[1]+0.5)*tileSize, false);
        // now move to the searchzone upper right's x-coordinate + half a tile
        // should we move right?
        if (odo.getXYT()[0] < searchZoneUR[0]) {
            // face right
            move.turnTo(90);
            localizer.quickLocalization();
            move.travelTo((searchZoneUR[0]+0.5)*tileSize, (searchZoneUR[1]+0.5)*tileSize, false);
        }
        // finally move to exact upper right
        move.travelTo(searchZoneUR[0]*tileSize, searchZoneUR[1]*tileSize, false);
        move.turnTo(move.roundAngle());
        localizer.completeQuickLocalization();
    }
    
    /**
     * Makes the robot travel to the current scan point in the search zone.
     * 
     * @see CanSearch#getCurrentScanPoint()
     * 
     * @author Julian Armour
     * @since March 25, 2019
     */
    public void travelToSearchZone() {
        float[] currentScanPoint = canSearcher.getCurrentScanPoint();
        double[] curPos = odo.getXYT();
        double[] dest = new double[2];
        
        // either approach from the right or left, depending on the starting corner
        if (SC == 1 || SC == 2) {
            dest[0] = currentScanPoint[0] + tileSize/2;
        } else {
            dest[0] = currentScanPoint[0] - tileSize/2;
        }
        dest[1] = curPos[1];//keep the y-pos the same
        // travel w.r.t current scan point's x-pos
        move.turnTo(move.calculateAngle(curPos[0], curPos[1], dest[0], dest[1]));
        localizer.quickLocalization();
        move.travelTo(dest[0], dest[1], false);
        // set dest w.r.t current scan point's y-pos
        curPos = odo.getXYT();
        dest[0] = curPos[0];
        dest[1] = currentScanPoint[1];
        // travel w.r.t current scan point's y-pos
        move.turnTo(move.calculateAngle(curPos[0], curPos[1], dest[0], dest[1]));
        localizer.quickLocalization();
        move.travelTo(dest[0], dest[1], false);
        // at this point the robot is right beside the scan point and CanSearch#scanZones() can be called.
    }

    /**
     * @deprecated was used for the demo
     * 
     * Causes the robot to travel to the lower left corner of the search zone. This
     * method should be called after the robot crosses the tunnel and localizes.
     * 
     * @author Julian Armour
     * @since March 16, 2019
     */
    public void travelToSearchZoneLL() {
        double[] curPos = odo.getXYT();
        // travel to half a tile under searchZoneLL's y-coordinate
        move.turnTo(move.calculateAngle(curPos[0], curPos[1], curPos[0], (searchZoneLL[1]) * tileSize));
        localizer.quickLocalization();
        move.travelTo(curPos[0], (searchZoneLL[1]) * tileSize, false);
        System.out.println("ODO:\t"+"X:"+odo.getXYT()[0]/tileSize+" Y:"+odo.getXYT()[1]/tileSize);
        localizer.quickLocalization();
        System.out.println("ODO:\t"+"X:"+odo.getXYT()[0]/tileSize+" Y:"+odo.getXYT()[1]/tileSize);
        move.travelTo(curPos[0], (searchZoneLL[1] - 0.5) * tileSize, false);
        // at this point the robot is half a tile bellow the searchZoneLL's y-coordinate
        // now travel to searchZoneLL's x-coordinate
        curPos = odo.getXYT();
        // move left or right?
        if (curPos[0] < searchZoneLL[0] * tileSize) {
            // face right
            move.turnTo(90);
        } else {
            // face left
            move.turnTo(270);
        }
        // fast localization to straighten
        localizer.quickLocalization();
        // now go
        move.travelTo(searchZoneLL[0] * tileSize, (searchZoneLL[1] - 0.5) * tileSize, false);
        // now to move up half a tile in the y-direction
        
        move.travelTo(searchZoneLL[0] * tileSize, searchZoneLL[1] * tileSize, false);
        // at this point the robot should be at searchzone's lower left. Localize to
        // make sure
        localizer.completeQuickLocalization();
    }
	
    /**
     * determines if a point is intersecting the tunnel
     * 
     * @param p
     *            the point that's checked
     * @return true if a point is intersecting the tunnel
     * 
     * @author Julian Armour
     * @since March 26, 2019
     */
    private boolean pointIsOnTunnel(float[] p) {
        if (p[0] < TLLX * tileSize || p[0] > TURX * tileSize)
            return false;
        else if (p[1] < TLLY * tileSize || p[1] > TURY * tileSize)
            return false;
        else
            return true;
    }
    
    /**
     * Finds the nearest dumping point and makes the robot travel to it.
     * It checks if a dump point is on the tunnel, in which case it ignores it.
     * <p>
     * The robot will end with orientation of either 270 (SC = 0|3) or 90 (SC = 1|2) degrees.
     * 
     * @author Julian Armour
     * @since March 26, 2019
     */
    public void travelToNearestDumpingPoint() {
        float[] closestPoint = null;
        
        List<float[]> dumpointPoints = canSearcher.getDumpingPoints();
        Iterator<float[]> it = dumpointPoints.iterator();
        while (it.hasNext()) {
            if (closestPoint == null) {
                closestPoint = it.next();
            }
            float[] p = (float[]) it.next();
            if (!pointIsOnTunnel(p)) {// point p isn't on the tunnel
                // compare distances current closest point with p
                double[] curPos = odo.getXYT();
                // no need for sqrt when simply checking which is larger 
                double dCurSquared = Math.pow(closestPoint[0]-curPos[0], 2)+Math.pow(closestPoint[1]-curPos[1], 2);
                double dPSquared = Math.pow(closestPoint[0]-p[0], 2)+Math.pow(closestPoint[1]-p[1], 2);
                if (dPSquared < dCurSquared) {
                    closestPoint = p;
                }
            }
        }
        // after finding closest point, travel to it
        move.travelTo(closestPoint[0], closestPoint[1], false);
        // face 270 or 90 depending on starting corner
        if (SC == 0 || SC == 3) {
            move.turnTo(270);//face West
        } else {
            move.turnTo(90);//face East
        }
    }
    
    /**
     * Travel to the tunnel from either the starting point or any point on the
     * island
     * 
     * @param direction
     *            Boolean, if true, robot is going to the tunnel from the starting
     *            zone, if false the robot is going to the tunnel from the search
     *            zone
     *            
     * @author Cedric Barre
     * @since March 25, 2019
     */
	public void travelToTunnel(boolean direction) {

		boolean OP1 = true;
		int turnToTunnel = 0;
		double tunnelTilePosYOP2 = 0, tunnelTilePosXOP2 = 0, tunnelTilePosXOP1 = 0, tunnelTilePosYOP1 = 0;
		if(direction) { //If robot is going to tunnel in starting zone, use these parameters
			switch(SC) {
			case 0:
				if(TURX > STZURX) {
					OP1 = true;
					turnToTunnel = 90;
					tunnelTilePosXOP1 = TLLX-1;
					tunnelTilePosYOP1 = TLLY + 0.5;
				} else {
					OP1 = false;
					tunnelTilePosYOP2 = TLLY - 1;
					tunnelTilePosXOP2 = TURX - 0.5;
					turnToTunnel = 0;
				}
				break;
			case 1:
				if(TLLX < STZLLX) {
					OP1 = true;
					tunnelTilePosXOP1 = TURX + 1;
					tunnelTilePosYOP1 = TURY - 0.5;
					turnToTunnel = 270;
				} else { 
					OP1 = false;
					tunnelTilePosXOP2 = TURX - 0.5;
					tunnelTilePosYOP2 = TLLY - 1;
					turnToTunnel = 0;
				}
				break;
			case 2:
				if(TLLX < STZLLX) {
					OP1 = true;
					tunnelTilePosXOP1 = TURX + 1;
					tunnelTilePosYOP1 = TURY - 0.5;
					turnToTunnel = 270;
					System.out.println("I AM HERE :)");
				} else {
					OP1 = false;
					tunnelTilePosYOP2 = TURY + 1;
					tunnelTilePosXOP2 = TURX - 0.5;
					turnToTunnel = 180;
				}
				break;
			case 3:
				if(TURX > STZURX) {
					OP1 = true;
					turnToTunnel = 90;
					tunnelTilePosXOP1 = TLLX-1;
					tunnelTilePosYOP1 = TLLY + 0.5;
				} else {
					OP1 = false;
					tunnelTilePosYOP2 = TURY + 1;
					tunnelTilePosXOP2 = TURX - 0.5;
					turnToTunnel = 180;
				}
				break;
			default:
				break;
			}
		} else { //If robot is going to tunnel from search zone, use these parameters
			switch(SC) {
			case 0:
				if(TURX > STZURX) {
					OP1 = true;
					turnToTunnel = 270;
					tunnelTilePosXOP1 = TURX + 1;
					tunnelTilePosYOP1 = TURY - 0.5;
				} else {
					OP1 = false;
					tunnelTilePosYOP2 = TURY - 0.5;
					tunnelTilePosXOP2 = TURX + 1;
					turnToTunnel = 180;
				}
				break;
			case 1:
				if(TLLX < STZLLX) {
					OP1 = true;
					tunnelTilePosXOP1 = TLLX - 1;
					tunnelTilePosYOP1 = TURY + 0.5;
					turnToTunnel = 90;
				} else { 
					OP1 = false;
					tunnelTilePosXOP2 = TURX - 0.5;
					tunnelTilePosYOP2 = TURY + 1;
					turnToTunnel = 180;
				}
				break;
			case 2:
				if(TLLX < STZLLX) {
					OP1 = true;
					tunnelTilePosXOP1 = TLLX - 1;
					tunnelTilePosYOP1 = TLLY + 0.5;
					turnToTunnel = 90;
				} else {
					OP1 = false;
					tunnelTilePosYOP2 = TLLY - 1;
					tunnelTilePosXOP2 = TLLX + 0.5;
					turnToTunnel = 0;
				}
				break;
			case 3:
				if(TURX > STZURX) {
					OP1 = true;
					turnToTunnel = 270;
					tunnelTilePosXOP1 = TURX + 1;
					tunnelTilePosYOP1 = TURY - 0.5;
				} else {
					OP1 = false;
					tunnelTilePosYOP2 = TLLY - 1;
					tunnelTilePosXOP2 = TLLX + 0.5;
					turnToTunnel = 0;
				}
				break;
			default:
				break;
			}
		}
		if(OP1) { 
		    //Path 1 to tunnel depending on tunnel position: if the tunnel is on the east or west side of the starting zone
		    //Move to the x position on the grid line before the tunnel
		    System.out.println("Current Position: "+odo.getXYT()[0]/tileSize+", "+odo.getXYT()[1]/tileSize);
		    System.out.println("Moving to: "+tunnelTilePosXOP1+", "+odo.getXYT()[1]/tileSize);
			move.travelTo(tunnelTilePosXOP1*tileSize, odo.getXYT()[1], false); 
		    localizer.quickLocalization();
			move.driveDistance(-lightSensorToWheelbase);
			//turn to to the y position between gridlines before the tunnel
			move.turnTo(move.calculateAngle(odo.getXYT()[0], odo.getXYT()[1], odo.getXYT()[0], tunnelTilePosYOP1 * tileSize));
			localizer.quickLocalization();
			//Move the to y position on the grid line in front of the tunnel
			if (move.roundAngle() == 180) {
			    move.travelTo(odo.getXYT()[0], (tunnelTilePosYOP1 + 0.5)*tileSize, false);
            } else {
                move.travelTo(odo.getXYT()[0], (tunnelTilePosYOP1 - 0.5)*tileSize, false);
            }
			// correct odometer
			localizer.quickLocalization();
			//Move the to y position on the grid line in the middle of the tile in front of the tunnel
            move.travelTo(odo.getXYT()[0], tunnelTilePosYOP1*tileSize, false);
            // finally face the tunnel entrance
			move.turnTo(turnToTunnel);
			localizer.quickLocalization(); //Make sure we are well facing the tunnel
		}
		else {
		    //Path 2 to tunnel depending on position: if tunnel is on the north or south side of the starting zone
			// move to y position on the grid line before the tunnel
		    move.travelTo(odo.getXYT()[0], tunnelTilePosYOP2*tileSize, false);
			localizer.quickLocalization();
			move.driveDistance(-lightSensorToWheelbase);
			// turn to the x position between gridlines before the tunnel
			move.turnTo(move.calculateAngle(odo.getXYT()[0], odo.getXYT()[1], tunnelTilePosXOP2*tileSize, odo.getXYT()[1]));
            localizer.quickLocalization();
            //Move the to x position on the grid line in front of the tunnel
            if (move.roundAngle() == 270) {
                move.travelTo((tunnelTilePosXOP2 + 0.5)*tileSize, odo.getXYT()[1], false);
            } else {
                move.travelTo((tunnelTilePosXOP2 - 0.5)*tileSize, odo.getXYT()[1], false);
            }
			// correct odometer
            localizer.quickLocalization();
            //Move the to x position on the grid line in the middle of the tile in front of the tunnel
            move.travelTo(tunnelTilePosXOP2*tileSize, odo.getXYT()[1], false);
			// finally face the tunnel entrance
			move.turnTo(turnToTunnel);
			localizer.quickLocalization(); //Make sure we are well facing the tunnel
		}
	}
	
	
	/**
	 * Travel across the tunnel from front to back or from back to front
	 * @param direction Boolean: if true, the robot is going from starting zone to search zone, if false, the robot is going from search zone to starting zone
	 * 
	 * @author Cedric Barre
	 * @since March 9, 2019
	 */
	public void travelThroughTunnel(boolean direction) {
		int posCorX = 0, posCorY = 0;
		int thetaCor = 0;
		boolean turnLoc = true;
		if (direction) { //If robot is going from starting zone to search zone, these are the parameters to use
			switch(SC) {
			case 0:
				if(TURX > STZURX) {
					if(TURY == IURY) {
						turnLoc = true;
						posCorX = TURX + 1;
						posCorY = TURY - 1;
					} else {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					}
				} else {
					if(TURX == IURX) {
						turnLoc = false;
						posCorX = TURX - 1;
						posCorY = TURY + 1;
					} else {
						turnLoc = true;
						posCorX = TURX;
						posCorY = TURY + 1;
					}
				}
				break;
			case 1:
				if(TLLX < STZLLX) {
					if(TURY == IURY) {
						turnLoc = false;
						posCorX = TLLX - 1;
						posCorY = TLLY;
					} else {
						turnLoc = true;
						posCorX = TLLX - 1;
						posCorY = TLLY + 1;
					}
				} else { 
					if(TLLX == ILLX) {
						turnLoc = true; 
						posCorX = TURX;
						posCorY = TURY + 1;
					} else {
						turnLoc = false;
						posCorX = TURX - 1;
						posCorY = TURY + 1;
					}
					
				}
				break;
			case 2:
				if(TLLX < STZLLX) {
					if(TLLY == ILLY) {
						turnLoc = true; 
						posCorX = TLLX - 1;
						posCorY = TLLY + 1;
					} else {
						turnLoc = false;
						posCorX = TLLX - 1;
						posCorY = TLLY;
					}
				} else {
					if(TLLX == ILLX) {
						turnLoc = false;
						posCorX = TLLX + 1;
						posCorY = TLLY - 1;
					} else {
						turnLoc = true;
						posCorX = TLLX;
						posCorY = TLLY - 1;
					}
				}
				break;
			case 3:
				if(TURX > STZURX) {
					if(TLLY == ILLY) {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					} else {
						turnLoc = true;
						posCorX = TURX + 1;
						posCorY = TURY - 1;
					}
				} else {
					if(TLLX == ILLX) {
						turnLoc = false;
						posCorX = TLLX + 1;
						posCorY = TLLY - 1;
					} else {
						turnLoc = true;
						posCorX = TLLX;
						posCorY = TLLY - 1;
					}
				}
				break;
				default:
					break;
			}
		} else { //If robot is going from search zone to starting zone, these are the parameters to use
			switch(SC) {
			case 0:
				if(TURX > STZURX) {
					if(TLLY == STZLLY) {
						turnLoc = true;
						posCorX = TLLX - 1;
						posCorY = TLLY + 1;
					} else {
						turnLoc = false;
						posCorX = TLLX - 1;
						posCorY = TLLY;
					}
				} else {
					if(TLLX == STZLLX) {
						turnLoc = false;
						posCorX = TLLX + 1;
						posCorY = TLLY - 1;
					} else {
						turnLoc = true;
						posCorX = TLLX;
						posCorY = TURY - 1;
					}
				}
				break;
			case 1:
				if(TLLX < STZLLX) {
					if(TLLY == ILLY) {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					} else {
						turnLoc = true;
						posCorX = TURX + 1;
						posCorY = TLLY - 1;
					}
				} else { 
					if(TURX == IURX) {
						turnLoc = true; 
						posCorX = TLLX;
						posCorY = TLLY - 1;
					} else {
						turnLoc = false;
						posCorX = TLLX + 1;
						posCorY = TLLY - 1;
					}
					
				}
				break;
			case 2:
				if(TLLX < STZLLX) {
					if(TURY == STZURY) {
						turnLoc = true; 
						posCorX = TURX + 1;
						posCorY = TURY - 1;
					} else {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					}
				} else {
					if(TURX == STZURX) {
						turnLoc = false;
						posCorX = TURX - 1;
						posCorY = TURY + 1;
					} else {
						turnLoc = true;
						posCorX = TURX;
						posCorY = TURY + 1;
					}
				}
				break;
			case 3:
				if(TURX > STZURX) {
					if(TURY == STZURY) {
						turnLoc = false;
						posCorX = TLLX - 1;
						posCorY = TLLY;
					} else {
						turnLoc = true;
						posCorX = TLLX - 1;
						posCorY = TLLY + 1;
					}
				} else {
					if(TLLX == STZLLX) {
						turnLoc = true;
						posCorX = TURX;
						posCorY = TLLY + 1;
					} else {
						turnLoc = false;
						posCorX = TURX - 1;
						posCorY = TURY + 1;
					}
				}
				break;
				default:
					break;
			}
		}
		
		
		
		move.driveDistance((bridgeTileLength+2)*tileSize - lightSensorToWheelbase); //Cross tunnel

		localizer.quickThetaCorrection(); //Correct angle and x position 
		move.driveDistance(-lightSensorToWheelbase); 
		move.rotateAngle(90, turnLoc);

		localizer.quickThetaCorrection(); //Correct y position
		move.driveDistance(-lightSensorToWheelbase);
		
		thetaCor = move.roundAngle(); //Update the odometer
		odo.setXYT(posCorX*tileSize, posCorY*tileSize, thetaCor);
		
	}
	
	/**
	 * Makes the robot travel to the gridpoint intersection of it's starting tile
	 * @author Cedric Barre
	 * @since March 27, 2019
	 */
    public void travelToStartingTile() {
        switch (SC) {
        case 0:
            move.travelTo(tileSize, tileSize, false);
            break;
        case 1:
            move.travelTo(14 * tileSize, tileSize, false);
            break;
        case 2:
            move.travelTo(14 * tileSize, 8 * tileSize, false);
            break;
        case 3:
            move.travelTo(tileSize, 8 * tileSize, false);
            break;
        }
    }

    /**
     * Makes the robot travel back to the first grid-point at the starting corner.
     * This method should be called from within the search zone.
     * 
     * @author Julian Armour
     * @since March 25, 2019
     */
    public void travelBackToStartingCorner() {
        if (SC == 0 || SC == 3) {
            move.turnTo(270);
        } else {
            move.turnTo(90);
        }
        localizer.quickLocalization();
        move.driveDistance(tileSize/2);
        
        travelToTunnel(false);
        travelThroughTunnel(false);
        travelToStartingTile();
    }
}
