package ca.mcgill.ecse211.navigators;
import java.util.function.IntPredicate;
import java.util.function.ToDoubleBiFunction;

import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.odometer.*;

/**
 * Provides the methods for navigation tasks
 * @author Cedric
 *
 */
public class Navigator {
	
	private static final double TILE_SIZE = 30.48;
	private static final double VERT_SENSOR_OFFSET = 5; 
	
	private static MovementController move;
	private static Odometer odo;
	private static Localization localizer;
	private static int TLLX, TLLY, TURX, TURY, STZLLX, STZLLY, STZURX, STZURY, ILLX, ILLY, IURX, IURY;
	
	
	
	private static int dumpingSpotX, dumpingSpotY;
	private static int bridgeTileLength;
	private static int SC;

	/**
	 * Contructor for the navigator class
	 * @param move Movement controller instance to control the robots movements
	 * @param odo Odometer instance for odometry
	 * @param localizer Localizer for the robot to use its sensors for localization
	 * @param TLL Lower left coordinates for the tunnel
	 * @param TUR Upper right coordinates for the tunnel
	 * @param STZLL Lower left coordinates for the starting zone
	 * @param STZUR Upper right coordinates for the starting zone
	 * @param SC Starting corner
	 * @param ILL Lower left coordinates for the island
	 * @param IUR Upper right coordinates for the island
	 */
	public Navigator(MovementController move, Odometer odo, Localization localizer, int[] TLL, int TUR[], int STZLL[], int STZUR[], int SC, int ILL[], int IUR[]) {
		this.move = move; 
		this.odo = odo;

		TLLX = TLL[0]; //Set tunnel coordinates
		TLLY = TLL[1];
		TURX = TUR[0];
		TURY = TUR[1];
		STZLLX = STZLL[0]; //Set starting zone coordinates
		STZLLY = STZLL[1];
		STZURX = STZUR[0];
		STZURY = STZUR[1];
		ILLX = ILL[0]; //Set search zone coordinates
		ILLY = ILL[1];
		IURX = IUR[0];
		IURY = IUR[1];
		this.localizer = localizer;
		this.SC = SC;
		bridgeTileLength = (Math.abs(TLLX-TURX) > Math.abs(TLLY-TURY)) ? Math.abs(TLLX-TURX) : Math.abs(TLLY-TURY); //Calculate bridge length from coordinates

		
	}
	
	/**
	 * Travel to the tunnel from either the starting point or any point on the island
	 * @param direction Boolean, if true, robot is going to the tunnel from the starting zone, if false the robot is going to the tunnel from the search zone
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
		if(OP1) { //Path 1 to tunnel depending on tunnel position: if the tunnel is on the east or west side of the starting zone
			move.travelTo(tunnelTilePosXOP1*TILE_SIZE, odo.getXYT()[1], false); //Move to the x position on the grid line before the tunnel
			localizer.quickThetaCorrection();
			odo.setTheta(move.roundAngle());
			move.driveDistance(-VERT_SENSOR_OFFSET);
			
			move.travelTo(odo.getXYT()[0], tunnelTilePosYOP1 * TILE_SIZE, false); //Move the to y position on the grid line in the middle of the tile in front of the tunnel
			move.turnTo(turnToTunnel);
			localizer.quickThetaCorrection(); //Make sure we are well facing the tunnel
			odo.setTheta(move.roundAngle());
			
		}
		else { //Path 2 to tunnel depending on position: if tunnel is on the north or south side of the starting zone
			move.travelTo(odo.getXYT()[0], tunnelTilePosYOP2*TILE_SIZE, false);
			localizer.quickThetaCorrection();
			odo.setTheta(move.roundAngle());
			move.driveDistance(-VERT_SENSOR_OFFSET);
			
			move.travelTo(tunnelTilePosXOP2*TILE_SIZE, odo.getXYT()[1], false);
			move.turnTo(turnToTunnel);
			localizer.quickThetaCorrection();
			odo.setTheta(move.roundAngle());
		}
	}
	
	/**
	 * Travel across the tunnel from front to back or from back to front
	 * @param direction Boolean: if true, the robot is going from starting zone to search zone, if false, the robot is going from search zone to starting zone
	 */
	public void throughTunnel(boolean direction) {
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
		
		move.driveDistance((bridgeTileLength+2)*TILE_SIZE - VERT_SENSOR_OFFSET); //Cross tunnel

		localizer.quickThetaCorrection(); //Correct angle and x position 
		move.driveDistance(-VERT_SENSOR_OFFSET); 
		move.rotateAngle(90, turnLoc);

		localizer.quickThetaCorrection(); //Correct y position
		move.driveDistance(-VERT_SENSOR_OFFSET);
		
		thetaCor = move.roundAngle(); //Update the odometer
		odo.setXYT(posCorX*TILE_SIZE, posCorY*TILE_SIZE, thetaCor);
		move.driveDistance(0.5*TILE_SIZE);
		move.rotateAngle(90, !turnLoc);
	}

	
	
