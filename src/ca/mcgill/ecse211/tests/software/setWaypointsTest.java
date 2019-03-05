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


	private static float TILE_LENGTH = (float) Main.TILE_SIZE;
	private static float deltaX;
	private static float deltaY;
	private static float SCAN_RADIUS = TILE_LENGTH*3;

	private static LinkedList<float[]> scanningPoints = new LinkedList<float[]>();

	private static int STATE_OF_TEST = 1;
	
	public static void main(String[] args) {
		if (STATE_OF_TEST == 0) {
			pointsForCornerOneAndTwo();
		}
		else pointsForCornerZeroAndThree();
		
		
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
		
		PLL[0] = 8*TILE_LENGTH;
		PLL[1] = 8*TILE_LENGTH;
		PUR[0] = 14*TILE_LENGTH;
		PUR[1] = 11*TILE_LENGTH;
		PISL_LL[0] = 5*TILE_LENGTH;
		PISL_LL[1] = 7*TILE_LENGTH;
		PISL_UR[0] = 15*TILE_LENGTH;
		PISL_UR[1] = 11*TILE_LENGTH;

		deltaX = PUR[0] - PISL_LL[0];
		deltaY = PUR[1] - PISL_LL[1];
		System.out.println(deltaX);
		System.out.println(deltaY);
		


		for(int i=0; i <3; i++) {
			for(int j=0; j<3; j++) {
				float[] nextPos = new float[2];

				if(i==0 && j ==0) {
				
					nextPos[0] = PISL_LL[0]+TILE_LENGTH/2;
					nextPos[1] = PISL_LL[1]+TILE_LENGTH/2;
				}
				if(i==0) {
					
					nextPos[0]=PISL_LL[0] + TILE_LENGTH/2;
					nextPos[1] = PISL_LL[1] + j*(deltaY/SCAN_RADIUS)*TILE_LENGTH;
				}
				if(j==0) {
					
					nextPos[1] = PISL_LL[1]+TILE_LENGTH/2;
					nextPos[0] = PISL_LL[0]+ i*(deltaX/SCAN_RADIUS)*TILE_LENGTH;
				}
				else {
					
					nextPos[0]=PISL_LL[0]+ i*(deltaX/SCAN_RADIUS)*TILE_LENGTH;
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

}
