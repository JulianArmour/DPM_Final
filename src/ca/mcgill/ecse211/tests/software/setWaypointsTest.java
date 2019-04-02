package ca.mcgill.ecse211.tests.software;

import java.util.Iterator;
import java.util.LinkedList;

import ca.mcgill.ecse211.Main;


public class setWaypointsTest {

	protected static final long CAN_SCAN_PERIOD = 100;

	private static float[] PLL = new float[2];
	private static float[] PUR = new float[2];

	private static float[] PISL_LL = new float[2];
	private static float[] PISL_UR = new float[2];
	
	private static int[] SZ_LL = new int[2];
	private static int[] SZ_UR = new int [2];


	private static float TILE_LENGTH = (float) Main.TILE_SIZE;
	private static float deltaX;
	private static float deltaY;
	private static float SCAN_RADIUS = TILE_LENGTH*2;

	private static LinkedList<float[]> scanningPoints = new LinkedList<float[]>();

	public static void main(String[] args) {
		
		pointsForCornerZeroAndThree();
		/*
		if (STATE_OF_TEST == 0) {
			pointsForCornerOneAndTwo();
		}
		else pointsForCornerZeroAndThree();
		*/
		
		
	}
	
	public static void pointsForCornerOneAndTwo() {


		PLL[0] = 8*TILE_LENGTH;
		PLL[1] = 8*TILE_LENGTH;
		PUR[0] = 14*TILE_LENGTH;
		PUR[1] = 11*TILE_LENGTH;
		
		PISL_LL[0] = 5*TILE_LENGTH;
		PISL_LL[1] = 7*TILE_LENGTH;
		PISL_UR[0] = 15*TILE_LENGTH;
		PISL_UR[1] = 11*TILE_LENGTH;
		
		

		deltaX = PISL_UR[0] - PLL[0];
		deltaY = PISL_UR[1] - PISL_LL[1];


		for(int i=0; i <3; i++) {
			for(int j=0; j<3; j++) {
				float[] nextPos = new float[2];

				if(i==0 && j ==0) {
				
					nextPos[0] = PISL_UR[0]-TILE_LENGTH/2;
					nextPos[1] = PISL_LL[1] + TILE_LENGTH/2;
				}

				else if(i==0) {
					
					nextPos[0] = PISL_UR[0]-TILE_LENGTH/2;
					nextPos[1] = PISL_LL[1] + j*(deltaY/SCAN_RADIUS)*TILE_LENGTH;
				}

				else if (j == 0) {
					
					nextPos[1] = PISL_LL[1] + TILE_LENGTH/2;
					nextPos[0] = PISL_UR[0] - i*(deltaX/SCAN_RADIUS)*TILE_LENGTH;
				}

				else {
					
					nextPos[0] = PISL_UR[0] - i*(deltaX/SCAN_RADIUS)*TILE_LENGTH;

					nextPos[1] = PISL_LL[1] + j*(deltaY/SCAN_RADIUS)*TILE_LENGTH;
				}
				scanningPoints.add(nextPos);
			}
		}
		Iterator<float[]> iterator = scanningPoints.iterator();
		while (iterator.hasNext()) {
			float[] point = (float[]) iterator.next();
			System.out.println("X =" + point[0]/TILE_LENGTH + "Y = " + point[1]/TILE_LENGTH );

		}
	}

	public static void pointsForCornerZeroAndThree() {
		
		
		SZ_LL[0] = 6;  
		SZ_LL[1] = 5;
		SZ_UR[0] = 9;
		SZ_UR[1] = 8;
		
		int startCorner = 2;
		
		  float deltaX = (SZ_UR[0] - SZ_LL[0])*TILE_LENGTH;
	        float deltaY = (SZ_UR[1] - SZ_LL[1])*TILE_LENGTH;
	        
		System.out.println(deltaX/TILE_LENGTH);
		System.out.println(deltaY/TILE_LENGTH);
		

		int nYPoints = Math.round(deltaY / SCAN_RADIUS);
        int nXPoints = Math.round(deltaX / SCAN_RADIUS);

        for (int i = 0; i < nXPoints; i++) {
            for (int j = 0; j < nYPoints; j++) {
                float[] nextPos = new float[2];

                nextPos[1] = SZ_LL[1]*TILE_LENGTH + j * SCAN_RADIUS;
                if (startCorner == 1 || startCorner == 2) {
                    nextPos[0] = SZ_LL[0]*TILE_LENGTH + (nXPoints - i)*SCAN_RADIUS;
                    // set the dumping points depending on searchpoints
                   
                } else {// startCorner = 0 or 3
                    nextPos[0] = SZ_LL[0]*TILE_LENGTH + i*SCAN_RADIUS;
                    // set the dumping points depending on searchpoints
                    
                }
                scanningPoints.add(nextPos);
            }
        }

		
		Iterator<float[]> iterator = scanningPoints.iterator();
		while (iterator.hasNext()) {
			float[] point = (float[]) iterator.next();
			System.out.println("X = " + point[0]/TILE_LENGTH + "Y = " + point[1]/TILE_LENGTH );

		}
		
	}

}