	public void goToStartingTile() {
		
		// if sc is 0, we want to go to (1,1)
		if(SC == 0) {
			move.travelTo(TILE_SIZE, TILE_SIZE, false);
			
		}
		//if SC is 1, we want to go to (14,1)
		else if(SC == 1) {
			move.travelTo(14*TILE_SIZE, TILE_SIZE, false);
		}
		//if SC =, we want to go to (14,8)
		else if(SC == 2) {
			move.travelTo(14*TILE_SIZE, 8*TILE_SIZE, false);
			
		}
		//if SC = 3, we want to go to (1,8)
		else {
			move.travelTo(TILE_SIZE, 8*TILE_SIZE, false);
			
		}
	}
	
	//sets the dumping waypoint depending on the tunnel and startingzone
	public int[] setDumpingPoint() {
		//TODO add the possibility that that spot is actually a wall or not accessible??
		if(SC == 0) {
	
				if(TLLX == STZURX || TURX-TLLX == 2) { //if horizontal
					dumpingSpotX = (int) (TURX+TILE_SIZE/2);
					dumpingSpotY = (int) (TURY - 1.5*TILE_SIZE);

				} else  { 
					dumpingSpotX = (int) (TURX + TILE_SIZE/2);
					dumpingSpotY = (int) (TURY + TILE_SIZE/2);
				}
			
		} else if(SC == 3){ 
			
				if(TLLX == STZURX || TURX-TLLX == 2) {
					//its horizontal
					dumpingSpotX = (int) (TLLX + TILE_SIZE/2);
					dumpingSpotY = (int) (TLLY - 1.5*TILE_SIZE);
				} else {
					//its vertical
					dumpingSpotX = (int) (TURX-2.5*TILE_SIZE);
					dumpingSpotY = (int) (TURY+TILE_SIZE/2);
				}
		} else if(SC == 1) {
			
			if(TURX == STZLLX || TURX-TLLX==2) {
				//its horizontal
				dumpingSpotX =  (int) (TURX - 1.5*TILE_SIZE);
				dumpingSpotY = (int) (TURY-1.5*TILE_SIZE);
			} else {
				//its vertical
				dumpingSpotX = (int) (TURX - 1.5*TILE_SIZE);
				dumpingSpotY = (int) (TURY + TILE_SIZE/2);
			}
			
		} else { //if (SC == 2)
			
			 if(TURX == STZLLX || TURX-TLLX == 2) {
				 //its horizontal
				 dumpingSpotX = (int) (TURX - 1.5*TILE_SIZE);
				 dumpingSpotY = (int) (TURY - 1.5*TILE_SIZE);
			 } else {
				 //its vertical
				 dumpingSpotX = (int) (TURX - 1.5*TILE_SIZE);
				 dumpingSpotY = (int) (TURY - 1.5*TILE_SIZE);
			 }
		}
		//puts the calculated X and Y into an array to return
		int[] dumpingWaypoint = new int[2];
		dumpingWaypoint[0] = dumpingSpotX;
		dumpingWaypoint[1] = dumpingSpotY;
		return dumpingWaypoint;

	}
}

