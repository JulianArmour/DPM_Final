package ca.mcgill.ecse211.navigators;
import java.util.function.IntPredicate;
import java.util.function.ToDoubleBiFunction;

import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.odometer.*;

public class Navigator {
	
	private static final double TILE_SIZE = 30.48;
	private static final double VERT_SENSOR_OFFSET = 5; 
	
	private static MovementController move;
	private static Odometer odo;
	private static Localization localizer;
	private static int TLLX, TLLY, TURX, TURY, STZLLX, STZLLY, STZURX, STZURY, SEZLLX, SEZLLY, SEZURX, SEZURY;
	
	
	
	private static int dumpingSpotX, dumpingSpotY;
	private static int bridgeTileLength;
	private static int SC;

	public Navigator(MovementController move, Odometer odo, Localization localizer, int[] TLL, int TUR[], int STZLL[], int STZUR[], int SC, int SEZLL[], int SEZUR[]) {
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
		SEZLLX = SEZLL[0]; //Set search zone coordinates
		SEZLLY = SEZLL[1];
		SEZURX = SEZUR[0];
		SEZURY = SEZUR[1];
		this.localizer = localizer;
		this.SC = SC;
		bridgeTileLength = (Math.abs(TLLX-TURX) > Math.abs(TLLY-TURY)) ? Math.abs(TLLX-TURX) : Math.abs(TLLY-TURY); //Calculate bridge length from coordinates

		
	}
	
	/**
	 * Travel to the tunnel from starting point
	 * 
	 */
	public void travelToFTunnel() {

		boolean OP1 = true;
		int turnToTunnel = 0;
		double tunnelTilePosYOP2 = 0, tunnelTilePosXOP2 = 0, tunnelTilePosXOP1 = 0, tunnelTilePosYOP1 = 0;
		switch(SC) {
		case 0:
			if(TURX > STZURX) {
				OP1 = true;
				turnToTunnel = 0;
				tunnelTilePosXOP1 = TLLX-1;
				tunnelTilePosYOP1 = TLLY + 0.5;
			} else {
				OP1 = false;
				tunnelTilePosYOP2 = TLLY - 1;
				tunnelTilePosXOP2 = TURX - 0.5;
				turnToTunnel = 90;
			}
			break;
		case 1:
			if(TLLX < STZLLX) {
				OP1 = true;
				tunnelTilePosXOP1 = TURX + 1;
				tunnelTilePosYOP1 = TURY - 0.5;
				turnToTunnel = 0;
			} else { 
				OP1 = false;
				tunnelTilePosXOP2 = TURX - 0.5;
				tunnelTilePosYOP2 = TLLY - 1;
				turnToTunnel = 90;
			}
			break;
		case 2:
			if(TLLX < STZLLX) {
				OP1 = true;
				tunnelTilePosXOP1 = TURX + 1;
				tunnelTilePosYOP1 = TURY - 0.5;
				turnToTunnel = 0;
			} else {
				OP1 = false;
				tunnelTilePosYOP2 = TURY + 1;
				tunnelTilePosXOP2 = TURX - 0.5;
				turnToTunnel = 270;
			}
			break;
		case 3:
			if(TURX > STZURX) {
				OP1 = true;
				turnToTunnel = 0;
				tunnelTilePosXOP1 = TLLX-1;
				tunnelTilePosYOP1 = TLLY + 0.5;
			} else {
				OP1 = false;
				tunnelTilePosYOP2 = TURY + 1;
				tunnelTilePosXOP2 = TURX - 0.5;
				turnToTunnel = 270;
			}
			break;
			default:
				break;
		}
		if(OP1) {
			move.travelTo(tunnelTilePosXOP1*TILE_SIZE, odo.getXYT()[1], false); //Move to the x position on the grid line before the tunnel
			localizer.quickThetaCorrection();
			odo.setTheta(move.roundAngle());
			move.driveDistance(-VERT_SENSOR_OFFSET);
			
			move.travelTo(odo.getXYT()[0], tunnelTilePosYOP1 * TILE_SIZE, false); //Move the to y position on the grid line in the middle of the tile in front of the tunnel
			move.turnTo(turnToTunnel);
			localizer.quickThetaCorrection(); //Make sure we are well facing the tunnel
			odo.setTheta(move.roundAngle());
			
		}
		else {
			move.travelTo(odo.getXYT()[0], tunnelTilePosYOP2*TILE_SIZE, false);
			localizer.quickThetaCorrection();
			odo.setTheta(move.roundAngle());
			move.driveDistance(-VERT_SENSOR_OFFSET);
			
			move.travelTo(tunnelTilePosXOP2*TILE_SIZE, odo.getXYT()[1], false);
			move.turnTo(turnToTunnel);;
			localizer.quickThetaCorrection();
			odo.setTheta(move.roundAngle());
		}
	}
	
	/**
	 * Travel across the tunnel
	 */
	public void throughTunnel(boolean direction) {
		int posCorX = 0, posCorY = 0;
		int thetaCor = 0;
		boolean turnLoc = true;
		if (direction) { //If robot is going from starting zone to search zone, these are the parameters to use
			switch(SC) {
			case 0:
				if(TURX > STZURX) {
					if(TURY == SEZURY) {
						turnLoc = true;
						posCorX = TURX + 1;
						posCorY = TURY - 1;
					} else {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					}
				} else {
					if(TURX == SEZURX) {
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
					if(TURY == SEZURY) {
						turnLoc = false;
						posCorX = TLLX - 1;
						posCorY = TLLY;
					} else {
						turnLoc = true;
						posCorX = TLLX - 1;
						posCorY = TLLY + 1;
					}
				} else { 
					if(TLLX == SEZLLX) {
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
					if(TLLY == SEZLLY) {
						turnLoc = true; 
						posCorX = TLLX - 1;
						posCorY = TLLY + 1;
					} else {
						turnLoc = false;
						posCorX = TLLX - 1;
						posCorY = TLLY;
					}
				} else {
					if(TLLX == SEZLLX) {
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
					if(TURY == SEZURY) {
						turnLoc = true;
						posCorX = TURX + 1;
						posCorY = TURY - 1;
					} else {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					}
				} else {
					if(TLLX == SEZLLX) {
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
					if(TLLY == SEZLLY) {
						turnLoc = false;
						posCorX = TURX + 1;
						posCorY = TURY;
					} else {
						turnLoc = true;
						posCorX = TURX + 1;
						posCorY = TLLY - 1;
					}
				} else { 
					if(TURX == SEZURX) {
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
	}
	
	/**
	 * Travel back to the tunnel from the search zone
	 */
	public void travelToBTunnel() {
		 //havent checked this yet after tunnel finder completion

		move.travelTo((TURX+1)*TILE_SIZE, odo.getXYT()[1], false);
		localizer.quickThetaCorrection();
		move.driveDistance(-VERT_SENSOR_OFFSET);
		odo.setTheta(move.roundAngle());
		move.travelTo(odo.getXYT()[0], TURY*TILE_SIZE, false);
		localizer.quickThetaCorrection();
		move.driveDistance(-VERT_SENSOR_OFFSET);
		odo.setTheta(move.roundAngle());
		move.turnTo(180);
		
		move.travelTo((TURX+1)*TILE_SIZE, odo.getXYT()[1], false);
		localizer.quickLocalization();
		move.driveDistance(-VERT_SENSOR_OFFSET);
		odo.setTheta(180);
		move.travelTo(odo.getXYT()[0], TURY*TILE_SIZE, false);
		
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

