package ca.mcgill.ecse211.navigators;
import java.util.function.IntPredicate;

import ca.mcgill.ecse211.localizers.Localization;
import ca.mcgill.ecse211.odometer.*;

public class Navigator {
	
	private static final double TILE_SIZE = 30.48;
	private static final double VERT_SENSOR_OFFSET = 5; 
	
	private static MovementController movementController;
	private static Odometer odo;
	private static Localization localizer;
	
	private static int[] TLL = new int[2];
	private static int[] TUR = new int[2];
	
	
	
	private static int bridgeTileLength;
	private static int SC;

	public Navigator(MovementController movementController, Odometer odo, Localization localizer, int[] TLL, int TUR[], int SC) {
		this.movementController = movementController; 
		this.odo = odo;
		this.TLL[0] = TLL[0];
		this.TLL[1] = TLL[1];
		this.TUR[0] = TUR[0];
		this.TUR[1] = TUR[1];
		this.localizer = localizer;
		this.SC = SC;
		bridgeTileLength = (Math.abs(TLL[0]-TUR[0]) > Math.abs(TLL[1]-TUR[1])) ? Math.abs(TLL[0]-TUR[0]) : Math.abs(TLL[1]-TUR[1]); //Calculate birdge length from coordinates
		
	}
	
	/**
	 * Travel to the tunnel from starting point
	 * 
	 */
	public void travelToFTunnel() {
		int tCornerIntX = 0, tCornerIntY = 0;
		boolean turnFirLoc = true, turnSecLoc = true;
		if(SC == 3) {
			if(TUR[1] == 9) {
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
			if(TUR[0] == 15) {
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
		movementController.travelTo((TLL[0] + tCornerIntX)*TILE_SIZE, (TLL[1] + tCornerIntY)*TILE_SIZE, false); //Travel to the tile intersection diagonally opposite to LL tunnel corner
		movementController.turnTo(90); 
		
		localizer.quickLocalization(); //Correction in y
		movementController.driveDistance(-VERT_SENSOR_OFFSET); 
		movementController.rotateAngle(90, turnFirLoc);
		movementController.driveDistance(-5);
		
		localizer.quickLocalization(); //Correction in x
		movementController.driveDistance((TILE_SIZE/2)-VERT_SENSOR_OFFSET); //Position itself in the middle of the tile in front and facing the tunnel
		movementController.rotateAngle(90, turnSecLoc);
		
	}
	
	/**
	 * Travel across the tunnel
	 */
	public void throughTunnel() {
		int posCorX = 0, posCorY = 0;
		int thetaCor = 0;
		boolean turnLoc = true;
		if(SC == 3) {
			if(TUR[1] == 9) {
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
		movementController.driveDistance((bridgeTileLength+2)*TILE_SIZE - VERT_SENSOR_OFFSET); //Cross tunnel
		
		localizer.quickLocalization(); //Correct angle and x position 
		movementController.driveDistance(-VERT_SENSOR_OFFSET); 
		movementController.rotateAngle(90, turnLoc);
		
		localizer.quickLocalization(); //Correct y position
		movementController.driveDistance(-VERT_SENSOR_OFFSET);
		
		odo.setXYT((TUR[0] + posCorX)*TILE_SIZE, (TUR[1] + posCorY)*TILE_SIZE, thetaCor);
	}
	
	/**
	 * Travel back to the tunnel from the search zone
	 */
	public void travelToBTunnel() {
		
		movementController.travelTo((TUR[0]+1)*TILE_SIZE, odo.getXYT()[1], false);
		localizer.quickLocalization();
		movementController.driveDistance(-VERT_SENSOR_OFFSET);
		odo.setTheta(180);
		movementController.travelTo(odo.getXYT()[0], TUR[1]*TILE_SIZE, false);
		
	}
	
	public void goToStartingTile() {
		if(SC==0) {
			
			
		}
	}
}
