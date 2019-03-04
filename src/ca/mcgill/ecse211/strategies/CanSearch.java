package ca.mcgill.ecse211.strategies;

import java.io.ObjectOutputStream.PutField;
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
	
	private Odometer odo;
	private MovementController movCon;
	private MedianDistanceSensor  USData;
	private float[] PLL,PUR,PTUN, PISL_LL, PISL_UR;
	private int startCorner;
	private float TILE_LENGTH;
	private float deltaX, deltaY;
	private float scanRadius = TILE_LENGTH*3;
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
		
		deltaX = PISL_UR[0] - PISL_LL[0];
		deltaY = PISL_UR[1] - PISL_LL[1];
		
		
		if (startCorner == 1) {

			//calculate positions as float[]
			float[] firstPos = {PISL_UR[0]-TILE_LENGTH/2 , PISL_LL[1]+TILE_LENGTH/2};
			scanningPoints.add(firstPos);
			
			for(int i=1; i <3; i++) {
				
				nextPos[0] = PISL_UR[0] - i*(deltaX/scanRadius)*TILE_LENGTH;
				
				for(int j=0; j<3; j++) {
					
					if (j == 0) {
						nextPos[1] = PISL_LL[1] + TILE_LENGTH/2;
					}
					
					nextPos[1] = PISL_LL[1] + j*(deltaY/scanRadius)*TILE_LENGTH;
					
					//now that nextPos[0] and nextPos[1] are defined, add them to the list
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
		while(USData.getFilteredDistance() > scanRadius) {
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
		
		
	}
	
	public List<Double[]> getCanLocations(double[] robotPos, double[] searchLL, double[] searchUR) {
	    
        return null;
	}
	
	/*
	 * approaches the position where a potential can was detected,
	 * performs a second scan, 
	 * takes position for grabbing
	 */
	public void secondaryScar() {
		
	}
	
	

}
