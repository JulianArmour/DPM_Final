package ca.mcgill.ecse211.navigators;
import java.util.function.IntPredicate;

import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.odometer.*;

public class Navigator {
	
	private static final double TILE_SIZE = 30.48;
	private static final double VERT_SENSOR_OFFSET = 5; 
	
	private static MovementController move;
	private static Odometer odo;
	private static Localization localizer;
	private static int TLLX, TLLY, TURX, TURY, ZLLX, ZLLY, ZURX, ZURY;

	
	
	
	private static int bridgeTileLength;
	private static int SC;

	public Navigator(MovementController move, Odometer odo, Localization localizer, int[] TLL, int TUR[], int ZLL[], int ZUR[], int SC) {
		this.move = move; 
		this.odo = odo;

		TLLX = TLL[0];
		TLLY = TLL[1];
		TURX = TUR[0];
		TURY = TUR[1];
		ZLLX = ZLL[0];
		ZLLY = ZLL[1];
		ZURX = ZUR[0];
		ZURY = ZUR[1];
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
			if(TURX > ZURX) {
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
			if(TLLX < ZLLX) {
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
			if(TLLX < ZLLX) {
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
			if(TURX > ZURX) {
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
	public void throughTunnel() {
		int posCorX = 0, posCorY = 0;
		int thetaCor = 0;
		boolean turnLoc = true;
		if(SC == 3) {
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
		
		localizer.quickThetaCorrection(); //Make sure robot is straight
		move.driveDistance((bridgeTileLength+2)*TILE_SIZE - VERT_SENSOR_OFFSET); //Cross tunnel

		localizer.quickLocalization(); //Make sure robot is straight
		move.driveDistance((bridgeTileLength+2)*TILE_SIZE - VERT_SENSOR_OFFSET); //Cross tunnel

		localizer.quickThetaCorrection(); //Correct angle and x position 
		move.driveDistance(-VERT_SENSOR_OFFSET); 
		move.rotateAngle(90, turnLoc);

		localizer.quickLocalization(); //Correct angle and x position 
		move.driveDistance(-VERT_SENSOR_OFFSET); 
		move.rotateAngle(90, turnLoc);

		localizer.quickThetaCorrection(); //Correct y position
		move.driveDistance(-VERT_SENSOR_OFFSET);

		localizer.quickLocalization(); //Correct y position
		move.driveDistance(-VERT_SENSOR_OFFSET);

		
		odo.setXYT((TURX + posCorX)*TILE_SIZE, (TURY + posCorY)*TILE_SIZE, thetaCor);
	}
	
	/**
	 * Travel back to the tunnel from the search zone
	 */
	public void travelToBTunnel() {
		

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

		if(SC==0) {


		}
	}
}

