package ca.mcgill.ecse211.navigators;
import ca.mcgill.ecse211.StartingCorner;
import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.odometer.*;

public class TunnelNavigator {
	
	private static final double TILE_SIZE = 30.48;
	private static final double VERT_SENSOR_OFFSET = 5; 
	
	private static MovementController move;
	private static Odometer odo;
	private static Localization localizer;
	private static int TLLX, TLLY, TURX, TURY;
	private static int bridgeTileLength;
	private static StartingCorner SC;

	public TunnelNavigator(MovementController move, Odometer odo, Localization localizer, int TLLX, int TLLY, int TURX, int TURY, StartingCorner SC) {
		this.move = move;
		this.odo = odo;
		this.TLLX = TLLX;
		this.TLLY = TLLY;
		this.TURX = TURX;
		this.TURY = TURY;
		this.localizer = localizer;
		this.SC = SC;
		bridgeTileLength = (Math.abs(TLLX-TURX) > Math.abs(TLLY-TURY)) ? Math.abs(TLLX-TURX) : Math.abs(TLLY-TURY); //Calculate birdge length from coordinates
		
	}
	
	public void travelToFTunnel() {
		int tCornerIntX = 0, tCornerIntY = 0;
		boolean turnFirLoc = true, turnSecLoc = true;
		if(SC == StartingCorner.RED_CORNER) {
			if(TURY == 9) {
				tCornerIntX = -1;
				tCornerIntY = 0;
				turnFirLoc = false;
				turnSecLoc = true;
			}else {
				tCornerIntX = -1;
				tCornerIntY = 1;
				turnFirLoc = true;
				turnSecLoc = false;
			}
			
		}
		else {
			if(TURX == 15) {
				tCornerIntX = 0;
				tCornerIntY = -1;
				turnFirLoc = true;
				turnSecLoc = false;
			}else {
				tCornerIntX = 1;
				tCornerIntY = -1;
				turnFirLoc = false;
				turnSecLoc = true;
			}
		}
		move.travelTo((TLLX + tCornerIntX)*TILE_SIZE, (TLLY + tCornerIntY)*TILE_SIZE, false); //Travel to the tile intersection diagonally opposite to LL tunnel corner
		move.turnTo(90); 
		
		localizer.quickLocalization(); //Correction in y
		move.driveDistance(-VERT_SENSOR_OFFSET); 
		move.rotateAngle(90, turnFirLoc);
		move.driveDistance(-5);
		
		localizer.quickLocalization(); //Correction in x
		move.driveDistance((TILE_SIZE/2)-VERT_SENSOR_OFFSET); //Position itself in the middle of the tile in front and facing the tunnel
		move.rotateAngle(90, turnSecLoc);
		
	}
	
	public void throughTunnel() {
		int posCorX = 0, posCorY = 0;
		int thetaCor = 0;
		boolean turnLoc = true;
		if(SC == StartingCorner.RED_CORNER) {
			if(TURY == 9) {
				posCorX = 1;
				posCorY = -1;
				turnLoc = true;
				thetaCor = 90;
			}else {
				posCorX = 1;
				posCorY = 0;
				turnLoc= false;
				thetaCor = 270;
			}
			
		}
		else {
				posCorX = -1;
				posCorY = +1;
				turnLoc = false;
				thetaCor = 0;
		}
		localizer.quickLocalization(); //Make sure robot is straight
		move.driveDistance((bridgeTileLength+2)*TILE_SIZE - VERT_SENSOR_OFFSET); //Cross tunnel
		
		localizer.quickLocalization(); //Correct angle and x position 
		move.driveDistance(-VERT_SENSOR_OFFSET); 
		move.rotateAngle(90, turnLoc);
		
		localizer.quickLocalization(); //Correct y position
		move.driveDistance(-VERT_SENSOR_OFFSET);
		
		odo.setXYT((TURX + posCorX)*TILE_SIZE, (TURY + posCorY)*TILE_SIZE, thetaCor);
		
	}
	
	public void travelToBTunnel() {
		
		move.travelTo((TURX+1)*TILE_SIZE, odo.getXYT()[1], false);
		localizer.quickLocalization();
		move.driveDistance(-VERT_SENSOR_OFFSET);
		odo.setTheta(180);
		move.travelTo(odo.getXYT()[0], TURY*TILE_SIZE, false);
		
	}
}
